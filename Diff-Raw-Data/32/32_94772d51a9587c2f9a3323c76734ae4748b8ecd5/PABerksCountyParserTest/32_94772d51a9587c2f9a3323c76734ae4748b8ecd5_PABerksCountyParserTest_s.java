 package net.anei.cadpage.parsers.PA;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 import net.anei.cadpage.parsers.PA.PABerksCountyParser;
 
 import org.junit.Test;
 
 
 public class PABerksCountyParserTest extends BaseParserTest {
   
   public PABerksCountyParserTest() {
     setParser(new PABerksCountyParser(), "BERKS COUNTY", "PA");
   }
   
   @Test
   public void testParser() {
     doTest("T1",
         "CAD MSG: *D MVAUNK   E STATE ST / S KEMP ST 0082 2 VEH / COMP IS ANDREW BROOKS / INVOLVED // AT THE INTERSECTION B",
         "CALL:MVAUNK - Accident unknown inj",
         "ADDR:E STATE ST & S KEMP ST",
         "CITY:LYONS",
         "INFO:2 VEH / COMP IS ANDREW BROOKS / INVOLVED / AT THE INTERSECTION B");
 
     doTest("T2",
         "CAD MSG: *D MVAWITH  102 BEAVER CREEK RD 0087 1 VEH / SPUN OUT / MALE OCC (COMP'S HUSBAND) BELIEVES HE FRACTURED H",
         "CALL:MVAWITH - Accident w/ injury",
         "ADDR:102 BEAVER CREEK RD",
         "CITY:ROCKLAND TWP",
         "INFO:1 VEH / SPUN OUT / MALE OCC (COMP'S HUSBAND) BELIEVES HE FRACTURED H");
     
     doTest("T3",
         "CAD MSG: *D FTEST    STATION 35 @110 PARK AV 0082 BERKS TESING THE TEXT PAGING SYSTEM NO ACTION REQUIRED",
         "CALL:FTEST",
         "PLACE:STATION 35",
         "ADDR:110 PARK AV",
         "MADDR:110 PARK AVE",
         "CITY:LYONS",
         "INFO:BERKS TESING THE TEXT PAGING SYSTEM NO ACTION REQUIRED");
     
     doTest("T4",
         "CAD MSG: *D MVAUNK   NOBLE ST / BASTIAN RD 0079 ONE CAR, OCCUPIED, INTO A FIELD // ONLY LANDMARK SHE COULD MEN",
         "CALL:MVAUNK - Accident unknown inj",
         "ADDR:NOBLE ST & BASTIAN RD",
         "CITY:MAXATAWNY TWP",
         "INFO:ONE CAR, OCCUPIED, INTO A FIELD / ONLY LANDMARK SHE COULD MEN");
     
     doTest("T5",
         "CAD MSG: *D SF       28 NOBLE ST 0081 SEES FLAMES IN BASEMENT //ADVISED TO EVAC /",
         "CALL:SF - Structure Fire",
         "ADDR:28 NOBLE ST",
         "CITY:KUTZTOWN",
         "INFO:SEES FLAMES IN BASEMENT /ADVISED TO EVAC /");
     
     doTest("T6",
         "CAD MSG: *D MVAENT   FORGEDALE RD / DAVIDS DR 0087 2VEHS HEAD ON/2 VICTIMS ENTRAPPED /1 FEMALE SERIOUSLY INJ/BLEED",
         "CALL:MVAENT - Accident w/ entrapment",
         "ADDR:FORGEDALE RD & DAVIDS DR",
         "CITY:ROCKLAND TWP",
         "INFO:2VEHS HEAD ON/2 VICTIMS ENTRAPPED /1 FEMALE SERIOUSLY INJ/BLEED");
     
     doTest("T7",
         "Subject:1/2\nCAD MSG: *D FTELE    STATION 35 @110 PARK AV 0082 CALL REF ITEMS SHE HAS THAT ARE BELEIVED TO BELONG TO YOUR COMPA",
         "CALL:FTELE",
         "PLACE:STATION 35",
         "ADDR:110 PARK AV",
         "MADDR:110 PARK AVE",
         "CITY:LYONS",
         "INFO:CALL REF ITEMS SHE HAS THAT ARE BELEIVED TO BELONG TO YOUR COMPA");
 
     doTest("T8",
         "CAD MSG: *D ALSMED   106 MAIN ST 0063 56 YOF THROAT FEELS LIKE IS SWELLING CLOSED /BREAKING OUT IN RASH/RESP",
         "CALL:ALSMED",
         "ADDR:106 MAIN ST",
         "CITY:STRAUSSTOWN",
         "INFO:56 YOF THROAT FEELS LIKE IS SWELLING CLOSED /BREAKING OUT IN RASH/RESP");
 
     doTest("T9",
         "CAD MSG: *D MVAWITH  I78 / MP 39.6 EB 0078 MC DRIVER DOWN IN THE ROADWAY / NOT MOVING / 2 TT PULLED OFF TO THE SI",
         "CALL:MVAWITH - Accident w/ injury",
         "ADDR:I-78 & MP 39.6 EB",
         "MADDR:I 78 & MP 39.6",
         "CITY:GREENWICH TWP",
         "INFO:MC DRIVER DOWN IN THE ROADWAY / NOT MOVING / 2 TT PULLED OFF TO THE SI");
 
     doTest("T10",
         "CAD MSG: *D BLSTRAUM 101 DOE MOUNTAIN LN @BEAR CREEK SKI AREA 0086   14YOM/BROKEN LEFT WRIST/CON AND ALERT/PT WILL BE IN THE SKI PATROLE RO",
         "CALL:BLSTRAUM",
         "ADDR:101 DOE MOUNTAIN LN",
         "PLACE:BEAR CREEK SKI AREA",
         "CITY:LONGSWAMP TWP",
         "INFO:14YOM/BROKEN LEFT WRIST/CON AND ALERT/PT WILL BE IN THE SKI PATROLE RO");
 
     doTest("T11",
         "FRM: \nMSG:CAD MSG: *D SF       65B MEADOW GLEN LN 0034 HEAVY SMOKE, GETTING WORSE; POSSIBLY COMING FROM DOWNSTAIRS APT; HUNGU",
         "CALL:SF - Structure Fire",
         "ADDR:65B MEADOW GLEN LN",
         "CITY:CUMRU TWP",
         "INFO:HEAVY SMOKE, GETTING WORSE; POSSIBLY COMING FROM DOWNSTAIRS APT; HUNGU");
 
     doTest("T12",
         "CAD MSG: *D SF       DIRECTLINK @2561 BERNVILLE RD 0054 PENSKI ENTERANCE / BUILDING 1 TRANSFORMER ROOM / SMOKE IN THE BUILDING",
         "CALL:SF - Structure Fire",
         "ADDR:2561 BERNVILLE RD",
         "PLACE:DIRECTLINK",
         "CITY:BERN TWP",
         "INFO:PENSKI ENTERANCE / BUILDING 1 TRANSFORMER ROOM / SMOKE IN THE BUILDING");
 
     doTest("T13",
         "CAD MSG: *D AFA      RHMC C BUILDING @600 SPRUCE ST 0038 BLDG C/GENERAL FIRE/ATN/610-988-8222",
         "CALL:AFA - Fire Alarm",
         "ADDR:600 SPRUCE ST",
         "PLACE:RHMC C BUILDING",
         "CITY:WEST READING",
         "INFO:BLDG C/GENERAL FIRE/ATN/610-988-8222");
 
     doTest("T14",
         "CAD MSG: *D ALSMED   459 MEMORIAL HWY 0091 F IN SEIZURES / 1YOF /",
         "CALL:ALSMED",
         "ADDR:459 MEMORIAL HWY",
         "CITY:RUSCOMBMANOR TWP",
         "INFO:F IN SEIZURES / 1YOF /");
 
     doTest("T15",
         "FRM: \nMSG:CAD MSG: *D AFA      1501 ELIZABETH AV @THOMS TAVERN 0067 KEYPAD FIRE ALM ZONE 95 BUS # 484-794-6038 / GRUMPY'S BARB",
         "CALL:AFA - Fire Alarm",
         "ADDR:1501 ELIZABETH AV",
         "MADDR:1501 ELIZABETH AVE",
         "PLACE:THOMS TAVERN",
         "CITY:LAURELDALE",
         "INFO:KEYPAD FIRE ALM ZONE 95 BUS # 484-794-6038 / GRUMPY'S BARB");
   }
   
   @Test
   public void testActive911() {
 
     doTest("T1",
         "(berks.co45@rsix.roamsecure.net) CAD MSG: *D MVAUNK   WILLOW RD / OAK LN 0091 1 VEH/ OFF THE ROAD INTO THE TREES/ ON WILLOW RD IN THE BEND RIGHT AFT\n\n" +
         "Sent by Berks County RSAN to CO45 All Call\n\n" +
         "--\n\n" +
         "You received this message because you registered on Alert Berks.  To change your alerting preferences go to http://berks.alertpa.org",
 
         "CALL:MVAUNK - Accident unknown inj",
         "ADDR:WILLOW RD & OAK LN",
         "CITY:RUSCOMBMANOR TWP",
         "INFO:1 VEH/ OFF THE ROAD INTO THE TREES/ ON WILLOW RD IN THE BEND RIGHT AFT");
 
     doTest("T2",
         "(berks.co45@rsix.roamsecure.net) CAD MSG: *D SF       50 MISTY LN 0084 HOUSE ON FIRE /COMP DEAN WINTERS/SMOKE COMING OUT OF WINDOWS/ COMP\n\n" +
         "Sent by Berks County RSAN to CO45 All Call\n\n" +
         "--\n\n" +
         "You received this message because you registered on Alert Berks.  To change your alerting preferences go to http://berks.alertpa.org",
 
         "CALL:SF - Structure Fire",
         "ADDR:50 MISTY LN",
         "CITY:RICHMOND TWP",
         "INFO:HOUSE ON FIRE /COMP DEAN WINTERS/SMOKE COMING OUT OF WINDOWS/ COMP");
 
     doTest("T3",
         "(berks.co45@rsix.roamsecure.net) CAD MSG: *D MVAWITH  81 FORGEDALE RD ;NEAR BICK RD 0087 VEH STRUCK SOMEKIND OF CONCRETE FIXTURE/SHE IS CHECKING ON INJURIES/PO\n\n" +
         "Sent by Berks County RSAN to CO45 All Call\n\n" +
         "--\n\n" +
         "You received this message because you registered on Alert Berks.  To change your alerting preferences go to http://berks.alertpa.org",
 
         "CALL:MVAWITH - Accident w/ injury",
         "ADDR:81 FORGEDALE RD",
         "X:BICK RD",
         "CITY:ROCKLAND TWP",
         "INFO:VEH STRUCK SOMEKIND OF CONCRETE FIXTURE/SHE IS CHECKING ON INJURIES/PO");
 
     doTest("T4",
         "[berks.575@rsix.roamsecure.net] CAD MSG: *D ALSMED   120 TREXLER AV @KUTZTOWN MANORAPT 221  0081 78YOM/PEG TUB CAME OUT/FLUIDS IN HIS LUNGS/TO RHMC/HX DEMENTIA ,DIABET\n" +
         "Sent by Berks County RSAN to Topton EMS All CALL\n" +
         "--\n" +
         "You received this message because you registered on Alert Berks.  To change your alerting preferences go to http://berks.alertpa.org\n",
 
         "CALL:ALSMED",
         "ADDR:120 TREXLER AV",
         "MADDR:120 TREXLER AVE",
         "APT:221",
         "PLACE:KUTZTOWN MANOR",
         "CITY:KUTZTOWN",
         "INFO:78YOM/PEG TUB CAME OUT/FLUIDS IN HIS LUNGS/TO RHMC/HX DEMENTIA ,DIABET");
 
     doTest("T5",
         "[berks.575@rsix.roamsecure.net] CAD MSG: *D ALSMED   403 HEILMAN HSE ; TOPTON LUTHERAN HOME 1 S HOME AVAPT 403  0086 83YOF/ COUGHING UP BLOOD / NORMAL BREATHING / BRIGHT RED/ TO RHMC/\n" +
         "Sent by Berks County RSAN to Topton EMS All CALL\n" +
         "--\n" +
         "You received this message because you registered on Alert Berks.  To change your alerting preferences go to http://berks.alertpa.org\n",
 
         "CALL:ALSMED",
         "PLACE:403 HEILMAN HSE - TOPTON LUTHERAN HOME",
         "ADDR:1 S HOME AV",
         "MADDR:1 S HOME AVE",
         "APT:403",
         "CITY:LONGSWAMP TWP",
         "INFO:83YOF/ COUGHING UP BLOOD / NORMAL BREATHING / BRIGHT RED/ TO RHMC/");
 
     doTest("T6",
         "[berks.575@rsix.roamsecure.net] CAD MSG: *D ALSMED   1 S HOME AV @TOPTON LUTHERAN HOMEAPT 137A  0086 A WING / ROTUNDA ENTRANCE / 83 YOM / ADEMA IN THE LEFT LEG / WEAKNESS\n" +
         "Sent by Berks County RSAN to Topton EMS All CALL\n" +
         "--\n" +
         "You received this message because you registered on Alert Berks.  To change your alerting preferences go to http://berks.alertpa.org\n",
 
         "CALL:ALSMED",
         "ADDR:1 S HOME AV",
         "MADDR:1 S HOME AVE",
         "APT:137A",
         "PLACE:TOPTON LUTHERAN HOME",
         "CITY:LONGSWAMP TWP",
         "INFO:A WING / ROTUNDA ENTRANCE / 83 YOM / ADEMA IN THE LEFT LEG / WEAKNESS");
 
     doTest("T7",
         "[berks.co85@rsix.roamsecure.net] CAD MSG: *D SF       721 DANIEL DRAPT 2  0066 FIRE IN BATHROOM\n" +
         "Sent by Berks County RSAN to CO85 All Call\n" +
         "--\n" +
         "You received this message because you registered on Alert Berks.  To change your alerting preferences go to http://berks.alertpa.org\n",
 
         "CALL:SF - Structure Fire",
         "ADDR:721 DANIEL DR",
         "APT:2",
         "CITY:MUHLENBERG TWP",
         "INFO:FIRE IN BATHROOM");
 
     doTest("T8",
         "[berks.co85@rsix.roamsecure.net] CAD MSG: *D SF       1007 MT LAUREL AV ;FLR 2APT 3  0066 ALARMS SOUNDING //SMOKE SHOWING //\n" +
         "Sent by Berks County RSAN to CO85 All Call\n" +
         "--\n" +
         "You received this message because you registered on Alert Berks.  To change your alerting preferences go to http://berks.alertpa.org\n",
 
         "CALL:SF - Structure Fire",
         "ADDR:1007 MT LAUREL AV",
         "MADDR:1007 MT LAUREL AVE",
         "APT:FLR 2 - APT 3",
         "CITY:MUHLENBERG TWP",
         "INFO:ALARMS SOUNDING /SMOKE SHOWING /");
 
     doTest("T9",
         "[berks.co85@rsix.roamsecure.net] CAD MSG: *D LIFTASST 1348 WEST WYOMISSING CTAPT P  0043 59 YOF /FELL BUT NOT INJ /COMP JUST NEEDS HELP GETTING HER UP /COMP\n" +
         "Sent by Berks County RSAN to CO85 All Call\n" +
         "--\n" +
         "You received this message because you registered on Alert Berks.  To change your alerting preferences go to http://berks.alertpa.org\n",
 
         "CALL:LIFTASST",
         "ADDR:1348 WEST WYOMISSING CT",
         "APT:P",
         "CITY:SPRING TWP",
         "INFO:59 YOF /FELL BUT NOT INJ /COMP JUST NEEDS HELP GETTING HER UP /COMP");
    
   }
   
   public static void main(String[] args) {
    new PABerksCountyParserTest().generateTests("T4", "CALL ADDR APT PLACE X CITY INFO");
   }
 }
