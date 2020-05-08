 package de.uni_hamburg.informatik.sep.zuul.gui;
 
import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JComponent;
 import javax.swing.JTextField;
 
 public class KonsoleneingabeWerkzeug
 {
 
 	private KonsoleneingabeUI _konsoleneingabeUI;
 
 	public KonsoleneingabeWerkzeug()
 	{
 		_konsoleneingabeUI = new KonsoleneingabeUI();
		
		setAreaListener();
 	}
 	
 	public JComponent getUI()
 	{
 		return _konsoleneingabeUI.getPanel();
 	}
 	
 	public void addNeueZeileListener(ActionListener arg0)
 	{
 		_konsoleneingabeUI.getEnterButton().addActionListener(arg0);
 	}
 
 	public String leseEingabeUndLeere()
 	{
 		JTextField field = _konsoleneingabeUI.getTextField();
 		String str = field.getText();
 		field.setText("");
 		return str;
 	}
	
	private void setAreaListener()
	{
		_konsoleneingabeUI.getTextField().addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				_konsoleneingabeUI.getEnterButton().doClick();
				
			}
		});
	}
 
 
 }
