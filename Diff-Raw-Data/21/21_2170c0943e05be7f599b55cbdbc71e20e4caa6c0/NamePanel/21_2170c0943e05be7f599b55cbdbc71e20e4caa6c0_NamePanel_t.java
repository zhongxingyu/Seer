 import javax.swing.*;
 
 import java.awt.*;
 import java.awt.event.*;
 
public class NamePanel extends JPanel implements ActionListener, KeyListener {
 
 	WordPathClient wpc;
 	JPanel appPanel = new JPanel();
 	JPanel nameEntryPanel = new JPanel();
	JLabel statusLabel = new JLabel("");
 	JTextField nameEntryField;
 	JButton nameAcceptButton;
 	
 	GridBagConstraints gbc = new GridBagConstraints();
 	
 	public NamePanel(WordPathClient parent) {
 		wpc = parent;
 		
 		nameAcceptButton = new JButton("Accept");
 		nameEntryField = new JTextField(20);
 		
 		setLayout(new GridBagLayout());
 		gbc.insets = new Insets(5,5,5,5);
 		gbc.gridx = 3;
 		gbc.gridy = 3;
 		add(appPanel, gbc);
		appPanel.add(new JLabel("Enter Name: "));
 		appPanel.add(nameEntryPanel);
 		
 		nameEntryPanel.setLayout(new BoxLayout(nameEntryPanel, BoxLayout.X_AXIS));
 		nameEntryPanel.add(nameEntryField);
 		nameEntryPanel.add(nameAcceptButton);
		
		gbc.gridy = 4;
		appPanel.add(statusLabel, gbc);
 		nameAcceptButton.addActionListener(this);
		nameEntryField.addKeyListener(this);
	}
	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_ENTER){
			nameAcceptButton.doClick();
		}
 	}
	public void keyTyped(KeyEvent e) {}
	public void keyReleased(KeyEvent e) {}
 	public void actionPerformed(ActionEvent ae) {
 		if(ae.getSource() == nameAcceptButton) {
 			wpc.checkNames(nameEntryField.getText());
 			wpc.setPlayerName(nameEntryField.getText());
 		}
 	}
 }
