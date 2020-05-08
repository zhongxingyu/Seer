 package net.anei.cadpage.parsers.NY;
 
 import net.anei.cadpage.parsers.Message;
 import net.anei.cadpage.parsers.SmartAddressParser;
 import net.anei.cadpage.parsers.MsgInfo.Data;
 
 /*
 Erie County, NY
 Contact: "Buttino, John" <John.Buttino@erie.gov>   (network administrator)
 AMH 238 WESTFIELD RD EMS 79 YO F/  CHEST PAIN
 AMH 52 ENDICOTT DR EMS 82 YO M
 AMH 670 LONGMEADOW RD EMS 71 Y/O F CHEST PAINS, DIFFICULTY BREATHING
 AMH 3030 SHERIDAN DR EMS RM 146 58 M TROUBLE BREATHING
 AMH 35 ELM RD EMS 69M CHEST/ARM PAIN
 
 Contact: brad marshall <northspartans@gmail.com>
 Sender: 777075442231
 ALERT@ERIE.GOV AMH KLEIN W RD&FOREST N RD MVA 2 CAR MVA - HEAD INJURY
 
 Contact: nyerpa96 <nyerpa96@gmail.com>
 ALERT@ERIE.GOV FIRE CO-DETECTOR 262 MILLER ST APT: GARAGE LANCASTER TOWN CO DETECTOR ACTIV / NO SYMPTOMS
 ALERT@ERIE.GOV EMS 4779 TRANSIT RD LANCASTER TOWN GREEN DODGE CALIBER 25 YO FEMALE FEELS AS THOUGH SHE MAY PASS OUT
 ALERT@ERIE.GOV EMS 4805 TRANSIT RD APT: 1106 LANCASTER TOWN EMS- 59 YO MALE, LEG PAIN AS A RESULT OF A FALL EARLIER
 ALERT@ERIE.GOV EMS 48 BENTLEY CR LANCASTER TOWN 79 Y/O FEMALE LETHARGIC
 ALERT@ERIE.GOV FIRE CO-DETECTOR 47 VIA DONATO E LANCASTER TOWN CO DETECTOR NO SYMPTOMS REFER TWIN DISTRICT FD
 ALERT@ERIE.GOV EMS 4805 TRANSIT RD APT: 3105 LANCASTER TOWN EMS/66 YO FEMALE SEVERE HEADACHE/GENERAL ILLNESS REFER TWIN DISTRICT FD REF
 
 Contact: Todd Rzeszutek <firefighter5502@yahoo.com>
 Sender: 9300xxxx
 NOTIFICATIONS 85 MANITOU ST DEPEW DEPEW FIRE DEPARTMENT- DAILY TEST
 NOTIFICATIONS 85 MANITOU ST DEPEW CONDUCTED THE DAILY FIRE RADIO AND PAGER TEST
 4569 BROADWAY DEPEW COMMERCIAL FIRE ALARM COMING FROM THE BANQUET ROOM
 
 Contact: Kira Wilson <kira.jeanne0106@gmail.com>
 ALERT@ERIE.GOV EMS DOG BITE 140 VALLEY VIEW
 
 Not yet implemented!
 Contact: peter putnam <medic4451@gmail.com>
 Sender: 17165415102
 Erie_Alert MUTUAL AID TO LAKEVIEW - FIRE ALARM ACTIVATION AT 5502 BIRCHWOOD.  <NOREPLY>
 
 
 Possible call code values
 
 AMA             MUTUAL AID-Automatic Mutual Aid                   
 AP              AIRPORT ALERT                                    
 AST             MISCELLANEOUS-Assist                             
 BA              ALARMS-Box Alarm                                 
 DUP             Duplicate Call                                    
 E911            Emergency Call                                    
 EMA             ALARMS-EMS Alarm                                 
 EMS             EMS-Emergency Medical Service                    
 EMSC            EMS-Emergency Medical Service Cold               
 F               Structure                                        
 FAL             ALARMS-Fire Alarm Activation                     
 HM              HAZMAT-Leak, Spill                               
 MAA             MUTUAL AID-Ambulance                             
 MAF             MUTUAL AID-Fire                                  
 MAR             MUTUAL AID-Rescue                                
 MAS             MUTUAL AID-Standby                               
 MF              Miscellaneous                                    
 MVA             EMS-Motor Vehicle Accident                       
 MVF             Motor Vehicle                                    
 NOT             Notification                                     
 POL             Police Emergency                                 
 R               EMS-Rescue                                       
 SA              Silent Alarm                                     
 TR              EMS-Transportation                               
 TRC             EMS-Transportation Cold                          
 TRH             EMS-Transportation Hot  
 */
 
 
 public class NYErieCountyAParser extends SmartAddressParser {
   
   private Message msg;
   
   public NYErieCountyAParser() {
     super(CITY_LIST, "ERIE COUNTY", "NY");
   }
 
   @Override
   protected Data parseMsg(Message msg, int parserFlags) {
     this.msg = msg;
     return super.parseMsg(msg, parserFlags);
   }
 
   @Override
  protected boolean parseMsg(String body, Data data) {
     
     if (body.startsWith("AMH ")) return parseAmhMsg(body.substring(4), data);
     if (msg.getAddress().equals("ALERT@ERIE.GOV")) return parseErieMsg(body, data);
    if (msg.getAddress().startsWith("9300")) return parseDepewMsg(body, data); 
     return false;
   }
 
   public boolean parseAmhMsg(String body, Data data) {
     parseAddress(StartType.START_ADDR, body, data);
     data.strCall = getLeft();
     
     return true;
   }
 
   private boolean parseErieMsg(String body, Data data) {
     parseAddress(StartType.START_CALL, body, data);
     data.strSupp = getLeft();
     return true;
   }
 
   private boolean parseDepewMsg(String body, Data data) {
     parseAddress(StartType.START_CALL, body, data);
     if (data.strAddress.length() == 0) return false;
     if (data.strCall.length() == 0) {
       data.strCall = getLeft();
     } else {
       data.strSupp = getLeft();
     }
     return true;
   }
   
   private static final String[] CITY_LIST = new String[]{
     "ALDEN TOWN",
     "ALDEN",
     "AMHERST TOWN",
     "ANGOLA ON THE LAKE",
     "ANGOLA",
     "AURORA TOWN",
     "BILLINGTON HEIGHTS",
     "BLASDELL",
     "BOSTON TOWN",
     "BRANT TOWN",
     "BUFFALO",
     "CHEEKTOWAGA TOWN",
     "CLARENCE CENTER",
     "CLARENCE TOWN",
     "COLDEN TOWN",
     "COLLINS TOWN",
     "CONCORD TOWN",
     "DEPEW",
     "EAST AMHERST",
     "EAST AURORA",
     "EDEN TOWN",
     "ELMA CENTER",
     "ELMA TOWN",
     "EVANS TOWN",
     "FARNHAM",
     "GOWANDA",
     "GRAND ISLAND TOWN",
     "HAMBURG TOWN",
     "HAMBURG",
     "HARRIS HILL",
     "HOLLAND TOWN",
     "KENMORE",
     "LACKAWANNA",
     "LAKE ERIE BEACH",
     "LAKE VIEW",
     "LANCASTER TOWN",
     "LANCASTER",
     "MARILLA TOWN",
     "NEWSTEAD TOWN",
     "NORTH BOSTON",
     "NORTH COLLINS TOWN",
     "NORTH COLLINS",
     "ORCHARD PARK TOWN",
     "ORCHARD PARK",
     "SARDINIA TOWN",
     "SLOAN",
     "SPRINGVILLE",
     "TONAWANDA TOWN",
     "TONAWANDA",
     "TOWN LINE",
     "WALES TOWN",
     "WEST SENECA TOWN",
     "WILLIAMSVILLE"
   };
 }
