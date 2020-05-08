 package ClassAdminFrontEnd;
 
 import java.awt.BorderLayout;
 import java.awt.Checkbox;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.LayoutManager;
 import java.awt.TextField;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
import javax.swing.JCheckBox;
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JSpinner;
 import javax.swing.SpinnerNumberModel;
 
 import ClassAdminBackEnd.EntityType;
 import ClassAdminBackEnd.Project;
 
 public class EntityTypePanel extends JPanel {
 	Project project;
 	JButton btnCreateEntityType;
 	TextField txfName;
	JCheckBox txfIsTextField;
 	SpinnerNumberModel SNMDefaultWeight;
 
 	public EntityTypePanel(final Project project) {
 		setLayout(new BorderLayout());
 		this.project = project;
 		
 		JPanel namePnl = new JPanel();
 		
 		namePnl.add(new JLabel("Enter the entity type's name"));
 		txfName = new TextField(20);
 		namePnl.add(txfName);
 		
 		JPanel isTextFieldPnl = new JPanel();
 		isTextFieldPnl.add(new JLabel("Text field?"));
		txfIsTextField = new JCheckBox();
 		isTextFieldPnl.add(txfIsTextField);
 		
 		JPanel numberPnl = new JPanel();
 		SpinnerNumberModel SNMDefaultWeight = new SpinnerNumberModel(
 				new Integer(0), // value
 				new Integer(0), // min
 				new Integer(100), // max
 				new Integer(1) // step
 		);
		final JSpinner numberChooser = new JSpinner(SNMDefaultWeight);
		numberPnl.add(new JLabel("Default weight of this field (only if not text field)"));
		numberPnl.add(numberChooser);
 		btnCreateEntityType = new JButton("Create new entity type");
 		
 		this.add(namePnl,BorderLayout.NORTH);
		this.add(isTextFieldPnl,BorderLayout.WEST);
		this.add(numberPnl,BorderLayout.CENTER);
 		this.add(btnCreateEntityType,BorderLayout.PAGE_END);
 
 		btnCreateEntityType.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
				System.out.println();
 
				if (txfName.getText().compareTo("") != 0) {					
						EntityType entType = new EntityType(txfName.getText(), null, null, null, txfIsTextField.isSelected(), null,Double.parseDouble(numberChooser.getValue().toString()) );
						project.getEntityTypes().add(entType);
					 
 				} else {
 					// TO DO
 				}
 			}
 		});
 	}
 
 }
