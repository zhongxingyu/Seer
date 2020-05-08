 package org.rsbot.gui;
 
 import org.rsbot.Configuration;
 import org.rsbot.bot.Bot;
 import org.rsbot.gui.component.Messages;
 import org.rsbot.script.Script;
 import org.rsbot.script.internal.ScriptHandler;
 import org.rsbot.script.internal.event.ScriptListener;
 import org.rsbot.script.provider.*;
 import org.rsbot.service.Preferences;
 
 import javax.swing.*;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.table.AbstractTableModel;
 import javax.swing.table.TableColumn;
 import java.awt.*;
 import java.awt.event.*;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.logging.Logger;
 
 /**
  * @author Paris
  */
 public class ScriptSelector extends JDialog implements ScriptListener {
 	private static final long serialVersionUID = 5475451138208522511L;
 	private static final Logger log = Logger.getLogger(ScriptSelector.class.getName());
 	private static final String[] COLUMN_NAMES = new String[]{"", "Name", "Description"};
 
 	private static final ScriptSource SRC_SOURCES;
 	private static final ScriptSource SRC_PRE_COMPILED;
 	private static final ScriptSource SRC_NETWORK;
 	private final BotGUI frame;
 	private final Bot bot;
 	private JTable table;
 	private JTextField search;
 	private final static Color searchAltColor = Color.GRAY;
 	private JComboBox accounts;
 	private JComboBox categories;
 	private final ScriptTableModel model;
 	private final List<ScriptDefinition> scripts;
 	private JButton submit, connect;
 	public static boolean connectPrompted = false;
 
 	static {
 		SRC_SOURCES = new FileScriptSource(new File(Configuration.Paths.getScriptsSourcesDirectory()));
 		SRC_PRE_COMPILED = new FileScriptSource(new File(Configuration.Paths.getScriptsPrecompiledDirectory()));
 		SRC_NETWORK = ScriptDeliveryNetwork.getInstance();
 	}
 
 	public ScriptSelector(final BotGUI frame, final Bot bot) {
 		super(frame, "Scripts", true);
 		this.frame = frame;
 		this.bot = bot;
 		scripts = new ArrayList<ScriptDefinition>();
 		model = new ScriptTableModel(scripts);
 	}
 
 	public void showGUI() {
 		init();
 		update();
 		load();
 		if (!connectPrompted && Preferences.getInstance().sdnUser.length() == 0) {
			log.info("Visit " + Configuration.Paths.URLs.HOST + "/scripts to create your custom script list!");
 			connect.doClick();
 		}
 		if (!connectPrompted) {
 			connectPrompted = true;
 		}
 		setVisible(true);
 	}
 
 	void update() {
 		final boolean available = bot.getScriptHandler().getRunningScripts().size() == 0;
 		submit.setEnabled(available && table.getSelectedRow() != -1);
 		table.setEnabled(available);
 		search.setEnabled(available);
 		accounts.setEnabled(available);
 		table.clearSelection();
 	}
 
 	private void load() {
 		scripts.clear();
 		final List<ScriptDefinition> net = SRC_NETWORK.list();
 		if (net != null) {
 			scripts.addAll(net);
 		}
 		scripts.addAll(SRC_PRE_COMPILED.list());
 		scripts.addAll(SRC_SOURCES.list());
 		Collections.sort(scripts);
 
 		filter();
 		table.revalidate();
 	}
 
 	private void init() {
 		setIconImage(Configuration.getImage(Configuration.Paths.Resources.ICON_SCRIPT));
 		setLayout(new BorderLayout());
 		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
 		bot.getScriptHandler().addScriptListener(ScriptSelector.this);
 		addWindowListener(new WindowAdapter() {
 			@Override
 			public void windowClosing(final WindowEvent e) {
 				bot.getScriptHandler().removeScriptListener(ScriptSelector.this);
 				setVisible(false);
 				dispose();
 			}
 		});
 		connect = new JButton(new ImageIcon(Configuration.getImage(Configuration.Paths.Resources.ICON_DISCONNECT)));
 		final JButton refresh = new JButton(new ImageIcon(Configuration.getImage(Configuration.Paths.Resources.ICON_REFRESH)));
 		refresh.setToolTipText("Refresh");
 		refresh.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				refresh.setEnabled(false);
 				connect.setEnabled(false);
 				new Thread() {
 					@Override
 					public void run() {
 						final Thread[] tasks = {new Thread(ScriptDeliveryNetwork.getInstance()), new Thread(ScriptUserList.getInstance())};
 						tasks[0].start();
 						tasks[1].start();
 						try {
 							tasks[0].join();
 							tasks[1].join();
 						} catch (final InterruptedException ignored) {
 						}
 						SwingUtilities.invokeLater(new Runnable() {
 							public void run() {
 								load();
 								refresh.setEnabled(true);
 								connect.setEnabled(true);
 							}
 						});
 					}
 				}.start();
 			}
 		});
 		table = new JTable(model) {
 			private static final long serialVersionUID = 6969410339933692133L;
 
 			@Override
 			public String getToolTipText(MouseEvent e) {
 				int row = rowAtPoint(e.getPoint());
 				ScriptDefinition def = model.getDefinition(row);
 				return def.toString();
 			}
 		};
 		table.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mousePressed(final MouseEvent e) {
 				if ((e.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK) {
 					final int row = table.rowAtPoint(e.getPoint());
 					table.getSelectionModel().setSelectionInterval(row, row);
 					showMenu(e);
 				}
 			}
 
 			@Override
 			public void mouseClicked(final MouseEvent e) {
 				if (e.getClickCount() == 2 && submit.isEnabled()) {
 					submit.doClick();
 				}
 			}
 
 			private void showMenu(final MouseEvent e) {
 				final int row = table.rowAtPoint(e.getPoint());
 				final ScriptDefinition def = model.getDefinition(row);
 
 				final JPopupMenu contextMenu = new JPopupMenu();
 				final JMenuItem visit = new JMenuItem();
 				visit.setText("Visit Site");
 				visit.setIcon(new ImageIcon(Configuration.getImage(Configuration.Paths.Resources.ICON_WEBLINK)));
 				visit.addMouseListener(new MouseAdapter() {
 					@Override
 					public void mousePressed(final MouseEvent e) {
 						BotGUI.openURL(def.website);
 					}
 				});
 
 				final JMenuItem start = new JMenuItem();
 				start.setText(submit.getToolTipText());
 				start.setIcon(new ImageIcon(Configuration.getImage(Configuration.Paths.Resources.ICON_PLAY)));
 				start.addActionListener(new ActionListener() {
 					public void actionPerformed(ActionEvent e) {
 						submit.doClick();
 					}
 				});
 				start.setEnabled(submit.isEnabled());
 
 				final JMenuItem delete = new JMenuItem();
 				delete.setText("Delete");
 				delete.setIcon(new ImageIcon(Configuration.getImage(Configuration.Paths.Resources.ICON_CLOSE)));
 				delete.addActionListener(new ActionListener() {
 					public void actionPerformed(ActionEvent e) {
 						final File path = def.path == null || def.path.isEmpty() ? null : new File(def.path);
 						if (path != null && path.exists() && path.delete()) {
 							log.info("Removed script " + def.getName());
 						} else {
 							log.warning("Could not remove " + def.getName());
 						}
 						scripts.remove(def);
 						load();
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
 		table.setFocusable(false);
 		table.setRowHeight(20);
 		table.setIntercellSpacing(new Dimension(1, 1));
 		table.setShowGrid(false);
 		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		table.getSelectionModel().addListSelectionListener(new TableSelectionListener());
 		setColumnWidths(table, 30, 200);
 		final JToolBar toolBar = new JToolBar();
 		toolBar.setMargin(new Insets(1, 1, 1, 1));
 		toolBar.setFloatable(false);
 		search = new JTextField();
 		final Color searchDefaultColor = search.getForeground();
 		final String searchDefaultText = "Search...";
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
 				filter();
 				table.revalidate();
 			}
 
 			@Override
 			public void keyReleased(final KeyEvent e) {
 				keyTyped(e);
 			}
 		});
 		submit = new JButton("Start", new ImageIcon(Configuration.getImage(Configuration.Paths.Resources.ICON_PLAY)));
 		submit.setToolTipText(submit.getText());
 		submit.setText("");
 		connect.setToolTipText(Messages.SDNALL);
 		connect.setEnabled(ScriptUserList.getInstance().isAvailable());
 		submit.setEnabled(false);
 		submit.addActionListener(new ActionListener() {
 			public void actionPerformed(final ActionEvent evt) {
 				final ScriptDefinition def = model.getDefinition(table.getSelectedRow());
 				setVisible(false);
 				final String account = (String) accounts.getSelectedItem();
 				bot.getScriptHandler().removeScriptListener(ScriptSelector.this);
 				dispose();
 				Script script = null;
 				try {
 					script = def.source.load(def);
 				} catch (final Exception e) {
 					log.severe(e.getMessage());
 				}
 				if (script != null) {
 					bot.setAccount(account);
 					bot.getScriptHandler().runScript(script);
 					frame.updateScriptControls();
 				}
 			}
 		});
 		final Component parent = this;
 		connect.addActionListener(new ActionListener() {
 			public void actionPerformed(final ActionEvent arg0) {
 				String user = null;
 				if (connect.getToolTipText().equals(Messages.SDNALL)) {
 					user = (String) JOptionPane.showInputDialog(parent,
 							"Load script list from " + Configuration.Paths.URLs.HOST + " account (leave blank to show all scripts):",
 							"Custom list", JOptionPane.QUESTION_MESSAGE, null, null, "");
 				}
 				if (user == null || user.length() == 0 || user.length() > 15 || user.length() < 3) {
 					user = "";
 				}
 				Preferences.getInstance().sdnUser = user;
 				connectUpdate();
 				load();
 			}
 		});
 		connectUpdate();
 		accounts = new JComboBox(AccountManager.getAccountNames());
 		categories = new JComboBox(new String[]{"All", "Agility", "Combat", "Construction", "Cooking", "Crafting", "Dungeoneering", "Farming",
 				"Firemaking", "Fishing", "Fletching", "Herblore", "Hunter", "Magic", "Minigame", "Mining", "Other", "Money Making", "Prayer",
 				"Ranged", "Runecrafting", "Slayer", "Smithing", "Summoning", "Thieving", "Woodcutting"});
 		accounts.setPreferredSize(new Dimension(125, 20));
 		categories.setPreferredSize(new Dimension(150, 20));
 		categories.addActionListener(new ActionListener() {
 			public void actionPerformed(final ActionEvent arg0) {
 				filter();
 			}
 		});
 		toolBar.add(search);
 		toolBar.add(Box.createHorizontalStrut(5));
 		toolBar.add(categories);
 		toolBar.add(Box.createHorizontalStrut(5));
 		toolBar.add(accounts);
 		toolBar.add(Box.createHorizontalStrut(5));
 		toolBar.add(refresh);
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
 		getRootPane().setDefaultButton(submit);
 		search.requestFocus();
 	}
 
 	private void filter() {
 		final String keys = ((String) categories.getSelectedItem()).toLowerCase();
 		model.search((search == null || search.getForeground() == searchAltColor) ? "" : search.getText(), keys.equals("all") ? null : keys);
 	}
 
 	private void setColumnWidths(final JTable table, final int... widths) {
 		for (int i = 0; i < widths.length; ++i) {
 			final TableColumn col = table.getColumnModel().getColumn(i);
 			col.setPreferredWidth(widths[i]);
 			col.setMinWidth(widths[i]);
 			col.setMaxWidth(widths[i]);
 		}
 	}
 
 	public void scriptStarted(final ScriptHandler handler) {
 		update();
 	}
 
 	public void scriptStopped(final ScriptHandler handler) {
 		update();
 	}
 
 	public void scriptResumed(final ScriptHandler handler) {
 	}
 
 	public void scriptPaused(final ScriptHandler handler) {
 	}
 
 	public void inputChanged(final Bot bot, final int mask) {
 	}
 
 	private void connectUpdate() {
 		final String user = Preferences.getInstance().sdnUser;
 		ScriptUserList.getInstance().run();
 		final boolean connected = user != null && user.length() != 0 && ScriptUserList.getInstance().isReady();
 		ScriptUserList.getInstance().enabled = connected;
 		connect.setToolTipText(connected ? String.format(Messages.SDNUSER, user) : Messages.SDNALL);
 		final String icon = connected ? Configuration.Paths.Resources.ICON_CONNECT : Configuration.Paths.Resources.ICON_DISCONNECT;
 		connect.setIcon(new ImageIcon(Configuration.getImage(icon)));
 		connect.repaint();
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
 		public static final ImageIcon ICON_SCRIPT_SRC = new ImageIcon(Configuration.getImage(Configuration.Paths.Resources.ICON_SCRIPT_CODE));
 		public static final ImageIcon ICON_SCRIPT_PRE = new ImageIcon(Configuration.getImage(Configuration.Paths.Resources.ICON_SCRIPT_GEAR));
 		public static final ImageIcon ICON_SCRIPT_NET = new ImageIcon(Configuration.getImage(Configuration.Paths.Resources.ICON_SCRIPT_LIVE));
 		private final List<ScriptDefinition> scripts;
 		private final List<ScriptDefinition> matches;
 
 		public ScriptTableModel(final List<ScriptDefinition> scripts) {
 			this.scripts = scripts;
 			matches = new ArrayList<ScriptDefinition>();
 		}
 
 		public void search(final String find, final String keys) {
 			matches.clear();
 			for (final ScriptDefinition def : scripts) {
 				if (ScriptUserList.getInstance().isSelected() && !ScriptUserList.getInstance().isListed(def)) {
 					continue;
 				}
 				if (find.length() != 0 && !def.name.toLowerCase().contains(find.toLowerCase())) {
 					continue;
 				}
 				if (keys == null || keys.length() == 0) {
 					matches.add(def);
 					continue;
 				}
 				final List<String> keywords = def.getKeywords();
 				final ArrayList<String> list = new ArrayList<String>(keywords.size());
 				for (final String key : keywords) {
 					list.add(key.toLowerCase());
 				}
 				if (list.contains(keys)) {
 					matches.add(def);
 					continue;
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
 				switch (columnIndex) {
 					case 0:
 						if (def.source == SRC_SOURCES) {
 							return ICON_SCRIPT_SRC;
 						}
 						if (def.source == SRC_PRE_COMPILED) {
 							return ICON_SCRIPT_PRE;
 						}
 						return ICON_SCRIPT_NET;
 					case 1:
 						return def.getName();
 					case 2:
 						return def.getDescription();
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
