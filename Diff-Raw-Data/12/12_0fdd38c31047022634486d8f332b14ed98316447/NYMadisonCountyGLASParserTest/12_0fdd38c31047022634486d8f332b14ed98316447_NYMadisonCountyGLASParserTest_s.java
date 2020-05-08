 package net.anei.cadpage.parsers.NY;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class NYMadisonCountyGLASParserTest extends BaseParserTest {
   
   public NYMadisonCountyGLASParserTest() {
     setParser(new NYMadisonCountyGLASParser(), "MADISON COUNTY", "NY");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "FRM:messaging@iamresponding.com\nSUBJ:Greater Lenox\nMSG:Sick Person\n7738 WISE RD , LENOX ( / N COURT)",
         "CALL:Sick Person",
         "ADDR:7738 WISE RD",
         "CITY:LENOX",
         "X:N COURT");
 
     doTest("T2",
         "FRM:messaging@iamresponding.com\nSUBJ:Greater Lenox\nMSG:Assist\n400 LAMB AV , CANASTOTA VILLAGE ( / DEPPOLITI AV)",
         "CALL:Assist",
         "ADDR:400 LAMB AV",
         "MADDR:400 LAMB AVE",
         "CITY:CANASTOTA VILLAGE",
         "X:DEPPOLITI AV");
 
     doTest("T3",
         "FRM:messaging@iamresponding.com\nSUBJ:Greater Lenox\nMSG:Heart Problem\n@MADISON COUNTY DSS (133 NORTH COURT ST (WAMPSVILLE VIL",
         "CALL:Heart Problem",
         "PLACE:MADISON COUNTY DSS",
         "ADDR:133 NORTH COURT ST",
         "CITY:WAMPSVILLE VILLAGE");
 
     doTest("T4",
         "FRM:messaging@iamresponding.com\nSUBJ:Greater Lenox\nMSG:Convulsions/Seizures\n7216 NELSON RD , LENOX (SENECA TRNPK / PAVONE PL)",
         "CALL:Convulsions/Seizures",
         "ADDR:7216 NELSON RD",
         "CITY:LENOX",
         "X:SENECA TRNPK / PAVONE PL");
 
     doTest("T5",
         "FRM:messaging@iamresponding.com\nSUBJ:Greater Lenox\nMSG:Sick Person\n400 LAMB AV , CANASTOTA VILLAGE ( / DEPPOLITI AV)",
         "CALL:Sick Person",
         "ADDR:400 LAMB AV",
         "MADDR:400 LAMB AVE",
         "CITY:CANASTOTA VILLAGE",
         "X:DEPPOLITI AV");
 
     doTest("T6",
         "FRM:messaging@iamresponding.com\nSUBJ:Greater Lenox\nMSG:Chest Pain\n123 CAYUGA AV , SULLIVAN ( ONEIDA LAKE AV / ROUTE 31)",
         "CALL:Chest Pain",
         "ADDR:123 CAYUGA AV",
         "MADDR:123 CAYUGA AVE",
         "CITY:SULLIVAN",
         "X:ONEIDA LAKE AV / ROUTE 31");
 
     doTest("T7",
         "FRM:messaging@iamresponding.com\nSUBJ:Greater Lenox\nMSG:Sick Person\n7885 TACKABURY RD , LENOX ( DITCH BANK RD / INDIAN OPENING RD)",
         "CALL:Sick Person",
         "ADDR:7885 TACKABURY RD",
         "CITY:LENOX",
         "X:DITCH BANK RD / INDIAN OPENING RD");
 
     doTest("T8",
         "FRM:messaging@iamresponding.com\nSUBJ:Greater Lenox\nMSG: Chest Pain\n3881 COTTONS RD , LINCOLN ( CLOCKVILLE RD / NELSON RD)",
         "CALL:Chest Pain",
         "ADDR:3881 COTTONS RD",
         "CITY:LINCOLN",
         "X:CLOCKVILLE RD / NELSON RD");
 
     doTest("T9",
         "FRM:messaging@iamresponding.com\nSUBJ:Greater Lenox\nMSG:MVA - Personal Injury\n@MM 261.7 (261 70 I90 )",
         "CALL:MVA - Personal Injury",
         "PLACE:MM 261.7",
         "ADDR:261 70 I90",
         "MADDR:261 70 I 90");
 
     doTest("T10",
         "FRM:messaging@iamresponding.com\nSUBJ:Greater Lenox\nMSG:MVA - Unknown\nRAILROAD \\ DEPOT (, CANASTOTA VILLAGE)",
         "CALL:MVA - Unknown",
        "ADDR:RAILROAD \\ DEPOT",
         "CITY:CANASTOTA VILLAGE");
 
     doTest("T11",
         "FRM:messaging@iamresponding.com\nSUBJ:Greater Lenox\nMSG:Sick Person\n400 LAMB AV , CANASTOTA VILLAGE ( / DEPPOLITI AV)",
         "CALL:Sick Person",
         "ADDR:400 LAMB AV",
         "MADDR:400 LAMB AVE",
         "CITY:CANASTOTA VILLAGE",
         "X:DEPPOLITI AV");
 
     doTest("T12",
         "FRM:messaging@iamresponding.com\nSUBJ:Greater Lenox\nMSG:MVA - Personal Injury\n5050 BURLESON RD , LINCOLN ( VEDDER RD / FOREST AV)",
         "CALL:MVA - Personal Injury",
         "ADDR:5050 BURLESON RD",
         "CITY:LINCOLN",
         "X:VEDDER RD / FOREST AV");
 
     doTest("T13",
         "FRM:messaging@iamresponding.com\nSUBJ:Greater Lenox\nMSG:Falls\n400 LAMB AV #144, CANASTOTA VILLAGE ( / DEPPOLITI AV)",
         "CALL:Falls",
         "ADDR:400 LAMB AV",
         "MADDR:400 LAMB AVE",
         "APT:144",
         "CITY:CANASTOTA VILLAGE",
         "X:DEPPOLITI AV");
 
     doTest("T14",
         "(Greater Lenox) Convulsions/Seizures\n7216 NELSON RD , LENOX ( SENECA TRNPK / PAVONE PL)",
         "CALL:Convulsions/Seizures",
         "ADDR:7216 NELSON RD",
         "CITY:LENOX",
         "X:SENECA TRNPK / PAVONE PL");
 
     doTest("T15",
         "FRM:messaging@iamresponding.com\nSUBJ:Greater Lenox\nMSG:MVA - Unknown\nCANAL RD , LENOX",
         "CALL:MVA - Unknown",
         "ADDR:CANAL RD",
         "CITY:LENOX");
 
     doTest("T16",
         "FRM:messaging@iamresponding.com\nSUBJ:Greater Lenox\nMSG:Chest Pain\n@THERMOLD & RMH CORPORATION (7059 HARP RD (LENOX) )",
         "CALL:Chest Pain",
         "PLACE:THERMOLD & RMH CORPORATION",
         "ADDR:7059 HARP RD",
         "CITY:LENOX");
 
     doTest("T17",
         "FRM:messaging@iamresponding.com\n" +
         "SUBJ:Greater Lenox\n" +
         "MSG:Traumatic Injuries\n" +
         "@SCHOOL CANASTOTA ROBERTS ST JR SR HIGH SCHOOL (302 ROBERTS ST (CANASTOTA VILLAGE) )",
 
         "CALL:Traumatic Injuries",
         "PLACE:SCHOOL CANASTOTA ROBERTS ST JR SR HIGH SCHOOL",
         "ADDR:302 ROBERTS ST",
         "CITY:CANASTOTA VILLAGE");
 
     doTest("T18",
         "FRM:messaging@iamresponding.com\n" +
         "SUBJ:Greater Lenox\n" +
         "MSG:Psychiatric/Suicide Attempt\r\n" +
         "403 S PETERBORO , CANASTOTA VILLAGE ( / RASBACH)",
 
         "CALL:Psychiatric/Suicide Attempt",
         "ADDR:403 S PETERBORO",
         "CITY:CANASTOTA VILLAGE",
         "X:RASBACH");
 
     doTest("T19",
         "FRM:messaging@iamresponding.com\nSUBJ:Greater Lenox\nMSG:Headache\r\n206 BROAD , ONEIDA CITY INSIDE ( WASHINGTON AV / LENOX AV)",
         "CALL:Headache",
         "ADDR:206 BROAD",
         "CITY:ONEIDA CITY INSIDE",
         "X:WASHINGTON AV / LENOX AV");
 
     doTest("T20",
        "FRM:messaging@iamresponding.com\nSUBJ:Greater Lenox\nMSG:MVA - Unknown\r\nROUTE 31 \\\\ MORRIS RD (, LENOX)",
         "CALL:MVA - Unknown",
         "ADDR:ROUTE 31 & MORRIS RD",
         "CITY:LENOX");
 
   }
   
   public static void main(String[] args) {
     new NYMadisonCountyGLASParserTest().generateTests("T1", "CALL PLACE ADDR APT CITY X");
   }
 }
