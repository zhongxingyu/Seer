 package org.sankozi.rogueland.model.guid;
 
 /**
 * Object with globally unique id.
 * GUIDs are generated through GuidGenerator.
  * @Author sankozi
  **/
 public interface Guid {
     /**
     * Returns GUID, each instance of Guid must always return the same value
      * @return guid
      */
     int getGuid();
 }
