 package confdb.gui;
 
 import javax.swing.*;
 import javax.swing.tree.*;
 import java.awt.*;
 import java.awt.event.*;
 
 import java.util.EventObject;
 
 import confdb.data.*;
 
 
 /**
  * ConfigurationTreeEditor
  * ----------------------
  * @author Philipp Schieferdecker
  *
  */
 class ConfigurationTreeEditor extends DefaultTreeCellEditor
 {
     //
     // data members
     //
     
     /** Referencable to be edited */
     private Object toBeEdited = null;
     
     /** the configuration being represented */
     private Configuration config = null;
     
 
     //
     // construction
     //
     
     /** standard constructor */
     public ConfigurationTreeEditor(JTree tree,DefaultTreeCellRenderer renderer)
     {
 	super(tree,renderer);
 	ConfigurationTreeModel model = (ConfigurationTreeModel)tree.getModel();
 	this.config = (Configuration)model.getRoot();
     }
 
     
     //
     // member functions
     //
     
     /** is the cell editable? don't respond to double clicks */
     public boolean isCellEditable(EventObject e)
     {
 	if (e instanceof MouseEvent) return false;
 	return true;
     }
 
     /**  DefaultTreeCellEditor's 'getCellEditorValue' */
     public Object getCellEditorValue()
     {
 	Object value = super.getCellEditorValue();
 	String name  = value.toString();
 
 	if (toBeEdited == null) return null;
 	
 	if (toBeEdited instanceof Referencable) {
 	    Referencable referencable = (Referencable)toBeEdited;
 	    if (config.isUniqueQualifier(name))
 		referencable.setName(name);
 	    else
 		referencable.setName("<ENTER UNIQUE NAME>");
 	}
 	else if (toBeEdited instanceof Instance) {
 	    Instance instance = (Instance)toBeEdited;
 	    Template template = instance.template();
 	    if (!template.hasInstance(name))
 		instance.setName(name);
	    else
		instance.setName("<ENTER UNIQUE NAME>");
 	}
 	else if (toBeEdited instanceof Reference) {
 	    Reference reference = (Reference)toBeEdited;
 	    Referencable referencable = reference.parent();
 	    if (config.isUniqueQualifier(name))
 		referencable.setName(name);
 	    else
 		referencable.setName("<ENTER UNIQUE NAME>");
 	}
 	return toBeEdited;
     }
     
     /** to determine the offset ;) */
     protected void determineOffset(JTree tree,
 				   Object value,
 				   boolean isSelected,
 				   boolean isExpanded,
 				   boolean isLeaf,
 				   int row)
     {
 	super.determineOffset(tree, value, isSelected, isExpanded, isLeaf, row);
 	Component rendererComponent =
 	    super.renderer.getTreeCellRendererComponent(tree,
 							value,
 							isSelected,
 							isExpanded,
 							isLeaf,
 							row,
 							true);
 	if (rendererComponent instanceof JLabel) {
 	    super.editingIcon = ((JLabel)rendererComponent).getIcon();
 	}
     }
     
     /** TreeCellEditor's 'getTreeCellEditorComponent' */
     public Component getTreeCellEditorComponent(JTree   tree,
 						Object  value,
 						boolean isSelected,
 						boolean expanded,
 						boolean leaf,
 						int     row)
     {
 	if (value instanceof Referencable||
 	    value instanceof Instance||
 	    value instanceof Reference) toBeEdited = value;
 	return super.getTreeCellEditorComponent(tree,value,
 						isSelected,expanded,
 						leaf,row);
     }
     
 }
