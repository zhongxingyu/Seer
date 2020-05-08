 /**
  * PendingSubmissionsTransformer.java
  *
  * This file is released under the same license as DSpace itself.
  *
  * @author Chris Charles ccharles@uoguelph.ca
  */
 
 package ca.uoguelph.lib.app.xmlui.aspect.general;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import org.dspace.app.xmlui.cocoon.AbstractDSpaceTransformer;
 import org.dspace.app.xmlui.wing.Message;
 import org.dspace.app.xmlui.wing.WingException;
 import org.dspace.app.xmlui.wing.element.Body;
 import org.dspace.app.xmlui.wing.element.Division;
 import org.dspace.app.xmlui.wing.element.Para;
 import org.dspace.eperson.EPerson;
 
 
 /**
  * Add a div to the DRI document notifying the user that there are pending
  * submissions.
  *
  * Pending submissions are items which have been submitted but have not yet
  * been accepted into any collections due to workflow restrictions.
  */
 public class PendingSubmissionsTransformer extends AbstractDSpaceTransformer
 {
     private static final Message T_pending_submissions_intro =
         message("xmlui.general.pending_submissions.intro");
     private static final Message T_one_pending_submission =
         message("xmlui.general.pending_submissions.link_single");
     private static final Message T_multiple_pending_submissions =
         message("xmlui.general.pending_submissions.link_multiple");
     private static final Message T_pending_submissions_outtro_single =
         message("xmlui.general.pending_submissions.outtro_single");
     private static final Message T_pending_submissions_outtro_multiple =
         message("xmlui.general.pending_submissions.outtro_multiple");
 
     /**
      * Add a new pending-submissions div and table to the DRI body and
      * populate from the database.
      *
      * @param body The DRI document's body element.
      */
     public void addBody(Body body) throws WingException, SQLException
     {
         EPerson currentUser = context.getCurrentUser();
 
         if (currentUser != null)
         {
             Statement statement = context.getDBConnection().createStatement();
 
             // Find out which metadata_field_id represents dc.title
             String title_field_query =
                 "SELECT metadata_field_id" +
                 "    FROM metadatafieldregistry AS fields," +
                 "            metadataschemaregistry AS schemas" +
                 "    WHERE fields.metadata_schema_id" +
                 "            = schemas.metadata_schema_id" +
                 "        AND schemas.short_id = 'dc'" +
                 "        AND fields.element = 'title'" +
                 "        AND fields.qualifier IS NULL" +
                 "    LIMIT 1;";
 
             ResultSet dcTitleField = statement.executeQuery(title_field_query);
             dcTitleField.next();
             String dcTitleID = dcTitleField.getString("metadata_field_id");
 
             // Retrieve the number of pending submissions
             String submissions_query =
                 "SELECT COUNT(metadatavalue.text_value)" +
                 "    FROM workflowitem, item, metadatavalue" +
                 "    WHERE workflowitem.item_id = item.item_id" +
                 "        AND item.item_id = metadatavalue.item_id" +
                 "        AND metadatavalue.metadata_field_id = " + dcTitleID +
                 "        AND item.submitter_id = " + currentUser.getID() + ";";
 
             ResultSet submissions = statement.executeQuery(submissions_query);
 
             // Advance to the first record and get its value
             submissions.next();
             int rowCount = submissions.getInt(1);
 
             // Add the pending-submissions div if pending submissions exist.
             if (rowCount > 0)
             {
                 Message T_link;
                 Message T_outtro;
 
                 if (rowCount == 1)
                 {
                     T_link = T_one_pending_submission;
                     T_outtro = T_pending_submissions_outtro_single;
                 }
                 else
                 {
                     T_link =
                         T_multiple_pending_submissions.parameterize(rowCount);
                     T_outtro  = T_pending_submissions_outtro_multiple;
                 }
 
                 Division pending = body.addDivision("pending-submissions");
                 Para para = pending.addPara();
                 para.addContent(T_pending_submissions_intro);
                para.addXref("submissions", T_link);
                 para.addContent(T_outtro);
             }
         }
     }
 }
