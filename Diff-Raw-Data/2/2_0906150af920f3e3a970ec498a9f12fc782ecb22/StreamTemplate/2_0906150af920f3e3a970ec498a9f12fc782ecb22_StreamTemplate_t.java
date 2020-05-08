 
 /*
  * Copyright (c) 1998, 1999 Semiotek Inc. All Rights Reserved.
  *
  * This software is the confidential intellectual property of
  * of Semiotek Inc.; it is copyrighted and licensed, not sold.
  * You may use it under the terms of the GNU General Public License,
  * version 2, as published by the Free Software Foundation. If you 
  * do not want to use the GPL, you may still use the software after
  * purchasing a proprietary developers license from Semiotek Inc.
  *
  * This software is provided "as is", with NO WARRANTY, not even the 
  * implied warranties of fitness to purpose, or merchantability. You
  * assume all risks and liabilities associated with its use.
  *
  * See the attached License.html file for details, or contact us
  * by e-mail at info@semiotek.com to get a copy.
  */
 
 
 package org.webmacro.engine;
 
 import java.util.*;
 import java.io.*;
 import org.webmacro.util.*;
 import org.webmacro.*;
 
 /**
   * StreamTempaltes are constructed with a stream from which they 
   * read their data. They can only read the stream once, and after
   * that will throw an exception. Mostly they are useful for testing
   * WebMacro directives on the command line, since a main() is 
   * provided which reads the template on standard input.
   */
 
 public class StreamTemplate extends WMTemplate
 {
 
    /**
      * Our stream
      */
    private Reader _in;
 
    /**
      * Instantiate a template based on the specified stream
      */
    public StreamTemplate(Broker broker, Reader inStream)
    {
       super(broker);
       _in = inStream;
    }
 
 
    /**
      * Get the stream the template should be read from. Parse will 
      * call this method in order to locate a stream.
      */
    protected Reader getReader() throws IOException {
       if (_in != null) {
          Reader ret = _in;
          _in = null;
          return ret;
       } else { 
          throw new IOException("Already read stream.");
       }
    }
 
    /**
      * Return a name for this template. For example, if the template reads
      * from a file you might want to mention which it is--will be used to
      * produce error messages describing which template had a problem.
      */
    public String toString() {
       return "StreamTemplate: (stream " + _in + ")";
    }
 
    /**
      * Simple test 
      */
    public static void main(String arg[]) 
    {
 
       Log.traceExceptions(true);
       Log.setLevel(Log.ALL);
       Log.setTarget(System.err);
       if (arg.length != 0) {
          System.out.println("Enabling log types");
          Log.enableTypes(arg);
       }
 
       // Build a context
       WebMacro wm = null;
       Context context = null;
 
       try {
          wm = new WM();
         context = wm.getContext();
          Object names[] = { "prop" };
          context.setProperty(names, "Example property");
       } catch (Exception e) {
          e.printStackTrace();
       }
 
       try {
          context.put("helloworld", "Hello World");
          context.put("hello", "Hello");
          context.put("file", "include.txt");
          TestObject[] fruits = { new TestObject("apple",false),
                           new TestObject("lemon",true),
                           new TestObject("pear",false),
                           new TestObject("orange",true),
                           new TestObject("watermelon",false),
                           new TestObject("peach",false),
                           new TestObject("lime",true) };
 
          SelectList sl = new SelectList(fruits, 3);
          context.put("sl-fruits", sl);
 
          context.put("fruits", fruits);
          context.put("flipper", new TestObject("flip",false));
 
          System.out.println("- - - - - - - - - - - - - - - - - - - -");
          System.out.println("Context contains: helloWorld, hello, file, TestObject[] fruits, SelectList sl(fruits, 3), TestObject flipper"); 
          System.out.println("- - - - - - - - - - - - - - - - - - - -");
 
          Template t1 = new StreamTemplate(wm.getBroker(), 
                new InputStreamReader(System.in));
          t1.parse();
 
          Writer w = new OutputStreamWriter(System.out);
 
          System.out.println("*** RESULT ***");
          t1.write(w,context);
          w.close();
          System.out.println("*** DONE ***");
          //System.out.println(result);
         
       } catch (Exception e) {
          e.printStackTrace();
       }
 
    }
 
 }
