 package com.testdroid.api.sample;
 
 import com.testdroid.api.APIClient;
 import com.testdroid.api.APIException;
 import com.testdroid.api.APIListResource;
 import com.testdroid.api.APIQueryBuilder;
 import com.testdroid.api.model.APIProject;
 import com.testdroid.api.model.APITag;
 import com.testdroid.api.model.APITestRun;
 import com.testdroid.api.model.APITestRunConfig;
 import com.testdroid.api.model.APIUser;
 import com.testdroid.api.sample.util.Common;
 import java.io.File;
 
 /**
  *
  * @author Sławomir
  */
 public class TestRunTagsSample {
 
    private static final APIClient CLIENT = Common.createApiClient("http://devel", "admin@localhost", "admin");
     
     public static void main(String[] args) {
         try {
             // Get authorized user
             APIUser me = CLIENT.me();
             
             // Create project
             APIProject project = me.createProject(APIProject.Type.ANDROID);
             
             // Get test run config
             APITestRunConfig config = project.getTestRunConfig();
             
             // Configure testrun
             config.setMode(APITestRunConfig.Mode.APP_CRAWLER);
             
             config.update();
             
             project.uploadApplication(new File(TestRunTagsSample.class.getResource(Common.ANDROID_APPLICATION_RESOURCE_PATH).getPath()), Common.ANDROID_FILE_MIME_TYPE);
             
             APITestRun testRun = project.run();
             
             String tagName = "Test run tag";
             APITag tag = testRun.addTag(tagName);
             
             System.out.println(String.format("Created tag with name: %s", tag.getName()));
             
            testRun.refresh();
            
            // List test run tags
            System.out.println("Test run have now tags:");
            for(APITag t: testRun.getTagsResource().getEntity().getData()) {
                System.out.println(String.format("\t%s", t.getName()));
            }
            
            // Project can be found using tag name
             APIListResource<APITestRun> projects = project.getTestRunsResource(new APIQueryBuilder().search(tagName));
             
             System.out.println(String.format("Projects found using tag name: %s", tagName));
             for(APITestRun tr: projects.getEntity().getData()) {
                 System.out.println(String.format("\t%s", tr.getDisplayName()));
             }
            
         } catch(APIException apie) {
             System.err.println(apie.getMessage());
         }
     }
     
 }
