 /*LICENSING
 
 Copyright (c) 2011, Matthew Wolfe, Andy Gospodnetich
 
 Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
 
 import java.awt.GridLayout;
 import java.awt.BorderLayout;
 
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 
 import javax.swing.JLabel;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JButton;
 import javax.swing.JTextField;
 import javax.swing.JPasswordField;
 import javax.swing.JOptionPane;
 
 public class Gui extends JFrame
 {
 	private BorderLayout layout;
 	
 	private JPanel buttonJPanel;
 	private JPanel textJPanel;
 	private JPanel textLabelJPanel;
 	private JPanel singletextJPanel;
 	
 	private JButton buttons[];
 	private JLabel fieldLabels[];
 	private JTextField fields[];
 	private JPasswordField password;
 	
 	private JLabel label;
 	public JTextField price;
 	
 	runPeriodicTask rpt;
 	Email email;
 	
 	public Gui()
 	{
 		//Create the Frame
 		super( "Stocker" );
 		layout = new BorderLayout(5, 5);
 		setLayout(layout);
 		
 		//Add the buttons
 		buttons = new JButton[6];
 		buttonJPanel = new JPanel();
 		buttonJPanel.setLayout(new GridLayout(2, 4));
 		
 		buttons[0] = new JButton("Start");
 		buttonJPanel.add(buttons[0]);
 		buttons[0].addActionListener(
 			new ActionListener()
 			{
 				public void actionPerformed(ActionEvent event)
 				{
 					System.out.println("\nStart clicked.");										
 									
 					if (guiErrorCount() == 0) {	// check for errors and proceed if there are none 
 						printGUIinfo();
 						buttons[0].setText("Add/Update");
 
 						if (getSize().height > 160) {removeGUIbuttons();}
 							
 						rpt = new runPeriodicTask(Integer.parseInt(fields[1].getText()), 
 							fields[0].getText().toUpperCase(),
 							Double.parseDouble(fields[2].getText()),
 							Double.parseDouble(fields[3].getText()),
 							fields[4].getText(), password.getText());
 					}	
 				} 
 			}
 		);	
 		
 		buttons[1] = new JButton("Stop");
 		buttonJPanel.add(buttons[1]);
 		buttons[1].addActionListener(
 			new ActionListener()
 			{
 				public void actionPerformed(ActionEvent event)
 				{
 					System.out.println("Stop clicked.\nQuitting...");					
 					rpt.quit=true;
 					System.exit(0);		// Quit
 				}
 			}
 		);
 		
 				
 		buttons[2] = new JButton("Remove");
 		buttonJPanel.add(buttons[2]);
 		buttons[2].addActionListener(
 			new ActionListener()
 			{
 				public void actionPerformed(ActionEvent event)
 				{
 					System.out.println("\nRemove clicked.");
 					
 					// disable ticker symbol
 					rpt = new runPeriodicTask(fields[0].getText().toString().toUpperCase(),2);	
 				}
 			}
 		);
 				
 		buttons[3] = new JButton("Email");
 		buttonJPanel.add(buttons[3]);
 		buttons[3].addActionListener(
 			new ActionListener()
 			{
 				public void actionPerformed(ActionEvent event)
 				{
 					System.out.println("\nEmail clicked.");
 
 					printGUIinfo();
 
 					String tickerSymbol = fields[0].getText().toUpperCase();
 					String lastTrade = runPeriodicTask.fetcher.getLastTrade(fields[0].getText());
 					
 					String to = fields[4].getText();
 					String pw = password.getText();
 
 					String subject = tickerSymbol + " " + lastTrade;
 						
 					//setTickerandPrice is called by runPeriodicTask to update the price field
 					String message = 
 						"Ticker Symbol: " + tickerSymbol +
 						"\nLast trade:    " + lastTrade +
 						"\n\n*** Automatically generated message from Stocker. Stocker Email Button Pushed ***";
 
 					email = new Email(to, pw, subject, message);		// send email									
 				}
 			}
 		);
 		
 		buttons[4] = new JButton("Get price");
 		buttonJPanel.add(buttons[4]);
 		buttons[4].addActionListener(
 			new ActionListener()
 			{
 				public void actionPerformed(ActionEvent event)
 				{
 					System.out.println("Get Price clicked.");
 					
 					// get current price -- this fetches the last trade value and sets the Current Price field on the GUI
 					setTickerandPrice(fields[0].getText().toUpperCase(), runPeriodicTask.fetcher.getLastTrade(fields[0].getText()));
 				}
 			}
 		);
 
 		buttons[5] = new JButton("Draw Chart");
 		buttonJPanel.add(buttons[5]);
 		buttons[5].addActionListener(
 			new ActionListener()
 			{
 				public void actionPerformed(ActionEvent event)
 				{
 					System.out.println("Draw Chart clicked.");
 
 					// draw chart (given ticker symbol)
 					rpt = new runPeriodicTask(fields[0].getText().toUpperCase(),5);											
 				}
 			}
 		);
 				
 		add( buttonJPanel, BorderLayout.SOUTH);
 		
 		//Add the button field text
 		fieldLabels = new JLabel[6];
 		textLabelJPanel = new JPanel();
 		textLabelJPanel.setLayout(new GridLayout(fieldLabels.length,1));
 		
 		fieldLabels[0] = new JLabel("Stock Name");
 		textLabelJPanel.add(fieldLabels[0]);
 		
 		fieldLabels[1] = new JLabel("Refresh Rate");
 		textLabelJPanel.add(fieldLabels[1]);
 		
 		fieldLabels[2] = new JLabel("High Value");
 		textLabelJPanel.add(fieldLabels[2]);
 		
 		fieldLabels[3] = new JLabel("Low Value");
 		textLabelJPanel.add(fieldLabels[3]);
 		
 		fieldLabels[4] = new JLabel("Email Address", 10);
 		textLabelJPanel.add(fieldLabels[4]);
 		
 		fieldLabels[5] = new JLabel("Email Password", 10);
 		textLabelJPanel.add(fieldLabels[5]);
 		
 		add( textLabelJPanel, BorderLayout.WEST);
 				
 		//Add the Fields
 		fields = new JTextField[6];
 		textJPanel = new JPanel();
 		textJPanel.setLayout(new GridLayout(fields.length, 1));
 		
 		fields[0] = new JTextField();
 		textJPanel.add(fields[0]);
 		
 		fields[1] = new JTextField();
 		textJPanel.add(fields[1]);
 
 		fields[2] = new JTextField();
 		textJPanel.add(fields[2]);
 		
 		fields[3] = new JTextField();
 		textJPanel.add(fields[3]);
 		
 		fields[4] = new JTextField();
 		textJPanel.add(fields[4]);
 		
 		//fields[5] = new JTextField();
 		//textJPanel.add(fields[5]);
 		password = new JPasswordField();
 		textJPanel.add(password);
 		
 		add( textJPanel, BorderLayout.CENTER);
 		
 		//Add the extras
 		singletextJPanel = new JPanel();
 		singletextJPanel.setLayout(new GridLayout(5,1));
 		
 		label = new JLabel("Current Price: ");
 		label.setToolTipText("This is the Current Price.");
 		singletextJPanel.add( label );
 		
 		price = new JTextField(10);
 		price.setEditable(false);
 		singletextJPanel.add( price );
 		add(singletextJPanel, BorderLayout.EAST);
 		
 		
 		//register event handlers
 		FieldHandler handler = new FieldHandler();
 		fields[0].addActionListener( handler );
 		fields[1].addActionListener( handler );
 		fields[2].addActionListener( handler );
 		fields[3].addActionListener( handler );
 		fields[4].addActionListener( handler );
 	}
 
 	private void printGUIinfo() {
 		System.out.println("ticker symbol = " + fields[0].getText() + "\n" +							
 							"refresh rate = " + fields[1].getText() + "\n" +
 							"high = "         + fields[2].getText() + "\n" +
 							"low = "          + fields[3].getText() + "\n" +
 							"email = "        + fields[4].getText() + "\n" +
 						    "password = "     + "*********");
 	}
 
 	private int guiErrorCount() {
 		int errorCount=0;
 		
 		if (fields[0].getText().isEmpty()) {
 			System.out.println("Error: Ticker symbol is empty. Enter a ticker symbol.");
 			errorCount++;
 			JOptionPane.showMessageDialog(null,
 				    "Ticker symbol is empty. Enter a ticker symbol.",
 				    "Ticker Symbol",
 				    JOptionPane.ERROR_MESSAGE);

 		}
 		
 		try {
 			Integer.parseInt(fields[1].getText());
 			
 		} catch (NumberFormatException nfe) {
 			System.out.println("Error: Refresh Rate requires an integer.");
 			errorCount++;
 			JOptionPane.showMessageDialog(null,
 				    "Refresh rate requires an integer.",
 				    "Refresh rate",
 				    JOptionPane.ERROR_MESSAGE);
 
 		}
 		try {
 			Double.parseDouble(fields[2].getText());
 			
 		} catch (NumberFormatException nfe) {
 			System.out.println("Error: High Threshold requires a double.");
 			errorCount++;
 			JOptionPane.showMessageDialog(null,
 				    "High Threshold requires a double.",
 				    "High Threshold",
 				    JOptionPane.ERROR_MESSAGE);
 		}
 		try {
 			Double.parseDouble(fields[3].getText());
 			
 		} catch (NumberFormatException nfe) {
 			System.out.println("Error: Low Threshold requires a double.");
 			errorCount++;
 			JOptionPane.showMessageDialog(null,
 				    "Low Threshold requires a double.",
 				    "Low Threshold",
 				    JOptionPane.ERROR_MESSAGE);
 		}
 		
 		// fields[4].getText(); 	  // email
 		// pw = fields[5].getText();  // pwd
 		
 		return errorCount;
 	}
 
 	public void setTickerandPrice(String tickerSymb, String curValue) {
 		price.setText(tickerSymb + " " + curValue);
 	}
 
 	
 	private void removeGUIbuttons() {
 
 		// remove "refresh" label
 		textLabelJPanel.remove(fieldLabels[1]);		
 		
 		// remove "refresh" field
 		textJPanel.remove(fields[1]);
 		
 		// remove "email" label
 		textLabelJPanel.remove(fieldLabels[4]);		
 		
 		// remove "email" field
 		textJPanel.remove(fields[4]);		
 
 		// remove "password" label
 		textLabelJPanel.remove(fieldLabels[5]);		
 		
 		// remove "password" field
 		textJPanel.remove(password);		
 				
 		// set layout to 1 less
 		textJPanel.setLayout(new GridLayout(fields.length-3, 1));
 		textLabelJPanel.setLayout(new GridLayout(fieldLabels.length-3,1));
 		
 		// resize after removing 2 fields & 2 buttons
 		// (350, 210) to (355, 160)
 		int x=350, y;
 		for (int i=1; i<=50; i++) {
 			if (i%10 == 0) {x++;}
 			y=210-i;
 			setSize(x, y); // (x,y)			// ends at (355, 160)
 		}
 
 	}
 	
 	private class FieldHandler implements ActionListener
 	{
 		public void actionPerformed(ActionEvent event)
 		{
 			String string = "";
 			
 			if(event.getSource() == fields[0])
 			string = String.format("fields[0]: %s", 
 				event.getActionCommand());
 			
 			else if(event.getSource() == fields[1])
 			string = String.format("fields[1]: %s", 
 				event.getActionCommand());
 			
 			else if(event.getSource() == fields[2])
 			string = String.format("fields[2]: %s", 
 				event.getActionCommand());
 			
 			else if(event.getSource() == fields[3])
 			string = String.format("fields[0]: %s", 
 				event.getActionCommand());
 			
 			else if(event.getSource() == fields[4])
 			string = String.format("fields[4]: %s", 
 				event.getActionCommand());
 			
 			JOptionPane.showMessageDialog(null, string);
 		}
 	}
 }
