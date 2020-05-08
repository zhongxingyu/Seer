 package net.anei.cadpage.parsers;
 
 import static org.junit.Assert.*;
 
 import net.anei.cadpage.parsers.MsgInfo;
 import net.anei.cadpage.parsers.general.GeneralParser;
 
 import org.junit.Test;
 
 public class MsgInfoTest {
   
   @Test
   public void testProblem() {
   }
 
   @Test
   public void testGetMapAddress() {
     
    doMapTest("TXLibertyCounty", "16897 ST 146,KENBURG,XX", "16897 SH-146N");
    
     doMapTest("INStJosephCounty", "30750 ST 2,KENBURG,XX", "30750 SR 2 HW");
     
     doMapTest("CASanBernardino", "I 10 E & N University St,KENBURG,XX", "I 10 Fwy E & N University St");
     
     doMapTest("NYOrangeCount", "1166 US 9W,KENBURG,XX", "1166 US RTE 9W");
     
     doMapTest("ORKlamathCounty", "97 S & STATE LINE RD,KENBURG,XX", "97 S & STATE LINE RD");
     
     doMapTest("NCForsythCOunty", "421 & SCOTT RD,KENBURG,XX", "NB 421 & SCOTT RD");
     
     doMapTest("CR", "2245 BLACK CIR,KENBURG,XX", "2245 BLACK CIR");
     doMapTest("CRNN", "E COUNTY ROAD 230 W,KENBURG,XX", "E CR 230 W");
     doMapTest("CRNNA", "E COUNTY ROAD 230A W,KENBURG,XX", "E CR 230A W");
     
     doMapTest("NCBuncombeCounty", "226 TAMPA CIR,KENBURG,XX", "226-B TAMPA CIR");
     
     doMapTest("NCBuncombeCounty", "862 CANE CREEK RD,KENBURG,XX", "862 CANE CREEK RD & CROSS CREEK FARM RD");
     
     doMapTest("NCBuncombeCounty", "110 SUMMER ST,KENBURG,XX", "110-1/2 SUMMER ST");
     
     doMapTest("NCAlexanderCounty", "8510 XX 90 E,KENBURG,XX", "8510 XX 90 HWY E");
     
     doMapTest("TXNassauBay", "ST 3 & MAGNOLIA AVE,KENBURG,XX", "SH 3 HWY & MAGNOLIA AVE");
     
     doMapTest("TXNassauBay", "20233  45 FWY,KENBURG,XX", "20233 IH 45 FWY");
     
     doMapTest("MOJeffersonCounty", "OLD MORSE MILL RD & STATE EE,KENBURG,XX", 
                                    "OLD MORSE MILL RD & STATE RD EE");
     
     doMapTest("TXLewsville", "251 N I 35 FRONTAGE RD,KENBURG,XX",
                              "251 N I35E SBFR");
     
     doMapTest("PAArmstrongCounty", "868 STATE 28,KENBURG,XX",
                                    "868 STATE ROUTE 28");
     
     doMapTest("CLHS1", "1600 BROADWAY,KENBURG,XX", "1600 BROADWAY & 4500 S 10TH ST");
     doMapTest("CLHS2", "1000 S RT 50,KENBURG,XX", "1000 S RT 50");
     doMapTest("CLHS3", "US 150 & HWY 30,KENBURG,XX", "US 150 & HWY 30");
     doMapTest("CLHS4", "1100 SE BLACK ST,KENBURG,XX", "1100-1200 SE BLACK ST");
     
     doMapTest("CR1", "1000 PINE ST,KENBURG,XX", "1000 PINE ST", "MULBERY RD");
     doMapTest("CR2", "PINE ST & BLACK ST,KENBURG,XX", "PINE ST & BLACK ST", "MULBERY RD");
     doMapTest("CR3", "PINE ST & MULBERY RD,KENBURG,XX", "PINE ST", "MULBERY RD");
     doMapTest("CR4", "PINE ST & MULBERY RD,KENBURG,XX", "PINE ST", "MULBERY RD/PINE ST");
     doMapTest("CR5", "PINE ST & MULBERY RD,KENBURG,XX", "PINE ST", "MULBERY RD & PINE ST");
     
     doMapTest("CN1", "11TH AVE & 18TH ST,KENBURG,XX", "11TH AVE&18TH ST");
     doMapTest("CN2", "11 AVE & 18 ST,KENBURG,XX", "11 AVE & 18 ST");
     
     doMapTest("APT1", "144 MAIN ST,KENBURG,XX", "144 MAIN ST #14");
     
     doMapTest("BND1", "US HIGHWAY 85 & E 77TH AVE,KENBURG,XX", "US HIGHWAY 85 NB & E 77TH AVE");
     doMapTest("BND2", "US HIGHWAY 100,KENBURG,XX", "US HIGHWAY 100 SB");
     
     doMapPlaceTest("PLC1", "PARK PLACE,US HIGHWAY 100,KENBURG,XX", "US HIGHWAY 100", "PARK PLACE");
     doMapPlaceTest("PLC2", "850 US HIGHWAY 100,KENBURG,XX", "850 US HIGHWAY 100", "PARK PLACE");
     
     doMapTest("RT1", "ST 150 & HWY 12,KENBURG,XX", "ST150 & HWY12");
     doMapTest("RT2", "SAME XX 30 & XX 15 LAST,KENBURG,XX", "SAME XX30 & XX15 LAST");
     doMapTest("RT3", "WE75 & XX 10,KENBURG,XX", "WE75&XX10");
     
     doMapTest("DR1", "345 W US 30,KENBURG,XX", "345 W US RT 30");
     doMapTest("DR2", "345 w st 30,KENBURG,XX", "345 w st hwy 30");
     doMapTest("DR3", "345 XX 22,KENBURG,XX", "345 XX RT 22");
     doMapTest("DR4", "345 XY RT 22,KENBURG,XX", "345 XY RT 22");
     doMapTest("DR5", "3643 ST 281,KENBURG,XX", "3643 STHY 281");
     
     doMapTest("BLK1", "6000 REIDSVILLE RD,KENBURG,XX", "6000BLK REIDSVILLE RD");
     doMapTest("BLK2", "6000 OBLKA ST,KENBURG,XX", "6000 OBLKA ST");
     doMapTest("BLK3", "100 OAK FEST RD,KENBURG,XX", "{100} OAK FEST RD");
     doMapTest("BLK4", "2700 BRAGG BLVD,KENBURG,XX", "2700-BLK BRAGG BLVD");
     
     doMapTest("AV", "150 BLACK AVE,KENBURG,XX", "150 BLACK AV");
     doMapTest("HW", "HWY 20,KENBURG,XX", "HW 20");
     doMapTest("HY", "HWY 20,KENBURG,XX", "HY 20");
     doMapTest("Hw", "Hwy 20,KENBURG,XX", "Hw 20");
     doMapTest("hw", "hwy 20,KENBURG,XX", "hw 20");
     
     doMapTest("STH", "ST 29 & 32,KENBURG,XX", "STH 29 & 32");
     
     doMapTest("&", "RED RD & BLACK ST,KENBURG,XX", "RED RD&BLACK ST");
     
     doMapTest("EO", "BLACK ST & RED RD,KENBURG,XX", "BLACK ST EO RED RD");
     
     doMapTest("SRT", "SRT 155 & SRT 161,KENBURG,XX", "SRT155&SRT161");
     
     doMapTest("NEAR", "BRICK CHURCH RD & RT 214,KENBURG,XX", "BRICK CHURCH RD NEAR RT 214");
   }
   
   private void doMapTest(String title, String result, String address) {
     doMapTest(title, result, address, "", "", "");
   }
   
   private void doMapTest(String title, String result, String address, String cross) {
     doMapTest(title, result, address, cross, "", "");
   }
   
   private void doMapTest(String title, String result, String address, 
                            String cross, String city, String state) {
     MsgInfo.Data data = new MsgInfo.Data(null);
     data.strAddress = address;
     data.strCross = cross;
     data.strCity = city;
     data.strState = state;
     
     data.defCity = "KENBURG";
     data.defState = "XX";
     
     data.parser = new GeneralParser();
     
     MsgInfo info = new MsgInfo(data);
     assertEquals(title, result, info.getMapAddress(false, null, null));
   }
   
   private void doMapPlaceTest(String title, String result, String address, String place) { 
     MsgInfo.Data data = new MsgInfo.Data(null);
     data.strAddress = address;
     data.strPlace = place;
     
     data.defCity = "KENBURG";
     data.defState = "XX";
     
     data.parser = new GeneralParser();
     
     MsgInfo info = new MsgInfo(data);
     assertEquals(title, result, info.getMapAddress(false, null,null));
   }
 
 }
