 package net.anei.cadpage.parsers.NY;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.dispatch.DispatchProQAParser;
 
 /*
 Buffalo, NY
 Contact: Kunch <joshkunch@msn.com>
 Sender: 2002000004
 RC:Run# 4330/1 Hot 911 & Pvt/28B1 Stroke/CVA-unknown status/189 LANGFIELD DR///BUFFALO/14215////14:18/<Unknown>////X=TOWER ST&SUFFOLK ST
 RC:Run# 3992/1 Hot 911 & Pvt/29B1 Traffic/Transport Accidents-injuries/E  LOVEJOY ST & N  OGDEN ST///BUFFALO/14206////17:35/<Unknown>////X=SCHILLER ST&N OGDEN 
 RC:Run# 4364/1 Hot 911 & Pvt/6C1 Breathing Problems-abnormal breathing/2128 ELMWOOD AVE///BUFFALO/14207////15:57/<Unknown>////X=HINMAN ST&DRIVEWAY
 RC:Run# 4330/1 Hot 911 & Pvt/28B1 Stroke/CVA-unknown status/189 LANGFIELD DR///BUFFALO/14215////14:18/<Unknown>////X=TOWER ST&SUFFOLK ST
 RC:Run# 3969/2 Cold  911/26A01 Sick-no priority symptoms(not 2-28)/BAILEY AVE & GENESEE ST///BUFFALO/14211////16:39/<Unknown>////X=WARRING AVE&USHY 62
 RC:Run# 3926/1 Hot 911 & Pvt/27B2 Stab/GSW/Penetrating Trauma-single peripheral wound/192 DAVIDSON AVE///BUFFALO/14215////14:49/<Unknown>////X=GODFREY ST&ORLEANS ST
 RC:Run# 3449/2 Cold  911/13A1 Diabetic-alert/behaving normally/1250 BAILEY AVE///BUFFALO/14206////07:50/<Unknown>////X=BOGARDUS ST&HENNEPIN ST
 RC:Run# 929/1 Hot 911 & Pvt/9E1 Cardiac/Resp Arrest/DOA-not breathing at all/69 SIDNEY ST///BUFFALO/14211////09:44/<Unknown>////X=FILLMORE AVE&RICKERT AVE
 
  */
 
 public class NYBuffaloParser extends DispatchProQAParser {
   
   public NYBuffaloParser() {
     super("BUFFALO", "NY",
           "SKIP CALL CALL? ADDR CITY ID X:X");
   }
   
   @Override
   public String getFilter() {
     return "2002000004";
   }
 
   @Override
   public boolean parseMsg(String body, Data data) {
     body = body.replace("/X=", "/X:");
     return super.parseMsg(body, data);
   }
 }
