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
 
 import org.apache.hadoop.io.WritableComparable;
 
 /**
 * This is a {@link Zone} and {@Vertex} that has been assigned the very
  * {@link Zone}.
  */
 public class ZoneAssignment implements WritableComparable<ZoneAssignment> {
 
   /**
    * the zone of this instance
    */
   private Zone z;
 
   /**
    * the vertex that is assigned the zone
    */
   private Vertex v;
 
   /**
    * Create an instance of a zone assignment.
    * 
    * @param z
    *          The zone to be assigned
    * @param v
    *          The vertex that is assigned the zone
    */
   public ZoneAssignment(Zone z, Vertex v) {
     this.z = z;
     this.v = v;
   }
 
   /**
    * Creates an empty instance.
    */
   public ZoneAssignment() {
   }
 
   /**
    * This method returns true if the other instance is either a
    * {@link ZoneAssignment } with zone and vertex equal to this instance.
    */
   @Override
   public boolean equals(Object o) {
     if (o instanceof ZoneAssignment) {
       boolean is = z.equals(((ZoneAssignment) o).z);
       is = is && v.equals(((ZoneAssignment) o).v);
       return is;
     } else {
       return false;
     }
   }
 
   @Override
   public String toString() {
     VertexWithDegree v = z.getVertex();
     return String
         .format("[%s (%d)] (%s)", v.getVertex(), v.getDegree(), this.v);
   }
 
   @Override
   public void readFields(DataInput in) throws IOException {
     z = new Zone();
     z.readFields(in);
     v = new Vertex();
     v.readFields(in);
   }
 
   @Override
   public void write(DataOutput out) throws IOException {
     z.write(out);
     v.write(out);
   }
 
   /**
    * Compare by zone first and than by value. Return 0 only if both are equal.
    */
   @Override
   public int compareTo(ZoneAssignment o) {
     int zone = z.compareTo(o.z);
     int vertex = v.compareTo(o.v);
     if (zone == 0 && vertex == 0) {
       return 0;
     } else {
       if (zone != 0) {
         return zone;
       } else {
         return vertex;
       }
     }
   }
 
   /**
    * Get the zone of this assignment
    * 
    * @return The zone
    */
   public Zone getZone() {
     return z;
   }
 
   /**
    * Get the vertex that was assigned the zone
    * 
    * @return The vertex that had been assigned
    */
   public Vertex getVertex() {
     return v;
   }
 
 }
