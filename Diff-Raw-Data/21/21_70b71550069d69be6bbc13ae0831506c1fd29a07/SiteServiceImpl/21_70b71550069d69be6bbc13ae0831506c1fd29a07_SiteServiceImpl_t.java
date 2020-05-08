 package com.site2go.services.impl;
 
 import com.google.common.base.Function;
 import com.google.common.collect.Collections2;
 import com.google.common.collect.Lists;
 import com.site2go.dao.entities.SiteEntity;
 import com.site2go.dao.entities.UserEntity;
 import com.site2go.dao.repositories.SiteRepository;
 import com.site2go.dao.repositories.UserRepository;
 import com.site2go.dto.Site;
 import com.site2go.dto.User;
 import com.site2go.services.SiteService;
 import org.dozer.Mapper;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.dao.EmptyResultDataAccessException;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import javax.annotation.Nullable;
 import java.util.List;
 
 @Service
@Transactional
 public class SiteServiceImpl implements SiteService {
     private SiteRepository siteRepository;
     private UserRepository userRepository;
     private Mapper mapper;
 
     @Override
     public Site getSiteByDomain(String domain) {
         SiteEntity siteEntity;
         try {
             siteEntity = this.siteRepository.findByDomain(domain);
         }
         catch(EmptyResultDataAccessException erdae) {
             return null;
         }
 
         return this.mapper.map(siteEntity, Site.class);
     }
 
     @Override
     public List<Site> getSitesByUser(User user) {
         Iterable<SiteEntity> siteEntities;
         if(user.getSuperAdmin()) {
             siteEntities = this.siteRepository.list();
         }
         else {
             UserEntity userEntity = this.userRepository.findByEmail(user.getEmail());
             siteEntities = userEntity.getSites();
         }
 
         List<Site> sites = Lists.newArrayList();
         for(SiteEntity siteEntity : siteEntities) {
             sites.add(this.mapper.map(siteEntity, Site.class));
         }
         return sites;
         /*
             Lol, times like this do make me miss Groovy. Bring on JDK8 tho! Gimme them lambda's baby!
         return Lists.newArrayList(Collections2.transform(userEntity.getSites(), new Function<SiteEntity, Site>() {
             public Site apply(@Nullable SiteEntity input) {
                 return mapper.map(input, Site.class);
             }
         }));
         */
     }
 
     @Autowired
     public void setSiteRepository(SiteRepository siteRepository) {
         this.siteRepository = siteRepository;
     }
 
     @Autowired
     public void setUserRepository(UserRepository userRepository) {
         this.userRepository = userRepository;
     }
 
     @Autowired
     public void setMapper(Mapper mapper) {
         this.mapper = mapper;
     }
 }
