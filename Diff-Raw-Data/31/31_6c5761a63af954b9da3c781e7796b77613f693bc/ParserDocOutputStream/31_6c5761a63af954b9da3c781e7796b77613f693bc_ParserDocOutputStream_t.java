 
 package org.paxle.parser.iotools;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.UnsupportedEncodingException;
 
 import org.paxle.core.charset.ACharsetDetectorOutputStream;
 import org.paxle.core.charset.ICharsetDetector;
 import org.paxle.core.doc.IParserDocument;
 import org.paxle.parser.ParserContext;
 import org.paxle.parser.ParserException;
 
 /**
  * This class is a wrapper around a {@link FileOutputStream} to save the written
  * data to a temp file. When the writing finishes, the resulting file on the disk
  * is being parsed and the resulting {@link IParserDocument} is added to the provided
  * one. Finally the temporary file is being deleted.
  * 
  * @see File#createTempFile(String, String)
  * @see FileOutputStream
  * @see ParserTools#parse(String, File)
  * @see IParserDocument#addSubDocument(String, IParserDocument)
  */
 public class ParserDocOutputStream extends OutputStream {
 	
 	private final OutputStream os;
 	private final File of;
 	
 	// TODO: cache file in ram
 	public ParserDocOutputStream() throws IOException {
 		this.of = ParserTools.createTempFile("parser-doc_tmp", ParserDocOutputStream.class);
 		final FileOutputStream fos = new FileOutputStream(this.of);
 		final ICharsetDetector cd = ParserContext.getCurrentContext().getCharsetDetector();
 		this.os = (cd != null) ? cd.createOutputStream(fos) : fos;
 	}
 	
 	@Override
 	public void write(int b) throws IOException {
 		this.os.write(b);
 	}
 	
 	@Override
 	public void write(byte[] b, int off, int len) throws IOException {
 		this.os.write(b, off, len);
 	}
 	
 	@Override
 	public void close() throws IOException {
		this.os.flush();
 		this.os.close();
 	}
 	
 	public String getCharset() {
 		if (this.os instanceof ACharsetDetectorOutputStream) {
 			return ((ACharsetDetectorOutputStream)this.os).getCharset();
 		} else {
 			return null;
 		}
 	}
 	
 	public IParserDocument parse(String location) throws ParserException, IOException {
		close();
 		final String charset = getCharset();
 		try {
 			return ParserTools.parse(location, charset, this.of);
 		} catch (UnsupportedEncodingException e) {
 			throw new ParserException("Error parsing file on close due to incorrectly detected charset '" + charset + "'", e);
 		}
 	}
 	
 	@Override
 	public void flush() throws IOException {
 		this.os.flush();
 	}
 }
