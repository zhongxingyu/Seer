 package net.anei.cadpage.parsers.TX;
 
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.dispatch.DispatchOSSIParser;
 
 /* 
 Humble TX, (Harris County)
 Contact: "Wassell, Michael" <MWassell@avfd.com>
 
 [AVFD EMS] - Cardiac -- 17111 Dawn Shadows Dr, Humble -- Map: 376H- -- Xst's: Forest Rain Ln Old Arbor -- Units: E-M29 -- 201105590
 [AVFD EMS] - Diabetic Emergency -- 5310 Moonshadows Dr, Humble -- Map: 337S- -- Xst's: Pocito Ct Wellswood Ct -- Units: E-M29 -- 201105542
 [AVFD EMS] - Medical Call -- 6810 Amber Ash Ct, Humble -- Map: 337Q- -- Xst's: Crimson Oak Trl -- Units: E-M19 -- 201105487
 [AVFD FIRE] - Fire Alarm - Residential -- 19222 Pinewood Mist Ln, Humble -- Map: 338W- -- Xst's: Pinewood Canyon Ln Pinewo -- Units: ATFD E-E39 -- 201105452
 [AVFD FIRE] - Motor Vehicle Incident -- E Fm 1960 & w Lake Houston P, Humble -- Map: 337Z- -- Xst's: W Lake Houston Pkwy Atasc -- Units: E-M19 E-E39 - 201105443
 [AVFD FIRE][Repage] - Mvi - Unknown -- Will Clayton Pkwy & atascoci -- Map: 376C- -- Xst's: Atascocita Rd Hunters Ter -- Units: E-M29 E-E39 - 201105192 
 
  */
 
 public class TXHumbleParser extends DispatchOSSIParser {
   
   // Pattern to find a single dash delimiter followed by a numeric ID token
   private static Pattern TRAIL_DELIM = Pattern.compile(" - (?=\\d+$ *)");
   
   public TXHumbleParser() {
     super("HUMBLE", "TX",
            "CALL ADDRCITY! Map:MAP Xsts:X Units:UNIT ID");
   }
   
   @Override
   public boolean parseMsg(String subject, String body, Data data) {
     
     // Strip off leading single dash
     // If last field delimiter is a single dash, turn it to a double dash
     data.strSource = new Parser(subject).get('|');
     if (body.startsWith("- ")) body = body.substring(2).trim();
     body = TRAIL_DELIM.matcher(body).replaceFirst(" -- ");
     
     // Split line into double dash delimited fields and process them
     body = body.replace("Xst's:", "Xsts:");
    return parseFields(body.split(" -- "), data);
   }
   
   @Override
   public String getProgram() {
     return "SRC " + super.getProgram();
   }
 }
