 package com.versionone.common.sdk;
 
 import java.math.BigDecimal;
 import java.text.NumberFormat;
 import java.util.ArrayList;
 
 import com.versionone.Oid;
 import com.versionone.apiclient.APIException;
 import com.versionone.apiclient.Asset;
 import com.versionone.apiclient.Attribute;
 import com.versionone.apiclient.IAttributeDefinition;
 import com.versionone.apiclient.V1Exception;
 import com.versionone.apiclient.IAttributeDefinition.AttributeType;
 import com.versionone.common.Activator;
 
 public class Workitem {
 
     public static final String TASK_PREFIX = "Task";
     public static final String STORY_PREFIX = "Story";
     public static final String DEFECT_PREFIX = "Defect";
     public static final String TEST_PREFIX = "Test";
     public static final String PROJECT_PREFIX = "Scope";
 
     public static final String ID_PROPERTY = "Number";
     public static final String DETAIL_ESTIMATE_PROPERTY = "DetailEstimate";
     public static final String NAME_PROPERTY = "Name";
     public static final String STATUS_PROPERTY = "Status";
     public static final String EFFORT_PROPERTY = "Actuals";
     public static final String DONE_PROPERTY = "Actuals.Value.@Sum";
     public static final String SCHEDULE_NAME_PROPERTY = "Schedule.Name";
     public static final String OWNERS_PROPERTY = "Owners";
     public static final String TODO_PROPERTY = "ToDo";
     public static final String DESCRIPTION_PROPERTY = "Description";
     public static final String CHECK_QUICK_CLOSE_PROPERTY = "CheckQuickClose";
     public static final String CHECK_QUICK_SIGNUP_PROPERTY = "CheckQuickSignup";
 
     protected ApiDataLayer dataLayer = ApiDataLayer.getInstance();
     protected Asset asset;
     public Workitem parent;
 
     /**
      * List of child Workitems.
      */
     public final ArrayList<Workitem> children;
 
     public Workitem(Asset asset, Workitem parent) {
         this.parent = parent;
         this.asset = asset;
 
         children = new ArrayList<Workitem>(asset.getChildren().size());
         for (Asset childAsset : asset.getChildren()) {
             if (dataLayer.isAssetSuspended(childAsset)) {
                 continue;
             }
 
             if (getTypePrefix().equals(PROJECT_PREFIX) || dataLayer.showAllTasks
                     || dataLayer.isCurrentUserOwnerAsset(childAsset)) {
                 children.add(new Workitem(childAsset, this));
             }
         }
         children.trimToSize();
     }
 
     public String getTypePrefix() {
         return asset.getAssetType().getToken();
     }
 
     public String getId() {
         if (asset == null) {// temporary
             return "NULL";
         }
         return asset.getOid().getMomentless().getToken();
     }
 
     public boolean hasChanges() {
         return asset.hasChanged();
     }
 
     public boolean isPropertyReadOnly(String propertyName) {
         String fullName = getTypePrefix() + '.' + propertyName;
         try {
             if (dataLayer.isEffortTrackingRelated(propertyName)) {
                 return isEffortTrackingPropertyReadOnly(propertyName);
             }
 
             return false;
         } catch (Exception e) {
             ApiDataLayer.warning("Cannot get property: " + fullName, e);
             return true;
         }
     }
 
     public boolean isPropertyDefinitionReadOnly(String propertyName) {
         String fullName = getTypePrefix() + '.' + propertyName;
         try {
             Attribute attribute = asset.getAttributes().get(fullName);
             return attribute.getDefinition().isReadOnly();
         } catch (Exception e) {
             ApiDataLayer.warning("Cannot get property: " + fullName, e);
             return true;
         }
     }
 
     private boolean isEffortTrackingPropertyReadOnly(String propertyName) {
         if (!dataLayer.isEffortTrackingRelated(propertyName)) {
             throw new IllegalArgumentException("This property is not related to effort tracking.");
         }
 
         EffortTrackingLevel storyLevel = dataLayer.storyTrackingLevel;
         EffortTrackingLevel defectLevel = dataLayer.defectTrackingLevel;
 
         if (getTypePrefix().equals(STORY_PREFIX)) {
             return storyLevel != EffortTrackingLevel.PRIMARY_WORKITEM && storyLevel != EffortTrackingLevel.BOTH;
         } else if (getTypePrefix().equals(DEFECT_PREFIX)) {
             return defectLevel != EffortTrackingLevel.PRIMARY_WORKITEM && defectLevel != EffortTrackingLevel.BOTH;
         } else if (getTypePrefix().equals(TASK_PREFIX) || getTypePrefix().equals(TEST_PREFIX)) {
             EffortTrackingLevel parentLevel;
             if (parent.getTypePrefix().equals(STORY_PREFIX)) {
                 parentLevel = storyLevel;
             } else if (parent.getTypePrefix().equals(DEFECT_PREFIX)) {
                 parentLevel = defectLevel;
             } else {
                 throw new IllegalStateException("Unexpected parent asset type.");
             }
             return parentLevel != EffortTrackingLevel.SECONDARY_WORKITEM && parentLevel != EffortTrackingLevel.BOTH;
         } else {
             throw new IllegalStateException("Unexpected asset type.");
         }
     }
 
     private PropertyValues getPropertyValues(String propertyName) {
         return dataLayer.getListPropertyValues(getTypePrefix(), propertyName);
     }
 
     /**
      * Checks if property value has changed.
      * 
      * @param propertyName
      *            Name of the property to get, e.g. "Status"
      * @return true if property has changed; false - otherwise.
      */
     public boolean isPropertyChanged(String propertyName) throws IllegalArgumentException {
         if (propertyName.equals(EFFORT_PROPERTY)) {
             return dataLayer.getEffort(asset) != null;
         }
         final String fullName = getTypePrefix() + '.' + propertyName;
         Attribute attribute = asset.getAttributes().get(fullName);
         if (attribute == null) {
             throw new IllegalArgumentException("There is no property: " + fullName);
         }
         return attribute.hasChanged();
     }
 
     /**
      * Resets property value if it was changed.
      * 
      * @param propertyName
      *            Name of the property to get, e.g. "Status"
      */
     public void resetProperty(String propertyName) throws IllegalArgumentException {
         if (propertyName.equals(EFFORT_PROPERTY)) {
             dataLayer.setEffort(asset, null);
         }
         final String fullName = getTypePrefix() + '.' + propertyName;
         Attribute attribute = asset.getAttributes().get(fullName);
         if (attribute == null) {
             throw new IllegalArgumentException("There is no property: " + fullName);
         }
         attribute.rejectChanges();
     }
 
     /**
      * Gets property value.
      * 
      * @param propertyName
      *            Name of the property to get, e.g. "Status"
      * @return String, Double, ValueId or PropertyValues.
      * @throws IllegalArgumentException
      *             If property cannot be got or there is no such one.
      * @see #NAME_PROPERTY
      * @see #STATUS_PROPERTY
      * @see #EFFORT_PROPERTY
      * @see #DONE_PROPERTY
      * @see #SCHEDULE_NAME_PROPERTY
      * @see #OWNERS_PROPERTY
      * @see #TODO_PROPERTY
      */
     public Object getProperty(String propertyName) throws IllegalArgumentException {
         if (propertyName.equals(EFFORT_PROPERTY)) {
             return dataLayer.getEffort(asset);
         }
         final String fullName = getTypePrefix() + '.' + propertyName;
         Attribute attribute = asset.getAttributes().get(fullName);
 
         if (attribute == null) {
             throw new IllegalArgumentException("There is no property: " + fullName);
         }
 
         if (attribute.getDefinition().isMultiValue()) {
             return getPropertyValues(propertyName).subset(attribute.getValues());
         }
 
         try {
             Object val = attribute.getValue();
             if (val instanceof Oid) {
                 return getPropertyValues(propertyName).find((Oid) val);
             }
 
             if (val instanceof Double) {
                 return BigDecimal.valueOf((Double) val).setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString();
             }
 
             return val;
         } catch (APIException e) {
             throw new IllegalArgumentException("Cannot get property: " + propertyName, e);
         }
     }
 
     public String getPropertyAsString(String propertyName) throws IllegalArgumentException {
         Object value = getProperty(propertyName);
         if (value == null) {
             return "";
         } else if (value instanceof Double) {
             // return numberFormat.format(value);
             return BigDecimal.valueOf((Double) value).setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString();
         }
         return value.toString();
     }
 
     /**
      * Sets property value.
      * 
      * @param propertyName
      *            Short name of the property to set, e.g. "Name".
      * @param newValue
      *            String, Double, null, ValueId, PropertyValues accepted.
      */
     public void setProperty(String propertyName, Object newValue) {
         final boolean isEffort = propertyName.equals(EFFORT_PROPERTY);
         try {
             if ("".equals(newValue)) {
                 newValue = null;
             }
 
             if ((isEffort || isNumeric(propertyName))) {
                 setNumericProperty(propertyName, newValue);
             } else if (isMultiValue(propertyName)) {
                 setMultiValueProperty(propertyName, (PropertyValues) newValue);
             } else {// List & String types
                 if (newValue instanceof ValueId) {
                     newValue = ((ValueId) newValue).oid;
                 }
                 setPropertyInternal(propertyName, newValue);
             }
 
         } catch (Exception ex) {
             ApiDataLayer.warning("Cannot set property " + propertyName + " of " + this, ex);
         }
     }
 
     private boolean isMultiValue(String propertyName) {
         final IAttributeDefinition attrDef = asset.getAssetType().getAttributeDefinition(propertyName);
         return attrDef.isMultiValue();
     }
 
     private boolean isNumeric(String propertyName) {
         final IAttributeDefinition attrDef = asset.getAssetType().getAttributeDefinition(propertyName);
         return attrDef.getAttributeType() == AttributeType.Numeric;
     }
 
     private void setNumericProperty(String propertyName, Object newValue) throws APIException {
         Double doubleValue = null;
         if (newValue != null) {
             // newValue = numberFormat.parse((String) newValue);
             doubleValue = Double.parseDouble(BigDecimal.valueOf(Double.parseDouble((String) newValue)).setScale(2,
                     BigDecimal.ROUND_HALF_UP).toPlainString());
         }
 
         if (propertyName.equals(EFFORT_PROPERTY)) {
             dataLayer.setEffort(asset, doubleValue);
         } else {
            if (doubleValue != null && doubleValue < 0) {
                 throw new IllegalArgumentException("The field cannot be negative");
             }
             setPropertyInternal(propertyName, doubleValue);
         }
     }
 
     private void setPropertyInternal(String propertyName, Object newValue) throws APIException {
         final Attribute attribute = asset.getAttributes().get(getTypePrefix() + '.' + propertyName);
         final Object oldValue = attribute.getValue();
         if ((oldValue == null && newValue != null) || !oldValue.equals(newValue)) {
             asset.setAttributeValue(asset.getAssetType().getAttributeDefinition(propertyName), newValue);
         }
     }
 
     private void setMultiValueProperty(String propertyName, PropertyValues newValues) throws APIException {
         final Attribute attribute = asset.getAttributes().get(getTypePrefix() + '.' + propertyName);
         final Object[] oldValues = attribute.getValues();
         final IAttributeDefinition attrDef = asset.getAssetType().getAttributeDefinition(propertyName);
         for (Object oldOid : oldValues) {
             if (!newValues.containsOid((Oid) oldOid)) {
                 asset.removeAttributeValue(attrDef, oldOid);
             }
         }
         for (ValueId newValue : newValues) {
             if (!checkContains(oldValues, newValue.oid)) {
                 asset.addAttributeValue(attrDef, newValue.oid);
             }
         }
     }
 
     private boolean checkContains(Object[] array, Object value) {
         for (Object item : array) {
             if (item.equals(value))
                 return true;
         }
         return false;
     }
 
     public boolean propertyChanged(String propertyName) {
         IAttributeDefinition attrDef = asset.getAssetType().getAttributeDefinition(propertyName);
         return asset.getAttribute(attrDef).hasChanged();
     }
 
     public void commitChanges() throws DataLayerException {
         try {
             dataLayer.commitAsset(asset);
         } catch (V1Exception e) {
             throw ApiDataLayer.warning("Failed to commit changes of workitem: " + this, e);
         }
     }
 
     public boolean isMine() {
         PropertyValues owners = (PropertyValues) getProperty(OWNERS_PROPERTY);
         return owners.containsOid(dataLayer.memberOid);
     }
 
     public boolean canQuickClose() {
         try {
             return (Boolean) getProperty("CheckQuickClose");
         } catch (IllegalArgumentException e) {
             ApiDataLayer.warning("QuickClose not supported.", e);
             return false;
         } catch (NullPointerException e) {
             ApiDataLayer.warning("QuickClose not supported.", e);
             return false;
         }
     }
 
     /**
      * Performs 'QuickClose' operation.
      * 
      * @throws DataLayerException
      */
     public void quickClose() throws DataLayerException {
         commitChanges();
         try {
             dataLayer.executeOperation(asset, asset.getAssetType().getOperation(ApiDataLayer.OP_QUICK_CLOSE));
             dataLayer.addIgnoreRecursively(this);
         } catch (V1Exception e) {
             throw ApiDataLayer.warning("Failed to QuickClose workitem: " + this, e);
         }
     }
 
     public boolean canSignup() {
         try {
             return (Boolean) getProperty("CheckQuickSignup");
         } catch (IllegalArgumentException e) {
             ApiDataLayer.warning("QuickSignup not supported.", e);
             return false;
         } catch (NullPointerException e) {
             ApiDataLayer.warning("QuickClose not supported.", e);
             return false;
         }
     }
 
     /**
      * Performs 'QuickSignup' operation.
      * 
      * @throws DataLayerException
      */
     public void signup() throws DataLayerException {
         try {
             dataLayer.executeOperation(asset, asset.getAssetType().getOperation(ApiDataLayer.OP_SIGNUP));
             dataLayer.refreshAsset(this);
         } catch (V1Exception e) {
             throw ApiDataLayer.warning("Failed to QuickSignup workitem: " + this, e);
         }
     }
 
     /**
      * Perform 'Inactivate' operation.
      * 
      * @throws DataLayerException
      */
     public void close() throws DataLayerException {
         try {
             dataLayer.executeOperation(asset, asset.getAssetType().getOperation(ApiDataLayer.OP_CLOSE));
             dataLayer.addIgnoreRecursively(this);
         } catch (V1Exception e) {
             throw ApiDataLayer.warning("Failed to Close workitem: " + this, e);
         }
     }
 
     public void revertChanges() {
         dataLayer.revertAsset(asset);
     }
 
     @Override
     public boolean equals(Object obj) {
         if (obj == null) {
             return false;
         }
         if (!(obj instanceof Workitem)) {
             return false;
         }
         Workitem other = (Workitem) obj;
         if (!other.asset.getOid().equals(asset.getOid())) {
             return false;
         }
         return true;
     }
 
     @Override
     public int hashCode() {
         return asset.getOid().hashCode();
     }
 
     @Override
     public String toString() {
         return getId() + (asset.hasChanged() ? " (Changed)" : "");
     }
 }
