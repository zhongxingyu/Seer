 package fedora.server.storage.types;
 
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 /**
  *
  * <p><b>Title:</b> DigitalObject.java</p>
  * <p><b>Description:</b> A holder of Fedora digital object information.</p>
  *
  * <p>A DigitalObject instance may be used by DOReader and DOWriter instances
  * as temporary storage for an object's attributes and components.</p>
  *
  * <p>Implementations of this interface are responsible for temporary
  * storage of these items, by whatever mechanism they deem fit.  The most
  * obvious implementation would simply store everything in memory.</p>
  *
  * <p>Implementations of this interface are <b>not</b> responsible for any sort of
  * validation on these items, or serialization/deserialization to/from specific
  * formats.</p>
  *
  * -----------------------------------------------------------------------------
  *
  * <p><b>License and Copyright: </b>The contents of this file are subject to the
  * Mozilla Public License Version 1.1 (the "License"); you may not use this file
  * except in compliance with the License. You may obtain a copy of the License
  * at <a href="http://www.mozilla.org/MPL">http://www.mozilla.org/MPL/.</a></p>
  *
  * <p>Software distributed under the License is distributed on an "AS IS" basis,
  * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
  * the specific language governing rights and limitations under the License.</p>
  *
  * <p>The entire file consists of original code.  Copyright &copy; 2002-2004 by The
  * Rector and Visitors of the University of Virginia and Cornell University.
  * All rights reserved.</p>
  *
  * -----------------------------------------------------------------------------
  *
  * @author cwilper@cs.cornell.edu
  * @version $Id$
  */
 public interface DigitalObject {
 
     public static int FEDORA_OBJECT=0;
     public static int FEDORA_BDEF_OBJECT=1;
     public static int FEDORA_BMECH_OBJECT=2;
 
     public int getFedoraObjectType();
 
     public void setFedoraObjectType(int t);
 
     public boolean isNew();
 
     public void setNew(boolean isNew);
 
     /**
      * Gets the pid.
      *
      * @return The pid, or null if it hasn't been set.
      */
     public String getPid();
 
     /**
      * Sets the pid.
      *
      * @param pid The pid.
      */
     public void setPid(String pid);
     
 	/**
 	 * Gets the object URI.
 	 *
 	 * @return The URI, or null if it hasn't been set.
 	 */
 	public String getURI();
 
 	/**
 	 * Sets the object URI.
 	 *
 	 * @param uri The URI.
 	 */
 	public void setURI(String uri);
 
     /**
      * Gets the state.
      *
      * @return The state, or null if it hasn't been set.
      */
     public String getState();
 
     /**
      * Sets the state.
      *
      * @param state The state.
      */
     public void setState(String state);
 
     /**
      * Gets the userid of the user who owns the object.
      *
      * @return The userid
      */
     public String getOwnerId();
 
     /**
      * Sets the owner of the object.
      *
      * @param user The userid.
      */
     public void setOwnerId(String user);
 
     /**
      * Gets the label.
      *
      * @return The label, or null if it hasn't been set.
      */
     public String getLabel();
 
     /**
      * Sets the label.
      *
      * @param label The label.
      */
     public void setLabel(String label);
 
     /**
      * Gets the content model id.
      *
      * @return The content model id.
      */
     public String getContentModelId();
 
     /**
      * Sets the content model id.
      *
      * @param id The content model id.
      */
     public void setContentModelId(String id);
 
     /**
      * Gets the date the object was created.
      *
      * @return The date, or null if it hasn't been set.
      */
     public Date getCreateDate();
 
     /**
      * Sets the date the object was created.
      *
      * @param createDate The date.
      */
     public void setCreateDate(Date createDate);
 
     /**
      * Gets the date the object was last modified.
      *
      * @return The date, or null if it hasn't been set.
      */
     public Date getLastModDate();
 
     /**
      * Sets the date the object was last modified.
      *
      * @param lastModDate The date.
      */
     public void setLastModDate(Date lastModDate);
 
     /**
      * Sets the preferred namespace uri-to-prefix map for XML renditions of
      * this object.
      *
      * @param mapping The uri-to-prefix mapping.
      */
     public void setNamespaceMapping(Map mapping);
 
     /**
      * Gets the preferred namespace uri-to-prefix map for XML renditions of
      * this object.
      *
      * @return The uri-to-prefix mapping.
      */
     public Map getNamespaceMapping();
 
     /**
      * Gets this object's mutable List of AuditRecord objects.
      *
      * @return The List of AuditRecords, possibly of zero size but never null.
      */
     public List getAuditRecords();
 
     /**
     * Gets an Iterator over the datastream ids in this object.
      * <p></p>
      * The Iterator is not tied to the underlying Collection and cannot
      * be used to remove datastreams.
      *
      * @return A new Iterator of datastream ids, possibly of zero size but
      *         never null.
      */
     public Iterator datastreamIdIterator();
 
     /**
     * Gets a mutable List of that consists of versions of the same 
     * datastream that is identified by the requested datastream
     * identifier.  
      *
      * @param id The datastream id.
      * @return The list, possibly of zero size but never null.
      */
     public List datastreams(String id);
 
     /**
     * Gets an Iterator over the disseminator ids in this object.
      * <p></p>
      * The Iterator is not tied to the underlying Collection and cannot
      * be used to remove datastreams.
      *
      * @return A new Iterator of disseminator ids, possibly of zero size but
      *         never null.
      */
     public Iterator disseminatorIdIterator();
 
     /**
     * Gets a mutable List that consists of versions of the same
     * disseminator which is identified by the requested disseminator
     * identifier. 
      *
     * @param id The disseminator id.
      * @return The list, possibly of zero size but never null.
      */
     public List disseminators(String id);
 
     /**
      * Generate a unique id for a datastream.
      */
     public String newDatastreamID();
 
     /**
      * Generate a unique id for a datastream version.
      */
     public String newDatastreamID(String dsID);
 
     /**
      * Generate a unique id for a disseminator.
      */
     public String newDisseminatorID();
 
     /**
      * Generate a unique id for a disseminator version.
      */
     public String newDisseminatorID(String dissID);
 
     /**
      * Generate a unique id for a datastreamBindingMap.
      */
     public String newDatastreamBindingMapID();
 
     /**
      * Generate a unique id for an audit record.
      */
     public String newAuditRecordID();
     
 	/**
 	 * Sets an extended property on the object.
 	 *
 	 * @param propName The property name, either a string, or URI as string.
 	 */
 	public void setExtProperty(String propName, String propValue);
 
 	/**
 	 * Gets an extended property value, given the property name.
 	 *
 	 * @return The property value.
 	 */
 	public String getExtProperty(String propName);
 	
 
 	/**
 	 * Gets a Map containing all of the extended properties
 	 * on the object.  Map key is property name.
 	 *
 	 * @return The property Map.
 	 */	
 	public Map getExtProperties();
 }
