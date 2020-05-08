 /*
  * Copyright 2009 University Corporation for Advanced Internet Development, Inc.
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
  */
 
 package org.openxrd.xrd;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import org.opensaml.xml.util.LazySet;
 import org.openxrd.xrd.core.Link;
 import org.openxrd.xrd.core.MediaType;
 import org.openxrd.xrd.core.Rel;
 
 /**
  * Basic link filter that matches links based on Rel and MediaType values.
  */
 public class BasicLinkFilter implements LinkFilter {
 
     /** Rels to match against link. */
     private Set<String> rels;
 
     /** MediaTypes to match against link. */
     private Set<String> mediaTypes;
 
     /** Constructor. */
     public BasicLinkFilter() {
         rels = new LazySet<String>();
         mediaTypes = new LazySet<String>();
     }
 
     /** {@inheritDoc} */
     public boolean matches(Link link) {
         if (!matchRels(link)) {
             return false;
         }
 
         if (!matchMediaTypes(link)) {
             return false;
         }
 
         return true;
     }
 
     /**
      * Match given link against rel values.
      * 
      * @param link link to match
      * @return true if link matches rel values for this filter
      */
     protected boolean matchRels(Link link) {
        if (link.getRels().size() != 0 && rels.size() != 0) {
             Set<String> linkRels = new HashSet<String>();
             for (Rel rel : link.getRels()) {
                 linkRels.add(rel.getValue());
             }
             for (String relValue : rels) {
                 if (!linkRels.contains(relValue)) {
                     return false;
                 }
             }
         }
 
         return true;
     }
 
     /**
      * Match given link against media type values.
      * 
      * @param link link to match
      * @return true if link matches media type values for this filter
      */
     protected boolean matchMediaTypes(Link link) {
        if (link.getMediaTypes().size() != 0 && mediaTypes.size() != 0) {
             Set<String> linkMediaTypes = new HashSet<String>();
             for (MediaType type : link.getMediaTypes()) {
                 linkMediaTypes.add(type.getValue());
             }
             for (String typeValue : mediaTypes) {
                 if (!linkMediaTypes.contains(typeValue)) {
                     return false;
                 }
             }
         }
 
         return true;
     }
 
     /**
      * Get rel values to match against links.
      * 
      * @return rels
      */
     public Set<String> getRels() {
         return rels;
     }
 
     /**
      * Get media type values to match against links.
      * 
      * @return media types
      */
     public Set<String> getMediatypes() {
         return mediaTypes;
     }
 
 }
