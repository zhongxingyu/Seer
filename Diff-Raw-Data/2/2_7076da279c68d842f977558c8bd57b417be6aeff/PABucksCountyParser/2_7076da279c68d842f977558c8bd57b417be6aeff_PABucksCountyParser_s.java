   package net.anei.cadpage.parsers.PA;
 
 import java.util.Properties;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.FieldProgramParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /*
 Bucks County, PA
 Contact: "Ryan Pankoe" <rpankoe@gmail.com>
 Contact: Ed Ackerman <ed.ackerman@gmail.com>
 Contact: Terry <securetalk@gmail.com>
 Contact: "Russ Swinehart Jr." <snoopfire10@yahoo.com>
 Contact: Jayson Wagner <jayson.wagner58@yahoo.com>
 Contact: John Shipp <dieseltruckie14@gmail.com>
 Contact: Kevin Coughlin <kcoughlin37@gmail.com>
 Contact: Patrick Roberts <pgremt@gmail.com>
 Contact: "sweetcheeks6839@aol.com" <sweetcheeks6839@aol.com>
 Sender: 8276,a@bnn.us,215,2002
 
 911:SQ134  type:BTAI    adr:S LIMEKILN PK/UPPER STATE RD ,48  aai:  box:74064  map:3033F7  tm:21:05:23  ED1042264
 911:SQ134  type:ARESP   adr:4217 HORSESHOE WY ,36  btwn BERRY BROW DR &amp; SPARROW WY  aai:  box:60002  map:2921B10  tm:05:02:04  ED1042293  
 911:SQ134  type:ARESP   adr:221 HAMPSHIRE DR ,48  btwn CORNWALL DR &amp; SURREY RD  aai:  box:74062  map:3033E6  tm:14:06:02  ED1042334  
 911:SQ134  type:AFAINT  adr:1553 LOWER STATE RD ,29  btwn BRISTOL RD &amp; OLD NEW RD  aai:  box:78054  map:3034E7  tm:20:28:45  ED1042385  
 911:SQ134  type:BTAI    adr:1235 S TOWNSHIP LINE RD ,36  btwn CHALFONT RD &amp; LIMEKILN PK  aai:  box:61003  map:2921D10  tm:12:02:56  ED1042577  
 911:SQ134  type:BBLED   adr:102 HARRISON FORGE CT ,48  btwn E FAIRWOOD DR &amp; CUL DE SAC  aai:  box:34030  map:3033H1  tm:14:38:26  ED1042595  
 911:SQ134  type:BINJ    adr:BARC HOUSE ,47 at 80 PUEBLO RD ,47 btwn LENAPE DR &amp; UTE RD  aai:DOWNSTAIRS  box:34033  map:3033J2  tm:16:57:04  ED1043086  
 911:STA19  type:FALRM   adr:DTH ,29 at 595 W STATE ST ,29 btwn PROGRESS DR &amp; RT 611 BY  aai:215 345-2200  box:19051  map:3034G2  tm:12:37:42  FD101999
 911:SQ134  type:ASEIZ   adr:BRISTOL RD/UPPER STATE RD ,29  aai:AT ENTR TO BLOOMING GLEN  box:34041  map:3033K5  tm:11:01:28  ED1043679
 911: STA19  type:FUNK    adr:EDISON RD/RT 611 ,29  aai:  box:79028  map:3034K5  tm:12:44:52  FD1108375    Run: E79    \n
 911: STA16  type:RAUTO   adr:ROBS TRUCK & TRAIL ,22 at 750 STREET RD ,22 btwn CLIVE AV & SAMUEL AV  aai:IFO  box:37017  map:3374A8  tm:16:43:05  FD1116594    Ru
 911:SQ134  type:ATRAN   adr:2343 BETHLEHEM PK #312 ,36 -- DOCK MEADOWS NH btwn UNIONVILLE PK &amp; CHURCH RD  aai:GO TO CHURCH RD NORTH ENTRANCE box:60063 map:3032E3 &nbsp;tm:14:48:06 ED1042598&nbsp;
 911: SQ134  type:BFALL   adr:800 MANOR DR #315 ,48 -- NEW SEASONS ASSIST btwn E SCHOOL HOUSE RD & COUNTY LINE  aai:  box:74066 map:3033E8 tm:16:50:43  ED1101813
 911: SQ134  type:AALLR   adr:4371 COUNTY LINE RD ,48 -- VERTIS DIRECT MKTG btwn BRITAIN WOOD CI & RR CROSSING  aai:  box:74096 map:3033C7 tm:09:28:45  ED1102262
 911: STA57  type:FEMS    adr:SLEEPY HOLLOW RD/SPINNERSTOWN RD ,45  aai:  box:75011  map:2693K9  tm:10:26:44  FD1104574    Run: E75
 911: STA57  stype:FTAI    adr:FRIER RD/MILL RD ,45  aai:  box:57002  map:  tm:13:51:38  FD1104463    Run: E57
 911: STA4  type:RAUTO   adr:PHILMONT AV/E STREET RD ,43  aai:PHILMONT SO STREET  box:04003  map:3261B8  tm:10:21:22  FD1104774    Run: R4 R6 E1
 911: STA53  type:RMAR    adr:LIONS PARK ,24 at 102 SAMUEL CLIFT ST ,24 btwn MILL ST & MARKET ST  aai:  box:51012  map:3375D5  tm:00:59:10  FD1105678 Run: R53 M53 M11
 911: STA53  type:WIREIN  adr:2218 TRENTON AV ,24  btwn BARRY PL & TAFT ST  aai:  box:53034  map:3375F2  tm:11:40:15  FD1104667    Run: L25 Q51 E50 E52
 911: STA53  stype:RAUTO   adr:NEW FALLS RD/PINE AV ,25  aai:  box:12044  map:3262G9  tm:21:30:34  FD1107837    Run: R13 R53 E82
 911: STA19  type:WATER   adr:EIGHTEENFOURTEEN H ,28 at 50 S MAIN ST ,28 btwn W OAKLAND AV & TAYLOR AV  aai:  box:19022  map:2922J10  tm:02:29:07  FD11
 911: STA19  type:RVBLD   adr:LOBSTER CLAW ,57 at 800 N EASTON RD ,57 btwn BKHAM TWP LINE & CROSS KEYS DR  aai:  box:20001  map:2922J6  tm:16:03:22  FD
 911: STA58  type:FBLD    adr:121 PARK AV ,58  btwn FRONT ST & S 2ND ST  aai:QUAKERTOWN ASSOCIATES 215-901-8033  box:17001  map:2695H10  tm:17:44:38   FD1110887    Run: L57 SQ18 E36 R18 R58 
 911: STA14  type:EXBLD   adr:3501 BRISTOL OXF VALLEY RD #1107 ,25 -- AVALON COURT APT btwn LESLIE DR & BORDER  aai:  box:82015  map:3263A9  tm:22:06:28  FD1110945  Run: TW14 E12
 911: SQ134  type:APOSXP  adr:307 W BUTLER AV ,48  btwn BRITTANY DR & SURREY RD  aai:NE SECTOR  box:74062  map:3033E6  tm:07:24:51  ED1126192
 911: STA4  type:APOSXP  adr:3449 AZALEA AV ,22  btwn TRAILWOOD DR & VALLEY RD  aai:  box:04007  map:3261E8  tm:05:29:00  ED1126324  
 911: SQ134  type:AASSLT  adr:STADIUM BAR & GRIL ,36 at 1903 BETHLEHEM PK ,36 btwn SWARTLEY RD & MILL RD aai:  box:60063  map:3032F3  tm:02:33:13  ED1127778  
 911: STA4  type:RDOM    adr:NESHA STATE PARK ,22 at 3401 STATE RD ,22 btwn DUNKSFERRY RD & WINKS LA  aai:NEXT TO POOL AREA AT PICNIC AREA  box:37023  map:3374C7  tm:12:52:17  FD1111780    Run: R16 SQ37 R4
 911: SQ134  type:ATRAN   adr:CENTER SQUARE TOWR #823-A ,28 at 555 BROAD ST #823-A ,28 btwn ATKINSON DR & VETE  aai:  box:19052  map:2922H10 tm:18:18:15  ED1127865  
 STA19  type:FALRM   adr:LINDEN ELEM SCH ,28 at 480 LINDEN AV ,28 btwn EAST ST & ROHR DR  aai:  box:19093  map:2923A10  tm:20:27:42  FD1111756    
 911: STA19  type:GALRM   adr:6 FLINT CI ,29  btwn WINDSOR WY & CUL DE SAC  aai:  box:79020  map:3034H5  tm:23:32:45  FD1112459    Run: E79    \n
 911: SQ122  type:ESPEC   adr:HARTS FIRE CO ,74 at 1195 YORK RD ,74 btwn OLD YORK RD & BRISTOL RD  aai:  box:93030  map:3147E4  tm:05:04:12  ED1131511
 911: SQ134  type:BASSLT  adr:248 INVERNESS CI ,48  btwn BONNIE LARK CT & CUL DE SAC  aai:  box:74059  map:3033F7  tm:16:38:31  ED1133930  
 911: SQ134  type:FIRCAL  adr:4201 MILORDS LA ,26  btwn SIR ANDREW CT & SIR ANDREW CT  aai:NEXT DOOR  box:19018  map:2923B4  tm:19:34:11  ED1136021  
 911: STA19  type:WIRES   adr:RT 202/N SHADY RETREAT RD ,47  aai:  box:19006  map:3034E1  tm:13:59:42  FD1116344    Run: E19    \n
 911: STA19  type:FDWL    adr:260 SANDY RIDGE RD ,29  btwn CAVALLO WY & IRON HILL RD  aai:  box:19006  map:3034C1  tm:12:08:50  FD1116685    Run: L79 E
 911: SQ134  type:ATAI    adr:BETHLEHEM PK/HILLTOWN PK ,36  aai:  box:60041  map:3032H4  tm:21:37:43  ED1139682  
 911: STA5  type:FALRM   adr:5263 MOUNTAIN TOP RD ,26  btwn HOLICONG RD & DEAD END  aai:MARTINI RESD #2157942445  box:05027  map:2924E9 tm:19:46:52  FD1118872    Run: E5
 911: STA5  type:RAUTO   adr:HOLICONG RD/YORK RD ,26  aai:ON YORK NO HOLICONG  box:05021  map:2924B6  tm:12:43:08  FD1117995    Run: R5 E5
 911: STA5  type:FBLD    adr:4201 MILORDS LA ,26  btwn SIR ANDREW CT & SIR ANDREW CT  aai:NEXT DOOR  box:19018  map:2923B4  tm:19:34:01 FD1116371    Run: L5 E79 E20 TW24
 911: STA5  type:WIRES   adr:RT 413/UPPER MOUNTAIN RD ,26  aai: box:05025  map:2924B10  tm:17:35:03  FD1118467    Run: E15-1
 911: STA5  type:WIRES   adr:CHURCH SCHOOL RD/MECHANICSVILLE RD ,26 aai:ON MECHANICSVILLE  box:15021  map:2923F9  tm:14:06:20  FD1118031 Run: E15
 911: STA5  type:ELEVAT  adr:OUR LADY GUADAL ,26 at 5194 COLD SPRING CREAME RD ,26 btwn GREYSTONE DR & RT 413  aai:  box:15052  map:2923F2 tm:14:31:10  FD1116465    Run: E15
 911: STA5  type:FINV    adr:AQUETONG RD/TAYLOR LA ,65  aai:  box:05031  map:2924E1  tm:21:09:49  FD1117661    Run: E5
 911: STA4  type:FSPEC   adr:2318 KAY AV ,22  btwn DEAD END & MASTER AV  aai:  box:04005  map:3261C10  tm:14:27:35  FD1200565    Run: E4-1
 911: STA4  type:FALRM   adr:3338 RICHLIEU RD ,22 -- COUNTRY COMMONS btwn DEBRA DR & PA TP  aai:215 639 2800  box:65031  map:3261H9  tm:17:34:44 FD1200573    Run: E65 E
 911: SQ134  type:COP     adr:90 ROBIN LA ,36  sbtwn MAPLE LEAF DR & COURT  saai:  box:27031  map:2920C5  tm:12:14:59  ED1202205  
 911: SQ134  type:BTRAN   adr:139 BIRCHWOOD DR ,27  btwn BIRCHWOOD DR & LYNWOOD DR  aai:  box:34011  map:3033E3  tm:19:20:35  ED1203702  
 911: SQ134  type:BBLED   adr:DOCK MEADOWS NH #14 ,36 at 2343 BETHLEHEM PK #14 ,36 btwn UNIONVILLE PK & CHURCH  aai:USE RT 309 ENTRANCE  box:60063  map:3032E3  tm:18:28:26  ED1203695  
 911: STA19  type:FALRM   adr:GRUNDY HALL ,29 at 1290 ALMSHOUSE RD ,29 btwn TURK RD & RT 611  aai:  box:79057  map:3034J7  tm:03:30:43  FD1201380    Ru
 911: SQ134  type:ACVA    adr:NEW SEASONS ASSIST #110 ,48 at 800 MANOR DR #110 ,48 btwn E SCHOOL HOUSE RD & AN  aai:  box:74066  map:3033E8  tm:18:15:21  ED1203569  
 911: SQ134  type:AFAINT  adr:1500 HORIZON DR #112 ,48 -- ENDODONTIC SPECIAL btwn HORIZON CI & COUNTY LINE RD  aai:  box:74058  map:3033F8  tm:16:29:39  ED1203559  
 911: SQ134  type:ABLED   adr:DOCK MEADOWS NH #14 ,36 at 2343 BETHLEHEM PK #14 ,36 btwn UNIONVILLE PK & CHURCH  aai:  box:60063  map:3032E3  tm:09:32:32  ED1203769  
 911: SQ134  type:BFALL   adr:WALMART STORE ,36 at 1515 BETHLEHEM PK ,36 btwn HILLTOWN PK & SWARTLEY RD  aai:W SECTOR  box:60062  map:3032H4  tm:14:32:51  ED1203813  
 911: SQ134  type:AABDO   adr:54 CHEROKEE RD ,47  btwn LENAPE DR & PUEBLO RD  aai:  box:34033  map:3033J2  tm:23:45:25  ED1203857
 911: STA57  type:BTAI    adr:PORTZER RD/RT 663 ,45  aai:ON PORTZER,,PAST ANIMAL HOSP  box:57007  map:2695A9  tm:18:05:19  ED1203958
 911: STA57  type:STDBY   adr:PORTZER RD/RT 663 ,45  aai:  box:57007  map:2695A9  tm:18:05:25  FD1201500    Run: E57
 911: STA57  type:FPOL    adr:OLD BETHLEHEM PK/PORTZER RD ,45  aai:  box:57003  map:2695A8  tm:18:16:46  FD1201502\4sRun: FP57 FP75
 911: STA57  type:BTAI    adr:PORTZER RD/RT 663 ,45  aai:ON PORTZER,,PAST ANIMAL HOSP  box:57007  map:2695A9  tm:18:05:19  ED1203958
 
 Contact: "porco23@yahoo.com" <porco23@yahoo.com>
 Contact: Patrick Roberts <pgremt@gmail.com>
 Sender: alert_@alert.bucksema.org
 (#6331  1/1) FDWL\nadr:2008 BRISTOL RD ,75  btwn SHADOW CREEK LA & GUINEA LA\nbox:29045\ntm:22:34:01 FD1200729\nRun: E29 E78 L29 E93
 (#6779  1/1) FEMS\nadr:1837 JERICHO DR ,75\nbox:29058\ntm:01:33:42 FD1201317\nRun: E29
 (#6613  1/1) WIREIN\nadr:2401 GREENSWARD RD N ,75\nbox:29501\ntm:11:51:55 FD1201251\n
 (#6393  1/1) FUMEIN\nadr:2009 CEDAR DR ,76  btwn WINDSOR LA & APPLE CT\nbox:66001\ntm:13:50:55 FD1200871\nRun: TW66 E66 E29 E93 R29
 (#6417  1/1) BFALL\nadr:3018 STREET RD ,75  btwn HONORA ST & FOLLY RD\nbox:78013\ntm:15:33:54 ED1202384
 (#6499  1/1) FDWL\nadr:2221 SARAHS LA ,75  btwn MARIE CI & CUL DE SAC\nbox:29059\ntm:22:13:24 FD1201135\nRun: E29 E78 L29 E66
 (#6613  1/1) WIREIN\nadr:2401 GREENSWARD RD N ,75\nbox:29501\ntm:11:51:55 FD1201251\nRun: E29 E78 L90
 (#7645  1/1) BTAI\nadr:W STREET RD/YORK RD ,74\nbox:90041 map:3147E7\ntm:08:14:08 ED1204133
 (#7651  1/2) BFALL\nadr:2010 YORK RD ,76 -- YORK TOWN MANOR\nbtwn:MEYER WY & PARK DR\naai:UNIT 119\nbox:66050 map:3035F10\ntm:09:07:36
 
 Contact: Joseph Montanya <flounder53@gmail.com>
 Contact: "Russ Swinehart Jr." <snoopfire10@yahoo.com>
 Contact: Kevin Campbell <kevincampbell800@gmail.com>
 Contact: "Ryan Pankoe" <rpankoe@gmail.com>
 Sender: @alert.bucksema.org
 Subject:1/2\nFAPT\nadr:CREEK VILL APT #F18 ,71 at 160 FALLSINGTON TULLYT RD #F18 ,71\nbox:33022\ntm:18:57:03 FD1201370\nRun: E33 L3
 Subject:1/1\nFSPEC\nadr:1812 FARRAGUT AV ,24\nbox:53039\ntm:12:44:12 FD1201444\nRun: L25
 Subject:1/2\nFAPT\nadr:GRUNDY TOWERS #1010 ,24 at 205 POND ST #1010 ,24\nbox:50092\ntm:18:46:52 FD1201459\nRun: E50 E52 E53 Q51 L25
 Subject:1/1\nFBLD\nadr:301 WOOD ST ,24 -- LBUCK SENIOR CITIZ\nbox:51015\ntm:09:59:54 FD1201481\nRun: E50 E52 E53 Q51 TW14 E11 R10
 Subject:1/2\nFALRM\nadr:ST ANDREWS EPISCOP ,79 at 55 W AFTON AV ,79\nbtwn:PENN VALLEY DR & BREECE DR\naai:PARRISH HOUSE\nbox:00071
 Subject:1/1\nATRAN \nadr:101 BARBERRY CT ,48 \nbtwn:CUL DE SAC & E FAIRWOOD DR \nbox:34030 map:3033H1 \ntm:11:11:13 ED1205059
 
 Contact: Ed Ackerman <ed.ackerman@gmail.com>
 Sender: Bucks RSAN
 FRM:Bucks RSAN\nSUBJ:1/1\nMSG:WIREIN\nadr:1871 WHITE BRIAR RD ,73\nbox:02047\ntm:09:52:53 FD1201327\nRun: E2 L1 E4 R6=
 FRM:Bucks RSAN\nSUBJ:1/1\nMSG:AUNR\nadr:BELMONT HILLS ELEM ,22 at 5000 NESHAMINY BL ,22\nbox:84048\ntm:09:05:59 ED1203237=
 FRM:Bucks RSAN\nSUBJ:1/1\nMSG:FALRM\nadr:ANDYS DINER ,22 at 2224 LINCOLN HY ,22\naai:215-638-1444\nbox:04032\ntm:13:07:04 FD1201397\nRun: E4-1=
 FRM:Bucks RSAN]nSUBJ:1/1]nMSG:FAPT\nadr:1577 NESHAMINY VALLEY DR ,22\nbtwn:SHEWELL RD & COUNTRY LIGHT CHAL E\nbox:88015\ntm:06:33:52 FD1201865  Run: E8=8 L65 L8 E84 E7 E4=
 FRM:Bucks RSAN\nSUBJ:1/1\nMSG:STA1 STA65 STA2 STA4 STA3:FAPT\nadr:400 E STREET RD ,43 -- CROFTWOOD APT\nbtwn:CENTRAL AV & STERNER MILL RD\naai:BUILDI=NG 4\nbox:01050\ntm:10:01:14 FD1202073  Run: L65 L2 MSS11 E4 Q73 R4=
 
 Contact: "Shapp, Dave" <dave.shapp@midwayvfc.com>
 Sender: iamresponding.com
 RAUTO  Box:15023\nAdr: CHURCH RD/RT 413 ,26\nbtwn:RT 413 3/4 OF A MILE FROM CHURCH\n01/21/12  12:43:51\nFD1201070\nRun: R5 E15
 COALRM Box:05035 adr: 3425 AQUETONG RD ,65 btwn:MECHANICSVILLE RD & SAW MILL RD 01/28/12  15:44:26 FD1201409   Run: C5
 FALRM  Box:05031 adr: 5960 HONEY HOLLOW RD ,65 btwn:STREET RD & DEWEES RD RESID-MINNAUGH 01/29/12  20:16:02 FD1201463   Run: E5
 
 Contact: Ryan Pankoe <rpankoe@gmail.com>
 Sender: alert9693@alert.bucksema.org
 Subject:1/1\nSQ134 STA60:ATAI\nadr:1235 S TOWNSHIP LINE RD ,36\nbtwn:CHALFONT RD & LIMEKILN PK\nbox:61003\ntm:08:57:03 ED1205458
 Subject:1/1\nSQ134:AABDO\nadr:10 DUBLIN RD ,36\nbtwn:HILLTOWN PK & PINESIDE DR\nbox:23017 map:2921D4\ntm:13:27:42 ED1205485
 Subject:1/1\nSQ134 SQ124:AABDO\nadr:10 DUBLIN RD ,36\nbtwn:HILLTOWN PK & PINESIDE DR\nbox:23017 map:2921D4\ntm:13:27:42 ED1205485
 911: STA19  type:RAUTO   adr:S MAIN ST/S RT 611 ,29  aai:  box:79025  map:3034K3  tm:23:13:16  FD1201989    Run: R19 E79    \n
 Subject:1/1\nSQ134:ASEIZ\nadr:517 N MAIN ST ,27\nbtwn:W HILLCREST AV & PEACE VALLEY RD\nbox:34008 map:3033E2\ntm:20:10:13 ED1205526
 911: STA19  type:FALRM   adr:COURT HOUSE MTR IN #137 ,28 at 625 N MAIN ST #137 ,28 btwn FONTHILL DR & CHAPMAN  aai:  box:19014  map:2922J8  tm:19:44:2
 Subject:1/2\nSQ134 SQ125:ACHESP\nadr:1080 PATRICK PL ,27\nbtwn:LINDENFIELD PY & LINDENFIELD PY\nbox:34028 map:3033G3\ntm:12:27:07 E
 Subject:1/1\nSQ125 SQ134:ACVA\nadr:216 UNION ST ,28\nbtwn:HARVEY AV & N HAMILTON ST\naai:"ALLEN GREER" RESD 215 348 1001\nbox:1901ap:2922H10\ntm:06:34:10 ED1205674
 Subject:1/1\nSQ134:AFALL\nadr:10 DUBLIN RD ,36\nbtwn:HILLTOWN PK & PINESIDE DR\nbox:23017 map:2921D4\ntm:18:47:44 ED1205765
 Subject:1/1\nSQ134:AFAINT \nadr:102 ASPEN CT ,48 \nbtwn:E FAIRWOOD DR & CUL DE SAC \nbox:34030 map:3033H1 \ntm:05:28:39 ED1205933
 Subject:1/1\nSQ134:ATRAN\nadr:209 OVERLOOK DR ,48\nbtwn:STONY HILL CT & RIDGE CT\nbox:74052 map:3033D5\ntm:00:21:23 ED1205914
 Subject:1/1\nSQ134:AFAINT\nadr:102 ASPEN CT ,48\nbtwn:E FAIRWOOD DR & CUL DE SAC\nbox:34030 map:3033H1\ntm:05:28:39 ED1205933
 STA19:WIREIN adr:131 WESTBURY CT ,29 btwn:AVENUE A & CUL DE SAC box:19042 tm:10:45:39 FD1202161 Run: L79 E19 E19-1 E79 Sent by mss911 Bucks to STA19,
 SQ134:AUNR\nadr:800 MANOR DR #325 ,48 -- NEW SEASONS ASSIST\nbtwn:E SCHOOL HOUSE RD & ANDRE DR\nbox:74066 map:3033E8:12:14:32 ED1205978
 Subject:1/1\nSQ134 STA60:ACHESP\nadr:31 CHURCH RD ,36\nbtwn:BETHLEHEM PK & PARK RD\nbox:60011\ntm:14:03:32 ED1205994
 Subject:1/1\nSQ134:ASEIZ\nadr:MANOR CARE HEALTH SERVICES#228,MONT TWP\naai:640 BETHLEHEM PIKE\nbox: map:\ntm:19:53:41 ED1206032
 Subject:1/2\nSQ134:FIRCAL\nadr:SALVAGE DIRECT ,47 at 77 BRISTOL RD ,47\nbtwn:W BUTLER AV & UNAMI TL\nbox:34025 map:3033J4\ntm:22:0
 STA19:FSTORE adr:NATIONAL PENN INSU ,28 at 169 BROAD ST ,28 btwn:N MAIN ST & UNION ST box:19012 tm:13:22:16 FD1202212 Run: L79 E19 E19-1 E79 Sent by m
 STA19:FALRM adr:GOLDEN LIVING ,28 at 432 MAPLE AV ,28 btwn:COTTAGE ST & EAST ST box:19055 tm:13:27:51 FD1202214 Run: E15 Sent by mss911 Bucks to STA19
 STA19:FALRM adr:GRUNDY HALL ,29 at 1290 ALMSHOUSE RD ,29 btwn:TURK RD & RT 611 box:79057 tm:12:13:33 FD1202423 Run: E79 Sent by mss911 Bucks to STA19,
 STA19:FALRM adr:1290 ALMSHOUSE RD ,29 - GRUNDY HALL btwn:TURK RD & RT 611 aai:2153434117 box:79057 tm:18:47:59 FD1202516 Run: E79 Sent by mss911 Bucks
 
 Contact: Jon DiNola <jbdinola@gmail.com>
 Sender: alert_@alert.bucksema.org
 (#9823  1/1) STA0:FEMS\nadr:4008 WALTHAM CT ,42\nbtwn:COVINGTON RD & CUL DE SAC\nbox:80012\ntm:19:07:17 FD1202015  Run: UT80
 
 Contact: "Terry (IT Consulting)" <securetalk@gmail.com>
 Sender: alert_@alert.bucksema.org
 (#10067  1/2) STA57:FPOL\nadr:1940 GERYVILLE PK ,45\nbtwn:SLEEPY HOLLOW RD & TITLOW RD\nbox:75001\ntm:18:15:10 FD1202048  Run: FP75 FP
 
 Contact: Joseph Montanya <flounder53@gmail.com>
 Sender: alert10071@alert.bucksema.org
 STA53:FALRM\nadr:HARBOUR LIGHTS APT #A ,24 at 1032 RADCLIFFE ST #A ,24\nbtwn:JEFFERSON AV & LINCOLN AV\nbox:53061\ntm:20:54 FD1202049  Run: E53
 
 Contact: Kevan Williams <kwscotch36@gmail.com>
 Sender: alert10485@alert.bucksema.org
 Subject:1/1\nSQ168:ABLED\nadr:SHERWOOD RESIDENCE ,44 at 92 OAKWOOD DR ,44\nbtwn:BUTTONWOOD DR & BUTTONWOOD DR\nbox:21011 map:3262E tm:09:50:07 ED1205837
 Subject:1/2\nSTA45 STA0:FAPT \nadr:106 DISPATCH DR ,72 \nbtwn:LEXINGTON CT & DISPATCH DR \nbox:71038 \ntm:19:20:02 FD1202317  Run: TR7
 
 Contact: "jecashjr@hotmail.com" <jecashjr@hotmail.com>
 Sender: a@bnn.us
 STA26 STA23 STA87 STA17 STA18:FDWL adr:1032 OLD BETHLEHEM RD ,33 btwn:RIDGE RD & CREEK RD box:76030 tm:09:07:08 FD1202476 Run: TW26 TR23 E26 E17 Sent 
 
 Contact support@active911.com
 Sender: "Bucks RSAN" <alert10965@alert.bucksema.org>
 [Important message from Bucks County RSAN] STA19:WIRES\nadr:400 OLD DUBLIN PK ,29 -- PENN COLOR INC\nbtwn:TRAFALGAR RD & PINE RUN RD\naai:IFO WAREHOUSE\nbox:19005\ntm:16:47:17 FD1202274  Run: E19\nSent by mss911 Bucks to STA19, mss911 Bucks (Voice/Fax Dialer, E-mail accounts, Pagers, Cell phones) through Bucks County RSAN\n
 [Important message from Bucks County RSAN] STA19:FINV\nadr:42 PEBBLE WOODS DR N ,29\nbtwn:TURK RD & OAK DR\naai:IRO\nbox:79025\ntm:22:35:50 FD1202284  Run: E79\nSent by mss911 Bucks to STA19, mss911 Bucks (Voice/Fax Dialer, E-mail accounts, Pagers, Cell phones) through Bucks County RSAN\n
 [Important message from Bucks County RSAN] STA19:FALRM\nadr:641 N MAIN ST ,28 -- CYCLE SPORTS\nbtwn:MERCER RD & SWAMP RD\nbox:19014\ntm:12:03:27 FD1202341  Run: E19\nSent by mss911 Bucks to STA19, mss911 Bucks (Voice/Fax Dialer, E-mail accounts, Pagers, Cell phones) through Bucks County RSAN\n
 [Important message from Bucks County RSAN] STA19:FCHIMN\nadr:3 WASHINGTON SQ ,29\nbtwn:OLDE COLONIAL DR & DEAD END\nbox:19007\ntm:19:18:33 FD1202357  Run: L79 E19 E19-1 E79\nSent by mss911 Bucks to STA19, mss911 Bucks (Voice/Fax Dialer, E-mail accounts, Pagers, Cell phones) through Bucks County RSAN\n
 [Important message from Bucks County RSAN] STA19:FBRU\nadr:217 UNION ST ,28\nbtwn:N HAMILTON ST & N CLINTON ST\nbox:19012\ntm:14:24:40 FD1202382  Run: E19 F79\nSent by mss911 Bucks to STA19, mss911 Bucks (Voice/Fax Dialer, E-mail accounts, Pagers, Cell phones) through Bucks County RSAN\n
 [Important message from Bucks County RSAN] STA29 STA19 STA34:FDWL\nadr:57 OLD NEW RD ,29\nbtwn:DAVIDS WY & WESTAWAY LA\nbox:78054\ntm:01:57:28 FD1202404  Run: E78 E29 L29 E79 TR34\nSent by mss911 Bucks to STA19, STA29, STA34, mss911 Bucks (Voice/Fax Dialer, E-mail accounts, Pagers, Cell phones) through Bucks County RSAN\n
 [Important message from Bucks County RSAN] STA19:FALRM\nadr:GRUNDY HALL ,29 at 1290 ALMSHOUSE RD ,29\nbtwn:TURK RD & RT 611\nbox:79057\ntm:12:13:33 FD1202423  Run: E79\nSent by mss911 Bucks to STA19, mss911 Bucks (Voice/Fax Dialer, E-mail accounts, Pagers, Cell phones) through Bucks County RSAN\n
 [Important message from Bucks County RSAN] STA19:FALRM\nadr:MOOSE LODGE ,28 at 127 E STATE ST ,28\nbtwn:BROAD ST & CHURCH ST\naai:2158507282\nbox:19023\ntm:14:01:57 FD1202430  Run: E19\nSent by mss911 Bucks to STA19, mss911 Bucks (Voice/Fax Dialer, E-mail accounts, Pagers, Cell phones) through Bucks County RSAN\n
 (Important message from Bucks County RSAN) SQ134 SQ125:FIRCAL\nadr:SALVAGE DIRECT ,47 at 77 BRISTOL RD ,47\nbtwn:W BUTLER AV & UNAMI TL\nbox:34025 map:3033J4\ntm:22:06:32 ED1206040\n\nSent by mss911 Bucks to SQ134, SQ125, mss911 Bucks (Voice/Fax Dialer, E-mail accounts, Pagers, Cell phones) through Bucks County RSAN
 (Important message from Bucks County RSAN) SQ134:ACHESP\nadr:31 CHURCH RD ,36\nbtwn:BETHLEHEM PK & PARK RD\nbox:60011 map:3032E1\ntm:14:03:32 ED1205994\n\nSent by mss911 Bucks to SQ134, mss911 Bucks (Voice/Fax Dialer, E-mail accounts, Pagers, Cell phones) through Bucks County RSAN
 (Important message from Bucks County RSAN) SQ134:ACHESP\nadr:1600 HORIZON DR #117 ,48 -- GWYNEDD FAMILY MED\nbtwn:HORIZON CI & COUNTY LINE RD\naai:STE 117\nbox:74058 map:3033F8\ntm:16:11:55 ED1206008\n\nSent by mss911 Bucks to SQ134, mss911 Bucks (Voice/Fax Dialer, E-mail accounts, Pagers, Cell phones) through Bucks County RSAN
 (Important message from Bucks County RSAN) SQ134:ACHESP\nadr:1600 HORIZON DR #117 ,48 -- GWYNEDD FAMILY MED\nbtwn:HORIZON CI & COUNTY LI\naai:STE 117\nbox:74058 map:3033F8\ntm:16:11:55 ED1206008\n\nSent by mss911 Bucks to SQ134, mss911 Bucks (Voice/Fax Dialer, E-mail accounts, Pagers, Cell phones) through Bucks County RSAN
 (Important message from Bucks County RSAN) SQ134:ACHESP\nadr:1600 HORIZON DR #117 ,48 -- GWYNEDD FAMILY MED\nbtwn:HORIZON CI & COUNTY LI\naai:STE 117\nbox:74058 map:3033F8\ntm:16:11:55 ED1206008\n\nSent by mss911 Bucks to SQ134, mss911 Bucks (Voice/Fax Dialer, E-mail accounts, Pagers, Cell phones) through Bucks County RSAN
 (Important message from Bucks County RSAN) SQ134:ASEIZ\nadr:MANOR CARE HEALTH SERVICES#228,MONT TWP\naai:640 BETHLEHEM PIKE\nbox: map:\ntm:19:53:41 ED1206032\n\nSent by mss911 Bucks to SQ134, mss911 Bucks (Voice/Fax Dialer, E-mail accounts, Pagers, Cell phones) through Bucks County RSAN
 (Important message from Bucks County RSAN) SQ134:FIRCAL\nadr:SALVAGE DIRECT ,47 at 77 BRISTOL RD ,47\nbtwn:W BUTLER AV & UNAMI TL\nbox:34025 map:3033J4\ntm:22:06:32 ED1206040\n\nSent by mss911 Bucks to SQ134, mss911 Bucks (Voice/Fax Dialer, E-mail accounts, Pagers, Cell phones) through Bucks County RSAN
 [Important message from Bucks County RSAN] STA19:FALRM\nadr:1290 ALMSHOUSE RD ,29 -- GRUNDY HALL\nbtwn:TURK RD & RT 611\naai:2153434117\nbox:79057\ntm:18:47:59 FD1202516  Run: E79\nSent by mss911 Bucks to STA19, mss911 Bucks (Voice/Fax Dialer, E-mail accounts, Pagers, Cell phones) through Bucks County RSAN\n
 
  */
 
 
 public class PABucksCountyParser extends FieldProgramParser {
   
   private static final Pattern MARKER1 = Pattern.compile("^[A-Z]+\\s+(?:Adr:|adr:|Box:)");
   private static final Pattern MARKER2 = Pattern.compile("^([A-Z0-9 ]+):([A-Z]+) *");
   private static final Pattern NAKED_DATE_TIME = Pattern.compile("(?<!: ?)\\d\\d/\\d\\d/\\d\\d +\\d\\d:\\d\\d:\\d\\d\\b");
   
   public PABucksCountyParser() {
     super("BUCKS COUNTY", "PA",
            "SRC type:CALL! Box:BOX? adr:ADDR! btwn:X aai:INFO box:BOX map:MAP tm:TIME% Run:UNIT");
   }
   
   @Override
   public String getFilter() {
     return "8276,@bnn.us,iamresponding.com,Bucks RSAN,@alert.bucksema.org";
   }
 
   @Override
   protected boolean parseMsg(String body, Data data) {
     
     if (body.startsWith("911:")) body = body.substring(4).trim();
     
     if (body.endsWith("=")) body = body.substring(0,body.length()-1).trim();
     int pt = body.lastIndexOf('=');
     if (pt >= 100) body = body.substring(0,pt) + body.substring(pt+1);
     pt = body.indexOf("Sent by mss911 ");
     if (pt >= 0) body = body.substring(0,pt).trim();
     boolean mark2 = false;
     Matcher match = MARKER1.matcher(body);
     if (match.find()) {
       body = "type:" + body.replace('\n', ' ').trim();
     } else {
       match = MARKER2.matcher(body);
       if (match.find()) {
         mark2 = true;
         body = match.group(1) + " type:" + match.group(2) + " " + body.substring(match.end()).replace('\n', ' ').trim();
       }
     }
     
     body = NAKED_DATE_TIME.matcher(body).replaceFirst(" tm: $0");
     body = body.replace(" Adr:", " adr:").replaceAll(" s?btwn ", " btwn:").replace(" stype:", " type:").replace(" saai:", " aai:").trim();
     if (! super.parseMsg(body, data)) return false;
     if (mark2 && data.strUnit.length() == 0) {
       data.strUnit = data.strSource;
       data.strSource = "";
     }
     return true;
   }
   
   private class MyCallField extends CallField {
     
     @Override
     public void parse(String field, Data data) {
       String desc = TYPE_CODES.getProperty(field);
       if (desc != null) field = field + " - " + desc;
       super.parse(field, data);
     }
   }
   
   private static final Pattern LA_PTN = Pattern.compile("\\bLA\\b", Pattern.CASE_INSENSITIVE);
   private static final Pattern CITY_CODE_PTN = Pattern.compile("(\\d+)[- ]*(.*)");
   private class MyAddressField extends AddressField {
     
     @Override
     public void parse(String sAddr, Data data) {
       Parser p = new Parser(sAddr);
       data.strPlace = p.getOptional(" at ");
       int pt = data.strPlace.indexOf(',');
       if (pt >= 0) data.strPlace = data.strPlace.substring(0,pt).trim();
       String cityCode = p.getLastOptional(',');
       sAddr = p.get();
       sAddr = LA_PTN.matcher(sAddr).replaceAll("LN");
       sAddr = sAddr.replace("FRIER RD", "FREIER RD");
       sAddr = sAddr.replace("WHITE BRIAR", "WHITEBRIAR");
       sAddr = expandStreet("COLD SPRING CREAMERY", sAddr);
       sAddr = expandStreet("FALLSINGTON TULLYTOWN", sAddr);
       super.parse(sAddr, data);
       if (cityCode.length() > 0) {
         Matcher match = CITY_CODE_PTN.matcher(cityCode);
         if (match.matches()) {
           int iCity = Integer.parseInt(match.group(1));
           iCity = iCity - INIT_TOWN_CODE;
           if (iCity >= 0 && iCity < TOWN_CODES.length){
             data.strCity = TOWN_CODES[iCity];
           }
           data.strPlace = append(data.strPlace, " - ", match.group(2));
         } else {
           data.strCity = cityCode;
         }
       }
     }
     
     private final String expandStreet(String fullName, String field) {
       int trigLen  = fullName.lastIndexOf(' ');
       if (trigLen < 0) return field;
       trigLen += 2;
       if (trigLen > fullName.length()) return field;
       String trigger = fullName.substring(0,trigLen);
       int pt1 = 0;
       while (true) {
         int pt2 = field.indexOf(trigger, pt1);
         if (pt2 < 0) break;
         int pt3 = field.indexOf(' ', pt2+trigLen);
         if (pt3 < 0) pt3 = field.length();
         if (fullName.startsWith(field.substring(pt2,pt3))) {
           field = field.substring(0,pt2) + fullName + field.substring(pt3);
         }
         pt1 = pt3;
       }
       return field;
     }
     
     @Override
     public String getFieldNames() {
       return "PLACE " + super.getFieldNames() + " CITY";
     }
   }
   
   private class MyCrossField extends CrossField {
     @Override
     public void parse(String field, Data data) {
       field = LA_PTN.matcher(field).replaceAll("LN");
       super.parse(field, data);
     }
   }
     
   private class MyTimeField extends TimeField {
     
     @Override
     public void parse(String field, Data data) {
       Parser p = new Parser(field);
       String token = p.get(' ');
       if (token.contains("/")) {
         data.strDate = token;
         token = p.get(' ');
       }
       data.strTime = token;
       data.strCallId = p.get();
     }
     
     @Override
     public String getFieldNames() {
       return "DATE TIME ID";
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("CALL")) return new MyCallField();
     if (name.equals("ADDR")) return new MyAddressField();
     if (name.equals("X")) return new MyCrossField();
     if (name.equals("TIME")) return new MyTimeField();
     return super.getField(name);
   }
   
   private static final int INIT_TOWN_CODE = 21;
   private static final String[] TOWN_CODES = new String[]{
   /*21*/ "BEDMINSTER TWP",
   /*22*/ "BENSALEM TWP",
   /*23*/ "BRIDGETON TWP",
   /*24*/ "BRISTOL",
   /*25*/ "BRISTOL TWP",
   /*26*/ "BUCKINGHAM TWP",
   /*27*/ "CHALFONT",
   /*28*/ "DOYLESTOWN",
   /*29*/ "DOYLESTOWN TWP",
   /*30*/ "",
   /*31*/ "DUBLIN",
   /*32*/ "DURHAM TWP",
   /*33*/ "EAST ROCKHILL",
   /*34*/ "FALLS TWP",
   /*35*/ "HAYCOCK TWP",
   /*36*/ "HILLTOWN TWP",
   /*37*/ "HULMEVILLE",
   /*38*/ "IVYLAND",
   /*39*/ "LANGHORNE",
   /*40*/ "",
   /*41*/ "LANGHORNE MANOR",
   /*42*/ "LOWER MAKEFIELD TWP",
   /*43*/ "LOWER SOUTHAMPTON TWP",
   /*44*/ "MIDDLETOWN TWP",
   /*45*/ "MILFORD TWP",
   /*46*/ "MORRISVILLE",
   /*47*/ "NEW BRITAIN",
   /*48*/ "NEW BRITAIN TWP",
   /*49*/ "NEW HOPE",
   /*50*/ "",
   /*51*/ "NEWTOWN",
   /*52*/ "NEWTOWN TWP",
   /*53*/ "NOCKAMIXON TWP",
   /*54*/ "NORTHAMPTON TWP",
   /*55*/ "PENNDEL",
   /*56*/ "PERKASIE",
   /*57*/ "PLUMSTEAD TWP",
   /*58*/ "QUAKERTOWN TWP",
   /*59*/ "RICHLAND TWP",
   /*60*/ "",
   /*61*/ "RICHLANDTOWN",
   /*62*/ "RIEGELSVILLE",
   /*63*/ "SELLERSVILLE",
   /*64*/ "SILVERDALE",
   /*65*/ "SOLEBURY TWP",
   /*66*/ "SPRINGFIELD TWP",
   /*67*/ "TELFORD",
   /*68*/ "TINICUM TWP",
   /*69*/ "TRUMBAUERSVILLE",
   /*70*/ "",
   /*71*/ "TULLYTOWN",
   /*72*/ "UPPER MAKEFIELD TWP",
   /*73*/ "UPPER SOUTHAMPTON TWP",
   /*74*/ "WARMINSTER TWP",
   /*75*/ "WARRINGTON TWP",
   /*76*/ "WARWICK TWP",
   /*77*/ "PENNRIDGE REGIONAL PD",
   /*78*/ "WRIGHTSTOWN TWP",
   /*79*/ "YARDLEY"
   };
   
   private static final Properties TYPE_CODES = buildCodeTable(new String[]{
       "AIRC",     "AIRPLANE CRASH (TAC)",
       "ACARDA",   "Cardiac/Respiratory Arrest",
       "APOSXP",   "POSSIBLE EXPIRATION",
       "BOMB",     "BOMB THREAT",
       "BURN",     "BURN VICTIM (LOC)",
       "CBURN",    "CONTROLLED BURN",
      "COALRM",   "CARBON MONOXIDE ALARM (LOC )",
       "DECON",    "DECON ASSIGNMENT (LOC)",
       "ELEVAT",   "STUCK ELEVATOR (LOC)",
       "EXBLD",    "EXTINGUISHED STRUCTURE (TAC)",
       "FALRM",    "FIRE ALARM (LOC)",
       "FAPL",     "APPLIANCE FIRE (TAC)",
       "FAPT",     "APARTMENT FIRE (BOX)",
       "FAST",     "FIRE ASSIST (OUT OF COUNTY)",
       "FAUTO",    "AUTOMOBILE (LOC)",
       "FBARN",    "BARN FIRE (TAC)",
       "FBLD",     "BUILDING FIRE (BOX)",
       "FBOAT",    "BOAT FIRE (LOC)",
       "FBRU",     "BRUSH FIRE (LOC)",
       "FBUS",     "BUS FIRE (LOC)",
       "FCHIMN",   "CHIMNEY FIRE (TAC)",
       "FCOV",     "FIRE STATION COVER",
       "FDRILL",   "FIRE DRILL",
       "FDUMP",    "DUMPSTER FIRE (LOC)",
       "FDWL",     "DWELLING FIRE (TAC)",
       "FEXP",     "EXPLOSION (LOC)",
       "FEMS",     "FIRE ASSIST EMS (LOC)",
       "FGARAG",   "GARAGE FIRE (TAC)",
       "FGRILL",   "GRILL FIRE (LOC)",
       "FHAZ",     "HAZMAT FIXED LOCATION (TAC)",
       "FHOSP",    "HOSPITAL FIRE",
       "FINFO",    "FIRE INFORMATION",
       "FINV",     "FIRE INVESTIGATION (LOC)",
       "FIRE",     "FIRE OTHERS (LOC)",
       "FIRCAL",   "FIRE CALL (EMS)",
       "FMPAGE",   "FM PAGE REQUEST",
       "FNURS",    "NURSING HOME FIRE (BOX)",
       "FOBLD",    "OUTBUILDING FIRE (TAC)",
       "FPAGE",    "FD PAGE REQUEST",
       "FPOL",     "FIRE POLICE REQUEST",
       "FRUB",     "RUBBISH FIRE (LOC)",
       "FSCHOL",   "SCHOOL FIRE (BOX)",
       "FSPEC",    "SPECIAL ASSIGNMENT",
       "FSPILL",   "FUEL LEAK INCIDENT (LOC)",
       "FSTORE",   "SMALL STORE FIRE (TAC)",
       "FTA",      "ACCIDENT W/FIRE (LOC)",
       "FTAI",     "ACCIDENT W/FIRE & INJ (LOC)",
       "FTHAZ",    "TRANSPORTATION HAZMAT",
       "FTRAIN",   "TRAIN FIRE (TAC)",
       "FTRUCK",   "TRUCK FIRE (LOC)",
       "FUMEIN",   "FUMES INSIDE STRUCTURE (TAC)",
       "FUMES",    "FUMES OUTSIDE (LOC)",
       "FUNK",     "UNKNOWN TYPE FIRE",
       "GALRM",    "GENERAL ALARM",
       "RAUTO",    "AUTO EXTRICATION",
       "RDOM",     "DOMESTIC RESCUE",
       "RMAR",     "MARINE RESCUE",
       "RVBLD",    "VEHICLE INTO A BUILDING",
       "STDBY",    "FIRE STANDBY",
       "WATER",    "WATER FLOW",
       "WIREIN",   "WIRES IN DWELLING",
       "WIRES",    "WIRES DOWN (LOC)",
 
       "AABDO",      "ACUTE ABDOMEN",
       "AALLR",      "ANAPHYLAXIS",
       "AASSL",      "ASSAULT VICTIM (ALS)",
       "ABLED",      "ACUTE HEMORRHAGE",
       "ABURN",      "CRITICAL BURN VICTIM",
       "ACHESP",     "CHEST PAINS (CARDIAC SYMPT.)",
       "ACHOKE",     "OBSTRUCTED AIRWAY (ACTIVE)",
       "ACOP",       "CARBON MONOXIDE POISONING",
       "ACVA",       "CVA/STROKE",
       "ADIAB",      "DIABETIC EMERGENCY",
       "ADROWN",     "DROWNING - CRITICAL",
       "AELEC",      "ELECTRICAL SHOCK VICTIM",
       "AENVIR",     "ENVIRONMENTAL EMERGENCY",
       "AFAINT",     "SYNCOPAL EPISODE",
       "AFALL",      "FALL VICTIM - CRITICAL",
       "AGUN",       "GUNSHOT VICTIM",
       "AHRI",       "ALS HIT & RUN ACCIDENT WITH INJURY",
       "AOBG",       "ALS OB/GYN EMERGENCY",
       "AOD",        "OVERDOSE/POISONING - CRITICAL",
       "APREG",      "MATERNITY - CRITICAL",
       "ARESP",      "RESPIRATORY DISTRESS",
       "ASEIZ",      "SEIZURES - ACTIVE",
       "AASSLT",     "ASSAULT W/ TRAUMA",
       "ASTAB",      "STABBING VICTIM",
       "ATAI",       "ALS TRAFFIC ACCIDENT INJURIES",
       "ATRAN",      "ALS/UNSPECIFIED EMERGENCY",
       "ATRAUM",     "MULTI-SYSTEMS TRAUMA",
       "AUNC",       "UNCONSCIOUS SUBJECT",
       "AUNR",       "UNRESPONSIVE SUBJECT",
       "BABDO",      "ABDOMINAL PAIN",
       "BALLR",      "ALLERGIC REACTION",
       "BASSLT",      "ASSAULT - NON CRITICAL",
       "BBLED",      "BLEEDING (NON TRAUMATIC)",
       "BCHESP",     "CHEST PAIN  (NON CRITICAL)",
       "BINJ",       "MINOR INJURY - NON CRITICAL",
       "BLAC",       "ANIMAL BITE",
       "BPSYCH",     "PSYCHIATRIC TRANSPORTATION",
       "BOD",        "OVERDOSE/POISONING - NON CRITICAL",
       "BPREG",      "MATERNITY - NON CRITICAL",
       "BUNK",       "BLS/UNKNOWN PROBLEM",
       "BBURN",      "SCALDING/MINOR BURNS",
       "BDROWN",     "AQUATIC ACCIDENT (NON CRITICAL)",
       "BFALL",      "FALL VICTIM - NON CRITICAL",
       "BLAC",       "LACERATION/CONTROLLED BLEEDING",
       "BPTAST",     "PATIENT ASSIST",
       "COP",        "CARBON MONOXIDE POISONING",
       "BTAI",       "BLS TRAFFIC ACCIDENT (MINOR INJURIES)",
       "BTRAN",      "EMERGENCY TRANSPORTATION",
       "ESPEC",      "STANDBY / SPECIAL ASSIGNMENT (EMS)",
       "MALRM",      "MEDICAL ALARM"
   });
 }
