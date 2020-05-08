 package net.anei.cadpage.parsers.CA;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class CAShastaCountyParserTest extends BaseParserTest {
   
   public CAShastaCountyParserTest() {
     setParser(new CAShastaCountyParser(), "SHASTA COUNTY", "CA");
   }
   
   @Test
   public void testParser() {
     
     doTest("T1",
         "(CAD Page) FIRE, SMOKE CHECK; 21199 HWY 299 E ,BELLAVISTA ; ; 11764 BLK REBECCA LN; Map: 3225; Inc# 009012; VOL33; neighbor burning",
         "CALL:FIRE, SMOKE CHECK",
         "ADDR:21199 HWY 299 E",
         "SRC:BELLAVISTA",
         "X:11764 BLK REBECCA LN",
         "MAP:3225",
         "ID:009012",
         "UNIT:VOL33",
         "INFO:neighbor burning");
 
     doTest("T2",
         "(CAD Page) MED, TRAFFIC COLLISION; 21044 HWY 299 E / 11760 NEAL LN ,BELLAVISTA ; ; ; Map: 3225; Inc# 009230; B2413 VOL33 E2462; VEH",
         "CALL:MED, TRAFFIC COLLISION",
         "ADDR:21044 HWY 299 E & 11760 NEAL LN",
         "MADDR:21044 HWY 299 E",
         "SRC:BELLAVISTA",
         "MAP:3225",
         "ID:009230",
         "UNIT:B2413 VOL33 E2462",
         "INFO:VEH");
 
     doTest("T3",
         "(CAD Page) MEDICAL; 21018BLK  JOANNA WY ,BELLAVISTA ; 21017 JOANNA; 11650 BLK HELENA DR; Map: 3225; Inc# 009041; VOL33; 69f, loc",
         "CALL:MEDICAL",
         "ADDR:21017 JOANNA",
         "SRC:BELLAVISTA",
         "X:11650 BLK HELENA DR & 21018BLK  JOANNA WY",
         "MAP:3225",
         "ID:009041",
         "UNIT:VOL33",
         "INFO:69f, loc");
         
     doTest("T4",
         "(CAD Page) MEDICAL; 9000 VISTA DEL SIERRA DR / 6378 PARK RIDGE DR ,STA43 ; 6433 VISTA DEL SIERRA DR; ; Map: 3675; Inc# 008927; VOL33",
         "CALL:MEDICAL",
         "ADDR:6433 VISTA DEL SIERRA DR",
         "SRC:STA43",
         "X:9000 VISTA DEL SIERRA DR / 6378 PARK RIDGE DR",
         "MAP:3675",
         "ID:008927",
         "UNIT:VOL33");
         
     doTest("T5",
         "(CAD Page) FIRE, VEH COMMERCIAL; 13575 WOODMAN LN ,BELLAVISTA ; ; 24101 BLK AJAX LN; Map: 3077; Inc# 008705; B2409 VOL33 WT33 E2485",
         "CALL:FIRE, VEH COMMERCIAL",
         "ADDR:13575 WOODMAN LN",
         "SRC:BELLAVISTA",
         "X:24101 BLK AJAX LN",
         "MAP:3077",
         "ID:008705",
         "UNIT:B2409 VOL33 WT33 E2485");
         
     doTest("T6",
         "(CAD Page) MEDICAL; 21940 OLD ALTURAS RD / 21480 SEVEN LAKES LN ,BELLAVISTA ; ; ; Map: 3225; Inc# 008684; VOL33; chrylsis charter sc",
         "CALL:MEDICAL",
         "ADDR:21940 OLD ALTURAS RD & 21480 SEVEN LAKES LN",
         "MADDR:21940 OLD ALTURAS RD",
         "SRC:BELLAVISTA",
         "MAP:3225",
         "ID:008684",
         "UNIT:VOL33",
         "INFO:chrylsis charter sc");
 
     doTest("T7",
         "(CAD Page) MEDICAL; ,BELLAVISTA ; 21376 FAITH;  DEAD-END; Map: 3225; Inc# 008945; VOL33 E2462; eld m poss stroke",
         "CALL:MEDICAL",
         "ADDR:21376 FAITH",
         "SRC:BELLAVISTA",
         "X:DEAD-END",
         "MAP:3225",
         "ID:008945",
         "UNIT:VOL33 E2462",
         "INFO:eld m poss stroke");
         
     doTest("T8",
         "(CAD Page) MEDICAL; 12169 KERN DR ,BELLAVISTA ; ; 21616 BLK KATHY LN; Map: 3225; Inc# 008957; VOL33 E2462; 68 f dxbr",
         "CALL:MEDICAL",
         "ADDR:12169 KERN DR",
         "SRC:BELLAVISTA",
         "X:21616 BLK KATHY LN",
         "MAP:3225",
         "ID:008957",
         "UNIT:VOL33 E2462",
         "INFO:68 f dxbr");
       
     doTest("T9",
         "(CAD Page) MEDICAL; 11894 BEST LN ,BELLAVISTA ; ;  DEAD-END; Map: 3225; Inc# 009298; VOL33 E2452; 91 YOM CHEST PAIN",
         "CALL:MEDICAL",
         "ADDR:11894 BEST LN",
         "SRC:BELLAVISTA",
         "X:DEAD-END",
         "MAP:3225",
         "ID:009298",
         "UNIT:VOL33 E2452",
         "INFO:91 YOM CHEST PAIN");
 
     doTest("T10",
         "FIRE, OTHER; 7600 BLACK BUTTE RD / 28502 HWY 44 ,SHINGLETOWN ; ; ; Map: 3678; Inc# 003825; E2460 VOL20 B2408; portable saw on fire.",
         "CALL:FIRE, OTHER",
         "ADDR:7600 BLACK BUTTE RD & 28502 HWY 44",
         "MADDR:7600 BLACK BUTTE RD",
         "SRC:SHINGLETOWN",
         "MAP:3678",
         "ID:003825",
         "UNIT:E2460 VOL20 B2408",
         "INFO:portable saw on fire.");
   }
   
   @Test
   public void testActive911() {
    
     doTest("T1",
        "(CAD Page) MEDICAL; 11633 SONORA TRL,SHASTACOLL ; ; 20818 BLK HWY 299 E; Map:3225; Inc# 001115;E2452 VOL33; 80 YOF GEN ILL",
         "CALL:MEDICAL",
         "ADDR:11633 SONORA TRL",
         "SRC:SHASTACOLL",
         "X:20818 BLK HWY 299 E",
         "MAP:225",
         "ID:001115",
         "UNIT:E2452 VOL33",
         "INFO:80 YOF GEN ILL");
   }
   
   public static void main(String[] args) {
    new CAShastaCountyParserTest().generateTests("T11", "CALL ADDR SRC X MAP ID UNIT INFO");
   }
 }
