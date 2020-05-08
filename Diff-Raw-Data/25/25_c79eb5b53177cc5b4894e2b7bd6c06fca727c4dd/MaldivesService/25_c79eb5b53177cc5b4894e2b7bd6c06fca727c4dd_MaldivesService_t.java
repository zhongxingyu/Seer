 package com.cqlybest.common.service;
 
 import java.util.Date;
 import java.util.List;
 import java.util.UUID;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.cqlybest.common.bean.MaldivesRoom;
 import com.cqlybest.common.bean.MaldivesSeaIsland;
 import com.cqlybest.common.dao.ImageDao;
 import com.cqlybest.common.dao.MaldivesDao;
 
 @Service
 public class MaldivesService {
 
   @Autowired
   private MaldivesDao maldivesDao;
   @Autowired
   private ImageDao imageDao;
 
   @Transactional
   public void add(MaldivesSeaIsland island) {
     island.setId(UUID.randomUUID().toString());
     Date now = new Date();
     island.setCreatedTime(now);
     island.setLastUpdated(now);
     maldivesDao.saveOrUpdate(island);
   }
 
   @Transactional
   public void add(MaldivesRoom room) {
     maldivesDao.saveOrUpdate(room);
   }
 
   @Transactional
   public void update(String id, String name, Object value) {
     maldivesDao.update(id, name, value);
   }
 
   @Transactional
   public void update(String[] ids, String name, Object value) {
     maldivesDao.update(ids, name, value);
   }
 
   @Transactional
   public void updateRoom(Integer id, String name, Object value) {
     maldivesDao.updateRoom(id, name, value);
   }
 
   @Transactional
   public void delete(String[] ids) {
     maldivesDao.delete(ids);
   }
 
   public MaldivesSeaIsland get(String id) {
     MaldivesSeaIsland island = maldivesDao.findById(id);
    List<MaldivesRoom> rooms = maldivesDao.getRooms(id);
    for (MaldivesRoom room : rooms) {
      room.setPictures(imageDao.queryImagesWithoutData("maldives-room-picture", room.getId()
          .toString()));
    }
    island.setRooms(rooms);
    island.setPictures(imageDao.queryImagesWithoutData("maldives-island-poster", id));
     return island;
   }
 
   public Long total() {
     return maldivesDao.total();
   }
 
   public List<MaldivesSeaIsland> list(int page, int pageSize) {
     return maldivesDao.find(page, pageSize);
   }
 
   public List<MaldivesRoom> getRooms(String islandId) {
     List<MaldivesRoom> rooms = maldivesDao.getRooms(islandId);
     for (MaldivesRoom room : rooms) {
       room.setPictures(imageDao.queryImagesWithoutData("maldives-room-picture", islandId));
     }
     return rooms;
   }
 
 }
