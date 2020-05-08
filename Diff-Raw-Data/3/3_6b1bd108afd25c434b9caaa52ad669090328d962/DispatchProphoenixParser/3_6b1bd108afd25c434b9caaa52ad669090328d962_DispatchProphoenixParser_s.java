 package net.anei.cadpage.parsers.dispatch;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.parsers.FieldProgramParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 
 /*
 Wayne County, NJ
 2012000006 {01/03/2012 10:58:16}\nAFCI     - Fire alarm - commercial/industrial{3}\n3 Willowbrook Mall;PD\nUnits:\n\nComments:\n  Station Dispatched : {C1 } Station Dispatched : {C2 } Units Recommended: ,;\nDispatched: {C2 }, {C1 } ZONE 3 FIRE ALARM/IDHIT/8772387739/ACWN/ADT
 2012000019 {01/04/2012 11:47:19}\nFCI      - Fire - commercial/industrial{1}\n1520 Rt23;PD\nUnits:\n\nComments:\n  Station Dispatched : {C3 } Station Dispatched : {C1 } Loc changed  \nfrom {1530 Rt23;PD} to {1520 Rt23;PD}\n\nPri from -> 5 to 1\n  Station Dispatched : {C2 } Station Dispatched : {C5 } Units Recommended: (Not able to recommend);\nDispatched: {C5 }, {C2 } Elco admin services\nRough,Jim  862 226 9566\nCaller stated her heard a popping sound inside the building and they nsmell smoke\nAddress in 1538 rt23  behing wayne Hardware Update reviewed by dispatcher- Peterson, Tracy L-716
 2012000017 {01/04/2012 10:49:09}\nAFCI     - Fire alarm - commercial/industrial{3}\n520 Rt46;PD.\n  Units:\n\nComments:\n  Station Dispatched : {C1 } Station Dispatched : {C2 } Units Recommended: ,;\nDispatched: {C2 }, {C1 } Adt/877 2387739/id iub/acwn\nRed Lobster\nzone 18 ANSUL
 2012000022 {01/04/2012 20:21:09}\nMVAE     - MVA - extrication{1}\nValley Rd/French Hill Rd;PD\nUnits:\n\nComments:\n  Station Dispatched : {R5 } Station Dispatched : {C1 } Units Recommended: ,;\nDispatched: {C1 }, {R5 } Caller states the white suv flipped over  -  \nmight be trapped inside - caller not involed - Caller states white suv  \nposs trapped States 4 veh involved - caller not involed -
 2012000023 {01/05/2012 00:07:46}\nAFCI     - Fire alarm - commercial/industrial{2}\n1 Willowbrook Mall;PD \nUnits:\n\nComments:\n   Station Dispatched : {C1 } Station Dispatched : {C2 } Units Recommended: ,;\nDispatched: {C2 }, {C1 } PreEmpt - 03N\nCode 4 MaCYS COMM CENTER/8662297579/opred/acwn glass beak perimeter
 
 Kenosha County, WI
 Subject:Phoenix Notification\n2012000453 {04/15/2012 14:23:51}\r\nMED CALL - Medical Call{1}\r\n9747 84th Pl\r\nUnits:\r\n    5642    \r\nComments:\r\n\r
 Subject:Phoenix Notification\n2012000454 {04/15/2012 17:21:48}\r\nFIRE SNG - Fire - Single Engine{1}\r\n905 114th St\r\nUnits:\r\n    5641    5611  \r
 Subject:Phoenix Notification\n2012000454 {04/15/2012 17:21:48}\r\nFIRE SNG - Fire - Single Engine{1}\r\n905 114th St\r\nUnits:\r\n    5641    5611  \r
 Subject:Phoenix Notification\n2012000619 {05/16/2012 17:47:56}\r\nFIRE ALA - Fire - Alarm{1}\r\n9901 77th St,CMPLX\r\nUnits:\r\n    5642    \r\nCommen\r
 Subject:Phoenix Notification\n2012000618 {05/16/2012 13:31:17}\r\nMED CALL - Medical Call{1}\r\n13000BLK 104th St\r\nUnits:\r\n    5642    \r\nComment\r
 
 */
 
 public class DispatchProphoenixParser extends FieldProgramParser {
   
   public DispatchProphoenixParser(String defCity, String defState) {
     super(defCity, defState,
            "IDDATETIME CALL ADDR! Units:UNIT+ Comments:INFO+");
   }
   
   @Override
   public boolean parseMsg(String body, Data data) {
     return parseFields(body.split("\n"), data);
   }
   
   private static final Pattern ID_DATE_TIME_PTN = 
     Pattern.compile("(\\d{10}) \\{(\\d\\d/\\d\\d/\\d{4}) (\\d\\d:\\d\\d:\\d\\d)\\}");
   private class IdDateTimeField extends Field {
     @Override
     public void parse(String field, Data data) {
       Matcher match = ID_DATE_TIME_PTN.matcher(field);
       if (!match.matches()) abort();
       data.strCallId = match.group(1);
       data.strDate = match.group(2);
       data.strTime = match.group(3);
     }
     
     @Override
     public String getFieldNames() {
       return "ID DATE TIME";
     }
   }
   
   private static final Pattern CALL_PTN = Pattern.compile("([A-Z ]+) *- *(.*?) *\\{(\\d)\\}");
   private class MyCallField extends CallField {
     @Override
     public void parse(String field, Data data) {
       Matcher match = CALL_PTN.matcher(field);
       if (!match.matches()) abort();
       data.strCode = match.group(1).trim();
       data.strCall = match.group(2).trim();
       data.strPriority = match.group(3);
     }
     
     @Override
     public String getFieldNames() {
       return "CODE CALL PRI";
     }
   }
   
   private class MyAddressField extends AddressField {
     @Override
     public void parse(String field, Data data) {
       int pt =  field.lastIndexOf(';');
       if (pt >= 0) field = field.substring(0,pt).trim();
       pt = field.lastIndexOf(',');
       if (pt >= 0) {
        
       }
       super.parse(field, data);
     }
   }
   
   private class MyUnitField extends UnitField {
     @Override
     public void parse(String field, Data data) {
       data.strUnit = append(data.strUnit, " ", field);
     }
   }
   
   private static final Pattern UNIT_PTN = Pattern.compile("\\{ *([A-Z0-9]+) *\\}");
   private class MyInfoField extends InfoField {
     @Override
     public void parse(String field, Data data) {
       
       // First pick out any unit designation in curly braces.
       // These are added to the unit field if they are not already present there
       int lastPt = 0;
       Matcher match = UNIT_PTN.matcher(field);
       while (match.find()) {
         String sUnit = match.group(1);
         lastPt = match.end();
         Pattern ptn = Pattern.compile("\\b" + sUnit + "\\b");
         if (!ptn.matcher(data.strUnit).find()) data.strUnit = append(data.strUnit, " ", sUnit);
       }
       
       // Ignore everything up to the last unit
       field = field.substring(lastPt).trim();
       
       // ANything else is information
       data.strSupp = append(data.strSupp, "\n", field);
     }
   }
   
   
   
   @Override
   public Field getField(String name) {
     if (name.equals("IDDATETIME")) return new IdDateTimeField();
     if (name.equals("CALL")) return new MyCallField();
     if (name.equals("ADDR")) return new MyAddressField();
     if (name.equals("UNIT")) return new MyUnitField();
     if (name.equals("INFO")) return new MyInfoField();
     return super.getField(name);
   }
 }
