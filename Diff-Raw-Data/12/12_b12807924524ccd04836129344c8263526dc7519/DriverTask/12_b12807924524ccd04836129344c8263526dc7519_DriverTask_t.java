 package com.karimson.jasmine.driver;
 
import com.karimson.jasmine.driver.htmlunit.HtmlUnitDriver;
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.Task;
 
 import java.io.IOException;
 
 public class DriverTask extends Task {
 
     private String pathToSpecRunner = "";
     private final Driver driver;
 
    public DriverTask() {
        this("", new HtmlUnitDriver());
    }

     public DriverTask(String pathToSpecRunner, Driver driver) {
         this.pathToSpecRunner = pathToSpecRunner;
         this.driver = driver;
     }
 
     public void execute() throws BuildException {
         SpecResults results;
         try {
             results = driver.executeSpecs(getPathToSpecRunner());
         }
         catch(IOException e) {
             throw new BuildException(e);
         }

        System.out.println(results.toString());

         if(results.containsFailedSpecs()) {
             throw new BuildException(results.toString());
         }
     }
 
     public String getPathToSpecRunner() {
         return pathToSpecRunner;
     }
 
     public void setPathToSpecRunner(String pathToSpecRunner) {
         this.pathToSpecRunner = pathToSpecRunner;
     }
 }
