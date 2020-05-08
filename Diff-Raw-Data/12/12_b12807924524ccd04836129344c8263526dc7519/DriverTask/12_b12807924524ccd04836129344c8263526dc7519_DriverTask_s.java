 package com.karimson.jasmine.driver;
 
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.Task;
 
 import java.io.IOException;
 
 public class DriverTask extends Task {
 
     private String pathToSpecRunner = "";
     private final Driver driver;
 
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
         if(results.containsFailedSpecs()) {
             throw new BuildException(results.toString());
         }
        System.out.println(results.toString());
     }
 
     public String getPathToSpecRunner() {
         return pathToSpecRunner;
     }
 
     public void setPathToSpecRunner(String pathToSpecRunner) {
         this.pathToSpecRunner = pathToSpecRunner;
     }
 }
