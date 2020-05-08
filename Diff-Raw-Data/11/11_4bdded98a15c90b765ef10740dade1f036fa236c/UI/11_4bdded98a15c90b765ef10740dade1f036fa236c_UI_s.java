 package de.aidger.view;
 
 import static de.aidger.utils.Translation._;
 
 import java.awt.BorderLayout;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.Frame;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseWheelEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.ActionMap;
 import javax.swing.ButtonGroup;
 import javax.swing.ButtonModel;
 import javax.swing.ImageIcon;
 import javax.swing.InputMap;
 import javax.swing.JCheckBox;
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.JScrollPane;
 import javax.swing.JTabbedPane;
 import javax.swing.JToolBar;
 import javax.swing.KeyStroke;
 import javax.swing.WindowConstants;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import de.aidger.controller.ActionNotFoundException;
 import de.aidger.controller.ActionRegistry;
 import de.aidger.controller.actions.AboutAction;
 import de.aidger.controller.actions.ExitAction;
 import de.aidger.controller.actions.HelpAction;
 import de.aidger.controller.actions.PrintAction;
 import de.aidger.controller.actions.SettingsAction;
 import de.aidger.controller.actions.TaskPaneAction;
 import de.aidger.controller.actions.TaskPaneAction.Task;
 import de.aidger.model.AbstractModel;
 import de.aidger.model.Runtime;
 import de.aidger.model.models.HourlyWage;
 import de.aidger.utils.Anonymizer;
 import de.aidger.utils.Logger;
 import de.aidger.view.models.GenericListModel;
 import de.aidger.view.models.TableModel;
 import de.aidger.view.tabs.EmptyTab;
 import de.aidger.view.tabs.Tab;
 import de.aidger.view.tabs.ViewerTab;
 import de.aidger.view.tabs.WelcomeTab;
 import de.aidger.view.tabs.ViewerTab.DataType;
 import de.unistuttgart.iste.se.adohive.exceptions.AdoHiveException;
 
 /**
  * The UI manages the main window and all its tabs. The main window consists of
  * the menu bar, the navigation and a tabbed pane which holds all tabs in the
  * center of the main window.
  * 
  * @author aidGer Team
  */
 @SuppressWarnings("serial")
 public final class UI extends JFrame {
     /**
      * Holds an instance of this class.
      */
     private static UI instance = null;
 
     /**
      * The tabbed plane that will contain the tabs.
      */
     private JTabbedPane tabbedPane;
 
     /**
      * A change listener for the tabbed pane
      */
     private ChangeListener tabbedPaneListener;
 
     /**
      * A list that contains the tabs.
      */
     private final List<Tab> tabs = new ArrayList<Tab>();
 
     /**
      * The label containing a status message.
      */
     private JLabel statusLabel;
 
     /**
      * A timer used for status messages.
      */
     private Timer statusTimer = new Timer();
 
     /**
      * Quick Settings CheckBox for the remember tabs setting.
      */
     private JCheckBox rememberCheckBox;
 
     /**
      * Quick Settings CheckBox for the open reports setting.
      */
     private JCheckBox openCheckBox;
 
     /**
      * Quick Settings RadioButton for the pessimistic calculation setting.
      */
     private JRadioButton pessimisticRadioButton;
 
     /**
      * Quick Settings RadioButton for the historical calculation setting.
      */
     private JRadioButton historicalRadioButton;
 
     /**
      * Quick Settings ButtonGroup for the calculation method setting.
      */
     private ButtonGroup calculationGroup;
 
     /**
      * Creates the main window of the application.
      */
     private UI() {
         super();
 
         // Create the menu bar
         setJMenuBar(createMainMenuBar());
 
         // Build the main panel
         Container contentPane = getContentPane();
         contentPane.setLayout(new BorderLayout());
 
         contentPane.add(createToolbar(), BorderLayout.PAGE_START);
         contentPane.add(createTaskPane(), BorderLayout.LINE_START);
         contentPane.add(createTabbedPane(), BorderLayout.CENTER);
         contentPane.add(createStatusPane(), BorderLayout.PAGE_END);
 
         pack();
 
         int posX = (int) ((int) (getSize().getWidth() / 2) - (this.getSize()
             .getWidth() / 2));
         int posY = (int) ((int) (getSize().getHeight() / 2) - (this.getSize()
             .getHeight() / 2));
 
         setLocation(posX, posY);
 
         setMinimumSize(new Dimension(950, 700));
 
         setExtendedState(Frame.MAXIMIZED_BOTH);
 
         setTitle("aidGer");
 
         // Initialize window event handling
         setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
         addWindowListener(new WindowAdapter() {
             @Override
             public void windowClosing(WindowEvent event) {
                 Action action;
                 try {
                     action = ActionRegistry.getInstance().get(
                         ExitAction.class.getName());
 
                     if (action != null) {
                         ActionEvent evt = new ActionEvent(event.getSource(),
                             event.getID(), null);
 
                         action.actionPerformed(evt);
                     }
                 } catch (ActionNotFoundException e) {
                     displayError(e.getMessage());
                 }
             }
         });
 
         // shortcuts for better tab handling
         JComponent comp = (JComponent) getContentPane();
         ActionMap actionMap = comp.getActionMap();
         InputMap inputMap = comp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
 
         inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_T,
             ActionEvent.CTRL_MASK), "addNewTab");
 
         inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_W,
             ActionEvent.CTRL_MASK), "removeCurrentTab");
 
         actionMap.put("addNewTab", new AbstractAction() {
             public void actionPerformed(ActionEvent e) {
                 addNewTab(new EmptyTab());
             }
         });
 
         actionMap.put("removeCurrentTab", new AbstractAction() {
             public void actionPerformed(ActionEvent e) {
                 removeCurrentTab();
             }
         });
 
         // Try to display saved tabs or add the welcome tab to the UI.
         if (!displaySavedTabs()) {
             addNewTab(new WelcomeTab());
         }
     }
 
     /**
      * Provides access to an instance of this class.
      * 
      * @return instance of this UI
      */
     public synchronized static UI getInstance() {
         if (instance == null) {
             instance = new UI();
         }
 
         return instance;
     }
 
     /**
      * Display an error message in a dialog window.
      * 
      * @param error
      *            The error message to display
      */
     public static void displayError(String error) {
        UI.getInstance().setStatusMessage(error);
         JOptionPane.showMessageDialog(instance, error, _("Error"),
             JOptionPane.ERROR_MESSAGE);
         Logger.error(error);
     }
 
     /**
      * Display an info message in a dialog window.
      * 
      * @param error
      *            The info message to display
      */
     public static void displayInfo(String info) {
        UI.getInstance().setStatusMessage(info);
         JOptionPane.showMessageDialog(instance, info, _("Info"),
             JOptionPane.INFORMATION_MESSAGE);
         Logger.info(info);
     }
 
     /**
      * Runs and displays the graphical user interface.
      */
     public void run() {
         setVisible(true);
 
         // start maximized
         setExtendedState(Frame.MAXIMIZED_BOTH);
 
         // Display the first start dialog on first start
         if (Runtime.getInstance().isFirstStart()) {
             FirstStartDialog dlg = new FirstStartDialog(this, true);
             dlg.setVisible(true);
         }
 
         // Anonymize assistants and display a message
         int count = Anonymizer.anonymizeAssistants();
         if (count > 0) {
             JOptionPane.showMessageDialog(this, MessageFormat.format(
                 _("{0} assistants have been anonymized"),
                 new Object[] { count }));
         }
     }
 
     /**
      * Adds a new tab at the given index to the tabbed plane.
      * 
      * @param tab
      *            The tab to be added.
      * @param index
      *            The position where the tab will be added
      */
     public void addNewTab(Tab tab, int index) {
         Logger.debug(MessageFormat.format(
             _("Adding new tab \"{0}\" at position {1}"), new Object[] {
                     tab.getTabName(), index }));
 
         tabbedPane.removeChangeListener(tabbedPaneListener);
 
         tab.performBeforeOpen();
 
         tabbedPane.add(new JScrollPane(tab), index);
         tabbedPane.setTabComponentAt(index, new CloseTabComponent(tab
             .getTabName()));
 
         tabbedPane.setSelectedIndex(index);
 
         tabbedPane.addChangeListener(tabbedPaneListener);
         saveCurrentTabs();
 
         if (!tabs.contains(tab)) {
             tabs.add(tab);
         }
     }
 
     /**
      * Adds a new tab at the end of the tabbed pane.
      * 
      * @param tab
      *            The tab to be added
      */
     public void addNewTab(Tab tab) {
         addNewTab(tab, tabbedPane.getTabCount() - 1);
     }
 
     /**
      * Adds a new empty tab and focus it if "+" button was clicked
      */
     public void addNewTab() {
         int index = tabbedPane.getTabCount() - 1;
 
         if (tabbedPane.getSelectedIndex() == index) {
             addNewTab(new EmptyTab());
         }
     }
 
     /**
      * Removes the tab at given index.
      * 
      * @param index
      *            The index identifies the tab that will be removed
      * @return the removed tab
      */
     public Tab removeTabAt(int index) {
         Logger.debug(MessageFormat.format(_("Removing {0}. tab"),
             new Object[] { index + 1 }));
 
         tabbedPane.removeChangeListener(tabbedPaneListener);
 
         Tab old = ((Tab) ((JScrollPane) tabbedPane.getComponentAt(index))
             .getViewport().getView());
 
         old.performBeforeClose();
 
         tabbedPane.remove(index);
 
         if (tabbedPane.getTabCount() == 1) {
             Tab welcomeTab = new WelcomeTab();
             tabbedPane.add(new JScrollPane(welcomeTab), 0);
             tabbedPane.setTabComponentAt(0, new CloseTabComponent(welcomeTab
                 .getTabName()));
 
             tabbedPane.setSelectedIndex(0);
         } else if (index == tabbedPane.getTabCount() - 1
                 && tabbedPane.getSelectedIndex() == tabbedPane.getTabCount() - 1) {
             tabbedPane.setSelectedIndex(index - 1);
         }
 
         tabbedPane.addChangeListener(tabbedPaneListener);
         saveCurrentTabs();
 
         return old;
     }
 
     /**
      * Removes the current tab.
      * 
      * @return the removed tab
      */
     public Tab removeCurrentTab() {
         return removeTabAt(tabbedPane.getSelectedIndex());
     }
 
     /**
      * Replaces the current tab with the given one. The old tab will be removed.
      * 
      * @param tab
      *            the new current tab
      */
     public void replaceCurrentTab(Tab tab) {
         int index = tabbedPane.getSelectedIndex();
 
         addNewTab(tab, index);
         Tab predecessor = removeTabAt(index + 1);
         tab.setPredecessor(predecessor);
     }
 
     /**
      * Get the currently selected tab.
      * 
      * @return The selected tab
      */
     public Tab getCurrentTab() {
         return (Tab) ((JScrollPane) tabbedPane.getSelectedComponent())
             .getViewport().getView();
     }
 
     /**
      * Returns all opened tabs.
      * 
      * @return all opened tabs.
      */
     public List<Tab> getTabs() {
         return tabs;
     }
 
     /**
      * Sets the current tab on the tabbed plane to the one specified.
      * 
      * @param tab
      *            The tab to be set as current. The tab must exist already.
      */
     public void setCurrentTab(Tab tab) {
         int index = tabbedPane.indexOfComponent(tab);
 
         if (index != -1) {
             setCurrentTabAt(index);
         }
     }
 
     /**
      * Sets the current tab to the tab at given position.
      * 
      * @param index
      */
     public void setCurrentTabAt(int index) {
         Tab newTab = (Tab) ((JScrollPane) tabbedPane.getComponentAt(index))
             .getViewport().getView();
 
         Logger.debug(MessageFormat.format(_("Setting current tab to \"{0}\""),
             new Object[] { newTab.getTabName() }));
 
         tabbedPane.setSelectedIndex(index);
     }
 
     /**
      * Sets the current to the previous tab.
      */
     public void setPreviousTab() {
         int prevIndex = tabbedPane.getSelectedIndex() - 1;
 
         if (prevIndex < 0) {
             prevIndex = tabbedPane.getTabCount() - 2;
         }
 
         setCurrentTabAt(prevIndex);
     }
 
     /**
      * Sets the current to the next tab.
      */
     public void setNextTab() {
         int nextIndex = tabbedPane.getSelectedIndex() + 1;
 
         if (nextIndex == tabbedPane.getTabCount() - 1) {
             nextIndex = 0;
         }
 
         setCurrentTabAt(nextIndex);
     }
 
     /**
      * Retrieves the tabbed pane.
      * 
      * @return the tabbed pane
      */
     public JTabbedPane getTabbedPane() {
         return tabbedPane;
     }
 
     /**
      * Adds all existing observers to the given model.
      * 
      * @param model
      *            the observerd model
      * @param modelBeforeEdit
      *            the observerd model before it is edited
      * @param type
      *            the type of the observed model
      */
     @SuppressWarnings("unchecked")
     public void addObserversTo(AbstractModel model,
             AbstractModel modelBeforeEdit, DataType type) {
         /*
          * model should also be observed by the already existing table and list
          * models of the same type
          */
         for (Tab t : getTabs()) {
             // table models
             if (t instanceof ViewerTab && ((ViewerTab) t).getType() == type) {
                 TableModel tM = ((ViewerTab) t).getTableModel();
 
                 tM.setModelBeforeEdit(modelBeforeEdit);
 
                 model.addObserver(tM);
             }
 
             // list models
             List<GenericListModel> lMs = t.getListModels();
 
             for (GenericListModel lM : lMs) {
                 if (lM.getDataType() == type) {
                     lM.setModelBeforeEdit(modelBeforeEdit);
 
                     model.addObserver(lM);
                 }
             }
         }
     }
 
     /**
      * Set the message of the status pane.
      * 
      * @param message
      *            The message to set
      */
     public void setStatusMessage(String message) {
         statusLabel.setText(message);
 
         statusTimer.cancel();
 
         statusTimer = new Timer();
         statusTimer.schedule(new TimerTask() {
             @Override
             public void run() {
                 setStatusMessage(_("Ready"));
             }
         }, 3000);
     }
 
     /**
      * Refresh the Quick Settings CheckBoxes.
      */
     public void refreshQuickSettings() {
         rememberCheckBox.setSelected(Boolean.valueOf(Runtime.getInstance()
             .getOption("auto-save")));
         openCheckBox.setSelected(Boolean.valueOf(Runtime.getInstance()
             .getOption("auto-open")));
         ButtonModel model = Runtime.getInstance().getOption("calc-method")
             .equals("1") ? pessimisticRadioButton.getModel()
                 : historicalRadioButton.getModel();
         calculationGroup.setSelected(model, true);
     }
 
     /**
      * Sets up the main menu bar.
      */
     private JMenuBar createMainMenuBar() {
         try {
             JMenuBar menuBar = new JMenuBar();
             menuBar.add(createFileMenu());
             menuBar.add(createHelpMenu());
 
             return menuBar;
         } catch (ActionNotFoundException e) {
             displayError(e.getMessage());
             return null;
         }
     }
 
     /**
      * Sets up the file menu.
      * 
      * @throws ActionNotFoundException
      */
     private JMenu createFileMenu() throws ActionNotFoundException {
         JMenu fileMenu = new JMenu(_("File"));
 
         fileMenu.add(new JMenuItem(ActionRegistry.getInstance().get(
             PrintAction.class.getName())));
         fileMenu.add(new JMenuItem(ActionRegistry.getInstance().get(
             SettingsAction.class.getName())));
         fileMenu.addSeparator();
         fileMenu.add(new JMenuItem(ActionRegistry.getInstance().get(
             ExitAction.class.getName())));
 
         return fileMenu;
     }
 
     /**
      * Sets up the help menu.
      * 
      * @throws ActionNotFoundException
      */
     private JMenu createHelpMenu() throws ActionNotFoundException {
         JMenu helpMenu = new JMenu(_("Help"));
 
         helpMenu.add(new JMenuItem(ActionRegistry.getInstance().get(
             HelpAction.class.getName())));
         helpMenu.add(new JMenuItem(ActionRegistry.getInstance().get(
             AboutAction.class.getName())));
 
         return helpMenu;
     }
 
     /**
      * Sets up the toolbar.
      */
     private JToolBar createToolbar() {
         JToolBar toolBar = new JToolBar();
 
         // toolBar.add(ActionRegistry.getInstance().get(OpenAction.class.getName()));
 
         return toolBar;
     }
 
     /**
      * Creates a button for the task pane.
      * 
      * @param name
      *            the button name
      * @param tab
      *            the tab that will be opened on click
      * @return the task pane button
      */
     private TaskPaneButton createTaskPaneButton(String name, Task task) {
         return new TaskPaneButton(new TaskPaneAction(name, task));
     }
 
     /**
      * Sets up the task pane.
      */
     private JScrollPane createTaskPane() {
         TaskPaneContainer tpc = new TaskPaneContainer();
 
         TaskPane tpMasterData = new TaskPane(_("Master Data Management"));
 
         tpMasterData.add(createTaskPaneButton(_("Courses"), Task.ViewCourses));
         tpMasterData.add(createTaskPaneButton(_("Assistants"),
             Task.ViewAssistants));
         tpMasterData.add(createTaskPaneButton(_("Financial Categories"),
             Task.ViewFinancialCategories));
         tpMasterData.add(createTaskPaneButton(_("Hourly Wages"),
             Task.ViewHourlyWages));
 
         TaskPane tpEmployments = new TaskPane(_("Employments"));
 
         tpEmployments.add(createTaskPaneButton(_("Show employments"),
             Task.ViewEmployments));
         tpEmployments.add(createTaskPaneButton(_("Hire assistant"),
             Task.CreateNewEmployment));
         tpEmployments.add(createTaskPaneButton(_("Show contracts"),
             Task.ViewContracts));
 
         TaskPane tpActivities = new TaskPane(_("Activities"));
         tpActivities.add(createTaskPaneButton(_("Show activities"),
             Task.ViewActivities));
         tpActivities.add(createTaskPaneButton(_("Export activities"),
             Task.ExportActivities));
 
         TaskPane tpReports = new TaskPane(_("Reports"));
         tpReports.add(createTaskPaneButton(_("Full Balance"),
             Task.ViewFullBalance));
         tpReports.add(createTaskPaneButton(_("Annual Balance"),
             Task.ViewAnnualBalance));
         tpReports.add(createTaskPaneButton(_("Semester Balance"),
             Task.ViewSemesterBalance));
         tpReports.add(createTaskPaneButton(_("Activity Report"),
             Task.ViewActivityReport));
 
         TaskPane tpControlling = new TaskPane(_("Controlling"));
         tpControlling.add(createTaskPaneButton(_("Check courses"),
             Task.CheckCourses));
         tpControlling.add(createTaskPaneButton(_("Check assistants"),
             Task.CheckAssistants));
 
         TaskPane tpQuickSettings = new TaskPane(_("Quick Settings"));
 
         rememberCheckBox = new JCheckBox(_("Remember my tabs"));
         rememberCheckBox
             .addChangeListener(new javax.swing.event.ChangeListener() {
                 public void stateChanged(ChangeEvent e) {
                     Runtime.getInstance().setOption(
                         "auto-save",
                         Boolean.toString(((JCheckBox) e.getSource())
                             .isSelected()));
                 }
             });
 
         openCheckBox = new JCheckBox(_("Open reports instantly"));
         openCheckBox.addChangeListener(new javax.swing.event.ChangeListener() {
             public void stateChanged(ChangeEvent e) {
                 Runtime.getInstance().setOption("auto-open",
                     Boolean.toString(((JCheckBox) e.getSource()).isSelected()));
             }
         });
 
         historicalRadioButton = new JRadioButton(_("Historical calculation"));
         historicalRadioButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent arg0) {
                 Runtime.getInstance().setOption("calc-method", "0");
             }
         });
 
         pessimisticRadioButton = new JRadioButton(_("Pessimistic calculation"));
         pessimisticRadioButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent arg0) {
                 Runtime.getInstance().setOption("calc-method", "1");
             }
         });
 
         calculationGroup = new ButtonGroup();
         calculationGroup.add(pessimisticRadioButton);
         calculationGroup.add(historicalRadioButton);
 
         refreshQuickSettings();
 
         tpQuickSettings.add(rememberCheckBox);
         tpQuickSettings.add(openCheckBox);
         tpQuickSettings.add(pessimisticRadioButton);
         tpQuickSettings.add(historicalRadioButton);
 
         tpc.addTask(tpMasterData);
         tpc.addTask(tpEmployments);
         tpc.addTask(tpActivities);
         tpc.addTask(tpReports);
         tpc.addTask(tpControlling);
         tpc.addTask(tpQuickSettings);
         tpc.addFiller();
 
         String[] collapsed = Runtime.getInstance().getOptionArray(
             "taskPaneCollapsed");
 
         if (collapsed == null) {
             collapsed = new String[] { "1", "2", "3", "4", "5" };
 
             Runtime.getInstance()
                 .setOptionArray("taskPaneCollapsed", collapsed);
         }
 
         for (int i = 0; i < collapsed.length; ++i) {
             if (!collapsed[i].isEmpty() && !collapsed[i].equals("null")) {
                 tpc.getTask(Integer.valueOf(collapsed[i])).setExpanded(false);
             }
         }
 
         return new JScrollPane(tpc, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
             JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
     }
 
     /**
      * Sets up the tabbed pane in the middle.
      */
     private JTabbedPane createTabbedPane() {
         tabbedPane = new JTabbedPane();
 
         tabbedPaneListener = new ChangeListener() {
             public void stateChanged(ChangeEvent e) {
                 addNewTab();
             }
         };
 
         tabbedPane.add(new JPanel(), new ImageIcon(getClass().getResource(
             "/de/aidger/view/icons/ui-tab--plus.png")));
         tabbedPane.setToolTipTextAt(0, _("Open a new tab"));
 
         tabbedPane.addChangeListener(tabbedPaneListener);
 
         tabbedPane.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);
 
         // mouse wheel support for tabs
         tabbedPane.addMouseWheelListener(new MouseAdapter() {
             @Override
             public void mouseWheelMoved(MouseWheelEvent e) {
                 if (tabbedPane.indexAtLocation(e.getPoint().x, e.getPoint().y) < 0) {
                     return;
                 }
 
                 if (e.getWheelRotation() < 0) {
                     setPreviousTab();
                 } else {
                     setNextTab();
                 }
             }
         });
 
         return tabbedPane;
     }
 
     /**
      * Sets up the panel that contains the status label.
      */
     private JPanel createStatusPane() {
         JPanel statusPane = new JPanel();
         statusPane.setLayout(new javax.swing.BoxLayout(statusPane, WIDTH));
 
         statusLabel = new JLabel(_("Ready"));
         statusPane.add(statusLabel);
 
         return statusPane;
     }
 
     /**
      * Save the currently open tabs if required.
      */
     private void saveCurrentTabs() {
         if (!Boolean.valueOf(Runtime.getInstance().getOption("auto-save"))) {
             return;
         }
 
         int count = tabbedPane.getTabCount();
         String[] list = new String[count - 1]; // -1 because of
         // CloseTabComponent
 
         for (int i = 0; i < count - 1; ++i) {
             Tab t = (Tab) ((JScrollPane) tabbedPane.getComponentAt(i))
                 .getViewport().getView();
             String tab = t.toString();
             if (tab != null) {
                 list[i] = tab;
             }
         }
 
         Runtime.getInstance().setOptionArray("tablist", list);
     }
 
     /**
      * Retrieve the saved tabs from the config and display them.
      * 
      * @return True if it succeeded, false if the default should be displayed
      */
     @SuppressWarnings("unchecked")
     private boolean displaySavedTabs() {
         if (!Boolean.valueOf(Runtime.getInstance().getOption("auto-save"))) {
             return false;
         }
 
         String[] list = Runtime.getInstance().getOptionArray("tablist");
         if (list == null || list.length == 0) {
             return false;
         }
 
         /* Loop through all tabs */
         for (String tab : list) {
             String classname = null;
             String[] params = new String[0];
             try {
                 /* Separate classnames and parameters */
                 int sep = tab.indexOf('<');
                 if (sep >= 0) {
                     classname = tab.substring(0, sep).trim();
                     params = tab.substring(sep + 1).split("<");
                 } else {
                     classname = tab.trim();
                 }
 
                 if (classname.equals("null")) {
                     continue;
                 }
 
                 /* Initialize the tab class */
                 Class clazz = Class.forName(classname);
                 Class[] searchParams = new Class[params.length];
                 List<Object> ctrParams = new ArrayList<Object>();
 
                 /* Convert the string parameter into the correct type */
                 for (int i = 0; i < params.length; ++i) {
                     String[] parts = params[i].split("@");
                     Class current = Class.forName(parts[0]);
                     searchParams[i] = current.getSuperclass().equals(
                         AbstractModel.class) ? AbstractModel.class : current;
 
                     if (current.equals(Integer.class)) {
                         ctrParams.add(Integer.parseInt(parts[1]));
                     } else if (current.isEnum()) {
                         Class obj = Class.forName(parts[0]);
                         ctrParams.add(Enum.valueOf(obj, parts[1]));
                     } else if (current.getSuperclass().equals(
                         AbstractModel.class)) {
                         Class obj = Class.forName(parts[0]);
                         AbstractModel a = (AbstractModel) obj.newInstance();
                         Object o;
 
                         if (current.equals(HourlyWage.class)) {
                             o = a.getByKeys(parts[1], Byte.valueOf(parts[2]),
                                 Short.valueOf(parts[3]));
                         } else {
                             o = a.getById(Integer.parseInt(parts[1]));
                         }
 
                         ctrParams.add(obj.getConstructor(
                             o.getClass().getInterfaces()[0]).newInstance(o));
                     } else if (current.getSuperclass().getSuperclass().equals(
                         AbstractModel.class)) {
                         Class obj = Class.forName(parts[0]);
                         AbstractModel a = (AbstractModel) obj.getSuperclass()
                             .newInstance();
                         Object o = a.getById(Integer.parseInt(parts[1]));
                         ctrParams.add(obj.getConstructor(
                             o.getClass().getInterfaces()[0]).newInstance(o));
                         searchParams[i] = AbstractModel.class;
                     } else {
                         Class obj = Class.forName(parts[0]);
                         ctrParams.add(obj.cast(parts[1]));
                     }
                 }
 
                 /* Get the constructor and create the tab */
                 Constructor ctr = clazz.getConstructor(searchParams);
                 addNewTab((Tab) ctr.newInstance(ctrParams.toArray()));
             } catch (ClassNotFoundException ex) {
                 Logger.error(MessageFormat.format(
                     _("Could not find tab class {0}"), new Object[] { ex
                         .getMessage() }));
             } catch (NoSuchMethodException ex) {
                 Logger.error(MessageFormat.format(
                     _("Could not find the correct constructor: {0}"),
                     new Object[] { ex.getMessage() }));
             } catch (InstantiationException ex) {
                 Logger.error(MessageFormat.format(
                     _("Instantiating the class {0} failed"), new Object[] { ex
                         .getMessage() }));
             } catch (IllegalAccessException ex) {
                 Logger.error(_("Can't access a needed function"));
             } catch (IllegalArgumentException ex) {
                 Logger.error(_("Incorrect arguments saved for a tab"));
                 Logger.error(ex.getMessage());
             } catch (InvocationTargetException ex) {
                 Logger.error(ex.getMessage());
             } catch (AdoHiveException ex) {
                 Logger.error(_("Getting the model from the database failed"));
             }
         }
 
         return tabbedPane.getTabCount() > 1;
     }
 
 }
