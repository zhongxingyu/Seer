 package net.anei.cadpage.parsers.IL;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class ILWoodfordCountyParserTest extends BaseParserTest {
   
   public ILWoodfordCountyParserTest() {
     setParser(new ILWoodfordCountyParser(), "WOODFORD COUNTY", "IL");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "(Woodford Comm) 11-022625 ODOR OF NATURAL GAS\n411 W FRONT ST\nN N PL /  N GREEN ST/\nCASEYS OF ROANOKE  ROANOKE\n1V14 ROFD WF27",
         "ID:11-022625",
         "CALL:ODOR OF NATURAL GAS",
         "ADDR:411 W FRONT ST",
         "X:N N PL /  N GREEN ST/",
         "PLACE:CASEYS OF ROANOKE",
         "CITY:ROANOKE",
         "UNIT:1V14 ROFD WF27");
 
     doTest("T2",
         "[Woodford Comm]  11-015377 FIRE\n1936 1400N\n1900E / 2000E/\nROANOKE\n1V24 ROFD",
         "ID:11-015377",
         "CALL:FIRE",
         "ADDR:1936 1400N",
         "X:1900E / 2000E/",
         "CITY:ROANOKE",
         "UNIT:1V24 ROFD");
 
     doTest("T3",
         "[Woodford Comm]  11-014297 AMBULANCE CALL\n304 N FRANKLIN ST\nW DAVISON ST / W BRO/AD ST\nROANOKE\n1V24 ROFD",
         "ID:11-014297",
         "CALL:AMBULANCE CALL",
         "ADDR:304 N FRANKLIN ST",
         "X:W DAVISON ST / W BRO/AD ST",
         "CITY:ROANOKE",
         "UNIT:1V24 ROFD");
 
     doTest("T4",
         "[Woodford Comm]  11-006970 ACCIDENT-WITH INJURY\n1400N/ 1900E\nROANOKE\n1V24 ROFD WF16 WF22 WF4",
         "ID:11-006970",
         "CALL:ACCIDENT-WITH INJURY",
         "ADDR:1400N & 1900E",
         "CITY:ROANOKE",
         "UNIT:1V24 ROFD WF16 WF22 WF4");
 
     doTest("T5",
         "(Woodford Comm) 10-035758 FIRE ALARM\n1797 1700E\n1700N / 1800N/\nSPAFAS INC  WASHBURN FARM  ROANOKE\n1V23 1V24 MEFD ROFD",
         "ID:10-035758",
         "CALL:FIRE ALARM",
         "ADDR:1797 1700E",
         "X:1700N / 1800N/",
         "PLACE:SPAFAS INC  WASHBURN FARM",
         "CITY:ROANOKE",
         "UNIT:1V23 1V24 MEFD ROFD");
 
     doTest("T6",
         "[Woodford Comm]  11-000116 ACCIDENT-PROPERTY DAMAGE\n217 W HUSSEMAN ST\nN JEFFERSON ST /  W/RANDOLPH ST\nGEISER FORD  ROANOKE\nROFD WF18",
         "ID:11-000116",
         "CALL:ACCIDENT-PROPERTY DAMAGE",
         "ADDR:217 W HUSSEMAN ST",
         "X:N JEFFERSON ST /  W/RANDOLPH ST",
         "PLACE:GEISER FORD",
         "CITY:ROANOKE",
         "UNIT:ROFD WF18");
 
     doTest("T7",
         "[Woodford Comm]  11-001634 911 LINES DOWN\n115 E COURT ST\nN MAIN ST /  DEAD EN/D EAST\nWOODCOM  EUREKA\nMIFD ROFD SBFD WAFD",
         "ID:11-001634",
         "CALL:911 LINES DOWN",
         "ADDR:115 E COURT ST",
         "X:N MAIN ST /  DEAD EN/D EAST",
         "PLACE:WOODCOM",
         "CITY:EUREKA",
         "UNIT:MIFD ROFD SBFD WAFD");
 
     doTest("T8",
         "[Woodford Comm]  11-005470 ACCIDENT INJURY UNKNOWN\n1386 STATE ROUTE 117\n1300N /  116/\nPARSONS COMPANY  ROANOKE\n1V24 ROFD WF8\n",
         "ID:11-005470",
         "CALL:ACCIDENT INJURY UNKNOWN",
         "ADDR:1386 STATE ROUTE 117",
         "MADDR:1386 STATE 117",
         "X:1300N /  116/",
         "PLACE:PARSONS COMPANY",
         "CITY:ROANOKE",
         "UNIT:1V24 ROFD WF8");

    doTest("T9",
        "(Woodford Comm) 12-002663 FIRE\n1248 1700E\n1200N / 1300N/\nROANOKE\n1V24 ROFD",
        "ID:12-002663",
        "CALL:FIRE",
        "ADDR:1248 1700E",
        "X:1200N / 1300N/",
        "CITY:ROANOKE",
        "UNIT:1V24 ROFD");
    
   }
   
   public static void main(String[] args) {
     new ILWoodfordCountyParserTest().generateTests("T1");
   }
 }
