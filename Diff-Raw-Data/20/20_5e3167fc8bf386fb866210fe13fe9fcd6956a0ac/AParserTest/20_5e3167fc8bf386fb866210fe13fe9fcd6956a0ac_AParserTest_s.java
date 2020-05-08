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
 	protected ITempFileManager tempFileManager = null;
 	protected IReferenceNormalizer refNormalizer = null;
 	protected IMimeTypeDetector mimetypeDetector = null;
 	protected ISubParserManager subParserManager = null;
 	
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		
 		this.fileNameToMimeTypeMap = new HashMap<String,String>();
 		this.mimeTypeToParserMap = new HashMap<String, ISubParser>();
 		
 		this.tempFileManager = new ITempFileManager() {		
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
 		
 		this.refNormalizer = new IReferenceNormalizer() {
 			public URI normalizeReference(String reference) {
 				return URI.create(reference);
 			}
 			public URI normalizeReference(String reference, Charset charset) {
 				return normalizeReference(reference);
 			}
 		};
 		
 		this.mimetypeDetector = new IMimeTypeDetector() {
 			public String getMimeType(byte[] arg0, String fileName) throws Exception {
 				return fileNameToMimeTypeMap.get(fileName);
 			}
 			
 			public String getMimeType(File file) throws Exception {
 				return getMimeType(null, file.getName());
 			}
 		};
 		
 		this.subParserManager = new ISubParserManager() {			
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
 			
 			public Collection<ISubParser> getSubParsers() {
 				return mimeTypeToParserMap.values();
 			}
 
 			public Collection<ISubParser> getSubParsers(String mimeType) {
 				ISubParser sp = this.getSubParser(mimeType);
 				if (sp == null) return Collections.emptyList();
 				return Arrays.asList(new ISubParser[]{sp});
 			}			
 			
 			public boolean isSupported(String mimeType) {
 				return true;
 			}
 		};
 		
 		// create a parser context with a dummy temp-file-manager
 		this.parserContext = new ParserContext(
 				this.subParserManager,
 				this.mimetypeDetector,
 				null, 
 				this.tempFileManager, 
 				this.refNormalizer
 		);
 		ParserContext.setCurrentContext(this.parserContext);		
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
