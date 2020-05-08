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
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Properties;
 import java.util.zip.Deflater;
 
 import javax.xml.transform.OutputKeys;
 
 import org.apache.commons.compress.archivers.ArchiveEntry;
 import org.apache.commons.compress.archivers.ArchiveOutputStream;
 import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
 import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
 import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
 import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
 import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
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
         name = "Write To Archive",
         creator = "Boris Capitanu",
         baseURL = "meandre://seasr.org/components/foundry/",
         firingPolicy = FiringPolicy.any,
         mode = Mode.compute,
         rights = Licenses.UofINCSA,
         tags = "#OUTPUT, io, write, zip, tar, tgz, archive",
         description = "This component writes an archive file containing all the data passed in the stream",
         dependency = {"protobuf-java-2.2.0.jar", "commons-compress-1.4.jar"}
 )
 public class WriteArchive extends AbstractStreamingExecutableComponent {
 
     //------------------------------ INPUTS ------------------------------------------------------
 
     @ComponentInput(
             name = Names.PORT_LOCATION,
             description = "The URL or file name specifying where the archive file will be written" +
                 "<br>TYPE: java.net.URI" +
                 "<br>TYPE: java.net.URL" +
                 "<br>TYPE: java.lang.String" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
     )
     protected static final String IN_LOCATION = Names.PORT_LOCATION;
 
     @ComponentInput(
             name = "file_name",
             description = "The file name to use to add to the archive" +
                 "<br>TYPE: java.lang.String" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
     )
     protected static final String IN_FILE_NAME = "file_name";
 
     @ComponentInput(
             name = "data",
             description = "The data corresponding to the file name specified, to be archived" +
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
             description = "The URL or file name of the resulting archive file" +
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
 
     @ComponentProperty(
             name = "timestamp_format",
             description = "The timestamp format to use. See Java SimpleDateFormat for documentation.",
             defaultValue = "yyyy-MM-dd-HH-mm-ss"
     )
     protected static final String PROP_TIMESTAMP_FORMAT = "timestamp_format";
 
     @ComponentProperty(
             name = "append_extension",
             description = "Append the appropriate extension (.zip, .tar, or .tgz depending on archive format) to the file specified in the location?",
             defaultValue = "true"
     )
     protected static final String PROP_APPEND_EXTENSION = "append_extension";
 
     @ComponentProperty(
             name = "archive_format",
             description = "The desired archive format. One of: zip, tar, tgz",
             defaultValue = "zip"
     )
     protected static final String PROP_ARCHIVE_FORMAT = "archive_format";
 
     //--------------------------------------------------------------------------------------------
 
 
     private String defaultFolder, publicResourcesDir;
 
     private boolean appendTimestamp;
     private String timestampFormat;
     private boolean appendExtension;
     private String archiveFormat;
 
     private Properties outputProperties;
     private boolean isStreaming = false;
     private File outputFile = null;
 
     private ArchiveOutputStream archiveStream = null;
 
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
         timestampFormat = getPropertyOrDieTrying(PROP_TIMESTAMP_FORMAT, ccp);
 
         appendExtension = Boolean.parseBoolean(getPropertyOrDieTrying(PROP_APPEND_EXTENSION, ccp));
         archiveFormat = getPropertyOrDieTrying(PROP_ARCHIVE_FORMAT, ccp).toLowerCase();
 
         if (!archiveFormat.equals("zip") && !archiveFormat.equals("tar") && !archiveFormat.equals("tgz"))
         	throw new ComponentContextException("Invalid archive format! Must be one of: zip, tar, tgz");
 
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
 
         if (archiveStream == null && componentInputCache.hasData(IN_LOCATION)) {
             Object input = componentInputCache.retrieveNext(IN_LOCATION);
             if (input instanceof StreamDelimiter)
                 throw new ComponentExecutionException(String.format("Stream delimiters should not arrive on port '%s'!", IN_LOCATION));
 
             String location = DataTypeParser.parseAsString(input)[0];
             if (appendExtension) location += String.format(".%s", archiveFormat);
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
                 String timestamp = new SimpleDateFormat(timestampFormat).format(new Date());
 
                 int pos = name.lastIndexOf(".");
                 if (pos < 0)
                     name += "_" + timestamp;
                 else
                     name = String.format("%s_%s%s", name.substring(0, pos), timestamp, name.substring(pos));
 
                 outputFile = new File(parentDir, name);
             }
 
             console.fine(String.format("Writing file %s", outputFile));
 
             if (archiveFormat.equals("zip")) {
 	            archiveStream = new ZipArchiveOutputStream(outputFile);
 	            ((ZipArchiveOutputStream) archiveStream).setLevel(Deflater.BEST_COMPRESSION);
             }
 
             else
 
             if (archiveFormat.equals("tar") || archiveFormat.equals("tgz")) {
             	OutputStream fileStream = new BufferedOutputStream(new FileOutputStream(outputFile));
             	if (archiveFormat.equals("tgz"))
             		fileStream = new GzipCompressorOutputStream(fileStream);
             	archiveStream = new TarArchiveOutputStream(fileStream);
             }
         }
 
         // Return if we haven't received a zip or tar location yet
         if (archiveStream == null) return;
 
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
                         closeArchiveAndPushOutput();
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
 
 
             byte[] entryData = null;
 
             if (inData instanceof byte[] || inData instanceof Bytes)
                 entryData = DataTypeParser.parseAsByteArray(inData);
 
             else
 
             if (inData instanceof Document) {
             	ByteArrayOutputStream baos = new ByteArrayOutputStream();
                 DOMUtils.writeXML((Document) inData, baos, outputProperties);
                 entryData = baos.toByteArray();
             }
 
             else
                 entryData = DataTypeParser.parseAsString(inData)[0].getBytes("UTF-8");
 
             String entryName = DataTypeParser.parseAsString(inFileName)[0];
 
             console.fine(String.format("Adding %s entry: %s", archiveFormat.toUpperCase(), entryName));
 
             ArchiveEntry entry = null;
             if (archiveFormat.equals("zip"))
             	entry = new ZipArchiveEntry(entryName);
 
             else
 
            if (archiveFormat.equals("tar") || archiveFormat.equals("tgz")) {
         	   entry = new TarArchiveEntry(entryName);
         	   ((TarArchiveEntry) entry).setSize(entryData.length);
            }
 
             archiveStream.putArchiveEntry(entry);
             archiveStream.write(entryData);
             archiveStream.closeArchiveEntry();
 
             if (!isStreaming) {
                 closeArchiveAndPushOutput();
                 break;
             }
         }
     }
 
     @Override
     public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
     	if (archiveStream != null) {
     		archiveStream.close();
     		archiveStream = null;
     	}
 
         if (componentContext.isFlowAborting() && outputFile != null)
         	outputFile.delete();
 
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
 
     protected void closeArchiveAndPushOutput() throws IOException, MalformedURLException, ComponentContextException {
         if (archiveStream != null) {
         	archiveStream.finish();
         	archiveStream.close();
         }
 
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
