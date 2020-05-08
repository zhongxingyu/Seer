 package com.versionone.common.sdk;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import com.versionone.Oid;
 import com.versionone.apiclient.APIException;
 import com.versionone.apiclient.AndFilterTerm;
 import com.versionone.apiclient.Asset;
 import com.versionone.apiclient.Attribute;
 import com.versionone.apiclient.FilterTerm;
 import com.versionone.apiclient.IAssetType;
 import com.versionone.apiclient.IAttributeDefinition;
 import com.versionone.apiclient.IFilterTerm;
 import com.versionone.apiclient.IMetaModel;
 import com.versionone.apiclient.IServices;
 import com.versionone.apiclient.Query;
 import com.versionone.apiclient.QueryResult;
 
 class RequiredFieldsValidator {
     
     private final IMetaModel metaModel;
     private final IServices services;
     private final Map<String, List<RequiredFieldsDTO>> requiredFieldsList = new HashMap<String, List<RequiredFieldsDTO>>();
     
     
     RequiredFieldsValidator(IMetaModel metaModel, IServices services) {
         this.metaModel = metaModel;
         this.services = services;
     }
 
     private List<RequiredFieldsDTO> getRequiredFields(String assetType) throws DataLayerException {
         final List<RequiredFieldsDTO> fields = new LinkedList<RequiredFieldsDTO>();
         final IAssetType attributeDefinitionAssetType = metaModel.getAssetType("AttributeDefinition");                
         final IAttributeDefinition nameAttributeDef = attributeDefinitionAssetType.getAttributeDefinition("Name");        
         final IAttributeDefinition assetNameAttributeDef = attributeDefinitionAssetType
                 .getAttributeDefinition("Asset.AssetTypesMeAndDown.Name");
         final IAssetType taskType = metaModel.getAssetType(assetType);
 
         Query query = new Query(attributeDefinitionAssetType);
         query.getSelection().add(nameAttributeDef);
         FilterTerm assetTypeTerm = new FilterTerm(assetNameAttributeDef);
         assetTypeTerm.Equal(assetType);
         query.setFilter(new AndFilterTerm(new IFilterTerm[] { assetTypeTerm }));
 
         QueryResult result = null;
         try {
             result = services.retrieve(query);
         } catch (Exception e) {
             throw ApiDataLayer.warning("Cannot get meta data for " + assetType, e);
         }
 
         for (Asset asset : result.getAssets()) {
             try {
                 String name = asset.getAttribute(nameAttributeDef).getValue().toString();
                 if (isRequiredField(taskType, name)) {
                     RequiredFieldsDTO reqFieldData = new RequiredFieldsDTO(
                             name,
                             taskType.getAttributeDefinition(name).getDisplayName() );
                     
                     fields.add(reqFieldData);
                 }
             } catch (Exception e) {
                 throw ApiDataLayer.warning("Cannot get meta data for " + assetType, e);
             }
         }
 
         return fields;
     }
     
     private boolean isRequiredField(IAssetType taskType, String name) {
         IAttributeDefinition def = taskType.getAttributeDefinition(name);
         return def.isRequired() && !def.isReadOnly();
     }
     
     Map<Asset, List<RequiredFieldsDTO>> validateRequiredFields(List<Asset> assets) throws DataLayerException, APIException {
         Map<Asset, List<RequiredFieldsDTO>> requiredData = new HashMap<Asset, List<RequiredFieldsDTO>>(); 
         for (Asset asset : assets) {            
             List<RequiredFieldsDTO> fields = getUnfilledRequiredFields(asset);
             
             if (fields.size()>0) {
                 requiredData.put(asset, fields);
             }
             requiredData.putAll(validateRequiredFields(asset.getChildren()));
         }
         return requiredData;
     }
     
     List<RequiredFieldsDTO> getUnfilledRequiredFields(Asset asset) throws DataLayerException, APIException {
         List<RequiredFieldsDTO> unfilledFields = new ArrayList<RequiredFieldsDTO>();
         final String type = asset.getAssetType().getToken();
         if (!requiredFieldsList.containsKey(type)) {
             return unfilledFields;
         }
 
         for (RequiredFieldsDTO field : requiredFieldsList.get(type)) {
             String fullName = type + "." + field.name;
             Attribute attribute = asset.getAttributes().get(fullName);
                             
             if (attribute == null) {
                 throw ApiDataLayer.warning("Incorrect attribute:" + fullName);                    
             }
 
             if (isMultiValueAndUnfilled(attribute) || isSingleValueAndUnfilled(attribute)) {
                 unfilledFields.add(field);
             }
             
         }
         
         return unfilledFields;
     }
 
     private boolean isSingleValueAndUnfilled(Attribute attribute) throws APIException {
         return !attribute.getDefinition().isMultiValue() && 
                 ((attribute.getValue() instanceof Oid && ((Oid)attribute.getValue()).isNull()) || attribute.getValue() == null);
     }
 
     private boolean isMultiValueAndUnfilled(Attribute attribute) {
         return (attribute.getDefinition().isMultiValue() && attribute.getValues().length < 1);
     }
 
     public Map<String, List<RequiredFieldsDTO>> init() throws DataLayerException {        
         requiredFieldsList.put(Workitem.TASK_PREFIX, getRequiredFields(Workitem.TASK_PREFIX));
         requiredFieldsList.put(Workitem.DEFECT_PREFIX, getRequiredFields(Workitem.DEFECT_PREFIX));
         requiredFieldsList.put(Workitem.STORY_PREFIX, getRequiredFields(Workitem.STORY_PREFIX));
         requiredFieldsList.put(Workitem.TEST_PREFIX, getRequiredFields(Workitem.TEST_PREFIX));
         return requiredFieldsList;
     }
 
     public String createErrorMessage(Map<Asset, List<RequiredFieldsDTO>> requiredData) throws APIException {
         ApiDataLayer apiDataLayer = ApiDataLayer.getInstance();        
         StringBuilder message = new StringBuilder();
         
         for (Asset asset : requiredData.keySet()) {
             final String type = asset.getAssetType().getToken();
             final String assetDisplayName = apiDataLayer.localizerResolve(asset.getAssetType().getDisplayName());
             final Attribute idAttribute = asset.getAttributes().get(type + ".Number");
            final String id = idAttribute.getValue() != null ? idAttribute.getValue().toString() : "New Items";
 
             message.append("The following fields are not filled for the ").append(id).append(" ").append(assetDisplayName).append(":");
             message.append(getMessageOfUnfilledFieldsList(requiredData.get(asset), "\n\t", "\n\t")).append("\n");
         }
         
         return message.toString();
     }
     
     public String getMessageOfUnfilledFieldsList(List<RequiredFieldsDTO> unfilledFields, String startWith, String delimiter) {
         StringBuilder message = new StringBuilder(startWith);
         ApiDataLayer dataLayer = ApiDataLayer.getInstance();
 
         for (RequiredFieldsDTO field : unfilledFields) {
                 String fieldDisplayName = dataLayer.localizerResolve(field.displayName);
                 message.append(fieldDisplayName).append(delimiter);
         }
         message.delete(message.length() - delimiter.length(), message.length());
         return message.toString();
 }
 
     public List<RequiredFieldsDTO> getFields(String typePrefix) {
         return requiredFieldsList.get(typePrefix);
     }   
 }
