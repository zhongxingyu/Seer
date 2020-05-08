 /*******************************************************************************
  * Copyright (c) 2012 Wind River Systems, Inc. and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.util;
 
 import java.util.Map;
 
 import org.eclipse.tcf.services.IPathMap;
 
 /**
  * A utility class that implements IPathMap.PathMapRule interface.
  */
 public class TCFPathMapRule implements IPathMap.PathMapRule {
 
     final Map<String,Object> props;
 
     public TCFPathMapRule(Map<String,Object> props) {
         this.props = props;
     }
 
     public Map<String,Object> getProperties() {
         return props;
     }
 
     public String getID() {
         return (String)props.get(IPathMap.PROP_ID);
     }
 
     public String getSource() {
         return (String)props.get(IPathMap.PROP_SOURCE);
     }
 
     public String getDestination() {
         return (String)props.get(IPathMap.PROP_DESTINATION);
     }
 
     public String getHost() {
         return (String)props.get(IPathMap.PROP_HOST);
     }
 
     public String getProtocol() {
         return (String)props.get(IPathMap.PROP_PROTOCOL);
     }
 
     public String getContextQuery() {
         return (String)props.get(IPathMap.PROP_CONTEXT_QUERY);
     }
 
     public int hashCode() {
         return props.hashCode();
     }
 
     public boolean equals(Object obj) {
         if (obj instanceof TCFPathMapRule) {
            props.equals(((TCFPathMapRule)obj).props);
         }
         return super.equals(obj);
     }
 
     public String toString() {
         return props.toString();
     }
 }
