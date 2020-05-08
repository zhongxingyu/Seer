 package org.concord.LabBook;
 
 import waba.ui.*;
 import waba.util.*;
 import extra.io.*;
 import org.concord.waba.extra.ui.*;
 
 public abstract class LabObject
 {
     public String type;
     public String name = null;
     public LabObjectPtr ptr;
     int objectType = -1;
 	public LabObjectFactory factory;
     public static LabBook lBook;
 
 	public LabObject(int type)
 	{
 		objectType = type;
 	}
 
 	public void init(){}
 
     public void readExternal(DataStream in)
     {
 		name = in.readString();
 		if(name.equals("_")) name = null;
 		//	Debug.println("Reading " + name + " " + typeNames[objectType]);
 
     }
 
     public void writeExternal(DataStream out)
     {
 		if(name == null){
 			out.writeString("_");
 			//Debug.println("Writing noname " + typeNames[objectType]);
 		} else {
 			//Debug.println("Writing " + name + " " + typeNames[objectType]);
 			out.writeString(name);
 		}
 
     }
 
     public boolean equals(TreeNode node)
     {
	if(node == this) return true;
	if(node instanceof LabObject){	    
	    LabObject obj = (LabObject)node;
	    return obj.ptr != null && ptr != null &&
		ptr.equals(obj.ptr);
	}

	return false;
     }
     public LabObjectView getView(ViewContainer vc, boolean edit, 
 				 				 LObjDictionary curDict, boolean embeddedState){
 		LabObjectView view = getView(vc,edit,curDict);
 		if(view != null) view.setEmbeddedState(embeddedState);
 		
 		return view;
 	}
 	  
     public LabObjectView getView(ViewContainer vc, boolean edit)    
     {
 		return getView(vc, edit, null);
     }
 
     public LabObjectView getView(ViewContainer vc, boolean edit, 
 				 				 LObjDictionary curDict){
 		return null;
     }
 
 	public LabObjectView getMinimizedView()
 	{
 		return new LObjMinimizedView(this);
 	}
 
     public LabObjectView getPropertyView(ViewContainer vc)    
     {
 		return getPropertyView(vc, null);
     }
 
     public LabObjectView getPropertyView(ViewContainer vc,LObjDictionary curDict){
 		return null;
     }
 
     public LabObject copy(){return null;}
 
     public void store(){lBook.store(this);}
 
 	public void storeNow()
 	{
 		LabObjectPtr ptr = lBook.store(this);
 		if(ptr != null){
 			lBook.commit(ptr);
 		}
 	}
 
 	Vector objListeners = null;
 	public void addLabObjListener(LabObjListener l)
 	{
 		if(objListeners == null) objListeners = new Vector();
 		objListeners.add(l);
 	}
 
 	public void delLabObjListener(LabObjListener l)
 	{
 		int index = objListeners.find(l);
 		if(index >= 0) objListeners.del(index);
 	}
 
 	public void notifyObjListeners(LabObjEvent e)
 	{
 		if(objListeners == null) return;
 		for(int i=0; i<objListeners.getCount(); i++){
 			LabObjListener l = (LabObjListener)objListeners.get(i);
 			if(l != null) l.labObjChanged(e);
 		}
 	}
 }
