 package net.ivoa.pdl.interpreter.groupInterpreter;
 
 import java.util.ArrayList;
 import java.util.EventListener;
 import java.util.List;
 import java.util.Vector;
 
 import javax.swing.event.EventListenerList;
 import javax.swing.event.TreeModelEvent;
 import javax.swing.event.TreeModelListener;
 import javax.swing.tree.TreeModel;
 import javax.swing.tree.TreePath;
 
 import net.ivoa.parameter.model.ConditionalStatement;
 import net.ivoa.parameter.model.ConstraintOnGroup;
 import net.ivoa.parameter.model.ParameterGroup;
 import net.ivoa.parameter.model.SingleParameter;
 import net.ivoa.pdl.interpreter.conditionalStatement.StatementHelperContainer;
 import net.ivoa.pdl.interpreter.utilities.Utilities;
 /**
  * This class acts as a decorator and a composite for the groups in the model.
  *  .
  * @author Paul Harrison (paul.harrison@manchester.ac.uk) 13 Mar 2012
  * @version $Revision$ $date$
  */
 public class GroupHandlerHelper implements TreeModel {
     
     private final static List<GroupHandlerHelper> allGroups = new ArrayList<GroupHandlerHelper>();
     private static GroupHandlerHelper root;
     
     private EventListenerList treeModelListeners =
             new EventListenerList();
 
 
     private final ParameterGroup group;
     private String fatherName;
     private List<String> sonNames = new ArrayList<String>();
     private final List<GroupHandlerHelper> children = new ArrayList<GroupHandlerHelper>();
     private List<SingleParameter> singleParamsIntoThisGroup;
    private boolean groupValid = false;
     private boolean groupActive = true;
     private final List<StatementHelperContainer> statementHelperList = new ArrayList<StatementHelperContainer>();
 
     
     public GroupHandlerHelper(ParameterGroup root){
         this(root, null);
     }
     public GroupHandlerHelper(ParameterGroup group, GroupHandlerHelper parent) {
         super();
 
         this.group = group;
         this.singleParamsIntoThisGroup = Utilities.getInstance()
                 .getParameterForTheGroup(this.group);
         for (ParameterGroup child : group.getParameterGroup()) {
             sonNames.add(child.getName());
             children.add(new GroupHandlerHelper(child, this));
         }
 
         
         ConstraintOnGroup condition = group
                 .getConstraintOnGroup();
         if (condition != null) {
             for (ConditionalStatement statement : condition.getConditionalStatement()) {
                 StatementHelperContainer tempHelper = new StatementHelperContainer(
                         statement);
                 statementHelperList.add(tempHelper);
             }
         }
         
         if (parent != null) {
             fatherName = parent.getGroupName();
         } else {
             fatherName = "root";
             root = this;
         }
         
         allGroups.add(this);
 
     }
 
     public String getFatherName() {
         return fatherName;
     }
 
     public ParameterGroup getGroup() {
         return group;
     }
 
     public List<StatementHelperContainer> getStatementHelperList() {
         return statementHelperList;
     }
 
     public String getGroupName() {
         return group.getName();
     }
 
     public List<SingleParameter> getSingleParamIntoThisGroup() {
         return singleParamsIntoThisGroup;
     }
 
     public Boolean getGroupValid() {
         return groupValid;
     }
 
     public void setGroupValid(Boolean groupValid) {
         this.groupValid = groupValid;
     }
 
     public String toString() {
         return group.getName();
     }
 
     /**
      * @return the groupActive
      */
     public boolean isGroupActive() {
         return groupActive;
     }
 
     /**
      * @param groupActive
      *            the groupActive to set
      */
     public void setGroupActive(boolean groupActive) {
         this.groupActive = groupActive;
     }
 
     /**
      * @return the allgroups
      */
     public static List<GroupHandlerHelper> getAllgroups() {
         return allGroups;
     }
 
     /* (non-Javadoc)
      * @see javax.swing.tree.TreeModel#getRoot()
      */
     @Override
     public Object getRoot() {
        return root;
     }
 
     /* (non-Javadoc)
      * @see javax.swing.tree.TreeModel#getChild(java.lang.Object, int)
      */
     @Override
     public Object getChild(Object parent, int index) {
         GroupHandlerHelper gh = (GroupHandlerHelper) parent;
         return gh.children.get(index);
     }
 
     /* (non-Javadoc)
      * @see javax.swing.tree.TreeModel#getChildCount(java.lang.Object)
      */
     @Override
     public int getChildCount(Object parent) {
         GroupHandlerHelper gh = (GroupHandlerHelper) parent;
         return gh.children.size();
     }
 
     /* (non-Javadoc)
      * @see javax.swing.tree.TreeModel#isLeaf(java.lang.Object)
      */
     @Override
     public boolean isLeaf(Object node) {
         GroupHandlerHelper gh = (GroupHandlerHelper) node;
         return gh.children.size() == 0;
     }
 
     /* (non-Javadoc)
      * @see javax.swing.tree.TreeModel#valueForPathChanged(javax.swing.tree.TreePath, java.lang.Object)
      */
     @Override
     public void valueForPathChanged(TreePath path, Object newValue) {
         //should not be allowed
         throw new  UnsupportedOperationException("TreeModel.valueForPathChanged() not implemented");
     }
 
     /* (non-Javadoc)
      * @see javax.swing.tree.TreeModel#getIndexOfChild(java.lang.Object, java.lang.Object)
      */
     @Override
     public int getIndexOfChild(Object parent, Object child) {
         GroupHandlerHelper gh = (GroupHandlerHelper) parent;
         return gh.children.indexOf(child);
     }
 
     /* (non-Javadoc)
      * @see javax.swing.tree.TreeModel#addTreeModelListener(javax.swing.event.TreeModelListener)
      */
     @Override
     public void addTreeModelListener(TreeModelListener l) {
         treeModelListeners.add(TreeModelListener.class,l);
     }
 
     /* (non-Javadoc)
      * @see javax.swing.tree.TreeModel#removeTreeModelListener(javax.swing.event.TreeModelListener)
      */
     @Override
     public void removeTreeModelListener(TreeModelListener l) {
         treeModelListeners.remove(TreeModelListener.class, l);
     }
 
     
     private void fireTreeInsert(TreePath path, Object child) {
         Object[] children = {child};
         int index = this.getIndexOfChild(path.getLastPathComponent(), child);
         int[] indicies = {index};
         TreeModelEvent e = new TreeModelEvent(this, path, indicies, children);
         EventListener[] listeners = treeModelListeners.getListeners(TreeModelListener.class);
         for (int ii = 0; ii < listeners.length; ii++) {
            ((TreeModelListener)listeners[ii]).treeNodesInserted(e);
         }
      }
 }
