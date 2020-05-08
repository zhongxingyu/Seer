 package client;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 
 import javax.swing.*;
 import java.awt.GridLayout;
 
 import javax.swing.JScrollPane;
 
 import ui.isometric.libraries.IsoCharacterImageLibrary;
 import util.ImagePanel;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 
 public class CharacterSelector extends JDialog {
 
 	/**
 	 * Launch the application.
 	 */
	public static void main(String[] args) {
		try {
			CharacterSelector dialog = new CharacterSelector();
			dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
			dialog.setSize(new Dimension(640,400));
			dialog.setVisible(true);
			System.out.println(dialog.getCharacterName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
 
 	private String name = null;
 	private ButtonGroup radioGroup;
 	private JRadioButton radioButton1;
 	private JRadioButton radioButton2;
 	private JRadioButton radioButton3;
 	private JRadioButton radioButton4;
 	private JRadioButton radioButton5;
 	private JRadioButton radioButton6;
 	private JRadioButton radioButton7;
 	private JRadioButton radioButton8;
 	private String[] charNames;
 	
 
 	/**
 	 * Create the dialog.
 	 */
 	public CharacterSelector() {
 		this.setTitle("Please choose a character...");
 		radioGroup = new ButtonGroup();
 		charNames = IsoCharacterImageLibrary.getAllCharacters().toArray(new String[0]);
 		if (charNames.length < 8) {
 			int i = 0;
 			String[] replacementArray = new String[8];
 			while (i < charNames.length) {
 				replacementArray[i] = charNames[i++];
 			}
 			while (i < 8) {
 				replacementArray[i++] = charNames[0];
 			}
 			charNames = replacementArray;
 			
 		}
 		charNames.toString();
 		
 		
 		setBounds(100, 100, 450, 300);
 		getContentPane().setLayout(new BorderLayout());
 		{
 			JPanel buttonPane = new JPanel();
 			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
 			getContentPane().add(buttonPane, BorderLayout.SOUTH);
 			{
 				JButton okButton = new JButton("OK");
 				okButton.addActionListener(new ActionListener() {
 					public void actionPerformed(ActionEvent arg0) {
 						if (arg0.getActionCommand().equals("OK")) {
 							setCharacterName();
 						}
 					}
 				});
 				okButton.setActionCommand("OK");
 				buttonPane.add(okButton);
 				getRootPane().setDefaultButton(okButton);
 			}
 		}
 		{
 			JScrollPane scrollPane = new JScrollPane();
 			getContentPane().add(scrollPane, BorderLayout.CENTER);
 			{
 				JPanel scrolledPanel = new JPanel();
 				scrollPane.setViewportView(scrolledPanel);
 				scrolledPanel.setLayout(new GridLayout(0, 8, 0, 0));
 				{
 					JPanel panel_1 = new JPanel();
 					scrolledPanel.add(panel_1);
 					panel_1.setLayout(new BorderLayout(0, 0));
 					{
 						radioButton1 = new JRadioButton(charNames[0]);
 						panel_1.add(radioButton1, BorderLayout.SOUTH);
 						radioGroup.add(radioButton1);
 					}
 					{
 						ImagePanel charPanel1 = new ImagePanel(IsoCharacterImageLibrary.imageForCharacterName(charNames[0]));
 						panel_1.add(charPanel1, BorderLayout.CENTER);
 					}
 				}
 				{
 					JPanel panel_2 = new JPanel();
 					scrolledPanel.add(panel_2);
 					panel_2.setLayout(new BorderLayout(0, 0));
 					{
 						radioButton2 = new JRadioButton(charNames[1]);
 						panel_2.add(radioButton2, BorderLayout.SOUTH);
 						radioGroup.add(radioButton2);
 					}
 					{
 						ImagePanel charPanel2 = new ImagePanel(IsoCharacterImageLibrary.imageForCharacterName(charNames[1]));
 						panel_2.add(charPanel2, BorderLayout.CENTER);
 					}
 				}
 				{
 					JPanel panel_3 = new JPanel();
 					scrolledPanel.add(panel_3);
 					panel_3.setLayout(new BorderLayout(0, 0));
 					{
 						radioButton3 = new JRadioButton(charNames[2]);
 						panel_3.add(radioButton3, BorderLayout.SOUTH);
 						radioGroup.add(radioButton3);
 					}
 					{
 						ImagePanel charPanel3 = new ImagePanel(IsoCharacterImageLibrary.imageForCharacterName(charNames[2]));
 						panel_3.add(charPanel3, BorderLayout.CENTER);
 					}
 				}
 				{
 					JPanel panel_4 = new JPanel();
 					scrolledPanel.add(panel_4);
 					panel_4.setLayout(new BorderLayout(0, 0));
 					{
 						radioButton4 = new JRadioButton(charNames[3]);
 						panel_4.add(radioButton4, BorderLayout.SOUTH);
 						radioGroup.add(radioButton4);
 					}
 					{
 						ImagePanel charPanel4 = new ImagePanel(IsoCharacterImageLibrary.imageForCharacterName(charNames[3]));
 						panel_4.add(charPanel4, BorderLayout.CENTER);
 					}
 				}
 				{
 					JPanel panel_5 = new JPanel();
 					scrolledPanel.add(panel_5);
 					panel_5.setLayout(new BorderLayout(0, 0));
 					{
 						radioButton5 = new JRadioButton(charNames[4]);
 						panel_5.add(radioButton5, BorderLayout.SOUTH);
 						radioGroup.add(radioButton5);
 					}
 					{
 						ImagePanel charPanel5 = new ImagePanel(IsoCharacterImageLibrary.imageForCharacterName(charNames[4]));
 						panel_5.add(charPanel5, BorderLayout.CENTER);
 					}
 				}
 				{
 					JPanel panel_6 = new JPanel();
 					scrolledPanel.add(panel_6);
 					panel_6.setLayout(new BorderLayout(0, 0));
 					{
 						radioButton6 = new JRadioButton(charNames[5]);
 						panel_6.add(radioButton6, BorderLayout.SOUTH);
 						radioGroup.add(radioButton6);
 					}
 					{
 						ImagePanel charPanel6 = new ImagePanel(IsoCharacterImageLibrary.imageForCharacterName(charNames[5]));
 						panel_6.add(charPanel6, BorderLayout.CENTER);
 					}
 				}
 				{
 					JPanel panel_7 = new JPanel();
 					scrolledPanel.add(panel_7);
 					panel_7.setLayout(new BorderLayout(0, 0));
 					{
 						radioButton7 = new JRadioButton(charNames[6]);
 						panel_7.add(radioButton7, BorderLayout.SOUTH);
 						radioGroup.add(radioButton7);
 					}
 					{
 						ImagePanel charPanel7 = new ImagePanel(IsoCharacterImageLibrary.imageForCharacterName(charNames[6]));
 						panel_7.add(charPanel7, BorderLayout.CENTER);
 					}
 				}
 				{
 					JPanel panel_8 = new JPanel();
 					scrolledPanel.add(panel_8);
 					panel_8.setLayout(new BorderLayout(0, 0));
 					{
 						radioButton8 = new JRadioButton(charNames[7]);
 						panel_8.add(radioButton8, BorderLayout.SOUTH);
 						radioGroup.add(radioButton8);
 					}
 					{
 						ImagePanel charPanel8 = new ImagePanel(IsoCharacterImageLibrary.imageForCharacterName(charNames[7]));
 						panel_8.add(charPanel8, BorderLayout.CENTER);
 					}
 				}
 			}
 		}
 	}
 	
 	public String getCharacterName() {
 		while (name == null) {
 			try {
 				Thread.sleep(100);
 			} catch (InterruptedException e) {}
 		}
 		this.dispose();
 		return name;
 		
 	} 
 	
 	public void setCharacterName() {
 		if (radioButton1.isSelected()) name = charNames[0];
 		if (radioButton2.isSelected()) name = charNames[1];
 		if (radioButton3.isSelected()) name = charNames[2];
 		if (radioButton4.isSelected()) name = charNames[3];
 		if (radioButton5.isSelected()) name = charNames[4];
 		if (radioButton6.isSelected()) name = charNames[5];
 		if (radioButton7.isSelected()) name = charNames[6];
 		if (radioButton8.isSelected()) name = charNames[7];
 	}
 
 }
