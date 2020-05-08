 import waba.ui.*;
 import waba.util.*;
 import waba.fx.*;
 import waba.io.*;
 
 import org.concord.waba.extra.ui.*;
 import org.concord.waba.extra.event.*;
 
 import org.concord.LabBook.*;
 import org.concord.CCProbe.*;
 
 public class CCProbe extends MainView
 	implements DialogListener
 {
 
 	LabBookSession mainSession;
     Menu edit;
     Title 		title;
 	int yOffset = 0;
 	int objViewHeight = 0;
     int newIndex = 0;
 
     String aboutTitle = "About CCProbe";
     String [] creationTypes = {"Folder", "Notes", "Data Collector", 
 							   "Drawing","UnitConvertor","Image"};
 
 	String [] palmFileStrings = {"Beam LabBook", aboutTitle};
 	String [] javaFileStrings = {"Open LabBook..", 
 								 aboutTitle, "Serial Port Setup..", "-",
 								 "Exit"};
 	String [] ceFileStrings = {aboutTitle, "-", "Exit"};
 
 	int		[]creationID = {0x00010100};
 	LObjDictionaryView loDictView = null;
 	File labBookFile = null;
 	Label fileNameLabel = null;
 
     public void onStart()
     {
 		super.onStart();
     	
 		LabBook.registerFactory(new DataObjFactory());
 
 		// Dialog.showImages = false;
 		// ImagePane.showImages = false;
 
 		graph.Bin.START_DATA_SIZE = 25000;
 		graph.LargeFloatArray.MaxNumChunks = 25;
 
 		objViewHeight = myHeight;
 
 		LabBookDB lbDB;
 		String plat = waba.sys.Vm.getPlatform();
 		if(plat.equals("PalmOS")){
 			graph.Bin.START_DATA_SIZE = 4000;
 			graph.LargeFloatArray.MaxNumChunks = 4;
 			GraphSettings.MAX_COLLECTIONS = 1;
 			graph.GraphViewLine.scrollStepSize = 0.45f;
 			lbDB = new LabBookCatalog("LabBook");
 			CCTextArea.INTER_LINE_SPACING = 0;
 			Dialog.showImages = false;
 		} else if(plat.equals("Java")){
 			/*
 			graph.Bin.START_DATA_SIZE = 4000;
 			graph.LargeFloatArray.MaxNumChunks = 4;
 			*/
 			// GraphSettings.MAX_COLLECTIONS = 1;			
 			lbDB = new LabBookCatalog("LabBook");
 			labBookFile = new File("LabBook.PDB", File.DONT_OPEN);
 			// Dialog.showImages = false;
 			// lbDB = new LabBookFile("LabBook"); 
 		} else {
 			lbDB = new LabBookFile("LabBook");
 			GraphSettings.MAX_COLLECTIONS = 4;
 		}
 
 		if(lbDB.getError()){
 			// Error;
 			exit(0);
 		}
 		
 		if(myHeight < 180){
 			// Add a palm title if this is a small window
 			yOffset = 13;
 			objViewHeight -= 13;
 			if(title == null) title = new Title("CCProbe");
 			title.setRect(0,0,width, 13);
 			me.add(title);
 		} else if(myHeight > 300){
 			objViewHeight -= 13;
 		}
 
 		if(plat.equals("PalmOS")){
 			addFileMenuItems(palmFileStrings, null);
 		} else if(plat.equals("Java")){
 			addFileMenuItems(javaFileStrings, null);
 		} else {
 			addFileMenuItems(ceFileStrings, null);
 		}
 
 		if(myHeight > 300){
 			fileNameLabel = new Label("");
 			fileNameLabel.setRect(x, yOffset+objViewHeight, width, 13);
 			me.add(fileNameLabel);
 		}
 
 		openLabBookDB(lbDB);
 		
 		if(plat.equals("PalmOS")){
 			loDictView.checkForBeam();
 		}
 
     }
 
 	public void openLabBookDB(LabBookDB lbDB)
 	{
 		LObjDictionary loDict = null;
 
 		if(labBook == null){
 			setLabBook(new LabBook());
 		}
 
 		Debug.println("Openning");
 		labBook.open(lbDB);
 		LabObjectPtr rootPtr = labBook.getRoot();
 		
 		mainSession = rootPtr.getSession();
 
 		loDict = (LObjDictionary)mainSession.getObj(rootPtr);
 		if(loDict == null){
 			loDict = DefaultFactory.createDictionary();
 			loDict.setName("Home");
 			mainSession.storeNew(loDict);
 			labBook.store(loDict);
 
 		}
 
 		if(loDictView != null){
 			me.remove(loDictView);
 		}
 
 		lObjView = loDict.getView(this, true, mainSession);
 		loDictView = (LObjDictionaryView)lObjView;
 		
 		lObjView.setRect(x,yOffset,width,objViewHeight);
 		lObjView.setShowMenus(true);
 		me.add(lObjView);
 		newIndex = loDict.getChildCount();
 
 		if(fileNameLabel != null){
 			fileNameLabel.setText(labBookFile.getPath());
 		}
     }
 
 
 	public String [] getCreateNames()
 	{
 		String  []createNames = creationTypes;
 		if(creationID != null){
 			for(int i = 0; i < creationID.length; i++){
 				int factoryType = (creationID[i] & 0xFFFF0000);
 				factoryType >>>= 16;
 				int objID = creationID[i] & 0xFFFF;
 				LabObjectFactory factory = null;
 				for(int f = 0; f < LabBook.objFactories.length; f++){
 					if(LabBook.objFactories[f] == null) continue;
 					if(LabBook.objFactories[f].getFactoryType() == factoryType){
 						factory = LabBook.objFactories[f];
 						break;
 					}
 				}
 				if(factory != null){
 					LabObjDescriptor []desc = factory.getLabBookObjDesc();
 					if(desc != null){
 						for(int d = 0; d < desc.length; d++){
 							if(desc[d] == null) continue;
 							if(desc[d].objType == objID){
 								String name = desc[d].name;
 								if(name != null){
 									String []newNames = new String[createNames.length+1];
 									waba.sys.Vm.copyArray(createNames,0,newNames,0,createNames.length);
 									newNames[createNames.length] = name;
 									createNames = newNames;
 								}
 							}
 						}
 					}
 				}
 			}
 		}	
 		return createNames;
 	}
 
 	public void createObj(String objType, LObjDictionaryView dView)
 	{
 		LabObject newObj = null;
 //		boolean autoEdit = false;
 		boolean autoEdit = true;
 		boolean autoProp = true;
 		
 		for(int f = 0; f < LabBook.objFactories.length; f++){
 			if(LabBook.objFactories[f] == null) continue;
 			LabObjDescriptor []desc = LabBook.objFactories[f].getLabBookObjDesc();
 			if(desc == null) continue;
 			boolean doExit = false;
 			for(int d = 0; d < desc.length; d++){
 				if(desc[d] == null) continue;
 				if(objType.equals(desc[d].name)){
 					newObj = LabBook.objFactories[f].makeNewObj(desc[d].objType);
 					if(objType.equals("Folder")){
 						autoEdit = false;
 					} else if(objType.equals("Data Collector")){	       
 						autoEdit = false;
 					}
 					doExit = true;
 					break;
 				}
 			}
 			if(doExit) break;
 		}
 
 		if(newObj != null){
 			dView.getSession().storeNew(newObj);
 			if(newIndex == 0){
 				newObj.setName(objType);		    
 			} else {
 				newObj.setName(objType + " " + newIndex);		    
 			}			
 			newIndex++;
 			
 			dView.insertAtSelected(newObj);
 
 			// The order seems to matter here.  
 			// insert and selected for some reason nulls the pointer.
 			// perhaps by doing a commit?
 			// newObj.store();
 			
 			if(autoEdit){
 				dView.openSelected(true);
 			} else if(autoProp){
 				dView.showProperties(newObj);
 			} 
 			
 		}
 	}
 
 	Dialog beamLBDialog = null;
     public void dialogClosed(DialogEvent e)
 	{
 		String command = e.getActionCommand();
 		if(e.getSource() == beamLBDialog){
 			if(command.equals("Beam")){
 				waba.sys.Vm.exec("CCBeam", "LabBook,,," +
 								 "LabBook for CCProbe", 0, true);
 			}
 		}
 	}
 
     public void reload(LabObjectView source)
     {
 		if(source != lObjView) Debug.println("Error source being removed");
 		LabObject obj = source.getLabObject();
 		LabBookSession oldSession = source.getSession();
 		source.close();
 		me.remove(source);
 		if(title != null){
 			me.remove(title);
 		}
 		LabObjectView replacement = obj.getView(this, true, oldSession);
 		// This automatically does the layout call for us
 
 		waba.fx.Rect myRect = content.getRect();
 		myHeight = myRect.height;
 		objViewHeight = myHeight;
 		if(myHeight < 180){
 			yOffset = 13;
 			objViewHeight -= 13;
 			if(title == null) title = new Title("CCProbe");
 			title.setRect(0,0,width, 13);
 			me.add(title);
 		} else if(myHeight > 300) {
 			objViewHeight -= 13;
 		}
 		replacement.setRect(x,yOffset,width,objViewHeight);
 		replacement.setShowMenus(true);
 		me.add(replacement);
 		lObjView = replacement;
     }
 
     public void actionPerformed(ActionEvent e)
     {
 		String command;
 		Debug.println("Got action: " + e.getActionCommand());
 
 		if(e.getSource() == file){
 			command = e.getActionCommand();
 			if(command.equals("Exit")){
 				handleQuit();
 			}else if(command.equals(aboutTitle)){
 				handleAbout();
 			} else if(command.equals("Beam LabBook")){
 				String [] buttons = {"Beam", "Cancel"};
 				beamLBDialog = Dialog.showConfirmDialog(this, "Confirm", 
 														"Close CCProbe on the receiving| " +
 														"Palm before beaming.",
 														buttons,
 														Dialog.INFO_DIALOG);
 			} else if(command.equals("Serial Port Setup..")){
 				DataExport.showSerialDialog();
 			} else if(command.equals("Open LabBook..")){
 				// Show them the pick file dialog
 				FileDialog fd = FileDialog.getFileDialog(FileDialog.FILE_LOAD, null);
 				fd.show();
 
 				if(fd.getFilePath() == null){
 					// They hit cancel on the file dialog
 					return;
 				}
 
 				LabBookDB lbDB = LObjDatabaseRef.getDatabase(fd.getFilePath());
 				
 				if(lbDB.getError()){
 					// Error;
 					Dialog.showMessageDialog(null, "Error..",
 											 "This LabBook file is invalid",
 											 "Close", Dialog.ERR_DIALOG);
 					return;
 				}
 		
 				// Once they have picked a file
 				onExit();
 
 				// Open a new labBook using their file
 				labBookFile = new File(fd.getFilePath(), File.DONT_OPEN);
 				openLabBookDB(lbDB);
 			} else {
 				super.actionPerformed(e);
 			}
 		}
     }
 
     public void onExit()
     {
 		Debug.println("closing");
 		if(curFullView != null){
 			curFullView.close();
 			curFullView = lObjView = null;
 		} else if(lObjView != null){
 			lObjView.close();
 			lObjView = null;
 		}
 
 		if(labBook != null){
 			labBook.commit();
 			String plat = waba.sys.Vm.getPlatform();
 			if(plat.equals("Java")){
 				Dialog messDialog = 
 					Dialog.showMessageDialog(null, "Please Wait..",
 											 "Please Wait... saving the LabBook",
 											 "Cancel", Dialog.INFO_DIALOG);
 
 				LabObjectPtr rootPtr = labBook.getRoot();
 				mainSession = rootPtr.getSession();
 
 				LObjDictionary loDict = (LObjDictionary)mainSession.getObj(rootPtr);
 				
 				// Note if the loDict is null then this won't make a labBook
 				// We ought to also do this if there are no changes.
 				File origLabBookFile = null;
 
 				if(loDict != null ){ 
 					LabBookDB tmpDB = null;
 					File tmpLabBookFile = null;
 					if(labBook.getDB() instanceof LabBookCatalog){
						// This is dangerous but I don't see another way to do it
						labBookFile.delete();

 						origLabBookFile = new File("LabBook.PDB", File.DONT_OPEN);
 						if(origLabBookFile.exists()){
 							origLabBookFile.rename("_tmp_LabBook.PDB");
 							origLabBookFile = new File("_tmp_LabBook.PDB", File.DONT_OPEN);
 						} else {
 							origLabBookFile = null;
 						}
 
 						tmpDB = (LabBookDB)new LabBookCatalog("LabBook");
 						tmpLabBookFile = new File("LabBook.PDB", File.DONT_OPEN);
 					} else {
 						tmpDB = (LabBookDB)new LabBookFile("_tmp_LabBook");
 						tmpLabBookFile = new File("_tmp_LabBook", File.DONT_OPEN);
 					}
 
 					labBook.export(loDict, tmpDB);
 					tmpDB.save();
 					tmpDB.close();
 

					if(labBook.getDB() instanceof LabBookFile){
						// This is dangerous but I don't see another way to do it
						labBookFile.delete();
					}

 					tmpLabBookFile.rename(labBookFile.getPath());
 					if(origLabBookFile != null){
 						origLabBookFile.rename("LabBook.PDB");
 					}
 				}
 				labBook = null;
 
 				messDialog.hide();
 			} else {
 				labBook.close();
 				labBook = null;			
 			}
 		}
     }
 
 	public void handleQuit(){
 		onExit();
 		exit(0);
 	}
 
 	public void handleAbout(){
 		Dialog.showAboutDialog(aboutTitle,AboutMessages.getMessage());
 	}
 }
