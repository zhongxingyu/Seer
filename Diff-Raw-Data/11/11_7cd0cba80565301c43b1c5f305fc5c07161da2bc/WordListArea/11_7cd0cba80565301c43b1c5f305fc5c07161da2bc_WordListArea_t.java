 package View.Components;
 
 import Control.*;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 
 public class WordListArea extends JPanel
 {
 	JButton addWord;
 	JButton deleteWord;
 	JList<String> wordList;
 	JTextField inputText;
 	public WordListArea(Controller control)
 	{
 		Dimension size = getPreferredSize();
 		size.width = 200;
 
 		
 		
 		addWord = new JButton("Add Word");
 		deleteWord = new JButton("Delete Word");
 		inputText = new JTextField(8);
		//wordList = new JList<String> (words);
 		setPreferredSize(size);
 		//this.setBackground(Color.red);
 
 		addWord.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				// TODO Auto-generated method stub
 				System.out.println(inputText.getText ());
 				
 			}
 		});
 		deleteWord.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				// TODO Auto-generated method stub
 				System.out.println("delete");
 			}
 		});
 		
 		add(inputText);
 		add(addWord);
 
 		add(deleteWord);
 	}
 }
