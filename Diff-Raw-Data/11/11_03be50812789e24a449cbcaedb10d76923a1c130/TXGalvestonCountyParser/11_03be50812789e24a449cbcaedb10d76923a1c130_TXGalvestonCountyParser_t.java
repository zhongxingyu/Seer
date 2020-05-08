 package net.anei.cadpage.parsers.TX;
 
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.MsgInfo.Data;
 import net.anei.cadpage.parsers.dispatch.DispatchOSSIParser;
 
 /* 
 Galveston County, TX
 Contact: Shawn Sprayberry <shawnsprayberry@gmail.com>
 Sender: iammessaging.com,777,410
 
 Mutual aid calls
 SANT = Santa fe
 HITC = Hitchcock
 DICK = Dickinson
 
 .... (Santa Fe Fire) CAD:GRASS FIRE 12603 PONDEROSA DR
 .... (Santa Fe Fire) CAD:GAS LEAK 11929 15TH ST SMELL OF GAS [10/27/11 13:40:00 JHILLMAN]
 .... (Santa Fe Fire) CAD:GRASS FIRE 5021 AVE O ST LARGE GRASS FIRE [10/28/11 09:48:02 EATONR]
 .... (Santa Fe Fire) CAD:FYI: FIRE - NON SPECIFIC 4146 HOLLOWAY RD/HWY 6 ST
 .... (Santa Fe Fire) CAD:POLE FIRE 7231 AVE M ST GRASS IS ON FIRE ALSO [10/31/11 13:17:49 EATONR] UTILITY POLE ON FIRE [10/31/11 13:17:31 EATONR]
 .... (Santa Fe Fire) CAD:FYI: FIRE - NON SPECIFIC 4607 GEORGIA ST LARGE TRASH FIRE UNATTENDED [11/03/11 07:59:46 JHILLMAN]
 .... (Santa Fe Fire) CAD:LINE DOWN 1946 N FM 646 RD/HARRIETT LN LINE DOWN ON THE GROUND [11/03/11 09:55:42 JHILLMAN]
 .... (Santa Fe Fire) CAD:FYI: FIRE - NON SPECIFIC 7600 AVE E ST between 32nd st and 33rd st..res is on Ave E..large fire appears to be speading to brush [11/30/11 20:49:27 BSCOTT]
 .... (Santa Fe Fire) CAD:FYI: FIRE - NON SPECIFIC 7330 AVE M ST FIRE IN HOME HEATER CENTRAL UNIT SMOKE THEN FLAMES IS OUT NOW WITH WATER IN TRL [12/07/11 10:30:23 JHILLMAN]
 .... (Santa Fe Fire) CAD:Update: FIRE - NON SPECIFIC 12619 HARRIETT LN SMALL CONT IN FENCE DUE TO WEATHER FIRE PUT OUT [12/07/11 12:58:06 JHILLMAN] IN AREA LOOKING OUT [12/07/11 12:53:05 JHILLMAN] NO CONTROL BURNS CALLED IN TODAY ON HARRIET LN MADE CONT WITH FI
 
.... (Santa Fe Fire) LANDING ZONE 15117 WALNUT ST Event spawned from STROKE. [03/04/2012 21:08:14 BSCOTT] {SFMED1} NEED LF OUT HERE [03/04/12 21:07:26 BSCOTT] CALLER ADV HE ASKED HIS WIFE IF SHE KNEW WHO HE WAS AND SHE THOUGHT HE WAS AN EMT.. [0
.... (Santa Fe Fire) POLE FIRE 5318 IKE FRANK RD CLR ADV THERE WAS A TRANSFORMER ON FIRE AT THIS ADDRESS. AVD THAT IT IS OUT. HAS STOPPED SMOKING [03/04/12 07:48:36 JIDEMA]

  */
 
 public class TXGalvestonCountyParser extends DispatchOSSIParser {
   
  private static final Pattern SUBJECT_PTN = Pattern.compile(".* FIRE", Pattern.CASE_INSENSITIVE);
  
   public TXGalvestonCountyParser() {
     super("GALVESTON COUNTY", "TX",
           "ADDR/SCI INFO+");
   }
   
   @Override
   public String getFilter() {
     return "iammessaging.com,777,410";
   }
   
   @Override
   protected boolean parseMsg(String subject, String body, Data data) {
    if (! SUBJECT_PTN.matcher(subject).matches()) return false;
     data.strSource = subject;
    if (!body.startsWith("CAD:")) body = "CAD:" + body;
     return super.parseMsg(body, data);
   }
   
   @Override
   public String getProgram() {
     return "SRC " + super.getProgram();
   }
   
   private static final Pattern AVE_X_ST_PTN = Pattern.compile("\\b(AVE [A-Z]) ST\\b");
   private class MyAddressField extends AddressField {
     @Override
     public void parse(String field, Data data) {
       if (field.startsWith("FYI:")) field = field.substring(4).trim();
       super.parse(field, data);
       data.strAddress = AVE_X_ST_PTN.matcher(data.strAddress).replaceAll("$1");
     }
     
     @Override
     public String getFieldNames() {
       return super.getFieldNames() + " X";
     }
   }
   
   @Override
   public Field getField(String name) {
     if (name.equals("ADDR")) return new MyAddressField();
     return super.getField(name);
   }
 }
