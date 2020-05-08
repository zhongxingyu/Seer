 package info.freelibrary.bagit;
 
 import info.freelibrary.util.BufferedFileWriter;
 import info.freelibrary.util.I18nObject;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.LineNumberReader;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 class Declaration extends I18nObject {
 
 	public static final String ENCODING = "UTF-8";
 
 	public static final String VERSION = "0.96";
 
 	private static final String ENCODING_TAG = "Tag-File-Character-Encoding";
 
 	private static final String FILE_NAME = "bagit.txt";
 
 	private static final Logger LOGGER = LoggerFactory
 			.getLogger(Declaration.class);
 
 	private static final String VERSION_TAG = "BagIt-Version";
 
 	private boolean isValid = true;
 
 	private File myBagDir;
 
 	private String myEncoding;
 
 	private String myVersion;
 
 	Declaration() {
 		myVersion = VERSION;
 		myEncoding = ENCODING;
 	}
 
 	/**
 	 * The tag file required to be in all bags conforming to this specification.
 	 * Contains tags necessary for bootstrapping the reading and processing of
 	 * the rest of a bag.
 	 * 
 	 * @param aBagDir The working directory of the declaration's
 	 *        <code>Bag</code>
 	 * @throws IOException If there is trouble writing the
 	 *         <code>BagItDeclaration</code>
 	 */
 	Declaration(File aBagDir) throws IOException {
 		File bagItTxt = new File(aBagDir, FILE_NAME);
 
 		// Keep a reference to our working directory
 		myBagDir = aBagDir;
 
 		if (!bagItTxt.exists()) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(getI18n("bagit.debug.create_bagit_txt"));
			}

			myVersion = VERSION;
			myEncoding = ENCODING;
 		}
 		else {
 			FileReader fileReader = new FileReader(bagItTxt);
 			LineNumberReader reader = null;
 			String line;
 
 			try {
 				reader = new LineNumberReader(new BufferedReader(fileReader));
 
 				while ((line = reader.readLine()) != null) {
 					int start = line.indexOf(":") + 1;
 
 					if (reader.getLineNumber() == 1
 							&& line.startsWith(VERSION_TAG + ": ")) {
 						myVersion = line.substring(start).trim();
 					}
 					else if (reader.getLineNumber() == 2
 							&& line.startsWith(ENCODING_TAG + ": ")) {
 						myEncoding = line.substring(start).trim();
 					}
 					else {
 						// Spec says MUST consist of only two lines (above)
 						isValid = false;
 					}
 				}
 			}
 			finally {
 				if (reader != null) {
 					reader.close();
 				}
 			}
 		}
 	}
 
 	/**
 	 * Gets the character encoding used in this <code>Bag</code>, UTF-8.
 	 * 
 	 * @return The character encoding used in this <code>Bag</code>, UTF-8
 	 */
 	String getEncoding() {
 		return myEncoding == null ? ENCODING : myEncoding;
 	}
 
 	/**
 	 * Gets the BagIt specification version to which this declaration conforms.
 	 * 
 	 * @return The BagIt specification version
 	 */
 	String getVersion() {
 		return myVersion == null ? VERSION : myVersion;
 	}
 
 	/**
 	 * Attempts to validate this BagIt declaration. It throws a
 	 * <code>BagException</code> if there is a problem with it.
 	 * 
 	 * @throws BagException A problem preventing the declaration from being a
 	 *         valid BagIt declaration
 	 */
 	void validate() throws BagException, IOException {
 		if (new File(myBagDir, FILE_NAME).exists()) {
 			if (!isValid) {
 				throw new BagException(getI18n("bagit.invalid_bagit",
 						getI18n("bagit.bagit_structure")));
 			}
 
 			if (myVersion == null) {
 				throw new BagException(getI18n("bagit.invalid_bagit",
 						getI18n("bagit.no_version")));
 			}
 
 			if (myVersion.indexOf(".") == -1) {
 				throw new BagException(getI18n("bagit.bad_version"));
 			}
 
 			if (myEncoding == null) {
 				throw new BagException(getI18n("bagit.invalid_bagit",
 						getI18n("bagit.no_encoding")));
 			}
 
 			if (!myEncoding.equals("UTF-8")) {
 				throw new BagException(getI18n("bagit.bad_encoding"));
 			}
 		}
 		else {
 			write(); // We shouldn't be able to write an invalid file
 		}
 	}
 
 	/**
 	 * Returns an XML representation of the BagIt declaration.
 	 * 
 	 * @return An XML representation of the BagIt declaration
 	 */
 	public String toString() {
 		String eol = System.getProperty("line.separator");
 		StringBuilder builder = new StringBuilder();
 
 		builder.append("<declaration>").append(eol).append("<version>");
 		builder.append(getVersion()).append("</version>").append(eol);
 		builder.append("<encoding>").append(getEncoding());
 		builder.append("</encoding>").append(eol).append("</declaration>");
 		builder.append(eol);
 
 		return builder.toString();
 	}
 
 	/**
 	 * Writes a new <code>bagit.txt</code> file. If you are working with an
 	 * existing bag that already has a valid declaration, you don't need to
 	 * write a new one. This method uses this library's defaults and will
 	 * overwrite a declaration from another bagit library.
 	 * 
 	 * @throws IOException If there is trouble writing the
 	 *         <code>bagit.txt</code> file
 	 */
 	void write() throws IOException {
 		File bagItTxt = new File(myBagDir, FILE_NAME);
 		BufferedFileWriter writer = null;
 
 		if (bagItTxt.exists() && !bagItTxt.delete()) {
 			throw new IOException(getI18n("bagit.file_delete", bagItTxt));
 		}
 
 		try {
 			writer = new BufferedFileWriter(bagItTxt);
 			writer.write(VERSION_TAG + ": " + VERSION);
 			writer.newLine();
 			writer.write(ENCODING_TAG + ": " + ENCODING);
 			writer.newLine();
 		}
 		finally {
 			if (writer != null) {
 				writer.close();
 			}
 		}
 	}
 }
