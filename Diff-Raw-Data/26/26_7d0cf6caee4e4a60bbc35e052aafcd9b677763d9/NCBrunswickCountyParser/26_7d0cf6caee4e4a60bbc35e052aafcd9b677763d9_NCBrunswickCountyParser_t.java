 package net.anei.cadpage.parsers.NC;
 
 import net.anei.cadpage.parsers.dispatch.DispatchSouthernParser;
 
 /* 
 Brunswick County, NC
 Contact: "~Heather~" <sunshine5grl@gmail.com>
 Contact: Tom Rogers <tomrogers2@gmail.com
 Sender: pagegate@brunswickes.com
 System: Southern Software
 
 349:560 CUMBERLAND ST SE BOLIVIA MDL 09B01 11-020650 17:02:34 Cardiac or Respiratiory Arrest / Death RECV CALL FROM FUNERAL HOME ADVING THAT THEY RECV CALL STATING A FEMALE WAS DEAD AT THIS LOCATION AD
 415:111 SE 32ND ST OAK ISLAND VERIZON WIRELESS 7045790781 MDL 09E01 11-025910 20:39:44 Cardiac or Respiratiory Arrest - De
 411:22 BOUNDARYLINE DR NW C CALABASH LYNN 9102094924 11-025720 06:59:32 Structure Fire SMELL SOMETHING BURNING IN RESD
 399:1295 DECATOR ST SW SUPPLY PAM 9106339368 MDL 09E01 11-025419 22:11:30 Cardiac or RespArrest  CALLER ADV THE PT IS STIFF
 428:BAY POINT CT / JENNIS AV FELICIA  9103685110 MDL 27D04 11-026412 21:09:48 Stab-Gunshot-Pen Trauma AT THE END OF  RD .
 426:BAY POINT CT-JENNIS AV FELICIA 9103685110 11-026412 21:07:25 Stab-Gun-Trauma AT THE END OF BAYPOINT RD MALE SUBJ IS CUT
 415:111 SE 32ND ST OAK ISLAND  7045790781 MDL 09E01 11-025910 20:39:44 Cardiac or Resp Arrest - Death TRANS BY OKI... ADVD TH
 411:22 BOUNDARYLINE DR NW C CALABASH LYNN 9102094924 11-025720 06:59:32 Structure Fire SMELL SOMETHING BURNING IN RESD,
 
 Contact: Anthony Nathan <tnathan55@gmail.com>
 1333 SOUTH DICKINSON DR LELAND 11-053813 10:30:06 Take Written Report (10-92) AT SUITE 110....10-83 WITH MIRANDA REF PROPERTY DAMAGE TO VEH
 1111 NEW POINTE BLVD LELAND 11-053827 11:42:05 911 HANG UP ON CALL BACK SPOKE W/ LAURA ADVD SUBJS SHE WAS CALLING IN REF TO LEFT THE BUSN
 
 Contact: Christopher Grace Jr <cgracejr@gmail.com>
 Sender: lelandems+bncCOqwh4kZEO2g9_EEGgTkSK1X@googlegroups.com
 2192:BRUNSWICK COVE NURSING HOME 1478 RIVER RD SE WINNABOW 11-062488 19:38:00 Falls
 
 Contact: Doritto <no1bravesfancandy@gmail.com>
 1051:MT PISGAH BAPTIST CHURCH 494 MT PISGAH RD SW SUPPLY 11-085300 18:43:59 Brush Fire ON THE RIGHT IF GOING TOWARDS HOLDEN BEACH RD JUST PASSED MT PISGAH CHURCH ... ADVD SMALL FIRE IN T
 
 Contact: Joe Jenkins <chasenfins@gmail.com>
 Sender: vtext.com@returns.groups.yahoo.com
 sentto-81008140-155-1334858821-9109162452=vtext.com@returns.groups.yahoo.com ([northwestvfd] ) 812:9035 DALES HAVEN RD NE NORTHWEST MDL 17B03-G 12-029328 14:02:41 Falls MEDICAL ALA
 sentto-81008140-157-1334863333-9109162452=vtext.com@returns.groups.yahoo.com ([northwestvfd] ) 814:6598 NEW GROUND TR NE NORTHWEST MDL 26C02 12-029355 15:17:43 Sick Person (Specifi
 
 Contact: Luke Duke <bigcountrymud@gmail.com>
 Sender: vtext.com@returns.groups.yahoo.com
 ([civietownvfd] )\n1644:375 HIGH HILL DR SW SHALLOTTE 12-030794 18:13:00 Traffic-Transportation Incident
 
 Contact: Daniel McCarthy <mcca6564@student.brunswickcc.edu>
 Sender: pagegate@brunswickes.com
 S:[calabashvems]  M:7045:998 WATERVIEW LN CAROLINA SHORES 12-034031 17:20:33 Sick Person (Specific Diagnosis) 2 YOM SICK AND THROWING UP FOR 5 DAYS\n
 
Contact: Keith Richardson <aldo4371@gmail.com>
Sender: vtext.com@returns.groups.yahoo.com
sentto-81052485-2179-1340294796-8439951185= vtext.com@returns.groups.yahoo.com ([calabashvems]) 7678:204 AZALEA CIR SUNSET BEACH 12-048247 12:04:31 Breathing Problems BREATHING PROB

 */
 
 public class NCBrunswickCountyParser extends DispatchSouthernParser {
   
   
   public NCBrunswickCountyParser() {
     super(CITY_LIST, "BRUNSWICK COUNTY", "NC", DSFLAG_OPT_DISPATCH_ID | DSFLAG_LEAD_PLACE);
   }
   
   @Override
   public String getFilter() {
    return "pagegate@brunswickes.com,vtext.com@returns.groups.yahoo.com";
   }
   
   private static final String[] CITY_LIST = new String[]{
     "LOCKWOODS FOLLY",
     "NORTHWEST",
     "SHALLOTTE",
     "SMITHVILLE",
     "TOWN CREEK",
     "WACCAMAW",
 
     "BALD HEAD ISLAND",
     "BELVILLE",
     "BOILING SPRING LAKES           ",
     "BOLIVIA",
     "CALABASH",
     "CAROLINA SHORES",
     "CASWELL BEACH",
     "HOLDEN BEACH",
     "LELAND",
     "NAVASSA",
     "NORTHWEST",
     "OAK ISLAND",
     "OCEAN ISLE BEACH",
     "SANDY CREEK",
     "SHALLOTTE",
     "SOUTHPORT",
     "ST. JAMES",
     "SUNSET BEACH",
     "VARNAMTOWN",
 
     "ANTIOCH",
     "ASH",
     "BATARORA",
     "BELL SWAMP",
     "BISHOP",
     "BIVEN",
     "BONAPARTE LANDING",
     "BOONES NECK",
     "BOWENSVILLE",
     "BRUNSWICK STATION",
     "CAMP BRANCH",
     "CEDAR GROVE",
     "CEDAR HILL",
     "CIVIETOWN",
     "CLAIRMONT",
     "CLARENDON",
     "COOLVALE",
     "DOE CREEK",
     "EASTBROOK",
     "EASY HILL",
     "PINEY GROVE",
     "SUPPLY",
     "SUNSET HARBOR",
     "WINNABOW",
   };
 
 }
