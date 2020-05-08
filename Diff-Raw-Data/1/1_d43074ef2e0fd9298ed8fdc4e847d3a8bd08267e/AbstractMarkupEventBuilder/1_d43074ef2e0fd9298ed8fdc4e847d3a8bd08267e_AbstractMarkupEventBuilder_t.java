 /*===========================================================================
   Copyright (C) 2009-2011 by the Okapi Framework contributors
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
 
 package net.sf.okapi.filters.abstractmarkup;
 
 import java.util.List;
 import java.util.regex.Pattern;
 
 import net.sf.okapi.common.Util;
 import net.sf.okapi.common.encoder.EncoderManager;
 import net.sf.okapi.common.encoder.IEncoder;
 import net.sf.okapi.common.filters.EventBuilder;
 import net.sf.okapi.common.filters.InlineCodeFinder;
 import net.sf.okapi.common.resource.Code;
 import net.sf.okapi.common.resource.TextFragment;
 import net.sf.okapi.common.resource.ITextUnit;
 
 public class AbstractMarkupEventBuilder extends EventBuilder {
 	/*
 	 * Typical whitespace space (U+0020) tab (U+0009) form feed (U+000C) line feed
 	 * (U+000A) carriage return (U+000D) zero-width space (U+200B) (IE6 does not
 	 * recognize these, they are treated as unprintable characters)
 	 */
 	private static final String WHITESPACE_REGEX = "[ \t\r\n\f\u200B]+";
 	private static final Pattern WHITESPACE_PATTERN = Pattern.compile(WHITESPACE_REGEX);
 		
 	private boolean useCodeFinder = false;
 	private InlineCodeFinder codeFinder;
 	private EncoderManager encoderManager;
 	private String encoding;
 	private String lineBreak;
 	
 	public AbstractMarkupEventBuilder(String rootId, boolean subFilter, 
 			EncoderManager encoderManager, String encoding, String lineBreak) {
 		super(rootId, subFilter);		
 		codeFinder = new InlineCodeFinder();
 		this.encoderManager = encoderManager;
 		this.encoding = encoding;
 		this.lineBreak = lineBreak;
 	}
 	
 	/**
 	 * Initializes the code finder. this must be called before the first time using it, for example 
 	 * when starting to process the inputs.
 	 * @param useCodeFinder true to use the code finder.
 	 * @param rules the string representation of the rules.
 	 */
 	public void initializeCodeFinder (boolean useCodeFinder,
 		String rules)
 	{
 		this.useCodeFinder = useCodeFinder;
 		if ( useCodeFinder ) {
 			codeFinder.fromString(rules);
 			codeFinder.compile();
 		}
 	}
 	
 	/**
 	 * Initializes the code finder. this must be called before the first time using it, for example 
 	 * when starting to process the inputs.
 	 * @param useCodeFinder true to use the code finder.
 	 * @param rules the string representation of the rules.
 	 */
 	public void initializeCodeFinder (boolean useCodeFinder,
 		List<String> rules)
 	{
 		this.useCodeFinder = useCodeFinder;
 		if ( useCodeFinder ) {
			codeFinder.reset();
 			for (String r : rules) {
 				codeFinder.addRule(r);
 			}
 			codeFinder.compile();
 		}
 	}
 
 	@Override
 	protected ITextUnit postProcessTextUnit (ITextUnit textUnit) {
 		// We can use getFirstPartContent() because nothing is segmented
 		TextFragment text = textUnit.getSource().getFirstContent();
 		// Treat the white spaces
 		text.setCodedText(normalizeHtmlText(text.getCodedText(), false, textUnit.preserveWhitespaces()));
 		// Apply the in-line codes rules if needed
 		if ( useCodeFinder ) {
 			encoderManager.setDefaultOptions(null, encoding, lineBreak);
 			encoderManager.updateEncoder(textUnit.getMimeType());
 			IEncoder encoder = encoderManager.getEncoder();
 			codeFinder.process(text);
 			// Escape inline code content
 			List<Code> codes = text.getCodes();
 			for ( Code code : codes ) {
 				// Escape the data of the new inline code (and only them)
 				if ( code.getType().equals(InlineCodeFinder.TAGTYPE) ) {										
 					code.setData(encoder.encode(code.getData(), 1));
 				}
 			}
 			
 		}
 		return textUnit;
 	}
 	
 	public String normalizeHtmlText(String text, boolean insideAttribute, boolean preserveWhitespace) {
 		// convert all entities to Unicode
 		String decodedValue = text;
 		
 		if (!preserveWhitespace) {
 			decodedValue = collapseWhitespace(decodedValue);
 			decodedValue = decodedValue.trim();
 		}
 
 		decodedValue = Util.normalizeNewlines(decodedValue);
 		return decodedValue;
 	}
 	
 	private String collapseWhitespace(String text) {
 		return WHITESPACE_PATTERN.matcher(text).replaceAll(" ");
 	}
 }
