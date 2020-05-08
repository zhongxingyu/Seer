 package com.inspedio.system.helper;
 
 import java.util.Vector;
 
 import com.inspedio.entity.primitive.InsPoint;
 import com.inspedio.system.helper.extension.InsPointerEvent;
 
 /**
  * This is the helper class for pointer input<br>
  * Useful for global detection of pointerEvent (Pressed, Released, etc)
  * 
  * @author Hyude
  * @version 1.0
  */
 public class InsPointer {
 	
 	public InsPointerEvent[] pressed = new InsPointerEvent[0];
 	public InsPointerEvent[] released = new InsPointerEvent[0];
 	public InsPointerEvent[] dragged = new InsPointerEvent[0];
 	
 	public int holdCount = 0;
 	public InsPoint[] hold = new InsPoint[0];
 	protected Vector holdList;
 	
 	protected Vector pressedEvents;
 	protected Vector releasedEvents;
 	protected Vector draggedEvents;
 	protected Vector holdEvents;
 	
 	public InsPointer(){
 		super();
 		this.pressedEvents = new Vector();
 		this.releasedEvents = new Vector();
 		this.draggedEvents = new Vector();
 		this.holdEvents = new Vector();
 		this.holdList = new Vector();
 	}
 	
 	protected void resetEvent(){
 		this.pressedEvents.removeAllElements();
 		this.releasedEvents.removeAllElements();
 		this.draggedEvents.removeAllElements();
 		this.holdEvents.removeAllElements();
 	}
 	
 	public void addEvent(InsPointerEvent e){
 		if(e.type == InsPointerEvent.PRESSED){
 			this.pressedEvents.addElement(e);
 			this.holdEvents.addElement(e);
 		}
 		else if(e.type == InsPointerEvent.RELEASED){
 			this.releasedEvents.addElement(e);
 			this.holdEvents.addElement(e);
 		}
 		else if(e.type == InsPointerEvent.DRAGGED){
 			this.draggedEvents.addElement(e);
 			this.holdEvents.addElement(e);
 		}
 	}
 	
 	public void updatePointerState(){
 		pressed = new InsPointerEvent[this.pressedEvents.size()];
 		for(int i = 0; i < pressed.length; i++){
 			pressed[i] = (InsPointerEvent) this.pressedEvents.elementAt(i);
 		}
 		
 		released = new InsPointerEvent[this.releasedEvents.size()];
 		for(int i = 0; i < released.length; i++){
 			released[i] = (InsPointerEvent) this.releasedEvents.elementAt(i);
 		}
 		
 		dragged = new InsPointerEvent[this.draggedEvents.size()];
 		for(int i = 0; i < dragged.length; i++){
 			dragged[i] = (InsPointerEvent) this.draggedEvents.elementAt(i);
 		}
 		
 		for(int i = 0; i < this.holdEvents.size(); i++){
			InsPointerEvent e = (InsPointerEvent) this.draggedEvents.elementAt(i);
 			if(e.type == InsPointerEvent.PRESSED){
 				this.addTouchPoint(e);
 			}
 			else if(e.type == InsPointerEvent.RELEASED){
 				this.removeTouchPoint(e);
 			}
 			else if(e.type == InsPointerEvent.DRAGGED){
 				this.moveTouchPoint(e);
 			}
 		}
 		
 		hold = new InsPoint[this.holdList.size()];
 		for(int i = 0; i < hold.length; i++){
 			hold[i] = (InsPoint) this.holdList.elementAt(i);
 		}
 		
 		resetEvent();
 	}
 	
 	protected void addTouchPoint(InsPointerEvent e){
 		this.holdList.addElement(new InsPoint(e.x, e.y));
 		this.holdCount++;
 		InsLogger.writeLog("Pointer Added");
 	}
 	
 	protected void removeTouchPoint(InsPointerEvent e){
 		int idx = this.searchNearestPoint(e.x, e.y);
 		if(idx != -1){
 			this.holdList.removeElementAt(idx);
 			InsLogger.writeLog("Pointer Removed");
 		}
 		this.holdCount--;
 	}
 	
 	protected void moveTouchPoint(InsPointerEvent e){
 		int idx = this.searchNearestPoint(e.x, e.y);
 		if(idx != -1){
 			((InsPoint) this.holdList.elementAt(idx)).setPoint(e.x, e.y);
 			InsLogger.writeLog("Pointer Moved");
 		}
 	}
 	
 	public int searchNearestPoint(int X, int Y){
 		int foundIdx = -1;
 		int distance = 0;
 		int tmpDist = 0;
 		for(int i = 0; i < this.holdList.size(); i++)
 		{
 			InsPoint p = (InsPoint) this.holdList.elementAt(i);
 			tmpDist = (int) InsUtil.Distance(p.x, X, p.y, Y);
 			if(foundIdx == -1){
 				distance = tmpDist;
 				foundIdx = i;
 			} else {
 				if(tmpDist < distance){
 					distance = tmpDist;
 					foundIdx = i;
 				}
 			}
 			
 		}
 		
 		return foundIdx;
 	}
 		
 	public boolean isAnythingPressed(){
 		return (this.holdCount > 0);
 	}
 	
 }
