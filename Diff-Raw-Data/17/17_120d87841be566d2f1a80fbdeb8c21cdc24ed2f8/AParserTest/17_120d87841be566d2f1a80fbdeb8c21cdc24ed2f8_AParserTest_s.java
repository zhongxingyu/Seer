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
 package org.paxle.parser.impl;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.Reader;
 import java.net.URI;
 import java.nio.charset.Charset;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 import org.jmock.integration.junit3.MockObjectTestCase;
 import org.paxle.core.doc.IParserDocument;
 import org.paxle.core.io.IOTools;
 import org.paxle.core.io.temp.ITempDir;
 import org.paxle.core.io.temp.ITempFileManager;
 import org.paxle.core.mimetype.IMimeTypeDetector;
 import org.paxle.core.norm.IReferenceNormalizer;
 import org.paxle.parser.ISubParser;
 import org.paxle.parser.ISubParserManager;
 import org.paxle.parser.ParserContext;
 
 
 public abstract class AParserTest extends MockObjectTestCase {
 	protected HashMap<String,String> fileNameToMimeTypeMap = null;
 	protected HashMap<String, ISubParser> mimeTypeToParserMap = null;
 	
 	protected ParserContext parserContext = null;
 	protected ITempFileManager aTempFileManager = null;
 	protected IReferenceNormalizer aRefNormalizer = null;
 	protected IMimeTypeDetector aMimetypeDetector = null;
 	protected ISubParserManager aSubParserManager = null;
 	
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		
 		this.fileNameToMimeTypeMap = new HashMap<String,String>();
 		this.mimeTypeToParserMap = new HashMap<String, ISubParser>();
 		
 		this.aTempFileManager = new ITempFileManager() {		
 			public void setTempDirFor(ITempDir arg0, String... arg1) { }		
 			public void removeTempDirFor(String... arg0) { }
 
 			public void releaseTempFile(File arg0) throws FileNotFoundException, IOException {
 				if (arg0 != null) arg0.delete();
 			}
 
 			public File createTempFile() throws IOException {
 				File tempfile = File.createTempFile("parserTest", ".tmp");
 				tempfile.deleteOnExit();
 				return tempfile;
 			}
 		};
 		IOTools.setTempFileManager(this.aTempFileManager);
 		
 		this.aRefNormalizer = new IReferenceNormalizer() {
 			public URI normalizeReference(String reference) {
 				return URI.create(reference);
 			}
 			public URI normalizeReference(String reference, Charset charset) {
 				return normalizeReference(reference);
 			}
 			public int getDefaultPort(String protocol) {
 				// TODO Auto-generated method stub
 				return 0;
 			}
 		};
 		
 		this.aMimetypeDetector = new IMimeTypeDetector() {
 			public String getMimeType(byte[] arg0, String fileName) throws Exception {
 				return fileNameToMimeTypeMap.get(fileName);
 			}
 			
 			public String getMimeType(File file) throws Exception {
 				return getMimeType(null, file.getName());
 			}
 		};
 		
 		this.aSubParserManager = new ISubParserManager() {			
 			public void disableMimeType(String arg0) {}			
 			public void enableMimeType(String arg0) {}
 			
 			public Set<String> disabledMimeTypes() {
 				return Collections.emptySet();
 			}
 			
 			public Collection<String> getMimeTypes() {
 				return mimeTypeToParserMap.keySet();
 			}
 			
 			public ISubParser getSubParser(String mimeType) {
 				return mimeTypeToParserMap.get(mimeType);
 			}
 			
 			public Map<String, ISubParser> getSubParsers() {
 				throw new RuntimeException("Not implemented");
 			}
 
 			public Collection<ISubParser> getSubParsers(String mimeType) {
 				ISubParser sp = this.getSubParser(mimeType);
 				if (sp == null) return Collections.emptyList();
 				return Arrays.asList(new ISubParser[]{sp});
 			}			
 			
 			public boolean isSupported(String mimeType) {
 				return true;
 			}
 			
 			public void disableParser(String service) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			public void enableParser(String service) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			public Set<String> enabledParsers() {
 				// TODO Auto-generated method stub
 				return null;
 			}
 			
 			public Map<String,Set<String>> getParsers() {
 				// TODO Auto-generated method stub
 				return null;
 			}
 			public void close() {
 				// TODO Auto-generated method stub
 				
 			}
 		};
 		
 		// create a parser context with a dummy temp-file-manager		
 		ParserContext.setThreadLocal(new ParserContextLocal() {{
 			this.subParserManager = aSubParserManager;
 			this.mimeTypeDetector = aMimetypeDetector;
 			this.charsetDetector = null;
 			this.tempFileManager = aTempFileManager;
 			this.referenceNormalizer = aRefNormalizer;
 		}});
 		this.parserContext = new ParserContext();
 	}
 	
 	protected void printParserDoc(final IParserDocument pdoc, final String name) throws IOException {
 		final Reader r = pdoc.getTextAsReader();
 		System.out.println(name);
 		if (r == null) {
 			System.out.println("null");
 			return;
 		}
 		final BufferedReader br = new BufferedReader(r);
 		try {
 			String line;
 			while ((line = br.readLine()) != null)
 				System.out.println(line);
 		} finally { br.close(); }
 		System.out.println();
 		System.out.println("-----------------------------------");
 		for (final Map.Entry<String,IParserDocument> sd : pdoc.getSubDocs().entrySet())
 			printParserDoc(sd.getValue(), sd.getKey());
 	}
 }
