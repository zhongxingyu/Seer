 package emcshop.gui;
 
 import java.awt.BorderLayout;
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.io.IOException;
 import java.sql.SQLException;
 import java.util.Date;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JSeparator;
 import javax.swing.JTextField;
 import javax.swing.KeyStroke;
 
 import net.miginfocom.swing.MigLayout;
 import emcshop.ShopTransaction;
 import emcshop.TransactionPuller;
 import emcshop.db.DbDao;
 import emcshop.util.Settings;
 import emcshop.util.TimeUtils;
 
 @SuppressWarnings("serial")
 public class MainFrame extends JFrame implements WindowListener {
 	private static final Logger logger = Logger.getLogger(MainFrame.class.getName());
 	private JButton update;
 	private JLabel lastUpdateDate;
 	private JTextField startDate;
 	private JTextField endDate;
 	private JComboBox groupBy;
 	private JButton show;
 
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
 
 		startDate = new JTextField();
 		endDate = new JTextField();
 		groupBy = new JComboBox();
 		groupBy.addItem("Item");
 		groupBy.addItem("Player");
 		show = new JButton("Show Transactions");
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
 
 		JLabel l = new JLabel("Start:");
 		p2.add(l, "align right");
 		startDate.setSize(100, 10);
 		p2.add(startDate, "w 100!, wrap");
 
 		p2.add(new JLabel("End:"), "align right");
 		p2.add(endDate, "w 100!, wrap");
 
 		p2.add(new JLabel("Group By:"), "align right");
 		p2.add(groupBy, "wrap");
 
 		p.add(p2, "wrap");
 		p.add(show, "align center");
 
 		return p;
 	}
 
 	private JPanel createRightPanel() {
 		JPanel p = new JPanel();
 		p.setLayout(new MigLayout());
 
 		JLabel label = new JLabel("<html><h1>Feb 25 2013 to today</h1></html>");
 		p.add(label);
 
 		return p;
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
