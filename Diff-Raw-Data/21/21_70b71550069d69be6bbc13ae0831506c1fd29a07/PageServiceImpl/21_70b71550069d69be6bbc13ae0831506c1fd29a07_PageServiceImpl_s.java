 package com.site2go.services.impl;
 
 import com.site2go.dao.entities.PageEntity;
 import com.site2go.dao.entities.SiteEntity;
 import com.site2go.dao.repositories.PageRepository;
 import com.site2go.dao.repositories.SiteRepository;
 import com.site2go.dto.Page;
 import com.site2go.dto.Site;
 import com.site2go.services.PageService;
 import org.dozer.Mapper;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 @Service
 public class PageServiceImpl implements PageService {
     private PageRepository pageRepository;
     private SiteRepository siteRepository;
     private Mapper beanMapper;
 
     @Override
     public Page getPageBySlug(Site site, String slug) {
         SiteEntity siteEntity = this.siteRepository.findByDomain(site.getDomain());
         PageEntity pageEntity = this.pageRepository.findBySiteAndSlug(siteEntity.getId(), slug);
         if(pageEntity == null) return null;
         return this.beanMapper.map(pageEntity, Page.class);
     }
 
     @Autowired
     public void setPageRepository(PageRepository pageRepository) {
         this.pageRepository = pageRepository;
     }
 
     @Autowired
     public void setSiteRepository(SiteRepository siteRepository) {
         this.siteRepository = siteRepository;
     }
 
     @Autowired
     public void setBeanMapper(Mapper beanMapper) {
         this.beanMapper = beanMapper;
     }
 }
