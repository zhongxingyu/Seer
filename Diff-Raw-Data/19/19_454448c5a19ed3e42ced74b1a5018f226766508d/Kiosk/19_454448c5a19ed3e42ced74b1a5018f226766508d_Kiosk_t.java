 package HW4;
 /**
  * This should be a single instance of a kiosk. Should be run as it's own
  * thread. May use its own worker threads to make the GUI more interactive.
  * 
  * A question: Is the Kiosk our primary UI through which all other functions are accessed?
  * It seems like it is but this is not documented anywhere and I'm working under the assumption that it is.
  */
 
 import java.awt.*;
 import java.awt.event.*;
 import java.beans.PropertyVetoException;
 import java.io.*;
 import java.lang.management.ManagementFactory;
 import java.text.DecimalFormat;
 import java.util.*;
 
 import javax.swing.*;
 import javax.swing.event.InternalFrameEvent;
 import javax.swing.event.InternalFrameListener;
 
 
 public class Kiosk {
 	public static int kioskID = 0;
 //	public static Restaurant restaurant = new Restaurant("CSU Rams"); //#TODO:how do we initialize restaurant kiosk is in? Load via a (String fileName) constructor or have a default restaurant?
 	private static GridBagConstraints gBC = new GridBagConstraints();
 	private static JFrame frame = new JFrame("PIZZA_STORE_NAME Kiosk: " + kioskID);
 	private static Order o = new Order() ;
 	private static ArrayList<Manager> managerList = new ArrayList<Manager>();
 	private static ArrayList<Menu> menuList = new ArrayList<Menu>();
 	private static Menu menu;
 	private static JLabel currentMenu;
 	private static JDesktopPane desktop;
 	private static ArrayList<JButton>menuItemButtons = new ArrayList<JButton>();
 	private static ArrayList<JButton>orderButtons = new ArrayList<JButton>();
 	private static KioskFacade kFacade = new KioskFacade();
 	public static Restaurant restaurant = kFacade.getRestaurant();
 	private static String[] args;
 
 
 	private static void createAndShowGUI(final String[] args) {
 		// variables
 		Color background = new Color(240, 125, 110) ;
 		final Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
 		Random generator = new Random(System.currentTimeMillis()) ;
 		o.orderID = generator.nextInt() ;
 		File txtFile = new File(Integer.toString(o.orderID));
 		while (txtFile.exists()) {  // the file already exists
 			o.orderID = generator.nextInt() ;
 			txtFile = new File(Integer.toString(o.orderID)) ;
 		}
 
 		// Create and set up the window.
 		//TODO: activate next line so that user cannot close window (removed for now because it makes testing hard)
 		//frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
 		desktop = new JDesktopPane();
 		frame.setContentPane(desktop) ;
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		desktop.setBackground(background) ;
 		frame.setMinimumSize(size) ;
 		desktop.setLayout(new GridBagLayout()); // this is the framing structure
 		gBC.fill = GridBagConstraints.HORIZONTAL;
 
 		//////// REMOVE - for building purposes ///////////
 //				Manager bob = new Manager("BOB", new Restaurant()) ;
 //				menu = new Menu("Breakfast", bob) ;
 //		
 //				MenuItem eggs = new MenuItem("Eggs", 5.99) ;
 //				MenuItem pancakes = new MenuItem("Pancakes", 6.99) ;
 //				MenuItem toast = new MenuItem("Toast and Jam", 3.99) ;
 //				MenuItem burrito = new MenuItem("Burrito", 6.99) ;
 //				MenuItem frenchToast = new MenuItem("French Toast", 5.99) ;
 //				MenuItem omelet = new MenuItem("Omelet", 7.99) ;
 //				menu.addMenuItem(eggs) ;
 //				menu.addMenuItem(pancakes) ;
 //				menu.addMenuItem(toast) ;
 //				menu.addMenuItem(burrito) ;
 //				menu.addMenuItem(frenchToast) ;
 //				menu.addMenuItem(omelet) ;
 //				menuList.add(menu);
 
 		loadMenu();
 		///////////////////////////////////////////////////
 
 		//###############  CONTENT  ######################
 		gBC.gridx = 0 ;
 		gBC.gridy = -1 ;
 		gBC.weightx = 0.5 ;
 		gBC.weighty = 0.25 ;
 
 		//////////Manager Window//////////////
 		JLabel loginLabel = new JLabel("Manager Name");
 		final JTextField loginTextField = new JTextField(15);
 		JButton submitLogInButton = new JButton("Submit");
 		submitLogInButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				boolean found = false;
 				for(Manager manager : managerList) {
 					if(manager.getName() == loginTextField.getText()) {
 						found = true;
 						showManagerWindow(manager);
 					}
 				}
 				if(!found){
 					Manager manager = new Manager("Ricky Bobby", restaurant);
 					showManagerWindow(manager);
 				}
 			}
 		});
 		desktop.add(loginLabel);
 		desktop.add(loginTextField);
 		desktop.add(submitLogInButton);
 		currentMenu = new JLabel("Current Menu: " + kFacade.getMenuName());
 		desktop.add(currentMenu);
 		gBC.gridy += 1;
 		//-- I am not 100% sure on the gBC, so if the above code messes things up let Zach know --
 		drawMenuItemButtons();
 //		if(menu != null){
 //			ArrayList<String> menuItemNames = kFacade.getCurrentMenuItemNames();
 //			ArrayList<Double> menuItemCosts = kFacade.getCurrentMenuItemPrices();
 //			for (int i=0; i< menuItemNames.size(); i++) {
 //				final MenuItem item = menu.getMenuItems().get(i) ;
 //				JButton b = new JButton(menuItemNames.get(i) + "\n $" + menuItemCosts.get(i)) ;
 //				b.setActionCommand(menuItemNames.get(i));
 //				b.setPreferredSize(new Dimension(150, 100));
 //				// set action
 //				b.addActionListener(new ActionListener() {
 //					public void actionPerformed(ActionEvent e) {
 //						o.addItem(item) ; // need to write order handler for facade, something that translates and grabs proper item
 //					}
 //				}) ;
 //				gBC.gridx = i%5 ;
 //				if ((i%5) == 0)
 //					gBC.gridy += 1 ;
 //				desktop.add(b, gBC) ;
 //				menuItemButtons.add(b);
 //			}
 //		}
 
 		// ******* adding bottom bar **********
 //		frame.getContentPane().add(new JSeparator()) ;
 //		JButton viewOrder = new JButton("View Order") ;
 //		JButton placeOrder = new JButton("Place Order") ;
 //		viewOrder.setPreferredSize(new Dimension(150, 100));
 //		viewOrder.setBackground(Color.green) ;
 //		placeOrder.setPreferredSize(new Dimension(150, 100));
 //		placeOrder.setBackground(Color.blue) ;
 //		// actions
 //		viewOrder.addActionListener(new ActionListener() {
 //			public void actionPerformed(ActionEvent e)
 //			{
 //				//Execute when button is pressed
 //				JInternalFrame internalFrame = new JInternalFrame("Order Summary", false, true, false, false);
 //				internalFrame.setBounds(0, 50, 500, size.height);
 //				internalFrame.setBackground(new Color(225, 225, 255)) ;
 //				gBC.ipady = size.height-100;
 //				gBC.weightx = 0 ;
 //				gBC.gridwidth = 4;
 //				gBC.gridx = 0 ;
 //				gBC.gridy = 0 ;
 //				// adding content to internal frame
 //				JLabel receipt = getReceipt() ;
 //				internalFrame.add(receipt) ;
 //				// displaying frame and bringing to front
 //				internalFrame.show() ;
 //				frame.add(internalFrame, gBC) ;
 //				internalFrame.setVisible(true);
 //				try {
 //					internalFrame.setSelected(true) ;
 //				} catch (PropertyVetoException e1) {
 //					e1.printStackTrace();
 //				}
 //			}
 //		});
 //		placeOrder.addActionListener(new ActionListener() {
 //			public void actionPerformed(ActionEvent e)
 //			{
 //				//Execute when button is pressed
 //				JDesktopPane desktop2 = new JDesktopPane() ;
 //				desktop2.setLayout(new GridBagLayout());
 //				JLabel receipt = getReceipt() ;
 //				gBC.ipadx = size.width/2 ;
 //				gBC.gridheight = 8;
 //				gBC.gridx = 0 ;
 //				gBC.gridy = 0 ;
 //				desktop2.add(receipt, gBC) ;
 //
 //				JLabel txt = new JLabel("<html><h1>Fill in all fields: </h1></html>") ;
 //				gBC.gridheight = 1 ;
 //				gBC.gridx = 1 ;
 //				desktop2.add(txt, gBC) ;
 //
 //				final JTextField name = new JTextField("First and Last Name") ;
 //				gBC.gridy = 1 ;
 //				desktop2.add(name, gBC) ;
 //
 //				final JTextField address = new JTextField("Address") ;
 //				gBC.gridy = 2 ;
 //				desktop2.add(address, gBC) ;
 //
 //				final JTextField phone = new JTextField("Phone Number") ;
 //				gBC.gridy = 3 ;
 //				desktop2.add(phone, gBC) ;
 //
 //				txt = new JLabel("<html><h3>Payment Information: </h3></html>") ;
 //				gBC.gridy = 4 ;
 //				desktop2.add(txt, gBC) ;
 //
 //				final JTextField cardName = new JTextField("Name on Card") ;
 //				gBC.gridy = 5 ;
 //				desktop2.add(cardName, gBC) ;
 //
 //				final JTextField cardNum = new JTextField("Credit Card Number", 16) ;
 //				gBC.gridy = 6 ;
 //				desktop2.add(cardNum, gBC) ;
 //
 //				final JTextField expDate = new JTextField("Expiration Date") ;
 //				gBC.gridy = 7 ;
 //				desktop2.add(expDate, gBC) ;
 //
 //				final JButton place = new JButton("Place Order") ;
 //				place.setBackground(Color.green) ;
 //				place.setPreferredSize(new Dimension(150, 100));
 //				gBC.gridy = 8 ;
 //
 //				//TODO: place order button needs to store all information and send it to the kitchen display
 //				place.addActionListener(new ActionListener() {
 //					public void actionPerformed(ActionEvent e) {
 //						// This is all the error checking to make sure the inputs are correct
 //						if(name.getText().equals("First and Last Name") || address.getText().equals("Address") ||
 //								phone.getText().equals("Phone Number") || cardName.getText().equals("Name on Card") ||
 //								cardNum.getText().equals("Credit Card Number") || expDate.getText().equals("Expiration Date"))
 //							JOptionPane.showOptionDialog(frame, "You must fill in all fields.", "Error", JOptionPane.DEFAULT_OPTION, 
 //									JOptionPane.ERROR_MESSAGE, null, null, e) ;
 //						else {
 //							if(phone.getText().length() != 10)
 //								JOptionPane.showOptionDialog(frame, "Invalid phone number. Use Format: 9991234567.", "Error", JOptionPane.DEFAULT_OPTION, 
 //										JOptionPane.ERROR_MESSAGE, null, null, e) ;
 //							else{
 //								if(cardNum.getText().length() != 16)
 //									JOptionPane.showOptionDialog(frame, "Invalid Card Number. Use Format: 1234567890123456", "Error", JOptionPane.DEFAULT_OPTION, 
 //											JOptionPane.ERROR_MESSAGE, null, null, e) ;
 //								else {
 //									//TODO: check for validity of expiration date
 //									// Theoretically this is where we would do card authorization
 //									JOptionPane.showOptionDialog(frame, "Your order has been placed.", "Order Placed", JOptionPane.DEFAULT_OPTION, 
 //											JOptionPane.PLAIN_MESSAGE, null, null, e) ;
 //									// creating the output file for the kitchen display to read
 //									String order = "" ;
 //									ArrayList<OrderItem> orderItems = o.getOrderList() ;
 //									for(int i=0 ; i<orderItems.size(); i++){
 //										order += orderItems.get(i).getItem().name+"\t\t"+orderItems.get(i).quantity+"\n";
 //									}
 //									FileWriter fstream;
 //									try {
 //										fstream = new FileWriter(Integer.toString(o.orderID)+".POS");
 //										BufferedWriter out = new BufferedWriter(fstream);
 //										out.write(name.getText()+"\n"+address.getText()+"\n"+phone.getText()+"\n"+order+"\n\n");
 //										//Close the output stream
 //										out.close();
 //									} catch (IOException e1) {
 //										e1.printStackTrace();
 //									}
 //									restartProgram(args) ;
 //								}
 //							}
 //						}
 //					}
 //				}) ;
 //
 //				desktop2.add(place, gBC) ;
 //
 //				desktop2.setVisible(true) ;
 //				frame.setContentPane(desktop2) ;
 //				frame.setVisible(true) ;
 //			}
 //		});
 //		gBC.gridy += 1 ;
 //		gBC.gridx = 0 ;
 //		desktop.add(viewOrder, gBC) ;
 //		gBC.gridx = 1 ;
 //		desktop.add(placeOrder, gBC) ;
 
 
 		/////// THIS SECTION WAS DEPRICATED - allows user to switch between menus ///////
 		// top line
 		/*JLabel label = new JLabel("Select Menu: ");
         gBC.weightx = 0.2 ;
         gBC.gridx = 0 ;
         gBC.gridy = 0 ;
         frame.getContentPane().add(label, gBC);
 
         // drop down to select menu
         ArrayList<Menu>menuList = restaurant.getMenuList();
         String[] menuNames = new String[menuList.size()+1];
         menuNames[0] = "Select a Menu";
         for(int i = 0; i < menuList.size(); i++){
         	menuNames[i+1] = menuList.get(i).menuName;
         }
         JComboBox menusList = new JComboBox(menuNames);
         menusList.setSelectedIndex(0);
         gBC.weightx = 0.8 ;
         gBC.gridx = 1;
         gBC.gridy = 0;
         menusList.addActionListener(new ActionListener() {
         	public void actionPerformed(ActionEvent e)
             {
         		JComboBox cb = (JComboBox)e.getSource();
                 String menuName = (String)cb.getSelectedItem();
                 if(!menuName.equals("Select a Menu")){
                 	JInternalFrame internalFrame = new JInternalFrame(menuName, false, true, false, false);
                 	internalFrame.setBounds(0, 50, size.width, size.height);
                 	internalFrame.setVisible(true);
                 	internalFrame.show();
                 	gBC.ipady = size.height-100;
                 	gBC.weightx = 0 ;
                 	gBC.gridwidth = 2;
                 	gBC.gridx = 0 ;
                 	gBC.gridy = 1 ;
                 	frame.getContentPane().add(internalFrame, gBC) ;
                 }
             }}) ;
         frame.getContentPane().add(menusList, gBC) ;
 
 
         // fill empty space
         gBC.ipady = size.height-100 ;
         gBC.weightx = 0 ;
         gBC.gridwidth = 3 ;
         gBC.gridx = 0 ;
         gBC.gridy = 1 ;
         frame.getContentPane().add(Box.createGlue(), gBC) ;*/
 		////////////////////////////////////////////////////////////////////////////////
 
 
 		//Display the window.
 		frame.pack();
 		frame.setVisible(true);
 	}
 
 	public static void restartProgram(String[] args)
 	{
 		StringBuilder cmd = new StringBuilder();
 		cmd.append(System.getProperty("java.home") + File.separator + "bin" + File.separator + "java ");
 		for (String jvmArg : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
 			cmd.append(jvmArg + " ");
 		}
 		cmd.append("-cp ").append(ManagementFactory.getRuntimeMXBean().getClassPath()).append(" ");
 		cmd.append(Kiosk.class.getName()).append(" ");
 		for (String arg : args) {
 			cmd.append(arg).append(" ");
 		}
 		try {
 			Runtime.getRuntime().exec(cmd.toString());
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		System.exit(0);
 	}
 
 	private static void loadMenu() {
 		String line;
 		int lineNumber = 0;
 		try {
 			FileInputStream inFile = new FileInputStream("menus.POS_MENU");
 			BufferedReader content = new BufferedReader(new InputStreamReader(inFile));
 			Menu loadMenu = null;
 			while((line = content.readLine()) != null){
 				if(lineNumber == 0) {
 					String elements[] = line.split("-");
 					loadMenu = new Menu(elements[0], new Manager(elements[1], restaurant));
 					menuList.add(loadMenu);
 					lineNumber ++;
 				}else {
 					if(!line.equals("NEXT")){
 						String elements[] = line.split("-");
 						MenuItem item = new MenuItem(elements[0], Double.parseDouble(elements[1]));
 						loadMenu.addMenuItem(item);
 					} else {
 						lineNumber = 0;
 					}
 				}
 			}
			restaurant.setMenuList(menuList);
 			menu = menuList.get(0);
 
 		} catch (Exception e) {
 			System.out.println("Error opening menu");
 		}
 
 	}
 
 	private static void saveMenu() {
 		FileWriter fstream;
 		try {
 			fstream = new FileWriter("menus.POS_MENU");
 			BufferedWriter out = new BufferedWriter(fstream);
 			for(int i = 0; i<menuList.size(); i++) {
 				out.write(menuList.get(i).menuName + "-" + menuList.get(i).getCreatingManager().name + "\n");
 				for(MenuItem item : menuList.get(i).getMenuItems()){
 					out.write(item.name + "-" + item.price+ "\n");
 				}
 				if(i<menuList.size()-1)
 					out.write("NEXT\n");
 			}
 			//			out.write(name.getText()+"\n"+address.getText()+"\n"+phone.getText()+"\n"+order+"\n\n");
 			//Close the output stream
 			out.close();
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		}
 	}
 
 	private static void clearMenuItemButtons() {
 		gBC.gridx = 0 ;
 		gBC.gridy = 0 ;
 		gBC.weightx = 0.5 ;
 		gBC.weighty = 0.25 ;
 
 		for(Component obj : menuItemButtons)
 			desktop.remove(obj);
 		for(Component obj : orderButtons)
 			desktop.remove(obj);
 		
 	}
 
 	private static void drawMenuItemButtons() {
 		clearMenuItemButtons();
 //		for(int i=0; i<menu.getMenuItems().size(); i++){
 //			final MenuItem menuItem = menu.getMenuItems().get(i) ;
 //			JButton b = new JButton(menuItem.name + "\n$" + menuItem.price) ;
 //			b.setActionCommand(menu.getMenuItems().get(i).name);
 //			b.setPreferredSize(new Dimension(150, 100));
 //			// set action
 //			b.addActionListener(new ActionListener() {
 //				public void actionPerformed(ActionEvent e) {
 //					o.addItem(menuItem) ;
 //				}
 //			}) ;
 //			gBC.gridx = i%5 ;
 //			if ((i%5) == 0)
 //				gBC.gridy += 1 ;
 //			desktop.add(b, gBC) ;
 //			menuItemButtons.add(b);
 //		}
 		if(menu != null){
 			ArrayList<String> menuItemNames = kFacade.getCurrentMenuItemNames();
 			ArrayList<Double> menuItemCosts = kFacade.getCurrentMenuItemPrices();
 			for (int i=0; i< menuItemNames.size(); i++) {
 				final MenuItem item = menu.getMenuItems().get(i) ;
 				JButton b = new JButton(menuItemNames.get(i) + "\n $" + menuItemCosts.get(i)) ;
 				b.setActionCommand(menuItemNames.get(i));
 				b.setPreferredSize(new Dimension(150, 100));
 				// set action
 				b.addActionListener(new ActionListener() {
 					public void actionPerformed(ActionEvent e) {
 						o.addItem(item) ; // need to write order handler for facade, something that translates and grabs proper item
 					}
 				}) ;
 				gBC.gridx = i%5 ;
 				if ((i%5) == 0)
 					gBC.gridy += 1 ;
 				desktop.add(b, gBC) ;
 				menuItemButtons.add(b);
 			}
 		}
 		drawOrderButtons();
 	}
 	
 	public static void drawOrderButtons() {
 		final Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
 		frame.getContentPane().add(new JSeparator()) ;
 		JButton viewOrder = new JButton("View Order") ;
 		JButton placeOrder = new JButton("Place Order") ;
 		viewOrder.setPreferredSize(new Dimension(150, 100));
 		viewOrder.setBackground(Color.green) ;
 		placeOrder.setPreferredSize(new Dimension(150, 100));
 		placeOrder.setBackground(Color.blue) ;
 		// actions
 		viewOrder.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e)
 			{
 				//Execute when button is pressed
 				JInternalFrame internalFrame = new JInternalFrame("Order Summary", false, true, false, false);
 				internalFrame.setBounds(0, 50, 500, size.height);
 				internalFrame.setBackground(new Color(225, 225, 255)) ;
 				gBC.ipady = size.height-100;
 				gBC.weightx = 0 ;
 				gBC.gridwidth = 4;
 				gBC.gridx = 0 ;
 				gBC.gridy = 0 ;
 				// adding content to internal frame
 				JLabel receipt = getReceipt() ;
 				internalFrame.add(receipt) ;
 				// displaying frame and bringing to front
 				internalFrame.show() ;
 				frame.add(internalFrame, gBC) ;
 				internalFrame.setVisible(true);
 				try {
 					internalFrame.setSelected(true) ;
 				} catch (PropertyVetoException e1) {
 					e1.printStackTrace();
 				}
 			}
 		});
 		placeOrder.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e)
 			{
 				//Execute when button is pressed
 				JDesktopPane desktop2 = new JDesktopPane() ;
 				desktop2.setLayout(new GridBagLayout());
 				JLabel receipt = getReceipt() ;
 				gBC.ipadx = size.width/2 ;
 				gBC.gridheight = 8;
 				gBC.gridx = 0 ;
 				gBC.gridy = 0 ;
 				desktop2.add(receipt, gBC) ;
 
 				JLabel txt = new JLabel("<html><h1>Fill in all fields: </h1></html>") ;
 				gBC.gridheight = 1 ;
 				gBC.gridx = 1 ;
 				desktop2.add(txt, gBC) ;
 
 				final JTextField name = new JTextField("First and Last Name") ;
 				gBC.gridy = 1 ;
 				desktop2.add(name, gBC) ;
 
 				final JTextField address = new JTextField("Address") ;
 				gBC.gridy = 2 ;
 				desktop2.add(address, gBC) ;
 
 				final JTextField phone = new JTextField("Phone Number") ;
 				gBC.gridy = 3 ;
 				desktop2.add(phone, gBC) ;
 
 				txt = new JLabel("<html><h3>Payment Information: </h3></html>") ;
 				gBC.gridy = 4 ;
 				desktop2.add(txt, gBC) ;
 
 				final JTextField cardName = new JTextField("Name on Card") ;
 				gBC.gridy = 5 ;
 				desktop2.add(cardName, gBC) ;
 
 				final JTextField cardNum = new JTextField("Credit Card Number", 16) ;
 				gBC.gridy = 6 ;
 				desktop2.add(cardNum, gBC) ;
 
 				final JTextField expDate = new JTextField("Expiration Date") ;
 				gBC.gridy = 7 ;
 				desktop2.add(expDate, gBC) ;
 
 				final JButton place = new JButton("Place Order") ;
 				place.setBackground(Color.green) ;
 				place.setPreferredSize(new Dimension(150, 100));
 				gBC.gridy = 8 ;
 
 				//TODO: place order button needs to store all information and send it to the kitchen display
 				place.addActionListener(new ActionListener() {
 					public void actionPerformed(ActionEvent e) {
 						// This is all the error checking to make sure the inputs are correct
 						if(name.getText().equals("First and Last Name") || address.getText().equals("Address") ||
 								phone.getText().equals("Phone Number") || cardName.getText().equals("Name on Card") ||
 								cardNum.getText().equals("Credit Card Number") || expDate.getText().equals("Expiration Date"))
 							JOptionPane.showOptionDialog(frame, "You must fill in all fields.", "Error", JOptionPane.DEFAULT_OPTION, 
 									JOptionPane.ERROR_MESSAGE, null, null, e) ;
 						else {
 							if(phone.getText().length() != 10)
 								JOptionPane.showOptionDialog(frame, "Invalid phone number. Use Format: 9991234567.", "Error", JOptionPane.DEFAULT_OPTION, 
 										JOptionPane.ERROR_MESSAGE, null, null, e) ;
 							else{
 								if(cardNum.getText().length() != 16)
 									JOptionPane.showOptionDialog(frame, "Invalid Card Number. Use Format: 1234567890123456", "Error", JOptionPane.DEFAULT_OPTION, 
 											JOptionPane.ERROR_MESSAGE, null, null, e) ;
 								else {
 									//TODO: check for validity of expiration date
 									// Theoretically this is where we would do card authorization
 									JOptionPane.showOptionDialog(frame, "Your order has been placed.", "Order Placed", JOptionPane.DEFAULT_OPTION, 
 											JOptionPane.PLAIN_MESSAGE, null, null, e) ;
 									// creating the output file for the kitchen display to read
 									String order = "" ;
 									ArrayList<OrderItem> orderItems = o.getOrderList() ;
 									for(int i=0 ; i<orderItems.size(); i++){
 										order += orderItems.get(i).getItem().name+"\t\t"+orderItems.get(i).quantity+"\n";
 									}
 									FileWriter fstream;
 									try {
 										fstream = new FileWriter(Integer.toString(o.orderID)+".POS");
 										BufferedWriter out = new BufferedWriter(fstream);
 										out.write(name.getText()+"\n"+address.getText()+"\n"+phone.getText()+"\n"+order+"\n\n");
 										//Close the output stream
 										out.close();
 									} catch (IOException e1) {
 										e1.printStackTrace();
 									}
 									restartProgram(args) ;
 								}
 							}
 						}
 					}
 				}) ;
 
 				desktop2.add(place, gBC) ;
 
 				desktop2.setVisible(true) ;
 				frame.setContentPane(desktop2) ;
 				frame.setVisible(true) ;
 			}
 		});
 		gBC.gridy += 1 ;
 		gBC.gridx = 0 ;
 		desktop.add(viewOrder, gBC) ;
 		orderButtons.add(viewOrder);
 		gBC.gridx = 1 ;
 		desktop.add(placeOrder, gBC) ;
 		orderButtons.add(placeOrder);
 	}
 
 	/**
 	 * Opens a manager window where the manager can configure things. W.I.P.
 	 * @param manager
 	 */
 	private static void showManagerWindow(final Manager manager) {
 		final Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
 
 		JInternalFrame internalFrame = new JInternalFrame("Manage", false, true, false, false);
 		//Used to save changes made by manager
 		InternalFrameListener i = new InternalFrameListener() {
 			//Refresh GUI elements on close
 			public void internalFrameClosing(InternalFrameEvent e) {
 				currentMenu.setText("Current Menu: " + menu.menuName);
 				saveMenu();
 				drawMenuItemButtons();
 			}
 			public void internalFrameActivated(InternalFrameEvent e) {}
 			public void internalFrameClosed(InternalFrameEvent e) {}
 			public void internalFrameDeactivated(InternalFrameEvent e) {}
 			public void internalFrameDeiconified(InternalFrameEvent e) {}
 			public void internalFrameIconified(InternalFrameEvent e) {}
 			public void internalFrameOpened(InternalFrameEvent e) {}		
 		};
 		internalFrame.addInternalFrameListener(i);
 		internalFrame.setBounds(0, 50, 500, size.height-100);
 		internalFrame.setBackground(new Color(200, 200, 200)) ;
 		//Add manager Elements
 		internalFrame.add(getCreateMenuGUI(manager));
 		//		internalFrame.add(getCurrentMenuItemCountGUI());
 
 		// displaying frame and bringing to front
 		internalFrame.show() ;
 		frame.add(internalFrame) ;
 		internalFrame.setVisible(true);
 
 		try {
 			internalFrame.setSelected(true) ;
 		} catch (PropertyVetoException e1) {
 			e1.printStackTrace();
 		}
 
 	}
 
 	public static JPanel getCreateMenuGUI(final Manager manager) {
 		final JPanel p = new JPanel();
 		GridLayout layout = new GridLayout(3, 3);
 		p.setLayout(layout);
 
 		//Create Label, textfield and button
 		JLabel addMenuLabel = new JLabel("Add Menu");
 		final JTextField addMenuTextField = new JTextField(15);
 		JButton addMenuButton = new JButton("Create Menu");
 		addMenuButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				boolean found = false;
 				for(Menu obj : menuList){
 					if(obj.menuName.equalsIgnoreCase(addMenuTextField.getText())){
 						found = true;
 						menu = obj;
 					}
 				}
 				if(!found) {
 					menu = new Menu(addMenuTextField.getText(), manager);
 					menuList.add(menu);
 					restaurant.addMenu(menu);
 				}
 				addMenuTextField.setText("Menu has been changed");
 			}
 		});
 		//Add elements to JPanel
 		p.add(addMenuLabel);
 		p.add(addMenuTextField);
 		p.add(addMenuButton);
 		for(Object obj : getCurrentMenuItemCountGUI())
 			p.add((Component) obj);
 		for(Object obj : addMenuItemInterface())
 			p.add((Component) obj);
 		return p;
 	}
 
 	public static ArrayList<Object> getCurrentMenuItemCountGUI() {
 		ArrayList<Object> p = new ArrayList<Object>();
 		JLabel title = new JLabel("Number of Menu Items");
 		p.add(title);
 		JLabel currentNumberMenuItemsLabel;
 		if(menu!=null)
 			currentNumberMenuItemsLabel = new JLabel("" + menu.getMenuItems().size());
 		else
 			currentNumberMenuItemsLabel = new JLabel("0");
 		p.add(currentNumberMenuItemsLabel);
 		p.add(new JLabel());
 		return p;
 	}
 
 	public static ArrayList<Object> addMenuItemInterface() {
 		ArrayList<Object> p = new ArrayList<Object>();
 		JLabel title = new JLabel("Add Item (item - price)");
 		p.add(title);
 		final JTextField inputForm = new JTextField(15);
 		p.add(inputForm);
 		JButton submitButton = new JButton("Add");
 		submitButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				String string = inputForm.getText();
 				String elements[] = string.split("-");
 				try {
 					elements[0] = elements[0].trim();
 					elements[1] = elements[1].trim();
 					MenuItem newItem = new MenuItem(elements[0].trim(), Double.parseDouble(elements[1].trim().substring(1)));
 					menu.addMenuItem(newItem);
 					inputForm.setText(newItem.name + " Added!");
 				} catch(Exception exc) {
 					inputForm.setText("Error adding item!");
 				}
 			}
 		});
 		p.add(submitButton);
 
 		return p;
 	}
 
 	public static JLabel getReceipt() {
 		ArrayList<OrderItem> orderItems = o.getOrderList() ;
 		String text = "<html><center><h1>ORDER SUMMARY</h1><br>" ;
 		text += "<table>" +
 				"<tr>" +
 				"<td>Item Name</td>" +
 				"<td>Quantity</td>" +
 				"<td>Unit Price</td>" +
 				"<td>Total Price</td>" +
 				"</tr>" ;
 		for(int i=0; i<orderItems.size(); i++) {
 			double lineTotal = orderItems.get(i).quantity * orderItems.get(i).getItem().price ;
 			text += "<tr><td>"+orderItems.get(i).getItem().name+"</td>" +
 					"<td>"+orderItems.get(i).quantity+"</td>" +
 					"<td>$"+orderItems.get(i).getItem().price+"</td>" +
 					"<td>$"+lineTotal+"</td></tr><br>" ;
 		}
 		DecimalFormat formatter = new DecimalFormat("#0.00");
 		String tot = formatter.format(o.total) ;
 		String payments = formatter.format(o.total-o.amountDue) ;
 		String amountDue = formatter.format(o.amountDue) ;
 		text += "<tr><td></td><td></td><td></td><td>Total: $"+tot+"</td></tr>" +
 				"<tr><td></td><td>Payments:</td><td></td><td>$"+payments+"</td></tr>" +
 				"<tr><td></td><td>Amount Due:</td><td></td><td>$"+amountDue+"</td><tr></table></center></html>" ;
 		JLabel recipt = new JLabel(text, JLabel.CENTER) ;
 		return recipt ;
 	}
 
 	public static void main(final String[] args1) {
 		//Schedule a job for the event-dispatching thread:
 		//creating and showing this application's GUI.
 		javax.swing.SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 				args = args1;
 				createAndShowGUI(args);
 			}
 		});
 	}
 }
