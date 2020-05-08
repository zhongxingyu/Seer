 package guiElements;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
import javax.swing.JComboBox;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 
 import dataStructures.Alternative;
 import dataStructures.Attribute;
 import dataStructures.AttributeKey;
 import dataStructures.DomainValue;
 import dataStructures.maps.AttributeMap;
 import dataStructures.maps.ValueMap;
 
 @SuppressWarnings("serial")
 public class CustomAlternativeDialog extends JDialog implements ActionListener{
 	//private JFrame parentFrame;
 	private AttributeMap map;
 	private LabeledComboBox[] boxes;
 	private Alternative singleAlternative;
 	private JButton okButton;
 	
 	
 	public CustomAlternativeDialog(JFrame parentFrame, AttributeMap map,Alternative singleAlternative){
 		super(parentFrame,true);
 		//this.parentFrame=parentFrame;
 		this.singleAlternative = singleAlternative;
 		this.map=map;
 		getContentPane().add(createGUI());
 		pack();
 		setLocationRelativeTo(parentFrame);
 		setVisible(true);
 	}
 	
 	private JPanel createGUI(){
 		JPanel panel = new JPanel();
 		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
 		
 		Attribute[] allAttributes = map.values().toArray(new Attribute[0]);
 		// set up the boxes
 		boxes = new LabeledComboBox[allAttributes.length];
 		for (int i = 0; i < boxes.length; i++) {
 			DomainValue[] values = allAttributes[i].getObject()
 					.getDomainValueList().toArray();
 			boxes[i] = new LabeledComboBox(allAttributes[i].getName(),values);
 			boxes[i].addActionListener(this);
 			panel.add(boxes[i]);
 		}
 		okButton = new JButton("ok");
 		okButton.addActionListener(this);
 		panel.add(okButton);
 		// then add a valuemap to the alternative with the defaults if there
 		// isn't one yet
 		if(singleAlternative==null){
 			singleAlternative = new Alternative("custom alternative",new Integer(-1),new ValueMap());
 		}
 		
 		ValueMap valueMap = singleAlternative.getObject();
 		if (valueMap == null || valueMap.isEmpty()) {
 			ValueMap newMap = new ValueMap();
 			for (LabeledComboBox box : boxes) {
 				DomainValue defaultValue = (DomainValue) box.getSelectedItem();
 				newMap.put(defaultValue.getAttributeKey(), defaultValue);
 			}
 			singleAlternative.setObject(newMap);
 
 			// if there is already a populated valueMap, then set the boxes to reflect
 			// what has already been selected
 		} else {
 			for (LabeledComboBox box : boxes) {
 				DomainValue defaultValue = (DomainValue) box.getSelectedItem();
 				AttributeKey aKey = defaultValue.getAttributeKey();
 //				System.out.println("aKey: " + aKey.getKey() + " defaultValue: "
 //						+ defaultValue.toString());
 				box.setSelectedItem(valueMap.get(aKey));
 			}
 		}
 		return panel;
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if (e.getSource().getClass() == JComboBox.class) {
			JComboBox activeComboBox = (JComboBox) e.getSource();
 			for (LabeledComboBox b : boxes) {
				if (activeComboBox == b.getComboBox()) {
 					//System.out.println(b.getSelectedItem().getClass());
 					DomainValue selectedValue = (DomainValue) b
 							.getSelectedItem();
 
 					if (singleAlternative == null) {
 						System.err
 								.println("In ValueTuple: Alternative should not be null!");
 					}
 //					System.out.println("Alternative: " + a.getName()
 //							+ " selectedValue: " + selectedValue.toString()
 //							+ " svKey: " + selectedValue.getAttributeKey());
 
 					ValueMap valueMap = singleAlternative.getObject();
 					if (valueMap == null) {
 						System.err
 								.println("In ValueTuple: ValueMap should not be null!");
 					} else
 						valueMap.put(selectedValue.getAttributeKey(),
 								selectedValue);
 					break;
 				}
 			}
 		}else if(e.getSource()==okButton){
 			dispose();
 		}
 	}
 
 	public Alternative getAlternative() {
 		return singleAlternative;
 	}
 
 }
