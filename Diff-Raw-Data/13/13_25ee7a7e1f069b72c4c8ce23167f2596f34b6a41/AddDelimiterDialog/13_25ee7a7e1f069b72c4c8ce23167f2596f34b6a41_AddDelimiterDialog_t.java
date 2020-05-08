 /*
  File: EditCloudNameDialog.java
 
  Copyright 2010 - The Cytoscape Consortium (www.cytoscape.org)
  
  Code written by: Layla Oesper
  Authors: Layla Oesper, Ruth Isserlin, Daniele Merico
  
  This library is free software: you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.
  
  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
  
  You should have received a copy of the GNU Lesser General Public License
  along with this project.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package cytoscape.csplugins.wordcloud;
 
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.Insets;
 
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 /**
  * This class handles all the dialog box associated with defining your own delimiter.
  * @author Layla Oesper
  * @version 1.0
  */
 
 public class AddDelimiterDialog extends JDialog implements ActionListener
 {
 
 	private static final long serialVersionUID = -2295450101951048637L;
 
 	//VARIABLES
 	private String newName = "";
 
 	private JButton btnCancel;
 	private JButton btnOK;
 	private JLabel jLabel1;
 	private JPanel jPanel1;
 	private JTextField tfDelimiter;
 
 	
 	//CONSTRUCTOR
 	/**
 	 * Creates a new AddDelimiterDialog object
 	 * @param parent - parent component to display this in
 	 * @param modal - whether this is a modal display
 	 */
 	public AddDelimiterDialog(Component parent, boolean modal)
 	{
 		super((JFrame) parent, modal);
 		initComponents();
 		
 		setSize(new java.awt.Dimension(300, 170));
 	}
 	
 	//METHODS
 	/**
 	 * Initializes all components for this dialog.
 	 */
 	private void initComponents() {
 		GridBagConstraints gridBagConstraints;
 
 		jLabel1 = new JLabel();
 		tfDelimiter = new JTextField();
 		jPanel1 = new JPanel();
 		btnOK = new JButton();
 		btnCancel = new JButton();
 
 		btnOK.addActionListener(this);
 		btnCancel.addActionListener(this);
 
 		getContentPane().setLayout(new GridBagLayout());
 
 		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
 		setTitle("Add Word Tokenization Delimiter");
 		jLabel1.setText("Please enter the new delimiter:");
 		gridBagConstraints = new GridBagConstraints();
 		gridBagConstraints.anchor = GridBagConstraints.WEST;
 		gridBagConstraints.insets = new Insets(15, 10, 0, 0);
 		getContentPane().add(jLabel1, gridBagConstraints);
 
 		gridBagConstraints = new GridBagConstraints();
 		gridBagConstraints.gridy = 1;
 		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
 		gridBagConstraints.weightx = 1.0;
 		gridBagConstraints.insets = new Insets(10, 10, 10, 10);
 		getContentPane().add(tfDelimiter, gridBagConstraints);
 
 		btnOK.setText("OK");
		//btnOK.setPreferredSize(new Dimension(65, 23));
		//btnCancel.setPreferredSize(new Dimension(65, 23));
 
 		jPanel1.add(btnOK);
 
 		btnCancel.setText("Cancel");
 		jPanel1.add(btnCancel);
 
 		gridBagConstraints = new GridBagConstraints();
 		gridBagConstraints.gridy = 2;
 		gridBagConstraints.insets = new Insets(10, 0, 10, 0);
 		getContentPane().add(jPanel1, gridBagConstraints);
 
 		pack();
 	}
 	
 	/**
 	 * Called when an action is performed on this dialog.
 	 * @param ActionEvent
 	 */
 	public void actionPerformed(ActionEvent e)
 	{
 		Object _actionObject = e.getSource();
 		
 		// handle button events
 		if(_actionObject instanceof JButton)
 		{
 			JButton _btn = (JButton) _actionObject;
 			
 			if (_btn == btnOK)
 			{
 				newName = tfDelimiter.getText();
 				this.dispose();
 			} else if (_btn == btnCancel)
 			{
 				this.dispose();
 			}
 		}
 	}
 	
 	/**
 	 * Returns the new Cloud Name.
 	 */
 	public String getNewDelimiter()
 	{
 		return newName;
 	}
 }
 
