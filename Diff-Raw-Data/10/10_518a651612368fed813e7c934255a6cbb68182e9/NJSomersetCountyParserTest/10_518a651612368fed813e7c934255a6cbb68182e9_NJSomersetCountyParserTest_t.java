 package net.anei.cadpage.parsers.NJ;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class NJSomersetCountyParserTest extends BaseParserTest {
   
   public NJSomersetCountyParserTest() {
     setParser(new NJSomersetCountyParser(), "SOMERSET COUNTY", "NJ");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "[CAD]  [CAD] EFD-FD:11051201:05/12/2011 15:44:57:FIRE ALARM: FRANKLI-CARING HOUSE PROJECTS / 18 BLOOMFIELD AVE",
         "SRC:EFD-FD",
         "ID:11051201",
         "DATE:05/12/2011",
         "TIME:15:44:57",
         "CALL:FIRE ALARM",
         "CITY:FRANKLIN TWP",
         "PLACE:CARING HOUSE PROJECTS",
         "ADDR:18 BLOOMFIELD AVE");
 
     doTest("T2",
         "[CAD]  [CAD] EFD-FD:11051982:05/14/2011 11:51:28:FIRE ALARM:GENERAL: FRANKLI-HAMPTON INN / 255 DAVIDSON AVE",
         "SRC:EFD-FD",
         "ID:11051982",
         "DATE:05/14/2011",
         "TIME:11:51:28",
         "CALL:FIRE ALARM:GENERAL",
         "CITY:FRANKLIN TWP",
         "PLACE:HAMPTON INN",
         "ADDR:255 DAVIDSON AVE");
 
     doTest("T3",
         "[CAD]  [CAD] EFD-FD:11052014:05/14/2011 13:35:40:FIRE ALARM:ZONE-2ND FL REAR HALL SMK: FRANKLI-1165 HAMILTON ST",
         "SRC:EFD-FD",
         "ID:11052014",
         "DATE:05/14/2011",
         "TIME:13:35:40",
         "CALL:FIRE ALARM:ZONE-2ND FL REAR HALL SMK",
         "CITY:FRANKLIN TWP",
         "ADDR:1165 HAMILTON ST");
 
     doTest("T4",
         "[CAD]  [CAD] EFD-FD:11052016:05/14/2011 13:40:22:FIRE ALARM:GENERAL ALARM: FRANKLI-449 SKILLMANS LANE (ODD)",
         "SRC:EFD-FD",
         "ID:11052016",
         "DATE:05/14/2011",
         "TIME:13:40:22",
         "CALL:FIRE ALARM:GENERAL ALARM",
         "CITY:FRANKLIN TWP",
         "ADDR:449 SKILLMANS LANE",
         "INFO:ODD");
 
     doTest("T5",
         "[CAD]  [CAD] EFD-FD:11052407:05/15/2011 14:41:50:CO ALARM:NO ILLNESS: FRANKLI-25 MAYNARD ROAD",
         "SRC:EFD-FD",
         "ID:11052407",
         "DATE:05/15/2011",
         "TIME:14:41:50",
         "CALL:CO ALARM:NO ILLNESS",
         "CITY:FRANKLIN TWP",
         "ADDR:25 MAYNARD ROAD");
 
     doTest("T6",
         "[CAD]  [CAD] EFD-FD:11052545:05/15/2011 21:22:21:MVC: FRANKLI-CASTLETON AVE & EASTON AVE",
         "SRC:EFD-FD",
         "ID:11052545",
         "DATE:05/15/2011",
         "TIME:21:22:21",
         "CALL:MVC",
         "CITY:FRANKLIN TWP",
         "ADDR:CASTLETON AVE & EASTON AVE");
 
     doTest("T7",
         "[CAD]  [CAD] EFD-FD:11052638:05/16/2011 04:39:12:FIRE ALARM:Z-GENERAL: FRANKLI-THE DEN / 700 HAMILTON ST",
         "SRC:EFD-FD",
         "ID:11052638",
         "DATE:05/16/2011",
         "TIME:04:39:12",
         "CALL:FIRE ALARM:Z-GENERAL",
         "CITY:FRANKLIN TWP",
         "PLACE:THE DEN",
         "ADDR:700 HAMILTON ST");
 
     doTest("T8",
         "[CAD]  [CAD] EFD-FD:11053024:05/16/2011 19:33:38:STRUCTURE FIRE: FRANKLI-12 NEWKIRK ROAD",
         "SRC:EFD-FD",
         "ID:11053024",
         "DATE:05/16/2011",
         "TIME:19:33:38",
         "CALL:STRUCTURE FIRE",
         "CITY:FRANKLIN TWP",
         "ADDR:12 NEWKIRK ROAD");
 
     doTest("T9",
         "[CAD]  [CAD] EFD-FD:11053250:05/17/2011 11:56:48:FIRE ALARM:Z/GEN: FRANKLI-RETA - RENAISSANCE EMPLOYMENT / 630 FRANKLIN BLVD",
         "SRC:EFD-FD",
         "ID:11053250",
         "DATE:05/17/2011",
         "TIME:11:56:48",
         "CALL:FIRE ALARM:Z/GEN",
         "CITY:FRANKLIN TWP",
         "PLACE:RETA - RENAISSANCE EMPLOYMENT",
         "ADDR:630 FRANKLIN BLVD");
 
     doTest("T10",
         "[CAD]  [CAD] EFD-FD:11053657:05/18/2011 13:30:59:STOVE/OVEN/FOOD: FRANKLI-138 ELLEN ST",
         "SRC:EFD-FD",
         "ID:11053657",
         "DATE:05/18/2011",
         "TIME:13:30:59",
         "CALL:STOVE/OVEN/FOOD",
         "CITY:FRANKLIN TWP",
         "ADDR:138 ELLEN ST");
   }
   
   @Test
   public void testParser2() {
 
     doTest("T1",
         "MGT-FD:11084743:07/25/2011 10:51:36:FIRE ALARM:ZONE/ GENERAL: MONTGOM-MONTGOMERY EVANGELICAL CHURCH / 246 BELLE MEAD GRIGGSTOWN ROAD",
         "SRC:MGT-FD",
         "ID:11084743",
         "DATE:07/25/2011",
         "TIME:10:51:36",
         "CALL:FIRE ALARM:ZONE/ GENERAL",
         "CITY:MONTGOMERY TWP",
         "PLACE:MONTGOMERY EVANGELICAL CHURCH",
         "ADDR:246 BELLE MEAD GRIGGSTOWN ROAD");
 
     doTest("T2",
         "GRG-FD:11087510:07/31/2011 13:19:31:FIRE ALARM:GENERAL: FRANKLI-GRIGGSTOWN GROUP HOME / 7 HONEYMAN ST",
         "SRC:GRG-FD",
         "ID:11087510",
         "DATE:07/31/2011",
         "TIME:13:19:31",
         "CALL:FIRE ALARM:GENERAL",
         "CITY:FRANKLIN TWP",
         "PLACE:GRIGGSTOWN GROUP HOME",
         "ADDR:7 HONEYMAN ST");
 
     doTest("T3",
         "MGT-FD:11084928:07/25/2011 17:32:48:SMOKE CONDITION: MONTGOM-952 COUNTY ROUTE 518 HWY",
         "SRC:MGT-FD",
         "ID:11084928",
         "DATE:07/25/2011",
         "TIME:17:32:48",
         "CALL:SMOKE CONDITION",
         "CITY:MONTGOMERY TWP",
         "ADDR:952 CO 518");
 
     doTest("T4",
         "GRG-FD:11088457:08/02/2011 15:39:21:MV FIRE:SMOKE ONLY: FRANKLI-12 BARBIERI CT",
         "SRC:GRG-FD",
         "ID:11088457",
         "DATE:08/02/2011",
         "TIME:15:39:21",
         "CALL:MV FIRE:SMOKE ONLY",
         "CITY:FRANKLIN TWP",
         "ADDR:12 BARBIERI CT");
 
     doTest("T5",
         "MGT-FD:11088557:08/02/2011 18:37:19:BRUSH FIRE: MONTGOM-SPRING HILL ROAD",
         "SRC:MGT-FD",
         "ID:11088557",
         "DATE:08/02/2011",
         "TIME:18:37:19",
         "CALL:BRUSH FIRE",
         "CITY:MONTGOMERY TWP",
         "ADDR:SPRING HILL ROAD");
 
     doTest("T6",
         "MGT-FD:11088946:08/03/2011 16:09:20:FIRE ALARM:SMOKE DECTORS: MONTGOM-3 LOWRY CT",
         "SRC:MGT-FD",
         "ID:11088946",
         "DATE:08/03/2011",
         "TIME:16:09:20",
         "CALL:FIRE ALARM:SMOKE DECTORS",
         "CITY:MONTGOMERY TWP",
         "ADDR:3 LOWRY CT");
 
     doTest("T7",
         "GRG-FD:11089016:08/03/2011 17:54:34:FIRE ALARM:FIRST FL SMK DECTOR: FRANKLI-46 SAPPHIRE LANE",
         "SRC:GRG-FD",
         "ID:11089016",
         "DATE:08/03/2011",
         "TIME:17:54:34",
         "CALL:FIRE ALARM:FIRST FL SMK DECTOR",
         "CITY:FRANKLIN TWP",
         "ADDR:46 SAPPHIRE LANE");
 
     doTest("T8",
         "MGT-FD:11089091:08/03/2011 21:11:26:FIRE ALARM:ZONE / 363: MONTGOM-STONEBRIDGE AT MONTGOMERY / 100 HOLLINSHEAD SPRING ROAD",
         "SRC:MGT-FD",
         "ID:11089091",
         "DATE:08/03/2011",
         "TIME:21:11:26",
         "CALL:FIRE ALARM:ZONE / 363",
         "CITY:MONTGOMERY TWP",
         "PLACE:STONEBRIDGE AT MONTGOMERY",
         "ADDR:100 HOLLINSHEAD SPRING ROAD");
 
     doTest("T9",
         "GRG-FD:11090214:08/06/2011 16:56:13:FIRE ALARM:SMOKE DETECTOR: FRANKLI-27 KIRBY LANE",
         "SRC:GRG-FD",
         "ID:11090214",
         "DATE:08/06/2011",
         "TIME:16:56:13",
         "CALL:FIRE ALARM:SMOKE DETECTOR",
         "CITY:FRANKLIN TWP",
         "ADDR:27 KIRBY LANE");
 
     doTest("T10",
         "GRG-FD:11090292:08/06/2011 21:34:50:FIRE ALARM:GENERAL: FRANKLI-3333 STATE HWY NO 27 HWY",
         "SRC:GRG-FD",
         "ID:11090292",
         "DATE:08/06/2011",
         "TIME:21:34:50",
         "CALL:FIRE ALARM:GENERAL",
         "CITY:FRANKLIN TWP",
         "ADDR:3333 ST 27");
 
     doTest("T11",
         "GRG-FD:11079916:07/14/2011 17:36:09:MVC:V POLE: FRANKLI-BUTLER ROAD & S MIDDLEBUSH ROAD",
         "SRC:GRG-FD",
         "ID:11079916",
         "DATE:07/14/2011",
         "TIME:17:36:09",
         "CALL:MVC:V POLE",
         "CITY:FRANKLIN TWP",
         "ADDR:BUTLER ROAD & S MIDDLEBUSH ROAD");
    
   }
   
   @Test
   public void testSamLambertz() {
 
     doTest("T1",
         "*3898: *messaging@iamresponding.com / 77 RESCUE / kharju:SBB-RS:12029633 :03/01/2012 23:53:12:SICK PERSON:22 YOM: SOUTH B-SOUTHSIDE GRILL / 2 MAIN ST #10",
         "SRC:SBB-RS",
         "ID:12029633",
         "DATE:03/01/2012",
         "TIME:23:53:12",
         "CALL:SICK PERSON:22 YOM",
         "CITY:SOUTH BRUNSWICK",
         "PLACE:SOUTHSIDE GRILL",
         "ADDR:2 MAIN ST",
         "APT:10");
 
     doTest("T2",
         "*4101: *ghargreave@co.somerset.nj.us /  / SBB-FD:12027787:02/27/2012 11:58:34:FIRE: SOUTH B-SOUTHSIDE GRILL / 2 MAIN ST",
         "SRC:SBB-FD",
         "ID:12027787",
         "DATE:02/27/2012",
         "TIME:11:58:34",
         "CALL:FIRE",
         "CITY:SOUTH BRUNSWICK",
         "PLACE:SOUTHSIDE GRILL",
         "ADDR:2 MAIN ST");

    doTest("T3",
        "  / SBB-FD:12033968:03/11/2012 10:49:03:STRUCTURE FIRE: BOUND B-547 WHEATLAND AVE\n",
        "SRC:SBB-FD",
        "ID:12033968",
        "DATE:03/11/2012",
        "TIME:10:49:03",
        "CALL:STRUCTURE FIRE",
        "CITY:BOUND BROOK",
        "ADDR:547 WHEATLAND AVE");
   }
   
   
   public static void main(String[] args) {
     new NJSomersetCountyParserTest().generateTests("T1", "SRC ID DATE TIME CALL CITY PLACE ADDR APT INFO");
   }
 }
