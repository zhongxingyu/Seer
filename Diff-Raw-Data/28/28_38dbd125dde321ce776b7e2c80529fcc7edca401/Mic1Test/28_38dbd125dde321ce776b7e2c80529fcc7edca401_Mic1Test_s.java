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
 package com.github.croesch.mic1;
 
 import static org.fest.assertions.Assertions.assertThat;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.junit.Test;
 
 import com.github.croesch.DefaultTestCase;
 import com.github.croesch.error.FileFormatException;
 import com.github.croesch.i18n.Text;
 import com.github.croesch.mic1.io.Input;
 import com.github.croesch.mic1.io.Output;
 import com.github.croesch.mic1.register.Register;
 import com.github.croesch.misc.Printer;
 import com.github.croesch.misc.Settings;
 
 /**
  * Provides test cases for {@link Mic1}.
  * 
  * @author croesch
  * @since Date: Dec 1, 2011
  */
 public class Mic1Test extends DefaultTestCase {
 
   private Mic1 processor;
 
   @Override
   protected void setUpDetails() throws FileFormatException {
     this.processor = new Mic1(ClassLoader.getSystemResourceAsStream("mic1/hi.mic1"),
                               ClassLoader.getSystemResourceAsStream("mic1/hi.ijvm"));
   }
 
   @Test(timeout = 1000)
   public void testPerformanceOfProcessor() throws FileFormatException {
     printlnMethodName();
     final Mic1 processor = new Mic1(ClassLoader.getSystemResourceAsStream("mic1/performance.mic1"),
                                     ClassLoader.getSystemResourceAsStream("mic1/empty.ijvm"));
 
     Register.PC.setValue(-1);
     for (int i = 0; i < 1000; ++i) {
       // MAR = PC = PC + 1; rd; goto 0;
       processor.doTick();
       assertThat(Register.PC.getValue()).isEqualTo(i);
       assertThat(Register.MAR.getValue()).isEqualTo(i);
       assertThat(Register.MDR.getValue()).isEqualTo(0);
     }
   }
 
   @Test(timeout = 4000)
   public void testDivide() throws FileFormatException {
     printlnMethodName();
     final ByteArrayOutputStream out = new ByteArrayOutputStream();
     Output.setOut(new PrintStream(out));
 
     final Mic1 processor = new Mic1(ClassLoader.getSystemResourceAsStream("mic1/mic1ijvm2.mic1"),
                                     ClassLoader.getSystemResourceAsStream("mic1/divtest.ijvm"));
     assertThat(processor.run()).isEqualTo(3965);
 
     assertThat(out.toString()).isEqualTo("11111111111111111111111110000000\n");
     Output.setOut(System.out);
   }
 
   @Test
   public void testIJVM() throws FileFormatException {
     printlnMethodName();
     final ByteArrayOutputStream out = new ByteArrayOutputStream();
     Output.setOut(new PrintStream(out));
     Output.setBuffered(false);
 
     final Mic1 processor = new Mic1(ClassLoader.getSystemResourceAsStream("mic1/mic1ijvm.mic1"),
                                     ClassLoader.getSystemResourceAsStream("mic1/ijvmtest.ijvm"));
     assertThat(processor.run()).isEqualTo(31816);
 
     assertThat(out.toString()).isEqualTo("OK");
     Output.setOut(System.out);
   }
 
   @Test
   public void testMicroStepN() {
     printlnMethodName();
     this.processor.microStep(200);
     assertThat(out.toString()).isEqualTo(Text.TICKS.text(14) + getLineSeparator());
 
     out.reset();
     this.processor.reset();
     this.processor.microStep(200);
     assertThat(out.toString()).isEqualTo(Text.TICKS.text(14) + getLineSeparator());
   }
 
   @Test
   public void testMicroStepOne() {
     printMethodName();
     for (int i = 0; i < 14; ++i) {
       this.processor.microStep();
       assertThat(out.toString()).isEqualTo(Text.TICKS.text(1) + getLineSeparator());
       out.reset();
       printStep();
     }
     printLoopEnd();
     this.processor.microStep();
     assertThat(out.toString()).isEmpty();
 
     this.processor.reset();
     for (int i = 0; i < 14; ++i) {
       this.processor.microStep();
       assertThat(out.toString()).isEqualTo(Text.TICKS.text(1) + getLineSeparator());
       out.reset();
       printStep();
     }
     this.processor.microStep();
     assertThat(out.toString()).isEmpty();
     printEndOfMethod();
   }
 
   @Test
   public void testHi() throws FileFormatException {
     printlnMethodName();
     testHi(this.processor);
 
     this.processor.reset();
     testRunHi1(this.processor);
 
     this.processor.reset();
     testRunHi2(this.processor);
   }
 
   @Test
   public void testHaltPerUndefinedAddress() throws FileFormatException {
     printlnMethodName();
     final Mic1 processor = new Mic1(ClassLoader.getSystemResourceAsStream("mic1/hi-halt-per-null.mic1"),
                                     ClassLoader.getSystemResourceAsStream("mic1/hi.ijvm"));
     testHi(processor);
 
     processor.reset();
     testRunHi1(processor);
 
     processor.reset();
     testRunHi2(processor);
   }
 
   private void testRunHi2(final Mic1 processor) {
     final ByteArrayOutputStream micOut = new ByteArrayOutputStream();
     Output.setOut(new PrintStream(micOut));
 
     assertThat(processor.isHaltInstruction()).isFalse();
     assertThat(processor.run()).isEqualTo(14);
     assertThat(processor.isHaltInstruction()).isTrue();
     assertThat(processor.run()).isEqualTo(0);
     assertThat(processor.isHaltInstruction()).isTrue();
 
     assertThat(micOut.toString()).isEqualTo("Hi!\n");
     Output.setOut(System.out);
   }
 
   private void testRunHi1(final Mic1 processor) {
     final ByteArrayOutputStream micOut = new ByteArrayOutputStream();
     Output.setOut(new PrintStream(micOut));
     Printer.setPrintStream(new PrintStream(out));
 
     processor.microStep(1);
     assertTicksDoneAndResetPrintStream(1);
     processor.microStep(3);
     assertThat(out.toString()).isEqualTo(Text.TICKS.text(3) + getLineSeparator());
     out.reset();
 
     assertThat(processor.isHaltInstruction()).isFalse();
     assertThat(processor.run()).isEqualTo(10);
     assertThat(processor.isHaltInstruction()).isTrue();
     assertThat(processor.run()).isEqualTo(0);
     assertThat(processor.isHaltInstruction()).isTrue();
 
     assertThat(micOut.toString()).isEqualTo("Hi!\n");
     Output.setOut(System.out);
   }
 
   private void testHi(final Mic1 processor) {
     final ByteArrayOutputStream micOut = new ByteArrayOutputStream();
     Output.setBuffered(false);
     Output.setOut(new PrintStream(micOut));
     Printer.setPrintStream(new PrintStream(out));
 
     // 00: MAR = PC = 0; rd; goto 1;
     processor.microStep();
     assertThat(Register.MAR.getValue()).isEqualTo(0);
     assertThat(Register.PC.getValue()).isEqualTo(0);
     assertThat(micOut.toString()).isEmpty();
     assertThat(processor.isHaltInstruction()).isFalse();
     assertTicksDoneAndResetPrintStream(1);
 
     // 01: LV = H = -1; goto 2;
     processor.microStep();
     assertThat(Register.MAR.getValue()).isEqualTo(0);
     assertThat(Register.PC.getValue()).isEqualTo(0);
     assertThat(Register.LV.getValue()).isEqualTo(-1);
     assertThat(Register.H.getValue()).isEqualTo(-1);
     assertThat(micOut.toString()).isEmpty();
     assertThat(processor.isHaltInstruction()).isFalse();
     assertTicksDoneAndResetPrintStream(1);
 
     // 02: LV = H + LV; goto 3;
     processor.microStep();
     assertThat(Register.MDR.getValue()).isEqualTo('H');
     assertThat(Register.MAR.getValue()).isEqualTo(0);
     assertThat(Register.PC.getValue()).isEqualTo(0);
     assertThat(Register.LV.getValue()).isEqualTo(-2);
     assertThat(Register.H.getValue()).isEqualTo(-1);
     assertThat(micOut.toString()).isEmpty();
     assertThat(processor.isHaltInstruction()).isFalse();
     assertTicksDoneAndResetPrintStream(1);
 
     // 03: MAR = LV - 1; wr; goto 4;
     processor.microStep();
     assertThat(Register.MDR.getValue()).isEqualTo('H');
     assertThat(Register.MAR.getValue()).isEqualTo(-3);
     assertThat(Register.PC.getValue()).isEqualTo(0);
     assertThat(Register.LV.getValue()).isEqualTo(-2);
     assertThat(Register.H.getValue()).isEqualTo(-1);
     assertThat(micOut.toString()).isEqualTo("H");
     assertThat(processor.isHaltInstruction()).isFalse();
     assertTicksDoneAndResetPrintStream(1);
 
     // 04: MAR = PC = PC + 1; rd; goto 5;
     processor.microStep();
     assertThat(Register.MDR.getValue()).isEqualTo('H');
     assertThat(Register.MAR.getValue()).isEqualTo(1);
     assertThat(Register.PC.getValue()).isEqualTo(1);
     assertThat(Register.LV.getValue()).isEqualTo(-2);
     assertThat(Register.H.getValue()).isEqualTo(-1);
     assertThat(micOut.toString()).isEqualTo("H");
     assertThat(processor.isHaltInstruction()).isFalse();
     assertTicksDoneAndResetPrintStream(1);
 
     // 05: MAR = LV - 1; goto 6;
     processor.microStep();
     assertThat(Register.MDR.getValue()).isEqualTo('i');
     assertThat(Register.MAR.getValue()).isEqualTo(-3);
     assertThat(Register.PC.getValue()).isEqualTo(1);
     assertThat(Register.LV.getValue()).isEqualTo(-2);
     assertThat(Register.H.getValue()).isEqualTo(-1);
     assertThat(micOut.toString()).isEqualTo("H");
     assertThat(processor.isHaltInstruction()).isFalse();
     assertTicksDoneAndResetPrintStream(1);
 
     // 06: wr; goto 7;
     processor.microStep();
     assertThat(Register.MDR.getValue()).isEqualTo('i');
     assertThat(Register.MAR.getValue()).isEqualTo(-3);
     assertThat(Register.PC.getValue()).isEqualTo(1);
     assertThat(Register.LV.getValue()).isEqualTo(-2);
     assertThat(Register.H.getValue()).isEqualTo(-1);
     assertThat(micOut.toString()).isEqualTo("Hi");
     assertThat(processor.isHaltInstruction()).isFalse();
     assertTicksDoneAndResetPrintStream(1);
 
     // 07: MAR = PC = PC + 1; rd; goto 8;
     processor.microStep();
     assertThat(Register.MDR.getValue()).isEqualTo('i');
     assertThat(Register.MAR.getValue()).isEqualTo(2);
     assertThat(Register.PC.getValue()).isEqualTo(2);
     assertThat(Register.LV.getValue()).isEqualTo(-2);
     assertThat(Register.H.getValue()).isEqualTo(-1);
     assertThat(micOut.toString()).isEqualTo("Hi");
     assertThat(processor.isHaltInstruction()).isFalse();
     assertTicksDoneAndResetPrintStream(1);
 
     // 08: MAR = LV - 1; goto 9;
     processor.microStep();
     assertThat(Register.MDR.getValue()).isEqualTo('!');
     assertThat(Register.MAR.getValue()).isEqualTo(-3);
     assertThat(Register.PC.getValue()).isEqualTo(2);
     assertThat(Register.LV.getValue()).isEqualTo(-2);
     assertThat(Register.H.getValue()).isEqualTo(-1);
     assertThat(micOut.toString()).isEqualTo("Hi");
     assertThat(processor.isHaltInstruction()).isFalse();
     assertTicksDoneAndResetPrintStream(1);
 
     // 09: wr; goto 10;
     processor.microStep();
     assertThat(Register.MDR.getValue()).isEqualTo('!');
     assertThat(Register.MAR.getValue()).isEqualTo(-3);
     assertThat(Register.PC.getValue()).isEqualTo(2);
     assertThat(Register.LV.getValue()).isEqualTo(-2);
     assertThat(Register.H.getValue()).isEqualTo(-1);
     assertThat(micOut.toString()).isEqualTo("Hi!");
     assertThat(processor.isHaltInstruction()).isFalse();
     assertTicksDoneAndResetPrintStream(1);
 
     // 10: MAR = PC = PC + 1; rd; goto 11;
     processor.microStep();
     assertThat(Register.MDR.getValue()).isEqualTo('!');
     assertThat(Register.MAR.getValue()).isEqualTo(3);
     assertThat(Register.PC.getValue()).isEqualTo(3);
     assertThat(Register.LV.getValue()).isEqualTo(-2);
     assertThat(Register.H.getValue()).isEqualTo(-1);
     assertThat(micOut.toString()).isEqualTo("Hi!");
     assertThat(processor.isHaltInstruction()).isFalse();
     assertTicksDoneAndResetPrintStream(1);
 
     // 11: MAR = LV - 1; goto 12;
     processor.microStep();
     assertThat(Register.MDR.getValue()).isEqualTo('\n');
     assertThat(Register.MAR.getValue()).isEqualTo(-3);
     assertThat(Register.PC.getValue()).isEqualTo(3);
     assertThat(Register.LV.getValue()).isEqualTo(-2);
     assertThat(Register.H.getValue()).isEqualTo(-1);
     assertThat(micOut.toString()).isEqualTo("Hi!");
     assertThat(processor.isHaltInstruction()).isFalse();
     assertTicksDoneAndResetPrintStream(1);
 
     // 12: wr; goto 13;
     processor.microStep();
     assertThat(Register.MDR.getValue()).isEqualTo('\n');
     assertThat(Register.MAR.getValue()).isEqualTo(-3);
     assertThat(Register.PC.getValue()).isEqualTo(3);
     assertThat(Register.LV.getValue()).isEqualTo(-2);
     assertThat(Register.H.getValue()).isEqualTo(-1);
     assertThat(micOut.toString()).isEqualTo("Hi!\n");
     assertThat(processor.isHaltInstruction()).isFalse();
     assertTicksDoneAndResetPrintStream(1);
 
     // 13: halt;
     processor.microStep();
     assertThat(Register.MDR.getValue()).isEqualTo('\n');
     assertThat(Register.MAR.getValue()).isEqualTo(-3);
     assertThat(Register.PC.getValue()).isEqualTo(3);
     assertThat(Register.LV.getValue()).isEqualTo(-2);
     assertThat(Register.H.getValue()).isEqualTo(-1);
     assertThat(micOut.toString()).isEqualTo("Hi!\n");
     assertThat(processor.isHaltInstruction()).isTrue();
     assertTicksDoneAndResetPrintStream(1);
 
     processor.microStep();
     assertThat(out.toString()).isEmpty();
 
     processor.microStep(15);
     assertThat(out.toString()).isEmpty();
 
     Output.setOut(System.out);
   }
 
   @Test
   public void testErrorTexts() {
     printlnMethodName();
     // less than four bytes
     try {
       new Mic1(new ByteArrayInputStream(new byte[] {}), new ByteArrayInputStream(new byte[] {}));
       throw new AssertionError("should throw exception");
     } catch (final FileFormatException e) {
       // expected
     }
     assertThat(out.toString()).isEqualTo(Text.ERROR.text(Text.WRONG_FORMAT_MIC1.text(Text.WRONG_FORMAT_TOO_SMALL))
                                                  + getLineSeparator()
                                                  + Text.ERROR.text(Text.WRONG_FORMAT_IJVM
                                                    .text(Text.WRONG_FORMAT_TOO_SMALL)) + getLineSeparator());
     out.reset();
 
     // wrong magic number
     try {
       new Mic1(new ByteArrayInputStream(new byte[] { 0x1d, (byte) 0xea, (byte) 0xdf, (byte) 0xad }),
                new ByteArrayInputStream(new byte[] { 0x12, 0x34, 0x56, 0x78 }));
       throw new AssertionError("should throw exception");
     } catch (final FileFormatException e) {
       // expected
     }
     assertThat(out.toString()).isEqualTo(Text.ERROR.text(Text.WRONG_FORMAT_MIC1.text(Text.WRONG_FORMAT_MAGIC_NUMBER))
                                                  + getLineSeparator()
                                                  + Text.ERROR.text(Text.WRONG_FORMAT_IJVM
                                                    .text(Text.WRONG_FORMAT_MAGIC_NUMBER + getLineSeparator())));
     out.reset();
 
     // empty files
     try {
       new Mic1(new ByteArrayInputStream(new byte[] { 0x12, 0x34, 0x56, 0x78 }),
                new ByteArrayInputStream(new byte[] { 0x1d, (byte) 0xea, (byte) 0xdf, (byte) 0xad }));
       throw new AssertionError("should throw exception");
     } catch (final FileFormatException e) {
       // expected
     }
     assertThat(out.toString()).isEqualTo(Text.ERROR.text(Text.WRONG_FORMAT_MIC1.text(Text.WRONG_FORMAT_EMPTY))
                                                  + getLineSeparator());
     out.reset();
 
     // unexpected eof
     try {
       new Mic1(new ByteArrayInputStream(new byte[] { 0x12, 0x34, 0x56, 0x78, 0, 0, 0, 0, 0 }),
                new ByteArrayInputStream(new byte[] { 0x1d, (byte) 0xea, (byte) 0xdf, (byte) 0xad, 0 }));
       throw new AssertionError("should throw exception");
     } catch (final FileFormatException e) {
       // expected
     }
     assertThat(out.toString()).isEqualTo(Text.ERROR.text(Text.WRONG_FORMAT_IJVM.text(Text.WRONG_FORMAT_UNEXPECTED_END))
                                                  + getLineSeparator());
     out.reset();
 
     // unexpected end of block
     try {
       new Mic1(new ByteArrayInputStream(new byte[] { 0x12, 0x34, 0x56, 0x78, 0, 0, 0, 0, 0 }),
                new ByteArrayInputStream(new byte[] { 0x1d,
                                                     (byte) 0xea,
                                                     (byte) 0xdf,
                                                     (byte) 0xad,
                                                     0,
                                                     0,
                                                     0,
                                                     0,
                                                     0,
                                                     0,
                                                     0,
                                                     12 }));
       throw new AssertionError("should throw exception");
     } catch (final FileFormatException e) {
       // expected
     }
     assertThat(out.toString()).isEqualTo(Text.ERROR.text(Text.WRONG_FORMAT_IJVM
                                            .text(Text.WRONG_FORMAT_UNEXPECTED_END_OF_BLOCK)) + getLineSeparator());
     out.reset();
 
     // file too big
     final byte[] mic1File = new byte[5000];
     mic1File[0] = 0x12;
     mic1File[1] = 0x34;
     mic1File[2] = 0x56;
     mic1File[3] = 0x78;
     try {
       new Mic1(new ByteArrayInputStream(mic1File), new ByteArrayInputStream(new byte[] { 0x1d,
                                                                                         (byte) 0xea,
                                                                                         (byte) 0xdf,
                                                                                         (byte) 0xad }));
       throw new AssertionError("should throw exception");
     } catch (final FileFormatException e) {
       // expected
     }
     assertThat(out.toString()).isEqualTo(Text.ERROR.text(Text.WRONG_FORMAT_MIC1.text(Text.WRONG_FORMAT_TOO_BIG))
                                                  + getLineSeparator());
     out.reset();
   }
 
   @Test
   public final void testListAllRegisters() throws IOException {
     printlnMethodName();
     Register.MAR.setValue(-1);
     Register.MDR.setValue(0);
     Register.PC.setValue(1);
     Register.MBR.setValue(0x1273);
     Register.SP.setValue(0x8bc);
     Register.LV.setValue(0x8bd);
     Register.CPP.setValue(0x8be);
     Register.TOS.setValue(0x8bf);
     Register.OPC.setValue(0x8c0);
     Register.H.setValue(0x8c1);
 
     assertThat(out.toString()).isEmpty();
     this.processor.listAllRegisters();
 
     assertThat(out.toString()).isEqualTo(Text.REGISTER_VALUE.text("MAR ", "0xFFFFFFFF") + getLineSeparator()
                                                  + Text.REGISTER_VALUE.text("MDR ", "0x0") + getLineSeparator()
                                                  + Text.REGISTER_VALUE.text("PC  ", "0x1") + getLineSeparator()
                                                  + Text.REGISTER_VALUE.text("MBR ", "0x73") + getLineSeparator()
                                                  + Text.REGISTER_VALUE.text("MBRU", "0x73") + getLineSeparator()
                                                  + Text.REGISTER_VALUE.text("SP  ", "0x8BC") + getLineSeparator()
                                                  + Text.REGISTER_VALUE.text("LV  ", "0x8BD") + getLineSeparator()
                                                  + Text.REGISTER_VALUE.text("CPP ", "0x8BE") + getLineSeparator()
                                                  + Text.REGISTER_VALUE.text("TOS ", "0x8BF") + getLineSeparator()
                                                  + Text.REGISTER_VALUE.text("OPC ", "0x8C0") + getLineSeparator()
                                                  + Text.REGISTER_VALUE.text("H   ", "0x8C1") + getLineSeparator());
   }
 
   @Test
   public final void testListSingleRegister() throws IOException {
     printlnMethodName();
     Register.MAR.setValue(0x4711);
     this.processor.listSingleRegister(Register.MAR);
     assertThat(out.toString()).isEqualTo(Text.REGISTER_VALUE.text("MAR ", "0x4711") + getLineSeparator());
     out.reset();
 
     Register.SP.setValue(-2);
     this.processor.listSingleRegister(Register.SP);
     assertThat(out.toString()).isEqualTo(Text.REGISTER_VALUE.text("SP  ", "0xFFFFFFFE") + getLineSeparator());
     out.reset();
   }
 
   @Test
   public final void testTraceMicro() throws IOException {
     printlnMethodName();
     final String firstLine = Text.EXECUTED_CODE.text("PC=MAR=0;rd;goto 0x1") + getLineSeparator();
     final String expected = firstLine + Text.EXECUTED_CODE.text("H=LV=-1;goto 0x2") + getLineSeparator()
                             + Text.EXECUTED_CODE.text("LV=H+LV;wr;goto 0x3") + getLineSeparator()
                             + Text.EXECUTED_CODE.text("MAR=LV-1;wr;goto 0x4") + getLineSeparator()
                             + Text.EXECUTED_CODE.text("PC=MAR=PC+1;rd;goto 0x5") + getLineSeparator()
                             + Text.EXECUTED_CODE.text("MAR=LV-1;goto 0x6") + getLineSeparator()
                             + Text.EXECUTED_CODE.text("wr;goto 0x7") + getLineSeparator()
                             + Text.EXECUTED_CODE.text("PC=MAR=PC+1;rd;goto 0x8") + getLineSeparator()
                             + Text.EXECUTED_CODE.text("MAR=LV-1;goto 0x9") + getLineSeparator()
                             + Text.EXECUTED_CODE.text("wr;goto 0xA") + getLineSeparator()
                             + Text.EXECUTED_CODE.text("PC=MAR=PC+1;rd;goto 0xB") + getLineSeparator()
                             + Text.EXECUTED_CODE.text("MAR=LV-1;goto 0xC") + getLineSeparator()
                             + Text.EXECUTED_CODE.text("wr;goto 0xD") + getLineSeparator()
                             + Text.EXECUTED_CODE.text("goto 0xD") + getLineSeparator() + Text.TICKS.text(14)
                             + getLineSeparator();
 
     this.processor.run();
     assertThat(out.toString()).isEqualTo(Text.TICKS.text(14) + getLineSeparator());
     out.reset();
 
     this.processor.reset();
 
     this.processor.traceMicro();
     this.processor.run();
     assertThat(out.toString()).isEqualTo(expected);
 
     this.processor.reset();
     out.reset();
 
     this.processor.microStep();
     assertThat(out.toString()).isEqualTo(firstLine + Text.TICKS.text(1) + getLineSeparator());
     out.reset();
 
     this.processor.untraceMicro();
     this.processor.run();
     assertThat(out.toString()).isEqualTo(Text.TICKS.text(13) + getLineSeparator());
   }
 
   @Test
   public void testAddRegisterBreakPoint() {
     printlnMethodName();
     this.processor.addRegisterBreakpoint(Register.H, Integer.valueOf(-1));
     this.processor.run();
     assertThat(out.toString()).isEqualTo(Text.TICKS.text(2) + getLineSeparator());
 
     out.reset();
     this.processor.run();
     assertThat(out.toString()).isEqualTo(Text.TICKS.text(1) + getLineSeparator());
 
     out.reset();
     this.processor.reset();
     this.processor.microStep();
     assertThat(out.toString()).isEqualTo(Text.TICKS.text(1) + getLineSeparator());
 
     out.reset();
     this.processor.microStep();
     assertThat(out.toString()).isEqualTo(Text.TICKS.text(1) + getLineSeparator());
 
     out.reset();
     this.processor.microStep();
     assertThat(out.toString()).isEqualTo(Text.TICKS.text(1) + getLineSeparator());
   }
 
   @Test
   public void testAddMicroBreakPoint() {
     printlnMethodName();
     this.processor.addMicroBreakpoint(Integer.valueOf(2));
     this.processor.addMicroBreakpoint(Integer.valueOf(3));
     this.processor.run();
     assertThat(out.toString()).isEqualTo(Text.TICKS.text(2) + getLineSeparator());
 
     out.reset();
     this.processor.run();
     assertThat(out.toString()).isEqualTo(Text.TICKS.text(1) + getLineSeparator());
 
     out.reset();
     this.processor.reset();
     this.processor.microStep(2);
     assertThat(out.toString()).isEqualTo(Text.TICKS.text(2) + getLineSeparator());
 
     out.reset();
     this.processor.microStep(2);
     assertThat(out.toString()).isEqualTo(Text.TICKS.text(1) + getLineSeparator());
 
     out.reset();
     this.processor.run();
     assertThat(out.toString()).isEqualTo(Text.TICKS.text(11) + getLineSeparator());
   }
 
   @Test
   public void testAddMacroBreakPoint() throws FileFormatException {
     printlnMethodName();
     this.processor = new Mic1(ClassLoader.getSystemResourceAsStream("mic1/mic1ijvm.mic1"),
                               ClassLoader.getSystemResourceAsStream("mic1/add.ijvm"));
     Input.setIn(new ByteArrayInputStream("2\n2\n".getBytes()));
 
     this.processor.addMacroBreakpoint(Integer.valueOf(2));
     this.processor.addMacroBreakpoint(Integer.valueOf(3));
     this.processor.run();
     assertThat(out.toString()).isEqualTo(Text.TICKS.text(7) + getLineSeparator());
 
     out.reset();
     this.processor.run();
     assertThat(out.toString()).isEqualTo(Text.TICKS.text(3) + getLineSeparator());
 
     out.reset();
     this.processor.reset();
     this.processor.microStep(2);
     assertThat(out.toString()).isEqualTo(Text.TICKS.text(2) + getLineSeparator());
 
     out.reset();
     this.processor.microStep(12);
     assertThat(out.toString()).isEqualTo(Text.TICKS.text(5) + getLineSeparator());
 
     out.reset();
     this.processor.run();
     assertThat(out.toString()).isEqualTo(Text.TICKS.text(3) + getLineSeparator());
 
     out.reset();
     this.processor.run();
     assertThat(out.toString()).isEqualTo(Text.INPUT_MIC1.text() + Text.INPUT_MIC1.text() + Text.TICKS.text(3282)
                                                  + getLineSeparator());
   }
 
   @Test
   public void testRemoveRegisterBreakPoint() {
     printlnMethodName();
     this.processor.addRegisterBreakpoint(Register.H, Integer.valueOf(-1));
     this.processor.run();
     assertThat(out.toString()).isEqualTo(Text.TICKS.text(2) + getLineSeparator());
     out.reset();
 
     this.processor.reset();
     this.processor.run();
     assertThat(out.toString()).isEqualTo(Text.TICKS.text(2) + getLineSeparator());
     out.reset();
 
     this.processor.listBreakpoints();
     final Matcher m = Pattern.compile(".*#([0-9]+).*" + getLineSeparator()).matcher(out.toString());
 
     assertThat(m.matches()).isTrue();
     this.processor.removeBreakpoint(Integer.parseInt(m.group(1)));
     out.reset();
 
     this.processor.reset();
     this.processor.run();
     assertThat(out.toString()).isEqualTo(Text.TICKS.text(14) + getLineSeparator());
     out.reset();
   }
 
   @Test
   public void testListBreakpoints() {
     printMethodName();
 
     this.processor.addRegisterBreakpoint(Register.MBRU, Integer.valueOf(16));
     this.processor.addRegisterBreakpoint(Register.MBRU, Integer.valueOf(-48));
 
     this.processor.addRegisterBreakpoint(Register.CPP, Integer.valueOf(-1));
     this.processor.addRegisterBreakpoint(Register.CPP, Integer.valueOf(Integer.MAX_VALUE));
     this.processor.addRegisterBreakpoint(Register.CPP, Integer.valueOf(Integer.MIN_VALUE));
 
     this.processor.addRegisterBreakpoint(Register.H, Integer.valueOf(2));
     this.processor.addRegisterBreakpoint(Register.H, Integer.valueOf(2));
     this.processor.addRegisterBreakpoint(Register.H, Integer.valueOf(3));
     this.processor.addRegisterBreakpoint(Register.H, Integer.valueOf(1));
 
     assertThat(out.toString()).isEmpty();
     this.processor.listBreakpoints();
     assertThat(out.toString()).matches(Text.BREAKPOINT_REGISTER.text("[0-9]+", Register.MBRU, "0x10")
                                                + getLineSeparator()
                                                + Text.BREAKPOINT_REGISTER.text("[0-9]+", Register.MBRU, "0xFFFFFFD0")
                                                + getLineSeparator()
                                                + Text.BREAKPOINT_REGISTER.text("[0-9]+", Register.CPP, "0xFFFFFFFF")
                                                + getLineSeparator()
                                                + Text.BREAKPOINT_REGISTER.text("[0-9]+", Register.CPP, "0x7FFFFFFF")
                                                + getLineSeparator()
                                                + Text.BREAKPOINT_REGISTER.text("[0-9]+", Register.CPP, "0x80000000")
                                                + getLineSeparator()
                                                + Text.BREAKPOINT_REGISTER.text("[0-9]+", Register.H, "0x2")
                                                + getLineSeparator()
                                                + Text.BREAKPOINT_REGISTER.text("[0-9]+", Register.H, "0x3")
                                                + getLineSeparator()
                                                + Text.BREAKPOINT_REGISTER.text("[0-9]+", Register.H, "0x1")
                                                + getLineSeparator());
 
     printEndOfMethod();
   }
 
   @Test
   public void testPrintContent() throws FileFormatException {
     printlnMethodName();
     this.processor = new Mic1(ClassLoader.getSystemResourceAsStream("mic1/mic1ijvm.mic1"),
                               ClassLoader.getSystemResourceAsStream("mic1/test.ijvm"));
 
     this.processor.printContent(0, 1);
     assertThat(out.toString()).isEqualTo(Text.MEMORY_CONTENT.text("     0x0", "0x10203") + getLineSeparator()
                                                  + Text.MEMORY_CONTENT.text("     0x1", "0x4050607")
                                                  + getLineSeparator());
     out.reset();
 
     this.processor.printContent(1, 0);
     assertThat(out.toString()).isEqualTo(Text.MEMORY_CONTENT.text("     0x0", "0x10203") + getLineSeparator()
                                                  + Text.MEMORY_CONTENT.text("     0x1", "0x4050607")
                                                  + getLineSeparator());
     out.reset();
 
     this.processor.printContent(0, 0);
     assertThat(out.toString()).isEqualTo(Text.MEMORY_CONTENT.text("     0x0", "0x10203") + getLineSeparator());
     out.reset();
 
     this.processor.printContent(2, -13);
     assertThat(out.toString()).isEqualTo(Text.MEMORY_CONTENT.text("     0x0", "0x10203") + getLineSeparator()
                                                  + Text.MEMORY_CONTENT.text("     0x1", "0x4050607")
                                                  + getLineSeparator()
                                                  + Text.MEMORY_CONTENT.text("     0x2", "0x8090A0B")
                                                  + getLineSeparator());
     out.reset();
 
     this.processor.printContent(3, 1);
     assertThat(out.toString()).isEqualTo(Text.MEMORY_CONTENT.text("     0x1", "0x4050607") + getLineSeparator()
                                                  + Text.MEMORY_CONTENT.text("     0x2", "0x8090A0B")
                                                  + getLineSeparator()
                                                  + Text.MEMORY_CONTENT.text("     0x3", "0xC0D0E0F")
                                                  + getLineSeparator());
     out.reset();
 
     this.processor.printContent(Byte.MAX_VALUE, Byte.MAX_VALUE - 3);
     assertThat(out.toString()).isEqualTo(Text.MEMORY_CONTENT.text("    0x7C", "0x0") + getLineSeparator()
                                                  + Text.MEMORY_CONTENT.text("    0x7D", "0x0") + getLineSeparator()
                                                  + Text.MEMORY_CONTENT.text("    0x7E", "0x0") + getLineSeparator()
                                                  + Text.MEMORY_CONTENT.text("    0x7F", "0x0") + getLineSeparator());
     out.reset();
   }
 
   @Test
   public void testStep() throws FileFormatException {
     printlnMethodName();
     this.processor = new Mic1(ClassLoader.getSystemResourceAsStream("mic1/mic1ijvm.mic1"),
                               ClassLoader.getSystemResourceAsStream("mic1/add.ijvm"));
     assertThat(Register.PC.getValue()).isEqualTo(Settings.MIC1_REGISTER_PC_DEFVAL.getValue());
 
     this.processor.step();
     assertThat(Register.PC.getValue()).isEqualTo(0);
     assertTicksDoneAndResetPrintStream(3);
 
     this.processor.step();
     assertThat(Register.PC.getValue()).isEqualTo(2);
     assertTicksDoneAndResetPrintStream(4);
 
     this.processor.step();
     assertThat(Register.PC.getValue()).isEqualTo(3);
     assertTicksDoneAndResetPrintStream(3);
 
     this.processor.step();
     assertThat(Register.PC.getValue()).isEqualTo(5);
     assertTicksDoneAndResetPrintStream(7);
 
     this.processor.step();
     assertThat(Register.PC.getValue()).isEqualTo(7);
     assertTicksDoneAndResetPrintStream(7);
 
     this.processor.step();
     assertThat(Register.PC.getValue()).isEqualTo(9);
     assertTicksDoneAndResetPrintStream(4);
 
     this.processor.step();
     assertThat(Register.PC.getValue()).isEqualTo(10);
     assertTicksDoneAndResetPrintStream(9);
 
     this.processor.step();
     assertThat(Register.PC.getValue()).isEqualTo(13);
     assertTicksDoneAndResetPrintStream(8);
 
     this.processor.step();
     assertThat(Register.PC.getValue()).isEqualTo(71);
     assertTicksDoneAndResetPrintStream(23);
   }
 
   @Test
   public void testStepN() throws FileFormatException {
     printlnMethodName();
     this.processor = new Mic1(ClassLoader.getSystemResourceAsStream("mic1/mic1ijvm.mic1"),
                               ClassLoader.getSystemResourceAsStream("mic1/add.ijvm"));
     Input.setIn(new ByteArrayInputStream("2\n2\n".getBytes()));
     Output.setBuffered(true);
     Output.setOut(new PrintStream(out));
     assertThat(Register.PC.getValue()).isEqualTo(Settings.MIC1_REGISTER_PC_DEFVAL.getValue());
 
     this.processor.step(0);
     assertThat(Register.PC.getValue()).isEqualTo(Settings.MIC1_REGISTER_PC_DEFVAL.getValue());
     assertThat(out.toString()).isEmpty();
 
     this.processor.step(1);
     assertThat(Register.PC.getValue()).isEqualTo(0);
     assertTicksDoneAndResetPrintStream(3);
 
     this.processor.step(2);
     assertThat(Register.PC.getValue()).isEqualTo(3);
     assertTicksDoneAndResetPrintStream(7);
 
     this.processor.step(3);
     assertThat(Register.PC.getValue()).isEqualTo(9);
     assertTicksDoneAndResetPrintStream(18);
 
     this.processor.step(-1);
     assertThat(Register.PC.getValue()).isEqualTo(9);
     assertThat(out.toString()).isEmpty();
 
     this.processor.step(0);
     assertThat(Register.PC.getValue()).isEqualTo(9);
     assertThat(out.toString()).isEmpty();
 
     this.processor.step(1);
     assertThat(Register.PC.getValue()).isEqualTo(10);
     assertTicksDoneAndResetPrintStream(9);
 
     this.processor.step(2);
     assertThat(Register.PC.getValue()).isEqualTo(71);
     assertTicksDoneAndResetPrintStream(31);
 
     this.processor.step(560);
     assertThat(Register.PC.getValue()).isEqualTo(0x11D);
    assertThat(out.toString()).isEqualTo(Text.INPUT_MIC1.text() + " 2" + getLineSeparator() + Text.INPUT_MIC1.text()
                                                 + "+2" + getLineSeparator() + "========" + getLineSeparator()
                                                 + "00000004" + getLineSeparator() + Text.TICKS.text(3213)
                                                 + getLineSeparator());
     out.reset();
 
     this.processor.step(560);
     assertThat(Register.PC.getValue()).isEqualTo(0x41);
     assertTicksDoneAndResetPrintStream(11);
 
     this.processor.step(560000);
     assertThat(Register.PC.getValue()).isEqualTo(0x41);
     assertThat(out.toString()).isEmpty();
 
     Output.setOut(System.out);
   }
 
   @Test
   public final void testTraceMacro() throws IOException {
     printlnMethodName();
     Input.setIn(new ByteArrayInputStream("2\n2\n2\n2\n".getBytes()));
 
     this.processor = new Mic1(ClassLoader.getSystemResourceAsStream("mic1/mic1ijvm.mic1"),
                               ClassLoader.getSystemResourceAsStream("mic1/add.ijvm"));
 
     final String firstLine = Text.EXECUTED_CODE.text("     0x0: [ 0x10] BIPUSH 0x0") + getLineSeparator();
     final String expected = firstLine + Text.EXECUTED_CODE.text("     0x2: [ 0x59] DUP") + getLineSeparator()
                             + Text.EXECUTED_CODE.text("     0x3: [ 0x36] ISTORE 0") + getLineSeparator()
                             + Text.EXECUTED_CODE.text("     0x5: [ 0x36] ISTORE 1") + getLineSeparator()
                             + Text.TICKS.text(24) + getLineSeparator();
 
     this.processor.run();
     assertThat(out.toString()).isEqualTo(Text.INPUT_MIC1.text() + Text.INPUT_MIC1.text() + Text.TICKS.text(3292)
                                                  + getLineSeparator());
     out.reset();
 
     this.processor.reset();
 
     this.processor.traceMacro();
     this.processor.step(5);
     assertThat(out.toString()).isEqualTo(expected);
 
     this.processor.reset();
     out.reset();
 
     this.processor.step(2);
     assertThat(out.toString()).isEqualTo(firstLine + Text.TICKS.text(7) + getLineSeparator());
     out.reset();
 
     this.processor.untraceMacro();
     this.processor.run();
     assertThat(out.toString()).isEqualTo(Text.INPUT_MIC1.text() + Text.INPUT_MIC1.text() + Text.TICKS.text(3285)
                                                  + getLineSeparator());
   }
 
   @Test
   public final void testPrintStack() throws IOException {
     printlnMethodName();
     Input.setIn(new ByteArrayInputStream("2\n2\n2\n2\n".getBytes()));
 
     this.processor = new Mic1(ClassLoader.getSystemResourceAsStream("mic1/mic1ijvm.mic1"),
                               ClassLoader.getSystemResourceAsStream("mic1/add.ijvm"));
 
     this.processor.step(15);
     out.reset();
     this.processor.printStack();
     assertThat(out.toString()).isEqualTo(Text.STACK_CONTENT.text(0, "  0xC000", "0x0") + getLineSeparator()
                                                  + Text.STACK_CONTENT.text(1, "  0xC001", "0xC003")
                                                  + getLineSeparator() + Text.STACK_CONTENT.text(2, "  0xC002", "0x0")
                                                  + getLineSeparator() + Text.STACK_CONTENT.text(3, "  0xC003", "0x10")
                                                  + getLineSeparator()
                                                  + Text.STACK_CONTENT.text(4, "  0xC004", "0x8000")
                                                  + getLineSeparator() + Text.STACK_CONTENT.text(5, "  0xC005", "0x32")
                                                  + getLineSeparator());
 
     this.processor.step();
     out.reset();
     this.processor.printStack();
     assertThat(out.toString()).isEqualTo(Text.STACK_CONTENT.text(0, "  0xC000", "0x0") + getLineSeparator()
                                                  + Text.STACK_CONTENT.text(1, "  0xC001", "0xC003")
                                                  + getLineSeparator() + Text.STACK_CONTENT.text(2, "  0xC002", "0x0")
                                                  + getLineSeparator() + Text.STACK_CONTENT.text(3, "  0xC003", "0x10")
                                                  + getLineSeparator()
                                                  + Text.STACK_CONTENT.text(4, "  0xC004", "0x8000")
                                                  + getLineSeparator() + Text.STACK_CONTENT.text(5, "  0xC005", "0x32")
                                                  + getLineSeparator() + Text.STACK_CONTENT.text(6, "  0xC006", "0x32")
                                                  + getLineSeparator());
 
     this.processor.step();
     out.reset();
     this.processor.printStack();
     assertThat(out.toString()).isEqualTo(Text.STACK_CONTENT.text(0, "  0xC000", "0x0") + getLineSeparator()
                                                  + Text.STACK_CONTENT.text(1, "  0xC001", "0xC003")
                                                  + getLineSeparator() + Text.STACK_CONTENT.text(2, "  0xC002", "0x0")
                                                  + getLineSeparator() + Text.STACK_CONTENT.text(3, "  0xC003", "0x10")
                                                  + getLineSeparator()
                                                  + Text.STACK_CONTENT.text(4, "  0xC004", "0x8000")
                                                  + getLineSeparator() + Text.STACK_CONTENT.text(5, "  0xC005", "0x32")
                                                  + getLineSeparator() + Text.STACK_CONTENT.text(6, "  0xC006", "0x32")
                                                  + getLineSeparator() + Text.STACK_CONTENT.text(7, "  0xC007", "0x30")
                                                  + getLineSeparator());
 
     this.processor.step();
     out.reset();
     this.processor.printStack();
     assertThat(out.toString()).isEqualTo(Text.STACK_CONTENT.text(0, "  0xC000", "0x0") + getLineSeparator()
                                                  + Text.STACK_CONTENT.text(1, "  0xC001", "0xC003")
                                                  + getLineSeparator() + Text.STACK_CONTENT.text(2, "  0xC002", "0x0")
                                                  + getLineSeparator() + Text.STACK_CONTENT.text(3, "  0xC003", "0x10")
                                                  + getLineSeparator()
                                                  + Text.STACK_CONTENT.text(4, "  0xC004", "0x8000")
                                                  + getLineSeparator() + Text.STACK_CONTENT.text(5, "  0xC005", "0x32")
                                                  + getLineSeparator() + Text.STACK_CONTENT.text(6, "  0xC006", "0x2")
                                                  + getLineSeparator());
 
     this.processor.step();
     out.reset();
     this.processor.printStack();
     assertThat(out.toString()).isEqualTo(Text.STACK_CONTENT.text(0, "  0xC000", "0x0") + getLineSeparator()
                                                  + Text.STACK_CONTENT.text(1, "  0xC001", "0xC003")
                                                  + getLineSeparator() + Text.STACK_CONTENT.text(2, "  0xC002", "0x0")
                                                  + getLineSeparator() + Text.STACK_CONTENT.text(3, "  0xC003", "0x10")
                                                  + getLineSeparator()
                                                  + Text.STACK_CONTENT.text(4, "  0xC004", "0x8000")
                                                  + getLineSeparator() + Text.STACK_CONTENT.text(5, "  0xC005", "0x32")
                                                  + getLineSeparator());
 
     this.processor.step();
     out.reset();
     this.processor.printStack();
     assertThat(out.toString()).isEqualTo(Text.STACK_CONTENT.text(0, "  0xC000", "0x0") + getLineSeparator()
                                                  + Text.STACK_CONTENT.text(1, "  0xC001", "0xC003")
                                                  + getLineSeparator() + Text.STACK_CONTENT.text(2, "  0xC002", "0x0")
                                                  + getLineSeparator() + Text.STACK_CONTENT.text(3, "  0xC003", "0x10")
                                                  + getLineSeparator()
                                                  + Text.STACK_CONTENT.text(4, "  0xC004", "0x8000")
                                                  + getLineSeparator() + Text.STACK_CONTENT.text(5, "  0xC005", "0x32")
                                                  + getLineSeparator() + Text.STACK_CONTENT.text(6, "  0xC006", "0x32")
                                                  + getLineSeparator());
   }
 }
