 package net.anei.cadpage.parsers.NJ;
 
 import java.util.Properties;
 
 import net.anei.cadpage.parsers.FieldProgramParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 
 /*
 Monmouth County, NJ (alternate?)
 Contact: Peter Hall <phall@englishtownfd.com>
 CodeMessage client: rc.317@c-msg.net
 
 (mCAD) [!] FIRA F FIRE ALARM | 21 LASATTA AV | X-ST: | BULIDING 700//SPRINKLER ROOM SMOKE DETECTOR | 08:22:17 | 02/17/2012
 (mCAD) [!] FIRA F FIRE ALARM | 4189 N RT 9 | X-ST: RT 522/SCHIBANOFF RD | BUSN: BROCK FARMS // ZONE: GENERAL | 15:30:16 | 01/26/2012
 (mCAD) [!] FIRA F FIRE ALARM | 124 SHINNECOCK DR | X-ST: | LEFT POT ON STOVE//HOUSE FILLED W/SMOKE | 15:31:14 | 01/26/2012
 (mCAD) [!] FIRS F FIRE STRUC | 15 APPLE BLOSSOM LN BLOSSUM | X-ST: THOMPSON GROVE RD/ | FIRE IN GARAGE | 21:22:19 | 01/26/2012
 (mCAD) [!] FIRS F FIRE STRUC | AVALON LA | X-ST: SPRINGHOUSE CI/ | ACROSS FROM 13 NO HOUSE NUMBER//SEES FLAMES | 18:59:46 | 01/27/2012
 (mCAD) [!] FIRS F FIRE STRUC | 18 KINNEY RD | X-ST: SWEETMANS LN/ | SMOKE COMING OUT OF WALL OVEN... | 18:18:33 | 01/28/2012
 (mCAD) [!] FIRW F FIRE WOODS | 47 LASATTA AV | X-ST: ENGLISH CLUB DR/CARRIAGE LN | 6X6 PIT UNATTENDED BURNING IN THE REAR OF THE | 14:30:44 | 01/31/2012
 (mCAD) [!] MVAF F MVA FIRE | FREEHOLD RD | X-ST: | PU TRUCK VS PU TRUCK/TELPHONE POLE AND WIRES | 13:22:55 | 02/02/2012
 (mCAD) [!] FIRA F FIRE ALARM | 49 LASATTA AV | X-ST: ENGLISH CLUB DR/CARRIAGE LN | BRANDYWINE/ZONE 2 GENERAL/ATT KEY | 10:41:21 | 02/05/2012
 (mCAD) [!] AIDF F AID-F | 5 SWEETMANS LN | X-ST: BORDER-MILLSTONE TWP/ST HWY 33 W | 6 HAVILAND DR .32 ODOR OF SMOKE IN RESD | 06:05:10 | 02/07/2012
 (mCAD) [!] MVFA F MVA FD/ALS | 232 SMITHBURG-527A RD | X-ST: ROBERTS RD/GRANDVIEW LN | CURVE NEAR CHURCH/2 VEH/ONE VEH WAS ON FIRE | 17:08:21 | 02/08/2012
 (mCAD) [!] FIRW F FIRE WOODS | 71 NEW BEGINNINGS CG | X-ST: TRACY STATION RD/GENESIS ST | KIDS AROUND A FIRE IN THE WOODS | 16:51:25 | 02/09/2012
 (mCAD) [!] FIRA F FIRE ALARM | 47 WILD TURKEY WY | X-ST: UNION HILL RD/ | APT D FIRE ALARM IN APT STILL GOING OFF PER C | 02:22:16 | 02/10/2012
 (mCAD) [!] GAS F GAS | 628 ST ANDREWS PL | X-ST: SAWGRASS DR/ | SMELLS GAS IN THE HALLWAY OF HIS RESIDENCE | 21:06:37 | 02/10/2012
 (mCAD) [!] FIRA F FIRE ALARM | 116 MILLHURST RD | X-ST: MAIN ST/STATION ST | COMMERCIAL-MANALAPAN ENGLISHTOWN BOARD OF ED | 11:28:04 | 02/14/2012
 (mCAD) [!] CRBA F CARB MONOX | 5 TURTLE HOLLOW DR | X-ST: KINNEY RD/ | CO ALARM SOUNDING//NO SYMPTOMS//EVACUATING | 09:34:25 | 02/15/2012
 (mCAD) [!] FIRA F FIRE ALARM | 21 LASATTA AV | X-ST: WATER ST/CARRIAGE LN | BULIDING 700//SPRINKLER ROOM SMOKE DETECTOR | 08:22:17 | 02/17/2012
 (eCAD) [!] FIRA F FIRE ALARM | 10 DANA CT | X-ST: | GENERAL FIRE ALARM//OP#3499 | 10:51:46 | 04/05/2012
 
 
 Code Messaging 
 
 [!] GAS F GAS | 1 STATION ST | X-ST: IRON ORE RD/ | AT REX LUMBER-GAS LINE STRUCK | 09:00:43 | 08/08/2012
 [!] FIRA F FIRE ALARM | 151 ST HWY 33 | X-ST: | NEXT GEN 732 617 9300 ZONE WATER FLOW | 07:56:50 | 08/07/2012
 [!] FIRA F FIRE ALARM | 151 ST HWY 33 | X-ST: MILLHURST RD/WOODWARD RD | NEXT GEN 732 617 9300 ZONE WATER FLOW | 07:56:50 | 08/07/2012
 [!] MVFA F MVA FD/ALS | 120 ST HWY 33 | X-ST: | 2 CAR MVA - NO INJURIES | 14:22:52 | 08/06/2012
 [!] MVFA F MVA FD/ALS | 120 ST HWY 33 | X-ST: RT 33 BYPASS/MILLHURST RD | 2 CAR MVA - NO INJURIES | 14:22:52 | 08/06/2012
 [!] FIRS F FIRE STRUC | 23 ST ANDREWS PL | X-ST: | THIRD PARTY CALLER/HOMEOWNER CONTACTED SAYING | 10:18:50 | 08/04/2012
 [!] FIRA F FIRE ALARM | 450 TENNENT RD | X-ST: | OLD TENENT CHURCH- GENERAL FIRE ALARM | 10:05:07 | 08/04/2012
 [!] FIRA F FIRE ALARM | 450 TENNENT RD | X-ST: FREEHOLD RD/CRAIG RD | OLD TENENT CHURCH- GENERAL FIRE ALARM | 10:05:07 | 08/04/2012
 
 
 */
 
 public class NJMonmouthCountyBParser extends FieldProgramParser {
   
   public NJMonmouthCountyBParser() {
     super(CITY_CODES, "MONMOUTH COUNTY", "NJ",
            "CALL ADDR/S X-ST:X! INFO TIME DATE!");
   }
   
   @Override
   public String getFilter() {
     return "@c-msg.net";
   }
   
   @Override
   public boolean parseMsg(String subject, String body, Data data) {
     if (!parseFields(body.split("\\|"), data)) return false;
     return true;
   }
   
   private class MyCrossField extends CrossField {
     @Override
     public void parse(String field, Data data) {
       if (field.endsWith("/")) field = field.substring(0,field.length()-1).trim();
       super.parse(field, data);
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("X")) return new MyCrossField();
     if (name.equals("TIME")) return new TimeField("\\d\\d:\\d\\d:\\d\\d", true);
     if (name.equals("DATE")) return new DateField("\\d\\d/\\d\\d/\\d{4}", true);
     return super.getField(name);
   }
   
   private static final Properties CITY_CODES = buildCodeTable(new String[]{
       
   });
 }
