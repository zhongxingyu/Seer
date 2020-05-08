 /*
  * Copyright (c) 2012, Tripwire, Inc.
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are
  * met:
  *
  *  o Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer.
  *
  *  o Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in the
  *    documentation and/or other materials provided with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
  * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
  * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
  * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
  * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package org.jmxdatamart.Extractor;
 
 import java.util.*;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.management.MalformedObjectNameException;
 import javax.management.ObjectName;
 import org.slf4j.LoggerFactory;
 
 /**
  * This class contains data related to any MBean such as name, alias and its
  * attributes
  * @author Binh Tran <mynameisbinh@gmail.com>
  */
 public class MBeanData {
     private String name;
     private String alias;
     private List<Attribute> attributes;
     private boolean enable;
     private final org.slf4j.Logger logger = LoggerFactory.getLogger(this.getClass());
 
     /**
      * @return the name
      */

     public String getName() {
         return name;
     }
 
     /**
      * @param name the name to set
      */
     public void setName(String name) {
         this.name = name;
     }
 
     /**
      * @return the alias
      */

     public String getAlias() {
         return alias;
     }
 
     /**
      * @param alias the alias to set
      */
     public void setAlias(String alias) {
         this.alias = alias;
     }
 
     /**
      * @return the attributes
      */
     public List<Attribute> getAttributes() {
         return attributes;
     }
 
     /**
      * @param attributes the attributes to set
      */
     public void setAttributes(List<Attribute> attributes) {
         this.attributes = attributes;
     }
     
     public MBeanData(String name, String alias, List<Attribute> attributes, boolean enable) {
         this.name = name;
         this.alias = alias;
         this.attributes = attributes;
         this.enable = enable;
     }
 
     public MBeanData() {
         name = "";
         alias = "";
         enable = true;
     }
     
     @Override
     public String toString() {
         String nl = System.getProperty("line.separator");
         return  this.name + " -> " + this.alias + nl +
                 attributes.toString() + nl;
     }
 
     /**
      * @return the enable
      */
    
     public boolean isEnable() {
         return enable;
     }
 
     /**
      * @param enable the enable to set
      */
     public void setEnable(boolean enable) {
         this.enable = enable;
     }
     
     /**
      * Check if the MBeanData object's alias is correctly formated
      * @return true if correctly formated, false if not
      */
     // Task 3041
     public boolean checkForValidAlias() {
         for (Attribute a : this.attributes) {
             if (!a.checkForValidAlias()) {
                 return false;
             }
         }
         
         throw new UnsupportedOperationException("Not yet implemented");
     }
     
     /**
      * Check if the MBeanData represent a pattern or a single MBean
      * @return true if MBeanData's name is a valid JMX pattern, false if not
      * @throws MalformedObjectNameException if MBeanData's name is not a valid
      * ObjectName's name
      */
     public boolean isPattern() {
       try {
         return (new ObjectName(this.name)).isPattern();
       } catch (MalformedObjectNameException ex) {
         logger.error("MBean name malformed in " + this.name, ex);
         return false;
       }
     }
 
     @Override
     public int hashCode() {
         int hash = 7;
         hash = 53 * hash + (this.name != null ? this.name.hashCode() : 0);
         hash = 53 * hash + (this.alias != null ? this.alias.hashCode() : 0);
         hash = 53 * hash + (this.attributes != null ? this.attributes.hashCode() : 0);
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
         final MBeanData other = (MBeanData) obj;
         if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
             return false;
         }
         if ((this.alias == null) ? (other.alias != null) : !this.alias.equals(other.alias)) {
             return false;
         }
         if (this.attributes != other.attributes && (this.attributes == null || !this.attributes.equals(other.attributes))) {
             return false;
         }
         return true;
     }
     
 }
