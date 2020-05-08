 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package system;
 
 import gui.ControlOfficeEntryForm;
 import gui.InitialGUI;
 import gui.MainGUI;
 import io.MYSQLEngine;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 
 /**
  * The highest level of the system as a whole, all major functionality will,
  * at least in principle, be controlled from here.
  * @author jonathanrainer
  */
 
 public class MainSystem {
     
     private MYSQLEngine mysqlEngine;
     private ArrayList<String> teamNames;
     private InitialGUI initialGUI;
     private MainGUI mainGUI;
     private User user;
     private ArrayList<String> masterListBox1;
     private HashMap<String,ArrayList<String>> masterListBox2;
     private HashMap<String,ArrayList<String>> masterListBox3;
     private HashMap<String,ArrayList<String>> masterListBox4;
     
     /**
      * Construct all the components of the Main System and such that it can have
      * control over them.
      */
     public MainSystem()
     {
        mysqlEngine = new MYSQLEngine("localhost", "Nexus","nexus","nexus2713");
        teamNames = mysqlEngine.enumerateTeamNames();
        initialGUI = new InitialGUI(teamNames);
        addActionListenersInitialGUI();
     }
     
     /**
      * Construct the Main System and then set the first GUI window so it's
      * visible.
      * @param args Any command line arguments
      */
     public static void main(String[] args)
     {
         MainSystem mainSystem  = new MainSystem(); 
         mainSystem.getInitialGUI().getWelcomeScreen().getMainFrame().setVisible(true);
     }
     
     /**
      * Add ActionListeners to the GUI elements that require them. All
      * ActionListeners are declared in-line rather than being written as 
      * separate classes. Any larger ActionListeners are declared in separate
      * classes.
      */
     private void addActionListenersInitialGUI()
     {
         /**
          * Add an ActionListener to the continue button on the welcome screen. 
          * Simply moves the system on a stage.
          */
         initialGUI.getWelcomeScreen().getContinueButton().addActionListener
                 (new ActionListener()
         {
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 initialGUI.getWelcomeScreen().getMainFrame().dispose();
                 initialGUI.getTeamSelectionScreen().getMainFrame().setVisible(true);
             }
         }
                 );
         
         /**
          * Add an ActionListener to the continue button on the Team Selection 
          * screen. This creates a user object and then begins to construct the
          * underlying system properly.
          */
         initialGUI.getTeamSelectionScreen().getContinueButton().addActionListener
                 (new ActionListener()
         {
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 user = new User(initialGUI.getTeamSelectionScreen().
                         getTeamComboBox().getSelectedItem().toString());
                 initialGUI.getTeamSelectionScreen().getMainFrame().dispose();
                 mainGUI = new MainGUI(user.getTeam(), teamNames);
                 addActionListenersMainGUI();
             }
         }
                 );
         
         initialGUI.getTeamSelectionScreen().getBackButton().addActionListener(
                 new ActionListener()
                 {
                     @Override
                     public void actionPerformed(ActionEvent e)
                     {
                         initialGUI.getTeamSelectionScreen().getMainFrame().
                                 dispose();
                         initialGUI.getWelcomeScreen().getMainFrame().setVisible
                                 (true);
                     }
                 });
        
     }
     
     private void addActionListenersMainGUI()
     {
         switch(user.getTeam()){
             case "Administration Team":
                 addActionListenersAdminTaskSelection();
                 break;
             case "Information Team":
                 addActionListenersInfoTaskSelection();
                 break;   
             case "Control Office":
                 addActionListenersControlOfficeTaskSelection();
                 break;    
         }
     }
     
     private void addActionListenersAdminTaskSelection() {
         mainGUI.getTaskSelectionScreen().getButtons().
                 get("writeNewCOTicketButton").addActionListener(
                 new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 ControlOfficeEntryForm cofe = createLimitedEntryForm();
                 
             }
         });
     }
     
     private void addActionListenersInfoTaskSelection(){
         mainGUI.getTaskSelectionScreen().getButtons().
                 get("writeNewCOTicketButton").addActionListener(
                 new ActionListener()
                 {
                     @Override
                     public void actionPerformed(ActionEvent e)
                     {
                         System.out.println("Place Holder");
                     }
                 }
                 );
     }
     
     private void addActionListenersControlOfficeTaskSelection(){
         mainGUI.getTaskSelectionScreen().getButtons().
                 get("writeNewCOTicketButton").addActionListener(
                 new ActionListener()
                 {
                     @Override
                     public void actionPerformed(ActionEvent e)
                     {
                         System.out.println("Place Holder");
                     }
                 }
                 );
     }
     
     /**
      * Create the form, with some data prefilled, so that it can be entered into
      * the database.
      * @return The form ready to be filled in
      */
     private ControlOfficeEntryForm createLimitedEntryForm()
     {
         final ControlOfficeEntryForm cofe = new ControlOfficeEntryForm(true);
         //Set the text in the two automated fields and make them unwriteable
         cofe.getTicketReferenceTextField().setText("Automated");
         cofe.getTicketReferenceTextField().setEditable(false);
         cofe.getDateTimeTextField().setText("Automated");
         cofe.getDateTimeTextField().setEditable(false);
         
         //Add to the ticket written combo box with the team the user is from
         cofe.getTicketWrittenComboBox().addItem(user.getTeam());
         cofe.getTicketWrittenComboBox().setSelectedIndex(0);
         cofe.getTicketWrittenComboBox().setEnabled(false);
         
         //Iterate over the team members to give a choice
         ArrayList<String> teamMembers = mysqlEngine.
                 enumerateTeamMembers(user.getTeam());
         Iterator<String> it1 = teamMembers.iterator();
         while (it1.hasNext()){
             cofe.getTeamMembersComboBox().addItem(it1.next());
         }
         
         //Generate master lists
         masterListBox1 = new ArrayList<>();
        Collections.addAll(masterListBox1, "Village", "Gate", "Venue", "Site", "ShowGround");
         Iterator it2 = masterListBox1.iterator();
         cofe.getProblemLocationComboBox1().addItem("Select a Location");
         while(it2.hasNext()){
             cofe.getProblemLocationComboBox1().addItem(it2.next());
         }
         generateMasterLists();
         //Create HashMap for ComboBox 2 it should link together the string
         //selected in the previous box with the new set of strings for the next
         //combo box.
         
             
         cofe.getProblemLocationComboBox1().addActionListener(
                 new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 if(e.getActionCommand().equals("comboBoxChanged") &&
                         !(cofe.getProblemLocationComboBox1().getSelectedItem().
                         equals("Select a Location"))){
                     ArrayList<String> secondBoxOptions = masterListBox2.get
                             (cofe.getProblemLocationComboBox1().getSelectedItem());
                     cofe.getProblemLocationComboBox2().removeAllItems();
                     cofe.getProblemLocationComboBox3().removeAllItems();
                     cofe.getProblemLocationComboBox4().removeAllItems();
                     Iterator<String> secondBoxIterator = secondBoxOptions.iterator();
                     cofe.getProblemLocationComboBox2().addItem("Select a Location");
                     while(secondBoxIterator.hasNext()){
                         cofe.getProblemLocationComboBox2().addItem(secondBoxIterator.next());
                     }
                     cofe.getProblemLocationComboBox2().setSelectedIndex(0);
                 }
             }
         });
         
         cofe.getProblemLocationComboBox2().addActionListener(
                 new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 if (e.getActionCommand().equals("comboBoxChanged")) {
                     if (!(cofe.getProblemLocationComboBox2().
                             getSelectedItem() == null)
                             && !(cofe.getProblemLocationComboBox2().
                             getSelectedItem().equals("Select a Location"))) {
                         ArrayList<String> thirdBoxOptions = masterListBox3.get(
                                 cofe.getProblemLocationComboBox2().getSelectedItem());
                         cofe.getProblemLocationComboBox3().removeAllItems();
                         cofe.getProblemLocationComboBox4().removeAllItems();
                         Iterator<String> thirdBoxIterator = thirdBoxOptions.iterator();
                        if (!(cofe.getProblemLocationComboBox1().getSelectedItem().equals("Gate"))) {
                             cofe.getProblemLocationComboBox3().addItem("Select a Location");
                         }
                         while (thirdBoxIterator.hasNext()) {
                             cofe.getProblemLocationComboBox3().addItem(thirdBoxIterator.next());
                         }
 
                     }
 
                 }
             }
         });
         
         return cofe;
         
     }
     
     private ArrayList<String> generateMasterListBox2(ArrayList<String> fieldsBox1)
     {
         masterListBox2 = new HashMap<>();
         // Village Colour ArrayList
         ArrayList<String> villageColour = new ArrayList<>();
         Collections.addAll(villageColour, "Blue", "Green", "Purple", "Red", "Yellow");
         //Gate Colour ArrayList (To differentiate gates from Villages,
         //there are spaces at the end of common names
         ArrayList<String> gateColour = new ArrayList<>();
         Collections.addAll(gateColour, "Blue (Outer)", "Blue (Inner)", "Green ",
                 "White (Outer)", "White (Inner)", "Red ", "Yellow ", "H.F.G",
                 "Black (Ped)", "Brown (Ped)");
         //Venue Type ArrayList
         ArrayList<String> venueType = new ArrayList<>();
         Collections.addAll(venueType, "Grown Ups", "Children/Youth", "General");
         // Site ArrayList
         ArrayList<String> site = new ArrayList<>();
         Collections.addAll(site, "Car Park (Day)", "Car Park (Main)", "Runway",
                "Bays", "BunkerBins", "Freezer Packs", "Hospitality", "Kitchens"
                , "Ticket Office (Blue)", "Ticket Office (Red)");
         
         ArrayList<ArrayList<String>> listOfLists = new ArrayList<>();
         Collections.addAll(listOfLists, villageColour, gateColour, venueType, 
                 site);
         
         ArrayList<String> listOfAllPotentialItems = new ArrayList<>();
         
         int i = 0;
         while(i < listOfLists.size())
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
         Collections.addAll(blue, "1","2","3","4","5","6","7","8","9","10");
         ArrayList<String> green = new ArrayList<>();
         Collections.addAll(green, "1","2","3","4","5","6","7");
         ArrayList<String> purple = new ArrayList<>();
         Collections.addAll(purple, "1","2","3","4");
         ArrayList<String> red = new ArrayList<>();
         Collections.addAll(red, "1","2","3","4","5","6","7");
         ArrayList<String> yellow = new ArrayList<>();
         Collections.addAll(yellow, "0","1","2","3","4","5","6");
         
         //Gates Options
         ArrayList<String> nextBox = new ArrayList<>();
         nextBox.add("See Next Box -->");
         
         //Adults options
         ArrayList<String> adultsVenues = new ArrayList<>();
         Collections.addAll(adultsVenues, "Meeting Place (inc. Café)",
                 "Impact", "Burn", "Leaders' Lounge", "Pastoral Prayer",
                 "Sanctuary", "Arts 3:16", "Manifest", "Renovate", "Synergy",
                 "Upper Room", "Tearfun (inc. Café)");
         //Childrens and Youth Options
         ArrayList<String> childrensYouthVenues = new ArrayList<>();
         Collections.addAll(childrensYouthVenues, "Gems", "Pebbles", 
                 "Stepping Stones", "Ground Breakers", "Rock Solid",
                 "Boulder Gang", "Fridge", "Club One", "Thirst", "Flava",
                 "Sports Field", "Our Place");
         //General Venues Options
         ArrayList<String> generalVenues = new ArrayList<>();
         Collections.addAll(generalVenues, "Food Court", "Marketplace (inc. Café)",
                 "Medical Centre", "New Wine FM");
         
         //Site Options
         ArrayList<String> carParkDaySite = new ArrayList<>();
         Collections.addAll(carParkDaySite, "NA");
         ArrayList<String> carParkMainSite = new ArrayList<>();
         Collections.addAll(carParkMainSite, "NA");
         ArrayList<String> runwaySite = new ArrayList<>();
         Collections.addAll(runwaySite, "NA");
         ArrayList<String> baysSite = new ArrayList<>();
         Collections.addAll(baysSite, "Bay 1", "Bay 2", "Bay 3", "Bay 4", "Bay 5");
         ArrayList<String> bunkerBinsSite = new ArrayList<>();
         Collections.addAll(bunkerBinsSite, "End of Sheds", "Red Gate", "WigWam");
         ArrayList<String> freezerPacksSite = new ArrayList<>();
         Collections.addAll(freezerPacksSite, "NA");
         ArrayList<String> hallamBuildingSite = new ArrayList<>();
         Collections.addAll(hallamBuildingSite, "Admin", "Control Office", 
                 "Finance", "Information", "Production", "Kitchen", "Team Lounge"
                 , "General");
         ArrayList<String> hospitalitySite = new ArrayList<>();
         Collections.addAll(hospitalitySite, "NA");
         ArrayList<String> kitchensSite = new ArrayList<>();
         Collections.addAll(kitchensSite, "NA");
         ArrayList<String> ticketOfficeBlueSite = new ArrayList<>();
         Collections.addAll(ticketOfficeBlueSite, "NA");
         ArrayList<String> ticketOfficeRedSite = new ArrayList<>();
         Collections.addAll(ticketOfficeRedSite, "NA");
         ArrayList<String> BandStandSite = new ArrayList<>();
         Collections.addAll(BandStandSite, "NA");
         ArrayList<String> fencingSite = new ArrayList<>();
         Collections.addAll(fencingSite, "NA");
         ArrayList<String> pondSite = new ArrayList<>();
         Collections.addAll(pondSite, "NA");
         
         ArrayList<ArrayList<String>> listOfLists = new ArrayList<>();
         Collections.addAll(listOfLists, blue, green, purple, red, yellow,
                 nextBox, nextBox, nextBox, nextBox, nextBox, nextBox,
                 nextBox, nextBox, nextBox, nextBox, adultsVenues, 
                 childrensYouthVenues, generalVenues, carParkDaySite, 
                 carParkMainSite, runwaySite, baysSite, bunkerBinsSite,
                 freezerPacksSite, hallamBuildingSite, hospitalitySite, kitchensSite,
                 ticketOfficeBlueSite, ticketOfficeRedSite, BandStandSite, fencingSite
                 , pondSite);
         
         ArrayList<String> listOfAllPotentialItems = new ArrayList<>();
         
         int i = 0;
        while(i < 15){
             masterListBox3.put(fieldsBox2.get(i), listOfLists.get(i));
             listOfAllPotentialItems.addAll(listOfLists.get(i));
             i++;
         }
         
         return listOfAllPotentialItems;
     }
     
     private void generateMasterLists()
     {
         ArrayList<String> potentialFieldsBox2 = generateMasterListBox2
                 (masterListBox1);
         ArrayList<String> potentialFieldsBox3 = generateMasterListBox3
                 (potentialFieldsBox2);
     }
     
     public InitialGUI getInitialGUI() {
         return initialGUI;
     }
     
     
     
     /**
      * Return the user object from the Main System.
      * @return The User object.
      */
     private User getUser()
     {
         return user;
     }
 }
 
 // //Gates Options
 //        ArrayList<String> blueOuterGate = new ArrayList<>();
 //        Collections.addAll(blueOuterGate, "Toilet (Male)", "Toilet(Female)", "Shower"
 //                , "Elsan Point", "Water Supply", "GF 06", "GF 07", "GF 08",
 //                "GF 09", "GF 10");
 //        ArrayList<String> blueInnerGate = new ArrayList<>();
 //        Collections.addAll(blueInnerGate, "Toilet (Male)", "Toilet(Female)", "Shower"
 //                , "Elsan Point", "Water Supply",  "GF 06", "GF 07", "GF 08",
 //                "GF 09", "GF 10");
 //        ArrayList<String> greenGate = new ArrayList<>();
 //        Collections.addAll(greenGate, "Toilet (Male)", "Toilet(Female)", "Shower"
 //                , "Elsan Point", "Water Supply",  "GF 06", "GF 07", "GF 08",
 //                "GF 09", "GF 10");
 //        ArrayList<String> whiteOuterGate = new ArrayList<>();
 //        Collections.addAll(whiteOuterGate, "Toilet (Male)", "Toilet(Female)", "Shower"
 //                , "Elsan Point", "Water Supply",  "GF 06", "GF 07", "GF 08",
 //                "GF 09", "GF 10");
 //        ArrayList<String> whiteInnerGate = new ArrayList<>();
 //        Collections.addAll(whiteInnerGate, "Toilet (Male)", "Toilet(Female)", "Shower"
 //                , "Elsan Point", "Water Supply",  "GF 06", "GF 07", "GF 08",
 //                "GF 09", "GF 10");
 //        ArrayList<String> redGate = new ArrayList<>();
 //        Collections.addAll(redGate, "Toilet (Male)", "Toilet(Female)", "Shower"
 //                , "Elsan Point", "Water Supply",  "GF 06", "GF 07", "GF 08",
 //                "GF 09", "GF 10");
 //        ArrayList<String> yellowGate = new ArrayList<>();
 //        Collections.addAll(yellowGate, "Toilet (Male)", "Toilet(Female)", "Shower"
 //                , "Elsan Point", "Water Supply",  "GF 06", "GF 07", "GF 08",
 //                "GF 09", "GF 10");
 //        ArrayList<String> hfgGate = new ArrayList<>();
 //        Collections.addAll(hfgGate, "Toilet (Male)", "Toilet(Female)", "Shower"
 //                , "Elsan Point", "Water Supply",  "GF 06", "GF 07", "GF 08",
 //                "GF 09", "GF 10");
 //        ArrayList<String> blackPedGate = new ArrayList<>();
 //        Collections.addAll(blackPedGate, "Toilet (Male)", "Toilet(Female)", "Shower"
 //                , "Elsan Point", "Water Supply",  "GF 06", "GF 07", "GF 08",
 //                "GF 09", "GF 10");
 //        ArrayList<String> brownPedGate = new ArrayList<>();
 //        Collections.addAll(brownPedGate, "Toilet (Male)", "Toilet(Female)", "Shower"
 //                , "Elsan Point", "Water Supply",  "GF 06", "GF 07", "GF 08",
 //                "GF 09", "GF 10");
