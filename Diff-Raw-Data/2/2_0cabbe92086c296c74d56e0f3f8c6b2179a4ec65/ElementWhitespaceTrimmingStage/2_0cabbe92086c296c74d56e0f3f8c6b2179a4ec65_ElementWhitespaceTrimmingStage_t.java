 /*
  * Copyright (C) 2013 University of Edinburgh.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package uk.org.ukfederation.mda.dom;
 
 /**
  * Stage to trim leading and trailing whitespace from the text content of named elements
 * within a {@link DomElementItem}.
  */
 public class ElementWhitespaceTrimmingStage extends AbstractElementVisitingStage {
 
     /** Constructor. */
     public ElementWhitespaceTrimmingStage() {
         super(new WhitespaceTrimmingVisitor());
     }
 
 }
