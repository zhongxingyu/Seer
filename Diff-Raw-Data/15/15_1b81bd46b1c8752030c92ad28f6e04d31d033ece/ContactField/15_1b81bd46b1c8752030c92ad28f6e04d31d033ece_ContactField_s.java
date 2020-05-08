 /**
  * @(#)ContactField.
  * Copyright © 2012 jbundle.org. All rights reserved.
  * GPL3 Open Source Software License.
  */
 package org.jbundle.main.db.base;
 
 import java.awt.*;
 import java.util.*;
 
 import org.jbundle.base.db.*;
 import org.jbundle.thin.base.util.*;
 import org.jbundle.thin.base.db.*;
 import org.jbundle.base.db.event.*;
 import org.jbundle.base.db.filter.*;
 import org.jbundle.base.field.*;
 import org.jbundle.base.field.convert.*;
 import org.jbundle.base.field.event.*;
 import org.jbundle.base.screen.model.*;
 import org.jbundle.base.screen.model.util.*;
 import org.jbundle.base.model.*;
 import org.jbundle.base.util.*;
 import org.jbundle.model.*;
 import org.jbundle.model.db.*;
 import org.jbundle.model.screen.*;
 import org.jbundle.main.db.*;
 
 /**
  *  ContactField - Contact type
 Either a Vendor or a Profile.
 Note: This class has a bunch of very specialized logic to take care of
 the fact that a contact can come from the Vendor or the Profile file
 depending on the contact type..
  */
 public class ContactField extends ReferenceField
 {
     protected Company m_recVendor = null;
     protected Company m_recProfile = null;
     protected String VENDOR_CONTACT_TYPE_ID;
     protected String PROFILE_CONTACT_TYPE_ID;
     /**
      * Default constructor.
      */
     public ContactField()
     {
         super();
     }
     /**
      * Constructor.
      * @param record The parent record.
      * @param strName The field name.
      * @param iDataLength The maximum string length (pass -1 for default).
      * @param strDesc The string description (usually pass null, to use the resource file desc).
      * @param strDefault The default value (if object, this value is the default value, if string, the string is the default).
      */
     public ContactField(Record record, String strName, int iDataLength, String strDesc, Object strDefault)
     {
         this();
         this.init(record, strName, iDataLength, strDesc, strDefault);
     }
     /**
      * Initialize class fields.
      */
     public void init(Record record, String strName, int iDataLength, String strDesc, Object strDefault)
     {
         m_recVendor = null;
         m_recProfile = null;
         VENDOR_CONTACT_TYPE_ID = "";
         PROFILE_CONTACT_TYPE_ID = "";
         super.init(record, strName, iDataLength, strDesc, strDefault);
     }
     /**
      * Get (or make) the current record for this reference.
      */
     public Record makeReferenceRecord(RecordOwner recordOwner)
     {
         ContactTypeField fldContactType = this.getContactTypeField();
         if (fldContactType == null)
             return null;
         if (this.getRecord().getRecordOwner() != null)
             recordOwner = this.getRecord().getRecordOwner();
         ContactType recContactType = (ContactType)fldContactType.getReferenceRecord();
         recContactType = (ContactType)fldContactType.getReference();
         if (m_recVendor == null)
         {
             String strRecordName = "Vendor";
             m_recVendor = (Company)recContactType.makeRecordFromRecordName(strRecordName, recordOwner);
             if (m_recVendor != null)
                 VENDOR_CONTACT_TYPE_ID = recContactType.getContactType(m_recVendor).getField(ContactType.ID).toString();
         }
         if (m_recProfile == null)
         {
             String strRecordName = "Profile";
             m_recProfile = (Company)recContactType.makeRecordFromRecordName(strRecordName, recordOwner);
             if (m_recProfile != null)
                 PROFILE_CONTACT_TYPE_ID = recContactType.getContactType(m_recProfile).getField(ContactType.ID).toString();
         }
         recContactType = (ContactType)fldContactType.getReference();
         String strHeaderRecordName = null;
         if (recContactType != null)
             strHeaderRecordName = recContactType.getField(ContactType.CODE).toString();
         if ((strHeaderRecordName == null) || (strHeaderRecordName.length() == 0))
             strHeaderRecordName = recordOwner.getProperty(fldContactType.getFieldName());
         Record record = m_recVendor;
         if (strHeaderRecordName != null)
         {    // Always
             if ("Vendor".equalsIgnoreCase(strHeaderRecordName))
                 record = m_recVendor;
             else if ("Profile".equalsIgnoreCase(strHeaderRecordName))
                 record = m_recProfile;
         }
         return record;
     }
     /**
      * Set up the default screen control for this field.
      * @param itsLocation Location of this component on screen (ie., GridBagConstraint).
      * @param targetScreen Where to place this component (ie., Parent screen or GridBagLayout).
      * @param converter The converter to set the screenfield to.
      * @param iDisplayFieldDesc Display the label? (optional).
      * @param properties Extra properties
      * @return Return the component or ScreenField that is created for this field.
      */
     public ScreenComponent setupDefaultView(ScreenLoc itsLocation, ComponentParent targetScreen, Convert converter, int iDisplayFieldDesc, Map<String, Object> properties)
     {
         this.makeReferenceRecord();
         if (m_recVendor == null)    // Possible that these are not in my classpath
             return super.setupDefaultView(itsLocation, targetScreen, converter, iDisplayFieldDesc, properties);
         
         ScreenComponent screenField = null;
         FieldListener listener = null;
         // ----------- Vendor -----------
         // First set up the code access:
         m_recVendor.setKeyArea(Company.CODE_KEY);
         m_recVendor.getField(Company.CODE).addListener(new MainReadOnlyHandler(Company.CODE_KEY));
         //  Set up the listener to read the current record on a valid main record
         m_recVendor.getField(Company.ID).addListener(listener = new MainReadOnlyHandler(Company.ID_KEY));
         listener.setRespondsToMode(DBConstants.INIT_MOVE, true);
         listener.setRespondsToMode(DBConstants.READ_MOVE, true);
         // On change or new, change this; on change, set the type to vendor
         m_recVendor.addListener(new MoveOnEventHandler(this, m_recVendor.getField(Company.ID), null, true, true, false, false, false, null, false));
         m_recVendor.addListener(new MoveOnValidHandler(this.getContactTypeField(), VENDOR_CONTACT_TYPE_ID));
         // ----------- Profile -----------
         // First set up the code access:
         m_recProfile.setKeyArea(Company.CODE_KEY);
         m_recProfile.getField(Company.CODE).addListener(new MainReadOnlyHandler(Company.CODE_KEY));
         //  Set up the listener to read the current record on a valid main record
         m_recProfile.getField(Company.ID).addListener(listener = new MainReadOnlyHandler(Company.ID_KEY));
         listener.setRespondsToMode(DBConstants.INIT_MOVE, true);
         listener.setRespondsToMode(DBConstants.READ_MOVE, true);
         // On change or new, change this; on change, set the type to profile
         m_recProfile.addListener(new MoveOnEventHandler(this, m_recProfile.getField(Company.ID), null, true, true, false, false, false, null, false));
         m_recProfile.addListener(new MoveOnValidHandler(this.getContactTypeField(), PROFILE_CONTACT_TYPE_ID));
         
         this.getContactTypeField().addListener(listener = new InitOnChangeHandler(this));
         listener.setRespondsToMode(DBConstants.INIT_MOVE, false);
         listener.setRespondsToMode(DBConstants.READ_MOVE, false);
         String strAltFieldDesc = null;
         // Do the code to read the correct record
         Converter checkConverter = new CheckConverter(this.getContactTypeField(), PROFILE_CONTACT_TYPE_ID, strAltFieldDesc, true);
         Converter convContactID = new FlagDepFieldConverter(m_recVendor.getField(Company.ID), m_recProfile.getField(Company.ID), checkConverter);
         this.addListener(listener = new MoveOnChangeHandler(convContactID));
         listener.setRespondsToMode(DBConstants.INIT_MOVE, true);
         listener.setRespondsToMode(DBConstants.READ_MOVE, true);
         // Setup the code box
         checkConverter = new CheckConverter(this.getContactTypeField(), PROFILE_CONTACT_TYPE_ID, strAltFieldDesc, true);
         Converter conv = new FlagDepFieldConverter(m_recVendor.getField(Company.CODE), m_recProfile.getField(Company.CODE), checkConverter);
         conv = new FieldDescConverter(conv, this);  // Use the description for this field
         screenField = createScreenComponent(ScreenModel.EDIT_TEXT, itsLocation, targetScreen, conv, iDisplayFieldDesc, properties);
         // Set up to display the record description
         checkConverter = new CheckConverter(this.getContactTypeField(), PROFILE_CONTACT_TYPE_ID, strAltFieldDesc, true);
         itsLocation = targetScreen.getNextLocation(ScreenConstants.RIGHT_OF_LAST, ScreenConstants.DONT_SET_ANCHOR);
         iDisplayFieldDesc = ScreenConstants.DONT_DISPLAY_DESC;      // Display it only once
         Converter convContactName = new FlagDepFieldConverter(m_recVendor.getField(Company.NAME), m_recProfile.getField(Company.NAME), checkConverter);
         //+convContactName = new DisableFieldConverter(convContactName);    // Don't enable
         ScreenComponent sfDesc = createScreenComponent(ScreenModel.EDIT_TEXT, itsLocation, targetScreen, convContactName, iDisplayFieldDesc, properties);
         sfDesc.setEnabled(false);
         // Add the lookup button and form (opt) button (Even though SSelectBoxes don't use converter, pass it, so field.enable(true), etc will work)
        properties = new HashMap<String,Object>();
        properties.put(ScreenModel.RECORD, m_recVendor);
        properties.put(ScreenModel.COMMAND, ThinMenuConstants.LOOKUP);
        properties.put(ScreenModel.IMAGE, ThinMenuConstants.LOOKUP);
        createScreenComponent(ScreenModel.CANNED_BOX, targetScreen.getNextLocation(ScreenConstants.RIGHT_OF_LAST, ScreenConstants.DONT_SET_ANCHOR), targetScreen, converter, ScreenConstants.DONT_DISPLAY_DESC, properties);
        /* ??
        new SSelectBox(, targetScreen, converter, , m_recVendor)
         {
             public Record getRecord()
             {
                 if (getContactTypeField() != null)
                 {
                     String strContactTypeID = getContactTypeField().toString();
                     if (PROFILE_CONTACT_TYPE_ID.equals(strContactTypeID))
                         return m_recProfile;
                 }
                 return m_record;
             }
         };
        */
         return screenField;
     }
     /**
      * Get the contact type field in the same record as this contact field.
      * @return The contact type field.
      */
     public ContactTypeField getContactTypeField()
     {
         BaseField field = this.getRecord().getField("ContactTypeID");
         if (field instanceof ContactTypeField)
             return (ContactTypeField)field;
         return null;    // Never
     }
 
 }
