 package pl.psnc.dl.wf4ever.evo;
 
import java.net.URI;
 import java.util.Collections;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.UUID;
 import java.util.concurrent.ConcurrentHashMap;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriInfo;
 
 import org.apache.log4j.Logger;
 
 import pl.psnc.dl.wf4ever.BadRequestException;
 import pl.psnc.dl.wf4ever.Constants;
 
 import com.sun.jersey.api.NotFoundException;
 
 /**
  * The RO copy REST API resource.
  * 
  * @author piotrhol
  * 
  */
 @Path("evo/copy/")
 public class CopyResource implements JobsContainer {
 
     /** logger. */
     @SuppressWarnings("unused")
     private static final Logger LOGGER = Logger.getLogger(CopyResource.class);
 
     /** Maximum number of concurrent jobs. */
     public static final int MAX_JOBS = 100;
 
     /** Maximum number of finished jobs kept in memory. */
     public static final int MAX_JOBS_DONE = 100000;
 
     /** Context. */
     @Context
     private HttpServletRequest request;
 
     /** URI info. */
     @Context
     private UriInfo uriInfo;
 
     /** Running jobs. */
     private static Map<UUID, Job> jobs = new ConcurrentHashMap<>(MAX_JOBS);
 
     /** Statuses of finished jobs. */
     @SuppressWarnings("serial")
     private static Map<UUID, JobStatus> finishedJobs = Collections
             .synchronizedMap(new LinkedHashMap<UUID, JobStatus>() {
 
                 protected boolean removeEldestEntry(Map.Entry<UUID, JobStatus> eldest) {
                     return size() > MAX_JOBS_DONE;
                 };
             });
 
     /** Statuses of finished jobs by target. */
     @SuppressWarnings("serial")
     private static Map<String, JobStatus> finishedJobsByTarget = Collections
             .synchronizedMap(new LinkedHashMap<String, JobStatus>() {
 
                 protected boolean removeEldestEntry(Map.Entry<String, JobStatus> eldest) {
                     return size() > MAX_JOBS_DONE;
                 };
             });
 
 
     /**
      * Creates a copy of a research object.
      * 
      * @param status
      *            operation parameters
      * @return 201 Created
      * @throws BadRequestException
      *             if the operation parameters are incorrect
      */
     @POST
     @Consumes(MediaType.APPLICATION_JSON)
     public Response createCopyJob(JobStatus status)
             throws BadRequestException {
         if (status.getCopyfrom() == null) {
             throw new BadRequestException("incorrect or missing \"copyfrom\" attribute");
         }
        if (!status.getCopyfrom().isAbsolute()) {
            URI rosrs = uriInfo.getAbsolutePath().resolve("../../ROs/");
            status.setCopyfrom(rosrs.resolve(status.getCopyfrom()));
            LOGGER.warn("resolving relative copy from uri");
            LOGGER.warn(status.getCopyfrom());
        }
         if (status.getType() == null) {
             throw new BadRequestException("incorrect or missing \"type\" attribute");
         }
         String id = null;
         if (status.isFinalize() && status.getTarget() != null) {
             id = status.getTarget().toString();
         } else {
             id = request.getHeader(Constants.SLUG_HEADER);
         }
         if (id == null) {
             id = UUID.randomUUID().toString();
         }
 
         CopyOperation copy = new CopyOperation(id);
 
         UUID jobUUID = UUID.randomUUID();
         Job job;
         if (!status.isFinalize()) {
             job = new Job(jobUUID, status, this, copy);
         } else {
             FinalizeOperation finalize = new FinalizeOperation();
             job = new Job(jobUUID, status, this, copy, finalize);
         }
         jobs.put(jobUUID, job);
         job.start();
 
         return Response.created(uriInfo.getAbsolutePath().resolve(jobUUID.toString())).build();
     }
 
 
     @Override
     public void onJobDone(Job job) {
         finishedJobs.put(job.getUUID(), job.getStatus());
         finishedJobsByTarget.put(job.getStatus().getTarget(), job.getStatus());
         jobs.remove(job.getUUID());
     }
 
 
     /**
      * Retrieve the job status.
      * 
      * @param uuid
      *            job id
      * @return job status
      */
     @GET
     @Path("{id}")
     public JobStatus getJob(@PathParam("id") UUID uuid) {
         if (jobs.containsKey(uuid)) {
             return jobs.get(uuid).getStatus();
         }
         if (finishedJobs.containsKey(uuid)) {
             return finishedJobs.get(uuid);
         }
         throw new NotFoundException("Job not found: " + uuid);
     }
 
 
     /**
      * Abort the job.
      * 
      * @param uuid
      *            job id
      */
     @DELETE
     @Path("{id}")
     public void abortJob(@PathParam("id") UUID uuid) {
         if (jobs.containsKey(uuid)) {
             jobs.get(uuid).abort();
             jobs.remove(uuid);
         }
         if (finishedJobs.containsKey(uuid)) {
             String target = finishedJobs.get(uuid).getTarget();
             finishedJobs.remove(uuid);
             finishedJobsByTarget.remove(target);
         }
         throw new NotFoundException("Job not found: " + uuid);
     }
 
 
     /**
      * Find the job status.
      * 
      * @param target
      *            target RO URI
      * @return the job status
      */
     public static JobStatus getStatusForTarget(String target) {
         return finishedJobsByTarget.get(target);
     }
 }
