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
 
 package net.sf.okapi.filters.plaintext.paragraphs;
 
 import java.util.LinkedList;
 
 import net.sf.okapi.common.resource.Code;
 import net.sf.okapi.common.resource.TextContainer;
 import net.sf.okapi.common.resource.TextFragment.TagType;
 import net.sf.okapi.filters.plaintext.base.BasePlainTextFilter;
 import net.sf.okapi.filters.plaintext.common.AbstractLineFilter;
 import net.sf.okapi.filters.plaintext.common.TextProcessingResult;
 import net.sf.okapi.filters.plaintext.common.TextUnitUtils;
 
 /**
  *  The filter breaks text into paragraphs and sends them as text units.
  *  The filter is aware of the following line terminators:
  * <ul><li>Carriage return character followed immediately by a newline character ("\r\n")
  * <li>Newline (line feed) character ("\n")
  * <li>Stand-alone carriage return character ("\r")</ul><p> 
  * 
  * @version 0.1, 09.06.2009
  */
 
 public class ParaPlainTextFilter extends BasePlainTextFilter{
 
 	public static final String FILTER_NAME			= "okf_plaintext_paragraphs";	
 	public static final String FILTER_CONFIG		= "okf_plaintext_paragraphs";
 	public static final String FILTER_CONFIG_LINES	= "okf_plaintext_paragraphs_lines";
 	
 	private Parameters params; // Plain Text Filter's parameters
 	private LinkedList<TextContainer> bufferedLines;
 	private boolean merging = false;
 	
 //	public void component_create() {
 //		
 //		super.component_create();
 		
 	public ParaPlainTextFilter() {
 		
 		setName(FILTER_NAME);
 		setParameters(new Parameters());	// Para Plain Text Filter parameters
 
 		addConfiguration(true,
 				FILTER_CONFIG,
 				"Plain Text (Paragraphs)",
				"Text files. Text is extracted by paragraphs (groups of lines separated by 1 or more empty lines).", 
 				"okf_plaintext_paragraphs.fprm");
 		
 		addConfiguration(false,
 				FILTER_CONFIG_LINES,
 				"Plain Text (Lines)",
				"Text is extracted by lines (every line is considered a separate paragraph).", 
 				"okf_plaintext_paragraphs_lines.fprm");
 	}
 
 	@Override
 	protected void component_init() {
 		
 		// Commons, should be included in all descendants introducing own params
 		params = getParameters(Parameters.class);	// Throws OkapiBadFilterParametersException		
 		super.component_init();		// Have the ancestor initialize its part in params  
 		
 		// Initialization		
 		if (!params.extractParagraphs) return;
 			
 		if (bufferedLines == null) 
 			bufferedLines = new LinkedList<TextContainer>();
 		else
 			bufferedLines.clear();
 	}
 	
 	@Override
 	protected TextProcessingResult component_exec(TextContainer lineContainer) {
 		
 		if (bufferedLines == null || !params.extractParagraphs) return super.component_exec(lineContainer);
 						
 		if (!TextUnitUtils.isEmpty(lineContainer)) {
 			
 			merging = true;
 			
 			bufferedLines.add(lineContainer);
 			return TextProcessingResult.DELAYED_DECISION;
 		}
 		else {
 			// Empty line, merge accumulated lines and send out
 			
 			if (merging) {
 				
 				merging = false;
 				return (mergeLines(true)) ? TextProcessingResult.ACCEPTED : TextProcessingResult.REJECTED;
 			}
 			
 			return super.component_exec(lineContainer);
 		}				
 	}
 
 	
 	@Override
 	protected void component_idle(boolean lastChance) {
 		
 		if (merging) mergeLines(false);
 		
 		super.component_idle(lastChance);
 	}
 
 	@Override
 	protected void component_done() {
 		
 //		if (merging) mergeLines();
 		
 		if (bufferedLines != null && params.extractParagraphs) 
 			bufferedLines.clear();
 		
 		merging = false;
 		
 		super.component_done();
 	}
 
 	private boolean mergeLines(boolean addLinebreak) {
 		
 		if (!params.extractParagraphs) return false; 			
 		if (bufferedLines == null) return false; 
 		if (bufferedLines.isEmpty()) return false;
 						
 		TextContainer mergedLine = new TextContainer();
 		
 		while (bufferedLines.size() > 0) {
 			
 			TextContainer curLine = bufferedLines.poll();
 					
 			if (mergedLine.isEmpty()) // Paragraph's first line
 				mergedLine.setProperty(curLine.getProperty(AbstractLineFilter.LINE_NUMBER));
 			else {
 		
 				switch (params.wrapMode) {
 				
 				case PLACEHOLDERS:
 					mergedLine.append(new Code(TagType.PLACEHOLDER, "line break", getLineBreak()));
 					break;
 					
 				case SPACES:
 					mergedLine.append(' ');
 					break;
 					
 				case NONE:
 				default:
 					mergedLine.append('\n');
 				}				
 			}
 							
 			mergedLine.append(curLine);
 		}
 		
 		sendAsSource(mergedLine);
 		
 		if (addLinebreak) 
 			sendSkeletonPart(getLineBreak());
 				
 		return true;		
 	}
 	
 }
