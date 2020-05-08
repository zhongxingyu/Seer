 package net.anei.cadpage.parsers.IN;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.MsgInfo.Data;
 import net.anei.cadpage.parsers.MsgParser;
 
 /*
 Wayne County, IN
 Contact: AJ Locker <aj.locker@yahoo.com>
 Sender: amoore@co.wayne.in.us
 
 The 209 is an apartment or room #. In Indiana, a 10-52 is a medical run. A 10-50 is an MVA. For the complete list of Indiana 10 codes visit http://www.n9wp.com/scan/codes.htm.  
 
 (911) 05/31 20:20 81 S 14TH ST #209 209: Landmark: MERLE HENDERSON APTS  E MAIN ST//S A ST  70 Y/O FEMALE.;CONSC AND ALERT. ABLE TO SPEAK. ; DB JUST STARTED.
 (911) 05/31 19:56 812 NW 5TH ST #LOT 4 10-52: Landmark: HARBOR LIGHTS  NW J ST//INDIANA AV  FEMALE FELL AGAIN FROM A STANDING POSITION NO LOC  SHE IS UP JUST
 (911) 05/31 18:43 2005 VI POST RD 10-52 DB:  TEST RD//SALISBURY RD S  72 Y/O MALE HAS BEEN SICK FOR A FEW DAYS. CONSC AND ALERT.
 (911) 05/31 16:37 E MAIN ST // WOODSIDE DR 10-50 PI: E MAIN ST // WOODSIDE DR  37TH ST//34TH ST  NECK PAIN;STILL IN THE ROAD WAY
 (911) 06/01 10:29 408 S 10TH ST #D 10-52 DB:  S C ST//S E ST  48 Y/O FEMALE. TROUBLE BREATHING HISTORY: COPD
 (911) 06/01 12:20 1212 S 20TH ST 10-52: Landmark: BETHLEHEM MANOR APARTMENTS  S L ST// ACCIDENTAL CUT TO THE HAND..WAITING AT THE ENTRANCE 
 (911) 06/01 13:27 3601 E MAIN ST : Landmark Comment: WALMART PLAZA Landmark: WALMART  37TH ST//34TH ST  ;JUST INSIDE THE GROCERY ENTRANCE
 (911) 06/01 00:00 BEELOR RD // US 27 10-50 UNK: 3081 BEELOR RD S US 27 // LIBERY AV  VEH IN THE DITCH NO DESC
 (911) 06/01 09:43 900 S A ST : Landmark: LELAND RESIDENCE  S 10TH ST//S 9TH ST
 
 */
 
 public class INWayneCountyParser extends MsgParser {
   
   private static final Pattern MASTER = Pattern.compile("(\\d\\d/\\d\\d) (\\d\\d:\\d\\d) ([^:]+): (.*)");
   
   public INWayneCountyParser() {
     super("WAYNE COUNTY", "IN");
   }
   
   @Override
   public String getFilter() {
     return "amoore@co.wayne.in.us";
   }
   
   @Override
   public boolean parseMsg(String subject, String body, Data data) {
     
     if (!subject.equals("911")) return false;
     
     Matcher match = MASTER.matcher(body);
     if (!match.matches()) return false;
     data.strDate = match.group(1);
     data.strTime = match.group(2);
     String sAddr = match.group(3).trim();
     String sExtra = match.group(4).trim();
     
     int pt = sAddr.lastIndexOf(" 10-");
     if (pt >= 0) {
       data.strCall = sAddr.substring(pt+1);
       sAddr = sAddr.substring(0,pt).trim();
     } else {
       data.strCall = "ALERT";
     }
     pt = sAddr.lastIndexOf('#');
     if (pt >= 0) {
       data.strApt = sAddr.substring(pt+1).trim();
       sAddr = sAddr.substring(0,pt).trim();
     }
     sAddr = sAddr.replace("//", "&");
     parseAddress(sAddr, data);
     
     String[] flds = sExtra.split("  +");
     int ndx = 0;
     String fld = flds[ndx];
     if (fld.startsWith("Landmark Comment:")) {
       pt = fld.indexOf(" Landmark:",17);
       if (pt >= 0) {
         data.strPlace = append(fld.substring(pt+10).trim(), "-", fld.substring(17,pt).trim());
       } else {
         data.strPlace = fld.substring(17).trim();
       }
       ndx++;
     }
     
     else if (fld.startsWith("Landmark:")) {
       data.strPlace = fld.substring(9).trim();
       ndx++;
     }
     
     boolean loop = false;
     do {
       loop = false;
       fld = flds[ndx].trim();
       if (fld.contains("//")) {
         data.strCross = fld.replace("//", " & ").replaceAll("  +", " ");
         ndx++;
         if (data.strAddress.equals(data.strCross)) {
           data.strCross = "";
           loop = true;
         }
       }
    } while (loop && ndx<flds.length);
     
     for ( ; ndx<flds.length; ndx++) {
       fld = flds[ndx].trim();
       if (fld.startsWith(";")) fld = fld.substring(1).trim();
       data.strSupp = append(data.strSupp, "  ", fld);
     }
     
     return true;
   }
 }
