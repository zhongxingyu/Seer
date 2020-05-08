 /**
  * @(#)FinStmt.
  * Copyright © 2012 tourapp.com. All rights reserved.
  */
 package com.tourapp.tour.genled.db.finstmt;
 
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
 import com.tourapp.model.tour.genled.db.finstmt.*;
 
 /**
  *  FinStmt - Financial Statement.
  */
 public class FinStmt extends VirtualRecord
      implements FinStmtModel
 {
     private static final long serialVersionUID = 1L;
 
     public static final int FIN_STMT_DETAIL_GRID_SCREEN = ScreenConstants.DETAIL_MODE;
     /**
      * Default constructor.
      */
     public FinStmt()
     {
         super();
     }
     /**
      * Constructor.
      */
     public FinStmt(RecordOwner screen)
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
         return (m_tableName == null) ? Record.formatTableNames(FIN_STMT_FILE, bAddQuotes) : super.getTableNames(bAddQuotes);
     }
     /**
      * Get the name of a single record.
      */
     public String getRecordName()
     {
         return "Statement";
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
         return DBConstants.LOCAL | DBConstants.USER_DATA;
     }
     /**
      * MakeScreen Method.
      */
     public ScreenParent makeScreen(ScreenLoc itsLocation, ComponentParent parentScreen, int iDocMode, Map<String,Object> properties)
     {
         ScreenParent screen = null;
         if ((iDocMode & ScreenConstants.DOC_MODE_MASK) == FinStmt.FIN_STMT_DETAIL_GRID_SCREEN)
            screen = Record.makeNewScreen(FinStmtDetail.FIN_STMT_DETAIL_SCREEN_CLASS, itsLocation, parentScreen, iDocMode | ScreenConstants.DONT_DISPLAY_FIELD_DESC, properties, this, true);
         else if ((iDocMode & ScreenConstants.MAINT_MODE) == ScreenConstants.MAINT_MODE)
             screen = Record.makeNewScreen(FIN_STMT_SCREEN_CLASS, itsLocation, parentScreen, iDocMode | ScreenConstants.DONT_DISPLAY_FIELD_DESC, properties, this, true);
         else
             screen = Record.makeNewScreen(FIN_STMT_GRID_SCREEN_CLASS, itsLocation, parentScreen, iDocMode | ScreenConstants.DONT_DISPLAY_FIELD_DESC, properties, this, true);
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
             field = new StringField(this, STATEMENT_DESC, 60, null, null);
         if (iFieldSeq == 4)
         {
             field = new ReferenceField(this, FIN_STMT_HEADER_ID, Constants.DEFAULT_FIELD_LENGTH, null, null);
             field.setNullable(false);
         }
         if (iFieldSeq == 5)
             field = new ShortField(this, STMT_SEQUENCE, Constants.DEFAULT_FIELD_LENGTH, null, null);
         if (iFieldSeq == 6)
             field = new StatementTypeField(this, STATEMENT_TYPE, 1, null, null);
         if (iFieldSeq == 7)
             field = new StatementFormatField(this, STATEMENT_FORMAT, 1, null, null);
         if (iFieldSeq == 8)
             field = new StatementNumberField(this, STATEMENT_NUMBER, 1, null, null);
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
             keyArea = this.makeIndex(DBConstants.NOT_UNIQUE, "FinStmtHeaderID");
             keyArea.addKeyField(FIN_STMT_HEADER_ID, DBConstants.ASCENDING);
             keyArea.addKeyField(STMT_SEQUENCE, DBConstants.ASCENDING);
         }
         if (keyArea == null)
             keyArea = super.setupKey(iKeyArea);     
         return keyArea;
     }
 
 }
