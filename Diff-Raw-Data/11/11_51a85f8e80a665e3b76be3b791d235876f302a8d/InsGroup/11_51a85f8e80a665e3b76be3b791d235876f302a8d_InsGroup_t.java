 package com.inspedio.entity;
 
 import java.util.Vector;
 
 import javax.microedition.lcdui.Graphics;
 
 
 /**
  * InsGroup is a collection of InsBasic used for grouping similar objects.<br>
  * It automatically called update() and draw() for all its member.<br>
  * You can get Group member by accessing <code>members.elementAt(index)</code> and cast it to your preferred Class.<br>
  * J2ME does not support Generic Class, so this is what we can do to overcome that limitation.<br>
  * 
  * @author Hyude
  * @version 1.0
  */
 
 public class InsGroup extends InsAtom{
 
 	/**
 	 * Array of all the <code>InsBasic</code>s that exist in this group.
 	 */
 	public Vector members;
 	
 	/**
 	 * Constructor. Instantiate Member Array
 	 */
 	public InsGroup()
 	{
 		super();
 		members = new Vector();
 	}
 	
 	/**
 	 * Simply Call <code>preUpdate()</code> for all its member.
 	 */
 	public void preUpdate() {
 		if(!this.deleted)
 		{
 			InsAtom atom;
 			for(int i = 0; i < members.size(); i++)
 			{
 				atom = (InsAtom) members.elementAt(i);
 				if((atom != null) && atom.exists && atom.active)
 				{
 					atom.preUpdate();
 				}
 			}
 		}
 	}
 
 	/**
 	 * Simply Call <code>update()</code> for all its member.
 	 */
 	public void update()
 	{
 		if(!this.deleted)
 		{
 			InsAtom atom;
 			for(int i = 0; i < members.size(); i++)
 			{
 				atom = (InsAtom) members.elementAt(i);
 				if((atom != null) && atom.exists && atom.active)
 				{
 					atom.update();
 				}
 			}
 		}
 	}
 
 	/**
 	 * Simply Call <code>postUpdate()</code> for all its member.
 	 */
 	public void postUpdate() {
 		if(!this.deleted)
 		{
 			InsAtom atom;
 			for(int i = 0; i < members.size(); i++)
 			{
 				atom = (InsAtom) members.elementAt(i);
 				if((atom != null) && atom.exists && atom.active)
 				{
 					atom.postUpdate();
 				}
 			}
 		}
 	}
 
 	/**
 	 * Simply Call <code>draw()</code> for all its member.
 	 */
 	public void draw(Graphics g)
 	{
 		if(!this.deleted)
 		{
 			InsAtom atom;
 			for(int i = 0; i < members.size(); i++)
 			{
 				atom = (InsAtom) members.elementAt(i);
 				if((atom != null) && atom.exists && atom.visible)
 				{
 					atom.draw(g);
 				}
 			}
 		}
 	}
 	
 	public void destroy()
 	{
 		super.destroy();
 		this.clear();
 	}
 	
 	/**
 	 * Destroy all of its member and empty member array.
 	 */
 	public void clear()
 	{
 		InsAtom atom;
 		for(int i = 0; i < members.size(); i++)
 		{
 			atom = (InsAtom) members.elementAt(i);
 			if(atom != null)
 			{
 				atom.destroy();
 			}
 		}
 		members.removeAllElements();
 		members = null;
 	}
 	
 	/**
 	 * Adds an object to group. Object always added after the last object.
 	 * 
 	 * @param obj	The object you want to add to the group 
 	 * 
 	 * @return true if object successfully added, false otherwise
 	 */
 	public boolean add(InsAtom obj)
 	{
 		if(!members.contains(obj))
 		{
 			members.addElement(obj);
 			return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * Removes an object from group If object isn't member, it will failed and return null.
 	 *
 	 * @param	obj		The object you want to removed from group.
 	 *
 	 * @return	true if object successfully removed, false otherwise
 	 */
 	public boolean remove(InsAtom obj)
 	{
 		return members.removeElement(obj);	
 	}
 
 	public boolean onPointerPressed(int X, int Y) {
 		for(int i = this.members.size()-1; i >= 0; i--){
 			InsAtom atom = (InsAtom) this.members.elementAt(i);
 			if(atom.exists && atom.active){
				if(atom.onPointerPressed(X, Y)) return true;
 			}
 		}
 		return false;
 	}
 
 	public boolean onPointerReleased(int X, int Y) {
 		for(int i = this.members.size()-1; i >= 0; i--){
 			InsAtom atom = (InsAtom) this.members.elementAt(i);
 			if(atom.exists && atom.active){
				if(atom.onPointerReleased(X, Y)) return true;
 			}
 		}
 		return false;
 	}
 
 	public boolean onPointerDragged(int X, int Y) {
 		for(int i = this.members.size()-1; i >= 0; i--){
 			InsAtom atom = (InsAtom) this.members.elementAt(i);
 			if(atom.exists && atom.active){
				if(atom.onPointerDragged(X, Y)) return true;
 			}
 		}
 		return false;
 	}
 
 	public boolean onPointerHold(int X, int Y) {
 		for(int i = this.members.size()-1; i >= 0; i--){
 			InsAtom atom = (InsAtom) this.members.elementAt(i);
 			if(atom.exists && atom.active){
				if(atom.onPointerHold(X, Y)) return true;
 			}
 		}
 		return false;
 	}
 	
 }
