 package net.anei.cadpage.parsers.MD;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 import net.anei.cadpage.parsers.MD.MDSaintMarysCountyParser;
 
 import org.junit.Test;
 
 
 public class MDSaintMarysCountyParserTest extends BaseParserTest {
   
   public MDSaintMarysCountyParserTest() {
     setParser(new MDSaintMarysCountyParser(), "SAINT MARYS COUNTY", "MD");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "21:10:05*Personal Injury Accident*22607 THREE NOTCH RD INTERSECTN*MACARTHUR BL*LEXINGTON PARK*CO9 CO3 SQ3 CO39 CO79 A796*N/B LANES TWO VEHICLES AT LEAST ONE INJURY*",
         "TIME:21:10:05",
         "CALL:Personal Injury Accident",
         "ADDR:22607 THREE NOTCH RD",
         "X:MACARTHUR BL",
         "CITY:LEXINGTON PARK",
         "UNIT:CO9 CO3 SQ3 CO39 CO79 A796",
         "INFO:N/B LANES TWO VEHICLES AT LEAST ONE INJURY");
 
     doTest("T2",
         "14:41:22*Personal Injury Accident*21050 WILLOWS RD INTERSECTN*ABBERLY CREST LN*LEXINGTON PARK*CO3 CO39 CO6R*",
         "TIME:14:41:22",
         "CALL:Personal Injury Accident",
         "ADDR:21050 WILLOWS RD",
         "X:ABBERLY CREST LN",
         "CITY:LEXINGTON PARK",
         "UNIT:CO3 CO39 CO6R");
 
     doTest("T3",
         "15:24:31*Working House Fire*20242 POINT LOOKOUT RD*OLD GREAT MILLS RD*INDIAN BRIDGE RD*GREAT MILLS*CO3 CO9 CO6 CO1 CO5 CO39 CO6R*",
         "TIME:15:24:31",
         "CALL:Working House Fire",
         "ADDR:20242 POINT LOOKOUT RD",
         "X:OLD GREAT MILLS RD / INDIAN BRIDGE RD",
         "CITY:GREAT MILLS",
         "UNIT:CO3 CO9 CO6 CO1 CO5 CO39 CO6R");
 
     doTest("T4",
         "21:11:30*Commercial Building Fire*46528 VALLEY CT APT3019*SPRING VALLEY DR*DEAD END*LEXINGTON PARK*CO3 CO13 CO9 TK3 CO7 TK7 CO39*Using ProQA Fire*",
         "TIME:21:11:30",
         "CALL:Commercial Building Fire",
         "ADDR:46528 VALLEY CT",
         "APT:APT3019",
         "X:SPRING VALLEY DR / DEAD END",
         "CITY:LEXINGTON PARK",
         "UNIT:CO3 CO13 CO9 TK3 CO7 TK7 CO39",
         "INFO:Using ProQA Fire");
 
     doTest("T5",
         "23:14:28*Heart Problems*22521 IVERSON DR UNIT3*AMBER DR*CUL DE SAC*CALIFORNIA*CO9*55YOF C/A/B; RAPID HEART RATE AND WEAK; HX DIABETES;*",
         "TIME:23:14:28",
         "CALL:Heart Problems",
         "ADDR:22521 IVERSON DR",
         "APT:UNIT3",
         "X:AMBER DR / CUL DE SAC",
         "CITY:CALIFORNIA",
         "UNIT:CO9",
         "INFO:55YOF C/A/B; RAPID HEART RATE AND WEAK; HX DIABETES;");
 
     doTest("T6",
         "((37593) CAD ) 22:12:45*CO Detector With Symptons*21353 FOXGLOVE CT*DEAD END*BAYWOODS RD*HERMANVILLE*CO3 CO39*Using ProQA Fire*",
         "TIME:22:12:45",
         "CALL:CO Detector With Symptons",
         "ADDR:21353 FOXGLOVE CT",
         "X:DEAD END / BAYWOODS RD",
         "PLACE:HERMANVILLE",
         "CITY:LEXINGTON PARK",
         "UNIT:CO3 CO39",
         "INFO:Using ProQA Fire");
 
     doTest("T7",
         "((44333) CAD ) 00:35:39*CHIMNEY FIRE*25120 DOVE POINT LN*PARSONS MILL RD*DEAD END*LOVEVILLE*CO1 TK1 CO7*Using ProQA Fire*",
         "TIME:00:35:39",
         "CALL:CHIMNEY FIRE",
         "ADDR:25120 DOVE POINT LN",
         "X:PARSONS MILL RD / DEAD END",
         "CITY:LOVEVILLE",
         "UNIT:CO1 TK1 CO7",
         "INFO:Using ProQA Fire");
 
     doTest("T8",
         "((60350) CAD ) 18:34:06*Breathing Difficulties*22030 OXFORD CT APT*GLOUCESTER CT*DEAD END*LEXINGTON PARK*ALS CO39 CO6R*66YOF HIGH BLOOD",
         "TIME:18:34:06",
         "CALL:Breathing Difficulties",
         "ADDR:22030 OXFORD CT",
         "X:GLOUCESTER CT / DEAD END",
         "CITY:LEXINGTON PARK",
         "UNIT:ALS CO39 CO6R",
         "INFO:66YOF HIGH BLOOD");
 
     doTest("T9",
         "((46589) CAD) 14:02:26*Stroke**APT A2**22027 OXFORD CT APTA2*GLOUCESTER CT*DEAD END*LEXINGTON PARK*CO39*Using ProQA Medical*",
         "TIME:14:02:26",
         "CALL:Stroke",
         "ADDR:22027 OXFORD CT",
         "APT:APTA2",
         "X:GLOUCESTER CT / DEAD END",
         "PLACE:APT A2",
         "CITY:LEXINGTON PARK",
         "UNIT:CO39",
         "INFO:Using ProQA Medical");
 
     doTest("T10",
         "((46677) CAD) 13:59:56*Chest Pain*40452 MEDLEYS LN*LAUREL GROVE RD*LOVEVILLE RD*OAKVILLE*CO79 ALS*subject has pacemaker*",
         "TIME:13:59:56",
         "CALL:Chest Pain",
         "ADDR:40452 MEDLEYS LN",
         "X:LAUREL GROVE RD / LOVEVILLE RD",
         "CITY:OAKVILLE",
         "UNIT:CO79 ALS",
         "INFO:subject has pacemaker");
 
     doTest("T11",
         "((46589) CAD) 13:29:15*Breathing Difficulties**ST MARYS NURSING CENTER**21585 PEABODY ST RM441A*HOLLYWOOD RD*DEAD END*LEONARDTOWN*CO19 CO79 ALS*hx copd*",
         "TIME:13:29:15",
         "CALL:Breathing Difficulties",
         "ADDR:21585 PEABODY ST",
         "APT:RM441A",
         "X:HOLLYWOOD RD / DEAD END",
         "PLACE:ST MARYS NURSING CENTER",
         "CITY:LEONARDTOWN",
         "UNIT:CO19 CO79 ALS",
         "INFO:hx copd");
 
     doTest("T12",
         "((46589) CAD) 12:47:33*Chest Pain*17498 GRAYSON RD*BEACHVILLE RD*VILLA RD*ST INIGOES*CO49R ALS*Using ProQA Medical*",
         "TIME:12:47:33",
         "CALL:Chest Pain",
         "ADDR:17498 GRAYSON RD",
         "X:BEACHVILLE RD / VILLA RD",
         "CITY:ST INIGOES",
         "UNIT:CO49R ALS",
         "INFO:Using ProQA Medical");
 
     doTest("T13",
         "((46589) CAD) 12:14:40*Sick Person**NEWTOWNE VILLAGE APTS**22810 DORSEY ST APT309*CONNELY CT*DEAD END*LEONARDTOWN*CO19 CO79 A799*Using ProQA Medical*",
         "TIME:12:14:40",
         "CALL:Sick Person",
         "ADDR:22810 DORSEY ST",
         "APT:APT309",
         "X:CONNELY CT / DEAD END",
         "PLACE:NEWTOWNE VILLAGE APTS",
         "CITY:LEONARDTOWN",
         "UNIT:CO19 CO79 A799",
         "INFO:Using ProQA Medical");
 
     doTest("T14",
         "((46589) CAD) 12:08:40*Sick Person*46104 SALTMARSH CT*WEST WESTBURY BL*DEAD END*LEXINGTON PARK*CO39 A397*Using ProQA Medical*",
         "TIME:12:08:40",
         "CALL:Sick Person",
         "ADDR:46104 SALTMARSH CT",
         "X:WEST WESTBURY BL / DEAD END",
         "CITY:LEXINGTON PARK",
         "UNIT:CO39 A397",
         "INFO:Using ProQA Medical");
 
     doTest("T15",
         "((47017) CAD ) 21:24:45*Seizures/Convulsions*THREE OAK CENTER*46905 LEI DR*THREE NOTCH RD*SOUTH CORAL DR*LEXINGTON PARK*ALS CO39*Using ProQA",
         "TIME:21:24:45",
         "CALL:Seizures/Convulsions",
         "ADDR:46905 LEI DR",
         "X:THREE NOTCH RD / SOUTH CORAL DR",
         "PLACE:THREE OAK CENTER",
         "CITY:LEXINGTON PARK",
         "UNIT:ALS CO39",
         "INFO:Using ProQA");
 
     doTest("T16",
         "( CAD ) 18:49:29*Sick Person*BRETON MEDICAL GROUP BLDG #3*22576 MACARTHUR BL SUITE 354*HALSEY CT*THREE NOTCH RD*SAN SOUCI*CO39 A387*Using ProQA Me(14072) CAD ) 08:27:10*Sick Person*BANK OF AMERICA*21800 NORTH SHANGRI LA DR*THREE NOTCH RD*GREAT MILLS RD*LEXINGTON PARK*CO39 A397*Using ProQA",
         "TIME:18:49:29",
         "CALL:Sick Person",
         "ADDR:22576 MACARTHUR BL",
         "APT:SUITE 354",
         "X:HALSEY CT / THREE NOTCH RD",
         "PLACE:BRETON MEDICAL GROUP BLDG #3",
         "CITY:CALIFORNIA",
         "UNIT:CO39 A387",
         "INFO:Using ProQA Me(14072) CAD ) 08:27:10 / Sick Person / BANK OF AMERICA / 21800 NORTH SHANGRI LA DR / THREE NOTCH RD / GREAT MILLS RD / LEXINGTON PARK / CO39 A397 / Using ProQA");
 
     doTest("T17",
         "((49169) CAD ) 15:07:57*Sick Person*46656 FLOWER OF THE FOREST RD*WILLOWS RD*DEAD END*LEXINGTON PARK*CO39*Using ProQA Medical* Eff Body:15:07:57*Sick Person*46656 FLOWER OF THE FOREST RD*WILLOWS RD*DEAD END*LEXINGTON PARK*CO39*Using ProQA Medical*",
         "TIME:15:07:57",
         "CALL:Sick Person",
         "ADDR:WILLOWS RD",
         "MADDR:WILLOWS RD & DEAD END",
         "X:DEAD END",
         "PLACE:46656 FLOWER OF THE FOREST RD",
         "CITY:LEXINGTON PARK",
         "UNIT:CO39",
         "INFO:Using ProQA Medical / Eff Body:15:07:57 / Sick Person / 46656 FLOWER OF THE FOREST RD / WILLOWS RD / DEAD END / LEXINGTON PARK / CO39 / Using ProQA Medical");
 
     doTest("T18",
         "((49639) CAD ) 17:18:40*Mutual Aid*14386 SOUTH SOLOMONS ISLAND RD*CO9 CO7 CO3*",
         "TIME:17:18:40",
         "CALL:Mutual Aid",
         "ADDR:14386 SOUTH SOLOMONS ISLAND RD",
         "UNIT:CO9 CO7 CO3");
 
     doTest("T19",
         "((23645) CAD ) 07:33:05*Mutual Aid EMS*401 EP WORTH CT*CO9*302; unresponsive*",
         "TIME:07:33:05",
         "CALL:Mutual Aid EMS",
         "ADDR:401 EP WORTH CT",
         "UNIT:CO9",
         "INFO:302; unresponsive");
 
     doTest("T20",
         "((19239) CAD ) 20:51:10*Chest Pain*19673 NORTH SNOW HILL MANOR RD*SOUTH SNOW HILL MANOR RD*LYARD RD*ST MARYS CITY*CO39 A398 ALS*PT. DOES HAVE A PACEMAKER",
         "TIME:20:51:10",
         "CALL:Chest Pain",
         "ADDR:19673 NORTH SNOW HILL MANOR RD",
         "X:SOUTH SNOW HILL MANOR RD / LYARD RD",
         "CITY:ST MARYS CITY",
         "UNIT:CO39 A398 ALS",
         "INFO:PT. DOES HAVE A PACEMAKER");
 
     doTest("T21",
         "((20390) CAD ) 09:06:30*Sick Person*CAMPUS CENTER*47600 MILL FIELD DR*POINT LOOKOUT RD*DEAD END*ST MARYS CITY*CO39*2ND FLOOR;*",
         "TIME:09:06:30",
         "CALL:Sick Person",
         "ADDR:47600 MILL FIELD DR",
         "X:POINT LOOKOUT RD / DEAD END",
         "PLACE:CAMPUS CENTER",
         "CITY:ST MARYS CITY",
         "UNIT:CO39",
         "INFO:2ND FLOOR;");
 
     doTest("T22",
         "((64277) CAD ) 09:48:02*Commercial Fire Alarm*ESPERANZA MIDDLE SCHOOL*22790 MAPLE RD*THREE NOTCH RD*TOWN CREEK*CO9 CO3 TWR9*gen alarm*",
         "TIME:09:48:02",
         "CALL:Commercial Fire Alarm",
         "ADDR:22790 MAPLE RD",
         "X:THREE NOTCH RD",
         "PLACE:ESPERANZA MIDDLE SCHOOL",
         "CITY:CALIFORNIA",
         "UNIT:CO9 CO3 TWR9",
         "INFO:gen alarm");
 
     doTest("T23",
         "((57079) CAD ) 16:18:26*Breathing Difficulties*CHESAPEAKE SHORES*21412 GREAT MILLS RD*SUBURBAN DR*SANNERS LN*LEXINGTON PARK*CO39 A397 ALS M3*RM102B-FRONT",
         "TIME:16:18:26",
         "CALL:Breathing Difficulties",
         "ADDR:21412 GREAT MILLS RD",
         "X:SUBURBAN DR / SANNERS LN",
         "PLACE:CHESAPEAKE SHORES",
         "CITY:LEXINGTON PARK",
         "UNIT:CO39 A397 ALS M3",
         "INFO:RM102B-FRONT");
 
     doTest("T24",
         "((61165) CAD ) 07:09:07*Sick Person*46369 HATTONS REST LN*DEAD END*POINT LOOKOUT RD*PARK HALL*CO39*Using ProQA Medical*",
         "TIME:07:09:07",
         "CALL:Sick Person",
         "ADDR:46369 HATTONS REST LN",
         "X:DEAD END / POINT LOOKOUT RD",
         "CITY:PARK HALL",
         "UNIT:CO39",
         "INFO:Using ProQA Medical");
 
     doTest("T25",
         "((60056) CAD ) 18:14:19*Traumatic Injuries*21121 ACE LN*WINDING WY*SHORT WY*LORD CALVERT TRLPK*CO39*Using ProQA Medical*",
         "TIME:18:14:19",
         "CALL:Traumatic Injuries",
         "ADDR:21121 ACE LN",
         "X:WINDING WY / SHORT WY",
         "PLACE:LORD CALVERT TRLPK",
         "CITY:LEXINGTON PARK",
         "UNIT:CO39",
         "INFO:Using ProQA Medical");
 
     doTest("T26",
         "((59734) CAD ) 16:59:32*Eye Problems/Injuries*LA QUINTA INN*22769 THREE NOTCH RD*CHANCELLORS RUN RD*GUNSTON DR*CALIFORNIA*CO39*at the front desk*",
         "TIME:16:59:32",
         "CALL:Eye Problems/Injuries",
         "ADDR:22769 THREE NOTCH RD",
         "X:CHANCELLORS RUN RD / GUNSTON DR",
         "PLACE:LA QUINTA INN",
         "CITY:CALIFORNIA",
         "UNIT:CO39",
         "INFO:at the front desk");
 
     doTest("T27",
         "((62231) CAD ) 15:12:53*Sick Person*CHESAPEAKE SHORES*21412 GREAT MILLS RD*SUBURBAN DR*SANNERS LN*LEXINGTON PARK*CO39*97 yr/f with GI bleed   rm 223A  (b",
         "TIME:15:12:53",
         "CALL:Sick Person",
         "ADDR:21412 GREAT MILLS RD",
         "X:SUBURBAN DR / SANNERS LN",
         "PLACE:CHESAPEAKE SHORES",
         "CITY:LEXINGTON PARK",
         "UNIT:CO39",
         "INFO:97 yr/f with GI bleed   rm 223A  (b");
 
     doTest("T28",
         "((3871) CAD ) 15:18:55*Seizures/Convulsions*PARK HALL ELEMENTARY*20343 HERMANVILLE RD*PIPER CT*DIXON CT*PARK HALL*A397 CO39 ALS M3*COME IN OFF OF ROUTE5*",
         "TIME:15:18:55",
         "CALL:Seizures/Convulsions",
         "ADDR:20343 HERMANVILLE RD",
         "X:PIPER CT / DIXON CT",
         "PLACE:PARK HALL ELEMENTARY",
         "CITY:PARK HALL",
         "UNIT:A397 CO39 ALS M3",
         "INFO:COME IN OFF OF ROUTE5");
 
     doTest("T29",
         "((3306) CAD ) 13:33:38*Allergic Reaction/Bee Stings*GREAT MILLS HIGH SCHOOL*21130 GREAT MILLS RD*CYPRESS WY*TRI COMMUNITY WY*GREAT MILLS*ALS CO39*Using P",
         "TIME:13:33:38",
         "CALL:Allergic Reaction/Bee Stings",
         "ADDR:21130 GREAT MILLS RD",
         "X:CYPRESS WY / TRI COMMUNITY WY",
         "PLACE:GREAT MILLS HIGH SCHOOL",
         "CITY:GREAT MILLS",
         "UNIT:ALS CO39",
         "INFO:Using P");
 
     doTest("T30",
         "((3130) CAD ) 12:57:41*Falls/Traumatic*ESPERANZA MIDDLE SCHOOL*22790 MAPLE RD*THREE NOTCH RD*ELM CT*TOWN CREEK*A389 CO39*USE THE MAIN ENTRANCE*",
         "TIME:12:57:41",
         "CALL:Falls/Traumatic",
         "ADDR:22790 MAPLE RD",
         "X:THREE NOTCH RD / ELM CT",
         "PLACE:ESPERANZA MIDDLE SCHOOL",
         "CITY:CALIFORNIA",
         "UNIT:A389 CO39",
         "INFO:USE THE MAIN ENTRANCE");
 
     doTest("T31",
         "((2455) CAD ) 10:10:53*Falls/Traumatic*BRETON MEDICAL GROUP BLDG #3*22576 MACARTHUR BL SUITE 354*HALSEY CT*THREE NOTCH RD*SAN SOUCI*CO39 A389*56yof C/A/B",
         "TIME:10:10:53",
         "CALL:Falls/Traumatic",
         "ADDR:22576 MACARTHUR BL",
         "APT:SUITE 354",
         "X:HALSEY CT / THREE NOTCH RD",
         "PLACE:BRETON MEDICAL GROUP BLDG #3",
         "CITY:CALIFORNIA",
         "UNIT:CO39 A389",
         "INFO:56yof C/A/B");
 
     doTest("T32",
         "((1741) CAD ) 05:36:56*Falls/Traumatic*CALLAWAY SHELL*20943 POINT LOOKOUT RD APT3*PINEY POINT RD*HUNTING QUARTER DR*CALLAWAY*CO39*44YOM C/A/B; CUT TO N",
         "TIME:05:36:56",
         "CALL:Falls/Traumatic",
         "ADDR:20943 POINT LOOKOUT RD",
         "APT:APT3",
         "X:PINEY POINT RD / HUNTING QUARTER DR",
         "PLACE:CALLAWAY SHELL",
         "CITY:CALLAWAY",
         "UNIT:CO39",
         "INFO:44YOM C/A/B; CUT TO N");
 
     doTest("T33",
         "((64130) CAD ) 18:24:21*Sick Person*SOUTH RIDGE*13425 POINT LOOKOUT RD APT2*CURLEYS RD*EVERGREEN ESTATES LN*RIDGE*CO39*29 yom side pains*",
         "TIME:18:24:21",
         "CALL:Sick Person",
         "ADDR:13425 POINT LOOKOUT RD",
         "APT:APT2",
         "X:CURLEYS RD / EVERGREEN ESTATES LN",
         "PLACE:SOUTH RIDGE",
         "CITY:RIDGE",
         "UNIT:CO39",
         "INFO:29 yom side pains");
 
     doTest("T34",
         "((62231) CAD ) 15:12:53*Sick Person*CHESAPEAKE SHORES*21412 GREAT MILLS RD*SUBURBAN DR*SANNERS LN*LEXINGTON PARK*CO39*97 yr/f with GI bleed   rm 223A  (b",
         "TIME:15:12:53",
         "CALL:Sick Person",
         "ADDR:21412 GREAT MILLS RD",
         "X:SUBURBAN DR / SANNERS LN",
         "PLACE:CHESAPEAKE SHORES",
         "CITY:LEXINGTON PARK",
         "UNIT:CO39",
         "INFO:97 yr/f with GI bleed   rm 223A  (b");
 
     doTest("T35",
         "((12416) CAD ) 18:36:28*Chest Pain*45848 KRYSTAL LN*TRI COMMUNITY WY*BRIDGETT LN*LORD CALVERT TRLPK*ALS CO39*Using ProQA Medical*",
         "TIME:18:36:28",
         "CALL:Chest Pain",
         "ADDR:45848 KRYSTAL LN",
         "X:TRI COMMUNITY WY / BRIDGETT LN",
         "PLACE:LORD CALVERT TRLPK",
         "CITY:LEXINGTON PARK",
         "UNIT:ALS CO39",
         "INFO:Using ProQA Medical");
 
     doTest("T36",
         "((62650) CAD ) 17:06:15*Personal Injury Accident*GOV THOMAS JOHNSON BRIDGE*46100 PATUXENT BEACH RD*NORTH PATUXENT BEACH RD*DEAD",
         "TIME:17:06:15",
         "CALL:Personal Injury Accident",
         "ADDR:46100 PATUXENT BEACH RD",
         "X:NORTH PATUXENT BEACH RD / DEAD",
         "PLACE:GOV THOMAS JOHNSON BRIDGE");
 
     doTest("T37",
         "((557) CAD ) 23:33:54*Breathing Difficulties*CEDAR LANE APARTMENTS*22680 CEDAR LANE CT APT2204*POINT LOOKOUT RD*CEDAR LANE RD*LEONARDTOWN*ALS CO19*74YOM",
         "TIME:23:33:54",
         "CALL:Breathing Difficulties",
         "ADDR:22680 CEDAR LANE CT",
         "APT:APT2204",
         "X:POINT LOOKOUT RD / CEDAR LANE RD",
         "PLACE:CEDAR LANE APARTMENTS",
         "CITY:LEONARDTOWN",
         "UNIT:ALS CO19",
         "INFO:74YOM");
 
     doTest("T38",
         "((25655) CAD ) 19:03:50*Mutual Aid*CALVERT CO*CO9 CO7 CO3*Box 3-03*",
         "TIME:19:03:50",
         "CALL:Mutual Aid",
         "ADDR:CALVERT CO",
         "UNIT:CO9 CO7 CO3",
         "INFO:Box 3-03");
 
     doTest("T39",
         "((25495) CAD ) 19:03:50*Mutual Aid*CALVERT CO*CO9 CO7 CO3*Box 3-03*",
         "TIME:19:03:50",
         "CALL:Mutual Aid",
         "ADDR:CALVERT CO",
         "UNIT:CO9 CO7 CO3",
         "INFO:Box 3-03");
 
     doTest("T40",
         "((25420) CAD ) 18:44:03*Vehicle Fire*NICOLET PARK*21777 BUNKER HILL DR*DEAD END*LEXINGTON PARK*CO3*Using ProQA Fire*",
         "TIME:18:44:03",
         "CALL:Vehicle Fire",
         "ADDR:21777 BUNKER HILL DR",
         "X:DEAD END",
         "PLACE:NICOLET PARK",
         "CITY:LEXINGTON PARK",
         "UNIT:CO3",
         "INFO:Using ProQA Fire");
 
     doTest("T41",
         "((20896) CAD ) 14:43:34*OUTSIDE FIRE*21139 THREE NOTCH RD INTERSECTN*HERMANVILLE RD*HERMANVILLE*CO3*APPROX 1 MILE IN FROM 235*",
         "TIME:14:43:34",
         "CALL:OUTSIDE FIRE",
         "ADDR:21139 THREE NOTCH RD",
         "X:HERMANVILLE RD",
         "PLACE:HERMANVILLE",
         "CITY:LEXINGTON PARK",
         "UNIT:CO3",
         "INFO:APPROX 1 MILE IN FROM 235");
 
     doTest("T42",
         "((24505) CAD ) 12:13:18*1050 PD Structure*21481 SYDNEY DR*SELL DR*LEXINGTON PARK*CO3 SQ3*VEHICLE INTO A HOUSE*",
         "TIME:12:13:18",
         "CALL:1050 PD Structure",
         "ADDR:21481 SYDNEY DR",
         "X:SELL DR",
         "CITY:LEXINGTON PARK",
         "UNIT:CO3 SQ3",
         "INFO:VEHICLE INTO A HOUSE");
 
     doTest("T43",
         "((33038) CAD ) 01:57:14*Hazardous Condition*APT 311 FOXCHASE*45910 FOXCHASE DR APT311*LEXINGTON DR*GREAT MILLS*CO9*Using ProQA Fire*",
         "TIME:01:57:14",
         "CALL:Hazardous Condition",
         "ADDR:45910 FOXCHASE DR",
         "APT:APT311",
         "X:LEXINGTON DR",
         "PLACE:APT 311 FOXCHASE",
         "CITY:GREAT MILLS",
         "UNIT:CO9",
         "INFO:Using ProQA Fire");
 
     doTest("T44",
         "((33120) CAD ) 03:41:27*CO Detector No Symptons*45713 SUMMER LN*DEAD END*LORD CALVERT TRLPK*CO3*Using ProQA Fire*",
         "TIME:03:41:27",
         "CALL:CO Detector No Symptons",
         "ADDR:45713 SUMMER LN",
         "X:DEAD END",
         "PLACE:LORD CALVERT TRLPK",
         "CITY:LEXINGTON PARK",
         "UNIT:CO3",
         "INFO:Using ProQA Fire");
 
     doTest("T45",
         "((38992) CAD ) 16:54:15*Tree Down*46449 SUE DR*PRATHER DR*LEXINGTON PARK*CO3*TREE IN THE POWER LINES/LINES ARE SPARKING/LEANING ON HOUSE*",
         "TIME:16:54:15",
         "CALL:Tree Down",
         "ADDR:46449 SUE DR",
         "X:PRATHER DR",
         "CITY:LEXINGTON PARK",
         "UNIT:CO3",
         "INFO:TREE IN THE POWER LINES/LINES ARE SPARKING/LEANING ON HOUSE");
 
     doTest("T46",
         "((10171) CAD ) 14:06:23*OUTSIDE FIRE*44761 KING WY*SURREY WY*WILDEWOOD*CO9*Using ProQA Fire*",
         "TIME:14:06:23",
         "CALL:OUTSIDE FIRE",
         "ADDR:44761 KING WY",
         "X:SURREY WY",
         "CITY:WILDEWOOD",
         "UNIT:CO9",
         "INFO:Using ProQA Fire");
 
     doTest("T47",
         "((10596) CAD ) 05:36:57*Psychiatric/Suicide Attempt*BURCH MART CHARLOTTE HALL*30295 THREE NOTCH RD*MT WOLF RD*CHAR HALL*CO29*CALLER IS INSIDE THE SHELL S",
         "TIME:05:36:57",
         "CALL:Psychiatric/Suicide Attempt",
         "ADDR:30295 THREE NOTCH RD",
         "X:MT WOLF RD",
         "PLACE:BURCH MART CHARLOTTE HALL",
         "CITY:CHARLOTTE HALL",
         "UNIT:CO29",
         "INFO:CALLER IS INSIDE THE SHELL S");
 
     doTest("T48",
         "((16151) CAD ) 17:53:28*Heart Problems*CHARLOTTE HALL VETERANS HOME*29449 CHARLOTTE HALL RD*CHARLOTTE HALL SCHOOL RD*CHAR HALL*ALS CO29*Wing 3B 308B*",
         "TIME:17:53:28",
         "CALL:Heart Problems",
         "ADDR:29449 CHARLOTTE HALL RD",
         "X:CHARLOTTE HALL SCHOOL RD",
         "PLACE:CHARLOTTE HALL VETERANS HOME",
         "CITY:CHARLOTTE HALL",
         "UNIT:ALS CO29",
         "INFO:Wing 3B 308B");
 
     doTest("T49",
         "((15958) CAD ) 16:50:03*Sick Person*26875 THREE NOTCH RD*LEELAND RD*LAUREL GROVE*ALS CO29*house on the left*",
         "TIME:16:50:03",
         "CALL:Sick Person",
         "ADDR:26875 THREE NOTCH RD",
         "X:LEELAND RD",
         "CITY:LAUREL GROVE",
         "UNIT:ALS CO29",
         "INFO:house on the left");
 
     doTest("T50",
         "((15830) CAD ) 15:13:56*Falls/Traumatic*CHARLOTTE HALL TRANSFER STAT*37766 NEW MARKET TURNER RD*ELIZA WY*NEW MARKET*CO29*50 year old, Male, Conscious, Br",
         "TIME:15:13:56",
         "CALL:Falls/Traumatic",
         "ADDR:37766 NEW MARKET TURNER RD",
         "X:ELIZA WY",
         "PLACE:CHARLOTTE HALL TRANSFER STAT",
         "CITY:NEW MARKET",
         "UNIT:CO29",
         "INFO:50 year old, Male, Conscious, Br");
 
     doTest("T51",
         "((18992) CAD ) 07:21:50*Personal Injury Accident*ADF BINGO*29062 THREE NOTCH RD*NEW MARKET VILLAGE RD*NEW MARKET*CO2 SQ2 CO29 EMS42*",
         "TIME:07:21:50",
         "CALL:Personal Injury Accident",
         "ADDR:29062 THREE NOTCH RD",
         "X:NEW MARKET VILLAGE RD",
         "PLACE:ADF BINGO",
         "CITY:NEW MARKET",
         "UNIT:CO2 SQ2 CO29 EMS42");
 
     doTest("T52",
         "((26213) CAD ) 22:12:56*Sick Person*39990 MRS GRAVES RD*GEORGE G PL*ORAVILLE*CO29*43 year old, Male, Conscious, Breathing.*",
         "TIME:22:12:56",
         "CALL:Sick Person",
         "ADDR:39990 MRS GRAVES RD",
         "X:GEORGE G PL",
         "CITY:ORAVILLE",
         "UNIT:CO29",
         "INFO:43 year old, Male, Conscious, Breathing.");
 
     doTest("T53",
         "((39425) CAD ) 07:18:31*Sick Person*CHARLOTTE HALL VETERANS HOME*29449 CHARLOTTE HALL RD*CHARLOTTE HALL SCHOOL RD*CHAR HALL*ALS CO29*Room 149B 1D*",
         "TIME:07:18:31",
         "CALL:Sick Person",
         "ADDR:29449 CHARLOTTE HALL RD",
         "X:CHARLOTTE HALL SCHOOL RD",
         "PLACE:CHARLOTTE HALL VETERANS HOME",
         "CITY:CHARLOTTE HALL",
         "UNIT:ALS CO29",
         "INFO:Room 149B 1D");
 
     doTest("T54",
         "((10199) CAD ) 18:20:24*Breathing Difficulties*APT 856 LOCUST RIDGE*44792 LOCUST RIDGE CT*SURREY WY*WILDEWOOD*CO79 A797 ALS CO9*59 year old, Male, Consci",
         "TIME:18:20:24",
         "CALL:Breathing Difficulties",
         "ADDR:44792 LOCUST RIDGE CT",
         "X:SURREY WY",
         "PLACE:APT 856 LOCUST RIDGE",
         "CITY:WILDEWOOD",
         "UNIT:CO79 A797 ALS CO9",
         "INFO:59 year old, Male, Consci");
 
     doTest("T55",
         "((10056) CAD ) 17:21:51*House Fire*20915 DEER WOOD PARK DR*REDMOND RD*CHESTNUT HILLS*CO9*e132 -4*",
         "TIME:17:21:51",
         "CALL:House Fire",
         "ADDR:20915 DEER WOOD PARK DR",
         "X:REDMOND RD",
         "CITY:CHESTNUT HILLS",
         "UNIT:CO9",
         "INFO:e132 -4");
 
     doTest("T56",
         "((10896) CAD ) 20:50:43*Personal Injury Accident*26488 YOWAISKI MILL RD*WEST SPICER DR*COUNTRY LAKES*CO2 SQ2 CO29 CO59*PARKED CAR*",
         "TIME:20:50:43",
         "CALL:Personal Injury Accident",
         "ADDR:26488 YOWAISKI MILL RD",
         "X:WEST SPICER DR",
         "CITY:COUNTRY LAKES",
         "UNIT:CO2 SQ2 CO29 CO59",
         "INFO:PARKED CAR");
 
     doTest("T57",
         "((17525) CAD ) 15:15:46*Miscellaneous*ECC*23090 LEONARD HALL DR*HOLLYWOOD RD*LEONARDTOWN*CO19 CO29 CO39 CO49R CO59 CO6R*Severe thunderstorm warning till",
         "TIME:15:15:46",
         "CALL:Miscellaneous",
         "ADDR:23090 LEONARD HALL DR",
         "X:HOLLYWOOD RD",
         "PLACE:ECC",
         "CITY:LEONARDTOWN",
         "UNIT:CO19 CO29 CO39 CO49R CO59 CO6R",
         "INFO:Severe thunderstorm warning till");
 
     doTest("T58",
         "((14501) CAD ) 14:57:30*Traumatic Injuries*26174 T WOOD DR*DEAD END*MECHANICSVILLE*CO29 CO59 ALS*FEMALE BELIEVES SHE INJURED HER RIB ON THURSDAY AT PHYSI",
         "TIME:14:57:30",
         "CALL:Traumatic Injuries",
         "ADDR:26174 T WOOD DR",
         "X:DEAD END",
         "CITY:MECHANICSVILLE",
         "UNIT:CO29 CO59 ALS",
         "INFO:FEMALE BELIEVES SHE INJURED HER RIB ON THURSDAY AT PHYSI");
 
     doTest("T59",
         "((22502) CAD ) 05:56:37*Breathing Difficulties*18360 THREE NOTCH RD*TOMS WY*ST JAMES*CO39*80 year old, Male, Conscious, Breathing.*",
         "TIME:05:56:37",
         "CALL:Breathing Difficulties",
         "ADDR:18360 THREE NOTCH RD",
         "X:TOMS WY",
         "PLACE:ST JAMES",
         "CITY:LEXINGTON PARK",
         "UNIT:CO39",
         "INFO:80 year old, Male, Conscious, Breathing.");
 
     doTest("T60",
         "((34827) CAD ) 07:20:02*Falls/Traumatic*22518 ARMSWORTHY CT*CUL DE SAC*SAN SOUCI*ST38*84 year old, Female, Conscious, Breathing.*",
         "TIME:07:20:02",
         "CALL:Falls/Traumatic",
         "ADDR:22518 ARMSWORTHY CT",
         "X:CUL DE SAC",
         "PLACE:SAN SOUCI",
         "CITY:CALIFORNIA",
         "UNIT:ST38",
         "INFO:84 year old, Female, Conscious, Breathing.");
 
     doTest("T61",
         "((14717) CAD ) 20:21:08*Commercial Building Fire*22026 OXFORD CT APTB8*GLOUCESTER CT*LEXINGTON PARK*CO3 TK3 CO13 CO9 TWR9 CO6 ST39*9.No one is trapped in",
         "TIME:20:21:08",
         "CALL:Commercial Building Fire",
         "ADDR:22026 OXFORD CT",
         "APT:APTB8",
         "X:GLOUCESTER CT",
         "CITY:LEXINGTON PARK",
         "UNIT:CO3 TK3 CO13 CO9 TWR9 CO6 ST39",
         "INFO:9.No one is trapped in");
 
     doTest("T62",
         "((14833) CAD ) 20:21:08*Commercial Building Fire*22026 OXFORD CT APTB8*GLOUCESTER CT*LEXINGTON PARK*CO3 TK3 CO13 CO9 TWR9 CO6 ST39*9.No one is trapped in",
         "TIME:20:21:08",
         "CALL:Commercial Building Fire",
         "ADDR:22026 OXFORD CT",
         "APT:APTB8",
         "X:GLOUCESTER CT",
         "CITY:LEXINGTON PARK",
         "UNIT:CO3 TK3 CO13 CO9 TWR9 CO6 ST39",
         "INFO:9.No one is trapped in");
 
     doTest("T63",
         "((15045) CAD ) 20:21:08*Commercial Building Fire*22026 OXFORD CT APTB8*GLOUCESTER CT*LEXINGTON PARK*CO3 TK3 CO13 CO9 TWR9 CO6 ST39*9.No one is trapped in",
         "TIME:20:21:08",
         "CALL:Commercial Building Fire",
         "ADDR:22026 OXFORD CT",
         "APT:APTB8",
         "X:GLOUCESTER CT",
         "CITY:LEXINGTON PARK",
         "UNIT:CO3 TK3 CO13 CO9 TWR9 CO6 ST39",
         "INFO:9.No one is trapped in");
 
     doTest("T64",
         "((55034) CAD ) 01:13:28*Personal Injury Accident*27161 YOWAISKI MILL RD*TANYARD DR*COUNTRY LAKES*CO2 SQ2 CO29 EMS42 ALS*single vehicle overturned; across",
         "TIME:01:13:28",
         "CALL:Personal Injury Accident",
         "ADDR:27161 YOWAISKI MILL RD",
         "X:TANYARD DR",
         "CITY:COUNTRY LAKES",
         "UNIT:CO2 SQ2 CO29 EMS42 ALS",
         "INFO:single vehicle overturned; across");
 
     doTest("T65",
         "((10967) CAD ) 06:28:12*Sick Person*APT 843 LOCUST RIDGE*44790 LOCUST RIDGE CT UNIT843*SURREY WY*WILDEWOOD*ST38*86 year old, Female, Conscious, Breathing",
         "TIME:06:28:12",
         "CALL:Sick Person",
         "ADDR:44790 LOCUST RIDGE CT",
         "APT:UNIT843",
         "X:SURREY WY",
         "PLACE:APT 843 LOCUST RIDGE",
         "CITY:WILDEWOOD",
         "UNIT:ST38",
         "INFO:86 year old, Female, Conscious, Breathing");
 
     doTest("T66",
         "((30978) CAD ) 02:32:58*Personal Injury Accident*38805 FOLEY MATTINGLY RD*DEAD END*HELEN*CO2 SQ2 CO29 A299 CO19 A196*17 YOF C/A/B CAR BACKED INTO HER POS",
         "TIME:02:32:58",
         "CALL:Personal Injury Accident",
         "ADDR:38805 FOLEY MATTINGLY RD",
         "X:DEAD END",
         "CITY:HELEN",
         "UNIT:CO2 SQ2 CO29 A299 CO19 A196",
         "INFO:17 YOF C/A/B CAR BACKED INTO HER POS");
 
     doTest("T67",
         "((41568) CAD ) 11:45:22*Breathing Difficulties*22085 ST LEONARDS CIR*ST GEORGES DR*GREAT MILLS*ST38 ST39 CO79 ALS*VOMMITING BLOOD*",
         "TIME:11:45:22",
         "CALL:Breathing Difficulties",
         "ADDR:22085 ST LEONARDS CIR",
         "X:ST GEORGES DR",
         "CITY:GREAT MILLS",
         "UNIT:ST38 ST39 CO79 ALS",
         "INFO:VOMMITING BLOOD");
 
     doTest("T68",
         "((62956) CAD ) 21:27:30*Unconscious Person/Fainting*45345 BAREFOOT DR*GARRISON DR*BAREFOOT ACRES*ST38 A387 ALS*73 year old, Male, Unconscious, Breathing.",
         "TIME:21:27:30",
         "CALL:Unconscious Person/Fainting",
         "ADDR:45345 BAREFOOT DR",
         "X:GARRISON DR",
         "PLACE:BAREFOOT ACRES",
         "CITY:CALIFORNIA",
         "UNIT:ST38 A387 ALS",
         "INFO:73 year old, Male, Unconscious, Breathing.");
 
     doTest("T69",
         "((28689) CAD ) 23:33:56*Unconscious Person/Fainting*2532 GUM WY*GREENTREE SOUTH DR*GLEN FOREST NAWC*ST39 A389 ALS*",
         "TIME:23:33:56",
         "CALL:Unconscious Person/Fainting",
         "ADDR:2532 GUM WY",
         "X:GREENTREE SOUTH DR",
         "PLACE:GLEN FOREST NAWC",
         "CITY:LEXINGTON PARK",
         "UNIT:ST39 A389 ALS");
 
     doTest("T70",
         "((43652) CAD ) 12:29:09*Breathing Difficulties*23263 BY THE MILL RD*ALS ST38*60 year old, Female, Conscious, Breathing.*",
         "TIME:12:29:09",
         "CALL:Breathing Difficulties",
         "ADDR:23263 BY THE MILL RD",
        "UNIT:ALS ST38",
        "INFO:60 year old, Female, Conscious, Breathing.");
 
     doTest("T71",
         "((50741) CAD ) 14:51:00*Seizures/Convulsions*20601 WHITE POINT RD*KNOTTS LANDING WY*MEDLEYS NECK*ST39 CO79*semi concious had seizure in front yard*",
         "TIME:14:51:00",
         "CALL:Seizures/Convulsions",
         "ADDR:20601 WHITE POINT RD",
         "X:KNOTTS LANDING WY",
         "PLACE:MEDLEYS NECK",
         "CITY:LEONARDTOWN",
         "UNIT:ST39 CO79",
         "INFO:semi concious had seizure in front yard");
 
    doTest("T72",
        "((54436) CAD ) 07:32:15*Mutual Aid EMS*12455 BLACKSMITH DR INTERSECTN*CALVERT COUNTY*ST38 A389*TROUBLE BREATHING*",
        "TIME:07:32:15",
        "CALL:Mutual Aid EMS",
        "ADDR:12455 BLACKSMITH DR",
        "CITY:CALVERT COUNTY",
        "UNIT:ST38 A389",
        "INFO:TROUBLE BREATHING");

    doTest("T73",
        "((52100) CAD ) 12:20:08*Mutual Aid EMS*11740 ASBUSY CIRCLE INTERSECTN*CALVERT COUNTY*ST38*APT1203-TROUBLE BREATHING*",
        "TIME:12:20:08",
        "CALL:Mutual Aid EMS",
        "ADDR:11740 ASBUSY CIRCLE",
        "CITY:CALVERT COUNTY",
        "UNIT:ST38",
        "INFO:APT1203-TROUBLE BREATHING");

   }
 
   public static void main(String[] args) {
    new MDSaintMarysCountyParserTest().generateTests("T72", "TIME CALL ADDR APT X PLACE CITY UNIT INFO");
   }
 }
