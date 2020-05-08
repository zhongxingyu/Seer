 package net.anei.cadpage.parsers.DE;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class DENewCastleCountyBParserTest extends BaseParserTest {
   
   public DENewCastleCountyBParserTest() {
     setParser(new DENewCastleCountyBParser(), "NEW CASTLE COUNTY", "DE");
   }
   
   @Test
   public void testBad() {
     doBadTest("(Chief ALT) [STA:7] -- ALS -- 118 E Pittsfield St Pennsville Township NJ - Cross STS:Sparks Ave -- Caller: DAULTON"); 
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "(Chief ALT) [CFC EMS] -- Syncope - Not Alert -- Amberwood -- 51 Karens Wy - Cross STS:Beechwood Ct & Silver Birch La - EMD:M31D3",
         "SRC:CFC EMS",
         "CALL:Syncope - Not Alert",
         "NAME:Amberwood",
         "ADDR:51 Karens Wy",
         "X:Beechwood Ct & Silver Birch La",
         "UNIT:M31D3");
 
     doTest("T2",
         "(Chief ALT) [CFC EMS] -- Medical Alarm -- Forest Glen Townhses -- 3 Pimlico La - Cross STS:Rue Madora & Dead - End - EMD:M32B2",
         "SRC:CFC EMS",
         "CALL:Medical Alarm",
         "NAME:Forest Glen Townhses",
         "ADDR:3 Pimlico La",
         "MADDR:3 Pimlico Ln",
        "X:Rue Madora & Dead",
         "UNIT:M32B2");
 
     doTest("T3",
         "(Chief ALT) [CFC EMS] -- Abdominal Pains -- Summerset Square Townhses -- 4 Candlewick Ct New Castle DE - Cross STS:Wilton Bl & Dead - End - EMD:M1A1",
         "SRC:CFC EMS",
         "CALL:Abdominal Pains",
         "NAME:Summerset Square Townhses",
         "ADDR:4 Candlewick Ct",
        "X:Wilton Bl & Dead",
         "UNIT:M1A1");
 
     doTest("T4",
         "(Chief ALT) [CFC EMS] -- Assault - Unknown -- Wawa Salem Center - Salem Center Shop Ctr -- 1605 Pulaski Hy - Cross STS:Glasgow Dr & Salem Church Rd - EMD:M4B3",
         "SRC:CFC EMS",
         "CALL:Assault - Unknown",
         "NAME:Wawa Salem Center - Salem Center Shop Ctr",
         "ADDR:1605 Pulaski Hy",
         "MADDR:1605 Pulaski Hwy",
         "X:Glasgow Dr & Salem Church Rd",
         "UNIT:M4B3");
 
     doTest("T5",
         "(Chief ALT) [CFC EMS] -- Assault -- Days Inn -- 900 Churchmans Rd #122 Newark DE - Cross STS:Rte 1 Sb & Old Churchmans Rd - EMD:M4B1A",
         "SRC:CFC EMS",
         "CALL:Assault",
         "NAME:Days Inn",
         "ADDR:900 Churchmans Rd",
         "APT:122",
         "CITY:Newark",
         "X:Rte 1 Sb & Old Churchmans Rd",
         "UNIT:M4B1A");
 
     doTest("T6",
         "(Chief ALT) [CFC EMS] -- Overdose / Poisoning - Not Alert -- Christiana Meadows Apts -- 401 Christiana Meadows - Cross STS:Bear Christiana Rd - EMD:M23C1I",
         "SRC:CFC EMS",
         "CALL:Overdose / Poisoning - Not Alert",
         "NAME:Christiana Meadows Apts",
         "ADDR:401 Christiana Meadows",
         "X:Bear Christiana Rd",
         "UNIT:M23C1I");
 
     doTest("T7",
         "(Chief ALT) [CFC Fire] -- L1*fire Alarm / General -- Medical Arts Pav 1 -- 4745 Stanton Ogletown Rd - Cross STS:Samoset Dr & Hygeia Dr - EMD:52C1G",
         "SRC:CFC Fire",
         "CALL:L1*fire Alarm / General",
         "NAME:Medical Arts Pav 1",
         "ADDR:4745 Stanton Ogletown Rd",
         "X:Samoset Dr & Hygeia Dr",
         "UNIT:52C1G");
 
     doTest("T8",
         "(Chief ALT) [CFC EMS] -- Cva - Sudden Weakness / Numbness -- Crossings At Christiana Townhses -- 216 Gladstone Wy - Cross STS:Dead - End & Baker Dr - EMD:M28C4C",
         "SRC:CFC EMS",
         "CALL:Cva - Sudden Weakness / Numbness",
         "NAME:Crossings At Christiana Townhses",
         "ADDR:216 Gladstone Wy",
        "X:Dead",
         "UNIT:M28C4C");
 
     doTest("T9",
         "(Chief ALT) [CFC EMS] -- Person Fallen -- Hillside Heights -- 109 Rosewood Dr - Cross STS:Greentree Rd & Lavender La - EMD:M17B1",
         "SRC:CFC EMS",
         "CALL:Person Fallen",
         "NAME:Hillside Heights",
         "ADDR:109 Rosewood Dr",
         "X:Greentree Rd & Lavender La",
         "UNIT:M17B1");
 
     doTest("T10",
         "(Chief ALT) [CFC Fire] -- L1*commercial Fire Alarm -- Direct Radiography - Glasgow Business Community -- 600 Technology Dr - Cross STS:Gbc Dr & Yoon La - EMD:52C3G",
         "SRC:CFC Fire",
         "CALL:L1*commercial Fire Alarm",
         "NAME:Direct Radiography - Glasgow Business Community",
         "ADDR:600 Technology Dr",
         "X:Gbc Dr & Yoon La",
         "UNIT:52C3G");
 
     doTest("T11",
         "(Chief ALT) [CFC FIRE] -- Vehicle Accident -- Interstate 95 -- Churchmans Rd / I95 Sb - EMD:M29B4",
         "SRC:CFC FIRE",
         "CALL:Vehicle Accident",
         "NAME:Interstate 95",
         "ADDR:Churchmans Rd & I95 Sb",
         "MADDR:Churchmans Rd & I 95",
         "UNIT:M29B4");
   }
   
   @Test
  public void testDeepWaterParser() {
    
   }
   
   public static void main(String[] args) {
     new DENewCastleCountyBParserTest().generateTests("T1");
   }
 }
     		
