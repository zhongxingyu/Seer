 package gui;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Dialog;
 import java.awt.FlowLayout;
 import java.awt.Frame;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.sql.Date;
 import java.sql.SQLException;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JPanel;
 import javax.swing.border.EmptyBorder;
 import javax.swing.GroupLayout;
 import javax.swing.GroupLayout.Alignment;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JTextField;
 import javax.swing.LayoutStyle.ComponentPlacement;
 import com.toedter.calendar.JDateChooser;
 
 import db.DBInterface;
 import main.*;
 
 public class AddIngredient extends JDialog {
 
 	private final JPanel contentPanel = new JPanel();
 	private JTextField textField;
 	private JTextField textField_1;
 	private JTextField textField_2;
 
 	/*
 	/**
 	 * Launch the application.
 	 
 	public static void main(String[] args) {
 		try {
 			AddIngredient dialog = new AddIngredient();
 			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
 			dialog.setVisible(true);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Create the dialog.
 	 */
 	public AddIngredient(Component parent) {
 	  super((Frame) parent); //for modailty i need to pass the parent through to here and use the super constructor to lock that parent down
 	  final Component parentFrame = parent;
 		setIconImage(Toolkit.getDefaultToolkit().getImage(AddIngredient.class.getResource("/gui/resources/img16x16/list-add-5.png")));
 		setTitle("Add Ingredient");
 		setBounds(100, 100, 450, 243);
 		setModalityType(Dialog.ModalityType.DOCUMENT_MODAL);
 		getContentPane().setLayout(new BorderLayout());
 		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
 		getContentPane().add(contentPanel, BorderLayout.WEST);
 		
 		JLabel label = new JLabel("Price:");
 		
 		JLabel label_1 = new JLabel("Name:");
 		
 		textField = new JTextField();
 		textField.setColumns(10);
 		
 		textField_1 = new JTextField();
 		textField_1.setColumns(10);
 		
 		JLabel label_2 = new JLabel("Stock:");
 		
 		textField_2 = new JTextField();
 		textField_2.setColumns(10);
 		
 		final JDateChooser dateChooser = new JDateChooser();
 		
 		JLabel lblBestBeforeDate = new JLabel("Due Date:");
 		GroupLayout gl_contentPanel = new GroupLayout(contentPanel);
 		gl_contentPanel.setHorizontalGroup(
 		  gl_contentPanel.createParallelGroup(Alignment.LEADING)
 		    .addGroup(gl_contentPanel.createSequentialGroup()
 		      .addGap(31)
 		      .addGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING)
 		        .addComponent(label_1, GroupLayout.PREFERRED_SIZE, 52, GroupLayout.PREFERRED_SIZE)
 		        .addGroup(gl_contentPanel.createSequentialGroup()
 		          .addGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING)
 		            .addComponent(label, GroupLayout.PREFERRED_SIZE, 52, GroupLayout.PREFERRED_SIZE)
 		            .addComponent(lblBestBeforeDate))
 		          .addGap(6)
 		          .addGroup(gl_contentPanel.createParallelGroup(Alignment.TRAILING, false)
 		            .addComponent(dateChooser, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 		            .addGroup(Alignment.LEADING, gl_contentPanel.createSequentialGroup()
 		              .addComponent(textField_1, GroupLayout.PREFERRED_SIZE, 116, GroupLayout.PREFERRED_SIZE)
 		              .addGap(32)
 		              .addGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING)
 		                .addComponent(label_2, GroupLayout.PREFERRED_SIZE, 52, GroupLayout.PREFERRED_SIZE)
 		                .addGroup(gl_contentPanel.createSequentialGroup()
 		                  .addGap(43)
 		                  .addComponent(textField_2, GroupLayout.PREFERRED_SIZE, 116, GroupLayout.PREFERRED_SIZE))))
 		            .addComponent(textField, GroupLayout.DEFAULT_SIZE, 307, Short.MAX_VALUE))))
 		      .addContainerGap(56, Short.MAX_VALUE))
 		);
 		gl_contentPanel.setVerticalGroup(
 		  gl_contentPanel.createParallelGroup(Alignment.LEADING)
 		    .addGroup(gl_contentPanel.createSequentialGroup()
 		      .addGap(23)
 		      .addGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING)
 		        .addComponent(textField, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
 		        .addGroup(gl_contentPanel.createSequentialGroup()
 		          .addGap(3)
 		          .addComponent(label_1, GroupLayout.PREFERRED_SIZE, 16, GroupLayout.PREFERRED_SIZE)))
 		      .addGap(23)
 		      .addGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING)
 		        .addGroup(Alignment.TRAILING, gl_contentPanel.createSequentialGroup()
 		          .addGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING)
 		            .addComponent(textField_1, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
 		            .addGroup(gl_contentPanel.createSequentialGroup()
 		              .addGap(3)
 		              .addComponent(label_2, GroupLayout.PREFERRED_SIZE, 16, GroupLayout.PREFERRED_SIZE))
 		            .addComponent(textField_2, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))
 		          .addGap(18))
 		        .addGroup(Alignment.TRAILING, gl_contentPanel.createSequentialGroup()
 		          .addComponent(label, GroupLayout.PREFERRED_SIZE, 16, GroupLayout.PREFERRED_SIZE)
 		          .addGap(21)))
 		      .addGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING)
 		        .addComponent(lblBestBeforeDate, GroupLayout.PREFERRED_SIZE, 16, GroupLayout.PREFERRED_SIZE)
 		        .addComponent(dateChooser, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
 		      .addContainerGap(16, Short.MAX_VALUE))
 		);
 		contentPanel.setLayout(gl_contentPanel);
 		{
 			JPanel buttonPane = new JPanel();
 			buttonPane.setLayout(new FlowLayout(FlowLayout.CENTER));
 			getContentPane().add(buttonPane, BorderLayout.SOUTH);
 			{
 				JButton okButton = new JButton("Accept");
 				okButton.addActionListener(new ActionListener() {
 				  public void actionPerformed(ActionEvent arg0) {
 				    try
             {
 				      Ingredient ingredient = new Ingredient(textField.getText(), Integer.parseInt(textField_2.getText()),  
 				                                            Double.parseDouble(textField_1.getText()), new java.sql.Date(dateChooser.getDate().getTime()));
 				      
 				      InputVerifier.verifyIngredient(ingredient);
 				      
 				      DBInterface.addIngredient(ingredient);
 				      
             } catch (SQLException e)
             {
               // TODO Auto-generated catch block
               e.printStackTrace();
             }
 				    catch(Exception e)
 				    {
				      Error err = new Error(parentFrame, e.getMessage());
 				      err.setVisible(true);
               e.printStackTrace();
 				    }
 				    
 				    
 				    
 				  }
 				});
 				okButton.setActionCommand("OK");
 				okButton.setIcon(new ImageIcon(AddIngredient.class.getResource("/gui/resources/img32x32/dialog-ok-apply-2.png")));
 				okButton.setBounds(69, 0, 118, 38);
 				buttonPane.add(okButton);
 				getRootPane().setDefaultButton(okButton);
 			}
 			{
 				JButton cancelButton = new JButton("Cancel");
 				cancelButton.addActionListener(new ActionListener() {
 					public void actionPerformed(ActionEvent arg0) {
 						//logic goes here
 						//just close the window on cancel
 						setVisible(false);
 					}
 				});
 				cancelButton.setIcon(new ImageIcon(AddIngredient.class.getResource("/gui/resources/img32x32/dialog-cancel-2.png")));
 				cancelButton.setBounds(199, 0, 118, 38);
 				cancelButton.setActionCommand("Cancel");
 				buttonPane.add(cancelButton);
 			}
 		}
 	}
 }
