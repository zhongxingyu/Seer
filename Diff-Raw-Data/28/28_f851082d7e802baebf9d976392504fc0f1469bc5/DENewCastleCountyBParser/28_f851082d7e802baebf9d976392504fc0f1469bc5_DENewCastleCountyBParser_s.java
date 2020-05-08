 package net.anei.cadpage.parsers.DE;
 
 import net.anei.cadpage.parsers.FieldProgramParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /*
 Newcastle County, DE (variant B)
 Contact: "Thomas Castiglione" <castiglione.thomas@gmail.com>
 Sender: msg@cfmsg.com
 
 (Chief ALT) [CFC EMS] -- Syncope - Not Alert -- Amberwood -- 51 Karens Wy - Cross STS:Beechwood Ct & Silver Birch La - EMD:M31D3
 (Chief ALT) [CFC EMS] -- Medical Alarm -- Forest Glen Townhses -- 3 Pimlico La - Cross STS:Rue Madora & Dead - End - EMD:M32B2
 (Chief ALT) [CFC EMS] -- Abdominal Pains -- Summerset Square Townhses -- 4 Candlewick Ct New Castle DE - Cross STS:Wilton Bl & Dead - End - EMD:M1A1
 (Chief ALT) [CFC EMS] -- Assault - Unknown -- Wawa Salem Center - Salem Center Shop Ctr -- 1605 Pulaski Hy - Cross STS:Glasgow Dr & Salem Church Rd - EMD:M4B3
 (Chief ALT) [CFC EMS] -- Assault -- Days Inn -- 900 Churchmans Rd #122 Newark DE - Cross STS:Rte 1 Sb & Old Churchmans Rd - EMD:M4B1A
 (Chief ALT) [CFC EMS] -- Overdose / Poisoning - Not Alert -- Christiana Meadows Apts -- 401 Christiana Meadows - Cross STS:Bear Christiana Rd - EMD:M23C1I
 (Chief ALT) [CFC Fire] -- L1*fire Alarm / General -- Medical Arts Pav 1 -- 4745 Stanton Ogletown Rd - Cross STS:Samoset Dr & Hygeia Dr - EMD:52C1G
 (Chief ALT) [CFC EMS] -- Cva - Sudden Weakness / Numbness -- Crossings At Christiana Townhses -- 216 Gladstone Wy - Cross STS:Dead - End & Baker Dr - EMD:M28C4C
 (Chief ALT) [CFC EMS] -- Person Fallen -- Hillside Heights -- 109 Rosewood Dr - Cross STS:Greentree Rd & Lavender La - EMD:M17B1
 (Chief ALT) [CFC Fire] -- L1*commercial Fire Alarm -- Direct Radiography - Glasgow Business Community -- 600 Technology Dr - Cross STS:Gbc Dr & Yoon La - EMD:52C3G
 (Chief ALT) [CFC FIRE] -- Vehicle Accident -- Interstate 95 -- Churchmans Rd / I95 Sb - EMD:M29B4
 
  */
 
 public class DENewCastleCountyBParser extends FieldProgramParser {
   
   public DENewCastleCountyBParser() {
     super(CITY_LIST, "NEW CASTLE COUNTY", "DE",
           "CALL! NAME! ADDR/S! Cross_STS:X? EMD:UNIT");
   }
   
   @Override
   public String getFilter() {
    return "@c-msg.net";
   }
 
   @Override
    protected boolean parseMsg(String subject, String body, Data data) {
     
     // Drop calls from Deepwater, NJ
     if (body.contains(" Caller:")) return false;
     
     if (!subject.startsWith("Chief ALT|")) return false;
     data.strSource = subject.substring(10).trim();
     
     if (!body.startsWith("-- ")) return false;
     body = body.substring(3).trim();
     
     String[] flds1 = body.split(" -- ");
    String[] flds2 = flds1[flds1.length-1].split(" - ");
     
     String[] flds = new String[flds1.length-1 + flds2.length];
     System.arraycopy(flds1, 0, flds, 0, flds1.length-1);
     System.arraycopy(flds2, 0, flds, flds1.length-1, flds2.length);
     return parseFields(flds, data);
   }
   
   @Override
   public String getProgram() {
     return "SRC " + super.getProgram();
   }
   
   private class MyAddressField extends AddressField {
     
     @Override
     public void parse(String field, Data data) {
       if (field.endsWith(" DE")) field = field.substring(0,field.length()-3).trim();
       super.parse(field, data);
       if (data.strCity.equalsIgnoreCase("NEW CASTLE")) data.strCity = "";
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("ADDR")) return new MyAddressField();
     return super.getField(name);
   }
   
   private static String[] CITY_LIST = new String[]{
     "NEW CASTLE",
     
     "ARDENCROFT",
     "ARDENTOWN",
     "BELLEFONTE",
     "CLAYTON",
     "DELAWARE CITY",
     "ELSMERE",
     "MIDDLETOWN",
     "NEW CASTLE",
     "NEWARK",
     "NEWPORT",
     "ODESSA",
     "SMYRNA",
     "TOWNSEND",
     "WILMINGTON",
     "BEAR",
     "BROOKSIDE",
     "CLAYMONT",
     "COLLINS PARK",
     "CHRISTIANA",
     "EDGEMOOR",
     "GLASGOW",
     "GREENVILLE",
     "HOCKESSIN",
     "HOLLY OAK",
     "MARSHALLTON",
     "MINQUADALE",
     "MONTCHANIN",
     "NORTH STAR",
     "OGLETOWN",
     "PIKE CREEK",
     "ROCKLAND",
     "ST. GEORGES",
     "STANTON",
     "WILMINGTON MANOR",
     "WINTERTHUR",
     "WINTERSET"
   };
 }
 
 
