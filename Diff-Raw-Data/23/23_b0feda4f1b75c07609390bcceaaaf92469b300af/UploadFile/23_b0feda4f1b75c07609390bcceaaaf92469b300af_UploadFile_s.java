 /**
 *
 * University of Illinois/NCSA
 * Open Source License
 *
 * Copyright (c) 2008, NCSA.  All rights reserved.
 *
 * Developed by:
 * The Automated Learning Group
 * University of Illinois at Urbana-Champaign
 * http://www.seasr.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal with the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject
 * to the following conditions:
 *
 * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimers.
 *
 * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimers in
 * the documentation and/or other materials provided with the distribution.
 *
 * Neither the names of The Automated Learning Group, University of
 * Illinois at Urbana-Champaign, nor the names of its contributors may
 * be used to endorse or promote products derived from this Software
 * without specific prior written permission.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
 *
 */
 
 package org.seasr.meandre.components.tools.text.io;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.meandre.annotations.Component;
 import org.meandre.annotations.ComponentOutput;
 import org.meandre.annotations.ComponentProperty;
 import org.meandre.annotations.Component.Licenses;
 import org.meandre.annotations.Component.Mode;
 import org.meandre.core.ComponentContext;
 import org.meandre.core.ComponentContextException;
 import org.meandre.core.ComponentContextProperties;
 import org.meandre.core.system.components.ext.StreamInitiator;
 import org.meandre.core.system.components.ext.StreamTerminator;
 import org.seasr.datatypes.BasicDataTypesTools;
 import org.seasr.meandre.components.tools.Names;
 import org.seasr.meandre.support.generic.io.JARInstaller;
 import org.seasr.meandre.support.generic.io.JARInstaller.InstallStatus;
 
 import com.oreilly.servlet.multipart.FilePart;
 import com.oreilly.servlet.multipart.MultipartParser;
 import com.oreilly.servlet.multipart.ParamPart;
 import com.oreilly.servlet.multipart.Part;
 
 /** NOTES
  *
  * This component takes NO inputs,
  * User selects one or more files to upload
  * Each file is uploaded to this component
  * on Execute, this component then pushes each of those uploaded files
  * to the next component
  *
  * It is assumed that the server running this component
  * has installed the fluid flash stuff in published_resources
  * see FluidUploader.vm for more details on the html
  *
  * The code here is based on FluidUploader.java in components.io.file
  *
  * @author Lily Dong
  * @author Mike Haberman
  * @author Boris Capitanu
  */
 
 @Component(
         creator = "Lily Dong",
         description = "Uploads and submits text files from user's own machine",
         name = "Upload File",
         tags = "file input upload text",
         rights = Licenses.UofINCSA,
         mode = Mode.webui,
         baseURL = "meandre://seasr.org/components/",
        dependency={ "velocity-1.6.1-dep.jar", "protobuf-java-2.2.0.jar", "fluid-components-0.8.jar" },
         resources={ "FluidUploadFile.vm" }
 )
 public class UploadFile extends GenericTemplate {
 
     //------------------------------ OUTPUTS -----------------------------------------------------
 
     @ComponentOutput(
             description = "The file path of the uploaded file(s)",
             name = Names.PORT_FILENAME
     )
     protected static final String OUT_FILE_PATH = Names.PORT_FILENAME;
 
     @ComponentOutput(
             description = "The MIME type of the uploaded file(s)",
             name = Names.PORT_MIME_TYPE
     )
     protected static final String OUT_MIME_TYPE = Names.PORT_MIME_TYPE;
 
     @ComponentOutput(
             description = "The content of the uploaded file(s)",
             name = Names.PORT_RAW_DATA
     )
     protected static final String OUT_RAW_DATA = Names.PORT_RAW_DATA;
 
     //------------------------------ PROPERTIES --------------------------------------------------
 
 	@ComponentProperty(
 	        description = "The template name",
 	        name = GenericTemplate.PROP_TEMPLATE,
 	        defaultValue = "org/seasr/meandre/components/tools/text/io/FluidUploadFile.vm"
 	)
     protected static final String PROP_TEMPLATE = GenericTemplate.PROP_TEMPLATE;
 
     @ComponentProperty(
             name = Names.PROP_WRAP_STREAM,
             description = "Wrap output as stream?",
             defaultValue = "false"
     )
     protected static final String PROP_WRAP_STREAM = Names.PROP_WRAP_STREAM;
 
     @ComponentProperty(
             name = Names.PROP_MAX_SIZE,
             description = "Maximum file size accepted (in MB) default: 10MB",
             defaultValue = "10"
     )
     protected static final String PROP_MAX_SIZE = Names.PROP_MAX_SIZE;
 
 	//--------------------------------------------------------------------------------------------
 
 
     private int maxFileSize;
     private boolean wrapStream;
 
 
     //--------------------------------------------------------------------------------------------
 
 	@Override
 	public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
 	    super.initializeCallBack(ccp);
 
 	    wrapStream = Boolean.parseBoolean(ccp.getProperty(PROP_WRAP_STREAM));
 	    maxFileSize = Integer.parseInt(ccp.getProperty(PROP_MAX_SIZE));
 	    maxFileSize *= 1024 * 1024;
 
 	    context.put("title", "File Upload");
 
 	    context.put("FPath", "/public/resources/fluid/fluid-components");
 	    String sFluidDir = ccp.getPublicResourcesDirectory() + File.separator + "fluid";
 
 	    File fluidJar = new File(ccp.getPublicResourcesDirectory() + File.separator +
 	            "contexts" + File.separator + "java" + File.separator + "fluid-components-0.8.jar");
 	    if (!fluidJar.exists())
 	        throw new ComponentContextException("Could not find dependency: " + fluidJar.toString());
 
 	    console.fine("Installing Fluid components from: " + fluidJar.toString());
 
         InstallStatus status = JARInstaller.installFromStream(new FileInputStream(fluidJar), sFluidDir, false);
         if (status == InstallStatus.SKIPPED)
             console.fine("Installation skipped - Fluid components already installed");
 
         if (status == InstallStatus.FAILED)
             throw new ComponentContextException("Failed to install Fluid components at " + new File(sFluidDir).getAbsolutePath());
 	}
 
 	public void executeCallBack(ComponentContext cc) throws Exception {
 	    if (wrapStream) pushInitiator();
 
 	    super.executeCallBack(cc);
 
 	    if (wrapStream) pushTerminator();
 	}
 
     //--------------------------------------------------------------------------------------------
 
 	@Override
     protected boolean processRequest(HttpServletRequest request) throws IOException
     {
 	    if (!expectMoreRequests(request)) return true;
 
 	    // TODO: look at using commons-fileupload lib for parsing upload requests - has friendlier license than oreilly
 	    // ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
         // List lstItems = upload.parseRequest(request);
 
 	    MultipartParser mp = new MultipartParser(request, maxFileSize);
 	    Part part;
 	    while ((part = mp.readNextPart()) != null) {
 	        String name = part.getName();
 	        if (part.isParam()) {
 	            // it's a parameter part
 	            ParamPart paramPart = (ParamPart) part;
 	            String value = paramPart.getStringValue();
 	            console.finest("param; name=" + name + ", value=" + value);
 	        }
 	        else if (part.isFile()) {
 	            // it's a file part
 	            FilePart filePart = (FilePart) part;
 	            String fileName = filePart.getFileName();
 	            if (fileName != null) {
 	                // the part actually contained a file
 	                ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
 	                long size = filePart.writeTo(dataStream);
 	                String filePath = filePart.getFilePath();
                     String contentType = filePart.getContentType();
 
 	                console.fine("Received " + filePath + " (" + contentType + "), size: " + size + " bytes");
 
 	                try {
                         componentContext.pushDataComponentToOutput(OUT_FILE_PATH, filePath);
                         componentContext.pushDataComponentToOutput(OUT_MIME_TYPE, contentType);
                         componentContext.pushDataComponentToOutput(OUT_RAW_DATA,
                                 BasicDataTypesTools.byteArrayToBytes(dataStream.toByteArray()));
                     }
                     catch (ComponentContextException e) {
                         throw new IOException(e.toString());
                     }
 
 	            }
 	            else {
 	                // the field did not contain a file
 	                console.finest("file; name=" + name + "; EMPTY");
 	            }
 	        }
 	    }
 
 		return true;
     }
 
     //--------------------------------------------------------------------------------------------
 
 	/**
      * Pushes an initiator.
      *
      * @throws Exception Something went wrong when pushing
      */
     private void pushInitiator() throws Exception {
         console.fine("Pushing " + StreamInitiator.class.getSimpleName());
 
         componentContext.pushDataComponentToOutput(OUT_FILE_PATH, new StreamInitiator());
         componentContext.pushDataComponentToOutput(OUT_MIME_TYPE, new StreamInitiator());
         componentContext.pushDataComponentToOutput(OUT_RAW_DATA, new StreamInitiator());
     }
 
     /**
      * Pushes a terminator.
      *
      * @throws Exception Something went wrong when pushing
      */
     private void pushTerminator() throws Exception {
         console.fine("Pushing " + StreamTerminator.class.getSimpleName());
 
         componentContext.pushDataComponentToOutput(OUT_FILE_PATH, new StreamTerminator());
         componentContext.pushDataComponentToOutput(OUT_MIME_TYPE, new StreamTerminator());
         componentContext.pushDataComponentToOutput(OUT_RAW_DATA, new StreamTerminator());
     }
 }
