 /*
 	Copyright 2013, Subhankar Sahu
 
 	Author(s): Subhankar Sahu                                 
 	Public Repo: https://github.com/kensuvy/Sabdakon                                                
 
 	This file is part of Sabdakon.
 
     	Sabdakon is free software: you can redistribute it and/or modify
    	it under the terms of the GNU General Public License as published by
     	the Free Software Foundation, either version 3 of the License, or
     	(at your option) any later version.
 
     	Sabdakon is distributed in the hope that it will be useful,
     	but WITHOUT ANY WARRANTY; without even the implied warranty of
     	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     	GNU General Public License for more details.
 
     	You should have received a copy of the GNU General Public License
     	along with Sabdakon.  If not, see <http://www.gnu.org/licenses/>.
 	
 */
 
 /*	
  	SabdGUI.java	
 
   	This is the module that handles the GUI. 
 */
 
 import javax.swing.JFrame;
 import javax.swing.JEditorPane;
 import javax.swing.JTextField;
 import javax.swing.JButton;
 import javax.swing.Box;
 import javax.swing.JOptionPane;
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 import javax.swing.JMenuBar;
 import java.awt.GridBagLayout;
 import java.awt.GridBagConstraints;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.awt.Font;
 import java.awt.Insets;
 
 public class SabdGUI extends JFrame
 {
 	private JTextField wordField;
 	private JButton findButton;
 	private JEditorPane resultArea;
 	private String word;
 	private String displayText;
 
 	public SabdGUI()
 	{
 		super("Sabdakon");
 		setLayout(new GridBagLayout());
 		GridBagConstraints c = new GridBagConstraints();
 
 		JMenu fileMenu = new JMenu("File");
 		fileMenu.setMnemonic('F');
 		JMenuItem aboutItem = new JMenuItem("About");
 		aboutItem.setMnemonic('A');
 		aboutItem.addActionListener(
 			new ActionListener()
 			{
 				public void actionPerformed(ActionEvent event)
 				{
					JOptionPane.showMessageDialog(SabdGUI.this, "Sabdakon", "About", JOptionPane.PLAIN_MESSAGE);
 				}
 			}
 		);
 		fileMenu.add(aboutItem);
 		JMenuItem exitItem = new JMenuItem("Exit");
 		exitItem.setMnemonic('E');
 		exitItem.addActionListener(
 			new ActionListener()
 			{
 				public void actionPerformed(ActionEvent event)
 				{
 					System.exit(0);
 				}
 			}
 		);
 		fileMenu.add(exitItem);
 		JMenuBar bar = new JMenuBar();
 		setJMenuBar( bar );
 		bar.add(fileMenu);
 
 		wordField = new JTextField();
 		Font wordBig = new Font ("san serif", Font.BOLD, 34);
 		wordField.setFont(wordBig);
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.gridx = 0;
 		c.gridy = 0;
 		c.weightx = 1.0;
 		c.insets = new Insets(10, 10, 10, 10);
 		add(wordField, c);
 
 		findButton = new JButton("Find");
 		findButton.setFont(wordBig);
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.gridx = 1;
 		c.gridy = 0;
 		c.weightx = 0.25;
 		c.insets = new Insets(10, 10, 10, 10);
 		add(findButton, c);
 
 		Box resultBox = Box.createHorizontalBox();
 		resultArea = new JEditorPane();
 		resultArea.setContentType("text/html");
 		resultArea.setEditable( false );
 		resultBox.add(resultArea);
 		c.fill = GridBagConstraints.BOTH;
 		c.gridx = 0;
 		c.gridy = 1;
 		c.gridwidth = 2;
 		c.weighty = 1.0;
 		c.insets = new Insets(10, 10, 10, 10);
 		add(resultBox, c);
 
 		TextFieldHandler handler = new TextFieldHandler();
 		wordField.addActionListener(handler);
 		findButton.addActionListener(handler);
 
 	}
 
 	private class TextFieldHandler implements ActionListener
 	{
 		public void actionPerformed(ActionEvent event)
 		{
 			if((event.getSource() == wordField)||(event.getSource() == findButton))
 			{
 				word = wordField.getText();
 
 				resultArea.setText("Loading");
 
 				displayText = "<html>";
 
 				SabdWord sWord = new SabdWord();
 				sWord.setWord(word);
 				displayText+=sWord.getFormattedOutput();
 
 				resultArea.setText(displayText);
 			}
 		}
 	}
 }
 
 	
 
