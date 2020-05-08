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
 
 package org.seasr.meandre.components.tools.io;
 
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Properties;
 import java.util.zip.Adler32;
 import java.util.zip.CheckedOutputStream;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipOutputStream;
 
 import javax.xml.transform.OutputKeys;
 
 import org.meandre.annotations.Component;
 import org.meandre.annotations.Component.FiringPolicy;
 import org.meandre.annotations.Component.Licenses;
 import org.meandre.annotations.Component.Mode;
 import org.meandre.annotations.ComponentInput;
 import org.meandre.annotations.ComponentOutput;
 import org.meandre.annotations.ComponentProperty;
 import org.meandre.core.ComponentContext;
 import org.meandre.core.ComponentContextException;
 import org.meandre.core.ComponentContextProperties;
 import org.meandre.core.ComponentExecutionException;
 import org.meandre.core.system.components.ext.StreamDelimiter;
 import org.meandre.core.system.components.ext.StreamInitiator;
 import org.meandre.core.system.components.ext.StreamTerminator;
 import org.seasr.datatypes.core.BasicDataTypes.Bytes;
 import org.seasr.datatypes.core.BasicDataTypesTools;
 import org.seasr.datatypes.core.DataTypeParser;
 import org.seasr.datatypes.core.Names;
 import org.seasr.meandre.components.abstracts.AbstractStreamingExecutableComponent;
 import org.seasr.meandre.support.generic.io.DOMUtils;
 import org.w3c.dom.Document;
 
 /**
  * @author Boris Capitanu
  */
 
 @Component(
         name = "Write To Zip",
         creator = "Boris Capitanu",
         baseURL = "meandre://seasr.org/components/foundry/",
         firingPolicy = FiringPolicy.any,
         mode = Mode.compute,
         rights = Licenses.UofINCSA,
         tags = "io, write, zip",
         description = "This component writes a zip file containing all the data passed in the stream",
         dependency = {"protobuf-java-2.2.0.jar"}
 )
 public class WriteZip extends AbstractStreamingExecutableComponent {
 
     //------------------------------ INPUTS ------------------------------------------------------
 
     @ComponentInput(
             name = Names.PORT_LOCATION,
             description = "The URL or file name specifying where the zip file will be written" +
                 "<br>TYPE: java.net.URI" +
                 "<br>TYPE: java.net.URL" +
                 "<br>TYPE: java.lang.String" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
     )
     protected static final String IN_LOCATION = Names.PORT_LOCATION;
 
     @ComponentInput(
             name = "file_name",
             description = "The file name to use to add to the ZIP set" +
                 "<br>TYPE: java.lang.String" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
     )
     protected static final String IN_FILE_NAME = "file_name";
 
     @ComponentInput(
             name = "data",
             description = "The data corresponding to the file name specified, to be zipped" +
                 "<br>TYPE: java.lang.String" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings" +
                 "<br>TYPE: byte[]" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Bytes" +
                 "<br>TYPE: org.w3c.dom.Document" +
                 "<br>TYPE: java.lang.Object"
     )
     protected static final String IN_DATA = "data";
 
     //------------------------------ OUTPUTS -----------------------------------------------------
 
     @ComponentOutput(
             name = Names.PORT_LOCATION,
             description = "The URL or file name of the resulting ZIP file" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
     )
     protected static final String OUT_LOCATION = Names.PORT_LOCATION;
 
     //------------------------------ PROPERTIES --------------------------------------------------
 
     @ComponentProperty(
             name = Names.PROP_DEFAULT_FOLDER,
             description = "The folder to write to. If the specified location " +
                     "is not an absolute path, it will be assumed relative to the " +
                     "published_resources folder.",
             defaultValue = ""
     )
     protected static final String PROP_DEFAULT_FOLDER = Names.PROP_DEFAULT_FOLDER;
 
     @ComponentProperty(
             name = Names.PROP_APPEND_TIMESTAMP,
             description = "Append the current timestamp to the file specified in the location?",
             defaultValue = "false"
     )
     protected static final String PROP_APPEND_TIMESTAMP = Names.PROP_APPEND_TIMESTAMP;
 
     //--------------------------------------------------------------------------------------------
 
 
     private String defaultFolder, publicResourcesDir;
     private boolean appendTimestamp;
     private Properties outputProperties;
     private boolean isStreaming = false;
     private ZipOutputStream zipStream = null;
     private File outputFile = null;
 
 
     //--------------------------------------------------------------------------------------------
 
     @Override
     public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
         super.initializeCallBack(ccp);
 
         defaultFolder = getPropertyOrDieTrying(PROP_DEFAULT_FOLDER, true, false, ccp);
         if (defaultFolder.length() == 0)
             defaultFolder = ccp.getPublicResourcesDirectory();
         else
             if (!defaultFolder.startsWith(File.separator))
                 defaultFolder = new File(ccp.getPublicResourcesDirectory(), defaultFolder).getAbsolutePath();
 
         console.fine("Default folder set to: " + defaultFolder);
 
         appendTimestamp = Boolean.parseBoolean(getPropertyOrDieTrying(PROP_APPEND_TIMESTAMP, ccp));
 
         publicResourcesDir = new File(ccp.getPublicResourcesDirectory()).getAbsolutePath();
         if (!publicResourcesDir.endsWith(File.separator)) publicResourcesDir += File.separator;
 
         outputProperties = new Properties();
         outputProperties.setProperty(OutputKeys.INDENT, "yes");
         outputProperties.setProperty("{http://xml.apache.org/xslt}indent-amount", "2");
         outputProperties.setProperty(OutputKeys.ENCODING, "UTF-8");
     }
 
     @Override
     public void executeCallBack(ComponentContext cc) throws Exception {
         componentInputCache.storeIfAvailable(cc, IN_LOCATION);
         componentInputCache.storeIfAvailable(cc, IN_FILE_NAME);
         componentInputCache.storeIfAvailable(cc, IN_DATA);
 
         if (zipStream == null && componentInputCache.hasData(IN_LOCATION)) {
            Object input = cc.getDataComponentFromInput(IN_LOCATION);
             if (input instanceof StreamDelimiter)
                 throw new ComponentExecutionException(String.format("Stream delimiters should not arrive on port '%s'!", IN_LOCATION));
 
             String location = DataTypeParser.parseAsString(input)[0];
             outputFile = getLocation(location, defaultFolder);
             File parentDir = outputFile.getParentFile();
 
             if (!parentDir.exists()) {
                 if (parentDir.mkdirs())
                     console.finer("Created directory: " + parentDir);
             } else
                 if (!parentDir.isDirectory())
                     throw new IOException(parentDir.toString() + " must be a directory!");
 
             if (appendTimestamp) {
                 String name = outputFile.getName();
                 String timestamp = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
 
                 int pos = name.lastIndexOf(".");
                 if (pos < 0)
                     name += "_" + timestamp;
                 else
                     name = String.format("%s_%s%s", name.substring(0, pos), timestamp, name.substring(pos));
 
                 outputFile = new File(parentDir, name);
             }
 
             console.fine(String.format("Writing file %s", outputFile));
             zipStream = new ZipOutputStream(new BufferedOutputStream(new CheckedOutputStream(new FileOutputStream(outputFile), new Adler32())));
             zipStream.setLevel(9);
         }
 
         // Return if we haven't received a zip location yet
         if (zipStream == null) return;
 
         while (componentInputCache.hasDataAll(new String[] { IN_FILE_NAME, IN_DATA })) {
             Object inFileName = componentInputCache.retrieveNext(IN_FILE_NAME);
             Object inData = componentInputCache.retrieveNext(IN_DATA);
 
             // check for StreamInitiator
             if (inFileName instanceof StreamInitiator || inData instanceof StreamInitiator) {
                 if (inFileName instanceof StreamInitiator && inData instanceof StreamInitiator) {
                     StreamInitiator siFileName = (StreamInitiator) inFileName;
                     StreamInitiator siData = (StreamInitiator) inData;
 
                     if (siFileName.getStreamId() != siData.getStreamId())
                         throw new ComponentExecutionException("Unequal stream ids received!!!");
 
                     if (siFileName.getStreamId() == streamId)
                         isStreaming = true;
                     else
                         // Forward the delimiter(s)
                         cc.pushDataComponentToOutput(OUT_LOCATION, siFileName);
 
                     continue;
                 } else
                     throw new ComponentExecutionException("Unbalanced StreamDelimiter received!");
             }
 
             // check for StreamTerminator
             if (inFileName instanceof StreamTerminator || inData instanceof StreamTerminator) {
                 if (inFileName instanceof StreamTerminator && inData instanceof StreamTerminator) {
                     StreamTerminator stFileName = (StreamTerminator) inFileName;
                     StreamTerminator stData = (StreamTerminator) inData;
 
                     if (stFileName.getStreamId() != stData.getStreamId())
                         throw new ComponentExecutionException("Unequal stream ids received!!!");
 
                     if (stFileName.getStreamId() == streamId) {
                         // end of stream reached
                         closeZipAndPushOutput();
                         isStreaming = false;
                         break;
                     } else {
                         // Forward the delimiter(s)
                         if (isStreaming)
                             console.warning("Likely streaming error - received StreamTerminator for a different stream id than the current active stream! - forwarding it");
                         cc.pushDataComponentToOutput(OUT_LOCATION, stFileName);
                         continue;
                     }
                 } else
                     throw new ComponentExecutionException("Unbalanced StreamDelimiter received!");
             }
 
             zipStream.putNextEntry(new ZipEntry(DataTypeParser.parseAsString(inFileName)[0]));
 
             if (inData instanceof byte[] || inData instanceof Bytes)
                 zipStream.write(DataTypeParser.parseAsByteArray(inData));
 
             else
 
             if (inData instanceof Document)
                 DOMUtils.writeXML((Document) inData, zipStream, outputProperties);
 
             else
                 zipStream.write(DataTypeParser.parseAsString(inData)[0].getBytes("UTF-8"));
 
             zipStream.closeEntry();
 
             if (!isStreaming) {
                 closeZipAndPushOutput();
                 break;
             }
         }
     }
 
     @Override
     public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
         zipStream = null;
         outputFile = null;
         outputProperties = null;
     }
 
     //--------------------------------------------------------------------------------------------
 
     @Override
     public boolean isAccumulator() {
         return true;
     }
 
     @Override
     public void handleStreamInitiators() throws Exception {
         executeCallBack(componentContext);
     }
 
     @Override
     public void handleStreamTerminators() throws Exception {
         executeCallBack(componentContext);
     }
 
     //--------------------------------------------------------------------------------------------
 
     protected void closeZipAndPushOutput() throws IOException, MalformedURLException, ComponentContextException {
         zipStream.close();
         zipStream = null;
 
         if (outputFile.getAbsolutePath().startsWith(publicResourcesDir)) {
             String publicLoc = outputFile.getAbsolutePath().substring(publicResourcesDir.length());
             URL outputURL = new URL(componentContext.getWebUIUrl(true), "/public/resources/" + publicLoc);
             console.info("File accessible at: " + outputURL);
             componentContext.pushDataComponentToOutput(OUT_LOCATION, BasicDataTypesTools.stringToStrings(outputURL.toString()));
         } else
             componentContext.pushDataComponentToOutput(OUT_LOCATION, BasicDataTypesTools.stringToStrings(outputFile.toString()));
     }
 
     /**
      * Gets a file reference to the location specified
      *
      * @param location The location; can be a full file:/// URL, or an absolute or relative pathname
      * @param defaultFolder The folder to use as base for relatively specified pathnames, or null to use current folder
      * @return The File reference to the location
      * @throws MalformedURLException
      * @throws URISyntaxException
      */
     protected File getLocation(String location, String defaultFolder) throws MalformedURLException, URISyntaxException {
         // Check if the location is a fully-specified URL
         URL locationURL;
         try {
             locationURL = new URI(location).toURL();
         }
         catch (IllegalArgumentException e) {
             // Not a fully-specified URL, check if absolute location
             if (location.startsWith(File.separator) || location.startsWith(":" + File.separator, 1))
                 locationURL = new File(location).toURI().toURL();
             else
                 // Relative location
                 locationURL = new File(defaultFolder, location).toURI().toURL();
         }
 
         return new File(locationURL.toURI());
     }
 }
