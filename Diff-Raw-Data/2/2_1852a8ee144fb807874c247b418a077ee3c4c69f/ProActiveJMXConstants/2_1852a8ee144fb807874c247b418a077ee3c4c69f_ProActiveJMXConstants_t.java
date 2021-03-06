 /*
  * ################################################################
  *
  * ProActive: The Java(TM) library for Parallel, Distributed,
  *            Concurrent computing with Security and Mobility
  *
  * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
  * Contact: proactive@objectweb.org
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
  * USA
  *
  *  Initial developer(s):               The ProActive Team
  *                        http://www.inria.fr/oasis/ProActive/contacts.html
  *  Contributor(s):
  *
  * ################################################################
  */
 package org.objectweb.proactive.extensions.jmx;
 
 import java.util.HashMap;
 
 
 /**
  *  Constants for ProActive JMX ServerConnector
  * @author ProActive Team
  *
  */
 public class ProActiveJMXConstants {
     public static final String PROTOCOL = "proactive";
     public static final String VERSION = "ProActive 3.2";
    public static final String SERVER_REGISTERED_NAME = "PAJMXServer";
     public static final HashMap<String, String> PROACTIVE_JMX_ENV = new HashMap<String, String>();
 
     static {
         PROACTIVE_JMX_ENV.put("jmx.remote.protocol.provider.pkgs",
             "org.objectweb.proactive.extensions.jmx.provider");
     }
 }
