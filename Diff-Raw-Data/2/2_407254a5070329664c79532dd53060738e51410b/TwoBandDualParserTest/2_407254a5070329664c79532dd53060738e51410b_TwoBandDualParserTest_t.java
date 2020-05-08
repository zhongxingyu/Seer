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
 
 import info.carlwithak.mpxg2.model.effects.algorithms.TwoBandDual;
 import org.junit.Test;
 
 import static info.carlwithak.mpxg2.test.IsValue.value;
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.assertThat;
 
 /**
  *
  * @author Carl Green
  */
 public class TwoBandDualParserTest {
 
     @Test
    public void testParse_SoloRoom() {
         byte[] effectParameters = {4, 6, 0, 0, 2, 14, 14, 6, 0, 0, 7, 0, 0, 0, 2, 14, 2, 5, 3, 0, 7, 0, 2, 0, 2, 14, 14, 6, 0, 0, 7, 0, 0, 0, 2, 14, 4, 10, 6, 0, 7, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
         TwoBandDual twoBandDual = TwoBandDualParser.parse(effectParameters);
         assertThat(twoBandDual.mix, is(value(100)));
         assertThat(twoBandDual.level, is(value(0)));
         assertThat(twoBandDual.gainLeft1, is(value(-30)));
         assertThat(twoBandDual.fcLeft1, is(value(110)));
         assertThat(twoBandDual.qLeft1, is(value(0.7)));
         assertThat(twoBandDual.modeLeft1, is(value(0))); // LShlf
         assertThat(twoBandDual.gainLeft2, is(value(-30)));
         assertThat(twoBandDual.fcLeft2, is(value(850)));
         assertThat(twoBandDual.qLeft2, is(value(0.7)));
         assertThat(twoBandDual.modeLeft2, is(value(2))); // HShlf
         assertThat(twoBandDual.gainRight1, is(value(-30)));
         assertThat(twoBandDual.fcRight1, is(value(110)));
         assertThat(twoBandDual.qRight1, is(value(0.7)));
         assertThat(twoBandDual.modeRight1, is(value(0))); // LShlf
         assertThat(twoBandDual.gainRight2, is(value(-30)));
         assertThat(twoBandDual.fcRight2, is(value(1700)));
         assertThat(twoBandDual.qRight2, is(value(0.7)));
         assertThat(twoBandDual.modeRight2, is(value(2))); // HShlf
     }
 
 }
