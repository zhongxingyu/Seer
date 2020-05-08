 package org.paxle.parser.iotools;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.Reader;
 import java.io.UnsupportedEncodingException;
 import java.nio.CharBuffer;
 
 import org.paxle.core.charset.ACharsetDetectorInputStream;
 import org.paxle.core.charset.ICharsetDetector;
 import org.paxle.core.doc.IParserDocument;
 import org.paxle.core.mimetype.IMimeTypeDetector;
 import org.paxle.parser.ISubParser;
 import org.paxle.parser.ParserContext;
 import org.paxle.parser.ParserException;
 
 public class ParserTools {
 	
 	/**
 	 * Replaces every non-space whitespace character with a space character and truncates
 	 * multiple spaces to a single one. This results in the text-representation free of
 	 * obscurities.
 	 *  
 	 * @param text the text to process
 	 * @return the words of the given text each separated by only a single space
 	 */
 	public static String whitespaces2Space(String text) {
 		if (text == null) return null;
 		return text.replaceAll("\\s", " ").trim();
 	}
 	
 	public static String getCharset(File file, String mimeType) throws IOException, ParserException {
 		final ParserContext context = ParserContext.getCurrentContext();
 		if (context == null)
 			throw new ParserException("cannot access ParserContext whereas this method must be used from within a sub-parser");
 		return getCharset(file, context.getCharsetDetector(), mimeType);
 	}
 	
 	public static String getCharset(File file, ICharsetDetector cd, String mimeType) throws IOException, ParserException {
 		if (cd == null)
 			throw new ParserException("no charset detector service available");
 		if (mimeType != null && !cd.isInspectable(mimeType))
 			throw new ParserException("MIME type '" + mimeType + "' cannot be processed by the charset detector");
 		
 		final ACharsetDetectorInputStream cdis = cd.createInputStream(new BufferedInputStream(new FileInputStream(file)));
 		try {
 			final byte[] buf = new byte[DEFAULT_BUFFER_SIZE_BYTES];
 			while (!cdis.charsetDetected() && cdis.read(buf) > -1);
 		} finally { cdis.close(); }
 		return cdis.getCharset();
 	}
 	
 	public static String getMimeType(File file) throws IOException, ParserException {
 		final ParserContext context = ParserContext.getCurrentContext();
 		if (context == null)
 			throw new ParserException("cannot access ParserContext whereas this method must be used from within a sub-parser");
 		return getMimeType(file, context.getMimeTypeDetector());
 	}
 	
 	public static String getMimeType(File file, IMimeTypeDetector mtd) throws IOException, ParserException {
 		if (mtd == null)
 			throw new ParserException("cannot determine the MIME type of " + file + " due to missing MIME type detector");
 		final String mimeType;
 		try {
 			mimeType = mtd.getMimeType(file);
 		} catch (Exception e) {
 			if (e instanceof RuntimeException)
 				throw (RuntimeException)e;
 			throw new ParserException("error detecting MIME type of file " + file, e);
 		}
 		return mimeType;
 	}
 	
 	/**
 	 * Determines the MIME type and character set of the given file and finally calles
 	 * the {@link ISubParser#parse(String, String, File)} of the found sub-parser.
 	 * <p>
 	 *  This method is intended for usage by archive parsers which have to handle
 	 *  multiple, possibly unknown internal files of archive containers.
 	 * </p>
 	 * 
 	 * @see #getMimeType(File)
 	 * @see #getCharset(File, String)
 	 * @see ParserContext#getCurrentContext()
 	 * @see ParserContext#getMimeTypeDetector()
 	 * @see ParserContext#getCharsetDetector()
 	 * @see ParserContext#getParser(String)
 	 * @see ISubParser#parse(String, String, File)
 	 * @param  location name or location of the resource within the container
 	 * @param  content {@link File} containing the raw content of the (uncompressed) file
 	 * @return the resulting {@link IParserDocument} (in case of an archive-parser to add to
 	 *         the main parser-doc as sub-document)
 	 * @throws <b>ParserException</b>:
 	 * <ul>
 	 *  <li>
 	 *   if no {@link ParserContext} is available because this method has not been called
 	 *   from the thread executing the {@link ISubParser}
 	 *  </li>
 	 *  <li>if no MIME type detector object could be retrieved from the {@link ParserContext}</li>
 	 *  <li>if a MIME type detection error occured</li>
 	 *  <li>if no sub-parser handling the detected MIME type could be found</li>
 	 *  <li>
 	 *   if the charset has been detected incorrectly and the selected {@link ISubParser} throwed
 	 *   an {@link UnsupportedEncodingException}
 	 *  </li>
 	 *  <li>if the selected {@link ISubParser} was unable to parse the given resource</li>
 	 * </ul>
 	 * @throws <b>IOException</b> if an I/O-error occures during reading <code>content</code>
 	 */
 	public static IParserDocument parse(String location, File content) throws ParserException, IOException {
 		final String mimeType = getMimeType(content);
 		if (mimeType == null)
 			throw new ParserException("detected MIME type of file " + content + " is null, cannot parse it");
 		
 		String charset;
 		try {
 			charset = getCharset(content, mimeType);
 		} catch (ParserException e) {
 			/* the charset is not "that" important, many formats have pre-defined character sets or save
 			 * their charset information internally, so a non-detectable charset is not really bad. The
 			 * sub-parser will throw an exception if it needed a previously detected charset. */
 			charset = null;
 		}
 		
 		try {
 			return parse(location, mimeType, charset, content);
 		} catch (UnsupportedEncodingException e) {
 			throw new ParserException("Detected wrong charset '" + charset + "' for file: " + content, e);
 		}
 	}
 	
 	public static IParserDocument parse(String location, String charset, File content) throws ParserException, IOException,
 			UnsupportedEncodingException {
 		final String mimeType = getMimeType(content);
 		if (mimeType == null)
 			throw new ParserException("detected MIME type of file " + content + " is null, cannot parse it");
 		return parse(location, mimeType, charset, content);
 	}
 	
 	public static IParserDocument parse(String location, String mimeType, String charset, File content) throws ParserException,
 			IOException, UnsupportedEncodingException {
 		// retrieve the sub-parser for the found MIME type, parse content and return the document
 		final ParserContext context = ParserContext.getCurrentContext();
 		if (context == null)
 			throw new ParserException("cannot access ParserContext whereas this method must be used from within a sub-parser");
 		
 		final ISubParser sp = context.getParser(mimeType);
 		if (sp == null)
 			throw new ParserException("No parser found for MIME type '" + mimeType + "'");
		final IParserDocument pdoc = sp.parse(location, charset, content);
		if (pdoc.getMimeType() == null)
			pdoc.setMimeType(mimeType);
		return pdoc;
 	}
 	
 	public static final int DEFAULT_BUFFER_SIZE_BYTES = 1024;
 	public static final int DEFAULT_BUFFER_SIZE_CHARS = DEFAULT_BUFFER_SIZE_BYTES / 2;
 	
 	/**
 	 * Copies all data from the given {@link Reader} to the given {@link Appendable}.
 	 * <p><i>Note: this method does not close the supplied Reader.</i></p>
 	 * 
 	 * @see #copy(Reader, Appendable, long) for details
 	 * @param in the reader to read from
 	 * @param out the appendable to append to
 	 * @return the number of copied characters
 	 * @throws <b>IOException</b> if an I/O-error occures
 	 */
 	public static long copy(Reader in, Appendable out) throws IOException {
 		return copy(in, out, -1);
 	}
 	
 	/**
 	 * Copies an amount of data from the given {@link Reader} to the given {@link Appendable}.
 	 * <p><i>Note: this method does not close the supplied Reader.</i></p>
 	 * 
 	 * @see #DEFAULT_BUFFER_SIZE_CHARS for the size of the buffer
 	 * @see Reader#read(char[], int, int)
 	 * @see Appendable#append(CharSequence)
 	 * @see CharBuffer#wrap(char[], int, int)
 	 * @param in the reader to read from
 	 * @param out the appendable to append to
 	 * @param bytes the number of characters to copy
 	 * @return the number of copied characters
 	 * @throws <b>IOException</b> if an I/O-error occures
 	 */
 	public static long copy(Reader in, Appendable out, long chars) throws IOException {
 		final char[] buf = new char[DEFAULT_BUFFER_SIZE_CHARS];                
 		int cs = (int)((chars > 0 && chars < (DEFAULT_BUFFER_SIZE_CHARS)) ? chars : (DEFAULT_BUFFER_SIZE_CHARS));
 		
 		int rn;
 		long rt = 0;
 		while ((rn = in.read(buf, 0, cs)) > 0) {
 			out.append(CharBuffer.wrap(buf, 0, rn));
 			rt += rn;
 			
 			if (chars > 0) {
 				cs = (int)Math.min(chars - rt, DEFAULT_BUFFER_SIZE_CHARS);
 				if (cs == 0)
 					break;
 			}
 		}
 		return rt;
 	}
 	
 	/**
 	 * Copies all data from the given {@link InputStream} to the given {@link OutputStream}
 	 * using the {@link #copy(InputStream, OutputStream, long)}-method.
 	 * <p><i>Note: this method does neither close the supplied InputStream nor the OutputStream.</i></p>
 	 * 
 	 * @see #copy(InputStream, OutputStream, long) for details
 	 * @param is the stream to read from
 	 * @param os the stream to write to
 	 * @return the number of copied bytes
 	 * @throws <b>IOException</b> if an I/O-error occures
 	 */
 	public static long copy(InputStream is, OutputStream os) throws IOException {
 		return copy(is, os, -1);
 	}
 	
 	/**
 	 * Copies an amount of data from the given {@link InputStream} to the given {@link OutputStream}.
 	 * <p><i>Note: this method does neither close the supplied InputStream nor the OutputStream.</i></p>
 	 * 
 	 * @see #DEFAULT_BUFFER_SIZE_BYTES for the size of the buffer
 	 * @see InputStream#read(byte[], int, int)
 	 * @see OutputStream#write(byte[], int, int)
 	 * @param is the stream to read from
 	 * @param os the stream to write to
 	 * @param bytes the number of bytes to copy
 	 * @return the number of copied bytes
 	 * @throws <b>IOException</b> if an I/O-error occures
 	 */
 	public static long copy(InputStream is, OutputStream os, long bytes) throws IOException {
 		final byte[] buf = new byte[DEFAULT_BUFFER_SIZE_BYTES];                
 		int cs = (int)((bytes > 0 && bytes < DEFAULT_BUFFER_SIZE_BYTES) ? bytes : DEFAULT_BUFFER_SIZE_BYTES);
 		
 		int rn;
 		long rt = 0;
 		while ((rn = is.read(buf, 0, cs)) > 0) {
 			os.write(buf, 0, rn);
 			rt += rn;
 			
 			if (bytes > 0) {
 				cs = (int)Math.min(bytes - rt, DEFAULT_BUFFER_SIZE_BYTES);
 				if (cs == 0)
 					break;
 			}
 		}
 		os.flush();
 		return rt;
 	}
 	
 	// from YaCy: de.anomic.plasma.parser.AbstractParser
 	public static File createTempFile(String name, Class clazz) throws IOException {
 		final String parserClassName = clazz.getSimpleName();
 		
 		// getting the file extension
 		int idx = name.lastIndexOf("/");
 		final String fileName = (idx != -1) ? name.substring(idx+1) : name;        
 		
 		idx = fileName.lastIndexOf(".");
 		final String fileExt = (idx > -1) ? fileName.substring(idx+1) : "";
 		
 		// creates the temp file
 		final File tempFile = File.createTempFile(
 				parserClassName + "_" + ((idx > -1) ? fileName.substring(0, idx) : fileName),
 				(fileExt.length() > 0) ? "." + fileExt : fileExt);
 		return tempFile;
 	}
 }
