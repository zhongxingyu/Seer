 /*
  * Copyright (C) 2011 Miami-Dade County.
  * Copyright (C) 2012 John Pritchard, Gap Data
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * 
  * Note: this file incorporates source code from 3d party entities. Such code 
  * is copyrighted by those entities as indicated below.
  */
 package json;
 
 import lxl.List;
 
 /**
  * Explicit representation of null value.
  */		
 public class NullJson
     extends Json
 {
     public final static NullJson Instance = new NullJson();
 
 
     public NullJson() {
         super();
     }
 
 		
 
     public boolean isNull() {
         return true;
     }
     public String toString(final boolean child, final int d){
         return "null";
     }
     public int hashCode() {
         return 0;
     }
     public boolean equals(Object x){
 
         return (x instanceof NullJson);
     }
     public int compareTo(Json that){
         if (this == that)
             return 0;
         else if (null == that)
             return +1;
         else if (that instanceof NullJson)
             return 0;
         else 
             return this.getClass().getName().compareTo(that.getClass().getName());
     }
 }
