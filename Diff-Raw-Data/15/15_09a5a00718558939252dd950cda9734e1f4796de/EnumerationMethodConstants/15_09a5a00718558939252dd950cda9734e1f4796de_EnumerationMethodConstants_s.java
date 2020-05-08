 package gov.nih.nci.cagrid.data;
 
 import javax.xml.namespace.QName;
 
 public interface EnumerationMethodConstants {
 
    // enumeration query method constants
     public static final String ENUMERATION_QUERY_METHOD_DESCRIPTION = "The standard caGrid Data Service query method which begins an Enumeration";
     public static final String ENUMERATION_QUERY_METHOD_NAME = "enumerationQuery";
     public static final String ENUMERATION_DATA_SERVICE_PACKAGE = ServiceNamingConstants.DATA_SERVICE_PACKAGE + ".enumeration";
     public static final String ENUMERATION_DATA_SERVICE_NAMESPACE = "http://gov.nih.nci.cagrid.data.enumeration/EnumerationDataService";
     public static final String ENUMERATION_QUERY_METHOD_OUTPUT_DESCRIPTION = "The enumerate response containing the EPR of the enumeration resource";
     public static final String ENUMERATION_RESPONSE_NAMESPACE = "http://gov.nih.nci.cagrid.enumeration/EnumerationResponseContainer";
     public static final QName ENUMERATION_QUERY_METHOD_OUTPUT_TYPE = new QName(ENUMERATION_RESPONSE_NAMESPACE, "EnumerationResponseContainer");
     
 }
