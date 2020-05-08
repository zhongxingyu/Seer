 package vms.gui;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import vms.*;
 import vms.Alert.AlertType;
 import common.*;
 
 import java.awt.Graphics;
 import java.util.ArrayList;
 import java.util.List;
 
 public class AlertPanel extends JPanel {
 	private List<Alert> _Alerts;
 
 	public AlertPanel() {
 		_Alerts = new ArrayList<Alert>();
 	}
 
 	public void update(final List<Alert> alerts) {
 		_Alerts = alerts;
 		this.repaint();
 	}
 	
 	public void paintComponent(Graphics g) {
 		Graphics2D g2 = (Graphics2D)g;
 		super.paintComponent(g);
 		int wWidth = 140;
 		int wHeight = 140;
 		Alert worstAlert = null;
 		for (Alert a : _Alerts) {
 			if (worstAlert == null || a.getType() == AlertType.HIGHRISK) {
 				worstAlert = a;
 				break;
 			}
 		}
 		
 
 		// Draw circle green if none of the if statements are active
 		g2.setColor(Color.green);
 		
 		// Draws circles around the ships; adds color if there is a high-risk or low-risk alert
 		if (worstAlert != null && worstAlert.getType() == AlertType.HIGHRISK)
 			g2.setColor(Color.red);
 					
 		if (worstAlert != null && worstAlert.getType() == AlertType.LOWRISK)
 			g2.setColor(Color.yellow);
 		
		g2.fillOval(wWidth/2, wHeight/2, wWidth, wHeight);
 	}
 
 }
