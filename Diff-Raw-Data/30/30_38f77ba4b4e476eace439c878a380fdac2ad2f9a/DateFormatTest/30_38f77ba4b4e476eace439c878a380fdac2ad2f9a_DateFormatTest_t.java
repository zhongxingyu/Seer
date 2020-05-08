/**
 *     Copyright (C) 2012 Johan Grufman
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
 package org.convx.format;
 
 import org.junit.Test;
 
 import static org.junit.Assert.assertEquals;
 
 /**
  * @author johan
  * @since 2012-04-05
  */
 public class DateFormatTest {
 
 
     @Test
     public void testParsing() throws Exception {
         DateFormat dateFormat = new DateFormat("dd/MM/yyyy");
         assertEquals("1990-02-01", dateFormat.parse("01/02/1990"));
     }
 
     @Test
     public void testWriting() throws Exception {
         DateFormat dateFormat = new DateFormat("dd/MM/yyyy");
         assertEquals("01/02/1990", dateFormat.write("1990-02-01"));
     }
 }
