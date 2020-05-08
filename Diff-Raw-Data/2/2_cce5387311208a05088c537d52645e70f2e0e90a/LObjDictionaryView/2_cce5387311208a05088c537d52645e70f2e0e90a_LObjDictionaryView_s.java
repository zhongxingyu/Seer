 package org.concord.LabBook;
 
 import waba.ui.*;
 import org.concord.waba.extra.ui.*;
 import org.concord.waba.extra.event.*;
 import extra.util.*;
 import extra.ui.*;
 
 public class LObjDictionaryView extends LabObjectView 
     implements ActionListener, ViewContainer, DialogListener
 {
 	public		boolean viewFromExternal = false;
     TreeControl treeControl;
     TreeModel treeModel;
     RelativeContainer me = new RelativeContainer();
     LabObjectView lObjView = null;
 
     int newIndex = 0;
 
     LObjDictionary dict;
     GridContainer buttons = null; 
  
     Button doneButton = new Button("Done");
     Button newButton = new Button("New");
     Button openButton = new Button("Open");
     Button editButton = new Button("Edit");
     Button delButton = new Button("Del");
 
     Menu editMenu = new Menu("Edit");
     Menu viewMenu = new Menu("View");
 
     boolean editStatus = false;
 
 	String [] fileStrings = {"New..", "Open", "Rename..", "Import..", "Export..", "Delete"};
 	String [] palmFileStrings = {"New..", "Open", "Rename..", "Delete"};
 
     public LObjDictionaryView(ViewContainer vc, LObjDictionary d)
     {
 		super(vc);
 		dict = d;
 		lObj =dict;
 		add(me);
 		editMenu.add("Cut");
 		editMenu.add("Paste");
 		editMenu.add("Properties...");
 		editMenu.add("Toggle hidden");
 		viewMenu.add("Paging View");
 		editMenu.addActionListener(this);
 		viewMenu.addActionListener(this);
     }
 
     public void layout(boolean sDone)
     {
 		if(didLayout) return;
 		didLayout = true;
 
 		showDone = sDone;
 
 		treeModel = new TreeModel(dict);
 		treeControl = new TreeControl(treeModel);
 		treeControl.showRoot(false);
 		me.add(treeControl);
 
 		if(showDone){
 			buttons = new GridContainer(4,1);
 			buttons.add(doneButton, 3, 0);
 		} else {
 			buttons = new GridContainer(3,1);
 		}
 		if(!viewFromExternal){
 			buttons.add(newButton, 0, 0);
 			buttons.add(openButton, 1, 0);
 			buttons.add(delButton, 2, 0);
 		}
 		me.add(buttons);
     }
 
     public void setRect(int x, int y, int width, int height)
     {
 		super.setRect(x,y,width,height);
 		if(!didLayout) layout(false);
 	
 		me.setRect(0,0, width, height);
 		treeControl.setRect(1,1,width-2, height-22);
 		Debug.println("Setting grid size: " + width + " " + height);
 		buttons.setRect(0,height-20,width,20);
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
 
 	public void openSelected()
 	{
 		TreeNode curNode;
 
 		curNode = treeControl.getSelected();
 		if(curNode == null || curNode.toString().equals("..empty..")) return;
 		showPage(curNode, false);		
 	}
 
 	public void insertAtSelected(LabObject obj)
 	{
 		insertAtSelected(dict.getNode(obj));		
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
 		dict.lBook.commit();
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
 		} else if(e.getSource() == rnDialog){
 			if(command.equals("Ok")){
 				// This is a bug
 	       
 				TreeNode selObj = treeControl.getSelected();
 				if(selObj == null){
 					dict.name = (String)e.getInfo();
 					return;
 				}
 
 				LabObject obj = dict.getObj(selObj);
 				if(obj != null){
 					obj.name = (String)e.getInfo();
 					obj.store();
 					if(!dict.lBook.commit()){
 						// error (what to do)
 						return;
 					}
 				}
 
 				treeControl.reparse();
 				treeControl.repaint();
 			}
 		}		   
     }
 
     TreeNode clipboardNode = null;
 
     public void actionPerformed(ActionEvent e)
     {
 		String command;
 		Debug.println("Got action: " + e.getActionCommand());
 
 		if(e.getSource() == lObjView){
 	    	if(e.getActionCommand().equals("Done")){
 				done(lObjView);
 	    	}
 		} else if(e.getSource() == viewMenu){
 	    	if(e.getActionCommand().equals("Paging View")){
 				dict.viewType = dict.PAGING_VIEW;
 				if(container != null){
 		    		container.reload(this);
 				}
 	    	}
 		} else if(e.getSource() == editMenu){	    
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
 				showProperties(dict.getObj(curNode));
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
 				    rnDialog = Dialog.showInputDialog(this, "Rename Object", "Old Name was " + selObj.toString(),
 								      buttons,Dialog.EDIT_INP_DIALOG,null,selObj.toString());
 				} else {
 				    rnDialog = Dialog.showInputDialog(this, "Rename Parent", "Old Name was " + dict.name,
 								      buttons,Dialog.EDIT_INP_DIALOG,null,selObj.toString());
 				}
 		    } else if(e.getActionCommand().equals("Import..")){
 				FileDialog fd = FileDialog.getFileDialog(FileDialog.FILE_LOAD, null);
 
 				fd.show();
 
 				if(fd.getFilePath() == null) return;
 
 				LabBookFile imFile = new LabBookFile(fd.getFilePath());
 
 				LabObject newObj = dict.lBook.importDB(imFile);
 				imFile.close();
 
 				if(newObj != null){
 				    TreeNode newNode = dict.getNode(newObj);
 				    insertAtSelected(newNode);
 				}
 
 		    } else if(e.getActionCommand().equals("Export..")){
 				if(waba.sys.Vm.getPlatform().equals("PalmOS")){
 				    dict.lBook.export(null, null);
 				} else {
 				    TreeNode curNode = treeControl.getSelected();
 				    LObjDictionary parent = (LObjDictionary)treeControl.getSelectedParent();
 				    if(parent == null) return;
 				    
 				    LabObject obj = parent.getObj(curNode);
 				    
 				    FileDialog fd = FileDialog.getFileDialog(FileDialog.FILE_SAVE, null);
 				    fd.setFile(obj.name);
 				    fd.show();
 
 				    LabBookFile lbFile = new LabBookFile(fd.getFilePath());
 				    dict.lBook.export(obj.ptr, lbFile);
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
 		
 		obj = dict.getObj(curNode);
 		if(obj == null) Debug.println("showPage: object not in database: " +
 					      ((LabObjectPtr)curNode).debug());
 		showPage(obj,edit);
 	}
 
 	public void showProperties(LabObject obj)
 	{
 		if(obj == null) return;
 		LabObjectView propView = obj.getPropertyView(null, (LObjDictionary)treeControl.getSelectedParent());
 		if(propView == null) return;
 		MainWindow mw = MainWindow.getMainWindow();
 		if(!(mw instanceof ExtraMainWindow)) return;
 		ViewDialog vDialog = new ViewDialog((ExtraMainWindow)mw, null, "Properties", propView);
 		vDialog.setRect(0,0,150,150);
 		vDialog.show();
 	}
 
 	public void showPage(LabObject obj, boolean edit)
 	{
 		editStatus = edit;
		lObjView = obj.getPropertyView(this, (LObjDictionary)treeControl.getSelectedParent());
 
 		if(lObjView == null){
 		    return;
 		}
 
 		dict.store();
 
 		if(addedMenus) delMenus();
 		remove(me);
 	    lObjView.layout(true);
 		lObjView.setRect(x,y,width,height);
 		lObjView.setShowMenus(showMenus);
 		add(lObjView);
     }
 
 	boolean addedMenus = false;
 	public void setShowMenus(boolean state)
 	{
 		if(!showMenus && state){
 			// our container wants us to show our menus
 			showMenus = true;
 			addMenus();
 			if(lObjView != null) lObjView.setShowMenus(state);
 		} else if(showMenus && !state){
 			// out container wants us to remove our menus
 			showMenus = false;
 			if(addedMenus) delMenus();
 			if(lObjView != null) lObjView.setShowMenus(state);
 		}
 	}
 
 	public void addMenus()
 	{
 		if(container == null) return;
 		
 		if(waba.sys.Vm.getPlatform().equals("PalmOS")){
 			fileStrings = palmFileStrings;
 		}
 		
 		if(editMenu != null) getMainView().addMenu(this, editMenu);
 		if(viewMenu != null) getMainView().addMenu(this, viewMenu);
 		getMainView().addFileMenuItems(fileStrings, this);
 
 		addedMenus = true;
 	}
 
 	public void delMenus()
 	{
 		if(container != null){
 			if(editMenu != null) container.getMainView().delMenu(this,editMenu);
 			if(viewMenu != null) container.getMainView().delMenu(this,viewMenu);
 			container.getMainView().removeFileMenuItems(fileStrings, this);
 			addedMenus = false;
 		}		
 	}
 
 	public MainView getMainView()
 	{
 		if(container != null) return container.getMainView();
 		return null;
 	}
 
     public void done(LabObjectView source)
     {
 		if(source == lObjView){
 			lObjView.close();
 	    
 			// I'm trying to have all objects be responsible for 
 			// their own storing now.
 			// dict.lBook.store(lObjView.lObj);
 
 			// I might want to do a commit here lets try it....
 			// of course if we are embedded this might be a problem
 			if(!dict.lBook.commit()){
 				// error (what to do)
 				return;
 			}
 
 			remove(lObjView);
 			treeControl.reparse();
 			add(me);
 			if(showMenus) addMenus();
 			lObjView = null;
 		}
 		//	System.gc();
     }
 
     public void reload(LabObjectView source)
     {
 		if(source == lObjView){
 			LabObject obj = source.getLabObject();
 			lObjView.setShowMenus(showMenus);
 			lObjView.close();
 			remove(lObjView);
 	    
 			lObjView = obj.getView(this, editStatus, (LObjDictionary)treeControl.getSelectedParent());
 			lObjView.layout(true);
 			lObjView.setRect(x,y,width,height);
 			lObjView.setShowMenus(showMenus);
 			add(lObjView);
 		}
     }
 
     public void close()
     {
 		if(lObjView != null){
 			lObjView.close();
 		}
 
 		super.close();
 		// Commit ???
 		// Store ??
     }
 
 }
