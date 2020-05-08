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
 
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Iterator;
 
 import org.apache.uima.cas.CAS;
 import org.apache.uima.cas.CASException;
 import org.apache.uima.cas.FSIndex;
 import org.apache.uima.cas.FSIterator;
 import org.apache.uima.cas.impl.XmiCasSerializer;
 import org.apache.uima.collection.CasConsumer_ImplBase;
 import org.apache.uima.collection.base_cpm.CasObjectProcessor;
 import org.apache.uima.examples.SourceDocumentInformation;
 import org.apache.uima.jcas.JCas;
 import org.apache.uima.jcas.tcas.Annotation;
 import org.apache.uima.resource.ResourceConfigurationException;
 import org.apache.uima.resource.ResourceInitializationException;
 import org.apache.uima.resource.ResourceProcessException;
 import org.apache.uima.util.ProcessTrace;
 import org.apache.uima.util.XMLSerializer;
 
 /**
  * Implementation of the CAS Consumer <br>
  * CasConsumer prints to an output file all annotations in the CAS. <br>
  * Parameters needed by the CasConsumer are
  * <ol>
  * <li> "outputFile" : file to which the output files should be written.</li>
  * </ol>
  */
 
 public class CasConsumer extends CasConsumer_ImplBase implements CasObjectProcessor {
   File outFile;
 
   FileWriter fileWriter;
 
   public CasConsumer() {
   }
 
   /**
    * Initializes this CAS Consumer with the parameters specified in the descriptor.
    * 
    * @throws ResourceInitializationException
    *           if there is error in initializing the resources
    */
   public void initialize() throws ResourceInitializationException {
 
     // extract configuration parameter settings
     String oPath = (String) getUimaContext().getConfigParameterValue("outputFile");
 
     // Output file should be specified in the descriptor
     if (oPath == null) {
       throw new ResourceInitializationException(
               ResourceInitializationException.CONFIG_SETTING_ABSENT, new Object[] { "outputFile" });
     }
     // If specified output directory does not exist, try to create it
     outFile = new File(oPath.trim());
     if (outFile.getParentFile() != null && !outFile.getParentFile().exists()) {
       if (!outFile.getParentFile().mkdirs())
         throw new ResourceInitializationException(
                 ResourceInitializationException.RESOURCE_DATA_NOT_VALID, new Object[] { oPath,
                     "outputFile" });
     }
     try {
       fileWriter = new FileWriter(outFile);
     } catch (IOException e) {
       throw new ResourceInitializationException(e);
     }
   }
 
  
   public synchronized void processCas(CAS aCAS) throws ResourceProcessException {
     JCas jcas;
     try {
       jcas = aCAS.getJCas();
     } catch (CASException e) {
       throw new ResourceProcessException(e);
     }
     
     /*Code added try to iterate over annotations
      *Retrieve the built index, iterate over the annotations
      *Generate the output in the required form */
 	FSIndex anIndex = jcas.getAnnotationIndex(Sentence.type);
 	FSIterator anIter = anIndex.iterator();
     while (anIter.isValid()) {
       Sentence annot = (Sentence) anIter.get();
       try {
     	  
     	//Code snippet to find number of spaces in the obtained gene
     	int cnt=1;
     	String geneName=annot.getGeneName();
     	for(int i=0;i<geneName.length();i++)
     	{
     		if(geneName.charAt(i)==' ')
     			cnt=cnt+1;
     	}
     	//Retrieve features and display them in the required format
    	//System.out.println("writing");
    	//System.out.println((String) getUimaContext().getConfigParameterValue("outputFile"));
 		fileWriter.write(annot.getSentenceID() + "|" + 
 						 annot.getGeneLoc() + ""  +
 						 " " +(Integer.parseInt(annot.getGeneLoc()) + annot.getGeneName().length()-cnt) + "|" +
 						 annot.getGeneName() + "\n");
 	} catch (IOException e) {
 		// TODO Auto-generated catch block
 		e.printStackTrace();
 	}
       anIter.moveToNext();
     }
   }
 
   /**
    * Called when a batch of processing is completed.
    * 
    * @param aTrace
    *          ProcessTrace object that will log events in this method.
    * @throws ResourceProcessException
    *           if there is an error in processing the Resource
    * @throws IOException
    *           if there is an IO Error
    * 
    * @see org.apache.uima.collection.CasConsumer#batchProcessComplete(ProcessTrace)
    */
   public void batchProcessComplete(ProcessTrace aTrace) throws ResourceProcessException,
           IOException {
     // nothing to do in this case as AnnotationPrinter doesnot do
     // anything cumulatively
   }
 
   /**
    * Called when the entire collection is completed.
    * 
    * @param aTrace
    *          ProcessTrace object that will log events in this method.
    * @throws ResourceProcessException
    *           if there is an error in processing the Resource
    * @throws IOException
    *           if there is an IO Error
    * @see org.apache.uima.collection.CasConsumer#collectionProcessComplete(ProcessTrace)
    */
   public void collectionProcessComplete(ProcessTrace aTrace) throws ResourceProcessException,
           IOException {
     if (fileWriter != null) {
       fileWriter.close();
     }
   }
 
   /**
    * Reconfigures the parameters of this Consumer. <br>
    * This is used in conjunction with the setConfigurationParameterValue to set the configuration
    * parameter values to values other than the ones specified in the descriptor.
    * 
    * @throws ResourceConfigurationException
    *           if the configuration parameter settings are invalid
    * 
    * @see org.apache.uima.resource.ConfigurableResource#reconfigure()
    */
   public void reconfigure() throws ResourceConfigurationException {
     super.reconfigure();
     // extract configuration parameter settings
     String oPath = (String) getUimaContext().getConfigParameterValue("outputFile");
     File oFile = new File(oPath.trim());
     // if output file has changed, close exiting file and open new
     if (!oFile.equals(this.outFile)) {
       this.outFile = oFile;
       try {
         fileWriter.close();
 
         // If specified output directory does not exist, try to create it
         if (oFile.getParentFile() != null && !oFile.getParentFile().exists()) {
           if (!oFile.getParentFile().mkdirs())
             throw new ResourceConfigurationException(
                     ResourceInitializationException.RESOURCE_DATA_NOT_VALID, new Object[] { oPath,
                         "outputFile" });
         }
         fileWriter = new FileWriter(oFile);
       } catch (IOException e) {
         throw new ResourceConfigurationException();
       }
     }
   }
 
   /**
    * Called if clean up is needed in case of exit under error conditions.
    * 
    * @see org.apache.uima.resource.Resource#destroy()
    */
   public void destroy() {
     if (fileWriter != null) {
       try {
         fileWriter.close();
       } catch (IOException e) {
         // ignore IOException on destroy
       }
     }
   }
 
 }
