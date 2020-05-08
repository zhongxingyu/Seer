 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package system;
 
 import gui.ControlOfficeEntryForm;
 import gui.DashBoard;
 import gui.InitialGUI;
 import gui.MainGUI;
 import gui.ResultsBox;
 import gui.Template;
 import io.RemoteInterfaceEngine;
 import io.MYSQLEngine;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import javax.swing.BorderFactory;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPasswordField;
 import javax.swing.JRadioButton;
 import javax.swing.JTextArea;
 import javax.swing.ListModel;
 import javax.swing.border.Border;
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeComparator;
 
 /**
  * The highest level of the system as a whole, all major functionality will, at
  * least in principle, be controlled from here.
  *
  * @author jonathanrainer
  */
 public class MainSystem
 {
 
     private MYSQLEngine mysqlEngine;
     private ArrayList<String> teamNames;
     private InitialGUI initialGUI;
     private MainGUI mainGUI;
     private User user;
     private DataStructures dataStructures;
     private RemoteInterfaceEngine remoteInterfaceEngine;
     private static final String DATEFORMAT = "dd/MM/yyyy HH:mm";
     private static final int UPDATEHOURS = 2;
 
     /**
      * Construct all the components of the Main System and such that it can have
      * control over them.
      */
     public MainSystem()
     {
         mysqlEngine = new MYSQLEngine("192.168.8.5", "Nexus", "nexususer", 
                 "nexuspassword");
         //mysqlEngine = new MYSQLEngine("localhost", "Nexus", "root", "");
         teamNames = mysqlEngine.enumerateTeamNames();
         initialGUI = new InitialGUI(teamNames);
         dataStructures = new DataStructures();
         remoteInterfaceEngine = new RemoteInterfaceEngine();
         addActionListenersInitialGUI(); 
 
     }
 
     /**
      * Construct the Main System and then set the first GUI window so it's
      * visible.
      *
      * @param args Any command line arguments
      */
     public static void main(String[] args)
     {
         MainSystem mainSystem = new MainSystem();
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
         initialGUI.getWelcomeScreen().getContinueButton().addActionListener(new ActionListener()
         {
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 initialGUI.getWelcomeScreen().getMainFrame().dispose();
                 initialGUI.getTeamSelectionScreen().getMainFrame().setVisible(true);
             }
         });
         
 
         /**
          * Add an ActionListener to the continue button on the Team Selection
          * screen. This creates a user object and then begins to construct the
          * underlying system properly.
          */
         initialGUI.getTeamSelectionScreen().getContinueButton().addActionListener(new ActionListener()
         {
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 user = new User(initialGUI.getTeamSelectionScreen().
                             getTeamComboBox().getSelectedItem().toString());
                 String password = "";
                 JPasswordField pf = new JPasswordField();
                 int okCxl = JOptionPane.showConfirmDialog(null, pf, "Please enter your password", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
 
                 if (okCxl == JOptionPane.OK_OPTION) 
                 {
                      password = new String(pf.getPassword());
                 }
                 if(!(password.equals(dataStructures.getPasswords().get(user.getTeam()))))
                 {
                     JOptionPane.showMessageDialog(initialGUI.
                             getTeamSelectionScreen().getMainFrame(), "Password did not match.");
                     user = null;
                 }
                 else
                 {
                     initialGUI.getTeamSelectionScreen().getMainFrame().dispose();
                     mainGUI = new MainGUI(user.getTeam(), teamNames);
                     addActionListenersMainGUI();
                 }
             }
         });
 
         initialGUI.getTeamSelectionScreen().getBackButton().addActionListener(
                 new ActionListener()
         {
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 initialGUI.getTeamSelectionScreen().getMainFrame().
                         dispose();
                 initialGUI.getWelcomeScreen().getMainFrame().setVisible(true);
             }
         });
         
         
 
     }
 
     private void addActionListenersMainGUI()
     {
         switch (user.getTeam())
         {
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
 
     private void addActionListenersAdminTaskSelection()
     {
         mainGUI.getTaskSelectionScreen().getButtons().
                 get("writeNewCOTicketButton").addActionListener(
                 new ActionListener()
         {
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 ControlOfficeEntryForm cofe = createLimitedEntryForm();
 
             }
         });
 
         mainGUI.getTaskSelectionScreen().getButtons().get("Logout").
                 addActionListener(
                 new ActionListener()
         {
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 mainGUI.getTaskSelectionScreen().getMainFrame().dispose();
                 user = null;
                 initialGUI.getTeamSelectionScreen().getMainFrame().setVisible(true);
             }
         });
     }
 
     private void addActionListenersInfoTaskSelection()
     {
         mainGUI.getTaskSelectionScreen().getButtons().
                 get("writeNewCOTicketButton").addActionListener(
                 new ActionListener()
         {
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 ControlOfficeEntryForm cofe = createLimitedEntryForm();
             }
         });
 
         mainGUI.getTaskSelectionScreen().getButtons().get("Logout").
                 addActionListener(
                 new ActionListener()
         {
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 mainGUI.getTaskSelectionScreen().getMainFrame().dispose();
                 user = null;
                 initialGUI.getTeamSelectionScreen().getMainFrame().setVisible(true);
             }
         });
     }
 
     private void addActionListenersControlOfficeTaskSelection()
     {
         mainGUI.getTaskSelectionScreen().getButtons().
                 get("writeNewCOTicketButton").addActionListener(
                 new ActionListener()
         {
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 ControlOfficeEntryForm cofe = createControlEntryForm();
             }
         });
 
         mainGUI.getTaskSelectionScreen().getButtons().
                 get("viewAmendCOTicketButton").addActionListener(
                 new ActionListener()
         {
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 String ticketID = JOptionPane.showInputDialog(
                         mainGUI.getTaskSelectionScreen().getMainFrame(),
                         "Please enter the Ticket ID of the Job Ticket you wish to"
                         + "update.", "Update/Amend Ticket Search",
                         JOptionPane.QUESTION_MESSAGE);
                 createUpdateAmendEntryForm(ticketID, "tickets");
             }
         });
 
         mainGUI.getTaskSelectionScreen().getButtons().get("Logout").
                 addActionListener(
                 new ActionListener()
         {
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 mainGUI.getTaskSelectionScreen().getMainFrame().dispose();
                 user = null;
                 initialGUI.getTeamSelectionScreen().getMainFrame().setVisible(true);
             }
         });
 
         mainGUI.getTaskSelectionScreen().getMainFrame().
                 getJMenuBar().getMenu(0).getItem(0).addActionListener(new ActionListener()
         {
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 final Ticket[] duplicateTickets = mysqlEngine.getDuplicates();
                 final ResultsBox resultsBox = new ResultsBox(duplicateTickets, "Duplicate");
                 resultsBox.getResultsArea().addMouseListener(new MouseAdapter()
                 {
                     @Override
                     public void mouseClicked(MouseEvent evt)
                     {
                         if (evt.getSource().equals(resultsBox.getResultsArea()))
                         {
                             if (evt.getClickCount() == 1)
                             {
                                 int index = resultsBox.
                                         getResultsArea().
                                         locationToIndex(evt.getPoint());
                                 Ticket[] duplicateSources = mysqlEngine.
                                         getTicketsThisTicketDuplicates(duplicateTickets[index]);
                                 resultsBox.getDuplicateResultsArea().setListData(duplicateSources);
                             }
                             if (evt.getClickCount() == 2)
                             {
                                 int index = resultsBox.
                                         getResultsArea().
                                         locationToIndex(evt.getPoint());
                                 ControlOfficeEntryForm cofeAmend =
                                         createUpdateAmendEntryForm("" + duplicateTickets[index].getJobRefId(), "duplicateQueue");
                                 cofeAmend.getSubmitFormButton().setEnabled(false);
                                 cofeAmend.getResetFormButton().setEnabled(false);
 
                             }
                         }
                     }
                 });
                 resultsBox.getDuplicateResultsArea().addMouseListener(new MouseAdapter()
                 {
                     @Override
                     public void mouseClicked(MouseEvent evt)
                     {
                     if (evt.getSource().equals(resultsBox.getDuplicateResultsArea()))
                         {
                             if (evt.getClickCount() == 2)
                             {
                                 int index = resultsBox.
                                         getResultsArea().
                                         locationToIndex(evt.getPoint());
                                 if(index < duplicateTickets.length)
                                 {
                                     ControlOfficeEntryForm cofeAmend =
                                         createUpdateAmendEntryForm("" + 
                                             duplicateTickets[index].getJobRefId(),
                                             "Tickets");
                                 cofeAmend.getSubmitFormButton().setEnabled(false);
                                 cofeAmend.getResetFormButton().setEnabled(false);
                                 }
 
                             }
                         }
                     }
                 });
                 resultsBox.getExitButton().addActionListener(new ActionListener()
                 {
                     public void actionPerformed(ActionEvent e)
                     {
                         resultsBox.getMainFrame().dispose();
                     }
                 });
                 resultsBox.getSubmitButton().addActionListener(new ActionListener()
                 {
                     public void actionPerformed(ActionEvent e)
                     {
                         ArrayList<Ticket> duplicatesArrayList = new ArrayList<>();
                         ArrayList<Ticket> nonDuplicatesArrayList = new ArrayList<>();
                         int i = 0;
                         int j = 0;
                         List<Ticket> duplicates = 
                                 resultsBox.getResultsArea().getSelectedValuesList();
                         ListModel<Ticket> allValues = resultsBox.getResultsArea().getModel();
                         while(i < duplicates.size())
                         {
                             duplicatesArrayList.add(duplicates.get(i));
                             i++;
                         }
                         while(j < allValues.getSize())
                         {
                             nonDuplicatesArrayList.add(allValues.getElementAt(j));
                             j++;
                         }
                         Iterator<Ticket> it1 = duplicatesArrayList.iterator();
                         while(it1.hasNext())
                         {
                             Ticket duplicateTicketToConsider = it1.next();
                             if(nonDuplicatesArrayList.contains(duplicateTicketToConsider))
                             {
                                 nonDuplicatesArrayList.remove(duplicateTicketToConsider);
                             }
                         }
                         mysqlEngine.markTicketsDuplicate(duplicatesArrayList);
                         mysqlEngine.markTicketsNonDuplicates(nonDuplicatesArrayList);
                         i = 0;
                         String duplicateIDs = "";
                         if(duplicatesArrayList.size() > 0)
                         {
                             while (i < duplicatesArrayList.size())
                             {
                                 duplicateIDs = duplicateIDs + "" + duplicatesArrayList.get(i).getJobRefId() + ", ";
                                 i++;
                             }
                             duplicateIDs = duplicateIDs.substring(0, duplicateIDs.length()-2);
                         }
                         j = 0;
                         String nonDuplicateIDs = "";
                         if(nonDuplicatesArrayList.size() > 0)
                         {
                             while (j < nonDuplicatesArrayList.size())
                                 {
                                     nonDuplicateIDs = nonDuplicateIDs + "" + nonDuplicatesArrayList.get(j).getJobRefId() + ", ";
                                     j++;
                                 }
                                 nonDuplicateIDs = nonDuplicateIDs.substring(0, nonDuplicateIDs.length() - 2);
                         }
                         JOptionPane.showMessageDialog(resultsBox.getMainFrame(), 
                                 "Tickets Successfully Marked as Duplicates \n "
                                 + "IDs: " + duplicateIDs + "\n" +
                                 "Tickets Successfully Marked as Non-Duplicates \n "
                                 + "IDs: " + nonDuplicateIDs + "\n", 
                                 "Success", JOptionPane.OK_OPTION);
                         resultsBox.getMainFrame().dispose();
                     }
                 });
             }
             
         });
        
         mainGUI.getTaskSelectionScreen().getMainFrame().getJMenuBar().getMenu(0).
                 getItem(1).addActionListener(new ActionListener()
         {
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 final Ticket[] unprintedTickets = mysqlEngine.getUnprinted();
                 final ResultsBox resultsBox = new ResultsBox(unprintedTickets, "Unprinted");
                 resultsBox.getResultsArea().addMouseListener(new MouseAdapter()
                 {
                     @Override
                     public void mouseClicked(MouseEvent evt)
                     {
                         if (evt.getSource().equals(resultsBox.getResultsArea()))
                         {
                             if (evt.getClickCount() == 2)
                             {
                                 int index = resultsBox.
                                         getResultsArea().
                                         locationToIndex(evt.getPoint());
                                 ControlOfficeEntryForm cofeAmend =
                                         createUpdateAmendEntryForm("" + 
                                         unprintedTickets[index].getJobRefId(), "Tickets");
                             }
                         }
                     }
                 });
 
 
             }
         });
         
         mainGUI.getTaskSelectionScreen().getMainFrame().getJMenuBar().getMenu(0).getItem(0).
                 addActionListener(new ActionListener()
                 {
                     @Override
                     public void actionPerformed(ActionEvent e)
                     {
                         DashBoard dashBoard = new DashBoard(mysqlEngine);
                     }
                     
                 });
 
     }
 
     /**
      * Create the form, with some data prefilled, so that it can be entered into
      * the database.
      *
      * @return The form ready to be filled in
      */
     private ControlOfficeEntryForm createLimitedEntryForm()
     {
         final ControlOfficeEntryForm cofe = new ControlOfficeEntryForm(true, user);
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
         cofe.getTeamMembersComboBox().addItem("Choose One");
         Iterator<String> it1 = teamMembers.iterator();
         while (it1.hasNext())
         {
             cofe.getTeamMembersComboBox().addItem(it1.next());
         }
 
         Iterator it2 = dataStructures.getMasterListBox1().iterator();
         cofe.getProblemLocationComboBox1().addItem("Select a Location");
         while (it2.hasNext())
         {
             cofe.getProblemLocationComboBox1().addItem(it2.next());
         }
         //Create HashMap for ComboBox 2 it should link together the string
         //selected in the previous box with the new set of strings for the next
         //combo box.
 
 
         cofe.getProblemLocationComboBox1().addActionListener(
                 new ActionListener()
         {
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 if (e.getActionCommand().equals("comboBoxChanged")
                         && !(cofe.getProblemLocationComboBox1().getSelectedItem().
                         equals("Select a Location")))
                 {
                     ArrayList<String> secondBoxOptions = dataStructures.getMasterListBox2().
                             get(cofe.getProblemLocationComboBox1().getSelectedItem().toString());
                     cofe.getProblemLocationComboBox2().removeAllItems();
                     cofe.getProblemLocationComboBox3().removeAllItems();
                     cofe.getProblemLocationComboBox4().removeAllItems();
                     Iterator<String> secondBoxIterator = secondBoxOptions.iterator();
                     cofe.getProblemLocationComboBox2().addItem("Select a Location");
                     while (secondBoxIterator.hasNext())
                     {
                         cofe.getProblemLocationComboBox2().addItem(secondBoxIterator.next());
                     }
                     cofe.getProblemLocationComboBox2().setSelectedIndex(0);
                 } else if (cofe.getProblemLocationComboBox1().getSelectedItem().
                         equals("Select a Location"))
                 {
                     cofe.getProblemLocationComboBox2().removeAllItems();
                     cofe.getProblemLocationComboBox3().removeAllItems();
                     cofe.getProblemLocationComboBox4().removeAllItems();
                 }
             }
         });
 
         cofe.getProblemLocationComboBox2().addActionListener(
                 new ActionListener()
         {
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 if (e.getActionCommand().equals("comboBoxChanged")
                         && !(cofe.getProblemLocationComboBox2().getSelectedItem() == null)
                         && !(cofe.getProblemLocationComboBox2().getSelectedItem().equals("Select a Location")))
                 {
                     ArrayList<String> thirdBoxOptions = dataStructures.getMasterListBox3().get(
                             cofe.getProblemLocationComboBox2().getSelectedItem().toString());
                     cofe.getProblemLocationComboBox3().removeAllItems();
                     cofe.getProblemLocationComboBox4().removeAllItems();
                     Iterator<String> thirdBoxIterator = thirdBoxOptions.iterator();
                     if (!(thirdBoxOptions.size() == 1))
                     {
                         cofe.getProblemLocationComboBox3().addItem("Select a Location");
                     }
                     while (thirdBoxIterator.hasNext())
                     {
                         cofe.getProblemLocationComboBox3().addItem(thirdBoxIterator.next());
                     }
 
                 }
                 if (!(cofe.getProblemLocationComboBox2().getSelectedItem() == null)
                         && (cofe.getProblemLocationComboBox2().getSelectedItem().equals("Select a Location")))
                 {
                     cofe.getProblemLocationComboBox3().removeAllItems();
                     cofe.getProblemLocationComboBox4().removeAllItems();
                 }
             }
         });
 
         cofe.getProblemLocationComboBox3().addActionListener(
                 new ActionListener()
         {
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 if (e.getActionCommand().equals("comboBoxChanged")
                         && !(cofe.getProblemLocationComboBox3().getSelectedItem() == null
                         || cofe.getProblemLocationComboBox3().getSelectedItem().equals("Select a Location")))
                 {
                     ArrayList<String> fourthBoxOptions;
                     if (cofe.getProblemLocationComboBox3().getSelectedItem().equals("NA"))
                     {
                         fourthBoxOptions =
                                 dataStructures.getMasterListBox4().get(cofe.
                                 getProblemLocationComboBox2().
                                 getSelectedItem() + "-"
                                 + cofe.getProblemLocationComboBox3().
                                 getSelectedItem());
                     } else
                     {
                         fourthBoxOptions =
                                 dataStructures.getMasterListBox4().get(cofe.getProblemLocationComboBox3()
                                 .getSelectedItem().toString());
                     }
                     cofe.getProblemLocationComboBox4().removeAllItems();
                     Iterator<String> fourthBoxIterator =
                             fourthBoxOptions.iterator();
                     if (!(fourthBoxOptions.size() == 1))
                     {
                         cofe.getProblemLocationComboBox4().addItem("Select a Facility");
                     }
                     while (fourthBoxIterator.hasNext())
                     {
                         cofe.getProblemLocationComboBox4().addItem(fourthBoxIterator.next());
                     }
                 }
                 if (!(cofe.getProblemLocationComboBox3().getSelectedItem() == null)
                         && (cofe.getProblemLocationComboBox3().getSelectedItem().equals("Select a Location")))
                 {
                     cofe.getProblemLocationComboBox4().removeAllItems();
                 }
 
             }
         });
 
         cofe.getWhoIsAComboBox().addItem("Who Is A?");
         cofe.getWhoIsAComboBox().addItem("Delegate");
         cofe.getWhoIsAComboBox().addItem("Speaker");
         cofe.getWhoIsAComboBox().addItem("Team Member");
 
         cofe.getContactViaComboBox().addItem("Choose One");
         cofe.getContactViaComboBox().addItem("Mobile");
         cofe.getContactViaComboBox().addItem("Radio");
         cofe.getContactViaComboBox().addItem("Village Host");
         cofe.getContactViaComboBox().addItem("Village");
         cofe.getContactViaComboBox().addItem("Not Required");
 
         cofe.getContactViaComboBox().addActionListener(new ActionListener()
         {
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 if (e.getActionCommand().equals("comboBoxChanged")
                         && cofe.getContactViaComboBox().getSelectedItem().equals("Not Required"))
                 {
                     cofe.getContactNumberTextField().setText(
                             cofe.getContactViaComboBox().
                             getSelectedItem().toString());
                 } else
                 {
                     cofe.getContactNumberTextField().setText("");
                 }
             }
         });
 
         cofe.getLocationVenueVillageComboBox().addItem("Choose One");
         cofe.getLocationVenueVillageComboBox().addItem("Location");
         cofe.getLocationVenueVillageComboBox().addItem("Venue");
         cofe.getLocationVenueVillageComboBox().addItem("Village");
         cofe.getLocationVenueVillageComboBox().addItem("Not Required");
 
         cofe.getSubmitFormButton().addActionListener(
                 new ActionListener()
         {
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 String team = user.getTeam();
                 user.setName(cofe.getTeamMembersComboBox().getSelectedItem().toString());
                 String member = user.getName();
                 String problemLocation = cofe.
                         getProblemLocationComboBox1().getSelectedItem()
                         + "-" + cofe.getProblemLocationComboBox2().getSelectedItem()
                         + "-" + cofe.getProblemLocationComboBox3().getSelectedItem()
                         + "-" + cofe.getProblemLocationComboBox4().getSelectedItem();
                 String problemDescription = cofe.getProblemDescriptionTextArea().getText();
                 Iterator<JRadioButton> buttonsIterator = cofe.getButtonsInGrid().iterator();
                 ArrayList<String> keyWords = new ArrayList<>();
                 while (buttonsIterator.hasNext())
                 {
                     JRadioButton buttonUnderConsideration = buttonsIterator.next();
                     if (buttonUnderConsideration.isSelected())
                     {
                         keyWords.add(buttonUnderConsideration.getName());
                     }
                 }
                 String problemReportedBy = cofe.getProblemReportedByTextField().getText();
                 String whoIsA = cofe.getWhoIsAComboBox().getSelectedItem().toString();
                 String contactVia = cofe.getContactViaComboBox().getSelectedItem().toString();
                 String contactNumber = cofe.getContactNumberTextField().getText();
                 String locationVenueVillage = (String) cofe.getLocationVenueVillageComboBox().
                         getSelectedItem().toString();
                 Ticket ticket = new Ticket(0, null, user,
                         problemLocation, problemDescription, keyWords,
                         problemReportedBy, whoIsA, contactVia, contactNumber,
                         locationVenueVillage, "Low", false, null, "Issue Reported", null,
                         null, null, null, null, null);
                 String validationResult = ticket.dataValidationEntry();
                 if (validationResult.equals("Passed"))
                 {
                     if (!(mysqlEngine.isTicketDuplicate(ticket)))
                     {
                         if (mysqlEngine.submitTicket(ticket, false))
                         {
                             JOptionPane.showMessageDialog(cofe.getMainFrame(),
                                     "The Job Ticket has been submitted. \n"
                                     + "The Job ID is: " + ticket.getJobRefId()
                                     + "\nIt was submitted at: " + ticket.getDateTime().toString(DATEFORMAT));
                             cofe.getMainFrame().dispose();
                         } else
                         {
                             JOptionPane.showMessageDialog(cofe.getMainFrame(), "An error has occured. \n"
                                     + "Don't close this window and report to Jonathan Rainer (Admin)"
                                     + "or Ian Walker for assistance.");
                         }
                     } else
                     {
                         if(mysqlEngine.submitTicket(ticket, true))
                         {
                             JOptionPane.showMessageDialog(cofe.getMainFrame(),
                                     "The Job Ticket you submitted has been "
                                     + "flagged as a duplicate. Control Office will advise. \n"
                                     + "The Temporary Job ID is: " + ticket.getJobRefId()
                                     + "\n It was submitted at: " + ticket.getDateTime().toString(DATEFORMAT));
                             cofe.getMainFrame().dispose();
                         }
                     }
                 } else
                 {
                     JOptionPane.showMessageDialog(cofe.getMainFrame(), validationResult);
                 }
             }
         });
 
         cofe.getResetFormButton().addActionListener(
                 new ActionListener()
         {
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 cofe.getTicketWrittenComboBox().setSelectedIndex(0);
                 cofe.getProblemLocationComboBox1().setSelectedIndex(0);
                 cofe.getProblemDescriptionTextArea().setText("");
                 Iterator<JRadioButton> buttonsIterator = cofe.getButtonsInGrid().iterator();
                 while (buttonsIterator.hasNext())
                 {
                     buttonsIterator.next().setSelected(false);
                 }
                 cofe.getProblemReportedByTextField().setText("");
                 cofe.getWhoIsAComboBox().setSelectedIndex(0);
                 cofe.getContactViaComboBox().setSelectedIndex(0);
                 cofe.getContactNumberTextField().setText("");
                 cofe.getLocationVenueVillageComboBox().setSelectedIndex(0);
             }
         });
 
         return cofe;
     }
 
     private ControlOfficeEntryForm createControlEntryForm()
     {
         final ControlOfficeEntryForm cofe = new ControlOfficeEntryForm(true, user);
         JMenu fileMenu = cofe.getMainFrame().getJMenuBar().getMenu(0);
         JMenuItem printAndSubmit = new JMenuItem("Print & Submit");
         printAndSubmit.addActionListener(new ActionListener()
         {
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 String ticketID = "" + submitTicket(cofe);
                 Ticket ticket = mysqlEngine.retrieveTicket(ticketID, "Tickets");
                 if (ticket.dataValidationEntry().equals("Passed")
                         && ticket.printingValidation().equals("Passed"))
                 {
                     printTicket(ticket);
                 }
             }
         });
         fileMenu.add(printAndSubmit, 0);
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
         cofe.getTeamMembersComboBox().addItem("Choose One");
         Iterator<String> it1 = teamMembers.iterator();
         while (it1.hasNext())
         {
             cofe.getTeamMembersComboBox().addItem(it1.next());
         }
 
         Iterator it2 = dataStructures.getMasterListBox1().iterator();
         cofe.getProblemLocationComboBox1().addItem("Select a Location");
         while (it2.hasNext())
         {
             cofe.getProblemLocationComboBox1().addItem(it2.next());
         }
         //Create HashMap for ComboBox 2 it should link together the string
         //selected in the previous box with the new set of strings for the next
         //combo box.
 
 
         cofe.getProblemLocationComboBox1().addActionListener(
                 new ActionListener()
         {
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 if (e.getActionCommand().equals("comboBoxChanged")
                         && !(cofe.getProblemLocationComboBox1().getSelectedItem().
                         equals("Select a Location")))
                 {
                     ArrayList<String> secondBoxOptions = dataStructures.getMasterListBox2().
                             get(cofe.getProblemLocationComboBox1().getSelectedItem().toString());
                     cofe.getProblemLocationComboBox2().removeAllItems();
                     cofe.getProblemLocationComboBox3().removeAllItems();
                     cofe.getProblemLocationComboBox4().removeAllItems();
                     Iterator<String> secondBoxIterator = secondBoxOptions.iterator();
                     cofe.getProblemLocationComboBox2().addItem("Select a Location");
                     while (secondBoxIterator.hasNext())
                     {
                         cofe.getProblemLocationComboBox2().addItem(secondBoxIterator.next());
                     }
                     cofe.getProblemLocationComboBox2().setSelectedIndex(0);
                 } else if (cofe.getProblemLocationComboBox1().getSelectedItem().
                         equals("Select a Location"))
                 {
                     cofe.getProblemLocationComboBox2().removeAllItems();
                     cofe.getProblemLocationComboBox3().removeAllItems();
                     cofe.getProblemLocationComboBox4().removeAllItems();
                 }
             }
         });
 
         cofe.getProblemLocationComboBox2().addActionListener(
                 new ActionListener()
         {
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 if (e.getActionCommand().equals("comboBoxChanged")
                         && !(cofe.getProblemLocationComboBox2().getSelectedItem() == null)
                         && !(cofe.getProblemLocationComboBox2().getSelectedItem().equals("Select a Location")))
                 {
                     ArrayList<String> thirdBoxOptions = dataStructures.getMasterListBox3().get(
                             cofe.getProblemLocationComboBox2().getSelectedItem().toString());
                     cofe.getProblemLocationComboBox3().removeAllItems();
                     cofe.getProblemLocationComboBox4().removeAllItems();
                     Iterator<String> thirdBoxIterator = thirdBoxOptions.iterator();
                     if (!(thirdBoxOptions.size() == 1))
                     {
                         cofe.getProblemLocationComboBox3().addItem("Select a Location");
                     }
                     while (thirdBoxIterator.hasNext())
                     {
                         cofe.getProblemLocationComboBox3().addItem(thirdBoxIterator.next());
                     }
 
                 }
                 if (!(cofe.getProblemLocationComboBox2().getSelectedItem() == null)
                         && (cofe.getProblemLocationComboBox2().getSelectedItem().equals("Select a Location")))
                 {
                     cofe.getProblemLocationComboBox3().removeAllItems();
                     cofe.getProblemLocationComboBox4().removeAllItems();
                 }
             }
         });
 
         cofe.getProblemLocationComboBox3().addActionListener(
                 new ActionListener()
         {
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 if (e.getActionCommand().equals("comboBoxChanged")
                         && !(cofe.getProblemLocationComboBox3().getSelectedItem() == null
                         || cofe.getProblemLocationComboBox3().getSelectedItem().equals("Select a Location")))
                 {
                     ArrayList<String> fourthBoxOptions;
                     if (cofe.getProblemLocationComboBox3().getSelectedItem().toString().substring(4).equals("NA"))
                     {
                         fourthBoxOptions =
                                 dataStructures.getMasterListBox4().get(cofe.
                                 getProblemLocationComboBox2().
                                 getSelectedItem() + "-"
                                 + cofe.getProblemLocationComboBox3().
                                 getSelectedItem());
                     } else
                     {
                         fourthBoxOptions =
                                 dataStructures.getMasterListBox4().get(cofe.getProblemLocationComboBox3()
                                 .getSelectedItem().toString());
                     }
                     cofe.getProblemLocationComboBox4().removeAllItems();
                     Iterator<String> fourthBoxIterator =
                             fourthBoxOptions.iterator();
                     if (!(fourthBoxOptions.size() == 1))
                     {
                         cofe.getProblemLocationComboBox4().addItem("Select a Facility");
                     }
                     while (fourthBoxIterator.hasNext())
                     {
                         cofe.getProblemLocationComboBox4().addItem(fourthBoxIterator.next());
                     }
                 }
                 if (!(cofe.getProblemLocationComboBox3().getSelectedItem() == null)
                         && (cofe.getProblemLocationComboBox3().getSelectedItem().equals("Select a Location")))
                 {
                     cofe.getProblemLocationComboBox4().removeAllItems();
                 }
 
             }
         });
 
         cofe.getWhoIsAComboBox().addItem("Who Is A?");
         cofe.getWhoIsAComboBox().addItem("Delegate");
         cofe.getWhoIsAComboBox().addItem("Speaker");
         cofe.getWhoIsAComboBox().addItem("Team Member");
 
         cofe.getContactViaComboBox().addItem("Choose One");
         cofe.getContactViaComboBox().addItem("Mobile");
         cofe.getContactViaComboBox().addItem("Radio");
         cofe.getContactViaComboBox().addItem("Village Host");
         cofe.getContactViaComboBox().addItem("Village");
         cofe.getContactViaComboBox().addItem("Not Required");
 
         cofe.getContactViaComboBox().addActionListener(new ActionListener()
         {
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 if (e.getActionCommand().equals("comboBoxChanged")
                         && cofe.getContactViaComboBox().getSelectedItem().
                         toString().equals("Not Required"))
                 {
                     cofe.getContactNumberTextField().setText(
                             cofe.getContactViaComboBox().
                             getSelectedItem().toString());
                 } else
                 {
                     cofe.getContactNumberTextField().setText("");
                 }
             }
         });
 
         cofe.getLocationVenueVillageComboBox().addItem("Choose One");
         cofe.getLocationVenueVillageComboBox().addItem("Location");
         cofe.getLocationVenueVillageComboBox().addItem("Venue");
         cofe.getLocationVenueVillageComboBox().addItem("Village");
         cofe.getLocationVenueVillageComboBox().addItem("Not Required");
 
         cofe.getSubmitFormButton().addActionListener(
                 new ActionListener()
         {
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 submitTicket(cofe);
             }
         });
 
         cofe.getResetFormButton().addActionListener(
                 new ActionListener()
         {
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 cofe.getTicketWrittenComboBox().setSelectedIndex(0);
                 cofe.getProblemLocationComboBox1().setSelectedIndex(0);
                 cofe.getProblemDescriptionTextArea().setText("");
                 Iterator<JRadioButton> buttonsIterator = cofe.getButtonsInGrid().iterator();
                 while (buttonsIterator.hasNext())
                 {
                     buttonsIterator.next().setSelected(false);
                 }
                 cofe.getProblemReportedByTextField().setText("");
                 cofe.getWhoIsAComboBox().setSelectedIndex(0);
                 cofe.getContactViaComboBox().setSelectedIndex(0);
                 cofe.getContactNumberTextField().setText("");
                 cofe.getLocationVenueVillageComboBox().setSelectedIndex(0);
             }
         });
         
         cofe.getPrintFormButton().addActionListener(
                 new ActionListener()
                 {
                     @Override
                     public void actionPerformed(ActionEvent e)
                     {
                         String ticketID = "" + submitTicket(cofe);
                        Ticket ticket = mysqlEngine.retrieveTicket(ticketID, "Tickets");
                         if (ticket.dataValidationEntry().equals("Passed")
                                 && ticket.printingValidation().equals("Passed"))
                         {
                             printTicket(ticket);
                         }
                     }
                 });
         
         cofe.getDelegateImpactComboBox().addItem("Low");
         cofe.getDelegateImpactComboBox().addItem("Medium");
         cofe.getDelegateImpactComboBox().addItem("High");
         cofe.getDelegateImpactComboBox().setSelectedItem(0);
 
         cofe.getTicketAllocatedToComboBox().addItem("Please Choose One");
         cofe.getTicketAllocatedToComboBox().addItem("Control Office");
         cofe.getTicketAllocatedToComboBox().addItem("Control Office (Admin)");
         cofe.getTicketAllocatedToComboBox().addItem("Day Stewards");
         cofe.getTicketAllocatedToComboBox().addItem("Night Stewards");
         cofe.getTicketAllocatedToComboBox().addItem("AV");
         cofe.getTicketAllocatedToComboBox().addItem("Finance");
         cofe.getTicketAllocatedToComboBox().addItem("Hospitality");
         cofe.getTicketAllocatedToComboBox().addItem("Information");
         cofe.getTicketAllocatedToComboBox().addItem("IT Support");
         cofe.getTicketAllocatedToComboBox().addItem("Logistics");
         cofe.getTicketAllocatedToComboBox().addItem("Production");
         cofe.getTicketAllocatedToComboBox().addItem("Site Crew");
         cofe.getTicketAllocatedToComboBox().addItem("WigWam");
         cofe.getTicketAllocatedToComboBox().addItem("Andy (Showground)");
         cofe.getTicketAllocatedToComboBox().addItem("Jackie (Cleaners)");
         cofe.getTicketAllocatedToComboBox().addItem("Other");
 
         cofe.getJobProgressComboBox().addItem("Issue Reported");
         cofe.getJobProgressComboBox().setSelectedIndex(0);
         cofe.getJobProgressComboBox().setEnabled(false);
 
         cofe.getAsAtTextField().setText("Automated");
         cofe.getAsAtTextField().setEditable(false);
 
 
         return cofe;
     }
 
     private ControlOfficeEntryForm createUpdateAmendEntryForm(String ticketID, String table)
     {
         final ControlOfficeEntryForm cofeAmmend = new ControlOfficeEntryForm(false, user);
         final Ticket ticket;
         ticket = mysqlEngine.retrieveTicket(ticketID, table);
         JMenu fileMenu = cofeAmmend.getMainFrame().getJMenuBar().getMenu(0);
         JMenuItem print = new JMenuItem("Print");
         print.addActionListener(new ActionListener()
         {
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 if (ticket.dataValidationEntry().equals("Passed")
                         && ticket.printingValidation().equals("Passed"))
                 {
                     printTicket(ticket);
                 }
             }
         });
         fileMenu.add(print, 0);
         try
         {
 
             //Add information back into the ticket
             cofeAmmend.getTicketReferenceTextField().setText("" + ticket.getJobRefId());
             cofeAmmend.getTicketReferenceTextField().setEnabled(false);
             cofeAmmend.getDateTimeTextField().setText(ticket.getDateTime().toString(DATEFORMAT));
             cofeAmmend.getDateTimeTextField().setEnabled(false);
             cofeAmmend.getTicketWrittenComboBox().addItem(ticket.getTicketRaisedBy().getTeam());
             cofeAmmend.getTicketWrittenComboBox().setSelectedIndex(0);
             cofeAmmend.getTicketWrittenComboBox().setEnabled(false);
             cofeAmmend.getTeamMembersComboBox().addItem(ticket.getTicketRaisedBy().getName());
             cofeAmmend.getTeamMembersComboBox().setSelectedIndex(0);
             cofeAmmend.getTeamMembersComboBox().setEnabled(false);
 
             String[] problemLocation = ticket.getProblemLocation().split("-");
             cofeAmmend.getProblemLocationComboBox1().addItem(problemLocation[0]);
             cofeAmmend.getProblemLocationComboBox1().setSelectedIndex(0);
             cofeAmmend.getProblemLocationComboBox1().setEnabled(false);
             cofeAmmend.getProblemLocationComboBox2().addItem(problemLocation[1]);
             cofeAmmend.getProblemLocationComboBox2().setSelectedIndex(0);
             cofeAmmend.getProblemLocationComboBox2().setEnabled(false);
             cofeAmmend.getProblemLocationComboBox3().addItem(problemLocation[2]);
             cofeAmmend.getProblemLocationComboBox3().setSelectedIndex(0);
             cofeAmmend.getProblemLocationComboBox3().setEnabled(false);
             cofeAmmend.getProblemLocationComboBox4().addItem(problemLocation[3]);
             cofeAmmend.getProblemLocationComboBox4().setSelectedIndex(0);
             cofeAmmend.getProblemLocationComboBox4().setEnabled(false);
 
             cofeAmmend.getProblemDescriptionTextArea().setText(ticket.getProblemDescription());
             cofeAmmend.getProblemDescriptionTextArea().setEnabled(false);
 
             Iterator<String> it1 = ticket.getCISKeywords().iterator();
             while (it1.hasNext())
             {
                 String keyword = it1.next();
                 Iterator<JRadioButton> it2 = cofeAmmend.getButtonsInGrid().iterator();
                 while (it2.hasNext())
                 {
                     JRadioButton buttonToConsider = it2.next();
                     if (buttonToConsider.getName().equals(keyword))
                     {
                         buttonToConsider.setSelected(true);
                     }
                     buttonToConsider.setEnabled(false);
                 }
             }
 
             cofeAmmend.getProblemReportedByTextField().setText(ticket.getReportedBy());
             cofeAmmend.getProblemReportedByTextField().setEnabled(false);
             cofeAmmend.getWhoIsAComboBox().addItem(ticket.getWhoIsA());
             cofeAmmend.getWhoIsAComboBox().setSelectedIndex(0);
             cofeAmmend.getWhoIsAComboBox().setEnabled(false);
 
             cofeAmmend.getContactViaComboBox().addItem(ticket.getContactVia());
             cofeAmmend.getContactViaComboBox().setSelectedIndex(0);
             cofeAmmend.getContactViaComboBox().setEnabled(false);
             cofeAmmend.getContactNumberTextField().setText(ticket.getContactNumber());
             cofeAmmend.getContactNumberTextField().setEnabled(false);
             cofeAmmend.getLocationVenueVillageComboBox().addItem(ticket.getLocationVenueVillage());
             cofeAmmend.getLocationVenueVillageComboBox().setSelectedIndex(0);
             cofeAmmend.getLocationVenueVillageComboBox().setEnabled(false);
 
             // All fields that should be filled in are above
 
             cofeAmmend.getDelegateImpactComboBox().addItem("Low");
             cofeAmmend.getDelegateImpactComboBox().addItem("Medium");
             cofeAmmend.getDelegateImpactComboBox().addItem("High");
             cofeAmmend.getDelegateImpactComboBox().setSelectedItem(
                     ticket.getDelegateImpact());
 
             cofeAmmend.getShowOnCISRadioButton().setSelected(ticket.isShowOnCIS());
 
             cofeAmmend.getTicketAllocatedToComboBox().addItem("Please Choose One");
             cofeAmmend.getTicketAllocatedToComboBox().addItem("Control Office");
             cofeAmmend.getTicketAllocatedToComboBox().addItem("Control Office (Admin)");
             cofeAmmend.getTicketAllocatedToComboBox().addItem("Day Stewards");
             cofeAmmend.getTicketAllocatedToComboBox().addItem("Night Stewards");
             cofeAmmend.getTicketAllocatedToComboBox().addItem("AV");
             cofeAmmend.getTicketAllocatedToComboBox().addItem("Finance");
             cofeAmmend.getTicketAllocatedToComboBox().addItem("Hospitality");
             cofeAmmend.getTicketAllocatedToComboBox().addItem("Information");
             cofeAmmend.getTicketAllocatedToComboBox().addItem("IT Support");
             cofeAmmend.getTicketAllocatedToComboBox().addItem("Logistics");
             cofeAmmend.getTicketAllocatedToComboBox().addItem("Production");
             cofeAmmend.getTicketAllocatedToComboBox().addItem("Site Crew");
             cofeAmmend.getTicketAllocatedToComboBox().addItem("WigWam");
             cofeAmmend.getTicketAllocatedToComboBox().addItem("Andy (Showground)");
             cofeAmmend.getTicketAllocatedToComboBox().addItem("Jackie (Cleaners)");
             cofeAmmend.getTicketAllocatedToComboBox().addItem("Other");
             if (!(ticket.getTicketAllocatedTo() == null))
             {
                 cofeAmmend.getTicketAllocatedToComboBox().
                         setSelectedItem(ticket.getTicketAllocatedTo());
             } else
             {
                 cofeAmmend.getTicketAllocatedToComboBox().setSelectedIndex(0);
             }
 
             cofeAmmend.getJobProgressComboBox().addItem("Issue Reported");
             cofeAmmend.getJobProgressComboBox().addItem("Ticket Printed");
             cofeAmmend.getJobProgressComboBox().addItem("Job In Progress");
             cofeAmmend.getJobProgressComboBox().addItem("Job Escalated");
            cofeAmmend.getJobProgressComboBox().addItem("Duplicate");
             cofeAmmend.getJobProgressComboBox().addItem("Job Done");
             cofeAmmend.getJobProgressComboBox().setSelectedItem(ticket.getJobProgress());
 
             cofeAmmend.getAsAtTextField().setText(ticket.getAsAt().toString(DATEFORMAT));
             cofeAmmend.getAsAtTextField().setEnabled(false);
 
             cofeAmmend.getUpdatedAtTextField1().setEnabled(false);
             if (!(ticket.getUpdatedAt().get(0) == null))
             {
                 cofeAmmend.getUpdateTextArea1().setText(ticket.getUpdateDescriptions().get(0));
                 cofeAmmend.getEstimatedCompletionByTextField1().setText(ticket.getEstimatedCompletions().get(0).toString(DATEFORMAT));
                 cofeAmmend.getUpdatedAtTextField1().setText(ticket.getUpdatedAt().get(0).toString(DATEFORMAT));
                 cofeAmmend.getUpdateTextArea1().setEnabled(false);
                 cofeAmmend.getEstimatedCompletionByTextField1().setEnabled(false);
 
             } else
             {
                 cofeAmmend.getEstimatedCompletionByTextField1().setText(DATEFORMAT);
                 cofeAmmend.getUpdatedAtTextField1().setText("Automated");
                 cofeAmmend.getUpdateTextArea2().setEnabled(false);
                 cofeAmmend.getEstimatedCompletionByTextField2().setEnabled(false);
                 cofeAmmend.getUpdateTextArea3().setEnabled(false);
                 cofeAmmend.getEstimatedCompletionByTextField3().setEnabled(false);
             }
             cofeAmmend.getUpdatedAtTextField2().setEnabled(false);
             if (!(ticket.getUpdatedAt().get(1) == null))
             {
                 cofeAmmend.getUpdateTextArea2().setText(ticket.getUpdateDescriptions().get(1));
                 cofeAmmend.getEstimatedCompletionByTextField2().setText(ticket.getEstimatedCompletions().get(1).toString(DATEFORMAT));
                 cofeAmmend.getUpdatedAtTextField2().setText(ticket.getUpdatedAt().get(1).toString(DATEFORMAT));
                 cofeAmmend.getUpdateTextArea2().setEnabled(false);
                 cofeAmmend.getEstimatedCompletionByTextField2().setEnabled(false);
             } else
             {
                 cofeAmmend.getEstimatedCompletionByTextField2().setText(DATEFORMAT);
                 cofeAmmend.getUpdatedAtTextField2().setText("Automated");
                 cofeAmmend.getUpdateTextArea3().setEnabled(false);
                 cofeAmmend.getEstimatedCompletionByTextField3().setEnabled(false);
             }
             cofeAmmend.getUpdatedAtTextField3().setEnabled(false);
             if (!(ticket.getUpdatedAt().get(2) == null))
             {
                 cofeAmmend.getUpdateTextArea3().setText(ticket.getUpdateDescriptions().get(2));
                 cofeAmmend.getEstimatedCompletionByTextField3().setText(ticket.getEstimatedCompletions().get(2).toString(DATEFORMAT));
                 cofeAmmend.getUpdatedAtTextField3().setText(ticket.getUpdatedAt().get(2).toString(DATEFORMAT));
                 cofeAmmend.getUpdateTextArea3().setEnabled(false);
                 cofeAmmend.getEstimatedCompletionByTextField3().setEnabled(false);
             } else
             {
                 cofeAmmend.getEstimatedCompletionByTextField3().setText(DATEFORMAT);
                 cofeAmmend.getUpdatedAtTextField3().setText("Automated");
             }
 
             cofeAmmend.getJobCompletedTextField().setEnabled(false);
             if (!(ticket.getJobClosed() == null))
             {
                 cofeAmmend.getJobCompletedRadioButton().setSelected(true);
                 cofeAmmend.getDelegateImpactComboBox().setEnabled(false);
                 cofeAmmend.getShowOnCISRadioButton().setEnabled(false);
                 cofeAmmend.getTicketAllocatedToComboBox().setEnabled(false);
                 cofeAmmend.getJobProgressComboBox().setEnabled(false);
                 cofeAmmend.getUpdateTextArea1().setEnabled(false);
                 cofeAmmend.getEstimatedCompletionByTextField1().setEnabled(false);
                 cofeAmmend.getUpdateTextArea2().setEnabled(false);
                 cofeAmmend.getEstimatedCompletionByTextField2().setEnabled(false);
                 cofeAmmend.getUpdateTextArea3().setEnabled(false);
                 cofeAmmend.getEstimatedCompletionByTextField3().setEnabled(false);
                 cofeAmmend.getJobCompletedRadioButton().setEnabled(false);
                 cofeAmmend.getJobCompletedTextField().setText(ticket.getJobClosed().toString(DATEFORMAT));
             } else
             {
                 cofeAmmend.getNextUpdateDueTextField().setEnabled(false);
                 cofeAmmend.getNextUpdateDueTextField().setText("Job Closed");
                 cofeAmmend.getJobCompletedTextField().setText("Automated");
             }
             cofeAmmend.getNextUpdateDueTextField().setText(calculateNextUpdateDue(ticket).toString(DATEFORMAT));
             cofeAmmend.getNextUpdateDueTextField().setEnabled(false);
 
             
 
 
             cofeAmmend.getResetFormButton().addActionListener(new ActionListener()
             {
                 @Override
                 public void actionPerformed(ActionEvent e)
                 {
                     cofeAmmend.getDelegateImpactComboBox().setSelectedItem(ticket.getDelegateImpact());
                     cofeAmmend.getShowOnCISRadioButton().setSelected(ticket.isShowOnCIS());
                     cofeAmmend.getTicketAllocatedToComboBox().setSelectedItem(ticket.getTicketAllocatedTo());
                     cofeAmmend.getJobProgressComboBox().setSelectedItem(ticket.getJobProgress());
                     cofeAmmend.getUpdateTextArea1().setText("");
                     cofeAmmend.getEstimatedCompletionByTextField1().setText(DATEFORMAT);
                     cofeAmmend.getUpdateTextArea2().setText("");
                     cofeAmmend.getEstimatedCompletionByTextField2().setText(DATEFORMAT);
                     cofeAmmend.getUpdateTextArea3().setText("");
                     cofeAmmend.getEstimatedCompletionByTextField3().setText(DATEFORMAT);
 
 
                 }
             });
 
             cofeAmmend.getSubmitFormButton().addActionListener(new ActionListener()
             {
                 @Override
                 public void actionPerformed(ActionEvent e)
                 {
                     ticket.setDelegateImpact(cofeAmmend.getDelegateImpactComboBox()
                             .getSelectedItem().toString());
                     ticket.setShowOnCIS(cofeAmmend.getShowOnCISRadioButton().isSelected());
                     ticket.setAsAt(stringToDateTime(cofeAmmend.getAsAtTextField().getText()));
                     if (cofeAmmend.getUpdateTextArea1().isEnabled() && !(cofeAmmend.getUpdateTextArea1().getText().equals("")))
                     {
                         ticket.getUpdateDescriptions().set(0, cofeAmmend.getUpdateTextArea1().getText());
                         ticket.getEstimatedCompletions().set(0, stringToDateTime(cofeAmmend.getEstimatedCompletionByTextField1().getText()));
                         ticket.getUpdatedAt().set(0, new DateTime());
                     } else if (cofeAmmend.getUpdateTextArea2().isEnabled() && !(cofeAmmend.getUpdateTextArea2().getText().equals("")))
                     {
                         ticket.getUpdateDescriptions().set(1, cofeAmmend.getUpdateTextArea2().getText());
                         ticket.getEstimatedCompletions().set(1, stringToDateTime(cofeAmmend.getEstimatedCompletionByTextField2().getText()));
                         ticket.getUpdatedAt().set(1, new DateTime());
                     } else if (cofeAmmend.getUpdateTextArea3().isEnabled() && !(cofeAmmend.getUpdateTextArea3().getText().equals("")))
                     {
                         ticket.getUpdateDescriptions().set(2, cofeAmmend.getUpdateTextArea3().getText());
                         ticket.getEstimatedCompletions().set(2, stringToDateTime(cofeAmmend.getEstimatedCompletionByTextField3().getText()));
                         ticket.getUpdatedAt().set(2, new DateTime());
                     }
 
                     if (cofeAmmend.getJobCompletedRadioButton().isSelected())
                     {
                         ticket.setJobClosed(new DateTime());
                     }
 
                     ticket.setNextUpdateDue(calculateNextUpdateDue(ticket));
                     if (mysqlEngine.updateTicket(ticket) && ticket.updateValidation())
                     {
                         JOptionPane.showMessageDialog(cofeAmmend.getMainFrame(),
                                 "Update Committed Successfully");
                     } else
                     {
 
                         JOptionPane.showMessageDialog(cofeAmmend.getMainFrame(),
                                 "It didn't work :( !");
                     }
 
 
                 }
             });
 
         } catch (Exception e)
         {
             cofeAmmend.getMainFrame().dispose();
             JOptionPane.showMessageDialog(
                     mainGUI.getTaskSelectionScreen().getMainFrame(),
                     "That Ticket ID does not exist. Please enter an existing one", "Error has occurred",
                     JOptionPane.ERROR_MESSAGE);
         }
 
         cofeAmmend.getPrintFormButton().addActionListener(new ActionListener()
         {
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 if (ticket.dataValidationEntry().equals("Passed")
                         && ticket.printingValidation().equals("Passed"))
                 {
                     printTicket(ticket);
                 }
             }
         });
 
         return cofeAmmend;
     }
 
     private DateTime calculateNextUpdateDue(Ticket ticket)
     {
         ArrayList<DateTime> timesToCompare = new ArrayList<>();
         int i = 0;
         while (i < ticket.getUpdatedAt().size())
         {
             timesToCompare.add(ticket.getUpdatedAt().get(i));
             i++;
         }
         timesToCompare.add(ticket.getAsAt());
         Collections.sort(timesToCompare, DateTimeComparator.getInstance());
         i = timesToCompare.size() - 1;
         boolean found = false;
         DateTime nextUpdateDue = null;
         while (i > -1)
         {
             if (timesToCompare.get(i) != null && !found)
             {
                 nextUpdateDue = timesToCompare.get(i).plusHours(UPDATEHOURS);
                 found = true;
                 i--;
             } else
             {
                 i--;
             }
         }
         return nextUpdateDue;
     }
 
     private DateTime stringToDateTime(String date)
     {
         DateTime timeToReturn = new DateTime(Integer.parseInt(date.substring(6, 10)),
                 Integer.parseInt(date.substring(3, 5)), 
                 Integer.parseInt(date.substring(0, 2)), 
                 Integer.parseInt(date.substring(11, 13)), 
                 Integer.parseInt(date.substring(14, 16)));
         return timeToReturn;
     }
 
     private void printTicket(Ticket ticket)
     {
         Template template = new Template();
         final JFrame progressFrame = new JFrame("Printing Ticket....");
         GridLayout layout = new GridLayout(2,1);
         
         JLabel printingProgressLabel = new JLabel(
                 template.headingString("Printing Progress", 2), JLabel.CENTER);
         final JTextArea messageText = new JTextArea();
         messageText.setPreferredSize(new Dimension(300,350));
         messageText.setText("");
         Border border = BorderFactory.createLineBorder(Color.BLACK, 2);
         messageText.setBorder(border);
         
         PrintRunner printRunner = new PrintRunner(ticket, remoteInterfaceEngine,
                 mysqlEngine);
         
         progressFrame.setLayout(layout);
         progressFrame.add(printingProgressLabel, JLabel.CENTER);
         progressFrame.add(messageText);
         progressFrame.setPreferredSize(new Dimension(600,400));
         progressFrame.pack();
         progressFrame.setVisible(true);
         
         
         printRunner.addPropertyChangeListener(new PropertyChangeListener()
         {
             @Override
             public void propertyChange(PropertyChangeEvent evt)
             {
                 if(evt.getPropertyName().equals("Message"))
                 {
                     messageText.append((String) evt.getNewValue());
                     progressFrame.repaint();
                 }
                 if(evt.getPropertyName().equals("state"))
                 {
                     if(evt.getNewValue().toString().equals("DONE"))
                     {
                         progressFrame.dispose();
                     }
                 }
             }
         });
         try
         {
             printRunner.execute();
         }
         catch(Exception e)
         {
             
         }
     }
 
     private int submitTicket(ControlOfficeEntryForm cofe)
     {
         String team = user.getTeam();
         user.setName(cofe.getTeamMembersComboBox().getSelectedItem().toString());
         String member = user.getName();
         String problemLocation = cofe.
                 getProblemLocationComboBox1().getSelectedItem().toString()
                 + "-" + cofe.getProblemLocationComboBox2().getSelectedItem().toString().substring(4)
                 + "-" + cofe.getProblemLocationComboBox3().getSelectedItem().toString().substring(4)
                 + "-" + cofe.getProblemLocationComboBox4().getSelectedItem().toString().substring(4);
         String problemDescription = cofe.getProblemDescriptionTextArea().getText();
         Iterator<JRadioButton> buttonsIterator = cofe.getButtonsInGrid().iterator();
         ArrayList<String> keyWords = new ArrayList<>();
         while (buttonsIterator.hasNext())
         {
             JRadioButton buttonUnderConsideration = buttonsIterator.next();
             if (buttonUnderConsideration.isSelected())
             {
                 keyWords.add(buttonUnderConsideration.getName());
             }
         }
         String problemReportedBy = cofe.getProblemReportedByTextField().getText();
         String whoIsA = cofe.getWhoIsAComboBox().getSelectedItem().toString();
         String contactVia = cofe.getContactViaComboBox().getSelectedItem().toString();
         String contactNumber = cofe.getContactNumberTextField().getText();
         String locationVenueVillage = (String) cofe.getLocationVenueVillageComboBox().
                 getSelectedItem().toString();
         String delegateImpact = cofe.getDelegateImpactComboBox().getSelectedItem().toString();
         boolean showOnCIS = cofe.getShowOnCISRadioButton().isSelected();
         String ticketAllocatedTo = cofe.getTicketAllocatedToComboBox().getSelectedItem().toString();
         if (ticketAllocatedTo.equals("Please Choose One"))
         {
             ticketAllocatedTo = "";
         }
         DateTime asAt = new DateTime();
         Ticket ticket = new Ticket(0, null, user,
                 problemLocation, problemDescription, keyWords,
                 problemReportedBy, whoIsA, contactVia, contactNumber,
                 locationVenueVillage, delegateImpact, showOnCIS, ticketAllocatedTo, "Issue Reported", asAt, null, null,
                 null, null, null);
         String validationResult = ticket.dataValidationEntry();
         if (validationResult.equals("Passed"))
         {
             if (!(mysqlEngine.isTicketDuplicate(ticket)))
             {
                 if (mysqlEngine.submitTicket(ticket, false))
                 {
                     JOptionPane.showMessageDialog(cofe.getMainFrame(),
                             "The Job Ticket has been submitted. \n"
                             + "The Job ID is: " + ticket.getJobRefId()
                             + "\nIt was submitted at: "
                             + ticket.getDateTime().toString(DATEFORMAT));
                     cofe.getMainFrame().dispose();
                 } else
                 {
                     JOptionPane.showMessageDialog(cofe.getMainFrame(), "An error has occured. \n"
                                     + "Don't close this window and report to Jonathan Rainer (Admin)"
                                     + "or Ian Walker (Control Office) for assistance.");
                     return -1;
                 }
             } else
             {
                 mysqlEngine.submitTicket(ticket, true);
                 JOptionPane.showMessageDialog(cofe.getMainFrame(),
                                     "The Job Ticket you submitted has been "
                                     + "flagged as a duplicate. Control Office will advise. \n"
                                     + "The Temporary Job ID is: " + ticket.getJobRefId()
                                     + "\n It was submitted at: " + ticket.getDateTime().toString(DATEFORMAT));
                 cofe.getMainFrame().dispose();
                 return -2;
             }
         } else
         {
             JOptionPane.showMessageDialog(cofe.getMainFrame(), validationResult);
             return -3;
         }
         return ticket.getJobRefId();
     }
     
     public InitialGUI getInitialGUI()
     {
         return initialGUI;
     }
 
     /**
      * Return the user object from the Main System.
      *
      * @return The User object.
      */
     private User getUser()
     {
         return user;
     }
 }
