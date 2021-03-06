 package nextapp.echo.extras.testapp.testscreen;
 
 import nextapp.echo.app.FillImage;
 import nextapp.echo.app.ImageReference;
 import nextapp.echo.app.ResourceImageReference;
 import nextapp.echo.app.event.ActionEvent;
 import nextapp.echo.app.event.ActionListener;
 import nextapp.echo.extras.app.Tree;
 import nextapp.echo.extras.app.tree.AbstractTreeModel;
 import nextapp.echo.extras.app.tree.DefaultTreeCellRenderer;
 import nextapp.echo.extras.app.tree.TreeModel;
 import nextapp.echo.extras.app.tree.TreeSelectionModel;
 import nextapp.echo.extras.testapp.AbstractTest;
 import nextapp.echo.extras.testapp.Styles;
 import nextapp.echo.extras.testapp.TestControlPane;
 
 public class TreeTest extends AbstractTest {
     
  // FIXME enable when default tree model is available.    
 //    private static TreeModel generateSimpleTreeModel() {
 //        DefaultMutableTreeNode      root = new DefaultMutableTreeNode("Tree");
 //        DefaultMutableTreeNode      parent;
 //
 //        parent = new DefaultMutableTreeNode("colors");
 //        root.add(parent);
 //        parent.add(new DefaultMutableTreeNode("blue"));
 //        parent.add(new DefaultMutableTreeNode("violet"));
 //        parent.add(new DefaultMutableTreeNode("red"));
 //        parent.add(new DefaultMutableTreeNode("yellow"));
 //
 //        parent = new DefaultMutableTreeNode("sports");
 //        root.add(parent);
 //        parent.add(new DefaultMutableTreeNode("basketball"));
 //        parent.add(new DefaultMutableTreeNode("soccer"));
 //        parent.add(new DefaultMutableTreeNode("football"));
 //        parent.add(new DefaultMutableTreeNode("hockey"));
 //
 //        parent = new DefaultMutableTreeNode("food");
 //        root.add(parent);
 //        parent.add(new DefaultMutableTreeNode("hot dogs"));
 //        parent.add(new DefaultMutableTreeNode("pizza"));
 //        parent.add(new DefaultMutableTreeNode("ravioli"));
 //        parent.add(new DefaultMutableTreeNode("bananas"));
 //        
 //        return new DefaultTreeModel(root);
 //    }
     
     private static TreeModel generateSimpleTreeTableModel() {
         return new AbstractTreeModel() {
 
             public Object getChild(Object parent, int index) {
                 return new Integer(index);
             }
 
             public int getChildCount(Object parent) {
                 int parentValue = ((Integer) parent).intValue();
                 return parentValue;
             }
             
             public int getColumnCount() {
                 return 5;
             }
             
             public Object getValueAt(Object node, int column) {
                 if (0 == column) {
                     return node;
                 }
                 return ((Integer) node).intValue() + " - " + column;
             }
 
             public int getIndexOfChild(Object parent, Object child) {
                 int childValue = ((Integer) child).intValue();
                 return childValue;
             }
 
             public Object getRoot() {
                 return new Integer(4);
             }
 
             public boolean isLeaf(Object object) {
                 int objectValue = ((Integer) object).intValue();
                 return objectValue == 0;
             }
         };
     }
     
     private static final TreeModel generateEndlessOneNodeTreeModel() {
         return new AbstractTreeModel() {
 
             public Object getChild(Object parent, int index) {
                 return new Integer(((Integer)parent).intValue() + 1);
             }
 
             public int getChildCount(Object parent) {
                 return 1;
             }
 
             public int getColumnCount() {
                 return 1;
             }
 
             public int getIndexOfChild(Object parent, Object child) {
                 return 0;
             }
 
             public Object getRoot() {
                 return new Integer(0);
             }
 
             public Object getValueAt(Object node, int column) {
                 return node;
             }
 
             public boolean isLeaf(Object object) {
                 return false;
             }
             
         };
     }
     
     private static final FillImage[] TEST_FILL_IMAGES = new FillImage[] { null, 
         Styles.FILL_IMAGE_SHADOW_BACKGROUND_DARK_BLUE, Styles.FILL_IMAGE_SHADOW_BACKGROUND_LIGHT_BLUE,
         Styles.FILL_IMAGE_PEWTER_LINE, Styles.FILL_IMAGE_LIGHT_BLUE_LINE,
         Styles.FILL_IMAGE_SILVER_LINE};
     
     private static final ImageReference DEFAULT_FOLDER_ICON = new ResourceImageReference("nextapp/echo/extras/app/resource/image/TreeFolder.gif");
     private static final ImageReference DEFAULT_LEAF_ICON = new ResourceImageReference("nextapp/echo/extras/app/resource/image/TreeLeaf.gif");
     
     final Tree tree;
     public TreeTest() {
         
         super("Tree", Styles.ICON_16_TAB_PANE);
         
         tree = new Tree(generateSimpleTreeTableModel());
         add(tree);
         
         setTestComponent(this, tree);
         // Add/Remove Tabs
         
         addFontPropertyTests(TestControlPane.CATEGORY_PROPERTIES, Tree.PROPERTY_FONT);
         addBorderPropertyTests(TestControlPane.CATEGORY_PROPERTIES, Tree.PROPERTY_BORDER);
         addInsetsPropertyTests(TestControlPane.CATEGORY_PROPERTIES, Tree.PROPERTY_INSETS);
         
         addIntegerPropertyTests(TestControlPane.CATEGORY_PROPERTIES, Tree.PROPERTY_LINE_STYLE, new int[] {0, 1, 2});
         
         addBooleanPropertyTests(TestControlPane.CATEGORY_PROPERTIES, Tree.PROPERTY_ROLLOVER_ENABLED);
         addColorPropertyTests(TestControlPane.CATEGORY_PROPERTIES, Tree.PROPERTY_ROLLOVER_FOREGROUND);
         addColorPropertyTests(TestControlPane.CATEGORY_PROPERTIES, Tree.PROPERTY_ROLLOVER_BACKGROUND);
         addFontPropertyTests(TestControlPane.CATEGORY_PROPERTIES, Tree.PROPERTY_ROLLOVER_FONT);
         addFillImagePropertyTests(TestControlPane.CATEGORY_PROPERTIES, Tree.PROPERTY_ROLLOVER_BACKGROUND_IMAGE, TEST_FILL_IMAGES);
         
         addBooleanPropertyTests(TestControlPane.CATEGORY_PROPERTIES, Tree.PROPERTY_SELECTION_ENABLED);
         testControlsPane.addButton(TestControlPane.CATEGORY_PROPERTIES, "Set SelectionMode = Single", new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_SELECTION);
             }
         });
         testControlsPane.addButton(TestControlPane.CATEGORY_PROPERTIES, "Set SelectionMode = Multiple", new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 tree.getSelectionModel().setSelectionMode(TreeSelectionModel.MULTIPLE_SELECTION);
             }
         });
         addColorPropertyTests(TestControlPane.CATEGORY_PROPERTIES, Tree.PROPERTY_SELECTION_FOREGROUND);
         addColorPropertyTests(TestControlPane.CATEGORY_PROPERTIES, Tree.PROPERTY_SELECTION_BACKGROUND);
         addFontPropertyTests(TestControlPane.CATEGORY_PROPERTIES, Tree.PROPERTY_SELECTION_FONT);
         addFillImagePropertyTests(TestControlPane.CATEGORY_PROPERTIES, Tree.PROPERTY_SELECTION_BACKGROUND_IMAGE, TEST_FILL_IMAGES);
         
         testControlsPane.addButton(TestControlPane.CATEGORY_PROPERTIES, "Use default node icons", new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 ((DefaultTreeCellRenderer)tree.getCellRenderer()).setFolderIcon(DEFAULT_FOLDER_ICON);
                 ((DefaultTreeCellRenderer)tree.getCellRenderer()).setLeafIcon(DEFAULT_LEAF_ICON);
                 // hack to invalidate the tree
                 tree.setCellRenderer(tree.getCellRenderer());
             }
         });
         
         testControlsPane.addButton(TestControlPane.CATEGORY_PROPERTIES, "Use no node icons", new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 ((DefaultTreeCellRenderer)tree.getCellRenderer()).setFolderIcon(null);
                 ((DefaultTreeCellRenderer)tree.getCellRenderer()).setLeafIcon(null);
                 // hack to invalidate the tree
                 tree.setCellRenderer(tree.getCellRenderer());
             }
         });
         
         addBooleanPropertyTests(TestControlPane.CATEGORY_PROPERTIES, Tree.PROPERTY_HEADER_VISIBLE);
         addBooleanPropertyTests(TestControlPane.CATEGORY_PROPERTIES, Tree.PROPERTY_ROOT_VISIBLE);
         addBooleanPropertyTests(TestControlPane.CATEGORY_PROPERTIES, Tree.PROPERTY_SHOWS_ROOT_HANDLE);
         
 // FIXME enable when default tree model is available.        
 //        testControlsPane.addButton(TestControlPane.CATEGORY_CONTENT, "Simple tree model", new ActionListener() {
 //            public void actionPerformed(ActionEvent e) {
 //                tree.setModel(generateSimpleTreeModel());
 //                tree.setHeaderVisible(false);
 //            }
 //        });
         
         testControlsPane.addButton(TestControlPane.CATEGORY_CONTENT, "Simple treetable model", new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 tree.setModel(generateSimpleTreeTableModel());
                 tree.setHeaderVisible(true);
             }
         });
         
         testControlsPane.addButton(TestControlPane.CATEGORY_CONTENT, "Tree model (always one child)", new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 tree.setModel(generateEndlessOneNodeTreeModel());
                 tree.setHeaderVisible(false);
             }
         });
        
        testControlsPane.addButton(TestControlPane.CATEGORY_INTEGRATION, "toggle visibility", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                tree.setVisible(!tree.isVisible());
            }
        });
     }
 }
