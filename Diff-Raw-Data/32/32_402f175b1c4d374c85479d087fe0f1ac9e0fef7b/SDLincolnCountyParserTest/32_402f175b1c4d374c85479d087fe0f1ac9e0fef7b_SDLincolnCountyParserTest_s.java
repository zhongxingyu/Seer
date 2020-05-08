 package net.anei.cadpage.parsers.SD;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class SDLincolnCountyParserTest extends BaseParserTest {
   
   public SDLincolnCountyParserTest() {
     setParser(new SDLincolnCountyParser(), "LINCOLN COUNTY", "SD");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "Subject:12-10952 - SIG1 : Injury Accident\nI-29 MM 64 \r\nPlease respond immediately. Roll over 6pts. I-29 nb south of the 64mm.\r",
         "ID:12-10952",
         "CALL:SIG1 : Injury Accident",
         "ADDR:I-29 MM 64",
         "MADDR:I 29 MM 64",
         "INFO:Roll over 6pts. I-29 nb south of the 64mm.");
 
     doTest("T2",
         "Subject:12-10680 -\n48548 480TH AVE, Canton \r\nPlease respond immediately. Diabetic emergency\r",
         "ID:12-10680",
         "CALL:ALERT",
         "ADDR:48548 480TH AVE",
         "CITY:Canton",
         "INFO:Diabetic emergency");
 
     doTest("T3",
         "Subject:AMB : Ambulance Call\n307 E Jefferson St # 7, Inwood eld male who has fallen\r",
         "CALL:AMB : Ambulance Call",
         "ADDR:307 E Jefferson St",
         "APT:7",
         "CITY:Inwood",
         "ST:IA",
         "INFO:eld male who has fallen");
 
     doTest("T4",
         "Subject:12-10682 -\n903 W 5th. st #3, ca \r\nPlease respond immediately. 60 yo. f fallen and hit head.\r",
         "ID:12-10682",
         "CALL:ALERT",
         "ADDR:903 W 5th st",
         "APT:3",
         "CITY:CANTON",
         "INFO:60 yo. f fallen and hit head.");
 
     doTest("T5",
         "Subject:AMB : Ambulance Call\nI-29 NB at 59 MM59 MM I-29 27 YOM with severe back pain\r",
         "CALL:AMB : Ambulance Call",
         "ADDR:I-29 NB",
         "MADDR:I 29",
         "INFO:at 59 MM59 MM I-29 27 YOM with severe back pain");
 
     doTest("T6",
         "Subject:AMB : Ambulance Call\n501 S MAPLE ST, Inwood female had a baby 1 week ago passing clots\r",
         "CALL:AMB : Ambulance Call",
         "ADDR:501 S MAPLE ST",
         "CITY:Inwood",
         "ST:IA",
         "INFO:female had a baby 1 week ago passing clots");
 
     doTest("T7",
         "Subject:AMB : Ambulance Call\n1022 N DAKOTA ST, CANTON, SD Non-ER transfer to Avera ER for behavioral evaluation\r",
         "CALL:AMB : Ambulance Call",
         "ADDR:1022 N DAKOTA ST",
         "CITY:CANTON",
         "ST:SD",
         "INFO:Non-ER transfer to Avera ER for behavioral evaluation");
 
     doTest("T8",
         "Subject:Mutual Aid for Field and Hay Baler Fire\n48259 289th St\r\nPlease respond immediately.\r",
         "CALL:Mutual Aid for Field and Hay Baler Fire",
         "ADDR:48259 289th St");
     
   }
   
   public static void main(String[] args) {
    new SDLincolnCountyParserTest().generateTests("T1", "ID CALL ADDR APT CITY ST INFO");
   }
 }
