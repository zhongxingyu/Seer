 package frontend;
 
 import net.miginfocom.swing.MigLayout;
 
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 import javax.swing.JMenuBar;
 import javax.swing.JPanel;
 
 public class TestEditorUI {
   private JFrame frame;
   private JComponent tablePanel;
   private JPanel panel;
   private JComponent tree;
   private JPanel testPanel;
   private MigLayout migLayout;
 
   public TestEditorUI(String title) {
     testPanel = createTestPanel();
     frame = new JFrame(title);
     panel = new JPanel();
     migLayout = new MigLayout("nogrid", "fill,grow");
    testPanel.setLayout(new MigLayout("nogrid", "fill,grow"));
     panel.setLayout(migLayout);
 
     frame.setLayout(new MigLayout("", "fill, grow", "fill,grow,top"));
     frame.add(panel, "wrap");
     frame.add(testPanel);
     frame.setSize(1024, 768);
     frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
     frame.setVisible(true);
 
   }
 
   private JPanel createTestPanel() {
     testPanel = new JPanel();
     testPanel.setLayout(new MigLayout("", "[350][fill,grow]"));
     return testPanel;
   }
 
   public void setMenuBar(JMenuBar menubar) {
     frame.setJMenuBar(menubar);
   }
 
   public void addPanelAndWrap(JComponent panel) {
     this.panel.add(panel, "wrap");
     frame.validate();
   }
 
   public void addPanel(JComponent panel) {
     this.panel.add(panel);
     frame.validate();
   }
 
   public void close() {
     frame.setVisible(false);
   }
 
   public void addTablePanel(JComponent panel) {
     if (tablePanel != null) {
       this.testPanel.remove(tablePanel);
     }
     this.testPanel.add(panel, 1);
     tablePanel = panel;
     frame.validate();
   }
 
   public JFrame getFrame() {
     return frame;
   }
 
   public void addTreePanel(JComponent newTree) {
     if (tree != null) {
       this.testPanel.remove(tree);
     }
     this.testPanel.add(newTree, 0);
     tree = newTree;
     frame.validate();
   }
 }
