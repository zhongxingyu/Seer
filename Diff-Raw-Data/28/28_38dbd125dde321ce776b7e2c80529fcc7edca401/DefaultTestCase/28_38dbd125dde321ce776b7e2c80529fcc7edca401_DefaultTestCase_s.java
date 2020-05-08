 package com.github.croesch;
 
 import static org.fest.assertions.Assertions.assertThat;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.PrintStream;
 import java.io.Reader;
 import java.util.Locale;
 
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Ignore;
 
 import com.github.croesch.i18n.Text;
 import com.github.croesch.mic1.io.Output;
 import com.github.croesch.misc.Printer;
 import com.github.croesch.misc.Utils;
 
 /*
  * Copyright (C) 2011-2012  Christian Roesch
  * 
  * This file is part of micro-debug.
  * 
  * micro-debug is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * micro-debug is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with micro-debug.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 /**
  * Default test case to be extended by all test classes.
  * 
  * @author croesch
  * @since Date: Jan 22, 2012
  */
 @Ignore("Just default case")
 public class DefaultTestCase {
 
   protected static final ByteArrayOutputStream out = new ByteArrayOutputStream();
 
   protected static final ByteArrayOutputStream micOut = new ByteArrayOutputStream();
 
   private static String helpFileText = null;
 
   @BeforeClass
   public static void setUpBeforeClass() throws Exception {
     Locale.setDefault(new Locale("test", "tst", " "));
   }
 
   @Before
   public final void setUp() throws Exception {
     Printer.setPrintStream(new PrintStream(out));
     Output.setOut(new PrintStream(micOut));
     Output.setBuffered(true);
     micOut.reset();
     out.reset();
     setUpDetails();
   }
 
   @AfterClass
   public static final void after() throws Exception {
     Printer.setPrintStream(System.out);
     Output.flush();
   }
 
   protected void setUpDetails() throws Exception {
     // let that be defined by subclasses
   }
 
   protected final StringBuilder readFile(final String name) throws IOException {
     final StringBuilder sb = new StringBuilder();
     final InputStream stream = ClassLoader.getSystemResourceAsStream(name);
     try {
       final Reader r = new InputStreamReader(stream);
       int c;
       while ((c = r.read()) != -1) {
        sb.append((char) c);
       }
     } finally {
       stream.close();
     }
     return sb;
   }
 
   protected synchronized final String getHelpFileText() throws IOException {
     if (helpFileText == null) {
       helpFileText = readFile("help.txt").toString();
     }
     return helpFileText;
   }
 
   protected final void printMethodName() {
     printMethodName(1);
   }
 
   protected final void printlnMethodName() {
     printMethodName(1);
     System.out.println();
   }
 
   protected final void printlnMethodName(final int lvl) {
     printMethodName(1 + lvl);
     System.out.println();
   }
 
   protected final void printMethodName(final int lvl) {
     System.out.print(Thread.currentThread().getStackTrace()[2 + lvl].getClassName() + "#");
     System.out.print(Thread.currentThread().getStackTrace()[2 + lvl].getMethodName() + " ");
   }
 
   protected final void printLoopEnd() {
     System.out.print(" ");
   }
 
   protected final void printStep() {
     System.out.print(".");
   }
 
   protected final void printEndOfMethod() {
     System.out.println();
   }
 
   protected final String getLineSeparator() {
     return Utils.getLineSeparator();
   }
 
   protected void assertTicksDoneAndResetPrintStream(final int ticks) {
     assertThat(out.toString()).isEqualTo(Text.TICKS.text(ticks) + getLineSeparator());
     out.reset();
   }
 }
