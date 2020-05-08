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
 
 package novelang.parser.antlr;
 
 import org.antlr.runtime.RecognitionException;
 //import org.antlr.runtime.tree.CommonErrorNode;
 import novelang.common.LocationFactory;
 import novelang.common.SyntacticTree;
 import novelang.parser.PartParser;
 
 /**
  * @author Laurent Caillette
 */
 class DelegatingPartParser
     extends AbstractDelegatingParser
     implements PartParser
 {
   public DelegatingPartParser( String text, LocationFactory locationFactory ) {
     super( text, new GrammarDelegate( locationFactory ) ) ;
   }
 
   public SyntacticTree parse() throws RecognitionException {
     final Object tree = getAntlrParser().part().getTree();
//    if( tree instanceof CommonErrorNode ) {
//      throw new RuntimeException( ( ( CommonErrorNode ) tree ).trappedException ) ;
//    } else {
       return ( SyntacticTree ) tree ;
//    }
   }
   
 }
