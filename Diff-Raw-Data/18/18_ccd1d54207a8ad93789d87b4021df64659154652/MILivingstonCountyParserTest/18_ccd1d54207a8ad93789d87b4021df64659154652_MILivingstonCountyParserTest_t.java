 package net.anei.cadpage.parsers.MI;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 import net.anei.cadpage.parsers.MI.MILivingstonCountyParser;
 
 import org.junit.Test;
 
 
 public class MILivingstonCountyParserTest extends BaseParserTest {
   
   public MILivingstonCountyParserTest() {
     setParser(new MILivingstonCountyParser(), "LIVINGSTON COUNTY", "MI");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "CAD:FYI: ;ALARMF;11555 PLEASANT VIEW DR;INDIAN TRL;SMOKE ALARMS GOING OFF IN THE HOUSE FOR THE LAST 30 MIN ON AND OFF/ NO SMOKE OR SMOKE SHOWING [09/24/10 04:17:07 ECOOK]",
         "CALL:ALARMF",
         "ADDR:11555 PLEASANT VIEW DR",
         "X:INDIAN TRL",
         "INFO:SMOKE ALARMS GOING OFF IN THE HOUSE FOR THE LAST 30 MIN ON AND OFF/ NO SMOKE OR SMOKE SHOWING",
         "DATE:09/24/10",
         "TIME:04:17:07");
 
     doTest("T2",
         "CAD:FYI: ;UNKACC;E M36/PETTYS RD;VEHICLE HIT A POLE/ MALE RUNNING E/B FROM SCENE [09/23/10 21:45:06 ECOOK]",
         "CALL:UNKACC",
         "ADDR:E M36 & PETTYS RD",
         "INFO:VEHICLE HIT A POLE/ MALE RUNNING E/B FROM SCENE",
         "DATE:09/23/10",
         "TIME:21:45:06");
 
     doTest("T3",
         "CAD:FYI: ;CHSTPN;8422 PAWNEE TRL;SHOSHONI PASS;[Medical Priority Info] RESPONSE: P1 STA 1 4 5 6 7 8 FC FT RESPONDER SCRIPT: 72 year old, Female, Conscious, Breathing. Chest Pain (Non-Traumatic). Abnormal breathing. Cal",
         "CALL:CHSTPN",
         "ADDR:8422 PAWNEE TRL",
         "X:SHOSHONI PASS",
         "INFO:72 year old, Female, Conscious, Breathing. Chest Pain (Non-Traumatic). Abnormal breathing. Cal");
 
     doTest("T4",
         "CAD:FYI: ;UNCON;8732 RIVER VALLEY RD;[Medical Priority Info] RESPONSE: P1 STA 1 2 3 4 5 6 7 8 FC FT RESPONDER SCRIPT: 63 year old, Female, Conscious, Breathing. Unconscious / Fainting (Near). Not alert. Caller Statement: UNCON.",
         "CALL:UNCON",
         "ADDR:8732 RIVER VALLEY RD",
         "INFO:63 year old, Female, Conscious, Breathing. Unconscious / Fainting (Near). Not alert. Caller Statement: UNCON.");
 
     doTest("T5",
         "CAD:FYI: ;HEMLAC;SHEHAN RD/WHITEWOOD RD;[Medical Priority Info]RESPONSE: P1 STA 1 4 5 6 7 8 FC FT RESPONDER SCRIPT: 35 year old, Female, Conscious, Brea",
         "CALL:HEMLAC",
         "ADDR:SHEHAN RD & WHITEWOOD RD",
         "INFO:35 year old, Female, Conscious, Brea");
 
     doTest("T6",
         "Pagecopy-Fr:CAD@lc911\nCAD:FYI: ;CHSTPN;7251 HAMBURG RD;WELLE RD;[Medical Priority Info] RESPONSE: P1 STA 1 3 4 5 6 7 8 FC FT RESPONDER S",
         "CALL:CHSTPN",
         "ADDR:7251 HAMBURG RD",
         "X:WELLE RD");
 
     doTest("T7",
         "CAD:FYI: ;CITAF;5579 E GRAND RIVER;WILDWOOD DR;Event spawned from CITIZEN ASSIST LAW. [12/10/10 20:08:59 SPHILLIPS] CALLER LIVES NEXT DOOR TO THE ADDRESS OF THE WATER MAIN BREAK [12/10/10 20:04:40 HROSSNER] CALLER ADV OF A WATER MAIN BREAK",
         "CALL:CITAF",
         "ADDR:5579 E GRAND RIVER",
         "X:WILDWOOD DR",
         "INFO:Event spawned from CITIZEN ASSIST LAW. / CALLER LIVES NEXT DOOR TO THE ADDRESS OF THE WATER MAIN BREAK / CALLER ADV OF A WATER MAIN BREAK",
         "DATE:12/10/10",
         "TIME:20:08:59");
 
     doTest("T8",
         "CAD:FYI: ;CITAF;5579 E GRAND RIVER;WILDWOOD DR;Event spawned from CITIZEN ASSIST LAW. [12/10/10 20:08:59 SPHILLIPS] CALLER LIVES NEXT DOOR TO THE ADDRESS OF THE WATER MAIN BREAK [12/10/10 20:04:40 HROSSNER] CALLER ADV OF A WATER MAIN BREAK",
         "CALL:CITAF",
         "ADDR:5579 E GRAND RIVER",
         "X:WILDWOOD DR",
         "INFO:Event spawned from CITIZEN ASSIST LAW. / CALLER LIVES NEXT DOOR TO THE ADDRESS OF THE WATER MAIN BREAK / CALLER ADV OF A WATER MAIN BREAK",
         "DATE:12/10/10",
         "TIME:20:08:59");
 
     doTest("T9",
         "CAD:FYI: ;UNCON;SANCTURY OF WOODLAND;7533 W GRAND RIVER;COURT ST;[Medical Priority Info] RESPONSE: P1 STA 1 2 3 4 5 6 7 8 FC FT RESPONDER SCRIPT: 80 year old, Female, Unconscious, Breathing. Unconscious / Fainting (Near). Unconscious -- Eff",
         "CALL:UNCON",
         "ADDR:7533 W GRAND RIVER",
         "PLACE:SANCTURY OF WOODLAND",
         "X:COURT ST",
         "INFO:80 year old, Female, Unconscious, Breathing. Unconscious / Fainting (Near). Unconscious -- Eff");
 
     doTest("T10",
         "CAD:FYI: ;DIABET;31 STRATFORD LN;NEWCASTLE LN;[Medical Priority Info] RESPONSE: P1 STA 1 2 4 5 6 7 8 FC FT RESPONDER SCRIPT: 47 year old, Female, :1of2",
         "CALL:DIABET",
         "ADDR:31 STRATFORD LN",
         "X:NEWCASTLE LN",
         "INFO:47 year old, Female,");
 
     doTest("T11",
         "Pagecopy-Fr:CAD@livingstoncounty.livco\nCAD:FYI: ;OVDOSE;4676 KENMORE DR;[Medical Priority Info] RESPONSE: P1 STA 1",
         "CALL:OVDOSE",
         "ADDR:4676 KENMORE DR");
 
     doTest("T12",
         "firediver11+caf_=5176671194=vtext.com@gmail.comPagecopy-Fr:CAD@livingstoncounty.livco\nCAD:FYI: ;SEIZUR;131 STRATFORD LN;BELMONT LN;[Medical Priority Info]",
         "CALL:SEIZUR",
         "ADDR:131 STRATFORD LN",
         "X:BELMONT LN");
 
     doTest("T13",
         "Pagecopy-Fr:CAD@livingstoncounty.livco CAD:Update: ;FALL;3031 WM36;[EMS] HAS BEEN VOMITTING - DIABETIC [02/14/11 09:55:08 RLADOUCEUR] [Me",
         "CALL:FALL",
         "ADDR:3031 WM36",
         "INFO:HAS BEEN VOMITTING - DIABETIC",
         "DATE:02/14/11",
         "TIME:09:55:08");
 
     doTest("T14",
         "CAD:FYI: ;OPENBU;8360 MCCLEMENTS RD;DEKASSEL DR;BURNING GARBAGE ONE HOUSE EAST OF CALLER [07/01/11 19:44:56 SSTURGIS]",
         "CALL:OPENBU",
         "ADDR:8360 MCCLEMENTS RD",
         "X:DEKASSEL DR",
         "INFO:BURNING GARBAGE ONE HOUSE EAST OF CALLER",
         "DATE:07/01/11",
         "TIME:19:44:56");
 
     doTest("T15",
         "FYI: ;SMKOUT;CHEROKEE BEND DR/RED OAKS DR;CALLER HEARD AN EXPLOSION ON CHEROKEE BEND, DOES NOT SEE ANYTHING AT THIS TIME. SMELLS SMOKE. [09/21/11 21:53:2",
         "CALL:SMKOUT",
         "ADDR:CHEROKEE BEND DR & RED OAKS DR",
         "INFO:CALLER HEARD AN EXPLOSION ON CHEROKEE BEND, DOES NOT SEE ANYTHING AT THIS TIME. SMELLS SMOKE.");
 
     doTest("T16",
         "FYI: ;CHOKE;SORG RESIDENCE;2917 LANIER CT;TAHOE BLVD;[Medical Priority Info] RESPONSE: P1 STA 1 2 3 4 5 6 7 8 FC FT RESPONDER SCRIPT: 3 year old, Male, C",
         "CALL:CHOKE",
         "ADDR:2917 LANIER CT",
         "PLACE:SORG RESIDENCE",
         "X:TAHOE BLVD",
         "INFO:3 year old, Male, C");
 
     doTest("T17",
         "FRM:CAD@livingstoncounty.livco\nMSG:CAD:FYI: ;PIA;DORR RD/E GRAND RIVER;JEEP LIBERTY VS SILVER VAN [11/30/11 09:18:58 CWESTPHAL]",
         "CALL:PIA",
         "ADDR:DORR RD & E GRAND RIVER",
         "INFO:JEEP LIBERTY VS SILVER VAN",
         "DATE:11/30/11",
         "TIME:09:18:58");
 
     doTest("T18",
         "CAD:CANCEL;N NATIONAL ST/E GRAND RIVER AVE; HOWL;AT CORNER",
         "CALL:CANCEL",
         "ADDR:N NATIONAL ST & E GRAND RIVER AVE",
         "CITY:HOWELL",
         "INFO:AT CORNER");
 
     doTest("T19",
         "CAD:Update: ;OIOUT;REGAL RECYCLERS;645 LUCY RD;INDUSTRIAL ST;{EN34} REQ CONSUMERS [12/28/11 17:51:25 LLOWE]",
         "CALL:OIOUT",
         "ADDR:645 LUCY RD",
         "PLACE:REGAL RECYCLERS",
         "X:INDUSTRIAL ST",
         "INFO:{EN34} REQ CONSUMERS",
         "DATE:12/28/11",
         "TIME:17:51:25");
 
     doTest("T20",
         "CAD:FYI: ;CHOKE;5385 GREEN RD;CLYDE RD;[Medical Priority Info] RESPONSE: P1 STA 1 2 3 4 5 6 7 8<tel:12345678> FC FT RESPONDER SCRIPT: 78 year old, Male, Conscious, Not breathing. Choking. Not alert. Caller Statement: CHOKING ON FOOD . 1.He is n",
         "CALL:CHOKE",
         "ADDR:5385 GREEN RD",
         "X:CLYDE RD",
         "INFO:78 year old, Male, Conscious, Not breathing. Choking. Not alert. Caller Statement: CHOKING ON FOOD . 1.He is n");
 
     doTest("T21",
         "CAD:FYI: ;PIA;N LATSON RD/E HIGHLAND RD;RED TRUCK VS SMALL SEDAN PER CALLER SUBJECT IS NOT OK [12/28/11 19:50:00 MSPICER]",
         "CALL:PIA",
         "ADDR:N LATSON RD & E HIGHLAND RD",
         "INFO:RED TRUCK VS SMALL SEDAN PER CALLER SUBJECT IS NOT OK",
         "DATE:12/28/11",
         "TIME:19:50:00");
 
     doTest("T22",
         "CAD:FYI: ;SICK;10165 CARLEE JUNE DR;RUNYAN LAKE RD;[Medical Priority Info] RESPONSE: P1 STA 1 4 5 6 7 8 FC FT RESPONDER SCRIPT: 37 year old, Male, Consci",
         "CALL:SICK",
         "ADDR:10165 CARLEE JUNE DR",
         "X:RUNYAN LAKE RD",
         "INFO:37 year old, Male, Consci");

    doTest("T23",
        "Update: ;TREEF;5117 CULVER RD;CANYON OAKS DR;CALLER STATES TREE COMPLETELY BLOCKING CULVER RD, [08/11/12 02:25:45 JMURPHY-KE]",
        "CALL:TREEF",
        "ADDR:5117 CULVER RD",
        "X:CANYON OAKS DR",
        "INFO:CALLER STATES TREE COMPLETELY BLOCKING CULVER RD,",
        "DATE:08/11/12",
        "TIME:02:25:45");

    doTest("T24",
        "Update: ;DEATH;4601 GOLF VIEW DR;GOLF VIEW CT;[Medical Priority Info] RESPONSE: P1 STA 1 2 3 4 5 6 7 8 FC FT RESPONDER SCRIPT: 83 year old, Male, Unconsc",
        "CALL:DEATH",
        "ADDR:4601 GOLF VIEW DR",
        "X:GOLF VIEW CT",
        "INFO:83 year old, Male, Unconsc");
 }
   
   public static void main(String[] args) {
     new MILivingstonCountyParserTest().generateTests("T1");
   }
 }
