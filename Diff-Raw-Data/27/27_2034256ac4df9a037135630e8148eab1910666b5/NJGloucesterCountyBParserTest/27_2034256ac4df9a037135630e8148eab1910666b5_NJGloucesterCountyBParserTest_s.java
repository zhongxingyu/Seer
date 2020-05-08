 package net.anei.cadpage.parsers.NJ;
 
 import net.anei.cadpage.parsers.BaseParserTest;
 
 import org.junit.Test;
 
 
 public class NJGloucesterCountyBParserTest extends BaseParserTest {
   
   public NJGloucesterCountyBParserTest() {
     setParser(new NJGloucesterCountyBParser(), "GLOUCESTER COUNTY", "NJ");
   }
   
   @Test
   public void testRunReport() {
 
     doTest("T1",
         "[FW: Automatic R&R Notification] \n" +
         "-----Original Message-----\n" +
         "From: DDOAK@S105KD4M.CO.GLOUCESTER.NJ.US\n" +
         "[mailto:DDOAK@S105KD4M.CO.GLOUCESTER.NJ.US]\n" +
         "Sent: Tuesday, June 12, 2012 10:33 PM\n" +
         "To: EmergencyResponse\n" +
         "Subject: Automatic R&R Notification\n" +
         "Importance: High\n" +
         " \n" +
         "** ** ** ** ** ** ** ** ** ** ** ** FINAL REPORT ** ** ** ** ** ** ** **\n" +
         "** ** ** ** \n" +
         "Incident Number  : 2012-00020243                    ORI: 0818102\n" +
         "Station: 10-2 \n" +
         "Incident Type  . : ALMC F CO ALARM             Priority: 3 \n" +
         "Incident Location: 811 MARLBOROUGH ST\n" +
         "Venue: WASHINGTON\n" +
         "Call Time- 20:50:34                                Date- 06/12/2012 \n" +
         "Dispatch - 20:51:29     En-route- 20:56:10     On-scene- 20:59:16\n" +
         "Depart 1- \n" +
         "Arrive 2 -              Depart 2-              In-statn-\n" +
         "Cleared - 21:37:16\n" +
         "Area:   R2              Section : 1004         Beat  . :    R \n" +
         "Grid:  K21              Quadrant: 1017         District: 1017\n" +
         "Phone Number: (856) 481-4361                Call Source:  TEL\n" +
         "Caller. . . : PAUL FISHER\n" +
         "Nature of Call : 811 MARLBOROUGH ST\n" +
         "Additional Info\n" +
         "  CO ALARM REDING 210 REQ FD\n" +
         "Narrative\n" +
         " Information on the units assigned to the call follows. \n" +
         "    Unit: 10-2    Radio:           Ofcr 1:            Ofcr 2: \n" +
         "       DSP: 06/12/12  20:51:29     ENR: 06/12/12  20:56:10 \n" +
         "       ARV: 06/12/12  20:59:16     DPT:             :  : \n" +
         "       QTR:             :  :       CLR: 06/12/12  21:37:16 \n" +
         "    Unit: F1032   Radio:           Ofcr 1:     F1032  Ofcr 2: \n" +
         "       DSP: 06/12/12  20:54:43     ENR: 06/12/12  20:54:43 \n" +
         "       ARV:             :  :       DPT:             :  : \n" +
         "       QTR:             :  :       CLR: 06/12/12  20:57:12 \n" +
         "    Unit: E1094   Radio:           Ofcr 1:            Ofcr 2: \n" +
         "       DSP: 06/12/12  21:18:52     ENR: 06/12/12  21:19:51 \n" +
         "       ARV: 06/12/12  21:22:45     DPT:             :  : \n" +
         "       QTR:             :  :       CLR: 06/12/12  22:33:24 \n" +
         " CALLER ADVISED TO EXIT RES                        JJVERRECCH 20:50:18 \n" +
         " R 1022 W/3                                        JBLAWRENCE 20:56:27 \n" +
         " R 1021 W/3                                        JBLAWRENCE 20:57:38 \n" +
         " O 1021                                            JBLAWRENCE 20:59:23 \n" +
         " O 1022                                            JBLAWRENCE 21:03:13 \n" +
         " PROGRESS REPORT.............NEG READINGS W/       JBLAWRENCE 21:18:58 \n" +
         " METERS,REQUEST 1 BLS TO CHECK HOMEOWNER           JBLAWRENCE 21:19:22\n" +
         "The Call Taker is VERRECCHIO JOHN J\n" +
         "The Dispatcher is MCGARVEY RAYMOND E\n",
 
         "CALL:RUN REPORT",
         "PLACE:Importance: High\n \n** ** ** ** ** ** ** ** ** ** ** ** FINAL REPORT ** ** ** ** ** ** ** **\n** ** ** ** \nIncident Number  : 2012-00020243                    ORI: 0818102\nStation: 10-2 \nIncident Type  . : ALMC F CO ALARM             Priority: 3 \nIncident Location: 811 MARLBOROUGH ST\nVenue: WASHINGTON\nCall Time- 20:50:34                                Date- 06/12/2012 \nDispatch - 20:51:29     En-route- 20:56:10     On-scene- 20:59:16\nDepart 1- \nArrive 2 -              Depart 2-              In-statn-\nCleared - 21:37:16\nArea:   R2              Section : 1004         Beat  . :    R \nGrid:  K21              Quadrant: 1017         District: 1017\nPhone Number: (856) 481-4361                Call Source:  TEL\nCaller. . . : PAUL FISHER\nNature of Call : 811 MARLBOROUGH ST\nAdditional Info\n  CO ALARM REDING 210 REQ FD\nNarrative\n Information on the units assigned to the call follows. \n    Unit: 10-2    Radio:           Ofcr 1:            Ofcr 2: \n       DSP: 06/12/12  20:51:29     ENR: 06/12/12  20:56:10 \n       ARV: 06/12/12  20:59:16     DPT:             :  : \n       QTR:             :  :       CLR: 06/12/12  21:37:16 \n    Unit: F1032   Radio:           Ofcr 1:     F1032  Ofcr 2: \n       DSP: 06/12/12  20:54:43     ENR: 06/12/12  20:54:43 \n       ARV:             :  :       DPT:             :  : \n       QTR:             :  :       CLR: 06/12/12  20:57:12 \n    Unit: E1094   Radio:           Ofcr 1:            Ofcr 2: \n       DSP: 06/12/12  21:18:52     ENR: 06/12/12  21:19:51 \n       ARV: 06/12/12  21:22:45     DPT:             :  : \n       QTR:             :  :       CLR: 06/12/12  22:33:24 \n CALLER ADVISED TO EXIT RES                        JJVERRECCH 20:50:18 \n R 1022 W/3                                        JBLAWRENCE 20:56:27 \n R 1021 W/3                                        JBLAWRENCE 20:57:38 \n O 1021                                            JBLAWRENCE 20:59:23 \n O 1022                                            JBLAWRENCE 21:03:13 \n PROGRESS REPORT.............NEG READINGS W/       JBLAWRENCE 21:18:58 \n METERS,REQUEST 1 BLS TO CHECK HOMEOWNER           JBLAWRENCE 21:19:22\nThe Call Taker is VERRECCHIO JOHN J\nThe Dispatcher is MCGARVEY RAYMOND E");
 
     doTest("T2",
         "[FW: Automatic R&R Notification] \n" +
         "-----Original Message-----\n" +
         "From: DDOAK@S105KD4M.CO.GLOUCESTER.NJ.US\n" +
         "[mailto:DDOAK@S105KD4M.CO.GLOUCESTER.NJ.US]\n" +
         "Sent: Tuesday, June 12, 2012 10:33 PM\n" +
         "To: EmergencyResponse\n" +
         "Subject: Automatic R&R Notification\n" +
         "Importance: High\n" +
         " \n" +
         "** ** ** ** ** ** ** ** ** ** ** ** FINAL REPORT ** ** ** ** ** ** ** **\n" +
         "** ** ** ** \n" +
         "Incident Number  : 2012-00020243                    ORI: 0818103\n" +
         "Station: 10-3 \n" +
         "Incident Type  . : ALMC F CO ALARM             Priority: 3 \n" +
         "Incident Location: 811 MARLBOROUGH ST\n" +
         "Venue: WASHINGTON\n" +
         "Call Time- 20:50:34                                Date- 06/12/2012 \n" +
         "Dispatch - 20:54:43     En-route- 20:54:43     On-scene-\n" +
         "Depart 1- \n" +
         "Arrive 2 -              Depart 2-              In-statn-\n" +
         "Cleared - 20:57:12\n" +
         "Area:   R2              Section : 1004         Beat  . :    R \n" +
         "Grid:  K21              Quadrant: 1017         District: 1017\n" +
         "Phone Number: (856) 481-4361                Call Source:  TEL\n" +
         "Caller. . . : PAUL FISHER\n" +
         "Nature of Call : 811 MARLBOROUGH ST\n" +
         "Additional Info\n" +
         "  CO ALARM REDING 210 REQ FD\n" +
         "Narrative\n" +
         " Information on the units assigned to the call follows. \n" +
         "    Unit: 10-2    Radio:           Ofcr 1:            Ofcr 2: \n" +
         "       DSP: 06/12/12  20:51:29     ENR: 06/12/12  20:56:10 \n" +
         "       ARV: 06/12/12  20:59:16     DPT:             :  : \n" +
         "       QTR:             :  :       CLR: 06/12/12  21:37:16 \n" +
         "    Unit: F1032   Radio:           Ofcr 1:     F1032  Ofcr 2: \n" +
         "       DSP: 06/12/12  20:54:43     ENR: 06/12/12  20:54:43 \n" +
         "       ARV:             :  :       DPT:             :  : \n" +
         "       QTR:             :  :       CLR: 06/12/12  20:57:12 \n" +
         "    Unit: E1094   Radio:           Ofcr 1:            Ofcr 2: \n" +
         "       DSP: 06/12/12  21:18:52     ENR: 06/12/12  21:19:51 \n" +
         "       ARV: 06/12/12  21:22:45     DPT:             :  : \n" +
         "       QTR:             :  :       CLR: 06/12/12  22:33:24 \n" +
         " CALLER ADVISED TO EXIT RES                        JJVERRECCH 20:50:18 \n" +
         " R 1022 W/3                                        JBLAWRENCE 20:56:27 \n" +
         " R 1021 W/3                                        JBLAWRENCE 20:57:38 \n" +
         " O 1021                                            JBLAWRENCE 20:59:23 \n" +
         " O 1022                                            JBLAWRENCE 21:03:13 \n" +
         " PROGRESS REPORT.............NEG READINGS W/       JBLAWRENCE 21:18:58 \n" +
         " METERS,REQUEST 1 BLS TO CHECK HOMEOWNER           JBLAWRENCE 21:19:22\n" +
         "The Call Taker is VERRECCHIO JOHN J\n" +
         "The Dispatcher is MCGARVEY RAYMOND E\n",
 
         "CALL:RUN REPORT",
         "PLACE:Importance: High\n \n** ** ** ** ** ** ** ** ** ** ** ** FINAL REPORT ** ** ** ** ** ** ** **\n** ** ** ** \nIncident Number  : 2012-00020243                    ORI: 0818103\nStation: 10-3 \nIncident Type  . : ALMC F CO ALARM             Priority: 3 \nIncident Location: 811 MARLBOROUGH ST\nVenue: WASHINGTON\nCall Time- 20:50:34                                Date- 06/12/2012 \nDispatch - 20:54:43     En-route- 20:54:43     On-scene-\nDepart 1- \nArrive 2 -              Depart 2-              In-statn-\nCleared - 20:57:12\nArea:   R2              Section : 1004         Beat  . :    R \nGrid:  K21              Quadrant: 1017         District: 1017\nPhone Number: (856) 481-4361                Call Source:  TEL\nCaller. . . : PAUL FISHER\nNature of Call : 811 MARLBOROUGH ST\nAdditional Info\n  CO ALARM REDING 210 REQ FD\nNarrative\n Information on the units assigned to the call follows. \n    Unit: 10-2    Radio:           Ofcr 1:            Ofcr 2: \n       DSP: 06/12/12  20:51:29     ENR: 06/12/12  20:56:10 \n       ARV: 06/12/12  20:59:16     DPT:             :  : \n       QTR:             :  :       CLR: 06/12/12  21:37:16 \n    Unit: F1032   Radio:           Ofcr 1:     F1032  Ofcr 2: \n       DSP: 06/12/12  20:54:43     ENR: 06/12/12  20:54:43 \n       ARV:             :  :       DPT:             :  : \n       QTR:             :  :       CLR: 06/12/12  20:57:12 \n    Unit: E1094   Radio:           Ofcr 1:            Ofcr 2: \n       DSP: 06/12/12  21:18:52     ENR: 06/12/12  21:19:51 \n       ARV: 06/12/12  21:22:45     DPT:             :  : \n       QTR:             :  :       CLR: 06/12/12  22:33:24 \n CALLER ADVISED TO EXIT RES                        JJVERRECCH 20:50:18 \n R 1022 W/3                                        JBLAWRENCE 20:56:27 \n R 1021 W/3                                        JBLAWRENCE 20:57:38 \n O 1021                                            JBLAWRENCE 20:59:23 \n O 1022                                            JBLAWRENCE 21:03:13 \n PROGRESS REPORT.............NEG READINGS W/       JBLAWRENCE 21:18:58 \n METERS,REQUEST 1 BLS TO CHECK HOMEOWNER           JBLAWRENCE 21:19:22\nThe Call Taker is VERRECCHIO JOHN J\nThe Dispatcher is MCGARVEY RAYMOND E");
 
   }
   
   @Test
   public void testActive911() {
 
     doTest("T1",
         "[FW: Automatic R&R Notification] \n" +
         "-----Original Message-----\n" +
         "From: LEEPRIGH@S105KD4M.CO.GLOUCESTER.NJ.US\n" +
         "[mailto:LEEPRIGH@S105KD4M.CO.GLOUCESTER.NJ.US]\n" +
         "Sent: Wednesday, June 13, 2012 8:53 PM\n" +
         "To: EmergencyResponse\n" +
         "Subject: Automatic R&R Notification\n" +
         "Importance: High\n" +
         " \n" +
         "Incident Number  : 2012-00020364                    ORI: 0818102\n" +
         "Station: 10-2 \n" +
         "Incident Type  . : STRC F STRUCTURE            Priority: 2 \n" +
         "Incident Location: PITMAN DOWNER RD\n" +
         "Venue: WASHINGTON \n" +
         "  Cross Street . : PLUMTREE DR\n" +
         "Call Time- 20:51:04                                Date- 06/13/2012 \n" +
         "Dispatch -              En-route-              On-scene-\n" +
         "Depart 1- \n" +
         "Arrive 2 -              Depart 2-              In-statn-\n" +
         "Cleared -\n" +
         "Area:   R3              Section :    R         Beat  . :    R        Map\n" +
         ". . :    C \n" +
         "Grid:  M18              Quadrant: 1013         District: 1013\n" +
         "Phone Number: (215) 459-2823                Call Source:  911\n" +
         "Caller. . . : RONNIE\n" +
         "Nature of Call : PLUMTREE DR\n" +
         "Additional Info\n" +
         "  IAO PEACHTREE CONDOS\n" +
         "  SHED OR DETACHED GARAGE\n" +
         "Narrative \n" +
         " CALLER FROM 319 GEORGIA CT                        SWREAGAN   20:51:51\n" +
         "Additional Inc#s:\n" +
         "The Call Taker is REAGAN STEPHEN W\n" +
         "The Dispatcher is EPRIGHT LEWIS E\n",
 
         "ID:2012-00020364",
         "SRC:10-2",
         "CALL:STRC F STRUCTURE",
         "PRI:2",
         "ADDR:PITMAN DOWNER RD",
         "MADDR:PITMAN DOWNER RD & PLUMTREE DR",
         "CITY:WASHINGTON",
         "X:PLUMTREE DR",
         "TIME:20:51:04",
         "DATE:06/13/2012",
         "MAP:1013/1013/R/R3",
         "PHONE:(215) 459-2823",
         "NAME:RONNIE",
         "INFO:PLUMTREE DR");
 
     doTest("T2",
         "[FW: Automatic R&R Notification] \n" +
         "-----Original Message-----\n" +
         "From: LEEPRIGH@S105KD4M.CO.GLOUCESTER.NJ.US\n" +
         "[mailto:LEEPRIGH@S105KD4M.CO.GLOUCESTER.NJ.US]\n" +
         "Sent: Wednesday, June 13, 2012 8:53 PM\n" +
         "To: EmergencyResponse\n" +
         "Subject: Automatic R&R Notification\n" +
         "Importance: High\n" +
         " \n" +
         "Incident Number  : 2012-00020364                    ORI: 0818102\n" +
         "Station: 10-3 \n" +
         "Incident Type  . : STRC F STRUCTURE            Priority: 2 \n" +
         "Incident Location: PITMAN DOWNER RD\n" +
         "Venue: WASHINGTON \n" +
         "  Cross Street . : PLUMTREE DR\n" +
         "Call Time- 20:51:04                                Date- 06/13/2012 \n" +
         "Dispatch - 20:52:32     En-route-              On-scene-\n" +
         "Depart 1- \n" +
         "Arrive 2 -              Depart 2-              In-statn-\n" +
         "Cleared -\n" +
         "Area:   R3              Section :    R         Beat  . :    R        Map\n" +
         ". . :    C \n" +
         "Grid:  M18              Quadrant: 1013         District: 1013\n" +
         "Phone Number: (215) 459-2823                Call Source:  911\n" +
         "Caller. . . : RONNIE\n" +
         "Units sent  : \n" +
         " 0818102     10-2                 0818103     10-3\n" +
         "Nature of Call : PLUMTREE DR\n" +
         "Additional Info\n" +
         "  IAO PEACHTREE CONDOS\n" +
         "  SHED OR DETACHED GARAGE\n" +
         "Narrative \n" +
         " CALLER FROM 319 GEORGIA CT                        SWREAGAN   20:51:51\n" +
         "Additional Inc#s: \n" +
         " 0818103     201200020364\n" +
         "The Call Taker is REAGAN STEPHEN W\n" +
         "The Dispatcher is EPRIGHT LEWIS E\n",
 
         "ID:2012-00020364",
         "SRC:10-3",
         "CALL:STRC F STRUCTURE",
         "PRI:2",
         "ADDR:PITMAN DOWNER RD",
         "MADDR:PITMAN DOWNER RD & PLUMTREE DR",
         "CITY:WASHINGTON",
         "X:PLUMTREE DR",
         "TIME:20:51:04",
         "DATE:06/13/2012",
         "MAP:1013/1013/R/R3",
         "PHONE:(215) 459-2823",
         "NAME:RONNIE",
         "UNIT:0818102 10-2 0818103 10-3",
         "INFO:PLUMTREE DR");
 
     doTest("T3",
         "[FW: Automatic R&R Notification] \n" +
         "-----Original Message-----\n" +
         "From: KRLANSDO@S105KD4M.CO.GLOUCESTER.NJ.US\n" +
         "[mailto:KRLANSDO@S105KD4M.CO.GLOUCESTER.NJ.US]\n" +
         "Sent: Friday, June 15, 2012 10:54 AM\n" +
         "To: EmergencyResponse\n" +
         "Subject: Automatic R&R Notification\n" +
         "Importance: High\n" +
         " \n" +
         "Incident Number  : 2012-00020558                    ORI: 0818102\n" +
         "Station: 10-2 \n" +
         "Incident Type  . : SERV F SERV ASSN            Priority: 5 \n" +
         "Incident Location: TUCKAHOE RD\n" +
         "Venue: WASHINGTON \n" +
         "  Cross Street . : CROSSKEYS BYP\n" +
         "Call Time- 10:53:57                                Date- 06/15/2012 \n" +
         "Dispatch -              En-route-              On-scene-\n" +
         "Depart 1- \n" +
         "Arrive 2 -              Depart 2-              In-statn-\n" +
         "Cleared -\n" +
         "Area:   R2              Section : 1008         Beat  . :    R        Map\n" +
         ". . :    C \n" +
         "Grid:  M21              Quadrant: 1009         District: 1009\n" +
         "Phone Number: (000) 000-0005                Call Source:  RAD\n" +
         "Caller. . . : ENGINE 1022\n" +
         "Nature of Call : CROSSKEYS BYP\n" +
         "Additional Info\n" +
         "  DUCKS IN THE SEWER, CALLED INTO THE FIRE COMM\n" +
         "  OFFICE DIRECTLY/BY AMERICAS BEST\n" +
         "Additional Inc#s:\n" +
         "The Call Taker is LANSDOWNE KENNETH R\n" +
         "The Dispatcher is LANSDOWNE KENNETH R\n",
 
         "ID:2012-00020558",
         "SRC:10-2",
         "CALL:SERV F SERV ASSN",
         "PRI:5",
         "ADDR:TUCKAHOE RD",
         "MADDR:TUCKAHOE RD & CROSSKEYS BYP",
         "CITY:WASHINGTON",
         "X:CROSSKEYS BYP",
         "TIME:10:53:57",
         "DATE:06/15/2012",
         "MAP:1009/1009/1008/R2",
         "PHONE:(000) 000-0005",
         "NAME:ENGINE 1022",
         "INFO:CROSSKEYS BYP");
 
     doTest("T4",
         "[FW: Automatic R&R Notification] \n" +
         "-----Original Message-----\n" +
         "From: KRTAYLOR@S105KD4M.CO.GLOUCESTER.NJ.US\n" +
         "[mailto:KRTAYLOR@S105KD4M.CO.GLOUCESTER.NJ.US]\n" +
         "Sent: Friday, June 15, 2012 12:37 PM\n" +
         "To: EmergencyResponse\n" +
         "Subject: Automatic R&R Notification\n" +
         "Importance: High\n" +
         " \n" +
         "Incident Number  : 2012-00020569                    ORI: 0818109\n" +
         "Station: 10-1 \n" +
         "Incident Type  . :  MVC E MVC                  Priority: 2 \n" +
         "Incident Location: WOODBURY TURNERSVILLE RD\n" +
         "Venue: WASHINGTON \n" +
         "  Cross Street . : BLACK HORSE PIKE\n" +
         "Call Time- 12:36:27                                Date- 06/15/2012 \n" +
         "Dispatch - 12:37:21     En-route-              On-scene-\n" +
         "Depart 1- \n" +
         "Arrive 2 -              Depart 2-              In-statn-\n" +
         "Cleared -\n" +
         "Area:   R1              Section : 1002         Beat  . :    R        Map\n" +
         ". . :    C \n" +
         "Grid:  H20              Quadrant: 1003         District: 1003\n" +
         "Phone Number: (000) 000-0000                Call Source:  CEL\n" +
         "Units sent  : \n" +
         " 0818101     10-1                 0818109     E1093\n" +
         "Nature of Call : BLACK HORSE PIKE\n" +
         "Additional Info\n" +
         "  INJURED FEMALE, INCOHERENT\n" +
         "Additional Inc#s: \n" +
         " 0818101     201200020569\n" +
         "The Call Taker is WESTON KARLEE J\n" +
         "The Dispatcher is TAYLOR KATHY R\n",
 
         "ID:2012-00020569",
         "SRC:10-1",
         "CALL:MVC E MVC",
         "PRI:2",
         "ADDR:WOODBURY TURNERSVILLE RD",
         "MADDR:WOODBURY TURNERSVILLE RD & BLACK HORSE PIKE",
         "CITY:WASHINGTON",
         "X:BLACK HORSE PIKE",
         "TIME:12:36:27",
         "DATE:06/15/2012",
         "MAP:1003/1003/1002/R1",
         "UNIT:0818101 10-1 0818109 E1093",
         "INFO:BLACK HORSE PIKE");
 
     doTest("T5",
         "[FW: Automatic R&R Notification] \n" +
         "-----Original Message-----\n" +
         "From: LJIOCONA@S105KD4M.CO.GLOUCESTER.NJ.US\n" +
         "[mailto:LJIOCONA@S105KD4M.CO.GLOUCESTER.NJ.US]\n" +
         "Sent: Friday, June 15, 2012 12:53 PM\n" +
         "To: EmergencyResponse\n" +
         "Subject: Automatic R&R Notification\n" +
         "Importance: High\n" +
         " \n" +
         "Incident Number  : 2012-00020570                    ORI: 0818109\n" +
         "Station: 10-1 \n" +
         "Incident Type  . : MEDE E MED EMERG            Priority: 3 \n" +
         "Incident Location: 11 HUNTER CT\n" +
         "Venue: WASHINGTON \n" +
         "Located Between  : LONG BOW DR/DEAD END\n" +
         "Call Time- 12:50:54                                Date- 06/15/2012 \n" +
         "Dispatch - 12:53:28     En-route-              On-scene-\n" +
         "Depart 1- \n" +
         "Arrive 2 -              Depart 2-              In-statn-\n" +
         "Cleared -\n" +
         "Area:   R4              Section : 1020         Beat  . :    R        Map\n" +
         ". . : \n" +
         "Grid:  H18              Quadrant: 1014         District: 1014\n" +
         "Phone Number: (856) 589-9155                Call Source:  911\n" +
         "Caller. . . : MADELINE UNDERWOOD\n" +
         "Units sent  : \n" +
         " 0818101     F1011                0818109     E1092 \n" +
         " 0818109     E1093\n" +
         "Nature of Call : LONG BOW DR/DEAD END\n" +
         "Additional Info\n" +
         "  88 YOM, GENERAL WEAKNESS, CAN\"T STAND UP\n" +
         "  DOORS ARE LOCKED\n" +
         "Additional Inc#s: \n" +
         " 0818101     201200020570\n" +
         "The Call Taker is LANSDOWNE KENNETH R\n" +
         "The Dispatcher is TAYLOR KATHY R\n",
 
         "ID:2012-00020570",
         "SRC:10-1",
         "CALL:MEDE E MED EMERG",
         "PRI:3",
         "ADDR:11 HUNTER CT",
         "CITY:WASHINGTON",
         "X:LONG BOW DR/DEAD END",
         "TIME:12:50:54",
         "DATE:06/15/2012",
         "MAP:1014/1014/1020/R4",
         "PHONE:(856) 589-9155",
         "NAME:MADELINE UNDERWOOD",
         "UNIT:0818101 F1011 0818109 E1092 0818109 E1093",
         "INFO:LONG BOW DR/DEAD END");
 
     doTest("T6",
         "[FW: Automatic R&R Notification] \n" +
         "-----Original Message-----\n" +
         "From: KRTAYLOR@S105KD4M.CO.GLOUCESTER.NJ.US\n" +
         "[mailto:KRTAYLOR@S105KD4M.CO.GLOUCESTER.NJ.US]\n" +
         "Sent: Friday, June 15, 2012 5:00 PM\n" +
         "To: EmergencyResponse\n" +
         "Subject: Automatic R&R Notification\n" +
         "Importance: High\n" +
         " \n" +
         "Incident Number  : 2012-00020601                    ORI: 0818103\n" +
         "Station: 10-3 \n" +
         "Incident Type  . : BLDG F BUILDING             Priority: 2 \n" +
         "Incident Location: 1 MCCLURE DR\n" +
         "Venue: WASHINGTON \n" +
         "Located Between  : GREENTREE RD/DEAD END Common Name. . . : WASH PD\n" +
         "Call Time- 16:58:18                                Date- 06/15/2012 \n" +
         "Dispatch -              En-route-              On-scene-\n" +
         "Depart 1- \n" +
         "Arrive 2 -              Depart 2-              In-statn-\n" +
         "Cleared -\n" +
         "Area:   R3              Section :    R         Beat  . :    R        Map\n" +
         ". . : 1004 \n" +
         "Grid:  K18              Quadrant: 1011         District: 1011\n" +
         "Phone Number: (000) 000-7148                Call Source:  PBX\n" +
         "Caller. . . : WASHINGTON PD ZONE\n" +
         "Units sent  : \n" +
         " 0818103     10-3\n" +
         "Nature of Call : GREENTREE RD/DEAD END\n" +
         "Additional Info\n" +
         "  SMELL OF SOMETHING BURNING, CORP SAYS IT\n" +
         "  SMELLS VERY STRONG\n" +
         "Address Checks\n" +
         "Building-GEO : Governmental Bldg \n" +
         "  Const. Type . :    Concrete \n" +
         "  Truss Type. . :    STEEL \n" +
         "  Water Source  :    Hydrant \n" +
         "  # of Floors . :    2 STORY \n" +
         "  Pre-Plan. . . :    YES SEE F24 DOCUMENTS \n" +
         "  Local Station      10-3 \n" +
         "  File Updated  :    06/06/07 \n" +
         "  Knox Box Loc  :    N/A \n" +
         "  Nearby Hydrant:    ON SITE 6\" \n" +
         "  Hazards . . . :    JAIL CELLS \n" +
         "  Bldg Dimension:    150L 105W 20H \n" +
         "  Elec. Shutoff :    BOILER RM \n" +
         "  BUSN/RES NAME :    WASHINGTON TWP POLICE ADMINISTRATION BLDG \n" +
         "  EMER CONTACT  :    POLICE DEPT 856.256.1212 \n" +
         "  PHONE NUMBER  :    856.256.1212 \n" +
         "  NARRATIVE     :    PRE-PLAN ATTACHED \n" +
         " Building Narrative\n" +
         "  BUILDING PRE-PLAN POLICE ADMINISTRATION\n" +
         "Additional Inc#s: \n" +
         " 0818101     201200020601             0818102     201200020601\n" +
         "The Call Taker is LANSDOWNE KENNETH R\n" +
         "The Dispatcher is TAYLOR KATHY R\n",
 
         "ID:2012-00020601",
         "SRC:10-3",
         "CALL:BLDG F BUILDING",
         "PRI:2",
         "ADDR:1 MCCLURE DR",
         "CITY:WASHINGTON",
         "X:GREENTREE RD/DEAD END",
         "PLACE:WASH PD",
         "TIME:16:58:18",
         "DATE:06/15/2012",
         "MAP:1011/1011/R/R3",
         "PHONE:(000) 000-7148",
         "NAME:WASHINGTON PD ZONE",
         "UNIT:0818103 10-3",
         "INFO:GREENTREE RD/DEAD END");
 
     doTest("T7",
         "[FW: Automatic R&R Notification] \n" +
         "-----Original Message-----\n" +
         "From: KRTAYLOR@S105KD4M.CO.GLOUCESTER.NJ.US\n" +
         "[mailto:KRTAYLOR@S105KD4M.CO.GLOUCESTER.NJ.US]\n" +
         "Sent: Friday, June 15, 2012 5:00 PM\n" +
         "To: EmergencyResponse\n" +
         "Subject: Automatic R&R Notification\n" +
         "Importance: High\n" +
         " \n" +
         "Incident Number  : 2012-00020601                    ORI: 0818103\n" +
         "Station: 10-2 \n" +
         "Incident Type  . : BLDG F BUILDING             Priority: 2 \n" +
         "Incident Location: 1 MCCLURE DR\n" +
         "Venue: WASHINGTON \n" +
         "Located Between  : GREENTREE RD/DEAD END Common Name. . . : WASH PD\n" +
         "Call Time- 16:58:18                                Date- 06/15/2012 \n" +
         "Dispatch - 16:59:32     En-route-              On-scene-\n" +
         "Depart 1- \n" +
         "Arrive 2 -              Depart 2-              In-statn-\n" +
         "Cleared -\n" +
         "Area:   R3              Section :    R         Beat  . :    R        Map\n" +
         ". . : 1004 \n" +
         "Grid:  K18              Quadrant: 1011         District: 1011\n" +
         "Phone Number: (000) 000-7148                Call Source:  PBX\n" +
         "Caller. . . : WASHINGTON PD ZONE\n" +
         "Units sent  : \n" +
         " 0818102     10-2                 0818103     10-3\n" +
         "Nature of Call : GREENTREE RD/DEAD END\n" +
         "Additional Info\n" +
         "  SMELL OF SOMETHING BURNING, CORP SAYS IT\n" +
         "  SMELLS VERY STRONG\n" +
         "Address Checks\n" +
         "Building-GEO : Governmental Bldg \n" +
         "  Const. Type . :    Concrete \n" +
         "  Truss Type. . :    STEEL \n" +
         "  Water Source  :    Hydrant \n" +
         "  # of Floors . :    2 STORY \n" +
         "  Pre-Plan. . . :    YES SEE F24 DOCUMENTS \n" +
         "  Local Station      10-3 \n" +
         "  File Updated  :    06/06/07 \n" +
         "  Knox Box Loc  :    N/A \n" +
         "  Nearby Hydrant:    ON SITE 6\" \n" +
         "  Hazards . . . :    JAIL CELLS \n" +
         "  Bldg Dimension:    150L 105W 20H \n" +
         "  Elec. Shutoff :    BOILER RM \n" +
         "  BUSN/RES NAME :    WASHINGTON TWP POLICE ADMINISTRATION BLDG \n" +
         "  EMER CONTACT  :    POLICE DEPT 856.256.1212 \n" +
         "  PHONE NUMBER  :    856.256.1212 \n" +
         "  NARRATIVE     :    PRE-PLAN ATTACHED \n" +
         " Building Narrative\n" +
         "  BUILDING PRE-PLAN POLICE ADMINISTRATION\n" +
         "Additional Inc#s: \n" +
         " 0818101     201200020601             0818102     201200020601 \n" +
         " 0818109     201200020601\n" +
         "The Call Taker is LANSDOWNE KENNETH R\n" +
         "The Dispatcher is TAYLOR KATHY R\n",
 
         "ID:2012-00020601",
         "SRC:10-2",
         "CALL:BLDG F BUILDING",
         "PRI:2",
         "ADDR:1 MCCLURE DR",
         "CITY:WASHINGTON",
         "X:GREENTREE RD/DEAD END",
         "PLACE:WASH PD",
         "TIME:16:58:18",
         "DATE:06/15/2012",
         "MAP:1011/1011/R/R3",
         "PHONE:(000) 000-7148",
         "NAME:WASHINGTON PD ZONE",
         "UNIT:0818102 10-2 0818103 10-3",
         "INFO:GREENTREE RD/DEAD END");
 
     doTest("T8",
         "[FW: Automatic R&R Notification] \n" +
         "-----Original Message-----\n" +
         "From: KRTAYLOR@S105KD4M.CO.GLOUCESTER.NJ.US\n" +
         "[mailto:KRTAYLOR@S105KD4M.CO.GLOUCESTER.NJ.US]\n" +
         "Sent: Friday, June 15, 2012 5:52 PM\n" +
         "To: EmergencyResponse\n" +
         "Subject: Automatic R&R Notification\n" +
         "Importance: High\n" +
         " \n" +
         "Incident Number  : 2012-00020610                    ORI: 0818103\n" +
         "Station: 10-3 \n" +
         "Incident Type  . : ALRM F ALARM FIRE           Priority: 3 \n" +
         "Incident Location: 10 GARTON CT\n" +
         "Venue: WASHINGTON \n" +
         "Located Between  : FARMINGHAM DR/DEAD END\n" +
         "Call Time- 17:51:55                                Date- 06/15/2012 \n" +
         "Dispatch - 17:52:09     En-route-              On-scene-\n" +
         "Depart 1- \n" +
         "Arrive 2 -              Depart 2-              In-statn-\n" +
         "Cleared -\n" +
         "Area:   R3              Section : 1078         Beat  . :    R        Map\n" +
         ". . : \n" +
         "Grid:  L17              Quadrant: 1011         District: 1011\n" +
         "Phone Number: (800) 364-3616                Call Source:  TEL\n" +
         "Caller. . . : OP 111 GUARDIAN\n" +
         "Units sent  : \n" +
         " 0818103     10-3\n" +
         "Nature of Call : FARMINGHAM DR/DEAD END\n" +
         "Additional Info \n" +
         "  GENERAL     GRAZIANO RES   856-218-9548\n" +
         "Additional Inc#s:\n" +
         "The Call Taker is ZELINSKY NICK J\n" +
         "The Dispatcher is TAYLOR KATHY R\n",
 
         "ID:2012-00020610",
         "SRC:10-3",
         "CALL:ALRM F ALARM FIRE",
         "PRI:3",
         "ADDR:10 GARTON CT",
         "CITY:WASHINGTON",
         "X:FARMINGHAM DR/DEAD END",
         "TIME:17:51:55",
         "DATE:06/15/2012",
         "MAP:1011/1011/1078/R3",
         "PHONE:(800) 364-3616",
         "NAME:OP 111 GUARDIAN",
         "UNIT:0818103 10-3",
         "INFO:FARMINGHAM DR/DEAD END");
 
     doTest("T9",
         "[FW: Automatic R&R Notification] \n" +
         "-----Original Message-----\n" +
         "From: MPGISMON@S105KD4M.CO.GLOUCESTER.NJ.US\n" +
         "[mailto:MPGISMON@S105KD4M.CO.GLOUCESTER.NJ.US]\n" +
         "Sent: Saturday, June 16, 2012 10:55 AM\n" +
         "To: EmergencyResponse\n" +
         "Subject: Automatic R&R Notification\n" +
         "Importance: High\n" +
         " \n" +
         "Incident Number  : 2012-00020691                    ORI: 0818103\n" +
         "Station: 10-3 \n" +
         "Incident Type  . : SPIL F MAT SPILL            Priority: 4 \n" +
         "Incident Location: 461 DELSEA DR\n" +
         "Venue: WASHINGTON \n" +
         "Located Between  : HURFFVILLE CROSSKEYS RD/HURFFVILLE GRENLOCH R\n" +
         "Call Time- 10:54:12                                Date- 06/16/2012 \n" +
         "Dispatch -              En-route-              On-scene-\n" +
         "Depart 1- \n" +
         "Arrive 2 -              Depart 2-              In-statn-\n" +
         "Cleared -\n" +
         "Area:   R4              Section : DEL4         Beat  . :    R        Map\n" +
         ". . :    S \n" +
         "Grid:  J17              Quadrant: 1008         District: 1008\n" +
         "Phone Number: (856) 582-4771                Call Source:  911\n" +
         "Caller. . . :  REBECCA ROBERTS, ,\n" +
         "Nature of Call : HURFFVILLE CROSSKEYS RD/HURFFVILLE GRENLOCH R\n" +
         "Additional Info\n" +
         "  GAS LEAKING FROM CAR GAS TANK\n" +
         "  CAR IN DRIVEWAY\n" +
         "Additional Inc#s:\n" +
         "The Call Taker is ZELINSKY NICK J\n" +
         "The Dispatcher is GISMONDI MARK P\n",
 
         "ID:2012-00020691",
         "SRC:10-3",
         "CALL:SPIL F MAT SPILL",
         "PRI:4",
         "ADDR:461 DELSEA DR",
         "CITY:WASHINGTON",
         "X:HURFFVILLE CROSSKEYS RD/HURFFVILLE GRENLOCH R",
         "TIME:10:54:12",
         "DATE:06/16/2012",
         "MAP:1008/1008/DEL4/R4",
         "PHONE:(856) 582-4771",
         "NAME:REBECCA ROBERTS, ,",
         "INFO:HURFFVILLE CROSSKEYS RD/HURFFVILLE GRENLOCH R");
 
     doTest("T10",
         "[FW: Automatic R&R Notification] \n" +
         "-----Original Message-----\n" +
         "From: MPGISMON@S105KD4M.CO.GLOUCESTER.NJ.US\n" +
         "[mailto:MPGISMON@S105KD4M.CO.GLOUCESTER.NJ.US]\n" +
         "Sent: Saturday, June 16, 2012 10:56 AM\n" +
         "To: EmergencyResponse\n" +
         "Subject: Automatic R&R Notification\n" +
         "Importance: High\n" +
         " \n" +
         "Incident Number  : 2012-00020691                    ORI: 0818103\n" +
         "Station: 10-2 \n" +
         "Incident Type  . : SPIL F MAT SPILL            Priority: 4 \n" +
         "Incident Location: 461 DELSEA DR\n" +
         "Venue: WASHINGTON \n" +
         "Located Between  : HURFFVILLE CROSSKEYS RD/HURFFVILLE GRENLOCH R\n" +
         "Call Time- 10:54:12                                Date- 06/16/2012 \n" +
         "Dispatch - 10:56:00     En-route-              On-scene-\n" +
         "Depart 1- \n" +
         "Arrive 2 -              Depart 2-              In-statn-\n" +
         "Cleared -\n" +
         "Area:   R4              Section : DEL4         Beat  . :    R        Map\n" +
         ". . :    S \n" +
         "Grid:  J17              Quadrant: 1008         District: 1008\n" +
         "Phone Number: (856) 582-4771                Call Source:  911\n" +
         "Caller. . . :  REBECCA ROBERTS, ,\n" +
         "Units sent  : \n" +
         " 0818103     10-3                 0818102     F1022\n" +
         "Nature of Call : HURFFVILLE CROSSKEYS RD/HURFFVILLE GRENLOCH R\n" +
         "Additional Info\n" +
         "  GAS LEAKING FROM CAR GAS TANK\n" +
         "  CAR IN DRIVEWAY\n" +
         "Additional Inc#s: \n" +
         " 0818102     201200020691\n" +
         "The Call Taker is ZELINSKY NICK J\n" +
         "The Dispatcher is GISMONDI MARK P\n",
 
         "ID:2012-00020691",
         "SRC:10-2",
         "CALL:SPIL F MAT SPILL",
         "PRI:4",
         "ADDR:461 DELSEA DR",
         "CITY:WASHINGTON",
         "X:HURFFVILLE CROSSKEYS RD/HURFFVILLE GRENLOCH R",
         "TIME:10:54:12",
         "DATE:06/16/2012",
         "MAP:1008/1008/DEL4/R4",
         "PHONE:(856) 582-4771",
         "NAME:REBECCA ROBERTS, ,",
         "UNIT:0818103 10-3 0818102 F1022",
         "INFO:HURFFVILLE CROSSKEYS RD/HURFFVILLE GRENLOCH R");
 
     doTest("T11",
         "[FW: Automatic R&R Notification] \n" +
         "-----Original Message-----\n" +
         "From: MPGISMON@S105KD4M.CO.GLOUCESTER.NJ.US\n" +
         "[mailto:MPGISMON@S105KD4M.CO.GLOUCESTER.NJ.US]\n" +
         "Sent: Saturday, June 16, 2012 12:07 PM\n" +
         "To: EmergencyResponse\n" +
         "Subject: Automatic R&R Notification\n" +
         "Importance: High\n" +
         " \n" +
         "Incident Number  : 2012-00020700                    ORI: 0818103\n" +
         "Station: 10-3 \n" +
         "Incident Type  . : FUME F FUMES                Priority: 3 \n" +
         "Incident Location: 1304 ARONS CIR\n" +
         "Venue: WASHINGTON \n" +
         "Located Between  : RYAN LN/DEAD END\n" +
         "Call Time- 12:04:10                                Date- 06/16/2012 \n" +
         "Dispatch - 12:07:21     En-route-              On-scene-\n" +
         "Depart 1- \n" +
         "Arrive 2 -              Depart 2-              In-statn-\n" +
         "Cleared -\n" +
         "Area:   R4              Section : 1086         Beat  . :    R        Map\n" +
         ". . : \n" +
         "Grid:  H17              Quadrant: 1014         District: 1014\n" +
         "Phone Number: (856) 716-1027                Call Source:  589\n" +
         "Caller. . . : CECILIA SIUDAH\n" +
         "Units sent  : \n" +
         " 0818103     10-3\n" +
         "Nature of Call : RYAN LN/DEAD END\n" +
         "Additional Info\n" +
         "  STRONG NATURAL GAS ODOR IN RESIDENCE \n" +
         "  1305 AND 1306      AT LEAST 2 DIZZY\n" +
         "Address Checks\n" +
         "Additional Inc#s:\n" +
         "The Call Taker is TAYLOR KATHY R\n" +
         "The Dispatcher is GISMONDI MARK P\n",
 
         "ID:2012-00020700",
         "SRC:10-3",
         "CALL:FUME F FUMES",
         "PRI:3",
         "ADDR:1304 ARONS CIR",
         "CITY:WASHINGTON",
         "X:RYAN LN/DEAD END",
         "TIME:12:04:10",
         "DATE:06/16/2012",
         "MAP:1014/1014/1086/R4",
         "PHONE:(856) 716-1027",
         "NAME:CECILIA SIUDAH",
         "UNIT:0818103 10-3",
         "INFO:RYAN LN/DEAD END");
 
     doTest("T12",
         "[FW: Automatic R&R Notification] \n" +
         "-----Original Message-----\n" +
         "From: MPGISMON@S105KD4M.CO.GLOUCESTER.NJ.US\n" +
         "[mailto:MPGISMON@S105KD4M.CO.GLOUCESTER.NJ.US]\n" +
         "Sent: Saturday, June 16, 2012 12:07 PM\n" +
         "To: EmergencyResponse\n" +
         "Subject: Automatic R&R Notification\n" +
         "Importance: High\n" +
         " \n" +
         "Incident Number  : 2012-00020700                    ORI: 0818103\n" +
         "Station: 10-2 \n" +
         "Incident Type  . : FUME F FUMES                Priority: 3 \n" +
         "Incident Location: 1304 ARONS CIR\n" +
         "Venue: WASHINGTON \n" +
         "Located Between  : RYAN LN/DEAD END\n" +
         "Call Time- 12:04:10                                Date- 06/16/2012 \n" +
         "Dispatch - 12:07:21     En-route-              On-scene-\n" +
         "Depart 1- \n" +
         "Arrive 2 -              Depart 2-              In-statn-\n" +
         "Cleared -\n" +
         "Area:   R4              Section : 1086         Beat  . :    R        Map\n" +
         ". . : \n" +
         "Grid:  H17              Quadrant: 1014         District: 1014\n" +
         "Phone Number: (856) 716-1027                Call Source:  589\n" +
         "Caller. . . : CECILIA SIUDAH\n" +
         "Units sent  : \n" +
         " 0818103     10-3                 0818102     F1022\n" +
         "Nature of Call : RYAN LN/DEAD END\n" +
         "Additional Info\n" +
         "  STRONG NATURAL GAS ODOR IN RESIDENCE \n" +
         "  1305 AND 1306      AT LEAST 2 DIZZY\n" +
         "Address Checks\n" +
         "Additional Inc#s: \n" +
         " 0818102     201200020700\n" +
         "The Call Taker is TAYLOR KATHY R\n" +
         "The Dispatcher is GISMONDI MARK P\n",
 
         "ID:2012-00020700",
         "SRC:10-2",
         "CALL:FUME F FUMES",
         "PRI:3",
         "ADDR:1304 ARONS CIR",
         "CITY:WASHINGTON",
         "X:RYAN LN/DEAD END",
         "TIME:12:04:10",
         "DATE:06/16/2012",
         "MAP:1014/1014/1086/R4",
         "PHONE:(856) 716-1027",
         "NAME:CECILIA SIUDAH",
         "UNIT:0818103 10-3 0818102 F1022",
         "INFO:RYAN LN/DEAD END");
 
     doTest("T13",
         "[FW: Automatic R&R Notification] \n" +
         "-----Original Message-----\n" +
         "From: MPGISMON@S105KD4M.CO.GLOUCESTER.NJ.US\n" +
         "[mailto:MPGISMON@S105KD4M.CO.GLOUCESTER.NJ.US]\n" +
         "Sent: Saturday, June 16, 2012 12:11 PM\n" +
         "To: EmergencyResponse\n" +
         "Subject: Automatic R&R Notification\n" +
         "Importance: High\n" +
         " \n" +
         "Incident Number  : 2012-00020703                    ORI: 0818101\n" +
         "Station: 10-1 \n" +
         "Incident Type  . : WIRE F WIRES                Priority: 3 \n" +
         "Incident Location: EGG HARBOR RD\n" +
         "Venue: WASHINGTON \n" +
         "  Cross Street . : MT PLEASANT RD\n" +
         "Call Time- 12:10:04                                Date- 06/16/2012 \n" +
         "Dispatch -              En-route-              On-scene-\n" +
         "Depart 1- \n" +
         "Arrive 2 -              Depart 2-              In-statn-\n" +
         "Cleared -\n" +
         "Area:   R1              Section :    R         Beat  . :    R        Map\n" +
         ". . :    C \n" +
         "Grid:  H18              Quadrant: 1002         District: 1002\n" +
         "Phone Number: (609) 221-1994                Call Source:  TEL\n" +
         "Caller. . . : MICHELE BOCHANSKI\n" +
         "Nature of Call : MT PLEASANT RD\n" +
         "Additional Info \n" +
         "  WIRES SMOKING   BTW MT PEASANT AND \n" +
         "  HURRFVILLE\n" +
         "Additional Inc#s:\n" +
         "The Call Taker is ZELINSKY NICK J\n" +
         "The Dispatcher is GISMONDI MARK P\n",
 
         "ID:2012-00020703",
         "SRC:10-1",
         "CALL:WIRE F WIRES",
         "PRI:3",
         "ADDR:EGG HARBOR RD",
         "MADDR:EGG HARBOR RD & MT PLEASANT RD",
         "CITY:WASHINGTON",
         "X:MT PLEASANT RD",
         "TIME:12:10:04",
         "DATE:06/16/2012",
         "MAP:1002/1002/R/R1",
         "PHONE:(609) 221-1994",
         "NAME:MICHELE BOCHANSKI",
         "INFO:MT PLEASANT RD");
 
     doTest("T14",
         "[FW: Automatic R&R Notification] \n" +
         "-----Original Message-----\n" +
         "From: MRVANEK@S105KD4M.CO.GLOUCESTER.NJ.US\n" +
         "[mailto:MRVANEK@S105KD4M.CO.GLOUCESTER.NJ.US]\n" +
         "Sent: Saturday, June 16, 2012 2:13 PM\n" +
         "To: EmergencyResponse\n" +
         "Subject: Automatic R&R Notification\n" +
         "Importance: High\n" +
         " \n" +
         "Incident Number  : 2012-00020714                    ORI: 0818103\n" +
         "Station: 10-3 \n" +
         "Incident Type  . : FUME F FUMES                Priority: 3 \n" +
         "Incident Location: 1305 ARONS CIR\n" +
         "Venue: WASHINGTON \n" +
         "Located Between  : RYAN LN/DEAD END\n" +
         "Call Time- 14:12:46                                Date- 06/16/2012 \n" +
         "Dispatch - 14:12:56     En-route-              On-scene-\n" +
         "Depart 1- \n" +
         "Arrive 2 -              Depart 2-              In-statn-\n" +
         "Cleared -\n" +
         "Area:   R4              Section : 1086         Beat  . :    R        Map\n" +
         ". . : \n" +
         "Grid:  H17              Quadrant: 1014         District: 1014\n" +
         "Phone Number: (856) 283-8238                Call Source:  CEL\n" +
         "Caller. . . : BOBBIE ROBINSON\n" +
         "Units sent  : \n" +
         " 0818103     10-3\n" +
         "Nature of Call : RYAN LN/DEAD END\n" +
         "Additional Info\n" +
         "  NATURAL GAS SMELL IN RESIDENCE\n" +
         "Address Checks\n" +
         "Additional Inc#s:\n" +
         "The Call Taker is TAYLOR KATHY R\n" +
         "The Dispatcher is VANEK MATTHEW R\n",
 
         "ID:2012-00020714",
         "SRC:10-3",
         "CALL:FUME F FUMES",
         "PRI:3",
         "ADDR:1305 ARONS CIR",
         "CITY:WASHINGTON",
         "X:RYAN LN/DEAD END",
         "TIME:14:12:46",
         "DATE:06/16/2012",
         "MAP:1014/1014/1086/R4",
         "PHONE:(856) 283-8238",
         "NAME:BOBBIE ROBINSON",
         "UNIT:0818103 10-3",
         "INFO:RYAN LN/DEAD END");
 
     doTest("T15",
         "[FW: Automatic R&R Notification] \n" +
         "-----Original Message-----\n" +
         "From: AMGALLO@S105KD4M.CO.GLOUCESTER.NJ.US\n" +
         "[mailto:AMGALLO@S105KD4M.CO.GLOUCESTER.NJ.US]\n" +
         "Sent: Saturday, June 16, 2012 2:17 PM\n" +
         "To: EmergencyResponse\n" +
         "Subject: Automatic R&R Notification\n" +
         "Importance: High\n" +
         " \n" +
         "Incident Number  : 2012-00020714                    ORI: 0818103\n" +
         "Station: 10-2 \n" +
         "Incident Type  . : FUME F FUMES                Priority: 3 \n" +
         "Incident Location: 1305 ARONS CIR\n" +
         "Venue: WASHINGTON \n" +
         "Located Between  : RYAN LN/DEAD END\n" +
         "Call Time- 14:12:46                                Date- 06/16/2012 \n" +
         "Dispatch - 14:16:39     En-route- 14:16:39     On-scene-\n" +
         "Depart 1- \n" +
         "Arrive 2 -              Depart 2-              In-statn-\n" +
         "Cleared -\n" +
         "Area:   R4              Section : 1086         Beat  . :    R        Map\n" +
         ". . : \n" +
         "Grid:  H17              Quadrant: 1014         District: 1014\n" +
         "Phone Number: (856) 283-8238                Call Source:  CEL\n" +
         "Caller. . . : BOBBIE ROBINSON\n" +
         "Units sent  : \n" +
         " 0818103     10-3                 0818102     F1022\n" +
         "Nature of Call : RYAN LN/DEAD END\n" +
         "Additional Info\n" +
         "  NATURAL GAS SMELL IN RESIDENCE\n" +
         "Narrative \n" +
         " SJ GAS LOCKED OUT METERS, BUT WILL RETURN TO      AMGALLO    14:13:15 \n" +
         " CHECK                                             AMGALLO    14:13:17 \n" +
         " R F1022/6                                         AMGALLO    14:16:31\n" +
         "Address Checks\n" +
         "Additional Inc#s: \n" +
         " 0818102     201200020714\n" +
         "The Call Taker is TAYLOR KATHY R\n" +
         "The Dispatcher is VANEK MATTHEW R\n",
 
         "ID:2012-00020714",
         "SRC:10-2",
         "CALL:FUME F FUMES",
         "PRI:3",
         "ADDR:1305 ARONS CIR",
         "CITY:WASHINGTON",
         "X:RYAN LN/DEAD END",
         "TIME:14:12:46",
         "DATE:06/16/2012",
         "MAP:1014/1014/1086/R4",
         "PHONE:(856) 283-8238",
         "NAME:BOBBIE ROBINSON",
         "UNIT:0818103 10-3 0818102 F1022",
         "INFO:RYAN LN/DEAD END");
 
     doTest("T16",
         "[FW: Automatic R&R Notification] \n" +
         "-----Original Message-----\n" +
         "From: NJZELINS@S105KD4M.CO.GLOUCESTER.NJ.US\n" +
         "[mailto:NJZELINS@S105KD4M.CO.GLOUCESTER.NJ.US]\n" +
         "Sent: Sunday, June 17, 2012 7:47 AM\n" +
         "To: EmergencyResponse\n" +
         "Subject: Automatic R&R Notification\n" +
         "Importance: High\n" +
         " \n" +
         "Incident Number  : 2012-00020795                    ORI: 0818102\n" +
         "Station: 10-2 \n" +
         "Incident Type  . : ALRM F ALARM FIRE           Priority: 3 \n" +
         "Incident Location: 3100 BLACK HORSE PIKE\n" +
         "Venue: WASHINGTON \n" +
         "Located Between  : FAIRMOUNT DR/ARDMORE AVE Common Name. . . : SATURN OF\n" +
         "TURNERSVILLE\n" +
         "Call Time- 07:46:45                                Date- 06/17/2012 \n" +
         "Dispatch -              En-route-              On-scene-\n" +
         "Depart 1- \n" +
         "Arrive 2 -              Depart 2-              In-statn-\n" +
         "Cleared -\n" +
         "Area:   R2              Section : PIK8         Beat  . :    R        Map\n" +
         ". . :    S \n" +
         "Grid:  L21              Quadrant: 1006         District: 1006\n" +
         "Phone Number: (800) 633-2677                Call Source:  589\n" +
         "Caller. . . : ASG/3465\n" +
         "Nature of Call : FAIRMOUNT DR/ARDMORE AVE\n" +
         "Additional Info\n" +
         "  SHOWROOM HVAC DUCT DETECT\n" +
         "  6096345155\n" +
         "Additional Inc#s:\n" +
         "The Call Taker is GALLO ANDREW M\n" +
         "The Dispatcher is ZELINSKY NICK J\n",
 
         "ID:2012-00020795",
         "SRC:10-2",
         "CALL:ALRM F ALARM FIRE",
         "PRI:3",
         "ADDR:3100 BLACK HORSE PIKE",
         "CITY:WASHINGTON",
         "X:FAIRMOUNT DR/ARDMORE AVE",
         "PLACE:SATURN OF TURNERSVILLE",
         "TIME:07:46:45",
         "DATE:06/17/2012",
         "MAP:1006/1006/PIK8/R2",
         "PHONE:(800) 633-2677",
         "NAME:ASG/3465",
         "INFO:FAIRMOUNT DR/ARDMORE AVE");
 
     doTest("T17",
         "[FW: Automatic R&R Notification] \n" +
         "-----Original Message-----\n" +
         "From: JRGRESKO@S105KD4M.CO.GLOUCESTER.NJ.US\n" +
         "[mailto:JRGRESKO@S105KD4M.CO.GLOUCESTER.NJ.US]\n" +
         "Sent: Sunday, June 17, 2012 9:42 PM\n" +
         "To: EmergencyResponse\n" +
         "Subject: Automatic R&R Notification\n" +
         "Importance: High\n" +
         " \n" +
         "Incident Number  : 2012-00020878                    ORI: STATEFIRE\n" +
         "Station: 10-1 \n" +
         "Incident Type  . : DWEL F DWELLING             Priority: 1 \n" +
         "Incident Location: 1 GLOUCESTER TWP\n" +
         "Venue: CAMDEN \n" +
         "Located Between  :  /\n" +
         "Call Time- 21:41:38                                Date- 06/17/2012 \n" +
         "Dispatch -              En-route-              On-scene-\n" +
         "Depart 1- \n" +
         "Arrive 2 -              Depart 2-              In-statn-\n" +
         "Cleared -\n" +
         "Area:   OT              Section :   OT         Beat  . :   OT        Map\n" +
         ". . : \n" +
         "Grid:  C14              Quadrant:              District:\n" +
         "Phone Number: (000) 000-0000                Call Source:  TEL\n" +
         "Caller. . . : CAMDEN CTY\n" +
         "Nature of Call :  /\n" +
         "Additional Info \n" +
         "  1016    1012 OAKWOOD AVE  X-ST LAKELAND RD\n" +
         "Additional Inc#s: \n" +
         " 0818101     201200020878\n" +
         "The Call Taker is TALARICO KEVIN P\n" +
         "The Dispatcher is GRESKO JOHN R\n",
 
         "ID:2012-00020878",
         "SRC:10-1",
         "CALL:DWEL F DWELLING",
         "PRI:1",
         "ADDR:1 GLOUCESTER TWP",  // Not mapping
         "CITY:CAMDEN",
         "TIME:21:41:38",
         "DATE:06/17/2012",
         "MAP:OT/OT",
         "NAME:CAMDEN CTY",
         "INFO:/");
 
     doTest("T18",
         "[FW: Automatic R&R Notification] \n" +
         "-----Original Message-----\n" +
         "From: KEMAYCOC@S105KD4M.CO.GLOUCESTER.NJ.US\n" +
         "[mailto:KEMAYCOC@S105KD4M.CO.GLOUCESTER.NJ.US]\n" +
         "Sent: Monday, June 18, 2012 11:14 AM\n" +
         "To: EmergencyResponse\n" +
         "Subject: Automatic R&R Notification\n" +
         "Importance: High\n" +
         " \n" +
         "Incident Number  : 2012-00020936                    ORI: 0818101\n" +
         "Station: 10-1 \n" +
         "Incident Type  . : ALRM F ALARM FIRE           Priority: 3 \n" +
         "Incident Location: 4 RICHARD DR\n" +
         "Venue: WASHINGTON \n" +
         "Located Between  : HURFFVILLE RD/GOODWIN PKY\n" +
         "Call Time- 11:13:50                                Date- 06/18/2012 \n" +
         "Dispatch -              En-route-              On-scene-\n" +
         "Depart 1- \n" +
         "Arrive 2 -              Depart 2-              In-statn-\n" +
         "Cleared -\n" +
         "Area:   R1              Section : 1018         Beat  . :    R        Map\n" +
         ". . : \n" +
         "Grid:  J19              Quadrant: 1003         District: 1003\n" +
         "Phone Number: (866) 666-8897                Call Source:  589\n" +
         "Caller. . . :  2683 ASG\n" +
         "Nature of Call : HURFFVILLE RD/GOODWIN PKY\n" +
         "Additional Info\n" +
         "  ZONE 2 SMOKE\n" +
         "  RODRIGUEZ, 374-5463\n" +
         "Additional Inc#s:\n" +
         "The Call Taker is HAINSWORTH EDWARD W\n" +
         "The Dispatcher is MAYCOCK KATHRYN ELIZABTH\n",
 
         "ID:2012-00020936",
         "SRC:10-1",
         "CALL:ALRM F ALARM FIRE",
         "PRI:3",
         "ADDR:4 RICHARD DR",
         "CITY:WASHINGTON",
         "X:HURFFVILLE RD/GOODWIN PKY",
         "TIME:11:13:50",
         "DATE:06/18/2012",
         "MAP:1003/1003/1018/R1",
         "PHONE:(866) 666-8897",
         "NAME:2683 ASG",
         "INFO:HURFFVILLE RD/GOODWIN PKY");
 
     doTest("T19",
         "[FW: Automatic R&R Notification] \n" +
         "-----Original Message-----\n" +
         "From: KEMAYCOC@S105KD4M.CO.GLOUCESTER.NJ.US\n" +
         "[mailto:KEMAYCOC@S105KD4M.CO.GLOUCESTER.NJ.US]\n" +
         "Sent: Monday, June 18, 2012 2:12 PM\n" +
         "To: EmergencyResponse\n" +
         "Subject: Automatic R&R Notification\n" +
         "Importance: High\n" +
         " \n" +
         "Incident Number  : 2012-00020955                    ORI: 0818101\n" +
         "Station: 10-1 \n" +
         "Incident Type  . : ALRM F ALARM FIRE           Priority: 3 \n" +
         "Incident Location: 43 FOX HOLLOW LN\n" +
         "Venue: WASHINGTON \n" +
         "Located Between  : MT PLEASANT RD/FOX HOLLOW LN\n" +
         "Call Time- 14:11:54                                Date- 06/18/2012 \n" +
         "Dispatch - 14:12:10     En-route-              On-scene-\n" +
         "Depart 1- \n" +
         "Arrive 2 -              Depart 2-              In-statn-\n" +
         "Cleared -\n" +
         "Area:   R1              Section : 1070         Beat  . :    R        Map\n" +
         ". . : \n" +
         "Grid:  H18              Quadrant: 1002         District: 1002\n" +
         "Phone Number: (800) 633-2677                Call Source:  589\n" +
         "Caller. . . : CADDY COMM/3641\n" +
         "Units sent  : \n" +
         " 0818101     10-1\n" +
         "Nature of Call : MT PLEASANT RD/FOX HOLLOW LN\n" +
         "Additional Info\n" +
         "  FAMILY RM ACTIVATION\n" +
         "  PREM 8562282238\n" +
         "Additional Inc#s:\n" +
         "The Call Taker is GISMONDI ETTORE J\n" +
         "The Dispatcher is MAYCOCK KATHRYN ELIZABTH\n",
 
         "ID:2012-00020955",
         "SRC:10-1",
         "CALL:ALRM F ALARM FIRE",
         "PRI:3",
         "ADDR:43 FOX HOLLOW LN",
         "CITY:WASHINGTON",
         "X:MT PLEASANT RD/FOX HOLLOW LN",
         "TIME:14:11:54",
         "DATE:06/18/2012",
         "MAP:1002/1002/1070/R1",
         "PHONE:(800) 633-2677",
         "NAME:CADDY COMM/3641",
         "UNIT:0818101 10-1",
         "INFO:MT PLEASANT RD/FOX HOLLOW LN");
 
     doTest("T20",
         "[FW: Automatic R&R Notification] \n" +
         "-----Original Message-----\n" +
         "From: KEMAYCOC@S105KD4M.CO.GLOUCESTER.NJ.US\n" +
         "[mailto:KEMAYCOC@S105KD4M.CO.GLOUCESTER.NJ.US]\n" +
         "Sent: Monday, June 18, 2012 3:00 PM\n" +
         "To: EmergencyResponse\n" +
         "Subject: Automatic R&R Notification\n" +
         "Importance: High\n" +
         " \n" +
         "Incident Number  : 2012-00020966                    ORI: 0818102\n" +
         "Station: 10-2 \n" +
         "Incident Type  . : SERV F SERV ASSN            Priority: 5 \n" +
         "Incident Location: 3501 BLACK HORSE PIKE\n" +
         "Venue: WASHINGTON \n" +
         "Located Between  : TUCKAHOE RD/AMERICAN BLVD Common Name. . . :\n" +
         "MARSHALLS\n" +
         "Call Time- 14:59:46                                Date- 06/18/2012 \n" +
         "Dispatch -              En-route-              On-scene-\n" +
         "Depart 1- \n" +
         "Arrive 2 -              Depart 2-              In-statn-\n" +
         "Cleared -\n" +
         "Area:   R2              Section : PIK7         Beat  . :    R        Map\n" +
         ". . :    S \n" +
         "Grid:  L21              Quadrant: 1006         District: 1006\n" +
         "Phone Number: (856) 371-3619                Call Source:  911\n" +
         "Caller. . . : GINNY\n" +
         "Nature of Call : TUCKAHOE RD/AMERICAN BLVD\n" +
         "Additional Info\n" +
         "  IFO/3 BABY DUCKLINGS DOWN THE DRAIN\n" +
         "  CONTACT LINE OFFICER\n" +
         "Address Checks\n" +
         "Building-GEO : Multiple Bldg \n" +
         "  Const. Type . :    Brick Building \n" +
         "  Truss Type. . :    STEEL \n" +
         "  Water Source  :    Hydrant \n" +
         "  # of Floors . :    1 STORY \n" +
         "  Pre-Plan. . . :    YES SEE F24 DOCUMENTS \n" +
         "  Gas Authority      South Jersey Gas \n" +
         "  Elec Authority     Atlantic City Electric \n" +
         "  Water Authority    Municipal Utility Authority \n" +
         "  Local Station      10-2 \n" +
         "  File Updated  :    09/18/07 \n" +
         "  Knox Box Loc  :    NO \n" +
         "  Gas Shutoff . :    DIV C \n" +
         "  Elec. Shutoff :    DIV C \n" +
         "  BUSN/RES NAME :    CROSSKEYS COMMONS SHOPPING CENTER \n" +
         "  EMER CONTACT  :    HERITAGE PROPERTY GROUP 516.466.4300 \n" +
         "  NARRATIVE     :    PRE-PLAN ATTACHED \n" +
         "                :    SITE PLAN \n" +
         "                :    MULTIPLE SEE DOC\"S F24 \n" +
         " Building Narrative\n" +
         "  BUILDING PRE-PLAN SHOPPING CENTER SITE\n" +
         "  BUILDING PRE-PLAN STAPLES OFFICE SUPPLY\n" +
         "Additional Inc#s:\n" +
         "The Call Taker is BREYER MICHAEL R\n" +
         "The Dispatcher is MAYCOCK KATHRYN ELIZABTH\n",
 
         "ID:2012-00020966",
         "SRC:10-2",
         "CALL:SERV F SERV ASSN",
         "PRI:5",
         "ADDR:3501 BLACK HORSE PIKE",
         "CITY:WASHINGTON",
         "X:TUCKAHOE RD/AMERICAN BLVD",
         "PLACE:MARSHALLS",
         "TIME:14:59:46",
         "DATE:06/18/2012",
         "MAP:1006/1006/PIK7/R2",
         "PHONE:(856) 371-3619",
         "NAME:GINNY",
         "INFO:TUCKAHOE RD/AMERICAN BLVD");
 
     doTest("T21",
         "[FW: Automatic R&R Notification] \n" +
         "-----Original Message-----\n" +
         "From: GDROEMMI@S105KD4M.CO.GLOUCESTER.NJ.US\n" +
         "[mailto:GDROEMMI@S105KD4M.CO.GLOUCESTER.NJ.US]\n" +
         "Sent: Tuesday, June 19, 2012 7:26 AM\n" +
         "To: EmergencyResponse\n" +
         "Subject: Automatic R&R Notification\n" +
         "Importance: High\n" +
         " \n" +
         "Incident Number  : 2012-00021043                    ORI: 0818102\n" +
         "Station: 10-2 \n" +
         "Incident Type  . : ALRM F ALARM FIRE           Priority: 3 \n" +
         "Incident Location: 5600 BLACK HORSE PIKE\n" +
         "Venue: WASHINGTON \n" +
         "Located Between  : WHITMAN DR/GREENTREE RD Common Name. . . : ARBYS\n" +
         "ROAST BEEF\n" +
         "Call Time- 07:25:16                                Date- 06/19/2012 \n" +
         "Dispatch -              En-route-              On-scene-\n" +
         "Depart 1- \n" +
         "Arrive 2 -              Depart 2-              In-statn-\n" +
         "Cleared -\n" +
         "Area:   R2              Section : PIK2         Beat  . :    R        Map\n" +
         ". . :    S \n" +
         "Grid:  J20              Quadrant: 1017         District: 1017\n" +
         "Phone Number: (877) 238-7739                Call Source:  589\n" +
         "Caller. . . : 37904 ADT\n" +
         "Nature of Call : WHITMAN DR/GREENTREE RD\n" +
         "Additional Info\n" +
         "  DUCT DETECTOR, 227-7442\n" +
         "Address Checks\n" +
         "Building-GEO : Restaurant Bldg \n" +
         "  Const. Type . :    Wooden Building \n" +
         "  Truss Type. . :    WOOD \n" +
         "  Water Source  :    Hydrant \n" +
         "  # of Floors . :    1 STORY \n" +
         "  Pre-Plan. . . :    YES SEE F24 DOCUMENTS \n" +
         "  Gas Authority      South Jersey Gas \n" +
         "  Elec Authority     Atlantic City Electric \n" +
         "  Water Authority    Municipal Utility Authority \n" +
         "  Local Station      10-2 \n" +
         "  File Updated  :    09/11/07 \n" +
         "  Knox Box Loc  :    NO \n" +
         "  Nearby Hydrant:    400\" WHITMAN DR \n" +
         "  Bldg Dimension:    20H 90L 39W \n" +
         "  Gas Shutoff . :    DIV B \n" +
         "  Elec. Shutoff :    DIV C \n" +
         "  BUSN/RES NAME :    ARBYS ROAST BEEF \n" +
         "  EMER CONTACT  :    BARB COOK 856.875.7715 \n" +
         "  PHONE NUMBER  :    OFFICE 856.227.7742 \n" +
         "  NARRATIVE     :    PRE-PLAN ATTACHED \n" +
         " Building Narrative\n" +
         "  BUILDING PRE-PLAN\n" +
         "Additional Inc#s:\n" +
         "The Call Taker is BASILE LINDA R\n" +
         "The Dispatcher is ROEMMICH GLENN D\n",
 
         "ID:2012-00021043",
         "SRC:10-2",
         "CALL:ALRM F ALARM FIRE",
         "PRI:3",
         "ADDR:5600 BLACK HORSE PIKE",
         "CITY:WASHINGTON",
         "X:WHITMAN DR/GREENTREE RD",
         "PLACE:ARBYS ROAST BEEF",
         "TIME:07:25:16",
         "DATE:06/19/2012",
         "MAP:1017/1017/PIK2/R2",
         "PHONE:(877) 238-7739",
         "NAME:37904 ADT",
         "INFO:WHITMAN DR/GREENTREE RD");
 
     doTest("T22",
         "[FW: Automatic R&R Notification] \n" +
         "-----Original Message-----\n" +
         "From: GDROEMMI@S105KD4M.CO.GLOUCESTER.NJ.US\n" +
         "[mailto:GDROEMMI@S105KD4M.CO.GLOUCESTER.NJ.US]\n" +
         "Sent: Tuesday, June 19, 2012 12:25 PM\n" +
         "To: EmergencyResponse\n" +
         "Subject: Automatic R&R Notification\n" +
         "Importance: High\n" +
         " \n" +
         "Incident Number  : 2012-00021074                    ORI: 0818102\n" +
         "Station: 10-2 \n" +
         "Incident Type  . : BRSH F BRUSH                Priority: 4 \n" +
         "Incident Location: 188 FRIES MILL RD\n" +
         "Venue: WASHINGTON \n" +
         "Located Between  : GEORGETOWN RD/UNIVERSITY DR Common Name. . . : QUEST\n" +
         "DIAGNOSTICS\n" +
         "Call Time- 12:24:18                                Date- 06/19/2012 \n" +
         "Dispatch - 12:24:43     En-route-              On-scene-\n" +
         "Depart 1- \n" +
         "Arrive 2 -              Depart 2-              In-statn-\n" +
         "Cleared -\n" +
         "Area:   R2              Section :    R         Beat  . :    R        Map\n" +
         ". . :    C \n" +
         "Grid:  K20              Quadrant: 1009         District: 1009\n" +
         "Phone Number: (856) 404-1277                Call Source:  CEL\n" +
         "Caller. . . : AL KNIGHTS\n" +
         "Units sent  : \n" +
         " 0818102     10-2\n" +
         "Nature of Call : GEORGETOWN RD/UNIVERSITY DR\n" +
         "Additional Info\n" +
         "  MULCH OUT FRONT\n" +
         "Additional Inc#s:\n" +
         "The Call Taker is RICHARDS BRYAN D\n" +
         "The Dispatcher is ROEMMICH GLENN D\n",
 
         "ID:2012-00021074",
         "SRC:10-2",
         "CALL:BRSH F BRUSH",
         "PRI:4",
         "ADDR:188 FRIES MILL RD",
         "CITY:WASHINGTON",
         "X:GEORGETOWN RD/UNIVERSITY DR",
         "PLACE:QUEST DIAGNOSTICS",
         "TIME:12:24:18",
         "DATE:06/19/2012",
         "MAP:1009/1009/R/R2",
         "PHONE:(856) 404-1277",
         "NAME:AL KNIGHTS",
         "UNIT:0818102 10-2",
         "INFO:GEORGETOWN RD/UNIVERSITY DR");
 
     doTest("T23",
         "[FW: Automatic R&R Notification] \n" +
         "-----Original Message-----\n" +
         "From: GDROEMMI@S105KD4M.CO.GLOUCESTER.NJ.US\n" +
         "[mailto:GDROEMMI@S105KD4M.CO.GLOUCESTER.NJ.US]\n" +
         "Sent: Tuesday, June 19, 2012 3:36 PM\n" +
         "To: EmergencyResponse\n" +
         "Subject: Automatic R&R Notification\n" +
         "Importance: High\n" +
         " \n" +
         "Incident Number  : 2012-00021097                    ORI: 0818109\n" +
         "Station: 10-2 \n" +
         "Incident Type  . :  MVC E MVC                  Priority: 2 \n" +
         "Incident Location: 362 GANTTOWN RD\n" +
         "Venue: WASHINGTON \n" +
         "Located Between  : BELLS LAKE RD/EGG HARBOR RD Common Name. . . : ST\n" +
         "PETER + PAUL CHURCH\n" +
         "Call Time- 15:33:54                                Date- 06/19/2012 \n" +
         "Dispatch - 15:35:27     En-route-              On-scene-\n" +
         "Depart 1- \n" +
         "Arrive 2 -              Depart 2-              In-statn-\n" +
         "Cleared -\n" +
         "Area:   R2              Section :    R         Beat  . :    R        Map\n" +
         ". . :    C \n" +
         "Grid:  K19              Quadrant: 1009         District: 1009\n" +
         "Phone Number: (856) 466-8272                Call Source:  911\n" +
         "Caller. . . : DONNA\n" +
         "Units sent  : \n" +
         " 0818102     10-2                 0818109     E1092\n" +
         "Nature of Call : BELLS LAKE RD/EGG HARBOR RD\n" +
         "Additional Info\n" +
         "  CAR INTO POLE / POLE DOWN ON THE CAR\n" +
         "  TRANSFORMER SPARKING\n" +
         "Additional Inc#s: \n" +
         " 008LS       201200021097             0818102     201200021097\n" +
         "The Call Taker is BREYER MICHAEL R\n" +
         "The Dispatcher is ROEMMICH GLENN D\n",
 
         "ID:2012-00021097",
         "SRC:10-2",
         "CALL:MVC E MVC",
         "PRI:2",
         "ADDR:362 GANTTOWN RD",
         "CITY:WASHINGTON",
         "X:BELLS LAKE RD/EGG HARBOR RD",
         "PLACE:ST PETER + PAUL CHURCH",
         "TIME:15:33:54",
         "DATE:06/19/2012",
         "MAP:1009/1009/R/R2",
         "PHONE:(856) 466-8272",
         "NAME:DONNA",
         "UNIT:0818102 10-2 0818109 E1092",
         "INFO:BELLS LAKE RD/EGG HARBOR RD");
 
     doTest("T24",
         "[FW: Automatic R&R Notification] \n" +
         "-----Original Message-----\n" +
         "From: GDROEMMI@S105KD4M.CO.GLOUCESTER.NJ.US\n" +
         "[mailto:GDROEMMI@S105KD4M.CO.GLOUCESTER.NJ.US]\n" +
         "Sent: Tuesday, June 19, 2012 3:37 PM\n" +
         "To: EmergencyResponse\n" +
         "Subject: Automatic R&R Notification\n" +
         "Importance: High\n" +
         " \n" +
         "Incident Number  : 2012-00021097                    ORI: 0818109\n" +
         "Station: 10-3 \n" +
         "Incident Type  . :  MVC E MVC                  Priority: 2 \n" +
         "Incident Location: 362 GANTTOWN RD\n" +
         "Venue: WASHINGTON \n" +
         "Located Between  : BELLS LAKE RD/EGG HARBOR RD Common Name. . . : ST\n" +
         "PETER + PAUL CHURCH\n" +
         "Call Time- 15:33:54                                Date- 06/19/2012 \n" +
         "Dispatch -              En-route-              On-scene-\n" +
         "Depart 1- \n" +
         "Arrive 2 -              Depart 2-              In-statn-\n" +
         "Cleared -\n" +
         "Area:   R2              Section :    R         Beat  . :    R        Map\n" +
         ". . :    C \n" +
         "Grid:  K19              Quadrant: 1009         District: 1009\n" +
         "Phone Number: (856) 466-8272                Call Source:  911\n" +
         "Caller. . . : DONNA\n" +
         "Units sent  : \n" +
         " 008LS       LS-3                 0818102     10-2 \n" +
         " 0818109     E1092\n" +
         "Nature of Call : BELLS LAKE RD/EGG HARBOR RD\n" +
         "Additional Info\n" +
         "  CAR INTO POLE / POLE DOWN ON THE CAR\n" +
         "  TRANSFORMER SPARKING\n" +
         "Additional Inc#s: \n" +
         " 008LS       201200021097             0818102     201200021097 \n" +
         " 0818103     201200021097\n" +
         "The Call Taker is BREYER MICHAEL R\n" +
         "The Dispatcher is ROEMMICH GLENN D\n",
 
         "ID:2012-00021097",
         "SRC:10-3",
         "CALL:MVC E MVC",
         "PRI:2",
         "ADDR:362 GANTTOWN RD",
         "CITY:WASHINGTON",
         "X:BELLS LAKE RD/EGG HARBOR RD",
         "PLACE:ST PETER + PAUL CHURCH",
         "TIME:15:33:54",
         "DATE:06/19/2012",
         "MAP:1009/1009/R/R2",
         "PHONE:(856) 466-8272",
         "NAME:DONNA",
         "UNIT:008LS LS-3 0818102 10-2 0818109 E1092",
         "INFO:BELLS LAKE RD/EGG HARBOR RD");
 
     doTest("T25",
         "[FW: Automatic R&R Notification] \n" +
         "-----Original Message-----\n" +
         "From: GDROEMMI@S105KD4M.CO.GLOUCESTER.NJ.US\n" +
         "[mailto:GDROEMMI@S105KD4M.CO.GLOUCESTER.NJ.US]\n" +
         "Sent: Tuesday, June 19, 2012 5:44 PM\n" +
         "To: EmergencyResponse\n" +
         "Subject: Automatic R&R Notification\n" +
         "Importance: High\n" +
         " \n" +
         "Incident Number  : 2012-00021113                    ORI: 0818103\n" +
         "Station: 10-3 \n" +
         "Incident Type  . : SERV F SERV ASSN            Priority: 5 \n" +
         "Incident Location: 6 SPRING LAKE CT\n" +
         "Venue: WASHINGTON \n" +
         "Located Between  : SPRING LAKE AVE/DEAD END\n" +
         "Call Time- 17:43:09                                Date- 06/19/2012 \n" +
         "Dispatch -              En-route-              On-scene-\n" +
         "Depart 1- \n" +
         "Arrive 2 -              Depart 2-              In-statn-\n" +
         "Cleared -\n" +
         "Area:   R3              Section : 1031         Beat  . :    R        Map\n" +
         ". . : \n" +
         "Grid:  M18              Quadrant: 1011         District: 1011\n" +
         "Phone Number: (856) 589-1621                Call Source:  911\n" +
         "Caller. . . : MARK KENNY\n" +
         "Nature of Call : SPRING LAKE AVE/DEAD END\n" +
         "Additional Info\n" +
         "  BABY DUCKS IN STORM\n" +
         "  YELLOW ACCESS GATE\n" +
         "Address Checks\n" +
         "House Watch  : Other Need\n" +
         " Emergency Contact- HOMEOWNER \n" +
         "  6 SPRING LAKE CT                               Phone: (267) 716-2574 \n" +
         " Special Hazardous Materials- SEVERAL FALSE GUN CALLS FROM AREA  House\n" +
         "Watch Narrative\n" +
         "  Resident TTY USER\n" +
         "  This resident utilizes a relay service and is a tty user.\n" +
         "Additional Inc#s:\n" +
         "The Call Taker is MAYCOCK KATHRYN ELIZABTH The Dispatcher is ROEMMICH\n" +
         "GLENN D\n",
 
         "ID:2012-00021113",
         "SRC:10-3",
         "CALL:SERV F SERV ASSN",
         "PRI:5",
         "ADDR:6 SPRING LAKE CT",
         "CITY:WASHINGTON",
         "X:SPRING LAKE AVE/DEAD END",
         "TIME:17:43:09",
         "DATE:06/19/2012",
         "MAP:1011/1011/1031/R3",
         "PHONE:(856) 589-1621",
         "NAME:MARK KENNY",
         "INFO:SPRING LAKE AVE/DEAD END");
 
     doTest("T26",
         "[FW: Automatic R&R Notification] \n" +
         "-----Original Message-----\n" +
         "From: CFBASILE@S105KD4M.CO.GLOUCESTER.NJ.US\n" +
         "[mailto:CFBASILE@S105KD4M.CO.GLOUCESTER.NJ.US]\n" +
         "Sent: Wednesday, June 20, 2012 12:05 AM\n" +
         "To: EmergencyResponse\n" +
         "Subject: Automatic R&R Notification\n" +
         "Importance: High\n" +
         " \n" +
         "Incident Number  : 2012-00021160                    ORI: STATEFIRE\n" +
         "Station: 10-1 \n" +
         "Incident Type  . :  MVC E MVC                  Priority: 2 \n" +
         "Incident Location: 3950 AC EXP EB\n" +
         "Venue: AC EXP \n" +
         "Located Between  : EXIT 38/EXIT 41\n" +
         "Call Time- 00:02:53                                Date- 06/20/2012 \n" +
         "Dispatch -              En-route-              On-scene-\n" +
         "Depart 1- \n" +
         "Arrive 2 -              Depart 2-              In-statn-\n" +
         "Cleared -\n" +
         "Area:   20              Section :   20         Beat  . :   20        Map\n" +
         ". . :    S \n" +
         "Grid:                   Quadrant: 2002         District: 2002\n" +
         "Phone Number: (000) 000-0000                Call Source:  589\n" +
         "Caller. . . : CAMDEN CTY\n" +
         "Units sent  : \n" +
         " 0818109     E1092                5400        54-255\n" +
         "Nature of Call : EXIT 38/EXIT 41\n" +
         "Additional Info\n" +
         "  CAR INTO TREE\n" +
         "  10-1 10-9\n" +
         "Additional Inc#s: \n" +
         " 0818101     201200021160             0818109     201200021160 \n" +
         " 5400        201200021160\n" +
         "The Call Taker is CONDO ROBERT J\n" +
         "The Dispatcher is BASILE CHRISTOPHER S\n",
 
         "ID:2012-00021160",
         "SRC:10-1",
         "CALL:MVC E MVC",
         "PRI:2",
        "ADDR:3950 AC EXP EB",  // What is this???
        "MADDR:3950 AC EXP",  
        "CITY:AC EXP",
         "X:EXIT 38/EXIT 41",
         "TIME:00:02:53",
         "DATE:06/20/2012",
         "MAP:2002/2002/20/20",
         "NAME:CAMDEN CTY",
         "UNIT:0818109 E1092 5400 54-255",
         "INFO:EXIT 38/EXIT 41");
 
     doTest("T27",
         "[FW: Automatic R&R Notification] \n" +
         "-----Original Message-----\n" +
         "From: CFBASILE@S105KD4M.CO.GLOUCESTER.NJ.US\n" +
         "[mailto:CFBASILE@S105KD4M.CO.GLOUCESTER.NJ.US]\n" +
         "Sent: Wednesday, June 20, 2012 12:05 AM\n" +
         "To: EmergencyResponse\n" +
         "Subject: Automatic R&R Notification\n" +
         "Importance: High\n" +
         " \n" +
         "Incident Number  : 2012-00021160                    ORI: STATEFIRE\n" +
         "Station: 10-3 \n" +
         "Incident Type  . :  MVC E MVC                  Priority: 2 \n" +
         "Incident Location: 3950 AC EXP EB\n" +
         "Venue: AC EXP \n" +
         "Located Between  : EXIT 38/EXIT 41\n" +
         "Call Time- 00:02:53                                Date- 06/20/2012 \n" +
         "Dispatch - 00:04:58     En-route-              On-scene-\n" +
         "Depart 1- \n" +
         "Arrive 2 -              Depart 2-              In-statn-\n" +
         "Cleared -\n" +
         "Area:   20              Section :   20         Beat  . :   20        Map\n" +
         ". . :    S \n" +
         "Grid:                   Quadrant: 2002         District: 2002\n" +
         "Phone Number: (000) 000-0000                Call Source:  589\n" +
         "Caller. . . : CAMDEN CTY\n" +
         "Units sent  : \n" +
         " 0818101     10-1                 0818103     F1038 \n" +
         " 0818109     E1092                5400        54-255\n" +
         "Nature of Call : EXIT 38/EXIT 41\n" +
         "Additional Info\n" +
         "  CAR INTO TREE\n" +
         "  10-1 10-9\n" +
         "Additional Inc#s: \n" +
         " 0818101     201200021160             0818103     201200021160 \n" +
         " 0818109     201200021160             5400        201200021160\n" +
         "The Call Taker is CONDO ROBERT J\n" +
         "The Dispatcher is BASILE CHRISTOPHER S\n",
 
         "ID:2012-00021160",
         "SRC:10-3",
         "CALL:MVC E MVC",
         "PRI:2",
         "ADDR:3950 AC EXP EB", 
        "MADDR:3950 AC EXP",
        "CITY:AC EXP",
         "X:EXIT 38/EXIT 41",
         "TIME:00:02:53",
         "DATE:06/20/2012",
         "MAP:2002/2002/20/20",
         "NAME:CAMDEN CTY",
         "UNIT:0818101 10-1 0818103 F1038 0818109 E1092 5400 54-255",
         "INFO:EXIT 38/EXIT 41");
 
     doTest("T28",
         "[FW: Automatic R&R Notification] \n" +
         "-----Original Message-----\n" +
         "From: CFBASILE@S105KD4M.CO.GLOUCESTER.NJ.US\n" +
         "[mailto:CFBASILE@S105KD4M.CO.GLOUCESTER.NJ.US]\n" +
         "Sent: Wednesday, June 20, 2012 1:52 AM\n" +
         "To: EmergencyResponse\n" +
         "Subject: Automatic R&R Notification\n" +
         "Importance: High\n" +
         " \n" +
         "Incident Number  : 2012-00021166                    ORI: 0818109\n" +
         "Station: 10-1 \n" +
         "Incident Type  . :  MVC E MVC                  Priority: 2 \n" +
         "Incident Location: GANTTOWN RD\n" +
         "Venue: WASHINGTON \n" +
         "  Cross Street . : BERWYN RD\n" +
         "Call Time- 01:49:55                                Date- 06/20/2012 \n" +
         "Dispatch - 01:51:46     En-route-              On-scene-\n" +
         "Depart 1- \n" +
         "Arrive 2 -              Depart 2-              In-statn-\n" +
         "Cleared -\n" +
         "Area:   R2              Section : 1006         Beat  . :    R        Map\n" +
         ". . :    C \n" +
         "Grid:  J20              Quadrant: 1005         District: 1005\n" +
         "Phone Number: (856) 981-6599                Call Source:  911\n" +
         "Caller. . . : ERIN\n" +
         "Units sent  : \n" +
         " 0818101     10-1                 0818109     E1092 \n" +
         " 0818109     E1096\n" +
         "Nature of Call : BERWYN RD\n" +
         "Additional Info\n" +
         "  OVERTURNED INTO WOODS\n" +
         "Address Checks\n" +
         "Additional Inc#s: \n" +
         " 0818101     201200021166\n" +
         "The Call Taker is DAVIS ELLIOT SCOTT\n" +
         "The Dispatcher is BASILE CHRISTOPHER S\n",
 
         "ID:2012-00021166",
         "SRC:10-1",
         "CALL:MVC E MVC",
         "PRI:2",
         "ADDR:GANTTOWN RD",
         "MADDR:GANTTOWN RD & BERWYN RD",
         "CITY:WASHINGTON",
         "X:BERWYN RD",
         "TIME:01:49:55",
         "DATE:06/20/2012",
         "MAP:1005/1005/1006/R2",
         "PHONE:(856) 981-6599",
         "NAME:ERIN",
         "UNIT:0818101 10-1 0818109 E1092 0818109 E1096",
         "INFO:BERWYN RD");
 
     doTest("T29",
         "[FW: Automatic R&R Notification] \n" +
         "-----Original Message-----\n" +
         "From: CFBASILE@S105KD4M.CO.GLOUCESTER.NJ.US\n" +
         "[mailto:CFBASILE@S105KD4M.CO.GLOUCESTER.NJ.US]\n" +
         "Sent: Wednesday, June 20, 2012 1:52 AM\n" +
         "To: EmergencyResponse\n" +
         "Subject: Automatic R&R Notification\n" +
         "Importance: High\n" +
         " \n" +
         "Incident Number  : 2012-00021166                    ORI: 0818109\n" +
         "Station: 10-3 \n" +
         "Incident Type  . :  MVC E MVC                  Priority: 2 \n" +
         "Incident Location: GANTTOWN RD\n" +
         "Venue: WASHINGTON \n" +
         "  Cross Street . : BERWYN RD\n" +
         "Call Time- 01:49:55                                Date- 06/20/2012 \n" +
         "Dispatch - 01:51:46     En-route-              On-scene-\n" +
         "Depart 1- \n" +
         "Arrive 2 -              Depart 2-              In-statn-\n" +
         "Cleared -\n" +
         "Area:   R2              Section : 1006         Beat  . :    R        Map\n" +
         ". . :    C \n" +
         "Grid:  J20              Quadrant: 1005         District: 1005\n" +
         "Phone Number: (856) 981-6599                Call Source:  911\n" +
         "Caller. . . : ERIN\n" +
         "Units sent  : \n" +
         " 0818101     10-1                 0818103     F1038 \n" +
         " 0818109     E1092                0818109     E1096\n" +
         "Nature of Call : BERWYN RD\n" +
         "Additional Info\n" +
         "  OVERTURNED INTO WOODS\n" +
         "Address Checks\n" +
         "Additional Inc#s: \n" +
         " 0818101     201200021166             0818103     201200021166\n" +
         "The Call Taker is DAVIS ELLIOT SCOTT\n" +
         "The Dispatcher is BASILE CHRISTOPHER S\n",
 
         "ID:2012-00021166",
         "SRC:10-3",
         "CALL:MVC E MVC",
         "PRI:2",
         "ADDR:GANTTOWN RD",
         "MADDR:GANTTOWN RD & BERWYN RD",
         "CITY:WASHINGTON",
         "X:BERWYN RD",
         "TIME:01:49:55",
         "DATE:06/20/2012",
         "MAP:1005/1005/1006/R2",
         "PHONE:(856) 981-6599",
         "NAME:ERIN",
         "UNIT:0818101 10-1 0818103 F1038 0818109 E1092 0818109 E1096",
         "INFO:BERWYN RD");
 
   }
   
   public static void main(String[] args) {
     new NJGloucesterCountyBParserTest().generateTests("T1");
   }
 }
