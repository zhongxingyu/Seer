 package no.thunaes.petter.svg.app.gui;
 
 import java.awt.BorderLayout;
 
 import javax.swing.JPanel;
 
 import no.thunaes.petter.svg.app.gui.domain.ChartInput;
 
 public class DomainView extends JPanel {
 
 	ChartInput chartInput;
 	
 	public DomainView() {
 		setLayout(new BorderLayout());
		add(chartInput = new ChartInput(), BorderLayout.LINE_START);
 		
 	}
 	
 }
