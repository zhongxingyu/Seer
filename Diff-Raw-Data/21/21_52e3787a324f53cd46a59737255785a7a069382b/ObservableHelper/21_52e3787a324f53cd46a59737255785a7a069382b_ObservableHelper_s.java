 package net.sourceforge.gjtapi;
 
 /*
 	Copyright (c) 1999,2002 Westhawk Ltd (www.westhawk.co.uk) 
 	Copyright (c) 2002 8x8 Inc. (www.8x8.com) 
 
 	All rights reserved. 
 
 	Permission is hereby granted, free of charge, to any person obtaining a 
 	copy of this software and associated documentation files (the 
 	"Software"), to deal in the Software without restriction, including 
 	without limitation the rights to use, copy, modify, merge, publish, 
 	distribute, and/or sell copies of the Software, and to permit persons 
 	to whom the Software is furnished to do so, provided that the above 
 	copyright notice(s) and this permission notice appear in all copies of 
 	the Software and that both the above copyright notice(s) and this 
 	permission notice appear in supporting documentation. 
 
 	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS 
 	OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
 	MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT 
 	OF THIRD PARTY RIGHTS. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR 
 	HOLDERS INCLUDED IN THIS NOTICE BE LIABLE FOR ANY CLAIM, OR ANY SPECIAL 
 	INDIRECT OR CONSEQUENTIAL DAMAGES, OR ANY DAMAGES WHATSOEVER RESULTING 
 	FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, 
 	NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION 
 	WITH THE USE OR PERFORMANCE OF THIS SOFTWARE. 
 
 	Except as contained in this notice, the name of a copyright holder 
 	shall not be used in advertising or otherwise to promote the sale, use 
 	or other dealings in this Software without prior written authorization 
 	of the copyright holder.
 */
 
 
 import java.util.Vector;
 import javax.telephony.events.Ev;
 /**
  * stupid class to re-implement JTAPI's idea of observer/observable
 * made abstract to get correct types returned and correct methosd called
  */
 abstract class ObservableHelper {
 	private Vector obs = new Vector(2);
 
 	void addObserver(Object o) {
 		obs.addElement(o);
 	}
 	Object[] getObjects(){
 		Object [] ret = null;
 		synchronized(obs){
 		  ret = mkObserverArray(obs.size());
 		  obs.copyInto(ret);
 		}
 		if (ret.length == 0){
 		  ret = null;
 		}
 		return ret;
 	}
 	/**
 	 * make an array of the appropriate size of the correct sort of Observers
 	 */
 	abstract Object[] mkObserverArray(int sz);
	/** 2 methods you must implement
	*/

 	/**
	 * call the appropriate Evmethod
 	 */
 	abstract void notifyObserver(Object observer, Ev [] e);
 	boolean removeObserver(Object o) {
 		return obs.removeElement(o);
 	}
 	void sendEvents(Ev [] ev){
 	  Object ojs [] = getObjects();
 	  if (ojs != null) {
 		for (int i=0;i<ojs.length;i++){
 		  try {
 			notifyObserver(ojs[i],ev);
 		  } catch (ClassCastException ccx){
 			ccx.printStackTrace();
 		  }
 		}
 	  }
 	}
 /**
  * Return the number of held observers.
  * Creation date: (2000-06-23 11:21:44)
  * @author: Richard Deadman
  * @return The number of Observers.
  */
 int size() {
 	return this.obs.size();
 }
 }
