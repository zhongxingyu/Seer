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
 
 import java.io.IOException;
 import java.io.Reader;
 import java.io.StringReader;
 import java.util.List;
 import org.apache.log4j.Logger;
 import org.jamwiki.Environment;
 
 /**
  * Parser used to implement MediaWiki syntax.
  */
 public class JAMWikiParser extends AbstractParser {
 
 	private static final Logger logger = Logger.getLogger(JAMWikiParser.class);
 
 	/**
 	 * Sets the basics for this parser.
 	 *
 	 * @param parserInfo General information about this parser.
 	 */
 	public JAMWikiParser(ParserInfo parserInfo) {
 		super(parserInfo);
 	}
 
 	/**
 	 * Utility method for executing a lexer parse.
 	 * FIXME - this is copy & pasted here and in VQWikiParser
 	 */
 	 protected StringBuffer lex(Lexer lexer) throws IOException {
 		StringBuffer contents = new StringBuffer();
 		while (true) {
 			String line = lexer.yylex();
 			if (line == null) break;
 			contents.append(line);
 		}
 		return contents;
 	}
 
 	/**
 	 * Parse text for online display.
 	 */
 	public String parseHTML(String rawtext, String topicName) throws IOException {
 		long start = System.currentTimeMillis();
 		StringBuffer contents = new StringBuffer();
 		Reader raw = new StringReader(rawtext.toString());
 		contents = this.parsePreProcess(raw);
 		if (this.parserInfo.getMode() != ParserInfo.MODE_NORMAL) {
 			// save or preview mode, add pre-save processor
 			String result = this.parsePreSave(contents.toString());
 			contents = new StringBuffer(result);
 		}
 		raw = new StringReader(contents.toString());
 		contents = this.parsePostProcess(raw);
 		logger.info("Parse time for " + topicName + "(" + ((System.currentTimeMillis() - start) / 1000.000) + " s.)");
 		return contents.toString();
 	}
 
 	/**
 	 *
 	 */
 	private StringBuffer parsePreProcess(Reader raw) throws IOException {
 		JAMWikiPreProcessor lexer = new JAMWikiPreProcessor(raw);
 		lexer.setParserInfo(parserInfo);
 		return this.lex(lexer);
 	}
 
 	/**
 	 * Parse MediaWiki signatures and other tags that should not be
 	 * saved as part of the topic source.
 	 *
 	 * @param raw The raw Wiki syntax to be converted into HTML.
 	 * @return HTML representation of the text for online.
 	 */
 	public String parsePreSave(String contents) throws IOException {
 		StringReader raw = new StringReader(contents);
 		JAMWikiPreSaveProcessor lexer = new JAMWikiPreSaveProcessor(raw);
 		lexer.setParserInfo(parserInfo);
 		return this.lex(lexer).toString();
 	}
 
 	/**
 	 *
 	 */
 	private StringBuffer parsePostProcess(Reader raw) throws IOException {
 		JAMWikiPostProcessor lexer = new JAMWikiPostProcessor(raw);
 		lexer.setParserInfo(parserInfo);
 		return this.lex(lexer);
 	}
 
 	/**
 	 *
 	 */
 	public String parseSlice(String rawtext, String topicName, int targetSection) throws IOException {
 		long start = System.currentTimeMillis();
 		StringBuffer contents = new StringBuffer();
 		Reader raw = new StringReader(rawtext.toString());
 		JAMWikiSpliceProcessor lexer = new JAMWikiSpliceProcessor(raw);
 		lexer.setParserInfo(parserInfo);
 		lexer.setTargetSection(targetSection);
 		contents = this.lex(lexer);
 		logger.info("Parse time for " + topicName + "(" + ((System.currentTimeMillis() - start) / 1000.000) + " s.)");
 		return contents.toString();
 	}
 
 	/**
 	 *
 	 */
 	public String parseSplice(String rawtext, String topicName, int targetSection, String replacementText) throws IOException {
 		long start = System.currentTimeMillis();
 		StringBuffer contents = new StringBuffer();
 		Reader raw = new StringReader(rawtext.toString());
 		JAMWikiSpliceProcessor lexer = new JAMWikiSpliceProcessor(raw);
 		lexer.setParserInfo(parserInfo);
 		lexer.setReplacementText(replacementText);
 		lexer.setTargetSection(targetSection);
 		contents = this.lex(lexer);
 		logger.info("Parse time for " + topicName + "(" + ((System.currentTimeMillis() - start) / 1000.000) + " s.)");
 		return contents.toString();
 	}
 }
