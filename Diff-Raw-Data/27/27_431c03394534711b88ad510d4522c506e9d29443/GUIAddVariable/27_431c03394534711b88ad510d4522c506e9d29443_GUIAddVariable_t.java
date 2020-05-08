 package gui;
 import internalformatting.Variable;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.ScrollPaneConstants;
 
 import storage.Saver;
 import storage.VariableDatabase;
 
 /**
  * Another GUI to learn a bit about gui making,
  * and to make a slightly different way to format
  * what we want in our GUI
  * 
  * @author May Camp
  * @author Michelle Len
  * @author Jonathan Tan
  */
 public class GUIAddVariable extends JPanel {
 
 	private static final long serialVersionUID = 6187284211221392126L;
 
 	private JPanel middlePanel;
 
 	private JPanel namePanel;
 	private JPanel unitPanel;
 	private JPanel infoPanel;
 	private JPanel tagPanel;
 	private JPanel addVariableButtonPanel;
 
	// INCORPORATED TWO-LINED LABELS - MICHELLE
	private JLabel nameLabel   = new JLabel("<HTML>Name <BR>(like 'x'): </HTML>");
 	private JLabel unitLabel   = new JLabel("Units: ");
 	private JLabel infoLabel   = new JLabel("Info: ");
	private JLabel tagLabel    = new JLabel("<HTML>Tags <BR>(separated by ','): </HTML>");
 
	private JTextField nameTextField   = new JTextField(52);
	private JTextField unitTextField   = new JTextField(51);
	private JTextArea infoTextArea     = new JTextArea(13, 51);
	private JTextArea tagTextArea      = new JTextArea(4, 51);
 
 	private JScrollPane infoScrollbar;
 	private JScrollPane tagScrollbar;
 
 	private JButton addVariableButton = new JButton("Add Variable");
 
 	private JComboBox<String> unitComboBox;
 	
 	public GUIAddVariable() {
 
 		setSize(720,480);
 		
 		// Setting flow layout so can align center
 		FlowLayout flow = new FlowLayout(FlowLayout.LEFT, 5, 5);
 		
 		// unitComboBox is the drop-down menu for Units
 		unitComboBox = new JComboBox<String>();
 		for(int i=0;i<GUIMain.UNITS.getSize();i++){
 			unitComboBox.addItem(GUIMain.UNITS.get(i).getName());
 		}
 
 		middlePanel = new JPanel();
 		middlePanel.setLayout(new BoxLayout(middlePanel, BoxLayout.Y_AXIS));
 		middlePanel.setSize(720, 480);
 		add(BorderLayout.CENTER, middlePanel);
 
 		// Scroll-bar for the Info JTextArea
 		infoScrollbar = new JScrollPane(infoTextArea);
 		infoScrollbar.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
 		infoScrollbar.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
 
 		// Scroll-bar for the Info JTextArea
 		tagScrollbar = new JScrollPane(tagTextArea);
 		tagScrollbar.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
 		tagScrollbar.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
 
 		namePanel   = new JPanel();
 		unitPanel   = new JPanel();
 		infoPanel   = new JPanel();
 		tagPanel    = new JPanel();
 
 		addVariableButtonPanel = new JPanel();
 
 		namePanel.setLayout(flow);
 		unitPanel.setLayout(flow);
 		infoPanel.setLayout(flow);
 		tagPanel.setLayout (flow);
 
 		addVariableButtonPanel.setLayout(new BoxLayout(addVariableButtonPanel, BoxLayout.X_AXIS));
 
 		//wrap words and lines and make sure you can't edit it
 		infoTextArea.setLineWrap(true);
 		infoTextArea.setWrapStyleWord(true);
 		//wrap words and lines and make sure you can't edit it
 		tagTextArea.setLineWrap(true);
 		tagTextArea.setWrapStyleWord(true);
 
 		namePanel.add(nameLabel);
		namePanel.add(Box.createRigidArea(new Dimension(58,0)));
 		namePanel.add(nameTextField);
 
 		unitPanel.add(unitLabel);
		unitPanel.add(Box.createRigidArea(new Dimension(69,0)));
 		unitPanel.add(unitComboBox);
 
 		infoPanel.add(infoLabel);
		infoPanel.add(Box.createRigidArea(new Dimension(77,0)));
 		infoPanel.add(infoScrollbar);
 
 		tagPanel.add(tagLabel);
		tagPanel.add(Box.createRigidArea(new Dimension(7,0)));
 		tagPanel.add(tagScrollbar);
 
 		addVariableButtonPanel.add(addVariableButton);
 
 		middlePanel.add(namePanel);
 		middlePanel.add(unitPanel);
 		middlePanel.add(infoPanel);
 		middlePanel.add(tagPanel);
 		middlePanel.add(addVariableButtonPanel);
 
 		setOpaque(false);
 		middlePanel.setOpaque(false);//THIS THING makes it not opaque
 
 		namePanel.setOpaque(false);
 		unitPanel.setOpaque(false);
 		infoPanel.setOpaque(false);
 		tagPanel.setOpaque(false);
 
 		addVariableButton.addActionListener(new addVariableButtonListener());
 		
 		addVariableButtonPanel.setOpaque(false);
 
 	}
 
 	//Button Listener Class
 	class addVariableButtonListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			String variableName = nameTextField.getText();
 			String variableInfo = infoTextArea.getText();
 			String[] tagsTemp = tagTextArea.getText().split(",");
 
 			Variable newVariable = new Variable(variableName);
 			newVariable.setInfo(variableInfo);
 			newVariable.setUnit(GUIMain.UNITS.get(unitComboBox.getSelectedIndex()));
 			for(int i=0;i<tagsTemp.length;i++){
 				newVariable.addTag(tagsTemp[i].toLowerCase());
 			}
 			((VariableDatabase)GUIMain.VARIABLES).addVariable(newVariable);
 			Saver.saveVars(GUIMain.VARIABLES);
 
 			nameTextField.setText("");
 			unitTextField.setText("");
 			infoTextArea.setText("");
 			tagTextArea.setText("");
 		}
 	}
 	
 	class createUnitButtonListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			middlePanel.removeAll();
 			GUIMain.updateUI();
 			middlePanel.add(new GUIAddUnit());
 		}
 	}
 
 }
