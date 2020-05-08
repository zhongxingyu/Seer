 package no.thunaes.petter.svg.app.gui;
 
 import java.awt.BorderLayout;
 
 import javax.swing.JPanel;
 
 public class CenterPanel extends JPanel {
 
 	DomainView domainview;
 	
 	public CenterPanel() {
 		setLayout(new BorderLayout());
		add(domainview = new DomainView());
 	}
 
 }
