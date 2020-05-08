 /**
  * @(#)TrxDesc.
  * Copyright © 2012 tourapp.com. All rights reserved.
  */
 package com.tourapp.tour.genled.db;
 
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
 import org.jbundle.base.model.*;
 import org.jbundle.base.util.*;
 import org.jbundle.model.*;
 import org.jbundle.model.db.*;
 import org.jbundle.model.screen.*;
 import com.tourapp.model.tour.genled.db.*;
 
 /**
  *  TrxDesc - Transaction type.
  */
 public class TrxDesc extends VirtualRecord
      implements TrxDescModel
 {
     private static final long serialVersionUID = 1L;
 
     /**
      * Default constructor.
      */
     public TrxDesc()
     {
         super();
     }
     /**
      * Constructor.
      */
     public TrxDesc(RecordOwner screen)
     {
         this();
         this.init(screen);
     }
     /**
      * Initialize class fields.
      */
     public void init(RecordOwner screen)
     {
         super.init(screen);
     }
     /**
      * Get the table name.
      */
     public String getTableNames(boolean bAddQuotes)
     {
         return (m_tableName == null) ? Record.formatTableNames(TRX_DESC_FILE, bAddQuotes) : super.getTableNames(bAddQuotes);
     }
     /**
      * Get the name of a single record.
      */
     public String getRecordName()
     {
         return "Transaction Description";
     }
     /**
      * Get the Database Name.
      */
     public String getDatabaseName()
     {
         return "genled";
     }
     /**
      * Is this a local (vs remote) file?.
      */
     public int getDatabaseType()
     {
         return DBConstants.LOCAL | DBConstants.SHARED_DATA | DBConstants.LOCALIZABLE;
     }
     /**
      * MakeScreen Method.
      */
     public ScreenParent makeScreen(ScreenLoc itsLocation, ComponentParent parentScreen, int iDocMode, Map<String,Object> properties)
     {
         ScreenParent screen = null;
         if ((iDocMode & ScreenConstants.DOC_MODE_MASK) == ScreenConstants.DETAIL_MODE)
             screen = Record.makeNewScreen(TrxGroup.TRX_GROUP_GRID_SCREEN_CLASS, itsLocation, parentScreen, iDocMode | ScreenConstants.DONT_DISPLAY_FIELD_DESC, properties, this, true);
        else if ((iDocMode & ScreenConstants.DOC_MODE_MASK) == (ScreenConstants.DETAIL_MODE | 4096))
             screen = Record.makeNewScreen(TrxStatus.TRX_STATUS_GRID_SCREEN_CLASS, itsLocation, parentScreen, iDocMode | ScreenConstants.DONT_DISPLAY_FIELD_DESC, properties, this, true);
         else if ((iDocMode & ScreenConstants.MAINT_MODE) == ScreenConstants.MAINT_MODE)
             screen = Record.makeNewScreen(TRX_DESC_SCREEN_CLASS, itsLocation, parentScreen, iDocMode | ScreenConstants.DONT_DISPLAY_FIELD_DESC, properties, this, true);
         else
             screen = Record.makeNewScreen(TRX_DESC_GRID_SCREEN_CLASS, itsLocation, parentScreen, iDocMode | ScreenConstants.DONT_DISPLAY_FIELD_DESC, properties, this, true);
         return screen;
     }
     /**
      * Add this field in the Record's field sequence.
      */
     public BaseField setupField(int iFieldSeq)
     {
         BaseField field = null;
         //if (iFieldSeq == 0)
         //{
         //  field = new CounterField(this, ID, Constants.DEFAULT_FIELD_LENGTH, null, null);
         //  field.setHidden(true);
         //}
         //if (iFieldSeq == 1)
         //{
         //  field = new RecordChangedField(this, LAST_CHANGED, Constants.DEFAULT_FIELD_LENGTH, null, null);
         //  field.setHidden(true);
         //}
         //if (iFieldSeq == 2)
         //{
         //  field = new BooleanField(this, DELETED, Constants.DEFAULT_FIELD_LENGTH, null, new Boolean(false));
         //  field.setHidden(true);
         //}
         if (iFieldSeq == 3)
             field = new StringField(this, DESC_CODE, 20, null, null);
         if (iFieldSeq == 4)
             field = new StringField(this, DESCRIPTION, 30, null, null);
         if (iFieldSeq == 5)
             field = new StringField(this, SOURCE_FILE, 50, null, null);
         if (iFieldSeq == 6)
         {
             field = new TrxSystemField(this, TRX_SYSTEM_ID, Constants.DEFAULT_FIELD_LENGTH, null, null);
             field.setNullable(false);
         }
         if (field == null)
             field = super.setupField(iFieldSeq);
         return field;
     }
     /**
      * Add this key area description to the Record.
      */
     public KeyArea setupKey(int iKeyArea)
     {
         KeyArea keyArea = null;
         if (iKeyArea == 0)
         {
             keyArea = this.makeIndex(DBConstants.UNIQUE, "ID");
             keyArea.addKeyField(ID, DBConstants.ASCENDING);
         }
         if (iKeyArea == 1)
         {
             keyArea = this.makeIndex(DBConstants.SECONDARY_KEY, "DescCode");
             keyArea.addKeyField(DESC_CODE, DBConstants.ASCENDING);
         }
         if (iKeyArea == 2)
         {
             keyArea = this.makeIndex(DBConstants.NOT_UNIQUE, "Description");
             keyArea.addKeyField(DESCRIPTION, DBConstants.ASCENDING);
         }
         if (iKeyArea == 3)
         {
             keyArea = this.makeIndex(DBConstants.NOT_UNIQUE, "TrxSystemID");
             keyArea.addKeyField(TRX_SYSTEM_ID, DBConstants.ASCENDING);
             keyArea.addKeyField(DESC_CODE, DBConstants.ASCENDING);
         }
         if (iKeyArea == 4)
         {
             keyArea = this.makeIndex(DBConstants.NOT_UNIQUE, "SourceFile");
             keyArea.addKeyField(SOURCE_FILE, DBConstants.ASCENDING);
         }
         if (keyArea == null)
             keyArea = super.setupKey(iKeyArea);     
         return keyArea;
     }
     /**
      * Convert the command to the screen document type.
      * @param strCommand The command text.
      * @param The standard document type (MAINT/DISPLAY/SELECT/MENU/etc).
      */
     public int commandToDocType(String strCommand)
     {
         if ("Status".equalsIgnoreCase(strCommand))
            return ScreenConstants.DETAIL_MODE | 4096;
         else
             return super.commandToDocType(strCommand);
     }
     /**
      * GetTrxDesc Method.
      */
     public TrxDesc getTrxDesc(String strSystemCode, String strDescCode)
     {
         TrxSystem recTrxSystem = new TrxSystem(this.findRecordOwner());
         // TODO - Move the SystemCode to this file.
         try {
             recTrxSystem.setKeyArea(TrxSystem.SYSTEM_CODE_KEY);
             recTrxSystem.getField(TrxSystem.SYSTEM_CODE).setString(strSystemCode);
             if (!recTrxSystem.seek("="))
                 return null;    // System code not found;
             this.setKeyArea(TrxDesc.TRX_SYSTEM_ID_KEY);
             this.getField(TrxDesc.TRX_SYSTEM_ID).moveFieldToThis(recTrxSystem.getField(TrxSystem.ID));
             this.getField(TrxDesc.DESC_CODE).setString(strDescCode);
             if (!this.seek("="))
                 return null;
         } catch (DBException ex)    {
             ex.printStackTrace();
         } finally {
             recTrxSystem.free();
         }
         return this;
     }
     /**
      * MakeSourceRecord Method.
      */
     public Record makeSourceRecord(RecordOwner recordOwner)
     {
         String strClassName = this.getField(TrxDesc.SOURCE_FILE).toString();
         return Record.makeRecordFromClassName(strClassName, recordOwner);
     }
     /**
      * MakeLinkTrxScreen Method.
      */
     public ScreenParent makeLinkTrxScreen(ScreenLoc itsLocation, ComponentParent parentScreen, int iDocMode, Map<String,Object> properties)
     {
         Record record = this.makeSourceRecord(this.findRecordOwner());
         
         ScreenParent screen = record.makeScreen(itsLocation, parentScreen, iDocMode, properties);
         
         return screen;
     }
 
 }
