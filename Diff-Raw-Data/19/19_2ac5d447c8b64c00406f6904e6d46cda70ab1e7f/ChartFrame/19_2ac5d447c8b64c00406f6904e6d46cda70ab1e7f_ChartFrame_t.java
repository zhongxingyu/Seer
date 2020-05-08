 package no.thunaes.petter.svg.app.gui.domain;
 
import javax.swing.BoxLayout;
 import javax.swing.JPanel;
 
 public class ChartFrame extends JPanel {
 	
 	ChartSettingsPanel chartSettings;
	ChartValuesPanel chartValues;
 	
 	public ChartFrame() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
 		add(chartSettings = new ChartSettingsPanel());
		add(chartValues = new ChartValuesPanel());
 	}
 
 }
