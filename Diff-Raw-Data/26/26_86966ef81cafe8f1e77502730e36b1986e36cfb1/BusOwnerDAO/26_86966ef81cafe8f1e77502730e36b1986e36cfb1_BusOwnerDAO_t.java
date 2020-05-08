 package com.janrain.backplane2.server.dao;
 
import com.janrain.backplane2.server.config.BusConfig2;
 import com.janrain.backplane2.server.config.User;
 import com.janrain.backplane2.server.config.Backplane2Config;
 import com.janrain.commons.supersimpledb.SimpleDBException;
 import com.janrain.commons.supersimpledb.SuperSimpleDB;
import org.apache.log4j.Logger;

import java.util.List;
 
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
        try {
            logger.info("=== BEGIN BUS OWNER DELETE ===");
            superSimpleDB.delete(bpConfig.getTableName(BP_BUS_OWNERS), id);
            // delete all associated buses
            BusDAO busDao = new BusDAO(superSimpleDB, bpConfig);
            for (BusConfig2 bus:busDao.retrieveBuses(id)) {
                busDao.delete(bus.getIdValue());
            }
            logger.info("Bus owner " + id + " deleted successfully");
            logger.info("=== END BUS OWNER DELETE ===");
        } catch (SimpleDBException sdbe) {
            logger.error("An exception occurred during an atomic operation.  Corruption may have occurred while removing bus owner: " + id);
            throw sdbe;
        }
     }
 
     public User retrieveBusOwner(String userId) throws SimpleDBException {
         return superSimpleDB.retrieve(bpConfig.getTableName(BP_BUS_OWNERS), User.class, userId);
     }
 
    // - PRIVATE

    private static final Logger logger = Logger.getLogger(BusOwnerDAO.class);

 }
