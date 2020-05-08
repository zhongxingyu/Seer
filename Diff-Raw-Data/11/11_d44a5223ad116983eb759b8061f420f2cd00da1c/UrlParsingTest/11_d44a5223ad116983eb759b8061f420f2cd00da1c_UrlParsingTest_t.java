 /*
  * Copyright (C) 2009 Laurent Caillette
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
 
 import org.junit.Test;
 import org.antlr.runtime.RecognitionException;
 import static novelang.parser.antlr.TreeFixture.tree;
 import static novelang.parser.NodeKind.URL_LITERAL;
 
 /**
  * Tests for URL parsing.
  *
  * @author Laurent Caillette
  */
 public class UrlParsingTest {
 
 
   @Test
   public void urlHttpGoogleDotCom() throws RecognitionException {
     PARSERMETHOD_URL.checkTreeAfterSeparatorRemoval(
         "http://google.com",
         tree( URL_LITERAL, "http://google.com" )
     ) ;
   }
 
   @Test
   public void urlHttpLocalhost() throws RecognitionException {
     PARSERMETHOD_URL.checkTreeAfterSeparatorRemoval(
         "http://localhost",
         tree( URL_LITERAL, "http://localhost" )
     ) ;
   }
 
   @Test
   public void urlHttpLocalhost8080() throws RecognitionException {
     PARSERMETHOD_URL.checkTreeAfterSeparatorRemoval(
         "http://localhost:8080",
         tree( URL_LITERAL, "http://localhost:8080" )
     ) ;
   }
 
   @Test
   public void urlFileWithHyphenMinus() throws RecognitionException {
     PARSERMETHOD_URL.checkTreeAfterSeparatorRemoval(
         "file:/path/to-file.ext",
         tree( URL_LITERAL, "file:/path/to-file.ext" )
     ) ;
   }
 
   @Test
   public void urlFileWithHyphenMinusNoPath() throws RecognitionException {
     PARSERMETHOD_URL.checkTreeAfterSeparatorRemoval(
         "file:my-file.ext",
         tree( URL_LITERAL, "file:my-file.ext" )
     ) ;
   }
 
   @Test
   public void urlHttpGoogleQuery() throws RecognitionException {
     PARSERMETHOD_URL.checkTreeAfterSeparatorRemoval(
         "http://www.google.com/search?q=url%20specification&sourceid=mozilla2&ie=utf-8&oe=utf-8",
         tree(
             URL_LITERAL,
             "http://www.google.com/search?q=url%20specification&sourceid=mozilla2&ie=utf-8&oe=utf-8"
         )
     ) ;
   }
 
   @Test
  public void urlHttpGoogleQuery2() throws RecognitionException {
    PARSERMETHOD_URL.checkTreeAfterSeparatorRemoval(
        "http://code.google.com/docreader/#p=google-guice&s=google-guice&t=Motivation",
        tree(
            URL_LITERAL,
            "http://code.google.com/docreader/#p=google-guice&s=google-guice&t=Motivation"
        )
    ) ;
  }

  @Test
   public void urlFilePathFileDotNlp() throws RecognitionException {
     PARSERMETHOD_URL.checkTreeAfterSeparatorRemoval(
         "file:/path/file.ppp",
         tree( URL_LITERAL, "file:/path/file.ppp" )
     ) ;
   }
 
 
   @Test
   public void urlWithTilde() throws RecognitionException {
     PARSERMETHOD_URL.checkTreeAfterSeparatorRemoval(
         "http://domain.org/path/file~tilde#anchor",
         tree(
             URL_LITERAL,
             "http://domain.org/path/file~tilde#anchor"
         )
     ) ;
   }
 
   @Test
   public void urlWithSolidusInParameters() throws RecognitionException {
     PARSERMETHOD_URL.checkTreeAfterSeparatorRemoval(
         "http://foo.net/resources?x/y",
         tree(
             URL_LITERAL,
             "http://foo.net/resources?x/y"
         )
     ) ;
   }
 
   @Test
   public void urlWithSolidusInParametersAndNumberSign() throws RecognitionException {
     PARSERMETHOD_URL.checkTreeAfterSeparatorRemoval(
         "http://novelang.sf.net/?p=1#x",
         tree(
             URL_LITERAL,
             "http://novelang.sf.net/?p=1#x"
         )
     ) ;
   }
 
   @Test
   public void urlWithSolidusInParametersAndNumberSignAndEqualsSign() throws RecognitionException {
     PARSERMETHOD_URL.checkTreeAfterSeparatorRemoval(
         "http://novelang.sf.net/?p=1#x=y",
         tree(
             URL_LITERAL,
             "http://novelang.sf.net/?p=1#x=y"
         )
     ) ;
   }
 
 // =======
 // Fixture
 // =======
 
   private static final ParserMethod PARSERMETHOD_URL = new ParserMethod( "url" ) ;
 
 }
