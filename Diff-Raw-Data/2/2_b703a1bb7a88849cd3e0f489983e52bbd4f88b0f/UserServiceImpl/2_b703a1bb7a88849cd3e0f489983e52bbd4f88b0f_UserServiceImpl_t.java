 package com.pa165.bookingmanager.service.impl;
 
 import com.pa165.bookingmanager.convertor.impl.UserConvertorImpl;
 import com.pa165.bookingmanager.dao.UserDao;
 import com.pa165.bookingmanager.dto.UserDto;
 import com.pa165.bookingmanager.entity.UserEntity;
 import com.pa165.bookingmanager.service.UserService;
 import org.hibernate.criterion.Criterion;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import java.util.List;
 
 /**
  * @author Jana Polakova, Jakub Polak
  */
 @Service("userService")
 @Transactional(readOnly = true)
 public class UserServiceImpl implements UserService
 {
     @Autowired
     UserDao userDao;
 
     @Autowired
     UserConvertorImpl userConvertor;
 
     /**
      * {@inheritDoc}
      */
     @Override
     public List<UserDto> findAll() {
         List<UserEntity> userEntities = userDao.findAll();
         List<UserDto> userDtos = null;
 
         if (userEntities != null){
             userDtos = userConvertor.convertEntityListToDtoList(userEntities);
         }
 
         return userDtos;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public List<UserDto> findByCriteria(Criterion criterion) {
         if (criterion == null){
             throw new IllegalArgumentException("Id can't be null.");
         }
 
        List<UserEntity> userEntities = userDao.findByCriteria(criterion);
         List<UserDto> userDtos = null;
 
         if (userEntities != null){
             userDtos = userConvertor.convertEntityListToDtoList(userEntities);
         }
 
         return userDtos;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public UserDto findOneByEmail(String email){
         if (email == null){
             throw new IllegalArgumentException("Email can't be null.");
         }
 
         UserEntity userEntity = userDao.findOneByEmail(email);
         UserDto userDto = null;
 
         if (userEntity != null){
             userDto = userConvertor.convertEntityToDto(userEntity);
         }
 
         return userDto;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public UserDto find(Long id) {
         if (id == null){
             throw new IllegalArgumentException("Id can't be null.");
         }
 
         UserEntity userEntity = userDao.find(id);
         UserDto userDto = null;
 
         if (userEntity != null){
             userDto = userConvertor.convertEntityToDto(userEntity);
         }
 
         return userDto;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @Transactional(readOnly = false)
     public void create(UserDto userDto) {
         if (userDto == null){
             throw new IllegalArgumentException("UserDto can't be null.");
         }
 
         userDao.create(userConvertor.convertDtoToEntity(userDto));
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @Transactional(readOnly = false)
     public void update(UserDto userDto) {
         if (userDto == null){
             throw new IllegalArgumentException("UserDto can't be null.");
         }
 
         UserEntity userEntity = userDao.find(userDto.getId());
 
         if (userEntity != null){
             userConvertor.convertDtoToEntity(userDto, userEntity);
             userDao.update(userEntity);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @Transactional(readOnly = false)
     public void delete(UserDto userDto) {
         if (userDto == null){
             throw new IllegalArgumentException("UserDto can't be null.");
         }
 
         UserEntity userEntity = userDao.find(userDto.getId());
 
         if (userEntity != null){
             userDao.delete(userEntity);
         }
     }
 }
