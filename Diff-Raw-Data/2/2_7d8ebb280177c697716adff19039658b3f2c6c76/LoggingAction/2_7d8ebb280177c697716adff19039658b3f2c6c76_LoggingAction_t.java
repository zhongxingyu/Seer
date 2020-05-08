 package org.jrecruiter.web.actions.admin;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.InputStream;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.List;
 
 import org.apache.log4j.Appender;
 import org.apache.log4j.Category;
 import org.apache.log4j.FileAppender;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.jrecruiter.common.CollectionUtils;
 import org.jrecruiter.web.actions.BaseAction;
 import org.jrecruiter.web.interceptor.RetrieveMessages;
 import org.texturemedia.smarturls.ActionName;
 import org.texturemedia.smarturls.ActionNames;
 import org.texturemedia.smarturls.Result;
 import org.texturemedia.smarturls.Results;
 
 /**
  * Show the main index page of the admin screens.
  *
  * @author Gunnar Hillert
  * @version $Id: IndexAction.java 154 2008-02-21 01:34:19Z ghillert $
  *
  */
 @ActionNames({
       @ActionName(name="logging", method="execute"),
       @ActionName(name="downloadLogFile", method="download")
     })
 @Results({
       @Result(name="download", location="fileToDownLoad", type="stream",
               params={"contentType","text/plain",
                       "inputName","fileToDownLoad",
                       "contentDisposition","attachment; filename=logfile.txt",
                      "bufferSize", "1024"})
     })
 public class LoggingAction extends BaseAction {
 
     private String log;
     private String level;
 
     private List<LogFileInfo> logFileInfos = CollectionUtils.getArrayList();
 
     private String fileName;
     private InputStream fileToDownLoad;
 
     /**
      *
      */
     @Override
     public String execute() {
 
         if (null != log) {
             Logger logger = ("".equals(log) ? Logger.getRootLogger() : Logger
                     .getLogger(log));
             logger.setLevel(Level.toLevel(level, Level.DEBUG));
 
 
         }
 
         Enumeration<Category> enumeration = Logger.getRootLogger().getAllAppenders();
         while (enumeration.hasMoreElements() ){
           Appender app = (Appender)enumeration.nextElement();
           if ( app instanceof FileAppender ){
 
             String fileName = ((FileAppender) app).getFile();
             File logFile = new File(fileName);
 
             LogFileInfo logFileInfo = new LogFileInfo();
 
             logFileInfo.setFileName(logFile.getName());
             logFileInfo.setFileLastChanged(new Date(logFile.lastModified()));
             logFileInfo.setFileSize(logFile.length());
             logFileInfos.add(logFileInfo);
 
           }
         }
 
         return SUCCESS;
     }
 
     public String download() throws Exception {
 
         if (this.fileName == null) {
             throw new IllegalArgumentException("FileName must not be null.");
         }
 
         Enumeration<Category> enumeration = Logger.getRootLogger().getAllAppenders();
         while (enumeration.hasMoreElements() ){
           Appender app = (Appender)enumeration.nextElement();
           if ( app instanceof FileAppender ){
 
             String fileName = ((FileAppender) app).getFile();
             File logFile = new File(fileName);
 
             if (logFile.getName().equalsIgnoreCase(this.fileName)) {
                 this.fileToDownLoad = new FileInputStream(logFile);
             }
 
           }
         }
 
         return "download";
     }
 
     public String getLog() {
         return log;
     }
 
     public void setLog(String log) {
         this.log = log;
     }
 
     public String getLevel() {
         return level;
     }
 
     public void setLevel(String level) {
         this.level = level;
     }
 
     public List<LogFileInfo> getLogFileInfos() {
         return logFileInfos;
     }
 
     public void setLogFileInfos(List<LogFileInfo> logFileInfos) {
         this.logFileInfos = logFileInfos;
     }
 
     public String getFileName() {
         return fileName;
     }
 
     public void setFileName(String fileName) {
         this.fileName = fileName;
     }
 
     public InputStream getFileToDownLoad() {
         return fileToDownLoad;
     }
 
     public void setFileToDownLoad(InputStream fileToDownLoad) {
         this.fileToDownLoad = fileToDownLoad;
     }
 
 }
