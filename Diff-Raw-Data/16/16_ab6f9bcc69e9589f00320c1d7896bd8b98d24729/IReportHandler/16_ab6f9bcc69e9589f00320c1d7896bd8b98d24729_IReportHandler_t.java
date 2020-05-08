 package com.github.egonw.moldbvalid;
 
 import org.openscience.cdk.validate.*;
 
 public interface IReportHandler {
 
   public void setFile(String filename);
 
   public void setSubject(String subject);
 
   public void handleError(String type, ValidationTest error);
 
  public void handleError(String type, String error);

 }
 
