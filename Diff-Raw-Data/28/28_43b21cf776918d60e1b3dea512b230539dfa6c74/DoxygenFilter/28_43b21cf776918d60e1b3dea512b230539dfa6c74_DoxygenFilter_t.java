 /*===========================================================================
   Copyright (C) 2008-2012 by the Okapi Framework contributors
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
 
 package net.sf.okapi.filters.doxygen;
 
 import java.io.BufferedReader;
 import java.io.IOException;
import java.util.EmptyStackException;
 import java.util.IdentityHashMap;
 import java.util.Map.Entry;
 import java.util.Stack;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import net.sf.okapi.common.BOMNewlineEncodingDetector;
 import net.sf.okapi.common.Event;
 import net.sf.okapi.common.IParameters;
 import net.sf.okapi.common.UsingParameters;
 import net.sf.okapi.common.encoder.EncoderManager;
 import net.sf.okapi.common.exceptions.OkapiIOException;
 import net.sf.okapi.common.filters.AbstractFilter;
 import net.sf.okapi.common.filters.FilterConfiguration;
 import net.sf.okapi.common.filters.IFilter;
 import net.sf.okapi.common.filterwriter.IFilterWriter;
 import net.sf.okapi.common.resource.Code;
 import net.sf.okapi.common.resource.Property;
 import net.sf.okapi.common.resource.RawDocument;
 import net.sf.okapi.common.resource.TextFragment.TagType;
 import net.sf.okapi.common.skeleton.GenericSkeleton;
 import net.sf.okapi.filters.doxygen.DoxygenParameter.ParameterLength;
 import static net.sf.okapi.filters.doxygen.DoxygenPatterns.*;
 
 /**
  * {@link IFilter} for a Doxygen-commented text file
  * 
  * @author Aaron Madlon-Kay
  */
 @UsingParameters(Parameters.class)
 public class DoxygenFilter extends AbstractFilter {
 
 	public static final String DOXYGEN_MIME_TYPE = "text/x-doxygen-txt";
 	
 	public static final String NUMLINES_PROPERTY = "numLines";
 
 	private final Logger LOGGER = LoggerFactory.getLogger(getClass());
 
 	private String linebreak = "\n";
 	private WhitespaceAdjustingEventBuilder eventBuilder;
 	private EncoderManager encoderManager;
 	private boolean hasUtf8Bom;
 	private boolean hasUtf8Encoding;
 	private RawDocument currentRawDocument;
 	private BOMNewlineEncodingDetector detector;
 	private Parameters params;
 	
 	private StringBuilder commentBuffer;
 	private PrefixSuffixTokenizer commentTokenizer;
 
 	private IdentityHashMap<Pattern, Object> commandPatterns;
 	
 	public DoxygenFilter()
 	{
 		super();
 
 		setMimeType(DOXYGEN_MIME_TYPE);
 		setMultilingual(false);
 		setFilterWriter(new DoxygenWriter());	
 		// Cannot use '_' or '-' in name: conflicts with other filters (e.g. plaintext, table)
 		// for defining different configurations
 		setName("okf_doxygen"); //$NON-NLS-1$
 		setDisplayName("Doxygen Filter"); //$NON-NLS-1$
 		addConfiguration(new FilterConfiguration(
 				getName(),
 				DOXYGEN_MIME_TYPE,
 				getClass().getName(),
 				"Doxygen-commented Text",
 				"Doxygen-commented Text Documents",
 				Parameters.DOXYGEN_PARAMETERS,
 				".h;.c;.cpp;.java;.py;.m;"));
 		setParameters(new Parameters());
 	}
 
 	@Override
 	public IFilterWriter createFilterWriter()
 	{
 		return super.createFilterWriter();
 	}
 
 	@Override
 	public void open(RawDocument input)
 	{
 		open(input, true);
 	}
 
 	@Override
 	public void open(RawDocument input, boolean generateSkeleton)
 	{
 		// close any previous streams we opened
 		close();
 
 		this.currentRawDocument = input;
 		
 		if (input.getInputURI() != null)
 			setDocumentName(input.getInputURI().getPath());
 
 		detector = new BOMNewlineEncodingDetector(input.getStream(), input.getEncoding());
 		detector.detectAndRemoveBom();
 
 		setEncoding(input.getEncoding());
 		hasUtf8Bom = detector.hasUtf8Bom();
 		hasUtf8Encoding = detector.hasUtf8Encoding();
 		linebreak = detector.getNewlineType().toString();
 		setNewlineType(linebreak);
 		
 		commentBuffer = new StringBuilder();
 
 		// set encoding to the user setting
 		String detectedEncoding = getEncoding();
 
 		// may need to override encoding based on what we detect
 		if (detector.isDefinitive()) {
 			detectedEncoding = detector.getEncoding();
 			LOGGER.debug(String.format(
 					"Overridding user set encoding (if any). Setting auto-detected encoding (%s).",
 					detectedEncoding));
 		} else if (!detector.isDefinitive() && getEncoding().equals(RawDocument.UNKOWN_ENCODING)) {
 			detectedEncoding = detector.getEncoding();
 			LOGGER.debug(String.format(
 					"Default encoding and detected encoding not found. Using best guess encoding (%s)",
 					detectedEncoding));
 		}
 
 		input.setEncoding(detectedEncoding);
 		setEncoding(detectedEncoding);
 		setOptions(input.getSourceLocale(), input.getTargetLocale(), detectedEncoding,
 				generateSkeleton);
 		
 		// Read in entire file right now
 		BufferedReader reader = new BufferedReader(input.getReader());
 		StringBuilder builder = new StringBuilder();
 		try {
 			for (String line = reader.readLine(); line != null; line = reader.readLine())
 				builder.append(line + linebreak);
 		} catch (IOException e) {
 			throw new OkapiIOException("IO error reading Doxygen-commented file", e);
 		}
 		try {
 			reader.close();
 		} catch (IOException e) {
 			LOGGER.warn("Error closing the Doxygen-commented text buffered reader.", e);
 		}
 		
 		commentTokenizer = new PrefixSuffixTokenizer(tokenizerDelimiters, builder.toString());
 		
 		// create EventBuilder with document name as rootId
 		if (eventBuilder == null) {
 			eventBuilder = new WhitespaceAdjustingEventBuilder();
 		} else {
 			eventBuilder.reset(null, this);
 		}
 		
 		eventBuilder.addFilterEvent(createStartFilterEvent());
 		eventBuilder.setPreserveWhitespace(params.isPreserveWhitespace());
 		
 		commandPatterns = new IdentityHashMap<Pattern, Object>();
 		for (Entry<Pattern, Object> e : params.getCustomCommandPatterns().entrySet())
 			commandPatterns.put(e.getKey(), null);
 		commandPatterns.put(DOXYGEN_COMMAND_PATTERN, null);
 	}
 
 	@Override
 	public void close()
 	{
 		if (currentRawDocument != null) {
 			currentRawDocument.close();
 		}
 	}
 
 	@Override
 	public EncoderManager getEncoderManager()
 	{
 		if (encoderManager == null) {
 			encoderManager = new EncoderManager();
 			encoderManager.setMapping(DOXYGEN_MIME_TYPE,
 					"net.sf.okapi.common.encoder.DefaultEncoder");
 		}
 		return encoderManager;
 	}
 
 	@Override
 	public IParameters getParameters()
 	{
 		return params;
 	}
 
 	@Override
 	public void setParameters(IParameters params)
 	{
 		this.params = (Parameters) params;
 	}
 
 	@Override
 	public boolean hasNext()
 	{
 		return eventBuilder.hasNext();
 	}
 
 	
 	@Override
 	public Event next()
 	{		
 		// process queued up events before we produce more
 		while (eventBuilder.hasQueuedEvents())
 			return eventBuilder.next();
 
 		parse();
 		
 		return eventBuilder.next();
 	}
 	
 	/**
 	 * Parse the input file.
 	 * 
 	 * Top-level parse function; called by next().
 	 * Iterates over the input file one line at a time.
 	 */
 	private void parse()
 	{
 		
 		for (PrefixSuffixTokenizer.Token t : commentTokenizer) {
 			
 			if (isCanceled()) return;
 			
 			if (t.prefixPattern() == null) {
 				// First portion of file
 				parseNonCommentString(t.toString());
 				
 			} else if (t.prefixPattern() == CPP_COMMENT_PREFIX_PATTERN) {
 				// Single-line comment
 				parseSingleCommentLine(t);
 			
 			} else if (t.prefixPattern() == CPP_COMMENT_SUFFIX_PATTERN) {
 				// After single-line comment
 				if (t.suffixPattern() == CPP_COMMENT_PREFIX_PATTERN && !t.toString().contains("\n")) {
 					// This is a skeleton part
 					GenericSkeleton skel = (GenericSkeleton) eventBuilder.peekMostRecentGroup().getSkeleton();
 					skel.append(t.toString());
 				}
 				else parseNonCommentString(t.toString());
 				
 			} else if (t.prefixPattern() == JAVADOC_COMMENT_PREFIX_PATTERN
 					|| t.prefixPattern() == PYTHON_SINGLE_COMMENT_PREFIX_PATTERN
 					|| t.prefixPattern() == PYTHON_DOUBLE_COMMENT_PREFIX_PATTERN) {
 				// Multi-line comment
 				parseMultiLineComment(t);
 			
 			} else {
 				// After end of comment, or untranslatable token
 				parseNonCommentString(t.prefix() + t.toString());
 			}
 			
 			if (eventBuilder.hasQueuedEvents()) return;
 		}
 		
 		// Reached the end of the file.
 		
 		if (commentBuffer.length() != 0) parseComment();
 		
 		// Close last unfinished TU and group, if present.
 		if (eventBuilder.isCurrentTextUnit()) eventBuilder.endTextUnit();
 		if (eventBuilder.isCurrentGroup()) eventBuilder.endGroup(null);
 		
 		eventBuilder.flushRemainingTempEvents();
 		
 		eventBuilder.addFilterEvent(createEndFilterEvent());
 	}
 
 	private void parseNonCommentString(String line) {
 		
 		// We've exited a comment section. Parse accumulated comment and end group.
 		if (commentBuffer.length() != 0) parseComment();
 		
 		eventBuilder.addDocumentPart(line);
 	}
 
 	/**
 	 * Parse a line containing (part of) a Doxygen comment.
 	 * The content is accumulated in commentBuffer, to be processed upon
 	 * encountering something other than another single-line comment.
 	 * @param t Token for this line
 	 */
 	private void parseSingleCommentLine(PrefixSuffixTokenizer.Token t)
 	{
 		// If we haven't started a group yet, start one now.
 		if (!eventBuilder.isCurrentGroup()) eventBuilder.startGroup(new GenericSkeleton(), null);
 		
 		GenericSkeleton skel = (GenericSkeleton) eventBuilder.peekMostRecentGroup().getSkeleton();
 		skel.append(t.prefix());
 		skel.flushPart();
 		
 		commentBuffer.append(t.toString() + t.suffix());
 	}
 	
 	/**
 	 * Parse a full multiline Doxygen comment.
 	 * @param t Token for this line
 	 */
 	private void parseMultiLineComment(PrefixSuffixTokenizer.Token t)
 	{
 		if (commentBuffer.length() != 0) parseComment();
 		
 		if (!eventBuilder.isCurrentGroup()) eventBuilder.startGroup(new GenericSkeleton(), null);
 		
 		GenericSkeleton skel = (GenericSkeleton) eventBuilder.peekMostRecentGroup().getSkeleton();
 		
 		skel.append(t.prefix());
 		
 		DelimiterTokenizer tokenizer = new DelimiterTokenizer(MULTILINE_DECORATION_PATTERN, t.toString());
 		
 		String firstDelimiter = null;
 		
 		int numLines = 0;
 		
 		for (DelimiterTokenizer.Token u : tokenizer) {
 			
 			String body = u.toString();
 			
 			if (u.delimiter() != null) {
 				
 				String delimiter = u.delimiter();
 				
 				if (firstDelimiter == null && delimiter.length() > 0) {
 					firstDelimiter = delimiter;
 				  // Split long decoration
 				} else if (firstDelimiter != null && delimiter.length() > firstDelimiter.length()) {
 					body = delimiter.substring(firstDelimiter.length()) + body;
 					delimiter = delimiter.substring(0, firstDelimiter.length());
 				}
 				
 				skel.append(delimiter);
 				skel.flushPart();
 				
 				numLines++;
 			}
 			
 			commentBuffer.append(body);
 		}
 		
 		eventBuilder.peekMostRecentGroup().setProperty(
 				new Property(NUMLINES_PROPERTY, new Integer(numLines).toString()));
 		
 		parseComment();
 	}
 	
 	
 	/**
 	 * Parse a fully extracted Doxygen comment block.
 	 * 
 	 * Takes the contents from commentBuffer, then resets the buffer.
 	 * 
 	 * Splits the text into chunks, e.g. special Doxygen markup that indicates structure
 	 * such as member comments (///<) and itemized lists (/// - and /// -#), for further
 	 * processing.
 	 */
 	private void parseComment()
 	{
 		String text = commentBuffer.toString();
 		commentBuffer.setLength(0);
 		
 		DelimiterTokenizer tokenizer = new DelimiterTokenizer(DOXYGEN_CHUNK_PATTERN, text);
 		// Delimiter: "block"-indicating Doxygen markup, e.g. "///< "
 		// Token: block content
 		
 		for (DelimiterTokenizer.Token t : tokenizer) {
 			
 			GenericSkeleton skel = null;
 			if (t.delimiter() != null) skel = new GenericSkeleton(t.delimiter());
 			
 			String chunk = t.toString();
 			
 			if (skel == null && chunk.length() == 0) continue;
 			
 			eventBuilder.startTextUnit(skel);
 			parseCommentChunk(t.toString());
 			eventBuilder.endTextUnit();
 		}
 		
 		if (eventBuilder.isCurrentGroup()) eventBuilder.endGroup(null);
 	}
 	
 
 	/**
 	 * Parse a chunk of Doxygen text.
 	 * 
 	 * Scans the text for Doxygen commands, then parses subchunk by subchunk, intelligently
 	 * handling the commands and their parameters.
 	 */
 	private void parseCommentChunk(String chunk)
 	{
 		
 		Stack<DoxygenCommand> commandStack = new Stack<DoxygenCommand>();
 		
 		DelimiterTokenizer tokenizer = new DelimiterTokenizer(commandPatterns, chunk);
 		// Delimiter: Doxygen command, e.g. "\param"
 		// Token: text following command
 		
 		for (DelimiterTokenizer.Token t : tokenizer) {
 			
 			if (t.delimiter() == null) {
 				parsePlainText(t.toString());
 				continue;
 			}
 			
 			String cmd = WhitespaceAdjustingEventBuilder.collapseWhitespace(t.delimiter());
 			
 			if (t.delimiterPattern() == DOXYGEN_COMMAND_PATTERN
 					&& !isDoxygenCommand(cmd)) {
 				// Wasn't a Doxygen command
 				LOGGER.warn("Invalid Doxygen command: " + cmd);
 				eventBuilder.addToTextUnit(cmd);
 				parsePlainText(t.toString());
 				continue;
 			}
 			
 			DoxygenCommand cmdInfo = commandInfo(cmd, t.delimiterPattern());
 			
 			// See if we're inside a non-translatable command
 			if (!eventBuilder.peekMostRecentTextUnit().isTranslatable()
 				&& !commandStack.isEmpty()) {
 				
 				String pair = commandStack.peek().getPair();
 				if (pair != null && !pair.equals(cmdInfo.getName())) {
 					eventBuilder.addToTextUnit(cmd);
 					eventBuilder.addToTextUnit(t.toString());
 					continue;
 				}
 			}
 			
			Code code = new Code(cmdInfo.getTagType(), cmdInfo.getCanonicalName(), cmd);
 			
 			if (cmdInfo.getTagType() == TagType.CLOSING && !cmdInfo.isInline()) {
 				// Encountered a closing tag.
 				
 				eventBuilder.addToTextUnit(code);
 				eventBuilder.endTextUnit();
 				eventBuilder.startTextUnit();
 				
				try {
					commandStack.pop();
				} catch (EmptyStackException e) {
					LOGGER.warn("Orphaned end command: " + cmd);
				}
 				
 			} else if (cmdInfo.isInline()) {
 				// Append inline (usually placeholder) tag.
 				eventBuilder.addToTextUnit(code);
 			
 			} else {
 				// Encountered a structure tag.
 				
 				if (eventBuilder.canStartNewTextUnit()) {
 					// Start a new text unit.
 					eventBuilder.startTextUnit();
 					
 				} else if (eventBuilder.peekMostRecentTextUnit().getSource().hasText(false)) {
 					// Current TU has content. End TU and start a new one.
 					eventBuilder.endTextUnit();
 					eventBuilder.startTextUnit();
 				}
 				
 				eventBuilder.addToTextUnit(code);
 				
 				eventBuilder.peekMostRecentTextUnit().setIsTranslatable(cmdInfo.isTranslatable());
 				eventBuilder.peekMostRecentTextUnit().setPreserveWhitespaces(cmdInfo.isPreserveWhitespace());
 			
 				if (cmdInfo.getTagType() == TagType.OPENING) commandStack.push(cmdInfo);
 			}
 			
 			String remainingText = parseParameters(cmdInfo, t.toString(), code);
 			
 			if (!cmdInfo.isTranslatable() || cmdInfo.isPreserveWhitespace()) {
 				// Do not manipulate untranslatable text or text where we must preserve whitespace.
 				eventBuilder.addToTextUnit(remainingText);
 			} else {
 				parsePlainText(remainingText);
 			}
 		}
		
		assert(commandStack.isEmpty());
 	}
 
 	/**
 	 * Parse the parameters of a Doxygen command.
 	 * 
 	 * Extracts strings of appropriate length from the supplied text in order to satisfy
 	 * each of the parameters described by cmd.
 	 * 
 	 * @param cmd Information about the Doxygen command in question.
 	 * @param text Text to extract the parameters from.
 	 * @param code The code to append parameters to, if applicable (can be null).
 	 * @return Remaining text that didn't match any parameters.
 	 */
 	private String parseParameters(DoxygenCommand cmd, String text, Code code) {
 		
 		if (!cmd.hasParameters()) return text;
 		
 		boolean canAddToCode = true;
 		
 		for (DoxygenParameter p : cmd) {
 			
 			ParameterExtractor extractor = new ParameterExtractor(p, text);
 			
 			text = extractor.remainder();
 			
 			if (!extractor.hasParameter() && !cmd.isInline()) {
 				code.append(extractor.frontWhitespace());
 				break;
 			}
 			
 			if (p.isTranslatable()) {
 				// Add parameter to content of current TextUnit.
 				eventBuilder.addToTextUnit(extractor.parameter());
 				
 				// Can no longer add to the code, as subsequent parameters would
 				// be out of order.
 				canAddToCode = false;
 				
 			} else if (canAddToCode) {
 				// Add non-translatable parameter to the Code that was passed in.
 				code.append(extractor.parameter());
 			
 			} else {
 				// Already added a translatable parameter as plain text, so subsequent parameters
 				// must take the form of Codes with type PLACEHOLDER.
 				eventBuilder.addToTextUnit(
 						new Code(TagType.PLACEHOLDER, cmd.getCanonicalName(), extractor.parameter()));
 			}
 			
 			if (p.length() == ParameterLength.PARAGRAPH
 					|| p.length() == ParameterLength.LINE) {
 				eventBuilder.endTextUnit();
 				eventBuilder.startTextUnit();
 				canAddToCode = false;
 			}
 		}
 		
 		if (canAddToCode && !cmd.isInline()) {
 			Matcher m = ParameterExtractor.FRONT_WHITESPACE_PATTERN.matcher(text);
 			m.find();
 			code.append(m.group());
 			text = text.substring(m.end());
 		}
 		
 		return text;
 	}
 	
 	
 	/**
 	 * Parse a plain text string and give it to the eventBuilder.
 	 * 
 	 * This separates out paragraphs by searching for blank lines. Should only be called on
 	 * Doxygen comment text that is known not to contain Doxygen commands.
 	 * 
 	 * @param text Plain text to parse.
 	 */
 	private void parsePlainText(String text) {
 		
 		if (text.length() == 0) return;
 		
 		DelimiterTokenizer tokenizer = new DelimiterTokenizer(BLANK_LINES_PATTERN, text);
 		// Delimiter: paragraph-separating blank lines
 		// Token: paragraph content
 		
 		for (DelimiterTokenizer.Token t : tokenizer) {
 			
 			if (t.delimiter() != null) {
 				eventBuilder.addToTextUnit(t.delimiter());
 				
 				if (eventBuilder.peekMostRecentTextUnit().getSource().hasText(false)) {
 					eventBuilder.endTextUnit();
 					eventBuilder.startTextUnit();
 				}
 			}
 			
 			eventBuilder.addToTextUnit(t.toString());
 		}
 		
 	}
 	
 
 	private boolean isDoxygenCommand(String cmd) {
 		return params.isDoxygenCommand(cmd);
 	}
 	
 	
 	private DoxygenCommand commandInfo(String cmd, Pattern p) {
 		return params.commandInfo(cmd, p);
 	}
 	
 
 	@Override
 	protected boolean isUtf8Bom()
 	{
 		return hasUtf8Bom;
 	}
 
 	@Override
 	protected boolean isUtf8Encoding()
 	{
 		return hasUtf8Encoding;
 	}
 
 }
