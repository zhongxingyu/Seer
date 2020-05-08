 package net.anei.cadpage.parsers.TX;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class TXHarrisCountyESD1BParserTest extends BaseParserTest {
   
   public TXHarrisCountyESD1BParserTest() {
     setParser(new TXHarrisCountyESD1BParser(), "HARRIS COUNTY", "TX");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "ID#:11-06-20685 - - TRASH FIRE - 111 Bayou Dr - Apt:C - Bldg: - Key Map: 498D - Cross Streets:EAST FWY/MARKET - Box #:2002",
         "ID:11-06-20685",
         "CALL:TRASH FIRE",
         "ADDR:111 Bayou Dr",
         "APT:C",
         "MAP:498D",
         "X:EAST FWY/MARKET",
         "BOX:2002");
 
     doTest("T2",
         "ID#:11-06-20710 - - FIRE - PreAlert - 16217 Ridlon - Apt: - Bldg: intra SERVICES - Key Map: 498C - Cross Streets:SHEILA LN/WOODLAND D",
         "ID:11-06-20710",
         "CALL:FIRE - PreAlert",
         "ADDR:16217 Ridlon",
         "APT:intra SERVICES",
         "MAP:498C",
         "X:SHEILA LN/WOODLAND D");
 
     doTest("T3",
         "ID#:11-06-20658 - - 29A1 MOTOR VEHICLE A - East Fwy / Delldale - Apt: - Bldg: - Key Map: 498E - Cross Streets: - Box #:2004",
         "ID:11-06-20658",
         "CALL:29A1 MOTOR VEHICLE A",
         "ADDR:East Fwy & Delldale",
         "MAP:498E",
         "BOX:2004");
 
     doTest("T4",
         "ID#:11-06-20627 - - 29A1 MOTOR VEHICLE A - 16410 Ave D - Apt:1 - Bldg: AVE D APTS - Key Map: 498D - Cross Streets:WOODLAND DR/CEDAR L",
         "ID:11-06-20627",
         "CALL:29A1 MOTOR VEHICLE A",
         "ADDR:16410 Ave D",
         "APT:AVE D APTS-1",
         "MAP:498D",
         "X:WOODLAND DR/CEDAR L");
   }
   
   @Test
   public void testParser2() {
 
     doTest("T1",
         "ID#:11-07-25331 - - ALARMS - 1518 Great Dover Cir - Apt: - Bldg: - Key Map: 458W",
         "ID:11-07-25331",
         "CALL:ALARMS",
         "ADDR:1518 Great Dover Cir",
         "MAP:458W");
 
     doTest("T2",
         "ID#:11-07-25341 - 06D02 - 6D1 RESPIRATORY - 11870 Greenloch Ln - Apt: - Bldg: - Key Map: 416K - Cross Streets:GREENCANYON DR/GREENROC",
         "ID:11-07-25341",
         "CODE:06D02",
         "CALL:6D1 RESPIRATORY",
         "ADDR:11870 Greenloch Ln",
         "MAP:416K",
         "X:GREENCANYON DR/GREENROC");
 
     doTest("T3",
         "ID#:11-07-25342 - 25A01 - PSYCHIATRIC - 15215 S Brentwood Dr - Apt: - Bldg: - Key Map: 498E - Cross Streets:Dead End/DELLDALE - Box #:2004",
         "ID:11-07-25342",
         "CODE:25A01",
         "CALL:PSYCHIATRIC",
         "ADDR:15215 S Brentwood Dr",
         "MAP:498E",
         "X:Dead End/DELLDALE",
         "BOX:2004");
 
     doTest("T4",
         "ID#:11-07-25378 - - Stroke (CVA) - 11715 Greenglen Dr - Apt: - Bldg: - Key Map: 416K - Cross Streets:JOHN RALSTON RD/GREENROCK LN - B",
         "ID:11-07-25378",
         "CALL:Stroke (CVA)",
         "ADDR:11715 Greenglen Dr",
         "MAP:416K",
         "X:JOHN RALSTON RD/GREENROCK LN");
 
     doTest("T5",
         "ID#:11-07-25416 - - HEMORRHAGE/LACERATIO - 12370 S San Circle Dr - Apt: - Bldg: UNK TRAILER PARK - Key Map: 456C - Cross Streets:BARK",
         "ID:11-07-25416",
         "CALL:HEMORRHAGE/LACERATIO",
         "ADDR:12370 S San Circle Dr",
         "APT:UNK TRAILER PARK",
         "MAP:456C",
         "X:BARK");
 
     doTest("T6",
         "ID#:11-08-25733 - 55B04 - ELECTRICAL HAZARD - 16102 E Ih 10 - Apt: - Bldg: OLD RIVER CHURCH DAY - Key Map: 498G - Cross Streets:IH 10 EAST BAYOU RAMP/IH 10 SH -",
         "ID:11-08-25733",
         "CODE:55B04",
         "CALL:ELECTRICAL HAZARD",
         "ADDR:16102 E Ih 10",
         "APT:OLD RIVER CHURCH DAY",
         "MAP:498G",
         "X:IH 10 EAST BAYOU RAMP/IH 10 SH");
 
     doTest("T7",
         "ID#:11-08-25969 - 29D02m - 29D5 MOTOR VEHICLE A - 539 S Sheldon Rd - Apt: - Bldg: - Key Map: 498K - Cross Streets:JACINTO PORT BLVD/MARKET - Box #:2001",
         "ID:11-08-25969",
         "CODE:29D02m",
         "CALL:29D5 MOTOR VEHICLE A",
         "ADDR:539 S Sheldon Rd",
         "MAP:498K",
         "X:JACINTO PORT BLVD/MARKET",
         "BOX:2001");
 
     doTest("T8",
         "ID#:11-08-25936 - 29B04 - Traffic/Trans Incide - E BW 8 N / WOODFOREST BW 8 NB - Apt: - Bldg: - Key Map: 457Y - Cross Streets: - Box #:2005",
         "ID:11-08-25936",
         "CODE:29B04",
         "CALL:Traffic/Trans Incide",
         "ADDR:E BW 8 N & WOODFOREST BW 8 NB",
         "MAP:457Y",
         "BOX:2005");
 
 
     doTest("T9",
         "ID#:11-08-26006 - - Citizen Assist - 5500 E Bw 8 N - Apt: - Bldg: big tex storage - Key Map: 457Y - Cross Streets:BW 8 NB WALLISVILLE EXIT RAMP/ - Box #:2005",
         "ID:11-08-26006",
         "CALL:Citizen Assist",
         "ADDR:5500 E Bw 8 N",
         "APT:big tex storage",
         "MAP:457Y",
         "X:BW 8 NB WALLISVILLE EXIT RAMP/",
         "BOX:2005");
  }
 
   public static void main(String[] args) {
    new TXHarrisCountyESD1BParserTest().generateTests("T9");
   }
 
 }
