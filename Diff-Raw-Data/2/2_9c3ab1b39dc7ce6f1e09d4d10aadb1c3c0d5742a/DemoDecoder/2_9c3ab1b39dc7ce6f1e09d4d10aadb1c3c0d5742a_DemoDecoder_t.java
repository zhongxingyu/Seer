 /**
  * Copyright 2012 University of Massachusetts Amherst
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  *   
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
 */
 package org.ets.nlp;
 
 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.io.InputStream;
 import java.io.PrintStream;
 import java.util.List;
 
 import com.googlecode.clearnlp.component.AbstractComponent;
 import com.googlecode.clearnlp.dependency.DEPTree;
 import com.googlecode.clearnlp.engine.EngineGetter;
 import com.googlecode.clearnlp.nlp.NLPDecode;
 import com.googlecode.clearnlp.nlp.NLPLib;
 import com.googlecode.clearnlp.reader.AbstractReader;
 import com.googlecode.clearnlp.segmentation.AbstractSegmenter;
 import com.googlecode.clearnlp.tokenization.AbstractTokenizer;
 import com.googlecode.clearnlp.util.UTInput;
 import com.googlecode.clearnlp.util.UTOutput;
 
 /**
  * @since 1.1.0
  * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
  * @since xxxxx
  * @author Chris Brew (@code christopher.brew@gmail.com)
  * changes so that model comes from uber jar. 
  */
 public class DemoDecoder
 {
         final String language = AbstractReader.LANG_EN;
         
         public DemoDecoder(InputStream dictStream, 
                         InputStream posModelStream, 
                         InputStream morphStream,
                         InputStream depModelStream,
                         InputStream predModelStream, 
                         InputStream roleModelStream, 
                         InputStream srlModelStream, String inputFile, String outputFile) throws Exception
         {
                 AbstractTokenizer tokenizer  = EngineGetter.getTokenizer(language, dictStream);
                 AbstractComponent tagger     = EngineGetter.getComponent(posModelStream, language, NLPLib.MODE_POS);
                 AbstractComponent analyzer   = EngineGetter.getComponent(morphStream, language, NLPLib.MODE_MORPH);
                 AbstractComponent parser     = EngineGetter.getComponent(depModelStream, language, NLPLib.MODE_DEP);
                 AbstractComponent identifier = EngineGetter.getComponent(predModelStream, language, NLPLib.MODE_PRED);
                 AbstractComponent classifier = EngineGetter.getComponent(roleModelStream, language, NLPLib.MODE_ROLE);
                 AbstractComponent labeler    = EngineGetter.getComponent(srlModelStream , language, NLPLib.MODE_SRL);
                 
                 AbstractComponent[] components = {tagger, analyzer, parser, identifier, classifier, labeler};
                 
                 String sentence = "I'd like to meet Dr. Choi.";
                 process(tokenizer, components, sentence);
                 process(tokenizer, components, UTInput.createBufferedFileReader(inputFile), UTOutput.createPrintBufferedFileStream(outputFile));
         }
         
         public void process(AbstractTokenizer tokenizer, AbstractComponent[] components, String sentence)
         {
                 NLPDecode nlp = new NLPDecode();
                 DEPTree tree = nlp.toDEPTree(tokenizer.getTokens(sentence));
                 
                 for (AbstractComponent component : components)
                         component.process(tree);
 
                 System.out.println(tree.toStringSRL()+"\n");
         }
         
         public void process(AbstractTokenizer tokenizer, AbstractComponent[] components, BufferedReader reader, PrintStream fout)
         {
                 AbstractSegmenter segmenter = EngineGetter.getSegmenter(language, tokenizer);
                 NLPDecode nlp = new NLPDecode();
                 DEPTree tree;
                 
                 for (List<String> tokens : segmenter.getSentences(reader))
                 {
                         tree = nlp.toDEPTree(tokens);
                         
                         for (AbstractComponent component : components)
                                 component.process(tree);
                         
                         fout.println(tree.toStringSRL()+"\n");
                 }
                 
                 fout.close();
         }
 
         public static void main(String[] args)
         {
                 InputStream dictStream      = DemoDecoder.class.getResourceAsStream("/dictionary-1.4.0.zip");
                 InputStream morphStream      = DemoDecoder.class.getResourceAsStream("/dictionary-1.4.0.zip");
                 InputStream posModelStream = DemoDecoder.class.getResourceAsStream("/ontonotes-en-pos-1.4.0.tgz"); 
                 InputStream depModelStream  = DemoDecoder.class.getResourceAsStream("/ontonotes-en-dep-1.4.0.tgz");
                 InputStream predModelStream = DemoDecoder.class.getResourceAsStream("/ontonotes-en-pred-1.4.0.tgz");
                 InputStream roleModelStream = DemoDecoder.class.getResourceAsStream("/ontonotes-en-role-1.4.0.tgz");
                InputStream srlModelStream  = DemoDecoder.class.getResourceAsStream("/ontonotes-en-srl-1.4.2.tgz");
                 String inputFile     = args[0];
                 String outputFile    = args[1];
 
                 try
                 {
                         new DemoDecoder(dictStream, posModelStream, 
                                         morphStream, depModelStream, predModelStream, roleModelStream, srlModelStream, 
                                                 inputFile, outputFile);
                 }
                 catch (Exception e) {e.printStackTrace();}
         }
 }
