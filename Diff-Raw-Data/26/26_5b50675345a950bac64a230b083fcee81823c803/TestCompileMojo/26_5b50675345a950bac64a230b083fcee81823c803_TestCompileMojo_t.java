 package org.charless.qxmaven.mojo.qooxdoo;
 
 
 import java.io.File;
 import java.net.URL;
 import java.util.HashMap;
 
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.json.JSONObject;
 import org.openqa.selenium.JavascriptExecutor;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.firefox.FirefoxDriver;
 
 /**
  * Goal which builds the qooxdoo testrunner
  * 
  * @goal test-compile
  * @phase test-compile
  * @author charless
  * @requiresDependencyResolution test
  */
 public class TestCompileMojo extends TestrunnerMojo {
 	
    /**
     * Name of the directory where testrunner will be builded.
     * Strongly encouraged to use a different directory that the default "test" one,
     * as we are using a simplified view for building the testrunner.
     *
     * @parameter expression="${qooxdoo.test.unitdir}"
     * 			  default-value="testunit"
     */
    protected String testUnitdir;
	
 	
     public void execute() throws MojoExecutionException, MojoFailureException
     {
     	 if(!skipTests) {
     	      getLog().info("Executing Unit Tests");
 	    	if (! "testrunner.view.Reporter".equals(this.testView)) {
 	    		this.testView = "testrunner.view.Reporter";
 	    		getLog().warn("The testrunner view has been forced to "+this.testView);
 	    	}
 	    	this.generator(testJob);
 	    	URL index = null ;
 	    	if (this.getTestrunnerIndexHtml().exists()) {
 	    		try {
 	    			index = this.getTestrunnerIndexHtml().toURI().toURL();
 	    		} catch(Exception e) {
 	    			e.printStackTrace();
 	    			getLog().error("Unexpected error while getting URL of testrunner index file.  Tests aborted.");
 	    		}
 	    	} else  {
 	    		getLog().error("File "+this.getTestrunnerIndexHtml().getAbsolutePath()+" has not been created. Tests aborted.");
 	    	}
 	    	startSelenium(index);
 	    	
   	    } else {
   	    	getLog().info("Skipping Unit Tests");
   	    }
     }
     
    protected String[] getCommandLineOptions() {
    	return new String[] {
    			"-m","TESTRUNNER_VIEW:"+this.testView,
    			"-m","BUILD_PATH:${ROOT}/"+this.testUnitdir
    	};
    }
    
    public void startSelenium(URL index) throws MojoExecutionException, MojoFailureException {
     	getLog().info("Starting Selenium FireFox driver on "+index.toString());
         // Create a new instance of the driver
         WebDriver driver = new FirefoxDriver();
         // And now use this to load the testrunner
         driver.get(index.toString());
         // Wait for tests being executed
         JavascriptExecutor js = (JavascriptExecutor) driver;
         boolean stop = false;
         int i = 0; 
         HashMap<String,JSONObject> tests = new HashMap<String,JSONObject>();
         String status = ""; // init,loading, ready, running, finished, aborted, error
         int nbTestsPending = -1;
         while(! stop) {
         	Object response = js.executeScript("return qx.core.Init.getApplication().runner.view.getTestSuiteState();");
             if (response != null) {
             	String oldStatus = status;
             	status = response.toString().toLowerCase();
             	if (! oldStatus.equals(status)) {
             		if ("running".equals(status)) {
             			getLog().info("Running tests...");
                 	} 
             	}
             	if ("running".equals(status)) {
             		response = js.executeScript("return qx.core.Init.getApplication().runner.view.getTestCount();");
                 	if (response != null) {
                 		try {
                 			if (Integer.parseInt(response.toString()) > nbTestsPending ) {
                 				nbTestsPending = Integer.parseInt(response.toString());
                 				if (nbTestsPending > 0) {
                 					getLog().info(nbTestsPending+" tests pending.");
                 				}
                 			}
                 		} catch (Exception e) {
                 			getLog().warn(e);
                 		}	
                 	}
             	}
             	if("finished".equals(status) ||
             			"aborted".equals(status) ||
             			"error".equals(status)) 
             	{
             		stop = true;
             	}
             } else {
             	i++;
             }
             try {
             	Thread.sleep(250);
             } catch (Exception e) {}
             if (i > 10) { stop = true;}  // 10*250 = 2500ms timeout
         }
  
         // Get Report
         Object response = js.executeScript("return qx.core.Init.getApplication().runner.view.getTestResults();");
     	String report = null;
         if (response != null) {
     		try {
     			report = response.toString();
     		} catch (Exception e) {
     			getLog().warn(e);
     		}	
     	}
     	
         //Close the browser
         driver.quit();
         
         // Final reporting
         // TODO: parse report as json data, do a real reporting
         if (report==null) {
         	getLog().error("Could not get report !");
         	throw new MojoExecutionException("Could not get report !");
         } else {
         	if (report.matches(".*state=failure, messages.*")) {
         		getLog().error("Unit tests failure !");
        		throw new MojoFailureException("Unit tests failure !");
         	}
         }
        
         getLog().info("ALL TESTS SUCCESSFULL !");
         
     }
     
     public File getTestrunnerIndexHtml() {
    	File basedir = new File(this.getApplicationTarget(),this.testUnitdir);
     	String index = (this.testJob != null ? this.testJob.replaceAll("test", "index") : "index")+".html";
     	return new File(basedir, index);
     }
 
 }
