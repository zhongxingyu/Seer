 package net.anei.cadpage.parsers.TX;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 import net.anei.cadpage.parsers.TX.TXCyCreekCommCenterParser;
 
 import org.junit.Test;
 
 
 public class TXCyCreekCommCenterParserTest extends BaseParserTest {
   
   public TXCyCreekCommCenterParserTest() {
     setParser(new TXCyCreekCommCenterParser(), "HARRIS COUNTY", "TX");
   }
   
   @Test
   public void testParser() {
     
     doTest("T1",
         "1of2:09/06 11:56 W HILLSIDE DR/EASTEX FRWY, ; Map:414D Sub: Nat:MA-MUTUAL AID / ASSIST AGENCY Units:E91 T81 T73 E-L19 X-St:EASTEX",
         "ADDR:W HILLSIDE DR & EASTEX FRWY",
         "MAP:414D",
         "CALL:MA-MUTUAL AID / ASSIST AGENCY",
         "UNIT:E91 T81 T73 E-L19",
         "X:EASTEX",
         "CITY:HOUSTON");
 
     doTest("T2",
         "1of2:09/06 11:28 19707 WOOD WALK LN, ; Map:337U Sub:PINEHURST OF ATASCOCITA Nat:09E01-ARREST - NOT BREATHING Units:E-M19 E-M29 E-7900",
         "ADDR:19707 WOOD WALK LN",
         "MAP:337U",
         "PLACE:PINEHURST OF ATASCOCITA",
         "CALL:09E01-ARREST - NOT BREATHING",
         "UNIT:E-M19 E-M29 E-7900");
 
     doTest("T3",
         "1of2:09/05 08:56 19226 AQUATIC DR, ; Map:378A Sub:WALDEN ON LAKE HOUSTON Nat:52B01G-FIRE ALARM - RESIDENTIAL Units:ATFD E-E39 X-",
         "ADDR:19226 AQUATIC DR",
         "MAP:378A",
         "PLACE:WALDEN ON LAKE HOUSTON",
         "CALL:52B01G-FIRE ALARM - RESIDENTIAL",
         "UNIT:ATFD E-E39");
     
     doTest("T4",
         "1of2:09/04 19:45 17219 KOBUK VALLEY CIR, ; Map:377E Sub:EAGLE SPRINGS Nat:67B03U-OUTSIDE FIRE - INVESTIGA Units:E-E39 X-St:*** Dead",
         "ADDR:17219 KOBUK VALLEY CIR",
         "MAP:377E",
         "PLACE:EAGLE SPRINGS",
         "CALL:67B03U-OUTSIDE FIRE - INVESTIGA",
         "UNIT:E-E39",
         "X:*** Dead");
     
     doTest("T5",
         "1of2:09/03 08:14 LILES LN/WOODLAND HILLS DR, ; Map:376H Sub:ATASCOCITA FOREST Nat:29-MOTOR VEHICLE INCIDENT Units:E-M19 E-E39 X-",
         "ADDR:LILES LN & WOODLAND HILLS DR",
         "MAP:376H",
         "PLACE:ATASCOCITA FOREST",
         "CALL:29-MOTOR VEHICLE INCIDENT",
         "UNIT:E-M19 E-E39");
 
     doTest("T6",
         "11/15 11:28 19506 SWEETGUM FOREST DR, ; Map:337V Sub:PINEHURST OF ATASCOCITA Nat:28B01U-STROKE/CVA - UNKNOWN Units:E-E39 X-St:TWELFTH FAIRWAY *** Dead",
         "ADDR:19506 SWEETGUM FOREST DR",
         "MAP:337V",
         "PLACE:PINEHURST OF ATASCOCITA",
         "CALL:28B01U-STROKE/CVA - UNKNOWN",
         "UNIT:E-E39",
         "X:TWELFTH FAIRWAY *** Dead");
     
     doTest("T7",
         "11/13 13:12 6918 ATASCA CREEK DR, ; Map:377C Sub:ATASCA WOODS Nat:52B01G-FIRE ALARM - RESIDENTIAL Units:ATFD E-E39 X-St:LEENS LODGE LN FOUNTAIN L",
         "ADDR:6918 ATASCA CREEK DR",
         "MAP:377C",
         "PLACE:ATASCA WOODS",
         "CALL:52B01G-FIRE ALARM - RESIDENTIAL",
         "UNIT:ATFD E-E39",
         "X:LEENS LODGE LN FOUNTAIN L");
     
     doTest("T8",
         "11/13 02:18 E FM 1960/DINERO DR, ; Map:337Z Sub: Nat:29D02M-MVI - PEDESTRIAN Units:E-M19 E-E39 E-B39 X-St:DINERO DR E FM 1960",
         "ADDR:E FM 1960 & DINERO DR",
         "MAP:337Z",
         "CALL:29D02M-MVI - PEDESTRIAN",
         "UNIT:E-M19 E-E39 E-B39",
         "X:DINERO DR E FM 1960");
 
     doTest("T9",
         "11/12 18:24 Repage: 8922 PINE SHORES DR, ; Map:338S Sub:ATASCOCITA SHORES Nat:60B02-GAS LEAK - UNKNOWN Units:E-E19 X-St:SHOREVIEW LN SUNCOVE LN",
         "ADDR:8922 PINE SHORES DR",
         "MAP:338S",
         "PLACE:ATASCOCITA SHORES",
         "CALL:60B02-GAS LEAK - UNKNOWN",
         "UNIT:E-E19",
         "X:SHOREVIEW LN SUNCOVE LN");
     
     doTest("T10",
         "11/12 18:08 Repage: ATASCOCITA RD/TIMBER FOREST D, ; Map:377A Sub: Nat:29-MOTOR VEHICLE INCIDENT Units:HUM2 E-E29 X-St:TIMBER FOREST DR ATASCOCI",
         "ADDR:ATASCOCITA RD & TIMBER FOREST D",
         "MAP:377A",
         "CALL:29-MOTOR VEHICLE INCIDENT",
         "UNIT:HUM2 E-E29",
         "X:TIMBER FOREST DR ATASCOCI");
     
     doTest("T11",
         "11/12 17:31 E FM 1960/POSSUM PARK RD, ; Map:336T Sub: Nat:68C01-HEAVY SMOKE OUTSIDE Units:E-E39 E-B39 X-St:POSSUM PARK RD E FM 1960",
         "ADDR:E FM 1960 & POSSUM PARK RD",
         "MAP:336T",
         "CALL:68C01-HEAVY SMOKE OUTSIDE",
         "UNIT:E-E39 E-B39",
         "X:POSSUM PARK RD E FM 1960");
     
     doTest("T12",
         " / 11/19 17:00 OAK HOLLOW DR-HC/GRANT RD-HC, ; Map:369E Nat:67B01U-SMALL OUTSIDE FIRE Units:E21 E26 B22 X-St:GRANT RD WILLOW LN",
         "ADDR:OAK HOLLOW DR & GRANT RD",
         "MAP:369E",
         "CALL:67B01U-SMALL OUTSIDE FIRE",
         "UNIT:E21 E26 B22",
         "X:GRANT RD WILLOW LN");
     
     doTest("T13",
         "12/29 11:17 <tel:177575> N SAM HOUSTON PKWY W EB, SAM HOUSTON RACE PARK; FL 1 Map:370T- Nat:71B01-VEHICLE FIRE Units:E24 NWE43 B23 X-St:FAIRBANKS N HOUSTON RD N",
         "PHONE:177575",
         "ADDR:N SAM HOUSTON PKWY W EB",
         "PLACE:SAM HOUSTON RACE PARK",
         "APT:FL 1",
         "MAP:370T-",
         "CALL:71B01-VEHICLE FIRE",
         "UNIT:E24 NWE43 B23",
         "X:FAIRBANKS N HOUSTON RD N");
     
     doTest("T14",
         "02/16 FM 2920-HC/RHODES RD-HC, ; Map:291N- Sub: Nat:29-MOTOR VEHICLE INCIDENT Units:M510 FO290 X-St:RHODES RD MARCIN DR 201107335",
         "ADDR:FM 2920 & RHODES RD",
         "MAP:291N-",
         "CALL:29-MOTOR VEHICLE INCIDENT",
         "UNIT:M510 FO290",
        "X:RHODES RD MARCIN DR");
 
     doTest("T15",
         "03/19 09:48 CROSBY TRAINING, ; Map:- Sub: Nat:HM3-HC HAZMAT TRAINING Units:HM1 X-St: 201112139",
         "ADDR:CROSBY TRAINING",
         "MAP:-",
         "CALL:HM3-HC HAZMAT TRAINING",
        "UNIT:HM1");
 
     doTest("T16",
         "  06/08 12:42 8314 CINNAMON RUN DR-HC, ; Map:250J- Sub: Nat:67-OUTSIDE FIRE Units:WDE107 E76 X-St:S ROLLING OAKS DR KUYKEND 201125394 TXT STOP to",
         "ADDR:8314 CINNAMON RUN DR",
         "MAP:250J-",
         "CALL:67-OUTSIDE FIRE",
         "UNIT:WDE107 E76",
        "X:S ROLLING OAKS DR KUYKEND 201125394 TXT STOP to");
 
     doTest("T17",
         "  06/08 16:04 12110 SARTI ST-HC, ; Map:371A- Sub:FOUNTAINHEAD Nat:31D02-UNCON - EFFECTIVE BREATHI Units:HCM93 D52 11M X-St:BROOKHEAD TRL 201125435 TXT STOP to opt-out",
         "ADDR:12110 SARTI ST",
         "PLACE:FOUNTAINHEAD",
         "MAP:371A-",
         "CALL:31D02-UNCON - EFFECTIVE BREATHI",
         "UNIT:HCM93 D52 11M",
        "X:BROOKHEAD TRL 201125435 TXT STOP to opt-out");
 
     doTest("T18",
         "09/05 15:19 18300 ELLA BLVD-HC, ; Map:331G- Sub:CYPRESSWOOD Nat:67D03-LARGE OUTSIDE FIRE Units:E62 B78 E61 B76 E37 X-St:BLUE CYPRESS DR",
         "ADDR:18300 ELLA BLVD",
         "PLACE:CYPRESSWOOD",
         "MAP:331G-",
         "CALL:67D03-LARGE OUTSIDE FIRE",
         "UNIT:E62 B78 E61 B76 E37",
         "X:BLUE CYPRESS DR");
 
     doTest("T19",
         "09/11 23:25 22539 SH 249 SB-HC, MATTRESS GIANT; Map:329N- Sub: Nat:69-LIGHT STRUCTURE INCIDENT Units:E23 E21 E33 E24 E36 TW23 L36 X-St:LAKEWOOD CROSSING DR",
         "ADDR:22539 SH 249 SB",
         "PLACE:MATTRESS GIANT",
         "MAP:329N-",
         "CALL:69-LIGHT STRUCTURE INCIDENT",
         "UNIT:E23 E21 E33 E24 E36 TW23 L36",
         "X:LAKEWOOD CROSSING DR");
 
   }
   
   public static void main(String[] args) {
     new TXCyCreekCommCenterParserTest().generateTests("T19", "PHONE ADDR PLACE APT MAP CALL UNIT X");
   }
 }
