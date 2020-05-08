 package ussr.aGui.fileChoosing;
 
 
import ussr.aGui.fileChoosing.fileDialog.FileDialogCustomizedOpen;
import ussr.aGui.fileChoosing.fileDialog.FileDialogCustomizedSave;
import ussr.aGui.fileChoosing.fileDialog.FileDialogTypes;
import ussr.aGui.fileChoosing.fileDialog.controllers.FileDialogControllerInter;
import ussr.aGui.fileChoosing.fileDialog.controllers.FileDialogXMLController;
 
 
 public interface FileDialogCustomizedInter extends FileChoosingWindowInter  {
 
 	
 	
 	public final static FileDialogControllerInter FD_XML_CONTROLLER = new FileDialogXMLController(); 
 	 /**
      * todo
      */
     public final static FileDialogCustomizedInter FD_OPEN_SIMULATION = new FileDialogCustomizedOpen(null,FileDialogTypes.OPEN_SIMULATION_XML,FD_XML_CONTROLLER),
                                                   FD_OPEN_ROBOT =  new FileDialogCustomizedOpen(null,FileDialogTypes.OPEN_ROBOT_XML,FD_XML_CONTROLLER),
                                                   FD_SAVE_SIMULATION = new FileDialogCustomizedSave(null,FileDialogTypes.SAVE_SIMULATION_XML,FD_XML_CONTROLLER),
                                                   FD_SAVE_ROBOT = new FileDialogCustomizedSave(null,FileDialogTypes.SAVE_ROBOT_XML,FD_XML_CONTROLLER);
                                                   
     ;
                                              /* FC_FRAME_SAVE_SIMULATION = new FileChooserCustomizedSave(FileFilterTypes.OPEN_SAVE_SIMULATION.getMap(),FC_XML_CONTROLLER),
                                               FC_FRAME_OPEN_ROBOT = new FileChooserCustomizedOpen(FileFilterTypes.OPEN_SAVE_ROBOT.getMap(),FC_XML_CONTROLLER),
                                               FC_FRAME_SAVE_ROBOT = new FileChooserCustomizedSave(FileFilterTypes.OPEN_SAVE_ROBOT.getMap(),FC_XML_CONTROLLER)    
                                               ;*/
     public String getFileName();
     
     public String getFileDirectory();
     
     public String getTopTitle();
     
     public void setSelectedFile( String selectedFile);
     
     
     public FileDialogControllerInter getFileDialogController();
     
     public FileDialogTypes getFileDialogType(); 
 	
 }
