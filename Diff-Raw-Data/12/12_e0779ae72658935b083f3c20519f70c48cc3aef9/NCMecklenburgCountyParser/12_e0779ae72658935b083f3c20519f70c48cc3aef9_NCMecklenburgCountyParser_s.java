 package net.anei.cadpage.parsers.NC;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.SmsMsgParser;
 
 /*
 Mecklenburg County, NC
 Contact: Rick <very.orange@gmail.com>
 Sender: paging@rcsepager.com
 System: TriTech VisiCAD
 
 Subject:Text Page\n18758 Silver Quay Dr                                                  Charlie                       17- Falls/Back injur
 Subject:Text Page\n03172011-073     Received:  08:42    Assigned:  08:43    Enroute:   08:45    Arrived:   08:48    Pt Contact:         Can
 Subject:Text Page\n19485 Old Jetton Rd           ste 100   Lakeside Familty Physicians anDelta                         31- Unconsciousness/
 Subject:Text Page\n03172011-239     Received:  16:59    Assigned:  16:59    Enroute:   17:01    Arrived:   17:02    Pt Contact:         Can
 
 Contact: Tucker Sizemore <tsizemore@gmail.com>
 Sender: @huntersvillefd.com
 (Text Page) 04022011-202     Received:  15:29    Assigned:  15:29    Enroute:            Arrived:            Pt Contact:
 (Text Page) 16710 Northcross Dr                                                   Fire -  Emergency             59-Fuel Spi
 (Text Page) 04022011-238     Received:  16:50    Assigned:  16:50    Enroute:   16:51    Arrived:   16:59    Pt Contact:
 
 */
 
 public class NCMecklenburgCountyParser extends SmsMsgParser {
   
   public NCMecklenburgCountyParser() {
     super("MECKLENBURG COUNTY", "NC");
   }
   
   @Override
   public String getFilter() {
     return "paging@rcsepager.com,@huntersvillefd.com";
   }
   
   @Override
   public boolean parseMsg(String subject, String body, Data data) {
     
     if (!subject.equals("Text Page")) return false;
     if(body.length() < 110) return false;
     if (body.contains("Received:")) return false;
     parseAddress(body.substring(0,30).trim(), data);
     data.strApt = body.substring(30,40).trim();
     data.strPlace = body.substring(40,70).trim();
     data.strSupp = body.substring(70,100).trim();
     data.strCall = body.substring(100).trim();
     
     return true;
   }
 
 }
