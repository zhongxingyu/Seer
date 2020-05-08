 package net.anei.cadpage.parsers.NY;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.SmartAddressParser;
 
 /*
 Nassau County, NY (version B)
 Contact: Tommy Johnson <tjohnson377.tj@gmail.com>
 Sender: paging@alpinesoftware.com
 
 FireCom:  3 HUDSON ST E (GENERAL-AUTO) FIRE ALARM RINGING
 FireCom:  31 MUNSON PL (SIG 9-AMBU) 20F ABDOMINAL PAINS
 FireCom: THE FISHERY 1 MAIN ST (SIG 9-AMBU) MALE FELL-FX WRIST 
 FireCom:  528 ATLANTIC AVE (SIG 9-AMBU) BACK DOOR DIFF BREATHING
 FireCom:  RHAME AVE (DISTRIC-HAZM) SW CORNER/SEWER-ILLEGALE DUMPING
 FireCom:  22 RIVERSIDE ROAD (SIG 9-AMBU) DEHYDRATED MA
 FireCom:  65 EMMET AVE (GENERAL-AUTO) ESTRIN RES/40A024
 
 Contact: cjk152@aol.com
 FRM:paging@alpinesoftware.com\nMSG:\nRedAlert: 528 MERRICK ROAD (SIG 9A-AMBU) SICK MALE
 
 Contact: Besnik Gjonlekaj <tigerfdny@gmail.com>
 ﻿FireCom:  180 KAMDA BLVD C/S: STEPHAN AVE / FRANKLIN AVE -    3 (RESCUE-AMBU) MALE FELL/HEAD INJURY D: 06/15 17:25 #: 000486﻿
 FireCom: NEW HYDE PARK DINER 160 HILLSIDE AVE C/S: 1ST (N) ST / 2ND (N) ST -    3 (RESCUE-AMBU) ELDERLY FEMALE FELL/HEAD INJURY D: 05﻿/31 08:25 #: 000424
 FireCom:  WEST END AVE C/S: LEONARD BLVD -    1 (RESCUE-AMBU) DRIVER SICK ON SCHOOL BUS D: 06/14 15:53 #: 000484﻿
 FireCom: NEW HYDE PARK FD HEADQUARTERS 1555 JERICHO TPK C/S: NEW HYDE PARK ROAD / TERRACE BLVD -    1 (RESCUE-AMBU) SICK FEMALE ON RA﻿MP D: 06/13 14:25 #: 000480
 FireCom:  1300 5TH AVE C/S: 13TH (S) ST / 14TH (S) ST - 2 (GENERAL-NATU) MAIN LINE BREAK BY CONTRACTOR D: 06/14 13:04 #: 000482﻿
 FireCom: HOLY SPIRIT SCHOOL 13 6TH ST S C/S: JERICHO TPK / 1ST AVE -    2 (GENERAL-NATU) INSIDE THE SCHOOL D: 06/14 13:29 #: 000483
 
 */
 
 
 public class NYNassauCountyBParser extends SmartAddressParser {
   
   private static final Pattern MARKER = Pattern.compile("^(?:FireCom:|RedAlert:)");
   private static final Pattern CALL_PTN = Pattern.compile(" *\\(([\\- A-Z0-9]{2,})\\) *");
   private static final Pattern PRIORITY_PTN = Pattern.compile(" - +(\\d)$");
 
   public NYNassauCountyBParser() {
     super("NASSAU COUNTY", "NY");
     addExtendedDirections();
   }
   
   public String getFilter() {
    return "paging@alpinesoftware.com";
   }
 
   @Override
   protected boolean parseMsg(String body, Data data) {
     
     Matcher match = MARKER.matcher(body);
     if (!match.find()) return false;
     body = body.substring(match.end()).trim();
     
     match = CALL_PTN.matcher(body);
     if (!match.find()) return false;
     String sAddr = body.substring(0, match.start());
     data.strCall = match.group(1);
     String sExtra = body.substring(match.end());
     
     match = PRIORITY_PTN.matcher(sAddr);
     if (match.find()) {
       data.strPriority = match.group(1);
       sAddr = sAddr.substring(0,match.start()).trim();
     }
     parseAddress(StartType.START_PLACE, FLAG_ANCHOR_END, sAddr, data);
     
     Parser p = new Parser(sExtra);
     data.strSupp = p.get(" D:");
     p.get(" #:");
     data.strCallId = p.get();
 
     return true;
   }
 }
 
 
