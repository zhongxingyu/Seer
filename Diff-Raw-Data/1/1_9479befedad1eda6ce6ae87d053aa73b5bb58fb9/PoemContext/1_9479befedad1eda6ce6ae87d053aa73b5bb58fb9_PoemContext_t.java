 /*
  * $Source$
  * $Revision$
  *
  * Copyright (C) 2005 Tim Pizey
  *
  * Part of Melati (http://melati.org), a framework for the rapid
  * development of clean, maintainable web applications.
  *
  * Melati is free software; Permission is granted to copy, distribute
  * and/or modify this software under the terms either:
  *
  * a) the GNU General Public License as published by the Free Software
  *    Foundation; either version 2 of the License, or (at your option)
  *    any later version,
  *
  *    or
  *
  * b) any version of the Melati Software License, as published
  *    at http://melati.org
  *
  * You should have received a copy of the GNU General Public License and
  * the Melati Software License along with this program;
  * if not, write to the Free Software Foundation, Inc.,
  * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA to obtain the
  * GNU General Public License and visit http://melati.org to obtain the
  * Melati Software License.
  *
  * Feel free to contact the Developers of Melati (http://melati.org),
  * if you would like to work out a different arrangement than the options
  * outlined here.  It is our intention to allow Melati to be used by as
  * wide an audience as possible.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * Contact details for copyright holder:
  *
  *     William Chesters <williamc@paneris.org>
  *     http://paneris.org/~williamc
  *     Obrechtstraat 114, 2517VX Den Haag, The Netherlands
  */
 
 package org.melati;
 
 import org.melati.util.UnexpectedExceptionException;
 
 /**
  * An object to hold the information about a Poem session.
  * 
  */
 
 public class PoemContext implements Cloneable {
 
   /** the database in use */
   String logicalDatabase;
   /** the table in use */
   String table;
   /** the troid in use */
   Integer troid;
   /** the method in use */
   String method;
 
  /**
   * Empty constructor.
   * If you use this then you have to populate the object by hand.
   */
   public PoemContext() {  }
 
  /**
   * Constructor.
   * @param logicalDatabase the name of a logical database
   * @param table           the name of the table we are dealing with table
   * @param troid           the Table Row Object ID we are dealing with
   * @param method          what we are doing to this object
   */
   public PoemContext(String logicalDatabase, String table, Integer troid,
                        String method) {
     this.logicalDatabase = logicalDatabase;
     this.table = table;
     this.troid = troid;
     this.method = method;
   }
 
   
  /**
   * @return a string representation of the state of this class
   */
   public String toString() {
     return "logicalDatabase = " + logicalDatabase + ", " +
            "table = " + table + ", " +
            "troid = " + troid + ", " +
            "method = " + method;
   }
 
  /**
   * Clone me.
   * @return a duplicate of this
   */
   public Object clone() {
     try {
       return super.clone();
     }
     catch (CloneNotSupportedException e) {
       throw new UnexpectedExceptionException(e);
     }
   }
   
 
  /**
   * @return the logical database name.
   */
   public String getLogicalDatabase() {
     return logicalDatabase;
   }
   
  /**
   * @return the table name.
   */
   public String getTable() {
     return table;
   }
   
  /**
   * @return the TROID.
   */
   public Integer getTroid() {
     return troid;
   }
   
  /**
   * @return the method.
   */
   public String getMethod() {
     return method;
   }
   /**
    * @param logicalDatabase The logicalDatabase to set.
    */
   public void setLogicalDatabase(String logicalDatabase) {
     this.logicalDatabase = logicalDatabase;
   }
   /**
    * @param method The method to set.
    */
   public void setMethod(String method) {
     this.method = method;
   }
   /**
    * @param table The table to set.
    */
   public void setTable(String table) {
     this.table = table;
   }
   /**
    * @param troid The troid to set.
    */
   public void setTroid(Integer troid) {
     this.troid = troid;
   }
 }
