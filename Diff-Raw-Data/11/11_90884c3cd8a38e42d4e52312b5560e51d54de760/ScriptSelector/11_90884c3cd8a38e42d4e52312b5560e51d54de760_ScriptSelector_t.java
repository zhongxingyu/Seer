 package org.rsbot.gui;
 
 import org.rsbot.bot.Bot;
 import org.rsbot.script.Script;
 import org.rsbot.script.internal.ScriptHandler;
 import org.rsbot.script.internal.event.ScriptListener;
 import org.rsbot.service.*;
 import org.rsbot.util.GlobalConfiguration;
 
 import javax.swing.*;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.table.AbstractTableModel;
 import java.awt.*;
 import java.awt.event.*;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Logger;
 
 /**
  * @author Jacmob
  */
 public class ScriptSelector extends JDialog implements ScriptListener {
 	public static void main(final String[] args) {
 		new ScriptSelector(null, null).setVisible(true);
 	}
 
 	private static final long serialVersionUID = 5475451138208522511L;
 	private static final Logger log = Logger.getLogger(ScriptSelector.class.getName());
 	private static final String[] COLUMN_NAMES = new String[]{"", "Name", "Version", "Author", "Description"};
 
 	private static final ScriptSource SRC_SOURCES;
 	private static final ScriptSource SRC_PRECOMPILED;
 	private static final ScriptSource SRC_BUNDLED;
 	private static final ScriptSource SRC_DRM;
 	private final Bot bot;
 	private JTable table;
 	private JTextField search;
 	private JComboBox accounts;
 	private final ScriptTableModel model;
 	private final List<ScriptDefinition> scripts;
 	private JButton submit;
 	private boolean connected = true;
 
 	static {
 		SRC_SOURCES = new FileScriptSource(new File(GlobalConfiguration.Paths.getScriptsSourcesDirectory()));
 		SRC_PRECOMPILED = new FileScriptSource(new File(GlobalConfiguration.Paths.getScriptsPrecompiledDirectory()));
 		if (GlobalConfiguration.RUNNING_FROM_JAR) {
 			SRC_BUNDLED = new FileScriptSource(new File(GlobalConfiguration.Paths.getScriptsExtractedCache()));
 		} else {
 			SRC_BUNDLED = new FileScriptSource(new File("." + File.separator + GlobalConfiguration.Paths.SCRIPTS_NAME_SRC));
 		}
 		SRC_DRM = ScriptDeliveryNetwork.getInstance();
 	}
 
 	public ScriptSelector(final Frame frame, final Bot bot) {
 		super(frame, "Script Selector");
 		this.bot = bot;
 		scripts = new ArrayList<ScriptDefinition>();
 		model = new ScriptTableModel(scripts);
 	}
 
 	public void showGUI() {
 		init();
 		update();
 		setVisible(true);
 		load();
 	}
 
 	public void update() {
 		final boolean available = bot.getScriptHandler().getRunningScripts().size() == 0;
 		submit.setEnabled(available && table.getSelectedRow() != -1);
 		table.setEnabled(available);
 		search.setEnabled(available);
 		accounts.setEnabled(available);
 		table.clearSelection();
 	}
 
 	private void load() {
 		scripts.clear();
 		if (connected) {
 			scripts.addAll(SRC_DRM.list());
 		}
 		scripts.addAll(SRC_BUNDLED.list());
 		scripts.addAll(SRC_PRECOMPILED.list());
 		scripts.addAll(SRC_SOURCES.list());
 		model.search("");
 	}
 
 	private void init() {
 		setLayout(new BorderLayout());
 		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
 		bot.getScriptHandler().addScriptListener(ScriptSelector.this);
 		addWindowListener(new WindowAdapter() {
 			@Override
 			public void windowClosing(final WindowEvent e) {
 				bot.getScriptHandler().removeScriptListener(ScriptSelector.this);
 				dispose();
 			}
 		});
 		table = new JTable(model);
 		table.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mousePressed(final MouseEvent e) {
 				if ((e.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK) {
 					final int row = table.rowAtPoint(e.getPoint());
 					table.getSelectionModel().setSelectionInterval(row, row);
 					showMenu(e);
 				}
 			}
 
 			private void showMenu(final MouseEvent e) {
 				final int row = table.rowAtPoint(e.getPoint());
 				final ScriptDefinition def = model.getDefinition(row);
 
 				final JPopupMenu contextMenu = new JPopupMenu();
 				final JMenuItem visit = new JMenuItem();
 				visit.setText("Visit Site");
 				visit.setIcon(new ImageIcon(GlobalConfiguration.getImage(GlobalConfiguration.Paths.Resources.ICON_WEBLINK)));
 				visit.addMouseListener(new MouseAdapter() {
 					@Override
 					public void mousePressed(final MouseEvent e) {
 						BotGUI.openURL(def.website);
 					}
 				});
 
 				final JMenuItem start = new JMenuItem();
 				start.setText(submit.getText());
 				start.setIcon(new ImageIcon(GlobalConfiguration.getImage(GlobalConfiguration.Paths.Resources.ICON_PLAY)));
 				start.addActionListener(new ActionListener(){
 					public void actionPerformed(ActionEvent e) {
 						submit.doClick();
 					}
 				});
 				start.setEnabled(submit.isEnabled());
 
 				final JMenuItem delete = new JMenuItem();
 				delete.setText("Delete");
 				delete.setIcon(new ImageIcon(GlobalConfiguration.getImage(GlobalConfiguration.Paths.Resources.ICON_CLOSE)));
 				delete.addActionListener(new ActionListener() {
 					public void actionPerformed(ActionEvent e) {
 						final File path = def.path == null || def.path.isEmpty() ? null : new File(def.path);
 						if (path != null && path.exists() && path.delete()) {
 							log.info("Deleted script " + def.name + " (" + def.path + ")");
 						} else {
 							log.warning("Could not delete " + def.name);
 						}
 					}
 				});
 
 				if (def.website == null || def.website.isEmpty()) {
 					visit.setEnabled(false);
 				}
 
 				contextMenu.add(start);
 				contextMenu.add(visit);
 				contextMenu.add(delete);
 				contextMenu.show(table, e.getX(), e.getY());
 			}
 		});
 		table.setRowHeight(20);
 		table.setIntercellSpacing(new Dimension(1, 1));
 		table.setShowGrid(false);
 		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		table.getSelectionModel().addListSelectionListener(new TableSelectionListener());
 		setColumnWidths(table, 30, 175, 50, 100);
 		final JToolBar toolBar = new JToolBar();
 		toolBar.setMargin(new Insets(1, 1, 1, 1));
 		toolBar.setFloatable(false);
 		search = new JTextField();
 		final Color searchDefaultColor = search.getForeground(), searchAltColor = Color.GRAY;
 		final String searchDefaultText = "Type to filter...";
 		search.setText(searchDefaultText);
 		search.setForeground(searchAltColor);
 		search.addFocusListener(new FocusAdapter() {
 			@Override
 			public void focusGained(final FocusEvent e) {
 				if (search.getForeground() == searchAltColor) {
 					search.setText("");
 					search.setForeground(searchDefaultColor);
 				}
 				table.clearSelection();
 			}
 
 			@Override
 			public void focusLost(final FocusEvent e) {
 				if (search.getText().isEmpty()) {
 					search.setText(searchDefaultText);
 					search.setForeground(searchAltColor);
 				}
 			}
 		});
 		search.addKeyListener(new KeyAdapter() {
 			@Override
 			public void keyTyped(final KeyEvent e) {
 				model.search(search.getText());
 				table.revalidate();
 			}
 		});
 		submit = new JButton("Start", new ImageIcon(
 				GlobalConfiguration.getImage(GlobalConfiguration.Paths.Resources.ICON_PLAY)));
 		final JButton connect = new JButton(new ImageIcon(
 				GlobalConfiguration.getImage(GlobalConfiguration.Paths.Resources.ICON_CONNECT)));
 		submit.setEnabled(false);
 		submit.addActionListener(new ActionListener() {
 			public void actionPerformed(final ActionEvent evt) {
 				final ScriptDefinition def = model.getDefinition(table.getSelectedRow());
 				try {
 					bot.setAccount((String) accounts.getSelectedItem());
 					bot.getScriptHandler().runScript(def.source.load(def));
 					bot.getScriptHandler().removeScriptListener(ScriptSelector.this);
 					dispose();
 				} catch (final ServiceException e) {
 					e.printStackTrace();
 				}
 			}
 		});
 		connect.setEnabled(GlobalConfiguration.SCRIPT_DRM ? true : false);
 		if (connect.isEnabled()) {
 			final ActionListener listenConnect = new ActionListener() {
 				public void actionPerformed(final ActionEvent e) {
 					final String icon = connected ? GlobalConfiguration.Paths.Resources.ICON_DISCONNECT :
 							GlobalConfiguration.Paths.Resources.ICON_CONNECT;
 					connect.setIcon(new ImageIcon(GlobalConfiguration.getImage(icon)));
 					connect.repaint();
 					connected = !connected;
 					load();
 				}
 			};
 			connect.addActionListener(listenConnect);
 		}
 		accounts = new JComboBox(AccountManager.getAccountNames());
 		accounts.setMinimumSize(new Dimension(200, 20));
 		accounts.setPreferredSize(new Dimension(200, 20));
 		toolBar.add(search);
 		toolBar.add(Box.createHorizontalStrut(5));
 		toolBar.add(accounts);
 		toolBar.add(Box.createHorizontalStrut(5));
 		toolBar.add(connect);
 		toolBar.add(Box.createHorizontalStrut(5));
 		toolBar.add(submit);
 		final JPanel center = new JPanel();
 		center.setLayout(new BorderLayout());
 		final JScrollPane pane = new JScrollPane(table, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
 				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
 		center.add(pane, BorderLayout.CENTER);
 		add(center, BorderLayout.CENTER);
 		add(toolBar, BorderLayout.SOUTH);
 		setSize(750, 400);
 		setMinimumSize(getSize());
 		setLocationRelativeTo(getParent());
 		search.requestFocus();
 	}
 
 	private void setColumnWidths(final JTable table, final int... widths) {
 		for (int i = 0; i < widths.length; ++i) {
 			table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
 			table.getColumnModel().getColumn(i).setMinWidth(widths[i]);
 			table.getColumnModel().getColumn(i).setMaxWidth(widths[i]);
 		}
 	}
 
 	public void scriptStarted(final ScriptHandler handler, final Script script) {
 		update();
 	}
 
 	public void scriptStopped(final ScriptHandler handler, final Script script) {
 		update();
 	}
 
 	public void scriptResumed(final ScriptHandler handler, final Script script) {
 	}
 
 	public void scriptPaused(final ScriptHandler handler, final Script script) {
 	}
 
 	public void inputChanged(final Bot bot, final int mask) {
 	}
 
 	private class TableSelectionListener implements ListSelectionListener {
 		public void valueChanged(final ListSelectionEvent evt) {
 			if (!evt.getValueIsAdjusting()) {
 				submit.setEnabled(table.getSelectedRow() != -1);
 			}
 		}
 	}
 
 	private static class ScriptTableModel extends AbstractTableModel {
 		private static final long serialVersionUID = 1L;
 		public static final ImageIcon ICON_SCRIPT_SRC = new ImageIcon(
 				GlobalConfiguration.getImage(GlobalConfiguration.Paths.Resources.ICON_SCRIPT_SRC));
 		public static final ImageIcon ICON_SCRIPT_PRE = new ImageIcon(
 				GlobalConfiguration.getImage(GlobalConfiguration.Paths.Resources.ICON_SCRIPT_PRE));
 		public static final ImageIcon ICON_SCRIPT_DRM = new ImageIcon(
 				GlobalConfiguration.getImage(GlobalConfiguration.Paths.Resources.ICON_SCRIPT_DRM));
 		public static final ImageIcon ICON_SCRIPT_BDL = new ImageIcon(
 				GlobalConfiguration.getImage(GlobalConfiguration.Paths.Resources.ICON_SCRIPT_BDL));
 		private final List<ScriptDefinition> scripts;
 		private final List<ScriptDefinition> matches;
 
 		public ScriptTableModel(final List<ScriptDefinition> scripts) {
 			this.scripts = scripts;
 			matches = new ArrayList<ScriptDefinition>();
 		}
 
 		public void search(String substr) {
 			matches.clear();
 			substr = substr.trim();
 			if (substr.isEmpty()) {
 				matches.addAll(scripts);
 			} else {
 				substr = substr.toLowerCase();
 				for (final ScriptDefinition def : scripts) {
 					if (def.name.toLowerCase().contains(substr)) {
 						matches.add(def);
 					} else {
 						for (final String keyword : def.keywords) {
 							if (keyword.toLowerCase().contains(substr)) {
 								matches.add(def);
 								break;
 							}
 						}
 					}
 				}
 			}
 			fireTableDataChanged();
 		}
 
 		public ScriptDefinition getDefinition(final int rowIndex) {
 			return matches.get(rowIndex);
 		}
 
 		public int getRowCount() {
 			return matches.size();
 		}
 
 		public int getColumnCount() {
 			return COLUMN_NAMES.length;
 		}
 
 		public Object getValueAt(final int rowIndex, final int columnIndex) {
 			if (rowIndex >= 0 && rowIndex < matches.size()) {
 				final ScriptDefinition def = matches.get(rowIndex);
 				if (columnIndex == 0) {
 					if (def.source == SRC_SOURCES) {
 						return ICON_SCRIPT_SRC;
 					}
 					if (def.source == SRC_PRECOMPILED) {
 						return ICON_SCRIPT_PRE;
 					}
 					if (def.source == SRC_BUNDLED) {
 						return ICON_SCRIPT_BDL;
 					}
 					return ICON_SCRIPT_DRM;
 				}
 				if (columnIndex == 1) {
 					return def.name;
 				}
 				if (columnIndex == 2) {
 					return def.version;
 				}
 				if (columnIndex == 3) {
 					final StringBuilder b = new StringBuilder();
 					for (final String author : def.authors) {
 						b.append(author).append(", ");
 					}
 					return b.replace(b.length() - 2, b.length(), "");
 				}
 				if (columnIndex == 4) {
 					return def.description;
 				}
 			}
 			return null;
 		}
 
 		@Override
 		public Class<?> getColumnClass(final int col) {
 			if (col == 0) {
 				return ImageIcon.class;
 			}
 			return String.class;
 		}
 
 		@Override
 		public String getColumnName(final int col) {
 			return COLUMN_NAMES[col];
 		}
 	}
 }
