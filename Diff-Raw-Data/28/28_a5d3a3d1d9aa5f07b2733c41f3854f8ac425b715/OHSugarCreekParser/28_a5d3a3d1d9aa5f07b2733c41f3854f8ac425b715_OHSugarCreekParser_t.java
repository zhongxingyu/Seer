 package net.anei.cadpage.parsers;
 
 /***
SugarCreek, OH
contact: Jon Fisher <stfd26@clearwire.net>
sender: CAD@sugarcreektownship.com <From%3ACAD@sugarcreektownship.com>

 "Ct:EMS MINOR       Loc:4336 STATE RT 725            Apt:      XSt:BAYBERRY COVE D LAKEMAN DR      Grid:003F Units:72                                        Rmk:"
 We use Cisco Public Safety�s ECAD program to page out CFS events.  CAD does send out multiple pages per event � one for the call and each responding unit.  Below are sample texts for review:
 "Ct:FIRE ALARM - RE Loc:4334 KILLIAN CT              Apt:      XSt:BELFAST DR                      Grid:0001 Units:M71   B70   P219                          Rmk:"
 "Ct:EMS MINOR       Loc:I 675 N/8MM                  Apt:      XSt:WILMINGTON PIKE FEEDWIRE RD     Grid:0045 Units:72                                        Rmk:"
 "Ct:TRAFFIC ACCIDEN Loc:FEEDWIRE RD/WINSHIRE TER     Apt:      XSt:LITTLE SUGARCRE LEDGESTONE BLVD Grid:0002 Units:C70   B70   E71   M72   P228              Rmk:"
 "Ct:OTHER FIRE      Loc:5980 WILMINGTON PIKE         Apt:      XSt:CLYO RD         WILMINGTON PIKE Grid:003A Units:72                                        Rmk:"
Ct:EMS MINOR       Loc:6244 WILMINGTON PIKE         Apt:      XSt:CLYO RD         CENTER POINT DR Grid:003B Units:E74   M72   R70   P241 
 ***/
 
 public class OHSugarCreekParser extends DispatchCiscoParser {
   
   public OHSugarCreekParser() {
     super(null, "SugarCreek", "OH");
   }
  
  @Override
  public String getFilter() {
    return "CAD@sugarcreektownship.com";
  }
 }
