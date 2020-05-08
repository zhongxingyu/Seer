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
 
 import org.junit.Assert;
 import org.junit.Test;
 import com.google.common.collect.Lists;
 import novelang.model.common.MetadataHelper;
 import static novelang.model.common.NodeKind.*;
 import novelang.model.common.NodePath;
 import novelang.model.common.Problem;
 import novelang.model.common.Tree;
 import novelang.model.common.TreeMetadata;
 import novelang.model.renderable.Renderable;
 import novelang.parser.Encoding;
 import static novelang.parser.antlr.TreeHelper.tree;
 
 /**
  * @author Laurent Caillette
  */
 public class GenericRendererTest {
 
   @Test
   public void whitespace1() throws Exception {
     final Tree tree = tree( PARENTHESIS, tree( WORD, "first" ), tree( WORD, "second") ) ;
    final GenericRenderer renderer = new GenericRenderer( new SimpleFragmentWriter(), "^" ) ;
     renderer.render( createRenderable( tree ), outputStream ) ;
    Assert.assertEquals( "PARENTHESIS(first^second)", getRenderedText() ) ;
   }
 
   @Test
   public void whitespace2() throws Exception {
     final Tree tree = tree(
         PARAGRAPH_PLAIN,
         tree( WORD, "w0" ),
         tree(
             PARENTHESIS,
             tree( WORD, "w1" ),
             tree( WORD, "w2"),
             tree( PUNCTUATION_SIGN, tree( SIGN_FULLSTOP ) )
         ),
         tree( WORD, "w3" )
     ) ;
    final GenericRenderer renderer = new GenericRenderer( new SimpleFragmentWriter(), "^" ) ;
     renderer.render( createRenderable( tree ), outputStream ) ;
     Assert.assertEquals(
        "PARAGRAPH_PLAIN(w0^PARENTHESIS(w1^w2PUNCTUATION_SIGN(SIGN_FULLSTOP()))^w3)",
         getRenderedText()
     ) ;
   }
 
 
 // =======
 // Fixture
 // =======
 
   private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
 
   private String getRenderedText() {
     try {
       return new String( outputStream.toByteArray(), Encoding.DEFAULT.name() ) ;
     } catch( UnsupportedEncodingException e ) {
       throw new RuntimeException( e ) ;
     }
   }
 
   private static Renderable createRenderable( final Tree tree ) {
     final TreeMetadata treeMetadata = MetadataHelper.createMetadata( tree, Encoding.DEFAULT ) ;
     return new Renderable() {
       public Iterable< Problem > getProblems() {
         return Lists.immutableList() ;
       }
       public Charset getEncoding() {
         return Encoding.DEFAULT ;
       }
       public boolean hasProblem() {
         return false;
       }
       public Tree getTree() {
         return tree ;
       }
       public TreeMetadata getTreeMetadata() {
         return treeMetadata ;
       }
     } ;
   }
 
   private static class SimpleFragmentWriter implements FragmentWriter {
 
     private PrintWriter writer ;
 
     public void startWriting(
         OutputStream outputStream,
         TreeMetadata treeMetadata,
         Charset encoding
     ) throws Exception {
       writer = new PrintWriter( outputStream ) ;
     }
 
     public void finishWriting() throws Exception {
       writer.flush() ;
     }
 
     public void start( NodePath kinship, boolean wholeDocument ) throws Exception {
       writer.append( kinship.getCurrent().name() ).append( "(" ) ;
     }
 
     public void end( NodePath kinship ) throws Exception {
       writer.append( ")" ) ;
     }
 
     public void write( NodePath kinship, String word ) throws Exception {
       writer.append( word ) ;
     }
 
     public void writeLitteral( NodePath kinship, String word ) throws Exception {
       write( kinship, word ) ;
     }
 
     public RenditionMimeType getMimeType() {
       return null ;
     }
   }
 
 }
