 /**
  * Copyright (c) 2011, Mikael Svahn, Softhouse Consulting AB
  * 
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so:
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 package se.softhouse.garden.orchid.commons.text.loader;
 
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Properties;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * A message cache that loads strings from bare files in a directory structure.
  * The file names have the following format
  * key_[language]_[country]_[variant].[ext]
  * 
  * If the [ext] equals properties the files is read as a property and insert
  * into the property tree. Note that the location and name of the property file
  * will be inserted into the message key.
  * 
  * The getMessage operations returns the found content or if a none leaf node is
  * queried the children of that node is returned.
  * 
  * This class is not thread safe.
  * 
  * @author Mikael Svahn
  * 
  */
 public class OrchidDirectoryMessageCache<T> {
 
 	private static final Pattern FILE_PATTERN = Pattern.compile("([^_.]*)_?([^.]*)\\.?(.*)");
 	private static final Pattern LOCALE_PATTERN = Pattern.compile("([^_.]*)_?([^.]*)_?([^.]*)");
 
 	private final OrchidDirectoryMessageTree cachedTree;
 	private final Map<String, Map<String, T>> cachedMessages;
 	private String charsetName = "UTF-8";
 	private MessageFactory<T> messageFactory;
 
 	/**
 	 * Creates a cache instance
 	 */
 	public OrchidDirectoryMessageCache() {
 		this.cachedMessages = new HashMap<String, Map<String, T>>();
 		this.cachedTree = new OrchidDirectoryMessageTree();
 	}
 
 	/**
 	 * Sets the charset to use when reading the files
 	 * 
 	 * @param charsetName
 	 *            The name of the {@linkplain java.nio.charset.Charset charset}.
 	 */
 	public void setCharsetName(String charsetName) {
 		this.charsetName = charsetName;
 	}
 
 	/**
 	 * Returns the charset
 	 */
 	public String getCharsetName() {
 		return this.charsetName;
 	}
 
 	/**
 	 * Sets the message factory to use
 	 */
 	public void setMessageFactory(MessageFactory<T> messageFactory) {
 		this.messageFactory = messageFactory;
 	}
 
 	/**
 	 * Returns the current message factory
 	 */
 	public MessageFactory<T> getMessageFactory() {
 		return this.messageFactory;
 	}
 
 	/**
 	 * Clears the cache, nothing new is read
 	 */
 	public void clear() {
 		this.cachedMessages.clear();
 	}
 
 	/**
 	 * Load messages into the cache from the specified directory
 	 * 
 	 * @param dir
 	 *            The directory to read from
 	 * @throws IOException
 	 */
 	public void load(File dir) throws IOException {
 		loadAllMessages("", dir);
 	}
 
 	/**
 	 * Returns the content of the message with the specified code.
 	 * 
 	 * @param code
 	 *            A case sensitive key for the message
 	 * @return The found message or null if none was found
 	 */
 	public T getMessage(String code) {
 		return getMessage(code, Locale.getDefault());
 	}
 
 	/**
 	 * Returns the content of the message with the specified code and locale
 	 * 
 	 * @param code
 	 *            A case sensitive key for the message
 	 * @param locale
 	 *            The locale to look for.
 	 * 
 	 * @return The found message or null if none was found
 	 */
 	public T getMessage(String code, Locale locale) {
 		return getMessageFromCache(code, locale);
 	}
 
 	/**
 	 * Returns the content of the message with the specified code and locale
 	 * 
 	 * @param code
 	 *            A case sensitive key for the message
 	 * @param locale
 	 *            The locale to look for.
 	 * 
 	 * @return The found message or null if none was found
 	 */
 	protected T getMessageFromCache(String code, Locale locale) {
 		List<String> localeKeys = calculateLocaleKeys(locale);
 		for (String localeKey : localeKeys) {
 			Map<String, T> map = this.cachedMessages.get(localeKey);
 			if (map != null) {
 				T message = map.get(code);
 				if (message != null) {
 					return message;
 				}
 			}
 		}
 		String list = this.cachedTree.getMessage(code, locale);
 		if (list != null) {
 			return this.messageFactory.createMessage(list, locale);
 		}
 		return null;
 	}
 
 	/**
 	 * Add the specified message to the cache,
 	 * 
 	 * @param code
 	 *            The code of the message
 	 * @param locale
 	 *            The localte of the message
 	 * @param message
 	 *            The content of the message
 	 * 
 	 * @return The message
 	 */
 	protected T addToCache(String code, String locale, T message) {
 		Map<String, T> map = this.cachedMessages.get(locale);
 		if (map == null) {
 			map = new HashMap<String, T>();
 			this.cachedMessages.put(locale, map);
 		}
 		map.put(code, message);
 		this.cachedTree.addMessage(code, locale);
 		return message;
 	}
 
 	/**
 	 * Reads the specified file and returns its content as a string.
 	 * 
 	 * @param file
 	 *            The file to read
 	 * 
 	 * @return The read string
 	 * @throws IOException
 	 */
 	protected String readFileAsString(File file) throws IOException {
 		DataInputStream dis = new DataInputStream(new FileInputStream(file));
 		try {
 			long len = file.length();
 			if (len > Integer.MAX_VALUE) {
 				throw new IOException("File " + file + " too large, was " + len + " bytes.");
 			}
 			byte[] bytes = new byte[(int) len];
 			dis.readFully(bytes);
 			return new String(bytes, this.charsetName);
 		} finally {
 			dis.close();
 		}
 	}
 
 	/**
 	 * Calculates the locale keys accoring to the following prio list.
 	 * 
 	 * 1. language_country_variant <br>
 	 * 2. language_country <br>
 	 * 3. language <br>
 	 * 4. language__variant <br>
 	 * 5. _country_variant <br>
 	 * 6. _country <br>
 	 * 7. __variant <br>
 	 * 8. <br>
 	 * 
 	 * @param locale
 	 * @return
 	 */
 	protected List<String> calculateLocaleKeys(Locale locale) {
 		List<String> result = new ArrayList<String>(4);
 		String language = locale.getLanguage().toLowerCase();
 		String country = locale.getCountry().toLowerCase();
 		String variant = locale.getVariant().toLowerCase();
 		boolean hasLanguage = language.length() > 0;
 		boolean hasCountry = country.length() > 0;
 		boolean hasVariant = variant.length() > 0;
 
 		if (hasLanguage && hasCountry && hasVariant) {
 			result.add(new StringBuilder().append(language).append("_").append(country).append("_").append(variant).toString());
 		}
 		if (hasLanguage && hasCountry) {
 			result.add(new StringBuilder().append(language).append("_").append(country).toString());
 		}
 		if (hasLanguage && hasVariant) {
 			result.add(new StringBuilder().append(language).append("__").append(variant).toString());
 		}
 		if (hasLanguage) {
 			result.add(new StringBuilder().append(language).toString());
 		}
 		if (hasCountry && hasVariant) {
 			result.add(new StringBuilder().append("_").append(country).append("_").append(variant).toString());
 		}
 		if (hasCountry) {
 			result.add(new StringBuilder().append("_").append(country).toString());
 		}
 		if (hasVariant) {
 			result.add(new StringBuilder().append("__").append(variant).toString());
 		}
 		result.add("");
 
 		return result;
 	}
 
 	/**
 	 * Load all messages in the specified dir and sub dirs into the package.
 	 * 
 	 * @param pkg
 	 *            The prefix to add to the code
 	 * @param dir
 	 *            The dir to read
 	 * @throws IOException
 	 */
 	protected void loadAllMessages(String pkg, File dir) throws IOException {
 		ArrayList<File> dirs = new ArrayList<File>();
 		File[] listFiles = dir.listFiles();
 		if (listFiles != null) {
 			for (File file : listFiles) {
 				if (file.isFile()) {
 					loadMessageFromFile(pkg, file);
 				} else if (file.isDirectory()) {
 					dirs.add(file);
 				}
 			}
 		}
 		for (File file : dirs) {
 			loadAllMessages(getPackage(pkg, file.getName()), file);
 		}
 	}
 
 	/**
 	 * Load a message from the specified file into the package.
 	 * 
 	 * @param pkg
 	 *            The prefix to add to the code
 	 * @param file
 	 *            The file to read
 	 * @throws IOException
 	 */
 	protected void loadMessageFromFile(String pkg, File file) throws IOException {
 		Matcher matcher = FILE_PATTERN.matcher(file.getName());
 		if (matcher.matches()) {
 			String code = matcher.group(1);
			String localeCode = matcher.group(2);
 			String ext = matcher.group(3);
 
 			Locale locale = Locale.getDefault();
 			Matcher localeMatcher = LOCALE_PATTERN.matcher(localeCode);
 			if (localeMatcher.matches()) {
 				locale = new Locale(localeMatcher.group(1), localeMatcher.group(2), localeMatcher.group(3));
 			}
 
 			if ("properties".equals(ext)) {
 				loadMessageFromPropertyFile(getPackage(pkg, code), file, localeCode, locale);
 			} else {
 				String fileAsString = readFileAsString(file);
 				addToCache(getPackage(pkg, code), localeCode, this.messageFactory.createMessage(fileAsString, locale));
 			}
 		}
 	}
 
 	/**
 	 * Load all messages from the specified property file into the package.
 	 * 
 	 * @param pkg
 	 *            The prefix to add to the code
 	 * @param file
 	 *            The file to read
 	 * @param localeCode
 	 *            The locale to load the messages into
 	 * @throws IOException
 	 */
 	protected void loadMessageFromPropertyFile(String pkg, File file, String localeCode, Locale locale) throws IOException {
 		Properties props = new Properties();
 		FileInputStream in = new FileInputStream(file);
 		props.load(in);
 		in.close();
 		Set<Entry<Object, Object>> entrySet = props.entrySet();
 		for (Entry<Object, Object> entry : entrySet) {
 			addToCache(getPackage(pkg, (String) entry.getKey()), localeCode, this.messageFactory.createMessage((String) entry.getValue(), locale));
 		}
 	}
 
 	/**
 	 * Create a package name by concat the pkg and the name.
 	 */
 	final private String getPackage(String pkg, String name) {
 		return (pkg.length() > 0 ? pkg + "." : "") + name;
 
 	}
 
 	/**
 	 * This factory class will create a message of type T from a string.
 	 */
 	public abstract static class MessageFactory<T> {
 		/**
 		 * Create a T from a String
 		 * 
 		 * @param message
 		 *            The string
 		 * @param locale
 		 *            The current locale
 		 * 
 		 * @return The created T
 		 */
 		public abstract T createMessage(String message, Locale local);
 	}
 
 }
