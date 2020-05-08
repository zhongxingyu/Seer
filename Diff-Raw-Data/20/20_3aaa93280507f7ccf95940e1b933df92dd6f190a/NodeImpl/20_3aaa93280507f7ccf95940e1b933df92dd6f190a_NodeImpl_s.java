 /*
  * (C) Copyright 2011 Nuxeo SA (http://nuxeo.com/) and contributors.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Lesser General Public License
  * (LGPL) version 2.1 which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/lgpl.html
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * Contributors:
  *     eugen
  */
 package org.nuxeo.ide.studio.data;
 
 /**
  * @author eugen
  *
  */
 public abstract class NodeImpl implements Node {
 
 
     protected final String id;
     protected final String type;
    protected final String label;
     protected final String url;
 
 
     /**
      * @param id
      * @param type
      * @param label
      * @param url
      */
     public NodeImpl(String id, String type, String label, String url) {
         super();
         this.id = id;
         this.type = type;
         this.label = label;
         this.url = url;
     }
 
 
     public String getId() {
         return id;
     }
 
 
     public String getType() {
         return type;
     }
 
 
     public String getLabel() {
         return label;
     }
 
 
     public String getKey() {
         return url;
     }
 
     public String toString() {
         return label;
     }
 
 }
