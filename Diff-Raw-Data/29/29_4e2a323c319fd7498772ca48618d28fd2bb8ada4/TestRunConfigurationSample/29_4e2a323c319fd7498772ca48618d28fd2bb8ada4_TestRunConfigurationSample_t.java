 package com.testdroid.api.sample;
 
 import com.testdroid.api.APIClient;
 import com.testdroid.api.APIException;
 import com.testdroid.api.model.APIProject;
 import com.testdroid.api.model.APITestRunConfig;
 import com.testdroid.api.model.APIUser;
 import com.testdroid.api.sample.util.Common;
 
 /**
  *
  * @author Sławomir
  */
 public class TestRunConfigurationSample {
 
     public static final APIClient CLIENT = Common.createApiClient("http://localhost:8080/testdroid-cloud", "admin@localhost", "admin");
     public static final String HOOK_URL = "some URL";
     
     public static void main(String[] args) {
         try {
             // Get user
             APIUser me = CLIENT.me();
             
             // Create android project
             APIProject project = me.createProject(APIProject.Type.ANDROID);
             
             // You would like to configure project before runing tests on it
             // Each test run have its configuration
             // First You need to get test run config
             APITestRunConfig testRunConfig = project.getTestRunConfig();
             
             // After that you can configure few things:
             // Scheduler
             // Mode - mode determinates how testdroid-cloud handles test run. for each type of project You can set different modes:
             //      AndroidProject: { APITestRunConfig.Mode.APP_CRAWLER | APITestRunConfig.Mode.FULL_RUN }
             //      IOSProject: { APITestRunConfig.Mode.IOS }
             //      CTSProject: { APITestRunConfig.Mode.CTS }
             //      UIAutomatorProject: { APITestRunConfig.Mode.UIAUTOMATOR }
             //      RemoteControllProject: { APITestRunConfig.Mode.REMOTECONTROL }
             // For projects only described values of mode are available. Attempt of setting incorrect mode results APIException
             //
             // AutoScreenshots
             // ScreenshotDir
             // LimitationType
             // ApplicationUsername
             // ApplicationPassword
             // UsedDeviceGroupId
             // HookURL
             // UIAutomatorTestClasses
             // CheckApp
             
             try {
                 testRunConfig.setMode(APITestRunConfig.Mode.UIAUTOMATOR);
                 testRunConfig.update();
             } catch(APIException e) {
                 System.out.println(e.getMessage());
             }
             
             testRunConfig.setMode(APITestRunConfig.Mode.FULL_RUN);
             testRunConfig.update();
             
             testRunConfig.setMode(APITestRunConfig.Mode.APP_CRAWLER);
             testRunConfig.update();
             
         } catch(APIException apie) {
             System.out.println(apie.getMessage());
         }
     }
     
 }
