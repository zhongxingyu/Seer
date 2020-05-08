 package no.thunaes.petter.svg.app.gui.domain;
 
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 
 import javax.swing.BoxLayout;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 
 import no.smidsrod.robin.svg.library.Chart;
 import no.smidsrod.robin.svg.library.Item;
 
 public class ValueEditWindow extends JFrame {
 
 	private static final long serialVersionUID = 1L;
 	private ValuesPanel valuesPanel;
 	private Item item;
 
 	public ValueEditWindow(ItemPanel itemPanel) {
 		item = itemPanel.getItem();
 		setLayout(new FlowLayout(FlowLayout.LEFT));
 		setSize(400, 500);
 		setLocationRelativeTo(null);
 		updateTitle();
 
 		add(new ValueMenuPanel(itemPanel));
 
 		JPanel wrapperValuesPanel = new JPanel();
 		wrapperValuesPanel.setPreferredSize(new Dimension(380, 417));
 		wrapperValuesPanel.setLayout(new BoxLayout(wrapperValuesPanel,
 				BoxLayout.X_AXIS));
 		wrapperValuesPanel.add(new JScrollPane(valuesPanel = new ValuesPanel(
 				itemPanel), JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
 				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
 		add(wrapperValuesPanel);
 	}
 
 	private String calcTitle() {
 		String itemName = item.getName().isEmpty() ? "<no name>" : item
 				.getName();
 		return "Values for item: " + itemName;
 	}
 
	public int getValuePanelCount() {
		return valuesPanel.getComponentCount();
	}

 	public void addValuePanel(Chart c) {
 		valuesPanel.addValuePanel(c);
 
 	}
 
 	public void removeValuePanel(ValuePanel v) {
 		valuesPanel.removeValuePanel(v);
 
 	}
 
 	public void generateValueFields(ValuePanel v, Chart c) {
 		valuesPanel.generateValueFields(v, c);
 
 	}
 
 	public void updateTitle() {
 		setTitle(calcTitle());
 	}
 }
