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
 package com.google.jstestdriver;
 
 import junit.framework.TestCase;
 
 /**
  * @author jeremiele@google.com (Jeremie Lenfant-Engelmann)
  */
 public class URLTranslatorTest extends TestCase {
 
   private static final String ORIGINAL_URL = "http://www.google.com";
  private static final String TRANSLATED_URL = "/forward/";
 
   public void testTranslateUrl() throws Exception {
     DefaultURLTranslator urlTranslator = new DefaultURLTranslator();
 
     urlTranslator.translate(ORIGINAL_URL);
     assertEquals(TRANSLATED_URL, urlTranslator.getTranslation(ORIGINAL_URL));
   }
 
   public void testRetrieveOriginal() throws Exception {
     DefaultURLTranslator urlTranslator = new DefaultURLTranslator();
     urlTranslator.translate(ORIGINAL_URL);
 
     assertEquals(ORIGINAL_URL, urlTranslator
         .getOriginal(urlTranslator.getTranslation(ORIGINAL_URL)));
   }
 
   public void testClearUrlTranslatorCache() throws Exception {
     DefaultURLTranslator urlTranslator = new DefaultURLTranslator();
     urlTranslator.translate(ORIGINAL_URL);
 
     urlTranslator.clear();
     assertNull(urlTranslator.getOriginal(urlTranslator.getTranslation(ORIGINAL_URL)));
   }
 }
