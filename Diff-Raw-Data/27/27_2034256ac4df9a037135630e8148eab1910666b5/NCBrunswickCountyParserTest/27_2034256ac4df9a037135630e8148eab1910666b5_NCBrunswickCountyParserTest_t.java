 package net.anei.cadpage.parsers.NC;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class NCBrunswickCountyParserTest extends BaseParserTest {
   
   public NCBrunswickCountyParserTest() {
     setParser(new NCBrunswickCountyParser(), "BRUNSWICK COUNTY", "NC");
   }
   
   @Test
   public void testParser1() {
 
     doTest("T1",
         "349:560 CUMBERLAND ST SE BOLIVIA MDL 09B01 11-020650 17:02:34 Cardiac or Respiratiory Arrest / Death RECV CALL FROM FUNERAL HOME ADVING THAT THEY RECV CALL STATING A FEMALE WAS DEAD AT THIS LOCATION AD",
         "ADDR:560 CUMBERLAND ST SE",
         "CITY:BOLIVIA",
         "CODE:09B01",
         "ID:11-020650",
         "TIME:17:02:34",
         "INFO:Cardiac or Respiratiory Arrest / Death RECV CALL FROM FUNERAL HOME ADVING THAT THEY RECV CALL STATING A FEMALE WAS DEAD AT THIS LOCATION AD");
 
     doTest("T2",
         "415:111 SE 32ND ST OAK ISLAND VERIZON WIRELESS 7045790781 MDL 09E01 11-025910 20:39:44 Cardiac or Respiratiory Arrest - De",
         "ADDR:111 SE 32ND ST",
         "CITY:OAK ISLAND",
         "NAME:VERIZON WIRELESS",
         "PHONE:7045790781",
         "CODE:09E01",
         "ID:11-025910",
         "TIME:20:39:44",
         "INFO:Cardiac or Respiratiory Arrest - De");
 
     doTest("T3",
         "411:22 BOUNDARYLINE DR NW C CALABASH LYNN 9102094924 11-025720 06:59:32 Structure Fire SMELL SOMETHING BURNING IN RESD",
         "ADDR:22 BOUNDARYLINE DR NW C",
         "CITY:CALABASH",
         "NAME:LYNN",
         "PHONE:9102094924",
         "ID:11-025720",
         "TIME:06:59:32",
         "INFO:Structure Fire SMELL SOMETHING BURNING IN RESD");
 
     doTest("T4",
         "399:1295 DECATOR ST SW SUPPLY PAM 9106339368 MDL 09E01 11-025419 22:11:30 Cardiac or RespArrest  CALLER ADV THE PT IS STIFF",
         "ADDR:1295 DECATOR ST SW",
         "CITY:SUPPLY",
         "NAME:PAM",
         "PHONE:9106339368",
         "CODE:09E01",
         "ID:11-025419",
         "TIME:22:11:30",
         "INFO:Cardiac or RespArrest  CALLER ADV THE PT IS STIFF");
 
     doTest("T5",
         "428:BAY POINT CT / JENNIS AV FELICIA  9103685110 MDL 27D04 11-026412 21:09:48 Stab-Gunshot-Pen Trauma AT THE END OF  RD .",
         "ADDR:BAY POINT CT & JENNIS AV",
         "MADDR:BAY POINT CT & JENNIS AVE",
         "NAME:FELICIA",
         "PHONE:9103685110",
         "CODE:27D04",
         "ID:11-026412",
         "TIME:21:09:48",
         "INFO:Stab-Gunshot-Pen Trauma AT THE END OF  RD .");
 
     doTest("T6",
         "426:BAY POINT CT-JENNIS AV FELICIA 9103685110 11-026412 21:07:25 Stab-Gun-Trauma AT THE END OF BAYPOINT RD MALE SUBJ IS CUT",
         "PLACE:BAY POINT",
         "ADDR:CT-JENNIS AV",
         "MADDR:BAY POINT,CT-JENNIS AVE",
         "NAME:FELICIA",
         "PHONE:9103685110",
         "ID:11-026412",
         "TIME:21:07:25",
         "INFO:Stab-Gun-Trauma AT THE END OF BAYPOINT RD MALE SUBJ IS CUT");
 
     doTest("T7",
         "415:111 SE 32ND ST OAK ISLAND  7045790781 MDL 09E01 11-025910 20:39:44 Cardiac or Resp Arrest - Death TRANS BY OKI... ADVD TH",
         "ADDR:111 SE 32ND ST",
         "CITY:OAK ISLAND",
         "PHONE:7045790781",
         "CODE:09E01",
         "ID:11-025910",
         "TIME:20:39:44",
         "INFO:Cardiac or Resp Arrest - Death TRANS BY OKI... ADVD TH");
 
     doTest("T8",
         "411:22 BOUNDARYLINE DR NW C CALABASH LYNN 9102094924 11-025720 06:59:32 Structure Fire SMELL SOMETHING BURNING IN RESD,",
         "ADDR:22 BOUNDARYLINE DR NW C",
         "CITY:CALABASH",
         "NAME:LYNN",
         "PHONE:9102094924",
         "ID:11-025720",
         "TIME:06:59:32",
         "INFO:Structure Fire SMELL SOMETHING BURNING IN RESD,");
   }
   
   @Test
   public void testParser2() {
 
     doTest("T1",
         "1333 SOUTH DICKINSON DR LELAND 11-053813 10:30:06 Take Written Report (10-92) AT SUITE 110....10-83 WITH MIRANDA REF PROPERTY DAMAGE TO VEH",
         "ADDR:1333 SOUTH DICKINSON DR",
         "CITY:LELAND",
         "ID:11-053813",
         "TIME:10:30:06",
         "INFO:Take Written Report (10-92) AT SUITE 110....10-83 WITH MIRANDA REF PROPERTY DAMAGE TO VEH");
 
     doTest("T2",
         "1111 NEW POINTE BLVD LELAND 11-053827 11:42:05 911 HANG UP ON CALL BACK SPOKE W/ LAURA ADVD SUBJS SHE WAS CALLING IN REF TO LEFT THE BUSN",
         "ADDR:1111 NEW POINTE BLVD",
         "CITY:LELAND",
         "ID:11-053827",
         "TIME:11:42:05",
         "INFO:911 HANG UP ON CALL BACK SPOKE W/ LAURA ADVD SUBJS SHE WAS CALLING IN REF TO LEFT THE BUSN");
    
   }
   
   @Test
   public void testParser3() {
 
     doTest("T1",
         "2192:BRUNSWICK COVE NURSING HOME 1478 RIVER RD SE WINNABOW 11-062488 19:38:00 Falls",
         "PLACE:BRUNSWICK COVE NURSING HOME",
         "ADDR:1478 RIVER RD SE",
         "CITY:WINNABOW",
         "ID:11-062488",
         "TIME:19:38:00",
         "INFO:Falls");
   }
   
   @Test
   public void testParser4() {
 
     doTest("T1",
         "1051:MT PISGAH BAPTIST CHURCH 494 MT PISGAH RD SW SUPPLY 11-085300 18:43:59 Brush Fire ON THE RIGHT IF GOING TOWARDS HOLDEN BEACH RD JUST PASSED MT PISGAH CHURCH ... ADVD SMALL FIRE IN T",
         "PLACE:MT PISGAH BAPTIST CHURCH",
         "ADDR:494 MT PISGAH RD SW",
         "CITY:SUPPLY",
         "ID:11-085300",
         "TIME:18:43:59",
         "INFO:Brush Fire ON THE RIGHT IF GOING TOWARDS HOLDEN BEACH RD JUST PASSED MT PISGAH CHURCH ... ADVD SMALL FIRE IN T");
 
   }
   
   @Test
   public void testJoeJenkins() {
 
     doTest("T1",
         "sentto-81008140-155-1334858821-9109162452=vtext.com@returns.groups.yahoo.com ([northwestvfd] ) 812:9035 DALES HAVEN RD NE NORTHWEST MDL 17B03-G 12-029328 14:02:41 Falls MEDICAL ALA",
         "ADDR:9035 DALES HAVEN RD NE",
         "CITY:NORTHWEST",
         "CODE:17B03-G",
         "ID:12-029328",
         "TIME:14:02:41",
         "INFO:Falls MEDICAL ALA");
 
     doTest("T2",
         "sentto-81008140-157-1334863333-9109162452=vtext.com@returns.groups.yahoo.com ([northwestvfd] ) 814:6598 NEW GROUND TR NE NORTHWEST MDL 26C02 12-029355 15:17:43 Sick Person (Specifi",
         "ADDR:6598 NEW GROUND TR NE",
         "CITY:NORTHWEST",
         "CODE:26C02",
         "ID:12-029355",
         "TIME:15:17:43",
         "INFO:Sick Person (Specifi");
     
   }
   
   @Test
   public void testLukeDuke() {
 
     doTest("T1",
         "([civietownvfd] )\n1644:375 HIGH HILL DR SW SHALLOTTE 12-030794 18:13:00 Traffic-Transportation Incident",
         "ADDR:375 HIGH HILL DR SW",
         "CITY:SHALLOTTE",
         "ID:12-030794",
         "TIME:18:13:00",
         "INFO:Traffic-Transportation Incident");
 
   }
   
   @Test
   public void testDanielMcCarthy() {
 
     doTest("T1",
         "S:[calabashvems]  M:7045:998 WATERVIEW LN CAROLINA SHORES 12-034031 17:20:33 Sick Person (Specific Diagnosis) 2 YOM SICK AND THROWING UP FOR 5 DAYS\n",
         "ADDR:998 WATERVIEW LN",
         "CITY:CAROLINA SHORES",
         "ID:12-034031",
         "TIME:17:20:33",
         "INFO:Sick Person (Specific Diagnosis) 2 YOM SICK AND THROWING UP FOR 5 DAYS");
  }
  
  @Test
  public void testKeithRichardson() {

    doTest("T1",
        "sentto-81052485-2179-1340294796-8439951185= vtext.com@returns.groups.yahoo.com ([calabashvems]) 7678:204 AZALEA CIR SUNSET BEACH 12-048247 12:04:31 Breathing Problems BREATHING PROB",
        "PLACE:sentto-81052485-2179-1340294796-8439951185= vtextcom@returnsgroupsyahoocom ([calabashvems]) 7678:",
        "ADDR:204 AZALEA CIR",
        "CITY:SUNSET BEACH",
        "ID:12-048247",
        "TIME:12:04:31",
        "INFO:Breathing Problems BREATHING PROB");
 
   }
 
   public static void main(String[] args) {
     new NCBrunswickCountyParserTest().generateTests("T1", "PLACE ADDR CITY NAME PHONE CODE ID TIME CALL INFO");
   }
 }
