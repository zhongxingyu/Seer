 package app.gui.searchServices;
 
 import java.awt.Rectangle;
 import java.util.Hashtable;
 import java.util.Vector;
 import javax.swing.JTree;
 import javax.swing.tree.TreeModel;
 import javax.swing.tree.TreeNode;
 import javax.swing.tree.TreePath;
 
 public class JCheckBoxTree extends JTree{
     Vector checkedPaths = new Vector();
     JCheckBoxTreeRenderer checkBoxCellRenderer;
 
     public JCheckBoxTree(Object[] value) {
         super(value);
     }
 
     public JCheckBoxTree(Vector<?> value){
         super(value);
     }
 
     public JCheckBoxTree(Hashtable<?,?> value){
         super(value);
     }
 
     public JCheckBoxTree(TreeNode root){
         super(root);
     }
 
     public JCheckBoxTree(TreeNode root, boolean asksAllowsChildren){
         super(root, asksAllowsChildren);
     }
     public JCheckBoxTree(TreeModel newModel){
         super(newModel);
     }
 
     public JCheckBoxTree() {
         super();
     }
 
     public void setChecked(TreePath path) {
         if(checkedPaths.contains(path)) {
             checkedPaths.remove(path);
             setParentsUnchecked(path);
             setDescendantsUnchecked(path);
         } else {
             checkedPaths.add(path);
             setParentsChecked(path);
             setDescendantsChecked(path);
         }
         setParentsChecked(path);
         repaintPath(path);
     }
 
     private void setDescendantsChecked(TreePath path) {
         if(!hasBeenExpanded(path)) {
             return;
         }
         Object component = path.getLastPathComponent();
         int childCount = getModel().getChildCount(component);
         for(int i = 0; i < childCount; i++) {
             Object childComponent = getModel().getChild(component, i);
             TreePath childComponentPath = path.pathByAddingChild(childComponent);
             if(!checkedPaths.contains(childComponentPath)) {
                 checkedPaths.add(childComponentPath);
                 repaintPath(childComponentPath);
             }
             setDescendantsChecked(childComponentPath);
         }
     }
 
     private void setDescendantsUnchecked(TreePath path) {
         if(hasBeenExpanded(path)) {
             Object cmp = path.getLastPathComponent();
 
             int component = getModel().getChildCount(cmp);
             for(int i = 0; i < component; i++) {
                 Object childComponent = getModel().getChild(cmp, i);
                 TreePath childComponentPath = path.pathByAddingChild(childComponent);
                 if(checkedPaths.contains(childComponentPath)) {
                     checkedPaths.remove(childComponentPath);
                     repaintPath(childComponentPath);
                 }
                 setDescendantsUnchecked(childComponentPath);
             }
         }
     }
 
     public boolean isAnyChildChecked(TreePath path) {
        if ((path != null) || (checkedPaths != null)) {
             for(int i=0; i < checkedPaths.size(); i++) {
                 TreePath checkedPath = (TreePath)checkedPaths.elementAt(i);
                 if(checkedPath==null){
                     System.err.println("checkedPath == null");
                     return false;
                 }
                 if(path.isDescendant(checkedPath)) {
                     return true;
                 }
             }
         }
         return false;
     }
 
     private boolean isParentChecked(TreePath path) {
         if ((path != null) || (checkedPaths != null)){
             if (checkedPaths == null) return false;
             TreePath parentPath = path.getParentPath();
             if(checkedPaths.contains(parentPath)) {
                 return true;
             }
         }
         return false;
     }
 
     private void setParentsChecked(TreePath path) {
         TreePath parentPath = path.getParentPath();
         if(parentPath != null){
             boolean shouldAdd = true;
             Object component = parentPath.getLastPathComponent();
             int childCount = getModel().getChildCount(component);
             for(int i=0; i<childCount;i++) {
                 Object childComponent = getModel().getChild(component, i);
                 TreePath childPath = parentPath.pathByAddingChild(childComponent);
                 if(!checkedPaths.contains(childPath)) {
                     shouldAdd = false;
                 }
             }
             if(shouldAdd) {
                 checkedPaths.add(parentPath);
             }
             repaintPath(parentPath);
             setParentsChecked(parentPath);
         }
     }
 
     private void setParentsUnchecked(TreePath path) {
         TreePath parentPath = path.getParentPath();
         if (parentPath != null){
             if(checkedPaths.contains(parentPath)) {
                 checkedPaths.remove(parentPath);
             }
             repaintPath(parentPath);
             setParentsUnchecked(parentPath);
         }
     }
 
     public boolean isChecked(TreePath path) {
         return (checkedPaths.contains(path));
     }
 
     private void repaintPath(TreePath path) {
         Rectangle pathRect = getPathBounds(path);
         if(pathRect != null){
             repaint(pathRect.x, pathRect.y, pathRect.width, pathRect.height);
         }
     }    
 }
