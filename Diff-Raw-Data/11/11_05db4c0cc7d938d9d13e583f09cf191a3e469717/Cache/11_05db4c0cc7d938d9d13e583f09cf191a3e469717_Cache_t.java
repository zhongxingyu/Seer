 /*
  * $Source$
  * $Revision$
  *
  * Part of Melati (http://melati.org), a framework for the rapid
  * development of clean, maintainable web applications.
  *
  * -------------------------------------
  *  Copyright (C) 2000 William Chesters
  * -------------------------------------
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  * A copy of the GPL should be in the file org/melati/COPYING in this tree.
  * Or see http://melati.org/License.html.
  *
  * Contact details for copyright holder:
  *
  *     William Chesters <williamc@paneris.org>
  *     http://paneris.org/~williamc
  *     Obrechtstraat 114, 2517VX Den Haag, The Netherlands
  *
  *
  * ------
  *  Note
  * ------
  *
  * I will assign copyright to PanEris (http://paneris.org) as soon as
  * we have sorted out what sort of legal existence we need to have for
  * that to make sense.  When WebMacro's "Simple Public License" is
  * finalised, we'll offer it as an alternative license for Melati.
  * In the meantime, if you want to use Melati on non-GPL terms,
  * contact me!
  */
 
 package org.melati.util;
 
 import java.lang.ref.*;
 import java.util.*;
 
 public final class Cache {
 
   private interface Node {
     Object key();
     Object value();
   }
 
   private static class HeldNode implements Node {
     Object key;
     Object value;
     HeldNode nextMRU = null;
     HeldNode prevMRU = null;
 
     HeldNode(Object key, Object value) {
       this.key = key;
       this.value = value;
     }
 
     synchronized void putBefore(HeldNode nextMRU) {
       if (this.nextMRU != null)
 	this.nextMRU.prevMRU = prevMRU;
 
       if (prevMRU != null)
 	prevMRU.nextMRU = this.nextMRU;
 
       if (nextMRU != null) {
 	if (nextMRU.prevMRU != null)
 	  nextMRU.prevMRU.nextMRU = this;
 	prevMRU = nextMRU.prevMRU;
 	nextMRU.prevMRU = this;
       }
       else
 	prevMRU = null;
 
       this.nextMRU = nextMRU;
     }
 
     public Object key() {
       return key;
     }
 
     public Object value() {
       return value;
     }
   }
 
   private static class DroppedNode extends SoftReference implements Node {
 
     Object key;
 
     DroppedNode(Object key, Object value, ReferenceQueue queue) {
       super(value, queue);
       this.key = key;
     }
 
     public Object key() {
       return key;
     }
 
     public Object value() {
      return this.get();
     }
   }
 
   private Hashtable table = new Hashtable();
   private HeldNode theMRU = null, theLRU = null;
   private int heldNodes = 0;
   private int maxSize;
 
   private ReferenceQueue collectedValuesQueue = new ReferenceQueue();
 
   public Cache(int maxSize) {
     setSize(maxSize);
   }
 
   public void setSize(int maxSize) {
     if (maxSize < 0)
       throw new IllegalArgumentException();
     this.maxSize = maxSize;
   }
 
   private synchronized void gc() {
     DroppedNode dropped;
     while ((dropped = (DroppedNode)collectedValuesQueue.poll()) != null)
       table.remove(dropped.key);
   }
 
   public synchronized void trim(int maxSize) {
     gc();
 
     HeldNode n = theLRU;
     while (n != null && heldNodes > maxSize) {
       HeldNode nn = n.prevMRU;
       if (n == theLRU) theLRU = n.prevMRU;
       if (n == theMRU) theMRU = n.nextMRU;
       n.putBefore(null);
       --heldNodes;
       table.put(n.key, new DroppedNode(n.key, n.value, collectedValuesQueue));
       n = nn;
     }
   }
 
   public synchronized void put(Object key, Object value) {
     if (key == null || value == null)
       throw new NullPointerException();
 
     trim(maxSize);
 
     if (maxSize == 0)
       table.put(key, new DroppedNode(key, value, collectedValuesQueue));
     else {
       HeldNode node = new HeldNode(key, value);
 
       Object previous = table.put(key, node);
       if (previous != null) {
 	table.put(key, previous);
 	throw new CacheDuplicationException();
       }
 
       node.putBefore(theMRU);
       theMRU = node;
       if (theLRU == null) theLRU = node;
 
       ++heldNodes;
     }
   }
 
   public synchronized Object get(Object key) {
 
     gc();
 
     Node node = (Node)table.get(key);
     if (node == null)
       return null;
     else {
       HeldNode held;
       if (node instanceof HeldNode)
 	held = (HeldNode)node;
       else {
 	if (node.value() == null)
 	  // This probably doesn't happen
 	  return null;
 
 	held = new HeldNode(key, node.value());
 	table.put(key, held);
       }
 
       held.putBefore(theMRU);
       theMRU = held;
       if (theLRU == null) theLRU = held;
       return held.value;
     }
   }
 
   public synchronized void iterate(Procedure f) {
     gc();
     for (Enumeration n = table.elements(); n.hasMoreElements();) {
       Object value = ((Node)n.nextElement()).value();
       if (value != null)
 	f.apply(value);
     }
   }
 
   public synchronized void dumpAnalysis() {
     Hashtable inLRU = new Hashtable();
     int numFromLRU = 0;
 
     // System.err.println("-- LRU->MRU");
 
     for (HeldNode n = theLRU; n != null; n = n.prevMRU) {
       // System.err.println("[" + n + "]");
       ++numFromLRU;
       if (!table.containsKey(n.key()))
         System.err.println("*** ERROR " + n + " is not in the table");
       if (inLRU.containsKey(n.key()))
         System.err.println("*** ERROR " + n + " is in LRU->MRU twice");
       inLRU.put(n.key(), n);
     }
 
     Hashtable inMRU = new Hashtable();
     int numFromMRU = 0;
     int contentsSize = 0;
 
     // System.err.println("-- MRU->LRU");
 
     for (HeldNode n = theMRU; n != null; n = n.nextMRU) {
       // System.err.println("[" + n + "]");
       ++numFromMRU;
       if (!table.containsKey(n.key()))
         System.err.println("*** ERROR " + n + " is not in the table");
       if (inMRU.containsKey(n.key()))
         System.err.println("*** ERROR " + n + " is in MRU->LRU twice");
       inMRU.put(n.key(), n);
       if (!inLRU.containsKey(n.key()))
         System.err.println("*** ERROR " + n + " is in LRU->MRU " +
                            "but not MRU->LRU");
     }
 
     if (numFromMRU != numFromMRU && numFromMRU != table.size())
       System.err.println("*** ERROR the table has " + table.size() +
                          " elements but LRU->MRU and vv. have " +
                          numFromLRU + " & " + numFromMRU);
     else
       System.err.println("Cache size: " + numFromMRU);
 
     System.err.println("Contents size: " + contentsSize);
   }
 }
