 package com.axiomalaska.sos;
 
 import javax.xml.namespace.QName;
 
 
 public class SosInjectorConstants {
     public static final String SOS_SERVICE = "SOS";
     public static final String SOS_V100 = "1.0.0";
     public static final String SOS_V200 = "2.0.0";
     public static final String SML_V101 = "1.0.1";
     
     // https://wiki.52north.org/bin/view/SensorWeb/SensorObservationServiceIVDocumentation#SplitDataArrayIntoObservations
     public static final String SPLIT_OBSERVATIONS_EXTENSION = "SplitDataArrayIntoObservations";
     
    public static final String IOOS_SML_FORMAT = "text/xml; subtype=\"sensorML/1.0.1/profiles/ioos_sos/1.0\"";
     public static final String NONE_OBS_PROP = "NONE";
     public static final String SWE_ARRAY_DEF = "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_SWEArrayObservation";
     public static final String MEASUREMENT_DEF = "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement";
     public static final String SAMPLING_POINT_DEF = "http://www.opengis.net/def/samplingFeatureType/OGC-OM/2.0/SF_SamplingPoint";
     public static final String PHENOMENON_TIME = "phenomenonTime";
     public static final String UNKNOWN_DEF = "http://www.opengis.net/def/nil/OGC/0/unknown";
 
     public static final String TEMPLATE = "template";
     public static final String TOKEN_SEPARATOR = "@";
     public static final String BLOCK_SEPARATOR = "#";
     public static final String DECIMAL_SEPARATOR = ".";
     
     public static final String LATEST = "latest";
     public static final String FIRST = "first";
     
     public static final QName QN_SOSINSERTIONMETADATA = new QName(
             XmlNamespaceConstants.NS_SOS_20, "SosInsertionMetadata", XmlNamespaceConstants.NS_SOS_PREFIX);
     
     public static final QName QN_SYSTEM = new QName(
             XmlNamespaceConstants.NS_SML, "System", XmlNamespaceConstants.NS_SML_PREFIX);
 
     public static final QName QN_DATARECORD_SWE2 = new QName(
             XmlNamespaceConstants.NS_SWE_20, "DataRecord", XmlNamespaceConstants.NS_SWE_PREFIX);
     
     public static final QName QN_SIMPLEDATARECORD = new QName(
             XmlNamespaceConstants.NS_SWE_101, "SimpleDataRecord", XmlNamespaceConstants.NS_SWE_PREFIX);
 
     public static final QName QN_POINT = new QName(
             XmlNamespaceConstants.NS_GML_32, "Point", XmlNamespaceConstants.NS_GML_PREFIX);
 
     public static final QName QN_LINESTRING = new QName(
             XmlNamespaceConstants.NS_GML_32, "LineString", XmlNamespaceConstants.NS_GML_PREFIX);
     
     public static final QName QN_TIMEINSTANT = new QName(
             XmlNamespaceConstants.NS_GML, "TimeInstant", XmlNamespaceConstants.NS_GML_PREFIX);
 
     public static final QName QN_TEXTENCODING = new QName(
             XmlNamespaceConstants.NS_SWE_20, "TextEncoding", XmlNamespaceConstants.NS_SWE_PREFIX);
 
     public static final QName QN_SF_SAMPLINGFEATURE = new QName(
             XmlNamespaceConstants.NS_SF, "SF_SamplingFeature", XmlNamespaceConstants.NS_SF_PREFIX);
 
     public static final QName QN_SF_SPATIALSAMPLINGFEATURE = new QName(
             XmlNamespaceConstants.NS_SAMS, "SF_SpatialSamplingFeature", XmlNamespaceConstants.NS_SAMS_PREFIX);
 
     public static final QName QN_SWE_TIME_SWE2 = new QName(
             XmlNamespaceConstants.NS_SWE_20, "Time", XmlNamespaceConstants.NS_SWE_PREFIX);
 
     public static final QName QN_SWE_QUANTITY_SWE2 = new QName(
             XmlNamespaceConstants.NS_SWE_20, "Quantity", XmlNamespaceConstants.NS_SWE_PREFIX);
 
     public static final QName QN_TEQUALS = new QName(
             XmlNamespaceConstants.NS_FES_2, "TEquals", XmlNamespaceConstants.NS_FES_2_PREFIX);
 }
