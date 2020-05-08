 /*
  * Copyright (c) 2004 UNINETT FAS
  *
  * This program is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the Free
  * Software Foundation; either version 2 of the License, or (at your option)
  * any later version.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
  * more details.
  *
  * You should have received a copy of the GNU General Public License along with
  * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
  * Place - Suite 330, Boston, MA 02111-1307, USA.
  *
  */
 
 package no.feide.moria.servlet;
 
 import java.util.HashMap;
 import java.util.Comparator;
 
 
 /**
  * 
  * @author Eva Indal
  * @version %I%
  *
  * Stores data for all possible user attributes registered. The attributes
  * are read from an xml file to make it easy to add and change the attributes.
  * 
  * The attribute is identified by the "key" keyword, which stores the attribute name,
  * but each attribute has other data as well. See feideattribs.xml for more information.
  * 
  */
 
 public class AttribsData implements Comparator {
     
     /* Used for sorting the attributes */
     private int idx; 
     
     /* Used for storing all info about a attribute */
     private HashMap hashmap;
    
     /**
      * Constructor.
      * @param Index order in xml file.
      */
     public AttribsData(int index) {
        idx = index;
        hashmap = new HashMap();
     }
     
     /**
      * Implements Comparator.compare to be able to sort attributes based on xml file order.
      * 
      * @param ad1 The first object.
      * @param ad2 The second object.
      * @return < 0 if object 1 index is less than object 2, > 0 if object 2 index i larger.
      * @see Comparator.compare
      */
     public int compare(Object ad1, Object ad2){
         AttribsData adata1 = (AttribsData) ad1;
         AttribsData adata2 = (AttribsData) ad2;
         return adata1.idx - adata2.idx;
     }
     
     /**
      * Implements Comparator.equals -- not used.
      * 
      * @param ad Object to compare.
     * @return true If equal.
      */
     public boolean equals(Object ad) {
         AttribsData adata = (AttribsData) ad;
         return idx == adata.idx;
     }
     
     /**
      * Adds info/data for an attribute.
      * 
      * @param name The name of the information.
      * @param data The actual data.
      * @throws IllegalArgumentException
      *         	If name or data is null or zero length.
      */
     public void addData(String name, String data) {
         if (name == null || name.equals("")) {
             throw new IllegalArgumentException("name must be a non-empty string.");
         }
         if (data == null || data.equals("")) {
             throw new IllegalArgumentException("data must be a non-empty string.");
         }
         hashmap.put(name, data);	
     }
     /**
      * Returns data for an attribute.
      * 
      * @param name the name of the infotmation to return
      * @return the data associated with name
      * @throws IllegalArgumentException
      *         	If name is null or zero length.
      */
     public String getData(String name) {
         if (name == null || name.equals("")) {
             throw new IllegalArgumentException("name must be a non-empty string.");
         }
         return (String) hashmap.get(name);
     }    
 }
 
     
     
 
