 /**
  * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, version 2.1, dated February 1999.
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the latest version of the GNU Lesser General
  * Public License as published by the Free Software Foundation;
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program (LICENSE.txt); if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
 package org.jamwiki.parser;
 
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import org.apache.commons.lang.StringUtils;
 import org.jamwiki.Environment;
 import org.jamwiki.utils.WikiLogger;
 import org.jamwiki.utils.Utilities;
 
 /**
  * This class is used to generate a table of contents based on values passed in
  * through the parser.
  */
 public class TableOfContents {
 
 	private static final WikiLogger logger = WikiLogger.getLogger(TableOfContents.class.getName());
 	/**
 	 * Status indicating that this TOC object has not yet been initialized.  For the JFlex parser
 	 * this will mean no __TOC__ tag has been added to the document being parsed.
 	 */
 	public static final int STATUS_TOC_UNINITIALIZED = 0;
 	/**
 	 * Status indicating that this TOC object has been initialized.  For the JFlex parser this
 	 * will mean a __TOC__ tag has been added to the document being parsed.
 	 */
 	public static final int STATUS_TOC_INITIALIZED = 1;
 	/** Status indicating that the document being parsed does not allow a table of contents. */
 	public static final int STATUS_NO_TOC = 2;
 	/** Force a TOC to appear */
 	private boolean forceTOC = false;
 	/** It is possible for a user to include more than one "TOC" tag in a document, so keep count. */
 	private int insertTagCount = 0;
 	/** Keep track of how many times the parser attempts to insert the TOC (one per "TOC" tag) */
 	private int insertionAttempt = 0;
 	/**
 	 * minLevel holds the minimum TOC heading level that is being displayed by the current TOC.  For
 	 * example, if the TOC contains only h3 and h4 entries, this value would be 3.
 	 */
 	private int minLevel = 4;
 	private final Map entries = new LinkedHashMap();
 	private int status = STATUS_TOC_UNINITIALIZED;
 	/** The minimum number of headings that must be present for a TOC to appear, unless forceTOC is set to true. */
 	private static final int MINIMUM_HEADINGS = 4;
 	/**
 	 * Keep track of the TOC prefix to display.  This array is initialized with all ones, and each element
 	 * is then incremented as the TOC is displayed.
 	 */
 	private int[] tocPrefixes = null;
 
 	/**
 	 * Add a new table of contents entry to the table of contents object.
 	 * The entry should contain the name to use in the HTML anchor tag,
 	 * the text to display in the table of contents, and the indentation
 	 * level for the entry within the table of contents.
 	 *
 	 * @param name The name of the entry, to be used in the anchor tag name.
 	 * @param text The text to display for the table of contents entry.
 	 * @param level The level of the entry.  If an entry is a sub-heading of
 	 *  another entry the value should be 2.  If there is a sub-heading of that
 	 *  entry then its value would be 3, and so forth.
 	 */
 	public void addEntry(String name, String text, int level) {
 		if (this.status != STATUS_NO_TOC && this.status != STATUS_TOC_INITIALIZED) {
 			this.setStatus(STATUS_TOC_INITIALIZED);
 		}
 		name = this.checkForUniqueName(name);
 		TableOfContentsEntry entry = new TableOfContentsEntry(name, text, level);
 		this.entries.put(name, entry);
 		if (level < minLevel) {
 			minLevel = level;
 		}
 	}
 
 	/**
 	 * This method checks to see if a TOC is allowed to be inserted, and if so
 	 * returns an HTML representation of the TOC.
 	 *
 	 * @return An HTML representation of the current table of contents object,
 	 *  or an empty string if the table of contents can not be inserted due
 	 *  to an inadequate number of entries or some other reason.
 	 */
 	public String attemptTOCInsertion() {
 		this.insertionAttempt++;
 		if (this.size() == 0 || (this.size() < MINIMUM_HEADINGS && !this.forceTOC)) {
 			// too few headings
 			return "";
 		}
 		if (this.getStatus() == TableOfContents.STATUS_NO_TOC) {
 			// TOC disallowed
 			return "";
 		}
 		if (!Environment.getBooleanValue(Environment.PROP_PARSER_TOC)) {
 			// TOC turned off for the wiki
 			return "";
 		}
 		if (this.insertionAttempt < this.insertTagCount) {
 			// user specified a TOC location, only insert there
 			return "";
 		}
 		return this.toHTML();
 	}
 
 	/**
 	 * Verify the the TOC name is unique.  If it is already in use append
 	 * a numerical suffix onto it.
 	 *
 	 * @param name The name to use in the TOC, unless it is already in use.
 	 * @return A unique name for use in the TOC, of the form "name" or "name_1"
 	 *  if "name" is already in use.
 	 */
 	public String checkForUniqueName(String name) {
 		if (StringUtils.isBlank(name)) {
 			name = "empty";
 		}
 		int count = 0;
 		String candidate = name;
 		while (count < 1000) {
 			if (this.entries.get(candidate) == null) {
 				return candidate;
 			}
 			count++;
 			candidate = name + "_" + count;
 		}
 		logger.warning("Unable to find appropriate TOC name after " + count + " iterations for value " + name);
 		return candidate;
 	}
 
 	/**
 	 * Internal method to close any list tags prior to adding the next entry.
 	 */
 	private void closeList(int level, StringBuffer text, int previousLevel) {
 		for (int i = previousLevel; i > level; i--) {
 			// close lists to current level
 			text.append("</li>\n</ul>");
 		}
 	}
 
 	/**
 	 * Return the current table of contents status, such as "no table of contents
 	 * allowed" or "uninitialized".
 	 *
 	 * @return The current status of this table of contents object.
 	 */
 	public int getStatus() {
 		return this.status;
 	}
 
 	/**
 	 *
 	 */
 	private String nextTocPrefix(int depth) {
 		// initialize the tocPrefixes value for display
 		int maxDepth = Environment.getIntValue(Environment.PROP_PARSER_TOC_DEPTH);
 		if (this.tocPrefixes == null) {
 			// initialize the prefix array
 			this.tocPrefixes = new int[maxDepth];
 			for (int i = 0; i < maxDepth; i++) {
 				this.tocPrefixes[i] = 0;
 			}
 		}
 		// increment current element
 		this.tocPrefixes[depth] = this.tocPrefixes[depth] + 1;
 		// clear out all lower elements
 		for (int i = depth + 1; i < maxDepth; i++) {
 			this.tocPrefixes[i] = 0;
 		}
 		// generate next prefix of the form 1.1.1
 		String prefix = new Integer(this.tocPrefixes[0]).toString();
 		for (int i = 1; i <= depth; i++) {
 			prefix += "." + this.tocPrefixes[i];
 		}
 		return prefix;
 	}
 
 	/**
 	 * Internal method to open any list tags prior to adding the next entry.
 	 */
 	private void openList(int level, StringBuffer text, int previousLevel) {
 		if (level <= previousLevel) {
 			// same or lower level as previous item, close previous and open new
 			text.append("</li>\n<li>");
 			return;
 		}
 		for (int i = previousLevel; i < level; i++) {
 			// open lists to current level
 			text.append("<ul>\n<li>");
 		}
 	}
 
 	/**
 	 * Force a TOC to appear, even if there are fewer than four headings.
 	 *
 	 * @param forceTOC Set to <code>true</code> if a TOC is being forced
 	 *  to appear, false otherwise.
 	 */
 	public void setForceTOC(boolean forceTOC) {
 		this.forceTOC = forceTOC;
 	}
 
 	/**
 	 * Set the current table of contents status, such as "no table of contents
 	 * allowed" or "uninitialized".
 	 *
 	 * @param status The current status of this table of contents object.
 	 */
 	public void setStatus(int status) {
 		if (status == STATUS_TOC_INITIALIZED) {
 			// keep track of how many TOC insertion tags are present
 			this.insertTagCount++;
 		}
 		this.status = status;
 	}
 
 	/**
 	 * Return the number of entries in this TOC object.
 	 *
 	 * @return The number of entries in this table of contents object.
 	 */
 	public int size() {
 		return this.entries.size();
 	}
 
 	/**
 	 * Return an HTML representation of this table of contents object.
 	 *
 	 * @return An HTML representation of this table of contents object.
 	 */
 	public String toHTML() {
 		StringBuffer text = new StringBuffer();
 		text.append("<table id=\"toc\">\n<tr>\n<td>\n");
 		TableOfContentsEntry entry = null;
 		int adjustedLevel = 0;
 		int previousLevel = 0;
 		Iterator tocIterator = this.entries.values().iterator();
 		while (tocIterator.hasNext()) {
 			entry = (TableOfContentsEntry)tocIterator.next();
 			// adjusted level determines how far to indent the list
 			adjustedLevel = ((entry.level - minLevel) + 1);
 			// cannot increase TOC indent level more than one level at a time
 			if (adjustedLevel > (previousLevel + 1)) {
 				adjustedLevel = previousLevel + 1;
 			}
 			if (adjustedLevel <= Environment.getIntValue(Environment.PROP_PARSER_TOC_DEPTH)) {
 				// only display if not nested deeper than max
 				closeList(adjustedLevel, text, previousLevel);
 				openList(adjustedLevel, text, previousLevel);
 				text.append("<a href=\"#").append(Utilities.encodeAndEscapeTopicName(entry.name)).append("\">");
 				text.append("<span class=\"tocnumber\">").append(this.nextTocPrefix(adjustedLevel - 1)).append("</span> ");
 				text.append("<span class=\"toctext\">").append(entry.text).append("</span></a>");
				previousLevel = adjustedLevel;
 			}
 		}
 		closeList(0, text, previousLevel);
 		text.append("\n</td>\n</tr>\n</table>\n");
 		return text.toString();
 	}
 
 	/**
 	 * Inner class holds TOC entries until they can be processed for display.
 	 */
 	class TableOfContentsEntry {
 
 		int level;
 		String name;
 		String text;
 
 		/**
 		 *
 		 */
 		TableOfContentsEntry(String name, String text, int level) {
 			this.name = name;
 			this.text = text;
 			this.level = level;
 		}
 	}
 }
