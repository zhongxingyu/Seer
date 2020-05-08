 package org.purl.wf4ever.wf2ro.rest;
 
 import java.io.IOException;
 import java.net.URI;
 import java.util.UUID;
 
 import org.apache.log4j.Logger;
 import org.purl.wf4ever.wf2ro.RodlConverter;
 import org.scribe.model.Token;
 
 import uk.org.taverna.scufl2.api.container.WorkflowBundle;
 import uk.org.taverna.scufl2.api.io.ReaderException;
 import uk.org.taverna.scufl2.api.io.WorkflowBundleIO;
 
 /**
  * Represents a conversion job. It runs in a separate thread.
  * 
  * @author piotrekhol
  */
 public class Job extends Thread {
 
     /** Logger. */
     private static final Logger LOG = Logger.getLogger(Job.class);
 
 
     /**
      * The job state.
      * 
      * @author piotrekhol
      * 
      */
     public enum State {
         /** The job has started and is running. */
         RUNNING,
         /** The job has finished succesfully. */
         DONE,
         /** The job has been cancelled by the user. */
         CANCELLED,
         /** The resource to be formated is invalid. */
         INVALID_RESOURCE,
         /** There has been an unexpected error during conversion. */
         RUNTIME_ERROR;
 
         @Override
         public String toString() {
            return this.toString().toLowerCase();
         };
     }
 
 
     /** Job UUID. */
     private UUID uuid;
 
     /** Job state. */
     private State state;
 
     /** Workflow URI. */
     private URI resource;
 
     /** Workflow format URI. */
     private URI format;
 
     /** RO URI. */
     private URI ro;
 
     /** RODL access token. */
     private Token token;
 
     /** Object holding reference to the job. */
     private JobsContainer container;
 
     /** The converter. */
     private RodlConverter converter;
 
     /** The service URI. */
     private URI service;
 
 
     /**
      * Constructor.
      * 
      * @param service
      *            Service URI for the converter
      * @param jobUUID
      *            job identifier assigned by its container
      * @param resource
      *            URI of the workflow to be converted
      * @param format
      *            URI of workflow format
      * @param ro
      *            RO URI, for the converter
      * @param token
      *            RODL access token
      * @param container
      *            the object that created this job
      */
     public Job(URI service, UUID jobUUID, URI resource, URI format, URI ro, String token, JobsContainer container) {
         this.service = service;
         this.uuid = jobUUID;
         this.resource = resource;
         this.format = format;
         this.ro = ro;
         this.token = new Token(token, null);
         this.container = container;
         state = State.RUNNING;
 
         setDaemon(true);
     }
 
 
     @Override
     public void run() {
         WorkflowBundleIO io = new WorkflowBundleIO();
         try {
             WorkflowBundle wfbundle = io.readBundle(resource.toURL(), format.toString());
             converter = new RodlConverter(service, wfbundle, ro, this.token);
             converter.convert();
             state = State.DONE;
         } catch (ReaderException | IOException e) {
             LOG.error("Can't download the resource", e);
             state = State.INVALID_RESOURCE;
         } catch (Exception e) {
             LOG.error("Unexpected exception during conversion", e);
             state = State.RUNTIME_ERROR;
         }
 
         container.onJobDone(this);
     }
 
 
     public UUID getUUID() {
         return uuid;
     }
 
 
     public State getJobState() {
         return state;
     }
 
 
     public JobStatus getJobStatus() {
         return new JobStatus(resource, format, ro, state, converter != null ? converter.getResourcesAdded() : null);
     }
 
 
     /**
      * Cancel the job. The job is aborted but not undone.
      */
     public void cancel() {
         //FIXME not sure if that's how we want to cancel this thread
         this.interrupt();
         this.state = State.CANCELLED;
     }
 
 }
