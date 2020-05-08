 package net.anei.cadpage.parsers;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 public class FieldProgramParserTest extends BaseParserTest {
   
   private static final String[] CITY_LIST = new String[]{"KENSBURG", "KEN TOWN"};
   private static final String DEF_CITY = "STATE OF MIND";
   private static final String DEF_STATE = "XX";
   
   FieldProgramParser parser;
   
   public FieldProgramParserTest() {
     parser = new TestParser(CITY_LIST, DEF_CITY, DEF_STATE, null);
     setParser(parser, DEF_CITY, DEF_STATE);
   }
   
   @Test
   public void testProblem() {
   }
   
   @Test
   public void testproblems() {
     doFieldTest("T1", "TYPE:CALL UNTS:UNIT XST:X UNTS:UNIT",
         "TYPE:FIRE;UNTS:A70;XST:BLACK ST",
         "CALL:FIRE",
         "UNIT:A70",
         "X:BLACK ST");
     doFieldTest("T2", "TYPE:CALL UNTS:UNIT XST:X UNTS:UNIT",
         "TYPE:FIRE;XST:BLACK ST;UNTS:A70",
         "CALL:FIRE",
         "X:BLACK ST",
         "UNIT:A70");
   }
   
   @Test
   public void testBasicFields() {
     doFieldTest("Fields", 
         "CALL CALL SKIP PLACE ADDR CITY APT X X UNIT MAP ID PHONE INFO INFO SRC CODE NAME ADDRCITY",
         "BLACK;BLUE;JUNK;TOWN;123 SOUTH;HOME;20;NORTH;SOUTH;CAR 22;MAPIT;666;222-1234;LINE1;LINE2;ST1;XXX;JOHN",
         "CALL:BLACK / BLUE",
         "PLACE:TOWN",
         "ADDR:123 SOUTH",
         "CITY:HOME",
         "APT:20",
         "X:NORTH & SOUTH",
         "UNIT:CAR 22",
         "MAP:MAPIT",
         "ID:666",
         "PHONE:222-1234",
         "INFO:LINE1 / LINE2",
         "SRC:ST1",
         "CODE:XXX",
         "NAME:JOHN");
   }
   
   public void testCompoundFields() {
     doFieldTest("Compound fields",
         "ADDRCITY",
         "100 BLACK ST, NORTH FACE",
         "ADDR:100 BLACK ST",
         "CITY:NORTH FACE");
   }
   
   @Test
   public void testFieldConditions() {
     
     doFieldTest("Address present",
         "ADDR? INFO", "US 30;LINE",
         "ADDR:US 30",
         "INFO:LINE");
     doFieldTest("Address absent",
         "ADDR? INFO", "LINE",
         "INFO:LINE");
     
     doFieldTest("City present",
         "CITY? INFO",
         "KEN TOWN;LIGHT",
         "CITY:KEN TOWN",
         "INFO:LIGHT");
     doFieldTest("City absent",
         "CITY? INFO",
         "LIGHT",
         "INFO:LIGHT");
     
     doFieldTest("Cross present",
         "X? INFO",
         "BLACK RD;LIGHT",
         "X:BLACK RD",
         "INFO:LIGHT");
     doFieldTest("Cross absent",
         "X? INFO",
         "LIGHT",
         "INFO:LIGHT");
     
     doFieldTest("ID present",
         "ID? INFO",
         "666;LIGHT",
         "ID:666",
         "INFO:LIGHT");
     doFieldTest("ID present",
         "ID? INFO",
         "LIGHT",
         "INFO:LIGHT");
     
     doFieldTest("INTLS 1",
         "INTLS? INFO",
         "XX;HELP",
         "INFO:HELP");
     doFieldTest("INTLS 2",
         "INTLS? INFO",
         "KEC;HELP",
         "INFO:HELP");
     doFieldTest("INTLS 3",
         "INTLS? INFO",
         "X;HELP",
         "INFO:X");
     doFieldTest("INTLS 4",
         "INTLS? INFO",
         "ab0;HELP",
         "INFO:ab0");
     
   }
   
   @Test
   public void testInfoAptField() {
     doFieldTest("T1", "INFO", "apt    201", "APT:201");
     doFieldTest("T2", "INFO", "aptnuk X", "INFO:aptnuk X");
     doFieldTest("T3", "INFO", "APT:16F", "APT:16F");
     doFieldTest("T4", "INFO", "APT# 30", "APT:30");
   }
   
   @Test
   public void testRequiredFields() {
 
     doFieldTest("All Fields", 
         "CALL CALL SKIP PLACE ADDR! CITY APT X X UNIT",
         "BLACK;BLUE;JUNK;TOWN;123 SOUTH;HOME;20;NORTH;SOUTH;CAR 22",
         "CALL:BLACK / BLUE",
         "PLACE:TOWN",
         "ADDR:123 SOUTH",
         "CITY:HOME",
         "APT:20",
         "X:NORTH & SOUTH",
         "UNIT:CAR 22");
 
     doFieldTest("Short fields", 
         "CALL CALL SKIP PLACE ADDR! CITY APT X X UNIT",
         "BLACK;BLUE;JUNK;TOWN;123 SOUTH",
         "CALL:BLACK / BLUE",
         "PLACE:TOWN",
         "ADDR:123 SOUTH");
 
     doFieldFail("Missing req field",
         "CALL CALL SKIP PLACE ADDR! CITY APT X X UNIT",
         "BLACK;BLUE;JUNK;TOWN");
     
     doFieldFail("Missing most everything",
         "CALL CALL SKIP PLACE ADDR! CITY APT X X UNIT",
         "NA");
   }
   
   @Test
   public void testOptionalFields() {
     
     doFieldTest("Optional field present",
         "CALL CITY? INFO",
         "HELLO DOLLY;KEN TOWN;BLUE LIGHT",
         "CALL:HELLO DOLLY",
         "CITY:KEN TOWN",
         "INFO:BLUE LIGHT");
     
     doFieldTest("Optional field absent",
         "CALL CITY? INFO",
         "HELLO DOLLY;BLUE LIGHT",
         "CALL:HELLO DOLLY",
         "INFO:BLUE LIGHT");
     
     doFieldTest("Optional field present skips",
         "CALL SKIP CITY? SKIP INFO",
         "HELLO DOLLY;JUNK;KEN TOWN;JUNK;BLUE LIGHT",
         "CALL:HELLO DOLLY",
         "CITY:KEN TOWN",
         "INFO:BLUE LIGHT");
     
     doFieldTest("Triggered Optional field present",
         "CITY?% INFO",
         "%MYTOWN;MY LIFE",
         "CITY:MYTOWN",
         "INFO:MY LIFE");
     
     doFieldTest("Triggered Optional field absent",
         "CITY?% INFO",
         "KEN TOWN;MY LIFE",
         "INFO:KEN TOWN");
     
     doFieldTest("Deferred field present",
         "PLACE? CITY",
         "MACWORLD;KEN TOWN",
         "PLACE:MACWORLD",
         "CITY:KEN TOWN");
     
     doFieldTest("Deferred field absent",
         "PLACE? CITY INFO",
         "KEN TOWN;MY WAY",
         "CITY:KEN TOWN",
         "INFO:MY WAY");
     
     doFieldTest("Long deferred opt field present",
         "CALL PLACE? INFO INFO INFO CITY",
         "FIRE;LONGVIEW;LINE1;LINE2;LINE3;KEN TOWN",
         "CALL:FIRE",
         "PLACE:LONGVIEW",
         "INFO:LINE1 / LINE2 / LINE3",
         "CITY:KEN TOWN");
     
     doFieldTest("Long deferred opt field absent",
         "CALL PLACE? INFO INFO INFO CITY",
         "FIRE;LINE1;LINE2;LINE3;KEN TOWN",
         "CALL:FIRE",
         "INFO:LINE1 / LINE2 / LINE3",
         "CITY:KEN TOWN");
     
     doFieldTest("Long deferred opt field present skips",
         "CALL SKIP PLACE? SKIP INFO INFO INFO SKIP CITY",
         "FIRE;SKIP;LONGVIEW;SKIP;LINE1;LINE2;LINE3;SKIP;KEN TOWN",
         "CALL:FIRE",
         "PLACE:LONGVIEW",
         "INFO:LINE1 / LINE2 / LINE3",
         "CITY:KEN TOWN");
     
     doFieldTest("Long deferred opt field absent skips",
         "CALL SKIP PLACE? SKIP INFO INFO INFO SKIP CITY",
         "FIRE;SKIP;SKIP;LINE1;LINE2;LINE3;SKIP;KEN TOWN",
         "CALL:FIRE",
         "INFO:LINE1 / LINE2 / LINE3",
         "CITY:KEN TOWN");
     
     doFieldTest("Optional field at end of program", "X?", "12");
   }
   
   @Test
   public void testBackToBackOptionalFields() {
     
     doFieldTest("preset - present",
         "PLACE? CITY INFO? ID",
         "BIG MAC;KEN TOWN;LINE1;666",
         "PLACE:BIG MAC",
         "CITY:KEN TOWN",
         "INFO:LINE1",
         "ID:666");
     
     doFieldTest("preset - ABSENT",
         "PLACE? CITY INFO? ID",
         "BIG MAC;KEN TOWN;666",
         "PLACE:BIG MAC",
         "CITY:KEN TOWN",
         "ID:666");
     
     doFieldTest("absent - present",
         "PLACE? CITY INFO? ID",
         "KEN TOWN;LINE1;666",
         "CITY:KEN TOWN",
         "INFO:LINE1",
         "ID:666");
     
     doFieldTest("absent - absent",
         "PLACE? CITY INFO? ID",
         "KEN TOWN;666",
         "CITY:KEN TOWN",
         "ID:666");
   }
   
   @Test
   public void testRepeatFields() {
     
     doFieldTest("Infinite repeat field",
         "CALL PLACE INFO+",
         "HELP ME;AT HOME;SOMEWHERE;OVER THE RAINBOW;SKYS ARE BLUE",
         "CALL:HELP ME",
         "PLACE:AT HOME",
         "INFO:SOMEWHERE / OVER THE RAINBOW / SKYS ARE BLUE");
     
     doFieldTest("Conditional repeat field",
         "X+? INFO",
         "BLACK RD;WHITE ST;STONEVILLE",
         "X:BLACK RD & WHITE ST",
         "INFO:STONEVILLE");
     
     doFieldTest("Deferred conditional repeat field",
         "INFO+? ID",
         "LINE1;LINE2;LINE3;666",
         "INFO:LINE1 / LINE2 / LINE3",
         "ID:666");
     
     doFieldTest("Long deferred conditional repeat field",
         "INFO+? PLACE NAME ID",
         "LINE1;LINE2;LINE3;BIG HOUSE;KEN CORBIN;666",
         "INFO:LINE1 / LINE2 / LINE3",
         "PLACE:BIG HOUSE",
         "NAME:KEN CORBIN",
         "ID:666");
     
     doFieldTest("empty deferred conditional repeat field",
         "INFO+? PLACE NAME ID",
         "BIG HOUSE;KEN CORBIN;666",
         "PLACE:BIG HOUSE",
         "NAME:KEN CORBIN",
         "ID:666");
   }
   
   @Test
   public void testZQual() {
     
     doFieldTest("Without",
         "ADDR? CITY",
         "AVE OF WISDOM;KEN TOWN",
         "CITY:AVE OF WISDOM");
     
     doFieldTest("With",
         "ADDR/Z? CITY",
         "AVE OF WISDOM;KEN TOWN",
         "ADDR:AVE OF WISDOM",
         "CITY:KEN TOWN");
   }
   
   @Test
   public void testconditionalBranch() {
     
     doFieldTest("Empty",
         "CALL ( ) ID",
         "FIRE;666",
         "CALL:FIRE",
         "ID:666");
     
     doFieldTest("simple-1",
         "CALL ( CITY ST | INFO INFO ) ID",
         "FIRE FIRE;KEN TOWN;PA;666",
         "CALL:FIRE FIRE",
         "CITY:KEN TOWN",
         "ST:PA",
         "ID:666");
     
     doFieldTest("simple-2",
         "CALL ( CITY ST | INFO INFO ) ID",
         "FIRE FIRE;LINE1;LINE2;666",
         "CALL:FIRE FIRE",
         "INFO:LINE1 / LINE2",
         "ID:666");
     
     doFieldTest("complex-1",
         "SKIP ( CITY ST PLACE | PLACE CITY ST | INFO INFO ID | ) NAME",
         "SKIP;KEN TOWN;PA;BOBS BURGERS;KCORBIN",
         "CITY:KEN TOWN",
         "ST:PA",
         "PLACE:BOBS BURGERS",
         "NAME:KCORBIN");
     
     doFieldTest("complex-2",
         "SKIP ( CITY ST PLACE | PLACE CITY ST | INFO INFO ID | ) NAME",
         "SKIP;BOBS BURGERS;KEN TOWN;PA;KCORBIN",
         "CITY:KEN TOWN",
         "ST:PA",
         "PLACE:BOBS BURGERS",
         "NAME:KCORBIN");
     
     doFieldTest("complex-3",
         "SKIP ( CITY ST PLACE | PLACE CITY ST | INFO INFO ID | ) NAME",
         "SKIP;LINE1;LINE2;666;KCORBIN",
         "INFO:LINE1 / LINE2",
         "ID:666",
         "NAME:KCORBIN");
     
     doFieldTest("complex-4",
         "SKIP ( CITY ST PLACE | PLACE CITY ST | INFO INFO ID | ) NAME",
         "SKIP;KCORBIN",
         "NAME:KCORBIN");
     
     doFieldTest("doubletest-1",
         "SKIP ( CITY PLACE? X | SKIP )",
         "SKIP;KEN TOWN;HOME;BLACK ST",
         "CITY:KEN TOWN",
         "PLACE:HOME",
         "X:BLACK ST");
     
     doFieldTest("doubletest-2",
         "SKIP ( CITY PLACE? X | SKIP )",
         "SKIP;KEN TOWN;BLACK ST",
         "CITY:KEN TOWN",
         "X:BLACK ST");
         
   }
   
   @Test
   public void testTagFields() {
 
     doFieldTest("Basic",
         "CALL LOC:ADDR! CTY:CITY RUN:ID",
         "FIRE; LOC: 100 PINE ST; CTY: BLACKBURG; RUN:666",
         "CALL:FIRE",
         "ADDR:100 PINE ST",
         "CITY:BLACKBURG",
         "ID:666");
     
     doFieldTest("Missing City",
         "CALL LOC:ADDR! CTY:CITY RUN:ID",
         "FIRE; LOC: 100 PINE ST; RUN:666",
         "CALL:FIRE",
         "ADDR:100 PINE ST",
         "ID:666");
 
     doFieldTest("Basic",
         "CALL LOC:ADDR! CTY:CITY RUN:ID",
         "FIRE; LOC: 100 PINE ST; CTY: BLACKBURG; RUN:666",
         "CALL:FIRE",
         "ADDR:100 PINE ST",
         "CITY:BLACKBURG",
         "ID:666");
     
     doFieldTest("Missing ID",
         "CALL LOC:ADDR! CTY:CITY RUN:ID",
         "FIRE; LOC: 100 PINE ST",
         "CALL:FIRE",
         "ADDR:100 PINE ST");
 
     doFieldFail("Missing City",
         "CALL LOC:ADDR! CTY:CITY RUN:ID",
         "FIRE; CTY: BLACKBURG; RUN:666");
     
     doFieldTest("tag w/blanks",
         "CALL New_Run_Num:ID",
         "STRUCTURE FIRE;New Run Num: 666",
         "CALL:STRUCTURE FIRE",
         "ID:666");
     
     doFieldTest("repeat",
         "Typ:CALL Com:INFO+ Run:ID",
         "Typ:FIRE;Com:LINE1;Com:LINE2;Run:666",
         "CALL:FIRE",
         "INFO:LINE1 / LINE2",
         "ID:666");
     
     doFieldTest("no repeat",
         "Typ:CALL Com:INFO Run:ID",
         "Typ:FIRE;Com:LINE1;Com:LINE2;Run:666",
         "CALL:FIRE",
         "INFO:LINE1",
         "ID:666");
     
     doFieldTest("inherited tag",
         "Typ:CALL Com:INFO+ Run:ID",
         "Typ:FIRE;Com:LINE1;LINE2;LINE3;Run:666",
         "CALL:FIRE",
         "INFO:LINE1 / LINE2 / LINE3",
         "ID:666");
     
     doFieldTest("skip untagged fields",
         "Typ:CALL Com:INFO Run:ID",
         "Typ:FIRE;Com:LINE1;LINE2;LINE3;Run:666",
         "CALL:FIRE",
         "INFO:LINE1",
         "ID:666");
     
     doFieldTest("Mixed tag and untagged",
         "Typ:CALL ADDR Com:INFO",
         "Typ:Fire;Block Addr;Com:this is a test",
         "CALL:Fire",
         "ADDR:Block Addr",
         "INFO:this is a test");
     
     doFieldTest("Mixed tag and untagged winc inc colon",
         "Typ:CALL ADDR Com:INFO",
         "Typ:Fire;Block: Addr;Com:this is a test",
         "CALL:Fire",
         "ADDR:Block: Addr",
         "INFO:this is a test");
     
     doFieldTest("Skip normal tag step",
         "Typ:CALL ADDR",
         "BLACK DR");
     
     doFieldTest("Skip optional tag step",
         "Typ:CALL? ADDR",
         "BLACK DR",
         "ADDR:BLACK DR");
   }
   
   @Test
   public void testOptionalTagFields() {
     doFieldTest("Present",
         "CALL PLACE? MAP:MAP",
         "FOREST FIRE;PETE ROCK;MAP:33",
         "CALL:FOREST FIRE",
         "PLACE:PETE ROCK",
         "MAP:33");
     
     doFieldTest("Missing",
         "CALL PLACE? MAP:MAP",
         "FOREST FIRE;MAP:33",
         "CALL:FOREST FIRE",
         "MAP:33");
 
   }
   
   @Test
   public void testSmartAddressField() {
     
     doFieldTest("CX", "ADDR/SCX", "FIRE 100 MAIN ST",
         "CALL:FIRE",
         "ADDR:100 MAIN ST");
     
     doFieldTest("PX", "ADDR/SPX", "FIRE 100 MAIN ST",
         "PLACE:FIRE",
         "ADDR:100 MAIN ST");
     
     doFieldTest("CP", "ADDR/SCP", "FIRE 100 MAIN ST BIG JOHNS",
         "CALL:FIRE",
         "ADDR:100 MAIN ST",
         "PLACE:BIG JOHNS");
     
     doFieldTest("XP", "ADDR/SXP!", "100 MAIN ST BIG JOHNS",
         "ADDR:100 MAIN ST",
         "PLACE:BIG JOHNS");
   }
   
   @Test
   public void testEndField() {
     
     doFieldTest("T1", "INFO+? UNIT END",
         "X1",
         "UNIT:X1");
     
     doFieldTest("T2", "INFO+? UNIT END",
         "LINE1;LINE2;LINE3;X1",
         "INFO:LINE1 / LINE2 / LINE3",
         "UNIT:X1");
   }
   
   @Override
   public void testBadMsg() {
     parser.setProgram("SKIP ADDR!");
     super.testBadMsg();
   }
 
   private void doFieldTest(String title, String program, String body, String ... result) {
     parser.setProgram(program);
     parser.checkForSkips();
     doTest(title, body, result);
     
   }
   
   private void doFieldFail(String title, String program, String body) {
     parser.setProgram(program);
     parser.checkForSkips();
     assertFalse(parser.parseMsg(body, new Data()));
   }
   
   private static class TestParser extends FieldProgramParser {
 
     public TestParser(String[] cities, String defCity, String defState, String programStr) {
       super(cities, defCity, defState, programStr);
     }
 
     @Override
     protected boolean parseMsg(String message, Data data) {
       return parseFields(message.split(";"), data);
     }
     
   }
 }
