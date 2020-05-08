 package org.iplantc.core.uidiskresource.client.models;
 
 import java.util.Date;
 
 import org.iplantc.core.jsonutil.JsonUtil;
 import org.iplantc.core.uicommons.client.util.DateParser;
 
 import com.google.gwt.json.client.JSONObject;
 
 /**
  * Models the metadata related to a File.
  */
 public class File extends DiskResource {
 
     public static final String DESCRIPTION = "description"; //$NON-NLS-1$
     public static final String TYPE = "type"; //$NON-NLS-1$
     public static final String SIZE = "file-size"; //$NON-NLS-1$
     public static final String PATH = "path"; //$NON-NLS-1$
 
     private static final long serialVersionUID = 7502468326721721826L;
 
     /**
      * Constructs a file instance using the data represented by the argument.
      * 
      * @param info a JavaScript Object containing file metadata to use during construction
      */
     public File(JsFile info) {
         super(info.getId(), info.getLabel(), new Permissions(info.getPermissions()));
 
         setLastModified(DateParser.parseDate(info.getLastModified()));
         setPath(info.getPath());
         setDescription(JsonUtil.formatString(info.getDescription()));
         set(TYPE, info.getType());
 
         try {
             setSize(Long.valueOf(info.getSize()));
         } catch (NumberFormatException nfe) {
             setSize(new Long(0));
         }
     }
 
     /**
      * Instantiate file from id and and name.
      * 
      * @param id unique id for this file.
      * @param name display name for this file.
      */
     public File(final String id, final String name, Permissions permissions) {
         super(id, name, permissions);
 
         setLastModified(new Date());
         setPath(""); //$NON-NLS-1$
         setDescription(""); //$NON-NLS-1$
        set(SIZE, new Long(0)); //$NON-NLS-1$
         set(TYPE, ""); //$NON-NLS-1$
     }
 
     /**
      * Instantiate from a JSON object that contains at least "id" and "label" values.
      * 
      * @param file A JSON object which should contain at least "id" and "label" values.
      */
     public File(JSONObject file) {
         super(file);
 
         setPath(""); //$NON-NLS-1$
         setDescription(""); //$NON-NLS-1$
         set(TYPE, ""); //$NON-NLS-1$
 
         try {
             setSize(Long.valueOf(JsonUtil.getString(file, SIZE)));
         } catch (NumberFormatException nfe) {
             setSize(new Long(0));
         }
     }
 
     /**
      * Set
      * 
      * @param path
      */
     public void setPath(String path) {
         set(PATH, path);
     }
 
     /**
      * Set file size
      * 
      * @param size
      */
     public void setSize(Long size) {
         set(SIZE, size);
     }
 
     /**
      * Gets the type of data the file contains.
      * 
      * @return a string representing the type of data in the file
      */
     public String getType() {
         return get(TYPE);
     }
 
     /**
      * Gets a user-defined description of the file.
      * 
      * @return a string representing the user's description of the file
      */
     public String getDescription() {
         String desc = get(DESCRIPTION);
 
         if (desc == null || desc.isEmpty()) {
             return getType();
         }
         return desc;
     }
 
     /**
      * Sets a user-defined description of the file.
      * 
      */
     public void setDescription(String desc) {
         set(DESCRIPTION, desc);
     }
 
     /**
      * Gets the status of the file in the system.
      * 
      * @return a string representing the current statue of the file
      */
     @Override
     public String getStatus() {
         return getName() + " - " + getLastModified(); //$NON-NLS-1$
     }
 
     /**
      * Get the file's path
      * 
      * @return a string representing the path of the file
      */
     public String getPath() {
         return get(PATH);
     }
 
     /**
      * Get the file's size
      * 
      * @return a string representing the size of the file
      */
     public Long getSize() {
         return get(SIZE);
     }
 }
