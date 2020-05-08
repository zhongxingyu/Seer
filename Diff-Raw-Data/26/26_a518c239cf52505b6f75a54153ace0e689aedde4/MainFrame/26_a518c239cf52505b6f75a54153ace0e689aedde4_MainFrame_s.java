 package emcshop.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.io.IOException;
 import java.net.URL;
 import java.sql.SQLException;
 import java.text.NumberFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSeparator;
 import javax.swing.JTable;
 import javax.swing.KeyStroke;
 import javax.swing.SwingConstants;
 import javax.swing.table.AbstractTableModel;
 import javax.swing.table.TableCellRenderer;
 
import net.miginfocom.layout.LC;
 import net.miginfocom.swing.MigLayout;
 
 import com.michaelbaranov.microba.calendar.DatePicker;
 
 import emcshop.ShopTransaction;
 import emcshop.TransactionPuller;
 import emcshop.db.DbDao;
 import emcshop.db.ItemGroup;
 import emcshop.util.Settings;
 import emcshop.util.TimeUtils;
 
 @SuppressWarnings("serial")
 public class MainFrame extends JFrame implements WindowListener {
 	private static final Logger logger = Logger.getLogger(MainFrame.class.getName());
 	private JButton update;
 	private JLabel lastUpdateDate;
 	private DatePicker toDatePicker;
 	private DatePicker fromDatePicker;
 	private JComboBox groupBy;
 	private JButton show;
 	private JPanel tablePanel;
 
 	private final DbDao dao;
 	private Settings settings;
 
 	public MainFrame(Settings settings, DbDao dao) {
 		this.dao = dao;
 		this.settings = settings;
 
 		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
 
 		createMenu();
 		createWidgets();
 		layoutWidgets();
 		setSize(settings.getWindowWidth(), settings.getWindowHeight());
 
 		addWindowListener(this);
 	}
 
 	private void createMenu() {
 		//http://docs.oracle.com/javase/tutorial/uiswing/components/menu.html
 
 		JMenuBar menuBar = new JMenuBar();
 
 		{
 			JMenu file = new JMenu("File");
 			file.setMnemonic(KeyEvent.VK_F);
 
 			JMenuItem exit = new JMenuItem("Exit");
 			exit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
 			exit.addActionListener(new ActionListener() {
 				@Override
 				public void actionPerformed(ActionEvent arg0) {
 					windowClosed(null);
 				}
 			});
 			file.add(exit);
 
 			menuBar.add(file);
 		}
 
 		{
 			JMenu tools = new JMenu("Tools");
 			tools.setMnemonic(KeyEvent.VK_T);
 
 			JMenuItem export = new JMenuItem("Export to CSV");
 			export.setEnabled(false);
 			tools.add(export);
 
 			tools.addSeparator();
 
 			JMenuItem settings = new JMenuItem("Settings");
 			tools.add(settings);
 
 			menuBar.add(tools);
 		}
 
 		{
 			JMenu help = new JMenu("Help");
 			help.setMnemonic(KeyEvent.VK_H);
 
 			JMenuItem about = new JMenuItem("About");
 			help.add(about);
 
 			menuBar.add(help);
 		}
 
 		setJMenuBar(menuBar);
 	}
 
 	private void createWidgets() {
 		update = new JButton("Update Transactions");
 		update.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				try {
 					TransactionPuller puller = new TransactionPuller(settings.getCookies());
 					ShopTransaction latest = dao.getLatestTransaction();
 					if (latest == null) {
 						int answer = JOptionPane.showConfirmDialog(MainFrame.this, "This is the first time you're updating your transactions.  If you have a large transaction history, it is highly recommended that you disable move perms on your res before starting the update.  If any transactions occur during the update, it will skew the results.\n\n/res set move false\n\nIt could take up to 20 minutes to parse your entire transaction history.\n\nAre you ready to perform the update?", "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
 						if (answer == JOptionPane.NO_OPTION) {
 							return;
 						}
 					} else {
 						puller.setStopAtDate(latest.getTs());
 					}
 
 					UpdateDialog w = new UpdateDialog(MainFrame.this, puller, dao);
 					w.setVisible(true);
 				} catch (SQLException e) {
 					ErrorDialog.show(MainFrame.this, "An error occurred connecting to the database.", e);
 				}
 			}
 		});
 
 		lastUpdateDate = new JLabel();
 		Date date = settings.getLastUpdated();
 		lastUpdateDate.setText((date == null) ? "-" : date.toString());
 
		toDatePicker = new DatePicker();
		toDatePicker.setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));
		toDatePicker.setShowNoneButton(true);
		toDatePicker.setShowTodayButton(true);

 		fromDatePicker = new DatePicker();
 		fromDatePicker.setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));
 		fromDatePicker.setShowNoneButton(true);
 		fromDatePicker.setShowTodayButton(true);
 
 		groupBy = new JComboBox();
 		groupBy.addItem("Item");
 		groupBy.addItem("Player");
 
 		show = new JButton("Show Transactions");
 		show.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				tablePanel.removeAll();
 				tablePanel.validate();
 
 				final LoadingDialog loading = new LoadingDialog(MainFrame.this, "Loading", "Querying . . .");
 				loading.setVisible(true);
 				Thread t = new Thread() {
 					@Override
 					public void run() {
 						try {
							Map<String, ItemGroup> itemGroups = dao.getItemGroups(fromDatePicker.getDate(), toDatePicker.getDate());
 							final List<ItemGroup> itemGroupsList = new ArrayList<ItemGroup>(itemGroups.values());
 							Collections.sort(itemGroupsList, new Comparator<ItemGroup>() {
 								@Override
 								public int compare(ItemGroup a, ItemGroup b) {
 									return a.getItem().compareToIgnoreCase(b.getItem());
 								}
 							});
 
 							JTable table = new JTable();
 							table.setColumnSelectionAllowed(false);
 							table.setRowSelectionAllowed(false);
 							table.setCellSelectionEnabled(false);
 							table.setRowHeight(24);
 							table.setModel(new AbstractTableModel() {
 								String[] columns = new String[] { "Item Name", "Sold", "Bought", "Net" };
 
 								@Override
 								public int getColumnCount() {
 									return columns.length;
 								}
 
 								@Override
 								public String getColumnName(int col) {
 									return columns[col];
 								}
 
 								@Override
 								public int getRowCount() {
 									return itemGroupsList.size();
 								}
 
 								@Override
 								public Object getValueAt(int row, int col) {
 									return itemGroupsList.get(row);
 								}
 
 								public Class<?> getColumnClass(int c) {
 									return ItemGroup.class;
 								}
 
 								@Override
 								public boolean isCellEditable(int row, int col) {
 									return false;
 								}
 							});
 							table.setDefaultRenderer(ItemGroup.class, new ItemGroupRenderer());
 
 							JScrollPane scrollPane = new JScrollPane(table);
 							table.setFillsViewportHeight(true);
 							tablePanel.add(scrollPane, "growx, growy, w 100%, h 100%");
 							tablePanel.validate();
 						} catch (SQLException e) {
 							ErrorDialog.show(MainFrame.this, "An error occurred querying the database.", e);
 						} finally {
 							loading.dispose();
 						}
 					}
 				};
 				t.start();
 			}
 		});
 	}
 
 	private static class ItemGroupRenderer implements TableCellRenderer {
 		@Override
 		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
 			NumberFormat nf = NumberFormat.getNumberInstance();
 			JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
 
 			ItemGroup group = (ItemGroup) value;
 			switch (col) {
 			case 0:
 				//TODO add the rest of the icons
 				ImageIcon img = getItemImage(group.getItem());
 				panel.add(new JLabel(group.getItem(), img, SwingConstants.LEFT));
 				break;
 			case 1:
 				if (group.getSoldQuantity() == 0) {
 					panel.add(new JLabel("-"));
 				} else {
 					panel.add(new JLabel(nf.format(group.getSoldQuantity()) + " / " + nf.format(group.getSoldAmount()) + "r"));
 				}
 				break;
 			case 2:
 				if (group.getBoughtQuantity() == 0) {
 					panel.add(new JLabel("-"));
 				} else {
 					panel.add(new JLabel(nf.format(group.getBoughtQuantity()) + " / " + nf.format(group.getBoughtAmount()) + "r"));
 				}
 				break;
 			case 3:
 				JLabel qtyLbl;
 				if (group.getNetQuantity() < 0) {
 					qtyLbl = new JLabel("<html><font color=red>" + nf.format(group.getNetQuantity()) + "</font></html>");
 				} else {
 					qtyLbl = new JLabel("<html><font color=green>+" + nf.format(group.getNetQuantity()) + "</font></html>");
 				}
 				panel.add(qtyLbl);
 
 				panel.add(new JLabel("/"));
 
 				JLabel amtLbl;
 				if (group.getNetAmount() < 0) {
 					amtLbl = new JLabel("<html><font color=red>" + nf.format(group.getNetAmount()) + "r</font></html>");
 				} else {
 					amtLbl = new JLabel("<html><font color=green>+" + nf.format(group.getNetAmount()) + "r</font></html>");
 				}
 				panel.add(amtLbl);
 				break;
 			}
 
 			return panel;
 		}
 
 		private ImageIcon getItemImage(String item) {
 			item = item.toLowerCase().replace(" ", "_");
 			URL url = getClass().getResource("items/" + item + ".png");
 			if (url == null) {
 				url = getClass().getResource("items/_empty.png");
 			}
 			ImageIcon img = new ImageIcon(url);
 			return new ImageIcon(img.getImage().getScaledInstance(16, 16, java.awt.Image.SCALE_SMOOTH));
 		}
 	}
 
 	void updateSuccessful(Date started, long time, int transactionCount) {
 		long components[] = TimeUtils.parseTimeComponents(time);
 		String message;
 		if (transactionCount == 0) {
 			message = "No new transactions found.";
 		} else {
 			StringBuilder sb = new StringBuilder();
 			sb.append("Update complete.\n");
 			sb.append(transactionCount).append(" transactions added in ");
 			if (components[2] > 0) {
 				sb.append(components[2]).append(" minutes and ");
 			}
 			sb.append(components[1]).append(" seconds.");
 			message = sb.toString();
 		}
 		JOptionPane.showMessageDialog(this, message, "Update complete", JOptionPane.INFORMATION_MESSAGE);
 
 		settings.setLastUpdated(started);
 		lastUpdateDate.setText(started.toString());
 	}
 
 	private void layoutWidgets() {
 		setLayout(new BorderLayout());
 		add(createLeftPanel(), BorderLayout.WEST);
 		add(createRightPanel(), BorderLayout.CENTER);
 	}
 
 	private JPanel createLeftPanel() {
 		JPanel p = new JPanel(new MigLayout());
 
 		p.add(update, "align center, wrap");
 
 		JPanel p2 = new JPanel(new FlowLayout());
 		p2.add(new JLabel("Last updated:"));
 		p2.add(lastUpdateDate);
 		p.add(p2, "wrap");
 
 		p.add(new JSeparator(), "w 200!, align center, wrap");
 
 		p2 = new JPanel(new MigLayout());
 
 		p2.add(new JLabel("Start:"), "align right");
 		p2.add(fromDatePicker, "wrap");
 
 		p2.add(new JLabel("End:"), "align right");
 		p2.add(toDatePicker, "wrap");
 
 		p2.add(new JLabel("Group By:"), "align right");
 		p2.add(groupBy, "wrap");
 
 		p.add(p2, "wrap");
 		p.add(show, "align center");
 
 		return p;
 	}
 
 	private JPanel createRightPanel() {
 		tablePanel = new JPanel();
 		tablePanel.setLayout(new MigLayout("width 100%, height 100%"));
 
 		JLabel label = new JLabel("<html><i>no results</i></html>");
 		tablePanel.add(label, "align center");
 
 		return tablePanel;
 	}
 
 	///////////////////////////////////
 
 	@Override
 	public void windowActivated(WindowEvent arg0) {
 		//do nothing
 	}
 
 	@Override
 	public void windowClosed(WindowEvent arg0) {
 		settings.setWindowWidth(getWidth());
 		settings.setWindowHeight(getHeight());
 		try {
 			settings.save();
 		} catch (IOException e) {
 			logger.log(Level.WARNING, "Problem persisting settings file.", e);
 		}
 
 		System.exit(0);
 	}
 
 	@Override
 	public void windowClosing(WindowEvent arg0) {
 		//do nothing
 	}
 
 	@Override
 	public void windowDeactivated(WindowEvent arg0) {
 		//do nothing
 	}
 
 	@Override
 	public void windowDeiconified(WindowEvent arg0) {
 		//do nothing
 	}
 
 	@Override
 	public void windowIconified(WindowEvent arg0) {
 		//do nothing
 	}
 
 	@Override
 	public void windowOpened(WindowEvent arg0) {
 		//do nothing
 	}
 }
