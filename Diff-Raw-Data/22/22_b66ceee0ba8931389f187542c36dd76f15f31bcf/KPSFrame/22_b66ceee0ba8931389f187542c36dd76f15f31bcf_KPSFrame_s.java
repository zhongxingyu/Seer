 package gui;
 
 import java.awt.BorderLayout;
 import java.awt.CardLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.Set;
 import java.util.List;
 
 import javax.swing.JFrame;
 import javax.swing.JInternalFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 import backend.PrioritisedRoute;
 import events.Event;
 
 import routes.DistributionCentre;
 import routes.Firm;
 
 public class KPSFrame extends JFrame {
 	
 	private KPSTitlePanel title;
 	private JPanel panel;
 	private KPSDefaultPanel defaultPanel;
 	private KPSMailPanel mailPanel;
 	private KPSEventsPanel eventsPanel;
 	private KPSUpdatePanel updatePanel;
 	private KPSMenuBar menuBar;
 	private String currentPanel;
 	String[] data = {"one", "two", "three"};
 	
 	public KPSFrame(ActionListener actionlistener, Set<DistributionCentre> centres, List<Firm> firms) {
 		title = new KPSTitlePanel();
 		panel = new JPanel(new CardLayout());
 		defaultPanel = new KPSDefaultPanel();
 		defaultPanel.setName("defaultPanel");
 		mailPanel = new KPSMailPanel(actionlistener, centres);
 		mailPanel.setName("mailPanel");
 		eventsPanel = new KPSEventsPanel();
 		eventsPanel.setName("eventsPanel");
 		updatePanel = new KPSUpdatePanel(actionlistener, centres, firms);
 		updatePanel.setName("updatePanel");
 		currentPanel = defaultPanel.getName();
 		
 		menuBar = new KPSMenuBar(actionlistener);
 		
 		panel.add(defaultPanel, "defaultPanel");
 		panel.add(mailPanel, "mailPanel");
 		panel.add(eventsPanel, "eventsPanel");
 		panel.add(updatePanel, "updatePanel");
 		
 		this.setLayout(new BorderLayout());
 		this.add(title, BorderLayout.NORTH);
 		this.add(panel, BorderLayout.CENTER);
 		this.setJMenuBar(menuBar);
 		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		this.pack();
 		this.setFocusable(true);
 		this.setResizable(false);
 		this.setVisible(true);
 	}
 	
 	public void displayPanel(String s) {
 		CardLayout cards = (CardLayout) panel.getLayout();
 		cards.show(panel, s);
 		currentPanel = s;
 	}
 	
 	public void resetMailPanel() {
 		CardLayout cards = (CardLayout) panel.getLayout();
 		cards.show(panel, "defaultPanel");
 		mailPanel.reset();
 	}
 	
 	public void resetUpdatePanel() {
 		CardLayout cards = (CardLayout) panel.getLayout();
 		cards.show(panel, "defaultPanel");
 		updatePanel.reset();
 	}
 	
 	public void resetAll() {
 		CardLayout cards = (CardLayout) panel.getLayout();
 		cards.show(panel, "defaultPanel");
 		for (Component c : panel.getComponents()) {
 			((KPSPanel)c).reset();
 		}
 	}
 	
 	public boolean getPanel(String panelName) {
 		return currentPanel.equals(panelName);
 	}
 	
 	public ArrayList returnMailPanelInfo() {
 		return mailPanel.returnInfo();
 	}
 	
 	public int returnEventTime() {
 		return eventsPanel.returnEventTime();
 	}
 	
 	public void populateEvents(int totalNumberOfEvents, Map<PrioritisedRoute, Double> deliveryTimes, 
			Map<PrioritisedRoute, Double> amountOfMail, Map<PrioritisedRoute, Double> weightOfMail,
 			Map<PrioritisedRoute, Double> volumeOfMail, List<Event> events) {
 		
 		eventsPanel.populate(totalNumberOfEvents, deliveryTimes, amountOfMail, weightOfMail, volumeOfMail, events);
 		
 	}
 	
 	public void updateEvents(Map<PrioritisedRoute, Double> deliveryTimes, 
			Map<PrioritisedRoute, Double> amountOfMail, Map<PrioritisedRoute, Double> weightOfMail,
 			Map<PrioritisedRoute, Double> volumeOfMail, List<Event> events) {
 		
 		eventsPanel.updateInfo(deliveryTimes, amountOfMail, weightOfMail, volumeOfMail, events);
 	}
 	
 	public ArrayList returnCustomerPriceUpdateInfo() {
 		return updatePanel.returnCustomerPriceUpdateInfo();
 	}
 	
 	public ArrayList returnTransportCostUpdateInfo() {
 		return updatePanel.returnTransportCostUpdateInfo();
 	}
 	
 	public ArrayList returnDiscontinueTransportInfo() {
 		return updatePanel.returnDiscontinueTransportInfo();
 	}
 
 	public void manager() {
 		menuBar.manager();	
 	}
 	
 	public void notManager() {
 		menuBar.notManager();
 	}
 
 }
