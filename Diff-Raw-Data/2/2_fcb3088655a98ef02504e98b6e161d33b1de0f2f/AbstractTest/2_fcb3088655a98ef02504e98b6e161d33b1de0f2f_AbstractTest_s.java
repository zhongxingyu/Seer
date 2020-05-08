 /*
  * Copyright (C) 2008 Herve Quiroz
  *
  * This library is free software; you can redistribute it and/or modify it under
  * the terms of the GNU Lesser General Public License as published by the Free
  * Software Foundation; either version 2.1 of the License, or (at your option)
  * any later version.
  * 
  * This library is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU Lesser General Public License
  * along with this library; if not, write to the Free Software Foundation, Inc.,
  * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
  *
  * $Id$
  */
 package org.trancecode;
 
 import java.io.File;
 
 import org.testng.annotations.BeforeSuite;
 import org.testng.annotations.BeforeTest;
 import org.trancecode.logging.Logger;
 import org.trancecode.logging.simple.SimpleLoggerManager;
 import org.trancecode.logging.spi.LoggerLevel;
 
 /**
  * @author Herve Quiroz
  */
 public abstract class AbstractTest
 {
     public static final String PROPERTY_QUIET = "test.quiet";
     public static final boolean QUIET = Boolean.getBoolean(PROPERTY_QUIET);
 
     protected final Logger log = Logger.getLogger(getClass());
 
     @BeforeSuite
     public static void setupLogging()
     {
         SimpleLoggerManager.setOutputDirectory(new File("target/test-logs"));
         if (!QUIET)
         {
             SimpleLoggerManager.setLevel(LoggerLevel.TRACE);
         }
         else
         {
            SimpleLoggerManager.setLevel(LoggerLevel.TRACE);
         }
     }
 
     @BeforeTest
     public void logTestDelimiter()
     {
         SimpleLoggerManager.setLogFileNamePrefix(getClass().getName());
         log.info("------------------------------------------------------------------------------");
     }
 }
