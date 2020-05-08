 package runThrghTestNG;
 
 import java.io.File;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import org.apache.commons.io.FileUtils;
 import org.openqa.selenium.OutputType;
 import org.openqa.selenium.TakesScreenshot;
 import org.testng.ITestContext;
 import org.testng.ITestResult;
 import org.testng.Reporter;
 import org.testng.TestListenerAdapter;
 
 public class TestNGCustomReport extends TestListenerAdapter {
 
     @Override
     public void onTestFailure(ITestResult tr) {
         ScreenShot(tr);
     }
 
     @Override
     public void onStart(ITestContext testContext) {
         System.out.print("Class: " + testContext.getName() + "\n");
     }
 
     @Override
     public void onTestStart(ITestResult result) {
         //Do Nothing
     }
 
     //Capture screenshot on TestFailure
     public void ScreenShot(ITestResult result) {
 
         try {
 
             String NewFileNamePath;
             String methodName = result.getName();
 
             //Get current date time with Date() to create unique file name
             SimpleDateFormat dateFormat = new SimpleDateFormat(
                     "ddMMMyy__hhmmaa");
             // get current date time with Date()
             Date date = new Date();
             String rprtPrgm = BaseClass.program;
             String rprtEnv = BaseClass.env;
 
             if (!(new File(BaseClass.directory.getCanonicalPath() + File.separator + "reports" + File.separator + rprtPrgm + "_" + rprtEnv + "_" + BaseClass.brwsr + File.separator + "screenshots")).exists()) {
                 new File(BaseClass.directory.getCanonicalPath() + File.separator + "reports" + File.separator + rprtPrgm + "_" + rprtEnv + "_" + BaseClass.brwsr + File.separator + "screenshots").mkdir();
             }
 
             NewFileNamePath = BaseClass.directory.getCanonicalPath() + File.separator + "reports" + File.separator + rprtPrgm + "_" + rprtEnv + "_" + BaseClass.brwsr + File.separator + "screenshots"
                     + File.separator + methodName + "_" + dateFormat.format(date) + ".png";
 
             System.out.println(NewFileNamePath);
 
             File screenshot = ((TakesScreenshot) BaseClass.driver).
                     getScreenshotAs(OutputType.FILE);
             FileUtils.copyFile(screenshot, new File(NewFileNamePath));
             Reporter.log(methodName + " failed; Click on image to enlarge<br/>"
                     + "<a target=\"_blank\" href=\"" + NewFileNamePath + "\"><img src=\"file:///" + NewFileNamePath
                    + "\" alt=\"\"" + "height='100' width='100'/><br />");
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 }
