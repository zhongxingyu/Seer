 /*
  * Copyright (c) 2011, dan.clark@nekocode.org
  *
  * Licensed under FreeBSD license.  See README for details.
  */
 
 package org.nekocode.itunes.remote.connection;
 
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
 
 import static java.lang.String.format;
 
 /**
  * Data returned from iTunes after a query has been executed.  The data is formatted in a tree structure,
  * with individual nodes identified by a 4-character string contentCode.  It is possible to have multiple
  * internal nodes with the same path and contentCode.  It is not possible to have multiple leaf nodes with
  * the same path and contentCode, although only a few contentCodes do this.
  */
 public class ITunesRemoteResponse {
     private static final Logger log = LogManager.getLogger(ITunesRemoteResponse.class);
 
     private Map<ContentCode, ITunesRemoteResponse> childNodes;
     private Map<ContentCode, List<ITunesRemoteResponse>> childMultiNodes;
     private Map<ContentCode, Object> leafNodes;
 
     /**
      * Creates an empty response object.
      */
     public ITunesRemoteResponse() {
         childNodes = new EnumMap<>(ContentCode.class);
         childMultiNodes = new EnumMap<>(ContentCode.class);
         leafNodes = new EnumMap<>(ContentCode.class);
     }
 
     /**
      * Add a branch node to this response.
      *
      * @param contentCode node contentCode
      * @param childNode child node to add
      */
     public void addChild(ContentCode contentCode, ITunesRemoteResponse childNode) {
         if (contentCode.isInternalMultiNodeType()) {
             if (!childMultiNodes.containsKey(contentCode)) {
                 childMultiNodes.put(contentCode, new ArrayList<ITunesRemoteResponse>());
             }
             childMultiNodes.get(contentCode).add(childNode);
         } else if (contentCode.isInternalNodeType()) {
             childNodes.put(contentCode, childNode);
         } else {
             log.error(contentCode + " is not a valid internal node contentCode");
             throw new RuntimeException(contentCode + " is not a valid internal node contentCode");
         }
     }
 
     /**
      * Returns a list of all internal nodes with the given contentCode.
      *
      * @param contentCodes node contentCodes
      * @return list of nodes matching this contentCode (never null)
      */
     public List<ITunesRemoteResponse> getMultiBranch(ContentCode... contentCodes) {
         if (!contentCodes[contentCodes.length - 1].isInternalMultiNodeType()) {
             log.warn("attempted to get " + Arrays.toString(contentCodes) + " as multi branch type");
         }
 
         ITunesRemoteResponse response = this;
         int i = 0;
         for (; i < contentCodes.length - 1; i++) {
             response = response.childNodes.get(contentCodes[i]);
         }
         if (response.childMultiNodes.containsKey(contentCodes[i])) {
             return response.childMultiNodes.get(contentCodes[i]);
         }
         return Collections.emptyList();
     }
 
     /**
      * Returns the internal node with the given contentCode.
      *
      * @param contentCodes node contentCodes
      * @return branch node, or empty node if branch does not exist (never null)
      */
     public ITunesRemoteResponse getBranch(ContentCode... contentCodes) {
         if (!contentCodes[contentCodes.length - 1].isInternalNodeType()) {
             log.warn("attempted to get " + Arrays.toString(contentCodes) + " as branch type");
         }
 
         ITunesRemoteResponse response = this;
         int i = 0;
         for (; i < contentCodes.length; i++) {
             response = response.childNodes.get(contentCodes[i]);
             if (response == null) {
                 return new ITunesRemoteResponse();
             }
         }
         return response;
     }
 
     /**
      * Add a leaf node containing data to this response.
      *
      * @param contentCode node contentCode
      * @param data data to store in leaf node
      */
     public void addChild(ContentCode contentCode, Object data) {
         if (data instanceof ITunesRemoteResponse) {
             addChild(contentCode, (ITunesRemoteResponse)data);
         } else {
             leafNodes.put(contentCode, data);
         }
     }
 
     /**
      * Returns the data from a specified leaf node as a String.
      *
      * @param contentCodes node contentCodes
      * @return String data.  Can cause ClassCastException if data is not a String
      */
     public String getString(ContentCode... contentCodes) {
         if (!contentCodes[contentCodes.length - 1].isStringType()) {
             log.warn("attempted to get " + Arrays.toString(contentCodes) + " as String type");
         }
 
         Object leaf = getLeaf(contentCodes);
         if (leaf == null) {
             return null;
         }
         return leaf.toString();
     }
 
     /**
      * Returns true if this response contains the specified leaf node.
      *
      * @param contentCodes node contentCodes
      * @return true if this response contains the specified leaf node and it is not empty
      */
     public boolean hasLeaf(ContentCode... contentCodes) {
         ITunesRemoteResponse response = this;
         int i = 0;
         for (; i < contentCodes.length - 1; i++) {
             response = response.childNodes.get(contentCodes[i]);
         }
         return response.leafNodes.containsKey(contentCodes[i]);
     }
 
     /**
      * Gets the leaf node without doing any object conversions.
      *
      * @param contentCodes node contentCodes
      * @return leaf node, or null if node doesn't exist
      */
     public Object getLeaf(ContentCode... contentCodes) {
         ITunesRemoteResponse response = this;
         int i = 0;
         for (; i < contentCodes.length - 1; i++) {
             response = response.childNodes.get(contentCodes[i]);
         }
         return response.leafNodes.get(contentCodes[i]);
     }
 
     public String getHexNumber(ContentCode... contentCodes) {
         return Long.toHexString(getLong(contentCodes)).toUpperCase();
     }
 
     public Number getNumber(ContentCode... contentCodes) {
         if (!contentCodes[contentCodes.length - 1].isIntegerType()) {
             log.warn("attempted to get " + Arrays.toString(contentCodes) + " as Number type");
         }
 
         return (Number) getLeaf(contentCodes);
     }
 
     public Long getLong(ContentCode... contentCode) {
         Number number = getNumber(contentCode);
         if (number == null)
             return null;
         return number.longValue();
     }
 
     public Integer getInt(ContentCode... contentCode) {
         Number number = getNumber(contentCode);
         if (number == null)
             return null;
         return number.intValue();
    }
 
     public Short getShort(ContentCode contentCode) {
         Number number = getNumber(contentCode);
         if (number == null)
             return null;
         return number.shortValue();
     }
 
     /**
      * Like {@link #getShort(ContentCode)}, but if the leaf node is null, return the defaultValue instead.
      * @param contentCode
      * @param defaultValue
      * @return
      */
     public short getShort(ContentCode contentCode, short defaultValue) {
         Number number = getNumber(contentCode);
         if (number == null)
             return defaultValue;
         return number.shortValue();
     }
 
     @Override
     public String toString() {
         return getDescription(0).toString();
     }
 
     /**
      * Generate a detailed description of this response.
      *
      * @param level starting indentation level
      * @return detailed description
      */
     private StringBuilder getDescription(int level) {
         StringBuilder sb = new StringBuilder();
 
         String newLine = format("%n");
         String indent = level == 0 ? "" : format("%" + (level * 4) + "s", "");
 
         // print all leaf nodes first
         for (Map.Entry<ContentCode, Object> leaf : leafNodes.entrySet()) {
             sb.append(indent);
             sb.append(leaf.getKey().id);
             sb.append("    ");
             Object value = leaf.getValue();
             if (value instanceof Number) {
                 sb.append(Long.toHexString(((Number)value).longValue()));
                 sb.append(" == ");
                 sb.append(value);
             } else if (value.getClass().isArray()) {
                 sb.append(Arrays.toString((byte[])value));
             } else {
                 sb.append(value);
             }
 
             sb.append("    (").append(leaf.getKey()).append(")");
             sb.append(newLine);
         }
 
         for (Map.Entry<ContentCode, ITunesRemoteResponse> internal : childNodes.entrySet()) {
             sb.append(indent);
             sb.append(internal.getKey().id);
             sb.append("    (").append(internal.getKey()).append(")");
             sb.append(newLine);
             sb.append(internal.getValue().getDescription(level + 1));
         }
 
         for (Map.Entry<ContentCode, List<ITunesRemoteResponse>> internal : childMultiNodes.entrySet()) {
             for (ITunesRemoteResponse response : internal.getValue()) {
                 sb.append(indent);
                 sb.append(internal.getKey().id);
                 sb.append("    (").append(internal.getKey()).append(")");
                 sb.append(newLine);
                 sb.append(response.getDescription(level + 1));
             }
         }
 
         return sb;
     }
 
     public Collection<ContentCode> getLeaves() {
         return leafNodes.keySet();
     }
 
     public Collection<ContentCode> getBranches() {
         return childNodes.keySet();
     }
 
     public Collection<ContentCode> getMultiBranches() {
         return childMultiNodes.keySet();
     }
 
     public boolean isEmpty() {
         return leafNodes.isEmpty() && childNodes.isEmpty() && childMultiNodes.isEmpty();
     }
 
     public boolean containsLeaf(ContentCode contentCode) {
         return leafNodes.containsKey(contentCode);
     }
 }
