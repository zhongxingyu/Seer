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
 import org.jmxdatamart.common.DataType;
 /**
  * This class contains information related to any given attribute such as name,
  * alias and its data type
  *
  * @author Binh Tran <mynameisbinh@gmail.com>
  */
 public class Attribute {
     private DataType dataType;
     private String name;
     private String alias;
 
     public Attribute() {
         name = "";
         alias = "";
     }
 
     /**
      * @return the dataType
      */
     public DataType getDataType() {
         return dataType;
     }
    
    public String getDataTypeClass() {
    	if (dataType == DataType.INT)
    		return "java.lang.Integer";
    	else if (dataType == DataType.FLOAT)
    		return "java.lang.Float";
    	else if (dataType == DataType.STRING)
    		return "java.lang.String";
    	else
    		return null;
    }
 
     /**
      * @param dataType the dataType to set
      */
     public void setDataType(DataType dataType) {
         this.dataType = dataType;
     }
 
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
 
     public Attribute(String name, String alias, DataType dataType) {
         this.name = name;
         this.alias = alias;
         this.dataType = dataType;
     }
 
     @Override
     public String toString() {
         return dataType.toString() + ": " + name + " -> " + alias;
     }
 
     @Override
     public int hashCode() {
         int hash = 5;
         hash = 97 * hash + (this.dataType != null ? this.dataType.hashCode() : 0);
         hash = 97 * hash + (this.name != null ? this.name.hashCode() : 0);
         hash = 97 * hash + (this.alias != null ? this.alias.hashCode() : 0);
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
         final Attribute other = (Attribute) obj;
         if (this.dataType != other.dataType) {
             return false;
         }
         if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
             return false;
         }
         if ((this.alias == null) ? (other.alias != null) : !this.alias.equals(other.alias)) {
             return false;
         }
         return true;
     }
 }
