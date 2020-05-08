 /*
  * Copyright (c) 2010 David Kellum
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you
  * may not use this file except in compliance with the License.  You may
  * obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
  * implied.  See the License for the specific language governing
  * permissions and limitations under the License.
  */
 
 package iudex.html.tree;
 
 import java.util.List;
 
 import iudex.html.tree.TreeFilter.Action;
 import static iudex.html.tree.TreeFilter.Action.*;
 
 import com.gravitext.xml.tree.Element;
 import com.gravitext.xml.tree.Node;
 
 public class TreeWalker
 {
    public static Action walkDepthFirst( final TreeFilter filter,
                                         final Node node )
     {
         Element element = node.asElement();
         if( element != null ) {
 
             final List<Node> children = element.children();
             for( int i = 0; i < children.size(); ) {
                 Node child = children.get( i );
                 switch( walkDepthFirst( filter, child ) ) {
                 case FOLD:
                     i = fold( element, child, i );
                     // New children already walked, skip them.
                     break;
                 case DROP:
                     child.detach();
                     break;
                 case TERMINATE:
                     return TERMINATE;
                 default:
                     ++i;
                 }
             }
         }
 
         return filter.filter( node );
     }
 
    public static Action walkBreadthFirst( final TreeFilter filter,
                                           final Node node )
     {
         Action action = filter.filter( node );
 
         if( action == CONTINUE || action == CHAIN_END ) {
             Element element = node.asElement();
             if( element != null ) {
                 final List<Node> children = element.children();
                for( int i = 0; i < children.size(); ) {
                     Node child = children.get( i );
                     switch( walkBreadthFirst( filter, child ) ) {
                     case FOLD:
                         fold( element, child, i );
                         // The new children still need to be walked.
                         // Don't skip them.
                         break;
                     case DROP:
                         child.detach();
                         break;
                     case TERMINATE:
                        return TERMINATE;
                     default:
                         ++i;
                     }
                 }
             }
         }
         return action;
     }
 
     private static int fold( final Element parent,
                              final Node child,
                              int childIndex )
     {
        Element celm = child.asElement();
 
        if( celm != null ) {
            final List<Node> gchildren = celm.children();
            while( gchildren.size() > 0) {
                parent.insertChild( ++childIndex, gchildren.get( 0 ) );
            }
        }
        child.detach();
 
        return childIndex;
     }
 
 }
