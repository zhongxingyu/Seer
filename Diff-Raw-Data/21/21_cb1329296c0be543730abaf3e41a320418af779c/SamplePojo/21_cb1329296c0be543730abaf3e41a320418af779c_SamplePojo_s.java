 //    Data SQL is a light JDBC wrapper.
 //    Copyright (C) 2014 Adrián Romero Corchado.
 //
 //    This file is part of Data SQL
 //
 //     Licensed under the Apache License, Version 2.0 (the "License");
 //     you may not use this file except in compliance with the License.
 //     You may obtain a copy of the License at
 //     
 //         http://www.apache.org/licenses/LICENSE-2.0
 //     
 //     Unless required by applicable law or agreed to in writing, software
 //     distributed under the License is distributed on an "AS IS" BASIS,
 //     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 //     See the License for the specific language governing permissions and
 //     limitations under the License.
 
 package com.adr.datasql;
 
 import com.adr.datasql.orm.Data;
 import com.adr.datasql.orm.DataPojo;
 import com.adr.datasql.orm.Definition;
 import com.adr.datasql.orm.Field;
 import com.adr.datasql.orm.FieldKey;
 
 /**
  *
  * @author adrian
  */
 public class SamplePojo {
     private String id;
     private String name;
     private String code;
     
     public final static Data<SamplePojo> DATA = new DataPojo(new Definition(
            "samplepojo",
             new FieldKey("id", Kind.STRING),
             new Field("code", Kind.STRING),
             new Field("name", Kind.STRING)));
 
     /**
      * @return the id
      */
     public String getId() {
         return id;
     }
 
     /**
      * @param id the id to set
      */
     public void setId(String id) {
         this.id = id;
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
      * @return the code
      */
     public String getCode() {
         return code;
     }
 
     /**
      * @param code the code to set
      */
     public void setCode(String code) {
         this.code = code;
     }
     
     @Override
     public String toString() {
         return "(" + id + ", " + name + ", " + code + ")";
     }       
 }
