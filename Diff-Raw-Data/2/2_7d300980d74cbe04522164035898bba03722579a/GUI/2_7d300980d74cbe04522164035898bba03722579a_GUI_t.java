 package org.powerbat.gui;
 
 import java.awt.*;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 
 import javax.swing.*;
 
 import org.powerbat.configuration.Global;
 import org.powerbat.methods.Updater;
 import org.powerbat.projects.Project;
 
 /**
  * Only for use in the boot and the setting and getting of Projects.
  *
  * @author Naux
  * @see GUI#removeTab(String)
  * @see GUI#tabByName(String)
  * @since 1.0
  */
 public class GUI extends JFrame {
 
     private static final long serialVersionUID = 4204914118301301061L;
 
     private static JTabbedPane tabs;
 
     /**
      * Creates a new GUI instance. Should only be done once per
      * <tt>Runtime</tt>. This will set all listeners and handle the
      * initialization of the static arguments to be used later.
      *
      * @since 1.0
      */
     public GUI() {
        super("Powerbat v" + Updater.clientVersion());
         final JPanel main = new JPanel(new BorderLayout());
         final JPanel content = new JPanel(new BorderLayout());
         final JPanel mainpane = new JPanel();
         final ProjectSelector selector = new ProjectSelector();
 
         tabs = new JTabbedPane();
 
         setContentPane(main);
         setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
         setPreferredSize(new Dimension(1000, 600));
         setMinimumSize(getPreferredSize());
         setLocationRelativeTo(getParent());
 
         try {
             setIconImage(Global.getImage(Global.ICON_IMAGE));
         } catch (Exception e) {
             Splash.setStatus("Downloading icon failed");
         }
 
         content.add(selector, BorderLayout.CENTER);
 
         mainpane.setOpaque(false);
         mainpane.add(new JLabel(
                 "<html><head><style>p.padding {padding-left:50px; padding-right:50px;}</style></head><body><p class=\"padding\">Home</p></body></html>"));
         tabs.setTabPlacement(SwingConstants.LEFT);
         tabs.add(content, tabs.getTabCount());
         tabs.setTabComponentAt(tabs.getTabCount() - 1, mainpane);
 
         main.add(tabs);
 
         setVisible(true);
         Splash.setStatus("Complete");
     }
 
     /**
      * Adds a project selected from the ProjectSelector.
      *
      * @param project The selected Project you wish to add.
      * @since 1.0
      */
 
     protected synchronized static void openProject(final Project project) {
         if (project == null) {
             return;
         }
         final JavaEditor temp = new JavaEditor(project);
         temp.setInstructionsText(project.getInstructions());
         if (tabByName(project.getName()) == null) {
             final Image image = Global.getImage(Global.CLOSE_IMAGE);
             final ImageIcon icon = new ImageIcon(image.getScaledInstance(20, 20, Image.SCALE_SMOOTH));
             final JLabel button = new JLabel(icon, JLabel.CENTER);
             final JLabel label = new JLabel(project.getName());
             final JPanel pane = new JPanel(new BorderLayout());
             final JPanel main = new JPanel(new BorderLayout());
 
             pane.setName(project.getName());
             pane.setPreferredSize(new Dimension(140, 30));
             pane.setOpaque(false);
             pane.add(label, BorderLayout.CENTER);
 
             tabs.add(temp, tabs.getTabCount());
             tabs.setTabComponentAt(tabs.getTabCount() - 1, main);
 
             main.add(button, BorderLayout.EAST);
             main.add(pane, BorderLayout.CENTER);
             main.setOpaque(false);
             main.setPreferredSize(new Dimension(170, 30));
 
             label.setLocation(pane.getWidth() - label.getWidth(), pane.getY());
 
             button.setToolTipText("Close Project");
             button.setOpaque(false);
             button.setPreferredSize(new Dimension(30, 30));
             button.addMouseListener(new MouseAdapter() {
                 @Override
                 public void mouseClicked(final MouseEvent e) {
                     removeTab(project.getName());
                 }
             });
 
         }
         tabs.setSelectedComponent(tabByName(project.getName()));
     }
 
     /**
      * This will return the <tt>JavaEditor</tt> that corresponds to the
      * given name.
      *
      * @param name The name of the tab, always portrayed through the Runner's name.
      * @return The tab instance of the Java editor.
      * @since 1.0
      */
 
     public synchronized static JavaEditor tabByName(String name) {
         for (final Component c : tabs.getComponents()) {
             if (c != null && c instanceof JavaEditor) {
                 final JavaEditor c1 = (JavaEditor) c;
                 if (name.equals(c1.getName())) {
                     return c1;
                 }
             }
         }
         return null;
     }
 
     /**
      * Removes the tab from the tabbed pane. Loads the tab by name.
      *
      * @param name Always represented through {@link org.powerbat.interfaces.Runner#getClassName()}
      * @see GUI#tabByName(String)
      * @since 1.0
      */
 
     public synchronized static void removeTab(String name) {
         final JavaEditor cur = tabByName(name);
         if (cur != null) {
             tabs.remove(cur);
             return;
         }
         System.err.println("Failed to close tab " + name);
     }
 
 }
