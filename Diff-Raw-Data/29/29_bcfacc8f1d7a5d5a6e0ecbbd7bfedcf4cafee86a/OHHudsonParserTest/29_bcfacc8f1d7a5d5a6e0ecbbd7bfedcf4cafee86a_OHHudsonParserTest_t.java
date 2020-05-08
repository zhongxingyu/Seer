 package net.anei.cadpage.parsers.OH;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class OHHudsonParserTest extends BaseParserTest {
   
   public OHHudsonParserTest() {
     setParser(new OHHudsonParser(), "HUDSON", "OH");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "HudsonCAD:CAD System 6150,NICHOLSON DR.,,HUDSON EMS,EMS63 YOM, DIABETIC SHOCK",
         "ADDR:6150 NICHOLSON DR",
        "SRC:HUDSON EMS",
         "CALL:EMS",
         "INFO:63 YOM / DIABETIC SHOCK");
 
     doTest("T2",
         "HudsonCAD:CAD System 0,ANYWHERE,OTHER LOCATION OUT OF CITY,HudsonEMS,EMSMUTAL AID TO TWINSBURG FOR WORKING HOUSE FIRE, 2416 WHITE MARSH",
         "ADDR:2416 WHITE MARSH",
         "PLACE:OTHER LOCATION OUT OF CITY",
        "SRC:HudsonEMS",
         "CALL:EMS",
         "INFO:MUTAL AID TO TWINSBURG FOR WORKING HOUSE FIRE",
         "CITY:TWINSBURG");
 
     doTest("T3",
         "HudsonCAD:CAD System 6150,NICHOLSON DR.,,HUDSON EMS,EMS",
         "ADDR:6150 NICHOLSON DR",
        "SRC:HUDSON EMS",
         "CALL:EMS");
 
     doTest("T4",
         "HudsonCAD:CAD System 75,EXECUTIVE PKWY.,ALLSTATE INSURANCE (75 EXEC),HUDSONEMS,EMSfemale diff breathing allergic reaction first floor front",
         "ADDR:75 EXECUTIVE PKWY",
         "PLACE:ALLSTATE INSURANCE (75 EXEC)",
        "SRC:HUDSONEMS",
         "CALL:EMS",
         "INFO:female diff breathing allergic reaction first floor front");
 
     doTest("T5",
         "HudsonCAD:CAD System 0,W STREETSBORO ST.,91 & 303,HUDSONEMS,EMStest do not respond test only",
         "ADDR:W STREETSBORO ST",
         "MADDR:91 & 303,W STREETSBORO ST",
         "PLACE:91 & 303",
        "SRC:HUDSONEMS",
         "CALL:EMS",
         "INFO:test do not respond test only");
 
     doTest("T6",
         "HudsonCAD:CAD System 5555,DARROW RD.,JOANN STORES INC.,HUDSONEMS,EMSSHORTNESS OF BREATH MALE N GATE SECURITY OFFICE",
         "ADDR:5555 DARROW RD",
         "PLACE:JOANN STORES INC.",
        "SRC:HUDSONEMS",
         "CALL:EMS",
         "INFO:SHORTNESS OF BREATH MALE N GATE SECURITY OFFICE");
  
     doTest("T7",
         "HudsonCAD:CAD System 200,LAUREL LAKE DR.,,EMS,EMSGREENWOOD 303 LUMBAR PAIN",
         "ADDR:200 LAUREL LAKE DR",
        "SRC:EMS",
         "CALL:EMS",
         "INFO:GREENWOOD 303 LUMBAR PAIN");
 
     doTest("T8",
         "HudsonCAD:CAD System 1430,WINSLOW DR.,,HUDSON EMS,EMSCHEST PAINS",
         "ADDR:1430 WINSLOW DR",
        "SRC:HUDSON EMS",
         "CALL:EMS",
         "INFO:CHEST PAINS");

    doTest("T9",
        "HudsonCAD:CAD System 200,LAUREL LAKE DR.,LAUREL LAKE RETIREMENT COMMUNITY,HUDSONF376,Fire Alarm: , Public Buildings 0600 to MidnightCAL COM CYTHIA 800 498 7565 COMMONS FIRE ALARM",
        "ADDR:200 LAUREL LAKE DR",
        "PLACE:LAUREL LAKE RETIREMENT COMMUNITY",
        "SRC:HUDSONF376",
        "INFO:Fire Alarm: / Public Buildings 0600 to MidnightCAL COM CYTHIA 800 498 7565 COMMONS FIRE ALARM");

    doTest("T10",
        "HudsonCAD:CAD System 7421,MARBLEHEAD DR.,,HudsonF374,Fire Alarm: ResidentialSMOKE HEAT FIRE ALARM PER JULIA WITH DIEBOLD 18005484478",
        "ADDR:7421 MARBLEHEAD DR",
        "SRC:HudsonF374",
        "INFO:Fire Alarm: ResidentialSMOKE HEAT FIRE ALARM PER JULIA WITH DIEBOLD 18005484478");
   }
   
   public static void main(String[] args) {
     new OHHudsonParserTest().generateTests("T9");
   }
 }
