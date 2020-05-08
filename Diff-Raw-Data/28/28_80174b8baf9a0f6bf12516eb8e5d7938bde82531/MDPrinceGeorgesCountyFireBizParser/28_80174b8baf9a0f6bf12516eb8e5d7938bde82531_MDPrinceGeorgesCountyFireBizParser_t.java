 package net.anei.cadpage.parsers.MD;
 
 import java.util.Properties;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.SmsMsgParser;
 
 /*
 Prince Georges County, MD
 for Prince Georges, Anne Arundel, and Charles counties. and Howard County
 Contact: charles woodland <lollipopjuggs@gmail.com>
 Contact: Russ <rcfireblitz@gmail.com>
 System: Fireblitz
 From: @fireblitz.com
 
 55: BUILDING FIRE\nE809 TK809 BO804\n4637 37TH ST (ALLISON ST & DEAD END)\n12/07 04:51\nhttp://fireblitz.com/09/9.html
 0-2 H12: PI\nS11 R11\nPG INDIAN HEAD HWY/FARMINGTON RD WEST\nNotes: WANTS ENGINE AND AMBULANCE FROM STATION 11\n12/07 06:14\nhttp://fireblitz.com/11/0.html
 26: INVESTIGATION\nE817\n6036 PARKLAND CT (OLD SILVER HILL RD & OLD SILVER HILL RD)\n12/06 22:40\nhttp://fireblitz.com/17/9.html
 48: FD ACCIDENT\nSQ818 B802\nANNAPOLIS RD/PRINCESS GARDEN PY (9006 ANNAPOLIS RD/5900 PRINCESS GARD\n12/07 07:02\nhttp://fireblitz.com/18/6.html
 24: OVERDOSE - ALS\nA824 MD847, CHARLIE OD/NOT ALRT\n1605 AIRPORT LN (SCHALL RD & BERRY RD)\n12/07 07:14\nhttp://fireblitz.com/24/8.html
 27: INJURED/SICK\nA827\n6304 MAXWELL DR #4 (CARSWELL AV & MORRIS AV)\n12/07 05:14\nhttp://fireblitz.com/27/2.html
 46: BUILDING FIRE\nE833 TK833 BO802\n1200 CAPITAL CENTER BL (BLVD AT THE CAP CTR)\n12/07 00:51\nhttp://fireblitz.com/33/9.html
 46: BUILDING FIRE\nE837 TK837 BO802\n1200 CAPITAL CENTER BL (BLVD AT THE CAP CTR)\n12/07 00:51\nhttp://fireblitz.com/37/5.html
 49: MEDIC LOCAL\nA849\n13218 DEERFIELD RD (DEAD END & MATTHEWS CT)\n12/06 23:00\nhttp://fireblitz.com/49/1.html
 48: TOWNHOUSE FIRE\nE818 BO802\n9903 BREEZY KNOLL CT [DEAD END & GREEN HAVEN RD]\n12/23 23:32\nhttp://fireblitz.com/18/8.shtm

Contact: Ricky Poole <handgunhunting@gmail.com>
45: PIA-HIGHWAY\nSQ827\nNB CRAIN HWY SE/NB HEATHERMORE BLVD, PP - <8322/ 0>\n07/05 10:02\nhttp://fireblitz.com/27/0.shtm
27: PIA-HIGHWAY\nA827 SQ827\nSB CAP BELT IL A HWY/NB BRANCH AVE, PP\n07/06 10:20\nhttp://fireblitz.com/27/5.shtm

  */
 
 
 public class MDPrinceGeorgesCountyFireBizParser extends SmsMsgParser {
   
   private static final Properties COUNTY_CODES = buildCodeTable(new String[]{
       "PG", "PRINCE GEORGES COUNTY",
       "CH", "CHARLES COUNTY",
       "AA", "ANNE ARUNDEL COUNTY",
       "HO", "HOWARD COUNTY",
       "DC", "WASHINGTON"
   });
   
   public MDPrinceGeorgesCountyFireBizParser() {
     super("PRINCE GEORGES COUNTY", "MD");
   }
   
   @Override
   public String getFilter() {
     return "@fireblitz.com";
   }
 
   @Override
   protected boolean parseMsg(String body, Data data) {
     
     String[] lines = body.split("\n");
     if (lines.length != 5 && lines.length != 6) return false;
     if (! lines[lines.length-1].startsWith("http://fireblitz.com/")) return false;
     
     int ndx = 1;
     for (String line : lines) {
       line = line.trim();
       
       switch (ndx) {
         
         // First line contains a station ID & map page followed by a colon and call description
       case 1:
         Parser p = new Parser(line);
         String station = p.get(':');
         data.strCall = p.get();
         
         // If the station contains a dash, there is a map page
         p = new Parser(station);
         data.strMap = p.getLastOptional('-');
         station = p.get();
         
         // First two characters *MIGHT* be a county code
         if (station.length() >= 2) {
           String code = station.substring(0,2);
           String county = COUNTY_CODES.getProperty(code);
           if (county != null) {
             data.defCity = county;
             if (code.equals("DC")) data.defState = "DC";
             station = station.substring(2);
           }
         }
         
         // Anything left is the response station
         data.strSource = station;
         ndx++;
         break;
         
         // Second line is units
       case 2:
         p = new Parser(line);
         data.strUnit = p.get(',');
         ndx++;
         break;
         
         // Third line is address, possibly with cross streets in parens
         // or square brackets
       case 3:
         if (line.startsWith("PG ")) line = line.substring(3).trim();
         int pt1 = line.indexOf('(');
         if (pt1 < 0) pt1 = line.indexOf('[');
         String sAddress;
         if (pt1 < 0) {
           sAddress = line;
         } else {
           sAddress = line.substring(0, pt1).trim();
           char cEnd = (line.charAt(pt1)=='(' ? ')' : ']');
           int pt2 = line.indexOf(cEnd, pt1+1);
           if (pt2 < 0) pt2 = line.length();
           data.strCross = line.substring(pt1+1, pt2);
         }
        pt1 = sAddress.indexOf(',');
        if (pt1 >= 0) sAddress = sAddress.substring(0, pt1).trim();
         parseAddress(sAddress, data);
         ndx++;
         break;
         
         // Fourth line may have notes
       case 4:
         if (line.startsWith("Notes:")) {
           data.strSupp = line.substring(6).trim();
           break;
         }
         
         // But should eventually have the firebiz URL
         if (line.startsWith("http://")) {
           if (data.strSupp.length() > 0) data.strSupp += '\n';
           data.strSupp += line;
           ndx++;
           break;
         }
       }
     }
     
     return true;
   }
 }
