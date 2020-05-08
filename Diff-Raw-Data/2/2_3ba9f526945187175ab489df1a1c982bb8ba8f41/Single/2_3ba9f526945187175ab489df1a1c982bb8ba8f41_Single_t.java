 /*
  * Copyright 2010 Hjortur Stefan Olafsson
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
  *
  */
 package twigkit.klustr.strategy;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import twigkit.klustr.ResourcesModificationException;
 
 /**
  * If only a single resource then always return that.
  */
 public class Single<T> implements Strategy<T> {
 
    private static final Logger logger = LoggerFactory.getLogger(Single.class);
 
     private T resource;
 
     public void setResources(T... resources) throws ResourcesModificationException {
         if (resources.length == 1) {
             this.resource = resources[0];
         }
         logger.error("Expecting 1 resource, got " + resources.length);
     }
 
     public T next() {
         if (logger.isDebugEnabled()) {
             logger.debug("Using resource " + resource.getClass().getName() + "[" + resource + "]");
         }
         return resource;
     }
 
     public void reset() {
         // Does nothing
     }
 }
