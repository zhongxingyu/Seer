 package test;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Cursor;
 import java.awt.Image;
 import java.awt.Rectangle;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import javax.swing.ImageIcon;
 import javax.swing.JFileChooser;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPopupMenu;
 import javax.swing.JTree;
 import javax.swing.SwingUtilities;
 import javax.swing.filechooser.FileFilter;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeCellRenderer;
 import javax.swing.tree.TreePath;
 import javax.swing.tree.TreeSelectionModel;
 
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 /**
  *
  * @author Matthew Shepherd <s0935850> and Robert Evans <s0949775>
  */
 public class LABELTEST extends javax.swing.JFrame {
 
     private String filepath = "image.jpg";
     private boolean _unsavedChanges;  // set this variable true when the first change after a save is made
     MyTreeModel treeModel;
     private DefaultMutableTreeNode rootNode;
     HelpWindow helpForm;
 
     /**
      * Used to flag unsaved changes b boolean for the flag
      */
     public void setUnsavedChanges(boolean b) {
 
         _unsavedChanges = b;
         jButton2.setEnabled(b);
         jMenuItem2.setEnabled(b);
     }
 
     /**
      * Creates new form LABELTEST
      */
     public LABELTEST() {
         super("Image Labeler");
         initComponents();
 
         setRedo(false);
         setUndo(false);
 
 
 
         imagePanel1.setLB(this);
         jToolBar1.setSize(100, 100);
         rootNode = new DefaultMutableTreeNode("Objects");
         treeModel = new MyTreeModel(rootNode);
         //DefaultMutableTreeNode category = new DefaultMutableTreeNode("Books for Java Programmers");
         //rootNode.add(category);
 
 
 
         imagePanel1.manager = new ObjectManager(this);
 
 
         jTree1.setModel(treeModel);
         jTree1.setCellRenderer(new MyRenderer(imagePanel1.manager));
         jTree1.addTreeSelectionListener(new MyTreeListener(imagePanel1.manager, jTree1, imagePanel1));
         jTree1.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
 
         final ActionListener menuListener = new ActionListener() {
             public void actionPerformed(ActionEvent event) {
                 PolygonObject po = imagePanel1.manager.getSelected();
                 if (po != null) {
                     if ("Rename".equals(event.getActionCommand())) {
                         String name = null;
                         name = JOptionPane.showInputDialog("Enter object name:",po.getName());
 
                         if (name != null && !name.equals(po.getName())) {
                             po.setName(name);
                             setUnsavedChanges(true);
                         }
                         jTree1.updateUI();
                     }
                     if ("Delete".equals(event.getActionCommand())) {
                         imagePanel1.manager.deleteObject(po);
                         if (imagePanel1.manager.objects.size() == 0) {
                             imagePanel1.mode = ImagePanel.Mode.AddPoly;
                         }
                         setUnsavedChanges(true);
                     }
                 }
             }
         };
         jTree1.addMouseListener(new MouseAdapter() {
             @Override
             public void mousePressed(MouseEvent e) {
                 if (SwingUtilities.isRightMouseButton(e)) {
                     TreePath path = jTree1.getPathForLocation(e.getX(), e.getY());
                     jTree1.setSelectionPath(path);
                     Rectangle pathBounds = jTree1.getUI().getPathBounds(jTree1, path);
                     if (pathBounds != null && pathBounds.contains(e.getX(), e.getY())) {
                         JPopupMenu contextMenu = new JPopupMenu();
                         JMenuItem rename = new JMenuItem("Rename");
                         rename.addActionListener(menuListener);
                         contextMenu.add(rename);
                         JMenuItem delete = new JMenuItem("Delete");
                         delete.addActionListener(menuListener);
                         contextMenu.add(delete);
                         contextMenu.show(jTree1, pathBounds.x, pathBounds.y + pathBounds.height);
                     }
                 }
             }
         });
 
 
 
         imagePanel1.manager.setWorkingTree(rootNode);
         imagePanel1.manager.jt = jTree1;
 
         imagePanel1.manager.model = treeModel;
 
         // Load in in image and label set
         double scale = imagePanel1.loadImage(filepath);
         XMLReader rd = new XMLReader(filepath + ".xml");
         removeNodes();
         rd.openXML(imagePanel1.manager, scale);
         setUnsavedChanges(false);
         drawMode();
 
     }
 
     public void setUndo(boolean b) {
         jButton5.setEnabled(b);
         jMenuItem3.setEnabled(b);
     }
 
     public void setRedo(boolean b) {
         jButton6.setEnabled(b);
         jMenuItem4.setEnabled(b);
     }
 
     public void showHelp() {
         HelpWindow a = new HelpWindow("HELP", "help/help.html");
         a.setVisible(true);
     }
     
     public void showAbout() {
         About b = new About();
         b.setVisible(true);
     }
 
     public void removeNodes() {
         //for(int i =0 ;i < rootNode.getChildCount(); i++) {
         //    treeModel.removeNodeFromParent(rootNode.getChildAt(i));
         //}
         rootNode = new DefaultMutableTreeNode("Objects");
         treeModel = new MyTreeModel(rootNode);
         imagePanel1.manager.setWorkingTree(rootNode);
         jTree1.setModel(treeModel);
         imagePanel1.manager.model = treeModel;
 
     }
 
     private void open() {
 
         JFileChooser c = new JFileChooser(".");
         //Add a custom file filter and disable the default
         //(Accept All) file filter.
             c.setFileFilter(new FileFilter(){
                 
                 public boolean accept(File f) {
                 if (f.isDirectory()) {
                     return true;
                 }
 
                 String extension = Utils.getExtension(f);
                 if (extension != null) {
                     if (extension.equals(Utils.tiff) ||
                         extension.equals(Utils.tif) ||
                         extension.equals(Utils.gif) ||
                         extension.equals(Utils.jpeg) ||
                         extension.equals(Utils.jpg) ||
                         extension.equals(Utils.png)) {
                             return true;
                     } else {
                         return false;
                     }
                 }
 
                 return false;
             }
 
             //The description of this filter
 
             public String getDescription() {
                 return "Just Images";
             }
                 
             });
             
             c.setAcceptAllFileFilterUsed(false);
  
         //Add custom icons for file types.
            c.setFileView(new ImageFileView());
 
         //Add the preview pane.
             c.setAccessory(new ImagePreview(c));
         int rVal = c.showOpenDialog(LABELTEST.this);
         if (rVal == JFileChooser.APPROVE_OPTION) {
             filepath = (c.getSelectedFile().getPath());
             double scale = imagePanel1.loadImage(filepath);
             setUnsavedChanges(false);
             XMLReader rd = new XMLReader(filepath + ".xml");
             removeNodes();
             rd.openXML(imagePanel1.manager, scale);
         }
         if (rVal == JFileChooser.CANCEL_OPTION) {
         }
         //editMode();
     }
 
     private void save() {
         //JFileChooser c = new JFileChooser();
         //int rVal = c.showSaveDialog(LABELTEST.this);
         //if (rVal == JFileChooser.APPROVE_OPTION) {
         //filepath = (c.getSelectedFile().getPath());
         // TODO Save polygon xml file to location c.getCurrentDirectory().toString()
         XMLBuilder b = new XMLBuilder(filepath + ".xml");
         b.buildWrite(imagePanel1.manager, (double) 1 / imagePanel1.scale);
         setUnsavedChanges(false);
         //}
         //if (rVal == JFileChooser.CANCEL_OPTION) {
         //}
 
     }
 
     public void undoAdd() {
         if (imagePanel1.mode == ImagePanel.Mode.AddPoint) {
             if (imagePanel1.po != null) {
                 if (imagePanel1.po.removeLastPoint() == 0) {
                     imagePanel1.po = null;
                     imagePanel1.mode = ImagePanel.Mode.AddPoly;
                     imagePanel1.updateUI();
                     setUndo(false);
                     setRedo(false);
 
                 } else {
                     setRedo(true);
                 }
             }
         }
     }
 
     public void redoPoint() {
         if (imagePanel1.mode == ImagePanel.Mode.AddPoint) {
             if (imagePanel1.po != null) {
                 if (imagePanel1.po.redo() == 0) {
 
                     imagePanel1.mode = ImagePanel.Mode.AddPoint;
                     imagePanel1.updateUI();
                     setUndo(true);
                     setRedo(false);
                 } else {
                     setRedo(true);
                 }
             }
         }
     }
 
     private void quit() {
         int dialogResult = 0;
         if (_unsavedChanges == true) {
             dialogResult = JOptionPane.showConfirmDialog(null, "Quit without saving?", "Warning", JOptionPane.YES_NO_OPTION);
         }
         if (dialogResult == JOptionPane.YES_OPTION || _unsavedChanges == false) {
             System.exit(0);
         }
     }
 
     private void editMode() {
         imagePanel1.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
         imagePanel1.setEdit();
         jButton8.getModel().setPressed(false);
         jButton9.getModel().setPressed(true);
 
     }
 
     private void drawMode() {
         imagePanel1.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
         imagePanel1.setDraw();
         jButton8.getModel().setPressed(true);
         jButton9.getModel().setPressed(false);
 
     }
 
     private class MyRenderer extends DefaultTreeCellRenderer {
 
         ObjectManager manager;
 
         public MyRenderer(ObjectManager mng) {
             manager = mng;
         }
 
         @Override
         public Component getTreeCellRendererComponent(
                 JTree tree,
                 Object value,
                 boolean sel,
                 boolean expanded,
                 boolean leaf,
                 int row,
                 boolean hasFocus) {
 
             super.getTreeCellRendererComponent(
                     tree, value, sel,
                     expanded, leaf, row,
                     hasFocus);
 
 
             DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
             //Object po =  (Object)(node.getUserObject());
             Object nodeObj = ((DefaultMutableTreeNode) value).getUserObject();
             PolygonObject po = manager.get(nodeObj.hashCode());
 
 
             //((PolygonObject)nodeObj).getColor();
             //PolygonObject a = (PolygonObject) po;
 
             //PolygonObject po = (PolygonObject) value;
             if (po != null) {
                 setIcon(new ImageIcon(generateIcon(po.color.getRed(), po.color.getGreen(), po.color.getBlue())));
             }
             return this;
         }
 
         Image generateIcon(int r, int g, int b) {
             BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);
             for (int y = 0; y < image.getHeight(); y++) {
                 for (int x = 0; x < image.getWidth(); x++) {
                     Color imageColor = new Color(r, g, b);
                     Color white = new Color(255, 255, 255);
                     //mix imageColor and desired color 
                     if (Math.pow(x - 8, 2) + Math.pow(y - 8, 2) < 64) {
                         image.setRGB(x, y, imageColor.getRGB());
                     } else {
                         image.setRGB(x, y, white.getRGB());
                     }
                 }
             }
             return image;
 
         }
     }
 
     /**
      * This method is called from within the constructor to initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is always
      * regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         jToolBar1 = new javax.swing.JToolBar();
         jButton1 = new javax.swing.JButton();
         jButton2 = new javax.swing.JButton();
         jButton5 = new javax.swing.JButton();
         jButton6 = new javax.swing.JButton();
         jButton7 = new javax.swing.JButton();
         jSeparator1 = new javax.swing.JToolBar.Separator();
         jButton8 = new javax.swing.JButton();
         jButton9 = new javax.swing.JButton();
         imagePanel1 = new test.ImagePanel();
         jScrollPane2 = new javax.swing.JScrollPane();
         jTree1 = new javax.swing.JTree();
         jLabel1 = new javax.swing.JLabel();
         jMenuBar1 = new javax.swing.JMenuBar();
         jMenu1 = new javax.swing.JMenu();
         jMenuItem1 = new javax.swing.JMenuItem();
         jMenuItem2 = new javax.swing.JMenuItem();
         jMenuItem7 = new javax.swing.JMenuItem();
         jMenu2 = new javax.swing.JMenu();
         jMenuItem3 = new javax.swing.JMenuItem();
         jMenuItem4 = new javax.swing.JMenuItem();
         jMenu3 = new javax.swing.JMenu();
         jMenuItem5 = new javax.swing.JMenuItem();
         jMenuItem6 = new javax.swing.JMenuItem();
 
         setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
         setResizable(false);
 
         jToolBar1.setFloatable(false);
         jToolBar1.setRollover(true);
 
         jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/test/Icons/open_small.png"))); // NOI18N
         jButton1.setToolTipText("Open");
         jButton1.setFocusable(false);
         jButton1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         jButton1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         jButton1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton1ActionPerformed(evt);
             }
         });
         jToolBar1.add(jButton1);
 
         jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/test/Icons/save_small.png"))); // NOI18N
         jButton2.setToolTipText("Save");
         jButton2.setFocusable(false);
         jButton2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         jButton2.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         jButton2.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton2ActionPerformed(evt);
             }
         });
         jToolBar1.add(jButton2);
 
         jButton5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/test/Icons/undo_small.png"))); // NOI18N
         jButton5.setToolTipText("Undo");
         jButton5.setFocusable(false);
         jButton5.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         jButton5.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         jButton5.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton5ActionPerformed(evt);
             }
         });
         jToolBar1.add(jButton5);
 
         jButton6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/test/Icons/redo_small.png"))); // NOI18N
         jButton6.setToolTipText("Redo");
         jButton6.setFocusable(false);
         jButton6.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         jButton6.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         jButton6.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton6ActionPerformed(evt);
             }
         });
         jToolBar1.add(jButton6);
 
         jButton7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/test/Icons/quit_small.png"))); // NOI18N
         jButton7.setToolTipText("Quit");
         jButton7.setFocusable(false);
         jButton7.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         jButton7.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         jButton7.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton7ActionPerformed(evt);
             }
         });
         jToolBar1.add(jButton7);
         jToolBar1.add(jSeparator1);
 
         jButton8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/test/Icons/draw_small.png"))); // NOI18N
         jButton8.setText(" Draw Mode");
         jButton8.setToolTipText("Draw new shapes");
         jButton8.setFocusable(false);
         jButton8.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
         jButton8.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
         jButton8.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton8ActionPerformed(evt);
             }
         });
         jToolBar1.add(jButton8);
 
         jButton9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/test/Icons/edit_small.png"))); // NOI18N
         jButton9.setText("Edit Mode ");
         jButton9.setToolTipText("Edit existing shapes");
         jButton9.setFocusable(false);
         jButton9.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
         jButton9.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         jButton9.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton9ActionPerformed(evt);
             }
         });
         jToolBar1.add(jButton9);
 
         org.jdesktop.layout.GroupLayout imagePanel1Layout = new org.jdesktop.layout.GroupLayout(imagePanel1);
         imagePanel1.setLayout(imagePanel1Layout);
         imagePanel1Layout.setHorizontalGroup(
             imagePanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 798, Short.MAX_VALUE)
         );
         imagePanel1Layout.setVerticalGroup(
             imagePanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 603, Short.MAX_VALUE)
         );
 
         jScrollPane2.setViewportView(jTree1);
 
         jLabel1.setText("Click to select:");
 
         jMenu1.setText("File");
 
         jMenuItem1.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
         jMenuItem1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/test/Icons/copy_small.png"))); // NOI18N
         jMenuItem1.setText("Open");
         jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMenuItem1ActionPerformed(evt);
             }
         });
         jMenu1.add(jMenuItem1);
 
         jMenuItem2.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
         jMenuItem2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/test/Icons/save_small.png"))); // NOI18N
         jMenuItem2.setText("Save");
         jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMenuItem2ActionPerformed(evt);
             }
         });
         jMenu1.add(jMenuItem2);
 
         jMenuItem7.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Q, java.awt.event.InputEvent.CTRL_MASK));
         jMenuItem7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/test/Icons/quit_small.png"))); // NOI18N
         jMenuItem7.setText("Quit");
         jMenuItem7.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMenuItem7ActionPerformed(evt);
             }
         });
         jMenu1.add(jMenuItem7);
 
         jMenuBar1.add(jMenu1);
 
         jMenu2.setText("Edit");
 
         jMenuItem3.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Z, java.awt.event.InputEvent.CTRL_MASK));
         jMenuItem3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/test/Icons/undo_small.png"))); // NOI18N
         jMenuItem3.setText("Undo");
         jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMenuItem3ActionPerformed(evt);
             }
         });
         jMenu2.add(jMenuItem3);
 
         jMenuItem4.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Y, java.awt.event.InputEvent.CTRL_MASK));
         jMenuItem4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/test/Icons/redo_small.png"))); // NOI18N
         jMenuItem4.setText("Redo");
         jMenuItem4.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMenuItem4ActionPerformed(evt);
             }
         });
         jMenu2.add(jMenuItem4);
 
         jMenuBar1.add(jMenu2);
 
         jMenu3.setText("Help");
 
         jMenuItem5.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, 0));
         jMenuItem5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/test/Icons/help_small.png"))); // NOI18N
         jMenuItem5.setText("Help");
         jMenuItem5.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMenuItem5ActionPerformed(evt);
             }
         });
         jMenu3.add(jMenuItem5);
 
         jMenuItem6.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F3, 0));
         jMenuItem6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/test/Icons/about_small.png"))); // NOI18N
         jMenuItem6.setText("About");
         jMenuItem6.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMenuItem6ActionPerformed(evt);
             }
         });
         jMenu3.add(jMenuItem6);
 
         jMenuBar1.add(jMenu3);
 
         setJMenuBar(jMenuBar1);
 
         org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jToolBar1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
             .add(layout.createSequentialGroup()
                 .addContainerGap()
                 .add(imagePanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(jScrollPane2)
                     .add(layout.createSequentialGroup()
                         .add(jLabel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 171, Short.MAX_VALUE)
                         .addContainerGap())))
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(layout.createSequentialGroup()
                 .add(jToolBar1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 8, Short.MAX_VALUE)
                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(org.jdesktop.layout.GroupLayout.TRAILING, imagePanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                         .add(jLabel1)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .add(jScrollPane2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 579, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                 .addContainerGap())
         );
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
     private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
         // Action for open in file menu
         open();
     }//GEN-LAST:event_jMenuItem1ActionPerformed
 
     private void jMenuItem4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem4ActionPerformed
         // TODO add your handling code here:
         redoPoint();
     }//GEN-LAST:event_jMenuItem4ActionPerformed
 
     private void jMenuItem5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem5ActionPerformed
         // TODO add your handling code here:
         showHelp();
     }//GEN-LAST:event_jMenuItem5ActionPerformed
 
     private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
         // Action for open icon on toolbar
         open();
     }//GEN-LAST:event_jButton1ActionPerformed
 
     private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
         // Action for save icon on toolbar
         save();
     }//GEN-LAST:event_jButton2ActionPerformed
 
     private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
         // Action for save icon in file menu
         save();
     }//GEN-LAST:event_jMenuItem2ActionPerformed
 
     private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
         // Redo (toolbar)
         redoPoint();
     }//GEN-LAST:event_jButton6ActionPerformed
 
     private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
         // Undo (toolbar)
         undoAdd();
     }//GEN-LAST:event_jButton5ActionPerformed
 
     private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem3ActionPerformed
         undoAdd();        // TODO add your handling code here:
     }//GEN-LAST:event_jMenuItem3ActionPerformed
 
     private void jMenuItem7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem7ActionPerformed
         quit();
     }//GEN-LAST:event_jMenuItem7ActionPerformed
 
     private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
         // Quit (toolbar)
         quit();
     }//GEN-LAST:event_jButton7ActionPerformed
 
     private void jButton9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton9ActionPerformed
         // Edit mode
         editMode();
     }//GEN-LAST:event_jButton9ActionPerformed
 
     private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
         // Draw mode
         drawMode();
     }//GEN-LAST:event_jButton8ActionPerformed
 
     private void jMenuItem6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem6ActionPerformed
         // TODO add your handling code here:
         showAbout();
     }//GEN-LAST:event_jMenuItem6ActionPerformed
 
     /**
      * @param args the command line arguments
      */
     public static void main(String args[]) {
         /* Set the Nimbus look and feel */
         //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
         /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
          * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
          */
         try {
             for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                 if ("Nimbus".equals(info.getName())) {
                     javax.swing.UIManager.setLookAndFeel(info.getClassName());
                     break;
                 }
             }
         } catch (ClassNotFoundException ex) {
             java.util.logging.Logger.getLogger(LABELTEST.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (InstantiationException ex) {
             java.util.logging.Logger.getLogger(LABELTEST.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (IllegalAccessException ex) {
             java.util.logging.Logger.getLogger(LABELTEST.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (javax.swing.UnsupportedLookAndFeelException ex) {
             java.util.logging.Logger.getLogger(LABELTEST.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         }
         //</editor-fold>
 
         /* Create and display the form */
         java.awt.EventQueue.invokeLater(new Runnable() {
             public void run() {
                 new LABELTEST().setVisible(true);
 
 
             }
         });
     }
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private test.ImagePanel imagePanel1;
     private javax.swing.JButton jButton1;
     private javax.swing.JButton jButton2;
     private javax.swing.JButton jButton5;
     private javax.swing.JButton jButton6;
     private javax.swing.JButton jButton7;
     private javax.swing.JButton jButton8;
     private javax.swing.JButton jButton9;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JMenu jMenu1;
     private javax.swing.JMenu jMenu2;
     private javax.swing.JMenu jMenu3;
     private javax.swing.JMenuBar jMenuBar1;
     private javax.swing.JMenuItem jMenuItem1;
     private javax.swing.JMenuItem jMenuItem2;
     private javax.swing.JMenuItem jMenuItem3;
     private javax.swing.JMenuItem jMenuItem4;
     private javax.swing.JMenuItem jMenuItem5;
     private javax.swing.JMenuItem jMenuItem6;
     private javax.swing.JMenuItem jMenuItem7;
     private javax.swing.JScrollPane jScrollPane2;
     private javax.swing.JToolBar.Separator jSeparator1;
     private javax.swing.JToolBar jToolBar1;
     private javax.swing.JTree jTree1;
     // End of variables declaration//GEN-END:variables
 }
