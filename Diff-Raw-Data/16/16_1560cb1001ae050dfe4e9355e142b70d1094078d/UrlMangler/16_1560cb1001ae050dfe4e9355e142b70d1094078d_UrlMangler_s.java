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
 package novelang.hierarchy;
 
 import novelang.common.SyntacticTree;
 import novelang.common.SimpleTree;
 import novelang.common.tree.Treepath;
 import novelang.common.tree.TreepathTools;
 import novelang.parser.NodeKind;
 import static novelang.parser.NodeKind.*;
 
 /**
  * Replaces {@link NodeKind#URL} nodes by {@link NodeKind#_EXTERNAL_LINK}, adding
  * preceding {@link NodeKind#BLOCK_INSIDE_DOUBLE_QUOTES} if there is one alone on a line.
  * 
  * <pre>
  *    "external link name"
  * http://url.towards.somewhe.re
  * </pre>
  *  
  * @author Laurent Caillette
  */
 public class UrlMangler {
 
   public static Treepath< SyntacticTree > fixNamedUrls(
       final Treepath< SyntacticTree > treepath
   ) {
     State state = State.OUTSIDE_PARAGRAPH;
     Treepath< SyntacticTree > treepathToName = null ;
     int paragraphDepth = -1 ;
 
     Treepath< SyntacticTree > current = treepath ;
     Treepath< SyntacticTree > result = current ;
 
     while( current != null ) {
       final SyntacticTree tree = current.getTreeAtEnd() ;
       
       switch ( state ) {
         
         case OUTSIDE_PARAGRAPH:
           state = evaluate(
               tree,
               WHITESPACE_,
               State.WHITESPACE_OUTSIDE_PARAGRAPH,
               evaluate( tree, PARAGRAPH_NODEKINDS, State.INSIDE_PARAGRAPH, State.OUTSIDE_PARAGRAPH )
           ) ;
           if( State.INSIDE_PARAGRAPH == state ) {
             paragraphDepth = current.getLength() ;
           }
           break ;
         
         case WHITESPACE_OUTSIDE_PARAGRAPH :
           state = evaluate(
               tree,
               PARAGRAPH_NODEKINDS,
               State.INSIDE_PARAGRAPH_PRECEDED_BY_WHITESPACE,
               State.OUTSIDE_PARAGRAPH
           ) ;
           if( State.INSIDE_PARAGRAPH_PRECEDED_BY_WHITESPACE == state ) {
             paragraphDepth = current.getLength() ;
           }
           break ;
         
         case INSIDE_PARAGRAPH_PRECEDED_BY_WHITESPACE :
           state = evaluate(
               tree,
               BLOCK_INSIDE_DOUBLE_QUOTES,
               State.DOUBLE_QUOTES,
               evaluate( tree, URL, State.URL )
           ) ;
           if ( State.DOUBLE_QUOTES == state ) {
             treepathToName = current ;
           }
           break ;
 
         case INSIDE_PARAGRAPH :
           state = evaluate( 
               tree,
               LINE_BREAK_,
               State.LINEBREAK_INSIDE_PARAGRAPH,
               evaluate( tree, URL, State.URL )
           ) ;
           break ;
         
         case LINEBREAK_INSIDE_PARAGRAPH :
           state = evaluate(
               tree,
               WHITESPACE_,
               State.INDENTATION_INSIDE_PARAGRAPH,
               evaluate( tree, URL, State.URL )              
           ) ;
           break ;
 
         case INDENTATION_INSIDE_PARAGRAPH :
           state = evaluate(
               tree,
               BLOCK_INSIDE_DOUBLE_QUOTES,
               State.DOUBLE_QUOTES
           ) ;
           if( state == State.DOUBLE_QUOTES ) {
             treepathToName = current ;
           }
           break ;
 
         case DOUBLE_QUOTES :
           state = evaluate( 
               tree, WHITESPACE_, State.WHITESPACE_AFTER_DOUBLE_QUOTES,
               evaluate( tree, LINE_BREAK_, State.LINE_BREAK_AFTER_DOUBLE_QUOTES )
           ) ;
           break ;
         
         case WHITESPACE_AFTER_DOUBLE_QUOTES :
           state = evaluate(
               tree,
               LINE_BREAK_,
               State.LINE_BREAK_AFTER_DOUBLE_QUOTES
           ) ;
           break ;
 
         case LINE_BREAK_AFTER_DOUBLE_QUOTES :
           state = evaluate(
               tree,
               URL,
               State.URL
           ) ;
           break ;
 
         case URL :
           break ;
 
         default :
           throw new IllegalStateException( "Unsupported: " + state ) ;
       }
 
       if( State.URL == state ) {
         if( null != treepathToName ) {
           current = TreepathTools.removeSubtree( current, treepathToName ) ;
         }
         current = replaceByExternalLink( current, treepathToName ) ;
         treepathToName = null ;
         state = State.INSIDE_PARAGRAPH ;
       }
 
       result = current ;
       if( State.DOUBLE_QUOTES == state
        || current.getTreeAtEnd().isOneOf( _EXTERNAL_LINK ) 
        || tree.isOneOf( SKIPPED_NODEKINDS )
       ) {
         current = TreepathTools.getNextUpInPreorder( current ) ;
       } else {
         current = TreepathTools.getNextInPreorder( current ) ;
       }
 
      if( current != null && current.getLength() < paragraphDepth ) {
         state = State.OUTSIDE_PARAGRAPH ;
       }
 
     }
     return result.getStart() ;
   }
 
   private static final NodeKind[] PARAGRAPH_NODEKINDS = new NodeKind[] {
       PARAGRAPH_REGULAR, 
       PARAGRAPH_AS_LIST_ITEM_WITH_TRIPLE_HYPHEN_
   } ;
   
   private static final NodeKind[] SKIPPED_NODEKINDS = new NodeKind[] {
       WORD_,
       CELL_ROWS_WITH_VERTICAL_LINE,
       RASTER_IMAGE,
       VECTOR_IMAGE,
       BLOCK_OF_LITERAL_INSIDE_GRAVE_ACCENTS,
       LEVEL_INTRODUCER_INDENT_,
       LINES_OF_LITERAL,
       RESOURCE_LOCATION
   } ;
   
   
 
   /**
    * Replaces the {@link NodeKind#URL} node at the end of the treepath by a 
    * {@link NodeKind#_EXTERNAL_LINK} node.
    *  
    * @param treepathToUrl treepath to the URL node.
    * @param treepathToUrl treepath to the name node, which must be
    *     of {@link NodeKind#BLOCK_INSIDE_DOUBLE_QUOTES} type. 
    * @return the new treepath.
    */
   private static Treepath< SyntacticTree > replaceByExternalLink( 
       Treepath< SyntacticTree > treepathToUrl, 
       Treepath< SyntacticTree > treepathToName
   ) {
     final SyntacticTree nameTree;
     final SyntacticTree[] children ;
     if( null == treepathToName ) {
       children = new SyntacticTree[] { treepathToUrl.getTreeAtEnd() } ;
 
     } else {
       nameTree = new SimpleTree(
           NodeKind._LINK_NAME.name(),
           treepathToName.getTreeAtEnd().getChildren()
       );
       children = new SyntacticTree[] { nameTree, treepathToUrl.getTreeAtEnd() } ;
     }
 
     final SyntacticTree externalLinkTree = new SimpleTree(
         NodeKind._EXTERNAL_LINK.name(),
         children
     ) ;
     return TreepathTools.replaceTreepathEnd( treepathToUrl, externalLinkTree ) ;
   }
 
   private static NodeKind[] kinds( NodeKind... nodeKinds ) {
     return nodeKinds ;
   }
 
   private static State evaluate(
       SyntacticTree tree,
       NodeKind nodeKind,
       State positive
   ) {
     return evaluate( tree, kinds( nodeKind ), positive, State.INSIDE_PARAGRAPH ) ;
   }
 
   private static State evaluate(
       SyntacticTree tree,
       NodeKind nodeKind,
       State positive,
       State negative
   ) {
     return evaluate( tree, kinds( nodeKind ), positive, negative ) ;
   }
 
   private static State evaluate(
       SyntacticTree tree,
       NodeKind[] nodeKinds,
       State positive,
       State negative
   ) {
     return tree.isOneOf( nodeKinds ) ? positive : negative ;
   }
 
   private enum State {
     OUTSIDE_PARAGRAPH,
     WHITESPACE_OUTSIDE_PARAGRAPH,
     INSIDE_PARAGRAPH_PRECEDED_BY_WHITESPACE,
     INSIDE_PARAGRAPH,
     LINEBREAK_INSIDE_PARAGRAPH,
     INDENTATION_INSIDE_PARAGRAPH,
     DOUBLE_QUOTES,
     WHITESPACE_AFTER_DOUBLE_QUOTES,
     LINE_BREAK_AFTER_DOUBLE_QUOTES,
     URL
   }
   
 }
