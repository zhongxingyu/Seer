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
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Properties;
 
 import javax.xml.transform.OutputKeys;
 
 import org.meandre.annotations.Component;
 import org.meandre.annotations.Component.FiringPolicy;
 import org.meandre.annotations.Component.Licenses;
 import org.meandre.annotations.Component.Mode;
 import org.meandre.annotations.ComponentInput;
 import org.meandre.annotations.ComponentOutput;
 import org.meandre.annotations.ComponentProperty;
 import org.meandre.core.ComponentContext;
 import org.meandre.core.ComponentContextProperties;
 import org.seasr.datatypes.core.BasicDataTypes.Bytes;
 import org.seasr.datatypes.core.BasicDataTypesTools;
 import org.seasr.datatypes.core.DataTypeParser;
 import org.seasr.datatypes.core.Names;
 import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
 import org.seasr.meandre.support.generic.io.DOMUtils;
 import org.w3c.dom.Document;
 
 import cc.mallet.types.InstanceList;
 
 /**
  * Writes the given data to a file
  *
  * @author Boris Capitanu
  * @author Ian Wood
  */
 
 @Component(
         name = "Write To File",
         creator = "Boris Capitanu",
         baseURL = "meandre://seasr.org/components/foundry/",
         firingPolicy = FiringPolicy.all,
         mode = Mode.compute,
         rights = Licenses.UofINCSA,
         tags = "#OUTPUT, io, file, write, bytes",
         description = "This component writes the given data to a file. Objects of type byte[] " +
         		"are written as is. Objects of type org.w3c.dom.Document are written as xml files. " +
         		"If " + WriteFile.PROP_SERIALIZE_DATA + "is set to 'true', Objects that implement " +
         		"java.io.Serializable are written as serialised data.",
         dependency = {"protobuf-java-2.2.0.jar"}
 )
 public class WriteFile extends AbstractExecutableComponent {
 
     //------------------------------ INPUTS ------------------------------------------------------
 
     @ComponentInput(
             name = Names.PORT_LOCATION,
             description = "The URL or file name specifying where the data will be written. " +
                 "<br>TYPE: java.net.URI" +
                 "<br>TYPE: java.net.URL" +
                 "<br>TYPE: java.lang.String" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
     )
     protected static final String IN_LOCATION = Names.PORT_LOCATION;
 
     @ComponentInput(
             name = "data",
             description = "The data to write" +
                 "<br>TYPE: java.lang.String" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings" +
                 "<br>TYPE: byte[]" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Bytes" +
                 "<br>TYPE: org.w3c.dom.Document" +
                 "<br>TYPE: java.lang.Object" +
                 "<br>TYPE: java.io.Serializable"
     )
     protected static final String IN_DATA = "data";
 
     //------------------------------ OUTPUTS -----------------------------------------------------
 
     @ComponentOutput(
             name = Names.PORT_LOCATION,
             description = "The URL or file name containing the written data. The location can be " +
             "a full file:/// URL, or an absolute or relative pathname." +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
     )
     protected static final String OUT_LOCATION = Names.PORT_LOCATION;
 
     @ComponentOutput(
             name = "data",
             description = "The data written" +
                 "<br>TYPE: same as input"
     )
     protected static final String OUT_DATA = "data";
 
     //------------------------------ PROPERTIES --------------------------------------------------
 
     @ComponentProperty(
             name = Names.PROP_DEFAULT_FOLDER,
             description = "The folder to write to. If the specified location " +
             		"is not a valid URL or an absolute path, it will be assumed relative to the " +
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
             name = "append_data_to_file",
             description = "Append the data to the output file if it exists?",
             defaultValue = "false"
     )
     protected static final String PROP_APPEND_DATA = "append_data_to_file";
 
     @ComponentProperty(
             name = "serialize_output",
             description = "Store data via the java.io.Serializable interface if possible.",
             defaultValue = "false"
     )
     protected static final String PROP_SERIALIZE_DATA = "serialize_output";
 
     //--------------------------------------------------------------------------------------------
 
 
     private String defaultFolder, publicResourcesDir;
     private boolean appendTimestamp, appendData, _serializeOutput;
     private Properties outputProperties;
     private File file = null;
 
     //--------------------------------------------------------------------------------------------
 
     @Override
     public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
         defaultFolder = getPropertyOrDieTrying(PROP_DEFAULT_FOLDER, true, false, ccp);
         if (defaultFolder.length() == 0)
             defaultFolder = ccp.getPublicResourcesDirectory();
         else
             if (!defaultFolder.startsWith(File.separator))
                 defaultFolder = new File(ccp.getPublicResourcesDirectory(), defaultFolder).getAbsolutePath();
 
         console.fine("Default folder set to: " + defaultFolder);
 
         appendTimestamp = Boolean.parseBoolean(getPropertyOrDieTrying(PROP_APPEND_TIMESTAMP, ccp));
         appendData = Boolean.parseBoolean(getPropertyOrDieTrying(PROP_APPEND_DATA, ccp));
         _serializeOutput = Boolean.parseBoolean(getPropertyOrDieTrying(PROP_SERIALIZE_DATA, ccp));
 
         publicResourcesDir = new File(ccp.getPublicResourcesDirectory()).getAbsolutePath();
         if (!publicResourcesDir.endsWith(File.separator)) publicResourcesDir += File.separator;
 
         outputProperties = new Properties();
         outputProperties.setProperty(OutputKeys.INDENT, "yes");
         outputProperties.setProperty("{http://xml.apache.org/xslt}indent-amount", "2");
         outputProperties.setProperty(OutputKeys.ENCODING, "UTF-8");
     }
 
     @Override
     public void executeCallBack(ComponentContext cc) throws Exception {
         String location = DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_LOCATION))[0];
         Object inData = cc.getDataComponentFromInput(IN_DATA);
 
         file = getLocation(location, defaultFolder);
         File parentDir = file.getParentFile();
 
         if (!parentDir.exists()) {
             if (parentDir.mkdirs())
                 console.finer("Created directory: " + parentDir);
         } else
             if (!parentDir.isDirectory())
                 throw new IOException(parentDir.toString() + " must be a directory!");
 
         if (appendTimestamp) {
             String name = file.getName();
             String timestamp = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
 
             int pos = name.lastIndexOf(".");
             if (pos < 0)
                 name += "_" + timestamp;
             else
                 name = String.format("%s_%s%s", name.substring(0, pos), timestamp, name.substring(pos));
 
             file = new File(parentDir, name);
         }
 
         console.fine(String.format("Writing file %s", file));
         System.out.println("Writing file "+file);
 
         // Write the data to file
         FileOutputStream fos = new FileOutputStream(file, appendData);
         try {
 	        if (inData instanceof Serializable && _serializeOutput) {
 	        	ObjectOutputStream ois = new ObjectOutputStream (fos);
 	        	ois.writeObject(inData);
 	        	ois.close();
 	        }
 
 	        else
 	        	
 	        if (inData instanceof byte[] || inData instanceof Bytes)
 	            fos.write(DataTypeParser.parseAsByteArray(inData));
 
 	        else
 
 	        if (inData instanceof Document)
 	            DOMUtils.writeXML((Document) inData, fos, outputProperties);
 
 	        else {
 	        	for (String s : DataTypeParser.parseAsString(inData)) 
 	        		fos.write(s.getBytes("UTF-8"));
 	        }
 	        }
         finally {
         	fos.close();
         }
 
         if (file.getAbsolutePath().startsWith(publicResourcesDir)) {
             String publicLoc = file.getAbsolutePath().substring(publicResourcesDir.length());
             URL outputURL = new URL(cc.getWebUIUrl(true), "/public/resources/" + publicLoc);
             console.info("File accessible at: " + outputURL);
             cc.pushDataComponentToOutput(OUT_LOCATION, BasicDataTypesTools.stringToStrings(outputURL.toString()));
         }
         else
             cc.pushDataComponentToOutput(OUT_LOCATION, BasicDataTypesTools.stringToStrings(file.toString()));
 
         cc.pushDataComponentToOutput(OUT_DATA, inData);
     }
 
     @Override
     public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
     	if (componentContext != null) {
     		if (componentContext.isFlowAborting() && file != null) {
     			try {
     				file.delete();
     			} catch (Exception e) { }
     		}
     	}
     	
     	file = null;
     }
 
     //--------------------------------------------------------------------------------------------
 
     /**
      * Gets a file reference to the location specified
      *
      * @param location The location; can be a full file:/// URL, or an absolute or relative pathname
      * @param defaultFolder The folder to use as base for relatively specified pathnames, or null to use current folder
      * @return The File reference to the location
     * @throws Exception 
      */
    protected File getLocation(String location, String defaultFolder) throws Exception {
         // Check if the location is a fully-specified URL
         URL locationURL;
         try {
             locationURL = new URI(location).toURL();
         }
        catch (Exception e) { // URISyntaxException MalformedURLException
        	if (e.getClass() != IllegalArgumentException.class && e.getClass() != URISyntaxException.class && e.getClass() != MalformedURLException.class ) 
        		throw e;
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
