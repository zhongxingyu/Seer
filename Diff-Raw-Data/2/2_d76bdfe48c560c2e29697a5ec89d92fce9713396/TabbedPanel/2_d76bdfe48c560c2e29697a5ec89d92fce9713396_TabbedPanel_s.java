 package at.tuwien.sbc.feeder.gui;
 
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.GridLayout;
 import java.awt.event.KeyEvent;
 
 import javax.swing.ImageIcon;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTabbedPane;
 
 public class TabbedPanel extends JPanel {
     
     private JTabbedPane tabs;
     
     public TabbedPanel() {
         super(new GridLayout(1, 1));
         
         tabs = new JTabbedPane();
         ImageIcon icon = createImageIcon("images/organize.jpg");
         
         JComponent panel1 = makeTextPanel("Panel #1");
         this.tabs.addTab("Organization", icon, panel1, "Does nothing");
         this.tabs.setMnemonicAt(0, KeyEvent.VK_1);
         
         icon = createImageIcon("images/overview.jpg");
         PeerEventsPanel pnlPE = new PeerEventsPanel();
         this.tabs.addTab("Overview", icon, pnlPE, "Does twice as much nothing");
         this.tabs.setMnemonicAt(1, KeyEvent.VK_2);
         
         // Add the tabbed pane to this panel.
         add(this.tabs);
         
         // The following line enables to use scrolling tabs.
         this.tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
     }
     
     public void enableTab(int tab, boolean enable) {
         if (tab == -1) {
             this.setEnabled(enable);
             for (Component c : this.tabs.getComponents()) {
                 this.recursiveEnabling((Container) c, enable);
                 this.recursiveEnabling((Container) c, enable);
             }
         } else {
             Component t = this.tabs.getComponentAt(tab);
             t.setEnabled(enable);
             this.recursiveEnabling((Container) this.tabs.getComponentAt(tab), enable);
         }
     }
     
     protected JComponent makeTextPanel(String text) {
         JPanel panel = new JPanel(false);
         JLabel filler = new JLabel(text);
         filler.setHorizontalAlignment(JLabel.CENTER);
         panel.setLayout(new GridLayout(1, 1));
         panel.add(filler);
         return panel;
     }
     
     /** Returns an ImageIcon, or null if the path was invalid. */
     protected static ImageIcon createImageIcon(String path) {
         return new ImageIcon(ClassLoader.getSystemResource(path));
     }
     
     private void recursiveEnabling(Container c, boolean enable) {
         c.setEnabled(enable);
         for (Component comp : c.getComponents()) {
             comp.setEnabled(enable);
             if (comp instanceof Container) {
                 this.recursiveEnabling((Container) comp, enable);
             }
         }
     }
     
 }
