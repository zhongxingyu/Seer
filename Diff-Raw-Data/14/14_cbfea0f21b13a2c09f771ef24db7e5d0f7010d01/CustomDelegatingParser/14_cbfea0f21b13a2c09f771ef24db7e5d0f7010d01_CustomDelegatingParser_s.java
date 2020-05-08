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
 
import java.lang.reflect.Method;
 import java.lang.reflect.InvocationTargetException;

import org.antlr.runtime.RecognitionException;
import org.junit.Assert;
 
 import novelang.common.LanguageTools;
 import novelang.common.ReflectionTools;
 
 /**
  * Calls a pluggable method of ANTLR-generated parser.
  *
  * @author Laurent Caillette
 */
 /*package*/ class CustomDelegatingParser extends AbstractDelegatingParser {
 
   private final Method parserMethod ;
 
   public CustomDelegatingParser( final Method parserMethod, final String text ) {
     super( text, new GrammarDelegate( TreeFixture.LOCATION_FACTORY ) ) ;
    Assert.assertEquals( NovelangParser.class, parserMethod.getDeclaringClass() ) ;
     this.parserMethod = parserMethod ;
   }
 
   @Override
   public Object callParserMethod() throws RecognitionException {
 
     final Object parsingResult ;
 
     try {
       parsingResult = parserMethod.invoke( antlrParser ) ;
     } catch( IllegalAccessException e ) {
       throw new RuntimeException( e ) ;
     } catch( InvocationTargetException e ) {
       if( e.getCause() instanceof RecognitionException ) {
         throw ( RecognitionException ) e.getCause() ;
       } else {
         LanguageTools.rethrowUnchecked( e ) ;
         throw new Error( "Compiler happy" ) ;
       } 
     }
 
     final Method getTreeMethod = ReflectionTools.getMethod( parsingResult.getClass(), "getTree" ) ;
     return ReflectionTools.invoke( getTreeMethod, parsingResult ) ;
   }
 }
