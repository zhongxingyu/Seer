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
 
 package novelang.model.implementation;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.apache.commons.lang.ClassUtils;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import novelang.ScratchDirectoryFixture;
 import novelang.TestResourceTools;
 import novelang.TestResources;
 import static novelang.model.common.NodeKind.*;
 import novelang.model.common.Tree;
 import novelang.parser.antlr.TreeHelper;
 import static novelang.parser.antlr.TreeHelper.tree;
 
 /**
  * @author Laurent Caillette
  */
 public class PartTest {
 
 
   @Test
   public void loadPartOk() throws IOException {
     final Part part = new Part( sections1File ) ;
     final Tree partTree = part.getTree();
     Assert.assertNotNull( partTree ) ;
     final Tree expected = tree( PART,  
        tree( SECTION,
            tree( IDENTIFIER, tree( WORD, "Section1nlp" ) ),
            tree( PARAGRAPH_PLAIN, tree( WORD, "p00" ), tree( WORD, "w001" ) )
        ),
        tree( SECTION,
            tree( TITLE, tree( WORD, "section1" ), tree( WORD, "w11" ) ),
            tree( PARAGRAPH_PLAIN,
                tree( WORD, "p10" ),
                tree( WORD, "w101" ),
                tree( WORD, "w102" )
            )
        )
     ) ;
     TreeHelper.assertEquals( expected, partTree ) ;
 
     Assert.assertFalse( part.getProblems().iterator().hasNext() ) ;
   }
 
   @Test
   public void findIdentifiersOk() throws IOException {
     final Part part = new Part( sections1File ) ;
     part.getIdentifiers() ;
   }
 
   private File book1Directory ;
   private File sections1File;
 
   @Before
   public void setUp() throws IOException {
     final String testName = ClassUtils.getShortClassName( getClass() );
     final ScratchDirectoryFixture scratchDirectoryFixture =
         new ScratchDirectoryFixture( testName ) ;
     book1Directory = scratchDirectoryFixture.getBook1Directory() ;
 
     TestResourceTools.copyResourceToFile( getClass(), TestResources.SECTIONS_1, book1Directory ) ;
     sections1File = new File( book1Directory, TestResources.SECTIONS_1 ) ;
   }
 
 }
