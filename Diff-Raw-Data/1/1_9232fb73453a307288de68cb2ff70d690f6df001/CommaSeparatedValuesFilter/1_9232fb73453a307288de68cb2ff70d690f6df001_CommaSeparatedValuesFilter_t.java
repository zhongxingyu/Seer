 /*===========================================================================
   Copyright (C) 2008-2009 by the Okapi Framework contributors
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
 
 package net.sf.okapi.filters.table.csv;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import net.sf.okapi.common.ListUtil;
 import net.sf.okapi.common.RegexUtil;
 import net.sf.okapi.common.StringUtil;
 import net.sf.okapi.common.Util;
 import net.sf.okapi.common.resource.Code;
 import net.sf.okapi.common.resource.TextContainer;
 import net.sf.okapi.common.resource.TextFragment;
 import net.sf.okapi.common.resource.TextUnit;
 import net.sf.okapi.common.resource.TextUnitUtil;
 import net.sf.okapi.common.resource.TextFragment.TagType;
 import net.sf.okapi.common.skeleton.GenericSkeleton;
 import net.sf.okapi.common.skeleton.GenericSkeletonPart;
 import net.sf.okapi.filters.table.base.BaseTableFilter;
 import net.sf.okapi.lib.extra.filters.TextProcessingResult;
 
 /**
  * Comma-Separated Values filter. Extracts text from a comma-separated values table, 
  * optionally containing a header with field names and other info.
  * 
  * @version 0.1, 09.06.2009
  */
 public class CommaSeparatedValuesFilter  extends BaseTableFilter {
 
 	public static final String FILTER_NAME		= "okf_table_csv";
 	public static final String FILTER_CONFIG	= "okf_table_csv";
 	
 	private static String MERGE_START_TAG	= "\ue10a";
 	private static String MERGE_END_TAG		= "\ue10b";
 	private static String LINE_BREAK_TAG	= "\ue10c";
 	private static String LINE_WRAP_TAG		= "\ue10d";
 
 //	Debug
 //	private static String MERGE_START_TAG	= "_start_";
 //	private static String MERGE_END_TAG		= "_end_";
 //	private static String LINE_BREAK_TAG	= "_line_";
 //	private static String LINE_WRAP_TAG		= "_wrap_";
 	
 	private Parameters params; // CSV Filter parameters
 	private List<String> buffer;
 	private boolean merging = false;
 	private int level = 0;
 	private boolean lineFlushed = false;
	@SuppressWarnings("unused")
 	private int qualifierLen;
 	
 	public CommaSeparatedValuesFilter() {
 		
 		setName(FILTER_NAME);
 
 		addConfiguration(true, // Do not inherit configurations from Base Table Filter
 				FILTER_CONFIG,
 				"Table (Comma-Separated Values)",
 				"Comma-separated values, optional header with field names.", 
 				"okf_table_csv.fprm");
 		
 		setParameters(new Parameters());	// CSV Filter's parameters
 	}
 
 	@Override
 	protected void component_init() {
 
 		merging = false;
 		level = 0;
 		lineFlushed = false;
 		
 		params = getParameters(Parameters.class);	// Throws OkapiBadFilterParametersException
 		qualifierLen = Util.getLength(params.textQualifier);
 		
 		super.component_init();
 		
 		if (buffer == null) 
 			buffer = new ArrayList<String>();
 		else
 			buffer.clear();		
 	}
 
 	@Override
 	protected String getFieldDelimiter() {
 		
 		return params.fieldDelimiter;
 	}
 
 	@Override
 	protected TextProcessingResult extractCells(List<TextUnit> cells, TextContainer lineContainer, long lineNum) {		
 		// Extract cells from the line, if no multi-line chunks, fill up the cells list, if there are, fill the chunk buffer.
 		// The cells is always an empty non-null list ready for addition
 		
 		if (cells == null) return TextProcessingResult.REJECTED;
 		if (lineContainer == null) return TextProcessingResult.REJECTED;
 		
 		String line = lineContainer.getCodedText();
 		
 		if (Util.isEmpty(params.fieldDelimiter)) return super.extractCells(cells, lineContainer, lineNum);		
 		
 		String[] chunks;
 		if (Util.isEmpty(line)) 
 			chunks = new String[] {""};
 		else					
 			chunks = ListUtil.stringAsArray(line, params.fieldDelimiter);
 				
 		boolean allowNesting = false; 
 		
 		// Analyze chunks for being multi-line
 		for (String chunk : chunks) {
 			
 			String trimmedChunk = chunk.trim();
 														
 			if (trimmedChunk.indexOf(params.textQualifier) < 0 && !merging)
 				{buffer.add(chunk); continue;}
 							
 			int numLeadingQ;
 			int numTrailingQ;
 			
 			if (trimmedChunk.equals(params.textQualifier)) {
 				
 				if (merging) {
 					
 					numLeadingQ = 0;
 					numTrailingQ = 1;
 				}
 				else {
 					
 					numLeadingQ = 1;
 					numTrailingQ = 0;
 				}					
 			}
 			else {
 				int numQ = StringUtil.getNumOccurrences(trimmedChunk, params.textQualifier);
 				numLeadingQ = RegexUtil.countLeadingQualifiers(trimmedChunk, params.textQualifier);
 				numTrailingQ = RegexUtil.countTrailingQualifiers(trimmedChunk, params.textQualifier);
 				int numUndetectedQ = numQ - (numLeadingQ + numTrailingQ); 
 		
 				// Nested qualified fragments are allowed only within a line; when a new line is started to be analyzed, no nesting is 
 				// allowed, and an attempt to increase the level causes canceling of merging.				
 				boolean startsQualified = trimmedChunk.startsWith(params.textQualifier);
 				
 				if (merging && level > 0 && startsQualified && !allowNesting)		
 					cancelMerging();
 				
 				if (numUndetectedQ > 0)					
 					if (merging) 
 						numTrailingQ += numUndetectedQ;
 					else
 						numLeadingQ += numUndetectedQ;					
 			}
 						
 			if (merging) {
 				
 //				if (numLeadingQ == numTrailingQ) // == 0 is included
 //					{buffer.add(chunk); continue;}
 //				
 //				if (numLeadingQ > numTrailingQ)
 //					{level += numLeadingQ - numTrailingQ; buffer.add(chunk); continue;}
 //				
 //				if (numLeadingQ < numTrailingQ)
 //					{level += numLeadingQ - numTrailingQ; buffer.add(chunk); if (level <= 0) endMerging(); continue;}
 				
 				
 //					
 //				if (startsQualified && endsQualified) {		// 111
 //					
 //					if (trimmedChunk.length() == qualifierLen) // hanging qualifier
 //						{buffer.add(chunk); endMerging(); continue;}
 //					else
 //						{cancelMerging(); buffer.add(chunk); continue;}
 //				}
 				
 				int saveLevel = level;
 				
 				level += numLeadingQ - numTrailingQ;
 				boolean endsQualified = trimmedChunk.endsWith(params.textQualifier);
 				if (level == saveLevel && endsQualified && numTrailingQ == 0) level--;
 				
 				buffer.add(chunk);
 				if (numLeadingQ <= numTrailingQ)
 					if (level <= 0) 
 						endMerging();
 				//continue;
 			}
 			else {
 				
 //				if (numLeadingQ == numTrailingQ) // == 0 is included
 //					{buffer.add(chunk); continue;}
 //				
 //				if (numLeadingQ > numTrailingQ)
 //					{startMerging(); level += numLeadingQ; buffer.add(chunk); continue;}
 //				
 //				if (numLeadingQ < numTrailingQ)
 //					{level -= numTrailingQ; buffer.add(chunk); continue;}
 				
 				if (numLeadingQ > numTrailingQ) {
 					
 					startMerging();
 					allowNesting = true; // Nesting of qualified fragments is allowed within a single line.
 				}
 					
 				level += numLeadingQ - numTrailingQ;
 				buffer.add(chunk);
 				//continue;
 			}
 			
 //			boolean startsQualified = trimmedChunk.startsWith(params.textQualifier); 
 //			boolean endsQualified = trimmedChunk.endsWith(params.textQualifier);
 //			
 ////			boolean evenNumQ = numQ % 2 == 0; 			
 ////			boolean startsQualified = false; 
 ////			boolean endsQualified = false;
 ////			
 ////			if (merging) {
 ////			
 ////				startsQualified = false; 
 ////
 ////				endsQualified = 
 ////					trimmedChunk.endsWith(params.textQualifier) && !evenNumQ;				
 ////			} 
 ////			else {
 ////				
 ////				startsQualified = 
 ////					(trimmedChunk.startsWith(params.textQualifier) && !evenNumQ) || (numQ > 0 && !evenNumQ);
 ////
 ////				endsQualified = 
 ////					trimmedChunk.endsWith(params.textQualifier) && evenNumQ;
 ////			}
 //				
 //			if (!merging && !startsQualified && !endsQualified)		// 000
 //				{buffer.add(chunk); continue;}
 //			
 //			if (!merging && !startsQualified && endsQualified)		// 001
 //				{buffer.add(chunk); continue;}
 //				
 //			if (!merging && startsQualified && !endsQualified)		// 010
 //				{startMerging(); buffer.add(chunk); continue;}
 //				
 //			if (!merging && startsQualified && endsQualified)		// 011
 //				{buffer.add(chunk); continue;}
 //				
 //			if (merging && !startsQualified && !endsQualified)		// 100
 //				{buffer.add(chunk); continue;}
 //				
 //			if (merging && !startsQualified && endsQualified)		// 101
 //				{buffer.add(chunk); endMerging(); continue;}
 //				
 //			if (merging && startsQualified && !endsQualified)		// 110
 //				{cancelMerging(); startMerging(); buffer.add(chunk); continue;}
 //				
 //			if (merging && startsQualified && endsQualified) {		// 111
 //				
 //				if (trimmedChunk.length() == qualifierLen) // hanging qualifier
 //					{buffer.add(chunk); endMerging(); continue;}
 //				else
 //					{cancelMerging(); buffer.add(chunk); continue;}
 //			}
 							
 		}
 		
 		buffer.add(LINE_BREAK_TAG);
 		buffer.add(String.valueOf(lineNum));
 		
 		processBuffer(false);
 		
 		return TextProcessingResult.DELAYED_DECISION;			
 	}
 
 	@Override
 	protected boolean processTU(TextUnit textUnit) {
 	
 		if (textUnit == null) return false;
 		
 		TextUnitUtil.trimTU(textUnit, true, true);
 		TextUnitUtil.removeQualifiers(textUnit, params.textQualifier);
 		
 		// Process wrapped lines
 		// We can use getFirstPartContent() because nothing is segmented
 		TextFragment src = textUnit.getSource().getFirstContent();
 		String cell = src.getCodedText();
 		
 		List<String> list = ListUtil.stringAsList(cell, LINE_WRAP_TAG);
 		
 		if (list.size() > 1) {
 			
 			src.setCodedText("");
 			
 			for (int i = 0; i < list.size(); i++) {
 				
 				String st = list.get(i);
 				
 				src.append(st);				
 				if (i == list.size() - 1) break;
 				
 				switch (params.wrapMode) {
 				
 				case PLACEHOLDERS:
 					src.append(new Code(TagType.PLACEHOLDER, "line break", getLineBreak()));
 					break;
 					
 				case SPACES:
 					src.append(' ');
 					break;
 					
 				case NONE:
 				default:
 					src.append('\n');
 				}
 			}			
 		}
 //		else
 //			src.setCodedText(cell); // No line wrappers found 
 
 		// Change 2 quotes inside the field to one quote (2 adjacent quotes in CSV are part of quoted text, not field qualifiers)
 		String st = src.getCodedText();		
 		String qq = params.textQualifier + params.textQualifier;
 		
 		int start = 0; // abs index
 		do {			
 			int index = st.indexOf(qq); // rel index
 			if (index == -1) break;
 			
 			src.changeToCode(start + index, start + index + 1, TagType.PLACEHOLDER, "CSV quote preamble"); // First quotation mark
 			
 			start += index + 3; // Code takes 2 positions			
 			st = src.getCodedText().substring(start); // To make sure we're synchronized
 		} while (true);
 
 		
 		return super.processTU(textUnit);
 	}
 		
 	@Override
 	protected void component_idle(boolean lastChance) {
 		
 		super.component_idle(lastChance);
 		processBuffer(lastChance);		
 	}
 	
 	@Override
 	protected void component_done() {
 				
 		super.component_done();
 	}	
 	
 	private void startMerging() {
 
 		if (merging) return;
 		
 		buffer.add(MERGE_START_TAG);
 		merging = true;
 		level = 0;
 	}
 	
 	private void endMerging() {
 		
 		if (!merging) return;
 		
 		buffer.add(MERGE_END_TAG);
 		merging = false;
 		level = 0;
 	}
 	
 	private void cancelMerging() {
 
 		if (!merging) return;
 		
 		// Remove the last merging start marker
 		int start = buffer.lastIndexOf(MERGE_START_TAG);
 		int end = buffer.lastIndexOf(MERGE_END_TAG);
 		
 		if (Util.checkIndex(start, buffer) && (end == -1 || (end > -1 && end < start)))
 			buffer.remove(start);
 		
 		merging = false;
 		level = 0;
 	}
 	
 	private void processBuffer(boolean forceEnding) {
 		// Scans the buffer for a line, merges chunks, removes and returns the line's chunks
 		
 		if (buffer == null) return;
 		if (buffer.isEmpty()) {
 			
 //			if (forceEnding)
 //				removeLineBreak();
 			
 			return;
 		}
 		
 		int start = -1;
 		int end = -1;
 		
 		// Locate ready merging areas, merge them, and remove contained line breaks				
 		while (true) {
 			
 			start = buffer.indexOf(MERGE_START_TAG);
 			end = buffer.indexOf(MERGE_END_TAG);
 			
 			if (start == -1 || end == -1) break;
 			if (start >= end) break;
 			
 			List<String> buf = ListUtil.copyItems(buffer, start + 1, end - 1);
 
 			while (true) {
 				int index = buf.indexOf(LINE_BREAK_TAG);		
 				if (index == -1) break;
 		
 				buf.set(index, LINE_WRAP_TAG); 
 				if (Util.checkIndex(index + 1, buf)) buf.remove(index + 1); // Line num
 			}
 			
 			while (true) {
 				int index = buf.indexOf(LINE_WRAP_TAG);
 				
 				if (index == -1) break;
 				if (!Util.checkIndex(index - 1, buf)) break;
 				if (!Util.checkIndex(index + 1, buf)) break;
 		
 				String mergedChunk = ListUtil.listAsString(buf.subList(index - 1, index + 2), "");
 				buf.subList(index, index + 2).clear();
 				
 				buf.set(index - 1, mergedChunk);
 			}
 			
 			String mergedChunk = ListUtil.listAsString(buf, params.fieldDelimiter);
 			
 			buffer.subList(start + 1, end + 1).clear();
 			
 			buffer.set(start, mergedChunk);			
 
 		}
 		
 //		if (!(start == -1 && end == -1)) return;
 		
 		// Extract a line
 		int index = buffer.indexOf(LINE_BREAK_TAG);
 		
 		if (forceEnding) {
 			// Remove hanging start tag
 			
 			if (start > -1 && index > -1 && index > start) {
 				buffer.remove(start);
 				index--;
 			}
 			
 		}
 		else
 			if (index >= start && start > -1) return;
 		
 		if (!Util.checkIndex(index, buffer)) return; // = -1, no complete line of chunks
 		if (!Util.checkIndex(index + 1, buffer)) return; // No line num item 
 		long lineNum = new Long(buffer.get(index + 1));
 		
 		buffer.remove(index); // Line break tag
 		buffer.remove(index); // Line num
 
 		if (index == 0) return; // No chunks before line break tag 
 		
 		// Transfer chunks to a temp buffer, process
 		
 //		List<String> buf0 = ListUtil.moveItems(buffer, 0, index - 1);
 		
 //		List<String> buf = new ArrayList<String>();
 //		buf.addAll(buffer.subList(0, index));		
 //		buffer.subList(0, index).clear();
 		
 //		addLineBreak(); // Insert a line break after the previous line
 		
 		List<TextUnit> buf = new ArrayList<TextUnit>();
 		
 		for (int i = 0; i < index; i++)			
 			buf.add(TextUnitUtil.buildTU(buffer.get(i)));
 		
 		buffer.subList(0, index).clear();
 		
 		if (lineFlushed && !forceEnding)
 			// We cannot add line break when forceEnding=true, as there's no event in the queue to provide a skeleton
 			addLineBreak();
 		
 		if (forceEnding)
 			getQueueSize();
 		
 		processCells(buf, lineNum);
 		lineFlushed = true;
 		
 		
 		if (lineFlushed && forceEnding) {
 		
 			TextUnit tu = getFirstTextUnit();
 			if (tu == null) return;
 			
 			GenericSkeleton skel = (GenericSkeleton) tu.getSkeleton();
 			if (skel != null) {
 			
 				List <GenericSkeletonPart> parts = skel.getParts();				
 				parts.add(0, new GenericSkeletonPart(getLineBreak()));
 			}
 		}
 		
 		
 	}
 	
 }
