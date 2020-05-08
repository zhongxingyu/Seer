 /*
  * This file is part of LinkJVM.
  *
  * Java Framework for the KIPR Link
  * Copyright (C) 2013 Markus Klein<m@mklein.co.at>
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 
package linkjvm.events;
 
 import java.util.ArrayList;
import java.util.Iterator;
 
 /**
  * 
  * @author Markus Klein
  *
  */
 
 public abstract class Event {
 	public ArrayList<Action> actionList = new ArrayList<Action>();
 	
 	/**
 	 * This function is called permanently to proof if the event should be triggered or not.
 	 * 
 	 * @return true if the event should be triggered, false if not
 	 */
 	public abstract boolean proof();
 	
 	/**
 	 * Adds an action which is is triggered by the event.
 	 * 
 	 * @param a action which should be added to the event
 	 * @return true if adding the action succeed
 	 */
 	public final boolean addAction(Action a){
 		return actionList.add(a);
 	}
 	
 	/**
 	 * Removes the first appearing instance of the specified action. 
 	 * 
 	 * @param a action
 	 * @return true if removing the action succeed
 	 */
 	public final boolean removeAction(Action a){
 		return actionList.remove(a);
 	}
 	
 	/**
 	 * 
 	 * Triggers all Actions assigned to the Event.
 	 *
 	 */	
 	public final void trigger(){
 		Iterator<Action> it = actionList.iterator();
		while(it.hasNext()){
			it.next().perform();
 		}
 	}	
 }
