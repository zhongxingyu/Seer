/* $Id: Attrs.java,v 1.11 2006/12/04 21:49:15 tufte Exp $    
    Colombia -- Java version of the Columbia Database Optimization Framework
 
    Copyright (c)    Dept. of Computer Science , Portland State
    University and Dept. of  Computer Science & Engineering,
    OGI School of Science & Engineering, OHSU. All Rights Reserved.
 
    Permission to use, copy, modify, and distribute this software and
    its documentation is hereby granted, provided that both the
    copyright notice and this permission notice appear in all copies
    of the software, derivative works or modified versions, and any
    portions thereof, and that both notices appear in supporting
    documentation.
 
    THE AUTHORS, THE DEPT. OF COMPUTER SCIENCE DEPT. OF PORTLAND STATE
    UNIVERSITY AND DEPT. OF COMPUTER SCIENCE & ENGINEERING AT OHSU ALLOW
    USE OF THIS SOFTWARE IN ITS "AS IS" CONDITION, AND THEY DISCLAIM ANY
    LIABILITY OF ANY KIND FOR ANY DAMAGES WHATSOEVER RESULTING FROM THE
    USE OF THIS SOFTWARE.
 
    This software was developed with support of NSF grants IRI-9118360,
    IRI-9119446, IRI-9509955, IRI-9610013, IRI-9619977, IIS 0086002,
    and DARPA (ARPA order #8230, CECOM contract DAAB07-91-C-Q518).
 */
 
 package niagara.optimizer.colombia;
 import java.util.ArrayList;
 
 /*
     ============================================================
     SET OF ATTRIBUTES - class Attrs
     //Used for free attributes, materialized attributes, and in Schema.  
     ============================================================
 */
 
 public class Attrs {
 
     ArrayList al;
 
     public Attribute get(int i) {
         return (Attribute) al.get(i);
     }
 
     public Attrs() {
         al = new ArrayList();
     }
 
     public Attrs(int size) {
         al = new ArrayList(size);
     }
 
     public Attrs(Attribute attr) {
         this();
         al.add(attr);
     }
 
     public Attrs(Attribute[] array, int size) {
     	if(al == null)
     		al = new ArrayList(size);
         al.ensureCapacity(size);
         for (int i = 0; i < size; i++)
             al.add(array[i]);
     }
 
     public Attrs(ArrayList list) {
         this();
         al.addAll(list);
     }
 
     Strings GetAttrNames() {
         Strings result = new Strings();
 
         for (int i = 0; i < size(); i++)
             result.add(get(i).getName());
         return result;
     }
 
    Attribute GetAttr(String name) {
         for (int i = 0; i < size(); i++)
             if (get(i).getName().equals(name))
                 return get(i);
         return null;
     }
 
     public int size() {
         return al.size();
     }
 
     public Attribute GetAt(int i) {
         return (Attribute) al.get(i);
     }
 
     public void add(Attribute attr) {
         al.add(attr);
     }
 
     public void merge(Attrs other) {
         for (int i = 0; i < other.size(); i++) {
             if (!contains(other.get(i)))
                 add(other.get(i));
         }
     }
 
     /** Is an attribute with the given name in this attribute set? */ 
     boolean Contains(String attrName) {
         for (int i = 0; i < size(); i++)
             if (get(i).matchesName(attrName))
                 return true;
 
         return false;
     }
 
     /** Are these attributes in this attribute set? */ 
     boolean Contains(Strings attrNames) {
         for (int i = 0; i < attrNames.size(); i++)
             if (!Contains(attrNames.get(i)))
                 return false;
 
         return true; // this Attrs is contained in array
     }
 
     public boolean contains(Attribute attr) {
         int size = al.size();
         for (int i = 0; i < size; i++)
             if (get(i).equals(attr))
                 return true;
         return false;
     }
 
     public boolean contains(Attrs other) {
         for (int i = 0; i < other.size(); i++)
             if (!contains(other.get(i)))
                 return false;
 
         return true; 
     }
     
     // New Attrs with just the attributes in "attrs" 
     public Attrs project(Attrs attrs) {
         Attrs result = new Attrs();
         for (int i = 0; i < size(); i++)
             if (attrs.contains(get(i)))
                 result.add(get(i));
         return result;
     }
 
     // New Attrs without the attributes in "attrs"
     public Attrs minus(Attrs attrs) {
         Attrs result = new Attrs();
         for (int i = 0; i < size(); i++)
             if (!attrs.contains(get(i)))
                 result.add(get(i));
         return result;
     }
 
     public Attrs copy() {
         Attrs result = new Attrs();
         // check duplicate element in vector
         for (int i = 0; i < size(); i++)
             result.add(get(i).copy());
         return result;
     }
 
     public boolean equals(Object obj) {
         if (obj == null || !(obj instanceof Attrs))
             return false;
         if (obj.getClass() != Attrs.class)
             return obj.equals(this);
         Attrs other = (Attrs) obj;
         if (al.size() != other.al.size())
             return false;
         for (int i = 0; i < al.size(); i++) {
             if (!al.get(i).equals(other.al.get(i)))
                 return false;
         }
         return true;
     }
 
     public int hashCode() {
         int hash = 0;
         for (int i = 0; i < al.size(); i++) {
             hash ^= al.get(i).hashCode();
         }
         return hash;
     }
 
     public String toString() {
         if (al.size() == 0)
             return "";
         StringBuffer sb = new StringBuffer();
         for (int i = 0; i < al.size() - 1; i++)
             sb.append(((Attribute) al.get(i)).getName()).append(",");
 
         sb.append(((Attribute) al.get(al.size() - 1)).getName());
         return sb.toString();
     }
 }
