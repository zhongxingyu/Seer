 package GenomicSetHandling;
 
 import genomeObjects.OSCreationInstructions;
 import genomeObjects.OrganismSet;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.Toolkit;
 import java.awt.Dialog.ModalityType;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JCheckBoxMenuItem;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.RootPaneContainer;
 
 import moduls.frm.FrmPrincipalDesk;
 
 public class NewGS extends JDialog implements ActionListener{
 
 	//fields
 	//data/base
 	private FrmPrincipalDesk f;
 	
 	//GUI
 	private JPanel jp, jp2, jpEnclosing;
 	private JTextField LblName, TxtName, LblNotes;
 	private JTextArea OrganismSetNotes;
 	private JButton btnOK;
 	
 	//Constructor
 	public NewGS(FrmPrincipalDesk f){
 		this.f = f;
 		this.getFrame();
 		this.getPanel();
 		this.pack();
 		
 		this.setModalityType(ModalityType.DOCUMENT_MODAL);
 		this.setVisible(true);
 	}
 	
 	//frame
 	public void getFrame(){
 		this.setSize(400,400);
 		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 		this.setLocationRelativeTo(null);
 		this.setTitle("Create New Genome Set");
 		this.setResizable(false);
 	}
 	
 	//panel
 	public void getPanel(){
 		
 		//initial GridBagLayout parameters
 		jp = new JPanel();
 		jp.setLayout(new GridBagLayout());
 		final GridBagConstraints c = new GridBagConstraints();
 		int gridy = 0;
 		c.weightx = 1;
 		c.insets = new Insets(2,2,2,2);
 		
 		c.gridx = 0;
 		c.gridy = gridy;
 		c.gridheight = 1;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.gridwidth = 1;
 		LblName = new JTextField("Name:");
 		LblName.setEditable(false);
 		jp.add(LblName, c);
 		
 		c.gridx = 1;
 		c.gridy = gridy;
 		c.gridheight = 1;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.gridwidth = 1;
 		TxtName = new JTextField("");
 		TxtName.setEditable(true);
 		TxtName.setColumns(30);
 		jp.add(TxtName, c);
 		
 		gridy++;
 		
 		c.gridx = 0;
 		c.gridy = gridy;
 		c.gridheight = 1;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.gridwidth = 1;
 		LblNotes = new JTextField("Notes:");
 		LblNotes.setEditable(false);
 		jp.add(LblNotes, c);
 		
 		//Actual enter points form
 		c.gridx = 1;
 		c.gridy = gridy;
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		c.fill = GridBagConstraints.NONE;
 		OrganismSetNotes = new JTextArea("");
 		OrganismSetNotes.setEditable(true);
 		JScrollPane ptsscroll = new JScrollPane(OrganismSetNotes);
 		ptsscroll.setPreferredSize(new Dimension(TxtName.getColumns()*12+8, 50));
 		jp.add(ptsscroll, c);
 		gridy++;
 		
 		//central frame
 		jp2 = new JPanel();
 		jp2.setLayout(new GridBagLayout());
 		GridBagConstraints d = new GridBagConstraints();
 		d.gridheight = 1;
 		d.gridx = 0;
 		d.gridy = 0;
 		d.fill = GridBagConstraints.NONE;
 		btnOK = new JButton("OK");
 		btnOK.addActionListener(this);
 		jp2.add(btnOK, d);
 		
 		//add to frame
 		jpEnclosing = new JPanel();
 		jpEnclosing.setLayout(new BorderLayout());
 		jpEnclosing.add(jp, BorderLayout.NORTH);
 		jpEnclosing.add(jp2, BorderLayout.SOUTH);
 		this.add(jpEnclosing);
 		
 	}
 
 	//Actions!
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if (e.getSource().equals(btnOK)){
 			//Empty string is just read as close window.
 			if (!TxtName.getText().equals("")){
 				if (f.getGenomeSets().keySet().contains(TxtName.getText())){
 					
 					JOptionPane.showMessageDialog(null,"A Genome Set of this name already exists.\nPlease choose another Name.",
 							"Name Already in use", JOptionPane.ERROR_MESSAGE);
 					
 				} else {
 					
 					//Information
 					OrganismSet OS = new OrganismSet();
 					OS.setName(TxtName.getText());
 					OS.setNotes(OrganismSetNotes.getText());
 					
 					//Switch options										
 					boolean AtLeastOneOS = true;
 					boolean SetNewOSToSelected = false;
 					
 					//If this is the first OS, not much more to do.
 					for (JCheckBoxMenuItem b : f.getCurrentItems()){
 						if (b.equals(f.getMG_NoGS())){
 							f.getMG_CurrentGS().remove(b);
 							f.getCurrentItems().remove(b);
 							AtLeastOneOS = false;
 							f.setOS(OS);
 							break;
 						} 
 					}
 					
 					//Last check
 					for (JCheckBoxMenuItem b : f.getCurrentItems()){
						if (b.getText().equals("Default Genome Set")){
 							AtLeastOneOS = true;
 						}
 					}
 
 					//if multiple OS, option to switch to newly created OS.
 					if (AtLeastOneOS){
 						
 						String MsgSwitch = "Would you like to switch to this genome set now?\n" +
 											"Depending on the number and size of genomes in a genome set,\n" +
 											"switching between genome sets may be a time-consuming process.";
 						
 						int SwitchOS = JOptionPane.showConfirmDialog(null,MsgSwitch,
 								"Proceed with context viewing", JOptionPane.YES_NO_OPTION);
 						
 						if (SwitchOS == JOptionPane.YES_OPTION){
 							
 							//add selection, and de-select all others
 							for (JCheckBoxMenuItem b : f.getCurrentItems()){
 								b.setSelected(false);
 							}
 							SetNewOSToSelected = true;
 						} 
 						
 					}
 					
 					//Initialize a file for the organism set, even if we don't use it.
 					f.ExportNonFocusOS(OS);
 					
 					//turn on additional options
 					f.OSMenuComponentsEnabled(true);
 					
 					//switch OS, if multiple OS, and option
 					if (SetNewOSToSelected){						
 						f.CallSwitchWorker(f.getOS().getName(), OS.getName());
 				     }
 
 					//Global actions
 					
 					//Add new check box menu item in the main menu
 					JCheckBoxMenuItem NewOS = new JCheckBoxMenuItem(OS.getName());
 					NewOS.setSelected(SetNewOSToSelected);	
 					if (!AtLeastOneOS){
 						NewOS.setSelected(true);
 					}
 					NewOS.addActionListener(f);
 					NewOS.setName(OS.getName());
 					
 					//update menu + corresponding list
 					f.getCurrentItems().add(NewOS);
 					f.getMG_CurrentGS().add(NewOS);
 					
 					//close window
 					this.dispose();
 				}
 				
 			} else{
 				this.dispose();
 			}
 		}
 	}
 }
