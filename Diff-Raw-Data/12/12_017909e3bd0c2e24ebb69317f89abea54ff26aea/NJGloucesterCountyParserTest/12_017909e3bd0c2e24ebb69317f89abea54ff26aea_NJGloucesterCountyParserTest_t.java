 package net.anei.cadpage.parsers.NJ;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 import net.anei.cadpage.parsers.NJ.NJGloucesterCountyParser;
 
 import org.junit.Test;
 
 
 public class NJGloucesterCountyParserTest extends BaseParserTest {
   
   public NJGloucesterCountyParserTest() {
     setParser(new NJGloucesterCountyParser(), "GLOUCESTER COUNTY", "NJ");
   }
   
   @Test
   public void testParser1() {
 
     doTest("T1",
         "Sta:23-1\nType:MVC\nLoc:233 N MAIN ST\nVen:HARRISON\nDsp-07:56:31\nClr-",
         "SRC:23-1",
         "CALL:MVC",
         "ADDR:233 N MAIN ST",
         "CITY:HARRISON TWP");
 
     doTest("T2",
         "Sta:23-1\nType:SEIZ\nLoc:143 SALVATORE DR\nVen:HARRISON\nDsp-15:26:16\nClr-",
         "SRC:23-1",
         "CALL:SEIZ",
         "ADDR:143 SALVATORE DR",
         "CITY:HARRISON TWP");
 
     doTest("T3",
         "Sta:23-1\nType:DWEL\nLoc:296 WESTBROOK DR\nVen:WOOLWICH\nDsp-08:16:44\nClr-",
         "SRC:23-1",
         "CALL:DWEL",
         "ADDR:296 WESTBROOK DR",
         "CITY:WOOLWICH TWP");
 
     doTest("T4",
         "Sta:23-1\nType:MVC\nLoc:203 N MAIN ST\nVen:HARRISON\nDsp-09:34:52\nClr-",
         "SRC:23-1",
         "CALL:MVC",
         "ADDR:203 N MAIN ST",
         "CITY:HARRISON TWP");
 
     doTest("T5",
         "Sta:23-1\nType:MVC\nLoc:203 N MAIN ST\nVen:HARRISON\nDsp-09:34:52\nClr-",
         "SRC:23-1",
         "CALL:MVC",
         "ADDR:203 N MAIN ST",
         "CITY:HARRISON TWP");
 
     doTest("T6",
         "Sta:23-1\nType:ALRM\nLoc:7 FERRELL RD APT A\nVen:S\nDsp-14:55:22\nClr-",
         "SRC:23-1",
         "CALL:ALRM",
         "ADDR:7 FERRELL RD",
         "APT:A",
         "CITY:S HARRISON TWP");
 
     doTest("T7",
         "Sta:23-1\nType:BRSH\nLoc:54 VESTRY RD\nVen:S\nDsp-12:40:42\nClr-",
         "SRC:23-1",
         "CALL:BRSH",
         "ADDR:54 VESTRY RD",
         "CITY:S HARRISON TWP");
 
     doTest("T8",
         "Sta:23-1\nType:FALL\nLoc:115 FRANKLIN DR\nVen:HARRISON\nDsp-16:01:31\nClr-",
         "SRC:23-1",
         "CALL:FALL",
         "ADDR:115 FRANKLIN DR",
         "CITY:HARRISON TWP");
 
     doTest("T9",
         "Sta:23-1\nType:BRSH\nLoc:49 TURTLE CREEK DR\nVen:HARRISON\nDsp-\nClr-",
         "SRC:23-1",
         "CALL:BRSH",
         "ADDR:49 TURTLE CREEK DR",
         "CITY:HARRISON TWP");
 
     doTest("T10",
         "Sta:23-1\nType:MVC\nLoc:2227 RT 322\nVen:WOOLWICH\nDsp-11:25:11\nClr-",
         "SRC:23-1",
         "CALL:MVC",
         "ADDR:2227 RT 322",
         "CITY:WOOLWICH TWP");
 
     doTest("T11",
         "Sta:23-1\nType:MVC\nLoc:TOMLIN STATION RD\nVen:HARRISON\nDsp-17:31:56\nClr-",
         "SRC:23-1",
         "CALL:MVC",
         "ADDR:TOMLIN STATION RD",
         "CITY:HARRISON TWP");
   }
   
   @Test
   public void testParser2() {
 
     doTest("T1",
         "(#265339) Dispatch\n\nSta:8-1\nType:MVCC\nLoc:HESSIAN AVE\nVen:NATNL\nDsp-17:33:46\nClr-",
         "SRC:8-1",
         "CALL:MVCC",
         "ADDR:HESSIAN AVE",
         "CITY:NATIONAL PARK");
 
     doTest("T2",
         "(#264149) Dispatch\n\nSta:8-1\nType:DWEL\nLoc:817 ASBURY AVE\nVen:NATNL\nDsp-\nClr-",
         "SRC:8-1",
         "CALL:DWEL",
         "ADDR:817 ASBURY AVE",
         "CITY:NATIONAL PARK");
 
     doTest("T3",
         "(#266773) Dispatch\n\nSta:8-1\nType:ALRM\nLoc:518 SIMPSON AVE\nVen:NATNL\nDsp-\nClr-",
         "SRC:8-1",
         "CALL:ALRM",
         "ADDR:518 SIMPSON AVE",
         "CITY:NATIONAL PARK");
   }
   
   @Test
   public void testParser3() {
 
     doTest("T1",
         "Subject:1/2\n\nDispatch\n\nSta:43-2\n\nType:SERV\n\nLoc:751 LINCOLN AVE               \n\nVen:FRANKLIN\n\nDsp-17:28:02\5s\n\nClr-",
         "SRC:43-2",
         "CALL:SERV",
         "ADDR:751 LINCOLN AVE",
         "CITY:FRANKLIN TWP");
 
   }
  
   @Test
   public void testParser4() {
 
     doTest("T1",
         "Dispatch\nSta:28-3\nType:ALRM\nLoc:158 KENTON AVE                \nVen:PITMAN\nDsp-\nClr-",
         "SRC:28-3",
         "CALL:ALRM",
         "ADDR:158 KENTON AVE",
         "CITY:PITMAN");
 
   }
   
  @Test
  public void testParser5() {

    doTest("T1",
        "COUNTY OF GLOUCESTER (#353519) Dispatch\n\nSta:10-2\nType:ALRM\nLoc:5 BELMONT BLVD\\                \nVen:WASHINGTON\nDsp-\nClr- STOP",
        "SRC:10-2",
        "CALL:ALRM",
        "ADDR:5 BELMONT BLVD",
        "CITY:WASHINGTON TWP");

  }
  
   public static void main(String[] args) {
     new NJGloucesterCountyParserTest().generateTests("T1", "SRC CALL ADDR APT CITY");
   }
 }
