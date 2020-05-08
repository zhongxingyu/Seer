 package ui;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.lang.reflect.Method;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JFileChooser;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JToggleButton;
 import javax.swing.JToolBar;
 
 import ui.annotations.MainActionInfo;
 import ui.newgui.DefaultEditorAction;
 import ui.newgui.EditorAction;
 import ui.newgui.EditorComponent;
 import ui.newgui.FileWindow;
 import ui.newgui.MainWindow;
 import ui.smallstep.SmallStepGUI;
 
 import com.sun.org.apache.xalan.internal.xsltc.runtime.Hashtable;
 
 import expressions.Expression;
 
 /**
  * An Editor in the UI. It contains the sourcefile and the excecutions.
  * 
  * @author cfehling
  * 
  */
 public class EditorWindow extends JPanel implements FileWindow {
 	private static final long serialVersionUID = -5830307198539052787L;
 
 	private SourceFile file;
 
 	private File systemfile = null;
 
 	private JToolBar menu;
 
 	private JToolBar actions;
 
 	private EditorComponent shownComponent;
 
 	private List<DefaultEditorAction> fileactions;
 
 	private List<DefaultEditorAction> runactions;
 
 	private List<DefaultEditorAction> toolbaractions;
 
 	private List<DefaultEditorAction> editactions;
 
 	private Hashtable myactions;
 
 	private boolean modified;
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param ifile
 	 *            The file to display.
 	 */
 	public EditorWindow(SourceFile ifile, MainWindow imywindow) {
 		file = ifile;
 		file.addPropertyChangeListener(new PropertyChangeListener() {
 
 			public void propertyChange(PropertyChangeEvent evt) {
 				if (evt.getPropertyName().equals("modified")) {
 					EditorWindow.this.setModified((Boolean) evt.getNewValue());
 					((DefaultEditorAction) myactions.get("Save"))
 							.setEnabled((Boolean) evt.getNewValue());
 				} else if (evt.getPropertyName().equals("filename")) {
 					firePropertyChange("name", evt.getOldValue(), evt
 							.getNewValue());
 				}
 			}
 		});
 		JPanel menupanel = new JPanel();
 		// TODO DONT use BorderLayout!!
 		menupanel.setLayout(new BorderLayout());
 		menu = new JToolBar();
 		actions = new JToolBar();
 		setShownComponent(file);
 		fileactions = new LinkedList<DefaultEditorAction>();
 		runactions = new LinkedList<DefaultEditorAction>();
 		toolbaractions = new LinkedList<DefaultEditorAction>();
 		editactions = new LinkedList<DefaultEditorAction>();
 		myactions = new Hashtable();
 		// modified = false;
 
 		this.setLayout(new BorderLayout());
 		menu.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
 		addEditorComponent(file);
 		add(menupanel, BorderLayout.NORTH);
 		menupanel.add(menu, BorderLayout.WEST);
 		menupanel.add(actions, BorderLayout.CENTER);
 		generateActions();
 		setActionStatus("Undo", shownComponent.getAction("Undo").isEnabled());
 		setActionStatus("Redo", shownComponent.getAction("Redo").isEnabled());
 	}
 
 	public String getTitle() {
 		return file.getFilename();
 
 	}
 
 	private void generateActions() {
 		Class me = this.getClass();
 		Method[] methods = me.getDeclaredMethods();
 		for (int i = 0; i < methods.length; i++) {
 			final Method tmp = methods[i];
 			MainActionInfo actioninfo = tmp.getAnnotation(MainActionInfo.class);
 			if (actioninfo != null) {
 				DefaultEditorAction newaction = new DefaultEditorAction();
 				newaction.setTitle(actioninfo.name());
 				newaction.setEnabled(true);
 				myactions.put(tmp.getName().substring(6), newaction);
 				if (!actioninfo.icon().equals("none")) {
 					java.net.URL imgURL = EditorWindow.class
 							.getResource(actioninfo.icon());
 					if (imgURL != null) {
 						newaction.setIcon(new ImageIcon(imgURL));
 					} else {
 						System.out.println("Imagefile not found!");
 					}
 				}
 				newaction.setGroup(1);
 				newaction.setAccelModifiers(actioninfo.accelModifiers());
 				newaction.setAccelKey(actioninfo.accelKey());
 				newaction.setActionlistener(new ActionListener() {
 					public void actionPerformed(ActionEvent event) {
 						try {
 							tmp.invoke(EditorWindow.this);
 						} catch (Exception e) {
 							e.printStackTrace();
 							// TODO Add Handling!!
 						}
 					}
 				});
 				if (actioninfo.visibleToolbar() == MainActionInfo.TOOLBAR_VISIBLE) {
 					toolbaractions.add(newaction);
 				}
 				if (actioninfo.visibleMenu() == MainActionInfo.MENU_FILE) {
 					fileactions.add(newaction);
 				}
 				if (actioninfo.visibleMenu() == MainActionInfo.MENU_RUN) {
 					runactions.add(newaction);
 				}
 				if (actioninfo.visibleMenu() == MainActionInfo.MENU_EDIT) {
 					editactions.add(newaction);
 				}
 
 			}
 		}
 	}
 
 	@MainActionInfo(name = "Save", icon = "icons/save.png", visibleMenu = MainActionInfo.MENU_FILE, visibleToolbar = MainActionInfo.TOOLBAR_VISIBLE, accelModifiers = KeyEvent.CTRL_MASK, accelKey = KeyEvent.VK_S)
 	public boolean handleSave() {
 		if (systemfile == null) {
 			return handleSaveAs();
 		} else {
 			return writeFile();
 		}
 	}
 
 	@MainActionInfo(name = "SaveAs", icon = "icons/saveas.png", visibleMenu = MainActionInfo.MENU_FILE, visibleToolbar = MainActionInfo.TOOLBAR_VISIBLE, accelModifiers = KeyEvent.CTRL_MASK
 			| KeyEvent.SHIFT_MASK, accelKey = KeyEvent.VK_S)
 	public boolean handleSaveAs() {
 		JFileChooser chooser = new JFileChooser();
 		chooser.showSaveDialog(this);
 		File outfile = chooser.getSelectedFile();
 		if (outfile != null) {
 			try {
 				outfile.createNewFile();
 				systemfile = outfile;
 				String oldname = file.getFilename();
 				String newname = outfile.getName();
 				file.setFilename(newname);
 				firePropertyChange("name", oldname, newname);
 				writeFile();
 				return true;
 			} catch (Exception e) {
 				e.printStackTrace();
 				return false;
 			}
 		} else {
 			return false;
 		}
 	}
 
 	@MainActionInfo(name = "Undo", icon = "icons/undo.gif", visibleMenu = MainActionInfo.MENU_EDIT, visibleToolbar = MainActionInfo.TOOLBAR_VISIBLE, accelModifiers = KeyEvent.CTRL_MASK, accelKey = KeyEvent.VK_Z)
 	public void handleUndo() {
 		shownComponent.handleUndo();
 	}
 
 	@MainActionInfo(name = "Redo", icon = "icons/redo.gif", visibleMenu = MainActionInfo.MENU_EDIT, visibleToolbar = MainActionInfo.TOOLBAR_VISIBLE, accelModifiers = KeyEvent.CTRL_MASK, accelKey = KeyEvent.VK_Y)
 	public void handleRedo() {
 		shownComponent.handleRedo();
 	}
 
 	@MainActionInfo(name = "SmallStep", icon = "none", visibleMenu = MainActionInfo.MENU_RUN, visibleToolbar = MainActionInfo.TOOLBAR_HIDDEN, accelModifiers = KeyEvent.VK_UNDEFINED, accelKey = KeyEvent.VK_F11)
 	public void handleSmallStep() {
 		try {
 			SmallStepGUI gui = new SmallStepGUI("SmallStep", file.getDocument()
 					.getText(0, file.getDocument().getLength()));
 			addEditorComponent(gui);
 			setActionStatus("Undo", gui.getAction("Undo").isEnabled());
 			setActionStatus("Redo", gui.getAction("Redo").isEnabled());
 		} catch (Exception e) {
 			JOptionPane.showMessageDialog(this, e.getMessage(), "Error",
 					JOptionPane.ERROR_MESSAGE);
 		}
 	}
 
 	@MainActionInfo(name = "TypeChecker", icon = "none", visibleMenu = MainActionInfo.MENU_RUN, visibleToolbar = MainActionInfo.TOOLBAR_HIDDEN, accelModifiers = KeyEvent.VK_UNDEFINED, accelKey = KeyEvent.VK_F12)
 	public void handleTypeChecker() {
 		try {
 			Expression e = file.getDocument().getExpression();
 			TypeCheckerGUI gui = new TypeCheckerGUI("TypeChecker");
 			gui.startTypeChecking(e);
 			addEditorComponent(gui);
 			setActionStatus("Undo", gui.getAction("Undo").isEnabled());
 			setActionStatus("Redo", gui.getAction("Redo").isEnabled());
 		} catch (Exception e) {
 			JOptionPane.showMessageDialog(this, e.getMessage(), "Error",
 					JOptionPane.ERROR_MESSAGE);
 		}
 	}
 
 	private void addEditorComponent(EditorComponent comp) {
 		JToggleButton compbutton = new JToggleButton(comp.getTitle());
 		Component[] buttons = menu.getComponents();
 		boolean existed = false;
 		for (int i = 0; i < buttons.length; i++) {
 			if (((JToggleButton) buttons[i]).getLabel().equals(comp.getTitle())) {
 				compbutton = (JToggleButton) buttons[i];
 				compbutton
 						.removeActionListener(compbutton.getActionListeners()[0]);
 				existed = true;
 				break;
 			}
 		}
 		compbutton.addActionListener(new EditorComponentListener(this, comp,
 				compbutton));
 		deselectButtons();
 		compbutton.setSelected(true);
 		if (!existed)
 			menu.add(compbutton);
 		remove((Component) getShownComponent());
 		add((Component) comp, BorderLayout.CENTER);
 		setShownComponent(comp);
 		updateActions(comp);
 		paintAll(getGraphics());
 	}
 
 	public void deselectButtons() {
 		Component[] buttons = menu.getComponents();
 		for (int i = 0; i < buttons.length; i++) {
 			((JToggleButton) buttons[i]).setSelected(false);
 		}
 	}
 
 	private boolean writeFile() {
 		try {
 			FileOutputStream out = new FileOutputStream(systemfile);
 			out.write(file.getDocument().getText(0,
 					file.getDocument().getLength()).getBytes());
 			out.flush();
 			out.close();
 			file.setModified(false);
 			return true;
 		} catch (Exception e) {
 			// TODO Add Handling!!
 			System.out.println("Error while saving!");
 			return false;
 		}
 	}
 
 	public void updateActions(EditorComponent comp) {
 		actions.removeAll();
 		addActions(comp.getActions(), actions);
 	}
 
 	private void addActions(List<EditorAction> list, JComponent component) {
 		List<EditorAction> actionlist = list;
 		for (int i = 0; i < actionlist.size(); i++) {
 			final EditorAction action = actionlist.get(i);
 			if (action.isVisible()) {
 				final JButton tmp;
 				if (action.getIcon() == null) {
 					tmp = new JButton(action.getTitle());
 				} else {
 					tmp = new JButton(action.getIcon());
 					tmp.setToolTipText(action.getTitle());
 				}
 				tmp.setEnabled(action.isEnabled());
 				// tmp.setAccelerator(KeyStroke.getKeyStroke(action.getAccelKey(),action.getAccelModifiers()));
 				action.addPropertyChangeListener("enabled",
 						new PropertyChangeListener() {
 							public void propertyChange(PropertyChangeEvent evt) {
 								tmp.setEnabled(action.isEnabled());
 							}
 						});
 				component.add(tmp);
 				tmp.addActionListener(action.getActionListener());
 			}
 		}
 	}
 
 	public EditorComponent getShownComponent() {
 		return shownComponent;
 	}
 
 	public void setShownComponent(EditorComponent ishownComponent) {
 		shownComponent = ishownComponent;
 		EditorAction undo = shownComponent.getAction("Undo");
 		EditorAction redo = shownComponent.getAction("Redo");
 		undo.addPropertyChangeListener("enabled", new PropertyChangeListener() {
 			public void propertyChange(PropertyChangeEvent evt) {
 				setActionStatus("Undo", (Boolean) evt.getNewValue());
 			}
 		});
 		redo.addPropertyChangeListener("enabled", new PropertyChangeListener() {
 			public void propertyChange(PropertyChangeEvent evt) {
 				setActionStatus("Redo", (Boolean) evt.getNewValue());
 			}
 		});
 	}
 
 	public void setActionStatus(String action, boolean enabled) {
 		((DefaultEditorAction) myactions.get(action)).setEnabled(enabled);
 	}
 
 	public List<EditorAction> getFileActions() {
 		List<EditorAction> tmp = new LinkedList<EditorAction>();
 		tmp.addAll(fileactions);
 		return tmp;
 	}
 
 	public List<EditorAction> getToolbarActions() {
 		List<EditorAction> tmp = new LinkedList<EditorAction>();
 		tmp.addAll(toolbaractions);
 		return tmp;
 	}
 
 	public List<EditorAction> getEditActions() {
 		List<EditorAction> tmp = new LinkedList<EditorAction>();
 		tmp.addAll(editactions);
 		return tmp;
 	}
 
 	public List<EditorAction> getRunActions() {
 		List<EditorAction> tmp = new LinkedList<EditorAction>();
 		tmp.addAll(runactions);
 		return tmp;
 	}
 
 	public JToolBar getMenu() {
 		return menu;
 	}
 
 	public SourceFile getFile() {
 		return file;
 	}
 
 	public void setFile(SourceFile file) {
 		this.file = file;
 	}
 
 	public File getSystemfile() {
 		return systemfile;
 	}
 
 	public void setSystemfile(File systemfile) {
 		this.systemfile = systemfile;
 	}
 
 	public boolean isModified() {
 		return modified;
 	}
 
 	public void setModified(boolean modified) {
 		this.modified = modified;
 	}
 
 }
