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
 
 package novelang.part;
 
 import java.io.File;
 import java.io.IOException;
import java.util.Iterator;
 
 import org.apache.commons.lang.ClassUtils;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import novelang.ScratchDirectoryFixture;
 import novelang.TestResourceTools;
 import novelang.TestResources;
 import static novelang.parser.NodeKind.*;
 import novelang.common.SyntacticTree;
import novelang.common.Problem;
 import novelang.parser.antlr.TreeFixture;
 import static novelang.parser.antlr.TreeFixture.tree;
 import novelang.parser.NodeKind;
 import novelang.parser.Escape;
import com.sun.org.apache.bcel.internal.generic.BREAKPOINT;
 
 /**
  * @author Laurent Caillette
  */
 public class PartTest {
 
 
   @Test
   public void loadPartOk() throws IOException {
     final Part part = new Part( justSections ) ;
     final SyntacticTree partTree = part.getDocumentTree();
     Assert.assertNotNull( partTree ) ;
     final SyntacticTree expected = tree( PART,
         tree( DELIMITER_THREE_EQUAL_SIGNS_,
             tree( DELIMITING_TEXT_, tree( WORD_, "Section1nlp" ) ),
             tree( NodeKind.PARAGRAPH_REGULAR, tree( WORD_, "p00" ), tree( WORD_, "w001" ) )
         ),
         tree( DELIMITER_THREE_EQUAL_SIGNS_,
             tree( DELIMITING_TEXT_, tree( WORD_, "section1" ), tree( WORD_, "w11" ) ),
             tree( NodeKind.PARAGRAPH_REGULAR, tree( WORD_, "p10" ), tree( WORD_, "w101" ), tree( WORD_, "w102" ) )
         )
     ) ;
 
     TreeFixture.assertEquals( expected, partTree ) ;
     Assert.assertFalse( part.getProblems().iterator().hasNext() ) ;
   }
 
   @Test
   public void loadPartWithMetadata() throws IOException {
     final Part part = new Part( justSections, true ) ;
     final SyntacticTree partTree = part.getDocumentTree();
     Assert.assertNotNull( partTree ) ;
     final SyntacticTree expected = tree( PART,
         tree( _META,
             tree( _WORD_COUNT, "8" )
         ),        
         tree( DELIMITER_THREE_EQUAL_SIGNS_,
             tree( DELIMITING_TEXT_, tree( WORD_, "Section1nlp" ) ),
             tree( NodeKind.PARAGRAPH_REGULAR, tree( WORD_, "p00" ), tree( WORD_, "w001" ) )
         ),
         tree( DELIMITER_THREE_EQUAL_SIGNS_,
             tree( DELIMITING_TEXT_, tree( WORD_, "section1" ), tree( WORD_, "w11" ) ),
             tree( NodeKind.PARAGRAPH_REGULAR, tree( WORD_, "p10" ), tree( WORD_, "w101" ), tree( WORD_, "w102" ) )
         )
     ) ;
 
     TreeFixture.assertEquals( expected, partTree ) ;
     Assert.assertFalse( part.getProblems().iterator().hasNext() ) ;
   }
 
   @Test
   public void loadSimpleStructure() throws IOException {
     final Part part = new Part( simpleStructureFile ) ;
     final SyntacticTree partTree = part.getDocumentTree();
     Assert.assertNotNull( partTree ) ;
     final SyntacticTree expected = tree( PART,
         tree( DELIMITER_TWO_EQUAL_SIGNS_,
             tree( DELIMITING_TEXT_, tree( WORD_, "Chapter-0" ) ),
             tree( DELIMITER_THREE_EQUAL_SIGNS_,
                 tree( DELIMITING_TEXT_, tree( WORD_, "Section-0-0" ) ),
                 tree( NodeKind.PARAGRAPH_REGULAR, tree( WORD_, "Paragraph-0-0-0" ) ),
                 tree( NodeKind.PARAGRAPH_REGULAR, tree( WORD_, "Paragraph-0-0-1" ) )
             ),
             tree( DELIMITER_THREE_EQUAL_SIGNS_,
                 tree( DELIMITING_TEXT_, tree( WORD_, "Section-0-1" ) ),
                 tree( NodeKind.PARAGRAPH_REGULAR, tree( WORD_, "Paragraph-0-1-0" ) )
             )
         ),
         tree( DELIMITER_TWO_EQUAL_SIGNS_,
             tree( DELIMITING_TEXT_, tree( WORD_, "Chapter-1" ) ),
             tree( DELIMITER_THREE_EQUAL_SIGNS_,
                 tree( DELIMITING_TEXT_, tree( WORD_, "Section-1-0" ) ),
                 tree( NodeKind.PARAGRAPH_REGULAR, tree( WORD_, "Paragraph-1-0-0" ) ),
                 tree( NodeKind.PARAGRAPH_REGULAR, tree( WORD_, "Paragraph-1-0-1" ) )
             ),
             tree( DELIMITER_THREE_EQUAL_SIGNS_,
                 tree( DELIMITING_TEXT_, tree( WORD_, "Section-1-1" ) ),
                 tree( NodeKind.PARAGRAPH_REGULAR, tree( WORD_, "Paragraph-1-1-0" ) )
             )
         )
     ) ;
 
     TreeFixture.assertEquals( expected, partTree ) ;
     Assert.assertFalse( part.getProblems().iterator().hasNext() ) ;
 
   }
   
   @Test 
   public void partWithParsingErrorDoesNotAttemptToCountWords() {
     final Part part = new Part( "````", true ) ;
     Assert.assertTrue( part.hasProblem() ) ;
   }
  
  @Test
  public void problemWithBadEscapeCodeHasLocation() {
    final Part part = new Part(
        "\n" +
        "..." + Escape.ESCAPE_START + "unknown-escape-code" + Escape.ESCAPE_END 
    ) ;
    Assert.assertTrue( part.hasProblem() ) ;
    final Iterator<Problem> problems = part.getProblems().iterator();
    final Problem problem = problems.next() ;
    Assert.assertFalse( problems.hasNext() ) ;
    Assert.assertEquals( 2, problem.getLocation().getLine() ) ;
    Assert.assertEquals( 4, problem.getLocation().getColumn() ) ;
  }
 
   @Test
   public void loadMessyIdentifiers() throws IOException {
     final Part part = new Part( messyIdentifiersFile ) ;
     part.getIdentifiers() ;    
   }
 
   @Test
   public void findIdentifiersOk() throws IOException {
     final Part part = new Part( justSections ) ;
     part.getIdentifiers() ;
   }
 
 
 
 // =======
 // Fixture
 // =======
 
   private File justSections;
   private File messyIdentifiersFile ;
   private File simpleStructureFile ;
 
   @Before
   public void setUp() throws IOException {
     final String testName = ClassUtils.getShortClassName( getClass() );
     final File scratchDirectory = new ScratchDirectoryFixture( testName ).
         getTestScratchDirectory() ;
 
     justSections = TestResourceTools.copyResourceToDirectory(
         getClass(),
         TestResources.JUST_SECTIONS,
         scratchDirectory
     ) ;
 
 
     messyIdentifiersFile = TestResourceTools.copyResourceToDirectory(
         getClass(),
         TestResources.MESSY_IDENTIFIERS,
         scratchDirectory
     ) ;
 
     simpleStructureFile = TestResourceTools.copyResourceToDirectory(
         getClass(),
         TestResources.SIMPLE_STRUCTURE,
         scratchDirectory
     ) ;
 
   }
 
 }
