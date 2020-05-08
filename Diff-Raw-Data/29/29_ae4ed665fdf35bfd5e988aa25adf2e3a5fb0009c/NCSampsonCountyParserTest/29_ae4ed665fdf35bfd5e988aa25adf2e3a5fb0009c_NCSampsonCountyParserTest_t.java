 package net.anei.cadpage.parsers.NC;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class NCSampsonCountyParserTest extends BaseParserTest {
   
   public NCSampsonCountyParserTest() {
     setParser(new NCSampsonCountyParser(), "SAMPSON COUNTY", "NC");
   }
   
   @Test
   public void testParser() {
 
     doTest("T1",
         "EMS:2012-013061* 2280 HOBBTON HWY* * * CLINTON* WEDGEWOOD LN* BUMPY LN* K12* * ABDOMINAL* ABDOMINAL PAIN* CSRS,EMS79* 1328A* Medical: No* Hazards:  ",
         "ID:2012-013061",
         "ADDR:2280 HOBBTON HWY",
         "CITY:CLINTON",
         "X:WEDGEWOOD LN & BUMPY LN",
         "MAP:K12",
         "INFO:ABDOMINAL PAIN / 1328A / Medical: No / Hazards:",
         "CALL:ABDOMINAL",
         "UNIT:CSRS,EMS79");
 
     doTest("T2",
         "EMS:2012-013062* 219 W CARTER ST* * * CLINTON* BARDEN ST* BUNTING ST* L11* * BREATH DIFF* BREATHING DIFFICULTIES* CSRS,EMS75* 1379* Medical: No* Hazards: No* 02/2",
         "ID:2012-013062",
         "ADDR:219 W CARTER ST",
         "CITY:CLINTON",
         "X:BARDEN ST & BUNTING ST",
         "MAP:L11",
         "INFO:BREATHING DIFFICULTIES / 1379 / Medical: No / Hazards: No / 02/2",
         "CALL:BREATH DIFF",
         "UNIT:CSRS,EMS75");
 
     doTest("T3",
         "EMS:2012-013047* I40 MM 343* * * NEWTON GROVE* MCLAMB RD UNDERPASS* EXIT 343 HOBBTON HWY* D11* * MVA I* MOTOR VEHICLE ACCIDENT WITH INJURIES* EMS79* 1263* Medical",
         "ID:2012-013047",
         "ADDR:I40 MM 343",
         "MADDR:I 40 MM 343 & MCLAMB RD UNDERPASS",
         "CITY:NEWTON GROVE",
         "X:MCLAMB RD UNDERPASS & EXIT 343 HOBBTON HWY",
         "MAP:D11",
         "INFO:MOTOR VEHICLE ACCIDENT WITH INJURIES / 1263 / Medical",
         "CALL:MVA I",
         "UNIT:EMS79");
 
     doTest("T4",
         "EMS:2012-013001* 91 N CHURCH AVE* * * GARLAND* W THIRD ST* W FOURTH ST* T10* * CHEST PAIN* CHEST PAIN* EMS77* 1376* Medical: No* Hazards: No* *",
         "ID:2012-013001",
         "ADDR:91 N CHURCH AVE",
         "CITY:GARLAND",
         "X:W THIRD ST & W FOURTH ST",
         "MAP:T10",
         "INFO:CHEST PAIN / 1376 / Medical: No / Hazards: No",
         "CALL:CHEST PAIN",
         "UNIT:EMS77");
 
     doTest("T5",
         "EMS:2012-012994* 303 MARTIN LUTHER KING JR BLVD* * * CLINTON* WEEKS ST* SOUTHEAST BLVD* L12* * UNCONSCIOUS* UNCONSCIOUS/FAINTING* CSRS,EMS77* 1380* Medical: No* H",
         "ID:2012-012994",
         "ADDR:303 MARTIN LUTHER KING JR BLVD",
         "CITY:CLINTON",
         "X:WEEKS ST & SOUTHEAST BLVD",
         "MAP:L12",
         "INFO:UNCONSCIOUS/FAINTING / 1380 / Medical: No / H",
         "CALL:UNCONSCIOUS",
         "UNIT:CSRS,EMS77");
 
     doTest("T6",
         "EMS:2012-012960* 120 SOUTHWOOD DR* 305A* * CLINTON* SOUTH WEST BLVD* * M12* Landmark Comment: UPDATED 01/07* BREATH DIFF* BREATHING DIFFICULTIES* CSRS,EMS75* 1380",
         "ID:2012-012960",
         "ADDR:120 SOUTHWOOD DR",
         "APT:305A",
         "CITY:CLINTON",
         "X:SOUTH WEST BLVD",
         "MAP:M12",
         "INFO:Landmark Comment: UPDATED 01/07 / BREATHING DIFFICULTIES / 1380",
         "CALL:BREATH DIFF",
         "UNIT:CSRS,EMS75");
 
     doTest("T7",
         "EMS:2012-012906* 229 e MORISEY BLVD* * * CLINTON* LISBON ST* DEVANE ST* L12* * BURNS* BURNS* CSRS,EMS75* 1275* Medical: No* Hazards: No* *",
         "ID:2012-012906",
         "ADDR:229 e MORISEY BLVD",
         "CITY:CLINTON",
         "X:LISBON ST & DEVANE ST",
         "MAP:L12",
         "INFO:BURNS / 1275 / Medical: No / Hazards: No",
         "CALL:BURNS",
         "UNIT:CSRS,EMS75");
 
     doTest("T8",
         "EMS:2012-012904* 134 W SECOND ST* * * GARLAND* S CHURCH AVE* BROOKS AVE* T10* * SICK CALLS* SICK CALLS* 7112* 1376* Medical: No* Hazards: No* *",
         "ID:2012-012904",
         "ADDR:134 W SECOND ST",
         "CITY:GARLAND",
         "X:S CHURCH AVE & BROOKS AVE",
         "MAP:T10",
         "INFO:SICK CALLS / 1376 / Medical: No / Hazards: No",
         "CALL:SICK CALLS",
         "UNIT:7112");
 
     doTest("T9",
         "EMS:2012-012890* 2100 REEDSFORD RD* * * CLINTON* MELVABROOK DR* BILLY LN* M13,M14* * MVA I* MOTOR VEHICLE ACCIDENT WITH INJURIES* CSRS,EMS76,STA14* 1330* Medical: Comment: TRACT 10 OVERDOSE* OVERDOSE* CSRS,EMS71* 1284A* Medical: No* Hazards: No* *",
         "ID:2012-012890",
         "ADDR:2100 REEDSFORD RD",
         "CITY:CLINTON",
         "X:MELVABROOK DR & BILLY LN",
         "MAP:M13,M14",
         "INFO:MOTOR VEHICLE ACCIDENT WITH INJURIES / 1330 / Medical: Comment: TRACT 10 OVERDOSE / OVERDOSE / CSRS,EMS71 / 1284A / Medical: No / Hazards: No",
         "CALL:MVA I",
         "UNIT:CSRS,EMS76,STA14");
 
     doTest("T10",
        "EMS:2012-012887* 249 KAYLA LN* * * CLINTON* KING RD* KING RD* I14* Geo Comment: TRACT *10* OVERDOSE* OVERDOSE* CSRS,EMS71* 1284A* Medical: No* Hazards: No* *",
         "ID:2012-012887",
         "ADDR:249 KAYLA LN",
         "CITY:CLINTON",
         "X:KING RD & KING RD",
         "MAP:I14",
        "INFO:Geo Comment: TRACT *10 / OVERDOSE / 1284A / Medical: No / Hazards: No",
         "CALL:OVERDOSE",
        "UNIT:CSRS,EMS71");
 
     doTest("T11",
         "EMS:2012-012877* 233 EFFIE PETERSON LN* * * ROSEBORO* PORTER RD* DEAD END* M9* * STRUCTURE FI* STRUCTURE FIRE* CSRS,EMS76* 1336* Medical: No* Hazards: No* *",
         "ID:2012-012877",
         "ADDR:233 EFFIE PETERSON LN",
         "CITY:ROSEBORO",
         "X:PORTER RD & DEAD END",
         "MAP:M9",
         "INFO:STRUCTURE FIRE / 1336 / Medical: No / Hazards: No",
         "CALL:STRUCTURE FI",
         "UNIT:CSRS,EMS76");
 
     doTest("T12",
         "EMS:2012-012839* 4056 REEDSFORD RD* * * CLINTON* HUCKLEBERRY LN* STEVE HARRIS LN* N14,N15* * CHEST PAIN* CHEST PAIN* CSRS,EMS71* 1347A* Medical: No* Hazards: No*",
         "ID:2012-012839",
         "ADDR:4056 REEDSFORD RD",
         "CITY:CLINTON",
         "X:HUCKLEBERRY LN & STEVE HARRIS LN",
         "MAP:N14,N15",
         "INFO:CHEST PAIN / 1347A / Medical: No / Hazards: No",
         "CALL:CHEST PAIN",
         "UNIT:CSRS,EMS71");
 
     doTest("T13",
        "EMS:2012-012887* 249 KAYLA LN* * * CLINTON* KING RD* KING RD* I14* Geo Comment: TRACT *10* OVERDOSE* OVERDOSE* CSRS,EMS71* 1284A* Medical: No* Hazards: No* *",
         "ID:2012-012887",
         "ADDR:249 KAYLA LN",
         "CITY:CLINTON",
         "X:KING RD & KING RD",
         "MAP:I14",
        "INFO:Geo Comment: TRACT *10 / OVERDOSE / 1284A / Medical: No / Hazards: No",
         "CALL:OVERDOSE",
        "UNIT:CSRS,EMS71");
 
     doTest("T14",
         "EMS:2012-012838* 1498 HAWLEY RD* * OR* DUNN* FRED TEW RD* UNION GROVE CH RD* C4* * MVA I* MOTOR VEHICLE ACCIDENT WITH INJURIES* EMS72,PVRS,Z951RT* 1254C* Medical:",
         "ID:2012-012838",
         "ADDR:1498 HAWLEY RD",
         "CH:OR",
         "CITY:DUNN",
         "X:FRED TEW RD & UNION GROVE CH RD",
         "MAP:C4",
         "INFO:MOTOR VEHICLE ACCIDENT WITH INJURIES / 1254C / Medical:",
         "CALL:MVA I",
         "UNIT:EMS72,PVRS,Z951RT");
 
     doTest("T15",
         "EMS:2012-012833* 253 PINE OAK LN* * * DUNN* MIDWAY ELEM SCH RD* DEAD END* E6* * SICK CALLS* SICK CALLS* EMS78* 1255A* Medical: No* Hazards: No* *",
         "ID:2012-012833",
         "ADDR:253 PINE OAK LN",
         "CITY:DUNN",
         "X:MIDWAY ELEM SCH RD & DEAD END",
         "MAP:E6",
         "INFO:SICK CALLS / 1255A / Medical: No / Hazards: No",
         "CALL:SICK CALLS",
         "UNIT:EMS78");
 
     doTest("T16",
         "EMS:2012-012830* 203 N MAIN ST* * * SALEMBURG* CLINTON ST* CHURCH ST* K6* * BREATH DIFF* BREATHING DIFFICULTIES* EMS76* 1378* Medical: No* Hazards: No* 02/26/2012",
         "ID:2012-012830",
         "ADDR:203 N MAIN ST",
         "CITY:SALEMBURG",
         "X:CLINTON ST & CHURCH ST",
         "MAP:K6",
         "INFO:BREATHING DIFFICULTIES / 1378 / Medical: No / Hazards: No / 02/26/2012",
         "CALL:BREATH DIFF",
         "UNIT:EMS76");
 
     doTest("T17",
         "EMS:2012-012826* 939 SOUTHWEST BLVD* * TAC3* CLINTON* MARTIN LUTH KING BLV* * L12* * STRUCTURE FI* STRUCTURE FIRE* CSRS,EMS72,STA14,STA16,STA8* 1380* Medical: No*",
         "ID:2012-012826",
         "ADDR:939 SOUTHWEST BLVD",
         "CH:TAC3",
         "CITY:CLINTON",
         "X:MARTIN LUTH KING BLV",
         "MAP:L12",
         "INFO:STRUCTURE FIRE / 1380 / Medical: No",
         "CALL:STRUCTURE FI",
         "UNIT:CSRS,EMS72,STA14,STA16,STA8");
 
     doTest("T18",
         "EMS:2012-012816* 301 MAIN ST* * * NEWTON GROVE* E CIRCLE ST* N CHURCH ST* C11* Landmark Comment: UPDATED 2-02* BREATH DIFF* BREATHING DIFFICULTIES* EMS71,EMS72,NG",
         "ID:2012-012816",
         "ADDR:301 MAIN ST",
         "CITY:NEWTON GROVE",
         "X:E CIRCLE ST & N CHURCH ST",
         "MAP:C11",
         "INFO:Landmark Comment: UPDATED 2-02 / BREATHING DIFFICULTIES",
         "CALL:BREATH DIFF",
         "UNIT:EMS71,EMS72,NG");

    doTest("T19",
        "EMS:2012-017694* 79 ROMAY MCKOY LN* * * ROSE HILL* BILL TOWN RD* LOOPS BACK TO ITSELF* S14* Geo Comment: *24* ABDOMINAL* ABDOMINAL PAIN* EMS77* 1352A* Medical: No",
        "ID:2012-017694",
        "ADDR:79 ROMAY MCKOY LN",
        "CITY:ROSE HILL",
        "X:BILL TOWN RD & LOOPS BACK TO ITSELF",
        "MAP:S14",
        "INFO:Geo Comment: *24 / ABDOMINAL PAIN / 1352A / Medical: No",
        "CALL:ABDOMINAL",
        "UNIT:EMS77");
   }
 
   public static void main(String[] args) {
     new NCSampsonCountyParserTest().generateTests("T1");
   }
 }
