 package net.anei.cadpage.parsers.NY;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class NYMadisonCountyAParserTest extends BaseParserTest {
   
   public NYMadisonCountyAParserTest() {
     setParser(new NYMadisonCountyAParser(), "MADISON COUNTY", "NY");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "(Dispatch) Dispatch ** 15:16 ** CHEST PAIN ** 3578 MUTTON HILL RD , FENNER ( FRANCIS RD / WYSS R ** - ** KENT, GEORGE SR ** 315-655-2533 **",
         "CALL:CHEST PAIN",
         "ADDR:3578 MUTTON HILL RD",
         "CITY:FENNER",
         "X:FRANCIS RD / WYSS R",
         "NAME:KENT, GEORGE SR",
         "PHONE:315-655-2533");
 
     doTest("T2",
         "(Dispatch) Dispatch ** 17:21 ** ALARM - FIRE - COLLEGE ** 22 SULLIVAN , CAZENOVIA VILLAGE ( GREEN /) ** - ** CAZENOVIA COLLE ** 315-655-7271 **",
         "CALL:ALARM - FIRE - COLLEGE",
         "ADDR:22 SULLIVAN",
         "CITY:CAZENOVIA VILLAGE",
         "X:GREEN",
         "NAME:CAZENOVIA COLLE",
         "PHONE:315-655-7271");
 
     doTest("T3",
         "Dispatch ** 21:35 ** BREATHING PROBLEM ** @HUBBARD HALL (5 SULLIVAN ST (CAZENOVIA VILLAGE ) ** HUBBARD HALL- **  **  **",
         "CALL:BREATHING PROBLEM",
         "PLACE:HUBBARD HALL",
         "ADDR:5 SULLIVAN ST",
         "CITY:CAZENOVIA VILLAGE");
 
     doTest("T4",
         "Dispatch ** 21:42 ** WATER PROBLEM ** 3243 ROUTE 20 , NELSON ** - ** michael manion ** 6559325 **",
         "CALL:WATER PROBLEM",
         "ADDR:3243 ROUTE 20",
         "CITY:NELSON",
         "NAME:michael manion",
         "PHONE:6559325");
 
     doTest("T5",
         "Dispatch ** 16:22 ** BREATHING PROBLEM ** 4188 MEADOW HILL RD , CAZENOVIA ( / ROUTE 20) ** - **  **  **",
         "CALL:BREATHING PROBLEM",
         "ADDR:4188 MEADOW HILL RD",
         "CITY:CAZENOVIA",
         "X:ROUTE 20");
 
     doTest("T6",
         "Dispatch ** 21:42 ** WATER PROBLEM ** 3243 ROUTE 20 , NELSON ** - ** michael manion ** 6559325 **",
         "CALL:WATER PROBLEM",
         "ADDR:3243 ROUTE 20",
         "CITY:NELSON",
         "NAME:michael manion",
         "PHONE:6559325");
 
   }
   
   public static void main(String[] args) {
     new NYMadisonCountyAParserTest().generateTests("T1");
   }
 }
