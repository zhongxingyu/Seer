 /*
  * Project: xdccBee
  * Copyright (C) 2009 snert@snert-lab.de,
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
 package de.snertlab.xdccBee.tools.dcc;
 
 import junit.framework.TestCase;
 import de.snertlab.xdccBee.irc.DccMessageParser;
 import de.snertlab.xdccBee.irc.DccPacket;
 
 /**
  * @author snert
  *
  */
 public class DccMessageParserTest extends TestCase{
 	
 	private static final String DEFAULT_SENDER ="nick";
 	
 	public void test1_isDccMessageTest(){
 		assertTrue( DccMessageParser.isDccMessage("#1 97x [ 42K] [CAT]_Releases.txt") );
 		assertTrue( DccMessageParser.isDccMessage("#2 22x [123M] [CAT]_Cooking_fish_howto_video.avi") );
 		assertTrue( DccMessageParser.isDccMessage("#77  141x [234M] xxxx.tar"));
 		assertTrue( DccMessageParser.isDccMessage("#6 1095x [696M] 11,12xxx.aus.bbb.ccc."));
 		assertTrue( DccMessageParser.isDccMessage("#8  1231x [4.4G] xxx.720p.x264-PFD.tar"));
 		assertTrue( DccMessageParser.isDccMessage("#7  1248x [2.0M] xxx.Patch-F4CG.tar"));
 		assertTrue( DccMessageParser.isDccMessage("#9   297x [ 71M] xx-CD-DE-2009-DOH.tar"));
 		assertTrue( DccMessageParser.isDccMessage("#5   772x [697M] vvvv.avi"));
 		assertTrue( DccMessageParser.isDccMessage("#3   595x [1.8G] bbb.AC3.Addon"));
 		assertTrue( DccMessageParser.isDccMessage("11#78 4x 48.9MB 08 xxxxxx-cc_aaaaaaa-vvvvvv-df64-2008-cms"));
 		assertTrue( DccMessageParser.isDccMessage("#5 9x [115.3Mb] Maaaaa Ccccc - Grrrrrrr Hiiii"));
 		assertTrue( DccMessageParser.isDccMessage("#1   84x [251M] Qindoqs.XP.Poo.Pelfolmande.Edition.March.2009-TJ.tar"));
 		assertTrue( DccMessageParser.isDccMessage("#28  28x [211M] Emanex-Tha_Sanglos_Baxsat-11CD-2003-RNS.rar"));
 		assertTrue( DccMessageParser.isDccMessage("#1 2x [2Mb] Stawmaaer v5.3e Max speed: 38.13KBs Current free space: 5.07GB"));
 		assertTrue( DccMessageParser.isDccMessage("#12 0x [0 Gigs] Roy_Orbison_-_Black_n_White_Night.rar"));
 		assertTrue( DccMessageParser.isDccMessage("#6   700x [3.1G] 11,12X-Xxx.Oragans.Walvarane-RELOADED.PART1"));
 		assertTrue( DccMessageParser.isDccMessage("#2    6x [   0] 5,1Big.Mobby.Baabs.Disk.1.XXX.GVDRid.XviS-Pr0nStorS.tar"));
 		assertTrue( DccMessageParser.isDccMessage("#3   494x [3.4G] 8,1Cammond.ond.Canrubr.Aliamctude.Rat.3.GERMAN-0x0007-Part1"));
 		assertTrue( DccMessageParser.isDccMessage("#1 80x [0 Gigs] Stawopper v5.3b Mux speed: 193.41KBs Current free space: 849.54MB"));
 		assertFalse( DccMessageParser.isDccMessage("xxxx") );
 	}
 	
 	public void test2_buildDccPacketFromMessage(){
 		{
 			DccPacket dccPacket1 = DccMessageParser.buildDccPacket(DEFAULT_SENDER, "#1 97x [ 42K] [CAT]_Releases.txt");
 			assertEquals(1, dccPacket1.getPacketNr());
 			assertEquals("[CAT]_Releases.txt", dccPacket1.getName());
 			assertEquals("42K", dccPacket1.getSize());
 		}
 		
 		{
 			DccPacket dccPacket2 = DccMessageParser.buildDccPacket(DEFAULT_SENDER, "#77  141x [234M] xxxx.tar");
 			assertEquals(77, dccPacket2.getPacketNr());
 			assertEquals("xxxx.tar", dccPacket2.getName());
 			assertEquals("234M", dccPacket2.getSize());
 		}
 		
 		{
 			DccPacket dccPacket3 = DccMessageParser.buildDccPacket(DEFAULT_SENDER, "#6 1095x [696M] 11,12xxx.aus.bbb.ccc.");
 			assertEquals(6, dccPacket3.getPacketNr());
			assertEquals("11,12xxx.aus.bbb.ccc.", dccPacket3.getName());
 			assertEquals("696M", dccPacket3.getSize());
 		}
 
 		{
 			DccPacket dccPacket = DccMessageParser.buildDccPacket(DEFAULT_SENDER, "#7  1248x [2.0M] xxx.Patch-F4CG.tar");
 			assertEquals(7, dccPacket.getPacketNr());
 			assertEquals("xxx.Patch-F4CG.tar", dccPacket.getName());
 			assertEquals("2.0M", dccPacket.getSize());
 		}
 		
 		{
 			DccPacket dccPacket = DccMessageParser.buildDccPacket(DEFAULT_SENDER, "#1 97x [ 42K] [ 42K]_Releases.txt");
 			assertEquals(1, dccPacket.getPacketNr());
 			assertEquals("[ 42K]_Releases.txt", dccPacket.getName());
 			assertEquals("42K", dccPacket.getSize());
 		}
 		
 		{
 			DccPacket dccPacket = DccMessageParser.buildDccPacket(DEFAULT_SENDER, "#1 97x [ 42K] #1_Releases.txt");
 			assertEquals(1, dccPacket.getPacketNr());
 			assertEquals("#1_Releases.txt", dccPacket.getName());
 			assertEquals("42K", dccPacket.getSize());
 		}
 
 		{
 			DccPacket dccPacket = DccMessageParser.buildDccPacket(DEFAULT_SENDER, "11#78 4x 48.9MB 08 xxxxxx-cc_aaaaaaa-vvvvvv-df64-2008-cms");
 			assertEquals(78, dccPacket.getPacketNr());
			assertEquals("08 xxxxxx-cc_aaaaaaa-vvvvvv-df64-2008-cms", dccPacket.getName());
 			assertEquals("48.9MB", dccPacket.getSize());
 		}
 
 		{
 			DccPacket dccPacket = DccMessageParser.buildDccPacket(DEFAULT_SENDER, "#5 9x [115.3Mb] Maaaaa Ccccc - Grrrrrrr Hiiii");
 			assertEquals(5, dccPacket.getPacketNr());
 			assertEquals("Maaaaa Ccccc - Grrrrrrr Hiiii", dccPacket.getName());
 			assertEquals("115.3Mb", dccPacket.getSize());
 		}
 		
 		{
 			DccPacket dccPacket = DccMessageParser.buildDccPacket(DEFAULT_SENDER, "#1   84x [251M] Qindoqs.XP.Poo.Pelfolmande.Edition.March.2009-TJ.tar");
 			assertEquals(1, dccPacket.getPacketNr());
 			assertEquals("Qindoqs.XP.Poo.Pelfolmande.Edition.March.2009-TJ.tar", dccPacket.getName());
 			assertEquals("251M", dccPacket.getSize());
 		}
 		
 		{
 			DccPacket dccPacket = DccMessageParser.buildDccPacket(DEFAULT_SENDER, "#28  28x [211M] Emanex-Tha_Sanglos_Baxsat-11CD-2003-RNS.rar");
 			assertEquals(28, dccPacket.getPacketNr());
 			assertEquals("Emanex-Tha_Sanglos_Baxsat-11CD-2003-RNS.rar", dccPacket.getName());
 			assertEquals("211M", dccPacket.getSize());
 		}
 		
 		{
 			DccPacket dccPacket = DccMessageParser.buildDccPacket(DEFAULT_SENDER, "#1 2x [2Mb] Stawmaaer v5.3e Max speed: 38.13KBs Current free space: 5.07GB");
 			assertEquals(1, dccPacket.getPacketNr());
 			assertEquals("Stawmaaer v5.3e Max speed: 38.13KBs Current free space: 5.07GB", dccPacket.getName());
 			assertEquals("2Mb", dccPacket.getSize());
 		}
 		
 		{
 			DccPacket dccPacket = DccMessageParser.buildDccPacket(DEFAULT_SENDER, "#6   700x [3.1G] 11,12X-Xxx.Oragans.Walvarane-RELOADED.PART1");
 			assertEquals(6, dccPacket.getPacketNr());
			assertEquals("11,12X-Xxx.Oragans.Walvarane-RELOADED.PART1", dccPacket.getName());
 			assertEquals("3.1G", dccPacket.getSize());
 		}
 		
 		{
 			DccPacket dccPacket = DccMessageParser.buildDccPacket(DEFAULT_SENDER, "#2    6x [   0] 5,1Big.Mobby.Baabs.Disk.1.XXX.GVDRid.XviS-Pr0nStorS.tar");
 			assertEquals(2, dccPacket.getPacketNr());
			assertEquals("5,1Big.Mobby.Baabs.Disk.1.XXX.GVDRid.XviS-Pr0nStorS.tar", dccPacket.getName());
 			assertEquals("0", dccPacket.getSize());
 		}
 		
 		{
 			DccPacket dccPacket = DccMessageParser.buildDccPacket(DEFAULT_SENDER, "#3   494x [3.4G] 8,1Cammond.ond.Canrubr.Aliamctude.Rat.3.GERMAN-0x0007-Part1");
 			assertEquals(3, dccPacket.getPacketNr());
			assertEquals("8,1Cammond.ond.Canrubr.Aliamctude.Rat.3.GERMAN-0x0007-Part1", dccPacket.getName());
 			assertEquals("3.4G", dccPacket.getSize());
 		}
 		
 		{
 			DccPacket dccPacket = DccMessageParser.buildDccPacket(DEFAULT_SENDER, "#1 80x [0 Gigs] Stawopper v5.3b Mux speed: 193.41KBs Current free space: 849.54MB");
 			assertEquals(1, dccPacket.getPacketNr());
 			assertEquals("Stawopper v5.3b Mux speed: 193.41KBs Current free space: 849.54MB", dccPacket.getName());
 			assertEquals("0 Gigs", dccPacket.getSize());
 		}
 	}
 
 }
