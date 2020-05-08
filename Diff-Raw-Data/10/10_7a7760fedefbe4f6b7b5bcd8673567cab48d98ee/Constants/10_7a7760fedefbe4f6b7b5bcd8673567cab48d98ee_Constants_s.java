 package com.github.cwilper.fcrepo.dto.foxml;
 
 abstract class Constants {
 
     public static final String XML_VERSION = "1.0";
     public static final String CHAR_ENCODING = "UTF-8";
 
     public static final int BASE64_LINE_LENGTH = 74;
     public static final String LINE_FEED = "\n";
 
     public static final String FOXML_VERSION = "1.1";
 
     public static final String xmlns = "info:fedora/fedora-system:def/foxml#";
 
     public static final String digitalObject = "digitalObject";
     public static final String VERSION = "VERSION";
     public static final String PID = "PID";
 
     public static final String objectProperties = "objectProperties";
     
     public static final String property = "property";
     public static final String NAME = "NAME";
    public static final String MODEL = "info:fedora/fedora:def/model#";
    public static final String VIEW = "info:fedora/fedora:def/view#";
     public static final String STATE_URI = MODEL + "state";
     public static final String LABEL_URI = MODEL + "label";
     public static final String OWNERID_URI = MODEL + "ownerId";
     public static final String CREATEDDATE_URI = MODEL + "createdDate";
     public static final String LASTMODIFIEDDATE_URI = VIEW + "lastModifiedDate";
     public static final String VALUE = "VALUE";
 
     public static final String datastream = "datastream";
     public static final String ID = "ID";
     public static final String STATE = "STATE";
     public static final String CONTROL_GROUP = "CONTROL_GROUP";
     public static final String VERSIONABLE = "VERSIONABLE";
 
     public static final String datastreamVersion = "datastreamVersion";
     public static final String ALT_IDS = "ALT_IDS";
     public static final String LABEL = "LABEL";
     public static final String CREATED = "CREATED";
     public static final String MIMETYPE = "MIMETYPE";
     public static final String FORMAT_URI = "FORMAT_URI";
     public static final String SIZE = "SIZE";
 
     public static final String contentDigest = "contentDigest";
     public static final String TYPE = "TYPE";
     public static final String DIGEST = "DIGEST";
     
     public static final String contentLocation = "contentLocation";
     public static final String INTERNALREF_SCHEME = "internal";
     public static final String INTERNALREF_TYPE = "INTERNAL_REF";
     public static final String URL_TYPE = "URL";
     public static final String REF = "REF";
 
     public static final String xmlContent = "xmlContent";
 
     public static final String binaryContent = "binaryContent";
 }
