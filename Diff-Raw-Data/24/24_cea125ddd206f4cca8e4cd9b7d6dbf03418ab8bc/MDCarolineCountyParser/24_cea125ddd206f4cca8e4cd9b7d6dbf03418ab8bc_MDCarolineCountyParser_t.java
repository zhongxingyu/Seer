 package net.anei.cadpage.parsers.MD;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.dispatch.DispatchAegisParser;
 
 /*
 Caroline County, MD
 Contact: Brent Marine <bamarine123@gmail.com>
 Sender: msg@cfmsg.com <From%3Amsg@cfmsg.com>
 (PM13 is the unit code.  26 A is part of the call description)
 
 (Chief ALT) [PM13] 26 A-Sick Person -- 520 Kerr Ave Denton - XSts: Dead End / Daffin Ln
 (Chief ALT) [200 Fire] Wires Down -- 22542 Dover Bridge Rd Preston - XSts: Poplar Neck Rd, Tanyard Rd / Bethlehem Rd
 (Chief ALT) [200 Fire] 17 A-Falls -- 21182 Marsh Creek Rd Preston - XSts: Frazier Neck Rd / Bethlehem Rd
 (Chief ALT) [200 EMS] House Fire -- 22272 Havercamp Rd Preston - XSts: Prchal Rd / Poplar Neck Rd
 
 Contact: "jg0169586@yahoo.com" <jg0169586@yahoo.com>
 (Chief ALT) [200 EMS] 23 B-Overdose / Poisoning -- 320 Park Ave Federalsburg - XSts: Railroad Ave / Academy Ave
 
 Contact: Pj Dyott <pdyott@gmail.com>
 (Chief ALT) [200 EMS] 19 D-Heart Problems / A.i.c.d. -- 121 E Central Ave Federalsburg - XSts: Reliance Ave / N Main St, S Main St, W Central Ave
 (Chief ALT) [200 EMS] 23 D-Overdose / Poisoning -- 156 Main St Preston - XSts: Harmony Rd / Lednum Ave
 (Chief ALT) [200 EMS] 27 B-Stab/gsw/pen. Trauma -- 6181 Bethlehem Rd Preston - XSts: Harmony Rd, Gilpin Point Rd / Bell Creek Rd, Grove Rd
 (Chief ALT) [200 EMS] 04 B-Assault -- 305 Bloomingdale Ave Federalsburg - XSts: N University Ave / Hayman Dr
 
Contact: sstarkey620 <sstarkey620@yahoo.com>
(Chief ALT) [600 EMS] 21 B-Hemmorrhage / Lacerations -- 420 Dutchmans Ln Greensboro - XSts: Dead End / Dead End
 
  */
 
 
 public class MDCarolineCountyParser extends DispatchAegisParser {
   
   public MDCarolineCountyParser() {
     super("CAROLINE COUNTY", "MD",
            "CALL ADDR XSts:X");
   }
   
  @Override
  public String getFilter() {
    return "msg@cfmsg.com";
  }
  
   // Assume single word city name, because that all they have
   private class MyAddressField extends AddressField {
     
     @Override
     public void parse(String field, Data data) {
       int pt = field.lastIndexOf(' ');
       if (pt < 0) abort();
       data.strCity = field.substring(pt+1).trim();
       super.parse(field.substring(0,pt).trim(), data);
     }
     
     @Override
     public String getFieldNames() {
       return super.getFieldNames() + " CITY";
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("ADDR")) return new MyAddressField();
     return super.getField(name);
   }
 }
