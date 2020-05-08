 import waba.ui.*;
 import waba.util.*;
 import extra.ui.*;
 import org.concord.waba.extra.ui.*;
 import org.concord.waba.extra.event.*;
 import org.concord.LabBook.*;
 import org.concord.CCProbe.*;
 
 class PtrWindow
 {
 	PtrWindow(LabObjectPtr ptr, LObjDictionary dict, boolean edit)
 	{
 		this.ptr = ptr;
 		this.dict = dict;
 		this.edit = edit;
 	}
 
 	LabObjectPtr ptr;
 
 	// is this going to be valid???
 	LObjDictionary dict;
 
 	boolean edit;
 }
 
 public class CCProbe extends ExtraMainWindow
     implements ViewContainer, MainView
 {
     LabBook labBook;
 	LabBookSession mainSession;
 	LabBookSession curWinSession = null;
     MenuBar menuBar;
     Menu file;
     Menu edit;
     TreeControl treeControl;
     TreeModel treeModel;
     Title 		title;
     
     
     Container me = new Container();
     LabObjectView lObjView = null;
     int myHeight;
 	int yOffset = 0;
 	Vector fileMenuStrings = new Vector();
 	
     int newIndex = 0;
 
     LObjDictionary loDict = null;
 
     String aboutTitle = "About CCProbe";
 	String [] fileStrings;
 	Vector fileListeners = new Vector();
 
     String [] creationTypes = {"Folder", "Notes", "Data Collector", 
 							   "Drawing","UnitConvertor","Image"};
 
 	int		[]creationID = {0x00010100};
     public void onStart()
     {
 		LabBook.init();
 		LabBook.registerFactory(new DataObjFactory());
 
 		// Dialog.showImages = false;
 		// ImagePane.showImages = false;
 
 		graph.Bin.START_DATA_SIZE = 25000;
 		graph.LargeFloatArray.MaxNumChunks = 25;
 
 		menuBar = new MenuBar();
 
 		// Notice the width and height will change here
 		setMenuBar(menuBar);
 		waba.fx.Rect myRect = content.getRect();
 		myHeight = myRect.height;
 		int dictHeight = myHeight;
 
 		me.setRect(0,0,width,myHeight);
 
 		add(me);
 
 		LabBookDB lbDB;
 		String plat = waba.sys.Vm.getPlatform();
 		if(plat.equals("PalmOS")){
 			graph.Bin.START_DATA_SIZE = 4000;
 			graph.LargeFloatArray.MaxNumChunks = 4;
 			GraphSettings.MAX_COLLECTIONS = 1;
 			lbDB = new LabBookCatalog("LabBook");
 		} else if(plat.equals("Java")){
 			/*
 			graph.Bin.START_DATA_SIZE = 1000;
 			graph.LargeFloatArray.MaxNumChunks = 1;
 			GraphSettings.MAX_COLLECTIONS = 1;
 			*/
 			lbDB = new LabBookCatalog("LabBook");
 		} else {
 			lbDB = new LabBookFile("LabBook");
 			GraphSettings.MAX_COLLECTIONS = 4;
 		}
 
 		if(myHeight < 180){
 			yOffset = 13;
 			dictHeight -= 13;
 			if(title == null) title = new Title("CCProbe");
 			title.setRect(0,0,width, 13);
 			me.add(title);
 		}
 
 		if(lbDB.getError()){
 			// Error;
 			exit(0);
 		}
 		file = new Menu("File");
 		
 		file.add(aboutTitle);
 		if(!plat.equals("PalmOS")){
 			file.add("-");
 			file.add("Exit");
 			fileStrings = new String [3];
 			fileStrings[0] = aboutTitle;
 			fileStrings[1] = "-";
 			fileStrings[2] = "Exit";
 		} else {
 			fileStrings = new String [1];
 			fileStrings[0] = aboutTitle;			
 		}
 		
 		file.addActionListener(this);
 		menuBar.add(file);
 
 		labBook = new LabBook();
 		LabObject.lBook = labBook;
 
 		Debug.println("Openning");
 		labBook.open(lbDB);
 
 		mainSession = labBook.getSession();
 
 		loDict = (LObjDictionary)mainSession.getObj(labBook.getRoot());
 		if(loDict == null){
 			loDict = DefaultFactory.createDictionary();
 			loDict.setName("Home");
 			labBook.store(loDict);
 
 		}
 		LabObjectView view = (LabObjectView)loDict.getView(this, true, mainSession);
 		
 		view.setRect(x,yOffset,width,dictHeight);
 		view.setShowMenus(true);
 		me.add(view);
 		lObjView = view;
 		if(loDict != null){
 			newIndex = loDict.getChildCount();
 		}
 
     }
 
 	public MainView getMainView()
 	{
 		return this;
 	}
 
     public void addMenu(LabObjectView source, Menu menu)
     {
 		if(menu != null) menuBar.add(menu);
     }
 
     public void delMenu(LabObjectView source, Menu menu)
     {
 		if(menu != null) menuBar.remove(menu);
     }
 
 	void updateFileMenu()
 	{		
 		int i;
 		file.removeAll();
 		for(i=0; i < fileMenuStrings.getCount(); i++){
 			String [] items = (String [])fileMenuStrings.get(i);
 			for(int j=0; j < items.length; j++){
 				file.add(items[j]);
 			}
 				file.add("-");
 
 		}		
 		
 		for(i = 0; i < fileStrings.length; i++){
 			file.add(fileStrings[i]);
 		}
 	}
 
 	public void addFileMenuItems(String [] items, ActionListener source)
 	{
 		fileMenuStrings.insert(0, items);
 		updateFileMenu();		
 		fileListeners.add(source);
 	}
 
 
 	public void removeFileMenuItems(String [] items, ActionListener source)
 	{
 		int index = fileMenuStrings.find(items);
 		if(index < 0) return;
 		fileMenuStrings.del(index);
 		updateFileMenu();
 		index = fileListeners.find(source);
 		if(index < 0) return;
 		fileListeners.del(index);
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
 				dView.showPage(newObj, true);
 			} else if(autoProp){
 				dView.showProperties(newObj);
 			} 
 			
 		}
 	}
 
     public void done(LabObjectView source)
 	{
 		if(source == curFullView){
 			curFullView.close();
 	    
 			// release it's session
 			if(curWinSession != null) curWinSession.release();
 			curWinSession = null;
 
 			// I don't think this is important
 			// The order is important here because closeTopWin
 			// calls setShowMenus which checks lObjView to decide which 
 			// menus to show.
 			closeTopWindowView();
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
 		int dictHeight = myHeight;
 		if(myHeight < 180){
 			yOffset = 13;
 			dictHeight -= 13;
 			if(title == null) title = new Title("CCProbe");
 			title.setRect(0,0,width, 13);
 			me.add(title);
 		}
 		replacement.setRect(x,yOffset,width,dictHeight);
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
 				Debug.println("commiting");
 				lObjView.close();
 				labBook.store(loDict);
 				if(!labBook.commit() ||
 				   !labBook.close()){
 					//error
 				} else {
 					labBook = null;
 					exit(0);
 				}
 			}else if(command.equals(aboutTitle)){
 				Dialog.showAboutDialog(aboutTitle,AboutMessages.getMessage());
 			} else {
 				for(int i=0; i<fileListeners.getCount(); i++){
 					((ActionListener)fileListeners.get(i)).actionPerformed(e);
 				}
 			}
 		}
     }
 
     public void onExit()
     {
 		Debug.println("closing");
 		if(labBook != null){
 			if(curFullView != null){
 				curFullView.close();
 			}
 			labBook.store(loDict);
 			labBook.commit();
 			labBook.close();
 		}
     }
 
 	LabObjectView curFullView = null;
 
 	public void closeTopWindowView()
 	{
 		if(fullViews != null &&
 		   fullViews.getCount() > 0){
 
 			Object topWin = fullViews.get(fullViews.getCount()-1);
 
 			if(!(topWin instanceof PtrWindow)) return;
 
 			remove(curFullView);
 
 			fullViews.del(fullViews.getCount()-1);			
 
 			if(fullViews.getCount() > 0){
 				Object newTopWin = fullViews.get(fullViews.getCount()-1);
 				if(!(newTopWin instanceof PtrWindow)) return;
 
 				PtrWindow pWin = (PtrWindow)newTopWin;
 				LabObjectPtr ptr = pWin.ptr;
 					
 				// the curWinSession should be not null
 				//				if(curWinSession != null) throw new RuntimeException("non null cur Win");
 				curWinSession = labBook.getSession();
 				LabObject lObj = curWinSession.load((LabObjectPtr)pWin.ptr);					
 
 				LabObjectView view = lObj.getView(this, pWin.edit, pWin.dict, curWinSession);
 				// if(view == null) throw new RuntimeException("");
 			
 				view.layout(true);
 				view.setRect(0,0,width,myHeight);
 				curFullView = view;
 
 				curFullView.setShowMenus(true);
 				add(curFullView);
 			} else {
 				curFullView = null;
 				lObjView.setShowMenus(true);
 				add(me);
 			}
 
 
 		}
 	}
 
 	Vector fullViews = new Vector();
 	public void showFullWindowObj(boolean edit, LObjDictionary dict,  LabObject obj,
 								  LabBookSession createdSession)
 	{
 		if(obj == null) return; //throw new RuntimeException("");
 		if(obj.ptr == null) return; //throw new RuntimeException("");
 
 		LabBookSession newSession = labBook.getSession();
 
 		if(curFullView == null){
 			lObjView.setShowMenus(false);
 			remove(me);
 			if(createdSession != null){
 				// We need to release this object from it's
 				// old session so it gets cleaned up if this window is 
 				// closed
 				newSession.load(obj.ptr);
 				createdSession.release(obj);
 			}
 		} else {
 			curFullView.setShowMenus(false);
 			if(curFullView.getContainer() != this) return; //throw new RuntimeException("error");
 
 			// close the view
 			curFullView.close();
 				
 			// release it's session
 			if(curWinSession != null){
 				if(curWinSession.contains(obj)){
 					// add this object to the new session
 					// because the oldWin will probably release it
 					newSession.load(obj.ptr);
 				}
 				curWinSession.release();
 			}
 			remove(curFullView);			
 		}
 
 		curWinSession = newSession;
 		LabObjectView view = obj.getView(this, edit, dict, curWinSession);
 		if(view == null) return; //throw new RuntimeException("");
 			
 		view.layout(true);
 		view.setRect(0,0,width,myHeight);
 		view.setShowMenus(true);
 		add(view);
 		curFullView = view;
 		
 		
 		fullViews.add(new PtrWindow(obj.ptr, dict, edit));		
 	}
 }
