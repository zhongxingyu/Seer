 /**
  * University of Illinois/NCSA
  * Open Source License
  *
  * Copyright (c) 2008, Board of Trustees-University of Illinois.
  * All rights reserved.
  *
  * Developed by:
  *
  * Automated Learning Group
  * National Center for Supercomputing Applications
  * http://www.seasr.org
  *
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to
  * deal with the Software without restriction, including without limitation the
  * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
  * sell copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  *  * Redistributions of source code must retain the above copyright notice,
  *    this list of conditions and the following disclaimers.
  *
  *  * Redistributions in binary form must reproduce the above copyright notice,
  *    this list of conditions and the following disclaimers in the
  *    documentation and/or other materials provided with the distribution.
  *
  *  * Neither the names of Automated Learning Group, The National Center for
  *    Supercomputing Applications, or University of Illinois, nor the names of
  *    its contributors may be used to endorse or promote products derived from
  *    this Software without specific prior written permission.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
  * CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
  * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
  * WITH THE SOFTWARE.
  */
 
 package org.seasr.meandre.components.analytics.mallet;
 
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.Writer;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.text.SimpleDateFormat;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.zip.GZIPOutputStream;
 
 import org.meandre.annotations.Component;
 import org.meandre.annotations.Component.FiringPolicy;
 import org.meandre.annotations.Component.Licenses;
 import org.meandre.annotations.Component.Mode;
 import org.meandre.annotations.ComponentInput;
 import org.meandre.annotations.ComponentOutput;
 import org.meandre.annotations.ComponentProperty;
 import org.meandre.core.ComponentContext;
 import org.meandre.core.ComponentContextProperties;
 import org.seasr.datatypes.core.BasicDataTypesTools;
 import org.seasr.datatypes.core.DataTypeParser;
 import org.seasr.datatypes.core.Names;
 import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
 
 import cc.mallet.topics.ParallelTopicModel;
 import cc.mallet.topics.TopicAssignment;
 import cc.mallet.types.IDSorter;
 import de.schlichtherle.io.FileOutputStream;
 
 /**
  * @author Boris Capitanu
  */
 
 @Component(
         name = "Document Topics To CSV",
         creator = "Boris Capitanu",
         baseURL = "meandre://seasr.org/components/foundry/",
         firingPolicy = FiringPolicy.all,
         mode = Mode.compute,
         rights = Licenses.UofINCSA,
        tags = "#output, mallet, topic model, csv",
         description = "This component creates a CSV file containing the processed documents, and for each processed document " +
         		"the set of topics and topic probabilities" ,
         dependency = {"protobuf-java-2.2.0.jar","trove-2.0.3.jar"}
 )
 public class DocumentTopicsToCSVFile extends AbstractExecutableComponent {
 
     //------------------------------ INPUTS ------------------------------------------------------
 
     @ComponentInput(
             name = "topic_model",
             description = "The topic model" +
                 "<br>TYPE: cc.mallet.topics.ParallelTopicModel"
     )
     protected static final String IN_TOPIC_MODEL = "topic_model";
 
     @ComponentInput(
             name = Names.PORT_LOCATION,
             description = "The URL or file name of the CSV file to be created" +
                 "<br>TYPE: java.net.URI" +
                 "<br>TYPE: java.net.URL" +
                 "<br>TYPE: java.lang.String" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
     )
     protected static final String IN_LOCATION = Names.PORT_LOCATION;
 
     //------------------------------ OUTPUTS -----------------------------------------------------
 
     @ComponentOutput(
             name = Names.PORT_LOCATION,
             description = "The location of the CSV file created" +
                     "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
     )
     protected static final String OUT_CSV_LOCATION = Names.PORT_LOCATION;
 
     @ComponentOutput(
             name = "topic_model",
             description = "The topic model (same as input)" +
                 "<br>TYPE: cc.mallet.topics.ParallelTopicModel"
     )
     protected static final String OUT_TOPIC_MODEL = "topic_model";
 
     //----------------------------- PROPERTIES ---------------------------------------------------
 
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
             name = "use_compression",
             description = "Use compression?",
             defaultValue = "false"
     )
     protected static final String PROP_USE_COMPRESSION = "use_compression";
 
     @ComponentProperty(
             name = Names.PROP_SEPARATOR,
             description = "The delimiter to use to separate the data columns in the CSV file",
             defaultValue = ","
     )
     protected static final String PROP_SEPARATOR = Names.PROP_SEPARATOR;
 
     //--------------------------------------------------------------------------------------------
 
 
     protected static final String NEWLINE = System.getProperty("line.separator");
     protected String _defaultFolder, _publicResourcesDir;
     protected boolean _appendTimestamp, _useCompression;
     protected String _separator;
 
 
     //--------------------------------------------------------------------------------------------
 
     @Override
     public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
         _defaultFolder = getPropertyOrDieTrying(PROP_DEFAULT_FOLDER, true, false, ccp);
         if (_defaultFolder.length() == 0)
             _defaultFolder = ccp.getPublicResourcesDirectory();
         else
             if (!_defaultFolder.startsWith(File.separator))
                 _defaultFolder = new File(ccp.getPublicResourcesDirectory(), _defaultFolder).getAbsolutePath();
 
         console.fine("Default folder set to: " + _defaultFolder);
 
         _appendTimestamp = Boolean.parseBoolean(getPropertyOrDieTrying(PROP_APPEND_TIMESTAMP, ccp));
         _useCompression = Boolean.parseBoolean(getPropertyOrDieTrying(PROP_USE_COMPRESSION, ccp));
         _separator = getPropertyOrDieTrying(PROP_SEPARATOR, false, true, ccp).replaceAll("\\\\t", "\t");
 
         _publicResourcesDir = new File(ccp.getPublicResourcesDirectory()).getAbsolutePath();
         if (!_publicResourcesDir.endsWith(File.separator)) _publicResourcesDir += File.separator;
     }
 
     @Override
     public void executeCallBack(ComponentContext cc) throws Exception {
         String location = DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_LOCATION))[0];
         ParallelTopicModel topicModel = (ParallelTopicModel) cc.getDataComponentFromInput(IN_TOPIC_MODEL);
 
         File file = getLocation(location, _defaultFolder);
         File parentDir = file.getParentFile();
 
         if (!parentDir.exists()) {
             if (parentDir.mkdirs())
                 console.finer("Created directory: " + parentDir);
         } else
             if (!parentDir.isDirectory())
                 throw new IOException(parentDir.toString() + " must be a directory!");
 
         if (_appendTimestamp) {
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
 
         OutputStream os = new BufferedOutputStream(new FileOutputStream(file));
         if (_useCompression)
             os = new GZIPOutputStream(os);
 
         Writer writer = new OutputStreamWriter(os);
         try {
             writer.write(String.format("doc_id%sdoc_name", _separator));
 
             int numTopics = topicModel.getNumTopics();
 
             IDSorter[] sortedTopics = new IDSorter[numTopics];
             for (int topic = 0; topic < numTopics; topic++) {
                 // Initialize the sorters with dummy values
                 sortedTopics[topic] = new IDSorter(topic, topic);
                 writer.write(String.format("%stopic_%d", _separator, topic));
             }
 
             writer.write(NEWLINE);
 
             int dataSize = topicModel.getData().size();
             int[] topicCounts = new int[numTopics];
             double temp_weight[] = new double[numTopics];
             int docNum = 0;
             for (TopicAssignment ta : topicModel.getData()) {
                 int[] features = ta.topicSequence.getFeatures();
 
                 // Count up the tokens
                 for (int i = 0, iMax = features.length; i < iMax; i++)
                     topicCounts[features[i]]++;
 
                 // And normalize
                 for (int topic = 0; topic < numTopics; topic++)
                     sortedTopics[topic].set(topic, (float) topicCounts[topic] / features.length);
 
                 Arrays.fill(topicCounts, 0); // initialize for next round
                 //Arrays.sort(sortedTopics);
 
                 writer.write(Integer.toString(docNum++) + _separator); // doc_id
                 writer.write(ta.instance.getName().toString()); // doc_name
 
                 for (int i = 0, iMax = sortedTopics.length; i < iMax; i++) {
                     double weight = sortedTopics[i].getWeight();
                     writer.write(String.format("%s%.4f", _separator, weight));
                 }
 
                 writer.write(NEWLINE);
 
                 if (docNum % 5000 == 0)
                     console.fine(String.format("Processed %,d out of %,d", docNum, dataSize));
             }
 
             console.fine("CSV created");
         }
         finally {
             writer.close();
             os.close();
         }
 
         cc.pushDataComponentToOutput(OUT_CSV_LOCATION, BasicDataTypesTools.stringToStrings(file.toURI().toURL().toString()));
         cc.pushDataComponentToOutput(OUT_TOPIC_MODEL, topicModel);
     }
 
     @Override
     public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
     }
 
     //--------------------------------------------------------------------------------------------
 
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
