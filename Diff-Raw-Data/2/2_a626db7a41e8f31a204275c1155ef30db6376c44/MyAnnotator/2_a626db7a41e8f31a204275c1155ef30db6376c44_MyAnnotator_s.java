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
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.StringReader;
 import java.text.BreakIterator;
 import java.text.ParsePosition;
 import java.util.Locale;
 import java.util.Map;
 
 import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
 import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
 import org.apache.uima.jcas.JCas;
 //import org.apache.uima.jcas.tcas.Annotation;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Properties;
 import java.util.Set;
 
 import org.apache.uima.resource.ResourceInitializationException;
 
 import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
 import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
 import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
 import edu.stanford.nlp.ling.CoreLabel;
 import edu.stanford.nlp.pipeline.Annotation;
 import edu.stanford.nlp.pipeline.StanfordCoreNLP;
 import edu.stanford.nlp.util.CoreMap;
 
 import java.io.File;
 import com.aliasi.chunk.AbstractCharLmRescoringChunker;
 import com.aliasi.chunk.BioTagChunkCodec;
 import com.aliasi.chunk.Chunk;
 import com.aliasi.chunk.Chunker;
 import com.aliasi.chunk.ChunkerEvaluator;
 import com.aliasi.chunk.Chunking;
 import com.aliasi.chunk.TagChunkCodec;
 import com.aliasi.chunk.TagChunkCodecAdapters;
 
 import com.aliasi.corpus.ObjectHandler;
 
 import com.aliasi.util.AbstractExternalizable;
 
 /**
  * Annotator class that annotates Tokens and Sentences. and annotates Gene names.
  * @author suyoun kim
  * @param 
  * @return 
  */
 public class MyAnnotator extends JCasAnnotator_ImplBase {
   JCas jcas;
 
   String input;
 
   ParsePosition pp = new ParsePosition(0);
 
   MyID myID;
 
   MyGene myGene;
 
   Chunker chunker;
 
   private StanfordCoreNLP pipeline;
 
   // *************************************************************
   // * process *
   // *************************************************************
 /**
  * initiate stanford NLP instance and the biomedical NER model file 
  * @author suyoun
  * @return void
  * @throws ResourceInitializationException
  */
   public void PosTagNamedEntityRecognizer() throws ResourceInitializationException {
     Properties props = new Properties();
     props.put("annotators", "tokenize, ssplit, pos");
     pipeline = new StanfordCoreNLP(props);
 
     try {
       File modelFile = new File(
              "D:/0.cmu/2012.fall/2.SE/workspace/11791/hw1-suyoung1/src/main/resources/bio-genetag.HmmChunker");
       System.out.println("Reading chunker from file=" + modelFile);
       chunker = (Chunker) AbstractExternalizable.readObject(modelFile);
 
     } catch (IOException e) {
       // TODO Auto-generated catch block
       e.printStackTrace();
     } catch (ClassNotFoundException e) {
       // TODO Auto-generated catch block
       e.printStackTrace();
     }
   }
 /**
  * extract sentence identifier and add to JCAS as a MyID type 
  * extract "name phrase" in each sentence and call the gene annotator
  * @author suyoun
  * @param text
  * @return position Map
  */
   public Map<Integer, Integer> getNounPhrase(String text) {
     Map<Integer, Integer> begin2end = new HashMap<Integer, Integer>();
     Annotation document = new Annotation(text);
     pipeline.annotate(document);
     List<CoreMap> sentences = document.get(SentencesAnnotation.class);
     for (CoreMap sentence : sentences) {
       int idx_t = 0;
       List<CoreLabel> candidate = new ArrayList<CoreLabel>();
       CoreLabel candidateID = new CoreLabel();
       for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
         String pos = token.get(PartOfSpeechAnnotation.class);
         idx_t++;
         if (pos.startsWith("NN") || pos.startsWith("JJ")) {
           candidate.add(token);
           if (pos.startsWith("NN") && idx_t == 1) {// && token.value().substring(0, 1).equals("P"))
                                                    // {
             candidateID = token;
             myID = new MyID(jcas, candidateID.beginPosition(), candidateID.endPosition());
             myID.addToIndexes();
             // System.out.println(":" + candidateID.value() + " " + candidateID.beginPosition() +
             // " "
             // + candidateID.endPosition());
             candidate.clear();
           }
         } else if (candidate.size() > 0) {
           int begin = candidate.get(0).beginPosition();
           int end = candidate.get(candidate.size() - 1).endPosition();
           begin2end.put(begin, end);
           // insert sentence id to CAS
           // for (int i = 0; i < candidate.size(); i++)
           // System.out.print(candidate.get(i).value() + " ");
           // System.out.print(begin + " " + end + " ");
 
           // insert noun phrase to CAS
           // myGene = new MyGene(jcas, begin, end);
           // myGene.addToIndexes();
           candidate.clear();
         }
       }
       if (candidate.size() > 0) {
         int begin = candidate.get(0).beginPosition();
         int end = candidate.get(candidate.size() - 1).endPosition();
         begin2end.put(begin, end);
         candidate.clear();
       }
     }
     return begin2end;
   }
 /**
  * annotate gene name and add to JCAS as a MyGene type
  * @author suyoun
  * @return void
  * @param text
  * @param noun
  */
   public void getGeneAnnot(String text, Map<Integer, Integer> noun) {
     for (Map.Entry<Integer, Integer> entry : noun.entrySet()) {
       String test;
       int begin = entry.getKey();
       int end = entry.getValue();
       test = text.substring(begin, end);
       // System.out.println("MAP:" + test );
 
       Chunking chunking = chunker.chunk(text.substring(begin, end));
       // System.out.println("Chunking=" + chunking);
       Set<Chunk> chunkset = chunking.chunkSet();
       Iterator it = chunkset.iterator();
       for (int n = 0; it.hasNext(); ++n) {
         Chunk chunk = (Chunk) it.next();
         int startc = chunk.start();
         int endc = chunk.end();
         String phrase = test.substring(startc, endc);
         // System.out.println("Gene:" + phrase);
 
         myGene = new MyGene(jcas, begin + startc, begin + endc);
         myGene.addToIndexes();
       }
     }
   }
 /**
  * annotator engine. call the NLP NER with input stream from JCAS parameter
  * @author suyoun
  * @return void
  * @param JCAS
  * 
  */
   public void process(JCas aJCas) throws AnalysisEngineProcessException {
     jcas = aJCas;
     input = jcas.getDocumentText();
 
     // sykim
     try {
       this.PosTagNamedEntityRecognizer();
     } catch (ResourceInitializationException e) {
       // TODO Auto-generated catch block
       e.printStackTrace();
     }
     // System.out.println(input);
     Map<Integer, Integer> nounPhrase = this.getNounPhrase(input);
 
     this.getGeneAnnot(input, nounPhrase);
     /**/
 
   }
 }
