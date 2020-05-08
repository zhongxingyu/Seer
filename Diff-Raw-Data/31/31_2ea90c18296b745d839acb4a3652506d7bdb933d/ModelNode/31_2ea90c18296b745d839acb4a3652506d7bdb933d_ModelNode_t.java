 /*******************************************************************************
  * JBoss, Home of Professional Open Source
  * Copyright 2010-2011, Red Hat, Inc. and individual contributors
  * by the @authors tag. See the copyright.txt in the distribution for a
  * full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  *******************************************************************************/
 package org.richfaces.tests.metamer.model.treeAdaptor;
 
 import java.io.Serializable;
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.atomic.AtomicReference;
 
 /**
  * @author <a href="mailto:lfryc@redhat.com">Lukas Fryc</a>
  * @version $Revision$
  */
 public class ModelNode extends Node {
     private static final long serialVersionUID = 1L;
 
     private static final int BS = 3;
     private static final int KS = 4;
     List<B> bs;
     Map<K, V> map;
     List<RecursiveNode> rs;
 
     public ModelNode() {
         super(null, null, null);
     }
 
     protected ModelNode(Node parent, AtomicReference<Boolean> nullable,
         Reference<LazyLoadingListener<Node>> lazyLoadingListenerReference) {
         super(parent, nullable, lazyLoadingListenerReference);
     }
 
     public static ModelNode getInstance(Node parent, AtomicReference<Boolean> nullable,
         Reference<LazyLoadingListener<Node>> lazyLoadingListenerReference) {
         return lazyLoadingChecker(new ModelNode(parent, nullable, lazyLoadingListenerReference));
     }
 
     public String getLabel() {
         return isRoot() ? "M" : getParent().getLabel() + "-M";
     }
 
     public A getValue() {
         return new A();
     }
 
     public List<B> getList() {
         if (bs == null) {
             bs = new LinkedList<B>();
             for (int i = 0; i < BS; i++) {
                 bs.add(new B(i));
             }
         }
         return bs;
     }
 
     public Map<K, V> getMap() {
         if (map == null) {
             map = new LinkedHashMap<K, V>();
             for (int i = 0; i < KS; i++) {
                 map.put(new K(i), new V(i));
             }
         }
         return map;
     }
 
     public List<RecursiveNode> getRecursive() {
         if (rs == null) {
             rs = RecursiveNode.createChildren(this, getNullable(), null);
         }
         return rs;
     }
 
     public class A implements Serializable {
         private static final long serialVersionUID = 1L;
 
         public String getLabel() {
             return ModelNode.this.getLabel() + "-A";
         }
     }
 
     public class B implements Serializable {
         private static final long serialVersionUID = 1L;
 
         int number;
 
         public B(int number) {
             this.number = number;
         }
 
         public String getLabel() {
             return ModelNode.this.getLabel() + "-B-" + number;
         }
     }
 
     public class K implements Serializable, Comparable<K> {
         private static final long serialVersionUID = 1L;
 
         int number;
 
         public K(int number) {
             this.number = number;
         }
 
         public String getLabel() {
             return ModelNode.this.getLabel() + "-K-" + number;
         }
         
         @Override
         public int compareTo(K o) {
             return this.number - o.number;
         }
        
        @Override
        public String toString() {
            return Integer.toString(number);
        }
     }
 
     public class V implements Serializable {
         private static final long serialVersionUID = 1L;
 
         int number;
 
         public V(int number) {
             this.number = number;
         }
 
         public String getLabel() {
             return ModelNode.this.getLabel() + "-V-" + number;
         }
     }
 
     @Override
     public String toString() {
         return getLabel();
     }
 }
