 package net.anei.cadpage.parsers.MD;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 import net.anei.cadpage.parsers.MD.MDMontgomeryCountyParser;
 
 import org.junit.Test;
 
 public class MDMontgomeryCountyParserTest extends BaseParserTest {
 
   public MDMontgomeryCountyParserTest() {
     setParser(new MDMontgomeryCountyParser(), "MONTGOMERY", "MD");
   }
   
   @Test
   public void testParser() {
     
     doTest("T1",
     		"MCo / [mCAD] * D * 2301 * BUILDING FIRE * 1620 E JEFFERSON ST ,RO * E723 E703 E721 E726 E705B AT723 AT703 RS703 A723 BC703 BC704 D3 D5 DFRS INV CEALRM CODE BCNOT",
     		"BOX:2301",
     		"CALL:BUILDING FIRE",
     		"ADDR:1620 E JEFFERSON ST",
     		"UNIT:E723 E703 E721 E726 E705B AT723 AT703 RS703 A723 BC703 BC704"
     		);
     doTest("T2",
     		"MCo / [mCAD] * D * 0302 * BUILDING FIRE * 733 MONROE ST ,RO * E723 E733 E726 E725 E721 AT703 AT723 RS741B M723 BC703 BC704 D3 D5 DFRS INV CEALRM CODE BCNOT",
     		"BOX:0302",
     		"CALL:BUILDING FIRE",
     		"ADDR:733 MONROE ST",
     		"UNIT:E723 E733 E726 E725 E721 AT703 AT723 RS741B M723 BC703 BC704"
     		);
     doTest("T3",
     		"MCo / [mCAD] * D * 0301 * HOUSE FIRE * 1006 DE BECK DR ,RO * E703 E723 E721 E725 E733 AT703 T731 RS703 A703 BC703 BC704",
     		"BOX:0301",
     		"CALL:HOUSE FIRE",
     		"ADDR:1006 DE BECK DR",
     		"UNIT:E703 E723 E721 E725 E733 AT703 T731 RS703 A703 BC703 BC704"
     		);
     doTest("T4",
     		"MCo / [mCAD] * D * 2312 * UNCONFIRMED POWDER * 5700 BOU AVE ,MCG (TARGET STORE) * E723 AT723 A723 M703 HM707 E720",
     		"BOX:2312",
     		"CALL:UNCONFIRMED POWDER",
     		"ADDR:5700 BOU AVE",
     		"UNIT:E723 AT723 A723 M703 HM707 E720",
     		"PLACE:TARGET STORE"
     		);
     doTest("T5",
     		"MCo / [mCAD] * D * 0801 * BOX ALARM * 17211 KING JAMES WAY ,MCG * E728 E731 E703 T731 RS703 A708C BC705 BC703",
     		"BOX:0801",
     		"CALL:BOX ALARM",
     		"ADDR:17211 KING JAMES WAY",
     		"UNIT:E728 E731 E703 T731 RS703 A708C BC705 BC703"
     		);
     doTest("T6",
         "CAD MSG: * D * 3132 * BOX ALARM * 209 FOUNTAIN GREEN LA ,GA  * E708 E722 E729 E734 E728 T731 AT708 RS703 M73M",
         "BOX:3132",
         "CALL:BOX ALARM",
         "ADDR:209 FOUNTAIN GREEN LA",
         "UNIT:E708 E722 E729 E734 E728 T731 AT708 RS703");
 
     doTest("T7",
         "CAD MSG: * D * 3525 * PIC w/VEH OVERTURNED-ALS1 * S270C 121-27 @ 17000 S270 X18 TO X16 HWY ,MCG  * M735 T7",
         "BOX:3525",
         "CALL:PIC w/VEH OVERTURNED-ALS1",
         "ADDR:S270C 121-27 @ 17000 S270 X18 TO X16 HWY",
         "MADDR:S270C 121 @ 17000 S270 X18 TO X16 HWY",
         "UNIT:M735");
 
     doTest("T8",
         "(MCo) [mCAD] * D * 4009 * PIC w/ ENTRAPMENT -ALS1 * 4700 BROOM DR / 18400 WICKHAM RD ,MCG * A740 E740 M704 RS704 D4 EMS EMSNOT BCNOT DFRS",
         "BOX:4009",
         "CALL:PIC w/ ENTRAPMENT -ALS1",
         "ADDR:4700 BROOM DR & 18400 WICKHAM RD",
         "MADDR:4700 BROOM DR",
         "UNIT:A740 E740 M704 RS704");
 
     doTest("T9",
         "FRM:General Alerts\nMSG:CAD MSG: * D *  * MAF FULL * 14615 PHILIP DR * AT715 BCNOT DFRS=",
         "CALL:MAF FULL",
         "ADDR:14615 PHILIP DR",
         "UNIT:AT715");
 
     doTest("T10",
         "CAD MSG: * D * 0609 * RSC - RESET ALARM * 4908 AUBURN AVE ,MCG  * RS741B ECC1",
         "BOX:0609",
         "CALL:RSC - RESET ALARM",
         "ADDR:4908 AUBURN AVE",
         "UNIT:RS741B");
 
    doTest("T11",
        "CAD MSG: * D * 1704 * ABDOMINAL PAIN * 7500 MATTINGLY LA ,MCG  * A717 ECC30",
        "BOX:1704",
        "CALL:ABDOMINAL PAIN",
        "ADDR:7500 MATTINGLY LN",
        "UNIT:A717");

   }
   
   public static void main(String[] args) {
    new MDMontgomeryCountyParserTest().generateTests("T1", "BOX CALL ADDR UNIT PLACE");
   }
   
 }
