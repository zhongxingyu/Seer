 package net.anei.cadpage.parsers.NC;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class NCJohnstonCountyParserTest extends BaseParserTest {
   
   public NCJohnstonCountyParserTest() {
     setParser(new NCJohnstonCountyParser(), "JOHNSTON COUNTY", "NC");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "CAD:Evacuation In-Progress;70 MYSTICAL CT; GARN;(S)JORDAN RIDGE (N)",
         "CALL:Evacuation In-Progress",
         "ADDR:70 MYSTICAL CT",
         "X:GARN & (S)JORDAN RIDGE (N)");
 
     doTest("T2",
         "CAD:OPS5;CLD;52C02C;CARBON MONOXIDE ALARM;70 MYSTICAL CT;(S)JORDAN RIDGE (N);GALAXY DR;DENA HOLLOMAN;12/28/2010 22:20:35;CLD1",
         "INFO:OPS5",
         "SRC:CLD",
         "CODE:52C02C",
         "CALL:CARBON MONOXIDE ALARM",
         "ADDR:70 MYSTICAL CT",
         "X:(S)JORDAN RIDGE (N) & GALAXY DR",
         "NAME:DENA HOLLOMAN",
         "DATE:12/28/2010",
         "TIME:22:20:35");
 
     doTest("T3",
         "CAD:CLDFR;CLD;17B01G;INJURED PERSON;104 LAKEVIEW CT;(S)SOUTH RIDGE (N);SOUTH RIDGE DR;DOBERT, HENRY;12/28/2010 11:00:58;CLDR1",
         "INFO:CLDFR",
         "SRC:CLD",
         "CODE:17B01G",
         "CALL:INJURED PERSON",
         "ADDR:104 LAKEVIEW CT",
         "X:(S)SOUTH RIDGE (N) & SOUTH RIDGE DR",
         "NAME:DOBERT, HENRY",
         "DATE:12/28/2010",
         "TIME:11:00:58");
 
     doTest("T4",
         "CAD:OPS;CLD;10C02;CHEST PAINS;643 BIG PINE RD;PEACE LN;GOVERNMENT RD;ESTERLENE MILLER;02/09/2011 15:21:31;CLDR2",
         "INFO:OPS",
         "SRC:CLD",
         "CODE:10C02",
         "CALL:CHEST PAINS",
         "ADDR:643 BIG PINE RD",
         "X:PEACE LN & GOVERNMENT RD",
         "NAME:ESTERLENE MILLER",
         "DATE:02/09/2011",
         "TIME:15:21:31");
 
     doTest("T5",
         "CAD:OPS;CLD;06D02;DIFFICULTY BREATHING;101 CHERRY CT;ALLEN RD;DAVIS, JOHN;02/09/2011 15:08:36;CLDR1",
         "INFO:OPS",
         "SRC:CLD",
         "CODE:06D02",
         "CALL:DIFFICULTY BREATHING",
         "ADDR:101 CHERRY CT",
         "X:ALLEN RD",
         "NAME:DAVIS, JOHN",
         "DATE:02/09/2011",
         "TIME:15:08:36");
 
     doTest("T6",
         "CAD:OPS4;CLD;52B01S;FIRE ALARM;330 STEEL BRIDGE RD;CONTENDER DR;CLEVELAND RD;BROADVIEW;02/09/2011 12:18:09;CLD1",
         "INFO:OPS4",
         "SRC:CLD",
         "CODE:52B01S",
         "CALL:FIRE ALARM",
         "ADDR:330 STEEL BRIDGE RD",
         "X:CONTENDER DR & CLEVELAND RD",
         "NAME:BROADVIEW",
         "DATE:02/09/2011",
         "TIME:12:18:09");
 
     doTest("T7",
         "CAD:OPS4;CLD;MVA PERSONAL INJURY;WATKINS RD/CLEVELAND RD;SPRINT PCS;02/08/2011 07:18:56;CLD1",
         "INFO:OPS4",
         "SRC:CLD",
         "CALL:MVA PERSONAL INJURY",
         "ADDR:WATKINS RD & CLEVELAND RD",
         "X:SPRINT PCS",
         "DATE:02/08/2011",
         "TIME:07:18:56");
 
     doTest("T8",
         "CAD:OPS4;CLD;GAS LEAK NATURAL GAS/PROPANE;105 JOSEPHINE RD;CLEVELAND RD;MOONLIGHT DR;POLENTA ELEMENTARY SCHOOL;SUSAN;02/08/2011",
         "INFO:OPS4",
         "SRC:CLD",
         "CALL:GAS LEAK NATURAL GAS/PROPANE",
         "ADDR:105 JOSEPHINE RD",
         "X:CLEVELAND RD & MOONLIGHT DR",
         "NAME:POLENTA ELEMENTARY SCHOOL",
         "DATE:02/08/2011");
 
     doTest("T9",
         "CAD:No Add`l Personnel / Unit;105 JOSEPHINE RD; GARN",
         "CALL:No Add`l Personnel / Unit",
         "ADDR:105 JOSEPHINE RD",
         "X:GARN");
 
     doTest("T10",
         "CAD:OPS7;CLD;VEHICLE FIRE;314 I-40 HWY;314.5;SPRINT PCS;02/07/2011 15:54:03;CLD1",
         "INFO:OPS7",
         "SRC:CLD",
         "CALL:VEHICLE FIRE",
         "ADDR:314 I-40 HWY",
         "MADDR:314 I 40",
         "X:314.5 & SPRINT PCS",
         "DATE:02/07/2011",
         "TIME:15:54:03");
 
     doTest("T11",
         "CAD:Standby At The Station;314 I-40 HWY; BENS;314.5",
         "CALL:Standby At The Station",
         "ADDR:314 I-40 HWY",
         "MADDR:314 I 40",
         "X:BENS & 314.5");
 
     doTest("T12",
         "CAD:No Add`l Personnel / Unit;314 I-40 HWY; BENS;314.5",
         "CALL:No Add`l Personnel / Unit",
         "ADDR:314 I-40 HWY",
        "MADDR:314 I 40",
         "X:BENS & 314.5");
 
     doTest("T13",
         "CAD:OPS;FOD;17A01G;INJURED PERSON;439 LONG BRANCH RD;BLACK CREEK RD;BANKS ST;DUPREE, KENNETH L & CATHERINE;02/14/2011 07:25:26;FODR1",
         "INFO:OPS",
         "SRC:FOD",
         "CODE:17A01G",
         "CALL:INJURED PERSON",
         "ADDR:439 LONG BRANCH RD",
         "X:BLACK CREEK RD & BANKS ST",
         "NAME:DUPREE, KENNETH L & CATHERINE");
 
     doTest("T14",
         "CAD:OPS;STA6;17A01;INJURED PERSON;439 LONG BRANCH RD;BLACK CREEK RD;BANKS ST;DUPREE, KENNETH L & CATHERINE;02/14/2011 07:08:53;EMS62",
         "INFO:OPS",
         "SRC:STA6",
         "CODE:17A01",
         "CALL:INJURED PERSON",
         "ADDR:439 LONG BRANCH RD",
         "X:BLACK CREEK RD & BANKS ST",
         "NAME:DUPREE, KENNETH L & CATHERINE");
 
     doTest("T15",
         "CAD:OPS;STA6;CVA/STROKE;3800-B US 701 HWY S;STEWART RD;PEACH ORCHARD RD;BENSON AREA MEDICAL CENTER INC;02/14/2011 15:49:44;FOR6X,EMS52",
         "INFO:OPS",
         "SRC:STA6",
         "CALL:CVA/STROKE",
         "ADDR:3800-B US 701 HWY S",
         "MADDR:3800 US 701 S",
         "X:STEWART RD & PEACH ORCHARD RD",
         "NAME:BENSON AREA MEDICAL CENTER INC");
 
     doTest("T16",
         "CAD:OPS;STA6;26C01;SUDDEN ILLNESS;565 BOYETTE RD;(S)FOUR OAKS ASSISTED LIVING (N);KEEN RD;US 301 HWY S;OAKVIEW COMMONS;OAKVIEW",
         "INFO:OPS",
         "SRC:STA6",
         "CODE:26C01",
         "CALL:SUDDEN ILLNESS",
         "ADDR:565 BOYETTE RD",
         "X:(S)FOUR OAKS ASSISTED LIVING (N) & KEEN RD",
         "NAME:US 301 HWY S");
 
     doTest("T17",
         "CAD:OPS;STA6;DIFFICULTY BREATHING;200 E WELLONS ST;S BAKER ST;BROWN ST;KINGS RESTAURANT;03/29/2011 02:33:20;EMS62",
         "INFO:OPS",
         "SRC:STA6",
         "CALL:DIFFICULTY BREATHING",
         "ADDR:200 E WELLONS ST",
         "X:S BAKER ST & BROWN ST",
         "NAME:KINGS RESTAURANT");
 
     doTest("T18",
         "CAD:CLD;06D02;DIFF BREATHING 06-D-02;77 CARRIAGE CREEK DR;(S)CARRIAGE CREEK (N);STEEP HILL CT;BRADLEY, EDWARD;08/07/2011 20:15:39;CLDR1",
         "INFO:CLD",
         "SRC:06D02",
         "CALL:DIFF BREATHING 06-D-02",
         "ADDR:77 CARRIAGE CREEK DR",
         "X:(S)CARRIAGE CREEK (N) & STEEP HILL CT",
         "NAME:BRADLEY, EDWARD",
         "DATE:08/07/2011",
         "TIME:20:15:39");
 
     doTest("T19",
         "CAD:No Add`l Personnel / Unit;1203 LIVE OAK CHURCH RD; SELM;6(S)PECAN GROVE MHP (N)",
         "CALL:No Add`l Personnel / Unit",
         "ADDR:1203 LIVE OAK CHURCH RD",
         "X:SELM & 6(S)PECAN GROVE MHP (N)");
 
     doTest("T20",
         "CAD:OPS;CLD;31D02;UNCONSCIOUS PERSON 31-D-02;16 CARRIAGE CREEK DR;(S)CARRIAGE CREEK (N);CLEVELAND RD;STEEP HILL CT;MCDANIELS, ALBERT M;01/26/2012 06:25:19;CLDR1",
         "INFO:OPS",
         "SRC:CLD",
         "CODE:31D02",
         "CALL:UNCONSCIOUS PERSON 31-D-02",
         "ADDR:16 CARRIAGE CREEK DR",
         "X:(S)CARRIAGE CREEK (N) & CLEVELAND RD & STEEP HILL CT",
         "NAME:MCDANIELS, ALBERT M",
         "DATE:01/26/2012",
         "TIME:06:25:19");
 
     doTest("T21",
         "FRM:cad paging \n" +
         "MSG:CAD:OPS;CLD;06D01;DIFF BREATHING 06-D-01;107 GARNET LN;(S)SWIFTVIEW (N);GOVERNMENT RD;AVERY, B A;03/14/2012 09:54:04;CLDR1",
 
         "INFO:OPS",
         "SRC:CLD",
         "CODE:06D01",
         "CALL:DIFF BREATHING 06-D-01",
         "ADDR:107 GARNET LN",
         "X:(S)SWIFTVIEW (N) & GOVERNMENT RD",
         "NAME:AVERY, B A",
         "DATE:03/14/2012",
         "TIME:09:54:04");
   }
   
   public static void main(String[] args) {
     new NCJohnstonCountyParserTest().generateTests("T1");
   }
 }
