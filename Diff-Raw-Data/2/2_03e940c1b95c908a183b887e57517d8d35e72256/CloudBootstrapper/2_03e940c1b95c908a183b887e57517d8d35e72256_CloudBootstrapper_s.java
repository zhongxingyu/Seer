 package org.cloudifysource.quality.iTests.framework.utils;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Map;
 
 import iTests.framework.utils.IOUtils;
 import org.apache.commons.lang.StringUtils;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.CommandTestUtils;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.services.CloudService;
 
 public class CloudBootstrapper extends Bootstrapper {
 
 	private static final int DEFAULT_BOOTSTRAP_CLOUD_TIMEOUT = 30;
 
     private CloudService cloudService;
     private String provider;
 	private boolean noWebServices = false;
 	private String useExistingFilePath = "";
 	private String cloudFolderPath = "";
 	private boolean useExisting = false;
     private boolean skipValidation = true;
 	private Map<String, Object> cloudOverrides;
 	
 	public CloudBootstrapper() {
 		super(DEFAULT_BOOTSTRAP_CLOUD_TIMEOUT);
 	}
 
     public String getCloudFolderPath() {
         return cloudFolderPath;
     }
 
     public boolean isUseExisting() {
         return useExisting;
     }
 
     public CloudService getCloudService() {
         return cloudService;
     }
 
     public String getProvider() {
         return provider;
     }
 
     public boolean isSkipValidation() {
         return skipValidation;
     }
 
     public Map<String, Object> getCloudOverrides() {
         return cloudOverrides;
     }
 
     public String getUseExistingFilePath() {
         return useExistingFilePath;
     }
 
     public CloudBootstrapper useExistingFilePath(final String useExistingFilePath) {
         this.useExistingFilePath = useExistingFilePath;
         return this;
     }
 
     public CloudBootstrapper useExisting(final boolean useExisting) {
         this.useExisting = useExisting;
         return this;
     }
 
     public void setCloudFolderPath(final String cloudFolderPath) {
         this.cloudFolderPath = cloudFolderPath;
     }
 
 
     public CloudBootstrapper skipValidation(final boolean skipValidation) {
         this.skipValidation = skipValidation;
         return this;
     }
 	
 	public CloudBootstrapper provider(final String provider) {
 		this.provider = provider;
 		return this;
 	}
 
     public void setCloudService(final CloudService cloudService) {
         this.cloudService = cloudService;
     }
 
     public CloudBootstrapper noWebServices(final boolean noWebServices) {
 		this.noWebServices = noWebServices;
 		return this;
 	}
 	
 	public boolean isNoWebServices() {
 		return noWebServices;
 	}
 	
 	public CloudBootstrapper cloudOverrides(final Map<String, Object> overrides) {
 		this.cloudOverrides = overrides;
 		return this;
 	}
 
     public String addTemplate(final String templatePath, final boolean isExpectedToFail) throws Exception {
         lastActionOutput = CommandTestUtils.runCommand(connectCommand() + ";add-templates "
                 + templatePath, true, isExpectedToFail);
         return lastActionOutput;
     }
 
     public String getTemplate(final String templateName, final boolean isExpectedToFail) throws Exception {
         lastActionOutput = CommandTestUtils.runCommand(connectCommand() + ";get-template "
                 + templateName, true, isExpectedToFail);
         return lastActionOutput;
     }
 
     public String listTemplates(final boolean isExpectedToFail) throws Exception {
         lastActionOutput = CommandTestUtils.runCommand(connectCommand() + ";list-templates", true, isExpectedToFail);
         return lastActionOutput;
     }
 
     public String removeTemplate(final String templateName, final boolean isExpectedToFail) throws Exception {
         lastActionOutput = CommandTestUtils.runCommand(connectCommand() + ";remove-template "
                 + templateName, true, isExpectedToFail);
         return lastActionOutput;
     }
 
 	@Override
 	public String getCustomOptions() throws IOException{
 		
 		StringBuilder builder = new StringBuilder();
 		if (noWebServices) {
 			builder.append("-no-web-services ");
 		}
 		if (cloudOverrides != null && !cloudOverrides.isEmpty()) {
 			File cloudOverridesFile = IOUtils.createTempOverridesFile(cloudOverrides);
 			builder
 				.append("-cloud-overrides").append(" ")
 				.append(cloudOverridesFile.getAbsolutePath().replace("\\", "/"));
 		}
 
         if (StringUtils.isNotBlank(useExistingFilePath)) {
             builder.append("-use-existing-from-file ").append(useExistingFilePath);
         }
 
         if (useExisting) {
            builder.append("-use-existing");
         }
 
         if (skipValidation) {
             builder.append("-skip-validation");
         }
 
 		return builder.toString();
 	}
 }
