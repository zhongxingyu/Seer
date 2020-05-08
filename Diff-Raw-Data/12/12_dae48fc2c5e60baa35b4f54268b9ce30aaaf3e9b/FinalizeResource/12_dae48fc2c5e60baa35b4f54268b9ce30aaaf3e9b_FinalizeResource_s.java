 package pl.psnc.dl.wf4ever.evo;
 
 import java.util.Collections;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.UUID;
 import java.util.concurrent.ConcurrentHashMap;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.Consumes;
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
 
 import com.sun.jersey.api.NotFoundException;
 
 /**
  * REST API resource for finalizing the RO state transformation.
  * 
  * @author piotrhol
  * 
  */
 @Path("evo/finalize/")
 public class FinalizeResource implements JobsContainer {
 
     /** logger. */
     @SuppressWarnings("unused")
     private static final Logger LOGGER = Logger.getLogger(FinalizeResource.class);
 
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
 
 
     /**
      * Creates a finalize of a research object.
      * 
      * @param newStatus
      *            operation parameters
      * @return 201 Created
      * @throws BadRequestException
      *             if the operation parameters are incorrect
      */
     @POST
     @Consumes(MediaType.APPLICATION_JSON)
     public Response createFinalizeJob(JobStatus newStatus)
             throws BadRequestException {
         if (newStatus.getTarget() == null) {
             throw new BadRequestException("incorrect or missing \"target\" attribute");
         }
         JobStatus status = CopyResource.getStatusForTarget(newStatus.getTarget());
         if (status == null) {
             throw new BadRequestException("Can't find a copy job for this target: " + newStatus.getTarget());
         }
         FinalizeOperation finalize = new FinalizeOperation();
 
         UUID jobUUID = UUID.randomUUID();
         Job job = new Job(jobUUID, status, this, finalize);
         jobs.put(jobUUID, job);
         job.start();

         return Response.created(uriInfo.getAbsolutePath().resolve(jobUUID.toString())).build();
     }
 
 
     @Override
     public void onJobDone(Job job) {
         finishedJobs.put(job.getUUID(), job.getStatus());
         jobs.remove(job.getUUID());
     }
 
 
     /**
      * Get job status.
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
 
 }
