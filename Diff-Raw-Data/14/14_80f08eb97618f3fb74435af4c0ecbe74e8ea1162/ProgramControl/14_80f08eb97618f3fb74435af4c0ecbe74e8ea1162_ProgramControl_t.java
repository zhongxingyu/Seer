 /**
  * @(#)ProgramControl.
  * Copyright © 2012 jbundle.org. All rights reserved.
  * GPL3 Open Source Software License.
  */
 package org.jbundle.thin.app.program.db;
 
 import java.util.*;
 import org.jbundle.thin.base.util.*;
 
 import org.jbundle.thin.base.db.*;
 
 import org.jbundle.model.app.program.db.*;
 
 public class ProgramControl extends FieldList
     implements ProgramControlModel
 {
     private static final long serialVersionUID = 1L;
 
 
     public ProgramControl()
     {
         super();
     }
     public ProgramControl(Object recordOwner)
     {
         this();
         this.init(recordOwner);
     }
     public static final String PROGRAM_CONTROL_FILE = "ProgramControl";
     /**
      *  Get the table name.
      */
     public String getTableNames(boolean bAddQuotes)
     {
         return (m_tableName == null) ? ProgramControl.PROGRAM_CONTROL_FILE : super.getTableNames(bAddQuotes);
     }
     /**
      *  Get the Database Name.
      */
     public String getDatabaseName()
     {
         return "program";
     }
     /**
      *  Is this a local (vs remote) file?.
      */
     public int getDatabaseType()
     {
         return Constants.LOCAL | Constants.USER_DATA;
     }
     /**
     * Set up the screen input fields.
     */
     public void setupFields()
     {
         FieldInfo field = null;
         field = new FieldInfo(this, ID, Constants.DEFAULT_FIELD_LENGTH, null, null);
         field.setDataClass(Integer.class);
         field.setHidden(true);
         field = new FieldInfo(this, LAST_CHANGED, Constants.DEFAULT_FIELD_LENGTH, null, null);
         field.setDataClass(Date.class);
         field.setHidden(true);
         field = new FieldInfo(this, DELETED, 10, null, new Boolean(false));
         field.setDataClass(Boolean.class);
         field.setHidden(true);
         field = new FieldInfo(this, PROJECT_NAME, 30, null, null);
         field = new FieldInfo(this, BASE_DIRECTORY, 100, null, null);
         field = new FieldInfo(this, SOURCE_DIRECTORY, 100, null, "src/main/java/");
         field = new FieldInfo(this, RESOURCES_DIRECTORY, 100, null, "src/main/resources/");
         field = new FieldInfo(this, CLASS_DIRECTORY, 100, null, "target/classes/");
        field = new FieldInfo(this, ARCHIVE_DIRECTORY, 127, null, null);
        field = new FieldInfo(this, DEV_ARCHIVE_DIRECTORY, 127, null, null);
         field = new FieldInfo(this, RESOURCE_TYPE, Constants.DEFAULT_FIELD_LENGTH, null, null);
         field = new FieldInfo(this, CLASS_RESOURCE_TYPE, Constants.DEFAULT_FIELD_LENGTH, null, null);
         field = new FieldInfo(this, PACKAGE_NAME, 40, null, null);
         field = new FieldInfo(this, INTERFACE_PACKAGE, Constants.DEFAULT_FIELD_LENGTH, null, null);
         field = new FieldInfo(this, THIN_PACKAGE, 40, null, null);
         field = new FieldInfo(this, RESOURCE_PACKAGE, 40, null, null);
         field = new FieldInfo(this, LAST_PACKAGE_UPDATE, 25, null, null);
         field.setDataClass(Date.class);
         field = new FieldInfo(this, PACKAGES_BASE_PATH, 100, null, null);
         field = new FieldInfo(this, PACKAGES_PATH, 100, null, null);
     }
     /**
     * Set up the key areas.
     */
     public void setupKeys()
     {
         KeyAreaInfo keyArea = null;
         keyArea = new KeyAreaInfo(this, Constants.UNIQUE, ID_KEY);
         keyArea.addKeyField(ID, Constants.ASCENDING);
     }
 
 }
