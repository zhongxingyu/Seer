 package com.testdroid.api.sample;
 
 import com.testdroid.api.APIClient;
 import com.testdroid.api.APIException;
 import com.testdroid.api.APIList;
 import com.testdroid.api.model.APIDeviceGroup;
 import com.testdroid.api.model.APIProject;
 import com.testdroid.api.model.APITestRunConfig;
 import com.testdroid.api.model.APIUser;
 import com.testdroid.api.sample.util.Common;
 import java.io.File;
 
 /**
  *
  * @author Sławomir
  */
 public class RunProjectSample {
     
     public static final APIClient CLIENT = Common.createApiClient();
     public static final String HOOK_URL = "some URL";
     
     public static void main(String[] args) {
         try {
             // Get authenticated user
             APIUser me = CLIENT.me();
             
             // Create project
             APIProject project = me.createProject(APIProject.Type.ANDROID);
             
             // Get or create device group
             APIList<APIDeviceGroup> deviceGroupsList = project.getPublicDeviceGroups().getEntity();
             APIDeviceGroup deviceGroup = deviceGroupsList.get(0);
             
             // Get test run config from project
             APITestRunConfig testRunConfig = project.getTestRunConfig();
             
             // Configure test run config
             testRunConfig.setApplicationPassword("applicationPassword");
             testRunConfig.setApplicationUsername("applicationUsername");
             testRunConfig.setAutoScreenshots(false);
             testRunConfig.setCheckApp(true);
             testRunConfig.setDeviceLanguageCode("EN");
             testRunConfig.setUsedDeviceGroupId(deviceGroup.getId());
             testRunConfig.setMode(APITestRunConfig.Mode.FULL_RUN);
             
             // Set hook URL to recieve signal, when test run is finished.
             // Server will send POST request to specified URL:
             // POST body:
             // projectId=[0-9]* - id of finished project
             // testRunId=[0-9]* - id of finished test run
             // status=FINISHED
             testRunConfig.setHookURL(HOOK_URL);
             
             // Update changes
             testRunConfig.update();
             
             // Upload application
             project.uploadApplication(new File(RunProjectSample.class.getResource("/fixtures/BitbarSampleApp.apk").getPath()), Common.ANDROID_FILE_MIME_TYPE);
             
             // Upload test
             project.uploadTest(new File(RunProjectSample.class.getResource("/fixtures/BitbarSampleAppTest.apk").getPath()), Common.ANDROID_FILE_MIME_TYPE);
             
            // Upload data
             project.uploadData(new File(RunProjectSample.class.getResource("/fixtures/testData.zip").getPath()), Common.ZIP_FILE_MIME_TYPE);
             
             // Run test run
             project.run("My test run name");
         } catch(APIException apie) {
             System.err.println(apie.getMessage());
         }
     }
             
 }
