 package net.anei.cadpage.parsers.MO;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.MsgParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /*
 Jefferson City, MO
 Contact: Jake Holee <jakeholee@gmail.com>
 Contact: Scott Kempker <jcfd133@gmail.com>
 Contact: "jacobholee04@pm.sprint.com" <jacobholee04@pm.sprint.com>
 Sender: paging@jeffcitymo.org
 
 Subject:DONOTREPLY\nSnyder, Jodi 14:00 Medical Emergency 718 MICHIGAN -Cross Streets- LOUISIANA AVE / MISSOURI BLVD E1   CONSCIOUS  OUTSID
 Subject:DONOTREPLY\nE4  14:28 Line Down  - 1505 SOUTHWEST -Cross Streets- CEDAR RIDGE RD / EDGEWOOD DR   LIMB IS ON A POWER LINE \n
 Subject:DONOTREPLY\nMcDowell, Shelley 13:40 Medical Emergency 301 HIGH -Cross Streets- BROADWAY ST / RAMP OFF RAMP, RAMP ON RAMP E1   FEMA
 Subject:DONOTREPLY\nBatt12 E1 L1 E3 S5  13:31 Fire Alarm - 1303 EDMONDS -Cross Streets- MONTANA ST / MYRTLE AVE   OPER 38  UNKNOWN ROOM  3 
 Subject:DONOTREPLY\nL1  12:02 Accident with Injuries - 1300 5463 BLK -Cross Streets- RAMP 25, HWY 54 E / RAMP 26   CCEMS EN ROUTE  NEED EX 
 Subject:DONOTREPLY\nMcDowell, Shelley 10:07 Medical Emergency 1705 CHRISTY -Cross Streets- CHRISTY CT / ELLIS BLVD E4   SUITE 208 \n
 Subject:DONOTREPLY\nMcDowell, Shelley 09:40 Medical Emergency 1434 NOTTING HILL -Cross Streets- DEAD END / PORTABELLO PLACE DR S5   EMS EN 
 Subject:DONOTREPLY\nMcDowell, Shelley 08:30 Medical Emergency 424 STADIUM -Cross Streets- ADAMS ST / YMCA DR, JACKSON ST E4   ELDERLY MALE 
 Subject:DONOTREPLY\nStiefermann, Angela 02:32 Medical Alarm 10 JACKSON 704 -Address Between- DEAD END / E STATE ST E1   \n
 Subject:DONOTREPLY\nE1  16:32 Assist Citizen Fire - MYRTLE EDMONDS -Cross Streets-   MALE WAS ROLLING BACKWARDS UNTIL CALLERS SON JUMPED O
 FRM:paging@jeffcitymo.org\nSUBJ:DONOTREPLY\nMSG:Aston, Nora 22:35 Medical Alarm 10 JACKSON 207 -Address Between- DEAD END / E STATE ST E1 EMS NOTIFIED \n
 
 1410000046:  1 of 2\nFRM:paging@jeffcitymo.org\nSUBJ:DONOTREPLY\nMSG:Batt12 E1 L1 S2 E4  13:40 Structure Fire - 600 ADAMS -Cross Streets-\nDEAD END / E CEDAR WAY\n(Con't) 2 of 2\n  LAST HOUSE DEAD END OF ADAMS  DECK ON FIRE  SMOKE IN THE AREA\n(End)\n
 1410000050:  1 of 2\nFRM:paging@jeffcitymo.org\nSUBJ:DONOTREPLY\nMSG:Gabathuler, Erin 20:34 Medical Emergency 207 FRANKLIN A -Cross Streets-\nMADISON ST / MONROE\n(Con't) 2 of 2\nST L1   HISTORY OF DIABETES  48 YO FEMALE HAS FELL AND IS\n(End)
 1410000052:  1 of 2\nFRM:paging@jeffcitymo.org\nSUBJ:DONOTREPLY\nMSG:Suchanek, Celeste 23:52 Medical Emergency 531 ELM A -Cross Streets-\nJACKSON ST / MARSHALL\n(Con't) 2 of 2\nST L1   F 5 MONTHS PREGNANT   BLEEDING\n(End)
 1410000045:  1 of 2\nFRM:paging@jeffcitymo.org\nSUBJ:DONOTREPLY\nMSG:Batt12 E1 L1 S5 E4  11:42 Fire Alarm - 1432 SOUTHWEST -Cross Streets-\nEDGEWOOD DR / CEDAR\n(Con't) 2 of 2\nRIDGE RD   GENERAL FIRE\n(End)
 1410000036:  1 of 2\nFRM:paging@jeffcitymo.org\nSUBJ:DONOTREPLY\nMSG:Snyder, Jodi 11:18 Medical Emergency 308 BENTON -Cross Streets- DEAD END\n/ COLLIER LN,\n(Con't) 2 of 2\nNORTHWAY DR L1   SUBJECT CAN'T WALK\n(End)
 1410000039:  1 of 2\nFRM:paging@jeffcitymo.org\nSUBJ:DONOTREPLY\nMSG:Morgan, Kacey 22:05 Medical Emergency 1020 ELIZABETH A -Cross Streets-\nEDMONDS ST / MYRTLE\n(Con't) 2 of 2\nAVE L1   SUBJECT IS ON A BREATHING MACHINE  ASTHMA ATTACK\n(End)
 1410000043:  1 of 2\nFRM:paging@jeffcitymo.org\nSUBJ:DONOTREPLY\nMSG:1101 1401 1110 112 CCPager  17:53 Fire Alarm - 2709 LILAC -Cross\nStreets- DEAD END / S\n(Con't) 2 of 2\nCOUNTRY CLUB DR   RESIDENTAL- PIERSONS - FIRE MAIN FLOOR\n(End)
 1410000038:  1 of 2\nFRM:paging@jeffcitymo.org\nSUBJ:DONOTREPLY\nMSG:1201 1401 1531 114 CCPager  17:45 Accident with Injuries - 8000 D BLK\n-Cross Streets-\n(Con't) 2 of 2\nSCRIVNER RD / BATES RD   RP JUST HEARD THE ACCIDENT  UNKNOWN INJURY\n(End)
 
 Jefferson County, MO
 Contact: support@active911.com
 Sender: paging@jeffcitymo.org
 (DONOTREPLY) E3  13:39 Line Down  - 1304 MCCARTY -Cross Streets- HAVANA ST, HART ST / HAVANA ST   TREE DOWN ACROSS W MCCARTY STREET - TOOK DOWN POWER LINES AS WELL  Call #: 617 - RP THINKS WIRES ARE DOWN ON MCCARTY  Call #: 617 - RP HEARD A LOUD POPPING SOUND 
 (DONOTREPLY) E1  13:46 Assist Police - 2201 MAIN -Cross Streets- MEMORIAL PARK DR / FOREST HILL AVE   93 REQUESTING WASH DOWN AFTER THE SAFETY PATROL PICNIC. (JUST SEND E1) 
 (DONOTREPLY) Degraffenreid, Jenny 14:24 Medical Emergency 120 DUNKLIN -Cross Streets- W DUNKLIN ST, JEFFERSON ST / MADISON ST S5  
 (DONOTREPLY) AC2 E4 S5 E3 L1  14:48 Fire Alarm - 1201 FAIRGROUNDS -Cross Streets- VILLAGE DR / S COUNTRY CLUB DR, W EDGEWOOD DR   OFFICE AREA 
 (DONOTREPLY) Taylor, Tiffany 15:08 Medical Emergency 711 KANSAS -Cross Streets- WASHINGTON PARK DR / MYRTLE AVE, SOUTH BLVD E1   SUBJ IS NOW TRYING TO LEAVE -  RP WAS ABLE TO GET
 (DONOTREPLY) Degraffenreid, Jenny 16:33 Medical Emergency 406 ATCHISON -Cross Streets- No Cross Streets Found E1   SEIZURE - BEING COMBATIVE 
 (DONOTREPLY) Degraffenreid, Jenny 16:42 Medical Emergency 2021 WILLIAM -Cross Streets- BLAIR DR / DIX RD E3   STOMACH PAIN, FACE IS FLUSH, PRESSURE IN HEAD.  S
 (DONOTREPLY) AC2 L1 E4  16:42 Accident with Injuries - JEFFERSON -Cross Streets-   EMS NOTIFIED  Call #: 691 - METALIC CAR  ELDERLY MALE DRIVER  Call #: 691 - AT CONOCO  UNKNOWN INJURIES  SIL CAR RAN INTO GAURD-RAIL  Call #: 691 - CALLER DOES NOT BELIEVE THIS IS AN ACCIDENT, BUT MAYBE ELDERLY MALE HAVING SOME KIND OF ISSUE  Call #: 691 - VEH IS UP ON GUARD RAIL  Call #: 691 - W/M
 (DONOTREPLY) Degraffenreid, Jenny 16:42 Medical Emergency 2021 WILLIAM -Cross Streets- BLAIR DR / DIX RD L3   STOMACH PAIN, FACE IS FLUSH, PRESSURE IN HEAD.  S
 (DONOTREPLY) Schlitt, Meagen 18:53 Medical Emergency 3038 TRUMAN -Cross Streets- DUPONT CIR / N TEN MILE DR E3   RM 401  RESIDENT MAY BE HAVING HEART ATTACK 
 (DONOTREPLY) AC2 E1 L1 S2 E4  19:22 Structure Fire - ELM -Cross Streets-   WHITE SMOKE COMING FROM FRONT OF BUILDING 
 (DONOTREPLY) Degraffenreid, Jenny 09:41 Medical Emergency 2225 WEATHERED ROCK 4 -Cross Streets- LORENZO GREENE DR / MILLBROOK DR E1   SAYS SHE TRIED TO DRIVE BUT COULDN'T. BLEEDING FR
 (DONOTREPLY) 1101 1401 1110 112 CCPager  16:58 Fire Alarm - 2227 SADDLEBROOKE LAKE -Cross Streets- CHERISH CT / SCRUGGS STATION RD   ALLEN RESIDENCE 
 (DONOTREPLY) AC2 E1 L1 S2 E3  08:40 Fire Alarm - 1445 CHRISTY -Cross Streets- OSCAR DR / RAMP 31-970733, TANNER BRIDGE RD   GENERAL  FIRE ALARM - ROOM SMOKE  MID MO ANESTHIA 
 (DONOTREPLY) DC4 E1 L1 S2 E4  23:39 Fire Alarm - 2033 CHRISTY -Cross Streets- VETERANS LN / RT B   Call Number 434 was created from Call Number 433(May  3 2012 11:37PM)  PERIMETER AND FIRE ALARM - OFFICE HALL MOTION DETECTOR #2406 
 (DONOTREPLY) S5  22:59 Accident with Injuries - 1700 INDUSTRIAL -Cross Streets- ARGONNE ST / HUGHES ST   EMS ADV  CAR VS TRAIN-  TRAPPED IN VEH 
 (DONOTREPLY) Batt11  22:59 Extrication - 1700 INDUSTRIAL -Cross Streets- ARGONNE ST / HUGHES ST   UNION PACIFIC ADVISED- THEY CALLED INTO REPORT A PROBLEM - THEY WERE UNSURE OF WHAT THE PROBLEM WAS- THEY HAVE BEEN ADVISED TO STOP ALL TRAIN TRAFFIC  EMS ADV  CAR VS TRAIN-  TRAPPED IN VEH 
 (DONOTREPLY) Gabathuler, Erin 20:08 Medical Alarm 1509 STADIUM -Address Between- MYRTLE AVE / SOUTHWEST BLVD E4   CCEMS ADVISED  KEY - UNDER WHITE ROCK - UNDER/NEX
 (DONOTREPLY)  19:01 Test - 1229 BROADWAY -Cross Streets- W RAILROAD RD, MARION ST / RT NN, MAIN ST   REGIONAL WEST FIRE PROTECTION DISTRICT ALARM AND PAGER TEST. 
 (DONOTREPLY) Gabathuler, Erin 19:46 Medical Emergency 925 DUNKLIN A -Cross Streets- LEE DR, LINN ST / CHESTNUT ST E1   FEMALE WAS SPEAKING WITH THE RP AND THEN HER EYES
 (DONOTREPLY) Suchanek, Celeste 17:51 Medical Emergency 8501 NO MORE VICTIMS -Cross Streets- DEAD END / CORTEZ DR S2   nose bleed   front entrance 
 (DONOTREPLY) E4  18:42 Natural Cover  - 7 HILLS -Cross Streets-   NATURAL COVER FIRE IN THE AREA 
 (DONOTREPLY) Suchanek, Celeste 17:02 Medical Emergency 807 MONROE -Cross Streets- E HESS WAY / E ASHLEY ST E1   FALLEN OFF BIKE 
 
 [DONOTREPLY] 1101 1401 1110 112 CCPager  16:58 Fire Alarm - 2227 SADDLEBROOKE LAKE -Cross Streets- CHERISH CT / SCRUGGS STATION RD   ALLEN RESIDENCE \n
 [DONOTREPLY] Suchanek, Celeste 17:02 Medical Emergency 807 MONROE -Cross Streets- E HESS WAY / E ASHLEY ST E1   FALLEN OFF BIKE \n
 [DONOTREPLY] Suchanek, Celeste 17:51 Medical Emergency 8501 NO MORE VICTIMS -Cross Streets- DEAD END / CORTEZ DR S2   nose bleed   front entrance \n
 [DONOTREPLY] E4  18:42 Natural Cover  - 7 HILLS -Cross Streets-   NATURAL COVER FIRE IN THE AREA \n
 [DONOTREPLY]  19:01 Test - 1229 BROADWAY -Cross Streets- W RAILROAD RD, MARION ST / RT NN, MAIN ST   REGIONAL WEST FIRE PROTECTION DISTRICT ALARM AND PAGER TEST. \n
 [DONOTREPLY] Gabathuler, Erin 19:46 Medical Emergency 925 DUNKLIN A -Cross Streets- LEE DR, LINN ST / CHESTNUT ST E1   FEMALE WAS SPEAKING WITH THE RP AND THEN HER EYES\n
 [DONOTREPLY] Gabathuler, Erin 20:08 Medical Alarm 1509 STADIUM -Address Between- MYRTLE AVE / SOUTHWEST BLVD E4   CCEMS ADVISED  KEY - UNDER WHITE ROCK - UNDER/NEX\n
 [DONOTREPLY] S5  22:59 Accident with Injuries - 1700 INDUSTRIAL -Cross Streets- ARGONNE ST / HUGHES ST   EMS ADV  CAR VS TRAIN-  TRAPPED IN VEH \n
 [DONOTREPLY] Batt11  22:59 Extrication - 1700 INDUSTRIAL -Cross Streets- ARGONNE ST / HUGHES ST   UNION PACIFIC ADVISED- THEY CALLED INTO REPORT A PROBLEM - THEY WERE UNSURE OF WHAT THE PROBLEM WAS- THEY HAVE BEEN ADVISED TO STOP ALL TRAIN TRAFFIC  EMS ADV  CAR VS TRAIN-  TRAPPED IN VEH \n
 [DONOTREPLY] DC4 E1 L1 S2 E4  23:39 Fire Alarm - 2033 CHRISTY -Cross Streets- VETERANS LN / RT B   Call Number 434 was created from Call Number 433(May  3 2012 11:37PM)  PERIMETER AND FIRE ALARM - OFFICE HALL MOTION DETECTOR #2406 \n
 [DONOTREPLY] AC2 E1 L1 S2 E3  08:40 Fire Alarm - 1445 CHRISTY -Cross Streets- OSCAR DR / RAMP 31-970733, TANNER BRIDGE RD   GENERAL  FIRE ALARM - ROOM SMOKE  MID MO ANESTHIA \n
 [DONOTREPLY] Degraffenreid, Jenny 09:41 Medical Emergency 2225 WEATHERED ROCK 4 -Cross Streets- LORENZO GREENE DR / MILLBROOK DR E1   SAYS SHE TRIED TO DRIVE BUT COULDN'T. BLEEDING FR\n
 [DONOTREPLY] E3  13:39 Line Down  - 1304 MCCARTY -Cross Streets- HAVANA ST, HART ST / HAVANA ST   TREE DOWN ACROSS W MCCARTY STREET - TOOK DOWN POWER LINES AS WELL  Call #: 617 - RP THINKS WIRES ARE DOWN ON MCCARTY  Call #: 617 - RP HEARD A LOUD POPPING SOUND \n
 [DONOTREPLY] E1  13:46 Assist Police - 2201 MAIN -Cross Streets- MEMORIAL PARK DR / FOREST HILL AVE   93 REQUESTING WASH DOWN AFTER THE SAFETY PATROL PICNIC. (JUST SEND E1) \n
 [DONOTREPLY] Degraffenreid, Jenny 14:24 Medical Emergency 120 DUNKLIN -Cross Streets- W DUNKLIN ST, JEFFERSON ST / MADISON ST S5  \n
 [DONOTREPLY] AC2 E4 S5 E3 L1  14:48 Fire Alarm - 1201 FAIRGROUNDS -Cross Streets- VILLAGE DR / S COUNTRY CLUB DR, W EDGEWOOD DR   OFFICE AREA \n
 [DONOTREPLY] Taylor, Tiffany 15:08 Medical Emergency 711 KANSAS -Cross Streets- WASHINGTON PARK DR / MYRTLE AVE, SOUTH BLVD E1   SUBJ IS NOW TRYING TO LEAVE -  RP WAS ABLE TO GET\n
 [DONOTREPLY] Degraffenreid, Jenny 16:33 Medical Emergency 406 ATCHISON -Cross Streets- No Cross Streets Found E1   SEIZURE - BEING COMBATIVE \n
 [DONOTREPLY] Degraffenreid, Jenny 16:42 Medical Emergency 2021 WILLIAM -Cross Streets- BLAIR DR / DIX RD E3   STOMACH PAIN, FACE IS FLUSH, PRESSURE IN HEAD.  S\n
 [DONOTREPLY] AC2 L1 E4  16:42 Accident with Injuries - JEFFERSON -Cross Streets-   EMS NOTIFIED  Call #: 691 - METALIC CAR  ELDERLY MALE DRIVER  Call #: 691 - AT CONOCO  UNKNOWN INJURIES  SIL CAR RAN INTO GAURD-RAIL  Call #: 691 - CALLER DOES NOT BELIEVE THIS IS AN ACCIDENT, BUT MAYBE ELDERLY MALE HAVING SOME KIND OF ISSUE  Call #: 691 - VEH IS UP ON GUARD RAIL  Call #: 691 - W/M\n
 [DONOTREPLY] Degraffenreid, Jenny 16:42 Medical Emergency 2021 WILLIAM -Cross Streets- BLAIR DR / DIX RD L3   STOMACH PAIN, FACE IS FLUSH, PRESSURE IN HEAD.  S\n
 [DONOTREPLY] Schlitt, Meagen 18:53 Medical Emergency 3038 TRUMAN -Cross Streets- DUPONT CIR / N TEN MILE DR E3   RM 401  RESIDENT MAY BE HAVING HEART ATTACK \n
 [DONOTREPLY] AC2 E1 L1 S2 E4  19:22 Structure Fire - ELM -Cross Streets-   WHITE SMOKE COMING FROM FRONT OF BUILDING \n
 [DONOTREPLY] 1401 1531 1201 114 CCPager  22:41 Accident with Injuries - 54 -Cross Streets-   OPER 133 CALLING TROOP  CCEMS ADVISED  ONE PATIENT INJURED  ONE VEH ACCIDENT   WHITE  93  CHEVY LUMINA-  HIT THE GUARDRAIL ON THE HWY -  FEMALES HEAD IS BLEEDING \n
 [DONOTREPLY] Volmert, F Michelle 01:01 Medical Emergency 401 DUNKLIN -Cross Streets- MULBERRY ST / MISSOURI BLVD, BOLIVAR ST E1   FEMALE IS LISA CREASON-   THEY ARE SITTING AT THE\n
 [DONOTREPLY] Volmert, F Michelle 05:16 Medical Emergency 706 HOUCHIN A -Cross Streets- E DUNKLIN ST / BALD HILL RD E4   ASTHMA ATTACK \n
 [DONOTREPLY] E1  07:12 Test - 621 HIGH -Cross Streets- BOLIVAR ST / RAMP ON RAMP, RAMP OFF RAMP  \n
 [DONOTREPLY] Bashore, April 10:39 Medical Alarm 12 JACKSON 513 -Address Between- DEAD END / E STATE ST E1   GENERAL MEDICAL ALARM \n
 [DONOTREPLY] E1  10:55 Trapped In Elevator - 12 JACKSON -Cross Streets- DEAD END / E STATE ST   SUBL STUCK IN ELEVATOR \n
 [DONOTREPLY] Batt11 E1 L1 E3 S5  12:15 Fire Alarm - 1303 EDMONDS -Cross Streets- MONTANA ST / MYRTLE AVE   SECOND FLOOR ROOM SMOKE \n
 [DONOTREPLY] Bashore, April 15:04 Medical Emergency 221 FILLMORE -Cross Streets- LINDEN DR, BROADWAY ST / WASHINGTON ST E1   SHE IS 101  MALE STATING MARGARET WAS READING AND\n
 [DONOTREPLY] Snyder, Jodi 18:23 Medical Emergency 242 JAYCEE -Cross Streets- SCHELLRIDGE RD / INDUSTRIAL DR E3   CUSTOMER HAVING SEZUIRE \n
 [DONOTREPLY] Snyder, Jodi 21:02 Medical Alarm 945 WINDSOR -Address Between- WESTWOOD DR / TOWER DR E3   JAMES WAGGONER, CODE FOR GARAGE 4949, CAN ACCESS \n
 [DONOTREPLY] 00:47 Trash Dumpster - 1131 MCCARTY 2 -Cross Streets- LOCUST ST / CLARK AVE E1  \n
 [DONOTREPLY] 05:15 Fire Alarm - 1201 FAIRGROUNDS -Cross Streets- VILLAGE DR / S COUNTRY CLUB DR, W EDGEWOOD DR Batt11 E4 S5 E3 L3  \n
 [DONOTREPLY] Medical Emergency 314 BROOKS ST JEFFERSON CITY -Cross Streets- W MCCARTY ST / W HIGH ST E1   E1 \n
 [DONOTREPLY] 08:27 Fire Alarm - 1609 ST MARYS -Cross Streets- HOWARD ST / GIPFERT LN Batt13 E4 L3 E3 S5   DOROTHIA SCOTT RES - HALL WAY ZONE \n
 [DONOTREPLY] 08:31 Fire Alarm - 1201 FAIRGROUNDS -Cross Streets- VILLAGE DR / S COUNTRY CLUB DR, W EDGEWOOD DR S5 E4 L1   MAIN OFFICE AREA \n
 [DONOTREPLY] Medical Emergency 706 OHIO ST JEFFERSON CITY -Cross Streets- MISSOURI BLVD / LOUISIANA AVE E1   ELDERLY CHURCH MEMBER HAS FALLEN AND HIT HEAD, LA E1 \n
 [DONOTREPLY] 12:38 Assist Citizen Fire - 2731 MERCHANTS -Cross Streets- METRO DR / S HWY 179 E3  \n
 [DONOTREPLY] Medical Emergency 730 W  STADIUM BLVD JEFFERSON CITY -Cross Streets- HYDE PARK RD / MISSOURI BLVD, ST MARYS BLVD E3   E3 \n
 [DONOTREPLY] 15:13 Fire Alarm - 1609 ST MARYS -Cross Streets- HOWARD ST / GIPFERT LN Batt13 E1 L3 E3 S5   SON IS ON SITE AND STATED EVERYTHING IS FINE - RESIDENT DOESN'T REMEMBER THE CODE  RESIDENT IS ELDERLY  HALLWAY FIRE  DORTHY SCOTT RESIDENT \n
 [DONOTREPLY] 15:16 Fire Alarm - 3016 DUPONT -Cross Streets- W TRUMAN BLVD / DEAD END Batt13 E1 L3 E4 S5   GENERAL \n
 [DONOTREPLY] Medical Emergency 2700 CHERRY CREEK CT I205 JEFFERSON CITY -Cross Streets- DEAD END / N HWY 179 E3   CCEMS NOTIFIED  ELDERLY FEMALE HAS FALLEN - DOOR  E3 \n
 [DONOTREPLY] Medical Emergency 812 E  HIGH ST A JEFFERSON CITY -Cross Streets- CHERRY ST / CHESTNUT ST E1   BAD PHONE CONNECTION  MALE CAN'T WALK  CCEMS NOTI E1 \n
 [DONOTREPLY] Medical Emergency 811 MONROE ST B JEFFERSON CITY -Cross Streets- E HESS WAY / E ATCHISON ST E1   FEMALE IS ON THE PATIO  GIRLFRIEND FELL OUT OF CH E1 \n
 [DONOTREPLY] 07:23 Line Down  - 800 MYRTLE blk -Cross Streets- PIKE ST / KANSAS ST, SOUTH BLVD E1   POSSIBLE POWER LINE BROKEN AND PARTIALLY LYING IN THE STREET \n
 [DONOTREPLY] 09:17 Fire Alarm - 6336 ALGOA -Cross Streets- CALBACKNA RD / N SHAMROCK RD Batt11 E3 L1 S2 E4  \n
 [DONOTREPLY] 10:00 Fire Alarm - 6336 ALGOA -Cross Streets- CALBACKNA RD / N SHAMROCK RD S2   WATERFLOW FIRE ALARM CAME IN 3 TIMES  AUTOMATIC FIRE ALARM \n
 [DONOTREPLY] Medical Emergency 1100 MISSOURI BLVD JEFFERSON CITY -Cross Streets- GEORGIA ST / KANSAS ST E1   CUSTOMER ON SCEN   CHEST PAIN  E1 \n
 [DONOTREPLY] Medical Emergency 1952 SARATOGA BLVD A JEFFERSON CITY -Cross Streets- LEXINGTON DR / LEXINGTON DR E4   91YO FEMALE  LOCATED IN THE KITCHEN  WIFE FELL AN E4 \n
 [DONOTREPLY] 14:19 Accident with Injuries - MISSOURI -Cross Streets- Batt11 L3   EMS ENROUTE  PULLING IN TO THE COLTONS PARKING LOT  HE STATES THE FEMALE IS REFUSING TO MOVE AWAY FROM THE LIGHT.  AND IS BLOCKING TRAFFIC  INEZ THOMAS IN THE TAN SONATA  2ND PARTY CALLED - COMPLAINING OF NECK PAIN  RP STATES THE OPERATOR OF THE SONATA THREATENED HIM.  HE BUMPED HER BUMPER, NO DAMA\n
 [DONOTREPLY] Medical Emergency 12 JACKSON ST JEFFERSON CITY -Cross Streets- DEAD END / E STATE ST E1   CCEMS NOTIFIED  JUST INSIDE IN THE LOBBY - MALE F E1 \n
 [DONOTREPLY] 17:48 Fire Alarm - 301 HIGH -Cross Streets- BROADWAY ST / RAMP ON RAMP, RAMP OFF RAMP Batt11 E1 L1 S2 E4  \n
 [DONOTREPLY] 18:04 Message Deliver - 5206 MONTICELLO -Cross Streets- WALTHER AVE / MOREAU RIDGE RD, OAKRIDGE RD   TRAINING MEETING TONIGHT 1900HRS AUTHORITY 110 \n
 [DONOTREPLY] 18:57 Unusual Fire Request - 1922 MISSOURI -Cross Streets- HILDA ST / DIX RD L3   OPER 149 CALLED STATION 1 - L3 WILL RESPOND  SOMEONE THREW THEIR KEYS ON THE ROOF OF BASKIN ROBBINS - EMPP DOES NOT HAVE ACCESS TO THE ROOF \n
 [DONOTREPLY] 19:01 Test - 4017 COUNTY PARK -Cross Streets- ROCKRIDGE RD / FAIRGROUNDS RD, LEANDRA LN   COLE CO FIRE PROTECTION DISTRICT WEEKLY ALARM AND PAGER TEST. \n
 [DONOTREPLY] Medical Emergency 210 EL MERCADO PLZ JEFFERSON CITY -Cross Streets- No Cross Streets Found S5   THIS IS METRO BUSINESS COLLEGE  TEACHER IS ILL, F S5 \n
 [DONOTREPLY] Medical Emergency 50 JEFFERSON CITY -Cross Streets- E3 S5   ****2 YO HAVING SEIZURE  2 YO WITH FLASHERS ON  H E3 S5 \n
 [DONOTREPLY] 23:04 Smoke In Structure - 509 TURNBERRY JEFFERSON CITY -Cross Streets- STONE BRIAR RD / PINEHURST CT Batt11 E4 S5 E3 L3  \n
 [DONOTREPLY] 23:39 Accident with Injuries - 1101 S  COUNTRY CLUB JEFFERSON CITY -Cross Streets- OLD LOHMAN RD / DENESE DR Batt11 S5   CCEMS ADVISED  UNKNOWN INJURIES-RP NOT ABLE TO GO OUTSIDE & CHECK  1 VEH IN RP'S FRONT YARD- \n
 [DONOTREPLY] 02:52 Fire Alarm - 509 TURNBERRY JEFFERSON CITY -Cross Streets- STONE BRIAR RD / PINEHURST CT Batt11 E4 S5 E3 L3   OPER TRAVIS REF # 2180  TOM KOLB RESD--GENERAL \n
 [DONOTREPLY] Medical Emergency 235 JOE LN JEFFERSON CITY -Cross Streets- DEAD END / SCENIC DR S2   NO HISTORY OF BREATHING PROBLEMS  63 YO MALE HAVI S2 \n
 
 */
 
 
 public class MOJeffersonCityParser extends MsgParser {
   
   private static final Pattern MASTER_PTN = 
     Pattern.compile("(?:(.*)\\b(\\d\\d:\\d\\d) )?(.*) (?:-Cross Streets-|-Address Between-) (.*)");
   private static final Pattern NUMBER_PTN = Pattern.compile("\\b\\d{2,}\\b");
   private static final Pattern UNIT_PTN = Pattern.compile("(?:(?: |^)[A-Z]+\\d+)+$", Pattern.CASE_INSENSITIVE);
   private static final Pattern UNIT2_PTN = Pattern.compile("(?:(?: |^)[A-Z]+\\d+)+ ", Pattern.CASE_INSENSITIVE);
   
   public MOJeffersonCityParser() {
     super("JEFFERSON CITY", "MO");
   }
   
   @Override
   public String getFilter() {
     return "paging@jeffcitymo.org";
   }
   
   @Override
   public int getMapFlags() {
     return MAP_FLG_SUPPR_DIRO;
   }
 
   @Override
   public boolean parseMsg(String subject, String body, Data data) {
     if (!subject.equals("DONOTREPLY")) return false;
     body = body.replace('\n', ' ');
     Matcher match = MASTER_PTN.matcher(body);
     if (!match.find()) return false;
     
     String sPrefix = getOptGroup(match.group(1));
     if (sPrefix.contains(",")) {
       data.strName = sPrefix;
     } else {
       data.strUnit = sPrefix;
     }
     data.strTime = getOptGroup(match.group(2));
     String sPart1 = match.group(3).trim();
     String sPart2 = match.group(4);
     
    if (sPart1.endsWith(" JEFFERSON CITY")) sPart1 = sPart1.substring(0,sPart1.length()-15);
    int pt = sPart1.indexOf(" - ");
     if (pt >= 0) {
       data.strCall = sPart1.substring(0,pt).trim();
       parseAddress(sPart1.substring(pt+3).trim(), data);
     } else if ((match = NUMBER_PTN.matcher(sPart1)).find()) {
       data.strCall = sPart1.substring(0,match.start()).trim();
       parseAddress(sPart1.substring(match.start()), data);
     } else return false;
     
     Parser p = new Parser(sPart2);
     String sCross = p.get("  ");
    data.strSupp = p.get();
     
     match = UNIT_PTN.matcher(sCross);
     if (match.find()) {
       data.strUnit = match.group().trim();
       sCross = sCross.substring(0,match.start()).trim();
     }
     
     if (data.strUnit.length() == 0 && data.strSupp.length() == 0) {
       match = UNIT2_PTN.matcher(sCross);
       if (match.find()) {
         data.strUnit = match.group().trim();
         data.strSupp = sCross.substring(match.end()).trim();
         sCross = sCross.substring(0,match.start()).trim();
       }
     }
     data.strCross = sCross;
     
     if (data.strUnit.length() > 0 && data.strSupp.length() > 0) {
       if (data.strSupp.endsWith(data.strUnit)) {
         data.strSupp = data.strSupp.substring(0,data.strSupp.length()-data.strUnit.length()).trim();
       }
     }
     return true;
   }
 }
