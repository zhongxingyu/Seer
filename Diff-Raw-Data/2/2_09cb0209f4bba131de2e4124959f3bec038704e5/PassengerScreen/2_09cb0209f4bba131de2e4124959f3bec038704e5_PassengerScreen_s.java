 import java.awt.*;
 import java.awt.event.*;
 import java.util.ArrayList;
 import javax.swing.*;
  
 class PassengerScreen extends JFrame implements ActionListener{
 
   //Declare the components 
   JLabel jLabelWelcome, jLabelChoose, titleLabel, timeLbl1, timeLbl2, timeLbl3, timeLbl4, timeLbl5;
   JLabel[] timeLbls;
   JButton jBtnDriver, jBtnPassanger, refreshBtn;
   JPanel contentPanel, jButtonsPanel;
   JComboBox JComboBoxBusList, JComboBoxRoutesList;
   String[] busStops;
   int[] routes;
   int routesCBoxPressedTimes = 0;
   String routeId, busStation;
   boolean refreshPressed = false;
 
   //Declare the colors
   Color layoutBgClr = new Color(255, 255, 255);
   Color lblFgClr = new Color(150, 150, 150);
   Color btnBgClr = new Color(245, 245, 245);
   Color btnFgClr = new Color(130, 130, 130);
  
   public PassengerScreen(String paramString){
 
     this.setTitle(paramString);
     
     database.openBusDatabase();
     busStops = BusStopInfo.getBusStops();
     
     routes = BusStopInfo.getRoutes();
     String[] finalRoutes = new String[routes.length];
     for (int i = 0; i<routes.length; i++)
     	finalRoutes[i] = Integer.toString(routes[i]);
     
     JComboBoxRoutesList = new JComboBox(finalRoutes);
     JComboBoxRoutesList.setEditable(false);
     JComboBoxRoutesList.setActionCommand("route");
     JComboBoxRoutesList.addActionListener(this);  
     
     JComboBoxBusList = new JComboBox();
     JComboBoxBusList.setEditable(false);
     JComboBoxBusList.setActionCommand("selectStop");
     JComboBoxBusList.addActionListener(this);    	
     
     //Create the content panel
     contentPanel = new JPanel();
     contentPanel.setPreferredSize(new Dimension(550, 200));
    contentPanel.setLayout(new GridLayout(11, 0));
     contentPanel.setBackground(this.layoutBgClr);
  
     //Create the labels
     jLabelWelcome = new JLabel("Welcome to G7 - IBMS System");
     jLabelWelcome.setForeground(this.lblFgClr);
     jLabelChoose = new JLabel("Choose the bus stop for which you want to display next 5 buses arriving: ");
     jLabelChoose.setForeground(this.lblFgClr);
     
     //Align the labels
     jLabelWelcome.setHorizontalAlignment(0);
     jLabelChoose.setHorizontalAlignment(0);
     
     //Create the buttons panel
     jButtonsPanel = new JPanel(new FlowLayout(10, 65, 20));
     jButtonsPanel.setBackground(this.layoutBgClr);
 
     refreshBtn = new JButton("Refresh");
     refreshBtn.setForeground(btnFgClr);
     refreshBtn.setBackground(btnBgClr);
     refreshBtn.setActionCommand("refresh");
     refreshBtn.addActionListener(this);
 
     titleLabel = new JLabel();
     timeLbl1 = new JLabel();
     timeLbl2 = new JLabel();
     timeLbl3 = new JLabel();
     timeLbl4 = new JLabel();
     timeLbl5 = new JLabel();
 
     timeLbls = new JLabel[5];
     
     timeLbls[0] = timeLbl1;
     timeLbls[1] = timeLbl2;
     timeLbls[2] = timeLbl3;
     timeLbls[3] = timeLbl4;
     timeLbls[4] = timeLbl5;
 
     //Add the components to the content panel
     contentPanel.add(this.jLabelWelcome);
     contentPanel.add(this.jLabelChoose);
     contentPanel.add(this.JComboBoxRoutesList);
     contentPanel.add(this.JComboBoxBusList);
     contentPanel.add(this.jButtonsPanel);
     contentPanel.add(titleLabel);
     
     for(int i = 0; i < 5; i++)
       contentPanel.add(timeLbls[i]);
  
     
     contentPanel.add(refreshBtn);
 
 
     //Resize and position the window
     Dimension localDimension = Toolkit.getDefaultToolkit().getScreenSize();
  
     int i = getSize().width;
     int j = getSize().height;
     int k = (localDimension.width - i) / 2 - 300;
     int m = (localDimension.height - j) / 2 - 150;
  
     this.setResizable(false);
  
     //Locate the window
     this.setLocation(k, m);
 
     this.setContentPane(this.contentPanel);
     this.pack();
     this.setVisible(true);
  
     //Define the action on closing the window
     addWindowListener(new WindowAdapter(){
     
       public void windowClosing(WindowEvent paramAnonymousWindowEvent){
          System.exit(0);
       }//windowClosing
     });//addWindowListener
 
     
     if(routeId != null && busStation != null){
       JComboBoxRoutesList.setSelectedItem(routeId);
       JComboBoxBusList.setSelectedItem(busStation);
     }
 
 
   }//constructor
  
   //Catch the click events
   public void actionPerformed(ActionEvent paramActionEvent){
 
   	if ("selectStop".equals(paramActionEvent.getActionCommand()))
   	{
       System.out.println("slected a bus stop");
 	  	JComboBox cb = (JComboBox)paramActionEvent.getSource();
 	  	//busStation = (String)JComboBoxBusList.getSelectedItem();
 	  	if (refreshPressed)
 	  	{
 	  		JComboBoxBusList.setSelectedItem(busStation);
 	  	  refreshPressed = false;
 	  	  repaint();
 	  	}
 	  	else
 	  	{
 	  		JComboBoxBusList.setSelectedItem((String)JComboBoxBusList.getSelectedItem());
 	  		repaint();
 	  	}
 	  	/*if (busStation != (String)JComboBoxBusList.getSelectedItem() && busStation != null){
 	  		  JComboBoxBusList.setSelectedItem(busStation);
 		      repaint();
 	  	}*/
       
 	  	String stopName = (String)cb.getSelectedItem();
       System.out.println("stopName selected: " + stopName);
     
       if(stopName != null){
 	    
         String[] tokens = stopName.split(" ");
 	  
         int stopID = Integer.parseInt(tokens[0]);
 	      String[] result = BusStopInfo.display5buses(stopID);
 
         System.out.println("Selected bus stop: " + stopID + " " + stopName);
 
         titleLabel.setText("Service        Time");
         titleLabel.setForeground(this.lblFgClr);
 
         System.out.println("Buses' time:");
      
         for(int i = 0; i < result.length; i++){
     
           System.out.println("Bus " + (i + 1) + " " + result[i]);
         
           timeLbls[i].setText(result[i]);
 
           timeLbls[i].setForeground(this.lblFgClr);
       
         }//for
 
         for(int i = result.length; i < 5; i++){
       
           timeLbls[i].setText("");
 
         }
 
         if(result.length == 0)
           timeLbls[0].setText("There are no buses to display for this stop.");
 
       if(busStation != null && routeId != JComboBoxRoutesList.getSelectedItem()){
         JComboBoxBusList.setSelectedItem(busStation);
         JComboBoxBusList.revalidate();
         repaint();
       }
 
       }//if
     }else if ("route".equals(paramActionEvent.getActionCommand())){
 
       System.out.println("Selected a route");
 
 	  	JComboBox cb = (JComboBox)paramActionEvent.getSource();
 	  	
 	    String route = (String)cb.getSelectedItem();
 	    int routeID = Integer.parseInt(route);
 	    int[] finalBusStops = BusStopInfo.getBusStops(routeID);
 
 	    for (int i = 0; i<finalBusStops.length; i++)
 	      busStops[i] = Integer.toString(finalBusStops[i]);
 
 	    for (int i = 0; i<finalBusStops.length; i++)
 	    	System.out.println(busStops[i]);
 
 	    String[] busStopNames = new String[busStops.length]; 
 	    String[] allBusStops = BusStopInfo.getBusStops();
 	    
       System.out.println("Selected route: " + routeID + " " + route);
 
 	    //This should compare the array of all bus stops which contains both id and name with the one which
 	    //only contains ids. if the small array element is contained in the bigger array, the busStopNames should be
 	    //the same as the big array element.
 	    for (int j = 0; j<allBusStops.length; j++){
 	    
         for (int i = 0; i<busStops.length; i++){
 	    	  
           if (allBusStops[j].contains(busStops[i])){ // this should work but doesn't!!!!!!!!!!!!!!!! WORKS
  	    	
             //System.out.println("BUS: " + allBusStops[j] + " contains: " + busStops[i]);
             busStopNames[i] = allBusStops[j];  // same about this!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! WORKS
 	    
           }//if
         }//for
       }//for
 
       routesCBoxPressedTimes++;
 
       if(routesCBoxPressedTimes > 1){
         JComboBoxBusList.removeAllItems();
         System.out.println("Items removed!");
       }
 	    
 
 	    for (int j = 0; j<busStopNames.length;j++)
 	    	System.out.println(busStopNames[j]);
 	    
 	    for (int i = 0; i<finalBusStops.length; i++)
 	    {
 	      JComboBoxBusList.addItem(busStopNames[i]);
 	      System.out.println(busStopNames[i] + " added");
 
 
 	    }
 
 
 	    int combosize = JComboBoxBusList.getItemCount();
 	    System.out.println(combosize);
 
 	    System.out.println("---------------");
   	
     }else if("refresh".equals(paramActionEvent.getActionCommand())){
 
       System.out.println("----------------------------Refresh btn pressed!");
 
       routeId = (String)JComboBoxRoutesList.getSelectedItem();
       busStation = (String)JComboBoxBusList.getSelectedItem();
       refreshPressed = true;
       ActionEvent e1 = new ActionEvent(JComboBoxRoutesList, ActionEvent.ACTION_PERFORMED, "route");  
       this.actionPerformed(e1);
       ActionEvent e2 = new ActionEvent(JComboBoxBusList, ActionEvent.ACTION_PERFORMED, "selectStop");
       this.actionPerformed(e2);
       contentPanel.revalidate();
       JComboBoxBusList.setSelectedItem(busStation);
       repaint();
       
       //   new PassengerScreen("G7 - IBMS System | Passenger - Information");
   
     }else{
        dispose();
        new PassengerScreen("G7 - IBMS System | Passenger - Information");
     }
   }//actionPerformed
 
       
 
 }
