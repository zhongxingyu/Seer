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
 package com.github.croesch.console;
 
 import static org.fest.assertions.Assertions.assertThat;
 
 import java.io.BufferedReader;
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintStream;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.junit.Test;
 
 import com.github.croesch.DefaultTestCase;
 import com.github.croesch.error.FileFormatException;
 import com.github.croesch.i18n.Text;
 import com.github.croesch.mic1.Mic1;
 import com.github.croesch.mic1.io.Input;
 import com.github.croesch.mic1.io.Output;
 import com.github.croesch.mic1.register.Register;
 import com.github.croesch.misc.Settings;
 
 /**
  * Provides test cases for {@link UserInstruction}.
  * 
  * @author croesch
  * @since Date: Dec 3, 2011
  */
 public class UserInstructionTest extends DefaultTestCase {
 
   private Mic1 processor;
 
   @Override
   protected void setUpDetails() throws FileFormatException {
     this.processor = new Mic1(ClassLoader.getSystemResourceAsStream("mic1/hi.mic1"),
                               ClassLoader.getSystemResourceAsStream("mic1/hi.ijvm"));
   }
 
   @Test
   public final void testOf() {
     printMethodName();
 
     assertThat(UserInstruction.of("Help")).isSameAs(UserInstruction.HELP);
 
     for (final UserInstruction ins : UserInstruction.values()) {
       final String name = ins.name().replaceAll("_", "-");
       assertThat(UserInstruction.of(name)).isSameAs(ins);
       assertThat(UserInstruction.of(name.toLowerCase())).isSameAs(ins);
       assertThat(UserInstruction.of("--" + name)).isNull();
       assertThat(UserInstruction.of("--" + name.toLowerCase())).isNull();
       printStep();
     }
 
     assertThat(UserInstruction.of(null)).isNull();
     assertThat(UserInstruction.of("")).isNull();
     assertThat(UserInstruction.of(" ")).isNull();
 
     printEndOfMethod();
   }
 
   @Test
   public final void testGetSize() {
     printlnMethodName();
     assertThat(UserInstruction.getSize(null)).isZero();
     assertThat(UserInstruction.getSize(new String[] {})).isZero();
     assertThat(UserInstruction.getSize(new String[] { null })).isEqualTo(1);
     assertThat(UserInstruction.getSize(new String[] { "" })).isEqualTo(1);
     assertThat(UserInstruction.getSize(new String[] { " " })).isEqualTo(1);
     assertThat(UserInstruction.getSize(new String[] { " ", "" })).isEqualTo(2);
     assertThat(UserInstruction.getSize(new String[] { "H", "14" })).isEqualTo(2);
     assertThat(UserInstruction.getSize(new Object[] { "H", "14" })).isEqualTo(2);
     assertThat(UserInstruction.getSize(new Object[] { "H", 14 })).isEqualTo(2);
   }
 
   @Test
   public final void testExecuteExit() throws IOException {
     printlnMethodName();
     assertThat(UserInstruction.EXIT.execute(null)).isFalse();
     assertThat(UserInstruction.EXIT.execute(null, "asd")).isFalse();
     assertThat(UserInstruction.EXIT.execute(null, "asd", "asd")).isFalse();
 
     assertThat(out.toString()).isEmpty();
   }
 
   @Test
   public final void testExecuteHelp() throws IOException {
     printlnMethodName();
     assertThat(UserInstruction.HELP.execute(null, "asd")).isTrue();
     assertThat(UserInstruction.HELP.execute(null, "asd", "asd")).isTrue();
     out.reset();
 
     assertThat(UserInstruction.HELP.execute(null)).isTrue();
 
     final StringBuilder sb = new StringBuilder();
     BufferedReader reader = null;
     try {
       reader = new BufferedReader(new InputStreamReader(getClass().getClassLoader()
         .getResourceAsStream("instruction-help.txt")));
       String line;
       while ((line = reader.readLine()) != null) {
         sb.append(line).append(getLineSeparator());
       }
     } finally {
       if (reader != null) {
         reader.close();
       }
     }
 
     assertThat(out.toString()).isEqualTo(sb.toString());
   }
 
   @Test(expected = NullPointerException.class)
   public final void testExecuteRun_NPE() throws IOException {
     printlnMethodName();
     UserInstruction.RUN.execute(null, "asd");
   }
 
   @Test
   public final void testExecuteRun() throws IOException {
     printlnMethodName();
     assertThat(UserInstruction.RUN.execute(this.processor, "asd")).isTrue();
     assertThat(UserInstruction.RUN.execute(this.processor, "asd", "asd")).isTrue();
     out.reset();
 
     assertThat(UserInstruction.RESET.execute(this.processor)).isTrue();
 
     assertThat(UserInstruction.RUN.execute(this.processor)).isTrue();
     assertThat(out.toString()).isEqualTo(Text.TICKS.text(14) + getLineSeparator());
 
     out.reset();
     assertThat(UserInstruction.RUN.execute(this.processor)).isTrue();
     assertThat(out.toString()).isEmpty();
   }
 
   @Test
   public final void testExecuteSet_WrongNumberOfParameters() {
     printlnMethodName();
     Register.CPP.setValue(0xa1234);
     assertThatNoParameterIsWrong(UserInstruction.SET, 2);
     assertThatOneParameterIsWrong(UserInstruction.SET);
     assertThatThreeParametersAreWrong(UserInstruction.SET, 2);
     assertThat(Register.CPP.getValue()).isEqualTo(0xa1234);
   }
 
   private void assertThatWrongNumberOfParametersIsPrintedAndResetOut(final int exp, final int was) {
     assertThat(out.toString()).isEqualTo(Text.ERROR.text(Text.WRONG_PARAM_NUMBER.text(exp, was)) + getLineSeparator());
     out.reset();
   }
 
   @Test
   public void testExecuteSet_Valid() {
     printlnMethodName();
     Register.CPP.setValue(0xa1234);
 
     assertThat(UserInstruction.SET.execute(null, Register.CPP.name(), "14")).isTrue();
     assertThat(Register.CPP.getValue()).isEqualTo(0xE);
     assertThat(out.toString()).isEmpty();
 
     assertThat(UserInstruction.SET.execute(null, Register.H.name(), "0xF1")).isTrue();
     assertThat(Register.CPP.getValue()).isEqualTo(0xE);
     assertThat(Register.H.getValue()).isEqualTo(0xF1);
     assertThat(out.toString()).isEmpty();
 
     assertThat(UserInstruction.SET.execute(null, Register.CPP.name(), "0b1010")).isTrue();
     assertThat(Register.CPP.getValue()).isEqualTo(0xa);
     assertThat(out.toString()).isEmpty();
   }
 
   @Test
   public void testExecuteSet_Invalid() {
     printlnMethodName();
     Register.CPP.setValue(0xa1234);
 
     assertThat(UserInstruction.SET.execute(null, "abc", "14")).isTrue();
     assertThat(Register.CPP.getValue()).isEqualTo(0xa1234);
     assertThat(out.toString()).isEqualTo(Text.ERROR.text(Text.INVALID_REGISTER.text("abc")) + getLineSeparator());
     out.reset();
 
     assertThat(UserInstruction.SET.execute(null, "", "14")).isTrue();
     assertThat(Register.CPP.getValue()).isEqualTo(0xa1234);
     assertThat(out.toString()).isEqualTo(Text.ERROR.text(Text.INVALID_REGISTER.text("")) + getLineSeparator());
     out.reset();
 
     assertThat(UserInstruction.SET.execute(null, " h ", "14")).isTrue();
     assertThat(Register.CPP.getValue()).isEqualTo(0xa1234);
     assertThat(out.toString()).isEqualTo(Text.ERROR.text(Text.INVALID_REGISTER.text(" h ")) + getLineSeparator());
     out.reset();
 
     assertThat(UserInstruction.SET.execute(null, Register.CPP.name(), "14_2")).isTrue();
     assertThat(Register.CPP.getValue()).isEqualTo(0xa1234);
     assertThat(out.toString()).isEqualTo(Text.ERROR.text(Text.INVALID_NUMBER.text("14_2")) + getLineSeparator());
     out.reset();
 
     assertThat(UserInstruction.SET.execute(null, Register.CPP.name(), "0xXY")).isTrue();
     assertThat(Register.CPP.getValue()).isEqualTo(0xa1234);
     assertThat(out.toString()).isEqualTo(Text.ERROR.text(Text.INVALID_NUMBER.text("0xXY")) + getLineSeparator());
     out.reset();
 
     assertThat(UserInstruction.SET.execute(null, Register.CPP.name(), "H")).isTrue();
     assertThat(Register.CPP.getValue()).isEqualTo(0xa1234);
     assertThat(out.toString()).isEqualTo(Text.ERROR.text(Text.INVALID_NUMBER.text("H")) + getLineSeparator());
     out.reset();
 
     assertThat(UserInstruction.SET.execute(null, "abc", "14_2")).isTrue();
     assertThat(Register.CPP.getValue()).isEqualTo(0xa1234);
     assertThat(out.toString()).isEqualTo(Text.ERROR.text(Text.INVALID_REGISTER.text("abc")) + getLineSeparator()
                                                  + Text.ERROR.text(Text.INVALID_NUMBER.text("14_2"))
                                                  + getLineSeparator());
     out.reset();
 
     assertThat(UserInstruction.SET.execute(null, "REG", "0xXY")).isTrue();
     assertThat(Register.CPP.getValue()).isEqualTo(0xa1234);
     assertThat(out.toString()).isEqualTo(Text.ERROR.text(Text.INVALID_REGISTER.text("REG")) + getLineSeparator()
                                                  + Text.ERROR.text(Text.INVALID_NUMBER.text("0xXY"))
                                                  + getLineSeparator());
     out.reset();
 
     assertThat(UserInstruction.SET.execute(null, "12", "H")).isTrue();
     assertThat(Register.CPP.getValue()).isEqualTo(0xa1234);
     assertThat(out.toString()).isEqualTo(Text.ERROR.text(Text.INVALID_REGISTER.text("12")) + getLineSeparator()
                                                  + Text.ERROR.text(Text.INVALID_NUMBER.text("H")) + getLineSeparator());
     out.reset();
   }
 
   @Test
   public final void testExecuteMicroStep() throws IOException {
     printlnMethodName();
     assertThat(UserInstruction.MICRO_STEP.execute(this.processor)).isTrue();
     assertThat(Register.MAR.getValue()).isZero();
     assertThat(Register.PC.getValue()).isZero();
     assertThat(this.processor.isHaltInstruction()).isFalse();
     assertThat(out.toString()).isEqualTo(Text.TICKS.text(1) + getLineSeparator());
     out.reset();
 
     assertThat(UserInstruction.RESET.execute(this.processor)).isTrue();
 
     assertThat(UserInstruction.MICRO_STEP.execute(this.processor, (String[]) null)).isTrue();
     assertThat(Register.MAR.getValue()).isZero();
     assertThat(Register.PC.getValue()).isZero();
     assertThat(this.processor.isHaltInstruction()).isFalse();
     assertThat(out.toString()).isEqualTo(Text.TICKS.text(1) + getLineSeparator());
     out.reset();
 
     assertThat(UserInstruction.MICRO_STEP.execute(this.processor, "zwei")).isTrue();
     assertThat(Register.MAR.getValue()).isZero();
     assertThat(Register.PC.getValue()).isZero();
     assertThat(this.processor.isHaltInstruction()).isFalse();
     assertThat(out.toString()).isEqualTo(Text.ERROR.text(Text.INVALID_NUMBER.text("zwei")) + getLineSeparator());
     out.reset();
 
     assertThat(UserInstruction.MICRO_STEP.execute(this.processor, "1")).isTrue();
     assertThat(Register.MAR.getValue()).isZero();
     assertThat(Register.PC.getValue()).isZero();
     assertThat(Register.LV.getValue()).isEqualTo(-1);
     assertThat(Register.H.getValue()).isEqualTo(-1);
     assertThat(this.processor.isHaltInstruction()).isFalse();
     assertThat(out.toString()).isEqualTo(Text.TICKS.text(1) + getLineSeparator());
     out.reset();
 
     assertThat(UserInstruction.RESET.execute(this.processor)).isTrue();
 
     assertThat(UserInstruction.MICRO_STEP.execute(this.processor, "20")).isTrue();
     assertThat(Register.MDR.getValue()).isEqualTo('\n');
     assertThat(Register.MAR.getValue()).isEqualTo(-3);
     assertThat(Register.PC.getValue()).isEqualTo(3);
     assertThat(Register.LV.getValue()).isEqualTo(-2);
     assertThat(Register.H.getValue()).isEqualTo(-1);
     assertThat(this.processor.isHaltInstruction()).isTrue();
     assertThat(out.toString()).isEqualTo(Text.TICKS.text(14) + getLineSeparator());
     out.reset();
 
     assertThat(UserInstruction.MICRO_STEP.execute(this.processor, "1")).isTrue();
     assertThat(Register.MDR.getValue()).isEqualTo('\n');
     assertThat(Register.MAR.getValue()).isEqualTo(-3);
     assertThat(Register.PC.getValue()).isEqualTo(3);
     assertThat(Register.LV.getValue()).isEqualTo(-2);
     assertThat(Register.H.getValue()).isEqualTo(-1);
     assertThat(this.processor.isHaltInstruction()).isTrue();
     assertThat(out.toString()).isEmpty();
     out.reset();
 
     assertThat(UserInstruction.RESET.execute(this.processor)).isTrue();
 
     assertThat(UserInstruction.MICRO_STEP.execute(this.processor, "2_10")).isTrue();
     assertThat(Register.MAR.getValue()).isZero();
     assertThat(Register.PC.getValue()).isZero();
     assertThat(Register.LV.getValue()).isEqualTo(-1);
     assertThat(Register.H.getValue()).isEqualTo(-1);
     assertThat(this.processor.isHaltInstruction()).isFalse();
     assertThat(out.toString()).isEqualTo(Text.TICKS.text(2) + getLineSeparator());
   }
 
   @Test
   public void testExecuteMicroStep_WrongNumberOfParameters() {
     printlnMethodName();
     assertThatTwoParametersAreWrong(UserInstruction.MICRO_STEP, 0);
     assertThatThreeParametersAreWrong(UserInstruction.MICRO_STEP, 0);
   }
 
   @Test
   public final void testExecuteLsReg_NoArg() throws IOException {
     printlnMethodName();
     Register.MAR.setValue(1);
     Register.MDR.setValue(2);
     Register.PC.setValue(3);
     Register.MBR.setValue(4);
     Register.MBRU.setValue(5);
     Register.SP.setValue(6);
     Register.LV.setValue(7);
     Register.CPP.setValue(8);
     Register.TOS.setValue(9);
     Register.OPC.setValue(10);
     Register.H.setValue(11);
 
     assertThat(out.toString()).isEmpty();
     assertThat(UserInstruction.LS_REG.execute(this.processor, (String[]) null)).isTrue();
 
     assertThat(out.toString()).isEqualTo(Text.REGISTER_VALUE.text("MAR ", "0x1") + getLineSeparator()
                                                  + Text.REGISTER_VALUE.text("MDR ", "0x2") + getLineSeparator()
                                                  + Text.REGISTER_VALUE.text("PC  ", "0x3") + getLineSeparator()
                                                  + Text.REGISTER_VALUE.text("MBR ", "0x4") + getLineSeparator()
                                                  + Text.REGISTER_VALUE.text("MBRU", "0x5") + getLineSeparator()
                                                  + Text.REGISTER_VALUE.text("SP  ", "0x6") + getLineSeparator()
                                                  + Text.REGISTER_VALUE.text("LV  ", "0x7") + getLineSeparator()
                                                  + Text.REGISTER_VALUE.text("CPP ", "0x8") + getLineSeparator()
                                                  + Text.REGISTER_VALUE.text("TOS ", "0x9") + getLineSeparator()
                                                  + Text.REGISTER_VALUE.text("OPC ", "0xA") + getLineSeparator()
                                                  + Text.REGISTER_VALUE.text("H   ", "0xB") + getLineSeparator());
   }
 
   @Test
   public final void testExecuteLsReg_NullArg() throws IOException {
     printlnMethodName();
     assertThat(out.toString()).isEmpty();
     assertThat(UserInstruction.LS_REG.execute(this.processor, new String[] { null })).isTrue();
     assertThat(out.toString()).isEmpty();
   }
 
   @Test
   public final void testExecuteLsReg_FalseArg() throws IOException {
     printlnMethodName();
     assertThat(out.toString()).isEmpty();
     assertThat(UserInstruction.LS_REG.execute(this.processor, new String[] { "Bernd" })).isTrue();
     assertThat(out.toString()).isEqualTo(Text.ERROR.text(Text.INVALID_REGISTER.text("Bernd")) + getLineSeparator());
   }
 
   @Test
   public final void testExecuteLsReg_WrongNumberOfParameters() {
     printlnMethodName();
     assertThatTwoParametersAreWrong(UserInstruction.LS_REG, 0);
     assertThatThreeParametersAreWrong(UserInstruction.LS_REG, 0);
   }
 
   @Test
   public final void testExecuteLsReg_OneArg() throws IOException {
     printlnMethodName();
     Register.MAR.setValue(0x4711);
     assertThat(UserInstruction.LS_REG.execute(this.processor, new String[] { "MAR" })).isTrue();
     assertThat(out.toString()).isEqualTo(Text.REGISTER_VALUE.text("MAR ", "0x4711") + getLineSeparator());
     out.reset();
 
     Register.SP.setValue(-2);
     assertThat(UserInstruction.LS_REG.execute(this.processor, new String[] { "SP" })).isTrue();
     assertThat(out.toString()).isEqualTo(Text.REGISTER_VALUE.text("SP  ", "0xFFFFFFFE") + getLineSeparator());
     out.reset();
   }
 
   @Test
   public void testExecuteTraceReg() {
     printMethodName();
     for (final Register r : Register.values()) {
       assertThat(this.processor.isTracing(r)).isFalse();
       assertThat(UserInstruction.TRACE_REG.execute(this.processor, r.name())).isTrue();
       assertThat(this.processor.isTracing(r)).isTrue();
       printStep();
     }
     printEndOfMethod();
   }
 
   @Test
   public void testExecuteTraceReg_All() {
     printMethodName();
     for (final Register r : Register.values()) {
       assertThat(this.processor.isTracing(r)).isFalse();
       printStep();
     }
     printLoopEnd();
 
     assertThat(UserInstruction.TRACE_REG.execute(this.processor, (String[]) null)).isTrue();
 
     for (final Register r : Register.values()) {
       assertThat(this.processor.isTracing(r)).isTrue();
       printStep();
     }
     printEndOfMethod();
   }
 
   @Test
   public final void testExecuteTraceReg_WrongNumberOfParameters() {
     printlnMethodName();
     assertThatTwoParametersAreWrong(UserInstruction.TRACE_REG, 0);
     assertThatThreeParametersAreWrong(UserInstruction.TRACE_REG, 0);
   }
 
   @Test
   public final void testExecuteUntraceReg_WrongNumberOfParameters() {
     printlnMethodName();
     assertThatTwoParametersAreWrong(UserInstruction.UNTRACE_REG, 0);
     assertThatThreeParametersAreWrong(UserInstruction.UNTRACE_REG, 0);
   }
 
   @Test
   public void testExecuteUntraceReg_All() {
     printMethodName();
     assertThat(UserInstruction.TRACE_REG.execute(this.processor, (String[]) null)).isTrue();
 
     for (final Register r : Register.values()) {
       assertThat(this.processor.isTracing(r)).isTrue();
       printStep();
     }
     printLoopEnd();
 
     assertThat(UserInstruction.UNTRACE_REG.execute(this.processor, (String[]) null)).isTrue();
 
     for (final Register r : Register.values()) {
       assertThat(this.processor.isTracing(r)).isFalse();
       printStep();
     }
     printEndOfMethod();
   }
 
   @Test
   public void testExecuteTraceReg_Invalid() {
     printlnMethodName();
     assertThat(UserInstruction.TRACE_REG.execute(this.processor, "null")).isTrue();
     assertThat(out.toString()).isEqualTo(Text.ERROR.text(Text.INVALID_REGISTER.text("null")) + getLineSeparator());
   }
 
   @Test
   public void testExecuteUntraceReg_Invalid() {
     printlnMethodName();
     assertThat(UserInstruction.UNTRACE_REG.execute(this.processor, "null")).isTrue();
     assertThat(out.toString()).isEqualTo(Text.ERROR.text(Text.INVALID_REGISTER.text("null")) + getLineSeparator());
   }
 
   @Test
   public void testExecuteUntraceReg() {
     printMethodName();
     // trace all registers
     assertThat(UserInstruction.TRACE_REG.execute(this.processor, (String[]) null)).isTrue();
 
     for (final Register r : Register.values()) {
       assertThat(this.processor.isTracing(r)).isTrue();
       assertThat(UserInstruction.UNTRACE_REG.execute(this.processor, r.name())).isTrue();
       assertThat(this.processor.isTracing(r)).isFalse();
       printStep();
     }
     printEndOfMethod();
   }
 
   @Test
   public void testUpdateTracedRegisters() {
     printlnMethodName();
     assertThat(UserInstruction.TRACE_REG.execute(this.processor, Register.PC.name())).isTrue();
     assertThat(out.toString()).isEmpty();
     assertThat(UserInstruction.RUN.execute(this.processor, (String[]) null)).isTrue();
 
     assertThat(out.toString()).isEqualTo(Text.REGISTER_VALUE.text("PC  ", "0x0") + getLineSeparator()
                                                  + Text.REGISTER_VALUE.text("PC  ", "0x0") + getLineSeparator()
                                                  + Text.REGISTER_VALUE.text("PC  ", "0x0") + getLineSeparator()
                                                  + Text.REGISTER_VALUE.text("PC  ", "0x0") + getLineSeparator()
                                                  + Text.REGISTER_VALUE.text("PC  ", "0x1") + getLineSeparator()
                                                  + Text.REGISTER_VALUE.text("PC  ", "0x1") + getLineSeparator()
                                                  + Text.REGISTER_VALUE.text("PC  ", "0x1") + getLineSeparator()
                                                  + Text.REGISTER_VALUE.text("PC  ", "0x2") + getLineSeparator()
                                                  + Text.REGISTER_VALUE.text("PC  ", "0x2") + getLineSeparator()
                                                  + Text.REGISTER_VALUE.text("PC  ", "0x2") + getLineSeparator()
                                                  + Text.REGISTER_VALUE.text("PC  ", "0x3") + getLineSeparator()
                                                  + Text.REGISTER_VALUE.text("PC  ", "0x3") + getLineSeparator()
                                                  + Text.REGISTER_VALUE.text("PC  ", "0x3") + getLineSeparator()
                                                  + Text.REGISTER_VALUE.text("PC  ", "0x3") + getLineSeparator()
                                                  + Text.TICKS.text(14) + getLineSeparator());
   }
 
   @Test
   public final void testExecuteTraceMicro() throws IOException {
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
 
     assertThat(UserInstruction.RUN.execute(this.processor)).isTrue();
     assertThat(out.toString()).isEqualTo(Text.TICKS.text(14) + getLineSeparator());
     out.reset();
 
     assertThat(UserInstruction.RESET.execute(this.processor)).isTrue();
 
     assertThat(UserInstruction.TRACE_MIC.execute(this.processor)).isTrue();
     assertThat(UserInstruction.RUN.execute(this.processor)).isTrue();
     assertThat(out.toString()).isEqualTo(expected);
 
     assertThat(UserInstruction.RESET.execute(this.processor)).isTrue();
     out.reset();
 
     assertThat(UserInstruction.TRACE_MIC.execute(this.processor)).isTrue();
     assertThat(UserInstruction.MICRO_STEP.execute(this.processor)).isTrue();
     assertThat(out.toString()).isEqualTo(firstLine + Text.TICKS.text(1) + getLineSeparator());
     out.reset();
 
     assertThat(UserInstruction.UNTRACE_MIC.execute(this.processor)).isTrue();
     assertThat(UserInstruction.RUN.execute(this.processor)).isTrue();
     assertThat(out.toString()).isEqualTo(Text.TICKS.text(13) + getLineSeparator());
   }
 
   @Test
   public final void testExecuteTraceMacro() throws IOException {
     printlnMethodName();
     Input.setIn(new ByteArrayInputStream("2\n2\n2\n2\n".getBytes()));
 
     this.processor = new Mic1(ClassLoader.getSystemResourceAsStream("mic1/mic1ijvm.mic1"),
                               ClassLoader.getSystemResourceAsStream("mic1/add.ijvm"));
 
     final String firstLine = Text.EXECUTED_CODE.text("     0x0: [ 0x10] BIPUSH 0x0") + getLineSeparator();
     final String expected = firstLine + Text.EXECUTED_CODE.text("     0x2: [ 0x59] DUP") + getLineSeparator()
                             + Text.EXECUTED_CODE.text("     0x3: [ 0x36] ISTORE 0") + getLineSeparator()
                             + Text.EXECUTED_CODE.text("     0x5: [ 0x36] ISTORE 1") + getLineSeparator()
                             + Text.TICKS.text(24) + getLineSeparator();
 
     assertThat(UserInstruction.RUN.execute(this.processor)).isTrue();
     assertThat(out.toString()).isEqualTo(Text.INPUT_MIC1.text() + Text.INPUT_MIC1.text() + Text.TICKS.text(3292)
                                                  + getLineSeparator());
     out.reset();
 
     assertThat(UserInstruction.RESET.execute(this.processor)).isTrue();
 
     assertThat(UserInstruction.TRACE_MAC.execute(this.processor)).isTrue();
     assertThat(UserInstruction.STEP.execute(this.processor, "5")).isTrue();
     assertThat(out.toString()).isEqualTo(expected);
 
     assertThat(UserInstruction.RESET.execute(this.processor)).isTrue();
     out.reset();
 
     assertThat(UserInstruction.STEP.execute(this.processor, "0b10")).isTrue();
     assertThat(out.toString()).isEqualTo(firstLine + Text.TICKS.text(7) + getLineSeparator());
     out.reset();
 
     assertThat(UserInstruction.UNTRACE_MAC.execute(this.processor)).isTrue();
     assertThat(UserInstruction.RUN.execute(this.processor)).isTrue();
     assertThat(out.toString()).isEqualTo(Text.INPUT_MIC1.text() + Text.INPUT_MIC1.text() + Text.TICKS.text(3285)
                                                  + getLineSeparator());
 
     assertThat(UserInstruction.RESET.execute(this.processor)).isTrue();
     out.reset();
 
     assertThat(UserInstruction.TRACE_MAC.execute(this.processor)).isTrue();
     assertThat(UserInstruction.MICRO_STEP.execute(this.processor, "7")).isTrue();
     assertThat(out.toString()).isEqualTo(firstLine + Text.TICKS.text(7) + getLineSeparator());
     out.reset();
   }
 
   @Test
   public final void testExecuteSetMem_WrongNumberOfParameters() {
     printlnMethodName();
     assertThatNoParameterIsWrong(UserInstruction.SET_MEM, 2);
     assertThatOneParameterIsWrong(UserInstruction.SET_MEM);
     assertThatThreeParametersAreWrong(UserInstruction.SET_MEM, 2);
   }
 
   @Test
   public void testExecuteSetMem_Valid() {
     printlnMethodName();
     assertThat(UserInstruction.SET_MEM.execute(this.processor, "0xA", "14")).isTrue();
     assertThat(this.processor.getMemoryValue(0xa)).isEqualTo(14);
     assertThat(out.toString()).isEmpty();
 
     assertThat(UserInstruction.SET_MEM.execute(this.processor, "12", "0xF1")).isTrue();
     assertThat(this.processor.getMemoryValue(12)).isEqualTo(0xf1);
     assertThat(out.toString()).isEmpty();
 
     assertThat(UserInstruction.SET_MEM.execute(this.processor, "0b10", "0b1010")).isTrue();
     assertThat(this.processor.getMemoryValue(2)).isEqualTo(10);
     assertThat(out.toString()).isEmpty();
   }
 
   @Test
   public void testExecuteSetMem_Invalid() {
     printlnMethodName();
     assertThat(UserInstruction.SET_MEM.execute(this.processor, "abc", "14")).isTrue();
     assertThat(out.toString()).isEqualTo(Text.ERROR.text(Text.INVALID_NUMBER.text("abc")) + getLineSeparator());
     out.reset();
 
     assertThat(UserInstruction.SET_MEM.execute(this.processor, "", "14")).isTrue();
     assertThat(out.toString()).isEqualTo(Text.ERROR.text(Text.INVALID_NUMBER.text("")) + getLineSeparator());
     out.reset();
 
     assertThat(UserInstruction.SET_MEM.execute(this.processor, " h ", "-123414")).isTrue();
     assertThat(out.toString()).isEqualTo(Text.ERROR.text(Text.INVALID_NUMBER.text(" h ")) + getLineSeparator());
     out.reset();
 
     assertThat(UserInstruction.SET_MEM.execute(this.processor, "23", "0b14")).isTrue();
     assertThat(out.toString()).isEqualTo(Text.ERROR.text(Text.INVALID_NUMBER.text("0b14")) + getLineSeparator());
     out.reset();
 
     assertThat(UserInstruction.SET_MEM.execute(this.processor, "0", "0xXY")).isTrue();
     assertThat(out.toString()).isEqualTo(Text.ERROR.text(Text.INVALID_NUMBER.text("0xXY")) + getLineSeparator());
     out.reset();
 
     assertThat(UserInstruction.SET_MEM.execute(this.processor, "12", "H")).isTrue();
     assertThat(out.toString()).isEqualTo(Text.ERROR.text(Text.INVALID_NUMBER.text("H")) + getLineSeparator());
     out.reset();
 
     assertThat(UserInstruction.SET_MEM.execute(this.processor, "abc", "14_2")).isTrue();
     assertThat(out.toString()).isEqualTo(Text.ERROR.text(Text.INVALID_NUMBER.text("abc")) + getLineSeparator()
                                                  + Text.ERROR.text(Text.INVALID_NUMBER.text("14_2"))
                                                  + getLineSeparator());
     out.reset();
 
     assertThat(UserInstruction.SET_MEM.execute(this.processor, "REG", "0xXY")).isTrue();
     assertThat(out.toString()).isEqualTo(Text.ERROR.text(Text.INVALID_NUMBER.text("REG")) + getLineSeparator()
                                                  + Text.ERROR.text(Text.INVALID_NUMBER.text("0xXY"))
                                                  + getLineSeparator());
     out.reset();
 
     assertThat(UserInstruction.SET_MEM.execute(this.processor, "0X", "H")).isTrue();
     assertThat(out.toString()).isEqualTo(Text.ERROR.text(Text.INVALID_NUMBER.text("0X")) + getLineSeparator()
                                                  + Text.ERROR.text(Text.INVALID_NUMBER.text("H")) + getLineSeparator());
   }
 
   @Test
   public void testExecuteLsMacroCode_All() throws IOException {
     printlnMethodName();
     this.processor = new Mic1(ClassLoader.getSystemResourceAsStream("mic1/mic1ijvm.mic1"),
                               ClassLoader.getSystemResourceAsStream("mic1/add.ijvm"));
     final StringBuilder sb = readFile("mic1/add.ijvm.dis");
 
     assertThat(UserInstruction.LS_MACRO_CODE.execute(this.processor)).isTrue();
     assertThat(out.toString()).isEqualTo(sb.toString());
   }
 
   @Test
   public void testExecuteLsMacroCode_Part1() throws IOException {
     printlnMethodName();
     this.processor = new Mic1(ClassLoader.getSystemResourceAsStream("mic1/mic1ijvm.mic1"),
                               ClassLoader.getSystemResourceAsStream("mic1/add.ijvm"));
     final StringBuilder sb = readFile("mic1/add_part1.ijvm.dis");
 
     assertThat(UserInstruction.LS_MACRO_CODE.execute(this.processor, "0_2", "0x1d")).isTrue();
     assertThat(out.toString()).isEqualTo(sb.toString());
     out.reset();
 
     assertThat(UserInstruction.LS_MACRO_CODE.execute(this.processor, "-1", "0x1d")).isTrue();
 
     assertThat(out.toString()).isEqualTo(sb.toString());
     out.reset();
 
     assertThat(UserInstruction.LS_MACRO_CODE.execute(this.processor, "-20_19", "0x1e")).isTrue();
 
     assertThat(out.toString()).isEqualTo(sb.toString());
   }
 
   @Test
   public void testExecuteLsMacroCode_Around1() throws IOException {
     printlnMethodName();
     this.processor = new Mic1(ClassLoader.getSystemResourceAsStream("mic1/mic1ijvm.mic1"),
                               ClassLoader.getSystemResourceAsStream("mic1/add.ijvm"));
     final StringBuilder sb = readFile("mic1/add_part1.ijvm.dis");
 
     assertThat(UserInstruction.LS_MACRO_CODE.execute(this.processor, "0x1d")).isTrue();
     assertThat(out.toString()).isEqualTo(sb.toString());
     out.reset();
 
     assertThat(UserInstruction.MICRO_STEP.execute(this.processor, "8")).isTrue();
     assertThat(UserInstruction.LS_MACRO_CODE.execute(this.processor, "0x1b")).isTrue();
     assertThat(out.toString()).isEqualTo(Text.TICKS.text(8) + getLineSeparator() + sb.toString());
     out.reset();
 
     assertThat(UserInstruction.MICRO_STEP.execute(this.processor, "60")).isTrue();
     assertThat(UserInstruction.LS_MACRO_CODE.execute(this.processor, "16")).isTrue();
     assertThat(out.toString()).isEqualTo(Text.TICKS.text(60) + getLineSeparator() + sb.toString());
     out.reset();
 
     assertThat(UserInstruction.RESET.execute(this.processor)).isTrue();
     assertThat(UserInstruction.MICRO_STEP.execute(this.processor, "8")).isTrue();
     assertThat(UserInstruction.LS_MACRO_CODE.execute(this.processor, "0x1b")).isTrue();
     assertThat(out.toString()).isEqualTo(Text.TICKS.text(8) + getLineSeparator() + sb.toString());
   }
 
   @Test
   public void testExecuteLsMacroCode_Part2() throws IOException {
     printlnMethodName();
     this.processor = new Mic1(ClassLoader.getSystemResourceAsStream("mic1/mic1ijvm.mic1"),
                               ClassLoader.getSystemResourceAsStream("mic1/add.ijvm"));
     final StringBuilder sb = readFile("mic1/add_part2.ijvm.dis");
     assertThat(UserInstruction.LS_MACRO_CODE.execute(this.processor, "0x18", "0x2E")).isTrue();
 
     assertThat(out.toString()).isEqualTo(sb.toString());
     out.reset();
 
     assertThat(UserInstruction.LS_MACRO_CODE.execute(this.processor, "0x18", "0x2F")).isTrue();
 
     assertThat(out.toString()).isEqualTo(sb.toString());
     out.reset();
 
     assertThat(UserInstruction.LS_MACRO_CODE.execute(this.processor, "0x17", "0x2E")).isTrue();
     assertThat(out.toString()).isNotEqualTo(sb.toString());
     out.reset();
 
     assertThat(UserInstruction.LS_MACRO_CODE.execute(this.processor, "0x18", "0x30")).isTrue();
     assertThat(out.toString()).isNotEqualTo(sb.toString());
     out.reset();
   }
 
   @Test
   public void testExecuteLsMacroCode_Around2() throws IOException {
     printlnMethodName();
     Input.setIn(new ByteArrayInputStream("2\n2\n".getBytes()));
 
     this.processor = new Mic1(ClassLoader.getSystemResourceAsStream("mic1/mic1ijvm.mic1"),
                               ClassLoader.getSystemResourceAsStream("mic1/add.ijvm"));
     final StringBuilder sb = readFile("mic1/add_part2.ijvm.dis");
 
     assertThat(UserInstruction.MICRO_STEP.execute(this.processor, "537")).isTrue();
     assertThat(UserInstruction.LS_MACRO_CODE.execute(this.processor, "11")).isTrue();
     assertThat(out.toString()).isEqualTo(Text.INPUT_MIC1.text() + Text.INPUT_MIC1.text() + Text.TICKS.text(537)
                                                  + getLineSeparator() + sb.toString());
     out.reset();
 
     Input.setIn(new ByteArrayInputStream("2\n2\n".getBytes()));
     assertThat(UserInstruction.RESET.execute(this.processor)).isTrue();
     assertThat(UserInstruction.MICRO_STEP.execute(this.processor, "537")).isTrue();
     assertThat(UserInstruction.LS_MACRO_CODE.execute(this.processor, "11")).isTrue();
     assertThat(out.toString()).isEqualTo(Text.INPUT_MIC1.text() + Text.INPUT_MIC1.text() + Text.TICKS.text(537)
                                                  + getLineSeparator() + sb.toString());
     out.reset();
   }
 
   @Test
   public void testExecuteLsMacroCode_Part3() throws IOException {
     printlnMethodName();
     this.processor = new Mic1(ClassLoader.getSystemResourceAsStream("mic1/mic1ijvm.mic1"),
                               ClassLoader.getSystemResourceAsStream("mic1/add.ijvm"));
     final StringBuilder sb = readFile("mic1/add_part3.ijvm.dis");
 
     assertThat(UserInstruction.LS_MACRO_CODE.execute(this.processor, "0xff", "0x11D")).isTrue();
     assertThat(out.toString()).isEqualTo(sb.toString());
     out.reset();
 
     assertThat(UserInstruction.LS_MACRO_CODE.execute(this.processor, "0xff", "0x11E")).isTrue();
     assertThat(out.toString()).isEqualTo(sb.toString());
     out.reset();
 
     assertThat(UserInstruction.LS_MACRO_CODE.execute(this.processor, "0xff", "0xFE111D")).isTrue();
     assertThat(out.toString()).isEqualTo(sb.toString());
     out.reset();
 
     assertThat(UserInstruction.LS_MACRO_CODE.execute(this.processor, "0x100", "0x11D")).isTrue();
     assertThat(out.toString()).isNotEqualTo(sb.toString());
     out.reset();
   }
 
   @Test
   public void testExecuteLsMacroCode_Around() throws IOException {
     printlnMethodName();
     this.processor = new Mic1(ClassLoader.getSystemResourceAsStream("mic1/mic1ijvm.mic1"),
                               ClassLoader.getSystemResourceAsStream("mic1/add.ijvm"));
 
     assertThat(UserInstruction.MICRO_STEP.execute(this.processor, "8")).isTrue();
     assertThat(UserInstruction.LS_MACRO_CODE.execute(this.processor, "0")).isTrue();
     assertThat(out.toString()).isEqualTo(Text.TICKS.text(8) + getLineSeparator() + "     0x2: [ 0x59] DUP"
                                                  + getLineSeparator());
 
     out.reset();
     assertThat(UserInstruction.RESET.execute(this.processor)).isTrue();
     assertThat(UserInstruction.MICRO_STEP.execute(this.processor, "8")).isTrue();
     assertThat(UserInstruction.LS_MACRO_CODE.execute(this.processor, "0")).isTrue();
     assertThat(out.toString()).isEqualTo(Text.TICKS.text(8) + getLineSeparator() + "     0x2: [ 0x59] DUP"
                                                  + getLineSeparator());
   }
 
   @Test
   public void testExecuteBreak() {
     printlnMethodName();
     assertThat(UserInstruction.BREAK.execute(this.processor, Register.H.name(), "-1")).isTrue();
     assertThat(UserInstruction.RUN.execute(this.processor)).isTrue();
     assertThat(out.toString()).isEqualTo(Text.TICKS.text(2) + getLineSeparator());
 
     out.reset();
     assertThat(UserInstruction.RUN.execute(this.processor)).isTrue();
     assertThat(out.toString()).isEqualTo(Text.TICKS.text(1) + getLineSeparator());
 
     out.reset();
     assertThat(UserInstruction.RESET.execute(this.processor)).isTrue();
     assertThat(UserInstruction.MICRO_STEP.execute(this.processor)).isTrue();
     assertThat(out.toString()).isEqualTo(Text.TICKS.text(1) + getLineSeparator());
 
     out.reset();
     assertThat(UserInstruction.MICRO_STEP.execute(this.processor)).isTrue();
     assertThat(out.toString()).isEqualTo(Text.TICKS.text(1) + getLineSeparator());
 
     out.reset();
     assertThat(UserInstruction.MICRO_STEP.execute(this.processor)).isTrue();
     assertThat(out.toString()).isEqualTo(Text.TICKS.text(1) + getLineSeparator());
   }
 
   @Test
   public void testExecuteMicroBreak() {
     printlnMethodName();
     assertThat(UserInstruction.MICRO_BREAK.execute(this.processor, "0x2")).isTrue();
     assertThat(UserInstruction.MICRO_BREAK.execute(this.processor, "0x3")).isTrue();
     assertThat(UserInstruction.RUN.execute(this.processor)).isTrue();
     assertThat(out.toString()).isEqualTo(Text.TICKS.text(2) + getLineSeparator());
 
     out.reset();
     assertThat(UserInstruction.RUN.execute(this.processor)).isTrue();
     assertThat(out.toString()).isEqualTo(Text.TICKS.text(1) + getLineSeparator());
 
     out.reset();
     assertThat(UserInstruction.RESET.execute(this.processor)).isTrue();
     assertThat(UserInstruction.MICRO_STEP.execute(this.processor, "0002")).isTrue();
     assertThat(out.toString()).isEqualTo(Text.TICKS.text(2) + getLineSeparator());
 
     out.reset();
     assertThat(UserInstruction.MICRO_STEP.execute(this.processor, "02")).isTrue();
     assertThat(out.toString()).isEqualTo(Text.TICKS.text(1) + getLineSeparator());
 
     out.reset();
     assertThat(UserInstruction.RUN.execute(this.processor)).isTrue();
     assertThat(out.toString()).isEqualTo(Text.TICKS.text(11) + getLineSeparator());
   }
 
   @Test
   public void testExecuteMacroBreak() throws FileFormatException {
     printlnMethodName();
     this.processor = new Mic1(ClassLoader.getSystemResourceAsStream("mic1/mic1ijvm.mic1"),
                               ClassLoader.getSystemResourceAsStream("mic1/add.ijvm"));
     Input.setIn(new ByteArrayInputStream("2\n2\n".getBytes()));
 
     assertThat(UserInstruction.MACRO_BREAK.execute(this.processor, "0x2")).isTrue();
     assertThat(UserInstruction.MACRO_BREAK.execute(this.processor, "0x3")).isTrue();
     assertThat(UserInstruction.RUN.execute(this.processor)).isTrue();
     assertThat(out.toString()).isEqualTo(Text.TICKS.text(7) + getLineSeparator());
 
     out.reset();
     assertThat(UserInstruction.RUN.execute(this.processor)).isTrue();
     assertThat(out.toString()).isEqualTo(Text.TICKS.text(3) + getLineSeparator());
 
     out.reset();
     assertThat(UserInstruction.RESET.execute(this.processor)).isTrue();
     assertThat(UserInstruction.MICRO_STEP.execute(this.processor, "0O2")).isTrue();
     assertThat(out.toString()).isEqualTo(Text.TICKS.text(2) + getLineSeparator());
 
     out.reset();
     assertThat(UserInstruction.MICRO_STEP.execute(this.processor, "0XC")).isTrue();
     assertThat(out.toString()).isEqualTo(Text.TICKS.text(5) + getLineSeparator());
 
     out.reset();
     assertThat(UserInstruction.RUN.execute(this.processor)).isTrue();
     assertThat(out.toString()).isEqualTo(Text.TICKS.text(3) + getLineSeparator());
 
     out.reset();
     assertThat(UserInstruction.RUN.execute(this.processor)).isTrue();
     assertThat(out.toString()).isEqualTo(Text.INPUT_MIC1.text() + Text.INPUT_MIC1.text() + Text.TICKS.text(3282)
                                                  + getLineSeparator());
   }
 
   @Test
   public final void testExecuteBreak_WrongNumberOfParameters() {
     printlnMethodName();
     assertThatNoParameterIsWrong(UserInstruction.BREAK, 2);
     assertThatOneParameterIsWrong(UserInstruction.BREAK);
     assertThatThreeParametersAreWrong(UserInstruction.BREAK, 2);
   }
 
   @Test
   public final void testExecuteMicroBreak_WrongNumberOfParameters() {
     printlnMethodName();
     assertThatNoParameterIsWrong(UserInstruction.MICRO_BREAK, 1);
     assertThatTwoParametersAreWrong(UserInstruction.MICRO_BREAK, 1);
     assertThatThreeParametersAreWrong(UserInstruction.MICRO_BREAK, 1);
   }
 
   @Test
   public final void testExecuteMacroBreak_WrongNumberOfParameters() {
     printlnMethodName();
     assertThatNoParameterIsWrong(UserInstruction.MACRO_BREAK, 1);
     assertThatTwoParametersAreWrong(UserInstruction.MACRO_BREAK, 1);
     assertThatThreeParametersAreWrong(UserInstruction.MACRO_BREAK, 1);
   }
 
   @Test
   public void testExecuteRmBreak() {
     printlnMethodName();
     assertThat(UserInstruction.BREAK.execute(this.processor, Register.H.name(), "-1")).isTrue();
     assertThat(UserInstruction.RUN.execute(this.processor)).isTrue();
     assertThat(out.toString()).isEqualTo(Text.TICKS.text(2) + getLineSeparator());
     out.reset();
 
     assertThat(UserInstruction.RESET.execute(this.processor)).isTrue();
     assertThat(UserInstruction.RUN.execute(this.processor)).isTrue();
     assertThat(out.toString()).isEqualTo(Text.TICKS.text(2) + getLineSeparator());
     out.reset();
 
     assertThat(UserInstruction.LS_BREAK.execute(this.processor)).isTrue();
     final Matcher m = Pattern.compile(".*#([0-9]+).*" + getLineSeparator()).matcher(out.toString());
 
     assertThat(m.matches()).isTrue();
     assertThat(UserInstruction.RM_BREAK.execute(this.processor, m.group(1))).isTrue();
     out.reset();
 
     assertThat(UserInstruction.RESET.execute(this.processor)).isTrue();
     assertThat(UserInstruction.RUN.execute(this.processor)).isTrue();
     assertThat(out.toString()).isEqualTo(Text.TICKS.text(14) + getLineSeparator());
     out.reset();
   }
 
   @Test
   public final void testExecuteRmBreak_WrongNumberOfParameters() {
     printlnMethodName();
     assertThatNoParameterIsWrong(UserInstruction.RM_BREAK, 1);
     assertThatTwoParametersAreWrong(UserInstruction.RM_BREAK, 1);
     assertThatThreeParametersAreWrong(UserInstruction.RM_BREAK, 1);
   }
 
   @Test
   public final void testExecuteRmBreak_Invalid() {
     printlnMethodName();
 
     assertThat(UserInstruction.RM_BREAK.execute(this.processor, "AA")).isTrue();
     assertThat(out.toString()).isEqualTo(Text.ERROR.text(Text.INVALID_NUMBER.text("AA")) + getLineSeparator());
   }
 
   private void assertThatNoParameterIsWrong(final UserInstruction instr, final int expected) {
     assertThat(instr.execute(null)).isTrue();
     assertThatWrongNumberOfParametersIsPrintedAndResetOut(expected, 0);
 
     assertThat(instr.execute(null, (String[]) null)).isTrue();
     assertThatWrongNumberOfParametersIsPrintedAndResetOut(expected, 0);
 
     assertThat(instr.execute(null, new String[] {})).isTrue();
     assertThatWrongNumberOfParametersIsPrintedAndResetOut(expected, 0);
   }
 
   private void assertThatOneParameterIsWrong(final UserInstruction instr) {
     assertThat(instr.execute(null, new String[] { null })).isTrue();
     assertThatWrongNumberOfParametersIsPrintedAndResetOut(2, 1);
 
     assertThat(instr.execute(null, new String[] { "H" })).isTrue();
     assertThatWrongNumberOfParametersIsPrintedAndResetOut(2, 1);
 
     assertThat(instr.execute(null, new String[] { "2" })).isTrue();
     assertThatWrongNumberOfParametersIsPrintedAndResetOut(2, 1);
   }
 
   private void assertThatThreeParametersAreWrong(final UserInstruction instr, final int exp) {
     assertThat(instr.execute(null, new String[] { null, "asd", "" })).isTrue();
     assertThatWrongNumberOfParametersIsPrintedAndResetOut(exp, 3);
 
     assertThat(instr.execute(null, new String[] { "H", null, " " })).isTrue();
     assertThatWrongNumberOfParametersIsPrintedAndResetOut(exp, 3);
 
     assertThat(instr.execute(null, new String[] { "2", "\t", null })).isTrue();
     assertThatWrongNumberOfParametersIsPrintedAndResetOut(exp, 3);
   }
 
   private void assertThatTwoParametersAreWrong(final UserInstruction instr, final int expected) {
     assertThat(instr.execute(null, new String[] { null, "asd" })).isTrue();
     assertThatWrongNumberOfParametersIsPrintedAndResetOut(expected, 2);
 
     assertThat(instr.execute(null, new String[] { "H", " " })).isTrue();
     assertThatWrongNumberOfParametersIsPrintedAndResetOut(expected, 2);
 
     assertThat(instr.execute(null, new String[] { "2", null })).isTrue();
     assertThatWrongNumberOfParametersIsPrintedAndResetOut(expected, 2);
   }
 
   @Test
   public void testExecuteLsMacroCode_WrongNumberOfParameters() {
     printlnMethodName();
     assertThatThreeParametersAreWrong(UserInstruction.LS_MACRO_CODE, 2);
   }
 
   @Test
   public void testExecuteLsMacroCode_One_Invalid() {
     printlnMethodName();
     assertThat(UserInstruction.LS_MACRO_CODE.execute(this.processor, "1x")).isTrue();
     assertThat(out.toString()).isEqualTo(Text.ERROR.text(Text.INVALID_NUMBER.text("1x")) + getLineSeparator());
   }
 
   @Test
   public void testExecuteLsMacroCode_Two_InvalidBoth() {
     printlnMethodName();
     assertThat(UserInstruction.LS_MACRO_CODE.execute(this.processor, "1x", "0x")).isTrue();
     assertThat(out.toString())
       .isEqualTo(Text.ERROR.text(Text.INVALID_NUMBER.text("1x")) + getLineSeparator()
                          + Text.ERROR.text(Text.INVALID_NUMBER.text("0x")) + getLineSeparator());
   }
 
   @Test
   public void testExecuteLsMacroCode_Two_Invalid1() {
     printlnMethodName();
     assertThat(UserInstruction.LS_MACRO_CODE.execute(this.processor, "1x", "0")).isTrue();
     assertThat(out.toString()).isEqualTo(Text.ERROR.text(Text.INVALID_NUMBER.text("1x")) + getLineSeparator());
   }
 
   @Test
   public void testExecuteLsMacroCode_Two_Invalid2() {
     printlnMethodName();
     assertThat(UserInstruction.LS_MACRO_CODE.execute(this.processor, "0", "1x")).isTrue();
     assertThat(out.toString()).isEqualTo(Text.ERROR.text(Text.INVALID_NUMBER.text("1x")) + getLineSeparator());
   }
 
   @Test
   public void testExecuteLsBreak() {
     printlnMethodName();
 
     assertThat(UserInstruction.BREAK.execute(this.processor, "MBRU", "16")).isTrue();
     assertThat(UserInstruction.BREAK.execute(this.processor, "MBRU", "-48")).isTrue();
 
     assertThat(UserInstruction.BREAK.execute(this.processor, "CPP", "-1")).isTrue();
     assertThat(UserInstruction.BREAK.execute(this.processor, "CPP", "0x7FfFfFfF")).isTrue();
     assertThat(UserInstruction.BREAK.execute(this.processor, "CPP", "-2147483648")).isTrue();
 
     assertThat(UserInstruction.BREAK.execute(this.processor, "H", "2")).isTrue();
     assertThat(UserInstruction.BREAK.execute(this.processor, "H", "0x2")).isTrue();
     assertThat(UserInstruction.BREAK.execute(this.processor, "H", "11_2")).isTrue();
     assertThat(UserInstruction.BREAK.execute(this.processor, "H", "1")).isTrue();
 
     assertThat(out.toString()).isEmpty();
     assertThat(UserInstruction.LS_BREAK.execute(this.processor)).isTrue();
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
   }
 
   @Test
   public void testExecuteLsMem() throws FileFormatException {
     printlnMethodName();
     this.processor = new Mic1(ClassLoader.getSystemResourceAsStream("mic1/mic1ijvm.mic1"),
                               ClassLoader.getSystemResourceAsStream("mic1/test.ijvm"));
 
     assertThat(UserInstruction.LS_MEM.execute(this.processor, "0x0", "0b1")).isTrue();
     assertThat(out.toString()).isEqualTo(Text.MEMORY_CONTENT.text("     0x0", "0x10203") + getLineSeparator()
                                                  + Text.MEMORY_CONTENT.text("     0x1", "0x4050607")
                                                  + getLineSeparator());
     out.reset();
 
     assertThat(UserInstruction.LS_MEM.execute(this.processor, "0x1", "0o0")).isTrue();
     assertThat(out.toString()).isEqualTo(Text.MEMORY_CONTENT.text("     0x0", "0x10203") + getLineSeparator()
                                                  + Text.MEMORY_CONTENT.text("     0x1", "0x4050607")
                                                  + getLineSeparator());
     out.reset();
 
     assertThat(UserInstruction.LS_MEM.execute(this.processor, "0x0", "0b0")).isTrue();
     assertThat(out.toString()).isEqualTo(Text.MEMORY_CONTENT.text("     0x0", "0x10203") + getLineSeparator());
     out.reset();
 
     assertThat(UserInstruction.LS_MEM.execute(this.processor, "2", "-13")).isTrue();
     assertThat(out.toString()).isEqualTo(Text.MEMORY_CONTENT.text("     0x0", "0x10203") + getLineSeparator()
                                                  + Text.MEMORY_CONTENT.text("     0x1", "0x4050607")
                                                  + getLineSeparator()
                                                  + Text.MEMORY_CONTENT.text("     0x2", "0x8090A0B")
                                                  + getLineSeparator());
     out.reset();
 
     assertThat(UserInstruction.LS_MEM.execute(this.processor, "0x3", "0b1")).isTrue();
     assertThat(out.toString()).isEqualTo(Text.MEMORY_CONTENT.text("     0x1", "0x4050607") + getLineSeparator()
                                                  + Text.MEMORY_CONTENT.text("     0x2", "0x8090A0B")
                                                  + getLineSeparator()
                                                  + Text.MEMORY_CONTENT.text("     0x3", "0xC0D0E0F")
                                                  + getLineSeparator());
     out.reset();
 
     assertThat(UserInstruction.LS_MEM.execute(this.processor, "0x7F", "0x7C")).isTrue();
     assertThat(out.toString()).isEqualTo(Text.MEMORY_CONTENT.text("    0x7C", "0x0") + getLineSeparator()
                                                  + Text.MEMORY_CONTENT.text("    0x7D", "0x0") + getLineSeparator()
                                                  + Text.MEMORY_CONTENT.text("    0x7E", "0x0") + getLineSeparator()
                                                  + Text.MEMORY_CONTENT.text("    0x7F", "0x0") + getLineSeparator());
     out.reset();
   }
 
   @Test
   public final void testExecuteLsMem_WrongNumberOfParameters() {
     printlnMethodName();
     assertThatNoParameterIsWrong(UserInstruction.LS_MEM, 2);
     assertThatOneParameterIsWrong(UserInstruction.LS_MEM);
     assertThatThreeParametersAreWrong(UserInstruction.LS_MEM, 2);
   }
 
   @Test
   public final void testExecuteLsMem_Invalid() {
     printlnMethodName();
     assertThat(UserInstruction.LS_MEM.execute(this.processor, "0x", "0x7C")).isTrue();
     assertThat(out.toString()).isEqualTo(Text.ERROR.text(Text.INVALID_NUMBER.text("0x")) + getLineSeparator());
     out.reset();
 
     assertThat(UserInstruction.LS_MEM.execute(this.processor, "0x7F", "0x")).isTrue();
     assertThat(out.toString()).isEqualTo(Text.ERROR.text(Text.INVALID_NUMBER.text("0x")) + getLineSeparator());
     out.reset();
 
     assertThat(UserInstruction.LS_MEM.execute(this.processor, "1x", "0x")).isTrue();
     assertThat(out.toString())
       .isEqualTo(Text.ERROR.text(Text.INVALID_NUMBER.text("1x")) + getLineSeparator()
                          + Text.ERROR.text(Text.INVALID_NUMBER.text("0x")) + getLineSeparator());
     out.reset();
   }
 
   @Test
   public void testExecuteStep_Invalid() {
     printlnMethodName();
     assertThat(UserInstruction.STEP.execute(this.processor, "null")).isTrue();
     assertThat(out.toString()).isEqualTo(Text.ERROR.text(Text.INVALID_NUMBER.text("null")) + getLineSeparator());
   }
 
   @Test
   public void testExecuteStep() throws FileFormatException {
     printlnMethodName();
     this.processor = new Mic1(ClassLoader.getSystemResourceAsStream("mic1/mic1ijvm.mic1"),
                               ClassLoader.getSystemResourceAsStream("mic1/add.ijvm"));
     assertThat(Register.PC.getValue()).isEqualTo(Settings.MIC1_REGISTER_PC_DEFVAL.getValue());
 
     assertThat(UserInstruction.STEP.execute(this.processor)).isTrue();
     assertThat(Register.PC.getValue()).isEqualTo(0);
     assertTicksDoneAndResetPrintStream(3);
 
     assertThat(UserInstruction.STEP.execute(this.processor)).isTrue();
     assertThat(Register.PC.getValue()).isEqualTo(2);
     assertTicksDoneAndResetPrintStream(4);
 
     assertThat(UserInstruction.STEP.execute(this.processor)).isTrue();
     assertThat(Register.PC.getValue()).isEqualTo(3);
     assertTicksDoneAndResetPrintStream(3);
 
     assertThat(UserInstruction.STEP.execute(this.processor)).isTrue();
     assertThat(Register.PC.getValue()).isEqualTo(5);
     assertTicksDoneAndResetPrintStream(7);
 
     assertThat(UserInstruction.STEP.execute(this.processor)).isTrue();
     assertThat(Register.PC.getValue()).isEqualTo(7);
     assertTicksDoneAndResetPrintStream(7);
 
     assertThat(UserInstruction.STEP.execute(this.processor)).isTrue();
     assertThat(Register.PC.getValue()).isEqualTo(9);
     assertTicksDoneAndResetPrintStream(4);
 
     assertThat(UserInstruction.STEP.execute(this.processor)).isTrue();
     assertThat(Register.PC.getValue()).isEqualTo(10);
     assertTicksDoneAndResetPrintStream(9);
 
     assertThat(UserInstruction.STEP.execute(this.processor)).isTrue();
     assertThat(Register.PC.getValue()).isEqualTo(13);
     assertTicksDoneAndResetPrintStream(8);
 
     assertThat(UserInstruction.STEP.execute(this.processor)).isTrue();
     assertThat(Register.PC.getValue()).isEqualTo(71);
     assertTicksDoneAndResetPrintStream(23);
   }
 
   @Test
   public void testExecuteStepN() throws FileFormatException {
     printlnMethodName();
     this.processor = new Mic1(ClassLoader.getSystemResourceAsStream("mic1/mic1ijvm.mic1"),
                               ClassLoader.getSystemResourceAsStream("mic1/add.ijvm"));
     Input.setIn(new ByteArrayInputStream("2\n2\n".getBytes()));
     Output.setBuffered(true);
     Output.setOut(new PrintStream(out));
     assertThat(Register.PC.getValue()).isEqualTo(Settings.MIC1_REGISTER_PC_DEFVAL.getValue());
 
     assertThat(UserInstruction.STEP.execute(this.processor, "0")).isTrue();
     assertThat(Register.PC.getValue()).isEqualTo(Settings.MIC1_REGISTER_PC_DEFVAL.getValue());
     assertThat(out.toString()).isEmpty();
 
     assertThat(UserInstruction.STEP.execute(this.processor, "1")).isTrue();
     assertThat(Register.PC.getValue()).isEqualTo(0);
     assertTicksDoneAndResetPrintStream(3);
 
     assertThat(UserInstruction.STEP.execute(this.processor, "2")).isTrue();
     assertThat(Register.PC.getValue()).isEqualTo(3);
     assertTicksDoneAndResetPrintStream(7);
 
     assertThat(UserInstruction.STEP.execute(this.processor, "3")).isTrue();
     assertThat(Register.PC.getValue()).isEqualTo(9);
     assertTicksDoneAndResetPrintStream(18);
 
     assertThat(UserInstruction.STEP.execute(this.processor, "-1")).isTrue();
     assertThat(Register.PC.getValue()).isEqualTo(9);
     assertThat(out.toString()).isEmpty();
 
     assertThat(UserInstruction.STEP.execute(this.processor, "0")).isTrue();
     assertThat(Register.PC.getValue()).isEqualTo(9);
     assertThat(out.toString()).isEmpty();
 
     assertThat(UserInstruction.STEP.execute(this.processor, "1")).isTrue();
     assertThat(Register.PC.getValue()).isEqualTo(10);
     assertTicksDoneAndResetPrintStream(9);
 
     assertThat(UserInstruction.STEP.execute(this.processor, "2")).isTrue();
     assertThat(Register.PC.getValue()).isEqualTo(71);
     assertTicksDoneAndResetPrintStream(31);
 
     assertThat(UserInstruction.STEP.execute(this.processor, "560")).isTrue();
     assertThat(Register.PC.getValue()).isEqualTo(0x11D);
    assertThat(out.toString()).isEqualTo(Text.INPUT_MIC1.text() + " 2" + getLineSeparator() + Text.INPUT_MIC1.text()
                                                 + "+2" + getLineSeparator() + "========" + getLineSeparator()
                                                 + "00000004" + getLineSeparator() + Text.TICKS.text(3213)
                                                 + getLineSeparator());
     out.reset();
 
     assertThat(UserInstruction.STEP.execute(this.processor, "560")).isTrue();
     assertThat(Register.PC.getValue()).isEqualTo(0x41);
     assertTicksDoneAndResetPrintStream(11);
 
     assertThat(UserInstruction.STEP.execute(this.processor, "0x7fe5")).isTrue();
     assertThat(Register.PC.getValue()).isEqualTo(0x41);
     assertThat(out.toString()).isEmpty();
 
     Output.setOut(System.out);
   }
 
   @Test
   public void testExecuteStep_WrongNumberOfParameters() {
     printlnMethodName();
     assertThatTwoParametersAreWrong(UserInstruction.STEP, 0);
     assertThatThreeParametersAreWrong(UserInstruction.STEP, 0);
   }
 
   @Test
   public void testExecuteLsMicroCode_WrongNumberOfParameters() {
     printlnMethodName();
     assertThatThreeParametersAreWrong(UserInstruction.LS_MICRO_CODE, 2);
   }
 
   @Test
   public void testExecuteLsMicroCode_One_Invalid() {
     printlnMethodName();
     assertThat(UserInstruction.LS_MICRO_CODE.execute(this.processor, "1x")).isTrue();
     assertThat(out.toString()).isEqualTo(Text.ERROR.text(Text.INVALID_NUMBER.text("1x")) + getLineSeparator());
   }
 
   @Test
   public void testExecuteLsMicroCode_Two_InvalidBoth() {
     printlnMethodName();
     assertThat(UserInstruction.LS_MICRO_CODE.execute(this.processor, "1x", "0x")).isTrue();
     assertThat(out.toString())
       .isEqualTo(Text.ERROR.text(Text.INVALID_NUMBER.text("1x")) + getLineSeparator()
                          + Text.ERROR.text(Text.INVALID_NUMBER.text("0x")) + getLineSeparator());
   }
 
   @Test
   public void testExecuteLsMicroCode_Two_Invalid1() {
     printlnMethodName();
     assertThat(UserInstruction.LS_MICRO_CODE.execute(this.processor, "1x", "0")).isTrue();
     assertThat(out.toString()).isEqualTo(Text.ERROR.text(Text.INVALID_NUMBER.text("1x")) + getLineSeparator());
   }
 
   @Test
   public void testExecuteLsMicroCode_Two_Invalid2() {
     printlnMethodName();
     assertThat(UserInstruction.LS_MICRO_CODE.execute(this.processor, "0", "1x")).isTrue();
     assertThat(out.toString()).isEqualTo(Text.ERROR.text(Text.INVALID_NUMBER.text("1x")) + getLineSeparator());
   }
 
   @Test
   public void testExecuteLsMicroCode_PrintCode_All() throws IOException {
     printlnMethodName();
     this.processor = new Mic1(ClassLoader.getSystemResourceAsStream("mic1/mic1ijvm.mic1"),
                               ClassLoader.getSystemResourceAsStream("mic1/add.ijvm"));
 
     assertThat(UserInstruction.LS_MICRO_CODE.execute(this.processor)).isTrue();
     assertThat(out.toString()).isEqualTo(readFile("mic1/mic1ijvm.mic1.dis").toString());
   }
 
   @Test
   public void testExecuteLsMicroCode_PrintCode_Part1() throws IOException {
     printlnMethodName();
     this.processor = new Mic1(ClassLoader.getSystemResourceAsStream("mic1/mic1ijvm.mic1"),
                               ClassLoader.getSystemResourceAsStream("mic1/add.ijvm"));
 
     final String expected = readFile("mic1/mic1ijvm_part1.mic1.dis").toString();
     final int end = 0x20;
 
     assertThat(UserInstruction.LS_MICRO_CODE.execute(this.processor, "0", String.valueOf(end))).isTrue();
     assertThat(out.toString()).isEqualTo(expected);
     out.reset();
 
     assertThat(UserInstruction.LS_MICRO_CODE.execute(this.processor, String.valueOf(end), "0")).isTrue();
     assertThat(out.toString()).isEqualTo(expected);
     out.reset();
 
     assertThat(UserInstruction.LS_MICRO_CODE.execute(this.processor, String.valueOf(end), "-42")).isTrue();
     assertThat(out.toString()).isEqualTo(expected);
     out.reset();
 
     assertThat(UserInstruction.LS_MICRO_CODE.execute(this.processor, "-42", String.valueOf(end))).isTrue();
     assertThat(out.toString()).isEqualTo(expected);
     out.reset();
   }
 
   @Test
   public void testExecuteLsMicroCode_PrintCode_Part2() throws IOException {
     printlnMethodName();
     this.processor = new Mic1(ClassLoader.getSystemResourceAsStream("mic1/mic1ijvm.mic1"),
                               ClassLoader.getSystemResourceAsStream("mic1/add.ijvm"));
 
     final String expected = readFile("mic1/mic1ijvm_part2.mic1.dis").toString();
 
     assertThat(UserInstruction.LS_MICRO_CODE.execute(this.processor, "0x35", "0x63")).isTrue();
     assertThat(out.toString()).isEqualTo(expected);
     out.reset();
 
     assertThat(UserInstruction.LS_MICRO_CODE.execute(this.processor, "0x63", "0x35")).isTrue();
     assertThat(out.toString()).isEqualTo(expected);
     out.reset();
   }
 
   @Test
   public void testExecuteLsMicroCode_PrintCode_Part3() throws IOException {
     printlnMethodName();
     this.processor = new Mic1(ClassLoader.getSystemResourceAsStream("mic1/mic1ijvm.mic1"),
                               ClassLoader.getSystemResourceAsStream("mic1/add.ijvm"));
 
     final String expected = readFile("mic1/mic1ijvm_part3.mic1.dis").toString();
     final int start = 0x87;
 
     assertThat(UserInstruction.LS_MICRO_CODE.execute(this.processor, String.valueOf(start), "0x1fF")).isTrue();
     assertThat(out.toString()).isEqualTo(expected);
     out.reset();
 
     assertThat(UserInstruction.LS_MICRO_CODE.execute(this.processor, "0x1FF", String.valueOf(start))).isTrue();
     assertThat(out.toString()).isEqualTo(expected);
     out.reset();
 
     assertThat(UserInstruction.LS_MICRO_CODE.execute(this.processor, String.valueOf(start), "4711")).isTrue();
     assertThat(out.toString()).isEqualTo(expected);
     out.reset();
 
     assertThat(UserInstruction.LS_MICRO_CODE.execute(this.processor, "0x4711", String.valueOf(start))).isTrue();
     assertThat(out.toString()).isEqualTo(expected);
     out.reset();
   }
 
   @Test
   public void testExecuteLsMicroCode_PrintCode_Around_Part1() throws IOException {
     printlnMethodName();
     this.processor = new Mic1(ClassLoader.getSystemResourceAsStream("mic1/mic1ijvm.mic1"),
                               ClassLoader.getSystemResourceAsStream("mic1/add.ijvm"));
 
     final String expected = readFile("mic1/mic1ijvm_part1.mic1.dis").toString();
 
     assertThat(UserInstruction.MICRO_STEP.execute(this.processor, "5")).isTrue();
     out.reset();
     assertThat(UserInstruction.LS_MICRO_CODE.execute(this.processor, "0x10")).isTrue();
     assertThat(out.toString()).isEqualTo(expected);
     out.reset();
 
     assertThat(UserInstruction.RESET.execute(this.processor)).isTrue();
 
     assertThat(UserInstruction.LS_MICRO_CODE.execute(this.processor, "0x20")).isTrue();
     assertThat(out.toString()).isEqualTo(expected);
     out.reset();
   }
 
   @Test
   public void testExecuteLsMicroCode_PrintCode_Around_Part2() throws IOException {
     printlnMethodName();
     this.processor = new Mic1(ClassLoader.getSystemResourceAsStream("mic1/mic1ijvm.mic1"),
                               ClassLoader.getSystemResourceAsStream("mic1/add.ijvm"));
 
     final String expected = readFile("mic1/mic1ijvm_part2.mic1.dis").toString();
 
     UserInstruction.MICRO_STEP.execute(this.processor, "7");
     out.reset();
     UserInstruction.LS_MICRO_CODE.execute(this.processor, "0x4C");
     assertThat(out.toString()).isNotEqualTo(expected);
     out.reset();
   }
 
   @Test
   public void testExecuteLsMicroCode_PrintCode_Hi() throws IOException {
     printlnMethodName();
 
     assertThat(UserInstruction.LS_MICRO_CODE.execute(this.processor)).isTrue();
     assertThat(out.toString()).isEqualTo(readFile("mic1/hi.mic1.dis").toString());
     out.reset();
 
     assertThat(UserInstruction.LS_MICRO_CODE.execute(this.processor, "0x10", "0x20")).isTrue();
     assertThat(out.toString()).isEmpty();
   }
 
   @Test
   public void testExecuteLsMicroCode_PrintCode_WithNullValues() throws IOException {
     printlnMethodName();
     this.processor = new Mic1(ClassLoader.getSystemResourceAsStream("mic1/hi-with-null.mic1"),
                               ClassLoader.getSystemResourceAsStream("mic1/hi.ijvm"));
 
     assertThat(UserInstruction.LS_MICRO_CODE.execute(this.processor)).isTrue();
     assertThat(out.toString()).isEqualTo(readFile("mic1/hi-with-null.mic1.dis").toString());
   }
 
   @Test
   public void testExecuteLsStack_Add() throws FileFormatException {
     printlnMethodName();
     Input.setIn(new ByteArrayInputStream("2\n2\n2\n2\n".getBytes()));
 
     this.processor = new Mic1(ClassLoader.getSystemResourceAsStream("mic1/mic1ijvm.mic1"),
                               ClassLoader.getSystemResourceAsStream("mic1/add.ijvm"));
     assertThat(UserInstruction.LS_STACK.execute(this.processor)).isTrue();
     assertThat(out.toString()).isEqualTo(Text.STACK_CONTENT.text(0, "  0xC000", "0x0") + getLineSeparator());
 
     assertThat(UserInstruction.STEP.execute(this.processor, "15")).isTrue();
     out.reset();
     assertThat(UserInstruction.LS_STACK.execute(this.processor)).isTrue();
     assertThat(out.toString()).isEqualTo(Text.STACK_CONTENT.text(0, "  0xC000", "0x0") + getLineSeparator()
                                                  + Text.STACK_CONTENT.text(1, "  0xC001", "0xC003")
                                                  + getLineSeparator() + Text.STACK_CONTENT.text(2, "  0xC002", "0x0")
                                                  + getLineSeparator() + Text.STACK_CONTENT.text(3, "  0xC003", "0x10")
                                                  + getLineSeparator()
                                                  + Text.STACK_CONTENT.text(4, "  0xC004", "0x8000")
                                                  + getLineSeparator() + Text.STACK_CONTENT.text(5, "  0xC005", "0x32")
                                                  + getLineSeparator());
 
     assertThat(UserInstruction.STEP.execute(this.processor)).isTrue();
     out.reset();
     assertThat(UserInstruction.LS_STACK.execute(this.processor)).isTrue();
     assertThat(out.toString()).isEqualTo(Text.STACK_CONTENT.text(0, "  0xC000", "0x0") + getLineSeparator()
                                                  + Text.STACK_CONTENT.text(1, "  0xC001", "0xC003")
                                                  + getLineSeparator() + Text.STACK_CONTENT.text(2, "  0xC002", "0x0")
                                                  + getLineSeparator() + Text.STACK_CONTENT.text(3, "  0xC003", "0x10")
                                                  + getLineSeparator()
                                                  + Text.STACK_CONTENT.text(4, "  0xC004", "0x8000")
                                                  + getLineSeparator() + Text.STACK_CONTENT.text(5, "  0xC005", "0x32")
                                                  + getLineSeparator() + Text.STACK_CONTENT.text(6, "  0xC006", "0x32")
                                                  + getLineSeparator());
 
     assertThat(UserInstruction.STEP.execute(this.processor)).isTrue();
     out.reset();
     assertThat(UserInstruction.LS_STACK.execute(this.processor)).isTrue();
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
 
     assertThat(UserInstruction.STEP.execute(this.processor)).isTrue();
     out.reset();
     assertThat(UserInstruction.LS_STACK.execute(this.processor)).isTrue();
     assertThat(out.toString()).isEqualTo(Text.STACK_CONTENT.text(0, "  0xC000", "0x0") + getLineSeparator()
                                                  + Text.STACK_CONTENT.text(1, "  0xC001", "0xC003")
                                                  + getLineSeparator() + Text.STACK_CONTENT.text(2, "  0xC002", "0x0")
                                                  + getLineSeparator() + Text.STACK_CONTENT.text(3, "  0xC003", "0x10")
                                                  + getLineSeparator()
                                                  + Text.STACK_CONTENT.text(4, "  0xC004", "0x8000")
                                                  + getLineSeparator() + Text.STACK_CONTENT.text(5, "  0xC005", "0x32")
                                                  + getLineSeparator() + Text.STACK_CONTENT.text(6, "  0xC006", "0x2")
                                                  + getLineSeparator());
 
     assertThat(UserInstruction.STEP.execute(this.processor)).isTrue();
     out.reset();
     assertThat(UserInstruction.LS_STACK.execute(this.processor)).isTrue();
     assertThat(out.toString()).isEqualTo(Text.STACK_CONTENT.text(0, "  0xC000", "0x0") + getLineSeparator()
                                                  + Text.STACK_CONTENT.text(1, "  0xC001", "0xC003")
                                                  + getLineSeparator() + Text.STACK_CONTENT.text(2, "  0xC002", "0x0")
                                                  + getLineSeparator() + Text.STACK_CONTENT.text(3, "  0xC003", "0x10")
                                                  + getLineSeparator()
                                                  + Text.STACK_CONTENT.text(4, "  0xC004", "0x8000")
                                                  + getLineSeparator() + Text.STACK_CONTENT.text(5, "  0xC005", "0x32")
                                                  + getLineSeparator());
 
     assertThat(UserInstruction.STEP.execute(this.processor)).isTrue();
     out.reset();
     assertThat(UserInstruction.LS_STACK.execute(this.processor)).isTrue();
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
