 package gui;
 
 import gui.PickPlottablesPanel.PlottableInfo;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 
 import javax.swing.JCheckBox;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.ListCellRenderer;
 
 public class PlottableItemRenderer extends JPanel implements ListCellRenderer {
 
 	Color stripeColor = new Color(220, 230, 240);
 	JCheckBox checkBox;
 	JLabel label;
 	
 	public PlottableItemRenderer() {
		setLayout(new FlowLayout(FlowLayout.LEFT));
 		checkBox = new JCheckBox();
		label = new JLabel();
 		this.add(checkBox);
 		this.add(label);
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
		label.setText(info.label + "    [" + info.descriptor + "]");
 		checkBox.setSelected(info.selected);
 		return this;
 	}
 
 }
