 package gov.nih.nci.cagrid.data;
 
 import javax.xml.namespace.QName;
 
 public interface EnumerationMethodConstants {
 
    // CQL 1 enumeration query method constants
     public static final String ENUMERATION_QUERY_METHOD_DESCRIPTION = "The standard caGrid Data Service query method which begins an Enumeration";
     public static final String ENUMERATION_QUERY_METHOD_NAME = "enumerationQuery";
     public static final String ENUMERATION_DATA_SERVICE_PACKAGE = ServiceNamingConstants.DATA_SERVICE_PACKAGE + ".enumeration";
     public static final String ENUMERATION_DATA_SERVICE_NAMESPACE = "http://gov.nih.nci.cagrid.data.enumeration/EnumerationDataService";
     public static final String ENUMERATION_QUERY_METHOD_OUTPUT_DESCRIPTION = "The enumerate response containing the EPR of the enumeration resource";
     public static final String ENUMERATION_RESPONSE_NAMESPACE = "http://gov.nih.nci.cagrid.enumeration/EnumerationResponseContainer";
     public static final QName ENUMERATION_QUERY_METHOD_OUTPUT_TYPE = new QName(ENUMERATION_RESPONSE_NAMESPACE, "EnumerationResponseContainer");
     
    // CQL 2 enumeration query method constants
    public static final String CQL2_ENUMERATION_QUERY_METHOD_DESCRIPTION = "The standard caGrid Data Service query method which takes CQL 2 and begins an Enumeration";
    public static final String CQL2_ENUMERATION_QUERY_METHOD_NAME = "executeEnumerationQuery";
    public static final String CQL2_ENUMERATION_DATA_SERVICE_PACKAGE = ServiceNamingConstants.CQL2_DATA_SERVICE_PACKAGE + ".enumeration";
    public static final String CQL2_ENUMERATION_DATA_SERVICE_NAMESPACE = "http://org.cagrid.dataservice.enumeration/Cql2EnumerationDataService";   
 }
