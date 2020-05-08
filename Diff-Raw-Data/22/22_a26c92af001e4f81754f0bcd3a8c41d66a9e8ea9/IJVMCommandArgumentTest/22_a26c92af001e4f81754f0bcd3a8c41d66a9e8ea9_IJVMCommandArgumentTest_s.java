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
 package com.github.croesch.micro_debug.mic1.mem;
 
 import static org.fest.assertions.Assertions.assertThat;
 
 import org.junit.Test;
 
 import com.github.croesch.micro_debug.DefaultTestCase;
 import com.github.croesch.micro_debug.error.FileFormatException;
 import com.github.croesch.micro_debug.mic1.register.Register;
 import com.github.croesch.micro_debug.settings.Settings;
 
 /**
  * Provides test cases for {@link IJVMCommandArgument}.
  * 
  * @author croesch
  * @since Date: Jan 23, 2012
  */
 public class IJVMCommandArgumentTest extends DefaultTestCase {
 
   private Memory mem;
 
   @Override
   protected void setUpDetails() throws FileFormatException {
     this.mem = new Memory(Settings.MIC1_MEM_MACRO_MAXSIZE.getValue(),
                           ClassLoader.getSystemResourceAsStream("mic1/test.ijvm"));
   }
 
   @Test
   public void testGetNumberOfBytes() {
     assertThat(IJVMCommandArgument.BYTE.getNumberOfBytes()).isEqualTo(1);
     assertThat(IJVMCommandArgument.CONST.getNumberOfBytes()).isEqualTo(1);
     assertThat(IJVMCommandArgument.INDEX.getNumberOfBytes()).isEqualTo(2);
     assertThat(IJVMCommandArgument.LABEL.getNumberOfBytes()).isEqualTo(2);
     assertThat(IJVMCommandArgument.OFFSET.getNumberOfBytes()).isEqualTo(2);
     assertThat(IJVMCommandArgument.VARNUM.getNumberOfBytes()).isEqualTo(1);
   }
 
   @Test
   public void testGetRepresentationOfArgument() {
     assertThat(IJVMCommandArgument.BYTE.getRepresentationOfArgument(-1, 0, this.mem)).isEqualTo("0x0");
     assertThat(IJVMCommandArgument.BYTE.getRepresentationOfArgument(1, 127, this.mem)).isEqualTo("0x7F");
     assertThat(IJVMCommandArgument.BYTE.getRepresentationOfArgument(42, -127, this.mem)).isEqualTo("0x81");
     assertThat(IJVMCommandArgument.BYTE.getRepresentationOfArgument(-42, 12, this.mem)).isEqualTo("0xC");
     assertThat(IJVMCommandArgument.BYTE.getRepresentationOfArgument(0, -12, this.mem)).isEqualTo("0xF4");
     assertThat(IJVMCommandArgument.BYTE.getRepresentationOfArgument(0, 0xF4, this.mem)).isEqualTo("0xF4");
     assertThat(IJVMCommandArgument.BYTE.getRepresentationOfArgument(0, 258, this.mem)).isEqualTo("0x2");
     assertThat(IJVMCommandArgument.BYTE.getRepresentationOfArgument(0, 130, this.mem)).isEqualTo("0x82");
     assertThat(IJVMCommandArgument.BYTE.getRepresentationOfArgument(0, -130, this.mem)).isEqualTo("0x7E");
 
     assertThat(IJVMCommandArgument.VARNUM.getRepresentationOfArgument(-1, 0, this.mem)).isEqualTo("0");
     assertThat(IJVMCommandArgument.VARNUM.getRepresentationOfArgument(1, 127, this.mem)).isEqualTo("127");
     assertThat(IJVMCommandArgument.VARNUM.getRepresentationOfArgument(42, -127, this.mem)).isEqualTo("129");
     assertThat(IJVMCommandArgument.VARNUM.getRepresentationOfArgument(-42, 12, this.mem)).isEqualTo("12");
     assertThat(IJVMCommandArgument.VARNUM.getRepresentationOfArgument(0, -12, this.mem)).isEqualTo("244");
     assertThat(IJVMCommandArgument.VARNUM.getRepresentationOfArgument(0, 258, this.mem)).isEqualTo("2");
     assertThat(IJVMCommandArgument.VARNUM.getRepresentationOfArgument(0, 130, this.mem)).isEqualTo("130");
     assertThat(IJVMCommandArgument.VARNUM.getRepresentationOfArgument(0, -130, this.mem)).isEqualTo("126");
 
     assertThat(IJVMCommandArgument.CONST.getRepresentationOfArgument(-1, 0, this.mem)).isEqualTo("0x0");
     assertThat(IJVMCommandArgument.CONST.getRepresentationOfArgument(1, 127, this.mem)).isEqualTo("0x7F");
     assertThat(IJVMCommandArgument.CONST.getRepresentationOfArgument(42, -127, this.mem)).isEqualTo("0x81");
     assertThat(IJVMCommandArgument.CONST.getRepresentationOfArgument(-42, 12, this.mem)).isEqualTo("0xC");
     assertThat(IJVMCommandArgument.CONST.getRepresentationOfArgument(0, -12, this.mem)).isEqualTo("0xF4");
     assertThat(IJVMCommandArgument.CONST.getRepresentationOfArgument(0, 0xF4, this.mem)).isEqualTo("0xF4");
     assertThat(IJVMCommandArgument.CONST.getRepresentationOfArgument(0, 258, this.mem)).isEqualTo("0x2");
     assertThat(IJVMCommandArgument.CONST.getRepresentationOfArgument(0, 130, this.mem)).isEqualTo("0x82");
     assertThat(IJVMCommandArgument.CONST.getRepresentationOfArgument(0, -130, this.mem)).isEqualTo("0x7E");
 
     assertThat(IJVMCommandArgument.LABEL.getRepresentationOfArgument(100, 0, this.mem)).isEqualTo("0x64");
     assertThat(IJVMCommandArgument.LABEL.getRepresentationOfArgument(80, 127, this.mem)).isEqualTo("0xCF");
     assertThat(IJVMCommandArgument.LABEL.getRepresentationOfArgument(500, -127, this.mem)).isEqualTo("0x175");
     assertThat(IJVMCommandArgument.LABEL.getRepresentationOfArgument(40, 12, this.mem)).isEqualTo("0x34");
     assertThat(IJVMCommandArgument.LABEL.getRepresentationOfArgument(30, -12, this.mem)).isEqualTo("0x12");
     assertThat(IJVMCommandArgument.LABEL.getRepresentationOfArgument(200, 0xF4, this.mem)).isEqualTo("0x1BC");
     assertThat(IJVMCommandArgument.LABEL.getRepresentationOfArgument(0, 258, this.mem)).isEqualTo("0x102");
     assertThat(IJVMCommandArgument.LABEL.getRepresentationOfArgument(1, 13456, this.mem)).isEqualTo("0x3491");
     assertThat(IJVMCommandArgument.LABEL.getRepresentationOfArgument(2, 0xABCD, this.mem)).isEqualTo("0xFFFFABCF");
     assertThat(IJVMCommandArgument.LABEL.getRepresentationOfArgument(3, 0x1010, this.mem)).isEqualTo("0x1013");
 
     Register.CPP.setValue(0);
     assertThat(IJVMCommandArgument.INDEX.getRepresentationOfArgument(-1, 0, this.mem)).isEqualTo("0[=0x10203]");
     assertThat(IJVMCommandArgument.INDEX.getRepresentationOfArgument(2, 1, this.mem)).isEqualTo("1[=0x4050607]");
     assertThat(IJVMCommandArgument.INDEX.getRepresentationOfArgument(42, 2, this.mem)).isEqualTo("2[=0x8090A0B]");
     Register.CPP.setValue(Register.CPP.getValue() + 1);
     assertThat(IJVMCommandArgument.INDEX.getRepresentationOfArgument(-42, 0, this.mem)).isEqualTo("0[=0x4050607]");
 
     Register.CPP.setValue(0);
     assertThat(IJVMCommandArgument.OFFSET.getRepresentationOfArgument(-1, 0, this.mem)).isEqualTo("0[=0x10203]");
     assertThat(IJVMCommandArgument.OFFSET.getRepresentationOfArgument(1, 1, this.mem)).isEqualTo("1[=0x4050607]");
     assertThat(IJVMCommandArgument.OFFSET.getRepresentationOfArgument(42, 2, this.mem)).isEqualTo("2[=0x8090A0B]");
     Register.CPP.setValue(Register.CPP.getValue() + 1);
     assertThat(IJVMCommandArgument.OFFSET.getRepresentationOfArgument(-42, 0, this.mem)).isEqualTo("0[=0x4050607]");
   }
 
 }
