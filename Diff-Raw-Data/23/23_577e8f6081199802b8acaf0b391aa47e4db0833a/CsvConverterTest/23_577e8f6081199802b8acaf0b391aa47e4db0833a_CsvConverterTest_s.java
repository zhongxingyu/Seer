 /**
  * Copyright (C) 2009 Mads Mohr Christensen, <hr.mohr@gmail.com>
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package dk.cubing.liveresults.utilities;
 
 import static org.junit.Assert.*;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import java.io.StringReader;
 import java.io.StringWriter;
 
 public class CsvConverterTest {
 
     private CsvConverter conv;
 
     @Before
     public void setUp() throws Exception {
         conv = new CsvConverter();                                
     }
 
     @Test
     public void testCompTool2Wca() throws Exception {
         String s = "Name,country,WCA ID,birthday,sex,,2,3,bts,ni,oh,py\n" +
                "Mads Mohr Christensen,Denmark,2007CHRI02,1978-03-13,m,,1,1,1,1,1,1";
 
         String expected = "Status,Name,Country,WCA ID,Birth Date,Gender,,333,222,333oh,pyram,333ni,333bts,Email,Guests,IP\n" +
                 "a,Mads Mohr Christensen,Denmark,2007CHRI02,1978-3-13,m,,1,1,1,1,1,1,,,127.0.0.1\n";
 
         StringWriter sw = new StringWriter();
         conv.compTool2Wca(new StringReader(s), sw);
         assertEquals(expected, sw.toString());
     }
 
     @Test
     public void testWca2CompTool() throws Exception {
         String s = "Status,Name,Country,WCA ID,Birth Date,Gender,,222,333,333bts,333ni,333oh,pyram,Email,Guests,IP\n" +
                 "a,Mads Mohr Christensen,Denmark,2007CHRI02,1978-3-13,m,,1,1,1,1,1,1,,,127.0.0.1\n";
 
         String expected = "Name,country,WCA ID,birthday,sex,,3,2,oh,py,ni,bts\n" +
                 "Mads Mohr Christensen,Denmark,2007CHRI02,1978-03-13,m,,1,1,1,1,1,1\n";
 
         StringWriter sw = new StringWriter();
         conv.wca2CompTool(new StringReader(s), sw);
         assertEquals(expected, sw.toString());
     }
 }
