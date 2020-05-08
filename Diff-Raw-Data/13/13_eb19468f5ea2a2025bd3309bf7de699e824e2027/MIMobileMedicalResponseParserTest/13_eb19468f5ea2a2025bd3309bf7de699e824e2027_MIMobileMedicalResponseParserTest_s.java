 package net.anei.cadpage.parsers.MI;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class MIMobileMedicalResponseParserTest extends BaseParserTest {
   
   public MIMobileMedicalResponseParserTest() {
     setParser(new MIMobileMedicalResponseParser(), "", "MI");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "Subject:<CAD> - part 1 of 1\nRC:Run# 17656/11775 N ISABELLA RD/P-2 Emergency No Lights//Chest Pain (Non-Traumatic)/herrick and pere marquette",
         "ID:17656",
         "ADDR:11775 N ISABELLA RD",
         "CALL:P-2 Emergency No Lights",
         "INFO:Chest Pain (Non-Traumatic) / herrick and pere marquette");
 
     doTest("T2",
         "Subject:<CAD> - part 1 of 1\nRC:Run# 17612/7800 LUDINGTON DR/P-3 Downgrade  (No Lts/Sirens)//Falls/x gibson and lake station ave on",
         "ID:17612",
         "ADDR:7800 LUDINGTON DR",
         "CALL:P-3 Downgrade  (No Lts/Sirens)",
         "INFO:Falls",
         "X:gibson and lake station ave on");
 
     doTest("T3",
         "Subject:<CAD> - part 1 of 1\nRC:Run# 16888/7181 N CLARE/P-1 Life Threatening Emergency//Cardiac or Respiratory Arrest/Death/x forres",
         "ID:16888",
         "ADDR:7181 N CLARE",
         "CALL:P-1 Life Threatening Emergency",
         "INFO:Cardiac or Respiratory Arrest / Death",
         "X:forres");
 
     doTest("T4",
         "Subject:<CAD> - part 1 of 1\nRC:Run# 17723/600 SE 4TH ST/P-1 Life Threatening Emergency/nurses station/Sick Person (Specific Diagnos",
         "ID:17723",
         "ADDR:600 SE 4TH ST",
         "CALL:P-1 Life Threatening Emergency",
         "INFO:nurses station / Sick Person (Specific Diagnos");
 
     doTest("T5",
         "Subject:<CAD> - part 1 of 1\nRC:Run# 17923/10197 S CLARE AVE/P-2 Emergency No Lights//Chest Pain (Non-Traumatic)/dept corrections bu",
         "ID:17923",
         "ADDR:10197 S CLARE AVE",
         "CALL:P-2 Emergency No Lights",
         "INFO:Chest Pain (Non-Traumatic) / dept corrections bu");
 
     doTest("T6",
         "Subject:<CAD> - part 1 of 1\nRC:Run# 17874/521 S SCOTT DR/P-1 Life Threatening Emergency//Breathing Problems/was in ER Saturday/ dx",
         "ID:17874",
         "ADDR:521 S SCOTT DR",
         "CALL:P-1 Life Threatening Emergency",
         "INFO:Breathing Problems / was in ER Saturday / dx");
 
     doTest("T7",
         "Subject:<CAD> - part 1 of 1\nRC:Run# 17991/391 MILL ST/P-2 Emergency No Lights/D/Sick Person (Specific Diagnosis)/pt had knee replac",
         "ID:17991",
         "ADDR:391 MILL ST",
         "CALL:P-2 Emergency No Lights",
         "APT:D",
         "INFO:Sick Person (Specific Diagnosis) / pt had knee replac");
 
     doTest("T8",
         "Subject:<CAD> - part 1 of 1\nRC:Run# 17893/235 GATEWAY DR/P-1 Life Threatening Emergency/105/Breathing Problems//ALS/CLARE///<Unknow",
         "ID:17893",
         "ADDR:235 GATEWAY DR",
         "CALL:P-1 Life Threatening Emergency",
         "APT:105",
         "INFO:Breathing Problems / ALS / CLARE / <Unknow");
 
     doTest("T9",
         "Subject:<CAD> - part 1 of 1\nRC:Run# 17970/602 BEECH ST/P-5 Local Transport/2ND FLOOR 205-A MED SURG/Int-Sick Person-Specify in Comm",
         "ID:17970",
         "ADDR:602 BEECH ST",
         "CALL:P-5 Local Transport",
         "INFO:2ND FLOOR 205-A MED SURG / Int-Sick Person-Specify in Comm");
 
     doTest("T10",
         "prvs=10834168ba=mailghost@mobilemedical.org (<CAD> - part 1 of 1) RC:Run# 24453/5186 FENTON/P-3 Downgrade  (No Lts/Sirens)//Overdose / Poisoning (Ingestion)/mundy twp // at kroger",
         "ID:24453",
         "ADDR:5186 FENTON",
         "CALL:P-3 Downgrade  (No Lts/Sirens)",
         "INFO:Overdose / Poisoning (Ingestion) / mundy twp / at kroger");
 
     doTest("T11",
         "<CAD> - part 1 of 1/ RC:Run# 34376/3300 PAULAN DR/P-1 Life Threatening Emergency//Chest Pain (Non-Traumatic)/72/F/ALS/BAY CITY///<Unknown>/20:04\n\n",
         "ID:34376",
         "ADDR:3300 PAULAN DR",
         "CALL:P-1 Life Threatening Emergency",
         "INFO:Chest Pain (Non-Traumatic) / 72 / F / ALS / BAY CITY / <Unknown> / 20:04");
 
     doTest("T12",
         "<CAD> - part 1 of 1/ RC:Run# 34848/3375 BOY SCOUT RD/P-1 Life Threatening Emergency//Convulsions/Seizures/x Sherwood & Euclid // 35 F/ALS/BAY CITY///<Unkno",
         "ID:34848",
         "ADDR:3375 BOY SCOUT RD",
         "CALL:P-1 Life Threatening Emergency",
         "INFO:Convulsions / Seizures / 35 F / ALS / BAY CITY / <Unkno",
         "X:Sherwood & Euclid");
 
     doTest("T13",
         "<CAD> - part 1 of 1/ Run# 0/ 800 S WASHINGTON AVE/ P-7 Long Distance/ 1/ Int-Overdose/Poisoning (Ingestion)/ EXTRAS: NONE... DECONDITIONING.../PT READY / PCS RE",
         "ID:0",
         "ADDR:800 S WASHINGTON AVE",
         "CALL:P-7 Long Distance",
         "APT:1",
         "INFO:Int-Overdose / Poisoning (Ingestion) / EXTRAS: NONE... DECONDITIONING... / PT READY / PCS RE");
 
     doTest("T14",
         "(<CAD> - part 1 of 1) Run# 55683/ 508 N MONROE ST/ P-3 Downgrade (No Lts/Sirens)/ Sick Person (Specific Diagnosis)/ 19/f/ ALS/ BAY CITY/ 1900 COLUMBUS A",
         "ID:55683",
         "ADDR:508 N MONROE ST",
         "CALL:P-3 Downgrade (No Lts/Sirens)",
         "INFO:Sick Person (Specific Diagnosis) / 19 / f / ALS / BAY CITY / 1900 COLUMBUS A");
}
   
   public static void main(String[] args) {
    new MIMobileMedicalResponseParserTest().generateTests("T14");
   }
 }
