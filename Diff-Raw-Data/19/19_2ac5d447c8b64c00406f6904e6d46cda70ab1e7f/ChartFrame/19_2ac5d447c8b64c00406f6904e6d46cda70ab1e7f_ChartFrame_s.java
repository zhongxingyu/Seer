 package no.thunaes.petter.svg.app.gui.domain;
 
import java.awt.GridLayout;

 import javax.swing.JPanel;
 
 public class ChartFrame extends JPanel {
 	
 	ChartSettingsPanel chartSettings;
 	
 	public ChartFrame() {
		setLayout(new GridLayout(1,1));		
 		add(chartSettings = new ChartSettingsPanel());
 	}
 
 }
