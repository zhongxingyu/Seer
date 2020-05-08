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
 package novelang.hierarchy;
 
 import org.junit.Test;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import novelang.common.SyntacticTree;
 import novelang.common.tree.Treepath;
 import static novelang.parser.NodeKind.*;
 import novelang.parser.antlr.TreeFixture;
 import static novelang.parser.antlr.TreeFixture.tree;
 
 /**
  * Tests for {@link novelang.hierarchy.Hierarchizer}.
  * 
  * @author Laurent Caillette
  */
 public class UrlManglerTest {
 
   private static final Logger LOGGER = LoggerFactory.getLogger( UrlManglerTest.class ) ;
 
   @Test
   public void doNothingWhenNothingToDo() {
     final SyntacticTree tree = tree(
         PART,
         tree( PARAGRAPH_REGULAR ),
         tree( PARAGRAPH_REGULAR )
     );
     verifyFixNamedUrls(
         tree,
         tree
     ) ;
   }
 
   @Test
   public void fixNamedUrlAtStartOfAPart() {
     verifyFixNamedUrls( 
         tree(
             PART,
             tree( 
                 PARAGRAPH_REGULAR,
                 tree( 
                     _EXTERNAL_LINK,
                     tree( 
                         _LINK_NAME,
                         tree( WORD_, "name" ) 
                     ),
                     tree( URL, "http://foo.com" )
                 )                            
             )            
         ),
         tree( 
             PART,
             tree( WHITESPACE_ ),
             tree( 
                 PARAGRAPH_REGULAR,
                 tree( BLOCK_INSIDE_DOUBLE_QUOTES, tree( WORD_, "name" ) ),
                 tree( LINE_BREAK_ ),
                 tree( URL, "http://foo.com" )
                             
             )
         )        
     ) ;
   }
 
  /**
   * This test reproduces a bug. It stresses detection of paragraph exit.
   */
   @Test
   public void fixNamedUrlAtStartOfParagraphAfterLevelIntroducer() {
     verifyFixNamedUrls( 
         tree(
             PART,
             tree( 
                 PARAGRAPH_REGULAR,
                 tree( WORD_, "p" )
             ),
             tree( 
                 LEVEL_INTRODUCER_,
                 tree( LEVEL_INTRODUCER_INDENT_, "==" ),
                 tree( LEVEL_TITLE, tree( WORD_, "t" ) )            
             ),
             tree( 
                 PARAGRAPH_REGULAR,
                 tree( 
                     _EXTERNAL_LINK,
                     tree( 
                         _LINK_NAME,
                         tree( WORD_, "name" ) 
                     ),
                     tree( URL, "http://foo.com" )
                 )                            
             )            
         ),
         tree( 
             PART,
             tree( 
                 PARAGRAPH_REGULAR,
                 tree( WORD_, "p" )
             ),
             tree( LINE_BREAK_ ),
             tree( LINE_BREAK_ ),
             tree( 
                 LEVEL_INTRODUCER_,
                 tree( LEVEL_INTRODUCER_INDENT_, "==" ),
                 tree( LEVEL_TITLE, tree( WORD_, "t" ) )            
             ),
             tree( LINE_BREAK_ ),
             tree( LINE_BREAK_ ),
             tree( WHITESPACE_ ),
             tree( 
                 PARAGRAPH_REGULAR, 
                 tree( 
                     BLOCK_INSIDE_DOUBLE_QUOTES,
                     tree( WORD_, "name" )                
                 ),
                 tree( WHITESPACE_ ),
                 tree( LINE_BREAK_ ),
                 tree( URL, "http://foo.com")
             )
         )    
     ) ;
   }
 
   @Test
   public void fixUnnamedUrlAtStartOfAPart() {
     verifyFixNamedUrls( 
         tree(
             PART,
             tree( 
                 PARAGRAPH_REGULAR,
                 tree( 
                     _EXTERNAL_LINK,
                     tree( URL, "http://foo.com" )
                 )                            
             )            
         ),
         tree( 
             PART,
             tree( WHITESPACE_ ),
             tree( 
                 PARAGRAPH_REGULAR,
                 tree( URL, "http://foo.com" )
                             
             )
         )
         
     ) ;
   }
 
   @Test
   public void fixNamedUrlInsideAParagraph() {
     verifyFixNamedUrls( 
         tree( 
             PARAGRAPH_REGULAR,
             tree( WORD_, "w" ),
             tree( 
                 _EXTERNAL_LINK,
                 tree( 
                     _LINK_NAME,
                     tree( WORD_, "name" ) 
                 ),
                 tree( URL, "http://foo.com" )
             )                            
         ),            
         tree( 
             PARAGRAPH_REGULAR,
             tree( WORD_, "w"),
             tree( LINE_BREAK_ ),
             tree( WHITESPACE_ ),
             tree( BLOCK_INSIDE_DOUBLE_QUOTES, tree( WORD_, "name" ) ),
             tree( LINE_BREAK_ ),
             tree( URL, "http://foo.com" )
                         
         )        
     ) ;
 
   }
 
   @Test
   public void fixUrlWithoutNameAtStartOfAParagraph() {
     verifyFixNamedUrls( 
         tree(
             PARAGRAPH_REGULAR,
             tree( 
                 _EXTERNAL_LINK,
                 tree( URL, "http://foo.com" )
             )            
         ),
         tree( 
             PARAGRAPH_REGULAR,
             tree( URL, "http://foo.com" )
                         
         )        
     ) ;
   }
 
   @Test
   public void fixUrlWithoutNameInsideAParagraph() {
     verifyFixNamedUrls( 
         tree(
             PARAGRAPH_REGULAR,
             tree( WORD_, "w" ),
             tree( 
                 _EXTERNAL_LINK,
                 tree( URL, "http://foo.com" )
             )            
         ),
         tree( 
             PARAGRAPH_REGULAR,
             tree( WORD_, "w" ),
             tree( LINE_BREAK_ ),
             tree( URL, "http://foo.com" )
                         
         )        
     ) ;
   }
 
 
 // =======
 // Fixture
 // =======
 
   private static void verifyFixNamedUrls(
       SyntacticTree expectedTree,
       SyntacticTree rawTree
   ) {
     LOGGER.info( "Expected tree: " + TreeFixture.asString( expectedTree ) ) ;
     final Treepath< SyntacticTree > expectedTreepath = Treepath.create( expectedTree ) ;
 
     final Treepath< SyntacticTree > rehierarchized = fixNamedUrls( rawTree ) ;
 
     TreeFixture.assertEqualsNoSeparators(
         expectedTreepath.getTreeAtEnd(),
         rehierarchized.getTreeAtEnd()
     ); ;
 
   }
 
 
   private static Treepath< SyntacticTree > fixNamedUrls( final SyntacticTree rawTree ) {
     LOGGER.info( "Raw tree: " + TreeFixture.asString( rawTree ) ) ;
     Treepath< SyntacticTree > mangledTreepath =
         UrlMangler.fixNamedUrls( Treepath.create( rawTree ) ) ;
     SyntacticTree mangledTree = mangledTreepath.getTreeAtEnd() ;
     LOGGER.info( "Mangled tree: " + TreeFixture.asString( mangledTree ) ) ;
     mangledTree = SeparatorsMangler.removeSeparators( mangledTree ) ;
     LOGGER.info( "  No separators: " + TreeFixture.asString( mangledTree ) ) ;
 
     return mangledTreepath ;
 
   }
 
 
 
 }
