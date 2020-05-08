 package net.anei.cadpage.parsers.MD;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class MDPrinceGeorgesCountyCParserTest extends BaseParserTest {
   
   public MDPrinceGeorgesCountyCParserTest() {
     setParser(new MDPrinceGeorgesCountyCParser(), "PRINCE GEORGES COUNTY", "MD");
   }
   
   @Test
   public void testParser1() {
 
     doTest("T1",
         "A830 Incident: F111850165, Type: BLS Amb, Loc:7008 VARNUM ST, PP, btwn ALLISON ST and 71ST AVE, TalkGroup: TGA2, Box: 3005, Map: 5410 G 9, Text: Medical ProQA recommends",
         "SRC:A830",
         "ID:F111850165",
         "CALL:BLS Amb",
         "ADDR:7008 VARNUM ST",
         "X:ALLISON ST and 71ST AVE",
         "CH:TGA2",
         "BOX:3005",
         "MAP:5410 G 9",
         "UNIT:A830");
 
     doTest("T2",
         "E830 Incident: F111880399, Type: Medic Local, Loc: 4714 66TH PL, PP, btwn WEBSTER ST and 66TH AVE, TalkGroup: TGA2, Box: 3004, Map: 5410 F 10, Text: Medical ProQA recommends dispatch at this time, Units:A830,PE830",
         "SRC:E830",
         "ID:F111880399",
         "CALL:Medic Local",
         "ADDR:4714 66TH PL",
         "X:WEBSTER ST and 66TH AVE",
         "CH:TGA2",
         "BOX:3004",
         "MAP:5410 F 10",
         "UNIT:A830,PE830");
 
     doTest("T3",
         "A830 Incident: F111920132, Type: BLS Amb, Loc: 7321 LANDOVER RD, PP, btwn PINEBROOK AVE and KENT TOWN DR, TalkGroup: TGA2, Box: 3302, Map: 5530 H 2, Text: Police ProQA recommends dispatch at this time, Unit:A830",
         "SRC:A830",
         "ID:F111920132",
         "CALL:BLS Amb",
         "ADDR:7321 LANDOVER RD",
         "X:PINEBROOK AVE and KENT TOWN DR",
         "CH:TGA2",
         "BOX:3302",
         "MAP:5530 H 2",
         "UNIT:A830");
 
     doTest("T4",
         "A843 Incident: F111840152, Type: Suicide, Loc:2217 HINDLE LN, PP, btwn HALLOW LN and HAWK LN, TalkGroup: TGA2, Box: 1608, Map: 5413 D 9, Text: Police ProQA recommends dispatch at this time, Unit:A843",
         "SRC:A843",
         "ID:F111840152",
         "CALL:Suicide",
         "ADDR:2217 HINDLE LN",
         "X:HALLOW LN and HAWK LN",
         "CH:TGA2",
         "BOX:1608",
         "MAP:5413 D 9",
         "UNIT:A843");
 
     doTest("T5",
         "E812 Incident: F111860096, Type: Working Code, Loc:KENT HALL - UM 22, UM, at 22 LEHIGH RD, UM, btwn PREINKERT DR and END, TalkGroup: TGA2, Box: 1207, Map: 5409 J 3, Text: UNCON MALE W/HEAD INJ, IFO BUILDING,UMPD ENROUTE, Unit:E812",
         "SRC:E812",
         "ID:F111860096",
         "CALL:Working Code",
         "ADDR:22 LEHIGH RD",
         "PLACE:KENT HALL - UM 22",
         "X:PREINKERT DR and END",
         "CH:TGA2",
         "BOX:1207",
         "MAP:5409 J 3",
         "INFO:UNCON MALE W/HEAD INJ / IFO BUILDING / UMPD ENROUTE",
         "UNIT:E812");
 
     doTest("T6",
         "E812 Incident: F111870042, Type: Invest Any Type, Loc:CUMBERLAND HALL - UM 122, UM, at 122 VALLEY DR, UM, btwn FARM DR and END, TalkGroup: TG1, Box: 1211, Map: 5409 K 2,Text: BELLS, Unit:E812",
         "SRC:E812",
         "ID:F111870042",
         "CALL:Invest Any Type",
         "ADDR:122 VALLEY DR",
         "PLACE:CUMBERLAND HALL - UM 122",
         "X:FARM DR and END",
         "CH:TG1",
         "BOX:1211",
         "MAP:5409 K 2",
         "INFO:BELLS",
         "UNIT:E812");
 
     doTest("T7",
         "E812 Incident: F111870154, Type: Building Fire, Loc:UM 795 - AVRUM GUDELSKY VET, UM, at 8050 GREENMEADE DR, UM, btwn END and METZEROTT RD, TalkGroup: TA11, Box: 1109, Map: 5287 J 10, Text: SMOKE IN THE BUILD LOADING DOCK AREA UM PD L,L, Units:BO886, E712, E812, E834, E835, TK801, TK814, TW807",
         "SRC:E812",
         "ID:F111870154",
         "CALL:Building Fire",
         "ADDR:8050 GREENMEADE DR",
         "PLACE:UM 795 - AVRUM GUDELSKY VET",
         "X:END and METZEROTT RD",
         "CH:TA11",
         "BOX:1109",
         "MAP:5287 J 10",
         "INFO:SMOKE IN THE BUILD LOADING DOCK AREA UM PD L / L",
         "UNIT:BO886,E712,E812,E834,E835,TK801,TK814,TW807");
 
     doTest("T8",
         "E812 Incident: F111870241, Type: Fire Alarm-AFA, Loc: HAGERSTOWN HALL - UM 258, UM, at 258 ELLICOTT DR, UM, btwn HAGERSTOWN DR and FARM DR, TalkGroup: TG1, Box: 1210, Map: 5409 J 2, Text: GEN FIRE ALRM, Unit:E812",
         "SRC:E812",
         "ID:F111870241",
         "CALL:Fire Alarm-AFA",
         "ADDR:258 ELLICOTT DR",
         "PLACE:HAGERSTOWN HALL - UM 258",
         "X:HAGERSTOWN DR and FARM DR",
         "CH:TG1",
         "BOX:1210",
         "MAP:5409 J 2",
         "INFO:GEN FIRE ALRM",
         "UNIT:E812");
 
     doTest("T9",
         "E812 Incident: F111880093, Type: Working Code, Loc: UM 998 UNIVERSITY COMMONS 3, PP, at 4318 KNOX RD, PP, btwn ROSSBURG DR and STERLING PL, TalkGroup: TGA2, Box: 1203, Map: 5409 K 4, Text: Medical ProQA recommends dispatch at this time, Units:A812, E812, MD812",
         "SRC:E812",
         "ID:F111880093",
         "CALL:Working Code",
         "ADDR:4318 KNOX RD",
         "PLACE:UM 998 UNIVERSITY COMMONS 3",
         "X:ROSSBURG DR and STERLING PL",
         "CH:TGA2",
         "BOX:1203",
         "MAP:5409 K 4",
         "UNIT:A812,E812,MD812");
 
     doTest("T10",
         "E812 Incident: F111880115, Type: House Fire, Loc: 511 EAST INDIAN SPRING DR, TalkGroup: TG1, Box: MAMC,Text: BOX 16-7 4TH DUE ENG...7C, Unit:E812",
         "SRC:E812",
         "ID:F111880115",
         "CALL:House Fire",
         "ADDR:511 EAST INDIAN SPRING DR",
         "CH:TG1",
         "BOX:MAMC",
         "INFO:BOX 16-7 4TH DUE ENG...7C",
         "UNIT:E812");
 
     doTest("T11",
         "E812 Incident: F111880271, Type: BLS Amb, Loc: 4107 BUCK CREEK RD, PP, btwn MORTON PL and TRUDE ST, TalkGroup: TGC2, Box: 3221, Map: 5765 K 2, Text: Medical ProQA recommends dispatch at this time, Unit:E812",
         "SRC:E812",
         "ID:F111880271",
         "CALL:BLS Amb",
         "ADDR:4107 BUCK CREEK RD",
         "X:MORTON PL and TRUDE ST",
         "CH:TGC2",
         "BOX:3221",
         "MAP:5765 K 2",
         "UNIT:E812");
 
     doTest("T12",
         "TK812 Incident: F111890027, Type: Street Alarm, Loc: 7867 BEAVER DAM RD, PP, btwn SHEEP RD and RESEARCH RD, TalkGroup: TGA9, Box: 3120, Map: 5288 K 6, Text: Fire ProQA recommends dispatch at this time, Units:BO886, E831, E835, TK812, TK814",
         "SRC:TK812",
         "ID:F111890027",
         "CALL:Street Alarm",
         "ADDR:7867 BEAVER DAM RD",
         "X:SHEEP RD and RESEARCH RD",
         "CH:TGA9",
         "BOX:3120",
         "MAP:5288 K 6",
         "UNIT:BO886,E831,E835,TK812,TK814");
 
     doTest("T13",
         "TK812 Incident: F111890028, Type: House Fire, Loc: 36 RIDGE RD #Q, GP, btwn HAMILTON PL and RIDGE CT, TalkGroup: TGA4, Box: 3510, Map: 5289 A 10, Text: CALLER ADV SHE CAN SEE AND SMELL SMOKE...., Units:BO886, E807, E811, E818, E835, TK812, TK814, TK831",
         "SRC:TK812",
         "ID:F111890028",
         "CALL:House Fire",
         "ADDR:36 RIDGE RD",
         "APT:Q",
         "X:HAMILTON PL and RIDGE CT",
         "CH:TGA4",
         "BOX:3510",
         "MAP:5289 A 10",
         "INFO:CALLER ADV SHE CAN SEE AND SMELL SMOKE....",
         "UNIT:BO886,E807,E811,E818,E835,TK812,TK814,TK831");
 
     doTest("T14",
         "A810 Incident: F111900107, Type: BLS Amb, Loc: 11217 BASSWOOD TER, PP, btwn BASSWOOD CT and END, TalkGroup: TGA2, Box: 4929, Map: 5289 H 3, Text: Medical ProQA recommends dispatch at this time, Unit:A810",
         "SRC:A810",
         "ID:F111900107",
         "CALL:BLS Amb",
         "ADDR:11217 BASSWOOD TER",
         "X:BASSWOOD CT and END",
         "CH:TGA2",
         "BOX:4929",
         "MAP:5289 H 3",
         "UNIT:A810");
 
     doTest("T15",
         "E810 Incident: F111900138, Type: Fire Alarm-AFA, Loc: 14402 LAUREL PL, LP, btwn MULBERRY ST and CATALPA ST, TalkGroup: TG1, Box: 1017, Map: 5169 C 6, Text: Fire ProQA recommends dispatch at this time, Unit:E810",
         "SRC:E810",
         "ID:F111900138",
         "CALL:Fire Alarm-AFA",
         "ADDR:14402 LAUREL PL",
         "X:MULBERRY ST and CATALPA ST",
         "CH:TG1",
         "BOX:1017",
         "MAP:5169 C 6",
         "UNIT:E810");
 
     doTest("T16",
         "A810 Incident: F111900241, Type: BLS Amb, Loc: 14200 LAUREL PARK DR, LP, btwn PARK CENTER DR and VAN DUSEN RD, TalkGroup: TGA2, Box: 1016, Map: 5168 K 7, Text: Medical ProQA recommends dispatch at this time RM 103 BED A, Unit:A810",
         "SRC:A810",
         "ID:F111900241",
         "CALL:BLS Amb",
         "ADDR:14200 LAUREL PARK DR",
         "X:PARK CENTER DR and VAN DUSEN RD",
         "CH:TGA2",
         "BOX:1016",
         "MAP:5168 K 7",
         "UNIT:A810");
 
     doTest("T17",
         "TW810 Incident: F111910137, Type: House Fire, Loc: 10 MAIN ST, LP, btwn END and LAFAYETTE AVE, TalkGroup: TGA2, Box: 4911, Map: 5169 F 4, Text: CHILD ADV FIRE HERE HIS FRIEND LIVES HERE THEN DISC LINE CANT CALLBACK, Units:AE27, BO886, E831, E849, HE11, HTK06, TK715, TW810",
         "SRC:TW810",
         "ID:F111910137",
         "CALL:House Fire",
         "ADDR:10 MAIN ST",
         "X:END and LAFAYETTE AVE",
         "CH:TGA2",
         "BOX:4911",
         "MAP:5169 F 4",
         "INFO:CHILD ADV FIRE HERE HIS FRIEND LIVES HERE THEN DISC LINE CANT CALLBACK",
         "UNIT:AE27,BO886,E831,E849,HE11,HTK06,TK715,TW810");
 
     doTest("T18",
         "E820 Incident: F111970009, Type: Medic Local, Loc: 12124 WHEELING AVE, PP, btwn END and BASKERVILLE PL,TalkGroup: TGC2, Box: 4503, Map: 5768 C 7, Text: Medical ProQA recommends dispatch at this time, Units:A823, E820, MD825",
         "SRC:E820",
         "ID:F111970009",
         "CALL:Medic Local",
         "ADDR:12124 WHEELING AVE",
         "X:END and BASKERVILLE PL",
         "CH:TGC2",
         "BOX:4503",
         "MAP:5768 C 7",
         "UNIT:A823,E820,MD825");
 
     doTest("T19",
         "E808 Incident: F111940223, Type: Street Alarm, Loc: 6982 WALKER MILL RD, PP, btwn KAREN BLVD and SHADY GLEN DR, TalkGroup: TGC3, Box: 2617, Map: 5650 J 1, Text: Fire ProQA recommends dispatch at this time, Units:BO883, E808, E826, QT838, TK829",
         "SRC:E808",
         "ID:F111940223",
         "CALL:Street Alarm",
         "ADDR:6982 WALKER MILL RD",
         "X:KAREN BLVD and SHADY GLEN DR",
         "CH:TGC3",
         "BOX:2617",
         "MAP:5650 J 1",
         "UNIT:BO883,E808,E826,QT838,TK829");
 
     doTest("T20",
         "E808 Incident: F111940099, Type: Medic Local, Loc: ROLLINS AVE/DENISE DR, PP,  <1200/ 1199>, TalkGroup:TGA2, Box: 0820, Map: 5650 F 1, Text: Medical ProQA recommends dispatch at this time, Units:A826, E808,MD846",
         "SRC:E808",
         "ID:F111940099",
         "CALL:Medic Local",
         "ADDR:ROLLINS AVE & DENISE DR",
         "CH:TGA2",
         "BOX:0820",
         "MAP:5650 F 1",
         "UNIT:A826,E808,MD846");
 
     doTest("T21",
         "E808 Incident: F111940284, Type: Medic Local, Loc: 7945 CENTRAL AVE, PP, btwn JONQUIL AVE and WESTHAMPTON AVE, TalkGroup: TGA2, Box: 0806, Map: 5531 A 8, Text: Medical ProQA recommends dispatch at this time, Units:E808, PA838",
         "SRC:E808",
         "ID:F111940284",
         "CALL:Medic Local",
         "ADDR:7945 CENTRAL AVE",
         "X:JONQUIL AVE and WESTHAMPTON AVE",
         "CH:TGA2",
         "BOX:0806",
         "MAP:5531 A 8",
         "UNIT:E808,PA838");
 
     doTest("T22",
         "E820 Incident: F111970009, Type: Medic Local, Loc: 12124 WHEELING AVE, PP, btwn END and BASKERVILLE PL,TalkGroup: TGC2, Box: 4503, Map: 5768 C 7, Text: Medical ProQA recommends dispatch at this time, Units:A823, E820, MD825",
         "SRC:E820",
         "ID:F111970009",
         "CALL:Medic Local",
         "ADDR:12124 WHEELING AVE",
         "X:END and BASKERVILLE PL",
         "CH:TGC2",
         "BOX:4503",
         "MAP:5768 C 7",
         "UNIT:A823,E820,MD825");
 
     doTest("T23",
         "TK834 Incident: F112000125, Type: Apartment Gas Leak, Loc: 1521 MADISON ST #204, PP, btwn NICHOLSON ST and 16TH AVE, TalkGroup: TGA2, Box: 4401, Map: 5409 D 7, Text: Fire ProQA recommends dispatch at this time, Units:BO884, E855, PE844, TK801, TK834",
         "SRC:TK834",
         "ID:F112000125",
         "CALL:Apartment Gas Leak",
         "ADDR:1521 MADISON ST",
         "X:NICHOLSON ST and 16TH AVE",
         "CH:TGA2",
         "BOX:4401",
         "MAP:5409 D 7",
         "APT:204",
         "UNIT:BO884,E855,PE844,TK801,TK834");
 
     doTest("T24",
         "E810 Incident: F112050265, Type: Fire Alarm-AFA, Loc: 14402 LAUREL PL, LP, btwn MULBERRY ST and CATALPA ST, TalkGroup: TG1, Box: 1017, Map: 5169 C 6, Text: Fire ProQA recommends dispatch at this time, Unit:E810",
         "SRC:E810",
         "ID:F112050265",
         "CALL:Fire Alarm-AFA",
         "ADDR:14402 LAUREL PL",
         "X:MULBERRY ST and CATALPA ST",
         "CH:TG1",
         "BOX:1017",
         "MAP:5169 C 6",
         "UNIT:E810");
 
     doTest("T25",
         "E808 Incident: F112050338<tel:112050338>, Type: Apartment Gas Leak, Loc: 5231 MARLBORO PIKE, PP, btwn SHAMROCK AVE and LEE JAY DR, TalkGroup: TGA4, Box: 2627, Map: 5650 D 2, Text: Fire ProQA recommends dispatch at this time, Units:BO883, E808, E817, QT838, TK829",
         "SRC:E808",
         "ID:F112050338<tel:112050338>",
         "CALL:Apartment Gas Leak",
         "ADDR:5231 MARLBORO PIKE",
         "X:SHAMROCK AVE and LEE JAY DR",
         "CH:TGA4",
         "BOX:2627",
         "MAP:5650 D 2",
         "UNIT:BO883,E808,E817,QT838,TK829");

    doTest("T26",
        "RE833 Incident: F112500109, Type: PIA Limited Access, Loc: WB JOHN HANSON HWY WB/BALTIMORE WASHINGTON PK Y NB, PP, at WB JOHN HANSON HWY WB/BALTIMORE WASHINGTON PKY NB, PP <0, TalkGroup: TGA2, Box: 2213, Map: 5530 A 3, Text: COMPL ADV OF A SIG 9I, LOC IS NEAR THE RAMP LOC IS B4 THE KENILWORTH EXIT ON THE RIGHT// E809, RE833",
        "SRC:RE833",
        "ID:F112500109",
        "CALL:PIA Limited Access",
        "ADDR:WB JOHN HANSON HWY WB & BALTIMORE WASHINGTON PKY NB",
        "PLACE:WB JOHN HANSON HWY WB & BALTIMORE WASHINGTON PK Y NB",
        "CH:TGA2",
        "BOX:2213",
        "MAP:5530 A 3",
        "INFO:COMPL ADV OF A SIG 9I / LOC IS NEAR THE RAMP LOC IS B4 THE KENILWORTH EXIT ON THE RIGHT// E809 / RE833",
        "UNIT:RE833");
  }
   
   public static void main(String[] args) {
    new MDPrinceGeorgesCountyCParserTest().generateTests("T27");
   }
   
   
 }
