 /**
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.apache.mahout.graph.model;
 
 import java.io.DataInput;
 import java.io.DataOutput;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.TreeSet;
 
 import org.apache.hadoop.io.WritableComparable;
 
 /**
  * Key type for various graph algorithm intermediate results. Although this
  * class can contain an arbitrary number of vertices it usually is used as a
  * membership key of one or two vertices.
  * 
  * The default behavior of membership is an ordered set semantic. But if it is
  * read from sequence file it shows bag semantics if altered. It can be
  * overwritten by setting the collection object of vertices and working on it
  * directly using {@link #setMembers(Collection)} and {@link #getMembers()}
  * methods.
  */
 public class Membership implements WritableComparable<Membership> {
 
   /**
   * Factory method that is equivalent to the {@link #readFields(DataInput)}
    * method.
    * 
    * @param in
    *          the sequence file data input where the next instance is serialized
    * @return a deserialized instance
    * @throws IOException
    *           if the sequence file operation throws this exception
    */
   public static Membership read(DataInput in) throws IOException {
     Membership read = new Membership();
     read.readFields(in);
     return read;
   }
 
   /**
    * membership collection
    */
   private Collection<Vertex> members;
 
   /**
    * Constructs an empty {@link Membership} instance
    */
   public Membership() {
   }
 
   ;
 
   @Override
   public void write(DataOutput out) throws IOException {
     if (members != null) {
       int howmany = members.size();
       out.writeInt(howmany);
       Iterator<Vertex> i = members.iterator();
       while (i.hasNext()) {
         Vertex m = i.next();
         m.write(out);
       }
     } else {
       out.writeInt(0);
     }
 
   }
 
   @Override
   public void readFields(DataInput in) throws IOException {
 
     int howmany = in.readInt();
     members = new ArrayList<Vertex>(howmany);
     while (howmany-- > 0) {
       Vertex m = Vertex.read(in);
       members.add(m);
     }
 
   }
 
   /**
    * Compares this instance to another. This method return 0 if both memberships
    * are null or both memberships are empty. It returns 0 if both memberships
    * contain the same vertices returned in the same order by there membership
    * collection objects.
    */
   @Override
   public int compareTo(Membership o) {
     int compareTo = 0;
     if (members != null) {
       if (o.members != null) {
         Iterator<Vertex> i = members.iterator();
         Iterator<Vertex> oi = o.members.iterator();
         while (true) {
           boolean n = i.hasNext();
           boolean on = i.hasNext();
           if (n && on) {
             // compare member by member
             compareTo = i.next().compareTo(oi.next());
             if (compareTo != 0) {
               return compareTo; // break on first difference
             } else {
               continue;
             }
           } else if (!n && on) {
             // we have fewer members than the other
             return Integer.MIN_VALUE;
           } else if (n && !on) {
             // the other has fewer members
             return Integer.MAX_VALUE;
           } else {
             return 0;
           }
         }
       } else {
         // the other has fewer
         return Integer.MAX_VALUE;
       }
     } else {
       // we have no members
       if (o.members == null) {
         // both no members
         return 0;
       } else {
         if (o.members.size() > 0) {
           // we have fewer
           return Integer.MIN_VALUE;
         } else {
           // both no members
           return 0;
         }
       }
     }
   }
 
   /**
    * Sets the membership collection to the membership
    * <code>WritableComparable</code>. The <code>Collection</code> will be used
    * directly. This way it is possible to overwrite the default ordered set
    * semantic of {@link Membership}.
    * 
    * @param members
    *          the collection to be used to contain the objects
    * @return this
    */
   public Membership setMembers(Collection<Vertex> members) {
     this.members = members;
     return this;
   }
 
   /**
    * Returns the membership collection. If not overwritten this is an ordered
    * set of vertices.
    * 
    * @return the membership collection
    */
   public Collection<Vertex> getMembers() {
     return members;
   }
 
   /**
    * Set a member to the membership collection. If this is the first call and no
    * membership collection had been set via
    * {@link Membership#setMembers(Collection)} an ordered set will be created to
    * contain the member.
    * 
    * @param m
    * @return this for convenience of chained invocation
    */
   public Membership addMember(Vertex m) {
     if (members == null) {
       members = new TreeSet<Vertex>();
     }
     members.add(m);
     return this;
   }
 
   /**
    * Equals another {@link Membership} if both contain the same vertices in the
    * same order.
    */
   @Override
   public boolean equals(Object o) {
     if (o instanceof Membership) {
       return this.compareTo((Membership) o) == 0;
     } else {
       return false;
     }
   }
 
   /**
    * The hash code returned will be a product of all hash codes of all vertices.
    */
   @Override
   public int hashCode() {
     int hash = 0;
     if (members != null) {
       Iterator<Vertex> i = members.iterator();
       hash = 1;
       while (i.hasNext()) {
         hash = hash * i.next().hashCode();
       }
     }
     return hash;
   }
   
   @Override
   public String toString() {
     return members.toString();
   }
 }
