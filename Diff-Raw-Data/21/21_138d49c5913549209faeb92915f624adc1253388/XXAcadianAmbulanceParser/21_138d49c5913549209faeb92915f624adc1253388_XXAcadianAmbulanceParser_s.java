 package net.anei.cadpage.parsers.general;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.FieldProgramParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /*
Acadian Ambulance, LA (also TX, and MS)
 Contact: Heath Manson <heath.manson@gmail.com>
 Sender: commcenter@acadian.com
 
 Texas
 Resp#9879458 Non Life Threatening EmergencySick Person (Specific Diag.) Loc: Add:228 Birchwood Dr APT: Cross St:Ridgewood Dr/E Old Field Dr City:Huffman Cnty: Harris Map Pg: Dest: Pt's Name:
 Resp#9881088 Life Threatening Emergency Breathing Problem Loc: Add:25809 Lakewater Dr APT: Cross St:Waterwood Dr/Dead End City:Huffman Cnty: Harris Map Pg: Dest: Pt's Name:
 Resp#9881174 Life Threatening Emergency Heart Problems Loc: Add:187 Cypress St APT: Cross St:Dead End/Main St City:Harris County Cnty: Harris Map Pg: Dest: Pt's Name:
 Resp#9881442 Life Threatening Emergency Heart Problems Loc: Add:24212 Fm 2100 Rd APT: Cross St:Unnamed Street/3rd St City:Huffman Cnty: Harris Map Pg: Dest: Pt's Name:
 Resp#9881815 Non Life Threatening EmergencySick Person (Specific Diag.) Loc: Add:2206 Stag Cir APT: Cross St:Rain Forest Dr/Dead End City:Huffman Cnty: Harris Map Pg: Dest: Pt's Name:
 Resp#9882387 Life Threatening Emergency Breathing Problem Loc: Add:58 Short Leaf Dr APT: Cross St:Hickory Ridge Dr/Magnolia Ln City:Harris County Cnty: Harris Map Pg: Dest: Pt's Name:
 Resp#9882614 Unscheduled Tx Unscheduled Transport Loc:Clear Lake Regional Medical CeAdd:500 Medical Center Blvd APT:5536 Cross St:Connecting Road/Unnamed Street City:Webster Cnty: Map Pg: Dest:Park Manor South Belt 11902 Resource Pkwy Pt's Name: taylor paula
 Resp#9884208 Immediate/Eme Response 29A01-Minor Injury Loc: Add:Plum Grove Rd & Huffman ClevelAPT: Cross St: City:Harris County Cnty: Harris Map Pg: Dest: Pt's Name:
 Resp#9885116 Life Threatening Emergency Traffic Accidents Loc: Add:Kingwood Dr & Huffman ClevelanAPT: Cross St: City:Harris County Cnty: Harris Map Pg: Dest: Pt's Name:
 Resp#9893942 Unscheduled Tx Unscheduled Transport Loc:West Houston Medical Center Add:12141 Richmond Ave APT:335 Cross St:Driveway/Unnamed Street City:Houston Cnty: Map Pg: Dest: 7789 Southwest Fwy Pt's Name: gonzalez ofelia
 Resp#9909614 Life Threatening Emergency Traffic Accidents Loc: Add:Fm 2100 & Huffman Rd APT: Cross St:FM 2100/Huffman Cleveland RD City:Huffman Cnty: Map Pg: Dest: Pt's Name:
 
 */
 
 public class XXAcadianAmbulanceParser extends FieldProgramParser {
   
   public XXAcadianAmbulanceParser(String defState) {
     super("", defState,
            "CALL! Loc:PLACE! Add:ADDR! APT:APT! Cross_St:X! City:CITY! Cnty:CITY! Map_Pg:MAP Dest:INFO Pt's_Name:NAME");
   }
   
   @Override
   public String getFilter() {
     return "commcenter@acadian.com";
   }
   
   private static final Pattern MARKER = Pattern.compile("^Resp(?:onse)?#(\\d+) +");
   
   @Override
   public boolean parseMsg(String body, Data data) {
     Matcher match = MARKER.matcher(body);
     if (!match.find()) return false;
     data.strCallId = match.group(1);
     body = body.substring(match.end());
     body = body.replace("Add:", " Add:").replace("APT:", " APT:").replace("City:", " City:").replace("Cnty:", " Cnty:");
     if (!super.parseMsg(body, data)) return false;
     
     // There is one particulare long, oft abbreviated road in Harris County, TX 
     // that always causes trouble
     if (data.defState.equals("TX") &&
         data.strCity.toUpperCase().startsWith("HARRIS")) {
       int pt = data.strAddress.lastIndexOf(' ');
       if (pt >= 0) {
         pt = data.strAddress.lastIndexOf(' ', pt-1);
         if (pt >= 0) {
           String tag = data.strAddress.substring(pt+1).toUpperCase();
           if ("HUFFMAN CLEVELAND RD".startsWith(tag)) {
             data.strAddress = data.strAddress.substring(0,pt+1) + "Huffman-Cleveland Rd";
           }
         }
       }
     }
     return true;
   }
   
   @Override
   public String getProgram() {
     return "ID " + super.getProgram();
   }
   
   private class MyCityField extends CityField {
     @Override
     public void parse(String field, Data data) {
       if (data.strCity.endsWith(" County") && data.strCity.startsWith(field)) return;
       data.strCity = append(data.strCity, ", ", field);
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("CITY")) return new MyCityField();
     return super.getField(name);
   }
 }
