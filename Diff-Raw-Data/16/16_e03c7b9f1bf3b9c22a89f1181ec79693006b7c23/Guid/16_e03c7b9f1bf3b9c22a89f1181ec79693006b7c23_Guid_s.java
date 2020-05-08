 package org.sankozi.rogueland.model.guid;
 
 /**
 * Object with globally unique id GUID across all object types
 * GUIDs are generated through
  * @Author sankozi
  **/
 public interface Guid {
     /**
     * Returns GUID, each instance of GUID must always return the same value
      * @return guid
      */
     int getGuid();
 }
