 package net.anei.cadpage.parsers;
 
 import java.util.Properties;
 import net.anei.cadpage.SmsMsgInfo.Data;
 
 /***
 **********************************************************
 Emmitsburg, MD (Northern Federick County)
 Contact: dalefogle@aol.com
 Contact: Dale Fogle <pullersnet@gmail.com>
 Sender:  www.codemessaging.net
  
 (CAD) [FredCo] CT: HOUSE / FIRE-VISIBLE 6801 HARBAUGH RD CSAB:NEXT TO HER HOME ESZ: 626 MAP: 3996A6 Disp: [23]
 (CAD) [FredCo] CT: WOODS FIRE 7819-B FRIENDS CREEK RD CEMB ESZ: 627 MAP: 3996F6 Disp: E63,B66,DNR [36]
 (CAD) [FredCo] CT: COMMERCIAL FIRE ALARM / AUTOMATIC 4 PAWS PL THUR: @CATOCTIN VETERINARY CLINIC ESZ: 1001 MAP: 4108E9 Disp: E102,TWR6 [37]
 (CAD) [FredCo] CT: OUTSIDE INVESTIGATION @MA ADAM CO: @RT15SB/BOYLE RD ESZ: -1 Disp: E63 [38]
        This was a mutual aid call to Adams Co. Pa at Rt.15 South Bound at the Boyle Rd. overpass
 (CAD) [FredCo] CT: HOUSE / ODOR 9152 OLD KILN RD CEMB ESZ: 617 MAP: 4109E4 Disp: E63,E102,A268,TWR6,ET64 [39]
 (CAD) [FredCo] CT: COMMERCIAL FIRE ALARM / AUTOMATIC 102 OLD OAK PL THUR ESZ: 1001 MAP: 4222D2 Disp: E102,TWR6 [41]
 (CAD) [FredCo] CT: COMMERCIAL FIRE ALARM / WATERFLOW 100 CREAMERY CT: @EMMITSBURG GLASS CO ESZ: 620 MAP: 3997K9 Disp: E63,TWR6 [42]
 (CAD) [FredCo] CT: PERSON FIRE (INSIDE) 16825 S SETON AVE CEMB: @NATIONAL EMERGENCY TRAINING CENTER:SIMULATION LAB ESZ: 620 MAP: 3997H9 Disp: [34]                    Rmk:"
 (FredCo) [CAD] CT: CHEST PAIN  1811 MONOCACY BLVD FRE1: @WAL MART: PHARMACY  ESZ: 215 MAP: 4448J9  Disp: A29,A247,M17
 
 ***/
 
 public class MDFrederickCountyParser extends SmartAddressParser {
   
   private static final String DEF_STATE = "MD";
   private static final String DEF_CITY = "FREDERICK COUNTY";
 
   private static final String[]Fredrickkeywords = new String[]{"CT", "ESZ", "MAP", "Disp",};
   
   private static final Properties CITY_CODE_TABLE = 
     buildCodeTable(new String[]{
         "THUR","Thurmont",
         "CEMB","Emmitsburg",
         "CSAB","Sabillasville",
         "CWAL","Walkersville",
         "CFR2","Frederick City",
         "FRE1", "Frederick City",
         "WOOD","Woodsboro",
         "CNMA","New Market",
         "ADAM","Adams County", //PA
         "ADAM CO", "Adams County",
         "ADCO","Franklin County", //PA
         "FRAN CO", "Franklin County"
     });
 
   public MDFrederickCountyParser(){
     super(CITY_CODE_TABLE, DEF_CITY, DEF_STATE);
   }
 
   
   @Override
   protected boolean parseMsg(String body, Data data) {
     
     
     if (!body.contains("[FredCo]") && !body.contains("(FredCo)")) return false;
     
     // If there is a : right after city then there is Supp data between City Code and ESZ:
     
     Properties props = parseMessage(body, Fredrickkeywords);
     String strAddr = props.getProperty("CT");
    if (strAddr == null) return false;
     
     // Everything changes if this is a Mutual aid call
     int ipt = strAddr.indexOf(" @MA ");
     if (ipt >= 0) {
       data.strCall = "Mutual Aid: " + strAddr.substring(0, ipt).trim();
       int ipt2 = strAddr.indexOf(':', ipt);
       if (ipt2 < 0) ipt2 = strAddr.length();
       data.strCity = strAddr.substring(ipt+5, ipt2).trim();
       strAddr = strAddr.substring(ipt2+1).trim().replaceAll("@", "");
       parseAddress(strAddr, data);
     }
 
     else {
       Parser p = new Parser(strAddr);
       strAddr = p.get(':');
       data.strPlace = p.get();
       parseAddress(StartType.START_CALL, FLAG_START_FLD_REQ, strAddr, data);
     }
     
     data.strMap = props.getProperty("MAP", "");
     data.strBox = props.getProperty("ESZ", "");
     data.strUnit = props.getProperty("Disp", "");
     data.strCity = convertCodes(data.strCity, CITY_CODE_TABLE);
     if (data.strCity.equals("Franklin County") || data.strCity.equals("Adams County")) {
       data.strState = "PA";
     }
     if (data.strUnit.contains("[") && data.strUnit.contains("]")) {
         data.strUnit = data.strUnit.substring(0,data.strUnit.indexOf("[")).trim();
       }
     return true;
   }
 }
