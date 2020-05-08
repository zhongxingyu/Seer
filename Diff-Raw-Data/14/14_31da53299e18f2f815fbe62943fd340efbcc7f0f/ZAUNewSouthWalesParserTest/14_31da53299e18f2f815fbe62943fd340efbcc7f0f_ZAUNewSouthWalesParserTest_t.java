 package net.anei.cadpage.parsers.ZAU;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class ZAUNewSouthWalesParserTest extends BaseParserTest {
   
   public ZAUNewSouthWalesParserTest() {
     setParser(new ZAUNewSouthWalesParser(), "", "NSW");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "SNRWLC, RFA 9071-54, Firstname Surname, 65 MCCLELLAND ST, WILLOUGHBY, 0433 381 999, LGE BRANCH, 15M UP THREAT TO FALL IN YARD.. CALL 0242516111",
         "UNIT:SNRWLC",
         "ID:RFA 9071-54",
         "NAME:Firstname Surname",
         "ADDR:65 MCCLELLAND ST",
         "CITY:WILLOUGHBY",
         "PHONE:0433 381 999",
         "CALL:LGE BRANCH / 15M UP THREAT TO FALL IN YARD");
 
     doTest("T2",
         "SNRWLC, RFA 9071-17, Firstname Surname, 37 INNES ROAD GREENWICH, 9906 999, TREE FALLEN & WEDGED ON 2 FENCES/SPLIT IN 3. CALL 0242516111",
         "UNIT:SNRWLC",
         "ID:RFA 9071-17",
         "NAME:Firstname Surname",
         "ADDR:37 INNES ROAD",
         "CITY:GREENWICH",
         "PHONE:9906 999",
         "CALL:TREE FALLEN & WEDGED ON 2 FENCES/SPLIT IN 3");
 
     doTest("T3",
         "SNRWLC, RFA 9072-18, Firstname Surname, 75 PARK ROAD NAREMBURN, 9439 999, ROOF GARDEN SHED COME LOOSE AND THREAT FALL.. CALL 0242516111",
         "UNIT:SNRWLC",
         "ID:RFA 9072-18",
         "NAME:Firstname Surname",
         "ADDR:75 PARK ROAD",
         "CITY:NAREMBURN",
         "PHONE:9439 999",
         "CALL:ROOF GARDEN SHED COME LOOSE AND THREAT FALL");
 
     doTest("T4",
         "SNRWLC, RFA 8984-40, Firstname Surname, 64 HAMILTON ST RIVERVIEW, 0402 984 0999, LRG BRANCH DOWN POSS ROOF DAMAGE DOG PRESENT. CALL 0242516111",
         "UNIT:SNRWLC",
         "ID:RFA 8984-40",
         "NAME:Firstname Surname",
         "ADDR:64 HAMILTON ST",
         "CITY:RIVERVIEW",
         "PHONE:0402 984 0999",
         "CALL:LRG BRANCH DOWN POSS ROOF DAMAGE DOG PRESENT");
 
     doTest("T5",
         "SNRWLC, RFA 9072-12, Firstname Surname, 4 FULLERS ROAD CHATSWOOD, 0417 204 999, ROOF DAMAGE REQUESTED TARPING. CALL 0242516111",
         "UNIT:SNRWLC",
         "ID:RFA 9072-12",
         "NAME:Firstname Surname",
         "ADDR:4 FULLERS ROAD",
         "CITY:CHATSWOOD",
         "PHONE:0417 204 999",
         "CALL:ROOF DAMAGE REQUESTED TARPING");
 
     doTest("T6",
         "SNRWLC, RFA 9045-41, Firstname Surname, 13B FRY ST, CHATSWOOD, 0477 415 999, LEAKING CEILING IN BATHROOM, COULD SES ATTEND ON 21/4. CALL 0242516111",
         "UNIT:SNRWLC",
         "ID:RFA 9045-41",
         "NAME:Firstname Surname",
         "ADDR:13B FRY ST",
         "CITY:CHATSWOOD",
         "PHONE:0477 415 999",
         "CALL:LEAKING CEILING IN BATHROOM / COULD SES ATTEND ON 21/4");
 
     doTest("T7",
         "SNRWLC, RFA 8902-70, Firstname Surname, 5 EMERSTAN DRIVE, CASTLE COVE, 9417 999, SUBSIDENCE- RETAINING WALL. CALL 0242516111",
         "UNIT:SNRWLC",
         "ID:RFA 8902-70",
         "NAME:Firstname Surname",
         "ADDR:5 EMERSTAN DRIVE",
         "CITY:CASTLE COVE",
         "PHONE:9417 999",
         "CALL:SUBSIDENCE- RETAINING WALL");
 
     doTest("T8",
         "SNRWLC, RFA 8835-101, Firstname Surname, 137 RIVER RD, NORTHWOOD, 0419999 999, LARGE TREE DOWN. CALL 0242516111",
         "UNIT:SNRWLC",
         "ID:RFA 8835-101",
         "NAME:Firstname Surname",
         "ADDR:137 RIVER RD",
         "CITY:NORTHWOOD",
         "PHONE:0419999 999",
         "CALL:LARGE TREE DOWN");

    doTest("T9",
        "SNRWLC, RFA 9400-20, MICHAEL FARBENBLUM, 62 TAMBOURINE BAY RD LANE COVE, 0419 257 816, FLOODING. CALL 0242516111 10/06 16:02:51",
        "UNIT:SNRWLC",
        "ID:RFA 9400-20",
        "NAME:MICHAEL FARBENBLUM",
        "ADDR:62 TAMBOURINE BAY RD",
        "CITY:LANE COVE",
        "PHONE:0419 257 816",
        "CALL:FLOODING",
        "DATE:10/06",
        "TIME:16:02:51");
   
   }
   
   public static void main(String[] args) {
     new ZAUNewSouthWalesParserTest().generateTests("T1");
   }
 }
