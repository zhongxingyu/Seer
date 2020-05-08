 /**
 *GUMA a simple math game for elementary school students
 *	Copyright (C) 2013-2014  Dimitrios Desyllas (pc_magas)
 *
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *Contact with me by main at thes address: pc_magas@yahoo.gr
 */
 package guma.gui;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.event.*;
 import guma.core.Game;
 import guma.gui.SwingWebLoader;
 
 public class WebGui extends JPanel implements ActionListener
 {
 	
 	/**
 	*Shows the Label for seting the size of downloaded file
 	*/
 	private JLabel sizeLabel= new JLabel("Μέγεθος Αρχείου σε Kilobytes");
 	
 	/**
 	*Shows the Label for seting the url of Downloaded file
 	*/
 	private JLabel urlLabel= new JLabel("Url παιχνιδιού στο διαδίκτυο");
 	
 	/**
 	*Input of Url
 	*/
 	private JTextField urlInput=new JTextField();
 	
 	/**
 	*Input of size
 	*/
 	private JTextField sizeInput=new JTextField("0");
 	
 	/**
 	*Menu for Paste
 	*/
 	private JPopupMenu pastemenu=new JPopupMenu();
 	
 	/**
 	*Showing the paste MenuItem
 	*/
 	private JMenuItem paste=new JMenuItem("Επικόληση");
 	
 	/**
 	*Showing the cut MenuItem
 	*/
 	private JMenuItem cut=new JMenuItem("Αποκοπή");
 	
 	/**
 	*Showing the cut MenuItem
 	*/
 	private JMenuItem cοpy=new JMenuItem("Αντιγραφή");
 	
 	/**
 	*Consructor Method Initialises a new WebGui
 	*/
 	public WebGui()
 	{
 		super();
 		
 		/*Setting right Click menu*/
 		cut.addActionListener(this);
 		cοpy.addActionListener(this);
 		paste.addActionListener(this);
 		
 		pastemenu.add(cut);
 		pastemenu.add(cοpy);
 		pastemenu.add(paste);
 		
 		urlInput.addMouseListener(new ShowMenu());
 		sizeInput.addMouseListener(new ShowMenu());
 		
 		/*Setting Layout*/
 		GridBagLayout gridbag= new GridBagLayout();
 		GridBagConstraints c=new GridBagConstraints();//This  variable helps us out in making the gui 
 		c.fill=GridBagConstraints.HORIZONTAL;
 		c.weightx=1.0;
 		c.weighty=1.0;
 		setLayout(gridbag);
 		
 		/*Adding the Url Label*/
 		c.gridwidth = GridBagConstraints.REMAINDER;
 		addComponent(urlLabel,gridbag,c);
 		addComponent(urlInput,gridbag,c);
 		
 		c.gridwidth = GridBagConstraints.RELATIVE;
 		addComponent(sizeLabel,gridbag,c);
 		
 		c.gridwidth = GridBagConstraints.REMAINDER;
 		addComponent(sizeInput,gridbag,c);
 	}
 	
 	/**
 	*Inner Class that allows you to show the copy paste popup menu
 	*/
 	private class ShowMenu extends MouseAdapter
 	{
 		public void mousePressed(MouseEvent e)
 		{
 			switch(e.getModifiers())
 			{
 				case InputEvent.BUTTON3_MASK:
 					pastemenu.show(e.getComponent(), e.getX(), e.getY());
 				break;
 				
 				case InputEvent.BUTTON2_MASK:
 					JTextField jte = (JTextField)pastemenu.getInvoker();
 					jte.paste();
 			}
 		}
 	}
 	
 	/**
 	*Method that allows you to Add component on a GridBagLayout Panel or Frame
 	*/
 	public void addComponent(JComponent component,GridBagLayout gridbag,GridBagConstraints c)
 	{
 		gridbag.setConstraints(component,c);
 		add(component);
 	}
 	
 	@Override
 	public void actionPerformed(ActionEvent e)
 	{
 		Object source=e.getSource();
 		
 		if(source==paste)
 		{
 			JTextField jte = (JTextField)pastemenu.getInvoker();
 			jte.paste();
 		}
 		else if(source==cοpy)
 		{
 			JTextField jte = (JTextField)pastemenu.getInvoker();
 			jte.copy();
 		}
 		else if(source==cut)
 		{
 			JTextField jte = (JTextField)pastemenu.getInvoker();
 			jte.cut();
 		}
 	}
 	
 	/**
 	*Returns a Game from the Web
 	*/
 	public Game getGame()
 	{
 	
 		String[] options={"OK","Ακύρωση"};
 		Game gameToCreate=null;
 		
 			
 		int returnVal=JOptionPane.showOptionDialog(null,this,"Κατέβασμα παιχνιδιού από το web",
 							JOptionPane.OK_CANCEL_OPTION,
 							JOptionPane.QUESTION_MESSAGE,
 							null,(Object[])options,
 							(Object)options[1]);//Shows the Dialog
 		
 		if(returnVal==JOptionPane.OK_OPTION)
 		{
 			String url=urlInput.getText();
 			long sizev=0;
 			try
 			{
 				String sizes=sizeInput.getText();
 				if(!sizes.equals(""))
 				{
 					sizev=Long.parseLong(sizes)*1024;
 				}
			
 				SwingWebLoader w=new SwingWebLoader(url,sizev);
				
				while(w.getStatus().equalsIgnoreCase("Downloading")){}//Wait to finish the download
				return w.getGame();
 			}
 			catch(java.io.IOException x)
 			{
 				x.printStackTrace();
 				JOptionPane.showMessageDialog(null,"Σφάλμα","Η διεύθυνση δικτύου δεν αντιστοιχεί σε παιχνίδι",JOptionPane.ERROR_MESSAGE);
 				return null;
 			}
 			catch(Exception e)
 			{
 				JOptionPane.showMessageDialog(null,"Σφάλμα","Δεν δώσατε αριθμιτική τιμή στο μέγεθος",JOptionPane.ERROR_MESSAGE);
 				return this.getGame();
 			}
 		}
 		else
 		{
 			 gameToCreate=null;
 		}

 		return gameToCreate;
 	}
 }
