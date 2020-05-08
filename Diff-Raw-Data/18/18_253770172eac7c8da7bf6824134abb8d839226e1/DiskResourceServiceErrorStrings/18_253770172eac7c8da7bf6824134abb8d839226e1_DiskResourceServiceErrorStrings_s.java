 package org.iplantc.core.uidiskresource.client.services.errors;
 
 import com.google.gwt.i18n.client.Messages;
 
 /**
  * @author jstroot
  *
  */
 public interface DiskResourceServiceErrorStrings extends Messages {
     
     /**
      * Error message displayed when a service call fails because disk resource(s) do not exist.
      * 
      * @param resourceNames
      * @return localized error string.
      */
     String diskResourceDoesNotExist(String resourceNames);
 
     /**
      * Error message displayed when a data service call fails because the rename request was incomplete.
      * 
      * @return localized error string.
      */
     String diskResourceIncompleteRename();
 
     /**
      * Error message displayed when a data service call fails because the move request was incomplete.
      * 
      * @return localized error string.
      */
     String diskResourceIncompleteMove();
 
     /**
      * Error message displayed when a data service call fails because the delete request was incomplete.
      * 
      * @return localized error string.
      */
     String diskResourceIncompleteDeletion();
 
     /**
      * Error message displayed when a data service call fails because the request is missing a parameter.
      * 
      * @return localized error string.
      */
     String dataErrorMissingQueryParameter();
 
     /**
      * Error message displayed when a data service call fails because the requesting user is not
      * authorized.
      * 
      * @return localized error string.
      */
     String dataErrorNotAuthorized();
 
     /**
      * Error message displayed when a data service call fails because the request JSON is missing or has
      * an incorrect field.
      * 
      * @return localized error string.
      */
     String dataErrorBadOrMissingField();
 
     /**
      * Error message displayed when a data service call fails because the request is invalid JSON.
      * 
      * @return localized error string.
      */
     String dataErrorInvalidJson();
 
     /**
      * Error message displayed when a service call fails because disk resource(s) already exists.
      * 
      * @param resourceNames
      * @return localized error string.
      */
     String diskResourceExists(String resourceNames);
 
     /**
      * Error message displayed when a data service call fails because disk resource(s) are not writable.
      * 
      * @param resourceNames
      * @return localized error string.
      */
     String diskResourceNotWriteable(String resourceNames);
 
     /**
      * Error message displayed when a data service call fails because disk resource(s) are not readable.
      * 
      * @param resourceNames
      * @return localized error string.
      */
     String diskResourceNotReadable(String resourceNames);
 
     /**
      * Error message displayed when a data service call fails because disk resource(s) are writable.
      * 
      * @param resourceNames
      * @return localized error string.
      */
     String diskResourceWriteable(String resourceNames);
 
     /**
      * Error message displayed when a data service call fails because disk resource(s) are readable.
      * 
      * @param resourceNames
      * @return localized error string.
      */
     String diskResourceReadable(String resourceNames);
 
     /**
      * Error message displayed when a data service call fails because the given username is invalid.
      * 
      * @return localized error string.
      */
     String dataErrorNotAUser();
 
     /**
      * Error message displayed when a service call fails because disk resource(s) are not files.
      * 
      * @param resourceNames
      * @return localized error string.
      */
     String diskResourceNotAFile(String resourceNames);
 
     /**
      * Error message displayed when a service call fails because disk resource(s) are not folders.
      * 
      * @param resourceNames
      * @return localized error string.
      */
     String diskResourceNotAFolder(String resourceNames);
 
     /**
      * Error message displayed when a data service call fails because disk resource(s) are files.
      * 
      * @param resourceNames
      * @return localized error string.
      */
     String diskResourceIsAFile(String resourceNames);
 
     /**
      * Error message displayed when a data service call fails because disk resource(s) are folders.
      * 
      * @param resourceNames
      * @return localized error string.
      */
     String diskResourceIsAFolder(String resourceNames);
 
     /**
      * Error message displayed when a service call fails because folder(s) do not exist.
      * 
      * @param resourceNames
      * @return localized error string.
      */
     String folderDoesNotExist(String resourceNames);
 
     /**
      * Error message displayed when a service call fails because folder(s) already exist.
      * 
      * @param folderNames
      * @return localized error string.
      */
     String folderExists(String folderNames);
 
     /**
      * Error message displayed when a data service call fails because folder(s) are not writable.
      * 
      * @param folderNames
      * @return localized error string.
      */
     String folderNotWriteable(String folderNames);
 
     /**
      * Error message displayed when a data service call fails because folder(s) are not readable.
      * 
      * @param folderNames
      * @return localized error string.
      */
     String folderNotReadable(String folderNames);
 
     /**
      * Error message displayed when a data service call fails because folder(s) are writable.
      * 
      * @param folderNames
      * @return localized error string.
      */
     String folderWriteable(String folderNames);
 
     /**
      * Error message displayed when a data service call fails because folder(s) are readable.
      * 
      * @param folderNames
      * @return localized error string.
      */
     String folderReadable(String folderNames);
 
     /**
      * Error message displayed when a service call fails because file(s) do not exist.
      * 
      * @param fileNames
      * @return localized error string.
      */
     String fileDoesNotExist(String fileNames);
 
     /**
      * Error message displayed when a service call fails because file(s) already exist.
      * 
      * @param fileNames
      * @return localized error string.
      */
     String fileExists(String fileNames);
 
     /**
      * Error message displayed when a data service call fails because file(s) are not writable.
      * 
      * @param fileNames
      * @return localized error string.
      */
     String fileNotWriteable(String fileNames);
 
     /**
      * Error message displayed when a data service call fails because file(s) are not readable.
      * 
      * @param fileNames
      * @return localized error string.
      */
     String fileNotReadable(String fileNames);
 
     /**
      * Error message displayed when a data service call fails because file(s) are writable.
      * 
      * @param fileNames
      * @return localized error string.
      */
     String fileWriteable(String fileNames);
 
     /**
      * Error message displayed when a data service call fails because file(s) are readable.
      * 
      * @param fileNames
      * @return localized error string.
      */
     String fileReadable(String fileNames);
 
 }
