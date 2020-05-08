 package net.anei.cadpage.parsers.NY;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.SmartAddressParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 
 /*
 Niagara County, NY
 Contact: Nicholas Baes <tcfcbaes04@gmail.com>
 Contact: 7167992315@vtext.com
 Contact: Mark Mccabe <markmccabe1423@gmail.com>,EMT1423@vtext.com
 Sender: 7164383393@niagaracounty.com
 
 ((12474) NCFC MMYERS ) 3111 Sndrs Stlmnt Rd-NCCC-C Bldg unk age f labor pains no further info. BLSP EMS/Air Ops. 1617hrs numerous other calls in cnty. From email 7164383393@niagaracounty.com
 (10444 NCFC JBEILEIN ) DUERO/WILLIAMS  MVA W MINOR INJURIES. From email 7164383393@niagaracounty.com
 (550 NCFC RZASTROW ) 1-AMB TO MVA WITH SHAWNEE/LKPT & SHAWNEE RDS. From email 7164383393@niagaracounty.com
 ((61571) NCFC TLEADERS ) ALS requested by Barker 1382 Quaker rd
 (61571 NCFC TLEADERS ) ALS requested by Barker 1382 Quaker rd hope this helps
 (59339 NCFC JABROWN ) 6331 Bayview Station leaking hot water tank
 (59687 NCFC MDIPASQU ) 1691,,LOCKPORT,ST,,80 YOF FELL SINK BLS-P BACK HALL
 (60104 NCFC HBABIS ) 3445 murphy rd/74yof weak/ 8/10 1611
 (60395 NCFC HBABIS ) Fire alarm act/eastern niagara newfane hosp 2600 williams st/8/10 1852
 ((6414) NCFC HBABIS ) 6061 edward av 18/94yof incoherant cons/br 9/27 1746
 ((6492) NCFC HBABIS ) 5981 ide rd fire alarm act 2nd floor 9/27 1813
 ((9597) NCFC RZASTROW ) fire alm newfane rehab ctr/2709 transit rd/olcott s-by in qtrs/OPS 1
 ((7792) NCFC MMYERS ) Newfane Elem School - fire alarm - PER 11M7 stand down burnt popcorn.  1602hrs
 ((8038) NCFC MMYERS ) Hartland Fire Hall - 34 yof in p/u truck seizing. ALSP EMS/Air Ops 2034hrs
 ((9307) NCFC RZASTROW ) NEWF ELEM SCHOOL/2909 TRANSIT RD/RM 141 HEAT DET ACTIVATED/OPS 1
 ((10575) NCFC TKONG ) #681 EMS 2171 HESS RD  80/F scald burns to arms and forehead
 ((13341) NCFC MDIPASQU ) 1608 FRANKLIN 59 YOF DIF BREATHING ALS-P
 ((7720) NCFC MMYERS ) 4895 Gasport Rd - m in 50s in driveway was uncon now conscious. ALSP EMS/Air Ops RM264 from Ward/Lkpt 1551hrs
 ((8994) NCFC RZASTROW ) FIRE POLICE TO MVA/ROCHESTER & BOLTON RDS.
 [(11511] NCFC WWALKER ) ASSIST GASPORT WITH A LIFT ASSIST 4540 LINCOLN DR 311D 10/1/11 1834
 ((17295) NCFC JBEILEIN ) 8815 CHESTNUT RIDGE RD 35YOF SEVERE UPPER ABDOMINAL PAIN ALSO BACK PAIN ALS P
 ((18371) NCFC RZASTROW ) car fire/mill & gasport rds/ops 1
 [(35020] NCFC JBEILEIN ) 7963 CHESTNUT RIDGE RD 85YOF ILL PARTY LEG PAIN BLS S
 ((37315) NCFC JBEILEIN ) 25E2 OUT OF SERVICE UNTIL FURTHER NOTICE
 [(7561] NCFC RYUREK ) IF YOU PLAN TO ATTEND THE FIRE PREVENTION EVENT ON 10/5, PLEASE MEET AT GASPORTS MAIN ALL AT 0800 PER 6M7
 ((44203) NCFC MSTEEN ) 6107 strauss rd apt g for ems alarm no contact
 ((35250) NCFC JBEILEIN ) 5082 RIDGE RD FIRE ALARM ZONE 21 SMOKE DETECTORS
 ((38762) NCFC JBEILEIN ) LOCKPORT JUNCTION/UPPER MOUNTAIN RD MVA CAR STRUCK BUS BLS P
 [(44051] NCFC KTOMTISH ) 5460 Ernest Rd - 82 YOF - Pace Maker pain - Radiating down left arm into fingers - EMS Opps on location
 ((27156) NCFC RYUREK ) o & c requested by rapids 7588 dysinger rd call fire control if available 10/11 1822
 ((3559) NCFC RYUREK ) ma/ ambulance to rapids 6718 riddle rd 71m fell, back pain 09/26 1012
 ((41014) NCFC JBRUCE ) 8376 AKRON RD 82 YOF DIF BREATHING/ALS PRIOR
 ((62154) NCFC CGREENE ) Requested by Hartland for Dump Truck fire behind Chapman's Market - 9202 Ridge Rd - Call Fire Control if responding. 09/21/11
 ((42817) NCFC MDIPASQU ) 6699 Wolcottsville rd 52 yof sick dehidrated bls-p
 ((42996) NCFC MDIPASQU ) 42 ALEXANDER PK 82 F FELL UNKNOWN INJ POSS LIFT ASSIST BLS-S
 ((41328) NCFC JBRUCE ) EMS ALARM @ 6666 SHETRAM RD
 (45676) NCFC DLEVEN) 425 West Lane/CO Detector with no symptoms/Ops 1
 (43218) NCFC TLEADERS 454 lockport st. 91 yom difficulty breathing. ALS-P 31R9 is at ECMC returning
 
 Contact: David Sanders <skiestwo@gmail.com>
 Sender: 7164383393@niagaracounty.com
 ((23406) NCFC CGREENE ) EMS @ 7300 Winbert Dr - 45yof Chest Pain - 01/06/12 @ 0442/hrs - EMS OPS  ALSP
 
Contact: 7163168535@messaging.nextel.com
((64253) NCFC CGREENE) FIRE ALM @ Wilson Central School - 374 Lake St (Main Bldg) - OPS 1 - 01/31/12 @ 0355/hrs

 */
 
 
 public class NYNiagaraCountyParser extends SmartAddressParser {
   
     private static final Pattern SUBJECT_PTN = Pattern.compile("[\\(\\[]?(\\d+)[\\)\\]]? NCFC .*");
     private static final Pattern MARKER1_PTN = Pattern.compile("[\\(\\[]?(\\d+)");
     private static final Pattern MARKER2_PTN = Pattern.compile("^NCFC [A-Z]+ *[\\)\\] ]");
     
     private static final Pattern DATE_TIME_PTN = Pattern.compile("\\b(\\d{1,2}/\\d{1,2}(?:/\\d{2,4})?) (\\d{4})$");
     private static final Pattern TIME_PTN = Pattern.compile("\\b(\\d{4})(?:hrs)?$");
     private static final Pattern DATE_TIME_PTN2 = 
       Pattern.compile("(?: - *)?(?:(\\b\\d\\d/\\d\\d/\\d\\d(?:\\d\\d)?))?(?: *@ *)?(\\b\\d{4}) */?hrs(?: *- )?");
     
    private static final Pattern CALL_AT_PTN = Pattern.compile("^([A-Z ]+)@");
    
     public NYNiagaraCountyParser() {
       super("NIAGARA COUNTY", "NY");
     }
     
     @Override
     public String getFilter() {
       return "@niagaracounty.com";
     }
 
 	  @Override
 	  protected boolean parseMsg(String subject, String body, Data data) { 
 	    
 	    do {
 	      Matcher match = SUBJECT_PTN.matcher(subject);
 	      if (match.matches()) {
 	        data.strCallId = match.group(1);
 	        break;
 	      }
 	      
 	      Matcher match1 = MARKER1_PTN.matcher(subject);
 	      if (match1.matches()) {
 	        Matcher match2 = MARKER2_PTN.matcher(body);
 	        if (match2.find()) {
 	          data.strCallId = match1.group(1);
 	          body = body.substring(match2.end()).trim();
 	          break;
 	        }
 	      }
 	      return false;
 	    } while (false);
 	    
 	    String save = body;
 	    
 	    Matcher match = DATE_TIME_PTN.matcher(body);
 	    if (match.find()) {
 	      data.strDate = match.group(1);
 	      data.strTime = expandTime(match.group(2));
 	      body = body.substring(0,match.start()).trim();
 	    } else {
 	      match = TIME_PTN.matcher(body);
 	      if (match.find()) {
 	        data.strTime = expandTime(match.group(1));
 	        body = body.substring(0,match.start()).trim();
 	      }
 	    }
 	    
 	    match = DATE_TIME_PTN2.matcher(body);
 	    if (match.find()) {
 	      String sDate = match.group(1);
 	      if (sDate != null) data.strDate = sDate;
	      data.strTime = expandTime(match.group(2));
 	      body = body.substring(0,match.start()).trim() + " " + body.substring(match.end()).trim();
 	    }
 	    
 	    body = body.replace(',', ' ').replace('.', ' ').replaceAll("//s+", " ");
	    
	    StartType st = StartType.START_CALL;
	    match = CALL_AT_PTN.matcher(body); 
	    if (match.find()) {
	      data.strCall = match.group(1).trim();
	      body = body.substring(match.end()).trim();
	      st = StartType.START_PLACE;
	    }
	    
	    parseAddress(st, FLAG_NO_IMPLIED_APT, body, data);
 	    if (getStatus() == 0) {
 	      data.strCall = "GENERAL ALERT";
 	      data.strAddress = "";
 	      data.strPlace = save;
 	      data.strDate = "";
 	      data.strTime = "";
 	      return true;
 	    }
 	    
 	    data.strSupp = unDash(getLeft());
 	    
 	    data.strCall = unDash(data.strCall);
 	    if (data.strCall.length() == 0) {
 	      data.strCall = data.strSupp;
 	      data.strSupp = "";
 	    }
 	    
	    data.strPlace = unDash(data.strPlace);
 	    return true;
 	  }
 
     private String expandTime(String time) {
       return time.substring(0,2) + ":" +time.substring(2,4);
     }
     
     private String unDash(String field) {
       if (field.startsWith("- ")) field = field.substring(2).trim();
       if (field.endsWith(" -")) field = field.substring(0,field.length()-2).trim();
       return field;
     }
 	}
 	
