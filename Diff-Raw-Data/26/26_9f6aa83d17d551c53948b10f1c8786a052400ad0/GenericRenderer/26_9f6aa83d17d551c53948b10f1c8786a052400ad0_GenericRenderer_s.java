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
 
 import java.io.OutputStream;
 import java.io.PrintWriter;
 
 import com.google.common.base.Preconditions;
 import com.google.common.base.Joiner;
 import novelang.common.Nodepath;
 import novelang.common.Problem;
 import novelang.common.Renderable;
 import novelang.common.SyntacticTree;
 import novelang.common.metadata.MetadataHelper;
 import novelang.parser.NodeKind;
 import static novelang.parser.NodeKind.*;
 import novelang.parser.NodeKindTools;
 
 /**
  * The only implementation of {@code Renderer} making sense as it delegates all specific
  * tasks to {@link novelang.rendering.FragmentWriter}.
  *
  * @author Laurent Caillette
  */
 public class GenericRenderer implements Renderer {
 
   private final FragmentWriter fragmentWriter ;
   private final String whitespace ;
 
   private static final String DEFAULT_WHITESPACE = " " ;
 
   public GenericRenderer( FragmentWriter fragmentWriter ) {
     this( fragmentWriter, DEFAULT_WHITESPACE ) ;
   }
 
   protected GenericRenderer( FragmentWriter fragmentWriter, String whitespace ) {
     this.fragmentWriter = Preconditions.checkNotNull( fragmentWriter ) ;
     this.whitespace = whitespace ;
   }
 
   final public void render(
       Renderable rendered,
       OutputStream outputStream
   ) throws Exception {
     if( rendered.hasProblem() ) {
       renderProblems( rendered.getProblems(), outputStream ) ;
     } else {
       fragmentWriter.startWriting(
           outputStream,
           MetadataHelper.createMetadata( rendered.getRenderingCharset() )
       ) ;
       final SyntacticTree root = rendered.getDocumentTree() ;
       renderTree( root, null, null ) ;
       fragmentWriter.finishWriting() ;
     }
   }
 
   public RenditionMimeType getMimeType() {
     return fragmentWriter.getMimeType() ;
   }
 
   private void renderTree(
       SyntacticTree tree,
       Nodepath kinship,
       NodeKind previous
   ) throws Exception {
 
     final NodeKind nodeKind = NodeKindTools.ofRoot( tree ) ;
     final Nodepath newPath = ( createNodepath( kinship, nodeKind ) ) ;
     boolean isRootElement = false ;
 
     switch( nodeKind ) {
 
       case WORD_:
         final SyntacticTree wordTree = tree.getChildAt( 0 ) ;
         fragmentWriter.write( newPath, wordTree.getText() ) ;
         // Handle superscript
         if( tree.getChildCount() > 1 ) {
           for( int childIndex = 1 ; childIndex < tree.getChildCount() ; childIndex++ ) {
             final SyntacticTree child = tree.getChildAt( childIndex ) ;
             renderTree( child, newPath, WORD_ ) ;
           }
         }
         break ;
 
       case WORD_AFTER_CIRCUMFLEX_ACCENT:
         final SyntacticTree superscriptTree = tree.getChildAt( 0 ) ;
         fragmentWriter.start( newPath, isRootElement ) ;
         fragmentWriter.write( newPath, superscriptTree.getText() ) ;
         fragmentWriter.end( newPath ) ;
         break ;
 
 
       case BLOCK_OF_LITERAL_INSIDE_GRAVE_ACCENTS :
       case BLOCK_OF_LITERAL_INSIDE_GRAVE_ACCENT_PAIRS :
         writeLiteral(
             fragmentWriter,
             newPath,
             Spaces.normalizeLiteral( tree.getChildAt( 0 ).getText() )
         ) ;
         break ;
 
       case ABSOLUTE_IDENTIFIER :
       case RELATIVE_IDENTIFIER :
       case COMPOSITE_IDENTIFIER :
         fragmentWriter.start( newPath, isRootElement ) ;
         final StringBuilder builder = new StringBuilder( "\\" ) ;
         for( final SyntacticTree child : tree.getChildren() ) {
           builder.append( "\\" ) ;
          builder.append( child.toString() ) ;
         }
         fragmentWriter.write( newPath, builder.toString() ) ;
         fragmentWriter.end( newPath ) ;
         break ;
 
       case URL_LITERAL :
       case TAG :
       case _WORD_COUNT :
       case _STYLE :
       case LINES_OF_LITERAL :
       case _IMAGE_WIDTH:
       case _IMAGE_HEIGHT:
         final SyntacticTree literalTree = tree.getChildAt( 0 ) ;
         writeLiteral( fragmentWriter, newPath, literalTree.getText() ) ;
         break ;
 
       case RESOURCE_LOCATION:
       case APOSTROPHE_WORDMATE :
       case SIGN_COLON :
       case SIGN_COMMA :
       case SIGN_ELLIPSIS :
       case SIGN_EXCLAMATIONMARK :
       case SIGN_FULLSTOP :
       case SIGN_QUESTIONMARK :
       case SIGN_SEMICOLON :
         fragmentWriter.start( newPath, false ) ;
         fragmentWriter.write( newPath, tree.getChildAt( 0 ).getText() ) ;
         fragmentWriter.end( newPath ) ;
         break ;
 
       case LEVEL_INTRODUCER_INDENT_ :
         break ;
 
       case PARAGRAPH_AS_LIST_ITEM_WITH_TRIPLE_HYPHEN_ :
         processByDefault( tree, createNodepath( kinship, _PARAGRAPH_AS_LIST_ITEM ), false ) ;
         break ;
 
       case EMBEDDED_LIST_ITEM_WITH_HYPHEN_ :
         processByDefault( tree, createNodepath( kinship, _EMBEDDED_LIST_ITEM ), false ) ;
         break ;
 
       case LEVEL_TITLE:
         processByDefault( tree, createNodepath( kinship, LEVEL_TITLE ), false ) ;
         break ;
 
       case BOOK:
       case PART :
         isRootElement = true ;
 
       default :
         processByDefault( tree, newPath, isRootElement );
         break ;
 
     }
 
   }
 
   private static void writeLiteral(
       FragmentWriter fragmentWriter,
       Nodepath newPath,
       String literal
   ) throws Exception {
     fragmentWriter.start( newPath, false ) ;
     fragmentWriter.writeLiteral( newPath, literal ) ;
     fragmentWriter.end( newPath ) ;
   }
 
   private Nodepath createNodepath( Nodepath kinship, NodeKind kind ) {
     return null == kinship ? new Nodepath( kind ) : new Nodepath( kinship, kind );
   }
 
   private void processByDefault( SyntacticTree tree, Nodepath path, boolean rootElement ) throws Exception {
     NodeKind previous;
     fragmentWriter.start( path, rootElement ) ;
     previous = null ;
     for( SyntacticTree subtree : tree.getChildren() ) {
       final NodeKind subtreeNodeKind = NodeKindTools.ofRoot( subtree );
       maybeWriteWhitespace( path, previous, subtreeNodeKind ) ;
       renderTree( subtree, path, previous ) ;
       previous = subtreeNodeKind;
     }
     fragmentWriter.end( path ) ;
   }
 
   private void maybeWriteWhitespace(
       Nodepath path,
       NodeKind previous,
       NodeKind nodeKind
   ) throws Exception {
     if( ! hasBlockAfterTilde( path ) && Spaces.isTrigger( previous, nodeKind ) ) {
       fragmentWriter.write( path, whitespace ) ;
     }
   }
 
   private static boolean hasBlockAfterTilde( Nodepath path ) {
     if( path == null ) {
       return false ;
     }
     if( path.getCurrent() == BLOCK_AFTER_TILDE ) {
       return true ;
     }
     return hasBlockAfterTilde( path.getAncestor() ) ;
 
   }
 
   protected RenditionMimeType renderProblems(
       Iterable< Problem > problems,
       OutputStream outputStream
   ) {
     final PrintWriter writer = new PrintWriter( outputStream ) ;
     for( final Problem problem : problems ) {
       writer.println( problem.getLocation() ) ;
       writer.println( "    " + problem.getMessage() ) ;
     }
     writer.flush() ;
     return RenditionMimeType.TXT;
   }
 
 
 }
