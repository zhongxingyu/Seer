 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package system;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 
 /**
  *
  * @author jonathanrainer
  */
 public class DataStructures
 {
     private ArrayList<String> masterListBox1;
     private HashMap<String, ArrayList<String>> masterListBox2;
     private HashMap<String, ArrayList<String>> masterListBox3;
     private HashMap<String, ArrayList<String>> masterListBox4;
     private HashMap<String, String> teamLevelPasswords;
     
     public DataStructures()
     {
         ArrayList<String> potentialFieldsBox1 = generateMasterListBox1();
         ArrayList<String> potentialFieldsBox2 = generateMasterListBox2(masterListBox1);
         ArrayList<String> potentialFieldsBox3 = generateMasterListBox3(potentialFieldsBox2);
         generateMasterListBox4(potentialFieldsBox3, potentialFieldsBox2);
         teamLevelPasswords = generateTeamLevelPasswords();
     }
     
     private ArrayList<String> generateMasterListBox1()
     {
         masterListBox1 = new ArrayList<>();
         Collections.addAll(masterListBox1, "Village", "Gate", "Venue", "Site");
         return masterListBox1;
     }
     
     
     private ArrayList<String> generateMasterListBox2(ArrayList<String> fieldsBox1)
     {
         masterListBox2 = new HashMap<>();
         // Village Colour ArrayList
         ArrayList<String> villageColour = new ArrayList<>();
         Collections.addAll(villageColour, "V___Blue", "V___Green", "V___Purple", 
                 "V___Red", "V___Yellow");
         //Gate Colour ArrayList (To differentiate gates from Villages,
         //there are spaces at the end of common names. Add
         ArrayList<String> gateColour = new ArrayList<>();
         Collections.addAll(gateColour, "G___Blue (Outer)", "G___Blue (Inner)", 
                 "G___Green ", "G___White (Outer)", "G___White (Inner)", "G___Red", 
                 "G___Yellow ", "G___H.F.G", "G___Black (Ped)", "G___Brown (Ped)");
         //Venue Type ArrayList
         ArrayList<String> venueType = new ArrayList<>();
         Collections.addAll(venueType, "Ve__Grown Ups", "Ve__Children/Youth", 
                 "Ve__General");
         // Site ArrayList
         ArrayList<String> site = new ArrayList<>();
         Collections.addAll(site, "S___Car Park (Day)", "S___Car Park (Main)", "S___Runway",
                 "S___Bays", "S___BunkerBins", "S___Hallam Building", "S___Freezer Packs",
                 "S___Hospitality", "S___Kitchens", "S___Ticket Office (Blue)",
                 "S___Ticket Office (Red)", "S___Band Stand", "S___Fencing", "S___Pond");
 
         ArrayList<ArrayList<String>> listOfLists = new ArrayList<>();
         Collections.addAll(listOfLists, villageColour, gateColour, venueType,
                 site);
 
         ArrayList<String> listOfAllPotentialItems = new ArrayList<>();
 
         int i = 0;
         while (i < listOfLists.size())
         {
             masterListBox2.put(fieldsBox1.get(i), listOfLists.get(i));
             listOfAllPotentialItems.addAll(listOfLists.get(i));
             i++;
         }
 
         return listOfAllPotentialItems;
 
 
     }
 
     private ArrayList<String> generateMasterListBox3(ArrayList<String> fieldsBox2)
     {
         masterListBox3 = new HashMap<>();
         //Villages Options
         ArrayList<String> blue = new ArrayList<>();
         Collections.addAll(blue, "VB__1", "VB__2", "VB__3", "VB__4", "VB__5",
                 "VB__6", "VB__7", "VB__8", "VB__9", "VB__10");
         ArrayList<String> green = new ArrayList<>();
         Collections.addAll(green, "VG__1", "VG__2", "VG__3", "VG__4",
                 "VG__5", "VG__6", "VG__7");
         ArrayList<String> purple = new ArrayList<>();
         Collections.addAll(purple, "VP__1", "VP__2", "VP__3", "VP__4");
         ArrayList<String> red = new ArrayList<>();
         Collections.addAll(red, "VR__1", "VR__2", "VR__3", "VR__4", "VR__5", "VR__6",
                 "VR__7");
         ArrayList<String> yellow = new ArrayList<>();
         Collections.addAll(yellow, "VY__1", "VY__2", "VY__3",
                 "VY__4", "VY__5", "VY__6");
 
         //Gates Options
         ArrayList<String> nextBox = new ArrayList<>();
         nextBox.add("G___NA");
 
         //Adults options
         ArrayList<String> adultsVenues = new ArrayList<>();
         Collections.addAll(adultsVenues, "VeA_Meeting Place (inc. Cafe)",
                "VeA_Impact", "VeA_Burn", "VeA_Leaders Lounge", "VeA_Pastoral Prayer",
                 "VeA_Sanctuary", "VeA_Arts 3:16", "VeA_Manifest", "VeA_Renovate", "VeA_Synergy",
                 "VeA_Upper Room", "VeA_Tearfund (inc. Cafe)");
         //Childrens and Youth Options
         ArrayList<String> childrensYouthVenues = new ArrayList<>();
         Collections.addAll(childrensYouthVenues, "VeC_Gems", "VeC_Pebbles",
                 "VeC_Stepping Stones", "VeC_Ground Breakers", "VeC_Rock Solid",
                 "VeC_Boulder Gang", "VeC_Fridge", "VeC_Club One", "VeC_Thirst", "VeC_Flava",
                 "VeC_Sports Field", "VeC_Our Place");
         //General Venues Options
         ArrayList<String> generalVenues = new ArrayList<>();
         Collections.addAll(generalVenues, "VeG_Food Court", "VeG_Marketplace (inc. Cafe)",
                 "VeG_Medical Centre", "VeG_New Wine FM");
 
         //Site Options
         ArrayList<String> carParkDaySite = new ArrayList<>();
         Collections.addAll(carParkDaySite, "CPD_NA");
         ArrayList<String> carParkMainSite = new ArrayList<>();
         Collections.addAll(carParkMainSite, "CPM_NA");
         ArrayList<String> runwaySite = new ArrayList<>();
         Collections.addAll(runwaySite, "RUN_NA");
         ArrayList<String> baysSite = new ArrayList<>();
         Collections.addAll(baysSite, "Bay 1", "Bay 2", "Bay 3", "Bay 4", "Bay 5");
         ArrayList<String> bunkerBinsSite = new ArrayList<>();
         Collections.addAll(bunkerBinsSite, "BB__End of Sheds", "BB__Red Gate", "BB__WigWam");
         ArrayList<String> hallamBuildingSite = new ArrayList<>();
         Collections.addAll(hallamBuildingSite, "HB__Admin", "HB__Control Office",
                 "HB__Finance", "HB__Information", "HB__Production", "HB__Kitchen", "HB__Team Lounge", 
                 "HB__General", "HB__Site Crew");
         ArrayList<String> freezerPacksSite = new ArrayList<>();
         Collections.addAll(freezerPacksSite, "FP__NA");
         ArrayList<String> hospitalitySite = new ArrayList<>();
         Collections.addAll(hospitalitySite, "HS__NA");
         ArrayList<String> kitchensSite = new ArrayList<>();
         Collections.addAll(kitchensSite, "KS__NA");
         ArrayList<String> ticketOfficeBlueSite = new ArrayList<>();
         Collections.addAll(ticketOfficeBlueSite, "TB__NA");
         ArrayList<String> ticketOfficeRedSite = new ArrayList<>();
         Collections.addAll(ticketOfficeRedSite, "TR__NA");
         ArrayList<String> BandStandSite = new ArrayList<>();
         Collections.addAll(BandStandSite, "BS__NA");
         ArrayList<String> fencingSite = new ArrayList<>();
         Collections.addAll(fencingSite, "FS__NA");
         ArrayList<String> pondSite = new ArrayList<>();
         Collections.addAll(pondSite, "PS__NA");
 
         ArrayList<ArrayList<String>> listOfLists = new ArrayList<>();
         Collections.addAll(listOfLists, blue, green, purple, red, yellow,
                 nextBox, nextBox, nextBox, nextBox, nextBox, nextBox,
                 nextBox, nextBox, nextBox, nextBox, adultsVenues,
                 childrensYouthVenues, generalVenues, carParkDaySite,
                 carParkMainSite, runwaySite, baysSite, bunkerBinsSite,
                 hallamBuildingSite, freezerPacksSite, hospitalitySite, kitchensSite,
                 ticketOfficeBlueSite, ticketOfficeRedSite, BandStandSite, fencingSite, pondSite);
 
         ArrayList<String> listOfAllPotentialItems = new ArrayList<>();
 
         int i = 0;
         while (i < listOfLists.size())
         {
             masterListBox3.put(fieldsBox2.get(i), listOfLists.get(i));
             listOfAllPotentialItems.addAll(listOfLists.get(i));
             i++;
         }
 
         return listOfAllPotentialItems;
     }
 
     private void generateMasterListBox4(ArrayList<String> fieldsBox3, 
             ArrayList<String> fieldsBox2)
     {
         masterListBox4 = new HashMap<>();
         //Blue Villages ArrayLists
         ArrayList<String> blue1 = new ArrayList<>();
         Collections.addAll(blue1, "VB1_Toilet (Male)", "VB1_Toilet (Female)", 
         "VB1_Shower", "VB1_Elsan Point", "VB1_Fire Point", "VB1_Water Supply",
         "VB1_Electrical Hook-Up", "VB1_Other");
         ArrayList<String> blue2 = new ArrayList<>();
         Collections.addAll(blue2, "VB2_Fire Point", "VB2_Water Supply",
         "VB2_Electrical Hook-Up", "VB2_Other");
         ArrayList<String> blue3 = new ArrayList<>();
         Collections.addAll(blue3, "VB3_Toilet (Male)", "VB3_Toilet (Female)", 
         "VB3_Shower", "VB3_Fire Point", "VB3_Water Supply",
         "VB3_Electrical Hook-Up", "VB3_Other");
         ArrayList<String> blue4 = new ArrayList<>();
         Collections.addAll(blue4, "VB4_Fire Point", "VB4_Water Supply",
         "VB4_Electrical Hook-Up", "VB4_Other");
         ArrayList<String> blue5 = new ArrayList<>();
         Collections.addAll(blue5, "VB5_Fire Point", "VB5_Water Supply",
         "VB5_Electrical Hook-Up", "VB5_Other");
         ArrayList<String> blue6 = new ArrayList<>();
         Collections.addAll(blue6, "VB6_Fire Point", "VB6_Water Supply",
         "VB6_Electrical Hook-Up", "VB6_Other");
         ArrayList<String> blue7 = new ArrayList<>();
         Collections.addAll(blue7, "VB7_Fire Point", "VB7_Water Supply",
         "VB7_Electrical Hook-Up", "VB7_Other");
         ArrayList<String> blue8 = new ArrayList<>();
         Collections.addAll(blue8, "VB8_Fire Point", "VB8_Water Supply",
         "VB8_Electrical Hook-Up", "VB8_Other");
         ArrayList<String> blue9 = new ArrayList<>();
         Collections.addAll(blue9, "VB9_Fire Point", "VB9_Water Supply",
         "VB9_Electrical Hook-Up", "VB9_Other");
         ArrayList<String> blue10 = new ArrayList<>();
         Collections.addAll(blue10, "VB10Fire Point", "VB10Water Supply",
         "VB10Electrical Hook-Up", "VB10Other");
         // Green Villages ArrayLists
         ArrayList<String> green1 = new ArrayList<>();
         Collections.addAll(green1, "VG1_Fire Point", "VG1_Water Supply",
         "VG1_Electrical Hook-Up", "VG1_Other");
         ArrayList<String> green2 = new ArrayList<>();
         Collections.addAll(green2, "VG2_Toilet (Male)", "VG2_Toilet (Female)", "VG2_Shower", 
                 "VG2_Fire Point", "VG2_Water Supply", "VG2_Electrical Hook-Up", "VG2_Other");
         ArrayList<String> green3 = new ArrayList<>();
         Collections.addAll(green3, "VG3_Fire Point", "VG3_Water Supply",
         "VG3_Electrical Hook-Up", "VG3_Other");
         ArrayList<String> green4 = new ArrayList<>();
         Collections.addAll(green4, "VG4_Fire Point", "VG4_Water Supply",
         "VG4_Electrical Hook-Up", "VG4_Other");
         ArrayList<String> green5 = new ArrayList<>();
         Collections.addAll(green5, "VG5_Fire Point", "VG5_Water Supply",
         "VG5_Electrical Hook-Up", "VG5_Other");
         ArrayList<String> green6 = new ArrayList<>();
         Collections.addAll(green6, "VG6_Fire Point", "VG6_Water Supply",
         "VG6_Electrical Hook-Up", "VG6_Other");
         ArrayList<String> green7 = new ArrayList<>();
         Collections.addAll(green7, "VG7_Fire Point", "VG7_Water Supply",
         "VG7_Electrical Hook-Up", "VG7_Other");
         //Purple Villages ArrayList
         ArrayList<String> purple1 = new ArrayList<>();
         Collections.addAll(purple1, "VP1_Toilet (Male)", "VP1_Toilet (Female)", 
         "VP1_Shower", "VP1_Fire Point", "VP1_Water Supply",
         "VP1_Electrical Hook-Up", "VP1_Other");
         ArrayList<String> purple2 = new ArrayList<>();
         Collections.addAll(purple2, "VP2_Fire Point", "VP2_Water Supply",
         "VP2_Electrical Hook-Up", "VP2_Other");
         ArrayList<String> purple3 = new ArrayList<>();
         Collections.addAll(purple3, "VP3_Toilet (Male)", "VP3_Toilet (Female)", 
         "VP3_Toilet (Disabled)", "VP3_Shower", "VP3_Elsan Point", "VP3_Fire Point", "VP3_Water Supply",
         "VP3_Electrical Hook-Up", "VP3_Other");
         ArrayList<String> purple4 = new ArrayList<>();
         Collections.addAll(purple4, "VP4_Fire Point", "VP4_Water Supply",
         "VP4_Electrical Hook-Up", "VP4_Other");
         //Red Villages
         ArrayList<String> red1 = new ArrayList<>();
         Collections.addAll(red1, "VR1_Fire Point", "VR1_Water Supply",
         "VR1_Electrical Hook-Up", "VR1_Other");
         ArrayList<String> red2 = new ArrayList<>();
         Collections.addAll(red2, "VR2_Fire Point", "VR2_Water Supply",
         "VR2_Electrical Hook-Up", "VR2_Other");
         ArrayList<String> red3 = new ArrayList<>();
         Collections.addAll(red3, "VR3_Fire Point", "VR3_Water Supply",
         "VR3_Electrical Hook-Up", "VR3_Other");
         ArrayList<String> red4 = new ArrayList<>();
         Collections.addAll(red4, "VR4_Toilet (Male)", "VR4_Toilet (Female)", 
         "VR4_Shower", "VR4_Elsan Point", "VR4_Fire Point", "VR4_Water Supply",
         "VR4_Electrical Hook-Up", "VR4_Other");
         ArrayList<String> red5 = new ArrayList<>();
         Collections.addAll(red5, "VR5_Fire Point", "VR5_Water Supply",
         "VR5_Electrical Hook-Up", "VR5_Other");
         ArrayList<String> red6 = new ArrayList<>();
         Collections.addAll(red6, "VR6_Fire Point", "VR6_Water Supply",
         "VR6_Electrical Hook-Up", "VR6_Other");
         ArrayList<String> red7 = new ArrayList<>();
         Collections.addAll(red7, "VR7_Fire Point", "VR7_Water Supply",
         "VR7_Electrical Hook-Up", "VR7_Other");
         //Yellow Vilages
         ArrayList<String> yellow1 = new ArrayList<>();
         Collections.addAll(yellow1, "VY1_Toilet (Male)", "VY1_Toilet (Female)", 
         "VY1_Shower", "VY1_Fire Point", "VY1_Water Supply",
         "VY1_Electrical Hook-Up", "VY1_Other");
         ArrayList<String> yellow2 = new ArrayList<>();
         Collections.addAll(yellow2, "VY2_Fire Point", "VY2_Water Supply",
         "VY2_Electrical Hook-Up", "VY2_Other");
         ArrayList<String> yellow3 = new ArrayList<>();
         Collections.addAll(yellow3, "VY3_Fire Point", "VY3_Water Supply",
         "VY3_Electrical Hook-Up", "VY3_Other");
         ArrayList<String> yellow4 = new ArrayList<>();
         Collections.addAll(yellow4, "VY4_Toilet (Male)", "VY4_Toilet (Female)", 
          "VY4_Shower", "VY4_Fire Point", "VY4_Water Supply",
         "VY4_Electrical Hook-Up", "VY4_Other");
         ArrayList<String> yellow5 = new ArrayList<>();
         Collections.addAll(yellow5, "VY5_Fire Point", "VY5_Water Supply",
         "VY5_Electrical Hook-Up", "VY5_Other");
         ArrayList<String> yellow6 = new ArrayList<>();
         Collections.addAll(yellow6, "VY6_Fire Point", "VY6_Water Supply",
         "VY6_Electrical Hook-Up", "VY6_Gliding Club Toilets", "VY6_Other");
         
          //Gates Options
         ArrayList<String> blueOuterGate = new ArrayList<>();
         Collections.addAll(blueOuterGate, "GBO_Gate", "GBO_Lighting", "GBO_Other");
         ArrayList<String> blueInnerGate = new ArrayList<>();
         Collections.addAll(blueInnerGate, "GBI_Toilet (Male)", "GBI_Toilet(Female)", "GBI_Toilet (Disabled)", "GBI_Shower"
                 , "GBI_Elsan Point", "GBI_Ticket Office", "GBI_Gate", "GBI_Lighting", "GBI_Shutters", "GBI_Other");
         ArrayList<String> greenGate = new ArrayList<>();
         Collections.addAll(greenGate, "GG__Gate", "GG__Lighting", "GG__Cabin", "GG__Other");
         ArrayList<String> whiteOuterGate = new ArrayList<>();
         Collections.addAll(whiteOuterGate, "GWO_Gate", "GWO_Lighting", "GWO_Other");
         ArrayList<String> whiteInnerGate = new ArrayList<>();
         Collections.addAll(whiteInnerGate, "GWI_Gate", "GWI_Other");
         ArrayList<String> redGate = new ArrayList<>();
         Collections.addAll(redGate, "GR__Toilet (Male)", "GR__Toilet(Female)", "GR__Shower"
                 , "GR__Elsan Point", "GR__Ticket Office", "GR__Gate", "GR__Lighting",
                 "GR__Shutters", "GR__Other");
         ArrayList<String> yellowGate = new ArrayList<>();
         Collections.addAll(yellowGate, "GY__Gate", "GY__Lighting", "GY__Other");
         ArrayList<String> hfgGate = new ArrayList<>();
         Collections.addAll(hfgGate, "GHF_Gate", "GHF_Other");
         ArrayList<String> blackPedGate = new ArrayList<>();
         Collections.addAll(blackPedGate, "GBP_Gate", "GBP_Other");
         ArrayList<String> brownPedGate = new ArrayList<>();
         Collections.addAll(brownPedGate, "GBrPGate", "GBrPOther");
         
         //Venues Options
         //Adult Venues Facilities
         ArrayList<String> meetingPlace = new ArrayList<>();
         Collections.addAll(meetingPlace, "AVM_Toilet (Male)", "AVM_Toilet (Female)", 
                 "AVM_Toilet (Disabled)", "AVM_Stage", "AVM_Cafe", "AVM_Lighting",
                 "AVM_Power Supply", "AVM_Door Locks", "AVM_Other");
         ArrayList<String> impact = new ArrayList<>();
         Collections.addAll(impact, "AVI_Toilet (Male)", "AVI_Toilet (Female)", 
                 "AVI_Kitchen", "AVI_Stage", "AVI_Cafe", "AVI_Lighting",
                 "AVI_Power Supply", "AVI_Other");
         ArrayList<String> burn = new ArrayList<>();
         Collections.addAll(burn, "AVB_Toilet (Male)", "AVB_Toilet (Female)", 
                 "AVB_Toilet (Disabled)", "AVB_Bar", "AVB_Stage", "AVB_Cafe", "AVB_Lighting",
                 "AVB_Power Supply", "AVB_Other");
         ArrayList<String> leadersLounge = new ArrayList<>();
         Collections.addAll(leadersLounge, "AVL_Toilet (Male)", "AVL_Toilet (Female)", 
                 "AVL_Shower (Male)", "AVL_Shower (Female)", 
                 "AVL_Kitchen", "AVL_Lighting", "AVL_Power Supply", "AVL_Other");
         ArrayList<String> pastoralPrayer = new ArrayList<>();
         Collections.addAll(pastoralPrayer, "AVP_Lighting",
                 "AVP_Power Supply", "AVP_Other");
         ArrayList<String> sanctuary = new ArrayList<>();
         Collections.addAll(sanctuary, "AVS_Lighting",
                 "AVS_Power Supply", "AVS_Other");
         ArrayList<String> arts316 = new ArrayList<>();
         Collections.addAll(arts316, "AVA_Lighting",
                 "AVA_Power Supply", "AVA_Other");
         ArrayList<String> manifest = new ArrayList<>();
         Collections.addAll(manifest, "AVMaStage", "AVMaLighting",
                 "AVMaPower Supply", "AVMaOther");
         ArrayList<String> renovate = new ArrayList<>();
         Collections.addAll(renovate, "AVR_Stage", "AVR_Lighting",
                 "AVR_Power Supply", "AVR_Other");
         ArrayList<String> synergy = new ArrayList<>();
         Collections.addAll(synergy, "AVSyStage", "AVSyLighting",
                 "AVSyPower Supply", "AVSyOther");
         ArrayList<String> upperRoom = new ArrayList<>();
         Collections.addAll(upperRoom, "AVU_Toilets (Male)", "AVU_Toilets (Female)", 
                 "AVU_Kitchen", "AVU_Lighting",
                 "AVU_Power Supply", "AVU_Other");
         ArrayList<String> tearfund = new ArrayList<>();
         Collections.addAll(tearfund, "AVT_Stage", "AVT_Cafe", "AVT_Lighting",
                 "AVT_Power Supply", "AVT_Other");
         //Children/Youth Venues Facilties
         ArrayList<String> gems = new ArrayList<>();
         Collections.addAll(gems, "CYG_Stage", "CYG_Lighting",
                 "CYG_Power Supply", "CYG_Other");
         ArrayList<String> pebbles = new ArrayList<>();
         Collections.addAll(pebbles, "CYP_Portaloo", "CYP_Stage", "CYP_Lighting",
                 "CYP_Power Supply", "CYP_Other");
         ArrayList<String> steppingStones = new ArrayList<>();
         Collections.addAll(steppingStones, "CYSSPortaloo", "CYSSStage", "CYSSLighting",
                 "CYSSPower Supply", "CYSSOther");
         ArrayList<String> groundBreakers = new ArrayList<>();
         Collections.addAll(groundBreakers, "CYGBStage", "CYGBLighting",
                 "CYGBPower Supply", "CYGBOther");
         ArrayList<String> rockSolid = new ArrayList<>();
         Collections.addAll(rockSolid, "CYRSStage", "CYRSLighting",
                 "CYRSPower Supply", "CYRSOther");
         ArrayList<String> boulderGang = new ArrayList<>();
         Collections.addAll(boulderGang, "CYBGStage", "CYBGLighting",
                 "CYBGPower Supply", "CYBGOther");
         ArrayList<String> fridge = new ArrayList<>();
         Collections.addAll(fridge, "CYF_Stage", "CYF_Lighting",
                 "CYF_Power Supply", "CYF_Other");
         ArrayList<String> clubOne = new ArrayList<>();
         Collections.addAll(clubOne, "CYCOStage", "CYCOLighting",
                 "CYCOPower Supply", "CYCOOther");
         ArrayList<String> thirst = new ArrayList<>();
         Collections.addAll(thirst, "CYT_Stage", "CYT_Lighting",
                 "CYT_Power Supply", "CYT_Other");
         ArrayList<String> flava = new ArrayList<>();
         Collections.addAll(flava, "CYF_Cafe", "CYF_Stage", "CYF_Lighting",
                 "CYF_Power Supply", "CYF_Other");
         ArrayList<String> sportsField = new ArrayList<>();
         Collections.addAll(sportsField, "CYSFOther");
         ArrayList<String> ourPlace = new ArrayList<>();
         Collections.addAll(ourPlace, 
                 "CYOPToilet (Disabled)", "CYOPShower (Disabled)", "CYOPKitchen",
                 "CYOPPower Supply", "CYOPLighting", "CYOPOther");
         //General Venues Facilities
         ArrayList<String> foodCourt = new ArrayList<>();
         Collections.addAll(foodCourt, "FC__NA");
         ArrayList<String> marketPlace = new ArrayList<>();
         Collections.addAll(marketPlace, "VMP_Toilet (Male)", "VMP_Toilet (Female)", 
                 "VMP_Toilet (Disabled)", "VMP_Lighting",
                 "VMP_Power Supply", "VMP_Other");
         ArrayList<String> medicalCentre = new ArrayList<>();
         Collections.addAll(medicalCentre, "VMC_Lighting", "VMC_Power Supply", "VMC_Other");
         ArrayList<String> newWineFM = new ArrayList<>();
         Collections.addAll(newWineFM,"VNF_Lighting", "VNF_Power Supply", "VNF_Other");
         
         //Site Facilities
         ArrayList<String> carParkDay = new ArrayList<>();
         Collections.addAll(carParkDay, "CPD_NA");
         ArrayList<String> carParkMain = new ArrayList<>();
         Collections.addAll(carParkMain, "CPM_Lighting", "CPM_Other");
         ArrayList<String> runway = new ArrayList<>();
         Collections.addAll(runway, "RUN_NA");
         ArrayList<String> bay1 = new ArrayList<>();
         Collections.addAll(bay1, "BA1_Power", "BA1_Lighting", "BA1_Other");
         ArrayList<String> bay2 = new ArrayList<>();
         Collections.addAll(bay2, "BA2_Power", "BA2_Lighting", "BA2_Other");
         ArrayList<String> bay3 = new ArrayList<>();
         Collections.addAll(bay3, "BA3_Power", "BA3_Lighting", "BA3_Other");
         ArrayList<String> bay4 = new ArrayList<>();
         Collections.addAll(bay4, "BA4_Power", "BA4_Lighting", "BA4_Other");
         ArrayList<String> bay5 = new ArrayList<>();
         Collections.addAll(bay5, "BA5_Power", "BA5_Lighting", "BA5_Other");
         ArrayList<String> endOfShedsBB = new ArrayList<>();
         Collections.addAll(endOfShedsBB, "BBE_Toilet", "BBE_Shower", "BBE_Power Supply",
                 "BBE_Other");
         ArrayList<String> redGateBB = new ArrayList<>();
         Collections.addAll(redGateBB, "BBR_Toilet", "BBR_Shower", "BBR_Power Supply",
                 "BBR_Other");
         ArrayList<String> wigWamBB = new ArrayList<>();
         Collections.addAll(wigWamBB, "BBW_Toilet", "BBW_Shower", "BBW_Power Supply",
                 "BBW_Other");
         ArrayList<String> admin = new ArrayList<>();
         Collections.addAll(admin, "HBA_Power Supply", "HBA_Lighting", "HBA_Other");
         ArrayList<String> controlOffice = new ArrayList<>();
         Collections.addAll(controlOffice, "HBC_Radio", "HBC_Power Supply", "HBC_Other");
         ArrayList<String> finance = new ArrayList<>();
         Collections.addAll(finance, "HBF_Power Supply", "HBF_Lighting", "HBF_Other");
         ArrayList<String> information = new ArrayList<>();
         Collections.addAll(information, "HBI_Power Supply", "HBI_Lighting", "HBI_Other");
         ArrayList<String> production = new ArrayList<>();
         Collections.addAll(production, "HBP_Power Supply", "HBP_Lighting", "HBP_Other");
         ArrayList<String> kitchen = new ArrayList<>();
         Collections.addAll(kitchen, "HBK_Power Supply", "HBK_Lighting", "HBK_Other");
         ArrayList<String> teamLounge = new ArrayList<>();
         Collections.addAll(teamLounge, "HBT_NA");
         ArrayList<String> general = new ArrayList<>();
         Collections.addAll(general, "HBG_Toilet (Male)", "HBG_Toilet(Female)",
                 "HBG_Toilet (Disabled)", "HBG_Shower (Male)", "HBG_Shower (Female)", 
                 "HBG_Diary Room");
         ArrayList<String> freezerPacks = new ArrayList<>();
         Collections.addAll(freezerPacks, "FP__Power Supply, FP__Lock, FP__Other");
         ArrayList<String> hospitality = new ArrayList<>();
         Collections.addAll(hospitality, "HO__Kitchen", "HO__Lighting", "HO__Power Supply", "HO__Other");
         ArrayList<String> ticketOfficeBlue = new ArrayList<>();
         Collections.addAll(ticketOfficeBlue, "TOB_Power Supply", "TOB_Lighting", "TOB_Other");
         ArrayList<String> ticketOfficeRed = new ArrayList<>();
         Collections.addAll(ticketOfficeRed, "TOR_Power Supply", "TOR_Lighting", "TOR_Other");
         ArrayList<String> bandStand = new ArrayList<>();
         Collections.addAll(bandStand, "BAS_NA");
         ArrayList<String> fencing = new ArrayList<>();
         Collections.addAll(fencing, "FEN_NA");
         ArrayList<String> pond = new ArrayList<>();
         Collections.addAll(pond, "PON_NA");
         
         ArrayList<ArrayList<String>> listOfLists = new ArrayList<>();
         Collections.addAll(listOfLists, blue1, blue2, blue3, blue4, blue5,
                 blue6, blue7, blue8, blue9, blue10, green1, green2, green3, 
                 green4, green5, green6, green7, purple1, purple2, purple3,
                 purple4, red1, red2, red3, red4, red5, red6, red7,
                 yellow1, yellow2, yellow3, yellow4, yellow5, yellow6, blueOuterGate,
                 blueInnerGate, greenGate, whiteOuterGate, whiteInnerGate, redGate,
                 yellowGate, hfgGate, blackPedGate, brownPedGate, meetingPlace,
                 impact, burn, leadersLounge, pastoralPrayer, sanctuary, arts316,
                 manifest, renovate, synergy, upperRoom, tearfund, gems, pebbles,
                 steppingStones, groundBreakers, rockSolid, boulderGang, fridge,
                 clubOne, thirst, flava, sportsField, ourPlace, foodCourt, marketPlace,
                 medicalCentre, newWineFM, carParkDay, carParkMain, runway,
                 bay1, bay2, bay3, bay4, bay5, endOfShedsBB, redGateBB, wigWamBB,
                 admin, controlOffice, finance, information, production, kitchen,
                 teamLounge, general, freezerPacks, hospitality, ticketOfficeBlue,
                 ticketOfficeRed, bandStand, fencing, pond);
 
         int i = 0;
         while (i < listOfLists.size())
         {
             if(fieldsBox3.get(i).substring(4).equals("NA") && i < 44)
             {
                 int marker = i - 29;
                 masterListBox4.put(fieldsBox2.get(marker) + "-" + fieldsBox3.get(i), listOfLists.get(i));
                 i++;
             }
             else if(fieldsBox3.get(i).substring(4).equals("NA") && (i > 44 && i < 77))
             {
                 int marker = i - 54;
                 masterListBox4.put(fieldsBox2.get(marker) + "-" + fieldsBox3.get(i), listOfLists.get(i));
                 i++;
             }
             else if(fieldsBox3.get(i).substring(4).equals("NA") && (i > 91))
             {
                 int marker = i - 67;
                 masterListBox4.put(fieldsBox2.get(marker) + "-" + fieldsBox3.get(i), listOfLists.get(i));
                 i++;
             }
             else
             {
                 masterListBox4.put(fieldsBox3.get(i), listOfLists.get(i));
                 i++;
 
             }
         }
     }
     
     private HashMap<String, String> generateTeamLevelPasswords()
     {
         HashMap<String,String> passwords = new HashMap<String, String>();
         passwords.put("Control Office", "NWN3Control");
         passwords.put("Information Team", "NWN3Info");
         passwords.put("Administration Team", "NWN3Admin");
         return passwords;
     }
 
     public ArrayList<String> getMasterListBox1()
     {
         return masterListBox1;
     }
 
     public HashMap<String, ArrayList<String>> getMasterListBox2()
     {
         return masterListBox2;
     }
 
     public HashMap<String, ArrayList<String>> getMasterListBox3()
     {
         return masterListBox3;
     }
 
     public HashMap<String, ArrayList<String>> getMasterListBox4()
     {
         return masterListBox4;
     }
     
     public HashMap<String, String> getPasswords()
     {
         return teamLevelPasswords;
     }
     
     
 }
