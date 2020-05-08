 /*
  * Copyright (c) 2010 Anders Holmbech Brandt
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.holmbech;
 
 import junit.framework.TestCase;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.apache.log4j.SimpleLayout;
 import org.apache.log4j.helpers.CyclicBuffer;
 
 public class PositronAppenderTest extends TestCase {
     private final String logFileName = "test.log";
     private Logger root = Logger.getLogger("Positron");
 
     public void testSimple() {
         final PositronAppender positronAppender = getPositronAppender();
         root.addAppender(positronAppender);
         final CyclicBuffer buffer = positronAppender.getBuffer();
         root.warn("x");
         assertEquals(1, buffer.length());
         root.warn("y");
         assertEquals(2, buffer.length());
         root.error("ERROR OCCURED");
         assertEquals(0, positronAppender.getBuffer().length());
     }
 
     public void testMinimumLevelToAdd() {
         final PositronAppender positronAppender = getPositronAppender();
         positronAppender.setLevelToLogBuffer(Level.ERROR);
         positronAppender.setMinimumLevelToAdd(Level.WARN);
         positronAppender.activateOptions();
         root.addAppender(positronAppender);
         root.debug("x 1");
         assertEquals(0, positronAppender.getBuffer().length());
         root.warn("x 1");
         assertEquals(1, positronAppender.getBuffer().length());
         root.error("x 1");
         assertEquals(0, positronAppender.getBuffer().length());
     }
 
     public void testLevelToLogBuffer() {
         final PositronAppender positronAppender = getPositronAppender();
         positronAppender.setLevelToLogBuffer(Level.DEBUG);
         positronAppender.activateOptions();
         root.addAppender(positronAppender);
         root.warn("x 1");
         assertEquals(0, positronAppender.getBuffer().length());
     }
 
    public void testCycling() {
         final PositronAppender positronAppender = getPositronAppender();
         positronAppender.setBufferSize(3);
         positronAppender.activateOptions();
 
 
     }
 
     private PositronAppender getPositronAppender() {
         final PositronAppender positronAppender = new PositronAppender();
         positronAppender.setFile(logFileName);
         positronAppender.setLayout(new SimpleLayout());
         positronAppender.activateOptions();
         return positronAppender;
     }
 }
