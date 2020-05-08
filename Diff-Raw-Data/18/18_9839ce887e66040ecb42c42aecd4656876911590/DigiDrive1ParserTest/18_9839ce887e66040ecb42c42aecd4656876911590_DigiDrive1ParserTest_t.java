 /*
  *  Copyright (C) 2012 Carl Green
  * 
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  * 
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  * 
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package info.carlwithak.mpxg2.sysex.effects.algorithms;
 
 import info.carlwithak.mpxg2.model.effects.algorithms.DigiDrive1;
 import org.junit.Test;
 
 import static info.carlwithak.mpxg2.test.IsValue.value;
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.assertThat;
 
 /**
  *
  * @author Carl Green
  */
 public class DigiDrive1ParserTest {
 
     @Test
     public void testParse() {
        byte[] effectParameters = {4, 6, 0, 0, 4, 6, 9, 11, 0, 0, 0, 0, 15, 15, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
         DigiDrive1 digiDrive1 = DigiDrive1Parser.parse(effectParameters);
         assertThat(digiDrive1.mix, is(value(100)));
         assertThat(digiDrive1.level, is(value(0)));
         assertThat(digiDrive1.drive, is(value(100)));
        assertThat(digiDrive1.low, is(value(-71)));
         assertThat(digiDrive1.mid, is(value(0)));
         assertThat(digiDrive1.high, is(value(0)));
     }
 
 }
