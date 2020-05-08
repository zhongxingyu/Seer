 /*
  * Copyright (c) 2000-2003 Netspective Communications LLC. All rights reserved.
  *
  * Netspective Communications LLC ("Netspective") permits redistribution, modification and use of this file in source
  * and binary form ("The Software") under the Netspective Source License ("NSL" or "The License"). The following
  * conditions are provided as a summary of the NSL but the NSL remains the canonical license and must be accepted
  * before using The Software. Any use of The Software indicates agreement with the NSL.
  *
  * 1. Each copy or derived work of The Software must preserve the copyright notice and this notice unmodified.
  *
  * 2. Redistribution of The Software is allowed in object code form only (as Java .class files or a .jar file
  *    containing the .class files) and only as part of an application that uses The Software as part of its primary
  *    functionality. No distribution of the package is allowed as part of a software development kit, other library,
  *    or development tool without written consent of Netspective. Any modified form of The Software is bound by these
  *    same restrictions.
  *
  * 3. Redistributions of The Software in any form must include an unmodified copy of The License, normally in a plain
  *    ASCII text file unless otherwise agreed to, in writing, by Netspective.
  *
  * 4. The names "Netspective", "Axiom", "Commons", "Junxion", and "Sparx" are trademarks of Netspective and may not be
  *    used to endorse products derived from The Software without without written consent of Netspective. "Netspective",
  *    "Axiom", "Commons", "Junxion", and "Sparx" may not appear in the names of products derived from The Software
  *    without written consent of Netspective.
  *
  * 5. Please attribute functionality where possible. We suggest using the "powered by Netspective" button or creating
  *    a "powered by Netspective(tm)" link to http://www.netspective.com for each application using The Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS" WITHOUT A WARRANTY OF ANY KIND. ALL EXPRESS OR IMPLIED REPRESENTATIONS AND
  * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT,
  * ARE HEREBY DISCLAIMED.
  *
  * NETSPECTIVE AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE OR ANY THIRD PARTY AS A
  * RESULT OF USING OR DISTRIBUTING THE SOFTWARE. IN NO EVENT WILL NETSPECTIVE OR ITS LICENSORS BE LIABLE FOR ANY LOST
  * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
  * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THE SOFTWARE, EVEN
  * IF HE HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
  *
  * @author Shahid N. Shah
  */
 
 /**
 * $Id: TableDialog.java,v 1.8 2003-08-28 14:44:30 shahid.shah Exp $
  */
 
 package com.netspective.sparx.form.schema;
 
 import com.netspective.sparx.form.*;
 import com.netspective.sparx.form.field.DialogField;
 import com.netspective.sparx.form.field.DialogFields;
 import com.netspective.sparx.navigate.NavigationContext;
 import com.netspective.axiom.schema.*;
 import com.netspective.axiom.ConnectionContext;
 import com.netspective.commons.value.ValueSource;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.naming.NamingException;
 import java.sql.SQLException;
 import java.io.Writer;
 import java.io.IOException;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 public class TableDialog extends Dialog
 {
     private static final Log log = LogFactory.getLog(TableDialog.class);
     public static final String PARAMNAME_PRIMARYKEY = "pk";
 
     private String schemaName;
     private String tableName;
     private Schema schema;
     private Table table;
     private Column primaryKeyColumn;
     private ValueSource dataSrc;
     private ValueSource primaryKeyValueForEditOrDelete;
 
     public TableDialog()
     {
         setDialogContextClass(TableDialogContext.class);
     }
 
     public TableDialog(DialogsPackage pkg)
     {
         super(pkg);
         setDialogContextClass(TableDialogContext.class);
     }
 
     public DialogFlags createDialogFlags()
     {
         return new TableDialogFlags();
     }
 
     public String getSchemaName()
     {
         return schemaName;
     }
 
     public void setSchemaName(String schemaName)
     {
         this.schemaName = schemaName;
     }
 
     public String getTableName()
     {
         return tableName;
     }
 
     public void setTableName(String tableName)
     {
         this.tableName = tableName;
     }
 
     public Schema getSchema()
     {
         return schema;
     }
 
     public void setSchema(Schema schema)
     {
         this.schema = schema;
         this.schemaName = schema.getName();
     }
 
     public Table getTable()
     {
         return table;
     }
 
     public void setTable(Table table)
     {
         this.table = table;
         this.tableName = table.getName();
         if(table.getPrimaryKeyColumns().size() == 1)
             primaryKeyColumn = table.getPrimaryKeyColumns().getSole();
     }
 
     public ValueSource getDataSrc()
     {
         return dataSrc;
     }
 
     public void setDataSrc(ValueSource dataSrc)
     {
         this.dataSrc = dataSrc;
     }
 
     public ValueSource getPrimaryKeyValueForEditOrDelete()
     {
         return primaryKeyValueForEditOrDelete;
     }
 
     public void setPrimaryKeyValueForEditOrDelete(ValueSource primaryKeyValueForEditOrDelete)
     {
         this.primaryKeyValueForEditOrDelete = primaryKeyValueForEditOrDelete;
     }
 
     /**
      * If the schema and table objects are null then try and use the names and look them up.
      * @param nc
      */
     public void finalizeContents(NavigationContext nc)
     {
         super.finalizeContents(nc);
 
         if(getSchema() == null)
             setSchema(nc.getSqlManager().getSchema(getSchemaName()));
 
         if(getTable() == null && getSchema() != null)
             setTable(getSchema().getTables().getByName(getTableName()));
 
         String pkColName = primaryKeyColumn.getName();
         DialogFields fields = getFields();
         for(int i = 0; i < fields.size(); i++)
         {
             DialogField field = fields.get(i);
             if(pkColName.equals(field.getName()))
             {
                 field.getFlags().setFlag(DialogField.Flags.PRIMARY_KEY);
                 break;
             }
         }
     }
 
     public boolean isValid(DialogContext dc)
     {
         if(! super.isValid(dc))
             return false;
 
         if(table == null)
         {
             dc.getValidationContext().addValidationError("Unable to find table {1} in schema {0}.", new Object[] { getSchemaName(), getTableName() });
             return false;
         }
 
         if(table.getPrimaryKeyColumns().size() != 1)
         {
             dc.getValidationContext().addValidationError("Table {1} in schema {0} does not have a single primary key.'", new Object[] { getSchemaName(), getTableName() });
             return false;
         }
 
         return true;
     }
 
     protected Object getPrimaryKeyValueForEditOrDelete(DialogContext dc)
     {
         if(primaryKeyValueForEditOrDelete != null)
             return primaryKeyValueForEditOrDelete.getValue(dc).getValue();
 
         HttpServletRequest request = dc.getHttpRequest();
         Object pkValue = request.getAttribute(PARAMNAME_PRIMARYKEY);
         if(pkValue == null)
         {
             pkValue = request.getAttribute(primaryKeyColumn.getName());
             if(pkValue == null && primaryKeyColumn != null)
             {
                 pkValue = request.getParameter(PARAMNAME_PRIMARYKEY);
                 if(pkValue == null)
                     pkValue = request.getParameter(primaryKeyColumn.getName());
             }
         }
         return pkValue;
     }
 
     public void populateValues(DialogContext dc, int formatType)
     {
         if(dc.isInitialEntry())
         {
             if(dc.addingData())
             {
                 DialogContextUtils.getInstance().populateFieldValuesFromRequestParamsAndAttrs(dc);
             }
             else if((dc.editingData() || dc.deletingData()))
             {
                 Object pkValue = getPrimaryKeyValueForEditOrDelete(dc);
 
                 try
                 {
                     if(pkValue != null)
                     {
                         ConnectionContext cc = null;
                         try
                         {
                             cc = dc.getConnection(dataSrc != null ? dataSrc.getTextValue(dc) : null, false, ConnectionContext.OWNERSHIP_DEFAULT);
                            Row row = table.getRowByPrimaryKeys(cc, new Object[] { pkValue }, null);
                             if(row != null)
                             {
                                DialogContextUtils.getInstance().populateFieldValuesFromRow(dc, row);
                                 ((TableDialogContext) dc).setPrimaryKeyValue(pkValue);
                             }
                             else if(! getDialogFlags().flagIsSet(TableDialogFlags.ALLOW_INSERT_IF_EDIT_PK_NOT_FOUND))
                                dc.getValidationContext().addValidationError("Unable to locate primary key {0}", new Object[] { pkValue });
                         }
                         finally
                         {
                             if(cc != null) cc.close();
                         }
                     }
                     else
                         dc.getValidationContext().addValidationError("Primary key not specified in primary-key-value-for-edit-or-delete attr, request attr or param.", (Object[]) null);
                 }
                 catch (NamingException e)
                 {
                     log.error("Error in populateValues PK = " + pkValue, e);
                 }
                 catch (SQLException e)
                 {
                     log.error("Error in populateValues PK = " + pkValue, e);
                 }
             }
         }
 
         super.populateValues(dc, formatType);
     }
 
     public void execute(Writer writer, DialogContext dc) throws IOException
     {
         TableDialogContext tdc = ((TableDialogContext) dc);
 
         ConnectionContext cc = null;
         try
         {
             cc = dc.getConnection(dataSrc != null ? dataSrc.getTextValue(dc) : null, false, ConnectionContext.OWNERSHIP_DEFAULT);
         }
         catch (Exception e)
         {
             handlePostExecuteException(writer, dc, dc.getPerspectives() + ": unable to establish connection", e);
             return;
         }
 
         Row row = DialogContextUtils.getInstance().createRowWithFieldValues(dc, getTable());
         try
         {
             switch((int) dc.getPerspectives().getFlags())
             {
                 case DialogPerspectives.ADD:
                     table.insert(cc, row);
                     tdc.setLastRowManipulated(row);
                     break;
 
                 case DialogPerspectives.EDIT:
                     if(getDialogFlags().flagIsSet(TableDialogFlags.ALLOW_INSERT_IF_EDIT_PK_NOT_FOUND))
                     {
                         Object pkValue = tdc.getPrimaryKeyValue();
                         if(pkValue == null)
                             table.insert(cc, row);
                         else
                             table.update(cc, row);
                     }
                     else
                         table.update(cc, row);
                     tdc.setLastRowManipulated(row);
                     break;
 
                 case DialogPerspectives.DELETE:
                     table.delete(cc, row);
                     tdc.setLastRowManipulated(row);
                     break;
             }
 
             cc.commitAndClose();
             handlePostExecute(writer, dc);
         }
         catch (Exception e)
         {
             handlePostExecuteException(writer, dc, dc.getPerspectives() + ": " + row, e);
         }
         finally
         {
             tdc.removePrimaryKeyValue();
         }
     }
 }
