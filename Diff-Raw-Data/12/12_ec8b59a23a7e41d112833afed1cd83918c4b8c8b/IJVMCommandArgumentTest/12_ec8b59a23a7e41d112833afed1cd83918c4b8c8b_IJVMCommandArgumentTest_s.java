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
 package com.github.croesch.mic1.mem;
 
 import static org.fest.assertions.Assertions.assertThat;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import com.github.croesch.error.FileFormatException;
 import com.github.croesch.mic1.register.Register;
 
 /**
  * Provides test cases for {@link IJVMCommandArgument}.
  * 
  * @author croesch
  * @since Date: Jan 23, 2012
  */
public class IJVMCommandArgumentTest {
 
   private Memory mem;
 
   @Before
   public void setUp() throws FileFormatException {
    this.mem = new Memory(Byte.MAX_VALUE, ClassLoader.getSystemResourceAsStream("mic1/test.ijvm"));
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
     assertThat(IJVMCommandArgument.BYTE.getRepresentationOfArgument(0, this.mem)).isEqualTo("0x0");
     assertThat(IJVMCommandArgument.BYTE.getRepresentationOfArgument(127, this.mem)).isEqualTo("0x7F");
     assertThat(IJVMCommandArgument.BYTE.getRepresentationOfArgument(-127, this.mem)).isEqualTo("0x81");
     assertThat(IJVMCommandArgument.BYTE.getRepresentationOfArgument(12, this.mem)).isEqualTo("0xC");
     assertThat(IJVMCommandArgument.BYTE.getRepresentationOfArgument(-12, this.mem)).isEqualTo("0xF4");
     assertThat(IJVMCommandArgument.BYTE.getRepresentationOfArgument(0xF4, this.mem)).isEqualTo("0xF4");
     assertThat(IJVMCommandArgument.BYTE.getRepresentationOfArgument(258, this.mem)).isEqualTo("0x2");
     assertThat(IJVMCommandArgument.BYTE.getRepresentationOfArgument(130, this.mem)).isEqualTo("0x82");
     assertThat(IJVMCommandArgument.BYTE.getRepresentationOfArgument(-130, this.mem)).isEqualTo("0x7E");
 
     assertThat(IJVMCommandArgument.VARNUM.getRepresentationOfArgument(0, this.mem)).isEqualTo("0");
     assertThat(IJVMCommandArgument.VARNUM.getRepresentationOfArgument(127, this.mem)).isEqualTo("127");
     assertThat(IJVMCommandArgument.VARNUM.getRepresentationOfArgument(-127, this.mem)).isEqualTo("129");
     assertThat(IJVMCommandArgument.VARNUM.getRepresentationOfArgument(12, this.mem)).isEqualTo("12");
     assertThat(IJVMCommandArgument.VARNUM.getRepresentationOfArgument(-12, this.mem)).isEqualTo("244");
     assertThat(IJVMCommandArgument.VARNUM.getRepresentationOfArgument(258, this.mem)).isEqualTo("2");
     assertThat(IJVMCommandArgument.VARNUM.getRepresentationOfArgument(130, this.mem)).isEqualTo("130");
     assertThat(IJVMCommandArgument.VARNUM.getRepresentationOfArgument(-130, this.mem)).isEqualTo("126");
 
     assertThat(IJVMCommandArgument.CONST.getRepresentationOfArgument(0, this.mem)).isEqualTo("0x0");
     assertThat(IJVMCommandArgument.CONST.getRepresentationOfArgument(127, this.mem)).isEqualTo("0x7F");
     assertThat(IJVMCommandArgument.CONST.getRepresentationOfArgument(-127, this.mem)).isEqualTo("0x81");
     assertThat(IJVMCommandArgument.CONST.getRepresentationOfArgument(12, this.mem)).isEqualTo("0xC");
     assertThat(IJVMCommandArgument.CONST.getRepresentationOfArgument(-12, this.mem)).isEqualTo("0xF4");
     assertThat(IJVMCommandArgument.CONST.getRepresentationOfArgument(0xF4, this.mem)).isEqualTo("0xF4");
     assertThat(IJVMCommandArgument.CONST.getRepresentationOfArgument(258, this.mem)).isEqualTo("0x2");
     assertThat(IJVMCommandArgument.CONST.getRepresentationOfArgument(130, this.mem)).isEqualTo("0x82");
     assertThat(IJVMCommandArgument.CONST.getRepresentationOfArgument(-130, this.mem)).isEqualTo("0x7E");
 
     assertThat(IJVMCommandArgument.LABEL.getRepresentationOfArgument(0, this.mem)).isEqualTo("0");
     assertThat(IJVMCommandArgument.LABEL.getRepresentationOfArgument(127, this.mem)).isEqualTo("127");
     assertThat(IJVMCommandArgument.LABEL.getRepresentationOfArgument(-127, this.mem)).isEqualTo("-127");
     assertThat(IJVMCommandArgument.LABEL.getRepresentationOfArgument(12, this.mem)).isEqualTo("12");
     assertThat(IJVMCommandArgument.LABEL.getRepresentationOfArgument(-12, this.mem)).isEqualTo("-12");
     assertThat(IJVMCommandArgument.LABEL.getRepresentationOfArgument(0xF4, this.mem)).isEqualTo("244");
     assertThat(IJVMCommandArgument.LABEL.getRepresentationOfArgument(258, this.mem)).isEqualTo("258");
     assertThat(IJVMCommandArgument.LABEL.getRepresentationOfArgument(13456, this.mem)).isEqualTo("13456");
     assertThat(IJVMCommandArgument.LABEL.getRepresentationOfArgument(0xABCD, this.mem)).isEqualTo("-21555");
     assertThat(IJVMCommandArgument.LABEL.getRepresentationOfArgument(0x1010, this.mem)).isEqualTo("4112");
 
     assertThat(IJVMCommandArgument.OFFSET.getRepresentationOfArgument(0, this.mem)).isEqualTo("0x0");
     assertThat(IJVMCommandArgument.OFFSET.getRepresentationOfArgument(127, this.mem)).isEqualTo("0x7F");
     assertThat(IJVMCommandArgument.OFFSET.getRepresentationOfArgument(-127, this.mem)).isEqualTo("0xFF81");
     assertThat(IJVMCommandArgument.OFFSET.getRepresentationOfArgument(12, this.mem)).isEqualTo("0xC");
     assertThat(IJVMCommandArgument.OFFSET.getRepresentationOfArgument(-12, this.mem)).isEqualTo("0xFFF4");
     assertThat(IJVMCommandArgument.OFFSET.getRepresentationOfArgument(0xF4, this.mem)).isEqualTo("0xF4");
     assertThat(IJVMCommandArgument.OFFSET.getRepresentationOfArgument(258, this.mem)).isEqualTo("0x102");
     assertThat(IJVMCommandArgument.OFFSET.getRepresentationOfArgument(130, this.mem)).isEqualTo("0x82");
     assertThat(IJVMCommandArgument.OFFSET.getRepresentationOfArgument(-130, this.mem)).isEqualTo("0xFF7E");
 
     assertThat(IJVMCommandArgument.INDEX.getRepresentationOfArgument(0, this.mem)).isEqualTo("0x0[=0x10203]");
     assertThat(IJVMCommandArgument.INDEX.getRepresentationOfArgument(1, this.mem)).isEqualTo("0x1[=0x4050607]");
     Register.CPP.setValue(Register.CPP.getValue() + 1);
     assertThat(IJVMCommandArgument.INDEX.getRepresentationOfArgument(0, this.mem)).isEqualTo("0x0[=0x4050607]");
   }
 
 }
