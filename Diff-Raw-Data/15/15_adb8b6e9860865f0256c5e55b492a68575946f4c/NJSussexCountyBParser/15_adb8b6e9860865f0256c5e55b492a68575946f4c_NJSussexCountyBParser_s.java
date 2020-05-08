 package net.anei.cadpage.parsers.NJ;
 
 import net.anei.cadpage.parsers.FieldProgramParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /*
 Sussex County, NJ (Replace)
 Contact: Todd Rudloff <btfd43capt2@gmail.com>
 
 [Message from HipLink]  BTFD\nFire Alarm\nPAGED\n11 MANSFIELD DR\nBTF02\nBTFIRE\nADT,877-535-0563 OPER#AMH, REPORTS AN ACTIVATED GENERAL FIRE ALARM.
 [Message from HipLink]  BTFD\nFire Other\nPAGED\n16 CARLISLE DR\nBTF03\nBTFIRE\nREPORT OF A FIRE IN THE FURNACE
 [Message from HipLink]  BTFD\nSuspicious Acti\nPAGED\n161 LACKAWANNA DR\nBTF02\nBTFIRE\nPOSSIBLE CHIMNEY FIRE\n24 E/R\nchief 2 i/s\nCall type l reopened by Cat
 [Message from HipLink]  BTFD\nFire Other\nPAGED\nI 80\nBTF06\nBTFIRE\nBRAKE FIRE ON A T.T. BYRAM FIRE AND EMS WAS DISPATCHED AS PER NJSP
 [Message from HipLink]  BTFD\nMV Acc Injury\nPAGED\nLACKAWANNA DR\nBTF02\n43EMS\nMOTOR VEHICLE ACCIDENT WITH ENTRAPPMENT\nPATIENT REMOVED
 [Message from HipLink]  BTFD\nFire Other\nPAGED\n17 STONEHEDGE LN\nBTF03\nBTFIRE
 
  */
 
 
 public class NJSussexCountyBParser extends FieldProgramParser {
   
   
   public NJSussexCountyBParser() {
     super("SUSSEX COUNTY", "NJ",
           "SRC CALL PAGED ADDR UNIT UNIT INFO+");
   }
 
   @Override
   protected boolean parseMsg(String subject, String body, Data data) {
     
    if (!subject.equals("Message from HipLink")) return false;
    String[] flds = body.split("\n");
    if (flds.length < 6) return false;
    return parseFields(flds, data);
   }
   
   private class MyUnitField extends UnitField {
     @Override
     public void parse(String field, Data data) {
       data.strUnit = append(data.strUnit, " ", field);
     }
   }
 
   @Override
   protected Field getField(String name) {
     if (name.equals("SRC")) return new SourceField("[A-Z]+", true);
     if (name.equals("PAGED")) return new SkipField("PAGED", true);
     if (name.equals("UNIT")) return new MyUnitField();
     return super.getField(name);
   }
   
   
 }
