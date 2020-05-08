 /**
  * Rig Client Commons.
  *
  * @license See LICENSE in the top level directory for complete license terms.
  *
  * Copyright (c) 2010, University of Technology, Sydney
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without 
  * modification, are permitted provided that the following conditions are met:
  *
  *  * Redistributions of source code must retain the above copyright notice, 
  *    this list of conditions and the following disclaimer.
  *  * Redistributions in binary form must reproduce the above copyright 
  *    notice, this list of conditions and the following disclaimer in the 
  *    documentation and/or other materials provided with the distribution.
  *  * Neither the name of the University of Technology, Sydney nor the names 
  *    of its contributors may be used to endorse or promote products derived from 
  *    this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE 
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  * @author Michael Diponio (mdiponio)
  * @date 5th May 2010
  */
 
 package au.edu.labshare.primitive;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.activation.MimetypesFileTypeMap;
 
 import au.edu.labshare.primitive.internal.Base64;
 import au.edu.uts.eng.remotelabs.rigclient.rig.IRigControl.PrimitiveRequest;
 import au.edu.uts.eng.remotelabs.rigclient.rig.IRigControl.PrimitiveResponse;
 import au.edu.uts.eng.remotelabs.rigclient.rig.primitive.IPrimitiveController;
 import au.edu.uts.eng.remotelabs.rigclient.util.ConfigFactory;
 import au.edu.uts.eng.remotelabs.rigclient.util.ILogger;
 import au.edu.uts.eng.remotelabs.rigclient.util.LoggerFactory;
 
 /**
  * Controller that transfers files from the rig client machine.
  */
 public class FileTransferController implements IPrimitiveController
 {
     /** Location where to obtain files from. */
     private String fileDir;
     
     /** List of files detected. */
     private List<String> detectedFiles;
     
     /** The map of file extensions to file type. */
     private MimetypesFileTypeMap mimeTypes;
     
     /** Logger. */
     private ILogger logger;
     
     @Override
     public boolean initController()
     {
         this.fileDir = ConfigFactory.getInstance().getProperty("File_Transfer_Directory");
         this.detectedFiles = new ArrayList<String>();
         this.mimeTypes = new MimetypesFileTypeMap();
         
         this.logger = LoggerFactory.getLoggerInstance();
         return true;
     }
     
     @Override
     public boolean preRoute()
     {
         return true;
     }
     
     public PrimitiveResponse binaryFileAction(PrimitiveRequest request)
     {
         PrimitiveResponse response = new PrimitiveResponse();
         response.setSuccessful(true);
         
         String fileName = request.getParameters().get("filename");
         if (fileName == null)
         {
             response.setSuccessful(false);
             response.setErrorCode(1);
             response.setErrorReason("File name not specified");
             return response;
         }
         
         /* Get the file. */
         File file = new File(this.fileDir, fileName);
         if (!file.isFile())
         {
             response.setSuccessful(false);
             response.setErrorCode(2);
             response.setErrorReason("File does not exist");
             return response;
         }
         
         FileInputStream fileInput = null;
         byte buf[] = new byte[1024];
         StringBuilder contents = new StringBuilder();
         int read;
         try
         {
             fileInput = new FileInputStream(file);
             while ((read = fileInput.read(buf)) != -1) contents.append(Base64.encodeBytes(buf, 0, read));
             response.addResult("filecontents", contents.toString());
         }
         catch (Exception e)
         {
             response.setSuccessful(false);
             response.setErrorCode(3);
             response.setErrorReason("Reading file failed.");
         }
         finally
         {
             if (fileInput != null) 
             {
                 try
                 {
                     fileInput.close();
                 }
                 catch (IOException e)
                 { /* Best effort to close it. */ }
             }
         }
         
         return response;
     }
     
     public PrimitiveResponse textFileAction(PrimitiveRequest request)
     {
         PrimitiveResponse response = new PrimitiveResponse();
         response.setSuccessful(true);
         
         String fileName = request.getParameters().get("filename");
         if (fileName == null)
         {
             response.setSuccessful(false);
             response.setErrorCode(1);
             response.setErrorReason("File name not specified");
             return response;
         }
         
         /* Get the file. */
         File file = new File(this.fileDir, fileName);
         if (!file.isFile())
         {
             response.setSuccessful(false);
             response.setErrorCode(2);
             response.setErrorReason("File does not exist");
             return response;
         }
         
         BufferedReader reader = null;
         StringBuilder contents = new StringBuilder();
         String tmp;
         try
         {
             reader = new BufferedReader(new FileReader(file));
             while ((tmp = reader.readLine()) != null)
             {
                 contents.append(tmp);
                 contents.append(System.getProperty("line.separator"));
             }
             response.addResult("filecontents", contents.toString());
         }
         catch (Exception e)
         {
             response.setSuccessful(false);
             response.setErrorCode(3);
             response.setErrorReason("Reading file failed.");
         }
         finally
         {
             if (reader != null)
             {
                 try
                 {
                     reader.close();
                 }
                 catch (IOException e)
                 { /* Best effort to close it. */}
             }
         }
 
         return response;
     }
     
     /**
      * Action which provides a list of files that are located within the
      * configured directory location. Invocations of this action will
      * provide indication whether the file was first detected in this
      * current invocation or have been previously detected. Only the name 
      * of the file is provided not the its fully qualified path. 
      * The name may be used as the requested filename in a call to either 
      * the <tt>binaryFile</tt>, or <tt>textFile</tt> actions.
      * <br />
      * The parameters for this action are:
      * <ul>
      *  <li>extension - The file name extension. This is optional and if it is
      *  not provided the file name extension is not filtered to match a file
      *  extension.</li>
      *  <li>regex - Regular expression pattern the file name should match.
      *  This is optional and if it is not provided the file is not name is not
      *  filtered to match a regular expression.</li>
      * </ul>
      * The response is a list of file information in the following form:
      * <ul>
      *  <li>&lt;name&gt; =&gt; &lt;name&gt;,&lt;mime&gt;,&lt;binary|text&gt;[,new]</li>
      *  <li>&lt;name&gt; =&gt; &lt;mime&gt;,&lt;binary|text&gt;[,new]</li>
      *  <li>...</li>
      *  <li>&lt;name&gt; =&gt; &lt;mime&gt;,&lt;binary|text&gt;[,new]</li>
      * </ul>
      * Where:
      * <ul>
      *  <li>name - Name of the file.</li>
      *  <li>mime - The detected mime type of the file.</li>
      *  <li>binary | text - Whether the file is detected as either a binary or
      *  text file.</li>
      *  <li>new - Specifies this is the first time the file is detected.</li>
      * </ul>
      * If this action isn't successful, the following error codes may be set:
      * <ul>
     *  <li>1 - No file transfer directory has been configured.</li>
     *  <li>2 - The configured directory does not exist.</li>
      * </ul>
      * 
      * @param request request parameters.
      * @return response parameters
      */
     public PrimitiveResponse listFilesAction(PrimitiveRequest request)
     {
         PrimitiveResponse response = new PrimitiveResponse();
         response.setSuccessful(true);
         
         String ext = request.getParameters().get("extension");
         if (ext != null)
         {
             this.logger.debug("Only going to list files that have the file extension '" + ext + "'.");
         }
         
         String regex = request.getParameters().get("regex");
         if (regex != null)
         {
             this.logger.debug("Only going to list files that match the regular expression '" + regex + "'.");
         }
         
        if (this.fileDir == null)
        {
            this.logger.warn("No file transfer directory configured.");
            response.setSuccessful(false);
            response.setErrorCode(1);
            response.setErrorReason("Directory not configured.");
            return response;
        }
        
         File dir = new File(this.fileDir);
         if (!dir.isDirectory())
         {
             this.logger.warn("Unable to detect files in directory " + this.fileDir + " becase it does not exist.");
             response.setSuccessful(false);
            response.setErrorCode(2);
             response.setErrorReason("Directory does not exist.");
             return response;
         }
         
         for (File file : dir.listFiles())
         {
             String name = file.getName();
             if ("".equals(name)) continue;
             
             /* Prune the files that don't match any specified contraints. */
             if (ext != null)
             {
                 if (!name.endsWith(ext)) continue;
             }
             
             if (regex != null)
             {
                 if (!name.matches(regex)) continue;
             }
             
             /* Name is viable so add it to the response. */
             StringBuilder info = new StringBuilder();
             
             String mime = this.mimeTypes.getContentType(file);
             info.append(this.mimeTypes.getContentType(file));
             info.append(',');
             
            System.out.println("MIME is " + mime);
             if (mime.startsWith("text"))
             {
                 info.append("text");
             }
             else
             {
                 info.append("binary");
             }
             
             if (!this.detectedFiles.contains(name))
             {
                 this.detectedFiles.add(name);
                 info.append(",new");
             }
             
             response.addResult(name, info.toString());
         }
         
         return response;
     }
 
     
     @Override
     public boolean postRoute()
     {
         return true;
     }
     
     @Override
     public void cleanup()
     { 
         this.detectedFiles.clear();
     }
 }
