 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.*;
 import static java.util.Calendar.*;
 import org.joda.time.MutableDateTime;
 import org.joda.time.Days;
 import org.joda.time.IllegalFieldValueException;
 
 class DriverHolidayViewNRScreen extends JFrame 
                                 implements ActionListener{
 
   //Declare variable
   int availableDays;
   int driverID;
   Holiday newHoliday = new Holiday();
 
   // Declare the components
   JLabel jLabelSelectedView, jLabelAvailableDays;
   JLabel jLabelError, jLabelSent; 
   JLabel jLabelStartDate, jLabelEndDate, jLabelDaysInTotal;
   JLabel jLabelSDate, jLabelSMonth, jLabelSYear;
   JLabel jLabelEDate, jLabelEMonth, jLabelEYear;
   JButton jBtnNRequest, jBtnRequests, jBtnNotifications;
   JButton jBtnSendRequest;
   JPanel contentPanel, submenuPanel, componentsPanel, mainContentPanel;
   JPanel startDatePanel, endDatePanel, sendButtonPanel;
   JMenuBar mainMenuBar, secondaryMenuBar;
   JMenu jMenuFile, jMenuView, jMenuNRequest;
   JMenuItem jMItemSave, jMItemPrint, jMItemExit;
   JMenuItem jMItemTimetable, jMItemHolidays;
   JComboBox jCBoxStartDates, jCBoxStartMonths; 
   JComboBox jCBoxStartYears, jCBoxEndDates;
   JComboBox jCBoxEndMonths, jCBoxEndYears;
 
   // Declare the colours
   Color layoutBgClr = new Color(255, 255, 255);
   Color lblFgClr = new Color(150, 150, 150);
   Color btnFgClr = new Color(100, 100, 100);
   Color btnBgClr = new Color(245, 245, 245);
   Color borderClr = new Color(225,225,225);
   Color lblBgClr = new Color(200,200,200);
   Color lblErrorFgClr = new Color(255, 51, 51);
   Color lblSentFgClr = new Color(26,127,26);
 
   // Declare the lists for the combo boxes
   Integer[] dates = {0, 1, 2, 3, 4, 5, 6, 7,
                      8, 9, 10, 11, 12, 13, 14, 15,
                     16, 17, 18, 19, 20, 21, 22, 23,
                     24, 25, 26, 27, 28, 29, 30, 31};
 
   Integer[] months = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
   
   Integer[] years = {0, 2013, 2014, 2015, 2016};
 
 
   public DriverHolidayViewNRScreen(String paramString, int requiredDriverID){
 
     setTitle(paramString);
  
     //Set the driverID and there avalable days
     driverID = requiredDriverID;
     availableDays = 25 - DriverInfo.getHolidaysTaken(driverID);
     
     //Create the menu bar
     mainMenuBar = new JMenuBar();
     mainMenuBar.setBackground(layoutBgClr);
 
     //Create the menus
     jMenuFile = new JMenu("File");
     jMenuFile.setMnemonic(70);
  
     jMenuView = new JMenu("View");
     jMenuView.setMnemonic(86);
 
     jLabelSelectedView = new JLabel("Selected View: Holidays");
     jLabelSelectedView.setForeground(lblFgClr);
     
     //Add the menus to the menu bar
     mainMenuBar.add(jMenuFile);
     mainMenuBar.add(jMenuView);
     mainMenuBar.add(Box.createHorizontalGlue());
     mainMenuBar.add(jLabelSelectedView);
  
     //Create the menu items
     jMItemSave = new JMenuItem("Save");
     jMItemPrint = new JMenuItem("Print");
     jMItemExit = new JMenuItem("Exit", 88);
     jMItemExit.setActionCommand("exit");
     jMItemExit.addActionListener(this);
  
     jMItemTimetable = new JMenuItem("Timetable", 84);
     jMItemTimetable.setActionCommand("timetable");
     jMItemTimetable.addActionListener(this);
  
     jMItemHolidays = new JMenuItem("Holidays", 72);
     jMItemHolidays.setActionCommand("holidays");
     jMItemHolidays.addActionListener(this);
  
     //Add the menu items to the menus
     jMenuFile.add(jMItemSave);
     jMenuFile.add(jMItemPrint);
     jMenuFile.add(jMItemExit);
     jMenuView.add(jMItemTimetable);
     jMenuView.add(jMItemHolidays);
  
     //Create the buttons
     jBtnNRequest = new JButton("New Request");
     jBtnNRequest.setOpaque(true);
     jBtnNRequest.setForeground(btnFgClr);
     jBtnNRequest.setBackground(lblBgClr);
     jBtnNRequest.setBorderPainted(false);
     jBtnNRequest.setHorizontalAlignment(SwingConstants.CENTER);
     jBtnNRequest.setActionCommand("newRequest");
     jBtnNRequest.addActionListener(this);
 
     jBtnRequests = new JButton("Requests");
     jBtnRequests.setForeground(btnFgClr);
     jBtnRequests.setBackground(layoutBgClr);
     jBtnRequests.setBorderPainted(false);
     jBtnRequests.setHorizontalAlignment(SwingConstants.CENTER);
     jBtnRequests.setActionCommand("requests");
     jBtnRequests.addActionListener(this);
 
     jBtnNotifications = new JButton("Notifications");
     jBtnNotifications.setForeground(btnFgClr);
     jBtnNotifications.setBackground(layoutBgClr);
     jBtnNotifications.setBorderPainted(false);
     jBtnNotifications.setHorizontalAlignment(SwingConstants.CENTER);
     jBtnNotifications.setActionCommand("notifications");
     jBtnNotifications.addActionListener(this);
 
     jBtnSendRequest = new JButton("Send Request");
     jBtnSendRequest.setForeground(btnFgClr);
     jBtnSendRequest.setBackground(btnBgClr);
     jBtnSendRequest.setEnabled(false);
     jBtnSendRequest.setHorizontalAlignment(SwingConstants.CENTER);
     jBtnSendRequest.setActionCommand("sendRequest");
     jBtnSendRequest.addActionListener(this);
     
     //Create the labels
     jLabelAvailableDays = new JLabel("Available Days: " + 
                                       availableDays);
     jLabelAvailableDays.setForeground(lblFgClr);
     jLabelAvailableDays.setHorizontalAlignment(SwingConstants.CENTER);
     jLabelError = new JLabel("Error: Please enter a valid date!"); 
     jLabelError.setForeground(this.lblErrorFgClr);
     jLabelError.setVisible(false);
     jLabelError.setHorizontalAlignment(0);
     jLabelSent = new JLabel("Request sent"); 
     jLabelSent.setForeground(this.lblSentFgClr);
     jLabelSent.setVisible(false);
     jLabelSent.setHorizontalAlignment(0);
     jLabelStartDate = new JLabel("Start Date");
     jLabelStartDate.setForeground(lblFgClr);
     jLabelStartDate.setBackground(layoutBgClr);
     jLabelStartDate.setHorizontalAlignment(SwingConstants.LEFT);
     
     jLabelEndDate = new JLabel("End Date");
     jLabelEndDate.setForeground(lblFgClr);
     jLabelEndDate.setBackground(layoutBgClr);
     jLabelEndDate.setHorizontalAlignment(SwingConstants.LEFT);
     
     jLabelDaysInTotal = new JLabel("Days in total: ");
     jLabelDaysInTotal.setForeground(lblFgClr);
     jLabelDaysInTotal.setBackground(layoutBgClr);
     jLabelDaysInTotal.setHorizontalAlignment(SwingConstants.LEFT);
 
     jLabelSDate = new JLabel("Date");
     jLabelSDate.setForeground(lblFgClr);
     jLabelSDate.setHorizontalAlignment(SwingConstants.LEFT);
 
     jLabelSMonth = new JLabel("Month");
     jLabelSMonth.setForeground(lblFgClr);
     jLabelSMonth.setHorizontalAlignment(SwingConstants.LEFT);
 
     jLabelSYear = new JLabel("Year");
     jLabelSYear.setForeground(lblFgClr);
     jLabelSYear.setHorizontalAlignment(SwingConstants.LEFT);
     
     jLabelEDate = new JLabel("Date");
     jLabelEDate.setForeground(lblFgClr);
     jLabelEDate.setHorizontalAlignment(SwingConstants.LEFT);
 
     jLabelEMonth = new JLabel("Month");
     jLabelEMonth.setForeground(lblFgClr);
     jLabelEMonth.setHorizontalAlignment(SwingConstants.LEFT);
 
     jLabelEYear = new JLabel("Year");
     jLabelEYear.setForeground(lblFgClr);
     jLabelEYear.setHorizontalAlignment(SwingConstants.LEFT);
 
     //Create the dropdowns
     jCBoxStartDates = new JComboBox(dates);
     jCBoxStartDates.setSelectedIndex(0);
     jCBoxStartDates.setForeground(lblFgClr);
     jCBoxStartDates.setBackground(layoutBgClr);
     jCBoxStartDates.setActionCommand("sdates");
     jCBoxStartDates.addActionListener(this);
 
     jCBoxStartMonths = new JComboBox(months);
     jCBoxStartMonths.setSelectedIndex(0);
     jCBoxStartMonths.setForeground(lblFgClr);
     jCBoxStartMonths.setBackground(layoutBgClr);
     jCBoxStartMonths.setActionCommand("smonths");
     jCBoxStartMonths.addActionListener(this);
 
     jCBoxStartYears = new JComboBox(years);
     jCBoxStartYears.setSelectedIndex(0);
     jCBoxStartYears.setForeground(lblFgClr);
     jCBoxStartYears.setBackground(layoutBgClr);
     jCBoxStartYears.setActionCommand("syears");
     jCBoxStartYears.addActionListener(this);
 
     jCBoxEndDates = new JComboBox(dates);
     jCBoxEndDates.setSelectedIndex(0);
     jCBoxEndDates.setForeground(lblFgClr);
     jCBoxEndDates.setBackground(layoutBgClr);
     jCBoxEndDates.setActionCommand("edates");
     jCBoxEndDates.addActionListener(this);
 
     jCBoxEndMonths = new JComboBox(months);
     jCBoxEndMonths.setSelectedIndex(0);
     jCBoxEndMonths.setForeground(lblFgClr);
     jCBoxEndMonths.setBackground(layoutBgClr);
     jCBoxEndMonths.setActionCommand("emonths");
     jCBoxEndMonths.addActionListener(this);
 
     jCBoxEndYears = new JComboBox(years);
     jCBoxEndYears.setSelectedIndex(0);
     jCBoxEndYears.setForeground(lblFgClr);
     jCBoxEndYears.setBackground(layoutBgClr);
     jCBoxEndYears.setActionCommand("eyears");
     jCBoxEndYears.addActionListener(this);
   
     //Create the content panel
     contentPanel = new JPanel();
     contentPanel.setPreferredSize(new Dimension(600, 300));
     contentPanel.setLayout(new BorderLayout());
     contentPanel.setBackground(layoutBgClr);
 
     //Create the submenuPanel
     submenuPanel = new JPanel();
     submenuPanel.setPreferredSize(new Dimension(600, 23));
     submenuPanel.setLayout(new GridLayout(1,4));
     submenuPanel.setBackground(layoutBgClr);
     submenuPanel.setBorder(BorderFactory.createMatteBorder(0,0,2,0, 
                                          borderClr));
 
     //Create the components panel
     componentsPanel = new JPanel();
     componentsPanel.setLayout(new FlowLayout());
     componentsPanel.setBackground(layoutBgClr);
 
     //Create the mainContent panel
     mainContentPanel = new JPanel();
     mainContentPanel.setPreferredSize(new Dimension(400, 270));
     mainContentPanel.setLayout(new GridLayout(8,1));
     mainContentPanel.setBackground(layoutBgClr);
 
     //Create the startDate panel
     startDatePanel = new JPanel();
     startDatePanel.setLayout(new FlowLayout());
     startDatePanel.setBackground(layoutBgClr); 
 
     //Create the endDate panel
     endDatePanel = new JPanel();
     endDatePanel.setLayout(new FlowLayout());
     endDatePanel.setBackground(layoutBgClr);
 
     //Create the sendButton panel
     sendButtonPanel = new JPanel();
     sendButtonPanel.setLayout(new FlowLayout());
     sendButtonPanel.setBackground(layoutBgClr);
     
     //Add the components to the submenuPanel
     submenuPanel.add(jBtnNRequest);
     submenuPanel.add(jBtnRequests);
     submenuPanel.add(jBtnNotifications);
     submenuPanel.add(jLabelAvailableDays);
 
     //Add the components to the startDatePanel
     startDatePanel.add(jLabelSDate);
     startDatePanel.add(jCBoxStartDates);
     startDatePanel.add(jLabelSMonth);
     startDatePanel.add(jCBoxStartMonths);
     startDatePanel.add(jLabelSYear);
     startDatePanel.add(jCBoxStartYears);
 
     //Add the components to the endDatePanel
     endDatePanel.add(jLabelEDate);
     endDatePanel.add(jCBoxEndDates);
     endDatePanel.add(jLabelEMonth);
     endDatePanel.add(jCBoxEndMonths);
     endDatePanel.add(jLabelEYear);
     endDatePanel.add(jCBoxEndYears);
 
     //Add the sendRequest button to the sendButtonPanel
     sendButtonPanel.add(jBtnSendRequest);
     
 
     //Add the components to the mainContentPanel
     mainContentPanel.add(jLabelStartDate);
     mainContentPanel.add(startDatePanel);
     mainContentPanel.add(jLabelEndDate);
     mainContentPanel.add(endDatePanel);
     mainContentPanel.add(jLabelDaysInTotal);
     mainContentPanel.add(jLabelError);
     mainContentPanel.add(sendButtonPanel);
     mainContentPanel.add(jLabelSent);
     
     //Add the components to the componentsPanel
     componentsPanel.add(Box.createHorizontalGlue());
     componentsPanel.add(mainContentPanel);
     componentsPanel.add(Box.createHorizontalGlue());
     
     //Add the panels to the content panel
     contentPanel.add(submenuPanel, BorderLayout.PAGE_START);
     contentPanel.add(componentsPanel, BorderLayout.CENTER);
     
  
     //Resize and position the window
     Dimension localDimension = Toolkit.getDefaultToolkit().getScreenSize();
 
     int i = getSize().width;
     int j = getSize().height;
     int k = (localDimension.width - i) / 2 - 300;
     int m = (localDimension.height - j) / 2 - 150;
  
     this.setResizable(false);
  
     //Locate the window
     this.setLocation(k, m);
  
     this.setJMenuBar(mainMenuBar);
     this.setContentPane(contentPanel);
     this.pack();
     this.setVisible(true);
  
     //Define the action on closing the window
     addWindowListener(new WindowAdapter(){
 
       public void windowClosing(WindowEvent paramAnonymousWindowEvent){
 
          System.exit(0);
       }//windowClosing
     });//addWindowListener
   }//constructor
  
   //Catch the click events
   public void actionPerformed(ActionEvent paramActionEvent){
 
     String actionCmd = paramActionEvent.getActionCommand();
     String title = "G7 - IBMS System | Driver";
     Boolean valid = false;
     Boolean reqSent = false;
 
     if ("exit".equals(actionCmd)){
       System.exit(0);
     }
     else if ("timetable".equals(actionCmd)){
       this.dispose();
       new DriverTimetableViewScreen(title, driverID);
     }
     else if ("holidays".equals(actionCmd)){
       this.dispose();
       new DriverHolidayViewNRScreen(title, driverID);
     }
     else if ("newRequest".equals(actionCmd)){
       this.dispose();
       new DriverHolidayViewNRScreen(title, driverID);
     }
     else if ("requests".equals(actionCmd)){
       this.dispose();
       new DriverHolidayViewRScreen(title, driverID);
     }
     else if ("notifications".equals(actionCmd)){
       this.dispose();
       new DriverHolidayViewNScreen(title, driverID);
     }
     else if ("sdates".equals(actionCmd) || "smonths".equals(actionCmd) ||
              "syears".equals(actionCmd) || "edates".equals(actionCmd) ||
              "emonths".equals(actionCmd) || "eyears".equals(actionCmd)){
              
       
       int startDay = (Integer)jCBoxStartDates.getSelectedItem();
       int startMonth = (Integer)jCBoxStartMonths.getSelectedItem();
       int startYear = (Integer)jCBoxStartYears.getSelectedItem();
       
       int endDay = (Integer)jCBoxEndDates.getSelectedItem();
       int endMonth = (Integer)jCBoxEndMonths.getSelectedItem();
       int endYear = (Integer)jCBoxEndYears.getSelectedItem();
       
       if(startDay == 0 || startMonth == 0 || startYear == 0 ||
          endDay == 0 || endMonth == 0 || endYear == 0)
       {
         jBtnSendRequest.setEnabled(false);
       }
       else
       {
      	  try
      	  {
      	    this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
           newHoliday = new Holiday(startDay, startMonth, startYear,
 	  			                         endDay, endMonth, endYear);
 	  		  newHoliday.checkIfOnHolidayAlready(driverID);
 
           //Check if the noOFDays > availableDays
           if(newHoliday.getNoOfDays() > availableDays){
               valid = false;
               jBtnSendRequest.setEnabled(false);
	  		      jLabelDaysInTotal.setText("Days in total: "+ newHoliday.getNoOfDays());
           }else{
               valid = true;
               jBtnSendRequest.setEnabled(true);
           }//else
 
 	  		}
 	  		catch(HolidayException e)
 	  		{
 	  		  jLabelError.setText("Error: "+ e.getMessage());
           jLabelError.setVisible(true);
           jBtnSendRequest.setEnabled(false);
           valid = false;
 	  		}
 	  		catch(InvalidQueryException e)
 	  		{
 	  		  jLabelError.setText("Error: "+ e.getMessage());
           jLabelError.setVisible(true);
           jBtnSendRequest.setEnabled(false);
           valid = false;
 	  		}
 	  		finally
 	  		{ 
 	  			this.setCursor(Cursor.getDefaultCursor());
 	  		}
 
         if(valid){
 
 	  		  jLabelDaysInTotal.setText("Days in total: "+ newHoliday.getNoOfDays());
           jLabelError.setVisible(false);
           jBtnSendRequest.setEnabled(true);
         }
           
            
       }//else
       
     }//else if
 
     //If the button is enabled and pressed, send the request
     if("sendRequest".equals(actionCmd)){
       try
       {
      	  this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
         newHoliday.saveToDB(driverID);
         reqSent = true;
       }
       catch(HolidayException e)
 	  	{
 	  	  jLabelError.setText("Error: "+ e.getMessage());
          jLabelError.setVisible(true);
          jBtnSendRequest.setEnabled(false);
          valid = false;
 	  	}
 	  	catch(InvalidQueryException e)
 	  	{
 	  	  jLabelError.setText("Error: "+ e.getMessage());
         jLabelError.setVisible(true);
         jBtnSendRequest.setEnabled(false);
         valid = false;
 	  	}
 	  	finally
 	  	{ 
 	  		this.setCursor(Cursor.getDefaultCursor());
 	  	}
 	  	if(reqSent)
 	  	{
       	jLabelSent.setText("Request sent.");
       	jLabelSent.setVisible(true);
         dispose();
         new DriverHolidayAckScreen(title, driverID);
       }
     }
 
 
   }//actionPerformed
 
 }//class
 
