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
 
 package novelang.rendering;
 
 import java.io.ByteArrayOutputStream;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.io.UnsupportedEncodingException;
 import java.nio.charset.Charset;
 
 import static org.junit.Assert.assertEquals;
 import org.junit.Test;
 import com.google.common.collect.ImmutableList;
 import novelang.common.Nodepath;
 import novelang.common.Problem;
 import novelang.common.Renderable;
 import novelang.common.StylesheetMap;
 import novelang.common.SyntacticTree;
 import novelang.common.metadata.DocumentMetadata;
 import novelang.common.metadata.MetadataHelper;
 import novelang.parser.NodeKind;
 import static novelang.parser.NodeKind.*;
 import static novelang.parser.antlr.TreeFixture.tree;
 import novelang.system.DefaultCharset;
 
 /**
  * @author Laurent Caillette
  */
 public class GenericRendererTest {
 
   @Test
   public void whitespace1() throws Exception {
     final SyntacticTree tree = tree(
         BLOCK_INSIDE_PARENTHESIS,
         tree( WORD_, "first" ),
         tree( WORD_, "second")
     ) ;
     final GenericRenderer renderer = new GenericRenderer( new SimpleFragmentWriter(), "^" ) ;
     renderer.render( createRenderable( tree ), outputStream ) ;
     assertEquals( "BLOCK_INSIDE_PARENTHESIS(first^second)", getRenderedText() ) ;
   }
 
   @Test
   public void superscript() throws Exception {
     final SyntacticTree tree = tree(
         BLOCK_INSIDE_PARENTHESIS,
         tree( WORD_, tree( "super"), tree( WORD_AFTER_CIRCUMFLEX_ACCENT, "script" ) )
     ) ;
     final GenericRenderer renderer = new GenericRenderer( new SimpleFragmentWriter(), "^" ) ;
     renderer.render( createRenderable( tree ), outputStream ) ;
     assertEquals( "BLOCK_INSIDE_PARENTHESIS(superWORD_AFTER_CIRCUMFLEX_ACCENT(script))", getRenderedText() ) ;
   }
 
   @Test
   public void noSpaceInsideBlockAfterTilde1() throws Exception {
     final SyntacticTree tree = tree(
         BLOCK_AFTER_TILDE,
         tree(
             SUBBLOCK,
             tree( WORD_, "x" ),
             tree( BLOCK_INSIDE_PARENTHESIS, tree( WORD_, "y" ) ),
             tree( WORD_, "z" )
         )
     ) ;
     final GenericRenderer renderer = new GenericRenderer( new SimpleFragmentWriter(), "^" ) ;
     renderer.render( createRenderable( tree ), outputStream ) ;
     assertEquals(
         "BLOCK_AFTER_TILDE(SUBBLOCK(xBLOCK_INSIDE_PARENTHESIS(y)z))",
         getRenderedText()
     ) ;
   }
 
   @Test
   public void noSpaceInsideBlockAfterTilde2() throws Exception {
     final SyntacticTree tree = tree(
         PARAGRAPH_REGULAR,
         tree(
             BLOCK_AFTER_TILDE,
             tree(
                 SUBBLOCK,
                 tree( WORD_, "x" ),
                 tree( BLOCK_OF_LITERAL_INSIDE_GRAVE_ACCENTS, "/" ),
                 tree( WORD_, "y" )
             )
         ),
         tree( WORD_, "z" )
     ) ;
     final GenericRenderer renderer = new GenericRenderer( new SimpleFragmentWriter(), "^" ) ;
     renderer.render( createRenderable( tree ), outputStream ) ;
     assertEquals(
         "PARAGRAPH_REGULAR(BLOCK_AFTER_TILDE(SUBBLOCK(xBLOCK_OF_LITERAL_INSIDE_GRAVE_ACCENTS(/)y))^z)",
         getRenderedText()
     ) ;
   }
 
   @Test
   public void spaceInsideTwoBlocksInsideGraveAccent() throws Exception {
     final SyntacticTree tree = tree(
         PARAGRAPH_REGULAR,
         tree( BLOCK_OF_LITERAL_INSIDE_GRAVE_ACCENTS, "y" ),
         tree( BLOCK_OF_LITERAL_INSIDE_GRAVE_ACCENTS, "z" )
     ) ;
     final GenericRenderer renderer = new GenericRenderer( new SimpleFragmentWriter(), "^" ) ;
     renderer.render( createRenderable( tree ), outputStream ) ;
     assertEquals(
         "PARAGRAPH_REGULAR(BLOCK_OF_LITERAL_INSIDE_GRAVE_ACCENTS(y)^BLOCK_OF_LITERAL_INSIDE_GRAVE_ACCENTS(z))",
         getRenderedText()
     ) ;
   }
 
   @Test
   public void whitespace2() throws Exception {
     final SyntacticTree tree = tree(
         NodeKind.PARAGRAPH_REGULAR,
         tree( WORD_, "w0" ),
         tree(
             BLOCK_INSIDE_PARENTHESIS,
             tree( WORD_, "w1" ),
             tree( WORD_, "w2"),
             tree( PUNCTUATION_SIGN, tree( SIGN_FULLSTOP, "." ) )
         ),
         tree( WORD_, "w3" )
     ) ;
     final GenericRenderer renderer = new GenericRenderer( new SimpleFragmentWriter(), "^" ) ;
     renderer.render( createRenderable( tree ), outputStream ) ;
     assertEquals(
         "PARAGRAPH_REGULAR(w0^BLOCK_INSIDE_PARENTHESIS(w1^w2PUNCTUATION_SIGN(SIGN_FULLSTOP(.)))^w3)",
         getRenderedText()
     ) ;
   }
 
 
   @Test
   public void identifiers() throws Exception {
     final SyntacticTree tree = tree(
         NodeKind._LEVEL,
         tree( _IMPLICIT_IDENTIFIER, "Implicit" ),
         tree( _EXPLICIT_IDENTIFIER, "Explicit" )
     ) ;
     final GenericRenderer renderer = new GenericRenderer( new SimpleFragmentWriter(), "^" ) ;
     renderer.render( createRenderable( tree ), outputStream ) ;
     assertEquals(
        "_LEVEL(_IMPLICIT_IDENTIFIER(Implicit)_EXPLICIT_IDENTIFIER(Explicit))",
         getRenderedText()
     ) ;
   }
 
 
 // =======
 // Fixture
 // =======
 
   private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
 
   private String getRenderedText() {
     try {
       return new String( outputStream.toByteArray(), DefaultCharset.RENDERING.name() ) ;
     } catch( UnsupportedEncodingException e ) {
       throw new RuntimeException( e ) ;
     }
   }
 
   private static Renderable createRenderable( final SyntacticTree tree ) {
     final DocumentMetadata documentMetadata = MetadataHelper.createMetadata( DefaultCharset.RENDERING ) ;
     return new Renderable() {
       public Iterable< Problem > getProblems() {
         return ImmutableList.of() ;
       }
       public Charset getRenderingCharset() {
         return DefaultCharset.RENDERING ;
       }
       public boolean hasProblem() {
         return false;
       }
       public SyntacticTree getDocumentTree() {
         return tree ;
       }
       public DocumentMetadata getTreeMetadata() {
         return documentMetadata;
       }
 
       public StylesheetMap getCustomStylesheetMap() {
         return StylesheetMap.EMPTY_MAP ;
       }
     } ;
   }
 
   private static class SimpleFragmentWriter implements FragmentWriter {
 
     private PrintWriter writer ;
 
     public void startWriting(
         OutputStream outputStream,
         DocumentMetadata documentMetadata
     ) throws Exception {
       writer = new PrintWriter( outputStream ) ;
     }
 
     public void finishWriting() throws Exception {
       writer.flush() ;
     }
 
     public void start( Nodepath kinship, boolean wholeDocument ) throws Exception {
       writer.append( kinship.getCurrent().name() ).append( "(" ) ;
     }
 
     public void end( Nodepath kinship ) throws Exception {
       writer.append( ")" ) ;
     }
 
     public void write( Nodepath kinship, String word ) throws Exception {
       writer.append( word ) ;
     }
 
     public void writeLiteral( Nodepath kinship, String word ) throws Exception {
       write( kinship, word ) ;
     }
 
     public RenditionMimeType getMimeType() {
       return null ;
     }
   }
 
 }
