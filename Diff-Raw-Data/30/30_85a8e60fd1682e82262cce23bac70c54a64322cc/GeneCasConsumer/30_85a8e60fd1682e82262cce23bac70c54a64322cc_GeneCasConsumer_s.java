 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 
 package org.apache.uima.cpe;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import org.apache.uima.cas.CAS;
 import org.apache.uima.cas.CASException;
 import org.apache.uima.cas.FSIterator;
 import org.apache.uima.cas.impl.XCASSerializer;
 import org.apache.uima.collection.CasConsumer_ImplBase;
 import org.apache.uima.examples.SourceDocumentInformation;
 import org.apache.uima.genetypesystem.id;
 import org.apache.uima.jcas.JCas;
 import org.apache.uima.resource.ResourceInitializationException;
 import org.apache.uima.resource.ResourceProcessException;
 import org.apache.uima.util.XMLSerializer;
 import org.xml.sax.SAXException;
 
 /**
  * A simple CAS consumer that generates XCAS (XML representation of the CAS) files in the
  * filesystem.
  * <p>
  * This CAS Consumer takes one parameters:
  * <ul>
  * <li><code>OutputDirectory</code> - path to directory into which output files will be written</li>
  * </ul>
  * 
  * 
  */
 public class GeneCasConsumer extends CasConsumer_ImplBase {
   /**
    * Name of configuration parameter that must be set to the path of a directory into which the
    * output files will be written.
    */
   public static final String PARAM_OUTPUTDIR = "OutputDirectory";
 
   private File mOutputDir;
   private File outputFile;
   FileWriter fw = null;
   BufferedWriter bw = null;
 
   public void initialize() throws ResourceInitializationException {
 
     mOutputDir = new File((String) getConfigParameterValue(PARAM_OUTPUTDIR));
     if (!mOutputDir.exists()) {
       mOutputDir.mkdir();
     }
     outputFile = new File(mOutputDir, "hw1.out");
     try {
       fw = new FileWriter(outputFile, true);
     } catch (IOException e) {
       // TODO Auto-generated catch block
       e.printStackTrace();
     }
     bw = new BufferedWriter(fw); 
   }
 
   /**
    * Processes the CasContainer which was populated by the TextAnalysisEngines. <br>
    * In this case, the CAS is converted to XML and written into the output file .
    * 
    * @param aCAS
    *          CasContainer which has been populated by the TAEs
    * 
    * @throws ResourceProcessException
    *           if there is an error in processing the Resource
    * 
    * @see org.apache.uima.collection.base_cpm.CasObjectProcessor#processCas(org.apache.uima.cas.CAS)
    */
   public void processCas(CAS aCAS) throws ResourceProcessException {
     JCas jcas;
     try {
       jcas = aCAS.getJCas();
     } catch (CASException e) {
       throw new ResourceProcessException(e);
     }
 
     // retreive the filename of the input file from the CAS
     FSIterator it = jcas.getAnnotationIndex(id.type).iterator();
     String outputString = new String();
      while (it.hasNext()) {
       id geneid = (id) it.next();
       int a = geneid.getBegin();
       int b = geneid.getEnd();
       String context = geneid.getId();
       String header = context.substring(0, context.indexOf(" ") );
       String test1 = context.substring(header.length() + a, header.length() +  b + 1);
       outputString = outputString + header + "|" + a + " " + b + "|"+ test1 + "\r\n"; 
      }
     try {
       writeToFile(outputString, outputFile);
     } catch (IOException e) {
       // TODO Auto-generated catch block
       e.printStackTrace();
      }
   }
 
 
   private void writeToFile(String outputline, File name) throws IOException {
       bw.write(outputline);
   }
 
 }
