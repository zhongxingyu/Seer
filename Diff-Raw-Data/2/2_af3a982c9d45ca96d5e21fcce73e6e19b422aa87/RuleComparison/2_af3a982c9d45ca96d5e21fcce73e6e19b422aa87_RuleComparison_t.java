 /*
  * Copyright 2011 Edmunds.com, Inc.
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
 package com.edmunds.etm.rules.api;
 
 /**
  * This enum represents the result of comparing two UrlRule objects.
  *
 * @author David Trott
  */
 public enum RuleComparison {
     /**
      * The two rules are identical, typically string equality.
      */
     IDENTICAL,
 
     /**
      * The two rules overlap, some urls match both rules but some only match one and some only match the other.
      */
     OVERLAP,
 
     /**
      * The two rules are completely distinct, there are no urls matched by both rules.
      */
     DISTINCT,
 
     /**
      * The current rule is more specific than the other rule.
      */
     HIGH_PRIORITY,
 
     /**
      * The current rule is less specific than the other rule.
      */
     LOW_PRIORITY;
 }
