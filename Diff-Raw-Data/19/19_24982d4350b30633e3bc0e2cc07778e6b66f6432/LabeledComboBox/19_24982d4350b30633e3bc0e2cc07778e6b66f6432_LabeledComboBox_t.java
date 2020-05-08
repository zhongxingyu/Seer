 package guiElements;
 
 import java.awt.event.ActionListener;
 
 import javax.swing.BoxLayout;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 @SuppressWarnings("serial")
 public class LabeledComboBox extends JPanel{
 	private JLabel label;
 	private JComboBox box;
 	public LabeledComboBox(String labelString, Object[] items){
 		this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
 		this.label = new JLabel(labelString);
 		this.box = new JComboBox(items);
 		this.add(label);
 		this.add(box);
 	}
 	
 	public void setLabel(String newLabelString){
 		label.setText(newLabelString);
 	}
 	
	public JComboBox getComboBox() {
		return box;
	}
	
 	public void addActionListener(ActionListener l){
 		box.addActionListener(l);
 	}
 	
 	public Object getSelectedItem(){
 		return box.getSelectedItem();
 	}
 	
 	public void setSelectedItem(Object o){
 		box.setSelectedItem(o);
 	}
 	
 	public void setSelectedIndex(int index){
 		box.setSelectedIndex(index);
 	}
 	
 	
 
 }
