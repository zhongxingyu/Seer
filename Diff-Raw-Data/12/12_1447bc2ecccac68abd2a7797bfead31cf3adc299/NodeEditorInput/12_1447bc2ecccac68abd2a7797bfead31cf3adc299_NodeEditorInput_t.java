 /*
  *
  *  JMoney - A Personal Finance Manager
  *  Copyright (c) 2004 Johann Gyger <jgyger@users.sf.net>
  *
  *
  *  This program is free software; you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation; either version 2 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program; if not, write to the Free Software
  *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
  *
  */
 
 package net.sf.jmoney.views;
 
 import java.util.Vector;
 
 import net.sf.jmoney.IBookkeepingPage;
 import net.sf.jmoney.JMoneyPlugin;
 import net.sf.jmoney.model2.Account;
 import net.sf.jmoney.model2.DatastoreManager;
 import net.sf.jmoney.model2.PageEntry;
 
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IMemento;
 import org.eclipse.ui.IPersistableElement;
 
 /**
  * TODO
  * 
  * @author Johann Gyger
  */
 public class NodeEditorInput implements IEditorInput, IPersistableElement {
 
     protected Object nodeObject;
     protected String label;
     protected Image image;
     
     protected Vector<PageEntry> pageFactories;
 
     protected IMemento memento;
     
     // Set when addPages called.
     IBookkeepingPage pages [];
     
     /**
      * Create a new editor input.
      * 
      * @param nodeObject An extendable object on which this input is based
      */
     public NodeEditorInput(Object nodeObject, String label, Image image, Vector<PageEntry> pageFactories, IMemento memento) {
         this.nodeObject = nodeObject;
         this.label = label;
         this.image = image;
         this.pageFactories = pageFactories;
         this.memento = memento;
     }
 
     /* (non-Javadoc)
      * @see org.eclipse.ui.IEditorInput#exists()
      */
     public boolean exists() {
         return false;
     }
 
     /* (non-Javadoc)
      * @see org.eclipse.ui.IEditorInput#getImageDescriptor()
      */
     public ImageDescriptor getImageDescriptor() {
     	// This method is never called.
     	// TODO: figure out when this method is supposed to be called
     	// and what we should return here.
     	return null;
     }
 
     /* (non-Javadoc)
      * @see org.eclipse.ui.IEditorInput#getName()
      */
     public String getName() {
     	return label;
     }
     
     // Or should we be using getImageDescriptor???
     // Because the editor input is created only when the editor is created,
     // this is not a performance issue.
     public Image getImage() {
     	return image;
     }
     
     /* (non-Javadoc)
      * @see org.eclipse.ui.IEditorInput#getPersistable()
      */
     public IPersistableElement getPersistable() {
         // This class implements the IPersistableElement
     	// methods, so return a pointer to this object.
         return this;
     }
 
     /* (non-Javadoc)
      * @see org.eclipse.ui.IEditorInput#getToolTipText()
      */
     public String getToolTipText() {
 		if (nodeObject instanceof TreeNode) {
 			return ((TreeNode)nodeObject).getId();
 		} else if (nodeObject instanceof Account) {
 			return ((Account)nodeObject).getFullAccountName();
 		} else {
 	        return "tool tip text to distinquish pages with same name";
 		}
     }
 
     /* (non-Javadoc)
      * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
      */
     public Object getAdapter(Class adapter) {
         // TODO Auto-generated method stub
         return null;
     }
 
     /* (non-Javadoc)
      * @see java.lang.Object#equals(java.lang.Object)
      */
     public boolean equals(Object obj) {
         if (obj == this) return true;
 
         if (obj instanceof NodeEditorInput) {
             NodeEditorInput input = (NodeEditorInput) obj;
             return nodeObject.equals(input.nodeObject);
         }
 
         return false;
     }
 
     /* (non-Javadoc)
      * @see java.lang.Object#hashCode()
      */
     public int hashCode() {
         return nodeObject.hashCode();
     }
 
     /**
      * @return The input object for this editor
      */
     public Object getNode() {
         return nodeObject;
     }
 
    public Vector<PageEntry> getPageListeners() {
         return pageFactories;
     }
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ui.IPersistableElement#getFactoryId()
 	 */
 	public String getFactoryId() {
 		return "net.sf.jmoney.nodeEditor";
 	}
 
 	/**
 	 * @param memento
 	 */
 	public void saveState(IMemento memento) {
 		// Views and editors can be restored in any order, and
 		// all must be able to be restored independently of the
 		// others.  Therefore the session memento must be saved in the memento 
 		// for every view and editor.
 		
     	// Save the details of the session.
     	DatastoreManager sessionManager = JMoneyPlugin.getDefault().getSessionManager();
 		if (sessionManager != null) {
 			IMemento sessionMemento = memento.createChild("session");
 			IPersistableElement pe = (IPersistableElement)sessionManager.getAdapter(IPersistableElement.class);
 			sessionMemento.putString("currentSessionFactoryId", pe.getFactoryId());
 			pe.saveState(sessionMemento.createChild("currentSession"));
 		}
 		
 		// Save the node.  The node may be either
 		// a TreeNode object or an object in the data model.
 		if (nodeObject instanceof TreeNode) {
 			memento.putString("treeNode", ((TreeNode)nodeObject).getId());
 		} else if (nodeObject instanceof Account) {
 			memento.putString("account", ((Account)nodeObject).getFullAccountName());
 		} else {
 			throw new RuntimeException("unknown object type");
 		}
 		
 		// Save the contents of each page.
 		// However, if the pages array is null then this means
 		// either the addPages method on the editor was not called
 		// or the editor has been closed.  In either case we cannot
 		// save the page state because the page controls do not exist.
 		if (pages == null) {
 			if (JMoneyPlugin.DEBUG) System.out.println("no pages set in " + nodeObject.toString());
 		} else {
 			for (int i = 0; i < pageFactories.size(); i++) {
 				PageEntry entry = pageFactories.get(i);
 				String pageId = entry.getPageId();
 				pages[i].saveState(memento.createChild(pageId));
 			}
 		}
 	}
 
 	/**
 	 * @return
 	 */
 	public IMemento getMemento() {
 		return memento;
 	}
 }
