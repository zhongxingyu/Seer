 package de.uni_hamburg.informatik.sep.zuul.gui;
 
 import javax.swing.JComponent;
 import javax.swing.JPanel;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 
 public class KonsolenausgabeUI
 {
 	
 	private JPanel _panel;
 	private JTextArea _textAusgabe;
 	
 	public KonsolenausgabeUI()
 	{
		_textAusgabe = new JTextArea("Text", 10, 50);
 		_textAusgabe.setEditable(false);
 		
 		_panel = new JPanel();
 		
 		_panel.add(_textAusgabe);
 		
 	}
 	
 	public JComponent getPanel()
 	{
 		// TODO Auto-generated method stub
 		return _panel;
 	}
 	
 	public JTextArea getTextArea()
 	{
 		return _textAusgabe;
 	}
 	
 	
 }
 
