 package net.anei.cadpage.parsers.IL;
 
 import net.anei.cadpage.parsers.FieldProgramParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /*
 Woodford County, IL
 Contact: Benjamin Wagner <roanokefpd8@gmail.com>
 Sender: woodford911@mchsi.com
 Agency: Woodford County Communications
 
 (Woodford Comm) 11-022625 ODOR OF NATURAL GAS\n411 W FRONT ST\nN N PL /  N GREEN ST/\nCASEYS OF ROANOKE  ROANOKE\n1V14 ROFD WF27
 [Woodford Comm]  11-015377 FIRE\n1936 1400N\n1900E / 2000E/\nROANOKE\n1V24 ROFD
 [Woodford Comm]  11-014297 AMBULANCE CALL\n304 N FRANKLIN ST\nW DAVISON ST / W BRO/AD ST\nROANOKE\n1V24 ROFD
 [Woodford Comm]  11-006970 ACCIDENT-WITH INJURY\n1400N/ 1900E\nROANOKE\n1V24 ROFD WF16 WF22 WF4
 (Woodford Comm) 10-035758 FIRE ALARM\n1797 1700E\n1700N / 1800N/\nSPAFAS INC  WASHBURN FARM  ROANOKE\n1V23 1V24 MEFD ROFD
 [Woodford Comm]  11-000116 ACCIDENT-PROPERTY DAMAGE\n217 W HUSSEMAN ST\nN JEFFERSON ST /  W/RANDOLPH ST\nGEISER FORD  ROANOKE\nROFD WF18
 [Woodford Comm]  11-001634 911 LINES DOWN\n115 E COURT ST\nN MAIN ST /  DEAD EN/D EAST\nWOODCOM  EUREKA\nMIFD ROFD SBFD WAFD
 [Woodford Comm]  11-005470 ACCIDENT INJURY UNKNOWN\n1386 STATE ROUTE 117\n1300N /  116/\nPARSONS COMPANY  ROANOKE\n1V24 ROFD WF8\n
(Woodford Comm) 12-002663 FIRE\n1248 1700E\n1200N / 1300N/\nROANOKE\n1V24 ROFD
 
 */
 
 public class ILWoodfordCountyParser extends FieldProgramParser {
   
   public ILWoodfordCountyParser() {
     super("WOODFORD COUNTY", "IL",
            "IDCALL ADDR X/Z? PLCITY UNIT END");
   }
   
   @Override
   public String getFilter() {
     return "woodford911@mchsi.com";
   }
   
   @Override
   protected boolean parseMsg(String subject, String body, Data data) {
     if (!subject.equals("Woodford Comm")) return false;
     String[] flds = body.split("\n");
     if (flds.length != 4 && flds.length != 5) return false;
     return parseFields(flds, data);
   }
   
   private class IdCallField extends Field {
     
     @Override
     public void parse(String field, Data data) {
       Parser p = new Parser(field);
       data.strCallId = p.get(' ');
       data.strCall = p.get();
     }
     
     @Override
     public String getFieldNames() {
       return "ID CALL";
     }
   }
   
   private class PlaceCityField extends Field {
     
     @Override
     public void parse(String field, Data data) {
       Parser p = new Parser(field);
       data.strCity = p.getLast("  ");
       data.strPlace = p.get();
     }
     
     @Override
     public String getFieldNames() {
       return "PLACE CITY";
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("IDCALL")) return new IdCallField();
     if (name.equals("PLCITY")) return new PlaceCityField();
     return super.getField(name);
     
   }
 }
