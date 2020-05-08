 package net.anei.cadpage.parsers.NC;
 
 import net.anei.cadpage.parsers.dispatch.DispatchSouthernParser;
 
 /* 
 Moore County, NC
 Contact: Daniel Strong <dstrong1970@yahoo.com>
 Sender: dispatch911@moorecountync.gov
 
 [[911 NOTIFICATION] ]  505 PINEHURST ST ABERDEEN 2011024766 19:52:16 F69 STRUCTURE FIRE LOOKS LIKE BACK DECK ON FIRE
 [[911 NOTIFICATION] ]  1 S. VINELAND SOUTH CELL TOWER 2011025029 09:31:20 M25 PSYCHIATRIC/SUICIDE ATTEMPT
 [[911 NOTIFICATION] ]  126 ROBIN HOOD LN ABERDEEN 2011025065 13:37:27 F52 ALARM-FIRE ACTIVATION AC 800 932-3822 OPER SP8
 [[911 NOTIFICATION] ]  632 SAND PIT RD ABERDEEN MDL 07A01 2011025721 11:06:58 M7 BURNS/EXPLOSIONS
 [[911 NOTIFICATION] ]  218 BERRY ST PINE BLUFF 2011026135 14:36:24 F67 OUTSIDE FIRE/WOODS/BRUSH TREE ON FIRE
 [[911 NOTIFICATION] ]  1 E NEW ENGLAND /S PEAR 2011025862 07:31:07 F67 OUTSIDE FIRE/WOODS/BRUSH
 
 Contact: Stephanie Dziok <dzioks@gmail.com>
 6 PAR DR WHISPERING PINES 2011045777 11:36:50 M10 CHEST PAIN CHEST PAIN
 
 Contact: David Alston <davidalston66@gmail.com>
 Contact: Alan Holmes <lieutenant41m@gmail.com>
 Contact: Richard Harris <mntgrown@yahoo.com>
 Sender: dispatch911@moorecountync.gov
 232 ALLEN LN ABERDEEN MDL 30A01 2012028950 15:18:54 M30 TRAUMATIC INJURIES\r
 358 R SANDS RD ABERDEEN, MDL 31D02, 2012031517, 15:45:55, M31 UNCONSCIOUSNESS/FAINTING (NEAR),\r
 3216 CALLAWAY RD, 2012031795, 05:27:06, F65 MUTUAL AID, COMMERCIAL STRUCTURE CROSS STREET MONTROSE
 S: M:1885 ADDOR RD ABERDEEN, 2012031801, 07:04:25, F18 STORM DAMAGE, 
 
 */
 
 public class NCMooreCountyParser extends DispatchSouthernParser {
   
   public NCMooreCountyParser() {
     super(CITY_LIST, "MOORE COUNTY", "NC", DSFLAG_UNIT);
   }
   
   @Override
   public String getFilter() {
     return "dispatch911@moorecountync.gov";
   }
   
   private static final String[] CITY_LIST = new String[]{
     "ABERDEEN",
     "CAMERON",
     "CARTHAGE",
     "FOXFIRE",
     "PINEBLUFF",
     "PINE BLUFF",
     "PINEHURST",
     "ROBBINS",
     "SEVEN LAKES",
     "SOUTHERN PINES",
     "TAYLORTOWN",
     "VASS",
     "WEST END",
     
     "CARTHAGE TWP",
     "BENSALEM TWP",
     "SHEFFIELDS TWP",
     "RITTER TWP",
     "DEEP RIVER TWP",
     "GREENWOOD TWP",
     "MCNEILL TWP",
     "SANDHILL TWP",
     "MINERAL SPRINGS TWP",
     "LITTLE RIVER TWP"
   };
 }
