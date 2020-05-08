 package no.thunaes.petter.svg.app.gui.domain;
 
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JPanel;
import javax.swing.JScrollPane;
 
 
 public class ChartValuesPanel extends JPanel {
 
 	JButton itemAddButton = new JButton("+");
 	ItemPanels itemPanels = new ItemPanels();
	JScrollPane scrollPanel;
 	
 	public ChartValuesPanel() {
 		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		scrollPanel = new JScrollPane(itemPanels);
		//scrollPanel;
 		add(itemAddButton);
		add(scrollPanel);

 	}
 }
