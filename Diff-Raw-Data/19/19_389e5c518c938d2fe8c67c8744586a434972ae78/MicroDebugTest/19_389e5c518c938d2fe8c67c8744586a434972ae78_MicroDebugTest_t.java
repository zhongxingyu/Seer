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
 package com.github.croesch.micro_debug;
 
 import static org.fest.assertions.Assertions.assertThat;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintStream;
 import java.io.StringReader;
 
 import org.junit.Test;
 
 import com.github.croesch.micro_debug.commons.Reader;
 import com.github.croesch.micro_debug.i18n.Text;
 import com.github.croesch.micro_debug.mic1.io.Output;
 
 /**
  * Contains tests for {@link MicroDebug}.
  * 
  * @author croesch
  * @since Date: Dec 2, 2011
  */
 public class MicroDebugTest extends DefaultTestCase {
 
   private final String GREETING = Text.GREETING.text() + getLineSeparator();
 
   private final String BORDER = Text.BORDER.text() + getLineSeparator();
 
   private final String WELCOME = Text.WELCOME.text() + getLineSeparator() + this.BORDER;
 
   @Test
   public final void testMain_FileNotFound() {
     printlnMethodName();
     MicroDebug.main(new String[] { "asd", "bas" });
 
     assertThat(out.toString()).isEqualTo(this.GREETING + this.WELCOME
                                                  + Text.ERROR.text(Text.FILE_NOT_FOUND.text("asd"))
                                                  + getLineSeparator()
                                                  + Text.ERROR.text(Text.FILE_NOT_FOUND.text("bas"))
                                                  + getLineSeparator());
   }
 
   @Test
   public final void testMain_FileNotFound1() {
     printlnMethodName();
     MicroDebug.main(new String[] { "asd", "src/test/resources/mic1/hi.mic1" });
 
     assertThat(out.toString()).isEqualTo(this.GREETING + this.WELCOME
                                                  + Text.ERROR.text(Text.FILE_NOT_FOUND.text("asd"))
                                                  + getLineSeparator());
   }
 
   @Test
   public final void testMain_FileNotFound2() {
     printlnMethodName();
     MicroDebug.main(new String[] { "src/test/resources/mic1/hi.mic1", "asd" });
 
     assertThat(out.toString()).isEqualTo(this.GREETING + this.WELCOME
                                                  + Text.ERROR.text(Text.FILE_NOT_FOUND.text("asd"))
                                                  + getLineSeparator());
   }
 
   @Test
   public final void testMain_WrongFormat1() {
     printlnMethodName();
     MicroDebug.main(new String[] { "src/test/resources/mic1/hi.mic1", "src/test/resources/mic1/hi.mic1" });
 
     assertThat(out.toString()).isEqualTo(this.GREETING + this.WELCOME + Text.ERROR.text(Text.WRONG_FORMAT_IJVM.text())
                                                  + getLineSeparator());
   }
 
   @Test
   public final void testMain_WrongFormat2() {
     printlnMethodName();
     MicroDebug.main(new String[] { "src/test/resources/mic1/hi.ijvm", "src/test/resources/mic1/hi.ijvm" });
 
     assertThat(out.toString()).isEqualTo(this.GREETING + this.WELCOME + Text.ERROR.text(Text.WRONG_FORMAT_MIC1.text())
                                                  + getLineSeparator());
   }
 
   @Test
   public final void testMain_TooFewArgs() {
     printlnMethodName();
     MicroDebug.main(new String[] { "-u" });
     assertThat(out.toString()).isEqualTo(this.GREETING + this.BORDER + Text.ERROR.text(Text.MISSING_IJVM_FILE)
                                                  + getLineSeparator() + Text.TRY_HELP + getLineSeparator());
 
     out.reset();
 
     MicroDebug.main(new String[] { "u" });
     assertThat(out.toString()).isEqualTo(this.GREETING + this.BORDER + Text.ERROR.text(Text.MISSING_IJVM_FILE)
                                                  + getLineSeparator() + Text.TRY_HELP + getLineSeparator());
 
     out.reset();
 
     MicroDebug.main(new String[] {});
     assertThat(out.toString()).isEqualTo(this.GREETING + this.BORDER + Text.ERROR.text(Text.MISSING_MIC1_FILE)
                                                  + getLineSeparator() + Text.ERROR.text(Text.MISSING_IJVM_FILE)
                                                  + getLineSeparator() + Text.TRY_HELP + getLineSeparator());
 
     out.reset();
 
     MicroDebug.main(null);
     assertThat(out.toString()).isEqualTo(this.GREETING + this.BORDER + Text.ERROR.text(Text.MISSING_MIC1_FILE)
                                                  + getLineSeparator() + Text.ERROR.text(Text.MISSING_IJVM_FILE)
                                                  + getLineSeparator() + Text.TRY_HELP + getLineSeparator());
   }
 
   @Test
   public final void testMain_Version() {
     printlnMethodName();
     MicroDebug.main(new String[] { "-v" });
     assertThat(out.toString()).isEqualTo(this.GREETING + this.BORDER + Text.VERSION.text() + getLineSeparator());
 
     out.reset();
 
     MicroDebug.main(new String[] { "--version" });
     assertThat(out.toString()).isEqualTo(this.GREETING + this.BORDER + Text.VERSION.text() + getLineSeparator());
 
     out.reset();
 
     MicroDebug.main(new String[] { "--version", "mic1", "ijvm" });
     assertThat(out.toString()).isEqualTo(this.GREETING + this.WELCOME + Text.VERSION.text() + getLineSeparator());
 
     out.reset();
 
     MicroDebug.main(new String[] { "--version", "mic1" });
     assertThat(out.toString()).isEqualTo(this.GREETING + this.WELCOME
                                                  + Text.ERROR.text(Text.FILE_NOT_FOUND.text("--version"))
                                                  + getLineSeparator()
                                                  + Text.ERROR.text(Text.FILE_NOT_FOUND.text("mic1"))
                                                  + getLineSeparator());
   }
 
   @Test
   public final void testMain_Unknown() throws IOException {
     printlnMethodName();
     MicroDebug.main(new String[] { "-xxx", "", "" });
     assertThat(out.toString()).isEqualTo(this.GREETING + this.WELCOME
                                                  + Text.ERROR.text(Text.UNKNOWN_ARGUMENT.text("-xxx"))
                                                  + getLineSeparator() + getHelpFileText());
 
     out.reset();
 
     MicroDebug.main(new String[] { "asd", "efgh", "xy", "as" });
     assertThat(out.toString()).isEqualTo(this.GREETING + this.WELCOME
                                                  + Text.ERROR.text(Text.UNKNOWN_ARGUMENT.text("asd"))
                                                  + getLineSeparator()
                                                  + Text.ERROR.text(Text.UNKNOWN_ARGUMENT.text("efgh"))
                                                  + getLineSeparator() + getHelpFileText());
   }
 
   @Test
   public final void testMain_Hi() {
     printlnMethodName();
     Output.setOut(new PrintStream(out));
     Reader.setReader(new StringReader("run\nexit"));
 
     MicroDebug.main(new String[] { "--unbuffered-output",
                                   "src/test/resources/mic1/hi.mic1",
                                   "src/test/resources/mic1/hi.ijvm" });
     assertThat(out.toString()).isEqualTo(this.GREETING + this.WELCOME + Text.INPUT_DEBUGGER.text() + "Hi!\n"
                                                  + Text.TICKS.text(14) + getLineSeparator()
                                                  + Text.INPUT_DEBUGGER.text());
   }
 
   @Test
   public final void testMain_Hi_OutputFile() throws IOException {
     printlnMethodName();
     Reader.setReader(new StringReader("run" + getLineSeparator() + "exit"));
 
     final String filePath = "tmp-micro-debug-test.del";
     MicroDebug.main(new String[] { "--unbuffered-output",
                                   "-o",
                                   filePath,
                                   "src/test/resources/mic1/hi.mic1",
                                   "src/test/resources/mic1/hi.ijvm" });
     for (final byte b : "This should not be visible in output file ...\n".getBytes()) {
       Output.print(b);
     }
     assertThat(out.toString()).isEqualTo(this.GREETING + this.WELCOME + Text.INPUT_DEBUGGER.text()
                                                  + Text.TICKS.text(14) + getLineSeparator()
                                                  + Text.INPUT_DEBUGGER.text());
 
     BufferedReader in = null;
     FileInputStream fileInputStream = null;
     try {
       fileInputStream = new FileInputStream(filePath);
       in = new BufferedReader(new InputStreamReader(fileInputStream));
       assertThat(in.readLine()).isEqualTo("Hi!");
       assertThat(in.readLine()).isNull();
     } finally {
       if (in != null) {
         in.close();
       }
       if (fileInputStream != null) {
         fileInputStream.close();
       }
       assertThat(new File(filePath).delete()).isTrue();
     }
   }

  @Test
  public final void testMain_EmptyAssemblerCode() {
    printlnMethodName();
    Reader.setReader(new StringReader("ls-macro-code\nexit"));

    MicroDebug
      .main(new String[] { "src/test/resources/mic1/selectionsort.mic1", "src/test/resources/mic1/values.ijvm" });
    assertThat(out.toString()).isEqualTo(this.GREETING + this.WELCOME + Text.INPUT_DEBUGGER + Text.INPUT_DEBUGGER);
  }
 }
