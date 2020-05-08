 package edu.umn.msi.tropix.jobs.newfile;
 
 import javax.annotation.ManagedBean;
 import javax.inject.Inject;
 
 import com.google.common.collect.Sets;
 
 import edu.umn.msi.tropix.files.NewFileMessageQueue.NewFileMessage;
 import edu.umn.msi.tropix.jobs.activities.descriptions.ActivityDescription;
 import edu.umn.msi.tropix.jobs.activities.descriptions.CreateProteomicsRunDescription;
 import edu.umn.msi.tropix.jobs.activities.descriptions.JobDescription;
 import edu.umn.msi.tropix.jobs.client.ActivityClient;
 import edu.umn.msi.tropix.models.TropixFile;
 import edu.umn.msi.tropix.models.utils.StockFileExtensionEnum;
 
 @ManagedBean
 @ForExtension(edu.umn.msi.tropix.models.utils.StockFileExtensionEnum.MZXML)
public class MzxmlNewFileProcessorImpl implements NewFileProcessor {
   private final ActivityClient activityClient;
 
   @Inject
   public MzxmlNewFileProcessorImpl(final ActivityClient activityClient) {
     this.activityClient = activityClient;
   }
 
   public void processFile(final NewFileMessage message, final TropixFile tropixFile) {
     final CreateProteomicsRunDescription description = new CreateProteomicsRunDescription();
     description.setDestinationId(message.getParentId());
     description.setMzxmlFileId(tropixFile.getId());
     final String name = StockFileExtensionEnum.MZXML.stripExtension(tropixFile.getName());
     description.setName(name);
     description.setCommitted(true);
     final JobDescription jobDescription = new JobDescription();
     jobDescription.setName(String.format("Create peak list %s", name));
     description.setJobDescription(jobDescription);
     activityClient.submit(Sets.<ActivityDescription>newHashSet(description), message.getCredential());
   }
 
 }
