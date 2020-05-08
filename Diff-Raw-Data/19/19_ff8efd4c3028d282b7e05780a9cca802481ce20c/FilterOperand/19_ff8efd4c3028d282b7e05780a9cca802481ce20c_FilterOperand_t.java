 /*
  * Copyright open knowledge GmbH
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
 
 package de.openknowledge.util.dge.filter;
 
 /**
  * Used to define the operand for a filter.
  * <p/>
  * At the moment it only contains greater than, lower than and equals operand, but it could also include filter operands like contains,
  * startsWith, endsWith and so on.
  *
  * @author Marc Petersen - open knowledge GmbH
  */
 public enum FilterOperand {
 
   /**
    * Greater Than, normally represented by >
    */
   GT,
 
   /**
   * Lower Than, normally represented by <
    */
   LT,
 
   /**
    * Equal
    */
   EQ
 
 }
