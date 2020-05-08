 package org.zoneproject.extractor.plugin.spotlight;
 
 /*
  * #%L
  * ZONE-plugin-Spotlight
  * %%
  * Copyright (C) 2012 ZONE-project
  * %%
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  * #L%
  */
 import java.util.ArrayList;
 import org.zoneproject.extractor.utils.Prop;
 import org.zoneproject.extractor.utils.ZoneOntology;
 
 /**
  *
  * @author Desclaux Christophe <christophe@zouig.org>
  */
 public class Annotation implements Comparable<Annotation> {
     private String uri;
     private double score;
     private String type;
     private int offset;
 
     public Annotation(String uri, double score, String type, int offset) {
         this.uri = uri;
         this.score = score;
         this.type = type;
         this.offset = offset;
     }
 
     public String getUri() {
         return uri;
     }
 
     public void setUri(String uri) {
         this.uri = uri;
     }
 
     public double getScore() {
         return score;
     }
 
     public void setScore(double score) {
         this.score = score;
     }
     
     public void addScore(double score) {
         this.score += score;
     }
 
     public String getType() {
         return type;
     }
     
     public ArrayList<Prop> getPropTypes(){
         ArrayList<Prop> result = new ArrayList<Prop>();
         for(String type: this.getType().split(",")){
             result.add(new Prop(ZoneOntology.PLUGIN_DBPEDIA_TYPE, type,true));
         }
         return result;
         
     }
 
     public void setType(String type) {
         this.type = type;
     }
     
 
     @Override
     public String toString() {
         return "Annotation{" + "uri=" + uri + ", score=" + score + ", type=" + type + ", offset=" + offset + "}";
     }
 
     @Override
     public int hashCode() {
         int hash = 5;
         hash = 83 * hash + (this.uri != null ? this.uri.hashCode() : 0);
         return hash;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         final Annotation other = (Annotation) obj;
         if ((this.uri == null) ? (other.uri != null) : !this.uri.equals(other.uri)) {
             return false;
         }
         return true;
     }
     
 
     @Override
     public int compareTo(Annotation o) {
         return Double.compare(o.getScore(), this.getScore());
     }
     
 
 }
