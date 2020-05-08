 package com.apps4you.client;
 
 import com.apps4you.shared.*;
 
 import java.awt.BorderLayout;
 import java.awt.FlowLayout;
 
import javax.swing.ComboBoxModel;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JPanel;
 import javax.swing.border.EmptyBorder;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import javax.swing.JLabel;
 import java.awt.Font;
import java.util.ArrayList;
import java.util.Vector;

 import javax.swing.JTextField;
 import javax.swing.JComboBox;
 import javax.swing.DefaultComboBoxModel;
 
 public class CreateWarrior extends JDialog {
 
 	
 	private static final long serialVersionUID = -3237504880400716966L;
 	
 	private final JPanel contentPanel = new JPanel();
 	private JTextField nameTextField;
 	private JTextField descTextField;
 	private JButton okButton = new JButton("OK");
 	private JButton cancelButton = new JButton("Cancel");
 	private JComboBox originComboBox = new JComboBox();
 	
 	/**
 	 * Create the dialog.
 	 */
 	public CreateWarrior() {
 		setTitle("Create a New Warrior");
 		setBounds(100, 100, 306, 300);
 		getContentPane().setLayout(new BorderLayout());
 		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
 		getContentPane().add(contentPanel, BorderLayout.CENTER);
 		contentPanel.setLayout(null);
 		{
 			JLabel newLabel = new JLabel("Name:");
 			newLabel.setFont(new Font("Tahoma", Font.BOLD, 11));
 			newLabel.setBounds(21, 11, 46, 14);
 			contentPanel.add(newLabel);
 		}
 		
 		nameTextField = new JTextField();
 		nameTextField.setBounds(73, 8, 189, 20);
 		contentPanel.add(nameTextField);
 		nameTextField.setColumns(30);
 		
 		JLabel originLabel = new JLabel("Origin:");
 		originLabel.setFont(new Font("Tahoma", Font.BOLD, 11));
 		originLabel.setBounds(21, 47, 46, 14);
//		Vector<String> valuesList = new Vector<String>();
//		for(Origins origin: Origins.values()){
//			valuesList.add(origin.getOrigin());
//		}
		
 		contentPanel.add(originLabel);
 		originComboBox.setModel(new DefaultComboBoxModel(Origins.values()));
 		
 
 		originComboBox.setBounds(73, 39, 189, 25);
 		contentPanel.add(originComboBox);
 		
 		JLabel descriptionLabel = new JLabel("Description:");
 		descriptionLabel.setFont(new Font("Tahoma", Font.BOLD, 11));
 		descriptionLabel.setBounds(21, 84, 75, 14);
 		contentPanel.add(descriptionLabel);
 		
 		descTextField = new JTextField();
 		descTextField.setBounds(21, 109, 251, 82);
 		contentPanel.add(descTextField);
 		descTextField.setColumns(10);
 		
 		ButtonHandler handler = new ButtonHandler();
 		
 		
 		{
 			JPanel buttonPane = new JPanel();
 			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
 			getContentPane().add(buttonPane, BorderLayout.SOUTH);
 			{
 
 				okButton.addActionListener(handler);
 				okButton.setActionCommand("OK");
 				buttonPane.add(okButton);
 				getRootPane().setDefaultButton(okButton);
 			}
 			{
 
 				cancelButton.addActionListener(handler);
 				cancelButton.setActionCommand("Cancel");
 				buttonPane.add(cancelButton);
 			}
 		} 
 	}
 	
 	public String getNewName()
 	{
 		return this.nameTextField.getText().toString();
 		
 	}
 	public Origins getOrigin()
 	{
 		return Origins.valueOf(this.originComboBox.getSelectedItem().toString());
 
 	}
 	public String getDescription()
 	{
 		return this.descTextField.toString();
 	}
 	
     private class ButtonHandler implements ActionListener
     {
 
 		@Override
 		public void actionPerformed(ActionEvent e)
 		{
 			if (e.getSource() == okButton) 
 		    {
 				CreateWarrior.this.setVisible(false);
 		    }
 			else if (e.getSource() == cancelButton)
 		    {	    	
 				CreateWarrior.this.dispose();
 		    }
 		}
     }
 }
