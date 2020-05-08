 package net.anei.cadpage.parsers.MD;
 
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.SmartAddressParser;
 
 /* Sample Arnold Pages.
  *
 Anne Arundel County (EMS), MD
 Sender: fd50NNNN@aacounty.org
 
 MEDICAL Box 17-7 1236 JONES STATION RD  [MT CALVARY UNITED METH CHURCH] E171,PM17 TASER PATIENT; 1145 [1/49]  /4m-5/ 
 Box Alarm 12-19 540 BENFIELD RD (CENTER DR) Echo E12,QNT30,E18,E171,QNT23,TK26,SQ23,PM12,BC2,SCA ODOR GAS IN STRUCTURE (HOT); 1351 [2/50]
 Local Alarm 17-12 24 CASCADE RD (GRANDVIEW RD) E171 ALARMS/NON HIGH LIFE (HOT); 1705 [3/51] 
 *MEDICAL BOX 23-5 831 RITCHIE HWY N  [Unit 307A]  [KRIS LEIGH CATERED LIVING] QNT23,PM17,TRO CARDIAC ARREST; 0548 [2/42]  /2d-96/
 *MEDICAL Box 23-12 1244 RITCHIE HWY S  [Unit 18]  [SEVERN COMMERCE CENTER] QNT23,PM17 STROKE; 0921 [3/43]  /lm-2/ 
 *Medical Box 40-83 RT50 W/JENNIFER RD MU40,E40,E171 MVC; 1517 [4/44]  /18d-19/ 
 Box Alarm 23-15 812 WINDSOR RD (FOREST LN) Echo QNT23,E171,E19,TW40,QNT30,SQ23,PM17,BC2,SCA STRUCTURE/BARN/GARAGE (HOT); 1607 [5/45] 
 MEDICAL Box 19-5 1604 ORCHARD WAY (HARMONY ACRES LN) E172,MU39 STROKE; 1643 [6/46] 
 MEDICAL Box 40-26 1724 VINEYARD TRL (RAMSHORN TRL) TW40,PM17 SEIZURES; 0035 [1/41]
 
 Contact: firemann172@yahoo.com
 These were from a screen cap so they may have issues. I asked the user for one's without the ...
 HazMat Box 17-17 1939 BALTO ANNAP BLVD (CARVEL DR) Kilo E171,SQ2,BC2,SU23,SCA,PM39 Chemical ODOR ...
 Local Box 23-6 88 HOYLE LN (BRENDA JOYCE LN) Charlie B17 ELECTRIC WIRES OUTSIDE (COLD); 1404[9...
 Still Box 40-24A 1872 ANNAPOLIS MALL [CHEESCAKE FACTORY] Kilo E172,TK36 ALARMS/MALLS (WARM ...
 Local Box 19-5 1652 PLEASANT PLAINS RD (OLD SAINT MARGARETS RD) Charlie HOLD,U@*,E172 ELECTRIC ...
 MEDICAL Box 19-13 1572 SECRETARIAT DR (MAN O WAR CT) A19,PM17 CHEST PAIN; 1341 [6/284] /13m-1 
 Medical Box 40-83 RT50 E/BESTGATE RD MU40,E40,E172 MVC; 1238 [5/283] /lm-7
 Medical Box 17-QA BRIDE POINT DR /COX [Unit NECK] [RD BX1-10] MOA PM17 MVC; PM17 REQ TAX 3 1133 ...
 
 Contact: Michael Brown <mike.a.brown09@gmail.com>
 *Medical Box 28-8 1496 ANNAPOLIS RD  [BLOCKBUSTER VIDEO] 12-F1 E281,MU29 MVC; 2306 [14/81]  /lm-33/
 *MEDICAL Box 28-13 TELEGRAPH RD/JACKSON GROVE RD 6-K13 E283,MU4 FAINTING; 1328 [9/90]  14m-2
 Medical Box 34-5 7390 BALTO ANNAP BLVD  [MTA LIGHT RAIL CROMWELL] 7-J1 TK33,MU21,A28 ABDOM PAIN; 1256 [8/89]
 *MEDICAL Box 5-22 1325 GREYSWOOD RD (CHAPELVIEW DR) 12-H6 E283,MU6 BREATHING PROB; 1145 [7/88]  22d-20
 *Medical Box 28-8 1496 ANNAPOLIS RD  [BLOCKBUSTER VIDEO] 12-F1 E281,MU29 MVC; 2306 [14/81]  lm-33
 *Medical Box 28-27 PINEY ORCHARD PKWY/RIVERSCAPE RD 12-E7 E281,PM26 MVC; 1415 [6/91]  lm-8
 MEDICAL Box 28-27 RIVERSCAPE RD/RUNNING WOLF TRL 12-F8 E283,MU21 CHEST PAIN; 2010 [13/78]
 Local Box 28-27 2615 LOTUSWOOD CT (RIVERSCAPE RD) 12-E7 E283 SERVICE CALL (COLD); 1740 [9/293]
 *MEDICAL Box 28-27 8718 THORNBROOK DR (RIVERSCAPE RD) 12-E7 E281,MU6 HEADACHE; BURNING INSIDE  2001 [15/297]  41m-1
 *MEDICAL Box 28-27 8700 NATURES TRAIL CT  [Unit 304] (RIVERSCAPE RD) 12-E8 A28,PM5 BREATHING PROB; 0524 [4/313]  6m-5
 *Medical Box 28-6 2012 BEALMEAR MILL LN  [Unit 7208] (COWMAN CT) 12-G1 E281,MU29 BACK PAIN; 0539 [1/68]  25d-2
 
 Contact: Len Pfeffer <copolenny@gmail.com>
 *Local Box 17-13 1641 WINCHESTER RD  [MONTESSORI INTL CHILDRENS HOME] E171 HI LIFE/ALARMS SOUNDING (WARM); 1243 [6/88]  /9d-7/
 MEDICAL Box 23-16 254 PENDLETON CT (WAYCROSS WAY) QNT23,PM17 BREATHING PROB; 0911 [3/127]
 
Contact: Randy Hopkins <firemann172@yahoo.com>
MEDICAL BOX 19-6 824 HOLLY DR E A19,PM17,TRO MVC; 1808 PM2 [3/25]\n

  */
 
 public class MDAnneArundelCountyEMSParser extends SmartAddressParser {
   
   private static final String DEF_STATE = "MD";
   private static final String DEF_CITY = "ANNE ARUNDEL COUNTY";
   
   private static final Pattern MARKER = Pattern.compile("^\\*?(?:MEDICAL|Medical|Local|HazMat|Still|Box) (?:BOX|Box|Alarm) (\\d{1,2}-[A-Z0-9]{1,3}) ");
  private static final Pattern T_MARKER = Pattern.compile(";? \\d{4} (?:[A-Z0-9]+ )?\\[\\d{1,2}/\\d{1,3}\\]");
   private static final Pattern OPEN_DELIM = Pattern.compile("\\(|\\[");
   private static final Pattern MAP1 = Pattern.compile(" (\\d{1,2}-[A-Z]\\d{1,2}) ");
   private static final Pattern MAP2 = Pattern.compile("\\d{1,2}-[A-Z]\\d{1,2}");
   private static final Pattern T_MARKER2 = Pattern.compile(";? \\d{4}\\b");
 
   public MDAnneArundelCountyEMSParser() {
     super(DEF_CITY, DEF_STATE);
   }
   
   @Override
   public String getFilter() {
     return "@aacounty.org";
   }
 
   @Override
   protected boolean parseMsg(String body, Data data) {
     
     // Check for starting signature
     Matcher match = MARKER.matcher(body);
     if (! match.find()) return false;
     data.strBox = match.group(1);
     body = body.substring(match.end()).trim();
     
     // End signature is optional, but we need to get rid of it lest it
     // confuse the bracket logic that follows
     match = T_MARKER.matcher(body);
     if (match.find()) body = body.substring(0, match.start()).trim();
     
     // OK, there are two ways to go from here.  If we find a open paren or square 
     // bracket, that definitively marks the end of the address
     match = OPEN_DELIM.matcher(body);
     if (match.find()) {
       int ipt = match.start();
       parseAddress(body.substring(0,ipt).trim(), data);
       body = body.substring(ipt);
       
       // Following the address, we may find a place name in square brackets or
       // a cross street in parenthesis.  In either order
       while (body.length() > 0) {
         char stch = body.charAt(0);
         char endch = 0;
         if (stch == '(') endch=')';
         else if (stch == '[') endch = ']';
         else break;
         Parser p = new Parser(body.substring(1));
         String fld = p.get(endch).trim();
         body = p.get();
         
         if (stch == '(') {
           data.strCross = append(data.strCross, " & ", fld);
         } else {
           if (fld.startsWith("Unit ")) {
             data.strApt = fld.substring(5).trim();
           } else {
             data.strPlace = append(data.strPlace," - ", fld);
           }
         }
       }
     }
     
     // No brackets, maybe we can find a map indicator, which would mark the end
     // of the address
     else if ((match = MAP1.matcher(body)).find()) {
       parseAddress(body.substring(0,match.start()).trim(), data);
       body = body.substring(match.start()).trim();
     }
     
     // If we didn't find a bracket terminator or map code, we will have to rely
     // on the smart parser to find the address
     else {
       parseAddress(StartType.START_ADDR, body, data);
       body = getLeft();
     }
     
     // We aren't done yet.
     // This might be followed by map code, or by a camel case priority
     // In any case the next item will be a unit designation
     // We'll identify the camel case name if the second character is lower case
     Parser p = new Parser(body);
     String token = p.get(' ');
     if ((match = MAP2.matcher(token)).matches()) {
       data.strMap = token;
       token = p.get(' ');
     } else if (token.length() >= 2 && Character.isLowerCase(token.charAt(1))) {
       data.strPriority = token.substring(0,1);
       token = p.get(' ');
     }
     data.strUnit = token;
     body = p.get();
     
     // Anything left up to a possible trailing marker is the call description
     match = T_MARKER2.matcher(body);
     if (match.find()) body = body.substring(0, match.start()).trim();
     data.strCall = body;
 
     return true;
   }
 }
