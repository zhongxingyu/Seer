 package amber.gui.misc;
 
 import amber.Amber;
 import amber.data.map.LevelMap;
 import amber.gui.dialogs.NewMapDialog;
 import amber.gui.dialogs.TNewDialog;
 import amber.swing.Dialogs;
 import amber.swing.tree.filesystem.FileTreeAdapter;
 import amber.swing.tree.filesystem.FileSystemTree;
 import amber.swing.tree.filesystem.FileTreeModel.FileTreeNode;
 import amber.tool.ToolDefinition;
 import amber.tool.ToolLoader;
 
 import javax.swing.*;
 import javax.swing.tree.TreePath;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseEvent;
 import java.io.File;
 import java.io.IOException;
 
 /**
  * @author Tudor
  */
 public class FileTreeExplorer extends FileTreeAdapter {
 
     private FileSystemTree tree;
     private final ImageIcon SCRIPT_ICON = new ImageIcon(ClassLoader.getSystemResource("icon/General.Script.png"));
     private final ImageIcon MAP_ICON = new ImageIcon(ClassLoader.getSystemResource("icon/General.Map.png"));
 
     public FileTreeExplorer(FileSystemTree system) {
         tree = system;
     }
 
     private boolean open(TreePath path) {
        if(path == null)
            return false;
         File file = ((FileTreeNode) path.getLastPathComponent()).getFile();
         if (!file.isDirectory() && Amber.getWorkspace().getOpenedFiles().add(file.getAbsolutePath())) {
             String name = file.getName();
             if (name.endsWith(".tool")) {
                 try {
                     ToolDefinition tool = ToolLoader.loadTool(file);
                     if (tool.isDecorator()) {
                         try {
                             Amber.openToolTab(tool);
                         } catch (Exception e) {
                             if (e instanceof RuntimeException) {
                                 throw e.getCause(); // Get cause from wrapped error
                             }
                             throw e;
                         }
                     } else {
                         tool.enable();
                     }
                 } catch (Throwable ex) {
                     ex.printStackTrace();
                     Dialogs.errorDialog()
                             .setTitle("Failed to load tool.")
                             .setMessage("An error occured while loading tool: " + ex)
                             .show();
                 }
             } else {
                 try {
                     Amber.openFileTab(file);
                 } catch (Throwable ex) {
                     ex.printStackTrace();
                     Dialogs.errorDialog()
                             .setTitle("Exception while reading file.")
                             .setMessage("Failed to read file: " + ex)
                             .show();
                 }
             }
             return true;
         }
         return false;
     }
 
     @Override
     public boolean shouldDisplay(File file) {
         return !file.getName().equals(".amber");
     }
 
     @Override
     public void nodeClicked(MouseEvent e, TreePath path) {
         if (e.isPopupTrigger()) {
             int row = tree.getClosestRowForLocation(e.getX(), e.getY());
             tree.setSelectionRow(row);
             JPopupMenu popup = new JPopupMenu();
 
 
             final File file = ((FileTreeNode) path.getLastPathComponent()).getFile();
 
             if (file.isDirectory()) {
                 JMenu newMenu = new JMenu("New");
                 {
                     JMenu maps = new JMenu("Map");
                     {
                         maps.add(new JMenuItem("2D Map")).addActionListener(new ActionListener() {
                             public void actionPerformed(ActionEvent evt) {
                                 new NewMapDialog(Amber.getUI(), file, LevelMap.Type._2D).setVisible(true);
                                 tree.synchronize();
                             }
                         });
                         maps.add(new JMenuItem("3D Map")).addActionListener(new ActionListener() {
                             public void actionPerformed(ActionEvent evt) {
                                 new NewMapDialog(Amber.getUI(), file, LevelMap.Type._3D).setVisible(true);
                                 tree.synchronize();
                             }
                         });
                     }
                     newMenu.add(maps);
 
                     newMenu.add(new JMenuItem("File")).addActionListener(new ActionListener() {
                         public void actionPerformed(ActionEvent evt) {
                             String name = new TNewDialog(Amber.getUI(), "File").showDialog();
                             if (name != null && !name.isEmpty()) {
                                 try {
                                     File newFile = new File(file, name);
 
                                     if (newFile.exists()) {
                                         if (Dialogs.confirmDialog()
                                                 .setTitle("Overwrite file?")
                                                 .setMessage("File already exists. Overwrite it?")
                                                 .setOptionType(JOptionPane.YES_NO_CANCEL_OPTION)
                                                 .setMessageType(JOptionPane.QUESTION_MESSAGE).show() != JOptionPane.YES_OPTION) {
                                             return;
                                         }
                                     }
 
                                     newFile.createNewFile();
                                     Amber.openFileTab(newFile);
                                     tree.synchronize();
                                 } catch (IOException ex) {
                                     ex.printStackTrace();
                                     Dialogs.errorDialog()
                                             .setTitle("Failed to create new file.")
                                             .setMessage("Failed to create file: " + ex)
                                             .show();
                                 }
                             }
                         }
                     });
 
                     newMenu.add(new JMenuItem("Folder")).addActionListener(new ActionListener() {
                         public void actionPerformed(ActionEvent evt) {
                             String name = new TNewDialog(Amber.getUI(), "Folder").showDialog();
                             if (name != null) {
                                 File dir = new File(file, name);
                                 dir.mkdir();
                                 tree.synchronize();
                             }
                         }
                     });
                 }
 
                 popup.add(newMenu);
             } else {
                 popup.add(new JMenuItem("Open")).addActionListener(new ActionListener() {
                     public void actionPerformed(ActionEvent evt) {
                         Amber.openFileTab(file);
                     }
                 });
 
                 popup.add(new JMenuItem("Rename")).addActionListener(new ActionListener() {
                     public void actionPerformed(ActionEvent evt) {
                     }
                 });
             }
 
             popup.addSeparator();
 
             popup.add(new JMenuItem("Delete")).addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent evt) {
                     if (JOptionPane.showConfirmDialog(Amber.getUI(), "Are you sure you want to delete this file?",
                             "Delete", JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION) {
                         file.delete();
                         tree.synchronize();
                     }
                 }
             });
             popup.show(e.getComponent(), e.getX(), e.getY());
         } else if (e.getClickCount() == 2 && (e.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) == MouseEvent.BUTTON1_DOWN_MASK) {
             open(tree.getPathForRow(tree.getClosestRowForLocation(e.getX(), e.getY())));
         }
     }
 
     @Override
     public void keyReleased(KeyEvent e) {
         switch (e.getKeyCode()) {
             case KeyEvent.VK_ENTER:
                 if (!open(tree.getSelectionPath())) {
                     tree.expandPath(tree.getSelectionPath());
                 }
                 break;
             case KeyEvent.VK_BACK_SPACE:
                 tree.collapsePath(tree.getSelectionPath());
                 break;
         }
     }
 
     @Override
     public Icon getIcon(File file, String ext, FileTreeNode path, Icon defaultIcon) {
         return ext.equalsIgnoreCase(".rb") ? SCRIPT_ICON : ext.equalsIgnoreCase(".m") ? MAP_ICON : defaultIcon;
     }
 }
