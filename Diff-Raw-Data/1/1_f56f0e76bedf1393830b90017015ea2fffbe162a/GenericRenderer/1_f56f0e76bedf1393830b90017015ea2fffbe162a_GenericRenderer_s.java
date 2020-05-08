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
 
 import com.google.common.base.Objects;
 import novelang.common.metadata.MetadataHelper;
 import novelang.common.NodeKind;
 import novelang.common.Nodepath;
 import novelang.common.Problem;
 import novelang.common.SyntacticTree;
 import novelang.common.Renderable;
 import novelang.common.LanguageTools;
 import static novelang.common.NodeKind.WORD;
 
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
     this.fragmentWriter = Objects.nonNull( fragmentWriter ) ;
     this.whitespace = whitespace ;
   }
 
   final public void render(
       Renderable rendered,
       OutputStream outputStream
   ) {
     if( rendered.hasProblem() ) {
       renderProblems( rendered.getProblems(), outputStream ) ;
     } else {
       try {
         fragmentWriter.startWriting(
             outputStream,
             MetadataHelper.createMetadata( rendered.getDocumentTree(), rendered.getEncoding() ),
             rendered.getEncoding()
         ) ;
         final SyntacticTree root = rendered.getDocumentTree() ;
         renderTree( root, null, null ) ;
         fragmentWriter.finishWriting() ;
       } catch( Exception e ) {
         LanguageTools.rethrowUnchecked( e ) ;
       }
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
 
     final NodeKind nodeKind = NodeKind.ofRoot( tree ) ;
     final Nodepath newPath = (
         null == kinship ? new Nodepath( nodeKind ) : new Nodepath( kinship, nodeKind ) ) ;
     boolean rootElement = false ;
 
     switch( nodeKind ) {
 
       case WORD :
         final SyntacticTree wordTree = tree.getChildAt( 0 ) ;
         fragmentWriter.write( newPath, wordTree.getText() ) ;
         // Handle superscript
         if( tree.getChildCount() > 1 ) {
           for( int childIndex = 1 ; childIndex < tree.getChildCount() ; childIndex++ ) {
             final SyntacticTree child = tree.getChildAt( childIndex ) ;
             renderTree( child, newPath, WORD ) ;
           }
         }
         break ;
 
       case SUPERSCRIPT :
         final SyntacticTree superscriptTree = tree.getChildAt( 0 ) ;
         fragmentWriter.start( newPath, rootElement ) ;
         fragmentWriter.write( newPath, superscriptTree.getText() ) ;
         fragmentWriter.end( newPath ) ;
         break ;
 
       case URL :
       case _META_TIMESTAMP :
       case LITERAL :
       case SOFT_INLINE_LITERAL :
       case HARD_INLINE_LITERAL :
         fragmentWriter.start( newPath, false ) ;
         final SyntacticTree literalTree = tree.getChildAt( 0 ) ;
         fragmentWriter.writeLiteral( newPath, literalTree.getText() ); ;
         fragmentWriter.end( newPath ) ;
         break ;
 
       case SIGN_COLON :
       case SIGN_COMMA :
       case SIGN_ELLIPSIS :
       case SIGN_EXCLAMATIONMARK :
       case SIGN_FULLSTOP :
       case SIGN_QUESTIONMARK :
       case SIGN_SEMICOLON :
         fragmentWriter.start( newPath, false ) ;
         fragmentWriter.end( newPath ) ;
         break ;
 
       case BOOK:
       case PART :
         rootElement = true ;
 
       default :
         fragmentWriter.start( newPath, rootElement ) ;
         previous = null ;
         for( SyntacticTree subtree : tree.getChildren() ) {
           final NodeKind subtreeNodeKind = NodeKind.ofRoot( subtree );
           maybeWriteWhitespace( newPath, previous, subtreeNodeKind ) ;
           renderTree( subtree, newPath, previous ) ;
           previous = subtreeNodeKind;
         }
         fragmentWriter.end( newPath ) ;
         break ;
 
     }
 
   }
 
   private void maybeWriteWhitespace(
       Nodepath path,
       NodeKind previous,
       NodeKind nodeKind
   ) throws Exception {
     if( WhitespaceTrigger.isTrigger( previous, nodeKind ) ) {
       fragmentWriter.write( path, whitespace ) ;
     }
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
