 /*
    Copyright 2010 Andy Hedges <andy@hedges.net>
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 */
 package net.hedges.fandangled.bindings.domain;
 
 import net.hedges.fandangled.bindings.domain.document.Document;
 
 public abstract class AbstractEntity {
 
     private String name;
    private Document description = new Document();
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public Document getDescription() {
         return description;
     }
 
     public void setDescription(Document description) {
         this.description = description;
     }
 }
