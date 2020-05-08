 package com.gentics.cr.template;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 
 import com.gentics.cr.exceptions.CRException;
 import com.gentics.cr.util.Constants;
 /**
  * loads a template from a file usint an input stream.
  * Last changed: $Date: 2010-04-01 15:25:54 +0200 (Do, 01 Apr 2010) $
  * @version $Revision: 545 $
  * @author $Author: supnig@constantinopel.at $
  *
  */
 public class FileTemplate implements ITemplate {
 
 	/**
 	 * Template source as String.
 	 */
 	private String source;
 	/**
 	 * Key identifying this template.
 	 */
 	private String key;
 
 	/**
 	 * gets the key of the template. usually a md5 hash
 	 * @return key
 	 */
 	public final String getKey() {
 		return key;
 	}
 
 	/**
 	 * @return source of the template.
 	 */
 	public final String getSource() {
 		return source;
 	}
 
 	/**
 	 * Creates a new instance of FileTemplate.
 	 * @param stream - stream with the template code
 	 * @throws CRException when we cannot read the stream or there was an error
 	 * generating the md5sum of the stream.
 	 */
 	public FileTemplate(final InputStream stream) throws CRException {
 		readSource(stream);
 		try {
 			MessageDigest digest = MessageDigest.getInstance("MD5");
 			digest.update(this.source.getBytes());
 			this.key = new String(digest.digest());
 		} catch (NoSuchAlgorithmException e) {
 			throw new CRException(e);
 		}
 	}
 	/**
 	 * Reads the given stream into the template source.
 	 * @param stream - stream to read
 	 * @throws CRException when the stream cannot be read
 	 */
 	private void readSource(final InputStream stream) throws CRException {
 		try {
 			this.source = slurp(stream);
 		} catch (IOException e) {
 			throw new CRException(e);
 		}
 	}
 
 	/**
 	 * Creates a new instance of FileTemplate.
 	 * @param stream - stream with the template code
 	 * @param file - file used to generate the key from the filename
 	 * @throws CRException when we cannot read the stream or there was an error
 	 * generating the md5sum of the stream.
 	 */
 	public FileTemplate(final FileInputStream stream, final File file)
 			throws CRException {
 		readSource(stream);
 		this.key = file.getAbsolutePath();
 	}
 
 	/**
 	 * Read a String from the given InputStream.
 	 * @param in - stream to read from
 	 * @return String with the contents read from the stream
 	 * @throws IOException when the stream cannot be read
 	 */
 	private static String slurp(final InputStream in) throws IOException {
 		StringBuffer out = new StringBuffer();
 		byte[] b = new byte[Constants.KILOBYTE];
 		int n;
 		while ((n = in.read(b)) != -1) {
 			out.append(new String(b, 0, n));
 		}
 		return out.toString();
 	}
 
 }
