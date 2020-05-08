 package kpsmart;
 
 import java.awt.BorderLayout;
 import java.awt.MenuItem;
 import java.awt.PopupMenu;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.JComponent;
 import javax.swing.JOptionPane;
 import javax.swing.JPasswordField;
 import javax.swing.JTextField;
 
 import priority.Priority;
 import routes.DistributionCentre;
 import routes.Firm;
 
 import gui.*;
 import backend.*;
 
 public class KPSmart implements ActionListener{
 	
 	//GUI ELEMENTS
 	KPSBackend kBackend;
 	KPSFrame kFrame;
 	JPasswordField kPasswordField;
 	boolean managerMode;
 	
 	/**
 	 * Creates a new instance of the KPSmart program
 	 */
 	public KPSmart() {
 	
 		kBackend = new KPSBackend();
 		kBackend.parseXMLRecord();
 		kFrame = new KPSFrame(this, kBackend.getDistributionCentres(), kBackend.findFirms());
 		kPasswordField = new JPasswordField(10);
 		managerMode = false;
 	
 	}
 
 	//ACTION LISTENER HANDLING
 	public void actionPerformed(ActionEvent e) {
 		
 		//FILE OPTIONS
 		if ("Save".equals(e.getActionCommand())) {
 			kBackend.createXMLRecord();
 			return;
 		}
 		
 		if ("Exit".equals(e.getActionCommand())) {
 			System.exit(0);
 		}
 				
 		//ACTION OPTIONS
 		if ("Send Mail".equals(e.getActionCommand())) {
 			kFrame.displayPanel("mailPanel");
 			return;
 		}
 		
 		if ("Update Costs".equals(e.getActionCommand())) {
 			kFrame.displayPanel("updatePanel");
 			return;
 		}
 		
 		if ("View Business Figures".equals(e.getActionCommand())) {
 			if (kFrame.getPanel("eventsPanel")) { return; }
 			int i = kBackend.getNumberOfEvents();
 			kFrame.populateEvents(i, kBackend.calculateDeliveryTimes(i), kBackend.calculateAmountOfMail(i),
 					kBackend.calculateTotalWeightOfMail(i), kBackend.calculateTotalVolumeOfMail(i),
 					kBackend.getCriticalRoute(i), kBackend.getEvents(i), kBackend.calculateRevenue(i), 
 					kBackend.calculateExpenditure(i), kBackend.getNumberOfEvents());
 			kFrame.displayPanel("eventsPanel");
 			if (managerMode) {
 				kFrame.enableBackward();
 				kFrame.disableForward();
 			}
 			else {
 				kFrame.disableBackward();
 				kFrame.disableForward();
 			}
 			return;
 		}
 		
 		if ("Sign in as manager".equals(e.getActionCommand())) {
 			kPasswordField = new JPasswordField(10);
 			JOptionPane.showMessageDialog(kFrame, kPasswordField, "Password Required", JOptionPane.WARNING_MESSAGE);
 		
 			String pass = String.valueOf(kPasswordField.getPassword());
 			if(kBackend.authenticateManager(pass)) {
 				kFrame.manager();
 				managerMode = true;
 				kFrame.enableBackward();
 				kFrame.disableForward();
 				return;
 			}
 			else {
 				JOptionPane.showMessageDialog(kFrame, "Uh, uh, uh! You didn't say the magic word!", "Incorrect Password", JOptionPane.ERROR_MESSAGE);
 			}
 			
 		}
 		
 		if ("Sign out as manager".equals(e.getActionCommand())) {
 			kFrame.notManager();
 			managerMode = false;
 			kFrame.disableBackward();
 			kFrame.disableForward();
 			return;
 		}
 		
 		//SEND MAIL HANDLING
 		if ("Send".equals(e.getActionCommand())) {
 			
 			//Check for mail panel
 			if (kFrame.getPanel("mailPanel")) {
 
 				ArrayList<Object> info = kFrame.returnMailPanelInfo();
 				int i = 0;
 				int id = Integer.valueOf(String.valueOf(info.get(i++)));
 				i++; //Address
 				double weight = Double.valueOf(String.valueOf(info.get(i++)));
 				double volume = Double.valueOf(String.valueOf(info.get(i++)));
 				String o = String.valueOf(info.get(i++));
 				DistributionCentre origin = null;
 				String d = String.valueOf(info.get(i++));
 				DistributionCentre destination = null;
 				Priority priority = (Priority)info.get(i);
 				for (DistributionCentre dist : kBackend.getDistributionCentres()) {
 					if (dist.getName().equals(o)) { origin = dist; }
 					if (dist.getName().equals(d)) { destination = dist; }
 				}
 				
 				boolean success = kBackend.sendMail(id, weight, volume, origin, destination, priority);
 				if (success) {
 					JOptionPane.showMessageDialog(kFrame, "Mail Sent Successfully", "Send Mail", JOptionPane.INFORMATION_MESSAGE);
 					kFrame.resetMailPanel();
 				}
 				else JOptionPane.showMessageDialog(kFrame, "Mail Could Not Be Sent: No Route At That Priority Exists", "Send Mail", JOptionPane.ERROR_MESSAGE);
 			}	
 			return;
 		}
 		
 		//EVENT DISPLAY UPDATE HANDLING
 		if ("<".equals(e.getActionCommand())) {
 			int eventTime = kFrame.returnEventTime();
 			eventTime--;
 			if (eventTime >= 0) {
 				kFrame.populateEvents(eventTime, kBackend.calculateDeliveryTimes(eventTime), kBackend.calculateAmountOfMail(eventTime),
 					kBackend.calculateTotalWeightOfMail(eventTime), kBackend.calculateTotalVolumeOfMail(eventTime),
 					kBackend.getCriticalRoute(eventTime), kBackend.getEvents(eventTime), kBackend.calculateRevenue(eventTime), 
 					kBackend.calculateExpenditure(eventTime), kBackend.getNumberOfEvents());
 				kFrame.enableForward();
 				if (eventTime == 0) { kFrame.disableBackward(); }
 			}
 			else {
 				eventTime = 0;
 			}
 			return;
 		}
 		
 		if (">".equals(e.getActionCommand())) {
 			int eventTime = kFrame.returnEventTime();
 			eventTime++;
 			if (eventTime <= kBackend.getNumberOfEvents()) {
 				kFrame.populateEvents(eventTime, kBackend.calculateDeliveryTimes(eventTime), kBackend.calculateAmountOfMail(eventTime),
 					kBackend.calculateTotalWeightOfMail(eventTime), kBackend.calculateTotalVolumeOfMail(eventTime),
 					kBackend.getCriticalRoute(eventTime), kBackend.getEvents(eventTime), kBackend.calculateRevenue(eventTime), 
 					kBackend.calculateExpenditure(eventTime), kBackend.getNumberOfEvents());
 				kFrame.enableBackward();
 				if (eventTime == kBackend.getNumberOfEvents()) { kFrame.disableForward(); }
 			}
 			else { 
 				eventTime = kBackend.getNumberOfEvents();
 			}
 			return;
 		}
 		
 		//CUSTOMER PRICE UPDATE HANDLING
 		if ("Update Customer Cost".equals(e.getActionCommand())) {
 			ArrayList info = kFrame.returnCustomerPriceUpdateInfo();
 			if (info != null) {
 				int i = 0;
 				DistributionCentre origin = null;
 				DistributionCentre destination = null;
 				String o = String.valueOf(info.get(i++));
 				String d = String.valueOf(info.get(i++));
 				Firm firm = new Firm((String)info.get(i++));
 				Priority priority = (Priority)info.get(i++);
 				double customerPriceCC = Double.valueOf(String.valueOf(info.get(i++)));
 				double customerPriceG = Double.valueOf(String.valueOf(info.get(i)));
 				
 				for (DistributionCentre dist : kBackend.getDistributionCentres()) {
 					if (dist.getName().equals(o)) { origin = dist; }
 					if (dist.getName().equals(d)) { destination = dist; }
 				}
 				
				if (kBackend.updatePrice(origin, destination, customerPriceG, customerPriceCC, priority, firm) != null) { 
					JOptionPane.showMessageDialog(kFrame, "Customer Price Update Successful", "Successful Update is Successful", JOptionPane.INFORMATION_MESSAGE);
					kFrame.resetUpdatePanel();
				}
				else 
					JOptionPane.showMessageDialog(kFrame, "Customer Price Update Unuccessful", "ERROR", JOptionPane.ERROR_MESSAGE);
 			}
 			return;
 		}
 		
 		//TRANSPORT COST UPDATE HANDLING
 		if ("Update Transport Cost".equals(e.getActionCommand())) {
 			ArrayList info = kFrame.returnTransportCostUpdateInfo();
 			if (info != null) {
 				int i = 0;
 				DistributionCentre origin = null;
 				DistributionCentre destination = null;
 				String o = String.valueOf(info.get(i++));
 				String d = String.valueOf(info.get(i++));
 				Firm firm = new Firm((String)info.get(i++));
 				Priority priority = (Priority)info.get(i++);
 				double transportPriceCC = Double.valueOf(String.valueOf(info.get(i++)));
 				double transportPriceG = Double.valueOf(String.valueOf(info.get(i++)));
 				int frequency = Integer.valueOf(String.valueOf(info.get(i++)));;
 				int duration = Integer.valueOf(String.valueOf(info.get(i++)));;;
 				Day day = (Day)info.get(i);
 				
 				for (DistributionCentre dist : kBackend.getDistributionCentres()) {
 					if (dist.getName().equals(o)) { origin = dist; }
 					if (dist.getName().equals(d)) { destination = dist; }
 				}
 				
 				kBackend.updateTransport(origin, destination, transportPriceG, transportPriceCC, frequency, duration, day, priority, firm);
 				JOptionPane.showMessageDialog(kFrame, "Transport Cost Update Successful", "Successful Update is Successful", JOptionPane.INFORMATION_MESSAGE);				kFrame.resetUpdatePanel();
 			}
 			return;
 		}
 		
 		//DISCONTINUE TRANSPORT HANDLING
 		if ("Discontinue Transport".equals(e.getActionCommand())) {
 			ArrayList info = kFrame.returnDiscontinueTransportInfo();
 			if (info != null) {
 				int i = 0;
 				DistributionCentre origin = null;
 				DistributionCentre destination = null;
 				String o = String.valueOf(info.get(i++));
 				String d = String.valueOf(info.get(i++));
 				Firm firm = new Firm((String)info.get(i++));
 				Priority priority = (Priority)info.get(i++);
 				
 				for (DistributionCentre dist : kBackend.getDistributionCentres()) {
 					if (dist.getName().equals(o)) { origin = dist; }
 					if (dist.getName().equals(d)) { destination = dist; }
 				}
 				
				if (kBackend.discontinueTransport(origin, destination, priority, firm) == true) {
					JOptionPane.showMessageDialog(kFrame, "Discontinue Update Successful", "Successful Update is Successful", JOptionPane.INFORMATION_MESSAGE);
					kFrame.resetUpdatePanel();
				}
				JOptionPane.showMessageDialog(kFrame, "Discontinue Update Failed: No Such Transport Exists", "ERROR", JOptionPane.ERROR_MESSAGE);
 				
 			}
 			return;			
 		}
 		
 		//CANCEL BUTTON HANDLING
 		if ("Cancel".equals(e.getActionCommand())) {
 			kFrame.resetAll();
 			return;
 		}
 		
 		//HELP OPTIONS
 		if ("About KPSSmart".equals(e.getActionCommand())) {
 			JOptionPane.showMessageDialog(kFrame, "KPSSmart by\n  Nick Malcolm\n  Liam O'Connor\n " +
 					" Janella Espinas\n  Sean Arnold\n  Robert Crowe");
 			return;
 		}
 	}
 }
