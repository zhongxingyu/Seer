 package net.anei.cadpage.parsers.PA;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class PABucksCountyBParserTest extends BaseParserTest {
   
   public PABucksCountyBParserTest() {
     setParser(new PABucksCountyBParser(), "BUCKS COUNTY", "PA");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         " <SYSTEM GENERATED> TO INT1 FROM FS01  : 134/E60\n" +
         "                                                    \n" +
         " \n" +
         " \n" +
         " Inc History for: #ED1213417    Xref:                           #PD121040864\n\n" +
         " \n" +
         " Entered      04/13/12  15:57:01  BY PD10   753      \n" +
         " Dispatched   04/13/12  15:57:23  BY FS01   619      \n" +
         " \n" +
         " Initial Type: ADIAB   Initial Alarm Level:  1  Final Alarm Level:  1\n" +
         " Final Type: ADIAB (DIABETIC EMERGENCY) Pri: 3 Dspo:      Prem: [  ]\n" +
         " EMS Blk:  134620 Fire Blk:  60063  Police Blk:  36106  Map Page: 3032G4 \n" +
         " Group: EN  Beat: 134 Src:  N ATT:  MTC - Meet the Complainant  \n" +
         " Loc: RT 309 OFFICE FURN ,36 at 1711 BETHLEHEM PK ,36 btwn SWARTLEY RD & MILL RD\n" +
         " \n" +
         " Loc Info: \n" +
         " Name: DAN MCTAGUE             Addr:                         Phone: 2676406378\n" +
         " \n" +
         "/1557  (753     )  ENTRY:         63YOM,,AWAKE BUT INCOHERENT,,REFUSING THE SUGA\n" +
         "                                  RS\n" +
         "/1557  (619     )  DISP    A134    #125093   PANKOE,RYAN \n" +
         "/1557              ADD     QUT60  ",
 
         "SRC:134/E60",
         "ID:ED1213417",
         "DATE:04/13/12",
         "TIME:15:57:23",
         "CALL:ADIAB (DIABETIC EMERGENCY)",
         "BOX:F:60063 E:134620",
         "MAP:3032G4",
         "PLACE:RT 309 OFFICE FURN",
         "ADDR:1711 BETHLEHEM PK",
         "MADDR:1711 BETHLEHEM PIKE",
         "CITY:HILLTOWN TWP",
         "X:SWARTLEY RD & MILL RD",
         "NAME:DAN MCTAGUE",
         "PHONE:2676406378",
         "INFO:63YOM,,AWAKE BUT INCOHERENT,,REFUSING THE SUGA RS",
         "UNIT:A134 QUT60");
 
     doTest("T2",
         " <SYSTEM GENERATED> TO INT1 FROM FS01  : 134\n\n\n\n" +
         " Inc History for: #ED1213447    Xref: #PD121041267\n\n" +
         " Entered      04/13/12  21:10:06  BY PD06   576\n" +
         " Dispatched   04/13/12  21:11:21  BY FS01   807\n\n" +
         " Initial Type: BOD     Initial Alarm Level:  1  Final Alarm Level:  1\n" +
         " Final Type: BOD   (OVERDOSE/POISIONING (NON CRIT)) Pri: 4 Dspo:\n" +
         "Prem: [  ]\n" +
         " EMS Blk:  134062 Fire Blk:  74062  Police Blk:  48101  Map Page: 3033E6\n" +
         " Group: EN  Beat: 134 Src:   ATT:\n" +
         " Loc: 214 HAMPSHIRE DR ,48  btwn CORNWALL DR & SURREY RD\n\n" +
         " Loc Info:\n" +
         " Name: SHANNON GILROY          Addr:                         Phone: 2679873669\n\n" +
         "/2110  (576     )  COPY           ,FOR AN INTOX MALE\n" +
         "/2110             $CROSS          #PD121041267\n" +
         "                                  ,FOR AN INTOX MALE\n" +
         "/2111  (807     )  DISP    A134    #125093   PANKOE,RYAN\n\n" +
         "-- \n\n\n" +
         "DISCLAIMER: This electronic message transmission has been sent by Chalfont Emergency Medical Services, Inc., which may contain confidential or privileged information. This information is intended for the individual or entity named above. If you are not the intended recipient, be aware that any disclosure, copying, distribution or use of the contents of this information is prohibited. If you have received this electronic transmission in error, please notify us immediately by telephone at 215-822-1308 x116, or by electronic mail (reply).",
 
         "SRC:134",
         "ID:ED1213447",
         "DATE:04/13/12",
         "TIME:21:11:21",
         "CALL:BOD   (OVERDOSE/POISIONING (NON CRIT))",
         "BOX:F:74062 E:134062",
         "MAP:3033E6",
         "ADDR:214 HAMPSHIRE DR",
         "CITY:NEW BRITAIN TWP",
         "X:CORNWALL DR & SURREY RD",
         "NAME:SHANNON GILROY",
         "PHONE:2679873669",
         "INFO:,FOR AN INTOX MALE",
         "UNIT:A134");
 
     doTest("T3",
         " <SYSTEM GENERATED> TO INT1 FROM FS01  : 125/134\n\n\n\n" +
         " Inc History for: #ED1213623    Xref:                           #PD121060278\n\n" +
         " Received     04/15/12  09:18:45  BY FD01   619\n" +
         " Entered      04/15/12  09:19:25  BY FD01   619\n" +
         " Dispatched   04/15/12  09:19:51  BY FS01   271\n\n" +
         " Initial Type: ATRAN   Initial Alarm Level:  1  Final Alarm Level:  1\n" +
         " Final Type: ATRAN (NON-SPECIFIC (CRITICAL) EMERG) Pri: 3 Dspo:      Prem: [  ]\n" +
         " EMS Blk:  125003 Fire Blk:  19003  Police Blk:  29202  Map Page: 2922D10\n" +
         " Group: EN  Beat: 125 Src:  9 ATT:\n" +
         " Loc: 39 PINEVALE RD ,29  btwn PINE VALLEY RD S & PINE VALLEY RD N\n\n" +
         " Loc Info:\n" +
         " Name: KAREN RICHARDSON        Addr: DAUGHTER                Phone: 2153487812\n\n" +
         "/0919  (619     )  ENTRY:         82YOF,,VERY WEAK,,UNABLE TO WALK\n" +
         "/0919  (271     )  DISP    A134    #078910   MCDONAGH, PENELOPE\n" +
         "                                  #131319   MEEHAN, JOSEPH (EMT)\n" +
         "/0919              ADD     1253    #208306   WHITESIDE, JESSICA (EMT)\n" +
         "                                  #100580   HARKER, KEVIN (EMT)\n\n\n\n" +
         "--\n\n\n" +
         "DISCLAIMER: This electronic message transmission has been sent by Chalfont\n" +
         "Emergency Medical Services, Inc., which may contain confidential or\n" +
         "privileged information. This information is intended for the individual or\n" +
         "entity named above. If you are not the intended recipient, be aware that\n" +
         "any disclosure, copying, distribution or use of the contents of this\n" +
         "information is prohibited. If you have received this electronic\n" +
         "transmission in error, please notify us immediately by telephone at\n" +
         "215-822-1308\n" +
         "x116, or by electronic mail (reply).",
 
         "SRC:125/134",
         "ID:ED1213623",
         "DATE:04/15/12",
         "TIME:09:19:51",
         "CALL:ATRAN (NON-SPECIFIC (CRITICAL) EMERG)",
         "BOX:F:19003 E:125003",
         "MAP:2922D10",
         "ADDR:39 PINEVALE RD",
         "CITY:DOYLESTOWN TWP",
         "X:PINE VALLEY RD S & PINE VALLEY RD N",
         "NAME:KAREN RICHARDSON (DAUGHTER)",
         "PHONE:2153487812",
         "INFO:82YOF,,VERY WEAK,,UNABLE TO WALK",
         "UNIT:A134 1253");
 
     doTest("T4",
         " <SYSTEM GENERATED> TO INT1 FROM FD02  : 134\n\n\n\n" +
         " Inc History for: #ED1213579    Xref:                           #PD121051282\n\n" +
         " Entered      04/14/12  23:49:37  BY PD06   741\n" +
         " Dispatched   04/14/12  23:50:00  BY FD02   807\n\n" +
         " Initial Type: BUNK    Initial Alarm Level:  1  Final Alarm Level:  1\n" +
         " Final Type: BUNK  (UNKNOWN EMERGENCY (3RD PARTY)) Pri: 3 Dspo:      Prem: []\n" +
         " EMS Blk:  134004 Fire Blk:  34005  Police Blk:  48201  Map Page: 2921F10\n" +
         " Group: EN  Beat: 134 Src:   ATT:\n" +
         " Loc: CALLOWHILL RD/NEW GALENA RD ,48\n\n" +
         " Loc Info:\n" +
         " Name:                         Addr:                         Phone:\n\n" +
         "/2349  (741     )  COPY           ,OFC REQ BLS SQUAD DIDN'T SPECIFY WHAT FOR WHE\n" +
         "                                 N ASKED\n" +
         "/2349             $CROSS          #PD121051282\n" +
         "                                 ,OFC REQ BLS SQUAD DIDN'T SPECIFY WHAT FOR WHE\n" +
         "                                 N ASKED\n" +
         "/2350  (807     )  DISP    A134    #078910   MCDONAGH, PENELOPE\n" +
         "                                  #131319   MEEHAN, JOSEPH (EMT)\n\n\n\n" +
         "--\n\n\n" +
         "DISCLAIMER: This electronic message transmission has been sent by Chalfont\n" +
         "Emergency Medical Services, Inc., which may contain confidential or\n" +
         "privileged information. This information is intended for the individual or\n" +
         "entity named above. If you are not the intended recipient, be aware that\n" +
         "any disclosure, copying, distribution or use of the contents of this\n" +
         "information is prohibited. If you have received this electronic\n" +
         "transmission in error, please notify us immediately by telephone at\n" +
         "215-822-1308\n" +
         "x116, or by electronic mail (reply).",
 
         "SRC:134",
         "ID:ED1213579",
         "DATE:04/14/12",
         "TIME:23:50:00",
         "CALL:BUNK  (UNKNOWN EMERGENCY (3RD PARTY))",
         "BOX:F:34005 E:134004",
         "MAP:2921F10",
         "ADDR:CALLOWHILL RD & NEW GALENA RD",
         "CITY:NEW BRITAIN TWP",
         "INFO:,OFC REQ BLS SQUAD DIDN'T SPECIFY WHAT FOR WHE N ASKED",
         "UNIT:A134");
 
     doTest("T5",
         " <SYSTEM GENERATED> TO INT1 FROM FS01  : 151/134/E59\n\n\n\n" +
         " Inc History for: #ED1213550    Xref:                           #PD121051005\n\n" +
         " Received     04/14/12  19:03:19  BY CT10   830\n" +
         " Entered      04/14/12  19:08:14  BY CT10   830\n" +
         " Dispatched   04/14/12  19:10:06  BY FS01   488\n\n" +
         " Initial Type: BTRAN   Initial Alarm Level:  1  Final Alarm Level:  1\n" +
         " Final Type: BTRAN (EMERGENCY TRANSPORTATION) Pri: 4 Dspo:      Prem: [FW]\n" +
         " EMS Blk:  152204 Fire Blk:  59004  Police Blk:  36118  Map Page: 2920E5\n" +
         " Group: EN  Beat: 151 Src:  9 ATT:\n" +
         " Loc: 1120 DIAMOND ST ,36  btwn NAROTHYN RD & E SCHOOLHOUSE RD\n\n" +
         " Loc Info: LIFEPATH HOME\n" +
         " Name: TAHALNY,JANET           Addr:                         Phone: 2152574233\n\n" +
         "/1908  (830     )  ENTRY:         COMPL UNSURE OF TWP,,KEEPS GIVING SELLERSVILLE\n" +
         "                                  AND CALLING THE BUISNESS LIFEPATH.....56/F,,B\n" +
         "                                 P 112/78,,GENERAL WEAKNESS,,HX OF MENTAL RETAR\n" +
         "                                 DATION\n" +
         "/1909  (488     )  HOLD\n" +
         "/1910              DISP    A134    #078910   MCDONAGH, PENELOPE\n" +
         "                                  #131319   MEEHAN, JOSEPH (EMT)\n" +
         "/1910              ADD     1512\n" +
         "/1910              ADD     QUT59\n\n\n\n" +
         "--\n\n\n" +
         "DISCLAIMER: This electronic message transmission has been sent by Chalfont\n" +
         "Emergency Medical Services, Inc., which may contain confidential or\n" +
         "privileged information. This information is intended for the individual or\n" +
         "entity named above. If you are not the intended recipient, be aware that\n" +
         "any disclosure, copying, distribution or use of the contents of this\n" +
         "information is prohibited. If you have received this electronic\n" +
         "transmission in error, please notify us immediately by telephone at\n" +
         "215-822-1308\n" +
         "x116, or by electronic mail (reply).",
 
         "SRC:151/134/E59",
         "ID:ED1213550",
         "DATE:04/14/12",
         "TIME:19:10:06",
         "CALL:BTRAN (EMERGENCY TRANSPORTATION)",
         "BOX:F:59004 E:152204",
         "MAP:2920E5",
         "PLACE:LIFEPATH HOME",
         "ADDR:1120 DIAMOND ST",
         "CITY:HILLTOWN TWP",
         "X:NAROTHYN RD & E SCHOOLHOUSE RD",
         "NAME:TAHALNY,JANET",
         "PHONE:2152574233",
         "INFO:COMPL UNSURE OF TWP,,KEEPS GIVING SELLERSVILLE AND CALLING THE BUISNESS LIFEPATH.....56/F,,B P 112/78,,GENERAL WEAKNESS,,HX OF MENTAL RETAR DATION");
 
     doTest("T6",
         " <SYSTEM GENERATED> TO INT1 FROM FS01  : 134\n" +
         "                                                        \n\n\n" +
         " Inc History for: #ED1215024    Xref:                           #PD121170060\n\n\n" +
         " Received     04/26/12  02:16:24  BY CT08   809      \n" +
         " Entered      04/26/12  02:17:25  BY CT08   809      \n" +
         " Dispatched   04/26/12  02:17:38  BY FS01   613      \n\n" +
         " Initial Type: BTRAN   Initial Alarm Level:  1  Final Alarm Level:  1\n" +
         " Final Type: BTRAN (EMERGENCY TRANSPORTATION) Pri: 4 Dspo:      Prem: [  ]\n" +
         " EMS Blk:  134001 Fire Blk:  34001  Police Blk:  48201  Map Page: 3033C2 \n" +
         " Group: EN  Beat: 134 Src:  9 ATT:  MTC - Meet the Complainant  \n" +
         " Loc: 17 WALDEN WY ,48  btwn CLOVER LA & CUL DE SAC\n\n" +
         " Loc Info: \n" +
         " Name: BUGGELN ROBERT W        Addr: 17 WALDEN WY            Phone: 2158229009\n\n" +
         "/0217  (809     )  ENTRY:         95 YOF,, SEVERE RECTAL PAIN ,, PT IS NOT* ALON\n" +
         "                                  E ,,\n" +
         "/0217  (613     )  DISP    A134    #078910   MCDONAGH, PENELOPE \n" +
         "                                   #131319   MEEHAN, JOSEPH (EMT) \n\n\n\n\n" +
         "-- \n\n\n" +
         "DISCLAIMER: This electronic message transmission has been sent by Chalfont Emergency Medical Services, Inc., which may contain confidential or privileged information. This information is intended for the individual or entity named above. If you are not the intended recipient, be aware that any disclosure, copying, distribution or use of the contents of this information is prohibited. If you have received this electronic transmission in error, please notify us immediately by telephone at 215-822-1308 x116, or by electronic mail (reply).",
 
         "SRC:134",
         "ID:ED1215024",
         "DATE:04/26/12",
         "TIME:02:17:38",
         "CALL:BTRAN (EMERGENCY TRANSPORTATION)",
         "BOX:F:34001 E:134001",
         "MAP:3033C2",
         "ADDR:17 WALDEN WY",
         "CITY:NEW BRITAIN TWP",
         "X:CLOVER LA & CUL DE SAC",
         "NAME:BUGGELN ROBERT W",
         "PHONE:2158229009",
         "INFO:95 YOF,, SEVERE RECTAL PAIN ,, PT IS NOT* ALON E ,,",
         "UNIT:A134");
 
     doTest("T7",
         " <SYSTEM GENERATED> TO INT1 FROM FS01  : 134\n" +
         "                                                        \n\n\n" +
         " Inc History for: #ED1215020    Xref:                           #PD121161588\n\n\n" +
         " Received     04/25/12  23:31:41  BY PD11   778      \n" +
         " Entered      04/25/12  23:34:06  BY PD11   778      \n" +
         " Dispatched   04/25/12  23:34:32  BY FS01   613      \n\n" +
         " Initial Type: BPSYCH  Initial Alarm Level:  1  Final Alarm Level:  1\n" +
         " Final Type: BPSYCH(PSYCHIATRIC EMERGENCY) Pri: 4 Dspo:      Prem: [  ]\n" +
         " EMS Blk:  134030 Fire Blk:  34030  Police Blk:  48201  Map Page: 3033H1 \n" +
         " Group: EN  Beat: 134 Src:  9 ATT:  MTC - Meet the Complainant  \n" +
         " Loc: 209 E FAIRWOOD DR ,48  btwn BARBERRY CT & ASPEN CT\n\n" +
         " Loc Info: \n" +
         " Name: ROBERTSON,CARROLL       Addr:                         Phone: 2157163337\n\n" +
         "/2334  (778     )  ENTRY:         WIFE IS BEATING COMPL,, NRW,, COMPL HAS GUNS L\n" +
         "                                  OCKED UP ATT,, NRI,, NO INTOX,, COMPL WANTS HE\n" +
         "                                  R BROUGHT TO CRISIS BY EMS\n" +
         "/2334  (613     )  DISP    A134    #078910   MCDONAGH, PENELOPE \n" +
         "                                   #131319   MEEHAN, JOSEPH (EMT) \n\n\n\n\n" +
         "-- \n\n\n" +
         "DISCLAIMER: This electronic message transmission has been sent by Chalfont Emergency Medical Services, Inc., which may contain confidential or privileged information. This information is intended for the individual or entity named above. If you are not the intended recipient, be aware that any disclosure, copying, distribution or use of the contents of this information is prohibited. If you have received this electronic transmission in error, please notify us immediately by telephone at 215-822-1308 x116, or by electronic mail (reply).",
 
         "SRC:134",
         "ID:ED1215020",
         "DATE:04/25/12",
         "TIME:23:34:32",
         "CALL:BPSYCH(PSYCHIATRIC EMERGENCY)",
         "BOX:F:34030 E:134030",
         "MAP:3033H1",
         "ADDR:209 E FAIRWOOD DR",
         "CITY:NEW BRITAIN TWP",
         "X:BARBERRY CT & ASPEN CT",
         "NAME:ROBERTSON,CARROLL",
         "PHONE:2157163337",
         "INFO:WIFE IS BEATING COMPL,, NRW,, COMPL HAS GUNS L OCKED UP ATT,, NRI,, NO INTOX,, COMPL WANTS HE R BROUGHT TO CRISIS BY EMS",
         "UNIT:A134");
 
     doTest("T8",
         " <SYSTEM GENERATED> TO INT1 FROM FS01  : 134\n" +
         "                                                        \n\n\n" +
         " Inc History for: #ED1215005    Xref:                           #PD121161461\n\n\n" +
         " Received     04/25/12  21:00:42  BY CT10   810      \n" +
         " Entered      04/25/12  21:02:16  BY CT10   810      \n" +
         " Dispatched   04/25/12  21:02:43  BY FS01   613      \n\n" +
         " Initial Type: ADIAB   Initial Alarm Level:  1  Final Alarm Level:  1\n" +
         " Final Type: ADIAB (DIABETIC EMERGENCY) Pri: 3 Dspo:      Prem: [  ]\n" +
         " EMS Blk:  134062 Fire Blk:  74062  Police Blk:  48101  Map Page: 3033F6 \n" +
         " Group: EN  Beat: 134 Src:  9 ATT:  MTC - Meet the Complainant  \n" +
         " Loc: 121 DOLLY LA ,48  btwn DOLLY CI & CORNWALL DR\n\n" +
         " Loc Info: \n" +
         " Name: EYRICH, SHIRLEY         Addr:                         Phone: 2158220221\n\n" +
         "/2102  (810     )  ENTRY:         TOOK WRONG DOSE OF INSULIN,, 75 YOF\n" +
         "/2102              SUPP           TXT: NOT FEELING ANY SYMPTOMS ATT BUT WOULD LI\n" +
         "                                  KE A SQUAD \n" +
         "/2102  (613     )  DISP    A134    #078910   MCDONAGH, PENELOPE \n" +
         "                                   #131319   MEEHAN, JOSEPH (EMT) \n\n\n\n\n" +
         "-- \n\n\n" +
         "DISCLAIMER: This electronic message transmission has been sent by Chalfont Emergency Medical Services, Inc., which may contain confidential or privileged information. This information is intended for the individual or entity named above. If you are not the intended recipient, be aware that any disclosure, copying, distribution or use of the contents of this information is prohibited. If you have received this electronic transmission in error, please notify us immediately by telephone at 215-822-1308 x116, or by electronic mail (reply).",
 
         "SRC:134",
         "ID:ED1215005",
         "DATE:04/25/12",
         "TIME:21:02:43",
         "CALL:ADIAB (DIABETIC EMERGENCY)",
         "BOX:F:74062 E:134062",
         "MAP:3033F6",
         "ADDR:121 DOLLY LA",
         "MADDR:121 DOLLY LN",
         "CITY:NEW BRITAIN TWP",
         "X:DOLLY CI & CORNWALL DR",
         "NAME:EYRICH, SHIRLEY",
         "PHONE:2158220221",
        "INFO:TOOK WRONG DOSE OF INSULIN,, 75 YOF\nNOT FEELING ANY SYMPTOMS ATT BUT WOULD LI KE A SQUAD",
         "UNIT:A134");
 
     doTest("T9",
         " <SYSTEM GENERATED> TO INT1 FROM FS01  : 125/134\n" +
         "                                                    \n\n\n" +
         " Inc History for: #ED1215003    Xref:                           #PD121161443\n\n\n" +
         " Received     04/25/12  20:53:50  BY CT12   825      \n" +
         " Entered      04/25/12  20:54:23  BY CT12   825      \n" +
         " Dispatched   04/25/12  20:55:24  BY FS01   613      \n\n" +
         " Initial Type: ASEIZ   Initial Alarm Level:  1  Final Alarm Level:  1\n" +
         " Final Type: ASEIZ (SEIZURE/ACTIVE (MEDIC)) Pri: 2 Dspo:      Prem: [  ]\n" +
         " EMS Blk:  125006 Fire Blk:  19006  Police Blk:  47001  Map Page: 3034C2 \n" +
         " Group: EN  Beat: 125 Src:  9 ATT:  MTC - Meet the Complainant  \n" +
         " Loc: 20 CEDAR DR ,47  btwn E RT 202 & VINE ST\n\n" +
         " Loc Info: \n" +
         " Name: GLASCOTT, JAKE          Addr:                         Phone: 2154897294\n\n" +
         "/2054  (825     )  ENTRY:         45/F,,ACTIVE SEIZURES\n" +
         "/2055  (613     )  DISP    A134    #078910   MCDONAGH, PENELOPE \n" +
         "                                   #131319   MEEHAN, JOSEPH (EMT) \n\n\n\n\n" +
         "-- \n\n\n" +
         "DISCLAIMER: This electronic message transmission has been sent by Chalfont Emergency Medical Services, Inc., which may contain confidential or privileged information. This information is intended for the individual or entity named above. If you are not the intended recipient, be aware that any disclosure, copying, distribution or use of the contents of this information is prohibited. If you have received this electronic transmission in error, please notify us immediately by telephone at 215-822-1308 x116, or by electronic mail (reply).",
 
         "SRC:125/134",
         "ID:ED1215003",
         "DATE:04/25/12",
         "TIME:20:55:24",
         "CALL:ASEIZ (SEIZURE/ACTIVE (MEDIC))",
         "BOX:F:19006 E:125006",
         "MAP:3034C2",
         "ADDR:20 CEDAR DR",
         "CITY:NEW BRITAIN",
         "X:E RT 202 & VINE ST",
         "NAME:GLASCOTT, JAKE",
         "PHONE:2154897294",
         "INFO:45/F,,ACTIVE SEIZURES",
         "UNIT:A134");
 
     doTest("T10",
         " <SYSTEM GENERATED> TO INT1 FROM FS01  : 339/134\n" +
         "                                                    \n\n\n" +
         " Inc History for: #ED1214986    Xref:                           #PD121161270\n\n\n" +
         " Received     04/25/12  18:31:37  BY CT12   825      \n" +
         " Entered      04/25/12  18:33:29  BY CT12   825      \n" +
         " Dispatched   04/25/12  18:34:40  BY FS01   613      \n\n" +
         " Initial Type: ATRAN   Initial Alarm Level:  1  Final Alarm Level:  1\n" +
         " Final Type: ATRAN (NON-SPECIFIC (CRITICAL) EMERG) Pri: 3 Dspo:      Prem: [ ]\n" +
         " EMS Blk:  339751 Fire Blk:  37501  Police Blk:  36117  Map Page: 2919K8 \n" +
         " Group: EN  Beat: 339 Src:  9 ATT:  MTC - Meet the Complainant  \n" +
         " Loc: 264 WASHINGTON PL ,36  btwn CONESTOGA WY & CUL DE SAC\n\n" +
         " Loc Info: \n" +
         " Name: HENSHAW,JOYCE           Addr: WIFE                    Phone: 2157210892\n\n" +
         "/1833  (825     )  ENTRY:         68/M,,IN AND OUT ON CONCIOUSNESS,,COMPLAINING \n" +
         "                                  OF PAIN IN HIS HANDS/LEGS,,PARKINSONS PATIENT,\n" +
         "                                  ,REQ GVH AND NO LIGHTS/SIRENS\n" +
         "/1834  (613     )  DISP    A134    #078910   MCDONAGH, PENELOPE \n" +
         "                                   #131319   MEEHAN, JOSEPH (EMT) \n\n\n\n\n" +
         "-- \n\n\n" +
         "DISCLAIMER: This electronic message transmission has been sent by Chalfont Emergency Medical Services, Inc., which may contain confidential or privileged information. This information is intended for the individual or entity named above. If you are not the intended recipient, be aware that any disclosure, copying, distribution or use of the contents of this information is prohibited. If you have received this electronic transmission in error, please notify us immediately by telephone at 215-822-1308 x116, or by electronic mail (reply).",
 
         "SRC:339/134",
         "ID:ED1214986",
         "DATE:04/25/12",
         "TIME:18:34:40",
         "CALL:ATRAN (NON-SPECIFIC (CRITICAL) EMERG)",
         "BOX:F:37501 E:339751",
         "MAP:2919K8",
         "ADDR:264 WASHINGTON PL",
         "CITY:HILLTOWN TWP",
         "X:CONESTOGA WY & CUL DE SAC",
         "NAME:HENSHAW,JOYCE (WIFE)",
         "PHONE:2157210892",
         "INFO:68/M,,IN AND OUT ON CONCIOUSNESS,,COMPLAINING OF PAIN IN HIS HANDS/LEGS,,PARKINSONS PATIENT, ,REQ GVH AND NO LIGHTS/SIRENS",
         "UNIT:A134");
 
     doTest("T11",
         " <SYSTEM GENERATED> TO INT1 FROM FS01  : 134\n" +
         "                                                        \n\n\n" +
         " Inc History for: #ED1214883    Xref:                           #PD121151236\n\n\n" +
         " Entered      04/24/12  20:54:23  BY EMS1   753      \n" +
         " Dispatched   04/24/12  20:54:54  BY FS01   807      \n\n" +
         " Initial Type: BFALL   Initial Alarm Level:  1  Final Alarm Level:  1\n" +
         " Final Type: BFALL (FALL VICTIM (NON-CRITICAL)) Pri: 4 Dspo:      Prem: [  ]\n" +
         " EMS Blk:  134029 Fire Blk:  34029  Police Blk:  27001  Map Page: 3033G4 \n" +
         " Group: EN  Beat: 134 Src:  N ATT:  MTC - Meet the Complainant  \n" +
         " Loc: PINE CREEK CONDOS #D53 ,27 at 26 PARK AV #D53 ,27 btwn CHESTNUT ST & PINE CREEK CONDOS\n\n" +
         " Loc Info: \n" +
         " Name: MARY GRAZIANO           Addr:                         Phone: 2678974536\n\n" +
         "/2054  (753     )  ENTRY:         MALE HAS FALLEN,,POSS SINCE THIS MORNING,,PT I\n" +
         "                                  S YELLING,,DOOR IS LOCKED,, COMPL KICKED DOOR \n" +
         "                                  IN\n" +
         "/2054  (807     )  DISP    A134    #131431   HENLEY, SCOTT \n\n\n\n\n" +
         "-- \n\n\n" +
         "DISCLAIMER: This electronic message transmission has been sent by Chalfont Emergency Medical Services, Inc., which may contain confidential or privileged information. This information is intended for the individual or entity named above. If you are not the intended recipient, be aware that any disclosure, copying, distribution or use of the contents of this information is prohibited. If you have received this electronic transmission in error, please notify us immediately by telephone at 215-822-1308 x116, or by electronic mail (reply).",
 
         "SRC:134",
         "ID:ED1214883",
         "DATE:04/24/12",
         "TIME:20:54:54",
         "CALL:BFALL (FALL VICTIM (NON-CRITICAL))",
         "BOX:F:34029 E:134029",
         "MAP:3033G4",
         "PLACE:PINE CREEK CONDOS #D53",
         "ADDR:26 PARK AV",
         "MADDR:26 PARK AVE",
         "APT:D53",
         "CITY:CHALFONT",
         "X:CHESTNUT ST & PINE CREEK CONDOS",
         "NAME:MARY GRAZIANO",
         "PHONE:2678974536",
         "INFO:MALE HAS FALLEN,,POSS SINCE THIS MORNING,,PT I S YELLING,,DOOR IS LOCKED,, COMPL KICKED DOOR IN",
         "UNIT:A134");
 
     doTest("T12",
         " <SYSTEM GENERATED> TO INT1 FROM FS01  : 134/E60\n" +
         "                                                    \n\n\n" +
         " Inc History for: #ED1214856    Xref:                           #PD121150873\n\n\n" +
         " Received     04/24/12  16:18:31  BY CT06   794      \n" +
         " Entered      04/24/12  16:19:27  BY CT06   794      \n" +
         " Dispatched   04/24/12  16:19:48  BY FS01   619      \n\n" +
         " Initial Type: ARESP   Initial Alarm Level:  1  Final Alarm Level:  1\n" +
         " Final Type: ARESP (RESPIRATORY DISTRESS) Pri: 2 Dspo:      Prem: [PW]\n" +
         " EMS Blk:  134620 Fire Blk:  60063  Police Blk:  36106  Map Page: 3032E3 \n" +
         " Group: EN  Beat: 134 Src:  9 ATT:  MTC - Meet the Complainant  \n" +
         " Loc: 2343 BETHLEHEM PK ,36 -- DOCK MEADOWS NH btwn UNIONVILLE PK & CHURCH RD\n\n" +
         " Loc Info: CHURCH ROAD ENTRANCE\n" +
         " Name: SUE                     Addr: NURSE-RN                Phone: 2158220688\n\n" +
         "/1619  (794     )  ENTRY:         80 YOM,,SOB,,HIGH TEMP,,\n" +
         "/1618?             SUPP           LOC: 2343 BETHLEHEM PK #107 ,36, \n" +
         "                                  LOCI: CHURCH ROAD ENTRANCE, \n" +
         "                                  TXT: 80 YOM,,SOB,,HIGH TEMP,, \n" +
         "/1619  (619     )  DISP    A134    #160645   BRUNK, RYAN \n" +
         "/1619              ADD     QUT60  \n\n\n\n\n" +
         "-- \n\n\n" +
         "DISCLAIMER: This electronic message transmission has been sent by Chalfont Emergency Medical Services, Inc., which may contain confidential or privileged information. This information is intended for the individual or entity named above. If you are not the intended recipient, be aware that any disclosure, copying, distribution or use of the contents of this information is prohibited. If you have received this electronic transmission in error, please notify us immediately by telephone at 215-822-1308 x116, or by electronic mail (reply).",
 
         "SRC:134/E60",
         "ID:ED1214856",
         "DATE:04/24/12",
         "TIME:16:19:48",
         "CALL:ARESP (RESPIRATORY DISTRESS)",
         "BOX:F:60063 E:134620",
         "MAP:3032E3",
         "PLACE:DOCK MEADOWS NH - CHURCH ROAD ENTRANCE",
         "ADDR:2343 BETHLEHEM PK",
         "MADDR:2343 BETHLEHEM PIKE",
         "CITY:HILLTOWN TWP",
         "X:UNIONVILLE PK & CHURCH RD",
         "NAME:SUE (NURSE-RN)",
         "PHONE:2158220688",
        "INFO:80 YOM,,SOB,,HIGH TEMP,,\nLOC: 2343 BETHLEHEM PK #107 ,36, LOCI: CHURCH ROAD ENTRANCE,\n80 YOM,,SOB,,HIGH TEMP,,",
         "UNIT:A134 QUT60");
 
     doTest("T13",
         " <SYSTEM GENERATED> TO INT1 FROM FS01  : 134/124\n" +
         "                                                    \n\n\n" +
         " Inc History for: #ED1214833    Xref:                           #PD121150498\n\n\n" +
         " Entered      04/24/12  11:35:44  BY CT07   578      \n" +
         " Dispatched   04/24/12  11:36:25  BY FS01   619      \n\n" +
         " Initial Type: ACVA    Initial Alarm Level:  1  Final Alarm Level:  1\n" +
         " Final Type: ACVA  (CVA/STROKE) Pri: 3 Dspo:      Prem: [  ]\n" +
         " EMS Blk:  123017 Fire Blk:  23018  Police Blk:  36099  Map Page: 2921H5 \n" +
         " Group: EN  Beat: 134 Src:  N ATT:    \n" +
         " Loc: TABORA ORCHARDS ,36 at 1104 UPPER STUMP RD ,36 btwn UPPER CHURCH RD &\n" +
         "OLD LIMEKILN RD\n\n" +
         " Loc Info: ITA\n" +
         " Name: PAUL STILMAN            Addr:                         Phone: 2155343199\n\n" +
         "/1135  (578     )  ENTRY:         46 YR OLD MALE\n" +
         "/1136              SUPP           LOCI: ITA IN A GRAVEL PKLOT ON A SWING, \n" +
         "                                  TXT: 46 YR OLD MALE HAVING A STROKE \n" +
         "/1136  (619     )  DISP    A124    #062940   MCGINTY,JOE \n\n\n\n\n" +
         "-- \n\n\n" +
         "DISCLAIMER: This electronic message transmission has been sent by Chalfont Emergency Medical Services, Inc., which may contain confidential or privileged information. This information is intended for the individual or entity named above. If you are not the intended recipient, be aware that any disclosure, copying, distribution or use of the contents of this information is prohibited. If you have received this electronic transmission in error, please notify us immediately by telephone at 215-822-1308 x116, or by electronic mail (reply).",
 
         "SRC:134/124",
         "ID:ED1214833",
         "DATE:04/24/12",
         "TIME:11:36:25",
         "CALL:ACVA  (CVA/STROKE)",
         "BOX:F:23018 E:123017",
         "MAP:2921H5",
         "PLACE:TABORA ORCHARDS - ITA",
         "ADDR:1104 UPPER STUMP RD",
         "CITY:HILLTOWN TWP",
         "X:UPPER CHURCH RD & OLD LIMEKILN RD",
         "NAME:PAUL STILMAN",
         "PHONE:2155343199",
        "INFO:46 YR OLD MALE\nLOCI: ITA IN A GRAVEL PKLOT ON A SWING,\n46 YR OLD MALE HAVING A STROKE",
         "UNIT:A124");
 
     doTest("T14",
         " <SYSTEM GENERATED> TO INT1 FROM FS01  : 134\n" +
         "                                                        \n\n\n" +
         " Inc History for: #ED1214511    Xref:                           #PD121120819\n\n\n" +
         " Received     04/21/12  15:47:06  BY CT11   749      \n" +
         " Entered      04/21/12  15:50:06  BY CT11   749      \n" +
         " Dispatched   04/21/12  15:50:45  BY FS01   764      \n\n" +
         " Initial Type: ARESP   Initial Alarm Level:  1  Final Alarm Level:  1\n" +
         " Final Type: ARESP (RESPIRATORY DISTRESS) Pri: 2 Dspo:      Prem: [  ]\n" +
         " EMS Blk:  134036 Fire Blk:  34036  Police Blk:  47001  Map Page: 3033K3 \n" +
         " Group: EN  Beat: 134 Src:  9 ATT:  MTC - Meet the Complainant  \n" +
         " Loc: W BUTLER AV/SAND RD ,47   \n\n" +
         " Loc Info: BY BUXMONT PARTY RENTALS\n" +
         " Name: T MOBILE, USA           Addr: MICHAEL SCHMID          Phone: 2153533596\n\n" +
         "/1550  (749     )  ENTRY:         +040.318568 -075.211243,,25 YOM,,CALLER IS PT.\n" +
         "                                  ..ON FOOT ...NOW SITTING ON CURB...CAN'T CATCH\n" +
         "                                   BREATH...HAS ADDICTION TO INHALLENTS\n" +
         "/1550  (764     )  DISP    A134    #160645   BRUNK, RYAN \n\n\n\n\n" +
         "-- \n\n\n" +
         "DISCLAIMER: This electronic message transmission has been sent by Chalfont Emergency Medical Services, Inc., which may contain confidential or privileged information. This information is intended for the individual or entity named above. If you are not the intended recipient, be aware that any disclosure, copying, distribution or use of the contents of this information is prohibited. If you have received this electronic transmission in error, please notify us immediately by telephone at 215-822-1308 x116, or by electronic mail (reply).",
 
         "SRC:134",
         "ID:ED1214511",
         "DATE:04/21/12",
         "TIME:15:50:45",
         "CALL:ARESP (RESPIRATORY DISTRESS)",
         "BOX:F:34036 E:134036",
         "MAP:3033K3",
         "PLACE:BY BUXMONT PARTY RENTALS",
         "ADDR:W BUTLER AV & SAND RD",
         "MADDR:W BUTLER AVE & SAND RD",
         "CITY:NEW BRITAIN",
         "NAME:T MOBILE, USA (MICHAEL SCHMID)",
         "PHONE:2153533596",
         "INFO:+040.318568 -075.211243,,25 YOM,,CALLER IS PT. ..ON FOOT ...NOW SITTING ON CURB...CAN'T CATCH BREATH...HAS ADDICTION TO INHALLENTS",
         "UNIT:A134");
 
     doTest("T15",
         " <SYSTEM GENERATED> TO INT1 FROM FS01  : /134\n" +
         "                                                    \n\n\n" +
         " Inc History for: #ED1214497\n\n\n" +
         " Entered      04/21/12  13:09:26  BY FD01   341      \n" +
         " Dispatched   04/21/12  13:10:58  BY FS01   764      \n\n" +
         " Initial Type: AUNC    Initial Alarm Level:  1  Final Alarm Level:  1\n" +
         " Final Type: AUNC  (UNCONSCIOUS PERSON) Pri: 1 Dspo:      Prem: [  ]\n" +
         " EMS Blk:   Fire Blk:   Police Blk:   Map Page:  \n" +
         " Group: EX  Beat:  Src:  N ATT:    \n" +
         " Loc: 721 CYPRESS CT MONTGOMERY TWP  \n\n" +
         " Loc Info: XSTS STREAMVIEW DR\n" +
         " Name: MONTCO                  Addr:                         Phone: INTERCOM\n\n\n" +
         "/1309  (341     )  ENTRY:         A134 COVER SQ345A, 93 YOM, UNC AND CLAMMY\n" +
         "/1310  (764     )  DISP    A134    #160645   BRUNK, RYAN \n" +
         "                                  ,IN THE NESHAMINY FALLS DEVELOPMENT\n\n\n\n\n" +
         "-- \n\n\n" +
         "DISCLAIMER: This electronic message transmission has been sent by Chalfont Emergency Medical Services, Inc., which may contain confidential or privileged information. This information is intended for the individual or entity named above. If you are not the intended recipient, be aware that any disclosure, copying, distribution or use of the contents of this information is prohibited. If you have received this electronic transmission in error, please notify us immediately by telephone at 215-822-1308 x116, or by electronic mail (reply).",
 
         "SRC:/134",
         "ID:ED1214497",
         "DATE:04/21/12",
         "TIME:13:10:58",
         "CALL:AUNC  (UNCONSCIOUS PERSON)",
         "PLACE:XSTS STREAMVIEW DR",
         "ADDR:721 CYPRESS CT",
         "CITY:MONTGOMERY TWP",
         "NAME:MONTCO",
         "PHONE:INTERCOM",
         "INFO:A134 COVER SQ345A, 93 YOM, UNC AND CLAMMY",
         "UNIT:A134");
 
     doTest("T16",
         "[911 Data]  <SYSTEM GENERATED> TO INT1 FROM FS01  : \n" +
         "134\n" +
         "                                                        \n" +
         " Inc History for: #ED1215452    Xref:                           #PD121200569 \n" +
         " Entered      04/29/12  14:13:01  BY CT06   815      \n" +
         " Dispatched   04/29/12  14:13:56  BY FS01   271      \n" +
         " Initial Type: BTAI    Initial Alarm Level:  1  Final Alarm Level:  1\n" +
         " Final Type: BTAI  (TRAFFIC ACCIDENT(NON-CRITICAL)) Pri: 3 Dspo:      Prem: [  ]\n" +
         " EMS Blk:  134055 Fire Blk:  74055  Police Blk:  48101  Map Page: 3033D7 \n" +
         " Group: EN  Beat: 134 Src:  N ATT:    \n" +
         " Loc: 560 W BUTLER AV ,48  btwn SCHOOL HOUSE RD & COUNTY LINE RD\n" +
         " Loc Info: \n" +
         " Name: AGERO MONITORING CTR    Addr: 3RD PARTY               Phone:           \n" +
         "/1413  (815     )  ENTRY:         NRI,,BLOCKING,,2 VEHS,,2 CHILDREN IN VEHS PARE\n" +
         "                                  NT WANT THEM CHECKED OUT\n" +
         "/1413  (271     )  DISP    A134    #078910   MCDONAGH, PENELOPE \n" +
         "                                   #095422   HUTTENLOCK, SCOTT (EMT) \n" +
         "-- \n" +
         "DISCLAIMER: This electronic message transmission has been sent by Chalfont Emergency Medical Services, Inc., which may contain confidential or privileged information. This information is intended for the individual or entity named above. If you are not the intended recipient, be aware that any disclosure, copying, distribution or use of the contents of this information is prohibited. If you have received this electronic transmission in error, please notify us immediately by telephone at 215-822-1308 x116, or by electronic mail (reply).\n",
 
         "SRC:134",
         "ID:ED1215452",
         "DATE:04/29/12",
         "TIME:14:13:56",
         "CALL:BTAI  (TRAFFIC ACCIDENT(NON-CRITICAL))",
         "BOX:F:74055 E:134055",
         "MAP:3033D7",
         "ADDR:560 W BUTLER AV",
         "MADDR:560 W BUTLER AVE",
         "CITY:NEW BRITAIN TWP",
         "X:SCHOOL HOUSE RD & COUNTY LINE RD",
         "NAME:AGERO MONITORING CTR (3RD PARTY)",
         "INFO:NRI,,BLOCKING,,2 VEHS,,2 CHILDREN IN VEHS PARE NT WANT THEM CHECKED OUT",
         "UNIT:A134");
 
     doTest("T17",
         "Subject: 911 Data \n\n" +
         " <SYSTEM GENERATED> TO INT1 FROM FS01  : \n" +
         "339/134/E59\n" +
         " \n" +
         "Inc History for: #ED1215743    Xref:                           #PD121221067 \n\n" +
         "Received     05/01/12  16:19:21  BY CT11   749\n" +
         "      \n" +
         " Entered      05/01/12  16:20:58  BY CT11   749      \n" +
         " Dispatched   05/01/12  16:22:41  BY FS01   764      \n\n" +
         " Initial Type: ARESP   Initial Alarm Level:  1  Final Alarm Level:  1\n" +
         " Final Type: ARESP (RESPIRATORY DISTRESS) Pri: 2 Dspo:      Prem: [  ]\n" +
         " EMS Blk:  152741 Fire Blk:  37401  Police Blk:  36117  Map Page: 2920B10 \n" +
         " Group: EN  Beat: 339 Src:  9 ATT:  MTC - Meet the Complainant  \n" +
         " Loc: SKYVIEW MEDICAL CE ,36 at 3456 BETHLEHEM PK ,36 btwn RT 309 BY & CHERRY LA\n" +
         " Loc Info: 2ND FLR,,RM #3\n" +
         " Name: CHOP CARE NETWORK INDIA Addr: SANDY, RN               Phone: 2157237045\n" +
         "/1620  (749     )  ENTRY:         4 MOS OLD MALE,,W/A DR ATT\n" +
         "/1621  (764     )  CHANGE         LOC: 3456 BETHLEHEM PK ,36 -->  SKYVIEW MEDICA\n" +
         "                                  L CE ,36\n" +
         "/1622              DISP    A134    #077385   GRAKE,LARRY \n" +
         "/1622              ADD     QUT59  \n\n" +
         "---------\n\n" +
         "-- \n\n" +
         "DISCLAIMER: This electronic message transmission has been sent by Chalfont Emergency Medical Services, Inc., which may contain confidential or privileged information. This information is intended for the individual or entity named above. If you are not the intended recipient, be aware that any disclosure, copying, distribution or use of the contents of this information is prohibited. If you have received this electronic transmission in error, please notify us immediately by telephone at 215-822-1308 x116, or by electronic mail (reply).",
 
         "SRC:339/134/E59",
         "ID:ED1215743",
         "DATE:05/01/12",
         "TIME:16:22:41",
         "CALL:ARESP (RESPIRATORY DISTRESS)",
         "BOX:F:37401 E:152741",
         "MAP:2920B10",
         "PLACE:SKYVIEW MEDICAL CE - 2ND FLR,,RM #3",
         "ADDR:3456 BETHLEHEM PK",
         "MADDR:3456 BETHLEHEM PIKE",
         "CITY:HILLTOWN TWP",
         "X:RT 309 BY & CHERRY LA",
         "NAME:CHOP CARE NETWORK INDIA (SANDY, RN)",
         "PHONE:2157237045",
         "INFO:4 MOS OLD MALE,,W/A DR ATT",
         "UNIT:A134 QUT59");
 
     doTest("T18",
         " <SYSTEM GENERATED> TO INT1 FROM FS01  : \n" +
         "134\n" +
         "                                                        \n\n\n" +
         " Inc History for: #ED1215718    Xref:                           #PD121220662 \n\n" +
         " Received     05/01/12  12:46:58  BY CT12   325      \n" +
         " Entered      05/01/12  12:47:57  BY CT12   325      \n" +
         " Dispatched   05/01/12  12:48:29  BY FS01   764      \n\n" +
         " Initial Type: AFAINT  Initial Alarm Level:  1  Final Alarm Level:  1\n" +
         " Final Type: AFAINT(SYNCOPAL EPISODE (SYNCOPE)) Pri: 3 Dspo:      Prem: [  ]\n" +
         " EMS Blk:  134058 Fire Blk:  74058  Police Blk:  48101  Map Page: 3033F8 \n" +
         " Group: EN  Beat: 134 Src:  9 ATT:    \n" +
         " Loc: 700 HORIZON CI ,48 -- GRANDVIEW MEDICAL btwn HORIZON DR & CUL DE SAC\n\n" +
         " Loc Info: SUITE #103\n" +
         " Name: GRAND VIEW MEDICAL PRAC Addr: KATIE                   Phone: 2158220852\n\n" +
         "/1247  (325     )  ENTRY:         LIGHT HEADED AND NUMBNESS ON RIGHT SIDE,, 43YO\n" +
         "                                  A FEMALE\n" +
         "/1246?             SUPP           TXT: PT STILL W/DR , ,NO BREATHING ISSUES \n" +
         "/1248  (764     )  DISP    A134    #077385   GRAKE,LARRY ",
 
         "SRC:134",
         "ID:ED1215718",
         "DATE:05/01/12",
         "TIME:12:48:29",
         "CALL:AFAINT(SYNCOPAL EPISODE (SYNCOPE))",
         "BOX:F:74058 E:134058",
         "MAP:3033F8",
         "PLACE:GRANDVIEW MEDICAL - SUITE #103",
         "ADDR:700 HORIZON CI",
         "MADDR:700 HORIZON CIR",
         "CITY:NEW BRITAIN TWP",
         "X:HORIZON DR & CUL DE SAC",
         "NAME:GRAND VIEW MEDICAL PRAC (KATIE)",
         "PHONE:2158220852",
        "INFO:LIGHT HEADED AND NUMBNESS ON RIGHT SIDE,, 43YO A FEMALE\nPT STILL W/DR , ,NO BREATHING ISSUES",
         "UNIT:A134");
 
     doTest("T19",
         "[911 Data]  <SYSTEM GENERATED> TO INT1 FROM FS01  : \n" +
         "125/134\n" +
         "                                                    \n" +
         " Inc History for: #ED1215649    Xref:                           #PD121211320 \n" +
         "Entered      04/30/12  21:29:44  BY CT06   810      \n" +
         " Dispatched   04/30/12  21:30:08  BY FS01   613      \n" +
         " Initial Type: ASEIZ   Initial Alarm Level:  1  Final Alarm Level:  1\n" +
         " Final Type: ASEIZ (SEIZURE/ACTIVE (MEDIC)) Pri: 2 Dspo:      Prem: [  ]\n" +
         " EMS Blk:  125007 Fire Blk:  19007  Police Blk:  47001  Map Page: 3034F2 \n" +
         " Group: EN  Beat: 125 Src:  N ATT:  MTC - Meet the Complainant  \n" +
         " Loc: FOUNDATIONS ADMIN ,47 at 833 E BUTLER AV ,47 btwn EVERGREEN AV & OLD RT 202\n" +
         " Loc Info: BEHAVIOR HEALTH UNIT 19\n" +
         " Name: KIRT                    Addr:                         Phone: 2679874076\n" +
         "/2129  (810     )  ENTRY:         BLEEDING FROM THE MOUTH,, 24 YOM\n" +
         "/2130  (613     )  DISP    A134    #077385   GRAKE,LARRY \n" +
         "-- \n" +
         "DISCLAIMER: This electronic message transmission has been sent by Chalfont Emergency Medical Services, Inc., which may contain confidential or privileged information. This information is intended for the individual or entity named above. If you are not the intended recipient, be aware that any disclosure, copying, distribution or use of the contents of this information is prohibited. If you have received this electronic transmission in error, please notify us immediately by telephone at 215-822-1308 x116, or by electronic mail (reply).\n",
 
         "SRC:125/134",
         "ID:ED1215649",
         "DATE:04/30/12",
         "TIME:21:30:08",
         "CALL:ASEIZ (SEIZURE/ACTIVE (MEDIC))",
         "BOX:F:19007 E:125007",
         "MAP:3034F2",
         "PLACE:FOUNDATIONS ADMIN - BEHAVIOR HEALTH UNIT 19",
         "ADDR:833 E BUTLER AV",
         "MADDR:833 E BUTLER AVE",
         "CITY:NEW BRITAIN",
         "X:EVERGREEN AV & OLD RT 202",
         "NAME:KIRT",
         "PHONE:2679874076",
         "INFO:BLEEDING FROM THE MOUTH,, 24 YOM",
         "UNIT:A134");
 
     doTest("T20",
         "[911 Data]  <SYSTEM GENERATED> TO INT1 FROM FS01  :\n" +
         "125/134\n" +
         " Inc History for: #ED1215649    Xref:                           #PD121211320\n" +
         " Entered      04/30/12  21:29:44  BY CT06   810\n" +
         " Dispatched   04/30/12  21:30:08  BY FS01   613\n" +
         " Initial Type: ASEIZ   Initial Alarm Level:  1  Final Alarm Level:  1\n" +
         " Final Type: ASEIZ (SEIZURE/ACTIVE (MEDIC)) Pri: 2 Dspo:      Prem: [  ]\n" +
         " EMS Blk:  125007 Fire Blk:  19007  Police Blk:  47001  Map Page: 3034F2\n" +
         " Group: EN  Beat: 125 Src:  N ATT:  MTC - Meet the Complainant\n" +
         " Loc: FOUNDATIONS ADMIN ,47 at 833 E BUTLER AV ,47 btwn EVERGREEN AV & OLD\n" +
         "RT 202\n" +
         " Loc Info: BEHAVIOR HEALTH UNIT 19\n" +
         " Name: KIRT                    Addr:                         Phone:\n" +
         "2679874076\n" +
         "/2129  (810     )  ENTRY:         BLEEDING FROM THE MOUTH,, 24 YOM\n" +
         "/2130  (613     )  DISP    A134    #077385   GRAKE,LARRY\n" +
         "-- \n" +
         "DISCLAIMER: This electronic message transmission has been sent by Chalfont Emergency Medical Services, Inc., which may contain confidential or privileged information. This information is intended for the individual or entity named above. If you are not the intended recipient, be aware that any disclosure, copying, distribution or use of the contents of this information is prohibited. If you have received this electronic transmission in error, please notify us immediately by telephone at 215-822-1308 x116, or by electronic mail (reply).\n",
 
         "SRC:125/134",
         "ID:ED1215649",
         "DATE:04/30/12",
         "TIME:21:30:08",
         "CALL:ASEIZ (SEIZURE/ACTIVE (MEDIC))",
         "BOX:F:19007 E:125007",
         "MAP:3034F2",
         "PLACE:FOUNDATIONS ADMIN - BEHAVIOR HEALTH UNIT 19",
         "ADDR:833 E BUTLER AV",
         "MADDR:833 E BUTLER AVE",
         "CITY:NEW BRITAIN",
         "X:EVERGREEN AV & OLD RT 202",
         "NAME:KIRT",
         "PHONE:2679874076",
         "INFO:BLEEDING FROM THE MOUTH,, 24 YOM",
         "UNIT:A134");
 
     doTest("T21",
         "[911 Data]  <SYSTEM GENERATED> TO INT1 FROM FS01  :\n" +
         "134\n" +
         " Inc History for: #ED1215444    Xref:              #FD1205858   #PD121200524\n" +
         " Entered      04/29/12  13:20:22  BY FD01   208\n" +
         " Dispatched   04/29/12  13:21:07  BY FS01   271\n" +
         " Initial Type: RMAR    Initial Alarm Level:  1  Final Alarm Level:  1\n" +
         " Final Type: RMAR  (MARINE RESCUE (MBOX)) Pri: 1 Dspo:      Prem: [PW]\n" +
         " EMS Blk:  134098 Fire Blk:  34098  Police Blk:  83002  Map Page: 2921J10\n" +
         " Group: EN  Beat: 134 Src:  N ATT:\n" +
         " Loc: PEACE VALL BOATRA2 ,83 at 264 CREEK RD ,83 btwn OLD IRON HILL RD &\n" +
         "CHEESE FACTORY RD\n" +
         " Loc Info: ANGLERS PIER\n" +
         " Name: PALE, MARYANNE          Addr:                         Phone:\n" +
         "2157049576\n" +
         "/1320  (208     )  ENTRY:         OVERTURNED SAILBOAT,,THREE SUBJECTS IN\n" +
         "THE WAT\n" +
         "                                 ER CALLING FOR HELP\n" +
         "/1320  (271     )  HOLD\n" +
         "/1321              DISP    A134    #078910   MCDONAGH, PENELOPE\n" +
         "                                  #095422   HUTTENLOCK, SCOTT (EMT)\n" +
         "-- \n" +
         "DISCLAIMER: This electronic message transmission has been sent by Chalfont Emergency Medical Services, Inc., which may contain confidential or privileged information. This information is intended for the individual or entity named above. If you are not the intended recipient, be aware that any disclosure, copying, distribution or use of the contents of this information is prohibited. If you have received this electronic transmission in error, please notify us immediately by telephone at 215-822-1308 x116, or by electronic mail (reply).\n",
 
         "SRC:134",
         "ID:ED1215444",
         "DATE:04/29/12",
         "TIME:13:21:07",
         "CALL:RMAR  (MARINE RESCUE (MBOX))",
         "BOX:F:34098 E:134098",
         "MAP:2921J10",
         "PLACE:PEACE VALL BOATRA2 - ANGLERS PIER",
         "ADDR:264 CREEK RD",
         "X:OLD IRON HILL RD & CHEESE FACTORY RD",
         "NAME:PALE, MARYANNE",
         "PHONE:2157049576",
         "INFO:OVERTURNED SAILBOAT,,THREE SUBJECTS IN THE WAT ER CALLING FOR HELP");
  }
   
   
   public static void main(String[] args) {
     new PABucksCountyBParserTest().generateTests("T19", "SRC ID DATE TIME CALL BOX MAP PLACE ADDR APT CITY X NAME PHONE INFO UNIT");
   }
 }
