 package net.anei.cadpage.parsers.PA;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.FieldProgramParser;
 
 /*
 Lackawanna County, PA
 Contact: greg rhodes <grhodes908@gmail.com>
 Contact: MarkPALackawannaCountyParser.java Krzan <mkrzan1@gmail.com>
 Sender: aegispage@lackawannacounty.org
     
 Unit  Location: 206 MILLER RD STEVENSON RD / CARBONDALE RD 1 Common Name: Call Type: ALS Call Time: 09/26/11 17:02 Narrative  pd notified  POSS CONFUSED ALSO  Nature Of Call: 79YOF FEELING WEEK. SHE DID FALL EARLIER
 Unit  Location: 711 MAY ST MAPLE ST / DEAD END 59 Common Name: Call Type: ALS Call Time: 09/26/11 21:33 Narrative  PD ADVISED  REF EMD  HX OF HEART PROBLEMS  Nature Of Call: 56 YOM RAPID HEART RATE
 Unit  Location: 108 TERRACE DR STURGES RD / SCRANTON CARBONDALE HWY 20 Common Name: LACKAWANNA HEALTH AND REHABCall Type: ALS Call Time: 09/26/11 21:02 Narrative  22-9/LA4 TO MOSES TAYLOR  22-9 ENRT MOMENTARILY  Nature Of Call: in c-2 -- 90 yo f diff breathing low o2 sats
 Unit  Location: 601 6TH ST DEAD END / MOOSIC HEIGHTS 94 Common Name: Call Type: ALS Call Time: 09/26/11 22:27 Narrative  94PD ADVISED  UNKNOWN DRUGS  MALE TOOK DRUGS  IS BREATHING - IS SEMI-RESPONSIVE  Nature Of Call: 24 YOM COLLAPSED
 
  */
 
 
 public class PALackawannaCountyParser extends FieldProgramParser {
   
   public PALackawannaCountyParser() {
     super("LACKAWANNA COUNTY", "PA",
            "Location:ADDRCITY/SXx! Common_Name:PLACE? Call_Type:CALL! Call_Time:SKIP Narrative:INFO Quadrant:MAP District:SKIP Nature_Of_Call:INFO");
   }
   
   @Override
   public String getFilter() {
     return "aegispage@lackawannacounty.org";
   }
   
   @Override
   protected boolean parseMsg(String body, Data data) {
     body = body.replace("Call Type:", " Call Type:").replace(" Narrative ", " Narrative: ");
     return super.parseMsg(body, data);
   }
   
   private class MyAddressCityField extends AddressCityField {
     @Override
     public void parse(String field, Data data) {
       if (field.startsWith("0 ")) field = field.substring(2).trim();
       super.parse(field, data);
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("ADDRCITY")) return new MyAddressCityField();
     return super.getField(name);
   }
 }
