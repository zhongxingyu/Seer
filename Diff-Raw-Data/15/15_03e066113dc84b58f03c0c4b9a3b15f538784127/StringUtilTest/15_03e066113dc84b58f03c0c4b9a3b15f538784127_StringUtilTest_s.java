 /*
  * Copyright 2010-2011 Heads Up Development Ltd.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.headsupdev.support.java;
 
 import junit.framework.TestCase;
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * Tests for the StringUtil class
  *
  * @author Andrew Williams
  * @since 1.1
  */
 public class StringUtilTest
     extends TestCase
 {
     public void testSimpleReplacement()
     {
         String format = "Hello ${planet}";
         Map<String, Object> parameters = new HashMap<String, Object>();
         parameters.put( "planet", "world" );
         
         String out = StringUtil.format( format, parameters );
         assertEquals( "Hello world", out );
     }
 
     public void testCustomReplacement()
     {
         String format = "Hello <planet>";
         Map<String, Object> parameters = new HashMap<String, Object>();
         parameters.put( "planet", "world" );
 
         String out = StringUtil.format( format, "<", ">", parameters );
         assertEquals( "Hello world", out );
     }
 
     public void testNumberReplacement()
     {
         String format = "Hello ${number}";
         Map<String, Object> parameters = new HashMap<String, Object>();
         parameters.put( "number", 45 );
 
         String out = StringUtil.format( format, parameters );
         assertEquals( "Hello 45", out );
     }
 
     public void testMissing()
     {
         String format = "Hello ${planet}";
         Map<String, Object> parameters = new HashMap<String, Object>();
         parameters.put( "wrong", "world" );
 
         String out = StringUtil.format( format, parameters );
         assertEquals( "Hello ${planet}", out );
     }
 }
