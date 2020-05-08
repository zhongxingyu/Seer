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
 package novelang.book;
 
 import novelang.ScratchDirectoryFixture;
 import novelang.TestResourceTools;
 import novelang.TestResources;
 import novelang.common.SyntacticTree;
 import novelang.parser.NodeKind;
 import static novelang.parser.NodeKind.*;
 import novelang.parser.antlr.TreeFixture;
 import static novelang.parser.antlr.TreeFixture.tree;
 import novelang.system.Log;
 import novelang.system.LogFactory;
 
 import org.apache.commons.lang.ClassUtils;
 import org.apache.commons.lang.SystemUtils;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 
 import java.io.File;
 import java.io.IOException;
 
 /**
  * Test for {@link Book} and also built-in functions.
  * 
  * @author Laurent Caillette
  */
 public class BookTest {
 
   private static final Log LOG = LogFactory.getLog( BookTest.class ) ;
 
  
 
   /**
    * Test {@link novelang.book.function.builtin.InsertCommand}.
    */
   @Test
   public void justInsert() {
 
     final Book book = new Book(
         SystemUtils.getUserDir(),
         "insert file:" + oneWordFile.getAbsolutePath()
     ) ;
     final SyntacticTree bookTree = book.getDocumentTree() ;
     TreeFixture.assertEqualsNoSeparators(
         tree( BOOK,
             tree( _META, tree( _WORD_COUNT, "1" ) ),
             tree(
                 NodeKind.PARAGRAPH_REGULAR,
                 tree( WORD_, "oneword" )
             )
         ),
         bookTree
     ) ;
     Assert.assertFalse( book.hasProblem() ) ;
 
   }
 
   /**
    * Test {@link novelang.book.function.builtin.InsertCommand}.
    */
   @Test
   public void insertWithRecursiveFileScan() throws IOException {
     final Book book = new Book( scannedBookNoStyle ) ;
     LOG.debug( "Book's document tree: %s", book.getDocumentTree().toStringTree() ) ;
 
     final SyntacticTree bookTree = book.getDocumentTree() ;
     TreeFixture.assertEqualsNoSeparators(
         tree( BOOK,
             tree( _META, tree( _WORD_COUNT, "6" ) ),
             tree(
                 _LEVEL,
                 tree( NodeKind.LEVEL_TITLE, tree( WORD_, "file1" ) ),
                 tree(
                     NodeKind.PARAGRAPH_REGULAR,
                     tree( WORD_, "content-of-file1" )
                 )
             ),
             tree(
                 _LEVEL,
                 tree( NodeKind.LEVEL_TITLE, tree( WORD_, "file2" ) ),
                 tree(
                     NodeKind.PARAGRAPH_REGULAR,
                     tree( WORD_, "content-of-file2" )
                 )
             ),
             tree(
                 _LEVEL,
                 tree( NodeKind.LEVEL_TITLE, tree( WORD_, "file3" ) ),
                 tree(
                     NodeKind.PARAGRAPH_REGULAR,
                     tree( WORD_, "content-of-file3" )
                 )
             )
         ),
         bookTree
     ) ;
     Assert.assertFalse( book.hasProblem() ) ;
 
 
   }
 
   /**
    * Test {@link novelang.book.function.builtin.InsertCommand}.
    */
   @Test
   public void insertWithFlatFileScan() throws IOException {
     final Book book = new Book( scannedBookNoStyleNoRecurse ) ;
     LOG.debug( "Book's document tree: %s", book.getDocumentTree().toStringTree() ) ;
 
     final SyntacticTree bookTree = book.getDocumentTree() ;
     TreeFixture.assertEqualsNoSeparators(
         tree( 
             BOOK,
             tree( _META, tree( _WORD_COUNT, "2" ) ),
             tree(
                 NodeKind.PARAGRAPH_REGULAR,
                 tree( WORD_, "content-of-file1" )
             ),
             tree(
                 NodeKind.PARAGRAPH_REGULAR,
                 tree( WORD_, "content-of-file2" )
             )
         ),
         bookTree
     ) ;
     Assert.assertFalse( book.hasProblem() ) ;
 
 
   }
 
 
   /**
    * Test {@link novelang.book.function.builtin.InsertCommand}.
    */
   @Test
   public void insertWithFileScanAndStyle() throws IOException {
     final Book book = new Book( scannedBookWithStyle );
     LOG.debug( "Book's document tree: %s", book.getDocumentTree().toStringTree() ) ;
 
     final SyntacticTree bookTree = book.getDocumentTree() ;
     TreeFixture.assertEqualsNoSeparators(
         tree( BOOK,
             tree( _META, tree( _WORD_COUNT, "6" ) ),
             tree(
                 _LEVEL,
                 tree( NodeKind._STYLE, tree( CUSTOM_STYLE ) ),
                 tree( NodeKind.LEVEL_TITLE, tree( WORD_, "file1" ) ),
                 tree(
                     NodeKind.PARAGRAPH_REGULAR,
                     tree( WORD_, "content-of-file1" )
                 )
             ),
             tree(
                 _LEVEL,
                 tree( NodeKind._STYLE, tree( CUSTOM_STYLE ) ),
                 tree( NodeKind.LEVEL_TITLE, tree( WORD_, "file2" ) ),
                 tree(
                     NodeKind.PARAGRAPH_REGULAR,
                     tree( WORD_, "content-of-file2" )
                 )
             ),
             tree(
                 _LEVEL,
                 tree( NodeKind._STYLE, tree( CUSTOM_STYLE ) ),
                 tree( NodeKind.LEVEL_TITLE, tree( WORD_, "file3" ) ),
                 tree(
                     NodeKind.PARAGRAPH_REGULAR,
                     tree( WORD_, "content-of-file3" )
                 )
             )
         ),
         bookTree
     ) ;
     Assert.assertFalse( book.hasProblem() ) ;
   }
 
 
 
   /**
    * Test {@link novelang.book.function.builtin.InsertCommand}.
    */
   @Test
   public void insertWithBadPart() throws IOException {
     final Book book = new Book( scannedBookWithBadPart ) ;
     LOG.debug( "Book's document tree: %s", book.getDocumentTree().toStringTree() ) ;
 
     Assert.assertTrue( book.hasProblem() ) ;
 
 
   }
 
 
 // =======
 // Fixture
 // =======
 
   private static final String ONE_WORD_FILENAME = TestResources.ONE_WORD_ABSOLUTEFILENAME;
   private File oneWordFile ;
 
   public static final String SCANNED_BOOK_FILENAME = TestResources.SCANNED_DIR ;
   private File scannedBookNoStyle ;
   private File scannedBookNoStyleNoRecurse ;
   private File scannedBookWithStyle ;
   private File scannedBookWithBadPart ;
 
   public static final String CUSTOM_STYLE = "mystyle" ;
 
   @Before
   public void setUp() throws Exception {
 
     final String testName = ClassUtils.getShortClassName( getClass() ) ;
     final ScratchDirectoryFixture scratchDirectoryFixture =
         new ScratchDirectoryFixture( testName ) ;
     final File contentDirectory = scratchDirectoryFixture.getTestScratchDirectory() ;
 
     oneWordFile = TestResourceTools.copyResourceToDirectory(
         getClass(),
         ONE_WORD_FILENAME,
         contentDirectory
     ) ;
 
     TestResourceTools.copyResourceToDirectory(
         getClass(), TestResources.SCANNED_FILE1, contentDirectory ) ;
     TestResourceTools.copyResourceToDirectory(
         getClass(), TestResources.SCANNED_FILE2, contentDirectory ) ;
     TestResourceTools.copyResourceToDirectory(
         getClass(), TestResources.SCANNED_FILE3, contentDirectory ) ;
     scannedBookNoStyle = TestResourceTools.copyResourceToDirectory(
         getClass(), TestResources.SCANNED_BOOK_NOSTYLE, contentDirectory ) ;
     scannedBookNoStyleNoRecurse = TestResourceTools.copyResourceToDirectory(
         getClass(), TestResources.SCANNED_BOOK_NOSTYLE_NORECURSE, contentDirectory ) ;
     scannedBookWithStyle = TestResourceTools.copyResourceToDirectory(
         getClass(), TestResources.SCANNED_BOOK_WITHSTYLE, contentDirectory ) ;
 
     scannedBookWithBadPart = TestResourceTools.copyResourceToDirectory(
         getClass(), TestResources.SERVED_BOOK_BADSCANNEDPART, contentDirectory ) ;
     TestResourceTools.copyResourceToDirectory(
         getClass(), TestResources.SERVED_PARTSOURCE_GOOD, contentDirectory ) ;
     TestResourceTools.copyResourceToDirectory(
         getClass(), TestResources.SERVED_PARTSOURCE_BROKEN, contentDirectory ) ;
 
   }
 
 }
