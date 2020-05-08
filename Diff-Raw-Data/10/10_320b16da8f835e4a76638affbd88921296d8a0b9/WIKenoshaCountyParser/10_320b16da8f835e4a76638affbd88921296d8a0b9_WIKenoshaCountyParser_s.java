 package net.anei.cadpage.parsers.WI;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.SmartAddressParser;
 
 /*
 Kenosha County, WI
 Contact:    David Wilkinson <firemedic53142@gmail.com>
 Sender: @kccjs.org
 
 LMC213 #:002011070865 ILS MED at 7701 95TH ST Rem: 50 M ;HEART PROBLEM CONSCIOUS: YES ,BREATHING: YES
 NLB260 #:002011071148 ALS MED at 4204 109TH ST Rem: 69 F ;CHEST PAIN CONSCIOUS: YES ,BREATHING: YES
 DMB193 #:002011072020 BLS MED at 10519 120TH AVE EAST TOURIST INFO CNTR Rem: CHEST PAIN
 DMB193 #:002011072054 SUPERVISOR ALRM at 7887 94TH AVE HOLIDAY INN EXPRESS
 DMB193 #:002011072060 MVA at 80TH AVE and COOPER RD
 
 Contact: "ksgrate@yahoo.com" <ksgrate@yahoo.com>
 From: CNT304 #:002011086007 ALS MED at 835 HICKORY RD Rem: 53 F ;SEIZURES CONSCIOUS: YES ,BREATHING: YES\37s
 
 */
 
 
 public class WIKenoshaCountyParser extends SmartAddressParser {
   
   private static final Pattern MARKER = Pattern.compile("^(?:From: )?[A-Z]{3}\\d{3} #:");
   
   public WIKenoshaCountyParser() {
     super("KENOSHA COUNTY", "WI");
   }
   
   @Override
   public String getFilter() {
     return "@kccjs.org";
   }
   
   @Override
   protected boolean parseMsg(String body, Data data) {
     Matcher match = MARKER.matcher(body);
     if (!match.find()) return false;
     body = body.substring(match.end()).trim();
     
     Parser p = new Parser(body);
     data.strCallId = p.get(' ');
     data.strCall = p.get(" at ");
     parseAddress(StartType.START_ADDR, p.get(" Rem: "), data);
     data.strPlace = getLeft();
     if (data.strAddress.length() == 0) return false;
     data.strSupp = p.get();
     return true;
   }
 }
