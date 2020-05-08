 package net.anei.cadpage.parsers.NC;
 
 import net.anei.cadpage.parsers.dispatch.DispatchSouthernParser;
 
 /* 
 Person County, NC
 Contact: Jonathan Wrenn <bwrenn89@gmail.com>
 Sender: 3365043576
 
 JBROWN:115 CROSS CREEK CT TIMBERLAKE 2011019922 18:27:47 FIRE STOVE
 Jason_White:HURDLE MILLS RD / PAYNES TAVERN RD 2011019901 15:46:01 MVA PI CARD 29 car vs motorcycle
 Shutchings:GUESS RD / HURDLE MILLS RD 2011019810 00:35:44 MVA PI CARD 29
 Shutchings:115 JACOBS-MOORE LN HURDLE MILLS 2011019797 23:05:28 FIRE STRUCTURE FLAMES VISIBLE the house is on fire
 Bmelton:HURDLE MILLS RD / POINDEXTER RD 2011019760 17:21:08 TREE DOWN
 BGILLISPIE:796 WHEELERS CHURCH RD HURDLE MILLS MDL 10C01 2011019661 05:54:13 CHEST PAIN CARD 10
 BROWN:225 CROWN BLVD TIMBERLAKE 2011020040 11:49:49 ALARMS-FIRE design works building 2 general fire alarm adt 8772857397
 
Contact: "todd.lane@yahoo.com" <todd.lane@yahoo.com>
Sender: BOAKLEY@personcounty.net
BOAKLEY:120 N OLD LANTERN RD TIMBERLAKE PLANTERS WAY X INT L/R CARRIAGE LN 18:44:47 FIRE STOVE GAS GRILL PROPANE TANK EXPLODED

 */
 
 public class NCPersonCountyParser extends DispatchSouthernParser {
   
   private static final String[] CITY_LIST = new String[]{
     "ROXBORO",
     
     "ALLENSVILLE",
     "BUSHY FORK",
     "CUNNINGHAM",
     "FLAT RIVER",
     "HOLLOWAY",
     "MT TIRZAH",
     "OLIVE HILL",
     "WOODSDALE",
     
     "HURDLE MILLS",
     "LEASBURG",
     "ROUGEMONT",
     "SEMORA",
     "TIMBERLAKE"
   };
 
   public NCPersonCountyParser() {
    super(CITY_LIST, "PERSON COUNTY", "NC", DSFLAG_DISPATCH_ID | DSFLAG_ID_OPTIONAL);
   }
 
 }
