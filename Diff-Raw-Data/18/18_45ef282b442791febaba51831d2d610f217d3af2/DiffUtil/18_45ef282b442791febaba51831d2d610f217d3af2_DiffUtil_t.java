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
 package org.jamwiki.utils;
 
 import java.util.List;
 import java.util.StringTokenizer;
 import java.util.Vector;
 import org.apache.log4j.Logger;
 import org.incava.util.diff.Diff;
 import org.incava.util.diff.Difference;
 import org.jamwiki.model.WikiDiff;
 
 /**
  * Utility class for creating either a text of HTML representation of the difference
  * between two files.
  */
 public class DiffUtil {
 
 	protected static Logger logger = Logger.getLogger(DiffUtil.class);
 	/** The number of lines of unchanged text to display before and after each diff. */
 	// FIXME - make this a property value
 	private static final int DIFF_UNCHANGED_LINE_DISPLAY = 2;
 
 	/**
 	 * Returned an HTML formatted table that displays a diff of two Strings.
 	 *
 	 * FIXME: return objects and parse to HTML from a JSP tag, not a class file.
 	 *
 	 * @param newVersion The String that is to be compared to, ie the later version of a topic.
 	 * @param oldVersion The String that is to be considered as having changed, ie the earlier
 	 *  version of a topic.
 	 * @return Returns a Vector of WikiDiff objects that correspond to the changed text.
 	 */
 	public static Vector diff(String newVersion, String oldVersion) {
 		if (oldVersion == null) oldVersion = "";
 		if (newVersion == null) newVersion = "";
 		if (newVersion.equals(oldVersion)) return new Vector();
 		return DiffUtil.process(newVersion, oldVersion);
 	}
 
 	/**
 	 *
 	 */
 	private static Vector process(String newVersion, String oldVersion) {
 		logger.debug("Diffing: " + oldVersion + " against: " + newVersion);
 		String[] oldArray = buildArray(oldVersion);
 		String[] newArray = buildArray(newVersion);
 		Diff diffObject = new Diff(oldArray, newArray);
 		List diffs = diffObject.diff();
 		Vector wikiDiffs = new Vector();
 		Difference currentDiff = null;
 		Difference previousDiff = null;
 		Difference nextDiff = null;
 		for (int i=0; i < diffs.size(); i++) {
 			currentDiff = (Difference)diffs.get(i);
 			preBufferDifference(currentDiff, previousDiff, wikiDiffs, oldArray, newArray);
 			processDifference(currentDiff, wikiDiffs, oldArray, newArray);
 			nextDiff = null;
 			if ((i+1) < diffs.size() ) {
 				nextDiff = (Difference)diffs.get(i+1);
 			}
 			postBufferDifference(currentDiff, nextDiff, wikiDiffs, oldArray, newArray);
 			previousDiff = currentDiff;
 		}
 		return wikiDiffs;
 	}
 
 	/**
 	 *
 	 */
 	private static void postBufferDifference(Difference currentDiff, Difference nextDiff, Vector wikiDiffs, String[] oldArray, String[] newArray) {
 		int deletedCurrent = (currentDiff.getDeletedEnd() + 1);
 		int addedCurrent = (currentDiff.getAddedEnd() + 1);
 		if (currentDiff.getDeletedEnd() == -1) {
 			deletedCurrent = (currentDiff.getDeletedStart());
 		}
 		if (currentDiff.getAddedEnd() == -1) {
 			addedCurrent = (currentDiff.getAddedStart());
 		}
 		for (int i=0; i < DIFF_UNCHANGED_LINE_DISPLAY; i++) {
 			int lineNumber = ((deletedCurrent < 0) ? 0 : deletedCurrent);
 			String oldLine = null;
 			String newLine = null;
 			boolean buffered = false;
 			if (deletedCurrent >= 0 && deletedCurrent < oldArray.length && ((nextDiff != null && nextDiff.getDeletedStart() > deletedCurrent) || nextDiff == null)) {
 				oldLine = oldArray[deletedCurrent];
 				deletedCurrent++;
 				buffered = true;
 			}
 			if (addedCurrent >= 0 && addedCurrent < newArray.length && ((nextDiff != null && nextDiff.getAddedStart() > addedCurrent) || nextDiff == null)) {
 				newLine = newArray[addedCurrent];
 				addedCurrent++;
 				buffered = true;
 			}
 			if (!buffered) continue;
 			WikiDiff wikiDiff = new WikiDiff(oldLine, newLine, lineNumber + 1, false);
 			wikiDiffs.add(wikiDiff);
 		}
 	}
 
 	/**
 	 *
 	 */
 	private static void preBufferDifference(Difference currentDiff, Difference previousDiff, Vector wikiDiffs, String[] oldArray, String[] newArray) {
 		int deletedCurrent = (currentDiff.getDeletedStart() - DIFF_UNCHANGED_LINE_DISPLAY);
 		int addedCurrent = (currentDiff.getAddedStart() - DIFF_UNCHANGED_LINE_DISPLAY);
 		if (previousDiff != null) {
			deletedCurrent = Math.max(previousDiff.getDeletedEnd() + 1, deletedCurrent);
			addedCurrent = Math.max(previousDiff.getAddedEnd() + 1, addedCurrent);
 		}
 		for (int i=0; i < DIFF_UNCHANGED_LINE_DISPLAY; i++) {
 			int lineNumber = ((deletedCurrent < 0) ? 0 : deletedCurrent);
 			String oldLine = null;
 			String newLine = null;
 			boolean buffered = false;
			// if diffs are close together, do not allow buffers to overlap
			if ((previousDiff == null || (previousDiff.getDeletedEnd() != -1 && deletedCurrent > (previousDiff.getDeletedEnd() + DIFF_UNCHANGED_LINE_DISPLAY)) || (previousDiff.getDeletedEnd() == -1 && deletedCurrent > (previousDiff.getDeletedStart() + DIFF_UNCHANGED_LINE_DISPLAY))) && deletedCurrent >= 0 && currentDiff.getDeletedStart() > deletedCurrent) {
 				oldLine = oldArray[deletedCurrent];
 				deletedCurrent++;
 				buffered = true;
 			}
			if ((previousDiff == null || (previousDiff.getAddedEnd() != -1 && addedCurrent > (previousDiff.getAddedEnd() + DIFF_UNCHANGED_LINE_DISPLAY)) || (previousDiff.getAddedEnd() == -1 && addedCurrent > (previousDiff.getAddedStart() + DIFF_UNCHANGED_LINE_DISPLAY))) && addedCurrent >= 0 && currentDiff.getAddedStart() > addedCurrent) {
 				newLine = newArray[addedCurrent];
 				addedCurrent++;
 				buffered = true;
 			}
 			if (!buffered) continue;
 			WikiDiff wikiDiff = new WikiDiff(oldLine, newLine, lineNumber + 1, false);
 			wikiDiffs.add(wikiDiff);
 		}
 	}
 
 	/**
 	 *
 	 */
 	private static void processDifference(Difference currentDiff, Vector wikiDiffs, String[] oldArray, String[] newArray) {
 		int deletedCurrent = currentDiff.getDeletedStart();
 		int addedCurrent = currentDiff.getAddedStart();
 		int count = 0;
 		logger.warn("Diff: " + currentDiff);
 		while (hasMoreDiffLines(addedCurrent, deletedCurrent, currentDiff)) {
 			int lineNumber = ((deletedCurrent < 0) ? 0 : deletedCurrent);
 			String oldLine = null;
 			String newLine = null;
 			if (currentDiff.getDeletedEnd() >= 0 && currentDiff.getDeletedEnd() >= deletedCurrent) {
 				oldLine = oldArray[deletedCurrent];
 				deletedCurrent++;
 			}
 			if (currentDiff.getAddedEnd() >= 0 && currentDiff.getAddedEnd() >= addedCurrent) {
 				newLine = newArray[addedCurrent];
 				addedCurrent++;
 			}
 			WikiDiff wikiDiff = new WikiDiff(oldLine, newLine, lineNumber + 1, true);
 			wikiDiffs.add(wikiDiff);
 			count++;
 			if (count > 500) {
 				logger.warn("Infinite loop in DiffUtils.processDifference");
 				break;
 			}
 		}
 	}
 
 	/**
 	 *
 	 */
 	private static boolean hasMoreDiffLines(int addedCurrent, int deletedCurrent, Difference currentDiff) {
 		if (addedCurrent == -1) addedCurrent = 0;
 		if (deletedCurrent == -1) deletedCurrent = 0;
 		return (addedCurrent <= currentDiff.getAddedEnd() || deletedCurrent <= currentDiff.getDeletedEnd());
 	}
 
 	/**
 	 * Split up a large String into an array of Strings made up of each line (indicated
 	 * by a newline) of the original String.
 	 */
 	private static String[] buildArray(String original) {
 		if (original == null) return null;
 		StringTokenizer tokens = new StringTokenizer(original, "\n");
 		int size = tokens.countTokens();
 		String[] array = new String[size];
 		int count = 0;
 		while (tokens.hasMoreTokens()) {
 			array[count] = tokens.nextToken();
 			count++;
 		}
 		return array;
 	}
 }
