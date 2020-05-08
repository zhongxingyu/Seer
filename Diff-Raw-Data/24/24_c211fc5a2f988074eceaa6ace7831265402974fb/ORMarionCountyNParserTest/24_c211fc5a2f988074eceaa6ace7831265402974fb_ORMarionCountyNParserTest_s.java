 package net.anei.cadpage.parsers.OR;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class ORMarionCountyNParserTest extends BaseParserTest {
   
   public ORMarionCountyNParserTest() {
     setParser(new ORMarionCountyNParser(), "MARION COUNTY", "OR");
   }
   
   @Test
   public void testParser1() {
 
     doTest("T1",
         "[(49962]  : ) TONE:  819::RAILWAY:AV:::::3030:VTON,STAF,:SAFE COM STA1@1830/APP CKS@1900 EMS@1945 STA1:20110302:174858",
         "ID:49962",
         "CALL:TONE",
         "ADDR:819 RAILWAY AV",
         "MADDR:819 RAILWAY AVE",
         "MAP:3030",
         "UNIT:VTON,STAF",
         "INFO:SAFE COM STA1@1830/APP CKS@1900 EMS@1945 STA1",
         "DATE:03/02/2011",
         "TIME:17:48:58");
 
     doTest("T2",
         "[(45354]  : ) CVA: 1732::PINE:ST:9::::3029:M24,R404,D411,STAF,N7,:35 Y M C/B/A LEFT SIDE PARALYSIS:20110223:110234",
         "ID:45354",
         "CALL:CVA",
         "ADDR:1732 PINE ST",
         "APT:9",
         "MAP:3029",
         "UNIT:M24,R404,D411,STAF,N7",
         "INFO:35 Y M C/B/A LEFT SIDE PARALYSIS",
         "DATE:02/23/2011",
         "TIME:11:02:34");
 
     doTest("T3",
         "[(47486]  : ) STRC:::ABIQUA:RD:::HW 213::2833:E435,E485,E405,E415,D411,T489,T418,T419,R484,PGE,TIME,STAF,N7,:LARGE FLAMES APPX 2 MILES EAST:20110226:183324",
         "ID:47486",
         "CALL:STRC",
         "ADDR:ABIQUA RD & HW 213",
         "MADDR:ABIQUA RD & HWY 213",
         "MAP:2833",
         "UNIT:E435,E485,E405,E415,D411,T489,T418,T419,R484,PGE,TIME,STAF,N7",
         "INFO:LARGE FLAMES APPX 2 MILES EAST",
         "DATE:02/26/2011",
         "TIME:18:33:24");
 
     doTest("T4",
         "[(48144]  : ) SICK: 5024::CROOKED FINGER:RD:::::3340:E485,D411,:75YOM C/B/A DIZZY:20110228:043844",
         "ID:48144",
         "CALL:SICK",
         "ADDR:5024 CROOKED FINGER RD",
         "MAP:3340",
         "UNIT:E485,D411",
         "INFO:75YOM C/B/A DIZZY",
         "DATE:02/28/2011",
         "TIME:04:38:44");
 
     doTest("T5",
         "[(50323]  : ) BREA:12884::MASCHER:RD:::::3230:E415,D411,STAF,:85YOF C/A/SOB FAST HEART RATE/150:20110303:140259",
         "ID:50323",
         "CALL:BREA",
         "ADDR:12884 MASCHER RD",
         "MAP:3230",
         "UNIT:E415,D411,STAF",
         "INFO:85YOF C/A/SOB FAST HEART RATE/150",
         "DATE:03/03/2011",
         "TIME:14:02:59");
 
     doTest("T6",
         "[(50791]  : ) FALL:  930::OAK:ST:130::::3031:M23,R404,D411,N7,:99YOF C/A/B GLF R HIP PAIN:20110304:073911",
         "ID:50791",
         "CALL:FALL",
         "ADDR:930 OAK ST",
         "APT:130",
         "MAP:3031",
         "UNIT:M23,R404,D411,N7",
         "INFO:99YOF C/A/B GLF R HIP PAIN",
         "DATE:03/04/2011",
         "TIME:07:39:11");
 
     doTest("T7",
         "[(52678]  : ) ALM: 1106::PINE:ST:::::3030:E405,D411,N7,:CO2 DETECTOR ALRM AT COMM LOC:20110306:215554",
         "ID:52678",
         "CALL:ALM",
         "ADDR:1106 PINE ST",
         "MAP:3030",
         "UNIT:E405,D411,N7",
         "INFO:CO2 DETECTOR ALRM AT COMM LOC",
         "DATE:03/06/2011",
         "TIME:21:55:54");
 
     doTest("T8",
         "[(52806]  : ) UNC: 5870::VALLEY VIEW:RD:::::3032:M24,R404,D411,N7,:65YOM UNC/NOT BREA:20110307:073728",
         "ID:52806",
         "CALL:UNC",
         "ADDR:5870 VALLEY VIEW RD",
         "MAP:3032",
         "UNIT:M24,R404,D411,N7",
         "INFO:65YOM UNC/NOT BREA",
         "DATE:03/07/2011",
         "TIME:07:37:28");
 
     doTest("T9",
         "[(53886]  : ) MISC:12622::SILVERTON:RD:::::3030:E415,D411,N7,:FLAMES SEEN FROM FIELD NEAR BY:20110308:190734",
         "ID:53886",
         "CALL:MISC",
         "ADDR:12622 SILVERTON RD",
         "MAP:3030",
         "UNIT:E415,D411,N7",
         "INFO:FLAMES SEEN FROM FIELD NEAR BY",
         "DATE:03/08/2011",
         "TIME:19:07:34");
 
     doTest("T10",
         "[(53944]  : ) SUIC: 1418::PINE:ST:D::::3030:M24,R404,D411,N7,:64YOF/C/B/SUIC ATTEMPT, SLASHED WRISTS:20110308:194443",
         "ID:53944",
         "CALL:SUIC",
         "ADDR:1418 PINE ST",
         "APT:D",
         "MAP:3030",
         "UNIT:M24,R404,D411,N7",
         "INFO:64YOF/C/B/SUIC ATTEMPT, SLASHED WRISTS",
         "DATE:03/08/2011",
         "TIME:19:44:43");
 
     doTest("T11",
         "[(54072]  : ) MVA:  918:N:FIRST:ST:::::3031:E405,E435,R404,D411,M23,N7,:ONE VEH ROLLOVER IFO ROTHS STORE:20110309:022055",
         "ID:54072",
         "CALL:MVA",
         "ADDR:918 N FIRST ST",
         "MAP:3031",
         "UNIT:E405,E435,R404,D411,M23,N7",
         "INFO:ONE VEH ROLLOVER IFO ROTHS STORE",
         "DATE:03/09/2011",
         "TIME:02:20:55");
 
     doTest("T12",
         "[(45886]  : ) MVA:::CASCADE:HW:::STATE:ST:3630:E425,D411,R404,M31,SBLE,SBLR,PGE,E415,LAW,N7,:SINGLE VEH ROLLOVER/ENTRAPMENT, NO INJ:20110224:063549",
         "ID:45886",
         "CALL:MVA",
         "ADDR:CASCADE HW & STATE ST",
         "MADDR:CASCADE HWY & STATE ST",
         "MAP:3630",
         "UNIT:E425,D411,R404,M31,SBLE,SBLR,PGE,E415,LAW,N7",
         "INFO:SINGLE VEH ROLLOVER/ENTRAPMENT, NO INJ",
         "DATE:02/24/2011",
         "TIME:06:35:49");
 
     doTest("T13",
         "((56873) : ) MISC: 8170::CASCADE:HW:::::2733:E485,D411,N7,:POWER LINE DOWN W/LARGE TREE BLKING:20110313:152421",
         "ID:56873",
         "CALL:MISC",
         "ADDR:8170 CASCADE HW",
         "MADDR:8170 CASCADE HWY",
         "MAP:2733",
         "UNIT:E485,D411,N7",
         "INFO:POWER LINE DOWN W/LARGE TREE BLKING",
         "DATE:03/13/2011",
         "TIME:15:24:21");
 
     doTest("T14",
         "((56971) : ) AOA:34000:S:ELLIS:RD:::::2737:E485,N7,:LARGE TREE ONTO HOUSE/UNK FURTHER:20110313:154421",
         "ID:56971",
         "CALL:AOA",
         "ADDR:34000 S ELLIS RD",
         "MAP:2737",
         "UNIT:E485,N7",
         "INFO:LARGE TREE ONTO HOUSE/UNK FURTHER",
         "DATE:03/13/2011",
         "TIME:15:44:21");
 
     doTest("T15",
         "((57141) : ) UNC: 3351::SEMINOLE:RD:::::3234:M24,R404,D411,N7,:86 YOF UNC/DIFF B/NOT ALERT:20110313:203929",
         "ID:57141",
         "CALL:UNC",
         "ADDR:3351 SEMINOLE RD",
         "MAP:3234",
         "UNIT:M24,R404,D411,N7",
         "INFO:86 YOF UNC/DIFF B/NOT ALERT",
         "DATE:03/13/2011",
         "TIME:20:39:29");
 
     doTest("T16",
         "((57514) : ) UNC:  728:W:MAIN:ST:::::3030:M24,R404,D411,STAF,N7,:74 YOM IN SEIZ:20110314:124736",
         "ID:57514",
         "CALL:UNC",
         "ADDR:728 W MAIN ST",
         "MAP:3030",
         "UNIT:M24,R404,D411,STAF,N7",
         "INFO:74 YOM IN SEIZ",
         "DATE:03/14/2011",
         "TIME:12:47:36");
 
     doTest("T17",
         "((57903) : ) MVA:::MT ANGEL:HW:::HOBART:RD:2929:E405,R404,M24,D411,LAW,STAF,N7,:2 VEH MVA/ELDERLY",
         "ID:57903",
         "CALL:MVA",
         "ADDR:MT ANGEL HW & HOBART RD",
         "MADDR:MT ANGEL HWY & HOBART RD",
         "MAP:2929",
         "UNIT:E405,R404,M24,D411,LAW,STAF,N7",
         "INFO:2 VEH MVA/ELDERLY");
 
     doTest("T18",
         "((58711) : ) CVA:  173::STEELHAMMER:RD:::::3031:M24,R404,D411,STAF,N7,:93YOF C/A/B HX OF CVA/NOT ABLE TO SPEAK:20110316:103935",
         "ID:58711",
         "CALL:CVA",
         "ADDR:173 STEELHAMMER RD",
         "MAP:3031",
         "UNIT:M24,R404,D411,STAF,N7",
         "INFO:93YOF C/A/B HX OF CVA/NOT ABLE TO SPEAK",
         "DATE:03/16/2011",
         "TIME:10:39:35");
 
     doTest("T19",
         "((58975) : ) PBLC:  206::WESTFIELD:ST:::::3030:E415,D411,STAF,N7,:REQ D411 RESPOND W/LADDER TO GET IN WINDOW:20110316:141622",
         "ID:58975",
         "CALL:PBLC",
         "ADDR:206 WESTFIELD ST",
         "MAP:3030",
         "UNIT:E415,D411,STAF,N7",
         "INFO:REQ D411 RESPOND W/LADDER TO GET IN WINDOW",
         "DATE:03/16/2011",
         "TIME:14:16:22");
 
     doTest("T20",
         "((4287) : ) MVA:::STATE:ST:::CASCADE:HW:3630:E425,D411,R404,M24,E52,R484,PGE,E415,LAW,N7,:1 MVA/POSS INJ/NOT BLOCKING:20111003:205247",
         "ID:4287",
         "CALL:MVA",
         "ADDR:STATE ST & CASCADE HW",
         "MADDR:STATE ST & CASCADE HWY",
         "MAP:3630",
         "UNIT:E425,D411,R404,M24,E52,R484,PGE,E415,LAW,N7",
         "INFO:1 MVA/POSS INJ/NOT BLOCKING",
         "DATE:10/03/2011",
         "TIME:20:52:47");
 
   }
   
   @Test
   public void testParser2() {
 
     doTest("T1",
         "(14036) :  / TRAU:10581::SARATOGA:DR:::::2628:R454,M23,N3,:65 YOF C/B/A LEG PAIN FROM FALL FROM FARM EQ:20110728:220453\n",
         "ID:14036",
         "CALL:TRAU",
         "ADDR:10581 SARATOGA DR",
         "MAP:2628",
         "UNIT:R454,M23,N3",
         "INFO:65 YOF C/B/A LEG PAIN FROM FALL FROM FARM EQ",
         "DATE:07/28/2011",
         "TIME:22:04:53");
 
     doTest("T2",
         "(13523) :  /  UNC:  830:N:MAIN:ST:9::::2530:R454,M22,:77YOM UNC/NOT RESP:20110728:114333\n",
         "ID:13523",
         "CALL:UNC",
         "ADDR:830 N MAIN ST",
         "APT:9",
         "MAP:2530",
         "UNIT:R454,M22",
         "INFO:77YOM UNC/NOT RESP",
         "DATE:07/28/2011",
         "TIME:11:43:33");
 
     doTest("T3",
         "(15331) :| AOA:  141::14TH:ST:LYONS::::3030:E405,E415,N7,:REQ ENGINE TO RESPOND TO FIRE SCN:20110730:170018 \n",
         "ID:15331",
         "CALL:AOA",
         "ADDR:141 14TH ST",
         "CITY:LYONS",
         "MAP:3030",
         "UNIT:E405,E415,N7",
         "INFO:REQ ENGINE TO RESPOND TO FIRE SCN",
         "DATE:07/30/2011",
         "TIME:17:00:18");
 
     doTest("T4",
         "(25615) :  /  CVA:  455:W:MARQUAM:ST:33::::2630:R454,M24,N7,:65 YOM POSS CVA  C/B/A SIDE OF FACE DROOPY:20110812:150536\n",
         "ID:25615",
         "CALL:CVA",
         "ADDR:455 W MARQUAM ST",
         "APT:33",
         "MAP:2630",
         "UNIT:R454,M24,N7",
         "INFO:65 YOM POSS CVA  C/B/A SIDE OF FACE DROOPY",
         "DATE:08/12/2011",
         "TIME:15:05:36");
 
     doTest("T5",
         "((2463) : ) UNC:20510::BOONES FERRY:RD:::::1530:R903,M21,N1,:22YOF C/DIFF B/SEMI ALERT:20110929:064217",
         "ID:2463",
         "CALL:UNC",
         "ADDR:20510 BOONES FERRY RD",
         "MAP:1530",
         "UNIT:R903,M21,N1",
         "INFO:22YOF C/DIFF B/SEMI ALERT",
         "DATE:09/29/2011",
         "TIME:06:42:17");
 
     doTest("T6",
         "((836) Hi Gina ) LIFT: 4279::THIRD:ST:::::1829:E876,N3,:80 YOF C/B/A  NON INJURY LIFT ASSIST:20110930:090843",
         "ID:836",
         "CALL:LIFT",
         "ADDR:4279 THIRD ST",
         "MAP:1829",
         "UNIT:E876,N3",
         "INFO:80 YOF C/B/A  NON INJURY LIFT ASSIST",
         "DATE:09/30/2011",
         "TIME:09:08:43");
 
     doTest("T7",
         "((339) Hi Gina ) COMM:  490:S:PACIFIC:HW:::::2127:E21,E22,E24,E875,LA21,A21,TIME,PGE,N3,:LOTS OF SMOKE COMING FROM BACK OF LOC/:20110929:222851",
         "ID:339",
         "CALL:COMM",
         "ADDR:490 S PACIFIC HW",
         "MADDR:490 S PACIFIC HWY",
         "MAP:2127",
         "UNIT:E21,E22,E24,E875,LA21,A21,TIME,PGE,N3",
         "INFO:LOTS OF SMOKE COMING FROM BACK OF LOC/",
         "DATE:09/29/2011",
         "TIME:22:28:51");
 
     doTest("T8",
         "((432) Hi Gina ) SICK:  950::EVERGREEN:RD:211::::2026:M23,E22,N3,:86 YF C/B/A FEELING DIZZY:20110930:000053",
         "ID:432",
         "CALL:SICK",
         "ADDR:950 EVERGREEN RD",
         "APT:211",
         "MAP:2026",
         "UNIT:M23,E22,N3",
         "INFO:86 YF C/B/A FEELING DIZZY",
         "DATE:09/30/2011",
         "TIME:00:00:53");
 
     doTest("T10",
         "(639) :  / SICK:25496:S:HW 99E::::::1433:R903,OPPS,N1,:65YOM C/A/B DIZZY,SWEATING,:20110930:070050\n",
         "ID:639",
         "CALL:SICK",
         "ADDR:25496 S HW 99E",
         "MADDR:25496 S HWY 99E",
         "MAP:1433",
         "UNIT:R903,OPPS,N1",
         "INFO:65YOM C/A/B DIZZY,SWEATING,",
         "DATE:09/30/2011",
         "TIME:07:00:50");
 
     doTest("T11",
         " : / FALL:  653:E:PINE:ST:::::4430:MD2,R81,:FALL SEMI CON 15YRM NO BLEEDING:20120202:210913",
         "CALL:FALL",
         "ADDR:653 E PINE ST",
         "MAP:4430",
         "UNIT:MD2,R81",
         "INFO:FALL SEMI CON 15YRM NO BLEEDING",
         "DATE:02/02/2012",
         "TIME:21:09:13");
   }
   
   @Test
   public void testFredBridgehouse() {
 
     doTest("T1",
         "(Incident) STRC F:10509 S WILDCAT RD, CLACKAMAS COUNTY::::R484, T419, T489, E435, E485, E495, D411, TIME1, MOLALLA-E82, MOLALLA-TND82, SVFSTAFF:NORTH OF",
         "CALL:STRC F",
         "ADDR:10509 S WILDCAT RD",
         "CITY:CLACKAMAS COUNTY",
         "UNIT:R484, T419, T489, E435, E485, E495, D411, TIME1, MOLALLA-E82, MOLALLA-TND82, SVFSTAFF",
         "INFO:NORTH OF");
 
     doTest("T2",
         "(Incident) UNC F:228 STEELHAMMER RD, SILVERTON:::NORTH 7:R404, D411, MED24::41 YOM IN AND OUT CONS/DIFF BREA/A:4/8/2012 1:32:13 PM",
         "CALL:UNC F",
         "ADDR:228 STEELHAMMER RD",
         "CITY:SILVERTON",
         "MAP:NORTH 7",
         "UNIT:R404, D411, MED24",
         "INFO:41 YOM IN AND OUT CONS/DIFF BREA/A",
         "DATE:04/08/2012",
         "TIME:13:32:13");
 
     doTest("T3",
         "(Incident) UNC F:1015 OAK ST 20 , SILVERTON:SILVER CLIFF ESTATES:::R404, D411, MED24::39 YOF UNC/BREA/A DIAB EMERGECY:4/8/2012",
         "CALL:UNC F",
         "ADDR:1015 OAK ST 20",
         "PLACE:SILVER CLIFF ESTATES",
         "CITY:SILVERTON",
         "UNIT:R404, D411, MED24",
         "INFO:39 YOF UNC/BREA/A DIAB EMERGECY");
 
   }
   
   @Test
   public void testActive911A() {
 
     doTest("T1",
         "(Incident) SICK F:12748 DONALD LOOP RD NE, MARION COUNTY::::P924, E914, MED21::95 YOM UTI / UNK FURTHER INFO:",
         "CALL:SICK F",
         "ADDR:12748 DONALD LOOP RD NE",
         "CITY:MARION COUNTY",
         "UNIT:P924, E914, MED21",
         "INFO:95 YOM UTI / UNK FURTHER INFO");
 
     doTest("T2",
         "(Incident) MVA INJURY F:LAT: 45.2758, LON: -122.77::::E914, TVFR-E52, AMR1::2 VEH MVA/IN MEDIAN/UNK INJURY/REAR ENDED:",
         "CALL:MVA INJURY F",
         "ADDR:45.2758,-122.77",
         "UNIT:E914, TVFR-E52, AMR1",
         "INFO:2 VEH MVA/IN MEDIAN/UNK INJURY/REAR ENDED");
 
     doTest("T3",
         "(Incident) MVA INJURY F:HW 99E MP 24:HW 99E MP 24:::R903, E914, AMR1:::",
         "CALL:MVA INJURY F",
         "ADDR:HW 99E MP 24",
         "MADDR:HWY 99E MP 24",
         "UNIT:R903, E914, AMR1");
 
     doTest("T4",
         "(Incident) MVA INJURY F:I5 NB MP 278:I5 NB MP 278:::E21, E914, E926, MED23::2 VEH MVA/1 MALE LAYING ON GROUND:",
         "CALL:MVA INJURY F",
         "ADDR:I5 NB MP 278",
         "MADDR:I 5 MP 278",
         "UNIT:E21, E914, E926, MED23",
         "INFO:2 VEH MVA/1 MALE LAYING ON GROUND");
 
     doTest("T5",
         "(Incident) FIRE ASSIST:7875 SW FAIRWAY DR, WILSONVILLE::::E914::TRI-PLEX FIRE / FLAMES SHOWING / OPS 35:",
         "CALL:FIRE ASSIST",
         "ADDR:7875 SW FAIRWAY DR",
         "CITY:WILSONVILLE",
         "UNIT:E914",
         "INFO:TRI-PLEX FIRE / FLAMES SHOWING / OPS 35");
 
     doTest("T6",
         "(Incident) FIRE ASSIST:7875 SW FAIRWAY DR, WILSONVILLE::::P924, E914, E926::TRI-PLEX FIRE / FLAMES SHOWING / OPS 35:",
         "CALL:FIRE ASSIST",
         "ADDR:7875 SW FAIRWAY DR",
         "CITY:WILSONVILLE",
         "UNIT:P924, E914, E926",
         "INFO:TRI-PLEX FIRE / FLAMES SHOWING / OPS 35");
 
     doTest("T7",
         "(Incident) ALM FIRE F:25496 S HWY 99E, CLACKAMAS COUNTY:TOP O HILL RESTAURANT:::R903, E914, T909::GENERAL FIRE:",
         "CALL:ALM FIRE F",
         "ADDR:25496 S HWY 99E",
         "PLACE:TOP O HILL RESTAURANT",
         "CITY:CLACKAMAS COUNTY",
         "UNIT:R903, E914, T909",
         "INFO:GENERAL FIRE");
 
     doTest("T8",
         "(Incident) ALM FIRE F:23766 3RD ST NE, MARION COUNTY::::E926::SMOKE ALARM GOING OFF:",
         "CALL:ALM FIRE F",
         "ADDR:23766 3RD ST NE",
         "CITY:MARION COUNTY",
         "UNIT:E926",
         "INFO:SMOKE ALARM GOING OFF");
 
     doTest("T9",
         "(Incident) SICK F:13681 WISTERIA DR NE, MARION COUNTY::::E914, MED21::88YOM C/B/A HIP PAIN FROM A FALL:",
         "CALL:SICK F",
         "ADDR:13681 WISTERIA DR NE",
         "CITY:MARION COUNTY",
         "UNIT:E914, MED21",
         "INFO:88YOM C/B/A HIP PAIN FROM A FALL");
 
   }
   
   public static void main(String[] args) {
     new ORMarionCountyNParserTest().generateTests("T1");
   }
 }
