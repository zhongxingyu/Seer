 package org.geworkbench.engine.skin;
 
 import net.eleritec.docking.DockableAdapter;
 import net.eleritec.docking.DockingManager;
 import net.eleritec.docking.DockingPort;
 import net.eleritec.docking.defaults.ComponentProviderAdapter;
 import net.eleritec.docking.defaults.DefaultDockingPort;
 import org.geworkbench.events.ComponentDockingEvent;
 import org.geworkbench.events.listeners.ComponentDockingListener;
 import org.geworkbench.engine.config.GUIFramework;
 import org.geworkbench.engine.config.VisualPlugin;
 import org.geworkbench.engine.config.PluginDescriptor;
 import org.geworkbench.engine.config.events.AppEventListenerException;
 import org.geworkbench.engine.config.events.EventSource;
 import org.geworkbench.engine.management.ComponentRegistry;
 import org.geworkbench.util.JAutoList;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.util.*;
 
 /**
  * <p>Title: Bioworks</p>
  * <p>Description: Modular Application Framework for Gene Expession, Sequence and Genotype Analysis</p>
  * <p>Copyright: Copyright (c) 2003 -2004</p>
  * <p>Company: Columbia University</p>
  *
  * @author manjunath at genomecenter dot columbia dot edu
  * @version 1.0
  */
 
 public class Skin extends GUIFramework {
 
     static HashMap visualRegistry = new HashMap();
     JPanel contentPane;
     JLabel statusBar = new JLabel();
     BorderLayout borderLayout1 = new BorderLayout();
     JSplitPane jSplitPane1 = new JSplitPane();
     BorderLayout borderLayout5 = new BorderLayout();
     DefaultDockingPort visualPanel = new DefaultDockingPort();
     DefaultDockingPort jControlPanel = new DefaultDockingPort();
     BorderLayout borderLayout2 = new BorderLayout();
     BorderLayout borderLayout3 = new BorderLayout();
     JSplitPane jSplitPane2 = new JSplitPane();
     JSplitPane jSplitPane3 = new JSplitPane();
     BorderLayout borderLayout4 = new BorderLayout();
     DefaultDockingPort selectionPanel = new DefaultDockingPort();
     GridLayout gridLayout1 = new GridLayout();
     JToolBar jToolBar = new JToolBar();
     DefaultDockingPort projectPanel = new DefaultDockingPort();
     BorderLayout borderLayout9 = new BorderLayout();
 
     Hashtable areas = new Hashtable();
 
     DockingNotifier eventSink = new DockingNotifier();
 
     public Skin() {
         registerAreas();
         try {
             jbInit();
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     private void jbInit() throws Exception {
         contentPane = (JPanel) this.getContentPane();
         contentPane.setLayout(borderLayout1);
         Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
         int guiWidth = (int) (dim.getWidth() * 0.9);
         int guiHeight = (int) (dim.getHeight() * 0.9);
         this.setSize(new Dimension(guiWidth, guiHeight));
         this.setTitle(System.getProperty("application.title"));
         statusBar.setText(" ");
         jSplitPane1.setBorder(BorderFactory.createLineBorder(Color.black));
         jSplitPane1.setDoubleBuffered(true);
         jSplitPane1.setContinuousLayout(true);
         jSplitPane1.setBackground(Color.black);
         jSplitPane1.setDividerSize(8);
         jSplitPane1.setOneTouchExpandable(true);
         jSplitPane1.setResizeWeight(0);
         jSplitPane2.setOrientation(JSplitPane.VERTICAL_SPLIT);
         jSplitPane2.setDoubleBuffered(true);
         jSplitPane2.setContinuousLayout(true);
         jSplitPane2.setDividerSize(8);
         jSplitPane2.setOneTouchExpandable(true);
         jSplitPane2.setResizeWeight(0.9);
         jSplitPane2.setMinimumSize(new Dimension(0, 0));
         jSplitPane3.setOrientation(JSplitPane.VERTICAL_SPLIT);
         jSplitPane3.setBorder(BorderFactory.createLineBorder(Color.black));
         jSplitPane3.setDoubleBuffered(true);
         jSplitPane3.setContinuousLayout(true);
         jSplitPane3.setDividerSize(8);
         jSplitPane3.setOneTouchExpandable(true);
         jSplitPane3.setResizeWeight(0.1);
         jSplitPane3.setMinimumSize(new Dimension(0, 0));
         contentPane.add(statusBar, BorderLayout.SOUTH);
         contentPane.add(jSplitPane1, BorderLayout.CENTER);
         jSplitPane1.add(jSplitPane2, JSplitPane.RIGHT);
         jSplitPane2.add(jControlPanel, JSplitPane.BOTTOM);
         jSplitPane2.add(visualPanel, JSplitPane.TOP);
         jSplitPane1.add(jSplitPane3, JSplitPane.LEFT);
         jSplitPane3.add(selectionPanel, JSplitPane.BOTTOM);
         jSplitPane3.add(projectPanel, JSplitPane.LEFT);
         contentPane.add(jToolBar, BorderLayout.NORTH);
         jSplitPane1.setDividerLocation(230);
         jSplitPane2.setDividerLocation((int) (guiHeight * 0.60));
         jSplitPane3.setDividerLocation((int) (guiHeight * 0.35));
         this.setLocation((dim.width - this.getWidth()) / 2, (dim.height - this.getHeight()) / 2);
         visualPanel.setComponentProvider(new ComponentProvider());
         jControlPanel.setComponentProvider(new ComponentProvider());
         selectionPanel.setComponentProvider(new ComponentProvider());
         projectPanel.setComponentProvider(new ComponentProvider());
         final String CANCEL_DIALOG = "cancel-dialog";
         contentPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0), CANCEL_DIALOG);
         contentPane.getActionMap().put(CANCEL_DIALOG, new AbstractAction() {
             public void actionPerformed(ActionEvent event) {
                 chooseComponent();
             }
         });//        contentPane.addKeyListener(new KeyAdapter() {
     }
 
     private static class DialogResult {
         public boolean cancelled = false;
     }
 
     private void chooseComponent() {
         // 1) Get all visual components
         ComponentRegistry registry = ComponentRegistry.getRegistry();
         VisualPlugin[] plugins = registry.getModules(VisualPlugin.class);
         final String[] names = new String[plugins.length];
         for (int i = 0; i < plugins.length; i++) {
             names[i] = registry.getDescriptorForPlugin(plugins[i]).getLabel();
         }
         // 2) Sort alphabetically
         Arrays.sort(names);
         // 3) Create dialog with JAutoText (prefix mode)
         DefaultListModel model = new DefaultListModel();
         for (int i = 0; i < names.length; i++) {
             model.addElement(names[i]);
         }
         final JDialog dialog = new JDialog();
         final DialogResult dialogResult = new DialogResult();
         final JAutoList autoList = new JAutoList(model) {
             protected void keyEntered(KeyEvent event) {
                 if (event.getKeyChar() == '\n') {
                     dialogResult.cancelled = false;
                     dialog.dispose();
                 } else if (event.getKeyChar() == 0x1b) {
                     dialogResult.cancelled = true;
                     dialog.dispose();
                 } else {
                     super.keyEntered(event);
                 }
             }
         };
         autoList.setPrefixMode(true);
        dialog.setTitle("Choose Component");
 //        JPanel panel = new JPanel();
 //        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
 //        dialog.getContentPane().add(panel);
         dialog.getContentPane().add(autoList);
         dialog.setModal(true);
         dialog.pack();
         Dimension size = dialog.getSize();
         Dimension frameSize = getSize();
         int x = getLocationOnScreen().x + (frameSize.width - size.width) / 2;
         int y = getLocationOnScreen().y + (frameSize.height - size.height) / 2;
         // 4) Register enter and esc.
 //        String actionOK = "OK";
 //        String actionCancel = "Cancel";
 //        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), actionOK);
 //        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), actionCancel);
 //        panel.getActionMap().put(actionOK, new AbstractAction() {
 //            public void actionPerformed(ActionEvent event) {
 //                autoList.get
 //            }
 //        });
 //        panel.getActionMap().put(actionCancel, new AbstractAction() {
 //            public void actionPerformed(ActionEvent event) {
 //                dialog.dispose();
 //            }
 //        });
 //
         // 5) Display and get result
         dialog.setBounds(x, y, size.width, size.height);
         dialog.setVisible(true);
         if (!dialogResult.cancelled) {
             int index = autoList.getHighlightedIndex();
             Set keys = areas.keySet();
             boolean found = false;
             for (Iterator iterator = keys.iterator(); iterator.hasNext();) {
                 String key = (String) iterator.next();
                 if (areas.get(key) instanceof DefaultDockingPort) {
                     DefaultDockingPort port = (DefaultDockingPort) areas.get(key);
                     if (port.getDockedComponent() instanceof JTabbedPane) {
                         JTabbedPane pane = (JTabbedPane) port.getDockedComponent();
                         int n = pane.getTabCount();
                         for (int i = 0; i < n; i++) {
                             String title = pane.getTitleAt(i);
                             if (title.equals(names[index])) {
                                 pane.setSelectedIndex(i);
                                 pane.getComponentAt(i).requestFocus();
                                 found = true;
                                 break;
                             }
                         }
                         if (found) {
                             break;
                         }
                     }
                 }
             }
         }
     }
 
     /**
      * Associates Visual Areas with Component Holders
      */
     protected void registerAreas() {
         areas.put(TOOL_AREA, jToolBar);
         areas.put(VISUAL_AREA, visualPanel);
         areas.put(COMMAND_AREA, jControlPanel);
         areas.put(SELECTION_AREA, selectionPanel);
         areas.put(PROJECT_AREA, projectPanel);
     }
 
     public void addToContainer(String areaName, Component visualPlugin) {
         DockableImpl wrapper = new DockableImpl(visualPlugin, visualPlugin.getName());
         DockingManager.registerDockable(wrapper);
         DefaultDockingPort port = (DefaultDockingPort) areas.get(areaName);
         port.dock(wrapper, DockingPort.CENTER_REGION);
         visualRegistry.put(visualPlugin, areaName);
     }
 
     /**
      * Removes the designated <code>visualPlugin</code> from the GUI.
      *
      * @param visualPlugin component to be removed
      */
     public void remove(Component visualPlugin) {
         Collection containers = areas.values();
         Iterator iterator = containers.iterator();
         JPanel container = null;
         Component[] components = null;
         while (iterator.hasNext()) {
             Component comp = (Component) iterator.next();
             if (comp instanceof JPanel) {
                 container = (JPanel) comp;
                 components = container.getComponents();
                 for (int i = 0; i < components.length; i++) {
                     if (components[i] == visualPlugin) {
                         container.remove(visualPlugin);
                         return;
                     }
                 }
             }
         }
         visualRegistry.remove(visualPlugin);
     }
 
     protected void processWindowEvent(WindowEvent e) {
         super.processWindowEvent(e);
         if (e.getID() == WindowEvent.WINDOW_CLOSING) {
             System.exit(0);
         }
     }
 
     public String getVisualArea(Component visualPlugin) {
         return (String) visualRegistry.get(visualPlugin);
     }
 
     public void addToContainer(String areaName, Component visualPlugin, String pluginName) {
         DockableImpl wrapper = new DockableImpl(visualPlugin, pluginName);
         DockingManager.registerDockable(wrapper);
         DefaultDockingPort port = (DefaultDockingPort) areas.get(areaName);
         port.dock(wrapper, DockingPort.CENTER_REGION);
         visualRegistry.put(visualPlugin, areaName);
     }
 
     private void dockingFinished(DockableImpl comp) {
         for (Enumeration e = areas.keys(); e.hasMoreElements();) {
             String area = (String) e.nextElement();
             Component port = (Component) areas.get(area);
             Component container = comp.getDockable().getParent();
             if (container instanceof JTabbedPane || container instanceof JSplitPane) {
                 if (container.getParent() == port) {
                     eventSink.throwEvent(comp.getPlugin(), area);
                 }
             } else if (container instanceof DefaultDockingPort) {
                 if (container == port)
                     eventSink.throwEvent(comp.getPlugin(), area);
             }
         }
     }
 
     private class DockableImpl extends DockableAdapter {
 
         private JPanel wrapper = null;
         private JLabel initiator = null;
         private String description = null;
         private Component plugin = null;
         private JPanel buttons = new JPanel();
         private JPanel topBar = new JPanel();
         private JButton docker = new JButton();
         private JButton minimize = new JButton();
         private JButton remove = new JButton();
         private boolean docked = true;
 
         private ImageIcon close_grey = new ImageIcon(Skin.class.getResource("close_grey.gif"));
         private ImageIcon close = new ImageIcon(Skin.class.getResource("close.gif"));
         private ImageIcon close_active = new ImageIcon(Skin.class.getResource("close_active.gif"));
         private ImageIcon min_grey = new ImageIcon(Skin.class.getResource("min_grey.gif"));
         private ImageIcon min = new ImageIcon(Skin.class.getResource("min.gif"));
         private ImageIcon min_active = new ImageIcon(Skin.class.getResource("min_active.gif"));
         private ImageIcon max_grey = new ImageIcon(Skin.class.getResource("max_grey.gif"));
         private ImageIcon max = new ImageIcon(Skin.class.getResource("max.gif"));
         private ImageIcon max_active = new ImageIcon(Skin.class.getResource("max_active.gif"));
         private ImageIcon dock_grey = new ImageIcon(Skin.class.getResource("dock_grey.gif"));
         private ImageIcon dock = new ImageIcon(Skin.class.getResource("dock.gif"));
         private ImageIcon dock_active = new ImageIcon(Skin.class.getResource("dock_active.gif"));
         private ImageIcon undock_grey = new ImageIcon(Skin.class.getResource("undock_grey.gif"));
         private ImageIcon undock = new ImageIcon(Skin.class.getResource("undock.gif"));
         private ImageIcon undock_active = new ImageIcon(Skin.class.getResource("undock_active.gif"));
 
         DockableImpl(Component plugin, String desc) {
             this.plugin = plugin;
             wrapper = new JPanel();
 
             docker.setPreferredSize(new Dimension(16, 16));
             docker.setBorderPainted(false);
             docker.setIcon(undock_grey);
             docker.setRolloverEnabled(true);
             docker.setRolloverIcon(undock);
             docker.setPressedIcon(undock_active);
             docker.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent e) {
                     docker_actionPerformed(e);
                 }
             });
 
             minimize.setPreferredSize(new Dimension(16, 16));
             minimize.setBorderPainted(false);
             minimize.setIcon(min_grey);
             minimize.setRolloverEnabled(true);
             minimize.setRolloverIcon(min);
             minimize.setPressedIcon(min_active);
             minimize.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent e) {
                     minimize_actionPerformed(e);
                 }
             });
 
             remove.setPreferredSize(new Dimension(16, 16));
             remove.setBorderPainted(false);
             remove.setIcon(close_grey);
             remove.setRolloverEnabled(true);
             remove.setRolloverIcon(close);
             remove.setPressedIcon(close_active);
             remove.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent e) {
                     remove_actionPerformed(e);
                 }
             });
 
             buttons.setLayout(new GridLayout(1, 3));
             buttons.add(docker);
             buttons.add(minimize);
             buttons.add(remove);
 
             initiator = new JLabel(" ");
             initiator.setForeground(Color.darkGray);
             initiator.setBackground(Color.getHSBColor(0.0f, 0.0f, 0.6f));
             initiator.setOpaque(true);
             initiator.addMouseListener(new MouseAdapter() {
                 public void mouseEntered(MouseEvent me) {
                     setMoveCursor(me);
                 }
 
                 public void mouseExited(MouseEvent me) {
                     setDefaultCursor(me);
                 }
             });
 
             topBar.setLayout(new BorderLayout());
             topBar.add(initiator, BorderLayout.CENTER);
             topBar.add(buttons, BorderLayout.EAST);
 
             wrapper.setLayout(new BorderLayout());
             wrapper.add(topBar, BorderLayout.NORTH);
             wrapper.add(plugin, BorderLayout.CENTER);
             description = desc;
         }
 
         private JFrame frame = null;
 
         private void docker_actionPerformed(ActionEvent e) {
             String areaName = getVisualArea(this.getPlugin());
             DefaultDockingPort port = (DefaultDockingPort) areas.get(areaName);
             if (docked) {
                 port.undock(wrapper);
                 port.reevaluateContainerTree();
                 port.revalidate();
                 docker.setIcon(dock_grey);
                 docker.setRolloverIcon(dock);
                 docker.setPressedIcon(dock_active);
                 docker.setSelected(false);
                 frame = new JFrame(description);
                 frame.setUndecorated(false);
                 frame.addWindowListener(new WindowAdapter() {
                     public void windowClosing(WindowEvent we) {
                         remove_actionPerformed(we);
                     }
                 });
                 frame.getContentPane().setLayout(new BorderLayout());
                 frame.getContentPane().add(wrapper, BorderLayout.CENTER);
                 frame.pack();
                 frame.setVisible(true);
                 docked = false;
                 return;
             } else {
                 if (frame != null) {
                     docker.setIcon(undock_grey);
                     docker.setRolloverIcon(undock);
                     docker.setPressedIcon(undock_active);
                     docker.setSelected(false);
                     port.dock(this, DockingPort.CENTER_REGION);
                     port.reevaluateContainerTree();
                     port.revalidate();
                     docked = true;
                     frame.getContentPane().remove(wrapper);
                     frame.dispose();
                 }
             }
         }
 
         private void minimize_actionPerformed(ActionEvent e) {
 
         }
 
         private void remove_actionPerformed(AWTEvent e) {
             String areaName = getVisualArea(getPlugin());
             if (areaName != null) {
                 DefaultDockingPort port = (DefaultDockingPort) areas.get(areaName);
                 if (docked) {
                     port.undock(wrapper);
                     port.reevaluateContainerTree();
                     port.revalidate();
                     remove(getPlugin());
                 } else {
                     remove(getPlugin());
                     frame.dispose();
                 }
             }
         }
 
         private void setMoveCursor(MouseEvent me) {
             initiator.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
         }
 
         private void setDefaultCursor(MouseEvent me) {
             initiator.setCursor(Cursor.getDefaultCursor());
         }
 
         public Component getDockable() {
             return wrapper;
         }
 
         public String getDockableDesc() {
             return description;
         }
 
         public Component getInitiator() {
             return initiator;
         }
 
         public Component getPlugin() {
             return plugin;
         }
 
         public void dockingCompleted() {
             dockingFinished(this);
         }
     }
 
     private class ComponentProvider extends ComponentProviderAdapter {
         public JTabbedPane createTabbedPane() {
             JTabbedPane pane = new JTabbedPane();
             pane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
             return pane;
         }
     }
 
     private class DockingNotifier extends EventSource {
         public void throwEvent(Component source, String region) {
             try {
                 throwEvent(ComponentDockingListener.class, "dockingAreaChanged", new ComponentDockingEvent(this, source, region));
             } catch (AppEventListenerException aele) {
                 aele.printStackTrace();
             }
         }
     }
 }
