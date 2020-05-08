 package pintosim;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.util.List;
 
 /**
  * Provides a graphical front end to PintoSim.
  *
  * @author PlzSendTheCodes team
  */
 public class GUI implements ActionListener, FocusListener {
 
     // GUI objects
     private JFrame frame = new JFrame("PintoSim");
     private JLabel statusOfCommand = new JLabel("");
 
     // Backend objects
     private EnviornmentMap map;
     private PintoManager pintoManager;
     private Command potentialGetItemCancelationCommand;
 
     // Item on the map
     private String name;
     private int x;
     private int y;
     
     private String query;
 
     /**
      * Constructs a GUI object.
      * @param pintomanager manages pintos
      * @param map          the environment map
      * @param command      a command
      */
     public GUI(PintoManager pintoManager, EnviornmentMap map) {
     	
     	this.pintoManager = pintoManager;
     	this.map = map;
 
         /* Menu Bar */
         JMenuBar menu = new JMenuBar();
         JMenu aboutMenu = new JMenu("About");
         frame.setJMenuBar(menu);
         menu.add(aboutMenu);
 
         /* add a content panel and a panel for all the commands. */
         JPanel content = new JPanel();
         content.setPreferredSize(new Dimension(1100, 680));
         statusOfCommand.setText("Pintos are waiting for a command.");
         JPanel commands = new JPanel(new FlowLayout(FlowLayout.LEFT));
         commands.setPreferredSize(new Dimension(1100, 161));
 
         /* Add a panel for location */
         JPanel locationPanel = new JPanel(new FlowLayout());
         locationPanel.setBorder(BorderFactory.createTitledBorder(" Note Location "));
         locationPanel.setPreferredSize(new Dimension(200, 130));
         JLabel xLabel = new JLabel("X: ");
         JLabel yLabel = new JLabel("Y: ");
         
         if (pintoManager == null && map == null) {
         	JOptionPane.showMessageDialog(frame, "PintoManager and Map is null!");
         }
 
         final JTextField xLoc = new JTextField("", 3);
         // Get the x location
         xLoc.addFocusListener(new FocusListener() {
             @Override
             public void focusGained(FocusEvent focusEvent) {
                 // Listen for input
             }
 
             @Override
             public void focusLost(FocusEvent focusEvent) {
                 if (xLoc.getText().equals("")) {
                     x = 0;
                 } else {
                     try {
                         x = Integer.parseInt(xLoc.getText());
                         x = Math.abs(x); // ignore negative numbers
                     } catch (NumberFormatException nfe) {
                         JOptionPane.showMessageDialog(frame, "Invalid input for x");
                         xLoc.setText("");
                     }
                 }
             }
         });
 
         final JTextField yLoc = new JTextField("", 3);
         // Get the y location
         yLoc.addFocusListener(new FocusListener() {
             @Override
             public void focusGained(FocusEvent focusEvent) {
                 // Listen for input
             }
 
             @Override
             public void focusLost(FocusEvent focusEvent) {
                 if (yLoc.getText().equals("")) {
                     y = 0;
                 } else {
                     try {
                         y = Integer.parseInt(yLoc.getText());
                         y = Math.abs(y); // ignore negative numbers
                     } catch (NumberFormatException nfe) {
                         JOptionPane.showMessageDialog(frame, "Invalid input for y");
                         yLoc.setText("");
                     }
                 }
             }
         });
 
         JLabel itemLabel = new JLabel("Name: ");
 
         final JTextField itemName = new JTextField("", 8);
         itemName.addFocusListener(new FocusListener() {
             @Override
             public void focusGained(FocusEvent focusEvent) {
                 // Listen for input
             }
 
             @Override
             public void focusLost(FocusEvent focusEvent) {
                 name = itemName.getText();
             }
         });
         // Location button
         JButton locationButton = new JButton("Add item");
         locationButton.setLayout(new FlowLayout());
         locationButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent actionEvent) {
                 if (itemName.getText().equals("")) {
                     JOptionPane.showMessageDialog(frame, "No name entered!");
                 } else {
                     performAddItem(new Command(Command.Type.ADD_ITEM, name, x, y));
                 }
                 // Clear all text fields after user is done entering
                 xLoc.setText("");
                 yLoc.setText("");
                 itemName.setText("");
             }
         });
 
         /* Get item panel */
         JPanel getPanel = new JPanel(new FlowLayout());
         getPanel.setBorder(BorderFactory.createTitledBorder(" Get Item "));
         getPanel.setPreferredSize(new Dimension(200, 130));
         JLabel getItemName = new JLabel("Name: ");
 
         final JTextField getItemField = new JTextField("", 8);
         getItemField.addFocusListener(new FocusListener() {
             @Override
             public void focusGained(FocusEvent focusEvent) {
                 // Listen for input
             }
 
             @Override
             public void focusLost(FocusEvent focusEvent) {
                 name = getItemField.getText();
             }
         });
         // Get item button
         JButton getItemButton = new JButton("Get Item");
         getItemButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent actionEvent) {
                 if (getItemField.getText().equals("")) {
                     JOptionPane.showMessageDialog(frame, "No name entered!");
                 } else {
                     performGetItem(new Command(Command.Type.GET_ITEM));
                     // Clear out values
                     getItemField.setText("");
                 }
             }
         });
 
         /* Get Status Panel */
         JPanel statusPanel = new JPanel(new FlowLayout());
         statusPanel.setBorder(BorderFactory.createTitledBorder(" Get status "));
         statusPanel.setPreferredSize(new Dimension(200, 130));
         JLabel statusNameLabel = new JLabel("Name: ");
 
         final JTextField statusNameField = new JTextField("", 8);
         statusNameField.addFocusListener(new FocusListener() {
             @Override
             public void focusGained(FocusEvent focusEvent) {
                 // Listen for input
             }
 
             @Override
             public void focusLost(FocusEvent focusEvent) {
                 name = statusNameField.getText();
             }
         });
 
         // status button
         JButton statusButton = new JButton("Get Status of item");
         statusButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent actionEvent) {
                 if (statusNameField.getText().equals("")) {
                     JOptionPane.showMessageDialog(frame, "No name entered!");
                 }
                 else {
                     performGetItemStatus(new Command(Command.Type.GET_ITEM_STATUS));
                     // Clear out fields
                     statusNameField.setText("");
                 }
             }
         });
 
         /* Cancel item Panel */
         JPanel cancelPanel = new JPanel(new FlowLayout());
         cancelPanel.setBorder(BorderFactory.createTitledBorder(" Cancel Item "));
         cancelPanel.setPreferredSize(new Dimension(200, 130));
         JLabel cancelNameLabel = new JLabel("Name: ");
 
         final JTextField cancelNameField = new JTextField("", 8);
         cancelNameField.addFocusListener(new FocusListener() {
             @Override
             public void focusGained(FocusEvent focusEvent) {
                 // Listen for input
             }
 
             @Override
             public void focusLost(FocusEvent focusEvent) {
                 name = cancelNameField.getText();
             }
         });
         // Cancel Button
         JButton cancelButton = new JButton("Cancel Item");
         cancelButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent actionEvent) {
                 if (cancelNameField.getText().equals("")) {
                     statusOfCommand.setText("No name entered!");
                 }
                 else {
                     performCancelGetItem(new Command(Command.Type.CANCEL_GET_ITEM));
                     cancelNameField.setText("");
                 }
             }
         });
 
         /* Help Desk Panel */
         JPanel helpPanel = new JPanel(new FlowLayout());
         helpPanel.setBorder(BorderFactory.createTitledBorder(" Call Help Desk "));
         helpPanel.setPreferredSize(new Dimension(250, 130));
         final JTextArea helpArea = new JTextArea("");
         helpArea.setBorder(BorderFactory.createTitledBorder(""));
         final JScrollPane helpScrollPane = new JScrollPane(helpArea);
         helpScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
         helpScrollPane.setPreferredSize(new Dimension(50, 30));
         helpArea.setEditable(true);
         helpArea.setPreferredSize(new Dimension(220, 60));
         helpArea.setCaretPosition(helpArea.getDocument().getLength());
         // Help Area listener
         helpArea.addFocusListener(new FocusListener() {
             @Override
             public void focusGained(FocusEvent focusEvent) {
                 // Listen for input
             }
 
             @Override
             public void focusLost(FocusEvent focusEvent) {
                 query = helpArea.getText();
             }
         });
         // Help button
         JButton helpButton = new JButton("Send query to Help Desk");
         helpButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent actionEvent) {
                 if (helpArea.getText().equals("")) {
                     JOptionPane.showMessageDialog(frame, "No query entered!");
                 } else {
                     // Send to help desk.
                     performHelpDesk();
                     // Clear all fields
                     helpArea.setText("");
                 }
             }
         });
 
         /* Create a commandStatus panel to notify the user if a command finishes */
         JPanel commandStatus = new JPanel(new FlowLayout());
         //commandStatus.setBorder(BorderFactory.createTitledBorder(" Status of commands "));
         commandStatus.setPreferredSize(new Dimension(1070, 25));
         commandStatus.add(statusOfCommand, BorderLayout.CENTER);
 
         /* Create a panel to house (pun intended) the house map */
         JPanel mapPanel = new JPanel(new BorderLayout());
         mapPanel.setBorder(BorderFactory.createTitledBorder(" House Map "));
         mapPanel.setPreferredSize(new Dimension(1070, 500));
 
         // About menu
         JMenuItem aboutTeam = new JMenuItem("About PintoSim");
         aboutMenu.add(aboutTeam);
         aboutTeam.addActionListener(this);
 
         /* Frame settings */
         // Add all the panels to the frame
         locationPanel.add(xLabel, FlowLayout.LEFT);
         locationPanel.add(xLoc);
         locationPanel.add(yLabel);
         locationPanel.add(yLoc);
         locationPanel.add(itemLabel, BorderLayout.PAGE_START);
         locationPanel.add(itemName, BorderLayout.EAST);
         locationPanel.add(locationButton);
         getPanel.add(getItemName);
         getPanel.add(getItemField);
         getPanel.add(getItemButton);
         statusPanel.add(statusNameLabel);
         statusPanel.add(statusNameField);
         statusPanel.add(statusButton);
         cancelPanel.add(cancelNameLabel);
         cancelPanel.add(cancelNameField);
         cancelPanel.add(cancelButton);
         helpPanel.add(helpArea);
         helpPanel.add(helpButton);
         commands.add(locationPanel);
         commands.add(getPanel);
         commands.add(statusPanel);
         commands.add(cancelPanel);
         commands.add(helpPanel);
         commands.add(commandStatus);
         content.add(new JSeparator(JSeparator.HORIZONTAL), BorderLayout.PAGE_END);
         content.add(commands);
         content.add(mapPanel);
         frame.add(content);
         frame.pack();
 
         // set defaults and make it visible
         JFrame.setDefaultLookAndFeelDecorated(true);
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         frame.setVisible(true);
     }
 
     // Manages actions on a object
     public void actionPerformed(ActionEvent e) {
         if (e.getActionCommand().equals("About PintoSim")) {
             JOptionPane.showMessageDialog(frame,
                     "Designed by PlzSendTheCodes team",
                     "About PintoSim",
                     JOptionPane.INFORMATION_MESSAGE);
         }
         // Add more actions and menu items if necessary.
     }
 
     @Override
     public void focusGained(FocusEvent focusEvent) {
         // Listen for input.
     }
 
     @Override
     public void focusLost(FocusEvent focusEvent) {
         // Method is overridden whenever needed.
     }
 
     /**
      * Adds an item to the environment map.
      *
      * @param cmd the addItem command
      */
     public void performAddItem(Command cmd) {
         String errorText = "";
         String succeedText = "";
         if (!map.withinBoundaries(cmd.getX(), cmd.getY())) {
             errorText = "I can't add the " + cmd.getItemName() +
                     " there, the location (" + x + "," + y + ")" +
                     " is outside of the bounds (" + map.getHeight() +
                     "," + map.getWidth() + ").";
             statusOfCommand.setText(errorText);
         }
         if (map.isLocationWalkable(cmd.getX(), cmd.getY())) {
             errorText = "I can't add the " + cmd.getItemName() +
                     " there, the location (" + cmd.getX() + "," +
                     cmd.getY() + ") is occupied by something else.";
             statusOfCommand.setText(errorText);
         } else {
             Item item = map.getItemByName(cmd.getItemName());
             if (item != null) {
                 errorText = "Can't add the item " + name +
                         ". It already exists in the system at (" +
                         cmd.getX() + "," + cmd.getY() + ").";
                 statusOfCommand.setText(errorText);
             }
 
             map.trackItem(new Item(cmd.getItemName(), cmd.getX(), cmd.getY()));
             succeedText = cmd.getItemName() + " recorded. I can now" +
                     " retrieve it anytime you want.";
             statusOfCommand.setText(succeedText);
         }
     }
 
     /**
      * Gets an item for the user
      * @param cmd the getItem command
      */
    public void performGetItem(Command cmd) {
         String errorText = "";
         String succeedText = "";
         final Item item = map.getItemByName((cmd.getItemName()));
 
         if (item == null) {
             errorText = "I don't know where " + name +
                     " is. You need to add the item first.";
             statusOfCommand.setText(errorText);
         }
         if (pintoManager.uncompletedTaskExistsFor(item)) {
             errorText = "You already requested that I bring " +
                     cmd.getItemName() + " to you. You can't request the same" +
                     " item again.";
             statusOfCommand.setText(errorText);
         } else {
             if (pintoManager.canImmediatelyFulfillATask()) {
                 succeedText = "Okay I will get " + cmd.getItemName() +
                         " right now. Please wait a moment.";
                 statusOfCommand.setText(succeedText);
             } else {
                 errorText = "All pintos are busy right now, but I will " +
                         "get " + cmd.getItemName() + " as soon as possible.";
                 statusOfCommand.setText(errorText);
             }
             cmd.setGetItemCallback(new GetItemCallback() {
                 @Override
                 public void onComplete(List<Point> path) {
                    statusOfCommand.setText("Here is your " + item.getName());
                 }
             });
             pintoManager.addCommand(cmd);
         }
     }
 
     /**
      * Gets the status of the item.
      * @param cmd the getStatus command
      */
     public void performGetItemStatus(Command cmd) {
         String errorText = "";
         String succeedText = "";
         Item item = map.getItemByName(cmd.getItemName());
         if (item == null) {
             errorText = "I never knew where " + cmd.getItemName() +
                     " was so it is possible I am not retrieving it for you. You need to first" +
                     " tell me where the item is.";
             statusOfCommand.setText(errorText);
         }
 
         if (!pintoManager.uncompletedTaskExistsFor(item) && !pintoManager.completedTaskExistsFor(item)) {
             errorText = item.getName() + " was not requested by you so there is nothing to report.";
             statusOfCommand.setText(errorText);
         }
 
         switch (pintoManager.getTaskStatus(item)) {
             case COMPLETE:
                 errorText = "I have already delivered " + item.getName() +
                         " to you. Status is complete.";
                 statusOfCommand.setText(errorText);
                 break;
             case QUEUED:
                 succeedText = "The status of your retrival request for " +
                         item.getName() +
                         " is 'Queued'. All my pintos are busy" +
                         " doing other things. They will get it for you soon.";
                 statusOfCommand.setText(succeedText);
                 break;
             case STARTED:
             case ITEM_BEING_CARRIED:
                 succeedText = "The status of your retrival request for" +
                         item.getName() + " is 'Started'. A pinto is " +
                         "working on it as we speak," +
                         " so you should have it soon.";
                 statusOfCommand.setText(succeedText);
                 break;
             default:
                 throw new UnsupportedOperationException("Unknown Status");
         }
     }
 
     /**
      * Cancels the item retrieval
      * @param cmd the Command
      */
     public void performCancelGetItem(Command cmd) {
     	Item item = map.getItemByName(cmd.getItemName());
     	String errorText = "";
     	if (item == null) {
     		errorText = "I never knew where " + cmd.getItemName() +
     				" was, so it's not possible that I am retrieving" +
     				" it for you.";
     		statusOfCommand.setText(errorText);
     	}
     	if (pintoManager.uncompletedTaskExistsFor(item)) {
     		if (pintoManager.isItemBeingCarried(item)) {
     			JOptionPane.showConfirmDialog(frame, "A Pinto is carrying" +
     					" the item now. The item will be left on the " +
     					"ground. Do you want to cancel it?");
     			// Work in progress
     			// Add action Listener here.
     			// Also set potentialGetItemCancelation to null depending on the user's answer.
     		}
     		else {
     			statusOfCommand.setText("Okay, it's canceled.");
 ;    		}
     	}
     	if (pintoManager.completedTaskExistsFor(item)) {
     		errorText = "I already completed the request for " +
     				item.getName() + " so it's too late to cancel.";
     		statusOfCommand.setText("errorText");
     	}
     	errorText = "You never requested that I retrieve " +
     			item.getName() + " for you, so there is nothing" +
     					" to cancel.";
     	statusOfCommand.setText(errorText);
     }
     
     /**
      * Cancels the items and the Pinto leaves it at the ground.
      * @param cmd the cancel command 
      */
     public void performConfirmedCancelGetItem(Command cmd) {
     	String notice = "";
     	if (potentialGetItemCancelationCommand == null) {
     		return;
     	}
     	Item item = map.getItemByName(potentialGetItemCancelationCommand.getItemName());
     	
     	if (cmd.getConfirmation() == true) {
     		notice = "Okay, I will leave it on the ground at (" +
     				item.getX() + "," + item.getY() + ").";
     		statusOfCommand.setText(notice);
      	}
     	else {
     		notice = "Okay you will have the item soon.";
     		statusOfCommand.setText(notice);
     		pintoManager.unPauseTask(item);
     	}
     	potentialGetItemCancelationCommand = null;
     }
 
     /**
      * Sends the user's help request to the help desk.
      */
     public void performHelpDesk() {
         statusOfCommand.setText("Okay, your message was sent to the Help Desk.");
     }
 }
