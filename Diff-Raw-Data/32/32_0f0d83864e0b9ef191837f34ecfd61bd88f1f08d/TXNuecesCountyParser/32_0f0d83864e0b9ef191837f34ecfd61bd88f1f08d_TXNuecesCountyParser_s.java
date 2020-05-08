 package net.anei.cadpage.parsers.TX;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.FieldProgramParser;
 
 /* 
 Nueces County, TX
 Contact: Mike Clack <chief@annavillevfd.com>
 Sender: ccpdpaging@cctexas.com<mailto:ccpdpaging@cctexas.com
 cc = corpus christi
 neucs = neuces
 
 Subject:CAD Notify\nTRAUMA - INJURY ALRM: 0 PRI: 1 ESZ: 67 / 11308 GUESS DR CC NUECS EV: 1110029194\n
 Subject:CAD Notify\nINV-FIRE - SMOKE ALRM: 0 PRI: 2 ESZ: 107 / 3695 CR 61 NUECS EV: 1110029353
 Subject:CAD Notify\nTA - EMS ALRM: 0 PRI: 1 ESZ: 107 / CR 48/HWY 77 FWY SB NUECS EV: 1110031040\n\n
 Subject:CAD Notify\nMEDICAL - SICK ALRM: 0 PRI: 1 ESZ: 6 / 4909 FM 2826 NUECS: @LABROCK CORRECTIONS SERVICES EV: 1110031803\n\n
 Subject:CAD Notify\nFIRE-OTHER - VEH-OUTSIDE ALRM: 0 PRI: 1 ESZ: 104 / 1332 FM 665 NUECS: @MEMORY GARDENS CEMETERY - FM 665 EV: 111003414
 Subject:CAD Notify\nTRAUMA - INJURY ALRM: 0 PRI: 1 ESZ: 67 / 4729 CALALLEN DR CC NUECS EV: 1110034169\n\n
 Subject:CAD Notify\nTA - RESCUE ALRM: 0 PRI: 1 ESZ: 2 / FM 665/FM 70 NUECS EV: 1110034392\n\n
 
  */
 
 public class TXNuecesCountyParser extends FieldProgramParser {
   
   public TXNuecesCountyParser() {
     super("NUECES COUNTY", "TX",
           "CALL! ALRM:SKIP! PRI:PRI! ESZ:ADDR! EV:ID!");
   }
   
   @Override
   public String getFilter() {
     return "ccpdpaging@cctexas.com";
   }
   
   @Override
   public boolean parseMsg(String subject, String body, Data data) {
     if (!subject.equals("CAD Notify")) return false;
     return super.parseMsg(body, data);
   }
   
   private class MyAddressField extends AddressField {
 
     @Override
     public void parse(String field, Data data) {
       int pt = field.indexOf('/');
       if (pt < 0) abort();
       data.strUnit = field.substring(0,pt).trim();
       field = field.substring(pt+1).trim();
       pt = field.lastIndexOf(": @");
       if (pt >= 0) {
         data.strPlace = field.substring(pt+3).trim();
         field = field.substring(0,pt);
       }
       if (field.endsWith(" NUECS")) field = field.substring(0,field.length()-6).trim();
       if (field.endsWith(" CC")) {
         data.strCity = "CORPUS CHRISTI";
         field = field.substring(0, field.length()-3).trim();
       }
       super.parse(field, data);
     }
     
     @Override
     public String getFieldNames() {
       return "UNIT " + super.getFieldNames() + " CITY PLACE";
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("ADDR")) return new MyAddressField();
     return super.getField(name);
   }
 }
