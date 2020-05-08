 /**
  * <copyright>
  * 
  * Copyright (c) 2010-2012 Thales Global Services S.A.S.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Thales Global Services S.A.S. - initial API and implementation
  * 
  * </copyright>
  */
 package org.eclipse.emf.diffmerge.util.structures;
 
 import java.util.Collections;
 import java.util.List;
 
 
 /**
  * A simple implementation of a modifiable finitary binary relation based on
  * a HashMap of ArrayLists conforming to the IEqualityTester.
  * The default equality tester is by reference.
  * @param T the type of the domain elements
  * @param U the type of the codomain elements
  * @author Olivier Constant
  */
 public class HashBinaryRelation<T, U> extends AbstractBinaryRelation<T, U>
 implements IModifiableBinaryRelation<T, U> {
   
   /** The non-null internal encoding of the relation */
   protected final FHashMap<T, List<U>> _contents;
   
   
   /**
    * Constructor
    * @param tester_p a potentially null equality tester for comparing elements
    */
   public HashBinaryRelation(IEqualityTester tester_p) {
     super(tester_p);
     _contents = new FHashMap<T, List<U>>(getEqualityTester());
   }
   
   /**
    * Constructor
    */
   public HashBinaryRelation() {
     this(null);
   }
   
   /**
    * @see org.eclipse.emf.diffmerge.util.structures.IModifiableBinaryRelation#add(Object, Object)
    */
   public void add(T source_p, U target_p) {
     assert source_p != null && target_p != null;
     List<U> values = _contents.get(source_p);
     if (values == null) {
       values = new FArrayList<U>(getEqualityTester());
       _contents.put(source_p, values);
     }
     if (!values.contains(target_p))
       values.add(target_p);
   }
   
   /**
    * @see org.eclipse.emf.diffmerge.util.structures.IModifiableBinaryRelation#clear()
    */
   public void clear() {
     _contents.clear();
   }
   
   /**
    * @see org.eclipse.emf.diffmerge.util.structures.IBinaryRelation#get(Object)
    */
   public List<U> get(T element_p) {
     assert element_p != null;
     List<U> result;
     List<U> values = _contents.get(element_p);
     if (values == null)
       result = Collections.emptyList();
     else
       result = Collections.unmodifiableList(values);
     return result;
   }
   
   /**
    * @see org.eclipse.emf.diffmerge.util.structures.IModifiableBinaryRelation#remove(Object, Object)
    */
   public void remove(T source_p, U target_p) {
     assert source_p != null && target_p != null;
     List<U> values = _contents.get(source_p);
     if (values != null) {
       values.remove(target_p);
     }
   }
 
 }
