 package pl.psnc.dl.wf4ever.monitoring;
 
 import java.io.IOException;
 import java.net.URI;
 
 import org.apache.log4j.Logger;
 import org.quartz.Job;
 import org.quartz.JobExecutionContext;
 import org.quartz.JobExecutionException;
 
 import pl.psnc.dl.wf4ever.ApplicationProperties;
 import pl.psnc.dl.wf4ever.darceo.client.DArceoClient;
 import pl.psnc.dl.wf4ever.darceo.client.DArceoException;
 import pl.psnc.dl.wf4ever.db.dao.ResearchObjectPreservationStatusDAO;
 import pl.psnc.dl.wf4ever.db.hibernate.HibernateUtil;
 import pl.psnc.dl.wf4ever.dl.UserMetadata;
 import pl.psnc.dl.wf4ever.dl.UserMetadata.Role;
 import pl.psnc.dl.wf4ever.model.Builder;
 import pl.psnc.dl.wf4ever.model.RO.ResearchObject;
 import pl.psnc.dl.wf4ever.preservation.ResearchObjectPreservationStatus;
 import pl.psnc.dl.wf4ever.preservation.Status;
 
 /**
  * This job calculates checksums for all resources of a research object and compares them with the checksums stored in
  * the database. The result is stored in the context.
  * 
  * @author pejot
  * 
  */
 public class PreservationJob implements Job {
 
     /** Logger. */
     private static final Logger LOGGER = Logger.getLogger(PreservationJob.class);
 
     /** Key for the input data. The value must be a URI. */
     public static final String RESEARCH_OBJECT_URI = "ResearchObjectUri";
 
     /** Resource model builder. */
     private Builder builder;
 
     /** PReservation dao. */
     ResearchObjectPreservationStatusDAO dao;
 
 
     @Override
     public void execute(JobExecutionContext context)
             throws JobExecutionException {
         boolean started = !HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().isActive();
         URI researchObjectUri = (URI) context.getMergedJobDataMap().get(RESEARCH_OBJECT_URI);
         if (started) {
             HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().begin();
         }
         try {
             if (builder == null) {
                 //FIXME RODL URI should be better
                 UserMetadata userMetadata = new UserMetadata("rodl", "RODL decay monitor", Role.ADMIN,
                         URI.create(ApplicationProperties.getContextPath()));
                 builder = new Builder(userMetadata);
             }
             dao = new ResearchObjectPreservationStatusDAO();
             ResearchObject researchObject = ResearchObject.get(builder, researchObjectUri);
            LOGGER.debug("Processing " + researchObjectUri.toString() + " in context of dArceo");
             ResearchObjectPreservationStatus status = dao.findById(researchObjectUri.toString());
             if (researchObject != null) {
                 if (status == null) {
                     status = new ResearchObjectPreservationStatus(researchObjectUri, Status.NEW);
                     dao.save(status);
                 } else if (status.getStatus() == null) {
                     status.setStatus(Status.NEW);
                     dao.save(status);
                 }
                 switch (status.getStatus()) {
                     case NEW:
                         DArceoClient.getInstance()
                                 .postORUpdateBlocking(DArceoClient.getInstance().post(researchObject));
                         break;
                     case UPDATED:
                         DArceoClient.getInstance().postORUpdateBlocking(
                             DArceoClient.getInstance().update(researchObject));
                         break;
                     case UP_TO_DATE:
                         break;
                     default:
                         break;
                 }
             } else {
                 status = dao.findById(researchObjectUri.toString());
                 if (status != null && status.getStatus() == Status.DELETED) {
                     DArceoClient.getInstance().delete(researchObjectUri);
                 }
             }
             status.setStatus(Status.UP_TO_DATE);
             dao.save(status);
         } catch (DArceoException | IOException e) {
             LOGGER.error("Couldn't preserved " + researchObjectUri);
         } finally {
             if (started) {
                 HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();
             }
         }
     }
 
 
     public Builder getBuilder() {
         return builder;
     }
 
 
     public void setBuilder(Builder builder) {
         this.builder = builder;
     }
 
 }
