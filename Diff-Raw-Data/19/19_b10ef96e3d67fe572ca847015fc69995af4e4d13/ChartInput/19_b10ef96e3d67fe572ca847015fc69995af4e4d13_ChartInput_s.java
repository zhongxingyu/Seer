 package no.thunaes.petter.svg.app.gui.domain;
 
 import javax.swing.BoxLayout;
 import javax.swing.JPanel;
 
 public class ChartInput extends JPanel {
 	
 	ChartSettings chartSettings;
 	ChartValues chartValues;
 	
 	public ChartInput() {
		setLayout(new BoxLayout(this, 1));
 		add(chartSettings = new ChartSettings());
 		add(chartValues = new ChartValues());
 	}
 
 }
