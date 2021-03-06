 /*
  * Copyright 2009 Google Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package com.google.gwt.uibinder;
 
import com.google.gwt.uibinder.parsers.FieldReferenceConverterTest;
import com.google.gwt.uibinder.parsers.SimpleAttributeParserTest;
import com.google.gwt.uibinder.parsers.StrictAttributeParserTest;
import com.google.gwt.uibinder.parsers.StringAttributeParserTest;
 import com.google.gwt.uibinder.rebind.GwtResourceEntityResolverTest;
 import com.google.gwt.uibinder.rebind.HandlerEvaluatorTest;
 import com.google.gwt.uibinder.rebind.TokenatorTest;
 import com.google.gwt.uibinder.rebind.XMLElementTest;
 import com.google.gwt.uibinder.rebind.model.OwnerClassTest;
 import com.google.gwt.uibinder.rebind.model.OwnerFieldClassTest;
 import com.google.gwt.uibinder.rebind.model.OwnerFieldTest;
 
 import junit.framework.Test;
 import junit.framework.TestSuite;
 
 /**
  * Suite of UiBinder tests that require the JRE
  */
 public class UiBinderJreSuite {
   public static Test suite() {
     TestSuite suite = new TestSuite("UiBinder tests that require the JRE");
 
     // rebind
     suite.addTestSuite(GwtResourceEntityResolverTest.class);
     suite.addTestSuite(HandlerEvaluatorTest.class);
     suite.addTestSuite(TokenatorTest.class);
     suite.addTestSuite(XMLElementTest.class);
 
     // model
     suite.addTestSuite(OwnerClassTest.class);
     suite.addTestSuite(OwnerFieldClassTest.class);
     suite.addTestSuite(OwnerFieldTest.class);
 
    // parsers
    suite.addTestSuite(FieldReferenceConverterTest.class);
    suite.addTestSuite(SimpleAttributeParserTest.class);
    suite.addTestSuite(StrictAttributeParserTest.class);
    suite.addTestSuite(StringAttributeParserTest.class);

     return suite;
   }
 
   private UiBinderJreSuite() {
   }
 }
