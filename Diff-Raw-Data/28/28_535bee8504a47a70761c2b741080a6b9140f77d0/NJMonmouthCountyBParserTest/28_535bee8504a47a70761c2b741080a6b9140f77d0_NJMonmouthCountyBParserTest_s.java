 package net.anei.cadpage.parsers.NJ;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class NJMonmouthCountyBParserTest extends BaseParserTest {
   
   public NJMonmouthCountyBParserTest() {
     setParser(new NJMonmouthCountyBParser(), "MONMOUTH COUNTY", "NJ");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "(mCAD) [!] FIRA F FIRE ALARM | 21 LASATTA AV | X-ST: | BULIDING 700//SPRINKLER ROOM SMOKE DETECTOR | 08:22:17 | 02/17/2012",
         "CALL:FIRA F FIRE ALARM",
         "ADDR:21 LASATTA AV",
         "MADDR:21 LASATTA AVE",
         "INFO:BULIDING 700//SPRINKLER ROOM SMOKE DETECTOR",
         "TIME:08:22:17",
         "DATE:02/17/2012");
 
     doTest("T2",
         "(mCAD) [!] FIRA F FIRE ALARM | 4189 N RT 9 | X-ST: RT 522/SCHIBANOFF RD | BUSN: BROCK FARMS // ZONE: GENERAL | 15:30:16 | 01/26/2012",
         "CALL:FIRA F FIRE ALARM",
         "ADDR:4189 N RT 9",
         "X:RT 522/SCHIBANOFF RD",
         "INFO:BUSN: BROCK FARMS // ZONE: GENERAL",
         "TIME:15:30:16",
         "DATE:01/26/2012");
 
     doTest("T3",
         "(mCAD) [!] FIRA F FIRE ALARM | 124 SHINNECOCK DR | X-ST: | LEFT POT ON STOVE//HOUSE FILLED W/SMOKE | 15:31:14 | 01/26/2012",
         "CALL:FIRA F FIRE ALARM",
         "ADDR:124 SHINNECOCK DR",
         "INFO:LEFT POT ON STOVE//HOUSE FILLED W/SMOKE",
         "TIME:15:31:14",
         "DATE:01/26/2012");
 
     doTest("T4",
         "(mCAD) [!] FIRS F FIRE STRUC | 15 APPLE BLOSSOM LN BLOSSUM | X-ST: THOMPSON GROVE RD/ | FIRE IN GARAGE | 21:22:19 | 01/26/2012",
         "CALL:FIRS F FIRE STRUC",
         "ADDR:15 APPLE BLOSSOM LN BLOSSUM",
         "X:THOMPSON GROVE RD",
         "INFO:FIRE IN GARAGE",
         "TIME:21:22:19",
         "DATE:01/26/2012");
 
     doTest("T5",
         "(mCAD) [!] FIRS F FIRE STRUC | AVALON LA | X-ST: SPRINGHOUSE CI/ | ACROSS FROM 13 NO HOUSE NUMBER//SEES FLAMES | 18:59:46 | 01/27/2012",
         "CALL:FIRS F FIRE STRUC",
         "ADDR:AVALON LA",
         "MADDR:AVALON LN & SPRINGHOUSE CIR",
         "X:SPRINGHOUSE CI",
         "INFO:ACROSS FROM 13 NO HOUSE NUMBER//SEES FLAMES",
         "TIME:18:59:46",
         "DATE:01/27/2012");
 
     doTest("T6",
         "(mCAD) [!] FIRS F FIRE STRUC | 18 KINNEY RD | X-ST: SWEETMANS LN/ | SMOKE COMING OUT OF WALL OVEN... | 18:18:33 | 01/28/2012",
         "CALL:FIRS F FIRE STRUC",
         "ADDR:18 KINNEY RD",
         "X:SWEETMANS LN",
         "INFO:SMOKE COMING OUT OF WALL OVEN...",
         "TIME:18:18:33",
         "DATE:01/28/2012");
 
     doTest("T7",
         "(mCAD) [!] FIRW F FIRE WOODS | 47 LASATTA AV | X-ST: ENGLISH CLUB DR/CARRIAGE LN | 6X6 PIT UNATTENDED BURNING IN THE REAR OF THE | 14:30:44 | 01/31/2012",
         "CALL:FIRW F FIRE WOODS",
         "ADDR:47 LASATTA AV",
         "MADDR:47 LASATTA AVE",
         "X:ENGLISH CLUB DR/CARRIAGE LN",
         "INFO:6X6 PIT UNATTENDED BURNING IN THE REAR OF THE",
         "TIME:14:30:44",
         "DATE:01/31/2012");
 
     doTest("T8",
         "(mCAD) [!] MVAF F MVA FIRE | FREEHOLD RD | X-ST: | PU TRUCK VS PU TRUCK/TELPHONE POLE AND WIRES | 13:22:55 | 02/02/2012",
         "CALL:MVAF F MVA FIRE",
         "ADDR:FREEHOLD RD",
         "INFO:PU TRUCK VS PU TRUCK/TELPHONE POLE AND WIRES",
         "TIME:13:22:55",
         "DATE:02/02/2012");
 
     doTest("T9",
         "(mCAD) [!] FIRA F FIRE ALARM | 49 LASATTA AV | X-ST: ENGLISH CLUB DR/CARRIAGE LN | BRANDYWINE/ZONE 2 GENERAL/ATT KEY | 10:41:21 | 02/05/2012",
         "CALL:FIRA F FIRE ALARM",
         "ADDR:49 LASATTA AV",
         "MADDR:49 LASATTA AVE",
         "X:ENGLISH CLUB DR/CARRIAGE LN",
         "INFO:BRANDYWINE/ZONE 2 GENERAL/ATT KEY",
         "TIME:10:41:21",
         "DATE:02/05/2012");
 
     doTest("T10",
         "(mCAD) [!] AIDF F AID-F | 5 SWEETMANS LN | X-ST: BORDER-MILLSTONE TWP/ST HWY 33 W | 6 HAVILAND DR .32 ODOR OF SMOKE IN RESD | 06:05:10 | 02/07/2012",
         "CALL:AIDF F AID-F",
         "ADDR:5 SWEETMANS LN",
         "X:BORDER-MILLSTONE TWP/ST HWY 33 W",
         "INFO:6 HAVILAND DR .32 ODOR OF SMOKE IN RESD",
         "TIME:06:05:10",
         "DATE:02/07/2012");
 
     doTest("T11",
         "(mCAD) [!] MVFA F MVA FD/ALS | 232 SMITHBURG-527A RD | X-ST: ROBERTS RD/GRANDVIEW LN | CURVE NEAR CHURCH/2 VEH/ONE VEH WAS ON FIRE | 17:08:21 | 02/08/2012",
         "CALL:MVFA F MVA FD/ALS",
         "ADDR:232 SMITHBURG-527A RD",
         "X:ROBERTS RD/GRANDVIEW LN",
         "INFO:CURVE NEAR CHURCH/2 VEH/ONE VEH WAS ON FIRE",
         "TIME:17:08:21",
         "DATE:02/08/2012");
 
     doTest("T12",
         "(mCAD) [!] FIRW F FIRE WOODS | 71 NEW BEGINNINGS CG | X-ST: TRACY STATION RD/GENESIS ST | KIDS AROUND A FIRE IN THE WOODS | 16:51:25 | 02/09/2012",
         "CALL:FIRW F FIRE WOODS",
         "ADDR:71 NEW BEGINNINGS CG",
         "MADDR:71 NEW BEGINNINGS CROSSING",
         "X:TRACY STATION RD/GENESIS ST",
         "INFO:KIDS AROUND A FIRE IN THE WOODS",
         "TIME:16:51:25",
         "DATE:02/09/2012");
 
     doTest("T13",
         "(mCAD) [!] FIRA F FIRE ALARM | 47 WILD TURKEY WY | X-ST: UNION HILL RD/ | APT D FIRE ALARM IN APT STILL GOING OFF PER C | 02:22:16 | 02/10/2012",
         "CALL:FIRA F FIRE ALARM",
         "ADDR:47 WILD TURKEY WY",
         "X:UNION HILL RD",
         "INFO:APT D FIRE ALARM IN APT STILL GOING OFF PER C",
         "TIME:02:22:16",
         "DATE:02/10/2012");
 
     doTest("T14",
         "(mCAD) [!] GAS F GAS | 628 ST ANDREWS PL | X-ST: SAWGRASS DR/ | SMELLS GAS IN THE HALLWAY OF HIS RESIDENCE | 21:06:37 | 02/10/2012",
         "CALL:GAS F GAS",
         "ADDR:628 ST ANDREWS PL",
         "X:SAWGRASS DR",
         "INFO:SMELLS GAS IN THE HALLWAY OF HIS RESIDENCE",
         "TIME:21:06:37",
         "DATE:02/10/2012");
 
     doTest("T15",
         "(mCAD) [!] FIRA F FIRE ALARM | 116 MILLHURST RD | X-ST: MAIN ST/STATION ST | COMMERCIAL-MANALAPAN ENGLISHTOWN BOARD OF ED | 11:28:04 | 02/14/2012",
         "CALL:FIRA F FIRE ALARM",
         "ADDR:116 MILLHURST RD",
         "X:MAIN ST/STATION ST",
         "INFO:COMMERCIAL-MANALAPAN ENGLISHTOWN BOARD OF ED",
         "TIME:11:28:04",
         "DATE:02/14/2012");
 
     doTest("T16",
         "(mCAD) [!] CRBA F CARB MONOX | 5 TURTLE HOLLOW DR | X-ST: KINNEY RD/ | CO ALARM SOUNDING//NO SYMPTOMS//EVACUATING | 09:34:25 | 02/15/2012",
         "CALL:CRBA F CARB MONOX",
         "ADDR:5 TURTLE HOLLOW DR",
         "X:KINNEY RD",
         "INFO:CO ALARM SOUNDING//NO SYMPTOMS//EVACUATING",
         "TIME:09:34:25",
         "DATE:02/15/2012");
 
     doTest("T17",
         "(mCAD) [!] FIRA F FIRE ALARM | 21 LASATTA AV | X-ST: WATER ST/CARRIAGE LN | BULIDING 700//SPRINKLER ROOM SMOKE DETECTOR | 08:22:17 | 02/17/2012",
         "CALL:FIRA F FIRE ALARM",
         "ADDR:21 LASATTA AV",
         "MADDR:21 LASATTA AVE",
         "X:WATER ST/CARRIAGE LN",
         "INFO:BULIDING 700//SPRINKLER ROOM SMOKE DETECTOR",
         "TIME:08:22:17",
         "DATE:02/17/2012");
 
     doTest("T18",
         "(eCAD) [!] FIRA F FIRE ALARM | 10 DANA CT | X-ST: | GENERAL FIRE ALARM//OP#3499 | 10:51:46 | 04/05/2012",
         "CALL:FIRA F FIRE ALARM",
         "ADDR:10 DANA CT",
         "INFO:GENERAL FIRE ALARM//OP#3499",
         "TIME:10:51:46",
         "DATE:04/05/2012");
     
    doTest("T19",
         "[!] GAS F GAS | 1 STATION ST | X-ST: IRON ORE RD/ | AT REX LUMBER-GAS LINE STRUCK | 09:00:43 | 08/08/2012",
         "CALL:GAS F GAS",
         "ADDR:1 STATION ST",
         "X:IRON ORE RD",
         "INFO:AT REX LUMBER-GAS LINE STRUCK",
         "TIME:09:00:43",
         "DATE:08/08/2012");
 
    doTest("T20",
         "[!] FIRA F FIRE ALARM | 151 ST HWY 33 | X-ST: | NEXT GEN 732 617 9300 ZONE WATER FLOW | 07:56:50 | 08/07/2012",
         "CALL:FIRA F FIRE ALARM",
         "ADDR:151 ST HWY 33",
         "MADDR:151 NJ 33",
         "INFO:NEXT GEN 732 617 9300 ZONE WATER FLOW",
         "TIME:07:56:50",
         "DATE:08/07/2012");
 
    doTest("T21",
         "[!] FIRA F FIRE ALARM | 151 ST HWY 33 | X-ST: MILLHURST RD/WOODWARD RD | NEXT GEN 732 617 9300 ZONE WATER FLOW | 07:56:50 | 08/07/2012",
         "CALL:FIRA F FIRE ALARM",
         "ADDR:151 ST HWY 33",
         "MADDR:151 NJ 33",
         "X:MILLHURST RD/WOODWARD RD",
         "INFO:NEXT GEN 732 617 9300 ZONE WATER FLOW",
         "TIME:07:56:50",
         "DATE:08/07/2012");
 
    doTest("T22",
         "[!] MVFA F MVA FD/ALS | 120 ST HWY 33 | X-ST: | 2 CAR MVA - NO INJURIES | 14:22:52 | 08/06/2012",
         "CALL:MVFA F MVA FD/ALS",
         "ADDR:120 ST HWY 33",
         "MADDR:120 NJ 33",
         "INFO:2 CAR MVA - NO INJURIES",
         "TIME:14:22:52",
         "DATE:08/06/2012");
 
    doTest("T23",
         "[!] MVFA F MVA FD/ALS | 120 ST HWY 33 | X-ST: RT 33 BYPASS/MILLHURST RD | 2 CAR MVA - NO INJURIES | 14:22:52 | 08/06/2012",
         "CALL:MVFA F MVA FD/ALS",
         "ADDR:120 ST HWY 33",
         "MADDR:120 NJ 33",
         "X:RT 33 BYPASS/MILLHURST RD",
         "INFO:2 CAR MVA - NO INJURIES",
         "TIME:14:22:52",
         "DATE:08/06/2012");
 
    doTest("T24",
         "[!] FIRS F FIRE STRUC | 23 ST ANDREWS PL | X-ST: | THIRD PARTY CALLER/HOMEOWNER CONTACTED SAYING | 10:18:50 | 08/04/2012",
         "CALL:FIRS F FIRE STRUC",
         "ADDR:23 ST ANDREWS PL",
         "INFO:THIRD PARTY CALLER/HOMEOWNER CONTACTED SAYING",
         "TIME:10:18:50",
         "DATE:08/04/2012");
 
    doTest("T25",
         "[!] FIRA F FIRE ALARM | 450 TENNENT RD | X-ST: | OLD TENENT CHURCH- GENERAL FIRE ALARM | 10:05:07 | 08/04/2012",
         "CALL:FIRA F FIRE ALARM",
         "ADDR:450 TENNENT RD",
         "INFO:OLD TENENT CHURCH- GENERAL FIRE ALARM",
         "TIME:10:05:07",
         "DATE:08/04/2012");
 
    doTest("T26",
         "[!] FIRA F FIRE ALARM | 450 TENNENT RD | X-ST: FREEHOLD RD/CRAIG RD | OLD TENENT CHURCH- GENERAL FIRE ALARM | 10:05:07 | 08/04/2012",
         "CALL:FIRA F FIRE ALARM",
         "ADDR:450 TENNENT RD",
         "X:FREEHOLD RD/CRAIG RD",
         "INFO:OLD TENENT CHURCH- GENERAL FIRE ALARM",
         "TIME:10:05:07",
         "DATE:08/04/2012");
 
   }
   
   public static void main(String[] args) {
     new NJMonmouthCountyBParserTest().generateTests("T1");
   }
 }
