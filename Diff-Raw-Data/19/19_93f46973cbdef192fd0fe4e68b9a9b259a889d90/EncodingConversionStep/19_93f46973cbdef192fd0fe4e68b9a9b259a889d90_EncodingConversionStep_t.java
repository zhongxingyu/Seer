 /*===========================================================================
   Copyright (C) 2009 by the Okapi Framework contributors
 -----------------------------------------------------------------------------
   This library is free software; you can redistribute it and/or modify it 
   under the terms of the GNU Lesser General Public License as published by 
   the Free Software Foundation; either version 2.1 of the License, or (at 
   your option) any later version.
 
   This library is distributed in the hope that it will be useful, but 
   WITHOUT ANY WARRANTY; without even the implied warranty of 
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser 
   General Public License for more details.
 
   You should have received a copy of the GNU Lesser General Public License 
   along with this library; if not, write to the Free Software Foundation, 
   Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 
   See also the full LGPL text here: http://www.gnu.org/copyleft/lesser.html
 ===========================================================================*/
 
 package net.sf.okapi.steps.encodingconversion;
 
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStreamWriter;
 import java.nio.ByteBuffer;
 import java.nio.CharBuffer;
 import java.nio.charset.Charset;
 import java.nio.charset.CharsetEncoder;
 import java.util.Hashtable;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.sf.okapi.common.BOMNewlineEncodingDetector;
 import net.sf.okapi.common.Event;
 import net.sf.okapi.common.IParameters;
 import net.sf.okapi.common.Util;
 import net.sf.okapi.common.exceptions.OkapiIOException;
 import net.sf.okapi.common.pipeline.BasePipelineStep;
 import net.sf.okapi.common.pipelinedriver.PipelineContext;
 import net.sf.okapi.common.resource.RawDocument;
 
 public class EncodingConversionStep extends BasePipelineStep {
 
 	private final Logger logger = Logger.getLogger(getClass().getName());
 	private static final int MAXBUF = 1024;
 
 	private Parameters params;
 	private String outFormat;
 	private CharsetEncoder outputEncoder;
 	private boolean useCER;
 	private Hashtable<String, Character> charEntities;
 	private CharBuffer buffer;
 	private Pattern pattern;
 	private Pattern xmlEncDecl;
 	private Pattern xmlDecl;
 	private Pattern htmlEncDecl;
 	private Pattern htmlDecl;
 	private Pattern htmlHead;
 	private String prevBuf;
 	private boolean isXML;
 	private boolean isHTML;
 	private boolean isDone;
 
 	public EncodingConversionStep () {
 		params = new Parameters();
 	}
 
 	@Override
 	/**
 	 * FIXME: Steps should only depend on the IPipeline, IPipelineStep and IContext interfaces. 
 	 * This step depends on the pipeline driver project. 
 	 */
 	public PipelineContext getContext() {		
 		return (PipelineContext)super.getContext();
 	}
 
 	public String getDescription () {
 		return "Convert the character set encoding of a text-based file.";
 	}
 
 	public String getName () {
 		return "Encoding Conversion";
 	}
 
 	@Override
 	public boolean isDone () {
 		return isDone;
 	}
 
 	@Override
 	public IParameters getParameters () {
 		return params;
 	}
 
 	@Override
 	public void setParameters (IParameters params) {
 		params = (Parameters)params;
 	}
 
 	@Override
 	public boolean needsOutput (int inputIndex) {
 		return isLastStep();
 	}
 
 	@Override
 	protected void handleStartBatch (Event event) {
 		buffer = CharBuffer.allocate(MAXBUF);
 		// Pre-compile the patterns for declaration detection
 		xmlEncDecl = Pattern.compile("((<\\?xml)(.*?)(encoding(\\s*?)=(\\s*?)(\\'|\\\")))", Pattern.DOTALL);
 		xmlDecl = Pattern.compile("((<\\?xml)(.*?)(version(\\s*?)=(\\s*?)(\\'|\\\")))", Pattern.DOTALL);
 		htmlEncDecl = Pattern.compile("(<meta)([^>]*?)(content)(\\s*?)=(\\s*?)[\\'|\\\"](\\s*?)text/html(\\s*?);(\\s*?)charset(\\s*?)=(\\s*?)([^\\s]+?)(\\s|\\\"|\\')",
 			Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
 		htmlDecl = Pattern.compile("(<html)", Pattern.CASE_INSENSITIVE);
 		htmlHead = Pattern.compile("<head>", Pattern.CASE_INSENSITIVE);
 		
 		// Pre-compile pattern for un-escaping
 		String tmp = "";
 		if ( params.unescapeNCR ) {
 			tmp += "&#([0-9]*?);|&#[xX]([0-9a-fA-F]*?);";
 		}
 		if ( params.unescapeCER ) {
 			if ( tmp.length() > 0 ) tmp += "|";
 			tmp += "(&\\w*?;)";
 		}
 		if ( params.unescapeJava ) {
 			if ( tmp.length() > 0 ) tmp += "|";
 			tmp += "(\\\\[Uu]([0-9a-fA-F]{1,4}))";
 		}
 		if ( tmp.length() > 0 ) {
 			pattern = Pattern.compile(tmp, Pattern.CASE_INSENSITIVE);
 			if ( charEntities == null ) createCharEntitiesTable();
 		}
 		else pattern = null;
         		
 		useCER = false;
 		switch ( params.escapeNotation ) {
 		case Parameters.ESCAPE_CER:
 			useCER = true;
 			outFormat = "&#x%X;"; // Here outFormat is used only if no CER can be used
 			break;
 		case Parameters.ESCAPE_JAVAL:
 			outFormat = "\\u%04x";
 			break;
 		case Parameters.ESCAPE_JAVAU:
 			outFormat = "\\u%04X";
 			break;
 		case Parameters.ESCAPE_NCRDECI:
 			outFormat = "&#%d;";
 			break;
 		case Parameters.ESCAPE_NCRHEXAL:
 			outFormat = "&#x%x;";
 			break;
 		case Parameters.ESCAPE_USERFORMAT:
 			outFormat = params.userFormat;
 			break;
 		case Parameters.ESCAPE_NCRHEXAU:
 		default:
 			outFormat = "&#x%X;";
 			break;
 		}
 	}
 	
 	@Override
 	protected void handleStartBatchItem (Event event) {
 		isDone = false;
 	}
 
 	@Override
 	protected void handleRawDocument (Event event) {
 		RawDocument rawDoc = (RawDocument)event.getResource();
 		BufferedReader reader = null;
 		OutputStreamWriter writer = null;
 		try {
 			// Try to detect the type of file from extension
 			isXML = false;
 			isHTML = false;
 			String ext = Util.getExtension(getContext().getRawDocument(0).getInputURI().getPath());
			if ( !Util.isEmpty(ext) ) {
 				isHTML = (ext.toLowerCase().indexOf(".htm")==0);
 				isXML = ext.equalsIgnoreCase(".xml");
 			}
 			
 			String outputEncoding = getContext().getOutputEncoding(0);
 			
 			//=== Try to detect the encoding
 			
 			InputStream is = rawDoc.getStream();
 			if ( is.markSupported() ) {
 				// First: guess from a possible BOM
 				BOMNewlineEncodingDetector detector = new BOMNewlineEncodingDetector(is, rawDoc.getEncoding());
 				detector.detectAndRemoveBom();
 				rawDoc.setEncoding(detector.getEncoding());
 				
 				// Then try to detect internal declaration
 				
 //				is.mark(MAXBUF);
 				
 				
 			}
 			
 			// Then try internal detection for XML/HTML type files
 			reader = new BufferedReader(rawDoc.getReader());
 			String inputEncoding = rawDoc.getEncoding();
 			reader.read(buffer);
 			String detectedEncoding = checkDeclaration(inputEncoding);
 			if ( !detectedEncoding.equalsIgnoreCase(inputEncoding) ) {
 				inputEncoding = detectedEncoding;
 			}
 			reader.close();
 
 			// Open the input document 
 			//TODO: Where did we reset the reader - cann't call this twice unless we reset it
 			reader = new BufferedReader(rawDoc.getReader());
 			logger.info("Input encoding: " + inputEncoding);
 			
 			// Open the output document
 			File outFile;
 			if ( isLastStep() ) {
 				outFile = new File(getContext().getOutputURI(0));
 				Util.createDirectories(outFile.getAbsolutePath());
 			}
 			else {
 				try {
 					outFile = File.createTempFile("okp-enc_", ".tmp");
 				}
 				catch ( Throwable e ) {
 					throw new OkapiIOException("Cannot create temporary output.", e);
 				}
 				outFile.deleteOnExit();
 			}
 			writer = new OutputStreamWriter(new BufferedOutputStream(
 				new FileOutputStream(outFile)), outputEncoding);
 			outputEncoder = Charset.forName(outputEncoding).newEncoder();
 			logger.info("Output encoding: " + outputEncoding);
 			Util.writeBOMIfNeeded(writer, params.BOMonUTF8, outputEncoding);
 			
 			int n;
 			CharBuffer tmpBuf = CharBuffer.allocate(1);
 			ByteBuffer encBuf;
 			boolean canEncode;
 			boolean checkDeclaration = true;
 
 			while ( true ) {
 				buffer.clear();
 				// Start with previous buffer remains if needed
 				if ( prevBuf != null ) {
 					buffer.append(prevBuf);
 				}
 				// Read the next block
 				n = reader.read(buffer);
 				// Check if we need to stop here
 				boolean needSplitCheck = true;
 				if ( n == -1 ) {
 					// Make sure we do not start an endless loop by
 					// re-checking the last previous buffer
 					if ( prevBuf != null ) {
 						needSplitCheck = false;
 						prevBuf = null;
 						buffer.limit(buffer.position());
 					}
 					else break; // No previous, no read: Done
 				}
 				
 				if ( checkDeclaration ) {
 					checkDeclaration(inputEncoding);
 					checkDeclaration = false;
 				}
 
 				// Un-escape if requested
 				if ( pattern != null ) {
 					if ( needSplitCheck ) checkSplitSequence();
 					unescape();
 				}
 				
 				// Output
 				n = buffer.position();
 				buffer.position(0);
 				for ( int i=0; i<n; i++ ) {
 					if ( !(canEncode = outputEncoder.canEncode(buffer.get(i))) ) {
 						if ( params.reportUnsupported ) {
 							logger.warning(String.format("Un-supported character: U+%04X ('%c')",
 								(int)buffer.get(i), buffer.get(i)));
 						}
 					}
 					
 					if (( params.escapeAll && ( buffer.get(i) > 127 )) || !canEncode ) {
 						boolean fallBack = false;
 						// Write escape form
 						if ( useCER ) {
 							String tmp = findCER(buffer.get(i));
 							if ( tmp == null ) fallBack = true;
 							else writer.write("&"+tmp+";");
 						}
 						else {
 							if ( params.useBytes ) { // Escape bytes
 								if ( canEncode ) {
 									tmpBuf.put(0, buffer.get(i));
 									tmpBuf.position(0);
 									encBuf = outputEncoder.encode(tmpBuf);
 									for ( int j=0; j<encBuf.limit(); j++ ) {
 										writer.write(String.format(outFormat,
 											(encBuf.get(j)<0 ? (0xFF^~encBuf.get(j)) : encBuf.get(j)) ));
 									}
 								}
 								else fallBack = true;
 							}
 							else { // Escape character
 								writer.write(String.format(outFormat, (int)buffer.get(i)));
 							}
 						}
 						if ( fallBack ) { // Default escaping when nothing else works
 							writer.write(String.format("&#x%X;", (int)buffer.get(i)));
 						}
 					}
 					else { // Normal raw forms
 						writer.write(buffer.get(i));
 					}
 				}
 			}
 			
 			// Done: close the output
 			writer.close();
 			// Set the new raw-document URI and the encoding (in case one was auto-detected)
 			// Other info stays the same
 			RawDocument newDoc = new RawDocument(outFile.toURI(), outputEncoding,
 				rawDoc.getSourceLanguage(), rawDoc.getTargetLanguage());
 			event.setResource(newDoc);
 			
 		}
 		catch ( FileNotFoundException e) {
 			throw new RuntimeException(e);
 		}
 		catch ( IOException e) {
 			throw new RuntimeException(e);
 		}
 		finally {
 			isDone = true;
 			try {
 				if ( writer != null ) {
 					writer.close();
 					writer = null;
 				}
 				if ( reader != null ) {
 					reader.close();
 					reader = null;
 				}
 			}
 			catch ( IOException e) {
 				throw new RuntimeException(e);
 			}
 		}
 	}
 
 	private String checkDeclaration (String defEncoding) {
 		// Convert the CharBuffer to a string
 		buffer.limit(buffer.position());
 		buffer.position(0);
 		StringBuffer text = new StringBuffer(buffer.toString());
 		String outputEncoding = getContext().getOutputEncoding(0);
 		
 		// Look for XML encoding declaration
 		String encoding = defEncoding;
 		Matcher m = xmlEncDecl.matcher(text);
 		if ( m.find() ) { // We have an XML encoding declaration
 			isXML = true;
 			// Get the declared encoding
 			String delim = String.valueOf(text.charAt(m.end()-1));
 			int end = text.indexOf(delim, m.end());
 			if ( end != -1 ) {
 				encoding = text.substring(m.end(), end);
 				// End replace the current declaration by the new one
 				text.replace(m.end(), end, outputEncoding);
 			}
 		}
 		else { // No XML encoding declaration found: Check if it is XML
 			m = xmlDecl.matcher(text);
 			if ( m.find() ) { // It is XML without encoding declaration
 				isXML = true;
 				// Encoding should UTF-8 or UTF-16/32, we will detect those later
 				encoding = "UTF-8";
 				// Add the encoding after the version
 				String delim = String.valueOf(text.charAt(m.end()-1));
 				int end = text.indexOf(delim, m.end());
 				if ( end != -1 ) {
 					text.insert(end+1, " encoding=\""+outputEncoding+"\"");
 				}
 			}
 			else { // No XML declaration found, maybe it's an XML without one
 				if ( isXML ) { // Was a .xml extension, assume UTF-8
 					encoding = "UTF-8";
 					text.insert(0, "<?xml version=\"1.0\" encoding=\""+outputEncoding+"\" ?>");
 				}
 			}
 		}
 
 		// Look for HTML declarations
 		m = htmlEncDecl.matcher(text);
 		if ( m.find() ) {
 			isHTML = true;
 			// Group 11 contains the encoding name
 			encoding = m.group(11);
 			// Replace it by the new encoding
 			int n = text.indexOf(encoding, m.start());
 			text.replace(n, n+encoding.length(), outputEncoding);
 		}
 		else if ( isHTML ) { // No HTML encoding found, but try to update if it was seen as HTML from extension 
 			// Try to place it after <head>
 			m = htmlHead.matcher(text);
 			if ( m.find() ) {
 				text.insert(m.end(), String.format(
 					"<meta http-equiv=\"Content-Type\" content=\"text/html; charset=%s\"></meta>",
 					outputEncoding));
 			}
 			else { // If no <head>, try <html>
 				m = htmlDecl.matcher(text);
 				if ( m.find() ) {
 					int n = text.indexOf(">", m.end());
 					if ( n != -1 ) {
 						text.insert(n+1, String.format(
 							"<head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=%s\"></meta></head>",
 							outputEncoding));
 					}
 				}
 			}
 		}
 		
 		// Convert the string back to a CharBuffer
 		int len = text.length();
 		// Make sure we have room for added characters
 		if ( len > buffer.capacity() ) {
 			buffer = CharBuffer.allocate(len);
 		}
 		else {
 			buffer.clear();
 		}
 		buffer.append(text.toString());
 		buffer.limit(len);
 		return encoding;
 	}
 	
 	private void checkSplitSequence () {
 		int len = buffer.position();
 		buffer.position(0);
 		// Search for the first & or \ in the last 10 (or less) characters
 		prevBuf = null;
 		int j = 0;
 		for ( int i=len-1; ((i>=0) && (j<10)); i-- ) {
 			if (( buffer.charAt(i) == '&' ) || ( buffer.charAt(i) == '\\' )) {
 				prevBuf = buffer.subSequence(i, len).toString();
 				len = i;
 				break;
 			}
 			j++;
 		}
 		buffer.position(len);
 		buffer.limit(len);
 	}
 	
 	private void unescape () {
 		int len = buffer.position();
 		buffer.position(0);
 		Matcher m = pattern.matcher(buffer);
 		int pos = 0;
 		StringBuilder tmp = new StringBuilder(len);
 		String seq = null;
 		while ( m.find(pos) ) {
 			// Copy any previous text
 			if ( m.start() > pos ) {
 				// Get text before
 				tmp.append(buffer.subSequence(pos, m.start()));
 			}
 			pos = m.end();
 
 			// Treat the escape sequence
 			seq = m.group();
 			int value = -1;
 			int uIndex = seq.indexOf('u');
 			if ( seq.indexOf('x') == 2 ) {
 				// Hexadecimal NCR "&#xHHH;"
 				value = Integer.parseInt(seq.substring(3, seq.length()-1), 16);
 			}
 			else if (( uIndex == 1 ) && ( seq.charAt(uIndex-1) == '\\' )) {
 				// Java style "\ and uHHH"
 				value = Integer.parseInt(seq.substring(2), 16);
 			}
 			else if ( seq.indexOf('#') == 1 ) {
 				// Decimal NCR "&#DDD;"
 				value = Integer.parseInt(seq.substring(2, seq.length()-1));
 			}
 			else {
 				// Character entity reference: &NAME;
 				seq = seq.substring(1, seq.length()-1);
 				if ( charEntities.containsKey(seq) ) {
 					value = (int)charEntities.get(seq);
 				}
 				else { // Unidentified: leave it like that
 					value = -1;
 				}
 			}
 
 			// Append the parsed escape
 			if ( value < 128 ) {
 				// Unknown pattern or ASCII values: Keep it as it
 				// (so <, &, ", etc.. stay escaped)
 				tmp.append(m.group());
 			}
 			else {
 				tmp.append((char)value);
 			}
 		}
 		
 		// Copy last part and re-build the buffer
 		if ( seq != null ) { // We had at least one match
 			if ( pos < len ) {
 				// Get text before
 				tmp.append(buffer.subSequence(pos, len));
 			}
 			// Reset the buffer
 			buffer.clear();
 			buffer.append(tmp.toString(), 0, tmp.length());
 		}
 		else { // Else: nothing to un-escape
 			buffer.position(len);
 		}
 	}
 	
 	private String findCER (char value) {
 		if ( charEntities == null ) createCharEntitiesTable();
 		for ( String key : charEntities.keySet() ) {
 			if ( value == charEntities.get(key) ) {
 				return key;
 			}
 		}
 		return null;
 	}
 	
 	private void createCharEntitiesTable () {
 		charEntities = new Hashtable<String, Character>();
 		charEntities.put("nbsp", '\u00a0');
 		charEntities.put("iexcl", '\u00a1');
 		charEntities.put("cent", '\u00a2');
 		charEntities.put("pound", '\u00a3');
 		charEntities.put("curren", '\u00a4');
 		charEntities.put("yen", '\u00a5');
 		charEntities.put("brvbar", '\u00a6');
 		charEntities.put("sect", '\u00a7');
 		charEntities.put("uml", '\u00a8');
 		charEntities.put("copy", '\u00a9');
 		charEntities.put("ordf", '\u00aa');
 		charEntities.put("laquo", '\u00ab');
 		charEntities.put("not", '\u00ac');
 		charEntities.put("shy", '\u00ad');
 		charEntities.put("reg", '\u00ae');
 		charEntities.put("macr", '\u00af');
 		charEntities.put("deg", '\u00b0');
 		charEntities.put("plusmn", '\u00b1');
 		charEntities.put("sup2", '\u00b2');
 		charEntities.put("sup3", '\u00b3');
 		charEntities.put("acute", '\u00b4');
 		charEntities.put("micro", '\u00b5');
 		charEntities.put("para", '\u00b6');
 		charEntities.put("middot", '\u00b7');
 		charEntities.put("cedil", '\u00b8');
 		charEntities.put("sup1",'\u00b9');
 		charEntities.put("ordm", '\u00ba');
 		charEntities.put("raquo", '\u00bb');
 		charEntities.put("frac14", '\u00bc');
 		charEntities.put("frac12", '\u00bd');
 		charEntities.put("frac34", '\u00be');
 		charEntities.put("iquest", '\u00bf');
 		charEntities.put("Agrave", '\u00c0');
 		charEntities.put("Aacute", '\u00c1');
 		charEntities.put("Acirc", '\u00c2');
 		charEntities.put("Atilde", '\u00c3');
 		charEntities.put("Auml", '\u00c4');
 		charEntities.put("Aring", '\u00c5');
 		charEntities.put("AElig", '\u00c6');
 		charEntities.put("Ccedil", '\u00c7');
 		charEntities.put("Egrave", '\u00c8');
 		charEntities.put("Eacute", '\u00c9');
 		charEntities.put("Ecirc", '\u00ca');
 		charEntities.put("Euml", '\u00cb');
 		charEntities.put("Igrave", '\u00cc');
 		charEntities.put("Iacute", '\u00cd');
 		charEntities.put("Icirc", '\u00ce');
 		charEntities.put("Iuml", '\u00cf');
 		charEntities.put("ETH", '\u00d0');
 		charEntities.put("Ntilde", '\u00d1');
 		charEntities.put("Ograve", '\u00d2');
 		charEntities.put("Oacute", '\u00d3');
 		charEntities.put("Ocirc", '\u00d4');
 		charEntities.put("Otilde", '\u00d5');
 		charEntities.put("Ouml", '\u00d6');
 		charEntities.put("times", '\u00d7');
 		charEntities.put("Oslash", '\u00d8');
 		charEntities.put("Ugrave", '\u00d9');
 		charEntities.put("Uacute", '\u00da');
 		charEntities.put("Ucirc", '\u00db');
 		charEntities.put("Uuml", '\u00dc');
 		charEntities.put("Yacute", '\u00dd');
 		charEntities.put("THORN", '\u00de');
 		charEntities.put("szlig", '\u00df');
 		charEntities.put("agrave", '\u00e0');
 		charEntities.put("aacute", '\u00e1');
 		charEntities.put("acirc", '\u00e2');
 		charEntities.put("atilde", '\u00e3');
 		charEntities.put("auml", '\u00e4');
 		charEntities.put("aring", '\u00e5');
 		charEntities.put("aelig", '\u00e6');
 		charEntities.put("ccedil", '\u00e7');
 		charEntities.put("egrave", '\u00e8');
 		charEntities.put("eacute", '\u00e9');
 		charEntities.put("ecirc", '\u00ea');
 		charEntities.put("euml", '\u00eb');
 		charEntities.put("igrave", '\u00ec');
 		charEntities.put("iacute", '\u00ed');
 		charEntities.put("icirc", '\u00ee');
 		charEntities.put("iuml", '\u00ef');
 		charEntities.put("eth", '\u00f0');
 		charEntities.put("ntilde", '\u00f1');
 		charEntities.put("ograve", '\u00f2');
 		charEntities.put("oacute", '\u00f3');
 		charEntities.put("ocirc", '\u00f4');
 		charEntities.put("otilde", '\u00f5');
 		charEntities.put("ouml", '\u00f6');
 		charEntities.put("divide", '\u00f7');
 		charEntities.put("oslash", '\u00f8');
 		charEntities.put("ugrave", '\u00f9');
 		charEntities.put("uacute", '\u00fa');
 		charEntities.put("ucirc", '\u00fb');
 		charEntities.put("uuml", '\u00fc');
 		charEntities.put("yacute", '\u00fd');
 		charEntities.put("thorn", '\u00fe');
 		charEntities.put("yuml", '\u00ff');
 		charEntities.put("OElig", '\u0152');
 		charEntities.put("oelig", '\u0153');
 		charEntities.put("Scaron", '\u0160');
 		charEntities.put("scaron", '\u0161');
 		charEntities.put("Yuml", '\u0178');
 		charEntities.put("circ", '\u02c6');
 		charEntities.put("tilde", '\u02dc');
 		charEntities.put("ensp", '\u2002');
 		charEntities.put("emsp", '\u2003');
 		charEntities.put("thinsp", '\u2009');
 		charEntities.put("zwnj", '\u200c');
 		charEntities.put("zwj", '\u200d');
 		charEntities.put("lrm", '\u200e');
 		charEntities.put("rlm", '\u200f');
 		charEntities.put("ndash", '\u2013');
 		charEntities.put("mdash", '\u2014');
 		charEntities.put("lsquo", '\u2018');
 		charEntities.put("rsquo", '\u2019');
 		charEntities.put("sbquo", '\u201a');
 		charEntities.put("ldquo", '\u201c');
 		charEntities.put("rdquo", '\u201d');
 		charEntities.put("bdquo", '\u201e');
 		charEntities.put("dagger", '\u2020');
 		charEntities.put("Dagger", '\u2021');
 		charEntities.put("permil", '\u2030');
 		charEntities.put("lsaquo", '\u2039');
 		charEntities.put("rsaquo", '\u203a');
 		charEntities.put("euro", '\u20ac');
 		charEntities.put("fnof", '\u0192');
 		charEntities.put("Alpha", '\u0391');
 		charEntities.put("Beta", '\u0392');
 		charEntities.put("Gamma", '\u0393');
 		charEntities.put("Delta", '\u0394');
 		charEntities.put("Epsilon", '\u0395');
 		charEntities.put("Zeta", '\u0396');
 		charEntities.put("Eta", '\u0397');
 		charEntities.put("Theta", '\u0398');
 		charEntities.put("Iota", '\u0399');
 		charEntities.put("Kappa", '\u039a');
 		charEntities.put("Lambda", '\u039b');
 		charEntities.put("Mu", '\u039c');
 		charEntities.put("Nu", '\u039d');
 		charEntities.put("Xi", '\u039e');
 		charEntities.put("Omicron", '\u039f');
 		charEntities.put("Pi", '\u03a0');
 		charEntities.put("Rho", '\u03a1');
 		charEntities.put("Sigma", '\u03a3');
 		charEntities.put("Tau", '\u03a4');
 		charEntities.put("Upsilon", '\u03a5');
 		charEntities.put("Phi", '\u03a6');
 		charEntities.put("Chi", '\u03a7');
 		charEntities.put("Psi", '\u03a8');
 		charEntities.put("Omega", '\u03a9');
 		charEntities.put("alpha", '\u03b1');
 		charEntities.put("beta", '\u03b2');
 		charEntities.put("gamma", '\u03b3');
 		charEntities.put("delta", '\u03b4');
 		charEntities.put("epsilon", '\u03b5');
 		charEntities.put("zeta", '\u03b6');
 		charEntities.put("eta", '\u03b7');
 		charEntities.put("theta", '\u03b8');
 		charEntities.put("iota", '\u03b9');
 		charEntities.put("kappa", '\u03ba');
 		charEntities.put("lambda", '\u03bb');
 		charEntities.put("mu", '\u03bc');
 		charEntities.put("nu", '\u03bd');
 		charEntities.put("xi", '\u03be');
 		charEntities.put("omicron", '\u03bf');
 		charEntities.put("pi", '\u03c0');
 		charEntities.put("rho", '\u03c1');
 		charEntities.put("sigmaf", '\u03c2');
 		charEntities.put("sigma", '\u03c3');
 		charEntities.put("tau", '\u03c4');
 		charEntities.put("upsilon", '\u03c5');
 		charEntities.put("phi", '\u03c6');
 		charEntities.put("chi", '\u03c7');
 		charEntities.put("psi", '\u03c8');
 		charEntities.put("omega", '\u03c9');
 		charEntities.put("thetasym", '\u03d1');
 		charEntities.put("upsih", '\u03d2');
 		charEntities.put("piv", '\u03d6');
 		charEntities.put("bull", '\u2022');
 		charEntities.put("hellip", '\u2026');
 		charEntities.put("prime", '\u2032');
 		charEntities.put("Prime", '\u2033');
 		charEntities.put("oline", '\u203e');
 		charEntities.put("frasl", '\u2044');
 		charEntities.put("weierp", '\u2118');
 		charEntities.put("image", '\u2111');
 		charEntities.put("real", '\u211c');
 		charEntities.put("trade", '\u2122');
 		charEntities.put("alefsym", '\u2135');
 		charEntities.put("larr", '\u2190');
 		charEntities.put("uarr", '\u2191');
 		charEntities.put("rarr", '\u2192');
 		charEntities.put("darr", '\u2193');
 		charEntities.put("harr", '\u2194');
 		charEntities.put("crarr", '\u21b5');
 		charEntities.put("lArr", '\u21d0');
 		charEntities.put("uArr", '\u21d1');
 		charEntities.put("rArr", '\u21d2');
 		charEntities.put("dArr", '\u21d3');
 		charEntities.put("hArr", '\u21d4');
 		charEntities.put("forall", '\u2200');
 		charEntities.put("part", '\u2202');
 		charEntities.put("exist", '\u2203');
 		charEntities.put("empty", '\u2205');
 		charEntities.put("nabla", '\u2207');
 		charEntities.put("isin", '\u2208');
 		charEntities.put("notin", '\u2209');
 		charEntities.put("ni", '\u220b');
 		charEntities.put("prod", '\u220f');
 		charEntities.put("sum", '\u2211');
 		charEntities.put("minus", '\u2212');
 		charEntities.put("lowast", '\u2217');
 		charEntities.put("radic", '\u221a');
 		charEntities.put("prop", '\u221d');
 		charEntities.put("infin", '\u221e');
 		charEntities.put("ang", '\u2220');
 		charEntities.put("and", '\u2227');
 		charEntities.put("or", '\u2228');
 		charEntities.put("cap", '\u2229');
 		charEntities.put("cup", '\u222a');
 		charEntities.put("int", '\u222b');
 		charEntities.put("there4", '\u2234');
 		charEntities.put("sim", '\u223c');
 		charEntities.put("cong", '\u2245');
 		charEntities.put("asymp", '\u2248');
 		charEntities.put("ne", '\u2260');
 		charEntities.put("equiv", '\u2261');
 		charEntities.put("le", '\u2264');
 		charEntities.put("ge", '\u2265');
 		charEntities.put("sub", '\u2282');
 		charEntities.put("sup", '\u2283');
 		charEntities.put("nsub", '\u2284');
 		charEntities.put("sube", '\u2286');
 		charEntities.put("supe", '\u2287');
 		charEntities.put("oplus", '\u2295');
 		charEntities.put("otimes", '\u2297');
 		charEntities.put("perp", '\u22a5');
 		charEntities.put("sdot", '\u22c5');
 		charEntities.put("lceil", '\u2308');
 		charEntities.put("rceil", '\u2309');
 		charEntities.put("lfloor", '\u230a');
 		charEntities.put("rfloor", '\u230b');
 		charEntities.put("lang", '\u2329');
 		charEntities.put("rang", '\u232a');
 		charEntities.put("loz", '\u25ca');
 		charEntities.put("spades", '\u2660');
 		charEntities.put("clubs", '\u2663');
 		charEntities.put("hearts", '\u2665');
 		charEntities.put("diams", '\u2666');
 	}
 
 }
