 package app.cts.form;
 
 import com.netspective.sparx.form.DialogContext;
 import com.netspective.sparx.form.DialogContextUtils;
 import com.netspective.sparx.form.field.type.DateTimeField;
 import com.netspective.axiom.sql.Query;
 import com.netspective.axiom.sql.QueryResultSet;
 import com.netspective.axiom.sql.ResultSetUtils;
 import com.netspective.axiom.ConnectionContext;
 import com.netspective.commons.value.GenericValue;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.naming.NamingException;
 import java.sql.SQLException;
 import java.util.Map;
 import java.io.IOException;
 import java.io.Writer;
 
 import auto.dal.db.DataAccessLayer;
 import auto.dal.db.vo.PersonContact;
 import auto.dal.db.vo.impl.PersonContactVO;
 import auto.dal.db.dao.person.PersonContactTable;
 import auto.dal.db.dao.PersonTable;
 import auto.dcb.cts.PersonContactInfoContext;
 import auto.id.sql.schema.db.enum.RecordStatus;
 
 
 /**
  */
 public class PersonContactDialog extends com.netspective.sparx.form.Dialog
 {
     public void populateValues(DialogContext dc, int formatType)
     {
         super.populateValues(dc, formatType);
         if(dc.getAuthenticatedUser() == null)
             return;
 
         dc.getPerspectives();
         HttpServletRequest request = dc.getHttpRequest();
         String idParam = request.getParameter("id");
         if(dc.isInitialEntry() && (dc.editingData() || dc.deletingData()))
         {
             DialogContext.DialogFieldStates fieldStates = dc.getFieldStates();
             Query query = dc.getSqlManager().getQuery(auto.id.sql.query.Cts.GET_CONTACT_INFO_BY_ID);
             try
             {
                 QueryResultSet qrs = query.execute(dc, new Object[] { idParam }, false);
                 Map responses = ResultSetUtils.getInstance().getResultSetSingleRowAsMap(qrs.getResultSet());
 
                 fieldStates.getState("contactType.method_type").getValue().setValue(responses.get("method_type"));
                 fieldStates.getState("method_value").getValue().setValue(responses.get("method_value"));
                 qrs.close(true);
                 qrs = null;
             }
             catch (NamingException e)
             {
                 e.printStackTrace();
             }
             catch (SQLException e)
             {
                 e.printStackTrace();
             }
         }
     }
 
     /**
      *
      * @param writer
      * @param dc
      * @throws IOException
      */
     public void execute(Writer writer, DialogContext dc) throws IOException
     {
         ConnectionContext cc = null;
         try
         {
             // get a connection from the default data source
             cc = dc.getConnection(null, true);
              // get the dialog context bean specific to our dialog so we can retrieve values
             PersonContactInfoContext pcic = new PersonContactInfoContext(dc);
 
             // grab the singleton  data access object (DAO)
             PersonTable personTable = DataAccessLayer.getInstance().getPersonTable();
             PersonContactTable pcTable = personTable.getPersonContactTable();
             try
             {
                 if (dc.addingData())
                 {
                     PersonContact pContact = new PersonContactVO();
                     pContact.setRecStatId(new Integer(RecordStatus.ACTIVE));
                     pContact.setMethodValue(pcic.getMethodValue().getTextValue());
                     pContact.setMethodType(Integer.valueOf(pcic.getContactType_methodType().getTextValue()));
                    pContact.setParentId(Long.valueOf(pcic.getId().getTextValue()));
 
                     PersonContactTable.Record record = pcTable.createRecord();
                     record.setValues(pContact);
                     record.insert(cc);
                     cc.getConnection().commit();
                 }
                 else if (dc.editingData())
                 {
                     PersonContactTable.Record record = pcTable.getRecordByPrimaryKey(cc, pcic.getId().getTextValue());
                     //DialogContextUtils.getInstance().populateRowWithFieldValues(dc, record.getRow());
                     record.setMethodValue(pcic.getMethodValue());
                     record.update(cc);
                     cc.getConnection().commit();
                 }
                 else if (dc.deletingData())
                 {
                     PersonContactTable.Record record = pcTable.getRecordByPrimaryKey(cc, pcic.getId().getTextValue());
                     record.delete(cc);
                     cc.getConnection().commit();
                 }
             }
             catch (SQLException se)
             {
                 // failed to add/edit/delete record
                 getLog().error("Error in data perspective " + dc.getPerspectives(), se);
                 handlePostExecuteException(writer, dc, "Error in data perspective, rolling back.", se);
                 try
                 {
                     cc.getConnection().rollback();
                 }
                 catch (Exception e)
                 {
                     handlePostExecuteException(writer, dc, "Unable to rollback after error", e);
                 }
                 return;
             }
             handlePostExecute(writer, dc);
 
         }
         catch (Exception e)
         {
             // failed to get a database connection
             e.printStackTrace();
             handlePostExecuteException(writer, dc, "Unable to get connection", e);
         }
         finally
         {
             try
             {
                 if(cc != null)
                     cc.close();
             }
             catch (SQLException se)
             {
                 handlePostExecuteException(writer, dc, "Unable to close connection", se);
             }
         }
     }
 }
