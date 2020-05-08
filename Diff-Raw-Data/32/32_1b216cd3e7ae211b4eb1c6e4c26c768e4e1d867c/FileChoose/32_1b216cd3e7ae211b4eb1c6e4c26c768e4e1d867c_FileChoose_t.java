 package Trains.GUI;
 
 import java.awt.GridBagConstraints;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.IOException;
 
 import javax.swing.JFileChooser;
 import javax.swing.filechooser.FileNameExtensionFilter;
 
 import Trains.Controllers.Controller;
 
 public class FileChoose extends GUI {
 	
 	private final JFileChooser chooser;
 	private final FileNameExtensionFilter filter = new FileNameExtensionFilter("Text Files", "txt", "TXT");
 	private Controller control = Controller.getInstance();
 	
 	public FileChoose() {
 		super();
 		chooser = new JFileChooser();
 		chooser.setFileFilter(filter);
 	}
 	
 	public void show() {
 		
 		// So we don't have any layout collisions
 		constraints = new GridBagConstraints();
 		
 		// Remove everything that was on that pane
 		frame.getContentPane().removeAll();
 		
 		// File Chooser
 		constraints.fill = GridBagConstraints.BOTH;
 		constraints.weightx = 1;
 		constraints.weighty = 1;
 		constraints.gridx = 0;
 		constraints.gridy = 0;
 		constraints.gridwidth = 1;
 		constraints.gridheight = 1;
 		layout.setConstraints(chooser, constraints);
 		chooser.addActionListener(new ChooserListener());
 		frame.add(chooser);
 		
 		// Refresh the frame
 		frame.invalidate();
 		frame.validate();
 		frame.repaint();
 	}
 	
 	public class ChooserListener implements ActionListener {
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			// If the user presses cancel do this
 			if(JFileChooser.CANCEL_SELECTION.equals(e.getActionCommand())) {
 				control.ChangeToGetNodes();
 			}
 			// If the user presses open
 			else if(JFileChooser.APPROVE_SELECTION.equals(e.getActionCommand())){
 				System.out.println("You chose to open this file: " +
 			            chooser.getSelectedFile().getName());
 				try {
					
					// TODO catch true or false version of this in order to dialog with user, bad file, string
					if(control.readFile(chooser.getSelectedFile().getCanonicalPath())) 
						control.ChangeToSelectSolutionType();
					
 				} catch (IOException e1) {
 					System.out.println("Problem Reading in the File");
 					e1.printStackTrace();
 				}
 			}
 		}
 		
 	}
 	
 }
