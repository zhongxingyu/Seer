 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.cids.mavenplugin.remote;
 
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.WebResource;
 
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 
 import javax.ws.rs.core.MediaType;
 
 import de.cismet.cids.mavenplugin.AbstractCidsMojo;
 
 /**
  * Goal which resets the cids reference system using the Remote Test Helper Service.
  *
  * @version  1.0, 20100512
  * @goal     reset-refsystem
  * @phase    process-test-resources
  */
 public class ResetReferenceSystemMojo extends AbstractCidsMojo {
 
     //~ Static fields/initializers ---------------------------------------------
 
     public static final String REF_SYSTEM_RESET_URL = "http://kif:9986/RemoteTestHelper/resetReferenceSystem"; // NOI18N
 
     //~ Instance fields --------------------------------------------------------
 
     /**
      * Whether to skip the execution of this mojo.
      *
      * @parameter  expression="${cids.generate-lib.skip}" default-value="false"
      * @required   false
      */
     private transient Boolean skip;
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * Connects to the Remote Test Helper Service and resets the cids reference system.
      *
      * @throws  MojoExecutionException  DOCUMENT ME!
      * @throws  MojoFailureException    DOCUMENT ME!
      */
     @Override
     public void execute() throws MojoExecutionException, MojoFailureException {
         if (skip) {
             if (getLog().isInfoEnabled()) {
                 getLog().info("reset reference system skipped"); // NOI18N
             }
             return;
         }
 
         try {
             resetReferenceSystem();
         } catch (final MojoExecutionException ex) {
             final String message = "failed to reset reference system"; // NOI18N
             if (failOnError) {
                 if (getLog().isErrorEnabled()) {
                     getLog().error(message, ex);
                 }
                 throw new MojoFailureException(message, ex);
             } else {
                 if (getLog().isWarnEnabled()) {
                     getLog().warn(message, ex);
                 }
             }
         }
     }
 
     /**
      * Connects to the Remote Test Helper Service and tries to reset the reference system.
      *
      * @throws  MojoExecutionException  if any error occurs or the response status is not ok or equal
      */
     private void resetReferenceSystem() throws MojoExecutionException {
         try {
             final Client c = Client.create();
             final WebResource webResource = c.resource(REF_SYSTEM_RESET_URL);
 
             // we accept anything since we don't expect a response
             final WebResource.Builder builder = webResource.accept(MediaType.MEDIA_TYPE_WILDCARD);
 
             final ClientResponse response = builder.put(ClientResponse.class);
 
             if (getLog().isInfoEnabled()) {
                 getLog().info("Remote Test Helper Service response status: " + response.getStatus()); // NOI18N
             }
 
             final int status = response.getStatus() - 200;
 
             if ((status < 0) || (status > 10)) {
                 throw new MojoExecutionException("status code did not indicate success: " + response.getStatus()); // NOI18N
             }
         } catch (final Exception ex) {
             throw new MojoExecutionException("could not reset reference system", ex);                              // NOI18N
         }
     }
 }
