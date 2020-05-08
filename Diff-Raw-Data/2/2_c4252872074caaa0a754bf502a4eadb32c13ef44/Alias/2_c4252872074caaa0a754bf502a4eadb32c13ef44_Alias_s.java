 /**
  * This file is part of Orion source code
  * 
  * Copyright (C) 2012 [Gore]Clan - http://www.goreclan.net
  *
  * Orion is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Lesser Public
  * License as published by the Free Software Foundation; either
  * version 2 of the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Orion. If not, write to the Free Software Foundation,
  * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
  * 
  * @author      Mathias Van Malderen, Daniele Pantaleone
  * @version     1.1
  * @copyright   Mathias Van Malderen, 05 October, 2012
  * @package     com.orion.domain
  **/
 
 package com.orion.domain;
 
 import org.joda.time.DateTime;
 
 public class Alias {
     
     private int id;
     private Client client;
     private String name;
     private int num_used;
     private DateTime time_add;
     private DateTime time_edit;
     
     
     /**
      * Object constructor
      * 
      * @author Daniele Pantaleone
      * @param  client The <tt>Client</tt> this <tt>Alias</tt> belongs to
      * @param  name The <tt>Alias</tt> name
      **/
     public Alias(Client client, String name) {
         this.setClient(client);
         this.setName(name);
         this.setNumUsed(this.getNumUsed() + 1);
     }
     
     
     /**
      * Object constructor
      * 
      * @author Daniele Pantaleone
      * @param  id The <tt>Alias</tt> id
      * @param  client The <tt>Client</tt> this <tt>Alias</tt> belongs to
      * @param  name The <tt>Alias</tt> name
      * @param  num_used A counter which holds the number of time this <tt>Alias</tt> has been used
      * @param  time_add The <tt>DateTime</tt> when this <tt>Alias</tt> has been created
      * @param  time_edit The <tt>DateTime</tt> when this <tt>Alias</tt> has been last edited
      **/
     public Alias(int id, Client client, String name, int num_used, DateTime time_add, DateTime time_edit) {
         this.setId(id);
         this.setClient(client);
         this.setName(name);
         this.setNumUsed(num_used);
         this.setTimeAdd(time_add);
         this.setTimeEdit(time_edit);
     }
 
     
     /**
      * Return the alias id
      * 
      * @author Daniele Pantaleone
      * @return The alias id
      **/
     public int getId() {
         return this.id;
     }
     
     
     /**
      * Return the <tt>Client</tt> who this alias belongs to
      * 
      * @author Daniele Pantaleone
      * @return The <tt>Client</tt> who this alias belongs to
      **/
     public Client getClient() {
         return this.client;
     }
     
     
     /**
      * Return the alias name
      * 
      * @author Daniele Pantaleone
      * @return The alias name
      **/
     public String getName() {
         return this.name;
     }
     
     
     /**
     * Tell the amount of this this alias has been used by the associated <tt>Client</tt>
      * 
      * @author Daniele Pantaleone
      * @return The amount of time this alias has been used
      **/
     public int getNumUsed() {
         return this.num_used;
     }
     
     
     /**
      * Return a <tt>DateTime</tt> object representing the time when the alias has been first created
      * 
      * @author Daniele Pantaleone
      * @return A <tt>DateTime</tt> object representing the time when the alias has been first created
      **/
     public DateTime getTimeAdd() {
         return this.time_add;
     }
     
     
     /**
      * Return a <tt>DateTime</tt> object representing the alias last time edit
      * 
      * @author Daniele Pantaleone
      * @return A <tt>DateTime</tt> object representing the alias last time edit
      **/
     public DateTime getTimeEdit() {
         return this.time_edit;
     }
 
     
     /**
      * Set the alias id
      * 
      * @author Daniele Pantaleone
      * @param  id The alias id
      **/
     public void setId(int id) {
         this.id = id;
     }
 
    
     /**
      * Set the <tt>Client</tt> who this alias belongs to
      * 
      * @author Daniele Pantaleone
      * @param  client The <tt>Client</tt> who this alias belongs to
      **/
     public void setClient(Client client) {
         this.client = client;
     }
 
     
     /**
      * Set the alias name
      * 
      * @author Daniele Pantaleone
      * @param  name The alias name
      **/
     public void setName(String name) {
         // Remove color codes from the client name
         this.name = name.replaceAll("\\^[0-9]{1}", "");
     }
     
 
     /**
      * Set the amount of time this alias has been used
      * 
      * @author Daniele Pantaleone
      * @param  num_used The amount of time this alias has been used by the associated <tt>Client</tt>
      **/
     public void setNumUsed(int num_used) {
         this.num_used = num_used;
     }
 
     
     /**
      * Set the alias time add
      * 
      * @author Daniele Pantaleone
      * @param  time_add A <tt>DateTime</tt> object representing the time when the alias has been first created
      **/
     public void setTimeAdd(DateTime time_add) {
         this.time_add = time_add;
     }
 
     
     /**
      * Set the alias last time edit
      * 
      * @author Daniele Pantaleone
      * @param  time_edit A <tt>DateTime</tt> object representing the alias last time edit
      **/
     public void setTimeEdit(DateTime time_edit) {
         this.time_edit = time_edit;
     }
     
     
     /**
      * <tt>String</tt> object representation
      * 
      * @author Daniele Pantaleone
      * @return A <tt>String</tt> representing the content of this object
      **/
     public String toString() {
         return "[ id : " + this.getId() + " | client : " + this.getClient().getId() + " | name : " + this.getName() + " | num_used : " + this.getNumUsed() + " | time_add : " + this.getTimeAdd().toString() + " | time_edit : " + this.getTimeEdit().toString() + " ]";   
     }
     
 }
