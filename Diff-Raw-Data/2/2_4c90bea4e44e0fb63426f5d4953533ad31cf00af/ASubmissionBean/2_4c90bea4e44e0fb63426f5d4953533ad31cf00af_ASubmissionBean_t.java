 package com.flexive.asubmission.war;
 
 import com.flexive.shared.exceptions.FxApplicationException;
 import com.flexive.shared.search.SortDirection;
 import com.flexive.shared.search.FxResultSet;
 import com.flexive.shared.search.query.SqlQueryBuilder;
 import com.flexive.shared.content.FxContent;
 import com.flexive.shared.content.FxPK;
 import com.flexive.shared.EJBLookup;
 import com.flexive.shared.CacheAdmin;
 import com.flexive.shared.FxContext;
 import com.flexive.shared.security.ACL;
 import com.flexive.faces.model.FxResultSetDataModel;
 import com.flexive.faces.messages.FxFacesMsgErr;
 
 import javax.faces.model.DataModel;
 
 
 /**
  * JSF managed bean for the announcement-submission example application.
  *
  * @author Hans Bacher (hans.bacher@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  * @version $Rev$
  */
 public class ASubmissionBean {
 
     private DataModel announcementEntries;
     private FxContent content; // announcement content instance
     private FxPK instancePk; // primary key object
 
     /**
      * Returns the result set from the database query as DataModel
      *
      * @return DataModel announcement entries
      * @throws FxApplicationException -
      */
     public DataModel getAnnouncementEntries() throws FxApplicationException {
         if (announcementEntries == null) {
             final FxResultSet result = new SqlQueryBuilder()
                     .select("@permissions", "@pk", "entryTitle", "submissionDate", "publishDate", "submissionURL", "publishURL")
                     .type("announcementEntry")
                     .orderBy("publishDate", SortDirection.DESCENDING)
                     .getResult();
             announcementEntries = new FxResultSetDataModel(result);
         }
         return announcementEntries;
     }
 
     /**
      * Save content instance
      *
      * @return null because the page is reloaded
      */
     public String save() {
         try {
             if (content.getPk().isNew()) // Instance ACL Id only set for new instances
                content.setAclId(CacheAdmin.getEnvironment().getACL("Announcement_Entry_ACL").getId());
             EJBLookup.getContentEngine().save(content.copy());
         } catch (FxApplicationException e) {
             new FxFacesMsgErr(e).addToContext();
         }
         return null;
     }
 
     /**
      * Delete content instance
      *
      * @return string outcome - a redirect is needed, if the deletion succeeds
      */
     public String delete() {
         try {
             EJBLookup.getContentEngine().remove(instancePk);
             return "instance_deleted";
         } catch (FxApplicationException e) {
             new FxFacesMsgErr(e).addToContext();
             return null;
         }
     }
 
     /**
      * Navigation method
      *
      * @return string outcome
      */
     public String navEditContent() {
         return "edit_content";
     }
 
     /**
      * Navigation method
      *
      * @return string outcome
      */
     public String navCreateContent() {
         return "create_content";
     }
 
     /**
      * Checks if the user may create announcements
      *
      * @return true if the user may create announcements
      */
     public boolean isMayCreateAnnouncement() {
         if (FxContext.getUserTicket().mayCreateACL(ACL.Category.STRUCTURE.getDefaultId(), -1L))
             return true;
         return false;
     }
 
     public FxContent getContent() {
         return content;
     }
 
     public void setContent(FxContent content) {
         this.content = content;
     }
 
     public FxPK getInstancePk() {
         return instancePk;
     }
 
     public void setInstancePk(FxPK instancePk) {
         this.instancePk = instancePk;
     }
 }
