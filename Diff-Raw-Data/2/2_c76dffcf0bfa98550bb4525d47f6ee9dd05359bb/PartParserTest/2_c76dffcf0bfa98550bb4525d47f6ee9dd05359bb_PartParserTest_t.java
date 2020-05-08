 /*
  * Copyright (C) 2008 Laurent Caillette
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation, either
  * version 3 of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package novelang.parser.antlr;
 
 import java.util.Map;
 
 import org.antlr.runtime.RecognitionException;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import static novelang.common.NodeKind.*;
 import novelang.common.SyntacticTree;
 import novelang.parser.Escape;
 import static novelang.parser.antlr.AntlrTestHelper.*;
 import static novelang.parser.antlr.TreeFixture.tree;
 
 /**
  * GUnit sucks as it has completely obscure failures and stupid reports,
  * but it has some nice ideas to borrow.
  *
  * @author Laurent Caillette
  */
 public class PartParserTest {
 
   private static final Logger LOGGER = LoggerFactory.getLogger( PartParserTest.class ) ;
   /*package*/ static final ParserMethod PARSERMETHOD_TITLE = 
       new ParserMethod( "title" ) ;
   /*package*/ static final ParserMethod PARSERMETHOD_HEADER_IDENTIFIER = 
       new ParserMethod( "headerIdentifier" ) ;
   /*package*/ static final ParserMethod PARSERMETHOD_WORD = 
       new ParserMethod( "word" ) ;
   /*package*/ static final ParserMethod PARSERMETHOD_PARAGRAPH = 
       new ParserMethod( "paragraph" ) ;
   /*package*/ static final ParserMethod PARSERMETHOD_BIG_DASHED_LIST_ITEM = 
       new ParserMethod( "bigDashedListItem" ) ;
   /*package*/ static final ParserMethod PARSERMETHOD_SECTION = 
       new ParserMethod( "section" ) ;
   /*package*/ static final ParserMethod PARSERMETHOD_LITERAL = 
       new ParserMethod( "literal" ) ;
   /*package*/ static final ParserMethod PARSERMETHOD_SOFT_INLINE_LITERAL = 
       new ParserMethod( "softInlineLiteral" ) ;
   /*package*/ static final ParserMethod PARSERMETHOD_HARD_INLINE_LITERAL = 
       new ParserMethod( "hardInlineLiteral" ) ;
   /*package*/ static final ParserMethod PARSERMETHOD_CHAPTER = 
       new ParserMethod( "chapter" ) ;
   /*package*/ static final ParserMethod PARSERMETHOD_PART = 
       new ParserMethod( "part" ) ;
   /*package*/ static final ParserMethod PARSERMETHOD_URL = 
       new ParserMethod( "url" ) ;
 
   @Test
   public void wordContainsOELigatured() throws RecognitionException {
     PARSERMETHOD_WORD.createTree( "\u0153\u0152" ) ;
   }
 
   @Test
   public void titleIsTwoWords() throws RecognitionException {
     PARSERMETHOD_TITLE.checkTree( "some title", tree(
         TITLE,
         tree( WORD, "some" ),
         tree( WORD, "title" )
     ) ) ;
   }
 
   @Test
   public void titleIsTwoWordsAndExclamationMark() throws RecognitionException {
     PARSERMETHOD_TITLE.checkTree( "some title !", tree(
         TITLE,
         tree(WORD, "some"),
         tree(WORD, "title"),
         tree( PUNCTUATION_SIGN, SIGN_EXCLAMATIONMARK )
     ) ) ;
   }
 
   @Test
   public void titleIsWordsAndParenthesisAndExclamationMark() throws RecognitionException {
     PARSERMETHOD_TITLE.checkTree( "some (title) !", tree(
         TITLE,
         tree( WORD, "some" ),
         tree( PARENTHESIS, tree( WORD, "title" ) ),
         tree( PUNCTUATION_SIGN, SIGN_EXCLAMATIONMARK )
     ) ) ;
   }
 
   @Test @Ignore
   public void identifierIsSingleWord() throws RecognitionException {
     PARSERMETHOD_HEADER_IDENTIFIER.checkTree( 
         "\\\\my-Identifier", tree( IDENTIFIER, "my-Identifier" ) ) ;
   }
 
   @Test
   public void wordCausedABug1() throws RecognitionException {
     PARSERMETHOD_WORD.checkTree( "myIdentifier", tree( WORD, "myIdentifier" ) ) ;
   }
 
   @Test
   /**
    * This one because {@code 'fi'} was recognized as the start of {@code 'file'}
    * and the parser generated this error: 
    * {@code line 1:10 mismatched character 'e' expecting 'l'}.
    */
   public void wordCausedABug2() throws RecognitionException {
     PARSERMETHOD_WORD.checkTree( "fi", tree( WORD, "fi" ) ) ;
   }
 
   @Test
   public void wordIsSingleLetter() throws RecognitionException {
     PARSERMETHOD_WORD.checkTree( "w", tree( WORD, "w" ) ) ;
   }
 
   @Test
   public void wordIsTwoLetters() throws RecognitionException {
     PARSERMETHOD_WORD.checkTree( "Www", tree( WORD, "Www" ) ) ;
   }
 
   @Test
   public void wordIsThreeDigits() throws RecognitionException {
     PARSERMETHOD_WORD.checkTree( "123", tree( WORD, "123" ) ) ;
   }
 
   @Test
   public void wordIsDigitsWithHyphenMinusInTheMiddle() throws RecognitionException {
     PARSERMETHOD_WORD.checkTree( "123-456", tree( WORD, "123-456" ) ) ;
   }
 
   @Test
   public void wordFailsWithLeadingApostrophe() throws RecognitionException {
     PARSERMETHOD_WORD.checkFails( "'w" ) ;
   }
 
   @Test
   public void wordFailsWithTrailingHyphenMinus() throws RecognitionException {
     PARSERMETHOD_WORD.checkFails( "'w-" ) ;
   }
 
   @Test
   public void wordWithSuperscript() throws RecognitionException {
     PARSERMETHOD_WORD.checkTree( "w^e", tree( WORD, tree( "w" ), tree( SUPERSCRIPT, "e" ) ) ) ;
   }
 
   @Test
   public void wordIsEveryEscapedCharacter() throws RecognitionException {
     final Map< String, Character > map = Escape.getCharacterEscapes() ;
     for( String key : map.keySet() ) {
       final String escaped = Escape.ESCAPE_START + key + Escape.ESCAPE_END ;
       final Character unescaped = map.get( key ) ;
       PARSERMETHOD_WORD.checkTree( escaped, tree( WORD, "" + unescaped ) ) ;
     }
   }
 
   @Test
   public void paragraphIsSimplestSpeech() throws RecognitionException {
     PARSERMETHOD_BIG_DASHED_LIST_ITEM.checkTree( "--- w0", tree(
         PARAGRAPH_SPEECH,
         tree( WORD, "w0" )
     ) ) ;
   }
 
   @Test @Ignore
   public void paragraphIsSimplestSpeechWithIdentifier() throws RecognitionException {
     PARSERMETHOD_PARAGRAPH.checkTree( "\\identifier" + BREAK +
         "--- w0", tree(
             PARAGRAPH_SPEECH,
             tree( IDENTIFIER, "identifier"),
             tree( WORD, "w0" )
         ) ) ;
   }
 
   @Test @Ignore
   public void paragraphIsSimplestSpeechEscape() throws RecognitionException {
     PARSERMETHOD_PARAGRAPH.checkTree( "--| w0", tree(
         PARAGRAPH_SPEECH_ESCAPED,
         tree( WORD, "w0" )
     ) ) ;
 
   }
 
   @Test @Ignore
   public void paragraphIsSimplestSpeechEscapeWithIdentifier() throws RecognitionException {
     PARSERMETHOD_PARAGRAPH.checkTree( "\\identifier" + BREAK +
         "--| w0", tree(
             PARAGRAPH_SPEECH_ESCAPED,
             tree( IDENTIFIER, "identifier"),
             tree( WORD, "w0" )
         ) ) ;
   }
 
   @Test @Ignore
   public void paragraphIsSimplestSpeechContinued() throws RecognitionException {
     PARSERMETHOD_PARAGRAPH.checkTree( "--+ w0", tree(
         PARAGRAPH_SPEECH_CONTINUED,
         tree( WORD, "w0" )
     ) ) ;
 
   }
 
   @Test @Ignore
   public void paragraphIsSimplestSpeechContinuedWithIdentifier() throws RecognitionException {
     PARSERMETHOD_PARAGRAPH.checkTree( "\\identifier" + BREAK +
         "--+ w0", tree(
             PARAGRAPH_SPEECH_CONTINUED,
             tree( IDENTIFIER, "identifier"),
             tree( WORD, "w0" )
         ) ) ;
   }
 
   
   // Following tests are for paragraphBody rule. But we need to rely on a rule
   // returning a sole tree as test primitives don't assert on more than one.
   // In addition, the ParagraphScope is declared in paragraph rule so we must get through it.
 
   @Test
   public void paragraphIsWordThenComma() throws RecognitionException {
     PARSERMETHOD_PARAGRAPH.checkTree( "w0,", tree(
         PARAGRAPH_PLAIN,
         tree( WORD, "w0" ),
         tree( PUNCTUATION_SIGN, SIGN_COMMA )
     ) ) ;
   }
 
   @Test @Ignore
   public void paragraphSingleWordWithIdentifier() throws RecognitionException {
     PARSERMETHOD_PARAGRAPH.checkTree( "\\identifier" + BREAK +
         "w0", tree(
             PARAGRAPH_PLAIN,
             tree( IDENTIFIER, "identifier" ),
             tree( WORD, "w0" )
         ) ) ;
   }
 
 
 
   @Test
   public void paragraphIsWordsWithCommaInTheMiddle1() throws RecognitionException {
     PARSERMETHOD_PARAGRAPH.checkTree( 
         "w0,w1", tree(
             PARAGRAPH_PLAIN,
             tree( WORD, "w0" ),
             tree( PUNCTUATION_SIGN, SIGN_COMMA ),
             tree( WORD, "w1" )
         ) 
     ) ;
   }
 
   @Test
   public void paragraphIsWordThenApostrophe() throws RecognitionException {
     PARSERMETHOD_PARAGRAPH.checkTree( 
         "w0'", 
         tree(
             PARAGRAPH_PLAIN,
             tree( WORD, "w0" ),
             tree( APOSTROPHE_WORDMATE )
         ) 
     ) ;
 
   }
 
   @Test
   public void paragraphIsWordsWithApostropheInTheMiddle() throws RecognitionException {
     PARSERMETHOD_PARAGRAPH.checkTree( 
         "w0'w1", 
         tree(
             PARAGRAPH_PLAIN,
             tree( WORD, "w0" ),
             tree( APOSTROPHE_WORDMATE ),
             tree( WORD, "w1" )
         ) 
     ) ;
   }
 
   @Test
   public void paragraphIsWordThenSemicolon() throws RecognitionException {
     SyntacticTree tree = PARSERMETHOD_WORD.createTree( "w0" ) ;
     LOGGER.debug( tree.toStringTree() ) ;
 
     PARSERMETHOD_PARAGRAPH.checkTree( 
         "w0;", 
         tree(
             PARAGRAPH_PLAIN,
             tree( WORD, "w0" ),
             tree( PUNCTUATION_SIGN, SIGN_SEMICOLON )
         ) 
     ) ;
 
   }
 
   @Test
   public void paragraphIsWordThenFullStop() throws RecognitionException {
     PARSERMETHOD_PARAGRAPH.checkTree( 
         "w0.", 
         tree(
             PARAGRAPH_PLAIN,
             tree( WORD, "w0" ),
             tree( PUNCTUATION_SIGN, SIGN_FULLSTOP )
         ) 
     ) ;
 
   }
 
   @Test
   public void paragraphIsWordThenQuestionMark() throws RecognitionException {
     PARSERMETHOD_PARAGRAPH.checkTree( 
         "w0?", 
         tree(
             PARAGRAPH_PLAIN,
             tree( WORD, "w0" ),
             tree( PUNCTUATION_SIGN, SIGN_QUESTIONMARK )
         ) 
     ) ;
 
   }
 
   @Test
   public void paragraphIsWordThenExclamationMark() throws RecognitionException {
     PARSERMETHOD_PARAGRAPH.checkTree( 
         "w0!", 
         tree(
             PARAGRAPH_PLAIN,
             tree( WORD, "w0" ),
             tree( PUNCTUATION_SIGN, SIGN_EXCLAMATIONMARK )
         ) 
     ) ;
 
   }
 
   @Test
   public void paragraphIsWordThenColon() throws RecognitionException {
     PARSERMETHOD_PARAGRAPH.checkTree( 
         "w0:", 
         tree(
             PARAGRAPH_PLAIN,
             tree( WORD, "w0" ),
             tree( PUNCTUATION_SIGN, SIGN_COLON )
         ) 
     ) ;
 
   }
 
   @Test
   public void paragraphIsWordThenEllipsis() throws RecognitionException {
     PARSERMETHOD_PARAGRAPH.checkTree( 
         "w0...", 
         tree(
             PARAGRAPH_PLAIN,
             tree( WORD, "w0" ),
             tree( PUNCTUATION_SIGN, SIGN_ELLIPSIS )
         ) 
     ) ;
 
   }
 
   @Test
   public void paragraphBodyIsEmphasizedWordThenWord()
       throws RecognitionException
   {
     PARSERMETHOD_PARAGRAPH.createTree( "//w0//w1" );
   }
 
   @Test
   public void paragraphIsWordsWithApostropheThenEmphasis() throws RecognitionException {
     PARSERMETHOD_PARAGRAPH.checkTree( 
         "w0 w1'w2//w3//.", 
         tree(
             PARAGRAPH_PLAIN,
             tree( WORD, "w0" ),
             tree( WORD, "w1" ),
             tree( APOSTROPHE_WORDMATE ),
             tree( WORD, "w2" ),
             tree( EMPHASIS, tree( WORD, "w3" ) ),
             tree( PUNCTUATION_SIGN, SIGN_FULLSTOP )
         ) 
     ) ;
 
   }
 
   @Test
   public void paragraphIsMultilineQuoteWithPunctuationSigns1() throws RecognitionException {
     PARSERMETHOD_PARAGRAPH.createTree( 
         "\"w1 w2. w3 w4." + BREAK +
         "w5 !\"" + BREAK +
         "w6 w7." );
   }
 
   @Test
   public void paragraphIsMultilineQuoteWithPunctuationSigns2() throws RecognitionException {
     PARSERMETHOD_PARAGRAPH.createTree( 
         "//w1.//" + BREAK +
         "w2. w3." 
     ) ;
   }
 
   @Test
   public void paragraphIsEmphasisAndQuoteWithPunctuationSigns1() throws RecognitionException {
     PARSERMETHOD_PARAGRAPH.createTree( 
         "//w0.// " + BREAK +
         "  w1. w2. w3. " + BREAK +
         "  w4 : w5 w6. " + BREAK +
         "  \"w7 w8 ?\"." 
     );
   }
 
 
   @Test
   public void paragraphIsJustEllipsis() throws RecognitionException {
     PARSERMETHOD_PARAGRAPH.createTree( "..." ) ;
   }
 
   @Test
   public void paragraphIsEllipsisThenWord() throws RecognitionException {
     PARSERMETHOD_PARAGRAPH.createTree( "...w0" ) ;
   }
 
   @Test
   public void paragraphIsEllipsisInsideBrackets() throws RecognitionException {
     PARSERMETHOD_PARAGRAPH.createTree( "[...]" ) ;
   }
 
   @Test
   public void paragraphIsWordsAndPunctuationSigns1() throws RecognitionException {
     PARSERMETHOD_PARAGRAPH.createTree( "w1 w2, w3 w4." ) ;
   }
 
   @Test
   public void paragraphIsParenthesizedWordsWithApostropheInTheMiddle()
       throws RecognitionException
   {
     PARSERMETHOD_PARAGRAPH.createTree( "(w0'w1)" ) ;
   }
 
   @Test
   public void paragraphIsParenthesizedWordsWithCommaInTheMiddle() throws RecognitionException {
     PARSERMETHOD_PARAGRAPH.createTree( "(w0,w1)" ) ;
   }
 
   @Test
   public void paragraphIsEmphasizedWordsWithApostropheInTheMiddle() throws RecognitionException {
     PARSERMETHOD_PARAGRAPH.createTree( "\"w0'w1\"" ) ;
   }
 
   @Test
   public void paragraphIsQuotedWordsWithCommaInTheMiddle() throws RecognitionException {
     PARSERMETHOD_PARAGRAPH.createTree( "\"w0,w1\"" ) ;
   }
 
   @Test
   public void paragraphIsInterpolatedWordsWithApostropheInTheMiddle() throws RecognitionException {
     PARSERMETHOD_PARAGRAPH.createTree( "--w0'w1--" );
   }
 
   @Test
   public void paragraphIsInterpolatedWordsWithCommaInTheMiddle() throws RecognitionException {
     PARSERMETHOD_PARAGRAPH.createTree( "--w0,w1--" );
   }
 
   @Test
   public void paragraphIsQuoteOfOneWordThenParenthesis() throws RecognitionException {
     PARSERMETHOD_PARAGRAPH.checkTree( 
         "\"w0(w1)\"", tree(
             PARAGRAPH_PLAIN,
             tree(
                 QUOTE,
                 tree( WORD, "w0" ),
                 tree( PARENTHESIS, tree( WORD, "w1" ) )
             )
         ) ) ;
   }
 
   @Test
   public void paragraphIsQuoteOfOneWordThenSpaceParenthesis() throws RecognitionException {
     PARSERMETHOD_PARAGRAPH.checkTree( 
         "\"w0 (w1)\"", tree(
             PARAGRAPH_PLAIN,
             tree(
                 QUOTE,
                 tree( WORD, "w0" ),
                 tree( PARENTHESIS, tree( WORD, "w1" ) )
             )
         ) ) ;
   }
 
   @Test @Ignore
   public void sectionHasIdentifier()
       throws RecognitionException
   {
     PARSERMETHOD_SECTION.checkTree( "=== s00", tree(
             SECTION,
             tree( TITLE, tree( WORD, "s00") )
         ) ) ;
   }
 
   @Test
   public void sectionHasQuote()
       throws RecognitionException
   {
     PARSERMETHOD_SECTION.checkTree( "=== \"q\" w", tree(
             SECTION,
             tree( TITLE, tree( QUOTE, tree( WORD, "q" ) ), tree( WORD, "w") )
         ) ) ;
   }
 
   @Test
   public void sectionIsAnonymous() throws RecognitionException {
     PARSERMETHOD_SECTION.checkTree( "===", tree( SECTION ) ) ;
   }
 
   @Test
   public void partWithSeveralMultilineParagraphs() throws RecognitionException {
     PARSERMETHOD_PART.checkTree( 
         BREAK +
         "p0 w01" + BREAK +
         "w02" + BREAK +
         BREAK +
         "p1 w11" + BREAK +
         "w12", tree(
             PART,
             tree( PARAGRAPH_PLAIN, tree( WORD, "p0" ), tree( WORD, "w01" ), tree( WORD, "w02" ) ),
             tree( PARAGRAPH_PLAIN, tree( WORD, "p1" ), tree( WORD, "w11" ), tree( WORD, "w12" )
      ) ) ) ;
   }
 
   @Test
   public void partHasTrailingSpacesEverywhere() throws RecognitionException {
     PARSERMETHOD_PART.checkTree( 
         BREAK +
         "  " + BREAK +
         " p0 w01  " + BREAK +
         "w02 " + BREAK +
         "  " + BREAK +
         "p1 w11  " + BREAK +
         " w12 ", tree(
             PART,
             tree( PARAGRAPH_PLAIN, tree( WORD, "p0" ), tree( WORD, "w01" ), tree( WORD, "w02" ) ),
             tree( PARAGRAPH_PLAIN, tree( WORD, "p1" ), tree( WORD, "w11" ), tree( WORD, "w12" ) )
         ) 
     ) ;
   }
 
   @Test
   public void someLiteral() throws RecognitionException {
     PARSERMETHOD_PART.checkTree( 
       "<<<" + BREAK +
       "  Here is some " + BREAK +
       "  //Literal// " + BREAK +
       ">>>",
       tree(
           PART,
           tree( LITERAL, "  Here is some " + BREAK + "  //Literal// " )
       ) 
     ) ;
   }
 
   @Test @Ignore
   public void someLiteralContainingLineComment() throws RecognitionException {
     PARSERMETHOD_PART.checkTree( 
         "<<<" + BREAK +
         "%% Not to be commented" +
         ">>>",
         tree(
             PART,
             tree( LITERAL, "%% Not to be commented" )
         ) 
     ) ;
   }
 
   @Test
   public void someLiteralContainingLowerthanSign() throws RecognitionException {
     PARSERMETHOD_PART.checkTree( 
         "<<<" + BREAK +
         "<" + BREAK +
         ">>>", tree( PART, tree( LITERAL, "<" )
       ) 
     ) ;
   }
 
   @Test
   public void someLiteralContainingGreaterthanSigns() throws RecognitionException {
     final String verbatim =
         " >>>" + BREAK +
         "> " + BREAK +
         ">> " + BREAK +
         ">> >>>"
     ;
 
     PARSERMETHOD_PART.checkTree( 
         "<<<" + BREAK +
         verbatim + BREAK +
         ">>>", tree( PART, tree( LITERAL, verbatim ) )
     ) ;
   }
 
   
   @Test
   public void literalWithBreaksAndOtherSeparators() throws RecognitionException {
     final String verbatim = "  Here is some " + BREAK + "//literal//. " ;
     PARSERMETHOD_LITERAL.checkTree( 
         "<<<" + BREAK +
         verbatim + BREAK +
         ">>>", tree( LITERAL, verbatim ) 
     ) ;
   }
 
   @Test
   public void literalWithEscapedCharacters() throws RecognitionException {
     PARSERMETHOD_LITERAL.checkTree( 
         "<<<" + BREAK +
         "2" + Escape.ESCAPE_START + "greaterthan" + Escape.ESCAPE_END + "1" + BREAK +
         ">>>", tree( LITERAL, "2>1" ) 
     ) ;
   }
 
   @Test
   public void softInlineLiteralNoEscape() throws RecognitionException {
     final String literal = "azer()+&%?" ;
     PARSERMETHOD_SOFT_INLINE_LITERAL.checkTree( 
         "`" + literal + "`", 
         tree( SOFT_INLINE_LITERAL, literal ) 
     ) ;
   }
 
   @Test
   public void softInlineLiteralWithEscape() throws RecognitionException {
     PARSERMETHOD_SOFT_INLINE_LITERAL.checkTree( 
         "`" + Escape.ESCAPE_START + "greaterthan" + Escape.ESCAPE_END +"`", 
         tree( SOFT_INLINE_LITERAL, ">" ) 
     ) ;
   }
 
   @Test
   public void hardInlineLiteralNothingSpecial() throws RecognitionException {
     final String literal = "azer()+&%?";
     PARSERMETHOD_HARD_INLINE_LITERAL.checkTree( 
         "``" + literal +"``", 
         tree( HARD_INLINE_LITERAL, literal ) 
     ) ;
   }
 
 
   @Test
   public void partHasAnonymousSectionAndHasBlockquoteWithSingleParagraph() 
       throws RecognitionException
   {
     PARSERMETHOD_PART.checkTree( 
         "===" + BREAK +
         BREAK +
         "<< w0 w1" + BREAK +
         ">>", tree( PART,
             tree( SECTION ),
             tree(
                 BLOCKQUOTE,
                 tree( PARAGRAPH_PLAIN, tree( WORD, "w0" ), tree( WORD, "w1" ) )
             )
         ) 
     ) ;
   }
 
   @Test
   public void partIsSectionThenParagraphThenBlockquoteThenParagraph()
       throws RecognitionException
   {
     PARSERMETHOD_PART.checkTree( 
         "===" + BREAK +
         BREAK +
         "p0" + BREAK +
         BREAK +
         "<< w0" + BREAK +
         ">>" + BREAK +
         BREAK +
         "p1", tree( PART,
             tree( SECTION ),
             tree( PARAGRAPH_PLAIN, tree( WORD, "p0" ) ),
             tree( BLOCKQUOTE, tree( PARAGRAPH_PLAIN, tree( WORD, "w0" ) ) ),
             tree( PARAGRAPH_PLAIN, tree( WORD, "p1" ) )
         ) 
     ) ;
   }
 
   @Test @Ignore
   public void blockquoteWithIdentifier()
       throws RecognitionException
   {
     PARSERMETHOD_PART.checkTree( 
         "  \\identifier " + BREAK +
         "<< w0" + BREAK +
         ">>", tree(
             PART,
             tree(
                 BLOCKQUOTE,
                 tree( IDENTIFIER, "identifier" ),
                 tree( PARAGRAPH_PLAIN, tree( WORD, "w0" ) )
             )
         ) 
     ) ;
   }
 
   @Test
   public void sectionIsAnonymousAndHasBlockquoteWithTwoParagraphs() 
       throws RecognitionException
   {
     PARSERMETHOD_PART.checkTree( 
         "===" + BREAK +
         BREAK +
         "<< w0 w1" + BREAK +
         BREAK +
         "w2" + BREAK +
         ">>", tree( PART,
             tree( SECTION ),
             tree(
                 BLOCKQUOTE,
                 tree( PARAGRAPH_PLAIN, tree( WORD, "w0" ), tree( WORD, "w1" ) ),
                 tree( PARAGRAPH_PLAIN, tree( WORD, "w2" ) )
             )
         ) 
     ) ;
   }
 
   @Test
   public void sectionIsAnonymousAndHasBlockquoteWithBreakInside() throws RecognitionException {
     PARSERMETHOD_PART.createTree( 
         "===" + BREAK +
         BREAK +
         "<< w0 w1" + BREAK +
         BREAK +
         ">>" 
     );
   }
 
   @Test
   public void sectionHasOneParagraphWithEmphasisThenWordOnTwoLines() throws RecognitionException {
     PARSERMETHOD_SECTION.createTree( 
         "===" + BREAK +
         BREAK +
         "//w0//" + BREAK +
         "w1" 
     );
   }
 
   @Test
   public void sectionHasOneParagraphWithParenthesisThenWordOnTwoLines()
       throws RecognitionException
   {
     PARSERMETHOD_SECTION.createTree( 
         "===" + BREAK +
         BREAK +
         "(w0)" + BREAK +
         "w1" 
     );
   }
 
   @Test
   public void sectionHasOneParagraphWithQuoteThenWordOnTwoLines() throws RecognitionException {
     PARSERMETHOD_SECTION.createTree( 
         "===" + BREAK +
         BREAK +
         "\"w0\"" + BREAK +
         "w1" 
     );
   }
 
   @Test
   public void paragraphBodyHasThreeWordsOnThreeLinesAndFullStopAtEndOfFirstLine()
       throws RecognitionException
   {
     PARSERMETHOD_PARAGRAPH.createTree( 
         "w0." + BREAK +
         "w1" + BREAK +
         "w2" 
     );
   }
 
   @Test
   public void paragraphBodyIsJustEmphasizedWord() throws RecognitionException {
     PARSERMETHOD_PARAGRAPH.checkTree( 
         "//w0//", tree(
         PARAGRAPH_PLAIN,
         tree( EMPHASIS, tree( WORD, "w0" ) ) ) 
     ) ;
   }
 
   @Test
   public void paragraphBodyIsJustParenthesizedWord() throws RecognitionException {
     PARSERMETHOD_PARAGRAPH.checkTree( 
         "(w0)", 
         tree(
             PARAGRAPH_PLAIN,
             tree( PARENTHESIS, tree( WORD, "w0" ) )
         ) 
     ) ;
   }
 
   @Test
   public void paragraphBodyIsJustQuotedWord() throws RecognitionException {
     PARSERMETHOD_PARAGRAPH.checkTree( 
         "\"w0\"", tree(
             PARAGRAPH_PLAIN,
             tree( QUOTE, tree( WORD, "w0" ) )
         ) 
     ) ;
   }
 
   @Test
   public void paragraphBodyIsJustInterpolatedWord() throws RecognitionException {
     PARSERMETHOD_PARAGRAPH.checkTree( 
         "-- w0 --", 
         tree(
             PARAGRAPH_PLAIN,
             tree( INTERPOLATEDCLAUSE, tree( WORD, "w0" ) )
         ) 
     ) ;
   }
 
   @Test
   public void paragraphBodyIsJustInterpolatedWordWithSilentEnd() throws RecognitionException {
     PARSERMETHOD_PARAGRAPH.checkTree( 
         "-- w0 -_", 
         tree(
             PARAGRAPH_PLAIN,
             tree( INTERPOLATEDCLAUSE_SILENTEND, tree( WORD, "w0" ) ) 
         ) 
     );
   }
 
   @Test
   public void paragraphBodyIsJustBracketedWord() throws RecognitionException {
     PARSERMETHOD_PARAGRAPH.checkTree( 
         "[w0]", 
         tree(
             PARAGRAPH_PLAIN,
             tree( SQUARE_BRACKETS, tree( WORD, "w0" ) )
         ) 
     ) ;
   }
 
   @Test
   public void paragraphBodyHasQuotesAndWordAndSpaceAndQuotes()
       throws RecognitionException
   {
     PARSERMETHOD_PARAGRAPH.createTree( "\"w0\"w2 \"w3\"" );
   }
 
   @Test
   public void paragraphBodyHasQuotesAndPunctuationSignsAndWordsInTheMiddle1()
       throws RecognitionException
   {
     PARSERMETHOD_PARAGRAPH.createTree( "\"w00\" w01 w02 \" w03 w04 ! \"." );
   }
 
   @Test
   public void paragraphBodyHasQuotesAndPunctuationSignsAndWordsInTheMiddle2()
       throws RecognitionException
   {
     PARSERMETHOD_PARAGRAPH.createTree( "w10 \"w11\" \"w12\", \"w13\"" );
   }
 
   @Test
   public void paragraphBodyHasQuotesAndPunctuationSignsAndWordsInTheMiddle3()
       throws RecognitionException
   {
     PARSERMETHOD_PARAGRAPH.createTree( "\"w20 w21... w22\" !" );
   }
 
   @Test
   public void paragraphBodyHasQuotesAndParenthesisAndPunctuationSignsAndWordsInTheMiddle()
       throws RecognitionException
   {
     PARSERMETHOD_PARAGRAPH.createTree( "\"p00 (w01) w02.\" w04 (w05 \"w06 (w07)\".)." );
   }
 
   @Test
   public void
   paragraphBodyHasQuotesAndParenthesisAndBracketsAndPunctuationSignsAndWordsInTheMiddle()
       throws RecognitionException
   {
     PARSERMETHOD_PARAGRAPH.createTree( "\"p00 (w01) w02.\"w04(w05 \"[w06] (w07)\".)." );
   }
 
   @Test
   public void paragraphBodyHasWordThenInterpolatedClauseThenFullStop()
       throws RecognitionException
   {
     PARSERMETHOD_PARAGRAPH.createTree( "p10 -- w11 w12 --." );
   }
 
   @Test
   public void paragraphBodyHasWordThenInterpolatedClauseSilentEndThenFullStop()
       throws RecognitionException
   {
     PARSERMETHOD_PARAGRAPH.createTree( "p20 -- w21 w22 -_." );
   }
 
   @Test
   public void paragraphBodyIsQuoteWithWordThenParenthesis() throws RecognitionException {
     PARSERMETHOD_PARAGRAPH.createTree( "\"w0 (w1)\"" );
   }
 
   @Test
   public void paragraphBodyIsNestingQuoteAndParenthesisAndEmphasis() throws RecognitionException {
     PARSERMETHOD_PARAGRAPH.createTree( "\"w0 (w1 //w2//)\"" );
   }
 
   @Test
   public void paragraphBodyIsNestingQuoteAndParenthesisAndEmphasisAndParenthesisAgain()
       throws RecognitionException
   {
     PARSERMETHOD_PARAGRAPH.createTree( "\"w0 (w1 //w2 (w3)//)\"" );
   }
 
   @Test
   public void
   paragraphBodyIsNestingQuoteAndParenthesisAndInterpolatedClauseAndParenthesisAgainAndBrackets()
       throws RecognitionException
   {
     PARSERMETHOD_PARAGRAPH.createTree( "\"(w0 -- w1 (w2 [w3]) --)\"" );
   }
 
   @Test
   public void paragraphBodyIsNestingEmphasisAndParenthesis() throws RecognitionException {
     PARSERMETHOD_PARAGRAPH.createTree( "//w0 (w1)//." );
   }
 
   @Test
   public void paragraphBodyIsNestingEmphasisAndParenthesisAndQuotesAndHasQuestionMarkAtTheEnd()
       throws RecognitionException
   {
     PARSERMETHOD_PARAGRAPH.createTree( "//w0 (w1, \"w2\")// ?" );
   }
 
   @Test
   public void paragraphBodyIsParenthesisWithWordThenExclamationMark() throws RecognitionException {
     PARSERMETHOD_PARAGRAPH.createTree( "(w0 !)" );
   }
 
   @Test
   public void paragraphBodyIsParenthesisWithWordAndQuotesAndEllipsisInside()
       throws RecognitionException
   {
     PARSERMETHOD_PARAGRAPH.createTree( "(w0 \"w1\"...)" );
   }
 
   @Test
   public void
   paragraphBodyHasNestingParenthesisAndQuoteEmphasisThenSemiColonAndWordAndExclamationMark()
       throws RecognitionException
   {
     PARSERMETHOD_PARAGRAPH.createTree( "(w0 \"w1 //w2//\") : w3 !" );
   }
 
   @Test
   public void
   paragraphBodyHasQuoteThenParenthesisThenEmphasisThenInterpolatedClauseThenBracketsNoSpace()
       throws RecognitionException
   {
     PARSERMETHOD_PARAGRAPH.createTree( "\"w00\"(w01)//w02//--w03--[w04]" );
   }
 
   @Test
   public void
   paragraphBodyIsNestingEmphasisAndParenthesisAndInterpolatedClauseAndQuotesOnSeveralLines()
       throws RecognitionException
   {
     PARSERMETHOD_PARAGRAPH.createTree( 
         "//w1" + BREAK +
         "(w2 " + BREAK +
         "-- w3  " + BREAK +
         "\"w4 " + BREAK +
         "w5\"--)//." 
     );
   }
 
 
   @Test
   public void partIsChapterThenSectionThenSingleWordParagraph() throws RecognitionException {
     PARSERMETHOD_PART.checkTree( 
         "*** c0" + BREAK +
         BREAK +
         "=== s0" + BREAK +
         BREAK +
         "p0", tree(
           PART,
           tree(
               CHAPTER,
               tree( TITLE, tree(WORD, "c0" ) )
           ),
           tree(
               SECTION,
               tree( TITLE, tree( WORD, "s0" ) )
           ),
           tree( PARAGRAPH_PLAIN, tree( WORD, "p0" ) )
         ) 
     ) ;
   }
 
   @Test
   public void partIsAnonymousSectionsWithLeadingBreaks() throws RecognitionException {
     PARSERMETHOD_PART.checkTree( 
         BREAK +
         BREAK +
         "===" + BREAK +
         BREAK +
         "p0" + BREAK +
         BREAK +
         "===" + BREAK +
         BREAK +
         "p1", tree( PART,
             tree( SECTION ),
             tree( PARAGRAPH_PLAIN, tree( WORD, "p0" ) ),
             tree( SECTION ),
             tree( PARAGRAPH_PLAIN, tree( WORD, "p1" ) )
         ) 
     ) ;
   }
 
   /**
    * This one because {@code 'lobs'} was recognized as the start of {@code 'localhost'}
    * and the parser generated this error:
    * {@code line 3:3 mismatched character 'b' expecting 'c' }.
    */
   @Test
   public void partMadeOfParticularContent() throws RecognitionException {
     PARSERMETHOD_PART.createTree( 
         "===" + BREAK +
         BREAK +
         " lobs " 
     );
   }
 
  @Test @Ignore
   public void chapterIsAnonymousWithHeaderIdentifier()
       throws RecognitionException
   {
     PARSERMETHOD_CHAPTER.checkTree( 
         "***" + BREAK + 
         "  \\\\identifier",
         tree( CHAPTER, tree( IDENTIFIER, "identifier" ) ) 
     ) ;
   }
 
   @Test @Ignore
   public void chapterHasTitleAndHeaderIdentifier()
       throws RecognitionException
   {
     PARSERMETHOD_CHAPTER.checkTree( 
         "*** Chapter has" + BREAK +
         "title " + BREAK +
         "  \\\\identifier", 
         tree(
             CHAPTER,
             tree( TITLE, tree( WORD, "Chapter"), tree( WORD, "has" ), tree( WORD, "title") ),
             tree( IDENTIFIER, "identifier" )
         ) 
     ) ;
   }
 
   @Test
   public void chapterIsAnonymousWithSimpleSectionContainingWordsWithPunctuationSigns1()
       throws RecognitionException
   {
     PARSERMETHOD_CHAPTER.createTree( 
         "***" + BREAK +
         BREAK +
         "===" + BREAK +
         BREAK +
         "w0, w1." 
     );
   }
 
   @Test
   public void chapterIsAnonymousWithSimpleSectionContainingWordsWithPunctuationSigns2()
       throws RecognitionException
   {
     PARSERMETHOD_CHAPTER.createTree( 
         "***" + BREAK +
         BREAK +
         "===" + BREAK +
         BREAK +
         "w0 : w1." 
     );
   }
 
   @Test
   public void chapterContainsUrl()
       throws RecognitionException
   {
     PARSERMETHOD_CHAPTER.createTree( 
         "***" + BREAK +
         BREAK +
         "===" + BREAK +
         BREAK +
         "http://google.com" 
     );
   }
 
   @Test
   public void urlHttpGoogleDotCom() throws RecognitionException {
     PARSERMETHOD_URL.checkTree( 
         "http://google.com", 
         tree( URL, "http://google.com" ) 
     ) ;
   }
 
   @Test
   public void urlHttpLocalhost() throws RecognitionException {
     PARSERMETHOD_URL.checkTree( 
         "http://localhost", 
         tree( URL, "http://localhost" ) 
     ) ;
   }
 
   @Test
   public void urlHttpLocalhost8080() throws RecognitionException {
     PARSERMETHOD_URL.checkTree( 
         "http://localhost:8080", 
         tree( URL, "http://localhost:8080" ) 
     ) ;
   }
 
   @Test
   public void urlFileWithHyphenMinus() throws RecognitionException {
     PARSERMETHOD_URL.checkTree( 
         "file:/path/to-file.ext", 
         tree( URL, "file:/path/to-file.ext" ) 
     ) ;
   }
 
   @Test
   public void urlFileWithHyphenMinusNoPath() throws RecognitionException {
     PARSERMETHOD_URL.checkTree( 
         "file:my-file.ext", 
         tree( URL, "file:my-file.ext" ) 
     ) ;
   }
 
   @Test
   public void urlHttpGoogleQuery() throws RecognitionException {
     PARSERMETHOD_URL.checkTree( 
         "http://www.google.com/search?q=url%20specification&sourceid=mozilla2&ie=utf-8&oe=utf-8", 
         tree(
             URL,
             "http://www.google.com/search?q=url%20specification&sourceid=mozilla2&ie=utf-8&oe=utf-8"
         ) 
     ) ;
   }
 
   @Test
   public void urlFilePathFileDotNlp() throws RecognitionException {
     PARSERMETHOD_URL.checkTree( 
         "file:/path/file.ppp", 
         tree( URL, "file:/path/file.ppp" ) 
     ) ;
   }
 
 
   @Test
   public void urlWithTilde() throws RecognitionException {
     PARSERMETHOD_URL.checkTree( 
         "http://domain.org/path/file~tilde#anchor", 
         tree(
             URL,
             "http://domain.org/path/file~tilde#anchor"
         ) 
     ) ;
   }
 
 
 
 }
