 /*
  * Copyright (C) 2010, Zenoss Inc.  All Rights Reserved.
  */
 
 package org.zenoss.zep.index.impl;
 
 import org.apache.lucene.util.Version;
 
 /**
  * Constants for the event index (field names stored with the Lucene documents).
  */
 public class IndexConstants {
     /**
      * The version of the event index. This should be updated each time a new field
      * is added to the index or the way a field gets indexed changes.
      */
     public static final int INDEX_VERSION = 7;
 
     public static final String FIELD_UUID = "uuid";
     public static final String FIELD_STATUS = "status";
     public static final String FIELD_COUNT = "count";
     public static final String FIELD_FIRST_SEEN_TIME = "first_seen_time";
     public static final String FIELD_LAST_SEEN_TIME = "last_seen_time";
     public static final String FIELD_STATUS_CHANGE_TIME = "status_change_time";
     public static final String FIELD_ELEMENT_IDENTIFIER = "element_identifier";
     public static final String FIELD_ELEMENT_IDENTIFIER_NOT_ANALYZED = "element_identifier_not_analyzed";
     public static final String FIELD_ELEMENT_TITLE = "element_title";
     public static final String FIELD_ELEMENT_TITLE_NOT_ANALYZED = "element_title_not_analyzed";
     public static final String FIELD_ELEMENT_SUB_IDENTIFIER = "element_sub_identifier";
     public static final String FIELD_ELEMENT_SUB_IDENTIFIER_NOT_ANALYZED = "element_sub_identifier_not_analyzed";
     public static final String FIELD_ELEMENT_SUB_TITLE = "element_sub_title";
     public static final String FIELD_ELEMENT_SUB_TITLE_NOT_ANALYZED = "element_sub_title_not_analyzed";
     public static final String FIELD_FINGERPRINT = "fingerprint";
     public static final String FIELD_SUMMARY = "summary";
     public static final String FIELD_SUMMARY_NOT_ANALYZED = "summary_not_analyzed";
     public static final String FIELD_SEVERITY = "severity";
     public static final String FIELD_EVENT_CLASS = "event_class";
     public static final String FIELD_EVENT_CLASS_NOT_ANALYZED = "event_class_not_analyzed";
     public static final String FIELD_TAGS = "tag";
     public static final String FIELD_UPDATE_TIME = "update_time";
     public static final String FIELD_CURRENT_USER_NAME = "current_user_name";
     public static final String FIELD_AGENT = "agent";
     public static final String FIELD_MONITOR = "monitor";
     public static final String FIELD_EVENT_KEY = "event_key";
    public static final String FIELD_EVENT_CLASS_KEY = "event_class_key";
    public static final String FIELD_EVENT_GROUP = "event_group";
     public static final String FIELD_PROTOBUF = "protobuf";
 
     // For PATH and IP_ADDRESS details
     public static final String SORT_SUFFIX = "_sort";
     
     // For IP_ADDRESS details
     public static final String IP_ADDRESS_TYPE_SUFFIX = "_type";
     public static final String IP_ADDRESS_TYPE_4 = "4";
     public static final String IP_ADDRESS_TYPE_6 = "6";
 
     /**
      * The version of Lucene we wish to use.
      */
     public static final Version LUCENE_VERSION = Version.LUCENE_35;
 }
 
