 package net.anei.cadpage.parsers.CO;
 
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.FieldProgramParser;
 
 /*
 Larimer County, CO (also Weld and Boulder Counties)
 Contact: Shane Doyon <shanedoyon@gmail.com>
 Sender: BFPD@notifyall.com
 System: Tritech?
 
 Weld County is a cross street.
 Default to Larimer county, but provide way to override empty set (None or NA)
 
 (BFPD) (CFS) Power Lines Down/Arcing        // E County Road 4 & Weld County // Apt/Lot            // Radio:MAC 5 NE                       //
 (BFPD) (CFS) MVA Unk Injury                 // 248790 Interstate 25 Sb // Apt/Lot            // Radio:TALK GP3                       // MM 249 SB I25
 (BFPD) (CFS) Grass / Wildland Fire          // 5307 Foothills Dr  // Apt/Lot            // Radio:TALK GP3                       //
 (BFPD) (CFS) Alarm Fire / Residential       // 20510 Weld County Road 5   // Apt/Lot            // Radio:TALK GP3                       // CLETCHER RESIDENCE
 (BFPD) (CFS) Convulsions/Seizures (C,D)     // 315 Goose Hollow Rd  // Apt/Lot            // Radio:TALK GP2                       //
 (BFPD) (CFS) Breathing Problems (E)         // 512 Redwood Cir  // Apt/Lot            // Radio:                               //
 
Contact: Josh Valerio <j247valerio@gmail.com>
Sender: LFR@notifyall.com
(NOTIFYall msg) (CFS) Alarm Fire / CO with Patients  // 1727 N Wilson Ave              // Apt/Lot 503        // Radio:TALK GP2                       //

  */
 
 
 public class COLarimerCountyParser extends FieldProgramParser {
   
   private static final Pattern SEPARATOR = Pattern.compile(" *// *");
 
   public COLarimerCountyParser() {
     super("LARIMER COUNTY", "CO",
            "CALL ADDR SKIP Radio:SKIP! INFO");
   }
   
   public String getFilter() {
    return "@notifyall.com";
   }
 
   @Override
   protected boolean parseMsg(String subject, String body, Data data) {
     
    Parser p = new Parser(subject);
    data.strSource = p.getOptional('|');
    if (!p.get().equals("CFS")) return false;
    if (data.strSource.toLowerCase().startsWith("notifyall")) data.strSource = "";
 
     String[] flds = SEPARATOR.split(body);
     return parseFields(flds, data);
   }
  
  @Override
  public String getProgram() {
    return "SRC " + super.getProgram();
  }
 }
