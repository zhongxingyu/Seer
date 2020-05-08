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
 
 import novelang.common.ReflectionTools;
 import novelang.common.SyntacticTree;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 
 import org.junit.Assert;
 import org.antlr.runtime.tree.CommonErrorNode;
 
 
 /**
  * Enumeration-based approach for calling parser methods.
  * 
  * @author Laurent Caillette
  */
 public class ParserMethod {
 
   protected ParserMethod( String methodName ) {
     this.method = ReflectionTools.getMethod( NovelangParser.class, methodName ) ;
   }
 
   private final Method method ;
 
   /**
    * Returns the tree object contained by parser's result object.
    */
   public SyntacticTree getTree( NovelangParser parser ) {
     final Object antlrResult ;
     try {
       antlrResult = method.invoke( parser ) ;
     } catch ( IllegalAccessException e ) {
       throw new RuntimeException( e ) ;
     } catch ( InvocationTargetException e ) {
       throw new RuntimeException( e ) ;
     }
     
    if( antlrResult instanceof CommonErrorNode ) {
      final CommonErrorNode errorNode = ( CommonErrorNode ) antlrResult ;
      throw new RuntimeException( errorNode.trappedException ) ;
    }

     final Method getTreeMethod = ReflectionTools.getMethod(
         antlrResult.getClass(),
         "getTree"
     ) ;
 
     
    final SyntacticTree tree = ReflectionTools.invoke( getTreeMethod, antlrResult ) ;    
    return tree ;
   }
   
   public SyntacticTree createTree( String text ) {
     final DelegatingPartParser parser = AntlrTestHelper.createPartParser( text ) ;
     final SyntacticTree tree = getTree( parser.getAntlrParser() ) ;
     AntlrTestHelper.checkSanity( parser ) ;
     return tree ;    
   }
   
   public void checkTree( String text, SyntacticTree expectedTree ) {
     final SyntacticTree actualTree = createTree( text ) ;
     TreeFixture.assertEquals( expectedTree, actualTree ) ;
     
   }
   
   public void checkFails( String text ) {
     final DelegatingPartParser parser = AntlrTestHelper.createPartParser( text ) ;
     getTree( parser.getAntlrParser() ) ;
     final String readableProblemList = 
         AntlrTestHelper.createProblemList( parser.getProblems() ) ;
     final boolean parserHasProblem = parser.hasProblem() ;
     Assert.assertTrue( readableProblemList, parserHasProblem ) ;
     
   }
 
 }
