 /* 
  * surveyforge-core - Copyright (C) 2006 OPEN input - http://www.openinput.com/
  *
  * This program is free software; you can redistribute it and/or modify it 
  * under the terms of the GNU General Public License as published by the 
  * Free Software Foundation; either version 2 of the License, or (at your 
  * option) any later version.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT 
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
  * FITNESS FOR A PARTICULAR PURPOSE. 
  * See the GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License along 
  * with this program; if not, write to 
  *   the Free Software Foundation, Inc., 
  *   59 Temple Place, Suite 330, 
  *   Boston, MA 02111-1307 USA
  *   
  * $Id$
  */
 package org.surveyforge.core.survey;
 
 import java.lang.reflect.Method;
 
 import org.testng.Assert;
 import org.testng.annotations.DataProvider;
 import org.testng.annotations.ExpectedExceptions;
 import org.testng.annotations.Test;
 import org.surveyforge.core.metadata.ConceptualDataElement;
 import org.surveyforge.core.metadata.LogicalValueDomain;
 import org.surveyforge.core.metadata.Register;
 import org.surveyforge.core.metadata.RegisterDataElement;
 import org.surveyforge.core.metadata.ValueDomain;
 
 /**
  * @author jsegura
  */
 public class QuestionnaireElementTest
   {
 
   @DataProvider(name = "dp")
   public Object[][] createData( Method m )
     {
     Register register = new Register( "register" );
     ConceptualDataElement conceptualDataElement = new ConceptualDataElement( new LogicalValueDomain( ), "conceptualDataElement" );
    RegisterDataElement registerDataElement = new RegisterDataElement( conceptualDataElement, new ValueDomain( ),
        "registerDataElement" );
     return new Object[][] {new Object[] {registerDataElement}};
     }
 
 
   @Test(dataProvider = "dp")
   @ExpectedExceptions( {NullPointerException.class})
   public void questionnaireElementCreationWithNullIdentifier( RegisterDataElement registerDataElement )
     {
     new QuestionnaireElement( registerDataElement, null );
     }
 
   @Test(dataProvider = "dp")
   @ExpectedExceptions( {NullPointerException.class})
   public void questionnaireElementCreationWithEmptyIdentifier( RegisterDataElement registerDataElement )
     {
     new QuestionnaireElement( registerDataElement, "" );
     }
 
 
   @Test(dataProvider = "dp")
   public void questionnaireElementCreation( RegisterDataElement registerDataElement )
     {
     new QuestionnaireElement( registerDataElement, "id" );
     }
 
   @Test(dataProvider = "dp")
   public void questionnaireElementIdentifier( RegisterDataElement registerDataElement )
     {
     String id = "id";
     QuestionnaireElement object = new QuestionnaireElement( registerDataElement, id );
     Assert.assertEquals( id, object.getIdentifier( ) );
     }
 
 
   @Test(dataProvider = "dp")
   public void questionnaireElementQuestion( RegisterDataElement registerDataElement )
     {
     String id = "id";
     QuestionnaireElement object = new QuestionnaireElement( registerDataElement, id );
     Question question = new Question( "question" );
     object.setQuestion( question );
     Assert.assertEquals( question, object.getQuestion( ) );
     }
 
 
   }
