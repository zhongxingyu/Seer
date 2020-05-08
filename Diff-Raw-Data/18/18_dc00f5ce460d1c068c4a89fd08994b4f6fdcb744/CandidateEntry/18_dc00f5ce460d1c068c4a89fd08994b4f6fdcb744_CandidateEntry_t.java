 
 /**
  * PreparsedMetabolite.java
  *
  * 2011.08.15
  *
  * This file is part of the CheMet library
  *
  * The CheMet library is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * CheMet is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with CheMet.  If not, see <http://www.gnu.org/licenses/>.
  */
 package uk.ac.ebi.metabolomes.webservices.util;
 
import com.google.common.base.Objects;
 
 
 /**
  *
  * A Candidate entry describes a paired identifier/description with a distance and comment. The
  * distance can be calculated in numerous ways (see: {@see ChemicalNameEntryDecider} and
  * {@see EntryDecider}).
  *
  * @author Pablo Moreno
  * @author John May 
  * 
  */
 public class CandidateEntry
   implements Comparable<CandidateEntry> {
 
     private String id;
     private String description;
     private Integer distance;
     private String comment;
 
 
     public CandidateEntry() {
     }
 
 
     /**
      * Construct a candidate entry given all possible attributes
      * 
      * @param id        Candidate Identifier
      * @param desc      Description of the candidate (e.g. compound name)
      * @param distance  The distance between the candidate and the query (not-stored in class)
      * @param comment   Additional comments on the candidate
      * 
      */
     public CandidateEntry(String id, String desc, Integer distance, String comment) {
         this.id = id;
         this.description = desc;
         this.distance = distance;
         this.comment = comment;
     }
 
 
     public String getId() {
         return id;
     }
 
 
     public void setId(String id) {
         this.id = id;
     }
 
 
     public String getDesc() {
         return description;
     }
 
 
     public String getDescription() {
         return description;
     }
 
 
     public void setDesc(String desc) {
         this.description = desc;
     }
 
 
     /**
      * @return the distance of this candidate
      */
     public Integer getDistance() {
         return distance;
     }
 
 
     /**
      * @param distance the distance to set
      */
     public void setDistance(Integer distance) {
         this.distance = distance;
     }
 
 
     /**
      * 
      * Compares two candidates on their set distance. If either distance is null the objects are
      * considered equal '0'
      *
      */
     @Override
     public int compareTo(CandidateEntry o) {
         if( distance == null || o.distance == null ) {
             return 0;
         }
         return this.distance.compareTo(o.distance);
     }
 
 
     public void setComment(String comment) {
         this.comment = comment;
     }
 
 
     public String getComment() {
         return comment;
     }
 
 
     /**
      * @inheritDoc
      */
     @Override
     public int hashCode() {
        return Objects.hashCode(id, description, distance, comment);
     }
 
 
     /**
      * @inheritDoc
      */
     @Override
     public boolean equals(Object obj) {
         if( obj == null ) {
             return false;
         }
         if( getClass() != obj.getClass() ) {
             return false;
         }
         final CandidateEntry other = (CandidateEntry) obj;
 
        if( !Objects.equal(this.id, other.id) ) {
             return false;
         }
        if( !Objects.equal(this.description, other.description) ) {
             return false;
         }
        if( !Objects.equal(this.distance, other.distance) ) {
             return false;
         }
        if( !Objects.equal(this.comment, other.comment) ) {
             return false;
         }
         return true;
     }
 
 
     /**
      * @inheritDoc
      */
     @Override
     public String toString() {
         StringBuilder sb = new StringBuilder(30);
         return sb.append(id).append(": ").append(this.description).append(" d=").append(
           this.distance).toString();
     }
 
 
     /**
      *
      * Clones the current candidate id and description with a new distance and comment. This
      * methods is primarily used in factory methods
      *
      * @return A new candidate entry
      * 
      */
     public CandidateEntry newInstance(Integer distance, String comment) {
         return new CandidateEntry(this.id, this.description, distance, comment);
     }
 
 
 }
