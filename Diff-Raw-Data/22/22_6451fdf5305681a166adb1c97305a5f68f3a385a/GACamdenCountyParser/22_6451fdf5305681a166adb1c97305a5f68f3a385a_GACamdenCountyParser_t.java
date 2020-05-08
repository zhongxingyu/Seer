 package net.anei.cadpage.parsers.GA;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.MsgInfo.Data;
 import net.anei.cadpage.parsers.dispatch.DispatchA3Parser;
 
 /*
 Camden County, GA
 Contact: "croney kingslandgeorgia.com" <croney@kingslandgeorgia.com>
 Sender: astudstill@co.camden.ga.us
 astudstill@co.camden.ga.us:2011-101695* HIGHWAY 17 STEFFANS* * * KINGSLAND* * Traffic Stop* TRAFFIC STOP* * * 1128,1140,1150,1152,509,514,523,532,LS3,R3* * Med
 astudstill@co.camden.ga.us:2011-104696* 4059 MARTIN LUTHER KING BLVD* N4* * 514,541,546,ENG4,LS4,MED4 KINGSLAND* * INJURY* INJURY* 509,ENG5,LS3* * Medical: No
 astudstill@co.camden.ga.us:2011-101995* 405 & HIGHWAY 40 OLD WAFFLE HOUSE* * * KINGSLAND* * ACCIDENT* ACCIDENT* ELAINE* 386-208-4465* 514,541,546,ENG4,LS4,MED4
 
 astudstill@co.camden.ga.us:2011-104843* 429 EAGLE BLVD* * * KINGSLAND* * PERSON SICK* PERSON SICK* * 540,ENG4,LS4* * Medical: No* Haz
 214 REDWOOD ST* * * KINGSLAND* * PERSON SICK* PERSON SICK* MS FAGEN*912-269-6157* LS3,R3* * Medical: No* Hazards: No* 
 
 Contact:  Jeff <kfr438@tds.net>
 astudstill@co.camden.ga.us:2011-181161* ADVANCE COLLISION CENTER* * * KINGSLAND* * WIRE DOWN* WIRE DOWN* JOHN* 316-258-1559* ENG4,LS4* * Medical: No* Haz
 
 Contact: Scott Gregory <kfr436@gmail.com>
 Sender: astudstill@co.camden.ga.us
 astudstill@co.camden.ga.us:2012-037466* BOONE AND SUMMERBROOK* * * KINGSLAND* * INVESTIGATE* INVESTIGATE SUSPICIOUS PERSON/VEHICLE* JUAN RODRIGUEZ* 912-8
 
 *** NOT PARSING CORRECTLY ***
 Contact: charles <croney@kingslandgeorgia.com>
 Sender: astudstill@co.camden.ga.us
 astudstill@co.camden.ga.us:2012-142354* CMC* * * ST MARYS* * * * * TRANSFER* TRANSFER TO ... (MEDICAL OR FIRE)* * * MED3* * Medical: No* Hazards: No* Lin
 astudstill@co.camden.ga.us:2012-142422* 119 TERESA LN* * * KINGSLAND* * * * * DOMESTIC* DOMESTIC PROBLEM* HUNTER* 912-576-1327* 525,531,544,LS3,R3* * Med
 astudstill@co.camden.ga.us:2012-142419* CMC TO SHANDS* * * KINGSLAND* * * * * TRANSFER* TRANSFER TO ... (MEDICAL OR FIRE)* * * MED4* * Medical: No* Hazar
 
Contact: "Roney" <croney@kingslandgeorgia.com>
Contact: "bcarreira tds.net" <bcarreira@tds.net>
astudstill@co.camden.ga.us:2012-142419* CMC TO SHANDS* * * KINGSLAND* * * * TRANSFER* TRANSFER TO ... (MEDICAL OR FIRE)* * * MED4* * Medical: No* Hazar
astudstill@co.camden.ga.us:2012-142222* STATION 4* * * KINGSLAND* * * * PERSON SICK* PERSON SICK* * * ENG4,LS4* * Medical: No* Hazards: No* Line18=*
astudstill@co.camden.ga.us:2012-142193* 2060 HWY 40 E* * * KINGSLAND* * * * FIRE ALARM* FIRE ALARM* * * ENG4,LS2,FIRE3,FIRE4,Q9,LAD3,ENG3,ENG5* Medic

Contact: "w1kfr tds.net" <w1kfr@tds.net>
astudstill@co.camden.ga.us:2012-142755* 102 ALMOND CIR* * * KINGSLAND* * * * UNCONSCIOUS* UNCONSCIOUS PERSON* * 912-674-6173* 542,ENG4,LS4* * Medical:

 */
 public class GACamdenCountyParser extends DispatchA3Parser {
   
   public GACamdenCountyParser() {
     super(1, "", "CAMDEN COUNTY", "GA");
   }
   
   @Override
   public String getFilter() {
     return "astudstill@co.camden.ga.us";
   }
   
   private static final Pattern UNIT_PTN = Pattern.compile("^[^ ]*,[^ ]* ");
   private class MyCityField extends CityField {
     @Override
     public void parse(String field, Data data) {
       Matcher match = UNIT_PTN.matcher(field);
       if (match.find()) field = field.substring(match.end()).trim();
       super.parse(field, data);
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("CITY")) return new MyCityField();
     return super.getField(name);
   }
 }
