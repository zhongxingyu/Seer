 /**
  * This file is part of the Paxle project.
  * Visit http://www.paxle.net for more information.
  * Copyright 2007-2008 the original author or authors.
  * 
  * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0"). 
  * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
  * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt 
  * or in the file LICENSE.txt in the root directory of the Paxle distribution.
  * 
  * Unless required by applicable law or agreed to in writing, this software is distributed
  * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  */
 
 package org.paxle.filter.languageidentification.impl;
 
 import java.io.BufferedInputStream;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.nio.charset.Charset;
 import java.util.HashSet;
 import java.util.Map;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.paxle.core.doc.IParserDocument;
 import org.paxle.core.filter.IFilter;
 import org.paxle.core.filter.IFilterContext;
 import org.paxle.core.queue.ICommand;
 
 import de.spieleck.app.cngram.NGramProfiles;
 
 /**
  * This helper class determines the language of a document and inserts its finding into a parser-doc and all of its subdocs
  */
 public class LanguageManager implements IFilter<ICommand> {
 
 	private Log logger = LogFactory.getLog(this.getClass());
 	NGramProfiles nps = null;
	
 	public LanguageManager() throws IOException  {
 		this.nps = new NGramProfiles();
 	}
	
 	/**
 	 * Sets the language for the given ParserDocument and all its sub pDocs
 	 * @param parserDoc
 	 */
 	private void getLanguage(IParserDocument parserDoc) {
 
 		Map<String,IParserDocument> subDocs = parserDoc.getSubDocs();
 		if (subDocs != null) {
 			for (IParserDocument subDoc : subDocs.values()) {
 				this.getLanguage(subDoc);
 			}
 		}
 
 		double start = System.currentTimeMillis();
 
 		NGramProfiles.RankResult res = null;
 
 		InputStreamReader isr = null;
 		try {
 			if (parserDoc.getTextFile() != null) {
 
 				NGramProfiles.Ranker ranker = nps.getRanker();
				
 				Charset pdoccs = parserDoc.getCharset();
 				if (pdoccs == null) pdoccs = Charset.forName("UTF-8"); //try to read pdocs without encoding as UTF-8
				
 				isr = new InputStreamReader(new BufferedInputStream(new FileInputStream(parserDoc.getTextFile())), pdoccs);
 				ranker.account(isr);
 				res = ranker.getRankResult();
 
 			} else {
 				logger.info("No language for document '" + parserDoc.getOID() + "', as it contains no text");
 			}
 		} catch (IOException e) {
 			logger.warn("Exception while trying to determine language of document '" +  parserDoc.getOID() + "'", e);
 		} finally {
 			try {
 				isr.close();
			} catch (IOException e) {/* ignore */ }
 		}
 
 		double end = System.currentTimeMillis();
 
 		logger.debug("Language detection took " + (end - start) + "ms");
 
 		HashSet<String> lngs = null;
		if (res != null && res.getScore(0) > 0.6) {
			logger.debug("Primary language of document '" + parserDoc.getOID() + "' is: " + res.getName(0) + ", " + res.getScore(0));
 			lngs = new HashSet<String>(1);
			lngs.add(res.getName(0));
 			parserDoc.setLanguages(lngs);
 		} else {
 			logger.debug("Primary language of document '" + parserDoc.getOID() + "' is unknown");
 			parserDoc.setLanguages(lngs); //set to null
 		}
 	}
 
 	/**
 	 * Inserts the ISO 639-1 code of the primary document language into the parser- and recursively all subparser docs
 	 */
 	public void filter(ICommand command, IFilterContext context) {
 		if (command == null) throw new NullPointerException("The command object is null.");
 
 		if (command.getResult() != ICommand.Result.Passed) {
 			logger.debug("Command didn't pass, aborting language detection.");
 			return;
 		}
 
 		IParserDocument pdoc = command.getParserDocument();
 		if (pdoc == null) {
 			this.logger.debug(String.format(
 					"No language detection possible for command '%s'. pdoc was null.",
 					command.getLocation().toASCIIString()
 			));
 			return;
 		} else if (pdoc.getStatus() != IParserDocument.Status.OK) {
 			logger.debug(String.format(
 					"Language of pDoc '%d' can't be determined. pDoc status was '%s': %s",
 					new Integer(pdoc.getOID()),
 					pdoc.getStatus().toString(),
 					pdoc.getStatusText()
 			));
 			return;
 		}
 
 		getLanguage(command.getParserDocument());
 
 	}
 
 }
