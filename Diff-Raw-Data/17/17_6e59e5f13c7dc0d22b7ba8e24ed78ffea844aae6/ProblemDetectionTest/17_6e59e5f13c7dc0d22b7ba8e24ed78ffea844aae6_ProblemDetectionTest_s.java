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
  */package novelang.parser.antlr;
 
 import org.antlr.runtime.RecognitionException;
 import org.junit.Assert;
 import org.junit.Test;
 import org.junit.Ignore;
 import novelang.common.Problem;
 import com.google.common.collect.Iterables;
 
 /**
  * Check that problems are correctly reported.
  */
 public class ProblemDetectionTest {
 
   /**
    * TODO detect two consecutive full stops in the grammar.
    */
   @Test @Ignore
   public void incompleteEllipsis() throws RecognitionException {
     final DelegatingPartParser parser = AntlrTestHelper.createPartParser( "..w ww" ) ;
     parser.parse() ;
     Assert.assertTrue( parser.hasProblem() ) ;
   }
 
   @Test
   public void unknownCharacter() throws RecognitionException {
     final DelegatingPartParser parser = AntlrTestHelper.createPartParser( "\u0001" ) ;
     parser.parse() ;
     Assert.assertTrue( parser.hasProblem() ) ;
   }
 
   @Test
  public void lineNumberForMissingClosingHyphenPair() throws RecognitionException {
    final DelegatingPartParser parser = AntlrTestHelper.createPartParser( "\n\n\n-- w" ) ;
     parser.parse() ;
    Assert.assertTrue( parser.hasProblem() ) ;
    final Problem problem = Iterables.getOnlyElement( parser.getProblems() ) ;
    Assert.assertEquals(
        "Problem reported as '" + problem.getMessage() + "'",
        4,
        problem.getLocation().getLine()
    ) ;
   }
 

 }
