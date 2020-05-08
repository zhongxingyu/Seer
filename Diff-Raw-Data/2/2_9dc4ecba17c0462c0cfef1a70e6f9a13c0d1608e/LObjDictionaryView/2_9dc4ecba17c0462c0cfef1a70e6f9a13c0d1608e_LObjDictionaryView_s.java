 package org.concord.LabBook;
 
 import waba.ui.*;
 import org.concord.waba.extra.ui.*;
 import org.concord.waba.extra.event.*;
 import extra.util.*;
 import extra.ui.*;
 
 public class LObjDictionaryView extends LabObjectView 
     implements ActionListener, DialogListener, ScrollListener, TreeControlListener
 {
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
     Button editButton = new Button("Edit");
     Button delButton = new Button("Del");
     
     Choice  folderChoice;
 
     Menu editMenu = new Menu("Edit");
 
     boolean editStatus = false;
 
 	String [] fileStrings = {"New..", "Open", "Rename..", "Import..", "Export..", "Delete"};
 	String [] palmFileStrings = {"New..", "Open", "Rename..", "Delete"};
 
 
 	CCScrollBar				scrollBar;
 	waba.util.Vector 		pathTree;
 
     public LObjDictionaryView(ViewContainer vc, LObjDictionary d,
 							  LabBookSession session)
     {
 		super(vc, (LabObject)d, session);
 		dict = d;
 
 		dictPtr = dict.getVisiblePtr();
 
 		add(me);
 		editMenu.add("Cut");
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
 
 		rootNode = new DictTreeNode(dict, session, dict.lBook);
 		treeModel = new TreeModel(rootNode);
 		treeControl = new TreeControl(treeModel);
 		treeControl.addTreeControlListener(this);
 		treeControl.showRoot(false);
 		me.add(treeControl);
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
 			buttons.add(delButton);
 			
 			me.add(buttons);
 		}
  		if(scrollBar == null)	scrollBar = new CCScrollBar(this);
 		me.add(scrollBar);
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
 			int choiceWidth = 40;
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
 			xStart += (buttWidth + 2);
 			delButton.setRect(xStart,1,buttWidth - 10,15);
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
 			} else if(e.target == delButton){
 				delSelected();
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
 							treeModel = new TreeModel(new DictTreeNode(dict, session, dict.lBook));
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
 		}
     }
 
 	public void newSelected()
 	{
 		String [] buttons = {"Cancel", "Create"};
 		newDialog = Dialog.showInputDialog( this, "Create", "Create a new Object",
 											buttons,Dialog.CHOICE_INP_DIALOG,
 											getMainView().getCreateNames());
 	}
 
 	public void delSelected()
 	{
 		TreeNode curNode;
 		TreeNode parent;	   
 
 		curNode = treeControl.getSelected();
 		if(curNode == null || curNode.toString().equals("..empty..")) return;
 		parent = treeControl.getSelectedParent();
 		treeModel.removeNodeFromParent(curNode, parent);
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
 		repaint();
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
 		}
     }
 
     TreeNode clipboardNode = null;
 
     public void actionPerformed(ActionEvent e)
     {
 		String command;
 		Debug.println("Got action: " + e.getActionCommand());
 
 		if(e.getSource() == editMenu){	    
 			if(e.getActionCommand().equals("Cut")){
 				TreeNode curNode = treeControl.getSelected();
 				if(curNode == null || curNode.toString().equals("..empty..")) return;
 				TreeNode parent = treeControl.getSelectedParent();
 				clipboardNode = curNode;
 				treeModel.removeNodeFromParent(curNode, parent);
 		    } else if(e.getActionCommand().equals("Paste")){
 				if(clipboardNode != null){
 				    insertAtSelected(clipboardNode);		    
 				}
 		    } else if(e.getActionCommand().equals("Properties...")){
 				TreeNode curNode = treeControl.getSelected();
 				if(curNode == null || curNode.toString().equals("..empty..")) return;
 				showProperties(rootNode.getObj(curNode));
 		    } else if(e.getActionCommand().equals("Toggle hidden")){
 				LObjDictionary.globalHide = !LObjDictionary.globalHide;
 				if(container != null) container.reload(this);
 
 		    }
 		} else {
 			// should be the file menu
 			if(e.getActionCommand().equals("New..")){
 				newSelected();
 			} else if(e.getActionCommand().equals("Open")){
 				openSelected();
 		    } else if(e.getActionCommand().equals("Rename..")){
 				TreeNode selObj = treeControl.getSelected();
 				String [] buttons = {"Cancel", "Ok"};
 				if(selObj != null){
 				    if(selObj.toString().equals("..empty..")) return;
 				    rnDialog = Dialog.showInputDialog(this, "Rename Object", 
 													  "New Name:                ",
 													  buttons,Dialog.EDIT_INP_DIALOG,null,
 													  selObj.toString());
 				} else {
 					waba.fx.Sound.beep();
 				}
 		    } else if(e.getActionCommand().equals("Import..")){
 				FileDialog fd = FileDialog.getFileDialog(FileDialog.FILE_LOAD, null);
 
 				fd.show();
 
 				if(fd.getFilePath() == null) return;
 
 				LabBookFile imFile = new LabBookFile(fd.getFilePath());
 
 				LabObject newObj = dict.lBook.importDB(imFile);
 				imFile.close();
 
 				if(newObj != null){
 				    TreeNode newNode = rootNode.getNode(newObj);
 				    insertAtSelected(newNode);
 				}
 
 		    } else if(e.getActionCommand().equals("Export..")){
 				if(waba.sys.Vm.getPlatform().equals("PalmOS")){
 				    dict.lBook.export(null, null);
 				} else {
 				    TreeNode curNode = treeControl.getSelected();
 				    DictTreeNode parent = (DictTreeNode)treeControl.getSelectedParent();
 				    if(parent == null) return;
 				    
 				    LabObject obj = parent.getObj(curNode);
 				    
 				    FileDialog fd = FileDialog.getFileDialog(FileDialog.FILE_SAVE, null);
 				    fd.setFile(obj.getName());
 				    fd.show();
 
 				    LabBookFile lbFile = new LabBookFile(fd.getFilePath());
 				    dict.lBook.export(obj, lbFile);
 				    lbFile.save();
 				    lbFile.close();
 				}
 		    } else if(e.getActionCommand().equals("Delete")){
 				delSelected();
 			}
 		}
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
 			treeModel = new TreeModel(new DictTreeNode(dict, session, dict.lBook));
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
		session.release();
 	}
 
 	public void updateWindow()
 	{
 		// release everything
 		session.release();
 
 		// reload our main dictionary
 		dict = (LObjDictionary)session.load(dictPtr);
 		setLabObject((LabObject)dict);
 
 		// we should refresh the display
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
 		repaint();
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
