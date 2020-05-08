 package org.concord.LabBook;
 
 import extra.util.*;
 import org.concord.waba.extra.event.*;
 import org.concord.waba.extra.ui.*;
 import waba.ui.*;
 
 public class ViewDialog extends Dialog
 	implements ViewContainer, MainView
 {
 	ExtraMainWindow owner = null;
 	LabObjectView view = null;
 	DialogListener listener = null;
 
 	public ViewDialog(ExtraMainWindow owner,DialogListener l,String title, 
 						  LabObjectView view)
 	{
 		super(title);
 		this.view = view;
 		this.owner = owner;
 		addDialogListener(l);
 		owner.setDialog(this);
 		listener = l;
 		if(view != null) view.setContainer(this);
 	}
 
 	public void setContent(){
  		waba.fx.Rect cRect = getContentPane().getRect();
 		if(view != null){
 			view.layout(true);
 			view.setRect(0, 0, cRect.width, cRect.height);
 			getContentPane().add(view);
 		}
 	}
 
 	public void onEvent(Event e){}
 
 	public MainView getMainView()
 	{
 		return this;
 	}
 
     public void done(LabObjectView source)
 	{
 		if(source != null) source.close();
 		hide();
 		if(owner != null) owner.setDialog(null);
 		if(listener != null) listener.dialogClosed(new DialogEvent(this,null,null,null,0));
 	}
 
     public void reload(LabObjectView source)
 	{
 		if(source != view) Debug.println("Error source being removed");
 
 		LabObject obj = source.getLabObject();
 		source.close();
 		getContentPane().remove(source);
 
 		LabObjectView replacement = obj.getView(this, true);
  		waba.fx.Rect cRect = getContentPane().getRect();
 		replacement.layout(true);
 		replacement.setRect(0, 0, cRect.width, cRect.height);
 		getContentPane().add(replacement);
 		view = replacement;
 	}
 
     public void addMenu(LabObjectView source, Menu menu){}
     public void delMenu(LabObjectView source, Menu menu){}
 	public void addFileMenuItems(String [] items, ActionListener source){}
 	public void removeFileMenuItems(String [] items, ActionListener source){}
 	public String [] getCreateNames(){return null;}
 	public void createObj(String name, LObjDictionaryView dView){}
 	public void showFullWindowView(LabObjectView view){}
 }
