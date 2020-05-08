 // PatroDyne: Patron Supported Dynamic Executables
 // Released under LGPL license. See terms at http://www.gnu.org.
 package org.patrodyne.etl.transformio.tui;
 
 import charva.awt.BorderLayout;
 import charva.awt.FlowLayout;
 import charva.awt.Frame;
 import charva.awt.event.ActionEvent;
 import charva.awt.event.ActionListener;
 
 import charvax.swing.JButton;
 import charvax.swing.JDialog;
 import charvax.swing.JPanel;
 import charvax.swing.JScrollPane;
 import charvax.swing.border.EmptyBorder;
 import charvax.swing.JTable;
 
 /**
  * Pop up to show the available quick keys.
  *
  * @author Rick O'Sullivan
  */
 public class QuickKeys
 	extends JDialog
 {
	private static final long serialVersionUID = 1L;

 	/**
 	 * Create the dialog.
 	 */
 	public QuickKeys(Frame frame)
 	{
 		super(frame, "Quick Keys");
 		getContentPane().setLayout(new BorderLayout());
 		JPanel contentPanel = new JPanel();
 		contentPanel.setLayout(new FlowLayout());
 		contentPanel.setBorder(new EmptyBorder(1, 1, 1, 1));
 		getContentPane().add(contentPanel, BorderLayout.CENTER);
 		{
 			String[] headers = {"Action", "Key"};
 			Object[][] keys = 
 			{
 			 	{"Debug Batch"	,"CTRL_G"},
 			 	{"Run Batch"	,"CTRL_R"},
 			 	{"Save Batch"	,"CTRL_S"},
 			 	{"Mark Text"	,"CTRL_A"},
 			 	{"Cut Text"		,"CTRL_X"},
 			 	{"Copy Text"	,"CTRL_C"},
 			 	{"Paste Text"	,"CTRL_V"},
 			 	{"Blank Target"	,"CTRL_B"},
 			 	{"Blank Console","CTRL_K"},
 			 	{"Engine List"	,"CTRL_E"}
 			};
 			JTable table = new JTable(keys, headers);
 			table.setRowSelectionAllowed(false);
 			JScrollPane scrollPane = new JScrollPane(table);
 			contentPanel.add(scrollPane);
 		}
 		{
 			JPanel buttonPane = new JPanel();
 			buttonPane.setLayout(new FlowLayout());
 			getContentPane().add(buttonPane, BorderLayout.SOUTH);
 			{
 				JButton okButton = new JButton("OK");
 				okButton.setActionCommand("OK");
 				buttonPane.add(okButton);
 				okButton.addActionListener
 				(
 					new ActionListener()
 					{
 						@Override
 						public void actionPerformed(ActionEvent e)
 						{
 							setVisible(false);
 						}
 					}
 				);
 				setFocus(okButton);
 			}
 		}
 		pack();
 		setLocationRelativeTo(frame);
 	}
 }
 // vi:set tabstop=4 hardtabs=4 shiftwidth=4:
