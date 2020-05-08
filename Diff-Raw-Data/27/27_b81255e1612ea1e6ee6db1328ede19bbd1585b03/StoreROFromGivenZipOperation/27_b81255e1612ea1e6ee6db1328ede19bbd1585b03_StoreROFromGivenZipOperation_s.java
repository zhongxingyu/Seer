 package pl.psnc.dl.wf4ever.zip;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URI;
 
 import javax.ws.rs.core.UriInfo;
 
 import pl.psnc.dl.wf4ever.db.hibernate.HibernateUtil;
 import pl.psnc.dl.wf4ever.exceptions.BadRequestException;
 import pl.psnc.dl.wf4ever.job.JobStatus;
 import pl.psnc.dl.wf4ever.job.Operation;
 import pl.psnc.dl.wf4ever.job.OperationFailedException;
 import pl.psnc.dl.wf4ever.model.Builder;
 import pl.psnc.dl.wf4ever.model.RO.ResearchObject;
 import pl.psnc.dl.wf4ever.util.MemoryZipFile;
 
 /**
  * Operation which stores a research object given in a zip format from outside.
  * 
  * @author pejot
  * 
  */
 public class StoreROFromGivenZipOperation implements Operation {
 
     /** resource builder. */
     private Builder builder;
     /** zip input stream. */
     File zipFile;
     /** request uri info. */
     UriInfo uriInfo;
 
 
     /**
      * Constructor.
      * 
      * @param builder
      *            model instance builder
      * @param zipFile
      *            processed zip file
      * @param uriInfo
     *            request uri info
      */
    public StoreROFromGivenZipOperation(Builder builder, File zipFile, UriInfo uriInfo) {
         this.builder = builder;
         this.zipFile = zipFile;
         this.uriInfo = uriInfo;
     }
 
 
     @Override
     public void execute(JobStatus status)
             throws OperationFailedException {
         if (zipFile == null) {
             throw new OperationFailedException("Givem zip file is empty or null");
         }
         ROFromZipJobStatus roFromZipJobStatus;
         boolean started = !HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().isActive();
         if (started) {
             HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().begin();
         }
         try {
             roFromZipJobStatus = (ROFromZipJobStatus) status;
             URI roUri = roFromZipJobStatus.getTarget();
             try {
                ResearchObject created = ResearchObject.create(builder, roUri, new MemoryZipFile(zipFile,
                        roFromZipJobStatus.getTarget().toString()), roFromZipJobStatus);
                 roFromZipJobStatus.setProcessedResources(roFromZipJobStatus.getSubmittedResources());
                 roFromZipJobStatus.setTarget(created.getUri());
             } catch (IOException | BadRequestException e) {
                 throw new OperationFailedException("Can't preapre a ro from given zip", e);
             }
         } finally {
             builder.getEventBusModule().commit();
             if (started) {
                 HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();
             }
             zipFile.delete();
         }
     }
 }
