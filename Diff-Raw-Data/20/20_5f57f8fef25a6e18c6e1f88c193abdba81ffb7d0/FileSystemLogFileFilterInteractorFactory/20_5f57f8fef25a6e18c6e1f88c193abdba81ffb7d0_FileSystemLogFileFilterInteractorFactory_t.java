 package com.crudetech.sample.logcrunch;
 
 import java.io.File;
 import java.nio.charset.Charset;
 
 
 public abstract class FileSystemLogFileFilterInteractorFactory implements LogFileFilterInteractorFactory {
     @Override
     public LogFileFilterInteractor createInteractor() {
 
         FileSystemLogFileLocator.LogFileFactory logfileFactory = new FileSystemLogFileLocator.LogFileFactory() {
             @Override
             public LogFile create(File logFile) {
                return new FileLogFile(logFile, logLineFactory(), getEncoding());
             }
         };
         LogFileLocator locator = new FileSystemLogFileLocator(getSearchPath(), logfileFactory);
         return new LogFileFilterInteractor(locator);
     }
 
     protected abstract File getSearchPath();
 
     protected abstract Charset getEncoding();
 
     protected abstract BufferedReaderLogFile.LogLineFactory logLineFactory();
 }
