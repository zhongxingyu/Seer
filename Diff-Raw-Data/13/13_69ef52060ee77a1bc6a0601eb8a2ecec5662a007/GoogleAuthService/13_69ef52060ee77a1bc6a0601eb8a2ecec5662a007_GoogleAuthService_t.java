 package com.britesnow.samplesocial.service;
 
 import com.britesnow.samplesocial.dao.SocialIdEntityDao;
import com.britesnow.samplesocial.entity.Service;
 import com.britesnow.samplesocial.entity.SocialIdEntity;
 import com.google.inject.Inject;
 import com.google.inject.Singleton;
 
 
 @Singleton
 public class GoogleAuthService implements AuthService {
     @Inject
     private SocialIdEntityDao socialIdEntityDao;
 
     @Override
     public SocialIdEntity getSocialIdEntity(Long userId) {
        SocialIdEntity socialId = socialIdEntityDao.getSocialdentity(userId, Service.Google);
         if (socialId != null) {
             if (socialId.getTokenDate().getTime() > System.currentTimeMillis()) {
                 return socialId;
             }
         }
         //if result is null, need redo auth
         return null;
     }
 
 }
