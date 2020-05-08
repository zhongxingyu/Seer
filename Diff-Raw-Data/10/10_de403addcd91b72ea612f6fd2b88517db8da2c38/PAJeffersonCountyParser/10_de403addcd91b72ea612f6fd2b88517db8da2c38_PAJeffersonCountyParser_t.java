 package net.anei.cadpage.parsers.PA;
 
 import net.anei.cadpage.parsers.MsgInfo.Data;
 import net.anei.cadpage.parsers.SmartAddressParser;
 
 /*
 Jefferson County, PA
 Contact: "Rhed, Tony" <trhed@crossettinc.com>  (historical)
 Sender: Dispatch@jeffersoncountypa.com
 prvs=0482c7782c=dispatch@jeffersoncountypa.com (Incident) MVA -29-D Entrapment N 4TH ST HILL ST Reynoldsville Narrative:  10 YEAR OLD BOY HIT BY A CAR.
 prvs=0483bf5710=dispatch@jeffersoncountypa.com (Incident) Structure Fire CRICKET RD & VO TECH RD Winslow Narrative:  612 TO STATION  634 TO STATION UNKNOWN INJURIES  POSSIBLE  ENTRAPMENT. BOX 6-F
 prvs=0484c9755a=dispatch@jeffersoncountypa.com (Incident) Fire - Alarm. 3823 ROUTE 310 HIMES HILL RD / CHIPS RD Winslow.    Narrative:  basement smoke detector-  6-C Fire Tower
 
 Contact: Chris Smith <ffsmith20@gmail.com>
 Sender: dispatch@jeffersoncountypa.com
 prvs=0518956c10=dispatch@ <dispatch@jeffersoncountypa.com> jeffersoncountypa.com <dispatch@jeffersoncountypa.com> (Incident) Fire - Alarm 251 S PENN STREET EXT Punxsutawney DEAD END / CLIFF ST 20-A Snyder Hill
 prvs=0518956c10=dispatch@ <dispatch@jeffersoncountypa.com> jeffersoncountypa.com <dispatch@jeffersoncountypa.com> (Incident) Structure Fire 251 S PENN ST Punxsutawney
 prvs=0516eca2ae=dispatch@ <dispatch@jeffersoncountypa.com> jeffersoncountypa.com <dispatch@jeffersoncountypa.com> (Incident) MVA ROUTE 119 / ROUTE 436 Young Narrative:
 prvs=0514f9c90b=dispatch@ <dispatch@jeffersoncountypa.com> jeffersoncountypa.com <dispatch@jeffersoncountypa.com> (Incident) MVA 3637 ROUTE 36 SPRING RD / HOLLIS LN Oliver SMITH HAULINGNarrative: 5-A Coolspring
 prvs=0512e03127=dispatch@ <dispatch@jeffersoncountypa.com> jeffersoncountypa.com <dispatch@jeffersoncountypa.com> (Incident) Structure Fire 106 FOUNDRY ST CRISSMAN ALY / W MAHONING ST Punxsutawney Narrative: 40-A Snyder Hill
prvs=05312311d9=dispatch@jeffersoncountypa.com (Incident) Tree Down GASKILL AVE ALTMAN AVE Punxsutawney 20-A Snyder Hill
 
 */
 
 public class PAJeffersonCountyParser extends SmartAddressParser {
   
   public PAJeffersonCountyParser() {
     super(CITY_LIST, "JEFFERSON COUNTY", "PA");
   }
   
   @Override
   public String getFilter() {
     return "dispatch@jeffersoncountypa.com";
   }
 
   @Override
   protected boolean parseMsg(String subject, String body, Data data) {
     
     if (!subject.equals("Incident")) return false;
     
     int pt = body.indexOf("Narrative:");
     if (pt >= 0) {
       data.strSupp = body.substring(pt+10).trim();
       body = body.substring(0,pt).trim();
     }
 
    // SNYDER HILL in info section confuses the smart address parer
    body = body.replace("Snyder Hill", "Snyder-Hill");
     parseAddress(StartType.START_CALL, FLAG_START_FLD_REQ | FLAG_PAD_FIELD, body, data);
     data.strCross = getPadField();
    String extra = getLeft().replace("Snyder-Hill", "Snyder Hill");
     
    if (data.strCross.length() == 0 && extra.contains(" / ")) {
       data.strCross = extra;
     } else {
       data.strPlace = extra;
     }
     
     return true;
   }
   
   private static final String[] CITY_LIST = new String[]{
     
     // Boroughs
     "BIG RUN",
     "BROCKWAY",
     "BROOKVILLE",
     "CORSICA",
     "FALLS CREEK",
     "PUNXSUTAWNEY",
     "REYNOLDSVILLE",
     "SUMMERVILLE",
     "SYKESVILLE",
     "TIMBLIN",
     "WORTHVILLE",
 
     // Townships
     "BARNETT",
     "BEAVER",
     "BELL",
     "CLOVER",
     "ELDRED",
     "GASKILL",
     "HEATH",
     "HENDERSON",
     "KNOX",
     "MCCALMONT",
     "OLIVER",
     "PERRY",
     "PINE CREEK",
     "POLK",
     "PORTER",
     "RINGGOLD",
     "ROSE",
     "SNYDER",
     "UNION",
     "WARSAW",
     "WASHINGTON",
     "WINSLOW",
     "YOUNG",
     
     // Unincoroporated community
     "SPRANKLE MILLS"
 
   };
 }
