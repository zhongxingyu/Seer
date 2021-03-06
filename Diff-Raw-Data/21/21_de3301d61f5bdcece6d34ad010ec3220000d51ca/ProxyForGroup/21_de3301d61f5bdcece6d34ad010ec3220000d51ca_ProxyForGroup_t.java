 /* 
 * ################################################################
 * 
 * ProActive: The Java(TM) library for Parallel, Distributed, 
 *            Concurrent computing with Security and Mobility
 * 
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive-support@inria.fr
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *  
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *  
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s): 
 * 
 * ################################################################
 */
 package org.objectweb.proactive.core.group;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.ListIterator;
 import java.util.Vector;
 
 import org.apache.log4j.Logger;
 import org.objectweb.proactive.Body;
 import org.objectweb.proactive.ProActive;
 import org.objectweb.proactive.core.UniqueID;
 import org.objectweb.proactive.core.body.LocalBodyStore;
 import org.objectweb.proactive.core.body.proxy.AbstractProxy;
 import org.objectweb.proactive.core.mop.ConstructionOfReifiedObjectFailedException;
 import org.objectweb.proactive.core.mop.ConstructorCall;
 import org.objectweb.proactive.core.mop.MOP;
 import org.objectweb.proactive.core.mop.MethodCall;
 import org.objectweb.proactive.core.mop.StubObject;
 
 public class ProxyForGroup extends AbstractProxy implements org.objectweb.proactive.core.mop.Proxy, Group, java.io.Serializable {
 
 	/** The logger for the Class */
 	protected static Logger logger = Logger.getLogger(ProxyForGroup.class.getName());
 
 	/** The name of the Class : all members of the group are "className" assignable */
 	protected String className;
 	/** The list of member : it contains exclusively StubObjects connected to Proxies */
 	protected Vector memberList;
 	/** Unique identificator for body (avoid infinite loop in some hierarchicals groups) */ // NOT FULLY IMPLEMENTED !!!
 	transient private UniqueID proxyForGroupID;
 	/** Number of awaited methodcall on the group's member. The Semantic is : we wait all call are done before continuing */
 	protected int waited = 0;
 	/** Flag to deternime the semantic of communication (broadcast or dispatching) */
 	protected boolean dispatching = false;
 	/** Flag to deternime the semantic of communication (unique serialization of parameters or not) */
 	protected boolean uniqueSerialization = false;
 	/** The stub of the typed group */
 	protected StubObject stub;
 
 
 
 	/* ----------------------- CONSTRUCTORS ----------------------- */
 	public ProxyForGroup(String nameOfClass) throws ConstructionOfReifiedObjectFailedException {
 		this.className = nameOfClass;
 		this.memberList = new Vector();
 		this.proxyForGroupID = new UniqueID();
 	}
 
 	public ProxyForGroup(String nameOfClass, Integer size) throws ConstructionOfReifiedObjectFailedException {
 		this.className = nameOfClass;
 		this.memberList = new Vector(size.intValue());
 		this.proxyForGroupID = new UniqueID();
 	}
 
 	public ProxyForGroup() throws ConstructionOfReifiedObjectFailedException {
 		this.memberList = new Vector();
 		this.proxyForGroupID = new UniqueID();
 	}
 
 	public ProxyForGroup(ConstructorCall c, Object[] p) throws ConstructionOfReifiedObjectFailedException {
 		this.memberList = new Vector();
 		this.proxyForGroupID = new UniqueID();
 	}
 
 
 	/* ----------------------------- GENERAL ---------------------------------- */
 
 	/**
 	 * Allows the Group to dispatch parameters.
 	 */
 	protected void setDispatchingOn() {
 		this.dispatching = true;
 	}
 
 	/**
 	 * Allows the Group to broadcast parameters.
 	 */
 	protected void setDispatchingOff() {
 		this.dispatching = false;
 	}
 
 	/**
 	 * Allows the Group to make an unique serialization of parameters.
 	 */
 	protected void setUniqueSerializationOn() {
 		this.uniqueSerialization = true;
 	}
 
 	/**
 	 * Removes the ability of the Group to make an unique serialization of parameters..
 	 */
 	protected void setUniqueSerializationOff() {
 		this.uniqueSerialization = false;
 	}
 
 	/**
 	 * Checks the semantic of communication of the Group.
 	 * @return <code>true</code> if the "scatter option" is enabled.
 	 */
 	protected boolean isDispatchingOn () {
 		return this.dispatching;
 	}
 	
 	private boolean isDispatchingCall (MethodCall mc) {
 		for (int i = 0 ; i < mc.getNumberOfParameter() ; i++)
 			if (ProActiveGroup.isScatterGroupOn(mc.getParameter(i)))
 				return true;
 		return false;
 	}
 
 
 
 	/* ------------------------ THE PROXY'S METHOD ------------------------ */
 
 	/**
 	 *  The proxy's method : implements the semantic of communication. This method invokes the
 	 * method call <code>mc</code> on each members of the Group.
 	 * @param <code>mc</code> the MethodCall to apply on each member of the Group.
 	 * @return the result of the call : <b> the result of a method call on a typed group is a
 	 * typed group</b>.
 	 * @throws InvocationTargetException if a problem occurs when invoking the method on the members of the Group
 	 */
 	public Object reify(MethodCall mc) throws InvocationTargetException {
 
 		/* result will be a stub on a proxy for group representing the group of results */
 		Object result = null;
 
 		/* if OneWay : do not construct result */
		if (AbstractProxy.isOneWayCall(mc)) {
			this.oneWayCallOnGroup(mc);
		}
		
		/* Special case : the method returns void but is Synchronous because it throws Exception */
		else if (mc.getReifiedMethod().getReturnType() == Void.TYPE) {
			this.oneWayCallOnGroup(mc);
		}
 
 		/* if the call is asynchronous the group of result will be a group a future */
		else { // with group in general case : SYNC == ASYNC !!!!
			result = this.asynchronousCallOnGroup(mc);
		}
 
 		/* A barrier of synchronisation to be sur that all calls are done before continuing the execution */
 		this.waitForAllCallsDone();
 
 		return result;
 	}
 
 
 
 	/* -------------- METHOD FOR THREAD CREATION AND BARRIER OF SYNCHRONISATION ----------------- */
 
 	/**
 	 *  Waits until the method has been apply to all the members.
 	 */
 	protected synchronized void waitForAllCallsDone() {
 		while (this.waited != 0) {
 			try {
 				wait();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	/**
 	 * Decrements the awaited counter and notify that a result is arrived.
 	 */
 	protected synchronized void decrementWaitedAndNotifyAll() {
 		waited--;
 		notifyAll();
 	}
 
 
 
 
 	/* ------------ FOR ASYNCHRONOUS CALL ------------ */
 
 	/**
 	 * Creates and initializes (and returns) the group of result, then launch threads for asynchronous call of each member.
 	 * @param <code>mc</code> the MethodCall to be applied on each member of the Group.
 	 * @return the result of the call.
 	 */
 	protected synchronized Object asynchronousCallOnGroup(MethodCall mc) {
 		Object result;
 		Body body = ProActive.getBodyOnThis();		
 
 		// Creates a stub + ProxyForGroup for representing the result
 		try {
 			Object[] paramProxy = new Object[0];
 			result = MOP.newInstance(mc.getReifiedMethod().getReturnType().getName(), null, "org.objectweb.proactive.core.group.ProxyForGroup", paramProxy);
 			((ProxyForGroup) ((StubObject) result).getProxy()).className = mc.getReifiedMethod().getReturnType().getName();
 		} catch (Exception e) {
 			e.printStackTrace();
 			return null;
 		}

		int size = this.memberList.size();
 		// Init the lists of result with null value to permit the "set(index)" operation
 		Vector memberListOfResultGroup = ((ProxyForGroup) ((StubObject) result).getProxy()).memberList;
 		for (int i = 0; i < size; i++) {
 			memberListOfResultGroup.add(null);
 		}
 
 		// Creating Threads
 		if (isDispatchingCall(mc) == false) {
 			if (uniqueSerialization)
 				mc.transformEffectiveArgumentsIntoByteArray();
 			for (int index = 0; index < this.memberList.size(); index++)
 				this.createThreadForAsync(this.memberList, memberListOfResultGroup, index, mc,body);			
 		}
 		else { // isDispatchingCall == true
 			for (int index = 0; index < memberList.size(); index++) {
 				Object[] individualEffectiveArguments = new Object[mc.getNumberOfParameter()];
 				for (int i = 0; i < mc.getNumberOfParameter(); i++)
 					if (ProActiveGroup.isScatterGroupOn(mc.getParameter(i)))
 						individualEffectiveArguments[i] = ProActiveGroup.get(mc.getParameter(i), index % ProActiveGroup.size(mc.getParameter(i)));
 					else
 						individualEffectiveArguments[i] = mc.getParameter(i);
 				this.createThreadForAsync(this.memberList, memberListOfResultGroup, index, new MethodCall(mc.getReifiedMethod(), individualEffectiveArguments), body);
 			}
 		}
 
 		LocalBodyStore.getInstance().setCurrentThreadBody(body);
 
 		return result;
 	}
 
 	/**
 	 * Creates the threads to make all the calls in parallel.
 	 * @param <code>memberList</code> the list of member on wich we make the call.
 	 * @param <code>memberListOfResultGroup</code> the list where we store the results.
 	 * @param <code>index</code> the rank of the member we consider
 	 * @param <code>mc</code> the MethodCall object to transmit to the member
 	 * @param <code>body</code> the body who have initiate the call.
 	 */
 	private synchronized void createThreadForAsync(Vector memberList, Vector memberListOfResultGroup, int index, MethodCall mc, Body body) {
 		new Thread(new ProcessForAsyncCall(this,memberList, memberListOfResultGroup, index, mc, body)).start();
 		this.waited++;
 	}
 
 	/**
 	 * Add the results (Future) into the typed group result at the correct poisition.
 	 * @param <code>memberListOfResultGroup</code> the list of the typed group result.
 	 * @param <code>result</code> the result of a call on member of a Group.  
 	 * @param <code>index</code> the rank of the result.
 	 */
 	protected synchronized void addToListOfResult(Vector memberListOfResultGroup, Object result, int index) {
 		memberListOfResultGroup.set(index, result);
 		decrementWaitedAndNotifyAll();
 	}
 
 
 	/* -------------------- FOR ONEWAY CALL ---------------------- */
 
 	/**
 	 * Launchs the threads for OneWay call of each member of the Group.
 	 * @param <code>mc</code> the MethodCall to be applied on each member of the Group.
 	 */
 	protected synchronized void oneWayCallOnGroup(MethodCall mc) {
 		Body body = ProActive.getBodyOnThis();		
 		// Creating Threads
 
 		if (isDispatchingCall(mc) == false) {
 			if (uniqueSerialization)
 				mc.transformEffectiveArgumentsIntoByteArray();
 			for (int index = 0; index < this.memberList.size(); index++)
 				this.createThreadForOneWay(this.memberList, index, mc, body);
 		}
 		else { // isDispatchingCall == true
 			for (int index = 0; index < memberList.size(); index++) {
 				Object[] individualEffectiveArguments = new Object[mc.getNumberOfParameter()];
 				for (int i = 0; i < mc.getNumberOfParameter(); i++)
 					if (ProActiveGroup.isScatterGroupOn(mc.getParameter(i)))
 						individualEffectiveArguments[i] = ProActiveGroup.get(mc.getParameter(i), index % ProActiveGroup.size(mc.getParameter(i)));
 					else
 						individualEffectiveArguments[i] = mc.getParameter(i);
 				this.createThreadForOneWay(this.memberList, index, new MethodCall(mc.getReifiedMethod(), individualEffectiveArguments), body);
 			}
 		}
 
 		LocalBodyStore.getInstance().setCurrentThreadBody(body);
 	}
 	
 	/**
 	 * Creates the threads to make all the calls in parallel.
 	 * @param <code>memberList</code> the list of member on wich we make the call.
 	 * @param <code>index</code> the rank of the member we consider
 	 * @param <code>mc</code> the MethodCall object to transmit to the member
 	 * @param <code>body</code> the body who have initiate the call.
 	 */
 	private synchronized void createThreadForOneWay(Vector memberList, int index, MethodCall mc, Body body) {
 		new Thread(new ProcessForOneWayCall(this,memberList, index, mc, body)).start();
 		this.waited++;
 	}
 
 
 
 	/* ------------------- THE COLLECTION'S METHOD ------------------ */
 
 	/** 
 	 * If o is a reified object and if it is "assignableFrom" the class of the group, add it into the group<br>
 	 *  - if o is a group merge it into the group<br>
 	 *  - if o is not a reified object nor a group : do nothing<br>
 	 * @param o - element whose presence in this group is to be ensured
 	 * @return <code>true</code> if this collection changed as a result of the call
 	 */
 	public boolean add(Object o) {
 		try {
 			if ((MOP.forName(this.className)).isAssignableFrom(o.getClass())) {
 				/* if o is an reified object and if it is "assignableFrom" the class of the group, ... add it into the group */
 				if (MOP.isReifiedObject(o)) {
 					boolean result = this.memberList.add(o);
 					/* in the particular case that o extends GroupMember, there are few more operations */
 					if (o instanceof org.objectweb.proactive.core.group.GroupMember) {
 						((org.objectweb.proactive.core.group.GroupMember)o).setMyGroup(stub);
 						((org.objectweb.proactive.core.group.GroupMember)o).setMyRank(this.memberList.indexOf(o));
 					}
 					return result;
 				} /* if o is a Group */
 				else if (o instanceof org.objectweb.proactive.core.group.ProxyForGroup) {
 					/* like an addMerge call */
 					return this.memberList.addAll(((org.objectweb.proactive.core.group.ProxyForGroup) o).memberList);
 				} /* o is a standard Java object */
 				else {
 					Object tmp = null;
 					try {
 						tmp = MOP.newInstance(o.getClass().getName(), null, "org.objectweb.proactive.core.body.future.FutureProxy", null);
 					} catch (Exception e) {
 						if (logger.isInfoEnabled())
 							logger.info("Unable to create a stub+proxy for the new member of the group");
 					}
 					((org.objectweb.proactive.core.body.future.FutureProxy)((StubObject)tmp).getProxy()).setResult(o);
 					return this.add(tmp);
 				}
 			}
 			else {
 				if (logger.isInfoEnabled())
 					logger.info("uncompatible Object");
 				return false;
 			}
 		}
 		catch (java.lang.ClassNotFoundException e) {
 			if (logger.isInfoEnabled())
 				logger.info("Unknown class : " + this.className); }
 		return true;
 	}
 
 	/**
 	 * Adds all of the elements in the specified Collection to this Group.
 	 * @param <code>c</code> - the elements to be inserted into this Group.
 	 * @return <code>true</code> if this collection changed as a result of the call.
 	 */
 	public boolean addAll(Collection c) {
 		boolean modified = false;
 		Iterator iterator = c.iterator();
 		while (iterator.hasNext()) {
 			modified |= this.add(iterator.next());
 		}
 		return modified;
 	}
 
 
 	/**
 	 * Removes all of the elements from this group.
 	 * This group will be empty after this method returns.
 	 */
 	public void clear() {
 		this.memberList.clear();
 	}
 
 	/**
 	 * This method returns true if and only if this group contains at least one element e such that <code>o.equals(e)</code>
 	 * @return <code>true</code> if this collection contains the specified element. 
 	 */
 	public boolean contains(Object o) {
 		return this.memberList.contains(o);
 	}
 
 	/**
 	 * Checks if this Group contains all of the elements in the specified collection.
 	 * @param <code>c</code> - the collection to be checked for containment in this Group.
 	 * @return <code>true</code> if this Group contains all of the elements in the specified collection
 	 */
 	public boolean containsAll(Collection c) {
 		boolean contained;
 		Iterator iterator = c.iterator();
 		while (iterator.hasNext()) {
 			contained = this.contains(iterator.next());
 			if (!contained) return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Compares the specified object with this group for equality.
 	 * @param <o> the Object for wich we test the equality.
 	 * @return <code>true</code> if <code>o</code> is the same Group as <code>this</code>.  
 	 */
 	public boolean equals(Object o) {
 		if (o instanceof org.objectweb.proactive.core.group.ProxyForGroup)
 			return this.proxyForGroupID.equals(((org.objectweb.proactive.core.group.ProxyForGroup)o).proxyForGroupID);
 		else
 			return false;
 	}
 
 	/**
 	 * Returns the hash code value for this Group.
 	 * @return the hash code value for this Group.
 	 */
 	public int hashCode() {
 		return this.memberList.hashCode();
 	}
 
 	/**
 	 * Check if the group is empty.
 	 * @return <code>true</code> if this collection contains no elements.
 	 */
  	public boolean isEmpty() {
  		return this.memberList.isEmpty();
  	}
 
 	/**
 	 * Returns an Iterator of the member in the Group.
 	 * @return an Iterator of the member in the Group.
 	 */
 	public Iterator iterator() {
 		return this.memberList.iterator();
 	}
 
 	/**
 	 * Removes a single instance of the specified element from this Group, if it is present.
 	 * It removes the first occurence e where <code>o.equals(e)</code> returns <code>true</code>. 
 	 * @param <code>o</code> the element to be removed from this Group (if present).
 	 * @return <code>true> if the Group contained the specified element. 
 	 */
 	public boolean remove(Object o) {
 		return this.memberList.remove(o);
 	}
 
 	/**
 	 * Removes all this Group's elements that are also contained in the specified collection.
 	 * After this call returns, this collection will contain no elements in common with the specified collection.
 	 * @param <code>c</code> - elements to be removed from this Group.
 	 * @return <code>true</code> if this Group changed as a result of the call 
 	 */
 	public boolean removeAll(Collection c) {
 		boolean modified = false;
 		Iterator iterator = c.iterator();
 		while (iterator.hasNext()) {
 			modified |= this.remove(iterator.next());
 		}
 		return modified;		
 	}
 
 	/**
 	 * Retains only the elements in this Group that are contained in the specified collection.
 	 * It removes from this Group all of its elements that are not contained in the specified collection.
 	 * @param <code>c</code> - elements to be retained in this Group.
 	 * @return <code>true</code> if this Group changed as a result of the call.
 	 */
 	public boolean retainAll(Collection c) {
 		boolean modified = false;
 		Iterator iterator = c.iterator();
 		while (iterator.hasNext()) {
 			Object tmp = iterator.next();
 			if (this.contains(tmp))
 				modified |= this.remove(tmp);
 		}
 		return modified;		
 
 	}
  
 	/**
 	 * Returns the number of member in this Group.
 	 * @return the number of member in this Group.
 	 */
 	public int size() {
 		return this.memberList.size();
 	}
 	
 	/**
 	 * Returns an array containing all of the elements in this Group in the correct order.
 	 * @return an array containing all of the elements in this Group in the correct order.
 	 */
 	public Object[] toArray() {
 		return this.memberList.toArray();
 	}
 
 	/**
 	 * Returns an array containing all of the elements in this collection;
 	 * the runtime type of the returned array is that of the specified array. 
 	 * @param <code>a</code> - the array into which the elements of this collection are to be stored, if it is big enough;
 	 * otherwise, a new array of the same runtime type is allocated for this purpose.
 	 * @return an array containing the elements of this collection.
 	 */		
 	public Object[] toArray(Object[] a) {
 		return this.memberList.toArray(a);
 	}
 
 
 
 	/* ---------------------- THE GROUP'S METHOD ------------------- */
 
 	/**
 	 *  Add all member of the group <code>ogroup</code> into the Group. <code>ogroup</code> can be :<br>
 	 * - a typed group<br>
 	 * - a Group<br>
 	 * - a standard Object<br>
 	 * but it have to be (or to extend) the Class of the Group.
 	 * @param <code>ogroup</code> the object(s) to merge into the Group.
 	 */
 	public void addMerge(Object oGroup) {
 		try {
 			/* check oGroup is an Reified Object and if it is "assignableFrom" the class of the group */
 			if ((MOP.isReifiedObject(oGroup)) && ((MOP.forName(this.className)).isAssignableFrom(oGroup.getClass()))) {
 				/* check oGroup is an object representing a group */
 				if (((StubObject) oGroup).getProxy() instanceof org.objectweb.proactive.core.group.ProxyForGroup) {
 					memberList.addAll(((ProxyForGroup) ((StubObject) oGroup).getProxy()).memberList);
 				} /* if oGroup is a Standard Active Object (but not a group), just add it */
 				else
 					this.add(oGroup);
 			} /* if oGroup is a Group */
 			else if (oGroup instanceof org.objectweb.proactive.core.group.ProxyForGroup) {
 				memberList.addAll(((org.objectweb.proactive.core.group.ProxyForGroup) oGroup).memberList);
 			}
 		} catch (java.lang.ClassNotFoundException e) {
 			if (logger.isInfoEnabled())
 				logger.info("Unknown class : " + this.className);
 		}
 	}
 
 
 	/**
 	 * Returns the index of the first occurence of the specified Object <code>obj</code>.
 	 * @param <code>obj</code> the obj tahat is searched in the Group. 
 	 * @return the rank of <code>obj</code> in the Group.
 	 * -1 if the list does not contain this object.
 	 */
 	public int indexOf(Object obj) {
 		return this.memberList.indexOf(obj);
 	}
 
 	/**
 	 * Returns a list iterator of the members in this Group (in proper sequence). 
 	 * @return a list iterator of the members in this Group.
 	 */
 	public ListIterator listIterator() {
 		return this.memberList.listIterator();
 	}
 
 
 	/**
 	 * Removes the element at the specified position.
 	 * @param <code>index</code> the rank of the object to remove in the Group.
 	 */
 	public void remove(int index) {
 		this.memberList.remove(index);
 	}
 
 	/**
 	 * Returns the i-th member of the group.
 	 * @param <code>index</code> the rank of the object to return.
 	 * @return the member of the Group at the specified rank.
 	 */
 	public Object get(int i) {
 		this.waitForAllCallsDone();
 		return this.memberList.get(i);
 	}
 
 
 	/**
 	 * Returns the ("higher") Class of group's member.
 	 * @return the Class that all Group's members are (or extend).
 	 * @throws java.lang.ClassNotFoundException if the class name of the Group is not known.
 	 */
 	public Class getType() throws java.lang.ClassNotFoundException {
 		return MOP.forName(this.className);
 	}
 
 	/**
 	 * Returns the full name of ("higher") Class of group's member
 	 * @return the name of the Class that all Group's members are (or extend).
 	 */
 	public String getTypeName() {
 		return this.className;
 	}
 
 	/**
 	 * Returns an Object (a <b>typed group</b> Object) representing the Group
 	 * @return a typed group corresponding to the Group.
 	 */
 	public Object getGroupByType() {
 		Object result;
 		try { // a new proxy is generated
 			result = MOP.newInstance(this.className, null, "org.objectweb.proactive.core.group.ProxyForGroup", null);
 		} catch (Exception e) {
 			e.printStackTrace();
 			return null;
 		}
 		((ProxyForGroup) ((StubObject) result).getProxy()).memberList = this.memberList;
 		((ProxyForGroup) ((StubObject) result).getProxy()).className = this.className;
 		((ProxyForGroup) ((StubObject) result).getProxy()).proxyForGroupID = this.proxyForGroupID;
 		((ProxyForGroup) ((StubObject) result).getProxy()).waited = this.waited;
 		return result;
 	}
 	//  This is the best thing to do, but createStubObject has a private acces !!!! :
 	//    // Instanciates the stub object
 	//    StubObject stub = MOP.createStubObject(this.className, MOP.forName(this.className));
 	//    // Connects the proxy to the stub
 	//    stub.setProxy(this);
 	//    return stub;
 	//  }
 	//  An other way is to "store" the stub and return it when asked
 
 	/**
 	 * To debug, display the size of the Group and all its members with there position
 	 */
 	public void display() {
 		System.out.println("Number of member : " + memberList.size());
 		for (int i = 0; i < memberList.size(); i++)
 			System.out.println("  " + i + " : " + memberList.get(i).getClass().getName());
 	}
 
 
 
 	/* ------------------- SYNCHRONIZATION -------------------- */
 
 	/**
 	 * Waits that all the members are arrived.
 	 */
 	public void waitAll() {
 		ProActive.waitForAll(this.memberList);
 	}
 	
 	/**
 	 * Waits that at least one member is arrived.
 	 */
 	public void waitOne() {
 		ProActive.waitForAny(this.memberList);
 	}
 
 	/**
 	 * Waits that the member at the specified rank is arrived.
 	 * @param <code>index</code> the rank of the awaited member.
 	 */
 	public void waitTheNth(int n) {
 		ProActive.waitFor(this.memberList.get(n));
 	}
 
 	/**
 	 * Waits that at least <code>n</code> members are arrived.
 	 * @param <code>n</code> the number of awaited members.
 	 */
 	public void waitN(int n) {
 		for (int i = 0; i < n ; i++) {
 			this.waitTheNth(i);
 		}
 	}
 
 	/**
 	 * Waits that at least one member is arrived and returns it.
 	 * @return a non-awaited member of the Group.
 	 */
 	public Object waitAndGetOne() {
 		return this.memberList.get(ProActive.waitForAny(this.memberList));
 	}
 
 	/**
 	 * Waits that the member at the specified rank is arrived and returns it.
 	 * @param <code>n</code> the rank of the wanted member.
 	 * @return the member (non-awaited) at the rank <code>n</code> in the Group.
 	 */
 	public Object waitAndGetTheNth(int n) {
 		ProActive.waitForTheNth(this.memberList,n);
 		return this.memberList.get(n);
 	}
 
 	/**
 	 * Checks if all the members of the Group are awaited.
 	 * @return <code>true</code> if all the members of the Group are awaited.
 	 */
 	public boolean allAwaited() {
 		for (int i = 0 ; i < this.memberList.size() ; i++)
 			if (!(ProActive.isAwaited(this.memberList.get(i))))
 				return false;
 		return true;
 	}
 
 	/**
 	 * Checks if all the members of the Group are arrived.
 	 * @return <code>true</code> if all the members of the Group are arrived.
 	 */
 	public boolean allArrived() {
 		for (int i = 0 ; i < this.memberList.size() ; i++)
 			if (ProActive.isAwaited(this.memberList.get(i)))
 				return false;
 		return true;
 	}
 
 	/**
 	 * Waits that at least one member is arrived and returns its index.
 	 * @return the index of a non-awaited member of the Group.
 	 */
 	public int waitOneAndGetIndex() {
 		int index = 0;
 		this.memberList.get(ProActive.waitForAny(this.memberList));
 		while (ProActive.isAwaited(this.memberList.get(index))) {
 			index++;
 		}
 		return index;
 	}
 
 	/* ---------------------- METHOD FOR SYNCHRONOUS CREATION OF A TYPED GROUP ---------------------- */
 
 	/**
 	 * Creates the threads to build members in parallel.
 	 * @param <code>className</code> the name of the Class of the members.
 	 * @param <code>param</code> an array that contains the parameters for the constructor of member.
 	 * @param <code>node</code> the node where the member will be created.
 	 */
 	protected synchronized void createThreadCreation(String className, Object[] param, String node, int index) {
 		new Thread(new ProcessForGroupCreation(this, className, param, node, index)).start();
 		waited++;
 	}
 
 	/**
 	 * Sets an object to the specified position in the Group
 	 * @param index - the position
 	 * @param o - the object to add
 	 */
 	protected void set(int index, Object o) {
 		this.memberList.set(index,o);
 	}
 	
 	/**
 	 * Initializes the Group to the correct size
 	 * @param size - the number of element the Group will contain
 	 */
 	protected void initSize(int size) {
 		for (int i = 0 ; i < size ; i++)
 			this.memberList.add(null);
 	}
 
 
 	/* ------------------------ PRIVATE METHODS FOR SERIALIZATION --------------------- */
 
 	private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
 		this.waitForAllCallsDone();
 		// Now that all the results are available, we can copy the group (of future)
 		out.defaultWriteObject();
 	}
 
 	//for the moment, we set the value of migration to false here
 	private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
 		in.defaultReadObject();
 		this.proxyForGroupID = new UniqueID();
 	}
 
 }
