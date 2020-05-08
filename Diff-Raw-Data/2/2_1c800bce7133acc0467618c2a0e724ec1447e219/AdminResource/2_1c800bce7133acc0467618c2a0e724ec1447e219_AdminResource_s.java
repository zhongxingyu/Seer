 package pl.psnc.dl.wf4ever.admin;
 
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.core.Response;
 
 import org.apache.log4j.Logger;
 import org.quartz.SchedulerException;
 
 import pl.psnc.dl.wf4ever.auth.RequestAttribute;
 import pl.psnc.dl.wf4ever.db.ResearchObjectId;
 import pl.psnc.dl.wf4ever.db.dao.ResearchObjectIdDAO;
 import pl.psnc.dl.wf4ever.model.Builder;
 import pl.psnc.dl.wf4ever.model.RO.ResearchObject;
 import pl.psnc.dl.wf4ever.monitoring.MonitoringScheduler;
 
 /**
  * The admin namespace for protected functions provided via API.
  * 
  * @author pejot
  * 
  */
 @Path("admin/")
 public class AdminResource {
 
     /** Resource builder. */
     @RequestAttribute("Builder")
     private Builder builder;
 
     /** logger. */
     private static final Logger LOGGER = Logger.getLogger(AdminResource.class);
 
 
     /**
      * Reindex solr data.
      * 
      * @return comunicate.
      */
     @POST
     @Path("solr/reindex/")
     public String solrReindex() {
         for (ResearchObject ro : ResearchObject.getAll(builder, null)) {
             ro.updateIndexAttributes();
         }
         return "Operation finished successfully";
     }
 
 
     /**
      * Schedule all monitoring jobs now.
      * 
      * @return 200 OK
      * @throws SchedulerException
      *             if jobs couldn't be scheduled
      */
     @POST
     @Path("monitor/all")
     public String monitorAll()
             throws SchedulerException {
         MonitoringScheduler.getInstance().scheduleAllJobsNow();
         return "Operation finished successfully";
     }
 
 
     /**
      * Delete research objects. Useful for ROs that have URIs not matching the RO API.
      * 
      * @param researchObjects
      *            a list of RO URIs
      * @return 200 OK
      */
     @POST
     @Consumes("text/uri-list")
     @Path("force-delete")
     public Response forceDeleteROs(String researchObjects) {
         String[] uris = researchObjects.split("\\s");
         StringBuilder sb = new StringBuilder("Successfully deleted the following research objects:\r\n");
         for (String uri : uris) {
             try {
                 URI uri2 = new URI(uri);
                 ResearchObject researchObject = ResearchObject.get(builder, uri2);
                 researchObject.delete();
                 sb.append("* " + uri2 + "\r\n");
             } catch (Exception e) {
                 LOGGER.warn("Can't delete RO " + uri, e);
             }
         }
         return Response.ok(sb.toString()).build();
     }
 
 
     @POST
    @Path("preservrvetion/id/synchronize")
     public String synchronizeIds() {
         List<URI> storedRosList = new ArrayList<URI>();
         for (ResearchObject ro : ResearchObject.getAll(builder, null)) {
             storedRosList.add(ro.getUri());
         }
 
         ResearchObjectIdDAO dao = new ResearchObjectIdDAO();
         for (ResearchObjectId id : dao.all()) {
             if (!storedRosList.contains(id.getId())) {
                 dao.delete(id);
             }
         }
         for (URI rui : storedRosList) {
             if (dao.findByPrimaryKey(rui) == null) {
                 dao.save(new ResearchObjectId(rui));
             }
         }
         return "Operation succeed";
     }
 }
