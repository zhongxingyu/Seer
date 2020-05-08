 package com.pa165.bookingmanager.service.impl;
 
 import com.pa165.bookingmanager.convertor.impl.ReservationConvertorImpl;
 import com.pa165.bookingmanager.dao.ReservationDao;
 import com.pa165.bookingmanager.dto.ReservationDto;
 import com.pa165.bookingmanager.entity.ReservationEntity;
 import com.pa165.bookingmanager.service.ReservationService;
 import org.hibernate.criterion.Criterion;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import java.util.List;
 
 /**
 * @author Jana Polakova, Jakub Polak, Jan Hrubes
  */
 @Service("reservationService")
 @Transactional(readOnly = true)
 public class ReservationServiceImpl implements ReservationService
 {
     @Autowired
     ReservationDao reservationDao;
 
     @Autowired
     ReservationConvertorImpl reservationConvertor;
 
     /**
      * {@inheritDoc}
      */
     @Override
     public List<ReservationDto> findAll() {
         List<ReservationEntity> reservationEntities = reservationDao.findAll();
         List<ReservationDto> reservationDtos = null;
 
         if (reservationEntities != null){
             reservationDtos = reservationConvertor.convertEntityListToDtoList(reservationEntities);
         }
 
         return reservationDtos;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public List<ReservationDto> findByCriteria(Criterion criterion) {
         if (criterion == null){
             throw new IllegalArgumentException("Criterion can't be null.");
         }
 
        List<ReservationEntity> reservationEntities = reservationDao.findByCriteria(criterion);
         List<ReservationDto> reservationDtos = null;
 
         if (reservationEntities != null){
             reservationDtos = reservationConvertor.convertEntityListToDtoList(reservationEntities);
         }
 
         return reservationDtos;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public ReservationDto find(Long id) {
         if (id == null){
             throw new IllegalArgumentException("Id can't be null.");
         }
 
         ReservationEntity reservationEntity = reservationDao.find(id);
         ReservationDto reservationDto = null;
 
         if (reservationEntity != null){
             reservationDto = reservationConvertor.convertEntityToDto(reservationEntity);
         }
 
         return reservationDto;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @Transactional(readOnly = false)
     public void create(ReservationDto reservationDto) {
         if (reservationDto == null){
             throw new IllegalArgumentException("ReservationDto can't be null.");
         }
 
         reservationDao.create(reservationConvertor.convertDtoToEntity(reservationDto));
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @Transactional(readOnly = false)
     public void update(ReservationDto reservationDto) {
         if (reservationDto == null){
             throw new IllegalArgumentException("ReservationDto can't be null.");
         }
 
         ReservationEntity reservationEntity = reservationDao.find(reservationDto.getId());
 
         if (reservationEntity != null){
             reservationConvertor.convertDtoToEntity(reservationDto, reservationEntity);
             reservationDao.update(reservationEntity);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @Transactional(readOnly = false)
     public void delete(ReservationDto reservationDto) {
         if (reservationDto == null){
             throw new IllegalArgumentException("ReservationDto can't be null.");
         }
 
         ReservationEntity reservationEntity = reservationDao.find(reservationDto.getId());
 
         if (reservationEntity != null){
             reservationDao.delete(reservationEntity);
         }
     }
 }
