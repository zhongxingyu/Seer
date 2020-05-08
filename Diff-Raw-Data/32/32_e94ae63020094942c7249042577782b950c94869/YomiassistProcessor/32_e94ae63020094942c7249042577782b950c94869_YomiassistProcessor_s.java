 /**
  * Copyright 2011 Richard North
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 package org.yomiassist;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.jdom.Element;
 import org.jdom.Text;
 import org.jdom.output.XMLOutputter;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.yomiassist.exception.YomiassistProcessingException;
 import org.yomiassist.exception.YomiassistSetupException;
 import org.yomiassist.sourcedata.Edict;
 import org.yomiassist.sourcedata.KnownWords;
 
 import com.google.common.base.Charsets;
 import com.google.common.io.Files;
 
 /**
  * Main text processor for Yomiassist. Parses, annotates and produces an
  * intermediate HTML form of the source text, ready for conversion to PDF.
  * 
  * @author Richard North <rich.north+yomiassist@gmail.com>
  * 
  */
 public class YomiassistProcessor {
 
 	
 	private static final Logger LOGGER = LoggerFactory.getLogger(YomiassistProcessor.class);
 
 	private final GoSenTextAnalyzer textAnalyzer;
 	private final KnownWords knownWords;
 	private final Edict edictDictionary;
 	private final Options options;
 
 	public YomiassistProcessor(Options options) throws YomiassistSetupException {
 		try {
 			this.options = options;
 			textAnalyzer = new GoSenTextAnalyzer();
 			knownWords = new KnownWords(options);
 			edictDictionary = new Edict();
 		} catch (IOException e) {
 			throw new YomiassistSetupException(e);
 		}
 	}
 	
 	/**
 	 * Process a source text.
 	 * 
 	 * @param options
 	 * @throws YomiassistProcessingException
 	 */
 	public void process() throws YomiassistProcessingException {
 		try {
 
 			final SourceDocument doc = new AozoraHtmlSourceDocument(options);
 			final String title = doc.getSourceDocumentTitle();
 			final String author = doc.getSourceDocumentAuthor();
 
 			LOGGER.info("About to process book {} by {}", title, author);
 
 			final List<Ruby> allRuby = new ArrayList<Ruby>();
 			final Map<String, Ruby> forcedRubies = new HashMap<String, Ruby>();
 			processSourceFile(doc, allRuby, forcedRubies, edictDictionary, knownWords);
 
 			writeOutputFile(options, title, author, allRuby);
 
 			LOGGER.info("Processing complete; {} ruby words identified with {} forced ruby readings", allRuby.size(), forcedRubies.size());
 
 		} catch (Exception e) {
 			LOGGER.error("An unexpected error occurred during processing: {}", e.getMessage());
 			throw new YomiassistProcessingException(e);
 		}
 	}
 
 	private void processSourceFile(SourceDocument doc, List<Ruby> allRuby, Map<String, Ruby> forcedRubies, Edict edictDictionary,
 			KnownWords knownWords) throws YomiassistProcessingException {
 
 		final List<?> childNodes = doc.getMainTextNode().getContent();
 		final Iterator<?> iterator = childNodes.iterator();
 
 		while (iterator.hasNext()) {
 			Object nextChild = iterator.next();
 
 			if (nextChild instanceof Text) {
 				final Text textNode = (Text) nextChild;
 				allRuby.addAll(textAnalyzer.analyzeText(textNode.getText(), forcedRubies, edictDictionary, knownWords));
 			} else if (nextChild instanceof Element) {
 				final Element elementNode = (Element) nextChild;
 
 				if (elementNode.getName().equals("ruby")) {
 					final Ruby forcedRuby = doc.copyForcedRuby(elementNode, edictDictionary);
 					forcedRubies.put(forcedRuby.getWrittenForm(), forcedRuby);
 					allRuby.add(forcedRuby);
 
 				} else {
 					allRuby.addAll(textAnalyzer.analyzeText(elementNode.getText(), forcedRubies, edictDictionary, knownWords));
 				}
 
 			}
 		}
 	}
 
 	private void writeOutputFile(final Options options, String title, String author, List<Ruby> allRuby) throws IOException {
 
 		options.outputFile.delete();
 		options.outputFile.createNewFile();
 
 		String headerContents = new String(Files.toByteArray(new File("templates/header.txt")));
 		headerContents = headerContents.replaceAll("%title%", title);
 		headerContents = headerContents.replaceAll("%author%", author);
 		Files.append(headerContents, options.outputFile, Charsets.UTF_8);
 
 		XMLOutputter xmlOutputter = new XMLOutputter();
 		for (Ruby r : allRuby) {
 			Files.append(xmlOutputter.outputString(r.toNode()) + '\n', options.outputFile, Charsets.UTF_8);
 		}
 
 		Files.append(new String(Files.toByteArray(new File("templates/footer.txt"))), options.outputFile, Charsets.UTF_8);
 	}
 
 	
 
 }
