 package net.anei.cadpage.parsers.dispatch;
 
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.FieldProgramParser;
 
 /*
 Gasconade County, MO
 
 MESSAGE / M50 CHEST PAIN  4449 MARIES CR 426 OUT OF COUNTY MapRegions:  CrossStreets: Description:  Dispatch: 3/25/2011 03:51:43\n\n\n\n\n
 MESSAGE / M51 MOTOR VEHICLE ACCIDENT-INJURIES  HWY 89  MapRegions:  CrossStreets: Description:  Dispatch: 3/26/2011 11:29:32\n\n\n\n\n
 MESSAGE / M51 MOTOR VEHICLE ACCIDENT-UNKNOWN INJ  HWY 89  MapRegions:  CrossStreets: Description:  Dispatch: 3/26/2011 11:39:53\n\n\n\n\n
 M51 FALL- PERSON FELL 408 N ALVARADO AVE BELLE MapRegions: CrossStreets: E BELAIR AVE 0.03 mi S E ROHRER DR 0.13 mi N Description: Dispatch: 3/24/2011 11:47:54
 M50 SICK PERSON- SICK CASE 4644 HIGHWAY B, Apt. BOX 14 GASCONADE COUNTY MapRegions: PAGE 103 CrossStreets: ELK HEAD RD 0.08 mi SW RED BIRD RD 0.46 mi N Description: HALF MILE SOUTH OF REDBIRD RD Dispatch: 3/24/2011 11:25:45
 M50 SICK PERSON- SICK CASE 901 W EIGHTH ST BELLE MapRegions: CrossStreets: S SHOCKLEY AVE 0.01 mi NE S APPLE AVE 0.19 mi E Description: Dispatch: 3/24/2011 21:05:48
 
 Contact:  bretth530 <bretth530@gmail.com>
 501 FALL- PERSON FELL FRENE VALLEY HEALTHCARE SOUTH 1016 W HIGHWAY 28 OWENSVILLE MapRegions: OFD SECTOR 1, PAGE 072 CrossStreets: HIGHWAY Y 0.05 mi N S L&D DR 0.44 mi SW Description: 96 YOA FALL BROKEN WRIST Dispatch: 4/3/2011 06:00:43
 501 ACCIDENTAL INJURY  1009 SPRINGFIELD RD, Apt. APT 21 OWENSVILLE MapRegions: OFD SECTOR 1, PAGE 065, PAGE 122 CrossStreets: CHERRY AVE 0.06 mi SE INDUSTRIAL DR 0.22 mi NE Description:  Dispatch: 4/6/2011 12:08:04\n\n\n\n
 O164 O171 O172 501 MOTOR VEHICLE ACCIDENT WITH RESCUE GASCONADE COUNTY E-911 405 E LINCOLN AVE OWENSVILLE MapRegions: OFD SECTOR 1, PAGE 073, PAGE 123 CrossStreets: OLIVE ST 0.08 mi E N WALNUT ST 0.04 mi W Description: TEST CALL FOR OFD***********TEST**********TEST Dispatch: 5/10/2011 1:39:36 PM Dispatch: 5/10/2011 1:39:36 PM Dispatch: 5/10/2011 1:39:36 PM Dispatch: 5/10/2011 1:39:36 PM
 O164 O171 O172 O170 501 726 O160 GAS LEAK OUTSIDE  2935 LAKE NORTHWOODS RD OWENSVILLE MapRegions: OFD SECTOR 1, PAGE 057, PAGE 117 CrossStreets: HIGHWAY 19 0.22 mi W Description: STRUTMANS. PROPANE TANK ROLLED OVER.\n\nCALLER STATES THAT HE WAS MOWING GRASS AND SAW THE PROPANE TANK ROLL OVER WITH GAS LEAKING . CALLER STATES THAT THE TANK IS ABOUT 50 FEET    FROM THE HOUSE. INITIAL CALLER STATES THAT HE KNOCKED ON THE DOOR WITH NO ANSWER, CALLER IS CALLING FROM THE NEIGHBORS HOUSE WHICH IS ALSO THE SISTER. ADVISED CALLER TO STAY AWAY AND STAY SAFE. CALLER IS STATING A VERY STRONG SMELL OF GAS . \n CALLED THE RP BACK AN\n More?
 502 TRAUMATIC INJURY  412 S CUBA ST OWENSVILLE MapRegions: OFD SECTOR 1, PAGE 073, PAGE 125 CrossStreets: E JEFFERSON AVE 0.06 mi N E MADISON AVE 0.02 mi SW Description:  Dispatch: 5/11/2011 18:14:27\n\n\n\n
 
 Ray County, MO
 
 RCAD WHFD FALLS 514 ARAPAHOE DR  HOMESTEAD VILLAGE CrossStreets: CHEROKEE DR 0.05 mi W HIGHWAY Y 0.16 mi E Description: BETTY JAMES 85 YO FEMALE\nFAL
 LFD EMS 31004 NOTTINGHAM LN   Description:  CrossStreets: SILVEY RD 6443.06 mi NW SILVEY RD 6443.06 mi NW
 WHFD RCAD FALLS 33408 W 160TH ST  RAY COUNTY Description: 91 yof\nfell from standing position\nconsious and breathing\ncomplaining of l hip pain\nup in 
 WHFD RCAD RCSO DEATH 32534 MAGNOLIA LN  RAY COUNTY Description: 61 yo female\n
 WHFD RCAD TRAUMATIC INJURIES (SPECIFIC) 2062 E RIDGE DR  RAY COUNTY Description: 15 YO MALE\nBICYLCE ACCIDENT\nFELL FROM BIKE\nCONSCIOUS\nBREATHING\nNO S
 WHFD RCAD M43 800 ABDOMINAL PAIN 12595 ORRICK RD  RAY COUNTY Description: 28 YOLD FEMALE \nCONS AND BREATH \nSEVERE STOMACH PAIN LOWER ABDOMEN \nLUPUS
 
 LMED1 LSQ1 L602 SICK PERSON (SPECIFIC DIAGNOSIS) 215 E 5TH ST 3A LAWSON Description: 123254415\nPT IS PATRICIA BIDDIX\nHIGH BP\n67 YO FEMALE\nBURNING SE
 LMED1 HEART PROBLEM / AICD 618 SHEPHERD LN   Description: 17 YO Rapid Heart rate\n\n
 LMED1 LSQ1 L602 SICK PERSON (SPECIFIC DIAGNOSIS) 316 KINGS DR  LAWSON Description: 90 yof\nill since for in morning\nhigh sugar\nbreathing difficulty\nu
 LMED1 LSQ1 L602 SEIZURE 34030 W 204TH ST  RAY COUNTY Description: camp wilderness retreat\n13/f\nbreathing\ncons\nseizing at this time\n1st seizure for a
 LMED1 LSQ1 L602 LACERATION 202 E MOSS ST  LAWSON Description: puncture wound in l foot\n42 yof\nconsious and breathing\nnausea CrossStreets: S DONIPHAN
 
 800 WHP1 WHT2 WHSQ1 LP4 LP1 LT1 OT2 RFD RCAD WHFD WHR1 FIRE STRUCTURE 11783 SIEGEL CEMETERY RD  RAY COUNTY Description: EXCELSIOR SPRINGS\nOUTSIDE CI
 800 WHB1 WHT2 WHFD WHR1 FIRE NATURAL COVER OR BRUSH 33637 HIGHWAY U  RAY COUNTY Description: SMALL BRUSH FIRE THAT IS NEXT TO A CROP FIELD\nRP IS STA
 WHP1 800 WHFD WHR1 FIRE ALARM 34684 HIGHWAY 10  RAY COUNTY Description: gen fire alarm. covers all zones\n\nkeyholder ron rouse 816-864-4434
 
 107 N RAYMORE ST  WOOD HEIGHTS WHFD RCAD RCSO 800 M42 WHR1 678 Clear: 22:48:39 Available: 23:34:02
 15595 BLACKBERRY TRAIL  WHFD 800 LFD 802 LT1 LP1 WHP1 Dispatch: 5/24/2011 23:15:33 Enroute: 23:15:35 OnScene: 23:23:58
 15141 W COUNTY LINE RD  RAY COUNTY WHFD RCAD 671A 675A 800 M44 WHR1 Clear: 22:20:51 Available: 23:05:20
 
 */
 
 
 public class DispatchGlobalDispatchParser extends FieldProgramParser {
   
   private Pattern stationPtn;
   private Pattern unitPtn;
   
   public DispatchGlobalDispatchParser(String[] cityList, String defCity, String defState,
                                        Pattern stationPtn, Pattern unitPtn) {
     super(cityList, defCity, defState,
            "ADDR/SC! MapRegions:MAP Description:INFO CrossStreets:X Description:INFO Dispatch:SKIP");
     this.stationPtn = stationPtn;
     this.unitPtn = unitPtn;
   }
   
   @Override
   public boolean parseMsg(String body, Data data) {
     if (! body.contains(" Description:")) return false;
     if (! super.parseMsg(body, data)) return false;
     if (data.strCall.length() == 0) return false;
     return true;
   }
   
   private class MyAddressField extends AddressField {
     
     @Override
     public void parse(String field, Data data) {
       field = field.replaceAll(",", "");
       
       // If we have station or unit patterns, these need to be stripped off 
       // the front and back of the address field
       if (stationPtn != null || unitPtn != null) {
         
         // Start by splitting field into list of words, and identifying
         // each word as station, unit or neither
         int firstAddr = -1;
         int lastAddr = -1;
         String[] words = field.split(" +");
         int[] types = new int[words.length];
         for (int ii = 0; ii<words.length; ii++) {
           String word = words[ii];
           types[ii] = (stationPtn != null && stationPtn.matcher(word).matches() ? 1 :
             unitPtn != null && unitPtn.matcher(word).matches() ? 2 : 0);
           if (types[ii] == 0) {
             if (firstAddr < 0) firstAddr = ii;
             lastAddr = ii;
           }
         }
         
         // We only pull words from either end, which means anything between the
         // first regular word and the last regular word should be treated as a
         // regular word
         for (int ii = firstAddr+1; ii<lastAddr; ii++) types[ii] = 0;
         
         // Construct three Stringbuilders with all of the regular, station, and unit words
         StringBuilder[] sba = new StringBuilder[3];
         for (int ii = 0; ii < 3; ii++) sba[ii] = new StringBuilder();
         for (int ii = 0; ii<words.length; ii++) {
           StringBuilder sb = sba[types[ii]];
           if (sb.length() > 0) sb.append(' ');
           sb.append(words[ii]);
         }
         
         // And finally convert the StringBuilders back to the appropriate fields
         field = sba[0].toString();
         data.strSource = sba[1].toString();
         data.strUnit = sba[2].toString();
       }
       super.parse(field, data);
     }
     
     @Override
     public String getFieldNames() {
       return "SRC UNIT " + super.getFieldNames();
     }
   }
   
   
   @Override
   public Field getField(String name) {
     if (name.equals("ADDR")) return new MyAddressField();
     return super.getField(name);
   }
 }
