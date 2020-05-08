 package com.site2go.services.impl;
 
 import com.site2go.dao.entities.UserEntity;
 import com.site2go.dao.repositories.UserRepository;
 import com.site2go.dto.User;
 import com.site2go.services.UserService;
 import org.dozer.Mapper;
 import org.mindrot.jbcrypt.BCrypt;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.dao.EmptyResultDataAccessException;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 @Service
 public class UserServiceImpl implements UserService {
     private Mapper beanMapper;
     private UserRepository userRepository;
 
     @Override
    @Transactional
     public User login(String email, String password) {
         UserEntity userEntity;
         try {
             userEntity = this.userRepository.findByEmail(email);
         }
         catch(EmptyResultDataAccessException erdae) {
             return null;
         }
 
         if(!BCrypt.checkpw(password, userEntity.getPassword())) {
             return null;
         }
 
         return this.beanMapper.map(userEntity, User.class);
     }
 
     @Autowired
     public void setUserRepository(UserRepository userRepository) {
         this.userRepository = userRepository;
     }
 
     @Autowired
     public void setBeanMapper(Mapper beanMapper) {
         this.beanMapper = beanMapper;
     }
 }
