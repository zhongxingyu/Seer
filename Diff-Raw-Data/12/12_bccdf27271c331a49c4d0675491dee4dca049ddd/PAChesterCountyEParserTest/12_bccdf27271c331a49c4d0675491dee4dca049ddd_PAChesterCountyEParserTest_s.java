 package net.anei.cadpage.parsers.PA;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class PAChesterCountyEParserTest extends BaseParserTest {
   
   public PAChesterCountyEParserTest() {
     setParser(new PAChesterCountyEParser(), "CHESTER COUNTY", "PA");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "(Dispatch) 05/31/11 * 13:30:52 * SYNCOPE/FAINTING - ALS * * 880 W BALTIMORE PK ,58 * LEWIS RD & PUSEY MILL RD * PENN * WELLNESS CENTER OFFICE-SOUTHER * GRANDEL,",
         "DATE:05/31/11",
         "TIME:13:30:52",
         "CALL:SYNCOPE/FAINTING - ALS",
         "ADDR:880 W BALTIMORE PK",
         "MADDR:880 W BALTIMORE PIKE",
         "X:LEWIS RD & PUSEY MILL RD",
         "CITY:PENN TWP",
         "PLACE:WELLNESS CENTER OFFICE-SOUTHER",
         "NAME:GRANDEL,");
 
     doTest("T2",
         "(Dispatch) 05/31/11 * 11:10:31 * OTHER TYPE RESCUE * * 880 W BALTIMORE PK ,58 * LEWIS RD & PUSEY MILL RD * PENN * IN PLOT-YMCA *  * 910-750-762 * 2264 *  * 22 *",
         "DATE:05/31/11",
         "TIME:11:10:31",
         "CALL:OTHER TYPE RESCUE",
         "ADDR:880 W BALTIMORE PK",
         "MADDR:880 W BALTIMORE PIKE",
         "X:LEWIS RD & PUSEY MILL RD",
         "CITY:PENN TWP",
         "PLACE:IN PLOT-YMCA",
         "PHONE:910-750-762",
         "UNIT:2264",
         "SRC:22");
 
     doTest("T3",
         "(Dispatch) 05/31/11 * 09:30:53 * HYPO TENSION - ALS * * 774 W GLENVIEW DR ,58 * PHEASANT WY & EDGEWOOD CT * PENN * - *  * 610-345-136 * 2203 *  * 22 * Dispatch *",
         "DATE:05/31/11",
         "TIME:09:30:53",
         "CALL:HYPO TENSION - ALS",
         "ADDR:774 W GLENVIEW DR",
         "X:PHEASANT WY & EDGEWOOD CT",
         "CITY:PENN TWP",
         "PHONE:610-345-136",
         "UNIT:2203",
         "SRC:22");
 
     doTest("T4",
         "(Dispatch) 05/31/11 * 00:36:12 * BARN FIRE * * 248 HOOD RD ,48 * MUNICIPAL BOUNDARY & N MOSQUITO LA * WMARLB * - *  * 610-316-731 * 3604   * 36 * Dispatch *",
         "DATE:05/31/11",
         "TIME:00:36:12",
         "CALL:BARN FIRE",
         "ADDR:248 HOOD RD",
         "X:MUNICIPAL BOUNDARY & N MOSQUITO LA",
         "CITY:WEST MARLBOROUGH TWP",
         "PHONE:610-316-731",
         "UNIT:3604",
         "SRC:36");
 
     doTest("T5",
         "(Dispatch) 03/21/12 * 13:49:18 * MOBILE HOME/TRAILER FIRE * * 8 VIEW LA ,58 * WESTVIEW WY & DEAD END * PENN * - *  * 610-960-709 * 2204 *  * 22 * Dispatch *",
         "DATE:03/21/12",
         "TIME:13:49:18",
         "CALL:MOBILE HOME/TRAILER FIRE",
         "ADDR:8 VIEW LA",
         "MADDR:8 VIEW LN",
         "CITY:PENN TWP",
         "X:WESTVIEW WY & DEAD END",
         "PHONE:610-960-709",
         "UNIT:2204",
         "SRC:22");
 
   }
   
   public static void main(String[] args) {
     new PAChesterCountyEParserTest().generateTests("T1");
   }
 }
