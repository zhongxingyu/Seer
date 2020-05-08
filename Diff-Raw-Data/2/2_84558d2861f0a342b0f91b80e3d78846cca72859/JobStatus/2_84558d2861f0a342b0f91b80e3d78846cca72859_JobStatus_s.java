 package pl.psnc.dl.wf4ever.portal.model;
 
 import java.net.URI;
 import java.util.List;
 
 import javax.xml.bind.annotation.XmlRootElement;
 
 /**
  * Job status as JSON.
  * 
  * @author piotrekhol
  * 
  */
 @XmlRootElement
 public class JobStatus {
 
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
 
 
     /** workflow URI. */
     private URI resource;
 
     /** workflow format URI. */
     private URI format;
 
     /** RO URI. */
     private URI ro;
 
     /** job state. */
     private State status;
 
     /** resources already uploaded. */
     private List<URI> added;
 
 
     /**
      * Default empty constructor.
      */
     public JobStatus() {
 
     }
 
 
     /**
      * Constructor.
      * 
      * @param resource
      *            workflow URI
      * @param format
      *            workflow format URI
      * @param ro
      *            RO URI
      * @param state
      *            job state
      * @param added
      *            resources added
      */
     public JobStatus(URI resource, URI format, URI ro, State state, List<URI> added) {
         super();
         this.resource = resource;
         this.format = format;
         this.ro = ro;
         this.status = state;
         this.added = added;
     }
 
 
     public URI getResource() {
         return resource;
     }
 
 
     public void setResource(URI resource) {
         this.resource = resource;
     }
 
 
     public URI getFormat() {
         return format;
     }
 
 
     public void setFormat(URI format) {
         this.format = format;
     }
 
 
     public URI getRo() {
         return ro;
     }
 
 
     public void setRo(URI ro) {
         this.ro = ro;
     }
 
 
     public State getStatus() {
         return status;
     }
 
 
     public void setStatus(State status) {
         this.status = status;
     }
 
 
     public List<URI> getAdded() {
         return added;
     }
 
 
     public void setAdded(List<URI> added) {
         this.added = added;
     }
 
 }
