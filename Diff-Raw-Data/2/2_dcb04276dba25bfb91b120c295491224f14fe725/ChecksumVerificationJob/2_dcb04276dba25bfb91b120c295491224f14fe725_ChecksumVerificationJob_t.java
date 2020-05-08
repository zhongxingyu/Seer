 package pl.psnc.dl.wf4ever.monitoring;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URI;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.apache.commons.codec.digest.DigestUtils;
 import org.apache.log4j.Logger;
 import org.quartz.Job;
 import org.quartz.JobExecutionContext;
 import org.quartz.JobExecutionException;
 
 import pl.psnc.dl.wf4ever.dl.UserMetadata;
 import pl.psnc.dl.wf4ever.dl.UserMetadata.Role;
 import pl.psnc.dl.wf4ever.model.Builder;
 import pl.psnc.dl.wf4ever.model.ORE.AggregatedResource;
 import pl.psnc.dl.wf4ever.model.RO.ResearchObject;
 
 /**
  * This job calculates checksums for all resources of a research object and compares them with the checksums stored in
  * the database. The result is stored in the context.
  * 
  * @author piotrekhol
  * 
  */
 public class ChecksumVerificationJob implements Job {
 
     /** Logger. */
     private static final Logger LOGGER = Logger.getLogger(ChecksumVerificationJob.class);
 
     /** Key for the input data. The value must be a URI. */
     public static final String RESEARCH_OBJECT_URI = "ResearchObjectUri";
 
     /** Resource model builder. */
     private Builder builder;
 
 
     @Override
     public void execute(JobExecutionContext context)
             throws JobExecutionException {
         URI researchObjectUri = (URI) context.getMergedJobDataMap().get(RESEARCH_OBJECT_URI);
         if (builder == null) {
             //FIXME RODL URI should be better
             UserMetadata userMetadata = new UserMetadata("rodl", "RODL decay monitor", Role.ADMIN, URI.create("rodl"));
             builder = new Builder(userMetadata);
         }
         ResearchObject researchObject = ResearchObject.get(builder, researchObjectUri);
         if (researchObject != null) {
             Result result = new Result();
             for (AggregatedResource resource : researchObject.getAggregatedResources().values()) {
                if (resource.isInternal() && resource.getStats() != null) {
                     String checksumStored = resource.getStats().getChecksum();
                     String checksumCalculated;
                     try (InputStream in = resource.getSerialization()) {
                         checksumCalculated = DigestUtils.md5Hex(in);
                     } catch (IOException e) {
                         LOGGER.error("Can't calculate checksum for " + resource, e);
                         continue;
                     }
                     if (!checksumCalculated.equalsIgnoreCase(checksumStored)) {
                         result.getMismatches().add(new Mismatch(resource.getUri(), checksumStored, checksumCalculated));
                     }
                 }
             }
             context.setResult(result);
         }
     }
 
 
     public Builder getBuilder() {
         return builder;
     }
 
 
     public void setBuilder(Builder builder) {
         this.builder = builder;
     }
 
 
     /**
      * A checksum verification job result.
      * 
      * @author piotrekhol
      * 
      */
     class Result {
 
         /** A set of differences in checksums expected and calculated. */
         private final Set<Mismatch> mismatches = new HashSet<>();
 
 
         /**
          * True if there are no mismatches in the resources aggregated by the RO.
          * 
          * @return true if there are no mismatches, false otherwise
          */
         public boolean matches() {
             return mismatches.isEmpty();
         }
 
 
         public Collection<Mismatch> getMismatches() {
             return mismatches;
         }
 
     }
 
 
     /**
      * A difference in checksum for a resource.
      * 
      * @author piotrekhol
      * 
      */
     class Mismatch {
 
         /** Resource URI. */
         private final URI resourceUri;
 
         /** Checksum that was expected. */
         private final String expectedChecksum;
 
         /** Checksum that was calculated. */
         private final String calculatedChecksum;
 
 
         /**
          * Constructor.
          * 
          * @param resourceUri
          *            resource URI
          * @param expectedChecksum
          *            checksum that was expected
          * @param calculatedChecksum
          *            checksum that was calculated
          */
         public Mismatch(URI resourceUri, String expectedChecksum, String calculatedChecksum) {
             this.resourceUri = resourceUri;
             this.expectedChecksum = expectedChecksum;
             this.calculatedChecksum = calculatedChecksum;
         }
 
 
         public URI getResourceUri() {
             return resourceUri;
         }
 
 
         public String getExpectedChecksum() {
             return expectedChecksum;
         }
 
 
         public String getCalculatedChecksum() {
             return calculatedChecksum;
         }
     }
 }
