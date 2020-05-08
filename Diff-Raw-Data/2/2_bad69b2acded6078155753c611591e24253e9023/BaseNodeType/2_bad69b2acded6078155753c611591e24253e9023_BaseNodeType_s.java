 package com.dc2f.contentrepository;
 
 import java.util.logging.Logger;
 
 import com.dc2f.contentrepository.nodetypedefinition.KeyValuePair;
 import com.dc2f.contentrepository.nodetypedefinition.MapNode;
 
 
 public abstract class BaseNodeType implements NodeType {
 	private static final Logger logger = Logger.getLogger(BaseNodeType.class.getName());
 
 	private NodeTypeInfo nodeTypeInfo;
 
 	public void init(NodeTypeInfo nodeTypeInfo) {
 		this.nodeTypeInfo = nodeTypeInfo;
 	}
 	
 	public NodeTypeInfo getNodeTypeInfo() {
 		return nodeTypeInfo;
 	}
 	
 	@Override
 	public AttributesDefinition getAttributeDefinitions() {
 		if (nodeTypeInfo == null) {
 			logger.severe("node type was not initialized! {" + this.getClass().getName() + "} nodeTypeInfo: {" + nodeTypeInfo + "}");
 		}
 		final Node attrDefinitions = (Node) nodeTypeInfo.get("attributes");
 		final Boolean freeattributes = (Boolean) nodeTypeInfo.get("freeattributes");
 		final String valueType = (String)nodeTypeInfo.get("valuetype");
 		final String valueNodeType = (String) nodeTypeInfo.get("valuenodetype");
 		
 		
		if (attrDefinitions == null) {
 			logger.severe("Node has no attribute defintions {" + this
 					+ "} (class:" + this.getClass().getName() + "} nodeTypeInfo: {" + nodeTypeInfo + "}");
 		}
 		
 		AttributesDefinition tmpParentAttrDefinitions = null;
 		if (nodeTypeInfo.getParentNodeType() != null) {
 			NodeType parent = nodeTypeInfo.getParentNodeType();
 			tmpParentAttrDefinitions = parent.getAttributeDefinitions();
 		}
 		final AttributesDefinition parentAttrDefinitions = tmpParentAttrDefinitions;
 
 
 		return new AttributesDefinition() {
 			
 			@Override
 			public String[] getAttributeNames() {
 				return nodeTypeInfo.getAttributeNames();
 			}
 			
 			@Override
 			public Node getAttributeDefinition(String propertyName) {
 					if (freeattributes != null && freeattributes.booleanValue()) {
 						return new MapNode(new KeyValuePair("type", valueType), new KeyValuePair("typeofnode", valueNodeType));
 					}
 					Object def = null;
 					if (attrDefinitions != null) {
 						def = attrDefinitions.get(propertyName);
 					}
 					if (def == null && parentAttrDefinitions != null) {
 						return (Node) parentAttrDefinitions.getAttributeDefinition(propertyName);
 					}
 					return (Node) def;
 				}
 				
 				
 				@Override
 				public String toString() {
 					return "{(" + BaseNodeType.this + ")" + String.valueOf(attrDefinitions) + " parent:(" + nodeTypeInfo.getParentNodeType() + ")" + String.valueOf(parentAttrDefinitions) + "}";
 				}
 			};
 	}
 	
 	@Override
 	public String toString() {
 		return "{NodeType:"+getNodeTypeInfo()+"}";
 	}
 }
