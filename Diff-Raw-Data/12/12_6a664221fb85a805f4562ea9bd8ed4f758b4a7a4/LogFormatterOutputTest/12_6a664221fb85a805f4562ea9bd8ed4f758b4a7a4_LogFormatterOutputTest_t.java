 /*
  * $Id: LogFormatterOutputTest.java 1610 2010-03-11 08:19:11Z amandel $
  *
  * Copyright 2006, The jCoderZ.org Project. All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are
  * met:
  *
  *    * Redistributions of source code must retain the above copyright
  *      notice, this list of conditions and the following disclaimer.
  *    * Redistributions in binary form must reproduce the above
  *      copyright notice, this list of conditions and the following
  *      disclaimer in the documentation and/or other materials
  *      provided with the distribution.
  *    * Neither the name of the jCoderZ.org Project nor the names of
  *      its contributors may be used to endorse or promote products
  *      derived from this software without specific prior written
  *      permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
  * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS
  * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
  * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
  * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
  * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package org.jcoderz.commons;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import junit.framework.TestCase;
 
 import org.jcoderz.commons.test.TssLogMessage;
 import org.jcoderz.commons.types.Date;
 import org.xml.sax.SAXException;
 
 
 
 /**
  * JUnit test to demonstrate the different outputs of
  * the {@link org.jcoderz.commons.LogFormatter}.
  *
  * @author Michael Griffel
  */
 public class LogFormatterOutputTest
       extends TestCase
 {
    private static final String CLASSNAME
          = LogFormatterOutputTest.class.getName();
    private static final Logger logger = Logger.getLogger(CLASSNAME);
 
    /**
     * Produces a log message from an exception w/ two parameters.
     */
    public void testLogEvent ()
    {
      TssLogMessage.ImpliedParametersLog3.log("foo");
    }
 
    /**
     * Produces a log message from an exception w/ two parameters.
     */
    public void testLogEventWithImpliedParams ()
    {
       TssLogMessage.ImpliedParametersLog.log(new RuntimeException("Foo!"));
    }
 
    /**
     * Produces a log message from an exception w/ two parameters and exception
     * as cause.
     */
    public void testLogException ()
    {
      TssLogMessage.ImpliedParametersLog3.log("foo",
             new Exception("This is the top level exception"));
    }
 
    /**
     * Produces a log message from a standard log record with a Throwable.
     *
     */
    public void testLogThrowable ()
    {
       final Throwable th = new Exception (
             "This is a top level test exception");
       logger.logp(Level.SEVERE, CLASSNAME, "logException", "Logging exception",
             th);
    }
 
    /**
     * Produces a log message from a standard log record with a nested Throwable.
     */
    public void testLogNestedThrowable ()
    {
       final Throwable th = new Exception (
             "This is a top level test exception");
       th.initCause(new Throwable("This is a nestedException"));
       logger.logp(Level.SEVERE, CLASSNAME, "logException", "Logging exception",
             th);
    }
 
    /** Test singel nested exception. */
    public void testServerExceptionLogWithNestedNullPointer ()
    {
       new InternalErrorException(
             "dump stack trace w/ nested NullPointerException",
             new NullPointerException()).log();
    }
    /** Test singel nested exception. */
    public void testServerExceptionLogWithNestedInternalErrorAndNullPointer ()
    {
       final NullPointerException e = new NullPointerException("root");
       final InternalErrorException x = new InternalErrorException("middle", e);
       System.err.println("---------------------------------------------------");
       new InternalErrorException(
             "dump stack trace w/ nested InternalErrorException and "
                + "NullPointerException",
             x).log();
       System.err.println("---------------------------------------------------");
    }
 
    /** Test deep nested exception. */
    public void testDeepNesting ()
    {
       final InternalErrorException inner
             = new InternalErrorException("inner");
       inner.addParameter("INNER-PARAMETER", "value");
       final InternalErrorException middle
             = new InternalErrorException("middle", inner);
       middle.addParameter("MIDDLE-PARAMETER", "this is just a value");
       final Exception middleDefault
             = new NullPointerException("middle-nullpointer");
       middleDefault.initCause(middle);
       final Loggable outer
             = new InternalErrorException("outer", middleDefault);
       outer.addParameter("OUTER-PARAMETER", "this is just an other value");
       outer.log();
    }
 
    /** Test nested exceptions. */
    public void testNesting ()
    {
       try
       {
          a();
       }
       catch (HighLevelException e)
       {
          new InternalErrorException("Test nesting.", e).log();
       }
    }
 
    /** Test deep nested with SAX exceptions exception. */
    public void testNestingWithSax ()
    {
       final InternalErrorException inner
             = new InternalErrorException("inner");
       inner.addParameter("INNER-PARAMETER", "value");
       final InternalErrorException middle
             = new InternalErrorException("middle", inner);
       middle.addParameter("MIDDLE-PARAMETER", "this is just a value");
       final Exception middleDefault
             = new SAXException("SAX Exception", middle);
       final Loggable outer
             = new InternalErrorException("outer", middleDefault);
       outer.addParameter("OUTER-PARAMETER", "this is just an other value");
       outer.log();
    }
 
    static void a ()
          throws HighLevelException
    {
       try
       {
          b();
       }
       catch (MidLevelException e)
       {
          throw new HighLevelException(e);
       }
    }
 
    static void b ()
          throws MidLevelException
    {
       c();
    }
 
 
    static void c ()
          throws MidLevelException
    {
       try
       {
          d();
       }
       catch (LowLevelException e)
       {
          throw new MidLevelException(e);
       }
    }
 
    static void d ()
          throws LowLevelException
    {
       e();
    }
 
    static void e ()
          throws LowLevelException
    {
       throw new LowLevelException();
    }
 
    static final class HighLevelException
          extends Exception
    {
       private static final long serialVersionUID = 1L;
 
       HighLevelException (Throwable cause)
       {
          super(cause);
       }
    }
 
    static final class MidLevelException
          extends Exception
    {
       private static final long serialVersionUID = 1L;
 
       MidLevelException (Throwable cause)
       {
          super(cause);
       }
    }
 
    static final class LowLevelException
          extends Exception
    {
       private static final long serialVersionUID = 1L;
    }
 
 }
