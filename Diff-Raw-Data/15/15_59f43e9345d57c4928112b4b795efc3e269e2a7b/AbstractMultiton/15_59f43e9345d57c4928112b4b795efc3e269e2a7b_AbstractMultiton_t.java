 /*
  * This file is part of LinkJVM.
  *
  * Java Framework for the KIPR Link
  * Copyright (C) 2014 Markus Klein<m@mklein.co.at>
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
 
 package linkjvm.low.factory;
 
 import java.lang.ref.Reference;
 import java.lang.ref.ReferenceQueue;
 import java.lang.ref.WeakReference;
 import java.util.HashMap;
 import java.util.LinkedList;
 
 public abstract class AbstractMultiton<U, I>{
 	private HashMap<U, WeakReference<I>> instances;
 	private ReferenceQueue<I> referenceQueue;
 	
 	/**
 	 * 
 	 */
 	public AbstractMultiton(){
 		instances = new HashMap<U, WeakReference<I>>();
 		referenceQueue = new ReferenceQueue<I>();
 	}
 	
 	/**
 	 * 
 	 * @param uniqueIdentifier
 	 * @return
 	 */
 	public synchronized final I getInstance(U uniqueIdentifier){
 		WeakReference<I> weakReference = instances.get(uniqueIdentifier);
 		if(weakReference == null){
 			weakReference = new WeakReference<I>(getNewConcreteInstance(uniqueIdentifier), referenceQueue);
 			instances.put(uniqueIdentifier, weakReference);
 		}
 		return weakReference.get();
 	}
 	
 	/**
 	 * 
 	 * @param uniqueIdentifier
 	 * @return
 	 */
 	protected abstract I getNewConcreteInstance(U uniqueIdentifier);
 
 	/**
 	 * 
 	 */
 	protected synchronized final void cleanup(){
 		System.out.println("Cleanup!");
 		@SuppressWarnings("unchecked")
 		Reference<I> referenceToDeletedObject = (Reference<I>) referenceQueue.poll();
 		if(referenceToDeletedObject != null){
 			System.out.println("Deleting object!");
			LinkedList<WeakReference<I>> list = new LinkedList<WeakReference<I>>(instances.values());
 			list.remove(referenceToDeletedObject.get());
 		}		
 	}
	
	public class A{
		
	}
	
	public class B extends A{
		
	}
	
	public class C extends A{
		
	}
 }
