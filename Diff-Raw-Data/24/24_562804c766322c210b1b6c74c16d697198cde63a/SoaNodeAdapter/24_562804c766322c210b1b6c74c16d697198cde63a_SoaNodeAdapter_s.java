 package org.easysoa.registry.types.adapters;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.easysoa.registry.InvalidDoctypeException;
 import org.easysoa.registry.SoaMetamodelService;
import org.easysoa.registry.types.ServiceImplementation;
 import org.easysoa.registry.types.SoaNode;
 import org.easysoa.registry.types.SubprojectNode;
 import org.easysoa.registry.types.ids.SoaNodeId;
 import org.easysoa.registry.utils.ListUtils;
 import org.nuxeo.ecm.core.api.ClientException;
 import org.nuxeo.ecm.core.api.DocumentModel;
 import org.nuxeo.ecm.core.api.model.PropertyException;
 import org.nuxeo.runtime.api.Framework;
 
 /**
  * 
  * @author mkalam-alami
  * 
  */
 public class SoaNodeAdapter extends AbstractDocumentAdapter implements SoaNode {
 
     protected SoaNodeId soaNodeId;
 
     public SoaNodeAdapter(DocumentModel documentModel) throws InvalidDoctypeException,
             PropertyException, ClientException {
         super(documentModel);
         this.soaNodeId = new SoaNodeId(
                 (String) documentModel.getPropertyValue(SubprojectNode.XPATH_SUBPROJECT),
                 documentModel.getType(),
                 (String) documentModel.getPropertyValue(SoaNode.XPATH_SOANAME));
     }
     
 	public String getDoctype() {
 		return documentModel.getType();
 	}
 
     public SoaNodeId getSoaNodeId() {
         return this.soaNodeId;
     }
 
     public String getSoaName() {
         return this.soaNodeId.getName();
     }
     
     public boolean isPlaceholder() throws Exception {
         return (Boolean) documentModel.getPropertyValue(XPATH_ISPLACEHOLDER);
     }
     
     public void setIsPlaceholder(boolean isPlaceholder) throws Exception {
         documentModel.setPropertyValue(XPATH_ISPLACEHOLDER, isPlaceholder);
     }
     
     public List<SoaNodeId> getParentIds() throws Exception {
 		Serializable[] parentsIdsArray = (Serializable[]) documentModel.getPropertyValue(XPATH_PARENTSIDS);
 		List<String> parentsIdsStringList = ListUtils.toStringList(parentsIdsArray);
 		List<SoaNodeId> parentsIds = new ArrayList<SoaNodeId>();
 		for (String parentIdString : parentsIdsStringList) {
 			SoaNodeId parentId = SoaNodeId.fromString(parentIdString);
 			if (parentId != null) {
 				parentsIds.add(parentId);
 			}
 		}
 		return parentsIds;
     }
 
 	public SoaNodeId getParentOfType(String doctype) throws ClientException {
     	SoaMetamodelService soaMetamodelService;
 		try {
 			soaMetamodelService = Framework.getService(SoaMetamodelService.class);
 		} catch (Exception e) {
 			throw new ClientException("Can't get SoaMetamodelService", e);
 		}
 		
 		Serializable[] parentsIdsArray = (Serializable[]) documentModel.getPropertyValue(XPATH_PARENTSIDS);
 		for (Serializable parentIdString : parentsIdsArray) {
 			 SoaNodeId parentId = SoaNodeId.fromString((String) parentIdString);
 			if (soaMetamodelService.isAssignable(parentId.getType(), doctype)) {
 				return parentId;
 			}
 		}
 		return null;
 	}
 
 	public void setParentIds(List<SoaNodeId> parentIds) throws PropertyException, ClientException {
 		List<String> parentsIdsStringList = new ArrayList<String>();
 		for (SoaNodeId parentId : parentIds) {
 			parentsIdsStringList.add(parentId.toString());
 		}
 		documentModel.setPropertyValue(XPATH_PARENTSIDS, (Serializable) parentsIdsStringList);
 	}
 
 	@Override
 	public String getUuid() throws Exception {
 		return documentModel.getId();
 	}
     
 }
