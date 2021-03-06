 package org.rstudio.studio.client.projects.model;
 
 import com.google.gwt.core.client.JavaScriptObject;
 
 public class OpenProjectResult extends JavaScriptObject
 {
    public final static int STATUS_OK = 0;
    public final static int STATUS_NOT_EXISTS = 1;
    public final static int STATUS_NO_WRITE_ACCESS = 3;
    
    protected OpenProjectResult()
    {
    }
    
    public native final int getStatus() /*-{
       return this.status;
    }-*/;
    
    public native final String getProjectFilePath() /*-{
       return this.project_file_path;
    }-*/;
 
 }
