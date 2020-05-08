 package gov.nih.nci.cagrid.data;
 
 import javax.xml.namespace.QName;
 
 public interface MetadataConstants {
 
     // metadata schema constants
     public static final String METADATA_SCHEMA_LOCATION = "xsd/cagrid/types";
     public static final String DATA_METADATA_SCHEMA = METADATA_SCHEMA_LOCATION + "/data/data.xsd";
     public static final String COMMON_METADATA_SCHEMA = METADATA_SCHEMA_LOCATION + "/common/common.xsd";
     public static final String CAGRID_METADATA_SCHEMA = METADATA_SCHEMA_LOCATION + "/caGridMetadata.xsd";
     public static final String DATA_METADATA_URI = "gme://caGrid.caBIG/1.0/gov.nih.nci.cagrid.metadata.dataservice";
     
     
     // DomainModel QName
     public static final QName DOMAIN_MODEL_QNAME = new QName("gme://caGrid.caBIG/1.0/gov.nih.nci.cagrid.metadata.dataservice", "DomainModel");
     
     // service metadata QName
     public static final QName SERVICE_METADATA_QNAME = new QName("gme://caGrid.caBIG/1.0/gov.nih.nci.cagrid.metadata", "ServiceMetadata");
     
     // query language support
     public static final String QUERY_LANGUAGE_SUPPORT_XSD = "QueryLanguageSupportMetadata.xsd";
     public static final QName QUERY_LANGUAGE_SUPPORT_QNAME = new QName("http://org.cagrid.dataservice.cql/QueryLanguageSupport", "QueryLanguageSupport");
    public static final String QUERY_LANGUAGE_SUPPORT_RESOURCE_GETTER_METHOD_NAME = "getQueryLanguageSupport";
     public static final String QUERY_LANGUAGE_SUPPORT_PACKAGE = "org.cagrid.dataservice.cql.support";
     public static final String QUERY_LANGUAGE_SUPPORT_DESCRIPTION = "Describes the level of support for CQL versions";
 }
