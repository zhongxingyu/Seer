 package edu.wustl.cab2b.server.path;
 
 import edu.common.dynamicextensions.domain.DomainObjectFactory;
 import edu.common.dynamicextensions.domaininterface.AttributeInterface;
 import edu.common.dynamicextensions.domaininterface.BooleanValueInterface;
 import edu.common.dynamicextensions.domaininterface.CaDSRValueDomainInfoInterface;
 import edu.common.dynamicextensions.domaininterface.DateValueInterface;
 import edu.common.dynamicextensions.domaininterface.DoubleValueInterface;
 import edu.common.dynamicextensions.domaininterface.FloatValueInterface;
 import edu.common.dynamicextensions.domaininterface.IntegerValueInterface;
 import edu.common.dynamicextensions.domaininterface.LongValueInterface;
 import edu.common.dynamicextensions.domaininterface.StringValueInterface;
 import edu.common.dynamicextensions.domaininterface.UserDefinedDEInterface;
 import edu.common.dynamicextensions.util.global.Constants.ValueDomainType;
 import edu.wustl.cab2b.server.util.DynamicExtensionUtility;
 import edu.wustl.common.util.logger.Logger;
 import gov.nih.nci.cagrid.metadata.common.Enumeration;
 import gov.nih.nci.cagrid.metadata.common.UMLAttribute;
 import gov.nih.nci.cagrid.metadata.common.ValueDomain;
 
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * Enumration for DataType.
  * It also provides method to get this enumeration based on String Datatype
  * @author Chandrakant Talele
  */
 enum DataType {
 
     STRING() {
         /**
          * @see DataType#createAttribute(UMLAttribute)
          */
         public AttributeInterface createAndPopulateAttribute(UMLAttribute umlAttribute) {
             AttributeInterface attribute = domainObjectFactory.createStringAttribute();
             ValueDomain valueDomain = umlAttribute.getValueDomain();
 
             Enumeration[] arr = valueDomain.getEnumerationCollection().getEnumeration();
             if (arr != null) {
                 UserDefinedDEInterface userDefinedDE = DomainObjectFactory.getInstance().createUserDefinedDE();
                 for (Enumeration e : arr) {
                     StringValueInterface value = domainObjectFactory.createStringValue();
                     value.setValue(e.getPermissibleValue());
                     DynamicExtensionUtility.setSemanticMetadata(value, e.getSemanticMetadata());
                     userDefinedDE.addPermissibleValue(value);
                 }
                 attribute.getAttributeTypeInformation().setDataElement(userDefinedDE);
             }
             return attribute;
         }
     },
 
     INTEGER() {
         /**
          * @see DataType#createAttribute(UMLAttribute)
          */
         public AttributeInterface createAndPopulateAttribute(UMLAttribute umlAttribute) {
             AttributeInterface attribute = domainObjectFactory.createIntegerAttribute();
             ValueDomain valueDomain = umlAttribute.getValueDomain();
 
             Enumeration[] arr = valueDomain.getEnumerationCollection().getEnumeration();
             if (arr != null) {
                 UserDefinedDEInterface userDefinedDE = DomainObjectFactory.getInstance().createUserDefinedDE();
                 for (Enumeration e : arr) {
                     IntegerValueInterface value = domainObjectFactory.createIntegerValue();
                     value.setValue(new Integer(e.getPermissibleValue()));
                     DynamicExtensionUtility.setSemanticMetadata(value, e.getSemanticMetadata());
                     userDefinedDE.addPermissibleValue(value);
                 }
                 attribute.getAttributeTypeInformation().setDataElement(userDefinedDE);
             }
             return attribute;
         }
     },
 
     DATE() {
         /**
          * @see DataType#createAttribute(UMLAttribute)
          */
         public AttributeInterface createAndPopulateAttribute(UMLAttribute umlAttribute) {
             AttributeInterface attribute = domainObjectFactory.createDateAttribute();
             ValueDomain valueDomain = umlAttribute.getValueDomain();
 
             Enumeration[] arr = valueDomain.getEnumerationCollection().getEnumeration();
             if (arr != null) {
                 UserDefinedDEInterface userDefinedDE = DomainObjectFactory.getInstance().createUserDefinedDE();
                 for (Enumeration e : arr) {
                     DateValueInterface value = domainObjectFactory.createDateValue();
                     //TODO what is meaning of permissible values for Date ??? 
                     //Not clear about date format string
                     Logger.out.info("Date Attribute has permissible value : " + e.getPermissibleValue());
                     //value.setValue(new Date(e.getPermissibleValue()));
                     DynamicExtensionUtility.setSemanticMetadata(value, e.getSemanticMetadata());
                     userDefinedDE.addPermissibleValue(value);
                 }
                 attribute.getAttributeTypeInformation().setDataElement(userDefinedDE);
             }
             return attribute;
         }
     },
     FLOAT() {
         /**
          * @see DataType#createAttribute(UMLAttribute)
          */
         public AttributeInterface createAndPopulateAttribute(UMLAttribute umlAttribute) {
             AttributeInterface attribute = domainObjectFactory.createFloatAttribute();
             ValueDomain valueDomain = umlAttribute.getValueDomain();
 
             Enumeration[] arr = valueDomain.getEnumerationCollection().getEnumeration();
             if (arr != null) {
                 UserDefinedDEInterface userDefinedDE = DomainObjectFactory.getInstance().createUserDefinedDE();
                 for (Enumeration e : arr) {
                     FloatValueInterface value = domainObjectFactory.createFloatValue();
                     value.setValue(new Float(e.getPermissibleValue()));
                     DynamicExtensionUtility.setSemanticMetadata(value, e.getSemanticMetadata());
                     userDefinedDE.addPermissibleValue(value);
                 }
                 attribute.getAttributeTypeInformation().setDataElement(userDefinedDE);
             }
             return attribute;
         }
     },
     BOOLEAN() {
         /**
          * @see DataType#createAttribute(UMLAttribute)
          */
         public AttributeInterface createAndPopulateAttribute(UMLAttribute umlAttribute) {
             AttributeInterface attribute = domainObjectFactory.createBooleanAttribute();
             ValueDomain valueDomain = umlAttribute.getValueDomain();
 
             Enumeration[] arr = valueDomain.getEnumerationCollection().getEnumeration();
             if (arr != null) {
                 UserDefinedDEInterface userDefinedDE = DomainObjectFactory.getInstance().createUserDefinedDE();
                 for (Enumeration e : arr) {
                     BooleanValueInterface value = domainObjectFactory.createBooleanValue();
                     Logger.out.info("boolean Attribute has permissible value : " + e.getPermissibleValue());
                     //TODO what is meaning of permissible values for boolean???
                     //is any string processing needed??
                     value.setValue(new Boolean(e.getPermissibleValue()));
                     DynamicExtensionUtility.setSemanticMetadata(value, e.getSemanticMetadata());
                     userDefinedDE.addPermissibleValue(value);
                 }
                 attribute.getAttributeTypeInformation().setDataElement(userDefinedDE);
             }
             return attribute;
         }
     },
     LONG() {
         /**
          * @see DataType#createAttribute(UMLAttribute)
          */
         public AttributeInterface createAndPopulateAttribute(UMLAttribute umlAttribute) {
             AttributeInterface attribute = domainObjectFactory.createLongAttribute();
             ValueDomain valueDomain = umlAttribute.getValueDomain();
 
             Enumeration[] arr = valueDomain.getEnumerationCollection().getEnumeration();
             if (arr != null) {
                 UserDefinedDEInterface userDefinedDE = DomainObjectFactory.getInstance().createUserDefinedDE();
                 for (Enumeration e : arr) {
                     LongValueInterface value = domainObjectFactory.createLongValue();
                     value.setValue(new Long(e.getPermissibleValue()));
                     DynamicExtensionUtility.setSemanticMetadata(value, e.getSemanticMetadata());
                     userDefinedDE.addPermissibleValue(value);
                 }
                 attribute.getAttributeTypeInformation().setDataElement(userDefinedDE);
             }
             return attribute;
         }
     },
     DOUBLE() {
         /**
          * @see DataType#createAttribute(UMLAttribute)
          */
         public AttributeInterface createAndPopulateAttribute(UMLAttribute umlAttribute) {
             AttributeInterface attribute = domainObjectFactory.createDoubleAttribute();
             ValueDomain valueDomain = umlAttribute.getValueDomain();
 
             Enumeration[] arr = valueDomain.getEnumerationCollection().getEnumeration();
             if (arr != null) {
                 UserDefinedDEInterface userDefinedDE = DomainObjectFactory.getInstance().createUserDefinedDE();
                 for (Enumeration e : arr) {
                     DoubleValueInterface value = domainObjectFactory.createDoubleValue();
                     value.setValue(new Double(e.getPermissibleValue()));
                     DynamicExtensionUtility.setSemanticMetadata(value, e.getSemanticMetadata());
                     userDefinedDE.addPermissibleValue(value);
                 }
                 attribute.getAttributeTypeInformation().setDataElement(userDefinedDE);
             }
             return attribute;
         }
 
     },
     OBJECT() {
         public AttributeInterface createAndPopulateAttribute(UMLAttribute umlAttribute) {
             AttributeInterface attribute = domainObjectFactory.createObjectAttribute();
             ValueDomain valueDomain = umlAttribute.getValueDomain();
             Enumeration[] arr = valueDomain.getEnumerationCollection().getEnumeration();
             if (arr != null) {
                 for (Enumeration e : arr) {
                     //for the time being setting this as error to get all such attributes
                     Logger.out.error("For attribute of Object type found permissible value : " + e.getPermissibleValue());
                 }
             }
             return attribute;
         }
 
     }
     ;
 
     /**
      * Builds enumeration and assigns passed string as value
      * @param value
      */
     DataType() {
     }
 
     private static Map<String, DataType> dataTypeMap = new HashMap<String, DataType>();
 
     private static DomainObjectFactory domainObjectFactory = DomainObjectFactory.getInstance();
 
     static {
         //put data type strings in small case
         dataTypeMap.put("number", DataType.INTEGER);
         dataTypeMap.put("java.lang.integer", DataType.INTEGER);
         dataTypeMap.put("int", DataType.INTEGER);
         dataTypeMap.put("java.lang.string", DataType.STRING);
         dataTypeMap.put("string", DataType.STRING);
         dataTypeMap.put("alphanumeric", DataType.STRING);
         dataTypeMap.put("character", DataType.STRING);
         dataTypeMap.put("java.util.date", DataType.DATE);
         dataTypeMap.put("date", DataType.DATE);
         dataTypeMap.put("java.lang.float", DataType.FLOAT);
         dataTypeMap.put("float", DataType.FLOAT);
         dataTypeMap.put("java.lang.boolean", DataType.BOOLEAN);
         dataTypeMap.put("boolean", DataType.BOOLEAN);
         dataTypeMap.put("java.lang.long", DataType.LONG);
         dataTypeMap.put("long", DataType.LONG);
         dataTypeMap.put("java.lang.double", DataType.DOUBLE);
         dataTypeMap.put("double", DataType.DOUBLE);
 //        dataTypeMap.put("java.lang.object", DataType.UNDEFINED);
 //        dataTypeMap.put("java.util.collection", DataType.UNDEFINED);
 //        dataTypeMap.put("java.util.vector", DataType.UNDEFINED);
 //        dataTypeMap.put("java.util.arrayList", DataType.UNDEFINED);
 //        dataTypeMap.put("java.util.hashSet", DataType.UNDEFINED);
         
     }
 
     /**
      * Method which creates dynamic extension attribute based on datatype. 
      * Each enumration must provide implement of this method.    
      * @param umlAttribute source UML attribute 
      * @return the newly created dynamic extension attribute.
      */
     public final AttributeInterface createAttribute(UMLAttribute umlAttribute) {
         AttributeInterface attribute = createAndPopulateAttribute(umlAttribute);
         if (attribute != null) {
             postProcessing(attribute, umlAttribute);
         }
         return attribute;
     }
 
     AttributeInterface createAndPopulateAttribute(UMLAttribute umlAttribute) {
         // TODO bypassing attributes, need to decide how to handle it.
         Logger.out.error("found attribute with type" + umlAttribute.getDataTypeName() + ". Not storing it");
         return null;
     }
 
     private void postProcessing(AttributeInterface attribute, UMLAttribute umlAttribute) {
         attribute.setPublicId(Long.toString(umlAttribute.getPublicID()));
         ValueDomain valueDomain = umlAttribute.getValueDomain();
         CaDSRValueDomainInfoInterface valueDomainInfo = domainObjectFactory.createCaDSRValueDomainInfo();
         valueDomainInfo.setDatatype(umlAttribute.getDataTypeName());
         valueDomainInfo.setName(valueDomain.getLongName());
         valueDomainInfo.setValueDomainType(ValueDomainType.NON_ENUMERATED);
         Enumeration[] arr = valueDomain.getEnumerationCollection().getEnumeration();
         if (arr != null && arr.length != 0) {
             valueDomainInfo.setValueDomainType(ValueDomainType.ENUMERATED);
         }
         attribute.setCaDSRValueDomainInfo(valueDomainInfo);
     }
 
     /**
      * Returns the enumration for input String.
      * @param value
      * @return Returns the DataType
      */
     public static DataType get(String value) {
 
         DataType dataType = dataTypeMap.get(value.toLowerCase());
         if (dataType == null) {
             //throw new RuntimeException("unknown datatype found : " + value);
             //for the time being setting this as error to get all such attributes
            Logger.out.error("Found attribute with type : " + value + ". Creating Object type attribute for it.");
             return  DataType.OBJECT;
         }
         return dataType;
     }
 
 }
