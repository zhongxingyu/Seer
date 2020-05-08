 package net.anei.cadpage.parsers.NC;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 import net.anei.cadpage.parsers.NC.NCDavidsonCountyParser;
 
 import org.junit.Test;
 
 
 public class NCDavidsonCountyParserTest extends BaseParserTest {
   
   public NCDavidsonCountyParserTest() {
     setParser(new NCDavidsonCountyParser(), "DAVIDSON COUNTY", "NC");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "CAD:50PD-MOTOR VEH/PD ONLY;3248 FRIENDSHIP LEDFORD RD;2010024643;SADDLECHASE LN;MOTSINGER RD;OIL AND FLUIDS ON ROADWAY [12/17/10 11:05:24 BHUGHES] UDTS: ",
         "CALL:50PD-MOTOR VEH/PD ONLY",
         "ADDR:3248 FRIENDSHIP LEDFORD RD",
         "ID:2010024643",
         "X:SADDLECHASE LN & MOTSINGER RD",
         "INFO:OIL AND FLUIDS ON ROADWAY / UDTS:",
         "DATE:12/17/10",
         "TIME:11:05:24");
 
     doTest("T2",
         "CAD:P29-TRAFFIC ACCIDENT-BRAVO;I 85 S/MM 99;2010024707;[Medical Priority Info] PROBLEM: traffic accident # PATS: 1 AGE: Unknown Range SEX: Unknown CONSCIOU",
         "CALL:P29-TRAFFIC ACCIDENT-BRAVO",
         "ADDR:I 85 S & MM 99",
         "ID:2010024707",
         "INFO:traffic accident # PATS: 1 AGE: Unknown Range SEX: Unknown CONSCIOU");
 
     doTest("T3",
         "CAD:HOUSE/SINGLE DWELLING;3637 BECKS CHURCH RD;2010024363;HUCKLE PL;NC HWY 47;[Fire Priority Info] PROBLEM: KITCHEN FIRE [12/14/10 18:25:04 BPOOLE] [Fire P",
         "CALL:HOUSE/SINGLE DWELLING",
         "ADDR:3637 BECKS CHURCH RD",
         "ID:2010024363",
         "X:HUCKLE PL & NC HWY 47",
         "INFO:KITCHEN FIRE",
         "DATE:12/14/10",
         "TIME:18:25:04");
 
     doTest("T4",
         "CAD:P26-SICK-DELTA;4970 REID RD;2010024356;EASTVIEW DR;[Medical Priority Info] PROBLEM: SICK # PATS: 1 AGE: 78 Years SEX: Female CONSCIOUS: Yes BREATHING:",
         "CALL:P26-SICK-DELTA",
         "ADDR:4970 REID RD",
         "ID:2010024356",
         "X:EASTVIEW DR",
         "INFO:SICK # PATS: 1 AGE: 78 Years SEX: Female CONSCIOUS: Yes BREATHING:");
 
     doTest("T5",
         "CAD:Co Fire Tac3 for call;I 85 S/MM 99; THA;7703351053",
         "CALL:Co Fire Tac3 for call",
         "ADDR:I 85 S & MM 99",
         "ID:7703351053");
 
     doTest("T6",
         "CAD:cancel further response;I 85 S/MM 99; THA;7703351053",
         "CALL:cancel further response",
         "ADDR:I 85 S & MM 99",
         "ID:7703351053");
 
     doTest("T7",
         "CAD:P29-TRAFFIC ACCIDENT-BRAVO;I 85 S/MM 96;2010025333;unk on inj, near mm97 [12/26/10 09:57:18 CHUFF] JUST NORTH OF THE 96....UKN INJ",
         "CALL:P29-TRAFFIC ACCIDENT-BRAVO",
         "ADDR:I 85 S & MM 96",
         "ID:2010025333",
         "INFO:unk on inj, near mm97 / JUST NORTH OF THE 96....UKN INJ",
         "DATE:12/26/10",
         "TIME:09:57:18");
   }
   
   @Test
   public void testParser2() {
 
     doTest("T1",
         "CAD:SMOKE INVESTIGATION;JOHNSONTOWN RD/TAFT HEDRICK RD;2010025211;no need for dispatch [12/25/2010 14:19:26 ASTILL]",
         "CALL:SMOKE INVESTIGATION",
         "ADDR:JOHNSONTOWN RD & TAFT HEDRICK RD",
         "ID:2010025211",
         "INFO:no need for dispatch",
         "DATE:12/25/2010",
         "TIME:14:19:26");
 
     doTest("T2",
         "CAD:P29-TRAFFIC ACCIDENT-BRAVO;I 85 N/MM 98;2010025251;[Medical Priority Info] PROBLEM: 10-50 # PATS: 255 AGE: Unknown Range SEX: Unkn",
         "CALL:P29-TRAFFIC ACCIDENT-BRAVO",
         "ADDR:I 85 N & MM 98",
         "ID:2010025251",
         "INFO:10-50 # PATS: 255 AGE: Unknown Range SEX: Unkn");
 
     doTest("T3",
         "CAD:CANCEL;2006 JOHNSONTOWN RD; THA",
         "CALL:CANCEL",
         "ADDR:2006 JOHNSONTOWN RD");
 
     doTest("T4",
         "CAD:Co Fire TAC4 for call;I 85 S/MM 96; LEX;4075477164",
         "CALL:Co Fire TAC4 for call",
         "ADDR:I 85 S & MM 96",
         "ID:4075477164");
 
     doTest("T5",
         "CAD:Co Fire Tac3 for call;3136 MOCK RD; HP",
         "CALL:Co Fire Tac3 for call",
         "ADDR:3136 MOCK RD");
 
     doTest("T6",
         "  / CAD:Fire Under Control;N COUNTY HOME RD/BRIAR PATCH DR; LEX;3473996166\n",
         "CALL:Fire Under Control",
         "ADDR:N COUNTY HOME RD & BRIAR PATCH DR",
         "ID:3473996166");
 
     doTest("T7",
         "  / CAD:Co Fire Tac5 for call;I 85 S/MM 96; LEX;3367062145",
         "CALL:Co Fire Tac5 for call",
         "ADDR:I 85 S & MM 96",
         "ID:3367062145");
 
     doTest("T8",
         "TREE DOWN/FIRE;LICK CREEK CHURCH RD/NC HWY 8;2011016908;big tree plum across the road [08/20/11 08:28:05 JEVERHART] 2048 en route [08/20/11 08:33:44 JEVERH",
         "CALL:TREE DOWN/FIRE",
         "ADDR:LICK CREEK CHURCH RD & NC HWY 8",
         "MADDR:LICK CREEK CHURCH RD & NC 8",
         "ID:2011016908",
         "INFO:big tree plum across the road / 2048 en route",
         "DATE:08/20/11",
         "TIME:08:28:05");
 
     doTest("T9",
         "BCAD:P10-CHEST PAINS-CHARLIE;1125 WEAVER RD;2011018228;FROG POND DR;MEADOWVIEW RD;[Medical Priority Info] PROBLEM: CHEST PAIN 76YOM # PATS: 1 AGE: 76 SEX: M",
         "CALL:BCAD:P10-CHEST PAINS-CHARLIE",
         "ADDR:1125 WEAVER RD",
         "ID:2011018228",
         "X:FROG POND DR & MEADOWVIEW RD",
         "INFO:CHEST PAIN 76YOM # PATS: 1 AGE: 76 SEX: M");
 
     doTest("T10",
         "CAD:P6-BREATHING PROBLEMS-DELTA;2570 RIVERVIEW RD;2011020042;BLUE WATER PT;RIVERHOUSE RD;[Medical Priority Info] PROBLEM: DIFF BREATHING # PATS: 1 AGE: 72",
         "CALL:P6-BREATHING PROBLEMS-DELTA",
         "ADDR:2570 RIVERVIEW RD",
         "ID:2011020042",
         "INFO:BLUE WATER PT / RIVERHOUSE RD / DIFF BREATHING # PATS: 1 AGE: 72");
 }
   
   public static void main(String[] args) {
    new NCDavidsonCountyParserTest().generateTests("T1");
   }
 }
 
 
 
 
 /*
 
 CAD:Co Fire Tac3 for call;I 85 S/MM 99; THA;7703351053
 CAD:cancel further response;I 85 S/MM 99; THA;7703351053
 CAD:CANCEL;2006 JOHNSONTOWN RD; THA
 CAD:Co Fire TAC4 for call;I 85 S/MM 96; LEX;4075477164
 */
