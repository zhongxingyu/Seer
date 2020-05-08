 /*===========================================================================
   Copyright (C) 2008-2011 by the Okapi Framework contributors
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
 
 package net.sf.okapi.filters.regex;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Stack;
 import java.util.regex.MatchResult;
 import java.util.regex.Matcher;
 
 import net.sf.okapi.common.BOMNewlineEncodingDetector;
 import net.sf.okapi.common.Event;
 import net.sf.okapi.common.EventType;
 import net.sf.okapi.common.IParameters;
 import net.sf.okapi.common.IdGenerator;
 import net.sf.okapi.common.UsingParameters;
 import net.sf.okapi.common.encoder.EncoderManager;
 import net.sf.okapi.common.exceptions.OkapiIllegalFilterOperationException;
 import net.sf.okapi.common.exceptions.OkapiIOException;
 import net.sf.okapi.common.exceptions.OkapiUnsupportedEncodingException;
 import net.sf.okapi.common.filters.FilterConfiguration;
 import net.sf.okapi.common.filters.IFilter;
 import net.sf.okapi.common.filters.IFilterConfigurationMapper;
 import net.sf.okapi.common.filterwriter.GenericFilterWriter;
 import net.sf.okapi.common.filterwriter.IFilterWriter;
 import net.sf.okapi.common.LocaleId;
 import net.sf.okapi.common.resource.DocumentPart;
 import net.sf.okapi.common.resource.Ending;
 import net.sf.okapi.common.resource.RawDocument;
 import net.sf.okapi.common.resource.Property;
 import net.sf.okapi.common.resource.StartDocument;
 import net.sf.okapi.common.resource.StartGroup;
 import net.sf.okapi.common.resource.TextFragment;
 import net.sf.okapi.common.resource.ITextUnit;
 import net.sf.okapi.common.resource.TextUnit;
 import net.sf.okapi.common.skeleton.GenericSkeleton;
 import net.sf.okapi.common.skeleton.GenericSkeletonWriter;
 import net.sf.okapi.common.skeleton.ISkeletonWriter;
 
 @UsingParameters(Parameters.class)
 public class RegexFilter implements IFilter {
 
 	private static String MIMETYPE = "text/x-regex";
 	
 	private boolean canceled;
 	private Parameters params;
 	private String encoding;
 	private String inputText;
 	private Stack<StartGroup> groupStack;
 	private int tuId;
 	private IdGenerator otherId;
 	private String docName;
 	private ITextUnit tuRes;
 	private LinkedList<Event> queue;
 	private int startSearch;
 	private int startSkl;
 	private int parseState = 0;
 	private LocaleId trgLang;
 	private String lineBreak;
 	private boolean hasUTF8BOM;
 	private EncoderManager encoderManager;
 	
 	public RegexFilter () {
 		params = new Parameters();
 	}
 
 	public void cancel () {
 		canceled = true;
 	}
 
 	public void close () {
 		inputText = null;
 		parseState = 0;
 	}
 
 	public String getName () {
 		return "okf_regex";
 	}
 	
 	public String getDisplayName () {
 		return "Regex Filter";
 	}
 
 	public String getMimeType () {
 		return MIMETYPE;
 	}
 
 	public List<FilterConfiguration> getConfigurations () {
 		List<FilterConfiguration> list = new ArrayList<FilterConfiguration>();
 		list.add(new FilterConfiguration(getName(),
 			MIMETYPE,
 			getClass().getName(),
 			"Regex Default",
 			"Default Regex configuration."));
 		list.add(new FilterConfiguration(getName()+"-srt",
 			MIMETYPE,
 			getClass().getName(),
 			"SRT Sub-Titles",
 			"Configuration for SRT (Sub-Rip Text) sub-titles files.",
 			"srt.fprm"));
 		list.add(new FilterConfiguration(getName()+"-textLine",
 			MIMETYPE,
 			getClass().getName(),
 			"Text (Line=Paragraph)",
 			"Configuration for text files where each line is a text unit",
 			"textLine.fprm"));
 		list.add(new FilterConfiguration(getName()+"-textBlock",
 			MIMETYPE,
 			getClass().getName(),
 			"Text (Block=Paragraph)",
 			"Configuration for text files where text units are separated by 2 or more line-breaks.",
 			"textBlock.fprm"));
 		return list;
 	}
 
 	public EncoderManager getEncoderManager () {
 		if ( encoderManager == null ) {
 			encoderManager = new EncoderManager();
 			encoderManager.setAllKnownMappings();
 		}
 		return encoderManager;
 	}
 	
 	public IParameters getParameters () {
 		return params;
 	}
 
 	public boolean hasNext () {
 		return (parseState > 0);
 	}
 
 	public Event next () {
 		// Cancel if requested
 		if ( canceled ) {
 			parseState = 0;
 			queue.clear();
 			queue.add(new Event(EventType.CANCELED));
 		}
 		
 		// Process queue if it's not empty yet
 		if ( queue.size() > 0 ) {
 			return nextEvent();
 		}
 
 		// Get the first best match among the rules
 		// trying to match expression
 		Rule bestRule;
 		int maxPos = inputText.length() + 99;
 		int bestPosition = maxPos;
 		MatchResult result = null;
 		
 		while ( true ) {
 			bestRule = null;
 			for ( Rule rule : params.getRules() ) {
 				Matcher m = rule.pattern.matcher(inputText);
 				if ( m.find(startSearch) ) {
 					if ( m.start() < bestPosition ) {
 						bestPosition = m.start();
 						bestRule = rule;
 					}
 				}
 			}
 			
 			if ( bestRule != null ) {
 				// Get the matching result
 				Matcher m = bestRule.pattern.matcher(inputText);
 				if ( m.find(bestPosition) ) {
 					result = m.toMatchResult();
 				}
 				// Check for empty content
 				if ( result.start() == result.end() ) {
 						startSearch = result.end() + 1;
 						bestPosition = maxPos;
 						if (startSearch >= inputText.length()) {
 							startSearch--;
 							break;						
 						}
 						continue;						
 				}
 				// Check for boundary to avoid infinite loop
 				else if ( result.start() != inputText.length() ) {
 					// Process the match we just found
 					return processMatch(bestRule, result);
 				}
 				else break; // Done
 			}
 			else break; // Done
 		}
 		
 		// Else: Send end of the skeleton if needed
 		if ( startSearch < inputText.length() ) {
 			// Treat strings outside rules
 //TODO: implement extract string out of rules
 			// Send the skeleton
 			addSkeletonToQueue(inputText.substring(startSkl, inputText.length()), true);
 		}
 
 		// Any group to close automatically?
 		closeGroups();
 		
 		// End finally set the end
 		// Set the ending call
 		Ending ending = new Ending(otherId.createId());
 		queue.add(new Event(EventType.END_DOCUMENT, ending));
 		return nextEvent();
 	}
 	
 	private void closeGroups () {
 		if ( groupStack.size() > 0 ) {
 			Ending ending = new Ending(otherId.createId());
 			queue.add(new Event(EventType.END_GROUP, ending));
 			groupStack.pop();
 		}
 	}
 
 	public void open (RawDocument input) {
 		open(input, true);
 	}
 	
 	@Override
 	public void setFilterConfigurationMapper (IFilterConfigurationMapper fcMapper) {
 	}
 
 	public void setParameters (IParameters params) {
 		this.params = (Parameters)params;
 	}
 
 	public ISkeletonWriter createSkeletonWriter() {
 		return new GenericSkeletonWriter();
 	}
 
 	public IFilterWriter createFilterWriter () {
 		return new GenericFilterWriter(createSkeletonWriter(), getEncoderManager());
 	}
 
 	public void open (RawDocument input,
 		boolean generateSkeleton)
 	{
 		close(); // Just in case resources need to be freed
 	
 		BOMNewlineEncodingDetector detector = new BOMNewlineEncodingDetector(input.getStream(), input.getEncoding());
 		detector.detectAndRemoveBom();
 		input.setEncoding(detector.getEncoding());
 		encoding = input.getEncoding();
 		
 		BufferedReader reader = null;		
 		try {
 			reader = new BufferedReader(new InputStreamReader(detector.getInputStream(), encoding));
 		}
 		catch ( UnsupportedEncodingException e ) {
 			throw new OkapiUnsupportedEncodingException(
 				String.format("The encoding '%s' is not supported.", encoding), e);
 		}
 		trgLang = input.getTargetLocale();
 		hasUTF8BOM = detector.hasUtf8Bom();
 		lineBreak = detector.getNewlineType().toString();
 		if ( input.getInputURI() != null ) {
 			docName = input.getInputURI().getPath();
 		}
 		
 		//TODO: Optimize this with a better 'readToEnd()'
 		StringBuilder tmp = new StringBuilder();
 		char[] buf = new char[2048];
 		int count = 0;
 		try {
 			while (( count = reader.read(buf)) != -1 ) {
 				tmp.append(buf, 0, count);
 			}
 		}
 		catch ( IOException e ) {
 			throw new OkapiIOException("Error reading the input.", e);
 		}
 		finally {
 			if ( reader != null ) {
 				try {
 					reader.close();
 				}
 				catch ( IOException e ) {
 					throw new OkapiIOException("Error closing the input.", e);
 				}
 			}
 		}
 		
 		// Set the input string
 		inputText = tmp.toString().replace(lineBreak, "\n");
 
 		parseState = 1;
 		canceled = false;
 		groupStack = new Stack<StartGroup>();
 		startSearch = 0;
 		startSkl = 0;
 		tuId = 0;
 		otherId = new IdGenerator(null, "o");
 
 		// Prepare the filter rules
 		params.compileRules();
 
 		// Set the start event
 		queue = new LinkedList<Event>();
 		StartDocument startDoc = new StartDocument(otherId.createId());
 		startDoc.setName(docName);
 		startDoc.setEncoding(encoding, hasUTF8BOM);
 		startDoc.setLocale(input.getSourceLocale());
 		startDoc.setLineBreak(lineBreak);
 		startDoc.setFilterParameters(getParameters());
 		startDoc.setFilterWriter(createFilterWriter());
 		startDoc.setType(params.getMimeType());
 		startDoc.setMimeType(params.getMimeType());
 		startDoc.setMultilingual(hasRulesWithTarget());
 		queue.add(new Event(EventType.START_DOCUMENT, startDoc));
 	}
 	
 	private Event processMatch (Rule rule,
 		MatchResult result)
 	{
 		GenericSkeleton skel;
 		switch ( rule.ruleType ) {
 		case Rule.RULETYPE_NOTRANS:
 		case Rule.RULETYPE_COMMENT:
 			// Skeleton data is the whole expression's match
 			//TODO: line-breaks conversion!!
 			skel = new GenericSkeleton(
 				inputText.substring(startSkl, result.end()).replace("\n", lineBreak));
 			// Update starts for next read
 			startSearch = result.end();
 			startSkl = result.end();
 			// If comment: process the source content for directives
 			if ( rule.ruleType == Rule.RULETYPE_COMMENT ) {
 				params.getLocalizationDirectives().process(result.group(rule.sourceGroup));
 			}
 			// Then just return one skeleton event
 			return new Event(EventType.DOCUMENT_PART,
 				new DocumentPart(otherId.createId(), false, skel));
 			
 		case Rule.RULETYPE_OPENGROUP:
 		case Rule.RULETYPE_CLOSEGROUP:
 			// Skeleton data include the content
 			skel = new GenericSkeleton(
 				inputText.substring(startSkl, result.end()).replace("\n", lineBreak));
 			// Update starts for next read
 			startSearch = result.end();
 			startSkl = result.end();
 			if ( rule.ruleType == Rule.RULETYPE_OPENGROUP ) {
 				// See if we need to auto-close the groups
 				if ( params.getOneLevelGroups() && (groupStack.size() > 0 )) {
 					closeGroups();
 				}
 				// Start the new one
 				StartGroup startGroup = new StartGroup(null);
 				startGroup.setId(otherId.createId());
 				startGroup.setSkeleton(skel);
 				if ( rule.nameGroup != -1 ) {
 					String name = result.group(rule.nameGroup);
 					if ( name.length() > 0 ) {
 						startGroup.setName(name);
 					}
 				}
 				groupStack.push(startGroup);
 				queue.add(new Event(EventType.START_GROUP, startGroup));
 				return queue.poll();
 			}
 			else { // Close group
 				if ( groupStack.size() == 0 ) {
 					throw new OkapiIllegalFilterOperationException("Rule for closing a group detected, but no group is open.");
 				}
 				groupStack.pop();
 				Ending ending = new Ending(otherId.createId());  
 				ending.setSkeleton(skel);
 				return new Event(EventType.END_GROUP, ending);
 				
 			}
 		}
 		
 		//--- Otherwise: process the content
 
 		// Set skeleton data if needed
 		if ( result.start() > startSkl ) {
 			addSkeletonToQueue(inputText.substring(startSkl, result.start()), false);
 		}
 		startSkl = result.start();
 		
 		startSearch = result.end(); // For the next read
 		
 		// Check localization directives
 		if ( !params.getLocalizationDirectives().isLocalizable(true) ) {
 			// If not to be localized: make it a skeleton unit
 			addSkeletonToQueue(inputText.substring(startSkl, result.end()), false);
 			startSkl = result.end(); // For the next read
 			// And return
 			return nextEvent();
 		}
 
 		//--- Else: We extract
 
 
 		// Process the data, this will create a queue of events if needed
 		if ( rule.ruleType == Rule.RULETYPE_CONTENT ) {
 			processContent(rule, result);
 		}
 		else if ( rule.ruleType == Rule.RULETYPE_STRING ) {
 			// Skeleton before
 			if ( result.start(rule.sourceGroup) > result.start() ) {
 				addSkeletonToQueue(inputText.substring(startSkl, result.start(rule.sourceGroup)), false);
 			}
 			// Extract the string(s)
 			processStrings(rule, result);
 			// Skeleton after
 			if ( result.end(rule.sourceGroup) < result.end() ) {
 				addSkeletonToQueue(inputText.substring(result.end(rule.sourceGroup), result.end()), false);
 			}
 			startSkl = result.end(); // For the next read
 		}
 		return nextEvent();
 	}
 
 	private void processContent (Rule rule,
 		MatchResult result)
 	{
 		// Create the new text unit and its skeleton
 		//TODO: handle un-escaping and mime-type
 		tuRes = new TextUnit(String.valueOf(++tuId), result.group(rule.sourceGroup));
 		GenericSkeleton skel = new GenericSkeleton();
 		tuRes.setSkeleton(skel);
 		boolean hasTarget = (rule.targetGroup != -1);
 		
 		if ( hasTarget ) {
 			// Add the target data
 			tuRes.setTargetContent(trgLang, new TextFragment(result.group(rule.targetGroup)));
 			// Case of source before target
 			if ( result.start(rule.targetGroup) > result.start(rule.sourceGroup) ) {
 				// Before the source
 				if ( result.start(rule.sourceGroup) > startSkl ) {
 					skel.append(inputText.substring(
 						startSkl, result.start(rule.sourceGroup)).replace("\n", lineBreak));
 				}
 				// The source
 				skel.addContentPlaceholder(tuRes);
 				// Between the source and the target
 				skel.append(inputText.substring(
 					result.end(rule.sourceGroup), result.start(rule.targetGroup)).replace("\n", lineBreak));
 				// The target
 				skel.addContentPlaceholder(tuRes, trgLang);
 				// After the target
 				if ( result.end(rule.targetGroup) < result.end() ) {
 					skel.append(inputText.substring(
 						result.end(rule.targetGroup), result.end()).replace("\n", lineBreak));
 				}
 			}
 			else { // Case of target before the source
 				// Before the target
 				if ( result.start(rule.targetGroup) > startSkl ) {
 					skel.append(inputText.substring(
 						startSkl, result.start(rule.targetGroup)).replace("\n", lineBreak));
 				}
 				// The target
 				skel.addContentPlaceholder(tuRes, trgLang);
 				// Between the target and the source
 				skel.append(inputText.substring(
 					result.end(rule.targetGroup), result.start(rule.sourceGroup)).replace("\n", lineBreak));
 				// The source
 				skel.addContentPlaceholder(tuRes);
 				// After the source
 				if ( result.end(rule.sourceGroup) < result.end() ) {
 					skel.append(inputText.substring(
 						result.end(rule.sourceGroup), result.end()).replace("\n", lineBreak));
 				}
 			}
 		}
 		else { // No target
 			if ( result.start(rule.sourceGroup) > startSkl ) {
 				skel.append(inputText.substring(
 					startSkl, result.start(rule.sourceGroup)).replace("\n", lineBreak));
 			}
 			skel.addContentPlaceholder(tuRes);
 			if ( result.end(rule.sourceGroup) < result.end() ) {
 				skel.append(inputText.substring(
 					result.end(rule.sourceGroup), result.end()).replace("\n", lineBreak));
 			}
 		}
 
 		// Move the skeleton start for next read
 		startSkl = result.end();
 		
 		tuRes.setMimeType(MIMETYPE); //TODO: work-out something for escapes in regex
 		if ( rule.preserveWS ) {
 			tuRes.setPreserveWhitespaces(true);
 		}
 		else { // Unwrap the content
 			tuRes.getSource().unwrap(true, true);
 			if ( hasTarget ) tuRes.getTarget(trgLang, false).unwrap(true, true);
 		}
 
 		if ( rule.useCodeFinder ) {
 			// We can use getFirstPartContent() because nothing is segmented yet
 			rule.codeFinder.process(tuRes.getSource().getFirstContent());
 			if ( hasTarget ) rule.codeFinder.process(tuRes.getTarget(trgLang, false).getFirstContent());
 		}
 
 		if ( rule.nameGroup != -1 ) {
 			String name = result.group(rule.nameGroup);
 			if ( name.length() > 0 ) {
 				tuRes.setName(name);
 			}
 		}
 		
 		if ( rule.noteGroup != -1 ) {
 			String note = result.group(rule.noteGroup);
 			if ( note.length() > 0 ) {
 				tuRes.setProperty(new Property(Property.NOTE, note, true));
 			}
 		}
 
 		queue.add(new Event(EventType.TEXT_UNIT, tuRes));
 	}
 	
 	private void processStrings (Rule rule,
 		MatchResult result)
 	{
 		int i = -1;
 		int startSearch = 0;
 		int count = 0;
 		String data = result.group(rule.sourceGroup);
 		char endChar = 0;
 		int n;
 		
 		while ( true  ) {
 			int start = startSearch;
 			int end = -1;
 			int state = 0;
 
 			// Search string one by one
 			while ( end == -1 ) {
 				if ( ++i >= data.length() ) break;
 				
 				// Deal with \\, \" and \' escapes
 				if ( state > 0 ) {
 					if ( params.getUseBSlashEscape() ) {
 						while ( data.codePointAt(i) == '\\' ) {
 							if ( i+2 < data.length() ) i += 2; // Now point to next
 							else throw new OkapiIllegalFilterOperationException("Escape syntax error in ["+data+"]");
 						}
 					}
 					if ( params.getUseDoubleCharEscape() ) {
 						// Is this a string delimiter?
 						char ch = 0x00;
 						if (( n = params.getStartString().indexOf(data.codePointAt(i)) ) > -1 ) {
 							ch = params.getStartString().charAt(n);
 						}
 						else if (( n = params.getEndString().indexOf(data.codePointAt(i)) ) > -1 ) {
 							ch = params.getEndString().charAt(n);
 						}
 						if ( ch != 0x00 ) { // Check if it's doubled
 							 if ( i+1 < data.length() ) {
								 if ( ch == data.codePointAt(i+1) ) {
 									 // It is a doubled character: skip it
 									 if ( i+2 < data.length() ) i += 2; // Now point to next
 									 else throw new OkapiIllegalFilterOperationException("Escape syntax error in ["+data+"]");
 								 }
 							 }
 						}
 					}
 				}
 			
 				// Check characters
 				switch ( state ) {
 				case 0:
 					n = params.getStartString().indexOf(data.codePointAt(i));
 					if ( n > -1 ) {
 						// Start of string match found, set search info for end
 						start = i+1; // Start of the string content
 						state = 1;
 						endChar = params.getEndString().charAt(n);
 					}
 					break;
 				case 1: // Look for the end mark
 					if ( data.codePointAt(i) == endChar ) {
 						// End of string match found
 						// Set the end of the string position (will stop the loop too)
 						end = i;
 						// Check for empty strings
 						if ( end == start ) {
 							end = -1;
 							state = 0;
 						}
 					}
 					break;
 				}
 			} // End of while end == -1
 			
 			// If we have found a string: process it
 			if ( end != -1 ) {
 				count++;
 				// Skeleton part before
 				if ( start > startSearch ) {
 					addSkeletonToQueue(data.substring(startSearch, start), false);
 				}
 
 				// Item to extract
 				tuRes = new TextUnit(String.valueOf(++tuId),
 					data.substring(start, end));
 				tuRes.setMimeType(MIMETYPE); //TODO: work-out something for escapes in regex
 				if ( rule.preserveWS ) {
 					tuRes.setPreserveWhitespaces(true);
 				}
 				else { // Unwrap the string
 					tuRes.getSource().unwrap(true, true);
 				}
 				
 				if ( rule.useCodeFinder ) {
 					// We can use getFirstPartContent() because nothing is segmented yet
 					rule.codeFinder.process(tuRes.getSource().getFirstContent());
 				}
 
 				if ( rule.nameGroup != -1 ) {
 					String name = result.group(rule.nameGroup);
 					if ( name.length() > 0 ) {
 						if ( count > 1 ) { // Add a number after the first string
 							tuRes.setName(String.format("%s%d", name, count));
 						}
 						else {
 							tuRes.setName(name);
 						}
 					}
 				}
 				
 				if ( rule.noteGroup != -1 ) {
 					String note = result.group(rule.noteGroup);
 					if ( note.length() > 0 ) {
 						tuRes.setProperty(new Property(Property.NOTE, note, true));
 					}
 				}
 
 				queue.add(new Event(EventType.TEXT_UNIT, tuRes));
 				// Reset the pointers: next skeleton will start from startSearch, end reset to -1
 				startSearch = end;
 			}
 
 			// Make sure we get out of the loop if needed
 			if ( i >= data.length() ) break;
 			
 		} // End of while true
 
 		// Skeleton part after the last string
 		if ( startSearch < data.length() ) {
 			addSkeletonToQueue(data.substring(startSearch), false);
 		}
 	}
 	
 	private void addSkeletonToQueue (String data,
 		boolean forceNewEntry)
 	{
 		GenericSkeleton skel;
 		if ( !forceNewEntry && ( queue.size() > 0 )) {
 			if ( queue.getLast().getResource() instanceof DocumentPart ) {
 				// Append to the last queue entry if possible
 				skel = (GenericSkeleton)queue.getLast().getResource().getSkeleton();
 				skel.append(data.replace("\n", lineBreak));
 				return;
 			}
 		}
 		// Else: create a new skeleton entry
 		skel = new GenericSkeleton(data.replace("\n", lineBreak));
 		queue.add(new Event(EventType.DOCUMENT_PART,
 			new DocumentPart(otherId.createId(), false, skel)));
 	}
 
 	private Event nextEvent () {
 		if ( queue.size() == 0 ) return null;
 		if ( queue.peek().getEventType() == EventType.END_DOCUMENT ) {
 			parseState = 0; // No more event after
 		}
 		return queue.poll();
 	}
 
 	// Tells if at least one rule has a target
 	private boolean hasRulesWithTarget () {
 		for ( Rule rule : params.getRules() ) {
 			if ( rule.targetGroup != -1 ) return true;
 		}
 		return false;
 	}
 
 }
