 package net.anei.cadpage.parsers.CA;
 
 import java.util.Properties;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.FieldProgramParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /*
 San Bernardino County, CA
 Sender: cad@confire.org
 Contact: support@active911.com
 
 [] | TU - T>W Cypress Ave / S Center St - REDC >LOC INFO: >AGN MAP:RED045 -TB MAP:648 B2 >X ST: >E261,MS261,AMR33>COMMENTS:GREEN 300 vs BLACK SEDAN,External Case Number 'RED _1200689' added for Redlands.,Automatic Case Number(s) issued for Ground Ambulance: \n
 [] | HEART->1600 E Citrus Ave -A REDC >LOC INFO:DR HOENSHELL >AGN MAP:RED069 -TB MAP:608 F7 >X ST:N DEARBORN ST/S LA SALLE ST >E261,MS261,AMR37>COMMENTS:[ProQA Session Aborted] ALLIED AGENCY,External Case Number 'RED _1200691' added for Redlands.,Automatic C\n
 [] | SICK-D>105 Terracina Blvd -106-a REDC >LOC INFO:BROOKSIDE HEALTH >AGN MAP:RED043 -TB MAP:647 H2 >X ST:BROOKSIDE AVE/CORDOVA AVE >E264,ME263R,AMR39>COMMENTS:793-2271,[ProQA Script] Dispatch code: 26D01 85 year old, Male, Conscious, Breathing. Sick Person\n
 [] | PS - P>27320 W Lugonia Ave - RED >LOC INFO:TARGET (RED) >AGN MAP:RED092 -TB MAP:607 H5 >X ST:INDIANA CT/N ALABAMA ST >E264>COMMENTS:CHILD LOCKED IN A WHITE CADILAC CTS,RED,External Case Number 'RED _1200693' added for Redlands.,\n
 [] | UNC-D2>205 Judson St -C REDC >LOC INFO:JUDSON APTS >AGN MAP:RED068 -TB MAP:608 E7 >X ST:CENTRAL AVE/LARAMIE AVE >E261,MS261,AMR32>COMMENTS:[ProQA Script] Dispatch code: 31D02 35 year old, Female, Unconscious, Breathing. Unconscious / Fainting (Near). Un\n
 [] | CP-D2 >1331 W Colton Ave - REDC >LOC INFO:HUSTLER CLUB-DE JA VU >AGN MAP:RED078 -TB MAP:607 J6 >X ST:INDUSTRIAL PARK AVE/TRI-CITY CENTER DR >E264,ME263R,AMR39>COMMENTS:WPH2 LAT:34.06252000 LON:-117.203372 METERS:57 %:063,ENTER THROUGH THE BACK IOF THE B\n
 [] | ABD-A1>101 W Redlands Blvd - REDC >LOC INFO:SAVON DRUG STORE >AGN MAP:RED066 -TB MAP:608 B7 >X ST:N 4TH ST/3RD ST >E261,MS261,AMR35>COMMENTS:PT IN PHARMACY,[ProQA Script] Dispatch code: 01A01 45 year old, Female, Conscious, Breathing. Abdominal Pain / P\n
 [] | FALL-B>620 E Highland Ave -26 REDC >LOC INFO:BRASWELLS CHATEAU >AGN MAP:RED046 -TB MAP:648 D2 >X ST:E REDLANDS BLVD/ROOSEVELT RD >E262,MS261,AMR30>COMMENTS:WPH2 LAT:34.04472100 LON:-117.164211 METERS:41 %:095,[ProQA Script] Dispatch code: 17B02G 97 year\n
 [] | BIRTH->1320 Industrial Park Ave - REDC >LOC INFO:BURGER KING >AGN MAP:RED078 -TB MAP:607 J6 >X ST:W COLTON AVE/TRI-CITY CENTER DR >E264,ME263R,AMR39>COMMENTS:WPH2 LAT:34.06319600 LON:-117.200990 METERS:14 %:063,[ProQA Script] Dispatch code: 24B02 21 yea\n
 [] | UNC-A1>626 N Dearborn St -87 REDC >LOC INFO:ORANGE GROVE MHP >AGN MAP:RED082 -TB MAP:608 F6 >X ST:INDEPENDENCE AVE/STILLMAN AVE >E261,ME263R,AMR39>COMMENTS:WPH2 LAT:34.06163000 LON:-117.143354 METERS:28 %:063,[ProQA Script] Dispatch code: 31A01 52 year \n
 [] | CI - C>1270 W Park Ave - REDC >LOC INFO:REDLANDS CITY YARDS >AGN MAP:RED065 -TB MAP:607 J7 >X ST:TENNESSEE ST/KANSAS ST >T261,BC705,ME263R,E264>COMMENTS:PD OS FIRE ALARM ACTIVATION,External Case Number 'RED _1200702' added for Redlands.,\n
 [] | CA- Co>350 Terracina Blvd - REDC >LOC INFO:RCH >AGN MAP:RED036 -TB MAP:647 J3 >X ST:LAUREL AVE/W FERN AVE >E262>COMMENTS:ZONE 1 GENERAL FIRE ALARM,REDC,External Case Number 'RED _1200703' added for Redlands.,\n
 [] | CVA-C4>222 Primrose Ave - REDC >LOC INFO: >AGN MAP:RED055 -TB MAP:648 C1 >X ST:No X-Street/HIBISCUS DR >E261,MS261,AMR34>COMMENTS:WPH2 LAT:34.04711400 LON:-117.171829 METERS:14 %:095,[ProQA Script] Dispatch code: 28C04L 60 year old, Female, Conscious, B\n
 [] | UNKM-B>1678 W Redlands Blvd - REDC >LOC INFO:99 CENT STORE >AGN MAP:RED077 -TB MAP:607 H6 >X ST:ALABAMA ST/IOWA ST >E264,ME263R,AMR39>COMMENTS:MAN DOWN INSIDE STORE ATTEMPTING TO GET FURTHER,External Case Number 'RED _1200705' added for Redlands.,Automa\n
 [] | TU - T>N University St / E San Bernardino Ave - REDC >LOC INFO: >AGN MAP:RED110 -TB MAP:608 D4 >X ST: >E261,MS261,AMR39>COMMENTS:401,REDC,External Case Number 'RED _1200707' added for Redlands.,Automatic Case Number(s) issued for Ground Ambulance: AMB12\n
 [] | FALL-A>365 Los Robles Crst - REDC >LOC INFO: >AGN MAP:RED039 -TB MAP:648 E2 >X ST:OAK ST/No X-Street >E262,AMR34,MS261>COMMENTS:WPH2 LAT:34.03896000 LON:-117.159169 METERS:21 %:095,[ProQA Script] Dispatch code: 17A01G 71 year old, Female, Conscious, Bre\n
 [] | UNC-D1>2050 W Redlands Blvd - REDC >LOC INFO:WALMART (RED) >AGN MAP:RED076 -TB MAP:607 G6 >X ST:NEW JERSEY ST/CALIFORNIA ST >E264,MS261,AMR31>COMMENTS:WPH2 LAT:34.06325000 LON:-117.225730 METERS:51 %:090,**PT AT MCDONALDS INSIDE WALMART**,60 YOF FAINTED\n
 [] | FC -St>620 E Highland Ave -4 REDC >LOC INFO:BRASWELLS CHATEAU >AGN MAP:RED046 -TB MAP:648 D2 >X ST:E REDLANDS BLVD/ROOSEVELT RD >T261,BC705,E262,E261,ME263,ME9,MS261,OSHA02,6FGND3>COMMENTS:SMOKE SHOWING FROM HEATING SYST IN ROOM #4,REDLANDS - CITY,Exter\n
 [] | SICK-D>450 Judson St -98 REDC >LOC INFO:SYLVAN MOBILE >AGN MAP:RED081 -TB MAP:608 E6 >X ST:LARAMIE AVE/SYLVAN BLVD >E261,MS261,AMR35>COMMENTS:[Address: 450 JUDSON ST] [Medium] [Unknown] FOR UNIT #99 ONLY - VIDEO-TRANSLATOR RELAY FOR DEAF-SIGN LANGUAGE R\n
 [] | DIA-C1>1325 Pacific St - REDC >LOC INFO: >AGN MAP:RED038 -TB MAP:648 C3 >X ST:W CRESCENT AVE/BALBOA CT >E262,MS261,AMR33,E262>COMMENTS:WPH2 LAT:34.03305900 LON:-117.177429 METERS:32 %:095,[ProQA Script] Dispatch code: 13C01 83 year old, Male, Conscious,\n
 [] | ALL-C1>1666 Brookside Ave - REDC >LOC INFO: >AGN MAP:RED043 -TB MAP:647 H2 >X ST:CASCADE ST/TERRACINA BLVD >E264,ME263,AMR39>COMMENTS:WPH2 LAT:34.04496800 LON:-117.211869 METERS:19 %:063,[ProQA Script] Dispatch code: 02C01 27 year old, Male, Conscious, \n
 [] | CI - C>250 E Fern Ave -109 REDC >LOC INFO: >AGN MAP:RED055 -TB MAP:648 C1 >X ST:SONORA ST/MYRTLE ST >T261,BC705,E261,E264>COMMENTS:[Address: 250 E FERN AVE] [Medium] [Unknown] FERNWOOD CONDOS RESIDENT IN WHEELCHAIR UNABLE TO EVACUATE WITHOUT ASSISTANCE,\n
  | FALL-A>1240 Devon Pl - REDC >LOC INFO: >AGN MAP:RED095 -TB MAP:608 D5 >X ST:E BROCKTON AVE/BARBRA LN >ME263,AMR38>COMMENTS:[ProQA Script] Dispatch code: 17A01G 70 year old, Female, Conscious, Breathing. Falls. NOT DANGEROUS body area (On the ground or f
  | CA- Co>111 W Lugonia Ave - REDC >LOC INFO:REDLANDS COMMUNITY CENTER >AGN MAP:RED094 -TB MAP:608 B5 >X ST:WASHINGTON ST/CLAY ST >E261>COMMENTS:FIRE SMOKE DETECTOR SOUTH ZONE4,External Case Number 'RED _1200732' added for Redlands.,
  | FU - U>Mission Rd / California St - LOMC >LOC INFO: >AGN MAP:LOM022 -TB MAP:647 F1 >X ST: >E264,MS251R,ME263,BC402>COMMENTS:WPH2 LAT:34.05005300 LON:-117.226889 METERS:14 %:095,RP STATES ON THE CORNER OF MISSION AND CALIFORNIA OFF THE ACCESS ROAD TO THE
  | FV - V>1786 E Lugonia Ave - REDC >LOC INFO:STATER BROS (RED3) >AGN MAP:RED097 -TB MAP:608 G5 >X ST:REVELATION WAY/N WABASH AVE >ME263>COMMENTS:WPH2 LAT:34.06911800 LON:-117.139986 METERS:9 %:063,**DODGE CHARGER**,External Case Number 'RED _1200734' adde
  | SICK-C>945 Salem Dr -6 REDC >LOC INFO:PLYMOUTH VILLAGE >AGN MAP:RED046 -TB MAP:648 D2 >X ST:STANDISH WAY/CARVER CIR >E261,MS261,AMR34>COMMENTS:WPH2 LAT:34.04314400 LON:-117.171807 METERS:25 %:063,[ProQA Script] Dispatch code: 26C01 95 year old, Female, 
  | SOB-C1>700 E Highland Ave -17-2 REDC >LOC INFO:COUNTRY VILLA >AGN MAP:RED047 -TB MAP:648 D2 >X ST:E REDLANDS BLVD/ROOSEVELT RD >E262,ME263,AMR37,MS261>COMMENTS:83 YOF, DIFF BREATHER,AMR ER,[ProQA Session Aborted] ALLIED AGENCY,External Case Number 'RED 
  | DIA-A1>1110 Oxford Dr -D REDC >LOC INFO: >AGN MAP:RED095 -TB MAP:608 C6 >X ST:E BROCKTON AVE/COURIER AVE >ME263,AMR33>COMMENTS:W911 LAT:34.06555700 LON:-117.170198 METERS:592 %:000,[ProQA Script] Dispatch code: 13A01 13 year old, Female, Conscious, Brea
  | ALL-D2>841 Sinclair Ct - REDC >LOC INFO: >AGN MAP:RED125 -TB MAP:608 C4 >X ST:No X-Street/DUKE ST >ME263,AMR33>COMMENTS:WPH2 LAT:34.08204700 LON:-117.172344 METERS:6 %:090,[ProQA Script] Dispatch code: 02D02 53 year old, Female, Conscious, Breathing. Al
  | SOB-C1>1249 Cornell Ave -E REDC >LOC INFO: >AGN MAP:RED096 -TB MAP:608 E5 >X ST:EDWARDS ST/N GROVE ST >ME263,AMR32>COMMENTS:[ProQA Script] Dispatch code: 06C01 19 year old, Male, Conscious, Breathing. Breathing Problems. Abnormal breathing. Caller State
  | FC -St>217 Kaye Ct -A REDC >LOC INFO: >AGN MAP:RED068 -TB MAP:608 E7 >X ST:CENTRAL AVE/No X-Street >T261,BC706,E261,E262,ME263,E264,MS261,OSHA03>COMMENTS:WPH2 LAT:34.05860400 LON:-117.158654 METERS:121 %:090,HOUSE FULL OF SMOKE,REDC,External Case Number
  | HEART->1301 California St - REDC >LOC INFO:KAISER >AGN MAP:RED091 -TB MAP:607 F5 >X ST:W LUGONIA AVE/ALMOND AVE >ME263,AMR38>COMMENTS:NURSE TREATMETN AREA 6 1ST FLOOR,[ProQA Script] Dispatch code: 19C04 65 year old, Female, Conscious, Breathing. Heart P
  | HL-D3 >151 Judson St -116 REDC >LOC INFO:CITRUS ARMS >AGN MAP:RED068 -TB MAP:608 E7 >X ST:E CITRUS AVE/CENTRAL AVE >E261,MS261,AMR37>COMMENTS:[ProQA Script] Dispatch code: 21D03 65 year old, Male, Conscious, Breathing. Hemorrhage / Lacerations. DANGEROU
  | FALL-B>945 Salem Dr -1 REDC >LOC INFO:ASSIST LIVING >AGN MAP:RED046 -TB MAP:648 D2 >X ST:STANDISH WAY/CARVER CIR >T261,ME263,AMR31>COMMENTS:WPH2 LAT:34.04302600 LON:-117.171550 METERS:41 %:063,[ProQA Script] Dispatch code: 17B01G 87 year old, Female, Co
  | SOB-D2>422 Orange St - REDC >LOC INFO:PACIFIC DENTAL >AGN MAP:RED066 -TB MAP:608 B7 >X ST:W REDLANDS BLVD/W STUART AVE >T261,MS261,AMR37>COMMENTS:[ProQA Script] Dispatch code: 06D02 82 year old, Female, Conscious, Breathing. Breathing Problems. DIFFICUL
  | UNKM-B>686 E Redlands Blvd -326 REDC >LOC INFO:CASA DE LA VISTA >AGN MAP:RED067 -TB MAP:608 C7 >X ST:E CLARK ST/E FERN AVE >E261,MS261,AMR31>COMMENTS:MEDICAL-ALARM COMPANY UNABLE TO REACH RESD,[ProQA Session Aborted] NO ANSWER WHEN RESD CALLED,External 
  | UNC-A1>101 W Redlands Blvd - REDC >LOC INFO:CVS PHARMACY >AGN MAP:RED066 -TB MAP:608 B7 >X ST:N 4TH ST/3RD ST >T261,ME263,AMR39,MS261>COMMENTS:[ProQA Script] Dispatch code: 31A01 45 year old, Male, Conscious, Breathing. Unconscious / Fainting (Near). Fa
  | CA- Co>1600 E Citrus Ave -A REDC >LOC INFO:EPIC MANAGMENT >AGN MAP:RED069 -TB MAP:608 F7 >X ST:N DEARBORN ST/S LA SALLE ST >E261>COMMENTS:GENERAKL FAP FIRE ALARM,External Case Number 'RED _1200760' added for Redlands.,
  | UNC-D2>1418 5th Ave - REDC >LOC INFO: >AGN MAP:RED056 -TB MAP:648 F1 >X ST:E HIGHLAND AVE/MARION AVE >E261,MS261,AMR35>COMMENTS:[ProQA Script] Dispatch code: 31D02 75 year old, Male, Unconscious, Breathing. Unconscious / Fainting (Near). Unconscious -- 
  | SZ-D2 >1150 Brookside Ave - REDC >LOC INFO:BEHIND GIOVANNIS PIZZA >AGN MAP:RED053 -TB MAP:648 A1 >X ST:No X-Street/No X-Street >E264,ME263,AMR34>COMMENTS:WPH2 LAT:34.05056800 LON:-117.196698 METERS:14 %:063,[ProQA Script] Dispatch code: 12D02E 31 year o
  | SICK-C>411 E Palm Ave - REDC >LOC INFO:GOODWILL BOOKSTORE >AGN MAP:RED055 -TB MAP:648 D1 >X ST:LA PALOMA ST/E REDLANDS BLVD >E261,MS261,AMR34>COMMENTS:[ProQA Script] Dispatch code: 26C01 61 year old, Male, Conscious, Breathing. Sick Person (Specific Dia
  | FALL-A>13082 South Ln - REDC >LOC INFO: >AGN MAP:RED019 -TB MAP:648 J5 >X ST:FREYA DR/SUTHERLAND DR >E262,MS261,AMR37>COMMENTS:[ProQA Script] Dispatch code: 17A01G 91 year old, Male, Conscious, Breathing. Falls. NOT DANGEROUS body area (On the ground or
  | TU - T>New Jersey St / W Redlands Blvd - REDC >LOC INFO: >AGN MAP:RED076 -TB MAP:607 G6 >X ST: >E264,ME263,AMR36>COMMENTS:REDLANDS,External Case Number 'RED _1200742' added for Redlands.,Automatic Case Number(s) issued for Ground Ambulance: AMB12-006845
  | FALL ->725 Chestnut Ave - REDC >LOC INFO: >AGN MAP:RED045 -TB MAP:648 C2 >X ST:S CENTER ST/PACIFIC ST >E261,MS261,AMR36>COMMENTS:*** EMD COMPLETED WITH CARDS NOT PROQA ***,85 YOM,External Case Number 'RED _1200743' added for Redlands.,Automatic Case Num
  | TI - T>N University St / I 10 Fwy E - REDC >LOC INFO: >AGN MAP:RED067 -TB MAP:608 D7 >X ST: >ME263,AMR30>COMMENTS:[ProQA Session Aborted] ALLIED AGENCY,1 PT COMPLAINING OF PAIN,External Case Number 'RED _1200744' added for Redlands.,Automatic Case Numbe
  | CA- Co>1969 Orange Tree Ln - REDC >LOC INFO: >AGN MAP:RED091 -TB MAP:607 G6 >X ST:N NEVADA ST/UNNAMED DR >E264>COMMENTS:BUILDING OS & Y VALVE,External Case Number 'RED _1200745' added for Redlands.,
  | HEART->1075 Parkford Dr - REDC >LOC INFO:76 STATION >AGN MAP:RED047 -TB MAP:648 E2 >X ST:MARSHALL ST/FORD ST >E262,MS261,AMR37>COMMENTS:PT INSIDE EMPLOYEE OFFICE,[ProQA Script] Dispatch code: 19C07 48 year old, Female, Conscious, Breathing. Heart Proble
  | HCE-B1>27510 W Lugonia Ave - RED >LOC INFO:JAMBA JUICE (RED) >AGN MAP:RED093 -TB MAP:607 J5 >X ST:CITRUS PLAZA DR/INDIANA CT >E264,ME263,AMR33>COMMENTS:WPH2 LAT:34.07141400 LON:-117.204530 METERS:51 %:090,CHILD LOCKED IN GOLD CAMRY/POSS 9-10 MONTHS OLD,
  | TU - T>Alabama St / I 10 Onrp W - REDC >LOC INFO: >AGN MAP:RED092 -TB MAP:607 H6 >X ST: >ME263,AMR33>COMMENTS:ON ALABAMA AT THE I 10 ONRAMP WESTBOUND,REDC,External Case Number 'RED _1200748' added for Redlands.,Automatic Case Number(s) issued for Ground
  | UNC-D3>202 E State St - REDC >LOC INFO:STATE STREET DELI >AGN MAP:RED066 -TB MAP:608 C7 >X ST:N 6TH ST/7TH ST >E261,MS261,AMR32>COMMENTS:PT IFO DELI,[ProQA Script] Dispatch code: 31D03 84 year old, Male, Conscious, Breathing. Unconscious / Fainting (Nea
  | HL-B3 >814 S Grove St - REDC >LOC INFO:**STAGE** >AGN MAP:RED056 -TB MAP:648 E1 >X ST:E PALM AVE/BEGONIA CT >E262,ME263,AMR30>COMMENTS:WPH2 LAT:34.05204900 LON:-117.159662 METERS:16 %:095,*** 9-1-1 HANG UP - ATTEMPTING CALL BACK ***,**STAGE***,PER CHP F
  | UNKM-B>1555 Campus Ave - REDC >LOC INFO: >AGN MAP:RED082 -TB MAP:608 F6 >X ST:FALCON LN/N DEARBORN ST >E261,ME263,AMR33>COMMENTS:WPH2 LAT:34.06575000 LON:-117.149019 METERS:88 %:090,REDC,*** 9-1-1 HANG UP - ATTEMPTING CALL BACK ***,External Case Number 
  | FALL-B>10 Terracina Blvd -126 REDC >LOC INFO:MISSION COMMONS-ASSISTED LIVING >AGN MAP:RED052 -TB MAP:647 H1 >X ST:BARTON RD/LADERA ST >E264,MS261,AMR33>COMMENTS:PT IS IN THE ASSISTED LIVING,ROOM 126,[ProQA Script] Dispatch code: 17B01 90 year old, Femal
  | UNC-C2>12835 17th St - REDC >LOC INFO: >AGN MAP:RED028 -TB MAP:649 B5 >X ST:OUTER 10 HWY S/FLORIDA ST >E262,MS261,AMR37>COMMENTS:56 YOM,External Case Number 'RED _1200718' added for Redlands.,Automatic Case Number(s) issued for Ground Ambulance: AMB12-0
  | ABD-C3>140 W Pioneer Ave -133 REDC >LOC INFO: >AGN MAP:RED124 -TB MAP:608 B4 >X ST:WASHINGTON ST/CLAY ST >ME263,AMR38>COMMENTS:[Address: 140 W PIONEER AVE] [Medium] [Location Information] SPACE#79 RESIDENT IS LUCILLE RIEDEL / TEL 909-792-5262 / GATE COD
  | CP-C2 >151 Palm Lane Dr E - REDC >LOC INFO: >AGN MAP:RED055 -TB MAP:648 C2 >X ST:No X-Street/E PALM AVE >E261,MS261,AMR39>COMMENTS:[ProQA Script] Dispatch code: 10C02 81 year old, Male, Conscious, Breathing. Chest Pain (Non-Traumatic). Heart attack or a
  | ASLT-B>605 E Redlands Blvd - REDC >LOC INFO:** STAGE ** JACK IN THE BOX >AGN MAP:RED067 -TB MAP:608 C7 >X ST:E CITRUS AVE/E CLARK ST >E261,MS261,AMR37>COMMENTS:[ProQA Session Aborted] ALLIED AGENCY,*** STAGE OUT OF THE AREA FOR LAW ENFORCEMENT ***,Exter
  | FALL-D>609 Georgia St - REDC >LOC INFO: >AGN MAP:RED081 -TB MAP:608 E6 >X ST:SYLVAN BLVD/STILLMAN AVE >E261,MS261,AMR33>COMMENTS:WPH2 LAT:34.06096500 LON:-117.155607 METERS:46 %:063,[ProQA Script] Dispatch code: 17D03 80 year old, Female, Conscious, Bre
  | SOB-D4>27450 W Lugonia Ave - RED >LOC INFO:BED BATH & BEYOND (RED) >AGN MAP:RED092 -TB MAP:607 J5 >X ST:INDIANA CT/N ALABAMA ST >T261,ME263,AMR34>COMMENTS:PT IN PERSONAL CARE AREA,[ProQA Script] Dispatch code: 06D04 54 year old, Female, Conscious, Breat
  | UNC-D3>2140 Mentone Blvd -133 MEN >LOC INFO:FRIENDLY HILLS MHP >AGN MAP:MEN099 -TB MAP:609 A5 >X ST:CRAFTON AVE/PLUMWOOD LN >ME263,AMR39>COMMENTS:WPH2 LAT:34.06730500 LON:-117.118313 METERS:11 %:063,[ProQA Script] Dispatch code: 31D03 82 year old, Femal
  | SOB-D2>W San Bernardino Ave / California St - REDC >LOC INFO: >AGN MAP:RED106 -TB MAP:607 F4 >X ST: >E264,MS261,AMR37>COMMENTS:WPH2 LAT:34.06375400 LON:-117.213778 METERS:3035 %:090,CITY OF REDLANDS VEHICLE,[ProQA Script] Dispatch code: 06D02 35 year ol
  | SOB-C1>1710 Barton Rd -208 REDC >LOC INFO:LLBMC >AGN MAP:RED052 -TB MAP:647 H1 >X ST:IOWA ST/TERRACINA BLVD >E264,MS261,AMR35>COMMENTS:83 YOF SOB,REDC,776,External Case Number 'RED _1200726' added for Redlands.,Automatic Case Number(s) issued for Ground
  | ZAP - >San Timoteo Canyon Rd / Beaumont Ave - REDC >LOC INFO: >AGN MAP:RED035 -TB MAP:647 H3 >X ST: >E261>COMMENTS:WPH2 LAT:34.01817700 LON:-117.188372 METERS:28 %:095,WIRES DOWN IN ROADWAY,External Case Number 'RED _1200727' added for Redlands.,
  | SOB-C1>812 Granada Ct - REDC >LOC INFO: >AGN MAP:RED078 -TB MAP:608 A6 >X ST:No X-Street/CARLSON AVE >ME263,AMR32,T261,MS261>COMMENTS:[ProQA Script] Dispatch code: 06C01 61 year old, Female, Conscious, Breathing. Breathing Problems. Abnormal breathing. 
  | FALL-B>Industrial Park Ave / W Colton Ave - REDC >LOC INFO:LONG JOHN SILVERS >AGN MAP:RED078 -TB MAP:607 J6 >X ST: >ME263,MS261,AMR36,E264>COMMENTS:WPH2 LAT:34.06314300 LON:-117.201955 METERS:9 %:090,[ProQA Script] Dispatch code: 17B01 88 year old, Male
  | SICK-D>Jasmine St / E Brockton Ave - REDC >LOC INFO: >AGN MAP:RED097 -TB MAP:608 F6 >X ST: >ME263,MS261,AMR31,E261>COMMENTS:WPH2 LAT:34.06647900 LON:-117.147002 METERS:19 %:063,PT IN SILVER TOYOTA,External Case Number 'RED _1200730' added for Redlands.,
 
  */
 
 public class CASanBernardinoCountyParser extends FieldProgramParser {
   
   public CASanBernardinoCountyParser() {
     super("SAN BERNARDINO COUNTY", "CA",
            "CALL! ADDR! LOC_INFO:PLACE AGN_MAP:MAP! X_ST:X! UNIT COMMENTS:INFO");
   }
   
   @Override
   public String getFilter() {
     return "cad@confire.org";
   }
 
   @Override
   protected boolean parseMsg(String body, Data data) {
     if (!body.startsWith("| ")) return false;
     body = body.substring(2).trim();
     return parseFields(body.split(">"), data);
   }
   
   private class MyCallField extends CallField {
     
     @Override
     public void parse(String field, Data data) {
       String key = field;
       int pt = key.indexOf(' ');
       if (pt >= 0) key = field.substring(0,pt);
       String desc = TYPE_CODES.getProperty(key);
       if (desc == null) {
         pt = key.indexOf('-');
         if (pt >= 0) key = key.substring(0,pt);
         desc = TYPE_CODES.getProperty(key);
       }
       if (desc != null) field = field + " - " + desc;
       super.parse(field, data);
     }
   }
   
   private class MyAddressField extends AddressField {
     
     @Override
     public void parse(String field, Data data) {
       Parser p = new Parser(field);
       data.strCity = convertCodes(p.getLast(' '), CITY_CODES);
       data.strApt = p.getLastOptional(" -");
       parseAddress(p.get(), data);
     }
     
     @Override
     public String getFieldNames() {
       return "ADDR APT CITY";
     }
   }
   
   private class MyMapField extends MapField {
     @Override
     public void parse(String field, Data data) {
       field = field.replace("-TB MAP:", "- ");
       super.parse(field, data);
     }
   }
   
   private static final Pattern PRO_QA_PTN = Pattern.compile(" *\\[ProQA .*?\\] *");
   private static final Pattern CODE_PTN = Pattern.compile("\\bDispatch code: *(\\w+)\\b");
   private class MyInfoField extends InfoField {
     @Override
     public void parse(String field, Data data) {
       int pt = field.indexOf(",External");
       if (pt >= 0) field = field.substring(0,pt).trim();
       field = PRO_QA_PTN.matcher(field).replaceAll(" ");
       Matcher match = CODE_PTN.matcher(field);
       if (match.find()) {
         data.strCode = match.group(1);
         field = field.substring(0,match.start()) + " " + field.substring(match.end());
       }
       field = field.replaceAll("  +", " ").trim();
       super.parse(field, data);
     }
     
     @Override
     public String getFieldNames() {
       return "CODE INFO";
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("CALL")) return new MyCallField();
     if (name.equals("ADDR")) return new MyAddressField();
     if (name.equals("MAP")) return new MyMapField();
     if (name.equals("INFO")) return new MyInfoField();
     return super.getField(name);
   }
   
   private static final Properties CITY_CODES = buildCodeTable(new String[]{
       "RED", "REDLANDS",
       "REDC", "REDLANDS"
   });
   
   private static final Properties TYPE_CODES = buildCodeTable(new String[]{
       "AB", "Animal Bites",
       "ABD", "Abdominal Pain",
       "ALL", "Allergies",
       "AP", "Aircraft Crash on Airport",
       "APH", "VCV AP Crash High",
       "APL", "VCV AP Crash Low",
       "APM", "VCV AP Crash Medium",
       "AS", "Aircraft Standby",
       "ASLT", "Assault",
       "AT", "Ambulance Transport",
       "BIRTH", "Childbirth",
       "BP", "Back Pain",
       "BT", "Bomb Threat",
       "CI", "Commercial Investigation",
       "CO", "Carbon Monoxide Alarm",
       "CP", "Chest Pains",
       "CPR", "CPR in Progress",
       "CST", "Create Strike Team",
       "CVA", "Stroke",
       "DIA", "Diabetic Problem",
       "ELEV", "Elevator Problems",
       "EYE", "Eye Problems",
       "FALL", "Fall Victim",
       "FAR", "Fire Alarm with Reset",
       "FD", "Dumpster Fire",
       "FG", "Vegetation Fire",
       "FH", "Hay Fire",
       "FI", "Improvement Fire",
       "FR", "Refuse Fire",
       "FS", "Structure Fire- Non Comm",
       "FT", "Motorhome/Truck/Bus Fire",
       "FTF", "Frwy Truck/Bus Fire",
       "FU", "Unknown Type Fire",
       "FV", "Vehicle Fire",
       "FVF", "Frwy Vehicle Fire",
       "FWI", "Fireworks Investigation",
       "GAS", "Ambulance Standby",
       "GBP", "Burn Permit",
       "GCC", "County Comm Incident",
       "GEH", "Environmental Health Inc",
       "GEM", "Emergency Medical Inc",
       "GHZ", "Hazardous Materials Inc",
       "GIT", "IT Incident",
       "GLL", "Loma Linda City Inc",
       "GMH", "Medical Helicopter Inc",
       "GMI", "Miscellaneous Gov Inc",
       "GMU", "Out-of-System Mutual Aid",
       "GPH", "Public Health Incident",
       "GRD", "Road Department Inc",
       "GRF", "Referral Incident",
       "GSW", "Gunshot Wound",
       "GWX", "Weather Incident",
       "HA", "Headache",
       "HCE", "Heat/Cold Exposure",
       "HL", "Hemorrhage/Laceration",
       "HP", "Heart problems",
       "HZ", "Hazardous Materials",
       "IN", "Inside Investigation",
       "INH", "Carbon Monoxide/Inh/Haz",
       "MA", "Medical Aid",
       "MCI", "Multi-Casualty Incident",
       "MU", "Mutual Aid Requested",
       "OD", "Overdose",
       "OE", "Other Emergency",
       "OI", "Outside Investigation",
       "PS", "Public Service",
       "PSY", "Psychiatric/Abn Behavior",
       "RA", "Residential Alarm",
       "RQ", "Rescue",
       "RR", "Railcar Incident",
       "RS", "Specialized Rescue",
       "SICK", "Sick Person",
       "SOB", "Shortness of Breath",
       "SP", "Fuel Spill",
       "STAB", "Stabbing",
       "SWTR", "Swift Water Rescue",
       "SZ", "Seizures",
       "TD", "Tree Down",
       "TE", "TC with Extrication",
       "TEF", "Frwy TC w/Extrication",
       "TEST", "TEST INCIDENT",
       "TF", "TC with Fire",
       "TFF", "Frwy TC w/Fire",
       "TFR", "Tactical Fire Response",
       "TI", "TC with Injuries",
       "TIF", "Frwy TC w/Injuries",
       "TO", "TC w Vehicle Over Side",
       "TOF", "Frwy TC Over the side",
       "TRAUMA", "Traumatic Injuries",
       "TU", "TC with Unknown Injuries",
       "TUF", "Frwy TC Unk Injuries",
       "UNC", "Unconscious Person",
       "UNKM", "Unk Medical (Man Down)",
       "WS", "Water Salvage",
       "ZAP", "Outside Elec Incident"
   });
 }
