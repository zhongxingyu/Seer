 package com.janrain.backplane2.server.dao;
 
 import com.janrain.backplane2.server.config.User;
 import com.janrain.backplane2.server.config.Backplane2Config;
 import com.janrain.commons.supersimpledb.SimpleDBException;
 import com.janrain.commons.supersimpledb.SuperSimpleDB;
 
 import static com.janrain.backplane2.server.config.Backplane2Config.SimpleDBTables.BP_BUS_OWNERS;
 
 /**
  * @author Johnny Bufu
  */
 public class BusOwnerDAO extends DAO {
 
     BusOwnerDAO(SuperSimpleDB superSimpleDB, Backplane2Config bpConfig) {
         super(superSimpleDB, bpConfig);
     }
 
     @Override
     public void persist(Object user) throws SimpleDBException {
         superSimpleDB.store(bpConfig.getTableName(BP_BUS_OWNERS), User.class, (User) user);
     }
 
     @Override
     public void delete(String id) throws SimpleDBException {
        superSimpleDB.delete(bpConfig.getTableName(BP_BUS_OWNERS), id);
     }
 
     public User retrieveBusOwner(String userId) throws SimpleDBException {
         return superSimpleDB.retrieve(bpConfig.getTableName(BP_BUS_OWNERS), User.class, userId);
     }
 
 }
