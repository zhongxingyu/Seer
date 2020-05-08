 package com.pa165.bookingmanager.service.impl;
 
 import com.pa165.bookingmanager.convertor.impl.RoomConvertorImpl;
 import com.pa165.bookingmanager.dao.RoomDao;
 import com.pa165.bookingmanager.dto.RoomDto;
 import com.pa165.bookingmanager.entity.RoomEntity;
 import com.pa165.bookingmanager.service.RoomService;
 import org.hibernate.criterion.Criterion;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import java.util.List;
 
 /**
 * @author Jana Polakova, Jakub Polak, Jan Hrubes
  */
 @Service("roomService")
 @Transactional(readOnly = true)
 public class RoomServiceImpl implements RoomService
 {
     @Autowired
     RoomDao roomDao;
 
     @Autowired
     RoomConvertorImpl roomConvertor;
 
     /**
      * {@inheritDoc}
      */
     @Override
     public List<RoomDto> findAll() {
         List<RoomEntity> roomEntities = roomDao.findAll();
         List<RoomDto> roomDtos = null;
 
         if (roomEntities != null){
             roomDtos = roomConvertor.convertEntityListToDtoList(roomEntities);
         }
 
         return roomDtos;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public List<RoomDto> findByCriteria(Criterion criterion) {
         if (criterion == null){
             throw new IllegalArgumentException("Criterion can't be null.");
         }
 
        List<RoomEntity> roomEntities = roomDao.findByCriteria(criterion);
         List<RoomDto> roomDtos = null;
 
         if (roomEntities != null){
             roomDtos = roomConvertor.convertEntityListToDtoList(roomEntities);
         }
 
         return roomDtos;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public RoomDto find(Long id) {
         if (id == null){
             throw new IllegalArgumentException("Id can't be null.");
         }
 
         RoomEntity roomEntity = roomDao.find(id);
         RoomDto roomDto = null;
 
         if (roomEntity != null){
             roomDto = roomConvertor.convertEntityToDto(roomEntity);
         }
 
         return roomDto;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @Transactional(readOnly = false)
     public void create(RoomDto roomDto) {
         if (roomDto == null){
             throw new IllegalArgumentException("RoomDto can't be null.");
         }
 
         roomDao.create(roomConvertor.convertDtoToEntity(roomDto));
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @Transactional(readOnly = false)
     public void update(RoomDto roomDto) {
         if (roomDto == null){
             throw new IllegalArgumentException("RoomDto can't be null.");
         }
 
         RoomEntity roomEntity = roomDao.find(roomDto.getId());
 
         if (roomEntity != null){
             roomConvertor.convertDtoToEntity(roomDto, roomEntity);
             roomDao.update(roomEntity);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @Transactional(readOnly = false)
     public void delete(RoomDto roomDto) {
         if (roomDto == null){
             throw new IllegalArgumentException("RoomDto can't be null.");
         }
 
         RoomEntity roomEntity = roomDao.find(roomDto.getId());
 
         if (roomEntity != null){
             roomDao.delete(roomEntity);
         }
     }
 }
