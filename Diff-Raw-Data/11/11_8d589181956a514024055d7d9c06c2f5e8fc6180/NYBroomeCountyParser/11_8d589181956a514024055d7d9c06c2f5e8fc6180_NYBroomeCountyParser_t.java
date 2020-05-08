 package net.anei.cadpage.parsers.NY;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.SmsMsgInfo.Data;
 import net.anei.cadpage.parsers.FieldProgramParser;
 
 
 /*
 Broome County, NY
 Contact: Chuck Rogers <cmr49a@gmail.com> (Dispatch CAD Administrator)
 Contact: David Abell <daemt25@gmail.com>
 Sender: messaging@iamresponding.com <From%3Amessaging@iamresponding.com>
 System: New World
 
 (49 Sanitaria Spr Fire) 49-DIABETIC-C :2786 ROUTE 79 :78 yo fem high blood sugar/unable t   &lt;13C02&gt; :78 year old, Female, Conscious, Breathing.  A
 25:DIABETIC-C :412 ADAMS AV APT 1S :67 yom diabetic / low sugar &lt;13C01&gt; :67 year old, Male, Conscious, Breathing. Not alert (Combative or Agg ressive). Cross Sts:TRACY ST/RIVERVIEW DR 01:29 02/07/2011 2011-00002425 Caller:BATTISTI,MAURICE,, Phone:607-239-4661 V/Endicott
 25:OVERDOSE-B :431 E MAIN ST MANLEY'S 25-1 :FEM INTOX - CONSC/ALERT 22.09 TXFR 23-B-1 : Cross Sts:LODER AV/VESTAL AV 00:52 02/07/2011 2011-00002423 Caller:LASKOWSKY,OFC,, Phone: V/Endicott
 25:FALLS -D :600 HIGH AV UHS SKILLED NURSING :87 YOF FALLEN/NOT ALERT 17-D-3 : ADULT CARE/DAVIS AV ENTRANCE ROOM 413 FLR 4 Cross Sts:W EDWARD ST/RIVER TERR 22:50 02/06/2011 2011-00002416 Caller:IDEAL SENIOR LIVING, Phone:607-786-7449 V/Endicott
 25:ALARM-BLDG :9 HILLSIDE CT APT 4 :POSS FIRE ALARM GOING OFF / TENANTS LEFT :NO SMELL OF SMOKE OR FIRE SEEN COMPL IN APT 1 Cross Sts:W MAIN ST/DEAD END 18:03 02/06/2011 2011-00000251 Caller:RANDO,TIM,, Phone:607-727-1115 V/Endicot
 25:ALARM-BLDG :722 W MAIN ST :GENERAL ALARM : ENJOY GOLF COURSE Cross Sts:ENDICOTT V-LINE/INDUSTRIAL PARK RD 14:40 02/06/2011 2011-00000249 Caller:SENTRY OP 79,,, Phone: V/Endicott
 (25 Endicott Fire) 25-BREATHNG-C :308 ARTHUR AV APT 3 :40 yom diff breathing &lt;06C01&gt; :40 year old, Male, Conscious, Breathing. Abnormal breathing  (Asthma) . Cross StsE MAIN ST/TRACY ST 00:12 02/01/2011 2011-00002020 Caller:WILLIAMS,CINDY,, Phone:000-242-5076 V/Endicott
 (25 Endicott Fire) 25:CHSTPAIN-C :415 E MAIN ST LOURDES PRIMARY 25 :41 yof chest pain &lt;10C04&gt; :41 year old, Female, Conscious, Breathing.  Breathing normally =&gt; 35. Cross Sts:LODER AV/VESTAL AV 17:39 02/01/2011 2011-00002058 Caller:CUTTING,SANDY,, Phone:607-786-180 V/Endicott
 (25 Endicott Fire) 25:SEIZURES-D :326 JENNINGS ST :42 yof seizure                        &lt;12D02&gt; :42 year old, Female, Unconscious, Breathing.  CO
 (59 Five Mile Pt Fire) 59:DIABETIC-C :913 ROUTE 11 APT 2D COUNTRYSIDE VILLAGE :34 YOF DIABETIC PROBLEM               &lt;13C01&gt; :34 year old, Female,
 
 Contact: Kahl <kdmiller324@aol.com>
 Sender: mplus@co.broome.ny.us
 ((26873) ) ) 32:ALARM-HOUS :416 E BENITA BLVD :SMOKE ALARM GOING OFF INTERMITTENLY IN :BASEMENT. NO SMOKE OR FIRE VISIBLE-POSS PROBLEM WITH DETECTOR Cross Sts
 
 Contact: Neal Haight <dmbfn290@gmail.com>
 Sender: mplus@co.broome.ny.us
 Subject:(11470) ) \nCOMM:CHSTPAIN-C :27 GOLDEN LN HARPURSVILLE SENIOR :66yom chest pain                      <10C02> :66 year old, Male, C
 Subject:(12610) ) \nCOMM:UNRSPNSV-D :528 JENSEN RD :81 YOM NOT ALERT/SEVERE HEADACHE/DELTA :CALLER IS VESTAL CREW CHIEF OR DRIVER/CAN DO E
 
Contact: "J.E. Dancesia" <john@ezems.com>
.... (46 Conklin Fire) 46:CHSTPAIN-D :12 WILLOW WAY :84 yom chest pain                     &lt;10D03&gt; :84 year old, Male, Conscious, Breathing.  CHANGING COLOR. Cross Sts:CONKLIN RD/DAVID RD 18:55 07/24/2011 2011-00013205 Caller:CEBULA,JAMES,, Phone:000-775-2793 T/Conklin
.... (46 Conklin Fire) 46:FALLS   -D :1186 CONKLIN RD :69 YOM FALLEN                         &lt;17D05&gt; :69 year old, Male, Conscious, Breathing.  LONG FALL. Cross Sts:POWERS RD/CAROL CT 13:54 07/23/2011 2011-00013114 Caller:KING,PAULINE,, Phone:000-206-2374 T/Conklin

 */
 
 
 public class NYBroomeCountyParser extends FieldProgramParser {
   
  private static Pattern PREFIX = Pattern.compile("^\\.{4} \\(.*?\\) ");
   private static Pattern LEADER = Pattern.compile("^([A-Z0-9]+)[\\-:]");
   private static Pattern TRAILER = Pattern.compile(" V/Endicott? *$");
   private static Pattern KEYWORD_PAT = Pattern.compile(" (|Cross Sts|Caller|Phone):");
   private static Pattern DATE_TIME_PAT = Pattern.compile(" \\d\\d:\\d\\d \\d\\d/\\d\\d/\\d{4} ");
   private static Pattern TRAIL_COMMA_PAT = Pattern.compile("[ ,]+$");
     
     public NYBroomeCountyParser() {
       super("BROOME COUNTY", "NY",
              "SRC CALL ADDR/SXP! INFO+ Cross_Sts:X Caller:NAME Phone:PHONE");
     }
     
     @Override
     public String getFilter() {
       return "messaging@iamresponding.com,mplus@co.broome.ny.us";
     }
 
 	  @Override
 	  protected boolean parseMsg(String body, Data data) {
 	    body = body.trim().replaceAll("  +", " ");
	    Matcher match = PREFIX.matcher(body);
	    if (match.find()) body = body.substring(match.end()).trim();
	    match = TRAILER.matcher(body);
 	    if (match.find()) body = body.substring(0,match.start()).trim();
 
 	    // Fix up leading field separator
 	    if (body.startsWith(")")) body = body.substring(1).trim();
 	    body = LEADER.matcher(body).replaceFirst("$1 :");
 	    
 	    // Using a colon as both a field separator and a keyword indicator makes life complicated :(
 	    List<String>fldList = new ArrayList<String>();
 	    match = KEYWORD_PAT.matcher(body);
 	    int st = 0;
 	    while (match.find()) {
 	      int end = match.start();
 	      if (end > st) fldList.add(body.substring(st, end).trim());
 	      st = end+1;
 	      end = match.end();
 	      if (end == st + 1) st = end;
 	    }
 	    int end = body.length();
       if (end > st) fldList.add(body.substring(st, end).trim());
       String[] flds = fldList.toArray(new String[fldList.size()]);
 	    
 	    return parseFields(flds, data);
 	  }
 	  
 	  // Source code must be 2 digits or COMM
 	  private class MySourceField extends SourceField {
 	    public MySourceField() {
 	      setPattern(Pattern.compile("\\d\\d|COMM"), true);
 	    }
 	  }
 
 	  // Cross street field needs to parse time, date, and ID data from field
 	  private class MyCrossField extends CrossField {
 
       @Override
       public void parse(String field, Data data) {
         Matcher match = DATE_TIME_PAT.matcher(field);
         if (match.find()) {
           data.strCallId = field.substring(match.end()).trim();
           field = field.substring(0, match.start()).trim();
         }
         super.parse(field, data);
       }
 
       @Override
       public String getFieldNames() {
         return "X ID";
       }
 	  }
 	  
 	  // Name field needs to remove trailing commas
 	  private class MyNameField extends NameField {
 
       @Override
       public void parse(String field, Data data) {
         
         Matcher match = TRAIL_COMMA_PAT.matcher(field);
         if (match.find()) {
           field = field.substring(0, match.start());
         }
         super.parse(field, data);
       }
 	  }
 
     @Override
     protected Field getField(String name) {
       if (name.equals("SRC")) return new MySourceField();
       if (name.equals("X")) return new MyCrossField();
       if (name.equals("NAME")) return new MyNameField();
       return super.getField(name);
     }
 	  
 	}
 	
