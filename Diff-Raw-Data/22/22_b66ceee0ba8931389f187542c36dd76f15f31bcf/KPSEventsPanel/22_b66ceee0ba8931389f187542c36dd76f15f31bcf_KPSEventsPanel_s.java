 package gui;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.util.List;
 import java.util.Map;
 
 import backend.PrioritisedRoute;
 import events.Event;
 
 import javax.swing.*;
 
 public class KPSEventsPanel extends JPanel implements KPSPanel{
 	
 	private String testString = "Your mum\nYour mum\nYour mum\nYour mum\nYour mum\nYour mum\nYour mum\nYour mum\nYour mum\nYour mum\nYour mum\nYour mum\nYour mum\nYour mum\nYour mum\nYour mum\nYour mum\nYour mum\nYour mum\nYour mum\nYour mum\nYour mum\nYour mum\nYour mum\nYour mum\nYour mum\nYour mum\nYour mum\nYour mum\nYour mum\nYour mum\nYour mum\nYour mum\nYour mum\nYour mum\nYour mum\nYour mum\nYour mum\nYour mum\nYour mum\nYour mum\nYour mum\n";
 	private int eventTime;
 	
 	//Events panel
 	private JPanel eventsPanel;
 	private JTextArea displayedEventsField;
 	private JScrollPane displayedEvents;
 	private JTextField totalEventsField;
 	private JButton forward;
 	private JButton backward;
 	
 	//Business figures panel
 	private JPanel businessPanel;
 	private JLabel revenueLabel;
 	private JTextField revenueField;
 	private JLabel expenditureLabel;
 	private JTextField expenditureField;
 	private JLabel averageDeliveryLabel;
 	private JTextArea averageDeliveryField;
 	private JScrollPane averageDelivery;
 	private JLabel amountOfMailLabel;
 	private JTextArea amountOfMailField;
 	private JScrollPane amountOfMail;
 	
 	//Critical routes panel
 	private JPanel criticalPanel;
 	private JLabel criticalRoutesLabel;
 	private JTextArea criticalRoutesField;
 	private JScrollPane criticalRoutes;
 	
 	
 	public KPSEventsPanel() {
 		super();
 		this.setPreferredSize(new Dimension(600,600));
 		this.setBackground(Color.GRAY);
 		
 		//Events
 		eventsPanel = new JPanel();
 		eventsPanel.setPreferredSize(new Dimension(350, 590));
 		
 		displayedEventsField = new JTextArea();
 		displayedEventsField.setEditable(false);
 		displayedEventsField.setText(testString);
 		displayedEvents = new JScrollPane(displayedEventsField);
 		displayedEvents.setPreferredSize(new Dimension(340, 525));
 		
 		totalEventsField = new JTextField();
 		totalEventsField.setEditable(false);
 		totalEventsField.setPreferredSize(new Dimension(340, 25));
 		
 		forward = new JButton(">");
 		backward = new JButton("<");
 		Box buttons = new Box(BoxLayout.X_AXIS);
 		buttons.add(backward);
 		buttons.add(Box.createRigidArea(new Dimension(100, 0)));
 		buttons.add(forward);
 		
 		Box allEvents = new Box(BoxLayout.Y_AXIS);
 		allEvents.add(displayedEvents);
 		allEvents.add(totalEventsField);
 		allEvents.add(Box.createRigidArea(new Dimension(0, 5)));
 		allEvents.add(buttons);
 		
 		eventsPanel.add(allEvents);
 		
 		//Business figures
 		businessPanel = new JPanel();
 		businessPanel.setPreferredSize(new Dimension(235, 590));
 		Box businessBox = new Box(BoxLayout.Y_AXIS);
 		businessBox.setPreferredSize(new Dimension(225, 80));
 		
 		revenueLabel = new JLabel("Revenue:");
 		revenueLabel.setFont(new Font("Arial", 0, 12));
 		revenueField = new JTextField();
 		revenueField.setEditable(false);
 		revenueField.setText("A");
 		revenueField.setAlignmentX(LEFT_ALIGNMENT);
 		businessBox.add(revenueLabel);
 		businessBox.add(revenueField);
 		businessBox.add(Box.createRigidArea(new Dimension(0, 5)));
 		
 		expenditureLabel = new JLabel("Expenditure:");
 		expenditureLabel.setFont(new Font("Arial", 0, 12));
 		expenditureField = new JTextField();
 		expenditureField.setEditable(false);
 		expenditureField.setText("B");
 		expenditureField.setAlignmentX(LEFT_ALIGNMENT);
 		businessBox.add(expenditureLabel);
 		businessBox.add(expenditureField);
 		businessBox.add(Box.createRigidArea(new Dimension(0, 5)));
 		
 		Box businessTriplesBox = new Box(BoxLayout.Y_AXIS);
 		businessTriplesBox.setPreferredSize(new Dimension(225, 300));
 		
 		averageDeliveryLabel = new JLabel("Average Delivery Time:");
 		averageDeliveryLabel.setFont(new Font("Arial", 0, 12));
 		averageDeliveryField = new JTextArea();
 		averageDeliveryField.setEditable(false);
 		averageDeliveryField.setText(testString);
 		averageDelivery = new JScrollPane(averageDeliveryField);
 		averageDelivery.setAlignmentX(LEFT_ALIGNMENT);
 		businessTriplesBox.add(averageDeliveryLabel);
 		businessTriplesBox.add(averageDelivery);
 		businessTriplesBox.add(Box.createRigidArea(new Dimension(0, 5)));
 		
 		amountOfMailLabel = new JLabel("Amount of Mail:");
 		amountOfMailLabel.setFont(new Font("Arial", 0, 12));
 		amountOfMailField = new JTextArea();
 		amountOfMailField.setEditable(false);
 		amountOfMailField.setText(testString);
 		amountOfMail = new JScrollPane(amountOfMailField);
 		amountOfMail.setAlignmentX(LEFT_ALIGNMENT);
 		businessTriplesBox.add(amountOfMailLabel);
 		businessTriplesBox.add(amountOfMail);
 		
 		Box completeBusiness = new Box(BoxLayout.Y_AXIS);
 		completeBusiness.add(businessBox);
 		completeBusiness.add(businessTriplesBox);
 		businessPanel.add(completeBusiness);
 		
 		//Critical routes
 		Box criticalBox = new Box(BoxLayout.Y_AXIS);
 		criticalBox.setPreferredSize(new Dimension(225, 195));
 		
 		criticalRoutesLabel = new JLabel("Critical Routes:");
 		criticalRoutesLabel.setFont(new Font("Arial", 0, 12));
 		criticalRoutesLabel.setAlignmentX(LEFT_ALIGNMENT);
 		criticalRoutesField = new JTextArea();
 		criticalRoutesField.setEditable(false);
 		criticalRoutesField.setText(testString);
 		criticalRoutes = new JScrollPane(criticalRoutesField);
 		criticalRoutes.setAlignmentX(LEFT_ALIGNMENT);
 		
 		criticalBox.add(criticalRoutesLabel);
 		criticalBox.add(criticalRoutes);
 		
 		businessPanel.add(criticalBox);
 		
 		Box leftSide = new Box(BoxLayout.Y_AXIS);
 		leftSide.add(eventsPanel);
 		Box rightSide = new Box(BoxLayout.Y_AXIS);
 		rightSide.add(businessPanel);
 		Box completeBox = new Box(BoxLayout.X_AXIS);
 		completeBox.add(leftSide);
 		completeBox.add(Box.createRigidArea(new Dimension(5, 0)));
 		completeBox.add(rightSide);
 		
 		
 		this.add(completeBox);
 		
 		int eventTime = 0;
 			
 		//Business Figures
 		
 		//Critical routes
 	}
 	
 	public void populate(int totalNumberOfEvents, Map<PrioritisedRoute, Double> deliveryTimes, 
			Map<PrioritisedRoute, Double> amountOfMail, Map<PrioritisedRoute, Double> weightOfMail,
 			Map<PrioritisedRoute, Double> volumeOfMail, List<Event> events) {
 		
 		eventTime = totalNumberOfEvents;
 		
 	}
 	
 	public void updateInfo(Map<PrioritisedRoute, Double> deliveryTimes, 
			Map<PrioritisedRoute, Double> amountOfMail, Map<PrioritisedRoute, Double> weightOfMail,
 			Map<PrioritisedRoute, Double> volumeOfMail, List<Event> events) {
 		
 	}
 	
 	public int returnEventTime() {
 		return eventTime;
 	}
 
 	public void reset() {}
 
 }
