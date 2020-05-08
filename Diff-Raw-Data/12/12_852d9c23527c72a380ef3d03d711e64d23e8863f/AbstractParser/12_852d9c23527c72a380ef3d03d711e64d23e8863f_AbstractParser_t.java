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
 
 import java.io.Reader;
 import java.util.Collection;
 import java.util.List;
 import org.jamwiki.utils.WikiLogger;
 
 /**
  * Abstract class to be used when implementing new lexers.  New lexers
  * should extend this class and override any methods that need to be
  * implemented differently.
  */
 public abstract class AbstractParser {
 
 	private static final WikiLogger logger = WikiLogger.getLogger(AbstractParser.class.getName());
 	protected ParserInput parserInput = null;
 
 	/**
 	 * Sets the basics for this parser.
 	 *
 	 * @param parserInput General information about this parser.
 	 */
 	public AbstractParser(ParserInput parserInput) {
 		this.parserInput = parserInput;
 	}
 
 	/**
 	 *
 	 */
 	public abstract String buildRedirectContent(String topicName);
 
 	/**
 	 * Utility method for executing a lexer parse.
 	 */
 	protected ParserOutput lex(AbstractLexer lexer) throws Exception {
 		this.parserInput.incrementDepth();
		// FIXME - this is a sloppy way to avoid infinite loops
		if (this.parserInput.getDepth() > 100000) {
 			throw new Exception("Infinite parsing loop - over 100 parser iterations");
 		}
 		StringBuffer content = new StringBuffer();
 		while (true) {
 			String line = lexer.yylex();
 			if (line == null) break;
 			content.append(line);
 		}
 		ParserOutput parserOutput = lexer.getParserOutput();
 		parserOutput.setContent(content.toString());
 		return parserOutput;
 	}
 
 	/**
 	 * Returns a HTML representation of the given wiki raw text for online representation.
 	 *
 	 * @param rawtext The raw Wiki syntax to be converted into HTML.
 	 */
 	public abstract ParserOutput parseHTML(String rawtext, String topicName, ParserMode mode) throws Exception;
 
 	/**
 	 * For syntax that is not saved with the topic source, this method provides
 	 * a way of parsing prior to saving.
 	 *
 	 * @param rawtext The raw Wiki syntax to be converted into HTML.
 	 * @return Results from parser execution.
 	 */
 	public abstract ParserOutput parsePreSave(String rawtext, ParserMode mode) throws Exception;
 
 	/**
 	 * When making a section edit this function provides the capability to retrieve
 	 * all text within a specific heading level.  For example, if targetSection is
 	 * specified as five, and the sixth heading is an &lt;h2&gt;, then this method
 	 * will return the heading tag and all text up to either the next &lt;h2&gt;,
 	 * &lt;h1&gt;, or the end of the document, whichever comes first.
 	 *
 	 * @param rawtext The raw Wiki text that is to be parsed.
 	 * @param topicName The name of the topic that is being parsed.
 	 * @param targetSection The section (counted from zero) that is to be returned.
 	 */
 	public abstract ParserOutput parseSlice(String rawtext, String topicName, int targetSection) throws Exception;
 
 	/**
 	 * This method provides the capability for re-integrating a section edit back
 	 * into the main topic.  The text to be re-integrated is provided along with the
 	 * full Wiki text and a targetSection.  All of the content of targetSection
 	 * is then replaced with the new text.
 	 *
 	 * @param rawtext The raw Wiki text that is to be parsed.
 	 * @param topicName The name of the topic that is being parsed.
 	 * @param targetSection The section (counted from zero) that is to be returned.
 	 * @param replacementText The text to replace the target section text with.
 	 */
 	public abstract ParserOutput parseSplice(String rawtext, String topicName, int targetSection, String replacementText) throws Exception;
 }
