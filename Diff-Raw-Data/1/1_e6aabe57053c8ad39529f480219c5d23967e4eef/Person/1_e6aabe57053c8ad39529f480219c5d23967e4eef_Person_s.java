 /*
  * Copyright (c) 2008, Carman Consulting, Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.domdrides.entity;
 
 /**
  * @author James Carman
  */
 public class Person extends UuidEntity
 {
 //**********************************************************************************************************************
 // Fields
 //**********************************************************************************************************************
 
     private String ssn;
     private String first;
     private String last;
 
 //**********************************************************************************************************************
 // Getter/Setter Methods
 //**********************************************************************************************************************
 
     public String getFirst()
     {
         return first;
     }
 
     public void setFirst( String first )
     {
         this.first = first;
     }
 
     public String getLast()
     {
         return last;
     }
 
     public void setLast( String last )
     {
         this.last = last;
     }
 
     public String getSsn()
     {
         return ssn;
     }
 
     public void setSsn( String ssn )
     {
         this.ssn = ssn;
     }
 
 //**********************************************************************************************************************
 // Canonical Methods
 //**********************************************************************************************************************
 
     public String toString()
     {
         return first + " " + last + " (" + ssn + ")";
     }
 }
