 package com.madalla.util.jcr.model;
 
 import java.util.Map;
 
 import org.apache.wicket.model.IChainingModel;
 import org.apache.wicket.model.IModel;
 
 public class NodeModelWrapper implements IChainingModel, IPluginModel {
 	private static final long serialVersionUID = 1L;
 
     protected JcrNodeModel nodeModel;
 
     public NodeModelWrapper(JcrNodeModel nodeModel) {
         this.nodeModel = nodeModel;
     }
 
     public JcrNodeModel getNodeModel() {
         return nodeModel;
     }
 
     // Implement IChainingModel
 
     public IModel getChainedModel() {
         return nodeModel;
     }
 
     public void setChainedModel(IModel model) {
         if (model instanceof JcrNodeModel) {
             nodeModel = (JcrNodeModel) model;
         }
     }
 
     public Object getObject() {
         return nodeModel.getObject();
     }
 
     public void setObject(Object object) {
         nodeModel.setObject(object);
     }
 
     public void detach() {
         if(nodeModel != null) {
             nodeModel.detach();
         }
     }
     
     // implement IPluginModel
     
    public Map<String, Object> getMapRepresentation() {
         return nodeModel.getMapRepresentation();
     }
 }
