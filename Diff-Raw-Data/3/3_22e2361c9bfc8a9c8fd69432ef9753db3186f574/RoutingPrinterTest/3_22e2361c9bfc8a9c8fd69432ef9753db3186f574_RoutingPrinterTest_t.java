 /*
  *  Copyright (C) 2011 Carl Green
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
 
 package info.carlwithak.mpxg2.printing;
 
 import info.carlwithak.mpxg2.model.Program;
import info.carlwithak.mpxg2.model.RoutingData;
 import org.junit.Test;
 
 import static org.junit.Assert.assertEquals;
 
 /**
  * Tests for RoutingPrinter.
  *
  * @author Carl Green
  */
 public class RoutingPrinterTest {
 
     /**
      * Test printing a textual representation of the routing.
      *
      * G2 Blue is a simple all along the upper route routing.
      */
     @Test
     public void testPrintG2Blue() throws PrintException {
         Program program = new Program();
 
         RoutingData routing = new RoutingData();
         routing.setEffectId(8);
         routing.setUpperInputConnection(0);
         routing.setLowerInputConnection(0);
         routing.setRouting(0);
         routing.setPathType(0);
         program.setRouting0(routing);
 
         routing = new RoutingData();
         routing.setEffectId(0);
         routing.setUpperInputConnection(0);
         routing.setLowerInputConnection(0);
         routing.setRouting(0);
         routing.setPathType(0);
         program.setRouting1(routing);
 
         routing = new RoutingData();
         routing.setEffectId(1);
         routing.setUpperInputConnection(0);
         routing.setLowerInputConnection(0);
         routing.setRouting(0);
         routing.setPathType(0);
         program.setRouting2(routing);
 
         routing = new RoutingData();
         routing.setEffectId(6);
         routing.setUpperInputConnection(0);
         routing.setLowerInputConnection(0);
         routing.setRouting(0);
         routing.setPathType(0);
         program.setRouting3(routing);
 
         routing = new RoutingData();
         routing.setEffectId(2);
         routing.setUpperInputConnection(0);
         routing.setLowerInputConnection(0);
         routing.setRouting(0);
         routing.setPathType(0);
         program.setRouting4(routing);
 
         routing = new RoutingData();
         routing.setEffectId(3);
         routing.setUpperInputConnection(0);
         routing.setLowerInputConnection(0);
         routing.setRouting(0);
         routing.setPathType(0);
         program.setRouting5(routing);
 
         routing = new RoutingData();
         routing.setEffectId(4);
         routing.setUpperInputConnection(0);
         routing.setLowerInputConnection(0);
         routing.setRouting(0);
         routing.setPathType(0);
         program.setRouting6(routing);
 
         routing = new RoutingData();
         routing.setEffectId(5);
         routing.setUpperInputConnection(0);
         routing.setLowerInputConnection(0);
         routing.setRouting(0);
         routing.setPathType(0);
         program.setRouting7(routing);
 
         routing = new RoutingData();
         routing.setEffectId(7);
         routing.setUpperInputConnection(0);
         routing.setLowerInputConnection(0);
         routing.setRouting(0);
         routing.setPathType(0);
         program.setRouting8(routing);
 
         String expected = "I=1=2=G=C=D=R=E=O";
 
         assertEquals(expected, RoutingPrinter.print(program));
     }
 
     /**
      * Test printing a textual representation of the routing.
      *
      * Guitar Solo splits into the lower route.
      */
     @Test
     public void testPrintGuitarSolo() throws PrintException {
         Program program = new Program();
 
         RoutingData routing = new RoutingData();
         routing.setEffectId(8);
         routing.setUpperInputConnection(0);
         routing.setLowerInputConnection(0);
         routing.setRouting(0);
         routing.setPathType(0);
         program.setRouting0(routing);
 
         routing = new RoutingData();
         routing.setEffectId(5);
         routing.setUpperInputConnection(0);
         routing.setLowerInputConnection(0);
         routing.setRouting(0);
         routing.setPathType(0);
         program.setRouting1(routing);
 
         routing = new RoutingData();
         routing.setEffectId(2);
         routing.setUpperInputConnection(0);
         routing.setLowerInputConnection(0);
         routing.setRouting(0);
         routing.setPathType(0);
         program.setRouting2(routing);
 
         routing = new RoutingData();
         routing.setEffectId(6);
         routing.setUpperInputConnection(0);
         routing.setLowerInputConnection(0);
         routing.setRouting(0);
         routing.setPathType(0);
         program.setRouting3(routing);
 
         routing = new RoutingData();
         routing.setEffectId(0);
         routing.setUpperInputConnection(0);
         routing.setLowerInputConnection(0);
         routing.setRouting(3);
         routing.setPathType(0);
         program.setRouting4(routing);
 
         routing = new RoutingData();
         routing.setEffectId(3);
         routing.setUpperInputConnection(0);
         routing.setLowerInputConnection(0);
         routing.setRouting(1);
         routing.setPathType(1);
         program.setRouting5(routing);
 
         routing = new RoutingData();
         routing.setEffectId(4);
         routing.setUpperInputConnection(0);
         routing.setLowerInputConnection(0);
         routing.setRouting(0);
         routing.setPathType(1);
         program.setRouting6(routing);
 
         routing = new RoutingData();
         routing.setEffectId(1);
         routing.setUpperInputConnection(0);
         routing.setLowerInputConnection(0);
         routing.setRouting(2);
         routing.setPathType(1);
         program.setRouting7(routing);
 
         routing = new RoutingData();
         routing.setEffectId(7);
         routing.setUpperInputConnection(0);
         routing.setLowerInputConnection(0);
         routing.setRouting(0);
         routing.setPathType(0);
         program.setRouting8(routing);
 
         // TODO E, C inactive
         String expected = "I=E=C=G=1===R=2=O\n" +
                           "        |=D===|";
         String actual = RoutingPrinter.print(program);
 
         assertEquals(expected, actual);
     }
 
     /**
      * Test printing a textual representation of the routing.
      *
      * Cordovox splits and has mono and stereo paths.
      */
     @Test
     public void testPrintCordovox() throws PrintException {
         Program program = new Program();
 
         RoutingData routing = new RoutingData();
         routing.setEffectId(8);
         routing.setUpperInputConnection(0);
         routing.setLowerInputConnection(0);
         routing.setRouting(0);
         routing.setPathType(0);
         program.setRouting0(routing);
 
         routing = new RoutingData();
         routing.setEffectId(5);
         routing.setUpperInputConnection(0);
         routing.setLowerInputConnection(0);
         routing.setRouting(0);
         routing.setPathType(0);
         program.setRouting1(routing);
 
         routing = new RoutingData();
         routing.setEffectId(6);
         routing.setUpperInputConnection(0);
         routing.setLowerInputConnection(0);
         routing.setRouting(0);
         routing.setPathType(0);
         program.setRouting2(routing);
 
         routing = new RoutingData();
         routing.setEffectId(2);
         routing.setUpperInputConnection(0);
         routing.setLowerInputConnection(0);
         routing.setRouting(3);
         routing.setPathType(0);
         program.setRouting3(routing);
 
         routing = new RoutingData();
         routing.setEffectId(0);
         routing.setUpperInputConnection(0);
         routing.setLowerInputConnection(4);
         routing.setRouting(1);
         routing.setPathType(1);
         program.setRouting4(routing);
 
         routing = new RoutingData();
         routing.setEffectId(1);
         routing.setUpperInputConnection(3);
         routing.setLowerInputConnection(0);
         routing.setRouting(0);
         routing.setPathType(1);
         program.setRouting5(routing);
 
         routing = new RoutingData();
         routing.setEffectId(3);
         routing.setUpperInputConnection(0);
         routing.setLowerInputConnection(0);
         routing.setRouting(0);
         routing.setPathType(1);
         program.setRouting6(routing);
 
         routing = new RoutingData();
         routing.setEffectId(4);
         routing.setUpperInputConnection(0);
         routing.setLowerInputConnection(0);
         routing.setRouting(0);
         routing.setPathType(1);
         program.setRouting7(routing);
 
         routing = new RoutingData();
         routing.setEffectId(7);
         routing.setUpperInputConnection(0);
         routing.setLowerInputConnection(0);
         routing.setRouting(2);
         routing.setPathType(1);
         program.setRouting8(routing);
 
         String expected = "I=E=G=C--\\2=D=R=O\n" +
                           "      |/1=======|";
         String actual = RoutingPrinter.print(program);
 
         assertEquals(expected, actual);
     }
 
     /**
      * Test printing an invalid routing where it splits into two routes but
      * never combines again.
      */
     @Test(expected = PrintException.class)
     public void testInvalidRouting() throws PrintException {
         Program program = new Program();
 
         RoutingData routing = new RoutingData();
         routing.setEffectId(8);
         routing.setUpperInputConnection(0);
         routing.setLowerInputConnection(0);
         routing.setRouting(0);
         routing.setPathType(0);
         program.setRouting0(routing);
 
         routing = new RoutingData();
         routing.setEffectId(5);
         routing.setUpperInputConnection(0);
         routing.setLowerInputConnection(0);
         routing.setRouting(0);
         routing.setPathType(0);
         program.setRouting1(routing);
 
         routing = new RoutingData();
         routing.setEffectId(2);
         routing.setUpperInputConnection(0);
         routing.setLowerInputConnection(0);
         routing.setRouting(0);
         routing.setPathType(0);
         program.setRouting2(routing);
 
         routing = new RoutingData();
         routing.setEffectId(6);
         routing.setUpperInputConnection(0);
         routing.setLowerInputConnection(0);
         routing.setRouting(0);
         routing.setPathType(0);
         program.setRouting3(routing);
 
         routing = new RoutingData();
         routing.setEffectId(0);
         routing.setUpperInputConnection(0);
         routing.setLowerInputConnection(0);
         routing.setRouting(3);
         routing.setPathType(0);
         program.setRouting4(routing);
 
         routing = new RoutingData();
         routing.setEffectId(3);
         routing.setUpperInputConnection(0);
         routing.setLowerInputConnection(0);
         routing.setRouting(1);
         routing.setPathType(1);
         program.setRouting5(routing);
 
         routing = new RoutingData();
         routing.setEffectId(4);
         routing.setUpperInputConnection(0);
         routing.setLowerInputConnection(0);
         routing.setRouting(1);
         routing.setPathType(1);
         program.setRouting6(routing);
 
         routing = new RoutingData();
         routing.setEffectId(1);
         routing.setUpperInputConnection(0);
         routing.setLowerInputConnection(0);
         routing.setRouting(1);
         routing.setPathType(1);
         program.setRouting7(routing);
 
         routing = new RoutingData();
         routing.setEffectId(7);
         routing.setUpperInputConnection(0);
         routing.setLowerInputConnection(0);
         routing.setRouting(1);
         routing.setPathType(1);
         program.setRouting8(routing);
 
         RoutingPrinter.print(program);
     }
 }
