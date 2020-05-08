 package no.thunaes.petter.svg.app.gui.domain;
 
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JPanel;
 
 
 public class ChartValuesPanel extends JPanel {
 
 	JButton itemAddButton = new JButton("+");
 	ItemPanels itemPanels = new ItemPanels();
 	
 	public ChartValuesPanel() {
 		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
 		add(itemAddButton);
		add(itemPanels);
 	}
 }
