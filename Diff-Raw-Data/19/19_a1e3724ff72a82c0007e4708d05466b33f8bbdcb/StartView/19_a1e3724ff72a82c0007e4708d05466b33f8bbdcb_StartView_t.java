 package se.kth.csc.iprog.dinnerplanner.view;
 
 import java.awt.Component;
 import java.awt.Font;
 
 import javax.imageio.ImageIO;
 
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 import javax.swing.JScrollPane;
 import javax.swing.JSpinner;
 import javax.swing.JTabbedPane;
 import javax.swing.SpringLayout;
 import javax.swing.JButton;
 
 import se.kth.csc.iprog.dinnerplanner.controller.MainController;
 import se.kth.csc.iprog.dinnerplanner.model.DinnerModel;
 import se.kth.csc.iprog.dinnerplanner.model.Dish;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Observable;
 import java.util.Observer;
 import java.util.Set;
 
 import javax.swing.SpinnerNumberModel;
import java.awt.Panel;
import javax.swing.JLayeredPane;
import java.awt.Canvas;
 
 public class StartView extends JPanel implements Observer {
 
 	public JButton preparationButton = new JButton("Preparation");
 	public JButton ingredientsButton = new JButton("Ingredients");
 	public JTabbedPane leftPanel = new JTabbedPane();
 	public JSpinner guestCountSpinner = new JSpinner();
 	public JLabel costLabel = new JLabel("$ 0.0");
 	public TabPanel starter;
 	public TabPanel main;
 	public TabPanel desert;
 	public SpringLayout sl_rightPanel;
 	public JPanel rightPanel;
 	public JLabel[] dishIcon = new JLabel[3];
 	public JLabel[] lblNewLabel = new JLabel[3];
 	public JButton[] btnNewButton = new JButton[3];
 
 	/**
 	 * Create the panel.
 	 * 
 	 * @throws IOException
 	 */
 
 	public StartView(DinnerModel model) {
 		model.addObserver(this);
 
 		setBounds(100, 100, 680, 440);
 		SpringLayout springLayout = new SpringLayout();
 
 		setLayout(springLayout);
 
 		// Create leftPanel [TabbedPane]
 		springLayout.putConstraint(SpringLayout.NORTH, leftPanel, 10,
 				SpringLayout.NORTH, this);
 		springLayout.putConstraint(SpringLayout.WEST, leftPanel, 10,
 				SpringLayout.WEST, this);
 		springLayout.putConstraint(SpringLayout.SOUTH, leftPanel, -10,
 				SpringLayout.SOUTH, this);
 
 		// //////////////////////////////////////////////////////////////////////////////////////////////////
 		// Add subPanel inside tab
 		starter = new TabPanel("Starter", model);
 		leftPanel.addTab("Starter", starter);
 		// //////////////////////////////////////////////////////////////////////////////////////////////////
 		main = new TabPanel("Main", model);
 		leftPanel.addTab("Main", main);
 		// //////////////////////////////////////////////////////////////////////////////////////////////////
 		desert = new TabPanel("Desert", model);
 		leftPanel.addTab("Desert", desert);
 		// //////////////////////////////////////////////////////////////////////////////////////////////////
 		// ADDS MODEL AS JLABELS
 
 		// //////////////////////////////////////////////////////////////////////////////////////////////////
 		// //////////////////////////////////////////////////////////////////////////////////////////////////
 
 		// Create rightPanel
 		rightPanel = new JPanel();
 		springLayout.putConstraint(SpringLayout.EAST, leftPanel, -6,
 				SpringLayout.WEST, rightPanel);
 		springLayout.putConstraint(SpringLayout.WEST, rightPanel, -250,
 				SpringLayout.EAST, this);
 
 		springLayout.putConstraint(SpringLayout.EAST, rightPanel, -10,
 				SpringLayout.EAST, this);
 		springLayout.putConstraint(SpringLayout.NORTH, rightPanel, 10,
 				SpringLayout.NORTH, this);
 		springLayout.putConstraint(SpringLayout.SOUTH, rightPanel, -10,
 				SpringLayout.SOUTH, this);
 		this.add(rightPanel);
 		sl_rightPanel = new SpringLayout();
 		rightPanel.setLayout(sl_rightPanel);
 
 		JLabel nopLabel = new JLabel("Number of people:");
 		sl_rightPanel.putConstraint(SpringLayout.WEST, nopLabel, 10,
 				SpringLayout.WEST, rightPanel);
 		sl_rightPanel.putConstraint(SpringLayout.NORTH, nopLabel, 40,
 				SpringLayout.NORTH, rightPanel);
 		rightPanel.add(nopLabel);
 
 		guestCountSpinner.setModel(new SpinnerNumberModel(new Integer(0),
 				new Integer(0), null, new Integer(1)));
 		sl_rightPanel.putConstraint(SpringLayout.NORTH, guestCountSpinner, 40,
 				SpringLayout.NORTH, rightPanel);
 		sl_rightPanel.putConstraint(SpringLayout.EAST, guestCountSpinner, -10,
 				SpringLayout.EAST, rightPanel);
 		sl_rightPanel.putConstraint(SpringLayout.WEST, guestCountSpinner, 50,
 				SpringLayout.EAST, nopLabel);
 		rightPanel.add(guestCountSpinner);
 		JLabel costTagLabel = new JLabel("Total cost:");
 		sl_rightPanel.putConstraint(SpringLayout.NORTH, costTagLabel, 20,
 				SpringLayout.SOUTH, nopLabel);
 		sl_rightPanel.putConstraint(SpringLayout.WEST, costTagLabel, 10,
 				SpringLayout.WEST, rightPanel);
 		rightPanel.add(costTagLabel);
 
 		sl_rightPanel.putConstraint(SpringLayout.NORTH, costLabel, 0,
 				SpringLayout.NORTH, costTagLabel);
 		sl_rightPanel.putConstraint(SpringLayout.EAST, costLabel, -20,
 				SpringLayout.EAST, rightPanel);
 		rightPanel.add(costLabel);
 
 		JLabel dinnerMenuLabel = new JLabel("Dinner menu", JLabel.CENTER);
 		sl_rightPanel.putConstraint(SpringLayout.NORTH, dinnerMenuLabel, 20,
 				SpringLayout.SOUTH, costTagLabel);
 		sl_rightPanel.putConstraint(SpringLayout.WEST, dinnerMenuLabel, 10,
 				SpringLayout.WEST, rightPanel);
 		sl_rightPanel.putConstraint(SpringLayout.EAST, dinnerMenuLabel, -10,
 				SpringLayout.EAST, rightPanel);
 		dinnerMenuLabel.setFont(new Font("Tahoma", Font.PLAIN, 18));
 		rightPanel.add(dinnerMenuLabel);
 
 		// Add the dishes delete buttons and images to the right pane
 		for (int i = 0; i < 3; i++) {
 			dishIcon[i] = new JLabel(new ImageIcon("images/delete_icon.gif"));
 			lblNewLabel[i] = new JLabel("null");
 			btnNewButton[i] = new JButton(new ImageIcon(
 					"images/delete_icon.gif"));
 
 			sl_rightPanel.putConstraint(SpringLayout.NORTH, dishIcon[i],
 					i == 0 ? 20 : 15, SpringLayout.SOUTH,
 					i == 0 ? dinnerMenuLabel : dishIcon[i - 1]);
 			sl_rightPanel.putConstraint(SpringLayout.SOUTH, dishIcon[i],
 					i == 0 ? 60 : 55, SpringLayout.SOUTH,
 					i == 0 ? dinnerMenuLabel : dishIcon[i - 1]);
 			sl_rightPanel.putConstraint(SpringLayout.WEST, dishIcon[i], 10,
 					SpringLayout.WEST, rightPanel);
 			sl_rightPanel.putConstraint(SpringLayout.EAST, dishIcon[i], 50,
 					SpringLayout.WEST, rightPanel);
 			rightPanel.add(dishIcon[i]);
 
 			sl_rightPanel.putConstraint(SpringLayout.NORTH, lblNewLabel[i], 0,
 					SpringLayout.NORTH, dishIcon[i]);
 			sl_rightPanel.putConstraint(SpringLayout.SOUTH, lblNewLabel[i], 0,
 					SpringLayout.SOUTH, dishIcon[i]);
 			sl_rightPanel.putConstraint(SpringLayout.WEST, lblNewLabel[i], 10,
 					SpringLayout.EAST, dishIcon[i]);
 			sl_rightPanel.putConstraint(SpringLayout.EAST, lblNewLabel[i], -10,
 					SpringLayout.WEST, btnNewButton[i]);
 			rightPanel.add(lblNewLabel[i]);
 
 			sl_rightPanel.putConstraint(SpringLayout.NORTH, btnNewButton[i], 5,
 					SpringLayout.NORTH, lblNewLabel[i]);
 			sl_rightPanel.putConstraint(SpringLayout.SOUTH, btnNewButton[i],
 					-5, SpringLayout.SOUTH, lblNewLabel[i]);
 			sl_rightPanel.putConstraint(SpringLayout.EAST, btnNewButton[i],
 					-10, SpringLayout.EAST, rightPanel);
 			sl_rightPanel.putConstraint(SpringLayout.WEST, btnNewButton[i],
 					-50, SpringLayout.EAST, rightPanel);
 			rightPanel.add(btnNewButton[i]);
 
 			dishIcon[i].setVisible(false);
 			lblNewLabel[i].setVisible(false);
 			btnNewButton[i].setVisible(false);
 
 		}
 
 		// preparationButton
 		sl_rightPanel.putConstraint(SpringLayout.WEST, preparationButton, 10,
 				SpringLayout.WEST, rightPanel);
 		sl_rightPanel.putConstraint(SpringLayout.SOUTH, preparationButton, -10,
 				SpringLayout.SOUTH, rightPanel);
 		rightPanel.add(preparationButton);
 
 		// ingredientsButton
 		sl_rightPanel.putConstraint(SpringLayout.EAST, ingredientsButton, -10,
 				SpringLayout.EAST, rightPanel);
 		sl_rightPanel.putConstraint(SpringLayout.SOUTH, ingredientsButton, -10,
 				SpringLayout.SOUTH, rightPanel);
 		rightPanel.add(ingredientsButton);
		
		JLabel lblStarter = new JLabel("Starter:");
		sl_rightPanel.putConstraint(SpringLayout.NORTH, lblStarter, 5, SpringLayout.SOUTH, dinnerMenuLabel);
		sl_rightPanel.putConstraint(SpringLayout.WEST, lblStarter, 0, SpringLayout.WEST, nopLabel);
		rightPanel.add(lblStarter);
		
		JLabel lblMain = new JLabel("Main:");
		sl_rightPanel.putConstraint(SpringLayout.NORTH, lblMain, 38, SpringLayout.SOUTH, lblStarter);
		sl_rightPanel.putConstraint(SpringLayout.WEST, lblMain, 10, SpringLayout.WEST, rightPanel);
		rightPanel.add(lblMain);
		
		JLabel lblDesert = new JLabel("Desert:");
		sl_rightPanel.putConstraint(SpringLayout.NORTH, lblDesert, 40, SpringLayout.SOUTH, lblMain);
		sl_rightPanel.putConstraint(SpringLayout.WEST, lblDesert, 0, SpringLayout.WEST, nopLabel);
		rightPanel.add(lblDesert);
 
 		// addModels(model);//TODO MAYBE
 		// adds the dishes
 		updateDishes(model.getDishes(), 0);
 		// add the leftPanel
 		add(leftPanel);
 
 		new MainController(model, this);
 	}
 
 	public void updateDishes(Set<Dish> modelDishes, int type) {
 
 		if (type == 1) {
 
 			starter.scrollPanel.removeAll();
 		} else if (type == 2) {
 
 			main.scrollPanel.removeAll();
 		} else if (type == 3) {
 
 			desert.scrollPanel.removeAll();
 		}
 
 		for (Dish dish : modelDishes) {
 			ImageIcon ic = null;
 			try {
 				ic = new ImageIcon(ImageIO.read(
 						new File("images/" + dish.getImage()))
 						.getScaledInstance(100, 100, 0));
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			JLabel tempDish = new JLabel(dish.getName(), ic, JLabel.CENTER);
 			tempDish.setName(dish.getName());
 			tempDish.setVerticalTextPosition(JLabel.BOTTOM);
 			tempDish.setHorizontalTextPosition(JLabel.CENTER);
 			switch (dish.getType()) {
 			case 1:
 				starter.scrollPanel.add(tempDish);
 
 				starter.updateUI();
 				break;
 			case 2:
 				main.scrollPanel.add(tempDish);
 				main.updateUI();
 				break;
 			case 3:
 				desert.scrollPanel.add(tempDish);
 				desert.updateUI();
 			}
 		}
 
 	}
 
 	public void addDishToRightPanel(int type, DinnerModel model) {
 
 		dishIcon[type - 1].setIcon(new ImageIcon("images/"
 				+ model.getSelectedDish(type).getImage()));
 		dishIcon[type - 1].setName(model.getSelectedDish(type).getName());
 		lblNewLabel[type - 1].setText("<html>" + type + ": "
 				+ model.getSelectedDish(type).getName() + "<br>Cost: $ "
 				+ model.getDishPrice(model.getSelectedDish(type)) + "</html>");
 		btnNewButton[type - 1].setVisible(true);
 		dishIcon[type - 1].setVisible(true);
 		lblNewLabel[type - 1].setVisible(true);
 	}
 
 	public void addDishes(DinnerModel model, boolean[] choosen) {
 		if (choosen[0]) {
 			addDishToRightPanel(1,model);
 		} 
 		if (choosen[1]) {
 			addDishToRightPanel(2,model);
 		} 
 		if (choosen[2]) {
 			addDishToRightPanel(3,model);
 			}
 
 	}
 
 	@Override
 	public void update(Observable arg0, Object object) {
 		if (object.equals("numberOfGuests")) {
 			DinnerModel model = (DinnerModel) arg0;
 			costLabel.setText("$ " + model.getTotalMenuPrice());
 		}
 	}
 }
