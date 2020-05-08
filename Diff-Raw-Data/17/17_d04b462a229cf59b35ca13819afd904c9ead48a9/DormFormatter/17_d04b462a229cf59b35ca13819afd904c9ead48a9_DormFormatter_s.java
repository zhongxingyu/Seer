 package com.zenika.dorm.core.util;
 
 import com.zenika.dorm.core.exception.CoreException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * @author Lukasz Piliszczuk <lukasz.piliszczuk AT zenika.com>
  */
 public final class DormFormatter {
 
     private static final Logger LOG = LoggerFactory.getLogger(DormFormatter.class);
 
    private static final String REGEX_EXTENSION_QUALIFIER = "[\\w\\s:]*";
     private static final String REGEX_VERSION = "[a-zA-Z0-9-.]*";
     private static final String REGEX_TYPE = "[a-zA-Z0-9-]*";
 
     private DormFormatter() {
 
     }
 
     public static String formatMetadataExtensionQualifier(String qualifier) {
 
         if (isEmpty(qualifier)) {
             throw new CoreException("Metadata extension qualifier is null or empty").type(CoreException.Type.NULL);
         }
 
         if (LOG.isDebugEnabled()) {
             LOG.debug("Format qualifier : " + qualifier);
         }
 
         // qualifier can contains any number of times :
         // - any letter case insensitive
         // - any number
        // - ":", "_"
         // - whitespace
         //"^[a-zA-Z0-9 \\s]+([\\:][a-zA-Z0-9 \\s]+)*$"
         if (!qualifier.matches(REGEX_EXTENSION_QUALIFIER)) {
             LOG.error("Invalid metadata extension qualifier: " + qualifier);
             throw new CoreException("Extension qualifier can contains only letters, numbers, " +
                    "whitespaces \"_\" and \":\"");
 
 
         }
 
         qualifier = qualifier.replaceAll(":", "-")
                 .replaceAll(" ", "_")
                 .trim();
 
         if (LOG.isDebugEnabled()) {
             LOG.debug("Formatted qualifier : " + qualifier);
         }
 
         return qualifier;
     }
 
     public static String formatMetadataVersion(String version) {
 
         if (isEmpty(version)) {
             throw new CoreException("Metadata version is null or empty").type(CoreException.Type.NULL);
         }
 
         if (LOG.isDebugEnabled()) {
             LOG.debug("Format version : " + version);
         }
 
         // version can contains any number of times :
         // - any letter case insensitive
         // - any number
         // - "-", "."
         if (!version.matches(REGEX_VERSION)) {
             LOG.error("Invalid version: " + version);
             throw new CoreException("Version can contains only letters, numbers, \"-\" and \".\"");
         }
 
         return version;
     }
 
     public static String formatMetadataType(String type) {
 
         if (isEmpty(type)) {
             throw new CoreException("Type is null or empty").type(CoreException.Type.NULL);
         }
 
         if (LOG.isDebugEnabled()) {
             LOG.debug("Format type : " + type);
         }
 
         // type can contains any number of times :
         // - any letter case insensitive
         // - any number
         // - "-"
         if (!type.matches(REGEX_TYPE)) {
             LOG.error("Invalid type : " + type);
             throw new CoreException("Version can contains only letters, numbers and \"-\"");
         }
 
         return type;
     }
 
     public static String formatMetadataQualifier(String extensionName, String extensionQualifier,
                                                  String version, String type) {
 
         String qualifier = extensionName + ":" + extensionQualifier + ":" + version + ":" + type;
 
         if (LOG.isDebugEnabled()) {
             LOG.debug("Formatted metadata qualifier : " + qualifier);
         }
 
         return qualifier;
     }
 
     private static boolean isEmpty(String string) {
         return null == string || string.isEmpty();
     }
 }
