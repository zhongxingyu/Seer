 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.internal.javascript.typeinference;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 
 public class HostCollection {
 
 	public static final int FUNCTION = 1;
 	public static final int NORMAL = 0;
 	
 	private final HostCollection parent;
 	private final HashMap reference = new HashMap();
 	private int type;
 
 	public IReference getReference(String key){
 		IReference reference2 = (IReference) reference.get(key);
 		if (reference2==null)if (parent!=null)return parent.getReference(key);
 		return reference2;		
 	}
 	
 	public Set queryElements(String id, boolean useGlobal) {
 		IReference r = getReference(id);
 		HashSet res=new HashSet();
 		if (r != null)
 		{			
 			res.add(r);
 			return res;
 		}
 		int pos=id.indexOf('.');
 		if (pos==-1)return res;
 		String rootName=id.substring(0,pos);
 		r=(IReference) getReference(rootName);
 		pos+=1;
 		String field;
 		while (pos!=0){
 			if (r==null)return new HashSet();
 			int k=id.indexOf('.',pos);
 			if (k==-1) field=id.substring(pos);
 			else field=id.substring(pos,k);
 			r=r.getChild(field, useGlobal);			
 			pos=k+1;
 		}
 		if (r==null)return res;
 		res.add(r);
 		return res;
 	}
 
 	public HostCollection(HostCollection parent) {
 		super();
 		this.parent = parent;
 	}
 
 	public Map getReferences() {
 		return reference;
 	}
 
 	public void write(String key, IReference ref) {
 		if (ref == null)
 			throw new IllegalArgumentException();		
 		reference.put(key, ref);
 	}
 
 	public void add(String key, IReference ref) {
 		if (ref == null)
 			throw new IllegalArgumentException();
 		Object object = reference.get(key);
 		if (object == null) {
 			reference.put(key, ref);
		} else if (object != ref) {
			if (ref instanceof OrReference)
			{
				// Test if those already are referencing each other.
				if ( ((OrReference)ref).one == object)
				{
					reference.put(key, ref);
					return;
				}
			}
			else if (ref instanceof TransparentRef)
			{
				if (((TransparentRef)ref).evaluateReference == object)
				{
					reference.put(key, ref);
					return;
				}
			}

 			ref = new OrReference((IReference) object, ref);
 			
 			reference.put(key, ref);
 		}
 	}
 
 	
 
 	public void oneOf(String key, IReference ref, IReference other) {
 		if (ref == null)
 			throw new IllegalArgumentException();
 		if (other == null)
 			throw new IllegalArgumentException();
 		if (ref.isChildishReference()||other.isChildishReference()){
 			add(key, ref);
 			add(key, other);
 		}
 		else reference.put(key, new OrReference(ref, other));
 	}
 
 	public HostCollection getParent() {
 		return parent;
 	}
 
 	public void mergeIf(HostCollection cl) {
 		
 		Iterator i = cl.reference.keySet().iterator();
 		while (i.hasNext()) {
 			
 			Object next = i.next();
 			if (next instanceof String)
 			{
 			String s = (String) next;
 			IReference rm = (IReference) cl.reference.get(s);
 			add(s, rm);
 			}
 		}
 		cl.pach(this);
 	}
 
 	public void mergeElseIf(HostCollection cl, HostCollection cl1) {
 				
 		HashSet sm = new HashSet(cl.reference.keySet());
 		sm.retainAll(cl1.reference.keySet());
 		Iterator i = sm.iterator();
 		while (i.hasNext()) {
 			String s = (String) i.next();
 			IReference rm = (IReference) cl.reference.get(s);
 			IReference rm1 = (IReference) cl1.reference.get(s);
 			oneOf(s, rm, rm1);
 		}
 		cl1.pach(this);
 		cl.pach(this);
 	}
 
 	public void override(HostCollection other) {
 		reference.putAll(other.reference);
 	}
 
 	public void setReference(String objId, IReference root) {
 		this.reference.put(objId, root);
 	}
 
 	public IReference getReferenceNoParentContext(String rootName) {
 		return (IReference) this.reference.get(rootName);
 	}
 
 	public IReference queryElement(String key1, boolean useGlobal) {		
 		Set queryElement = this.queryElements(key1, useGlobal);
 		if (queryElement.isEmpty())return null;
 		return (IReference) queryElement.iterator().next();
 	}
 
 	HashSet transparent=new HashSet();
 	public void addTransparent(TransparentRef transparentRef) {
 		transparent.add(transparentRef);
 	}
 	
 	private void pach(HostCollection col){
 		Iterator iterator = transparent.iterator();
 		while (iterator.hasNext()){
 			TransparentRef next = (TransparentRef) iterator.next();
 			next.patchRef(col);
 		}
 	}
 
 	public void recordDelete(String objId) {
 		reference.remove(objId);
 	}
 
 	private String name;
 	public void setName(String functionName) {
 		this.name=functionName;
 	}
 	public String getName(){
 		return name; 
 	}
 
 	public int getType() {
 		return type;
 	}
 
 	public void setType(int type) {
 		this.type = type;
 	}
 	public void recordFunction(Object function,HostCollection collection){
 		reference.put(function, collection);
 	}
 	public HostCollection getFunction(Object funObject){
 		return (HostCollection) reference.get(funObject);
 	}
 }
