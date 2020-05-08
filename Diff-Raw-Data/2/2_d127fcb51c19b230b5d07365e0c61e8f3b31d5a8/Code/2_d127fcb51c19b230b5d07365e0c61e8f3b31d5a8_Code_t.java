 package org.smartsnip.core;
 
 import java.io.IOException;
 
 import org.smartsnip.persistence.IPersistence;
 import org.smartsnip.shared.XCode;
 
 /**
  * 
  * @author Felix Niederwanger
  * @author littlelion
  * 
  */
 public abstract class Code {
 	/** Concrete code */
 	public final String code;
 	/** Code language */
 	public final String language;
 	/** Owner snippet of the code object */
 	public final Long snippetId;
 	/** Version of this code object, auto incrementing */
 	private final int version;
 
 	/** If the code has a downloadable source */
 	// TODO Implement downloadable source
 	private final boolean downloadAbleSource = false;
 
 	/** Identifier of this code segment */
 	private long id = 0L;
 
 	/**
 	 * Constructor of a new code object
 	 * 
 	 * @param code
 	 * @param language
 	 * @param snippetId
 	 * @param id
 	 *            of the object. If null, the id has not been assigned from the
 	 *            persistence yet
 	 * @param version
 	 */
 	Code(String code, String language, Long snippetId, Long id, int version) {
 		if (code.length() == 0)
 			throw new IllegalArgumentException("Cannot create snippet with no code");
 		if (language.length() == 0)
 			throw new IllegalArgumentException("No coding language defined");
 		this.code = formatCode(code);
 		this.language = language;
 		this.snippetId = snippetId;
 		this.version = version;
 
 		// If the id is null, it has not been assigned from the peristence yet
 		if (id != null) {
 			this.id = id;
 		}
 	}
 
 	public boolean equals(Code code) {
 		if (code == null)
 			return false;
 		if (code.version != this.version)
 			return false;
 		return this.code.equals(code.code);
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (obj == null)
 			return false;
 		if (obj instanceof Code)
 			return equals((Code) obj);
 
 		// This String equals is used in Snippet.edit
 		if (obj instanceof String)
 			return this.code.equals(obj);
 		return false;
 	}
 
 	@Override
 	public int hashCode() {
 		return code.hashCode();
 	}
 
 	@Override
 	public String toString() {
 		return code;
 	}
 
 	/**
 	 * @return the formatted and highlighted code in HTML
 	 */
 	public abstract String getFormattedHTML();
 
 	/**
 	 * Formats the code. Must be implemented in the corresponding concrete code
 	 * class
 	 * 
 	 * @param code
 	 *            Raw
 	 * @return the formatted code
 	 */
 	protected abstract String formatCode(String code);
 
 	/**
 	 * Gets the code of the snippet in plaintext.
 	 * 
 	 * @return the code in plaintext
 	 */
 	public String getCode() {
 		return code;
 	}
 
 	/**
 	 * @return the code language of the code
 	 */
 	public String getLanguage() {
 		return language;
 	}
 
 	/**
 	 * @return current version of this code object
 	 */
 	public int getVersion() {
 		return version;
 	}
 
 	/**
 	 * Creates a new code object with the given code and the language.
 	 * 
 	 * The method automatically detects the concrete class by inspecting the
 	 * language. If the given language is not supported, a new
 	 * {@link UnsupportedLanguageException} is thrown. The inspection is not
 	 * case-sensitive.
 	 * 
 	 * @param code
 	 *            concrete code. Must not be null or empty
 	 * @param language
 	 *            Coding language. It must be supported by the system, otherwise
 	 *            a new {@link UnsupportedLanguageException} is thrown.
 	 * @param owner
 	 *            The owner snippet of the code
 	 * @return The newly generated code object
 	 * @throws UnsupportedLanguageException
 	 *             Thrown if the given language is not supported
 	 * @throws IOException
 	 *             Thrown, if occuring during database access
 	 * @throws NullPointerException
 	 *             Thrown if the code or the language or the snippet is null
 	 * @throws IllegalArgumentException
 	 *             Thrown if the code or if the language is empty
 	 */
 	// TODO add Version
 	public static Code createCode(String code, String language, Long ownerSnippetId, int version) {
 		if (code == null || language == null)
 			throw new NullPointerException();
 		if (code.isEmpty())
 			throw new IllegalArgumentException("Cannot create code object with no code");
 		if (language.isEmpty())
 			throw new IllegalArgumentException("Cannot create code object with no language");
 		if (ownerSnippetId == null)
 			throw new NullPointerException("Cannot create code segment without a snippet");
 
 		language = language.trim();
 
 		// build always a generic code object
 		Code result = new CodeGeneric(code, language, ownerSnippetId, null, version);
 
 		addToDB(result);
 		return result;
 	}
 
 	/**
 	 * Creates a new code object with the given code and the language.
 	 * 
 	 * This method should only be used by the database to create object in the
 	 * application layer!
 	 * 
 	 * The method automatically detects the concrete class by inspecting the
 	 * language. If the given language is not supported, a new
 	 * {@link UnsupportedLanguageException} is thrown. The inspection is not
 	 * case-sensitive.
 	 * 
 	 * @param code
 	 *            concrete code. Must not be null or empty
 	 * @param language
 	 *            Coding language. It must be supported by the system, otherwise
 	 *            a new {@link UnsupportedLanguageException} is thrown.
 	 * @param ownerSnippetId
 	 *            The owner snippet of the code
 	 * @param id
 	 *            the identifier
 	 * @param version
 	 *            the version number of the code
 	 * @param downloadableSourceName
 	 *            the filename of the downloadable code
 	 * @return The newly generated code object
 	 * @throws UnsupportedLanguageException
 	 *             Thrown if the given language is not supported
 	 * @throws IOException
 	 *             Thrown, if occuring during database access
 	 * @throws NullPointerException
 	 *             Thrown if the code or the language or the snippet is null
 	 * @throws IllegalArgumentException
 	 *             Thrown if the code or if the language is empty
 	 */
 	public static Code createCodeDB(String code, String language, Long ownerSnippetId, Long id, int version, String downloadableSourceName) {
 		if (code == null || language == null)
 			throw new NullPointerException();
 		if (code.isEmpty())
 			throw new IllegalArgumentException("Cannot create code object with no code");
 		if (language.isEmpty())
 			throw new IllegalArgumentException("Cannot create code object with no language");
 		if (ownerSnippetId == null)
 			throw new NullPointerException("Cannot create code segment without a snippet");
 		if (downloadableSourceName != null) {
 			// FIXME downloadable code not implemented in the core
 			// this.downloadAbleSource = true;
 		}
 
 		language = language.trim();
 
 		// build always a generic code object
 		Code result = new CodeGeneric(code, language, ownerSnippetId, null, 0);
 
 		// language = language.trim().toLowerCase();
 		//
 		// /* Here the language inspection takes place */
 		// Code result = null;
 		// if (language.equals("java")) { // Java object
 		// result = new CodeJava(code, owner, id, version);
 		// }
 		//
 		// // Failback mode: Use CodeText
 		// if (result == null)
 		// result = new CodeText(code, language, owner, id, version);
 
 		/*
 		 * THIS METHOD IS CALLED FROM THE DB, DO NOT WRITE INTO THE DB!!
 		 */
 
 		return result;
 	}
 
 	/**
 	 * @return the identifier object of this code object
 	 * @deprecated Because of name convention. Use getHashID() instant.
 	 */
 	@Deprecated
 	public long getID() {
 		return getHashID();
 	}
 
 	/**
 	 * @return the identifier object of this code object
 	 */
 	public long getHashID() {
 		return id;
 	}
 
 	/**
 	 * Adds
 	 */
 	protected static synchronized void addToDB(Code code) {
 		if (code == null)
 			return;
 
 		try {
			code.id = Persistence.instance.writeCode(code, IPersistence.DB_NEW_ONLY);
 		} catch (IOException e) {
 			System.err.println("IOException writing out code object with id=" + code.getHashID() + ": " + e.getMessage());
 			e.printStackTrace(System.err);
 		}
 	}
 
 	/**
 	 * @return the associated snippet to the code
 	 */
 	public Snippet getSnippet() {
 		if (this.snippetId == null) {
 			return null;
 		}
 		try {
 			return Persistence.instance.getSnippet(snippetId);
 		} catch (IOException e) {
 			System.err.println("IOException reading snippet object with id=" + this.snippetId + ": " + e.getMessage());
 			e.printStackTrace(System.err);
 			return null;
 		}
 	}
 
 	/**
 	 * @return the snippetId
 	 */
 	public Long getSnippetId() {
 		return this.snippetId;
 	}
 
 	/**
 	 * @return if there is a downloadable source code
 	 */
 	public boolean hasDownloadableSource() {
 		return downloadAbleSource;
 	}
 
 	/**
 	 * @return the downloadable source or null, if not existing
 	 */
 	public Byte[] getDownloadableSource() {
 		if (!hasDownloadableSource())
 			return null;
 
 		try {
 			File file = Persistence.getInstance().getCodeFile(this.id);
 			return file.getContent();
 
 		} catch (IOException e) {
 			System.err.println("IOException during getting source from code (id=" + this.id + "): " + e.getMessage());
 			e.printStackTrace(System.err);
 
 			return null;
 		}
 	}
 
 	/**
 	 * Adds a source code file to this code segment
 	 */
 	public void applySourceCode(String filename) throws IOException {
 		writeCodeFile(this.id, filename);
 	}
 
 	/**
 	 * Writes a file to a code object
 	 * 
 	 * @param codeID
 	 *            id of the code object
 	 * @param fileName
 	 *            File to be written
 	 */
 	public static void writeCodeFile(long codeID, String filename) throws IOException {
 
 		File file = new File(File.getContents(filename), filename);
 		Persistence.getInstance().writeCodeFile(codeID, file, IPersistence.DB_DEFAULT);
 	}
 
 	/**
 	 * Creates a {@link XCode} object out of this object
 	 * 
 	 * @return created {@link XCode} object
 	 */
 	public XCode toXCode() {
 		XCode result = new XCode();
 
 		result.code = code;
 		result.codeHTML = getFormattedHTML();
 		result.language = getLanguage();
 		result.downloadAbleSource = downloadAbleSource;
 		result.id = id;
 		result.snippetId = snippetId;
 		result.version = version;
 
 		return result;
 	}
 
 	/**
 	 * Get code object from DB
 	 * 
 	 * @param codeID
 	 *            id of the code object to fetch
 	 * @return the fetched code object or null if not exitisting or on error
 	 */
 	public static Code getCode(long codeID) {
 		try {
 			return Persistence.getInstance().getCode(codeID);
 		} catch (IOException e) {
 			System.err.println("IOException during fetch of code object with id = " + codeID + ": " + e.getMessage());
 			e.printStackTrace(System.err);
 			return null;
 		}
 	}
 }
