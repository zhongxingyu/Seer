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
 
 /**
  * 
  */
 public class BooleanJson
     extends Json
 {
     private boolean val;
 
 
     public BooleanJson() {
         super();
     }
     public BooleanJson(Boolean val) { 
         super(); 
         this.val = val;
     }
 
 		
     public boolean asBoolean() { return val; }		
     public boolean isBoolean() { return true;	}		
 
     public String toString(final boolean child, final int d){
         Boolean val = this.val;
         if (null == val)
             return "null";
         else if (val)
             return "true";
         else
             return "false"; 
     }
     public int hashCode() { return val ? 1 : 0; }
     public boolean equals(Object x)
     {
         return x instanceof BooleanJson && ((BooleanJson)x).val == val;
     }
     public int compareTo(Json that){
         if (this == that)
             return 0;
         else if (null == that)
             return +1;
         else if (that instanceof BooleanJson){
             boolean thatVal = ((BooleanJson)that).val;
 
             if (this.val == thatVal)
                 return 0;
             else if (this.val)
                 return +1;
             else
                 return -1;
         }
         else 
             return this.getClass().getName().compareTo(that.getClass().getName());
     }
 }
