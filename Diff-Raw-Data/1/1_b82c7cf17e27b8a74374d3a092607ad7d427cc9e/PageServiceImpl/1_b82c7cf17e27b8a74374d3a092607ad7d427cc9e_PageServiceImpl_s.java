 /*
  * cilla - Blog Management System
  *
  * Copyright (C) 2012 Richard "Shred" Körber
  *   http://cilla.shredzone.org
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as published
  * by the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.shredzone.cilla.service.impl;
 
 import java.io.IOException;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.regex.Pattern;
 
 import javax.activation.DataSource;
 import javax.annotation.Resource;
 
 import org.shredzone.cilla.core.datasource.ResourceDataSource;
 import org.shredzone.cilla.core.event.Event;
 import org.shredzone.cilla.core.event.EventService;
 import org.shredzone.cilla.core.event.EventType;
 import org.shredzone.cilla.core.model.Medium;
 import org.shredzone.cilla.core.model.Page;
 import org.shredzone.cilla.core.model.Section;
 import org.shredzone.cilla.core.model.Store;
 import org.shredzone.cilla.core.model.User;
 import org.shredzone.cilla.core.repository.MediumDao;
 import org.shredzone.cilla.core.repository.PageDao;
 import org.shredzone.cilla.core.repository.StoreDao;
 import org.shredzone.cilla.core.repository.UserDao;
 import org.shredzone.cilla.service.CommentService;
 import org.shredzone.cilla.service.PageService;
 import org.shredzone.cilla.service.SecurityService;
 import org.shredzone.cilla.service.resource.ImageTools;
 import org.shredzone.cilla.ws.ImageProcessing;
 import org.shredzone.cilla.ws.SectionFacade;
 import org.shredzone.cilla.ws.exception.CillaServiceException;
 import org.springframework.cache.annotation.CacheEvict;
 import org.springframework.scheduling.annotation.Scheduled;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.util.FileCopyUtils;
 
 /**
  * Default implementation of {@link PageService}.
  *
  * @author Richard "Shred" Körber
  */
 @Service
 @Transactional
 public class PageServiceImpl implements PageService {
 
     private @Resource UserDao userDao;
     private @Resource PageDao pageDao;
     private @Resource MediumDao mediumDao;
     private @Resource StoreDao storeDao;
     private @Resource ImageTools imageProcessor;
     private @Resource CommentService commentService;
     private @Resource EventService eventService;
     private @Resource SecurityService securityService;
     private @Resource SectionFacade sectionFacade;
 
     @Override
     public Page createNew() {
         Date now = new Date();
         User user = userDao.fetch(securityService.getAuthenticatedUser().getUserId());
 
         Page page = new Page();
         page.setLanguage(user.getLanguage());
         page.setCreator(user);
         page.setCreation(now);
         page.setModification(now);
         page.setCommentable(true);
         return page;
     }
 
     @Override
     @CacheEvict(value = "tagCloud", allEntries = true)
     public void create(Page page) {
         pageDao.persist(page);
         eventService.fireEvent(new Event(EventType.PAGE_NEW).value(page));
     }
 
     @Override
     @CacheEvict(value = "tagCloud", allEntries = true)
     public void update(Page page) {
         page.setModification(new Date());
         eventService.fireEvent(new Event(EventType.PAGE_UPDATE).value(page));
     }
 
     @Override
     @CacheEvict(value = "tagCloud", allEntries = true)
     public void remove(Page page) throws CillaServiceException {
         commentService.removeAll(page);
 
         Set<Section> removables = new HashSet<Section>(page.getSections());
         for (Section section : removables) {
             sectionFacade.deleteSection(section);
         }
 
         Set<Medium> removableMedia = new HashSet<Medium>(mediumDao.fetchAll(page));
         for (Medium medium : removableMedia) {
             removeMedium(page, medium);
         }
 
         pageDao.delete(page);
         eventService.fireEvent(new Event(EventType.PAGE_DELETE).value(page));
     }
 
     @Override
     @Scheduled(cron = "${cron.pagePublishEvent}")
     public void updatePublishedState() {
         for (Page page : pageDao.fetchBadPublishState()) {
             boolean before = page.isPublishedState();
             boolean after = page.isPublished();
 
             if (before != after) {
                 EventType type = (after ? EventType.PAGE_PUBLISH : EventType.PAGE_UNPUBLISH);
                 eventService.fireEvent(new Event(type).value(page));
                 page.setPublishedState(after);
             }
         }
     }
 
     @Override
     public boolean isVisible(Page page) {
         if (!page.isPublished()) {
             securityService.requireRole("ROLE_PREVIEW");
         }
         return true;
     }
 
     @Override
     public boolean isAcceptedResponse(Page page, String response) {
         if (!page.isRestricted()) {
             // If the page is not restricted, the response is always acceptable
             return true;
         }
 
         Pattern pattern = Pattern.compile(page.getResponsePattern(), Pattern.CASE_INSENSITIVE);
         return (pattern.matcher(response).matches());
     }
 
     @Override
     public void addMedium(Page page, Medium medium, DataSource source) throws CillaServiceException {
         try {
             medium.setPage(page);
 
             Store store = medium.getImage();
             store.setContentType(source.getContentType());
            store.setName(source.getName());
             store.setLastModified(new Date());
 
             mediumDao.persist(medium);
 
             ResourceDataSource ds = storeDao.access(store);
             FileCopyUtils.copy(source.getInputStream(), ds.getOutputStream());
         } catch (IOException ex) {
             throw new CillaServiceException("Could not set medium", ex);
         }
     }
 
     @Override
     @CacheEvict(value = "processedImages", allEntries = true)
     public void updateMedium(Page page, Medium medium, DataSource source) throws CillaServiceException {
         if (!medium.getPage().equals(page)) {
             throw new IllegalArgumentException("Medium id " + medium.getId() + " does not belong to Page id " + page.getId());
         }
 
         if (source != null) {
             try {
                 Store store = medium.getImage();
                 store.setContentType(source.getContentType());
                 store.setName(source.getName());
                 store.setLastModified(new Date());
 
                 ResourceDataSource ds = storeDao.access(store);
                 FileCopyUtils.copy(source.getInputStream(), ds.getOutputStream());
             } catch (IOException ex) {
                 throw new CillaServiceException("Could not set medium", ex);
             }
         }
     }
 
     @Override
     @CacheEvict(value = "processedImages", allEntries = true)
     public void removeMedium(Page page, Medium medium) throws CillaServiceException {
         if (!medium.getPage().equals(page)) {
             throw new IllegalArgumentException("Medium id " + medium.getId() + " does not belong to Page id " + page.getId());
         }
 
         try {
             storeDao.access(medium.getImage()).delete();
             mediumDao.delete(medium);
         } catch (IOException ex) {
             throw new CillaServiceException("Could not delete medium id " + medium.getId(), ex);
         }
     }
 
     @Override
     public Medium createNewMedium() {
         Medium medium = new Medium();
         medium.setCreatedBy(userDao.fetch(securityService.getAuthenticatedUser().getUserId()));
         return medium;
     }
 
     @Override
     public ResourceDataSource getMediumImage(Medium medium, ImageProcessing process) throws CillaServiceException {
         try {
             ResourceDataSource ds = storeDao.access(medium.getImage());
             if (process != null) {
                 ds = imageProcessor.processImage(ds, process);
             }
             return ds;
         } catch (IOException ex) {
             throw new CillaServiceException(ex);
         }
     }
 
 }
