 package net.anei.cadpage.parsers.MD;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.FieldProgramParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /*    
 Baltimore County, MD
 Contact: Emma Schneider <ejschneider41@verizon.net>
 Sender: postmaster@sparkgroup.net
 
 (Station 41 ALERT!!) UNCONSCIOUS DIABETIC\n046-04\nE461 E18 M465 IV415 CCM129 EMS5\n9341 EDWAY CI\n\nIncident Number (120911179)
 (Station 41 ALERT!!) FALL INJURY NON DANGEROUS\n041-21\nIV415\n300 CANTATA CT APT 302, 21136, MD\n\nIncident Number (120911036)
 (Station 41 ALERT!!) BUILDING FIRE\n056-06\nBC21 E56 E412 E311 E401 T313 T404\n11602 REISTERSTOWN RD, 21136, MD\n\nIncident Number (120910568)
 (Station 41 ALERT!!) UNKNOWN ILLNESS OR INJURY\n041-21\nA415 M56\n300 CANTATA CT APT 314, 21136, MD\n\nIncident Number (120910566)
 (Station 41 ALERT!!) CP  CLAMMY\n041-06\nM415 CCM139\n744 COCKEYS MILL RD, 21136, MD\n\nIncident Number (120910541)
 (Station 41 ALERT!!) STROKE  NOT ALERT\n049-10\nM395 M415\n14032 CUBA RD\n\nIncident Number (120901630)
 (Station 41 ALERT!!) FALL  POSS DANGEROUS AREA\n056-06\nA415 IV465\n11722 REISTERSTOWN RD, 21136, MD\n\nIncident Number (120901441)
 (Station 41 ALERT!!) INJ-DANGROUS BDY AREA\n041-03\nM56 SU418\nSTOCKSDALE AV & POWER LINE, 21136, MD\n\nIncident Number (120901111)
 (Station 41 ALERT!!) SICK SUBJECT NOT ALERT\n041-01\nM415 M56\n10 VILLAGE CENTER RD, 21136, MD\n\nIncident Number (120901433)
 (Station 41 ALERT!!) 1050PI\n041-01\nA415 M315 E413\nFRANKLIN ES @  33 COCKEYS MILL RD, 21136, MD\n\nIncident Number (120900817)
 (Station 41 ALERT!!) 1050PI ROLL-OVER\n041-20\nA415 M315 E413\nI795 & WESTMINSTER PK, 21136, MD\n\nIncident Number (120900400)
 (Station 41 ALERT!!) TRBR-W/DIFF SPEAKING\n056-05\nIV415 M2 EMS5\n300 STONECASTLE AV, 21136, MD\n\nIncident Number (120911287)
 (Station 41 ALERT!!) ABD PAIN  NOT ALERT\n056-08\nA315 IV415\n111 EMBLETON RD, 21117, MD\n\nIncident Number (120911354)
 (Station 41 ALERT!!) 1050PI ROLL-OVER\n041-08\nA415 M56 E412\n5 BRIAN DANIEL CT, 21136, MD\n\nIncident Number (120920186)
 (Station 41 ALERT!!) BLEEDING SUBJ W/TRBR\n056-06\nM415 M19\n11 WOODENBRIDGE CT, 21136, MD\n\nIncident Number (120921328)
 (Station 41 ALERT!!) FALL INJURY NON DANGEROUS\n041-21\nA415 M56\n300 CANTATA CT APT 302, 21136, MD\n\nIncident Number (120921264)
 (Station 41 ALERT!!) FALL INJURY NON DANGEROUS\n056-04\nE56 A415 IV505\n828 IVYDALE AV, 21136, MD\n\nIncident Number (120951716)
 (Station 41 ALERT!!) LOCK OUT DETAIL/NON EMERG\n041-23\nE412\n505 DEACON BROOK CI, 21136, MD\n\nIncident Number (120980427)
 (Station 41 ALERT!!) BUILDING FIRE\nCAR-43\nE432 S414 T404 E412\n643 LUCABAUGH MILL RD\n\nIncident Number (120971868)
 (Station 41 ALERT!!) EMOT ILL CONS/BREATHING\n041-23\nM415 M315\n400 VALLEY MEADOW CI APT B1, 21136, MD\n\nIncident Number (120990430)
 (Station 41 ALERT!!) FAINTING  ALERT W/TRBR\n041-23\nM415 M56\n406 DEACON BROOK CI, 21136, MD\n\nIncident Number (120990933)
 
 */
 
 public class MDBaltimoreCountyParser extends FieldProgramParser {
   
   private static final Pattern SUBJECT_PTN = Pattern.compile("Station (\\d+) ALERT!!");
   
   public MDBaltimoreCountyParser() {
     super("BALTIMORE COUNTY", "MD",
           "CALL MAP UNIT ADDR/S INFO ID!");
   }
   
   @Override
   public String getFilter() {
     return "postmaster@sparkgroup.net";
   }
 
   @Override
   protected boolean parseMsg(String subject, String body, Data data) {
     
     Matcher match = SUBJECT_PTN.matcher(subject);
     if (!match.find()) return false;
     data.strSource = match.group(1);
    return parseFields(body.split("\n"), 6, data);
   }
   
   @Override
   public String getProgram() {
     return "SRC " + super.getProgram();
   }
   
   private class MyAddressField extends AddressField {
     @Override
     public void parse(String field, Data data) {
       Parser p = new Parser(field);
       data.strPlace = p.getOptional('@');
       super.parse(p.get(','), data);
       data.strCity = p.get(',');
       data.strState = p.get();
       if (data.strState.equals(data.defState)) data.strState = "";
     }
     
     @Override
     public String getFieldNames() {
       return "PLACE " + super.getFieldNames() + " CITY ST";
     }
   }
 
   private static final Pattern ID_PTN = Pattern.compile("Incident Number \\((\\d+)\\)");
   private class MyIdField extends IdField {
     @Override
    public void parse(String field, Data data) {
       Matcher match = ID_PTN.matcher(field);
      if (!match.matches()) abort();
       super.parse(match.group(1), data);
     }
   }
 
   @Override
   protected Field getField(String name) {
     if (name.equals("MAP")) return new MapField("(?:\\d{3}|CAR)-\\d{2}", true);
     if (name.equals("ADDR")) return new MyAddressField();
     if (name.equals("ID")) return new MyIdField();
     return super.getField(name);
   }
 }
