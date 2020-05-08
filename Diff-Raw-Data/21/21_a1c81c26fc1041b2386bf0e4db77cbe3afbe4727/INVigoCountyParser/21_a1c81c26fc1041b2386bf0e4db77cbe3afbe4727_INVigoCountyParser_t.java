 package net.anei.cadpage.parsers.IN;
 
 import java.util.Properties;
 
import net.anei.cadpage.parsers.MsgInfo.Data;
 import net.anei.cadpage.parsers.dispatch.DispatchCiscoParser;
 
 /***
 Vigo County, IN
 Contact: Billy Roberts <thfd334@gmail.com>
Sender: 37284,cisco.paging@vigocounty.in.gov
 System: Cisco
 Ct:FALL Loc:3301 ST MARY'S RD Apt: XSt:BLOOMTOWN RD US HWY 150 Grid:112D Units:E-42 TC-1 Rmk:92 YOLD FML; FELL; FACE DOWN ON FLOOR -
 Ct:FALL Loc:309 N CHURCH ST Apt: XSt:PARIS AV Grid:0WTH Units:E-41 TC-2 Rmk:65 Years F ;fall
 Ct:CHEST PAIN Loc:500 W PARIS AV Apt: XSt:6TH ST WTH 4TH ST WTH Grid:0WTH Units:E-41 TC-1 Rmk:
 Ct:CHOKING Loc:2925 N MAPLE PL Apt: XSt:LOCUST AV Grid:0126 Units:E-42 TC-1 Rmk:18 Months F ;CHOKING
 Ct:MVA-UNKNOWN-INJ Loc:US HWY 40/INTERSTATE 70 Apt: XSt: Grid:0169 Units:E-41 Rmk:SEMI AND PASSENGER CAR//UNKNOWN Injuries
 Ct:EMS MAJOR Loc:6244 WILMINGTON PIKE Apt: XSt:CLYO RD CENTER POINT DR Grid:003B Units:C70 M71 P223 Rmk:
 
 Contact: "Dennis Boyd" <dboyd462@gmail.com>
 Ct:FIRE-ENTRAPMENT Loc:101 N 7TH ST WTH Apt: XSt:JOHNSON AV MILLER AV Grid:0WTH Units:E-41 E-42 L-41 R-41 Rmk:HOUSE ON FIRE PEOPLE STILL INSIDE
 
Contact: Billy Roberts <thfd334@gmail.com>
Sender: 37284,cisco.paging@vigocounty.in.gov
[TRASH OR RUBBISH-FIRE]  Loc:428 W POPLAR ST XSt:5TH ST WTH 4TH ST WTH Grid:0WTH Units:E-41 Rmk:RP ADV MALE SUBJECT THERE THAT IS CAUSING PROBLEMS AND

 ***/
 
 public class INVigoCountyParser extends DispatchCiscoParser {
   
   private static final Properties CITY_CODES = buildCodeTable(new String[]{
       "WTH", "WEST TERRE HAUTE"
   });
   
   public INVigoCountyParser() {
     super(CITY_CODES, "VIGO COUNTY", "IN");
   }
   
   @Override
   public String getFilter() {
    return "37284,cisco.paging@vigocounty.in.gov";
  }
  
  @Override
  protected boolean parseMsg(String subject, String body, Data data) {
    if (!body.startsWith("Ct:")) {
      if (subject.length() == 0) return false;
      body = "Ct:" + subject + " " + body;
    }
    return super.parseMsg(body, data);
   }
 }
