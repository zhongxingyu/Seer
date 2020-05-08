 package Sirius.navigator.types.treenode;
 
 /*
 // header - edit "Data/yourJavaHeader" to customize
 // contents - edit "EventHandlers/Java file/onCreate" to customize
 //
  */
 import javax.swing.*;
 
 import Sirius.server.middleware.types.*;
 import Sirius.navigator.connection.*;
 import Sirius.navigator.resource.*;
 import java.awt.EventQueue;
 
 public class ObjectTreeNode extends DefaultMetaTreeNode {
 
     protected ImageIcon nodeIcon;
     private MetaClass metaClass;
 
     public ObjectTreeNode(MetaObjectNode MetaObjectNode) {
         super(MetaObjectNode);
 
         try {
             MetaClass metaClass = this.getMetaClass();
             if (metaClass != null && metaClass.getObjectIconData().length > 0) {
                 this.nodeIcon = new ImageIcon(metaClass.getObjectIconData());
             } else {
                 this.nodeIcon = ResourceManager.getManager().getIcon("ObjectNodeIcon.gif");
             }
         } catch (Exception exp) {
             this.nodeIcon = ResourceManager.getManager().getIcon("ObjectNodeIcon.gif");
         }
 
     }
 
     public MetaObjectNode getMetaObjectNode() {
         return (MetaObjectNode) this.userObject;
     }
 
     public TreeNodeLoader getTreeNodeLoader() {
         return ((DefaultMetaTreeNode) this.getParent()).getTreeNodeLoader();
     }
 
     // --------------------------------------------------------------------------
     public final synchronized void explore() throws Exception {
         if (logger.isDebugEnabled()) {
             logger.debug("[ObjectNode] Begin explore()");
         }
 
         if (!isExplored() && !this.getMetaObjectNode().isLeaf()) {
             this.explored = this.getTreeNodeLoader().addChildren(this);
             //this.explored = this.getTreeNodeLoader().addChildren(this, this.getChildren());
             //explored = addChildren(this.getMetaObjectNode().getChildren());
         }
 
 
         if (logger.isDebugEnabled()) {
             logger.debug("[ObjectNode] End explore()");
         }
     }
 
     public final boolean isRootNode() {
         return false;
     }
 
     public final boolean isWaitNode() {
         return false;
     }
 
     public final boolean isPureNode() {
         return false;
     }
 
     public final boolean isObjectNode() {
         return true;
     }
 
     public final boolean isClassNode() {
         return false;
     }
 
     // ---------------------------------------------------------------------------
     public final String toString() {
         String toString = getMetaObjectNode().getName();
         if (toString == null) {
             MetaObject mo = getMetaObjectNode().getObject();
             if (mo != null) {
                 toString = mo.toString();
                 getNode().setName(toString);
             } else {
                 //Implicitly stores toString in getNode().setName(...) for future use.
                 //See implementation of getMetaObject().
                 toString = getMetaObject().toString();
             }
         }
         return toString;
     }
 
     public final String getDescription() {
         // dann eben doch nicht
         //return this.getMetaObjectNode().getDescription() != null ? this.getMetaObjectNode().getDescription() : this.getMetaObjectNode().getObject().getDescription();
 
         return this.getMetaObjectNode().getDescription();
     }
 
     public final ImageIcon getOpenIcon() {
         return this.nodeIcon;
     }
 
     public final ImageIcon getClosedIcon() {
         return this.nodeIcon;
     }
 
     public final ImageIcon getLeafIcon() {
         return this.nodeIcon;
     }
 
     public final boolean equals(DefaultMetaTreeNode node) {
         if (node.isObjectNode() && this.getID() == node.getID() && this.getDomain().equals(node.getDomain())) {
             return true;
         } else {
             return false;
         }
     }
 
     public final boolean equalsNode(Node node) {
         if (node instanceof MetaObjectNode && this.getMetaObjectNode().getDomain().equals(node.getDomain()) && this.getMetaObjectNode().getId() == node.getId()) {
             return true;
         } else {
             return false;
         }
     }
 
     // ===========================================================================
     public final int getID() {
         return this.getMetaObjectNode().getId();
     }
 
     public final String getDomain() {
         return this.getMetaObjectNode().getDomain();
     }
 
    public final MetaObject getMetaObject() {
         if (this.getMetaObjectNode().getObject() == null) {
             try {
                 if (logger.isDebugEnabled()) {
                     logger.debug("caching object node");
                 }
                 MetaObject metaObject = SessionManager.getProxy().getMetaObject(this.getMetaObjectNode().getObjectId(), this.getMetaObjectNode().getClassId(), this.getMetaObjectNode().getDomain());
                 this.getMetaObjectNode().setObject(metaObject);
                 if (getNode().getName() == null || getNode().getName().equals("NameWirdGeladen")) {
                     getNode().setName(metaObject.toString());
                 }
             } catch (Throwable t) {
                 logger.error("could not retrieve meta object of node '" + this + "'", t);
             }
         }

         return this.getMetaObjectNode().getObject();
     }
 
     /**
      * Setzt ein neues MetaObject, bzw. die ver\u00E4nderte Kopie des alten
      * MetaObjects dieser Node.
      */
     public final void setMetaObject(MetaObject metaObject) {
         logger.fatal("setting mo from " + getMetaObject() + " to " + metaObject);
         this.getMetaObjectNode().setObject(metaObject);
         this.setChanged(true);
     }
 
     public final MetaClass getMetaClass() throws Exception {
         if (metaClass == null) {
             metaClass = SessionManager.getProxy().getMetaClass(this.getMetaObjectNode().getClassId(), this.getDomain());
         }
         return metaClass;
     }
 
     public String getKey() throws Exception {
         logger.debug("getkey");
         return this.getMetaObject().getKey().toString();
     }
 
     @Override
     public int getClassID() {
         return this.getMetaObjectNode().getClassId();
     }
 //    //vor messe
 //    public boolean equals(Object obj) {
 //        try {
 //            return getMetaObject().equals(((ObjectTreeNode)obj).getMetaObject());
 //        }
 //        finally {
 //            return false;
 //        }
 //    }
 //    
 //    public int hashCode() {
 //        try {
 //            return getMetaObject().hashCode();
 //        }
 //        finally {
 //            return super.hashCode();
 //        }
 //                
 //    }
 }
