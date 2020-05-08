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
 
 import novelang.common.SyntacticTree;
 import novelang.common.Problem;
 import org.antlr.runtime.RecognitionException;
 import junit.framework.AssertionFailedError;
 
 /**
  * Various utilities strongly inspired (copied) from {@code AntlrTestHelper}.
  * Copying was useful because it avoids touching original file for extracting utility methods.
  * 
  * @author Laurent Caillette
  */
 public class Antlr311TestHelper {
   
   static void delimitedSpreadBlock( String text, SyntacticTree expectedTree ) throws RecognitionException {
     final SyntacticTree actualTree = delimitedSpreadBlock( text ) ;
     TreeFixture.assertEquals( expectedTree, actualTree ) ;
   }
  
   public static SyntacticTree delimitedSpreadBlock( String text ) throws RecognitionException {
     final DelegatingPartParser parser = createPartParser( text ) ;
    final SyntacticTree tree = ( SyntacticTree ) parser.getAntlrParser().delimitedSpreadblock().getTree() ;
     checkSanity( parser );
     return tree;
   }
 
   
 // =========  
 // Utilities  
 // =========  
   
   public static DelegatingPartParser createPartParser( String text ) {
     return ( DelegatingPartParser )
         new DefaultPartParserFactory().createParser( TreeFixture.LOCATION_FACTORY, text );
   }
   
   public static void checkSanity( AbstractDelegatingParser parser ) {
     if( parser.hasProblem() ) {
       throw new AssertionFailedError(
           "Parser has problems. " + createProblemList( parser.getProblems() ) ) ;
     }
   }
   
   public static final String BREAK = "\n" ;
  
   public static String createProblemList( Iterable<Problem> problems ) {
     final StringBuffer buffer = new StringBuffer( "Problems:" ) ;
     for( final Problem problem : problems ) {
       buffer.append( "\n    " ) ;
       buffer.append( problem.getMessage() ) ;
       buffer.append( "  at  " ) ;
       buffer.append( problem.getLocation() ) ;
     }
     return buffer.toString() ;
   }
   
 }
