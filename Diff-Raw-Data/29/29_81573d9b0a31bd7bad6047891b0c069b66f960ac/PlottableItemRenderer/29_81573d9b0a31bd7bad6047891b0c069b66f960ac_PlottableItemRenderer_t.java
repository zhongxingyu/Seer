 package gui;
 
 import gui.PickPlottablesPanel.PlottableInfo;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 
import javax.swing.Box;
import javax.swing.BoxLayout;
 import javax.swing.JCheckBox;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.ListCellRenderer;
 
 public class PlottableItemRenderer extends JPanel implements ListCellRenderer {
 
 	Color stripeColor = new Color(220, 230, 240);
 	JCheckBox checkBox;
 	JLabel label;
	JLabel prop;
 	
 	public PlottableItemRenderer() {
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
 		checkBox = new JCheckBox();
		label = new JLabel("  ");
		prop = new JLabel("  ");
 		this.add(checkBox);
		this.add(Box.createHorizontalStrut(15));
 		this.add(label);
		this.add(Box.createHorizontalStrut(30));
		this.add(prop);
 		setPreferredSize(new Dimension(200, 30));
 		setMaximumSize(new Dimension(200, 30));
 
 	}
 	
 	
 	@Override
 	public Component getListCellRendererComponent(JList arg0, Object value,
 			int index, boolean selected, boolean hasFocus) {
 		
 		if (index%2==0) {
 			setBackground(Color.white);
 		}
 		else {
 			setBackground(stripeColor);
 		}
 		
 		
 		PlottableInfo info = (PlottableInfo)value;
		label.setText(info.label);
		prop.setText("<html><em> " + info.descriptor + "</em></html>");
 		checkBox.setSelected(info.selected);
 		return this;
 	}
 
 }
