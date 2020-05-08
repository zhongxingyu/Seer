 package org.nebulostore.appcore.model;
 
 import org.nebulostore.appcore.addressing.NebuloAddress;
 import org.nebulostore.appcore.addressing.ObjectId;
 import org.nebulostore.appcore.exceptions.NebuloException;
 
/**
 * @author rafalhryciuk
 * @author Bolek Kulbabinski
 */
 public interface NebuloObjectFactory {
 
   NebuloObject fetchExistingNebuloObject(NebuloAddress address) throws NebuloException;
 
   NebuloFile createNewNebuloFile();
 
   NebuloFile createNewNebuloFile(ObjectId objectId);
 
   NebuloFile createNewNebuloFile(NebuloAddress address);
 
   NebuloList createNewNebuloList();
 
   NebuloList createNewNebuloList(ObjectId objectId);
 
   NebuloList createNewNebuloList(NebuloAddress address);
 
 }
