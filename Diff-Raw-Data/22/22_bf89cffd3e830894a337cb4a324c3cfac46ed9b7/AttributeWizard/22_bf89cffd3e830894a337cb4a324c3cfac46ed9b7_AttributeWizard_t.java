 package org.glite.authz.pap.common.xacml.wizard;
 
 import org.glite.authz.pap.common.xacml.utils.CtxAttributeTypeHelper;
 import org.glite.authz.pap.common.xacml.utils.DataType;
 import org.glite.authz.pap.common.xacml.wizard.AttributeWizard.AttributeWizardType.TargetElement;
 import org.glite.authz.pap.common.xacml.wizard.exceptions.UnsupportedAttributeException;
 import org.opensaml.xacml.ctx.AttributeType;
 import org.opensaml.xacml.policy.AttributeAssignmentType;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class AttributeWizard {
 
     public enum AttributeWizardType {
         ACTION("action", "urn:oasis:names:tc:xacml:1.0:action:action-id", DataType.STRING, TargetElement.ACTION),
        DEFAULT("default", "http://cnaf.tmp/xacml/subject/default", DataType.STRING, TargetElement.SUBJECT),
         VO("vo", "http://authz-interop.org/xacml/subject/vo", DataType.STRING, TargetElement.SUBJECT),
         DN("dn", "urn:oasis:names:tc:xacml:1.0:subject:subject-id", DataType.X_500_NAME, TargetElement.SUBJECT),
         FQAN("fqan", "http://authz-interop.org/xacml/subject/voms-fqan", DataType.STRING, TargetElement.SUBJECT),
         PFQAN("pfqan", "http://authz-interop.org/xacml/subject/primaryfqan", DataType.STRING, TargetElement.SUBJECT),
         CA("ca", "http://cnaf.tmp/xacml/subject/ca", DataType.X_500_NAME, TargetElement.SUBJECT),
         GA("ga", "http://authz-interop.org/xacml/subject/voms-generic-attribute", DataType.STRING, TargetElement.SUBJECT),
         RESOURCE_PS("resource", "urn:oasis:names:tc:xacml:1.0:resource:resource-id", DataType.STRING, TargetElement.RESOURCE);
 
         public enum TargetElement {
             ACTION, ENVIRONMENT, RESOURCE, SUBJECT
         }
 
         private String dataType;
         private String id;
         private TargetElement targetElement;
         private String xacmlId;
 
         private AttributeWizardType(String id, String xacmlId, String dataType, TargetElement category) {
             this.id = id;
             this.xacmlId = xacmlId;
             this.dataType = dataType;
             this.targetElement = category;
         }
 
         public static AttributeWizardType getById(String id) {
             for (AttributeWizardType awt : AttributeWizardType.values()) {
                 if (awt.id.equals(id))
                     return awt;
             }
             return null;
         }
 
         public static AttributeWizardType getByXACMLId(String xacmlId) {
             for (AttributeWizardType awt : AttributeWizardType.values()) {
                 if (awt.xacmlId.equals(xacmlId))
                     return awt;
             }
             return null;
         }
 
         public static boolean idExist(String id) {
             for (AttributeWizardType awt : AttributeWizardType.values()) {
                 if (awt.id.equals(id))
                     return true;
             }
             return false;
         }
 
         public static boolean xacmlIdExist(String id) {
             for (AttributeWizardType awt : AttributeWizardType.values()) {
                 if (awt.xacmlId.equals(id))
                     return true;
             }
             return false;
         }
 
         public static boolean xacmlIdMatchesTargetElement(String xacmlId, TargetElement targetElement) {
             for (AttributeWizardType awt : AttributeWizardType.values()) {
                 if (awt.xacmlId.equals(xacmlId)) {
                     if (awt.targetElement.equals(targetElement))
                         return true;
                 }
             }
             return false;
         }
 
         public String getId() {
             return id;
         }
 
         public TargetElement getTargetElement() {
             return targetElement;
         }
         
         public String getXACMLID() {
             return xacmlId;
         }
 
     }
 
     @SuppressWarnings("unused")
     private static final Logger log = LoggerFactory.getLogger(AttributeWizard.class);
 
     private AttributeWizardType attributeWizardType;
 
     private String value;
 
     public AttributeWizard(AttributeType attribute) {
         String xacmlId = attribute.getAttributeID();
         attributeWizardType = AttributeWizardType.getByXACMLId(xacmlId);
         if (attributeWizardType == null)
             throw new UnsupportedAttributeException("Attribute not supported: " + xacmlId);
         this.value = CtxAttributeTypeHelper.getFirstValue(attribute);
     }
     
     public AttributeWizard(AttributeAssignmentType attributeAssignment) {
         String xacmlId = attributeAssignment.getAttributeId();
         attributeWizardType = AttributeWizardType.getByXACMLId(xacmlId);
         if (attributeWizardType == null) {
             throw new UnsupportedAttributeException("Attribute not supported: " + xacmlId);
         }
         this.value = attributeAssignment.getValue();
     }
 
     public AttributeWizard(AttributeWizardType attributeWizardType, String value) {
         this.attributeWizardType = attributeWizardType;
         this.value = value;
     }
 
     public AttributeWizard(String idEqualValue) {
         this(getIdFromIdEqualValue(idEqualValue), getValueFromIdEqualValue(idEqualValue));
     }
 
     public AttributeWizard(String identifier, String value) {
 
         attributeWizardType = AttributeWizardType.getById(identifier);
 
         if (attributeWizardType == null) {
             throw new UnsupportedAttributeException("id=" + identifier);
         }
 
         this.value = value;
     }
 
     public static boolean isActionAttribute(AttributeType attribute) {
         String xacmlId = attribute.getAttributeID();
         return AttributeWizardType.xacmlIdMatchesTargetElement(xacmlId, TargetElement.ACTION);
     }
 
     public static boolean isEnvironmentAttribute(AttributeType attribute) {
         String xacmlId = attribute.getAttributeID();
         return AttributeWizardType.xacmlIdMatchesTargetElement(xacmlId, TargetElement.ENVIRONMENT);
     }
 
     public static boolean isResouceAttribute(AttributeType attribute) {
         String xacmlId = attribute.getAttributeID();
         return AttributeWizardType.xacmlIdMatchesTargetElement(xacmlId, TargetElement.RESOURCE);
     }
 
     public static boolean isSubjectAttribute(AttributeType attribute) {
         String xacmlId = attribute.getAttributeID();
         return AttributeWizardType.xacmlIdMatchesTargetElement(xacmlId, TargetElement.SUBJECT);
     }
 
     private static String getIdFromIdEqualValue(String idEqualValue) {
         int separatorPosition = getSeparatorPositionForIdEqualValue(idEqualValue);
         return idEqualValue.substring(0, separatorPosition);
     }
 
     private static int getSeparatorPositionForIdEqualValue(String idEqualValue) {
         int separatorPosition = idEqualValue.indexOf('=');
         if (separatorPosition == -1)
             throw new UnsupportedAttributeException("'" + idEqualValue + "' is not in the format 'id=value'");
         return separatorPosition;
     }
 
     private static String getValueFromIdEqualValue(String idEqualValue) {
         int separatorPosition = getSeparatorPositionForIdEqualValue(idEqualValue);
 
         String value = idEqualValue.substring(separatorPosition + 1);
 
         if (value.startsWith("\""))
             value = value.substring(1);
 
         if (value.endsWith("\""))
             value = value.substring(0, value.length());
 
         return value;
     }
 
     public boolean equals(Object attributeWizardObject) {
         
         
         if (!(attributeWizardObject instanceof AttributeWizard)) {
             return false;
         }
         
         AttributeWizard attributeWizard = (AttributeWizard) attributeWizardObject;
         
         if (!this.getId().equals(attributeWizard.getId())) {
             return false;
         }
         
         if (!this.getXacmlId().equals(attributeWizard.getXacmlId())) {
             return false;
         }
         
         if (!this.value.equals(attributeWizard.getValue())) {
             return false;
         }
         return true;
     }
 
     public AttributeWizardType getAttributeWizardType() {
         return attributeWizardType;
     }
     
     public String getDataType() {
         return attributeWizardType.dataType;
     }
 
     public String getId() {
         return attributeWizardType.id;
     }
 
     public AttributeWizardType.TargetElement getTargetElementType() {
         return attributeWizardType.getTargetElement();
     }
 
     public String getValue() {
         return value;
     }
 
     public AttributeType getXACML() {
         return CtxAttributeTypeHelper.build(attributeWizardType.xacmlId, attributeWizardType.dataType, value);
     }
 
     public String getXacmlId() {
         return attributeWizardType.xacmlId;
     }
 
     public boolean isActionAttribute() {
         if (AttributeWizardType.TargetElement.ACTION.equals(attributeWizardType.targetElement))
             return true;
         return false;
     }
 
     public boolean isEnvironmentAttribute() {
         if (AttributeWizardType.TargetElement.ENVIRONMENT.equals(attributeWizardType.targetElement))
             return true;
         return false;
     }
 
     public boolean isResourceAttribute() {
         if (AttributeWizardType.TargetElement.RESOURCE.equals(attributeWizardType.targetElement))
             return true;
         return false;
     }
 
     public boolean isSubjectAttribute() {
         if (AttributeWizardType.TargetElement.SUBJECT.equals(attributeWizardType.targetElement))
             return true;
         return false;
     }
     
     public String toFormattedString() {
         return getId() + "=\"" + getValue() + "\"";
     }
 }
