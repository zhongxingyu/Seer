 /**
  * This file is part of the Paxle project.
  * Visit http://www.paxle.net for more information.
  * Copyright 2007-2009 the original author or authors.
  *
  * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0").
  * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
  * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt
  * or in the file LICENSE.txt in the root directory of the Paxle distribution.
  *
  * Unless required by applicable law or agreed to in writing, this software is distributed
  * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  */
 package org.paxle.parser.pdf.impl;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.io.Writer;
 import java.net.URI;
 import java.nio.CharBuffer;
 import java.util.Arrays;
 import java.util.Calendar;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.felix.scr.annotations.Component;
 import org.apache.felix.scr.annotations.Property;
 import org.apache.felix.scr.annotations.Service;
import org.paxle.core.doc.ICommandProfile;
 import org.paxle.core.doc.IParserDocument;
import org.paxle.parser.IParserContext;
 import org.paxle.parser.ISubParser;
 import org.paxle.parser.ParserContext;
 import org.paxle.parser.ParserException;
 import org.pdfbox.pdfparser.PDFParser;
 import org.pdfbox.pdmodel.PDDocument;
 import org.pdfbox.pdmodel.PDDocumentInformation;
 import org.pdfbox.pdmodel.encryption.AccessPermission;
 import org.pdfbox.pdmodel.encryption.StandardDecryptionMaterial;
 import org.pdfbox.util.PDFTextStripper;
 
 @Component(metatype=false)
 @Service(ISubParser.class)
 @Property(name=ISubParser.PROP_MIMETYPES, value={"application/pdf"})
 public class PdfParser implements ISubParser {
 	/**
 	 * For logging
 	 */
 	private final Log logger = LogFactory.getLog(this.getClass());	
 
 	public IParserDocument parse(URI location, String charset, InputStream fileIn) throws ParserException, UnsupportedEncodingException, IOException {
 		IParserDocument parserDoc = null;
 		PDDocument pddDoc = null;
 		
 		try {
			final IParserContext pc = ParserContext.getCurrentContext();
			final ICommandProfile cmdProfile = pc.getCommandProfile();
			
			// create an empty document			
			parserDoc = pc.createDocument();
 			
 			// parse it
 			final PDFParser parser = new PDFParser(fileIn);
 			parser.parse();
 			pddDoc = parser.getPDDocument();
 			
 			// check document encryption
 			if (pddDoc.isEncrypted()) {
 				if (this.logger.isDebugEnabled()) {
 					this.logger.debug(String.format(
 						"Document '%s' is encrypted."
 					));
 				}
 				
				// determine the decryption password
				String pwd = "";
				if (cmdProfile != null) {
					String tmp = (String) cmdProfile.getProperty("org.paxle.parser.pdf.impl.decryptionPassword");
					if (tmp != null) pwd = tmp;
				}				
				
				// try to open document with the given password
 				try {
					final StandardDecryptionMaterial dm = new StandardDecryptionMaterial(pwd);
 					pddDoc.openProtection(dm);
 					final AccessPermission accessPermission = pddDoc.getCurrentAccessPermission();
 					
 					if (accessPermission == null || !accessPermission.canExtractContent()) {
 						if (this.logger.isInfoEnabled()) {
 							this.logger.debug(String.format(
 								"No permission to extract content of document '%s'."
 							));						
 						}
 						parserDoc.setStatus(IParserDocument.Status.FAILURE,"PDF Document is encrypted.");
 						return parserDoc;
 					}
 				} catch (Throwable e) {
 					this.logger.error(String.format("Unable to decrypt document '%s'.",location),e);
 					parserDoc.setStatus(IParserDocument.Status.FAILURE,String.format("Unable to decrypt document. %s: %s", e.getClass().getName(),e.getMessage()));
 					return parserDoc;
 				}
 			}
 			
 			// extract metadata
 			final PDDocumentInformation metadata = pddDoc.getDocumentInformation();
 			if (metadata != null) {
 				// document title
 				final String title = metadata.getTitle();
 				if (title != null && title.length() > 0) parserDoc.setTitle(title);
 				
 				// document author(s)
 				final String author = metadata.getAuthor();
 				if (author != null && author.length() > 0) parserDoc.setAuthor(author);;
 				
 				// subject
 				final String summary = metadata.getSubject();
 				if (summary != null && summary.length() > 0) parserDoc.setSummary(summary);
 				
 				// keywords
 				final String keywords = metadata.getKeywords();
 				if (keywords != null && keywords.length() > 0) {
 					String[] keywordArray = keywords.split("[,;\\s]");
 					if (keywordArray != null && keywordArray.length > 0) {
 						parserDoc.setKeywords(Arrays.asList(keywordArray));
 					}
 				}
 				
 				// last modification date
 				final Calendar lastMod = metadata.getModificationDate();
 				if (lastMod != null) {
 					parserDoc.setLastChanged(lastMod.getTime());
 				}
 			}
 			
 			// init text stripper
 			final PDFTextStripper stripper = new PDFTextStripper();
 			final AppenderWriter pdocWrapper = new AppenderWriter(parserDoc);
 			stripper.writeText(pddDoc, pdocWrapper);
 			
 			parserDoc.setStatus(IParserDocument.Status.OK);
 			return parserDoc;
 		} catch (Throwable e) {
 			throw new ParserException("Error parsing pdf document. " + e.getMessage(), e);
 		} finally {
 			if (pddDoc != null) try { pddDoc.close(); } catch (Exception e) {/* ignore this */}
 		}
 	}
 	
 	private static class AppenderWriter extends Writer {
 		
 		private final IParserDocument pdoc;
 		
 		public AppenderWriter(final IParserDocument pdoc) {
 			this.pdoc = pdoc;
 		}
 		
 		@Override
 		public void write(int c) throws IOException {
 			pdoc.append((char)(c & 0xFFFF));
 		}
 		
 		@Override
 		public void write(char[] cbuf, int off, int len) throws IOException {
 			pdoc.append(CharBuffer.wrap(cbuf, off, len));
 		}
 		
 		@Override
 		public Writer append(char c) throws IOException {
 			pdoc.append(c);
 			return this;
 		}
 		
 		@Override
 		public Writer append(CharSequence csq) throws IOException {
 			pdoc.append(csq);
 			return this;
 		}
 		
 		@Override
 		public Writer append(CharSequence csq, int start, int end) throws IOException {
 			pdoc.append(csq, start, end);
 			return this;
 		}
 		
 		@Override
 		public void write(String str, int off, int len) throws IOException {
 			pdoc.append(str, off, off + len);
 		}
 		
 		@Override
 		public void close() throws IOException {
 			// ignore
 		}
 		
 		@Override
 		public void flush() throws IOException {
 			// ignore
 		}
 	}
 	
 	public IParserDocument parse(URI location, String charset, File content) throws ParserException, UnsupportedEncodingException, IOException {
 		InputStream fileIn = null;
 		try {
 			// open file
 			fileIn = new BufferedInputStream(new FileInputStream(content));
 			return parse(location, charset, fileIn);
 		} finally {
 			if (fileIn != null) try { fileIn.close(); } catch (Exception e) {/* ignore this */}
 		}
 	}
 	
 }
