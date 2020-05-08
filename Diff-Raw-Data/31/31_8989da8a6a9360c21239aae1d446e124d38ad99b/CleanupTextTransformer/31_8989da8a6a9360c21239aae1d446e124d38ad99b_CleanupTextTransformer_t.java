 package com.gentics.cr.lucene.indexer.transformer;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.StringWriter;
 import java.io.UnsupportedEncodingException;
 import java.util.regex.Pattern;
 
 import org.apache.commons.io.IOUtils;
 
 import com.gentics.cr.CRResolvableBean;
 import com.gentics.cr.configuration.GenericConfiguration;
 import com.gentics.cr.exceptions.CRException;
 import com.google.common.base.CharMatcher;
 
 /**
  * Cleanup an attribute from not readable characters and not well readable
  * characters such es endless lines of point in the index pages of word
  * documents. Replace multiple occurances of newlines, tabs, spaces with only one.
  */
 public class CleanupTextTransformer extends ContentTransformer {
 
 	/**
 	 * attribute to cleanup in all beans.
 	 */
 	private String attribute = "";
 
 	/**
 	 * configuration key to read the attribute.
 	 */
 	private static final String TRANSFORMER_ATTRIBUTE_KEY = "attribute";
 
 	/**
 	 * Remove indexdots like those which can be found in TOCs of word documents.
 	 */
 	private static final String TRANSFORMER_CLEAN_TABLE_OF_INDEX_DOTS = "cleanTableOfIndexDots";
 
 	/**
 	 * Remove non printable characters (but NOT control characters).
 	 */
 	private static final String TRANSFORMER_REMOVE_NON_PRINTABLE_CHARACTERS = "removeNonPrintableCharacters";
 
 	/**
 	 * @see TRANSFORMER_CLEAN_TABLE_OF_INDEX_DOTS
 	 */
 	private boolean cleanTableOfIndexDots = true;
	
	/**
	 * Newline.
	 */
	private static final String NEWLINE_CHARACTER = System.getProperty("line.separator");
 
 	/**
 	 * @see TRANSFORMER_REMOVE_NON_PRINTABLE_CHARACTERS
 	 */
 	private boolean removeNonPrintableCharacters = true;
 
 	public CleanupTextTransformer(final GenericConfiguration config) {
 		super(config);
 		attribute = config.getString(TRANSFORMER_ATTRIBUTE_KEY);
 		if (config.getString(TRANSFORMER_CLEAN_TABLE_OF_INDEX_DOTS) != null) {
 			cleanTableOfIndexDots = config.getBoolean(TRANSFORMER_CLEAN_TABLE_OF_INDEX_DOTS);
 		}
 		if (config.getString(TRANSFORMER_REMOVE_NON_PRINTABLE_CHARACTERS) != null) {
 			removeNonPrintableCharacters = config.getBoolean(TRANSFORMER_REMOVE_NON_PRINTABLE_CHARACTERS);
 		}
 	}
 
 	@Override
 	public void processBean(CRResolvableBean bean) throws CRException {
 		if (attribute == null) {
 			LOGGER.error("No attribute for processing specified. Nothing to do here.");
 		}
 
 		String content = readAttribute(bean);
 		if (cleanTableOfIndexDots) {
 			content = cleanTableOfIndexDots(content);
 			content = cleanTableOfIndexDotsWithSpacesInBetween(content);
 		}
 		if (removeNonPrintableCharacters) {
 			content = removeNonPrintableCharacters(content);
 		}
 		content = normalizeWhiteSpaceCharacters(content);
 		content = removeWhiteSpaces(content);
 
 		bean.set(attribute, content);
 	}
 
 	private String readAttribute(final CRResolvableBean bean) {
 		Object obj = bean.get(attribute);
 		String content = "";
 		if (obj != null) {
 			if (obj instanceof byte[]) {
 				try {
 					ByteArrayInputStream stream = new ByteArrayInputStream((byte[]) obj);
 					StringWriter writer = new StringWriter();
 					IOUtils.copy(stream, writer, "UTF-8");
 					content = writer.toString();
 				} catch (UnsupportedEncodingException e) {
 					LOGGER.error("UTF-8 has to be supported.", e);
 				} catch (IOException e) {
 					LOGGER.error("Could not retrieve string from bytearray", e);
 				}
 			} else {
 				content = obj.toString();
 			}
 		}
 		return content;
 	}
 
 	/**
 	 * replace all kinds of spaces (en, nonbreaking, ..) using normal spaces.
 	 */
 	private String normalizeWhiteSpaceCharacters(final String content) {
 		return CharMatcher.WHITESPACE.and(CharMatcher.isNot(' ')).and(CharMatcher.isNot('\n')).replaceFrom(content, "");
 	}
 
 	/**
 	 * Removes newlines, blanks, tabs and replace them with only one of each.
 	 * @param content
 	 * @return
 	 * @throws IOException 
 	 */
 	private String removeWhiteSpaces(final String content) {
 		Pattern spaces = Pattern.compile("[\t ]+");
 		Pattern emptyLines = Pattern.compile("^\\s+$?", Pattern.MULTILINE);
 		Pattern newlines = Pattern.compile("\\s*\\n+");
		return newlines.matcher(emptyLines.matcher(spaces.matcher(content).replaceAll(" ")).replaceAll("")).replaceAll(NEWLINE_CHARACTER);
 	}
 
 	/**
 	 * Replace more than 3 dots with 3.
 	 **/
 	private String cleanTableOfIndexDots(final String content) {
 		return content.replaceAll("(\\.){3,}", "...");
 	}
 
 	/**
 	 * Replace occurance of ". . . " with ... 
 	 */
 	private String cleanTableOfIndexDotsWithSpacesInBetween(final String content) {
 		return content.replaceAll("(\\. ){3,}", "... ");
 	}
 
 	/**
 	 * Remove non printable characters but don't remove controlchars like newline, tabs.
 	 */
 	private String removeNonPrintableCharacters(final String input) {
 		return input.replaceAll("[^\\P{Cc}\\t\\r\\n]", "");
 	}
 
 	@Override
 	public void destroy() {
 
 	}
 
 }
