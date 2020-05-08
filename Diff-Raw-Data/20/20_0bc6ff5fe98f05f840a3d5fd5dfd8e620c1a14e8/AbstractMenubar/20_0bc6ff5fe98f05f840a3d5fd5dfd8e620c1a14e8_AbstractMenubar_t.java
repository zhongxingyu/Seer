 package ch.zhaw.simulation.menu;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JCheckBoxMenuItem;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.KeyStroke;
 
 import ch.zhaw.simulation.clipboard.ClipboardInterface;
 import ch.zhaw.simulation.clipboard.ClipboardListener;
 import ch.zhaw.simulation.icon.IconLoader;
 import ch.zhaw.simulation.menutoolbar.actions.MenuToolbarAction;
 import ch.zhaw.simulation.menutoolbar.actions.MenuToolbarActionHandler;
 import ch.zhaw.simulation.menutoolbar.actions.MenuToolbarActionType;
 import ch.zhaw.simulation.model.SimulationType;
 import ch.zhaw.simulation.sysintegration.SysMenuShortcuts;
 import ch.zhaw.simulation.sysintegration.Sysintegration;
 import ch.zhaw.simulation.undo.UndoHandler;
 import ch.zhaw.simulation.undo.UndoListener;
 
 /**
  * The menubar
  * 
  * @author Andreas Butti
  */
 public class AbstractMenubar extends MenuToolbarActionHandler implements UndoListener {
 	/**
 	 * The menubar
 	 */
 	protected JMenuBar mb = new JMenuBar();
 
 	/**
 	 * Menu file
 	 */
 	protected JMenu mFile;
 
 	/**
 	 * Menu view
 	 */
 	protected JMenu mView;
 
 	/**
 	 * Menu edit
 	 */
 	protected JMenu mEdit;
 
 	/**
 	 * Menu simulation
 	 */
 	protected JMenu mSimulation;
 
 	/**
 	 * Menu help
 	 */
 	protected JMenu mHelp;
 
 	/**
 	 * Redo menuitem
 	 */
 	protected JMenuItem mRedo;
 
 	/**
 	 * Undo menuitem
 	 */
 	protected JMenuItem mUndo;
 
 	/**
 	 * Layout menu
 	 */
 	protected JMenu mLayout = new JMenu("Layout");
 
 	/**
 	 * Recent menu
 	 */
 	protected JMenu recentMenu;
 
 	/**
 	 * The shortcuts for a specific command
 	 */
 	protected SysMenuShortcuts sysmenu;
 
 	/**
 	 * The undo redo handle, to read the undo redo state
 	 */
 	protected UndoHandler um;
 
 	/**
 	 * true if this is the main menu with all menupoints or false if this is a
 	 * submenu with only a part of
 	 */
 	protected boolean mainMenu;
 
 	protected ClipboardInterface clipboard;
 	protected JCheckBoxMenuItem sidebar;
 
 	private String otherTypeName;
 
 	private SimulationType otherType;
 
 	public AbstractMenubar(Sysintegration sysintegration, UndoHandler um, ClipboardInterface clipboard, String otherTypeName, SimulationType otherType) {
 		this.sysmenu = sysintegration.getMenu();
 		this.um = um;
 		this.clipboard = clipboard;
 		this.otherTypeName = otherTypeName;
 		this.otherType = otherType;
 
 		if (um == null) {
 			throw new NullPointerException("um == null");
 		}
 
 		if (this.sysmenu == null) {
 			throw new NullPointerException("this.sysmenu == null");
 		}
 
 		if (clipboard == null) {
 			throw new NullPointerException("clipboard == null");
 		}
 
 	}
 
 	/**
 	 * Initialize the menus
 	 * 
 	 * @param recentMenu
 	 *            The recent menu
 	 * @param mainMenu
 	 *            true if this is the main menu, or false if this is a sub
 	 *            editor without load save etc.
 	 */
 	public void initMenusToolbar(JMenu recentMenu, boolean mainMenu) {
 		this.recentMenu = recentMenu;
 		this.mainMenu = mainMenu;
 
 		initFileMenu();
 		initEditMenu();
 		initViewMenu();
 
 		initSimulationMenu();
 
 		initHelpMenu();
 	}
 
 	protected void initSimulationMenu() {
 		mSimulation = new JMenu("Simulation");
 		mSimulation.setMnemonic('S');
 
 		if (mainMenu) {
 			addMenuItem(mSimulation, "Zeitsimulation", "start-simulation", MenuToolbarActionType.START_SIMULATION, sysmenu.getSimulationSimulation());
			addMenuItem(mSimulation, "Lade letzte Resultate", "load-recent-results", MenuToolbarActionType.LOAD_RESULTS, null);
 		}
 
 		addAdditionalSimulation();
 		addMathConsole();
 	}
 
 	protected void addAdditionalSimulation() {
 	}
 
 	protected void addMathConsole() {
 		addMenuItem(mSimulation, "Mathekonsole", "math", MenuToolbarActionType.SHOW_MATH_CONSOLE, sysmenu.getSimulationMathconsole());
 		mb.add(mSimulation);
 	}
 
 	protected void initViewMenu() {
 		mView = new JMenu("Ansicht");
 		mView.setMnemonic('A');
 		if (mainMenu) {
 
 			JMenu laf = new JMenu("Look & Feel");
 			laf.setIcon(IconLoader.getIcon("style"));
 
 			addMenuItem(laf, "System LAF", "system", new ActionListener() {
 
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					setLookAndFeel("sys");
 				}
 			}, null);
 
 			addMenuItem(laf, "Nimbus", "Java-Logo", new ActionListener() {
 
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					setLookAndFeel("nimbus");
 				}
 			}, null);
 
 			addMenuItem(laf, "Java LAF", "Java-Logo", new ActionListener() {
 
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					setLookAndFeel("java");
 				}
 			}, null);
 
 			mView.add(laf);
 		}
 
 		this.sidebar = new JCheckBoxMenuItem("Seitenleiste");
 		this.sidebar.setSelected(true);
 
 		sidebar.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				fireMenuActionPerformed(new MenuToolbarAction(MenuToolbarActionType.SHOW_SIDEBAR, sidebar.isSelected()));
 			}
 		});
 
 		sidebar.setAccelerator(sysmenu.getViewSidebar());
 
 		mView.add(sidebar);
 
 		mb.add(mView);
 	}
 
 	protected void setLookAndFeel(String lookAndFeel) {
 		fireMenuActionPerformed(new MenuToolbarAction(MenuToolbarActionType.LOOK_AND_FEEL_CHANGED, lookAndFeel));
 	}
 
 	protected void initHelpMenu() {
 		mHelp = new JMenu("Hilfe");
 		mHelp.setMnemonic('H');
 
 		addMenuItem(mHelp, "Hilfe", "help", MenuToolbarActionType.HELP, sysmenu.getHelpHelp());
 
 		addMenuItem(mHelp, "Über", "help-about", MenuToolbarActionType.ABOUT, sysmenu.getHelpAbout());
 
 		mb.add(mHelp);
 	}
 
 	@Override
 	public void undoRedoUpdated() {
 		mUndo.setEnabled(um.canUndo());
 		mRedo.setEnabled(um.canRedo());
 
 		if (um.canUndo()) {
 			mUndo.setText(um.getUndoPresentationName());
 		} else {
 			mUndo.setText("Rückgängig");
 		}
 
 		if (um.canRedo()) {
 			mRedo.setText(um.getRedoPresentationName());
 		} else {
 			mRedo.setText("Widerherstellen");
 		}
 	}
 
 	protected void initEditMenu() {
 		mEdit = new JMenu("Bearbeiten");
 		mEdit.setMnemonic('B');
 
 		mUndo = new JMenuItem("Rückgängig", IconLoader.getIcon("edit-redo"));
 		mUndo.setAccelerator(sysmenu.getEditUndo());
 		mUndo.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				fireMenuActionPerformed(new MenuToolbarAction(MenuToolbarActionType.UNDO));
 			}
 		});
 		mEdit.add(mUndo);
 
 		mRedo = new JMenuItem("Widerherstellen", IconLoader.getIcon("edit-undo"));
 		mRedo.setAccelerator(sysmenu.getEditRedo());
 		mRedo.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				fireMenuActionPerformed(new MenuToolbarAction(MenuToolbarActionType.REDO));
 			}
 		});
 		mEdit.add(mRedo);
 
 		undoRedoUpdated();
 
 		final JMenuItem mCut = addMenuItem(mEdit, "Ausschneiden", "edit-cut", MenuToolbarActionType.CUT, sysmenu.getEditCut());
 		final JMenuItem mCopy = addMenuItem(mEdit, "Kopieren", "editcopy", MenuToolbarActionType.COPY, sysmenu.getEditCopy());
 		final JMenuItem mPaste = addMenuItem(mEdit, "Einfügen", "editpaste", MenuToolbarActionType.PASTE, sysmenu.getEditPaste());
 
 		mCopy.setEnabled(false);
 		mPaste.setEnabled(false);
 		mCut.setEnabled(false);
 
 		clipboard.addListener(new ClipboardListener() {
 
 			@Override
 			public void pasteEnabled(boolean enabled) {
 				mPaste.setEnabled(enabled);
 			}
 
 			@Override
 			public void cutCopyEnabled(boolean enabled) {
 				mCut.setEnabled(enabled);
 				mCopy.setEnabled(enabled);
 			}
 		});
 
 		mEdit.addSeparator();
 
 		addMenuItem(mEdit, "Alles markieren", "edit-select-all", MenuToolbarActionType.SELECT_ALL, sysmenu.getEditSelectAll());
 		addMenuItem(mEdit, "Löschen", "edit-delete", MenuToolbarActionType.DELETE_SELECTION, sysmenu.getEditDelete());
 
 		mLayout.setIcon(IconLoader.getIcon("alignCenterVertical"));
 		mEdit.add(mLayout);
 
 		initLayoutMenu();
 
 		if (mainMenu) {
 			addMenuItem(mEdit, "Einstellungen", "preferences", MenuToolbarActionType.SETTINGS, sysmenu.getEditSettings());
 		}
 		mb.add(mEdit);
 	}
 
 	protected void initLayoutMenu() {
 		addMenuItem(mLayout, "Unten ausrichten", "alignBottom", MenuToolbarActionType.LAYOUT_BOTTOM, sysmenu.getLayoutBottom());
 		addMenuItem(mLayout, "Oben ausrichten", "alignTop", MenuToolbarActionType.LAYOUT_TOP, sysmenu.getLayoutTop());
 		addMenuItem(mLayout, "Links ausrichten", "alignLeft", MenuToolbarActionType.LAYOUT_LEFT, sysmenu.getLayoutLeft());
 		addMenuItem(mLayout, "Rechts ausrichten", "alignRight", MenuToolbarActionType.LAYOUT_RIGHT, sysmenu.getLayoutRight());
 		addMenuItem(mLayout, "Horizontal zentrieren", "alignCenterHorizontal", MenuToolbarActionType.LAYOUT_CENTER_HORIZONTAL,
 				sysmenu.getLayoutCenterHorizontal());
 		addMenuItem(mLayout, "Vertikal zentrieren", "alignCenterVertical", MenuToolbarActionType.LAYOUT_CENTER_VERTICAL, sysmenu.getLayoutCenterVertical());
 	}
 
 	protected void initFileMenu() {
 		mFile = new JMenu("Datei");
 		mFile.setMnemonic('D');
 
 		if (mainMenu) {
 			addMenuItem(mFile, "Neu", "file-new", MenuToolbarActionType.NEW_FILE, sysmenu.getFileNew());
 
 			addMenuItem(mFile, "Neu - " + otherTypeName, "file-new", new ActionListener() {
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					fireMenuActionPerformed(new MenuToolbarAction(MenuToolbarActionType.NEW_FILE, otherType));
 				}
 			}, null);
 
 			mFile.addSeparator();
 
 			addMenuItem(mFile, "Öffnen", "open", MenuToolbarActionType.OPEN_FILE, sysmenu.getFileOpen());
 			mFile.add(recentMenu);
 			mFile.addSeparator();
 
 			addMenuItem(mFile, "Speichern", "save", MenuToolbarActionType.SAVE, sysmenu.getFileSave());
 			addMenuItem(mFile, "Speichern unter", "save-as", MenuToolbarActionType.SAVE_AS, sysmenu.getFileSaveAs());
 			mFile.addSeparator();
 		}
 
 		addMenuItem(mFile, "Speichern als Bild", "photos", MenuToolbarActionType.SNAPSHOT, sysmenu.getFileTakeSnapshot());
 		mFile.addSeparator();
 
 		if (mainMenu) {
 			addMenuItem(mFile, "Beenden", "exit", MenuToolbarActionType.EXIT, sysmenu.getFileExitApplication());
 		} else {
 			addMenuItem(mFile, "Schliessen", "exit", MenuToolbarActionType.CLOSE, sysmenu.getFileClose());
 		}
 		mb.add(mFile);
 
 	}
 
 	protected JMenuItem addMenuItem(JMenu menu, String name, String icon, final MenuToolbarActionType action, KeyStroke keyStroke) {
 		return addMenuItem(menu, name, icon, new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				fireMenuActionPerformed(new MenuToolbarAction(action));
 			}
 		}, keyStroke);
 	}
 
 	protected JMenuItem addMenuItem(JMenu menu, String name, String icon, final ActionListener listener, KeyStroke keyStroke) {
 
 		JMenuItem it = new JMenuItem(name, IconLoader.getIcon(icon));
 		it.addActionListener(listener);
 
 		if (keyStroke != null) {
 			it.setAccelerator(keyStroke);
 		}
 
 		menu.add(it);
 
 		return it;
 	}
 
 	public JMenuBar getMenubar() {
 		return mb;
 	}
 }
