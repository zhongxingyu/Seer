 package com.mcvs.controller;
 
 import java.awt.event.*;
 import java.io.*;
 import java.util.*;
 import javax.swing.*;
 import javax.swing.event.*;
 import java.awt.*;
 import com.mcvs.view.*;
 import com.mcvs.core.*;
 import com.mcvs.model.*;
 
 public class MCVSController {
 	private static MCVSController INSTANCE = null;
 	private MCVSView view;
 	private MCVSModel model;
 	
 	/*
 	 * Controller for the application. Listens for events from the view
 	 * and uses the model to perform appropriate actions
 	 */
 	private MCVSController(MCVSView v, MCVSModel m) {
 		view = v;
 		model = m;
 		
 		this.firstBuildDirectories(model);
 		model.lockFile("MCVC.lock");
 		
 		/*
 		 * Gets the views JTable and sets the model with the data gathered from
 		 * the getEntites method. Sets the current version label as well.
 		 */
 		view.getVersionTable().setModel(new VersionTableModel(model.getEntities()));
 		view.setCurrentVersionLabel(model.getCurrentVersion(true));
 		
 		/*
 		 * Gets the views reference to the add jar dialog object and sets its 
 		 * combo box model to a new VersionComboBoxModel. The VersionComboBoxModel
 		 * is passed an array of strings returned by the getAllMCVersions function
 		 * in the MCVSModel class.
 		 */
 		view.getAddJarDialog().getVersionComboBox().setModel(new VersionComboBoxModel(model.getAllMCVersions()));
 		
 		//Add action listeners to the views controls
 		view.addLaunchButtonListener(new LaunchButtonListener());
 		view.addJarItemListener(new AddJarListener());
 		view.addExitListener(new ExitListener());
 		view.addReportBugItemListener(new ReportBugListener());
 		view.addAboutItemListener(new AboutListener());
 		view.addVersionTableListener(new VersionTableMouseListener());
 		view.addRenameItemListener(new RenameItemListener());
 		view.addDeleteItemListener(new DeleteItemListener());
 		
 		//Add action listeners to the add jar dialog controls
 		view.getAddJarDialog().addSubmitButtonListener(new AddJarSubmitButtonListener());
 		view.getAddJarDialog().addCancelButtonListener(new AddJarCancelButtonListener());
 		view.getAddJarDialog().addNameTextDocumentListener(new AddJarNameTextDocumentListener());
 		
 		/*
 		 * Once everything is built, display the GUI
 		 */
 		view.setVisible(true);
 		this.firstRunPrompt();
 	}
 	
 	public static MCVSController getInstance(MCVSView v, MCVSModel m) {
 		if(INSTANCE==null) {
 			INSTANCE = new MCVSController(v, m);
 		}
 		
 		return INSTANCE;
 	}
 	
 	public void firstBuildDirectories(MCVSModel model) {
		if(model.checkFirstRun()) {
 			
 			try {
 				model.createWorkingDirectory();
 			} 
 			catch (IOException ex) {
 				// TODO Auto-generated catch block
 				ex.printStackTrace();
 			} 
 			catch (InterruptedException ex) {
 				// TODO Auto-generated catch block
 				ex.printStackTrace();
 			}
 		}
 	}
 	
 	public void firstRunPrompt() {
 		if(model.checkFirstRun()) {
 			int opt = JOptionPane.showConfirmDialog(view, "<html>It appears this is your first time running Minecraft " +
 					"Version Control.<br /> Would you like to setup your current version of Minecraft?<br /><br /><b>Note: " +
 					"If you don't then nothing will appear in your file chooser<br /> and any new jar you add " +
 					"will overwrite your current version.</b><br /><br /></html>", "First run", JOptionPane.YES_NO_OPTION);
 			
 			if(opt == JOptionPane.YES_OPTION) {
 				FileDialog fileDialog = view.getFileChooser();
 				fileDialog.setDirectory(model.getPlatformManger().getMinecraftDirectory());
 				fileDialog.setVisible(true);
 				
 				if(fileDialog.getFile()!=null) {
 					AddJarDialog jarDialog = view.getAddJarDialog();
 					ArrayList<Entity> temp = new ArrayList<Entity>();
 					
 					temp.add(new Entity(fileDialog.getFile(), fileDialog.getDirectory()));
 					jarDialog.setListViewData(temp);
 					jarDialog.setVisible(true);
 				}
 			}
 		}
 	}
 	
 	/*
 	 * Inner classes implementing the ActionListener interface
 	 * used to listen for events on the view.
 	 */
 	
 	class LaunchButtonListener implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			model.launchMinecraft();
 		}
 	}
 	
 	class ExitListener implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			model.closeMCVS();
 		}
 	}
 	
 	class ReportBugListener implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			view.getBugDialog().setVisible(true);
 		}
 	}
 	
 	class AboutListener implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			JOptionPane.showMessageDialog(view, "Program: Minecraft Controller Switcher\nCreated by: Kyle Schattler\nVersion: " +
 					model.getMCVSVersion(), "About", JOptionPane.PLAIN_MESSAGE);
 		}
 	}
 	
 	class AddJarListener implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			FileDialog fileDialog = view.getFileChooser();
 			fileDialog.setDirectory(model.getPlatformManger().getHomeDirectory());
 			fileDialog.setVisible(true);
 			
 			if(fileDialog.getFile()!=null) {
 				AddJarDialog jarDialog = view.getAddJarDialog();
 				ArrayList<Entity> temp = new ArrayList<Entity>();
 				
 				temp.add(new Entity(fileDialog.getFile(), fileDialog.getDirectory()));
 				jarDialog.setListViewData(temp);
 				jarDialog.setVisible(true);
 			}
 		}
 	}
 	
 	/*
 	 * Class used to implement the submit button functionality on 
 	 * the AddJarDialog frame.
 	 */
 	class AddJarSubmitButtonListener implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			/*
 			 * Creates temporary reference objects of the AddJarDialog, source
 			 * entity object, and the destination entity object.
 			 */
 			AddJarDialog temp = view.getAddJarDialog();
 			Entity sourceEntity = temp.getSelectedEntity();
 			Entity destEntity = new Entity(temp.getNameText(), (String)temp.getVersion(), "");
 			int opt = -1;
 			
 			//Checks that the version doesn't already exist in the versions directory
 			if(!model.makeDirectory((String)temp.getVersion())) {
 				opt = JOptionPane.showConfirmDialog(view, "This version of Minecraft already" +
 						" exists, do you want to overwrite it?", "File already exists", JOptionPane.YES_NO_OPTION);
 			}
 			
 			/*
 			 * If the version already exists and the user doesn't want to replace
 			 * it then return out of the method.
 			 */
 			if(opt==JOptionPane.NO_OPTION) {
 				return;
 			}
 			
 			/*
 			 * If the version already exists and the user does want to replace 
 			 * it then attempt to delete the directory for the old version and 
 			 * create a directory for the new version.
 			 */
 			if(opt==JOptionPane.YES_OPTION) {
 				boolean deleted = model.deleteVersionFile(destEntity.getVersion());
 				boolean made = model.makeDirectory(destEntity.getVersion());
 				/*
 				 * If the file old version wasn't deleted or the new version 
 				 * wasn't made, inform the user that something wen't wrong.
 				 */
 				if(!deleted || !made) {
 					//TODO Copy the old version and rewrite to if something went wrong.
 					JOptionPane.showMessageDialog(view, "Something went wrong when trying to delete the file. Please " +
 							"Try again.");
 				}
 			}
 			//Finally try to add the new version jar to the file
 			try {
 				model.addVersion(sourceEntity, destEntity);
 			} 
 			catch (IOException ex) {
 				// TODO Auto-generated catch block
 				ex.printStackTrace();
 			} catch (InterruptedException ex) {
 				// TODO Auto-generated catch block
 				ex.printStackTrace();
 			}
 			/*
 			 * If the setAsCurrentVersion checkbox is checked replace the current
 			 * Minecraft version with the one just added.
 			 */
 			if(temp.setAsCurrentVersion()) {
 				try {
 					model.moveVersion(destEntity.getVersion(), destEntity.getName());
 				}
 				catch (FileNotFoundException ex) {
 					ex.printStackTrace();
 					JOptionPane.showConfirmDialog(view, "Something went wrong when moving the " +
 							"Minecraft jar file. Hit okay to send error to developer.", "Error", JOptionPane.WARNING_MESSAGE);
 				}
 				catch (IOException ex) {
 					// TODO Auto-generated catch block
 					ex.printStackTrace();
 				}
 				
 				/*
 				 * Updates the currentVer text file and sets the current version label
 				 * to the selected value
 				 */
 				model.updateCurrentVersion(destEntity.getVersion());
 				view.setCurrentVersionLabel(destEntity.getVersion());
 			}
 			
 			view.addEntityToTable(destEntity);
 			temp.setVisible(false);
 		}
 	}
 	
 	/*
 	 * Sets the add jar dialog frame to invisible
 	 */
 	class AddJarCancelButtonListener implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			view.getAddJarDialog().setVisible(false);
 		}
 	}
 	
 	/*
 	 * Class used to listen to changes in the name TextField on the
 	 * AddJarDialog frame.
 	 */
 	class AddJarNameTextDocumentListener implements DocumentListener {
 		private AddJarDialog temp = view.getAddJarDialog();
 		
 		/*
 		 * (non-Javadoc)
 		 * @see javax.swing.event.DocumentListener#changedUpdate(javax.swing.event.DocumentEvent)
 		 * All methods perform the same task, just on different triggers.
 		 * If the name TextField is empty disable the submit button, else
 		 * enable the submit button.
 		 */
 		
 		@Override
 		public void changedUpdate(DocumentEvent e) {
 			if(temp.getNameText().length()==0) {
 				temp.setSubmitButtonEnabled(false);
 			}
 			else {
 				temp.setSubmitButtonEnabled(true);
 			}
 		}
 
 		@Override
 		public void insertUpdate(DocumentEvent e) { 
 			if(temp.getNameText().length()==0) {
 				temp.setSubmitButtonEnabled(false);
 			}
 			else {
 				temp.setSubmitButtonEnabled(true);
 			}
 		}
 
 		@Override
 		public void removeUpdate(DocumentEvent e) {
 			if(temp.getNameText().length()==0) {
 				temp.setSubmitButtonEnabled(false);
 			}
 			else {
 				temp.setSubmitButtonEnabled(true);
 			}
 		}
 	}
 	
 	/*
 	 * Classed used to capture mouse events from the version table
 	 */
 	class VersionTableMouseListener implements MouseListener {
 
 		@Override
 		public void mouseClicked(MouseEvent e) { 
 			//Checks if the user double clicked using the left mouse button
 			if(e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
 				JTable tempTable = view.getVersionTable();
 				
 				/* 
 				 * Makes sure the current version saved in the model doesn't match with the current version
 				 * being displayed by the view.
 				 */
 				if(!model.getCurrentVersion(false).equals(tempTable.getValueAt(tempTable.getSelectedRow(), 1))) {
 					
 					//Prompts the user to make sure they really meant to switch versions
 					int opt = JOptionPane.showConfirmDialog(view, "Are you sure you want to" +
 							" change to this version?", "Change version", JOptionPane.YES_NO_OPTION);
 					
 					//If they click the yes button
 					if(opt == JOptionPane.YES_OPTION) {
 						
 						//Store temp values for the version and filename of the selected row 
 						String tempVer = (String) tempTable.getValueAt(tempTable.getSelectedRow(), 1);
 						String tempName = (String) tempTable.getValueAt(tempTable.getSelectedRow(), 0);
 						try {
 							model.moveVersion(tempVer, tempName);
 						}
 						catch (FileNotFoundException ex) {
 							ex.printStackTrace();
 							JOptionPane.showConfirmDialog(view, "Something went wrong when moving the " +
 									"Minecraft jar file. Hit okay to send error to developer.", "Error", JOptionPane.WARNING_MESSAGE);
 						}
 						catch (IOException ex) {
 							// TODO Auto-generated catch block
 							ex.printStackTrace();
 						}
 						
 						/*
 						 * Updates the currentVer text file and sets the current version label
 						 * to the selected value
 						 */
 						model.updateCurrentVersion(tempVer);
 						view.setCurrentVersionLabel(tempVer);
 					}
 				}
 			}
 		}
 
 		@Override
 		public void mouseEntered(MouseEvent e) { }
 
 		@Override
 		public void mouseExited(MouseEvent e) { }
 
 		@Override
 		public void mousePressed(MouseEvent e) {
 			/*
 			 * Needed in mousePressed and mouseReleased to work on all
 			 * operating systems.
 			 * 
 			 *  Checks of the right mouse button was released
 			 */
 			if(e.getButton() == MouseEvent.BUTTON3) {
 				/*
 				 * Stores a temp reference of the views JTable and gets the row
 				 * that contains the point at e.getPoint
 				 */
 				JTable temp = view.getVersionTable();
 				int r = temp.rowAtPoint(e.getPoint());
 				
 				/*
 				 * Makes sure the value of r is within the bounds of the tables rows
 				 * if it is set that row as the selected row; else clear the current
 				 * selected row.
 				 */
 				if (r >= 0 && r < temp.getRowCount()) {
 					temp.setRowSelectionInterval(r, r);
 				} else {
 					temp.clearSelection();
 				}
 	            
 				/*
 				 * Get the row index by getting the selected row, if that index is
 				 * less than 0, return out of the function.
 				 */
 	            int rowindex = temp.getSelectedRow();
 	            if (rowindex < 0) {
 	                return;
 	            }
 	            
 	            //Display the popup menu at the clicked point
 	            if (e.isPopupTrigger() && e.getComponent() instanceof JTable ) {
 	                view.showPopupMenu(e.getComponent(), e.getX(), e.getY());
 	            }
 			}
 		}
 
 		@Override
 		public void mouseReleased(MouseEvent e) {
 			/*
 			 * Needed in mousePressed and mouseReleased to work on all
 			 * operating systems.
 			 * 
 			 *  Checks of the right mouse button was released
 			 */
 			if(e.getButton() == MouseEvent.BUTTON3) {
 				/*
 				 * Stores a temp reference of the views JTable and gets the row
 				 * that contains the point at e.getPoint
 				 */
 				JTable temp = view.getVersionTable();
 				int r = temp.rowAtPoint(e.getPoint());
 				
 				/*
 				 * Makes sure the value of r is within the bounds of the tables rows
 				 * if it is set that row as the selected row; else clear the current
 				 * selected row.
 				 */
 				if (r >= 0 && r < temp.getRowCount()) {
 					temp.setRowSelectionInterval(r, r);
 				} else {
 					temp.clearSelection();
 				}
 	            
 				/*
 				 * Get the row index by getting the selected row, if that index is
 				 * less than 0, return out of the function.
 				 */
 	            int rowindex = temp.getSelectedRow();
 	            if (rowindex < 0) {
 	                return;
 	            }
 	            
 	            //Display the popup menu at the clicked point
 	            if (e.isPopupTrigger() && e.getComponent() instanceof JTable ) {
 	                view.showPopupMenu(e.getComponent(), e.getX(), e.getY());
 	            }
 			}
 		}
 	}
 	
 	/*
 	 * Class used to implement the renaming of filenames when selecting the
 	 * rename option on the JPopupMenu for the JTable.
 	 */
 	class RenameItemListener implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			
 			//Shows the dialog to get users input for the new name
 			String value = JOptionPane.showInputDialog(view, "Enter new name for file:", "New name");
 			
 			//Checks that the value entered by the user isn't null or empty
 			if(value!=null && value.trim()!="") {
 				/*
 				 * Renames the file in the JTable and passes the old name of the
 				 * file to a new entity object.
 				 */
 				Entity entity = view.renameEntityInTable(value);
 				try {
 					//Tries to rename the actual file and rewrite the entity file to match
 					model.renameFile(entity.getVersion()+"/"+entity.getName(), entity.getVersion()+"/"+value);
 					model.writeEntityFile(value, entity.getVersion());
 				} 
 				catch (IOException e1) {
 					// TODO Auto-generated catch block
 					e1.printStackTrace();
 				} catch (InterruptedException ex) {
 					// TODO Auto-generated catch block
 					ex.printStackTrace();
 				}
 			}
 		}
 	}
 	
 	/*
 	 * Class used to implement the deleting of a Minecraft version from the JTable
 	 */
 	class DeleteItemListener implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			//Makes sure the user meant to select the delete option
 			int opt = JOptionPane.showConfirmDialog(view, "Are you sure you want to delete this" +
 					" version of Minecraft?", "Delete file", JOptionPane.WARNING_MESSAGE);
 			
 			//If they did, remove the entity from the table and delete the version file
 			if(opt==JOptionPane.YES_OPTION) {
 				model.deleteVersionFile(view.removeEntityFromTable(model.getCurrentVersion(false)));
 			}
 		}	
 	}
 	
 	/*
 	 * Class used to check if an update is currently available for MCVersion-Control
 	 */
 	class UpdateItemListener implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent arg0) {
 			
 		}
 		
 	}
 }
