 /**
  * ******************************************************************************************
  * Copyright (C) 2012 - Food and Agriculture Organization of the United Nations (FAO).
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification,
  * are permitted provided that the following conditions are met:
  *
  *    1. Redistributions of source code must retain the above copyright notice,this list
  *       of conditions and the following disclaimer.
  *    2. Redistributions in binary form must reproduce the above copyright notice,this list
  *       of conditions and the following disclaimer in the documentation and/or other
  *       materials provided with the distribution.
  *    3. Neither the name of FAO nor the names of its contributors may be used to endorse or
  *       promote products derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
  * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
  * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
  * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,PROCUREMENT
  * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,STRICT LIABILITY,OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
  * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  * *********************************************************************************************
  */
 package org.sola.services.ejb.search.repository.entities;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  *
  * @author Manoku
  */
 public class GenericResult {
 
     private String[] fieldNames = new String[0];
     private List<String[]> values = new ArrayList<String[]>();
 
     public GenericResult() {
     }
 
     public String[] getFieldNames() {
         return fieldNames;
     }
 
    public void setFieldNames(String[] fieldNames) { //NOSONAR
        this.fieldNames = fieldNames; //NOSONAR
     }
 
     public List<String[]> getValues() {
         return values;
     }
 
     public void setValues(List<String[]> values) { //NOSONAR
         this.values = values; //NOSONAR
     }
 
     public void addRow(Object[] values){ //NOSONAR   
         String[] row = new String[fieldNames.length];
         for (int fieldIndex = 0; fieldIndex < this.fieldNames.length; fieldIndex++) {
             if (values[fieldIndex] == null){
                 row[fieldIndex] = "";
             }else{
                 row[fieldIndex] = values[fieldIndex].toString();
             }
         }
         if (this.values == null){
             this.values = new ArrayList<String[]>();
         }
             
         this.values.add(row);
     }
 }
