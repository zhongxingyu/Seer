 package de.htwg.fourInARow.aview.gui;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.border.EmptyBorder;
 import javax.swing.ButtonGroup;
 import javax.swing.GroupLayout;
 import javax.swing.GroupLayout.Alignment;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JTextField;
 import javax.swing.LayoutStyle.ComponentPlacement;
 import javax.swing.JButton;
 import javax.swing.JRadioButton;
 import javax.swing.JComboBox;
 import javax.swing.JSeparator;
 
 import de.htwg.fourInARow.controller.IFourInARowController;
 import de.htwg.fourInARow.model.IPlayer;
 import de.htwg.fourInARow.model.impl.Player;
 import de.htwg.util.Constants;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.util.Map.Entry;
 import java.util.Vector;
 
 public class ConfigurationManager extends JDialog {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	private JTextField textField;
 	private JTextField textField1;
 	private IFourInARowController controller;
 	private JFrame parent;
 	private JRadioButton rdbtnHuman;
 	private JRadioButton radioButton;
 	private JRadioButton rdbtnComputer;
	private JComboBox<String> comboBox;
	private JComboBox<String> comboBox2;
 	
 	public ConfigurationManager(final JFrame parent, final IFourInARowController controller) {
 		this.controller = controller;
 		this.parent = parent;
 		setTitle("X in a Row");
 		setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
 		setResizable(false);
 		setAlwaysOnTop(true);
 		setBounds(100, 100, 400, 390);
 		JPanel contentPane = new JPanel();
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		setContentPane(contentPane);
 		
 		JLabel lblPlayer = new JLabel("Player 1: ");
 		
 		JLabel lblPlayer1 = new JLabel("Player 2:");
 		
 		textField = new JTextField();
 		textField.setColumns(10);
 		textField.setText(controller.getPlayerOne().getName());
 		
 		textField1 = new JTextField();
 		textField1.setColumns(10);
 		textField1.setText(controller.getPlayerTwo().getName());		
 		
 		
 		JLabel lblMode = new JLabel("Mode:");
 		
 		ButtonGroup groupPlayer1 = new ButtonGroup();
 		rdbtnHuman = new JRadioButton("Human");
 		rdbtnHuman.setSelected(true);
 		rdbtnComputer = new JRadioButton("Computer");
 		groupPlayer1.add(rdbtnHuman);
 		groupPlayer1.add(rdbtnComputer);
 		
 		ButtonGroup groupPlayer2 = new ButtonGroup();
 		radioButton = new JRadioButton("Human");
 		radioButton.setSelected(true);
 		JRadioButton radioButton1 = new JRadioButton("Computer");
 		groupPlayer2.add(radioButton);
 		groupPlayer2.add(radioButton1);
 		
 		JLabel lblIdentifier = new JLabel("Identifier:");
 		
 		Vector<String> values = fillBox();
		comboBox = new JComboBox<String>(values);
 		
 		comboBox.setSelectedIndex(3);
 		
 		JLabel lblIndentifier = new JLabel("Indentifier:");
 		
		comboBox2= new JComboBox<String>(values);
 		comboBox2.setSelectedIndex(4);
 		
 		JSeparator separator = new JSeparator();
 		
 		JSeparator separator1 = new JSeparator();
 		
 		JButton btnOk = new JButton("OK");
 		btnOk.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				setPlayer();			
 			}		
 		});
 				
 		JButton btnCancel = new JButton("Close");
 		btnCancel.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				controller.setStatusMessage("Player configuration settings canceled.");
 				parent.setEnabled(true);
 				dispose();
 			}
 		});
 		
 		JLabel label = new JLabel("Mode:");
 		
 		JSeparator separator2 = new JSeparator();
 		
 		JSeparator separator3 = new JSeparator();
 		
 		JLabel lblPleaseEnterSettings = new JLabel("Please enter Settings and start the game.");
 		lblPleaseEnterSettings.setFont(new Font("Arial", Font.PLAIN, 16));
 		GroupLayout glcontentPane = new GroupLayout(contentPane);
 		glcontentPane.setHorizontalGroup(
 			glcontentPane.createParallelGroup(Alignment.LEADING)
 				.addGroup(glcontentPane.createSequentialGroup()
 					.addGroup(glcontentPane.createParallelGroup(Alignment.LEADING)
 						.addGroup(glcontentPane.createSequentialGroup()
 							.addComponent(separator2, GroupLayout.PREFERRED_SIZE, 1, GroupLayout.PREFERRED_SIZE)
 							.addGap(9)
 							.addGroup(glcontentPane.createParallelGroup(Alignment.LEADING)
 								.addGroup(glcontentPane.createParallelGroup(Alignment.TRAILING)
 									.addGroup(glcontentPane.createSequentialGroup()
 										.addComponent(btnOk)
 										.addPreferredGap(ComponentPlacement.RELATED)
 										.addComponent(btnCancel))
 									.addComponent(separator1, GroupLayout.PREFERRED_SIZE, 360, GroupLayout.PREFERRED_SIZE))
 								.addGroup(glcontentPane.createSequentialGroup()
 									.addGroup(glcontentPane.createParallelGroup(Alignment.LEADING)
 										.addComponent(lblIdentifier)
 										.addGroup(glcontentPane.createSequentialGroup()
 											.addPreferredGap(ComponentPlacement.RELATED)
 											.addComponent(lblPlayer1))
 										.addGroup(glcontentPane.createSequentialGroup()
 											.addPreferredGap(ComponentPlacement.RELATED)
 											.addComponent(label, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE))
 										.addGroup(glcontentPane.createSequentialGroup()
 											.addPreferredGap(ComponentPlacement.RELATED)
 											.addComponent(lblIndentifier)))
 									.addGap(18)
 									.addGroup(glcontentPane.createParallelGroup(Alignment.LEADING, false)
 										.addComponent(comboBox2, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 										.addComponent(comboBox, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 										.addGroup(Alignment.TRAILING, glcontentPane.createSequentialGroup()
 											.addComponent(radioButton, GroupLayout.PREFERRED_SIZE, 59, GroupLayout.PREFERRED_SIZE)
 											.addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 											.addComponent(radioButton1, GroupLayout.PREFERRED_SIZE, 73, GroupLayout.PREFERRED_SIZE))
 										.addComponent(textField1, 141, 141, Short.MAX_VALUE)
 										.addComponent(textField, GroupLayout.DEFAULT_SIZE, 141, Short.MAX_VALUE)
 										.addComponent(rdbtnComputer, Alignment.TRAILING))
 									.addGap(191)
 									.addComponent(separator, GroupLayout.PREFERRED_SIZE, 1, GroupLayout.PREFERRED_SIZE))
 								.addGroup(glcontentPane.createSequentialGroup()
 									.addComponent(lblMode)
 									.addGap(43)
 									.addComponent(rdbtnHuman, GroupLayout.DEFAULT_SIZE, 128, Short.MAX_VALUE)
 									.addGap(204))
 								.addGroup(glcontentPane.createSequentialGroup()
 									.addPreferredGap(ComponentPlacement.RELATED)
 									.addComponent(lblPlayer))))
 						.addGroup(glcontentPane.createSequentialGroup()
 							.addContainerGap()
 							.addComponent(separator3, GroupLayout.PREFERRED_SIZE, 360, GroupLayout.PREFERRED_SIZE))
 						.addGroup(glcontentPane.createSequentialGroup()
 							.addContainerGap()
 							.addComponent(lblPleaseEnterSettings)))
 					.addContainerGap())
 		);
 		glcontentPane.setVerticalGroup(
 			glcontentPane.createParallelGroup(Alignment.LEADING)
 				.addGroup(glcontentPane.createSequentialGroup()
 					.addComponent(lblPleaseEnterSettings)
 					.addGap(10)
 					.addGroup(glcontentPane.createParallelGroup(Alignment.TRAILING)
 						.addComponent(separator2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 						.addComponent(separator, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 						.addGroup(glcontentPane.createSequentialGroup()
 							.addComponent(separator3, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 							.addGap(18)
 							.addGroup(glcontentPane.createParallelGroup(Alignment.BASELINE)
 								.addComponent(lblPlayer)
 								.addComponent(textField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
 							.addPreferredGap(ComponentPlacement.UNRELATED)
 							.addGroup(glcontentPane.createParallelGroup(Alignment.BASELINE)
 								.addComponent(lblMode)
 								.addComponent(rdbtnHuman)
 								.addComponent(rdbtnComputer))
 							.addPreferredGap(ComponentPlacement.RELATED)
 							.addGroup(glcontentPane.createParallelGroup(Alignment.BASELINE)
 								.addComponent(lblIdentifier)
 								.addComponent(comboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
 							.addGap(40)
 							.addGroup(glcontentPane.createParallelGroup(Alignment.BASELINE)
 								.addComponent(textField1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 								.addComponent(lblPlayer1))
 							.addPreferredGap(ComponentPlacement.UNRELATED)
 							.addGroup(glcontentPane.createParallelGroup(Alignment.BASELINE)
 								.addComponent(radioButton)
 								.addComponent(radioButton1)
 								.addComponent(label))
 							.addGap(7)
 							.addGroup(glcontentPane.createParallelGroup(Alignment.BASELINE)
 								.addComponent(lblIndentifier)
 								.addComponent(comboBox2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
 							.addGap(44)
 							.addComponent(separator1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 							.addPreferredGap(ComponentPlacement.UNRELATED)
 							.addGroup(glcontentPane.createParallelGroup(Alignment.BASELINE)
 								.addComponent(btnOk)
 								.addComponent(btnCancel))
 							.addGap(17)))
 					.addContainerGap())
 		);
 		contentPane.setLayout(glcontentPane);
 	}
 	
 	public Vector<String> fillBox() {
 		final Vector<String> elements = new Vector<String>();
 		for (Entry<Character, Color> e : Constants.COLORS.entrySet()) {
 			elements.add(Constants.getStringForColor(e.getValue()));
 		}
 		return elements;
 	}
 	
 	private void setPlayer() {
 		if(textField.getText().equals("") || textField1.getText().equals("")) {
 			JOptionPane.showMessageDialog(parent, "Validation error: Please fill all fields.");
 		}
 		else {
 			IPlayer p1 = controller.getPlayerOne();
 			p1.setName(textField.getText());
 			if(rdbtnHuman.isSelected()) {
 				p1.setSession(Player.mode.human);
 			}
 			else {
 				p1.setSession(Player.mode.computer);
 			}
 			p1.setSign(Constants.getCharForColorString(comboBox.getSelectedItem().toString()));
 			
 			IPlayer p2 = controller.getPlayerTwo();
 			p2.setName(textField1.getText());
 			if(radioButton.isSelected()) {
 				p2.setSession(Player.mode.human);
 			}
 			else {
 				p2.setSession(Player.mode.computer);	
 			}
 			p2.setSign(Constants.getCharForColorString(comboBox2.getSelectedItem().toString()));
 			
 			controller.newGame();
 			controller.setStatusMessage("Player configuration changed.");
 			
 			parent.setEnabled(true);
 			dispose();
 		}
 	}
 	
 }
