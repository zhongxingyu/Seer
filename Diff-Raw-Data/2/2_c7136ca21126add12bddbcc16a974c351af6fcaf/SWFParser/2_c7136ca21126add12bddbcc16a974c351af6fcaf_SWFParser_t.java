 
 package org.paxle.parser.swf.impl;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.URI;
 import java.nio.ByteBuffer;
 import java.nio.charset.Charset;
 import java.util.Arrays;
 import java.util.List;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.karlchenofhell.swf.TextExtractorTagFactory;
 import org.karlchenofhell.swf.TextSink;
 import org.karlchenofhell.swf.parser.SWFTagReader;
 import org.paxle.core.doc.IParserDocument;
 import org.paxle.core.doc.ParserDocument;
 import org.paxle.core.io.IOTools;
 import org.paxle.parser.ASubParser;
 import org.paxle.parser.ISubParser;
 import org.paxle.parser.ParserContext;
 import org.paxle.parser.ParserException;
 import org.paxle.parser.swf.ISWFParser;
 
 public class SWFParser extends ASubParser implements ISWFParser {
 	
 	private static final List<String> MIME_TYPES = Arrays.asList("application/x-shockwave-flash");
 	
 	private static final Charset UTF8 = Charset.forName("UTF-8");
 	
 	private final Log logger = LogFactory.getLog(SWFParser.class);
 	
 	public List<String> getMimeTypes() {
 		return MIME_TYPES;
 	}
 	
 	@Override
 	public IParserDocument parse(final URI location, String charset, InputStream is)
 			throws ParserException, UnsupportedEncodingException, IOException {
 		
 		final IParserDocument pdoc = new ParserDocument();
 		
 		final ParserContext context = ParserContext.getCurrentContext();
 		
 		final class SwfTextSink implements TextSink {
 			
 			Exception ex;
 			
 			public void addText(String text, boolean isHTML) {
 				try {
 					if (isHTML) {
						final ISubParser htmlParser = context.getParser("text/html");
 						if (htmlParser == null) {
 							logger.warn("Cannot parse HTML content of SWF-file due to missing HTML-parser");
 						} else {
 							final ByteBuffer bb = UTF8.encode(text);
 							final IParserDocument htmlParserDoc = htmlParser.parse(
 									location,
 									UTF8.name(),
 									new ByteArrayInputStream(bb.array(), 0, bb.limit()));
 							if (htmlParserDoc.getStatus() != IParserDocument.Status.OK) {
 								logger.warn("Failed parsing HTML-content of SWF-file from '" + location + "'");
 							} else {
 								final StringBuilder sb = new StringBuilder();
 								IOTools.copy(htmlParserDoc.getTextAsReader(), sb);
 								pdoc.addText(sb);
 							}
 						}
 					} else {
 						pdoc.addText(text);
 					}
 				} catch (Exception e) { ex = e; }
 			}
 		}
 		
 		final SwfTextSink sink = new SwfTextSink();
 		final TextExtractorTagFactory tetf = new TextExtractorTagFactory(sink);
 		try {
 			SWFTagReader.processAll(location.toString(), is, tetf);
 			if (sink.ex != null)
 				throw new ParserException("error parsing '" + location + "'", sink.ex);
 		} finally { pdoc.close(); }
 		pdoc.setStatus(IParserDocument.Status.OK);
 		return pdoc;
 	}
 }
