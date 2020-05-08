 package com.madalla.util.jcr.model;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.jcr.AccessDeniedException;
 import javax.jcr.ItemNotFoundException;
 import javax.jcr.Node;
 import javax.jcr.NodeIterator;
 import javax.jcr.RepositoryException;
 import javax.jcr.Session;
 
 import org.apache.commons.lang.builder.EqualsBuilder;
 import org.apache.commons.lang.builder.HashCodeBuilder;
 import org.apache.commons.lang.builder.ToStringBuilder;
 import org.apache.commons.lang.builder.ToStringStyle;
 import org.springmodules.jcr.JcrCallback;
 import org.springmodules.jcr.JcrTemplate;
 
 import com.madalla.util.jcr.model.tree.JcrTreeNode;
 
 
 public class JcrNodeModel implements IPluginModel {
     private static final long serialVersionUID = 1L;
     
     private ContentNode contentNode;
     private transient boolean parentCached = false;
     private JcrNodeModel parent = null;
 
     public JcrNodeModel(Node node) {
     	contentNode = new ContentNode(node);
     	Node parentNode = null;
 		try {
 			parentNode = node.getParent();
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
     	if (parentNode != null){
     		parent = new JcrNodeModel(parentNode);
     		parentCached = true;
     	}
     }
     
    public Map getMapRepresentation() {
         Map map = new HashMap();
         map.put("node", contentNode.getPath());
         return map;
     }
 
     public JcrNodeModel getParentModel() {
         return parent;
     }
 
     public JcrNodeModel findRootModel() {
         JcrNodeModel result = this;
         while (result.getParentModel() != null) {
             result = result.getParentModel();
         }
         return result;
     }
 
     // override Object
     public String toString() {
         return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE).append("contentNode", contentNode.toString())
                 .toString();
     }
 
     public boolean equals(Object object) {
         if (object instanceof JcrNodeModel == false) {
             return false;
         }
         if (this == object) {
             return true;
         }
         JcrNodeModel nodeModel = (JcrNodeModel) object;
         return new EqualsBuilder().append(contentNode, nodeModel.contentNode).isEquals();
     }
 
     public int hashCode() {
         return new HashCodeBuilder(57, 433).append(contentNode).toHashCode();
     }
 
 	public Object getObject() {
 		return contentNode;
 	}
 
 	public void setObject(Object obj) {
 		contentNode = (ContentNode) obj;
 	}
 
 	public void detach() {
 		//contentNode = null;
 	}
 
 	public ContentNode getContentNode() {
 		return contentNode;
 	}
 }
