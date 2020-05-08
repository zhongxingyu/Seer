 /*
  * Copyright 2007 Josh Kropf
  * 
  * This file is part of bb-ant-tools.
  * 
  * bb-ant-tools is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  * 
  * bb-ant-tools is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with bb-ant-tools; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
  */
 package ca.slashdev.bb;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.taskdefs.Java;
 import org.apache.tools.ant.types.Path;
 import org.apache.tools.ant.util.FileUtils;
 
 /**
  * Sigtool task will launch the signature tool on a given cod file or set of
  * cod files.  If a single cod file is specified, then a file will be created
  * along side the cod file to mark it as signed.
  * @author josh
  */
 public class SigtoolTask extends BaseTask
 {
    private File sigtoolJar;
    
    private boolean forceClose = false;
    private boolean close = true;
    private boolean request = true;
    
    private File codFile;
    private Path cods;
    
    @Override
    public void init() throws BuildException {
       cods = new Path(getProject());
       super.init();
    }
    
    /**
     * Sets the jde home directory and attempts to locate the
     * SignatureTool.jar file.
     */
    @Override
    public void setJdeHome(File jdeHome) {
       super.setJdeHome(jdeHome);
       
       File bin = new File(jdeHome, "bin");
       if (bin.isDirectory()) {
          sigtoolJar = new File(bin, "SignatureTool.jar");
       } else {
          throw new BuildException("jde home missing \"bin\" directory");
       }
    }
    
    /**
     * Closes signature tool regardless of its success, defaults to false.
     * @param forceClose
     */
    public void setForceClose(boolean forceClose) {
       this.forceClose = forceClose;
    }
    
    /**
     * Closes signature tool after request if no errors occur, defaults to true.
     * @param close
     */
    public void setClose(boolean close) {
       this.close = close;
    }
    
    /**
     * Requests signatures when the tool launches, defaults to true.
     * @param request
     */
    public void setRequest(boolean request) {
       this.request = request;
    }
    
    /**
     * Sets the cod file to sign
     * @param codFile
     */
    public void setCodFile(File codFile) {
       this.codFile = codFile;
    }
    
    /**
     * Adds codPath to path of cod files to sign
     * @param codPath
     */
    public void addCod(Path codPath) {
       cods.add(codPath);
    }
    
    @Override
    public void execute() throws BuildException {
       if (jdeHome == null) {
          throw new BuildException("jdehome not set");
       }
       
       if (cods.size() == 0 && codFile == null) {
          throw new BuildException("cods attribute or <cod> element required!");
       }
       
       if (codFile != null && cods.size() > 0) {
          throw new BuildException("codfile attribute cant be used in conjunction with <cod> element");
       }
       
       if (codFile != null) {
          String codFilePath = codFile.getAbsolutePath();
          File touchFile = new File(codFilePath.replace(".cod", ".signed"));
         if (!FileUtils.getFileUtils().isUpToDate(codFile, touchFile)) {
             cods.add(new Path(getProject(), codFilePath));
             executeSigtool();
            touch(touchFile);
          } else {
             log("cod file already signed");
          }
       } else {
          executeSigtool();
       }
    }
    
    private void executeSigtool() {
       Java java = (Java)getProject().createTask("java");
       java.setTaskName(getTaskName());
       
       java.setFailonerror(true);
       java.setFork(true);
       java.setJar(sigtoolJar);
       
       if (forceClose) java.createArg().setValue("-C");
       if (close) java.createArg().setValue("-c");
       if (request) java.createArg().setValue("-a");
       
       for (String file : cods.list()) {
          java.createArg().setFile(new File(file));
       }
       
       java.execute();
    }
    
    /*
     * Creates a file (if it doesn't already exist) and explicitly sets
     * the last modified date to the current time.
     */
    private void touch(File file) {
       try {
          // create file if it doesn't already exist
          if (!file.exists()) file.createNewFile();
          
          // update last modified time
          file.setLastModified(System.currentTimeMillis());
       } catch (IOException e) {
          throw new BuildException("error touching file", e);
       }
    }
 }
