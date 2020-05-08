 /*
  *  The OpenDiamond Platform for Interactive Search
  *  Version 5
  *
  *  Copyright (c) 2007, 2009 Carnegie Mellon University
  *  All rights reserved.
  *
  *  This software is distributed under the terms of the Eclipse Public
  *  License, Version 1.0 which can be found in the file named LICENSE.
  *  ANY USE, REPRODUCTION OR DISTRIBUTION OF THIS SOFTWARE CONSTITUTES
  *  RECIPIENT'S ACCEPTANCE OF THIS AGREEMENT
  */
 
 package edu.cmu.cs.diamond.opendiamond;
 
 import java.util.*;
 
 /**
  * A single result from a {@link Search}, consisting of key-value pairs
  * (attributes).
  * 
  */
 public class Result {
     final private Map<String, byte[]> attributes = new HashMap<String, byte[]>();
 
     private final String hostname;
 
     Result(Map<String, byte[]> attributes, String hostname) {
         this.attributes.putAll(attributes);
 
         this.hostname = hostname;
     }
 
     /**
      * Gets the "data" attribute of this result. Equivalent to
     * <code>getData("")</code>.
      * 
      * @return a byte array with the data of this result
      */
     public byte[] getData() {
         return getValue("");
     }
 
     /**
      * Gets the value associated with a particular key.
      * 
      * @param key
      *            the name of the attribute to get the value for
      * @return the value
      */
     public byte[] getValue(String key) {
         byte[] v = attributes.get(key);
         return Arrays.copyOf(v, v.length);
     }
 
     /**
      * Gets the keys of this result, for use in <code>getValue</code>.
      * 
      * @return a set of keys
      */
     public Set<String> getKeys() {
         return Collections.unmodifiableSet(attributes.keySet());
     }
 
     @Override
     public String toString() {
         StringBuilder sb = new StringBuilder();
         sb.append("Result [");
 
         for (String name : getKeys()) {
             byte value[] = getValue(name);
             sb.append(" '" + name + "'");
             if (value.length == 0) {
                 sb.append(":" + "''");
             } else if (name.endsWith(".int")) {
                 sb.append(":" + Util.extractInt(value));
             } else if (name.endsWith("-Name")) {
                 sb.append(":'" + Util.extractString(value) + "'");
             } else if (name.endsWith(".time")) {
                 sb.append(":" + Util.extractLong(value));
             }
             sb.append(" (" + value.length + ")");
         }
         sb.append(" ]");
         return sb.toString();
     }
 
     /**
      * Gets the "server name" of this result. The server name is a
     * server-defined string stored in the <code>Device-Name</code> attribute.
      * 
      * @return the server-defined server name of this result
      */
     public String getServerName() {
         return Util.extractString(getValue("Device-Name"));
     }
 
     /**
     * Gets the "name" of this result. The name is a server-defined string
     * stored in the <code>Display-Name</code> attribute.
      * 
      * @return the server-defined name of this result
      */
     public String getName() {
         return Util.extractString(getValue("Display-Name"));
     }
 
     String getObjectID() {
         return Util.extractString(getValue("_ObjectID"));
     }
 
     String getHostname() {
         return hostname;
     }
 }
