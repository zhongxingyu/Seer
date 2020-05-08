 package org.concord.LabBook;
 
 import waba.ui.*;
 import waba.fx.*;
 import waba.io.*;
 import org.concord.waba.extra.ui.*;
 import org.concord.waba.extra.io.*;
 import org.concord.waba.extra.event.*;
 import org.concord.waba.extra.util.*;
 // import org.concord.waba.WFTPClient.*;
 
 /*
 class ScreenWriter 
 	implements LinePrinter
 {
 	Graphics g = null;
 	FontMetrics fm = null;
 	int lineY = 10;
 	int lineX = 0;
 
 	ScreenWriter(Graphics g, FontMetrics fm)
 	{
 		this.g = g;
 		this.fm = fm;
 	}
 
 	public void print(String s)
 	{
 		g.drawText(s, lineX, lineY);
 		lineX += fm.getTextWidth(s);
 		// System.out.print(s);
 	}
 
 	public void println(String s)
 	{
 		g.drawText(s, lineX, lineY);
 		lineY += 10;
 		lineX = 0;
 		// System.out.println(s);
 	}
 }
 */
 
 public class LObjDictionaryView extends LabObjectView 
     implements ActionListener, DialogListener, ScrollListener, TreeControlListener
 {
 	final static String beamCatNameOut = "CCBeamOutDB";
 	final static String beamCatNameIn = "CCBeamInDB";
 
 	public		boolean viewFromExternal = false;
     TreeControl treeControl;
     TreeModel treeModel;
     RelativeContainer me = new RelativeContainer();
 
     int newIndex = 0;
 
     LObjDictionary dict;
 	LabObjectPtr dictPtr;
  //   GridContainer buttons = null; 
     Container buttons = null; 
  
     Button doneButton = new Button("Done");
     Button newButton = new Button("New");
     Button openButton = new Button("Open");
     
     Choice  folderChoice;
 
     Menu editMenu = new Menu("Edit");
 
     boolean editStatus = false;
 
 	//	String [] fileStrings = {"New..", "Open", "Rename..", "Send via FTP", "FTP settings..", 
 	// 						 "Import..", "Export..", "Delete"};
 	String [] fileStrings = {"New..", "Open", "Rename..", "Import..", "Export..", "Delete"};
 	String [] palmFileStrings = {"New..", "Open", "Beam selected", "Rename..", "Delete"};
 
 	CCScrollBar				scrollBar;
 	waba.util.Vector 		pathTree;
 
 	/*
 	String username = "scytacki";
 	String ftpServer = "4.19.234.31";
 	String ftpDirectory = ".";
 	*/
 
     public LObjDictionaryView(ViewContainer vc, LObjDictionary d,
 							  LabBookSession session)
     {
 		super(vc, (LabObject)d, session);
 		dict = d;
 
 		dictPtr = dict.getVisiblePtr();
 
 		add(me);
 		editMenu.add("Cut");
 		editMenu.add("Copy");
 		editMenu.add("Paste");
 		editMenu.add("Properties...");
 		editMenu.add("Toggle hidden");
 		editMenu.addActionListener(this);
     }
 
 	public static String ROOT_TREE_NODE_NAME = "Home";
 	DictTreeNode rootNode = null;
 
     public void layout(boolean sDone)
     {
 		if(didLayout) return;
 		didLayout = true;
 
 		showDone = sDone;
 
 		rootNode = new DictTreeNode(dict.getVisiblePtr(), session, dict.lBook);
 		treeModel = new TreeModel(rootNode);
 		treeControl = new TreeControl(treeModel);
 		treeControl.addTreeControlListener(this);
 		treeControl.showRoot(false);
 		folderChoice = new Choice();
 		if(pathTree == null){
 			folderChoice.add(ROOT_TREE_NODE_NAME);
 		}else{
 			for(int n = 0; n < pathTree.getCount(); n++){
 				folderChoice.add(pathTree.get(n).toString());
 			}
 		}
 		me.add(folderChoice);
 /*
 		if(showDone){
 			buttons = new GridContainer(6,1);
 			buttons.add(doneButton, 5, 0);
 		} else {
 			buttons = new GridContainer(5,1);
 		}
 */
 		buttons = new Container();
 //		if(showDone){
 //			buttons.add(doneButton);
 //		}
 
 
 		if(!viewFromExternal){
 			buttons.add(newButton);
 			buttons.add(openButton);
 			
 			me.add(buttons);
 		}
  		if(scrollBar == null)	scrollBar = new CCScrollBar(this);
 		me.add(scrollBar);
 		me.add(treeControl);
     }
 
     public void setRect(int x, int y, int width, int height)
     {
 		super.setRect(x,y,width,height);
 		if(!didLayout) layout(false);
 		int wsb = (waba.sys.Vm.getPlatform().equals("WinCE"))?11:7;
 	
 		me.setRect(0,0, width, height);
 		Debug.println("Setting grid size: " + width + " " + height);
 		if(viewFromExternal){
 			treeControl.setRect(1,1,width-wsb-2, height-2);
 		}else{
 			int buttWidth = 35;
 			int choiceWidth = 65;
 			treeControl.setRect(1,19,width-wsb-2, height-20);
 			folderChoice.setRect(1,1,choiceWidth,17);
 			int buttonsWidth = width - 2 - choiceWidth - 1;
 			buttons.setRect(choiceWidth+1,1,buttonsWidth,17);
 			if(showDone){
 //				doneButton.setRect(buttonsWidth - 3 - buttWidth ,1,buttWidth,15);
 			}
 			int xStart = 1;
 			newButton.setRect(xStart,1,buttWidth - 10,15);
 			xStart += (buttWidth + 2 - 10);
 			openButton.setRect(xStart,1,buttWidth,15);
 		}
 		if(scrollBar != null){
 			waba.fx.Rect rT = treeControl.getRect();
 			scrollBar.setRect(width-wsb,rT.y,wsb, rT.height);
 		}
 		redesignScrollBar();
     }
 
     Dialog newDialog = null;
 
     public void onEvent(Event e)
     {
 		if(e.type == ControlEvent.PRESSED){
 		    TreeNode curNode;
 		    TreeNode parent;	   
 
 		    LabObject newObj;
 		    if(e.target == newButton){
 				newSelected();
 			} else if(e.target == openButton){
 				openSelected();
 		    } else if(e.target == doneButton){
 				if(container != null){
 			    	container.done(this);
 				}
 		    }else if(e.target == folderChoice){
 		    	if(pathTree != null){
 		    		int sel = folderChoice.getSelectedIndex();
 		    		if(sel < 0 || sel > pathTree.getCount() - 1) return;
 		    		TreeNode node = (TreeNode)pathTree.get(sel);
 		    		int numbToDelete =  sel;
 		    		if(numbToDelete > 0){
 		    			for(int i = 0; i < numbToDelete; i++){
 		    				pathTree.del(0);
 		    			}
 		    		}
 					LabObject obj = rootNode.getObj(node);
 					redefineFolderChoiceMenu();
 					if(obj instanceof LObjDictionary){
 						LObjDictionary d = (LObjDictionary)obj;
 						if(d.viewType == LObjDictionary.TREE_VIEW){
 							dict = d;
 							me.remove(treeControl);
 							treeModel = new TreeModel(new DictTreeNode(dict.getVisiblePtr(), session, dict.lBook));
 							treeControl = new TreeControl(treeModel);
 							treeControl.addTreeControlListener(this);
 							treeControl.showRoot(false);
 							me.add(treeControl);
 							waba.fx.Rect r = getRect();
 							setRect(r.x,r.y,r.width,r.height);
 						}else{
 							showPage(node,false);
 						}
 
 					}else if(node != null){
 						showPage(node, false);		
 		    		}
 		    	}
 		    }	    
 		} else if(e.type == TreeControl.DOUBLE_CLICK){
 			if(!viewFromExternal) openSelected();
 		} else if(e.type == ControlEvent.TIMER){
 			if(timer != null){
 				removeTimer(timer);
 				timer = null;
 				functionOnSelected(dialogFName, yieldID);
 			}
 		}
     }
 
 	public void newSelected()
 	{
 		String [] buttons = {"Cancel", "Create"};
 		newDialog = Dialog.showInputDialog( this, "Create", "Create a new Object",
 											buttons,Dialog.CHOICE_INP_DIALOG,
 											getMainView().getCreateNames());
 	}
 
 	public void openSelected(boolean edit)
 	{
 		TreeNode curNode;
 
 		curNode = treeControl.getSelected();
 		if(curNode == null || curNode.toString().equals("..empty..")) return;
 		showPage(curNode, edit);		
 	}
 
 	public void openSelected()
 	{
 		openSelected(false);
 	}
 
 	public LabObjectPtr insertAtSelected(LabObject obj)
 	{
 		TreeNode newNode = rootNode.getNode(obj);
 		LabObjectPtr newPtr = rootNode.getPtr(newNode);
 		insertAtSelected(newNode);		
 
 		return newPtr;
 		/*
 		 *  We shouldn't need this anymore
 		 */
 
 		/* This is a little hack
 		 * a commit just happened so this object 
 		 * is not "loaded" any more so if lBook.load()
 		 * is called attempting to get this object it will
 		 * create a second object.  So we use a special
 		 * case of reload to handle this.
 		 * this sticks the object back into the "loaded" 
 		 * list so it won't get loaded twice
 		 */
 
 		//		dict.lBook.reload(obj);
 	}
 
     public void insertAtSelected(TreeNode node)
     {
 		TreeNode curNode = treeControl.getSelected();
 		TreeNode parent = treeControl.getSelectedParent();
 		if(curNode == null){
 			treeModel.insertNodeInto(node, treeModel.getRoot(), treeModel.getRoot().getChildCount());
 		} else {
 			treeModel.insertNodeInto(node, parent, parent.getIndex(curNode)+1);
 		}
 		session.checkPoint();
     }
 
 	public void redesignScrollBar(){
 		if(scrollBar == null) return;
 		if(treeControl == null) return;
 		int allLines = treeControl.getAllLines();
 		int maxVisLine = treeControl.maxVisLines();
 		scrollBar.setMinMaxValues(0,allLines - maxVisLine);
 		scrollBar.setAreaValues(allLines,maxVisLine);
 		scrollBar.setIncValue(1);
 		scrollBar.setPageIncValue((int)(0.8f*maxVisLine+0.5f));
 		scrollBar.setRValueRect();
 		if(allLines > maxVisLine){
 			scrollBar.setValue(treeControl.firstLine);
 		}else{
 			treeControl.firstLine = 0;
 			scrollBar.setValue(0);
 		}
 		scrollBar.repaint();
 	}
 
     Dialog rnDialog = null;
 
     public void dialogClosed(DialogEvent e)
     {
 		String command = e.getActionCommand();
 		if(e.getSource() == newDialog){
 			if(command.equals("Create")){
 				String objType = (String)e.getInfo();
 				getMainView().createObj(objType, this);
 			}
 		} else if((rnDialog != null) && (e.getSource() == rnDialog)){
 			if(command.equals("Ok")){
 				// This is a bug
 	       
 				TreeNode selObj = treeControl.getSelected();
 				if(selObj == null){
 					dict.setName((String)e.getInfo());
 					return;
 				}
 
 				LabObject obj = rootNode.getObj(selObj);
 				if(obj != null){
 					obj.setName((String)e.getInfo());
 					obj.store();
 					session.checkPoint();
 				}
 
 				treeControl.reparse();
 				treeControl.repaint();
 			}
 		} else if(e.getSource() == propDialog){
 			// We should release the propDialog's session
 			
 			// and checkpoint our's
 			session.checkPoint();
 			treeControl.reparse();
 			treeControl.repaint();			
 		} else if(e.getSource() == confirmDialog){
 			if(command.equals(confirmYesStr)){
 				functionOnSelected(dialogFName, yieldID);
 			}
 			confirmDialog = null;
 		}
     }
 
     TreeNode clipboardNode = null;
 
     public void actionPerformed(ActionEvent e)
     {
 		String command;
 		Debug.println("Got action: " + e.getActionCommand());
 
 		functionOnSelected(e.getActionCommand(), 0);
 	}
 
 	String dialogFName = null;
 	Timer timer = null;
 	int yieldID = 0;
 	public void yield(String fName, int id)
 	{
 		yieldID = id;
 		dialogFName = fName;
 		timer = addTimer(50);
 	}
 
     public void functionOnSelected(String fName, int yieldID)
     {
 		TreeNode curNode = treeControl.getSelected();
 		DictTreeNode parent = (DictTreeNode)treeControl.getSelectedParent();
 
 		if(fName.equals("Cut")){
 			if(curNode == null || curNode.toString().equals("..empty..")) return;
 
 			clipboardNode = curNode;
 			treeModel.removeNodeFromParent(curNode, parent);
 		} else if(fName.equals("Copy")){
 			if(yieldID == 0){
				showWaitDialog("Copying selected");
 				yield(fName, 1);
 			} else {
 				LabObjectPtr curPtr = DictTreeNode.getPtr(curNode);
 				LabObjectPtr copyPtr = dict.lBook.copy(curPtr);
 				
 				if(copyPtr.objType == DefaultFactory.DICTIONARY){
 					clipboardNode = new DictTreeNode(copyPtr, session, dict.lBook);
 				} else {
 					clipboardNode = (TreeNode)copyPtr;
 				}
 				hideWaitDialog();
 			}
 		} else if(fName.equals("Paste")){
 			if(clipboardNode != null){
				insertAtSelected(clipboardNode);		    
 			}
 		} else if(fName.equals("Properties...")){
 			if(curNode == null || curNode.toString().equals("..empty..")) return;
 			showProperties(rootNode.getObj(curNode));
 		} else if(fName.equals("Toggle hidden")){
 			LObjDictionary.globalHide = !LObjDictionary.globalHide;
 			if(container != null) container.reload(this);
 		} else if(fName.equals("New..")){
 			newSelected();
 		} else if(fName.equals("Open")){
 			openSelected();
 		} else if(fName.equals("Beam selected")){
 			Catalog ccBeam = new Catalog("CCBeam.cCCB.appl", Catalog.READ_ONLY);
 			if(ccBeam.isOpen()){
 				ccBeam.close();
 			} else {
 				// This user doesn't have CCBeam installed
 				// if we could we should try to intall it for them
 				Dialog.showMessageDialog(null, "Beam Error",
 										 "You need to install CCBeam.",
 										 "Ok", Dialog.INFO_DIALOG);
 				return;
 			}
 
 			if(parent == null) return;
 
 			if(yieldID == 0){
				showWaitDialog("Preparing to beam selected");
 				yield(fName, 1);
 				return;
 			} else {
 				LabObject obj = parent.getObj(curNode);
 				LabBookCatalog lbCat = new LabBookCatalog(beamCatNameOut);
 			
 				dict.lBook.export(obj, lbCat);
 
 				lbCat.save();
 				lbCat.close();
 
 				hideWaitDialog();
 				waba.sys.Vm.exec("CCBeam", beamCatNameOut + "," +
 								 beamCatNameIn + "," +	
 								 "CCProbe," + 
 								 obj.getName(), 0, true);
 			
 				Catalog beamCat = new Catalog(beamCatNameOut + ".LaBk.DATA", Catalog.READ_WRITE);
 				if(beamCat.isOpen()){
 					beamCat.delete();
 				}
 			}
 		} else if(fName.equals("Receive")){
 			if(yieldID == 0){
				showWaitDialog("Importing received beam");
 				yield(fName, 1);
 				return;
 			} else {				
 				LabBookCatalog bmCat = new LabBookCatalog(beamCatNameIn);				
 
 				LabObject newObj = dict.lBook.importDB(bmCat);
 				bmCat.close();
 
 				if(newObj != null){
 					TreeNode newNode = rootNode.getNode(newObj);
 					insertAtSelected(newNode);
 				}			
 				Catalog beamCat = new Catalog(beamCatNameIn + ".LaBk.DATA", Catalog.READ_WRITE);
 				if(beamCat.isOpen()){
 					beamCat.delete();
 				}
 
 				hideWaitDialog();
 				return;
 			}
 		} else if(fName.equals("Rename..")){
 			if(curNode == null || curNode.toString().equals("..empty..")) return;
 
 			String [] buttons = {"Cancel", "Ok"};
 			rnDialog = Dialog.showInputDialog(this, "Rename Object", 
 											  "New Name:                ",
 											  buttons,Dialog.EDIT_INP_DIALOG,null,
 											  curNode.toString());
 		} else if(fName.equals("Send via FTP")){
 			/*
 			  TreeNode curNode = treeControl.getSelected();
 			  DictTreeNode parent = (DictTreeNode)treeControl.getSelectedParent();
 			  if(parent == null) return;
 			  
 			  LabObject obj = parent.getObj(curNode);
 			  
 			  LabBookFile lbFile = new LabBookFile("LabObj-" + username);
 			  
 			  dict.lBook.export(obj, lbFile);
 			  lbFile.save();
 			  lbFile.close();
 			  
 			  ftpSend("LabObj-" + username);
 			  
 			  File objFile = new File("LabObj-" + username, File.DONT_OPEN);
 			  if(objFile.exists()){
 			  objFile.delete();
 			  }
 			*/
 		} else if(fName.equals("FTP settings..")){
 
 		} else if(fName.equals("Import..")){
 			FileDialog fd = FileDialog.getFileDialog(FileDialog.FILE_LOAD, null);
 				
 			fd.show();
 
 			LabBookDB imDB = LObjDatabaseRef.getDatabase(fd.getFilePath());
 			
 			if(imDB == null) return;
 
 			LabObject newObj = dict.lBook.importDB(imDB);
 			imDB.close();
 			
 			if(newObj != null){
 				TreeNode newNode = rootNode.getNode(newObj);
 				insertAtSelected(newNode);
 			}
 
 		} else if(fName.equals("Export..")){
 			if(waba.sys.Vm.getPlatform().equals("PalmOS")){
 				dict.lBook.export(null, null);
 			} else {
 				if(parent == null) return;
 				
 				LabObject obj = parent.getObj(curNode);
 				
 				FileDialog fd = FileDialog.getFileDialog(FileDialog.FILE_SAVE, null);
 				String fileName = null;
 				if(fd != null){
 					fd.setFile(obj.getName());
 					fd.show();
 					fileName = fd.getFilePath();
 				} else {
 					fileName = "LabObj-export";
 				}
 				
 				LabBookFile lbFile = new LabBookFile(fileName);
 				dict.lBook.export(obj, lbFile);
 				lbFile.save();
 				lbFile.close();
 			}
 		} else if(fName.equals("Delete")){
 			if(curNode == null || curNode.toString().equals("..empty..")) return;
 			if(yieldID == 0){
 				showConfirmDialog("Are you sure you| " +
 								  "want to delete:| " + 
 								  curNode.toString(), 
 								  "Delete");
 				this.dialogFName = fName;
 				this.yieldID = 1;
 				return;
 			} else {			
 				treeModel.removeNodeFromParent(curNode, parent);
 			}
 		}
 	}
 
 	Dialog waitDialog = null;
 	Dialog confirmDialog = null;
 	String confirmYesStr = null;
 	public void showWaitDialog(String message)
 	{
 		waitDialog = Dialog.showMessageDialog(null, "Please Wait..",
 											  message,
 											  "Cancel", Dialog.INFO_DIALOG);
 	}
 	public void hideWaitDialog()
 	{
 		if(waitDialog != null) waitDialog.hide();
 		waitDialog = null;
 	}
 
 	public void showConfirmDialog(String message, String yesButton)
 	{
 		String [] buttons = {yesButton, "Cancel"};
 		confirmYesStr = yesButton;
 		confirmDialog = Dialog.showConfirmDialog(this, "Confirmation", message,
 												 buttons, Dialog.QUEST_DIALOG);
 	}
 
 	public void checkForBeam()
 	{
 		// First see if we've got a bogus out db sitting around
 		Catalog beamCat = new Catalog(beamCatNameOut + ".LaBk.DATA", Catalog.READ_ONLY);
 		if(beamCat.isOpen()){
 			// cross our fingers and hope this helps.
 			// if we are here it means we crashed during export
 			// this means the LabBook was left open and so was
 			// this database.  I don't know if deleting it will help
 			beamCat.delete();
 		}
 		
 		beamCat = new Catalog(beamCatNameIn + ".LaBk.DATA", Catalog.READ_ONLY);
 		if(!beamCat.isOpen()){
 			return;
 		} else {
 			beamCat.close();
 		}
 		
 		functionOnSelected("Receive", 0);
 	}
 
 	public void ftpSend(String fileName)
 	{
 		/*
 		ScreenWriter sWriter = new ScreenWriter(createGraphics(), getFontMetrics(MainWindow.defaultFont));
 
 		// connect and test supplying port no.
 		FTPClient ftp = new FTPClient(ftpServer, 21, sWriter);
 		
 		byte [] testBuf = { (byte)'t', (byte)'e', (byte)'s', (byte)'t',
 							(byte)'\r', (byte)'\n',};
 
 		int errorCode = ftp.getError();
 		if(errorCode != 0){
 			sWriter.println("opening error num: " + errorCode);
 			return;
 		}
 
 		if(!ftp.login(username, password)){
 			sWriter.println("login error num: " + ftp.getError() +
 							" str: " + ftp.getErrorStr());
 			return;
 		}
 
 		// do binary by default
 		ftp.setType(FTPTransferType.BINARY);
 
 		// change dir
 		ftp.chdir(ftpDirectory);
 
 		// put a local file to remote host
 		ftp.put(fileName, fileName);
 
 		ftp.quit();		
 		*/
 	}
 
     public void showPage(TreeNode curNode, boolean edit)
     {
 		LabObject obj = null;
 		
 		obj = rootNode.getObj(curNode);
 		if(obj instanceof LObjDictionary &&
 		   ((LObjDictionary)obj).viewType == LObjDictionary.TREE_VIEW){
 			if(pathTree == null){
 				pathTree = new waba.util.Vector();
 			}
 			TreeLine selLine = treeControl.getSelectedLine();
 			
 			int currIndex = 0;
 			if(pathTree.getCount() > 0) pathTree.del(0);
 			while(selLine != null){
 				TreeNode node = selLine.getNode();
 				if(node != null){
 					pathTree.insert(currIndex++,node);
 				}
 				selLine = selLine.getLineParent();
 			}
 			pathTree.insert(0,curNode);
 			redefineFolderChoiceMenu();
 //			folderChoice.repaint();
 			LObjDictionary d = (LObjDictionary)obj;
 
 			dict = d;
 			me.remove(treeControl);
 			treeModel = new TreeModel(new DictTreeNode(dict.getVisiblePtr(), session, dict.lBook));
 			treeControl = new TreeControl(treeModel);
 			treeControl.addTreeControlListener(this);
 			treeControl.showRoot(false);
 			me.add(treeControl);
 			waba.fx.Rect r = getRect();
 			setRect(r.x,r.y,r.width,r.height);
 			return;
 		}
 		
 		DictTreeNode parent = (DictTreeNode)treeControl.getSelectedParent();
 		if(parent == null) parent = rootNode;
 
 		getMainView().showFullWindowObj(edit, parent.getDict(), rootNode.getPtr(curNode));	
 		if(session != null) session.release();
 	}
 
 	public void updateWindow()
 	{
 		// release everything
 		session.release();
 
 		// reload our main dictionary
 		dict = (LObjDictionary)session.load(dictPtr);
 		setLabObject((LabObject)dict);
 
 		// we should refresh the display
 		treeControl.reparse();
 		treeControl.repaint();
 	}
 
 	public void redefineFolderChoiceMenu(){
 		if(folderChoice != null) me.remove(folderChoice);
 		folderChoice = new Choice();
 		if(pathTree == null){
 			folderChoice.add(ROOT_TREE_NODE_NAME);
 		}else{
 			for(int n = 0; n < pathTree.getCount(); n++){
 				folderChoice.add(pathTree.get(n).toString());
 			}
 		}
 		me.add(folderChoice);
 	}
 
 	ViewDialog propDialog = null;
 
 	public void showProperties(LabObject obj)
 	{
 		if(obj == null) return;
 		LabObjectView propView = obj.getPropertyView(null, session);
 		if(propView == null) return;
 		MainWindow mw = MainWindow.getMainWindow();
 		if(!(mw instanceof ExtraMainWindow)) return;
 		ViewDialog vDialog = new ViewDialog((ExtraMainWindow)mw, this, "Properties", propView);
 		propDialog = vDialog;
 		vDialog.setRect(0,0,150,150);
 		vDialog.show();		
 	}
 
 	boolean addedMenus = false;
 	public void setShowMenus(boolean state)
 	{
 		if(!showMenus && state){
 			// our container wants us to show our menus
 			showMenus = true;
 			addMenus();
 		} else if(showMenus && !state){
 			// out container wants us to remove our menus
 			showMenus = false;
 			if(addedMenus) delMenus();
 		}
 	}
 
 	public void addMenus()
 	{
 		
 		if(waba.sys.Vm.getPlatform().equals("PalmOS")){
 			fileStrings = palmFileStrings;
 		}
 		
 		if(editMenu != null) getMainView().addMenu(this, editMenu);
 		getMainView().addFileMenuItems(fileStrings, this);
 
 		addedMenus = true;
 	}
 
 	public void delMenus()
 	{
 		
 		
 		if(editMenu != null) getMainView().delMenu(this,editMenu);
 		getMainView().removeFileMenuItems(fileStrings, this);
 		addedMenus = false;
 	}
 
 	public MainView getMainView()
 	{
 		if(container != null) return container.getMainView();
 		return null;
 	}
 
     public void close()
     {
 		if(scrollBar != null) scrollBar.close();
 		super.close();
 		// Commit ???
 		// Store ??
     }
 
 	public void scrollValueChanged(ScrollEvent se){
 		if(se.target != scrollBar) return;
 		int value = se.getScrollValue();
 		treeControl.firstLine = value;
 		treeControl.repaint();
 	}
 	public void treeControlChanged(TreeControlEvent ev){
 		redesignScrollBar();
 	}
 	
 	
 /*
 	public void onPaint(waba.fx.Graphics g){
 		if(g == null) return;
 		g.fillRect(0,0,width,height);
 	}
 */
 }
