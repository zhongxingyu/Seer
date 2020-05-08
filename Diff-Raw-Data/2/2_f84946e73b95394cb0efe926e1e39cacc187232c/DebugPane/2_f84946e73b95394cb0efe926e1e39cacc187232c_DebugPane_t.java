 /*
  * This software is licensed under the GPLv3 license, included as
  * ./GPLv3-LICENSE.txt in the source distribution.
  *
  * Portions created by Brett Wilson are Copyright 2013 Brett Wilson.
  * All rights reserved.
  */
 
 
 package org.wwscc.protimer;
 
 import java.awt.BorderLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.util.logging.Logger;
 
 import javax.swing.JButton;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 
 import org.wwscc.util.MT;
 import org.wwscc.util.MessageListener;
 import org.wwscc.util.Messenger;
 
 
 public class DebugPane extends JPanel implements ActionListener, MessageListener
 {
 	private static final Logger log = Logger.getLogger(DebugPane.class.getCanonicalName());
 	
 	JTextArea text;
 	JTextField input;
 	JButton enter;
 	OutputStreamWriter file;
 
 	public DebugPane() throws FileNotFoundException
 	{
 		super(new BorderLayout());
 
 		text = new JTextArea();
 		input = new JTextField(40);
 		enter = new JButton("Send");
 		enter.addActionListener(this);
 
 		try {
 			file = new FileWriter("debug.log", true);
 		} catch (IOException ioe) {
 			log.severe("Can't open debug log");
 		}
 
 		JScrollPane sp = new JScrollPane(text);
 		sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 
 		JPanel p = new JPanel();
 		p.add(input);
 		p.add(enter);
 
 		add(p, BorderLayout.NORTH);
 		add(sp, BorderLayout.CENTER);
		
		Messenger.register(MT.SERIAL_GENERIC_DATA, this);
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e)
 	{
 		String s = input.getText();
 		if (!s.equals(""))
 			Messenger.sendEvent(MT.INPUT_TEXT, s);
 		input.setText("");
 	}
 
 	@Override
 	public void event(MT type, Object o)
 	{
 		switch (type)
 		{
 			case SERIAL_GENERIC_DATA:
 				log(new String((byte[])o));
 				break;
 		}
 	}
 	
 	public void log(String s)
 	{
 		try { file.write(s); file.flush(); } catch (Exception e) { log.info("Failed to write log: " + e.getMessage()); }
 		text.append(s);
 		text.setCaretPosition(text.getDocument().getLength());
 	}
 }
 
 
 
