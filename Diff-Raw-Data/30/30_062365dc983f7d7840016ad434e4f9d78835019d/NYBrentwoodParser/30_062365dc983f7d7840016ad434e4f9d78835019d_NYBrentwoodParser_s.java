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
 TYPE: PSYCHIATRIC / ABNORMAL BEHAVIOR / SUICIDE LOC: 200 WIRELESS BLVD HAUPPA: @SOCIAL SERVICES HAUPPAUGE INTERVIEW AREA CROSS: MORELAND RD /
 
 Contact: Christopher Greco <cgreco31@gmail.com>
 TYPE: PSYCHIATRIC / ABNORMAL BEHAVIOR / SUICIDE LOC: 79 ABBOTT AV MASTIC ***_VIP_***: CROSS: ELGIN ST / FOXCROFT ST CODE: 25-B-6 TIME: 14:36:05
 
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
     super(citiesCodes, DEF_CITY, DEF_STATE);
   }
 
   @Override
   protected boolean parseMsg(String body, Data data) {
 
     if (! body.startsWith("TYPE:")) return false;
 
     Properties props = parseMessage(body, new String[]{"LOC", "CROSS", "CODE", "TIME"});
     data.strCall = props.getProperty("TYPE", "");
     String sAddress = props.getProperty("LOC", "").replaceAll(":", " ");
     int pt = sAddress.indexOf('@');
     if (pt >= 0) {
       data.strPlace = sAddress.substring(pt+1).trim();
       sAddress = sAddress.substring(0,pt).trim();
       pt = sAddress.lastIndexOf(' ');
       if (pt >= 0) {
         data.strCity = sAddress.substring(pt+1);
         sAddress = sAddress.substring(0, pt).trim();
       }
       parseAddress(sAddress, data);
     }
     else {
       parseAddress(StartType.START_ADDR, sAddress, data);
       data.strPlace = getLeft();
     }
     data.strCross = props.getProperty("CROSS", "");
    data.strMap = props.getProperty("CODE", "");
     
     data.strCity = convertCodes(data.strCity, cityCodeTable);
     
     return true;
   }
 }
