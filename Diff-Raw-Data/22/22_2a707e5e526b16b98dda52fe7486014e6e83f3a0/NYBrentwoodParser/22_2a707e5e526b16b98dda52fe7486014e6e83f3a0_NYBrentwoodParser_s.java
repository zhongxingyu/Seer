 package net.anei.cadpage.parsers;
 
 import java.util.Properties;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 
 /* Sample Suffolk Page
 Contact: Jason Pedota <jpedota805@gmail.com>
 Sender: 6317967579
 TYPE: GAS LEAKS / GAS ODOR (NATURAL / L.P.G.) LOC: 11 BRENTWOOD PKWY BRENTW HOMELESS SHELTER CROSS: PENNSYLVANIA AV / SUFFOLK AV CODE: 60-B-2 TIME: 12:54:16
 TYPE: STRUCTURE FIRE LOC: 81 NEW HAMPSHIRE AV NBAYSH  CROSS: E FORKS RD / E 3 AV CODE: 69-D-10 TIME: 16:36:48
 TYPE: OPEN BURNING LOC: 65 GRANT AVE BRENTW CROSS: SUFFOLK AVE 18:39:20 CODE: 54-C-6
 TYPE: BLEEDING / LACERATIONS LOC: 462 SPUR DR N NBAYSH  CROSS: WB SSP OFF RAMP-X42N 5TH AV / E 3 AV CODE: 21-A-1 TIME: 03:36:22
 TYPE: PREGNANCY / CHILDBIRTH / MISCARRIAGE LOC: 330 MOTOR PKWY HAUPPA:@FELDMAN, KRAMER & MONACO STE 400  CROSS: WASHINGTON AV / MARCUS BLVD C
 */
 
 public class NYBrentwoodParser extends SmartAddressParser {
 
   private static String[] citiesCodes = new String[]{"BRENTW", "NBAYSH", "BAYSHO", "HAUPPA"};
   
   private Properties cityCodeTable = buildCodeTable(new String[]{
     "BRENTW", "Brentwood",
     "NBAYSH", "Bay Shore",
     "BAYSHO", "Bay Shore",
     "HAUPPA", "Hauppauge"
   });
   
   private static final String DEF_STATE = "NY";
   private static final String DEF_CITY = "SUFFOLK COUNTY";
   
   public NYBrentwoodParser() {
     super(citiesCodes, DEF_STATE);
   }
 
   @Override
   public boolean isPageMsg(String body) {
     return body.startsWith("TYPE:");
   }
 
   @Override
   protected void parse(String body, Data data) {
   
     data.defState=DEF_STATE;
     data.defCity=DEF_CITY;
 
    body = body.replaceAll(":@", " ");
     Properties props = parseMessage(body, new String[]{"LOC", "CROSS", "CODE", "TIME"});
     data.strCall = props.getProperty("TYPE", "");
    parseAddress(StartType.START_ADDR, props.getProperty("LOC", ""), data);
    data.strPlace = getLeft();
     data.strCross = props.getProperty("CROSS", "");
     data.strMap = props.getProperty("CODE", "");
     
     data.strCity = convertCodes(data.strCity, cityCodeTable);
   }
 }
