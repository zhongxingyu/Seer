 package gov.nih.nci.ncicb.webtree;
 
import java.util.Iterator;

 import org.apache.myfaces.custom.tree2.TreeNodeBase;
 
 public class LazyActionTreeNode extends TreeNodeBase {
    /**
     * serial id for serialisation versioning
     */
    private static final long serialVersionUID = 1L;
    private String    _action;      // * optional - url action for node *
    private String _toolTip;
    
    public LazyActionTreeNode() {
    }
 
    public LazyActionTreeNode(String type, String description, boolean leaf) {
            super(type, description, leaf);
    }
    public LazyActionTreeNode(String type, String description, String actionURL, boolean leaf) {
            super(type, description, leaf);
            _action = actionURL;
    }
 
    public LazyActionTreeNode(String type, String description, String actionURL, String identifier, boolean leaf) {
            super(type, description, identifier, leaf);
           _action = actionURL;
    }
 /**
    public List getChildren() {
         // if there are no children, try and retrieve them
            if (super.getChildren().size() == 0) {
               // create dummy tree nodes for example
               int id = Integer.parseInt(getIdentifier());
                    for(int i = 0; i < id; i++) {
                   super.getChildren().add(new LazyActionTreeNode("document","" + id, "" + id,false));
               }
            }
 
            return super.getChildren();
    }
 */
    public void setAction(String action) {
       this._action = action;
    }
 
    public String getAction() {
       return _action;
    }
    
    public String getPath() {
       return "";
    }
 
     public void setToolTip(String toolTip) {
         this._toolTip = toolTip;
     }
 
     public String getToolTip() {
         return _toolTip;
     }
    
    public void addLeafSortedByDescription(LazyActionTreeNode newLeaf) {
        Iterator nodeIter = this.getChildren().iterator();
        int index = 0;
        while (nodeIter.hasNext()) {
            LazyActionTreeNode leafNode = (LazyActionTreeNode) nodeIter.next();
            
            if (leafNode.getDescription().compareTo(newLeaf.getDescription()) <0)
             index ++; 
            else {
              break;
            }
        }
        this.getChildren().add(index, newLeaf);
        
    }
 
    
 }
