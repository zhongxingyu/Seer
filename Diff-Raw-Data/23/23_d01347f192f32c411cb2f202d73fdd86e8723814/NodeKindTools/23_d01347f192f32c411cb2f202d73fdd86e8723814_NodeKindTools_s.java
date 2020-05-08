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
 package novelang.parser;
 
 import java.util.List;
 import java.util.Set;
 
 import com.google.common.base.Function;
 import com.google.common.base.Predicate;
 import com.google.common.collect.Collections2;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Ordering;
 import com.google.common.collect.Sets;
 import novelang.common.SyntacticTree;
 
 /**
  * As {@link NodeKind} is generated, all its utility methods should go there.
  *
  * @author Laurent Caillette
  */
 public class NodeKindTools {
 
   private NodeKindTools() {
     throw new Error() ;
   }
 
   public static NodeKind ofRoot( final SyntacticTree tree ) {
     final NodeKind nodeKind = tree.getNodeKind() ;
     if( nodeKind == null ) {
       throw new IllegalStateException( "No nodeKind" ) ;
     }
     return nodeKind;
   }
 
   public static String tokenNameAsXmlElementName( final String tokenName ) {
     String result = tokenName.toLowerCase().replace( "_", "-" ) ;
     if( result.startsWith( "-" ) ) {
       result = result.substring( 1 ) ;
     }
     return result ;
   }
 
   private static final Function< String,String > TOKEN_NAME_AS_XML_ELEMENT_NAME =
       new Function< String, String >() {
         public String apply( final String nodeName ) {
           return tokenNameAsXmlElementName( nodeName ) ;
         }
       }
   ;
 
   private static final Predicate< String > NO_TRAILING_UNDERSCORE =
       new Predicate< String >() {
         public boolean apply( final String s ) {
           return ! s.endsWith( "_" ) ;
         }
       }
   ;
 
   public static Set< String > getRenderingNames() {
     final List< String > filteredNames = Lists.newArrayList(
         Collections2.filter( NodeKind.getNames(), NO_TRAILING_UNDERSCORE ) ) ;
     final List< String > sortedNames = Ordering.natural().sortedCopy( filteredNames ) ;
     return Sets.newTreeSet( Lists.transform( sortedNames, TOKEN_NAME_AS_XML_ELEMENT_NAME ) ) ;
   }
 }
