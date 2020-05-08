 package com.easytag.core.managers;
 
 import com.easytag.core.entity.jpa.Album;
 import com.easytag.core.entity.jpa.EasyTagFile;
 import com.easytag.core.entity.jpa.Photo;
 import com.easytag.core.enums.PhotoStatus;
 import com.easytag.exceptions.TagException;
 import com.easytag.utils.PreviewUtils;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import javax.ejb.EJB;
 import javax.ejb.Stateless;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.Query;
 
 /**
  *
  * @author rogvold
  */
 @Stateless
 public class PhotoManager implements PhotoManagerLocal {
 
     @PersistenceContext(unitName = "EasyTagCorePU")
     EntityManager em;
     
     @EJB
     AlbumManagerLocal aMan;
     
     @EJB
     FileManagerLocal  fMan;
 
     @Override
     public Photo getPhotoById(Long photoId) {
         if (photoId == null) {
             return null;
         }
         return em.find(Photo.class, photoId);
     }
 
     @Override
     public List<Photo> getPhotosInAlbum(Long albumId, boolean includeDeleted) {
         StringBuilder queryString = new StringBuilder("select p from Photo p where p.albumId = :albumId");
         if (!includeDeleted) {
             queryString.append(" and p.status <> :status");
         }
         queryString.append(" order by p.name");
         Query query = em.createQuery(queryString.toString()).setParameter("albumId", albumId);
         if (!includeDeleted) {
             query = query.setParameter("status", PhotoStatus.DELETED);
         }
         List<Photo> list = query.getResultList();
         if (list == null || list.isEmpty()) {
             return Collections.EMPTY_LIST;
         }
         return list;
     }
     
     @Override
     public Integer getPhotosAmount(Long albumId) throws TagException {
         Query q = em.createQuery("select count(p) from Photo p where p.albumId = :albumId").setParameter("albumId", albumId);
         return ((Long) q.getSingleResult()).intValue();
     }
 
     @Override
     public Photo addPhoto(Long userId, Long albumId, String name, String description, String tags, Long fileId, Long height, Long width) throws TagException {
         
         if (albumId == null) {
             throw new TagException("Album Id is not specified"); 
         }
         if (userId == null) {
             throw new TagException("User Id is not specified"); 
         }
         Album album = em.find(Album.class, albumId);
         
         if (!userId.equals(album.getCreatorId())){
             throw new TagException("User is not creator");
         }
         
         if (fileId == null) {
             throw new TagException("File Id is not specified");
         }
         //TODO check file existance
         
         Photo photo =  new Photo(name, description, tags,
                 PhotoStatus.NEW, getPhotosAmount(albumId) + 1, albumId, fileId, height, width);
         // for now set preview the same as original file
         photo.setPreviewId(fileId);
         
         return em.merge(photo);
     }
 
     @Override
     public List<Photo> addPhotos(Long userId, Long albumId, List<Long> fileIdList) throws TagException {
         
         if (albumId == null) {
             throw new TagException("Album Id is not specified"); 
         }
         if (userId == null) {
             throw new TagException("User Id is not specified"); 
         }
         Album album = em.find(Album.class, albumId);
         
         if (!userId.equals(album.getCreatorId())){
             throw new TagException("User is not creator");
         }
         
         for(Long id:fileIdList){
             if (id == null) {
                 throw new TagException("addPhotos: File Id is not specified");
             }
             //TODO check file existance
         }
         
         List<Photo> lp = new ArrayList<Photo>();
         Integer number = getPhotosAmount(albumId);
         for(Long id:fileIdList){
             number++;
             Photo photo =  new Photo(null, null, null, PhotoStatus.NEW,  number, albumId, id, null, null);
             em.merge(photo);
             lp.add(photo);
         }
         return lp;        
     }
 
     @Override
     public Album getAlbumByPhotoId(Long photoId) throws TagException {     
         Photo p = getPhotoById(photoId);
         if (p == null){
             throw new TagException("Photo is not specified");
         }
         return aMan.getAlbumById(p.getAlbumId());
     }   
     
     @Override
     public void generatePreviewAndDefaultView(Photo p) throws TagException {
         
         EasyTagFile etf = fMan.findFileById(p.getFileId());
         
         String path = etf.getCurrentPath();        
         String prevPath = path.substring(0, path.lastIndexOf(".")) 
                 + PreviewUtils.POSTFIX + path.substring(path.lastIndexOf("."));
         String defPath = path.substring(0, path.lastIndexOf(".")) 
                 + PreviewUtils.DEFAULT_POSTFIX + path.substring(path.lastIndexOf("."));
                 
         String name = etf.getOriginalName();
         String prevName = name.substring(0, name.lastIndexOf(".")) 
                 + PreviewUtils.POSTFIX + name.substring(name.lastIndexOf("."));
         String defName = name.substring(0, name.lastIndexOf(".")) 
                 + PreviewUtils.DEFAULT_POSTFIX + name.substring(name.lastIndexOf("."));
         
         try {
             PreviewUtils.makePreview(path, prevPath);
             System.out.println("vima: I'm here");
             PreviewUtils.makeDefaultView(path, defPath);
         } catch (Exception e) {            
             throw new TagException("can't make a preview");
         }
         
         EasyTagFile defaultFile = fMan.addFile(etf.getUserId(), defName, defPath, etf.getContentType());
         EasyTagFile prevFile = fMan.addFile(etf.getUserId(), prevName, prevPath, etf.getContentType());
         p.setPreviewId(prevFile.getId());  
         p.setOriginal_id(p.getFileId());
         p.setFileId(defaultFile.getId());
         
         
         em.merge(p);
     }
     
     private static final String SELECT_PHOTO_BY_TAGS = "select distinct p from "
                     + "Photo p, EasyTag e where LOWER(e.name) like LOWER(:query) "
                     + "and p.status <> :status "
                     + "and p.id = e.photoId";
     
     private static final String SELECT_PHOTO_BY_NAME = "select p from "
                     + "Photo p where LOWER(p.name) "                
                     + "like LOWER(:query) and p.status <> :status";
 
     @Override
     public List<Photo> findPhotosByTagName(String query) {
         if (query == null || query.isEmpty()) {
             return Collections.EMPTY_LIST;
         }
                 
         String[] words = query.split("\\s+");
         
         List<Photo> currTag, currName, diff;
         Query q = em.createQuery(SELECT_PHOTO_BY_TAGS)
                     .setParameter("query", "%" + words[0] + "%").setParameter("status", PhotoStatus.DELETED);            
         List<Photo> list = q.getResultList();
         q = em.createQuery(SELECT_PHOTO_BY_NAME)
                     .setParameter("query", "%" + words[0] + "%").setParameter("status", PhotoStatus.DELETED);
         currName = q.getResultList();
         diff = new ArrayList<Photo>(list);
         diff.retainAll(currName);
         currName.removeAll(diff);
         list.addAll(currName);     
         
         
         for(int i = 1; i < words.length; i++){        
             q = em.createQuery(SELECT_PHOTO_BY_TAGS)
                     .setParameter("query", "%" + words[i] + "%").setParameter("status", PhotoStatus.DELETED);
             currTag = q.getResultList();
             q = em.createQuery(SELECT_PHOTO_BY_NAME)
                     .setParameter("query", "%" + words[i] + "%").setParameter("status", PhotoStatus.DELETED);
             currName = q.getResultList();
             diff = new ArrayList<Photo>(currTag);
             diff.retainAll(currName);
             currName.removeAll(diff);
             currTag.addAll(currName);            
             list.retainAll(currTag);
         }     
         
        
         if (list == null || list.isEmpty()) {
             return Collections.EMPTY_LIST;
         }
         return list;
     }
     
     @Override
     public List<Photo> findPhotosInAlbum(String query, Long albumId) {
         if (query == null || query.isEmpty()) {
             return Collections.EMPTY_LIST;
         }
                 
         String[] words = query.split("\\s+");
         
         List<Photo> currTag, currName, diff;
         Query q = em.createQuery(SELECT_PHOTO_BY_TAGS + " and p.albumId = :albumId")
                     .setParameter("query", "%" + words[0] + "%").setParameter("albumId", albumId).setParameter("status", PhotoStatus.DELETED);            
         List<Photo> list = q.getResultList();
         q = em.createQuery(SELECT_PHOTO_BY_NAME + " and p.albumId = :albumId")
                     .setParameter("query", "%" + words[0] + "%").setParameter("albumId", albumId).setParameter("status", PhotoStatus.DELETED);
         currName = q.getResultList();
         diff = new ArrayList<Photo>(list);
         diff.retainAll(currName);
         currName.removeAll(diff);
         list.addAll(currName);     
         
         
         for(int i = 1; i < words.length; i++){        
             q = em.createQuery(SELECT_PHOTO_BY_TAGS + " and p.albumId = :albumId")
                     .setParameter("query", "%" + words[i] + "%").setParameter("albumId", albumId).setParameter("status", PhotoStatus.DELETED);
             currTag = q.getResultList();
             q = em.createQuery(SELECT_PHOTO_BY_NAME + " and p.albumId = :albumId")
                     .setParameter("query", "%" + words[i] + "%").setParameter("albumId", albumId).setParameter("status", PhotoStatus.DELETED);
             currName = q.getResultList();
             diff = new ArrayList<Photo>(currTag);
             diff.retainAll(currName);
             currName.removeAll(diff);
             currTag.addAll(currName);            
             list.retainAll(currTag);
         }     
         
        
         if (list == null || list.isEmpty()) {
             return Collections.EMPTY_LIST;
         }
         return list;
     }
     
     
     @Override
     public void deletePhoto(Long photoId) throws TagException {
         Photo p = getPhotoById(photoId);
         if (p == null){
             throw new TagException("Photo is not specified");
         }
         p.setStatus(PhotoStatus.DELETED);
         em.merge(p);
     }
     
     @Override
     public void restorePhoto(Long photoId) throws TagException {
         Photo p = getPhotoById(photoId);
         if (p == null){
             throw new TagException("Photo is not specified");
         }
         p.setStatus(PhotoStatus.IN_PROGRESS);
         em.merge(p);
     }
     
 
     @Override
     public Photo updatePhoto(Photo photo) {
         if (photo == null)
             return null;
         if (photo.getId() == null)
             return null;
         em.persist(photo);
         return photo;
     }
     
     private static final String SELECT_PHOTO_WITHOUT_DEFAULT_VIEW = "select p from "
                    + "Photo p where p.fileId = p.original_id and p.status <> :deleted";
     
     @Override
     public void generateDefaultViews() throws TagException {
        Query q = em.createQuery(SELECT_PHOTO_WITHOUT_DEFAULT_VIEW).setParameter("deleted", PhotoStatus.DELETED);            
         List<Photo> list = q.getResultList();
         for(Photo p: list){
             EasyTagFile etf = fMan.findFileById(p.getFileId());
             String path = etf.getCurrentPath();                    
             String defPath = path.substring(0, path.lastIndexOf(".")) 
                     + PreviewUtils.DEFAULT_POSTFIX + path.substring(path.lastIndexOf("."));
 
             String name = etf.getOriginalName();            
             String defName = name.substring(0, name.lastIndexOf(".")) 
                     + PreviewUtils.DEFAULT_POSTFIX + name.substring(name.lastIndexOf("."));
 
             try {                
                 PreviewUtils.makeDefaultView(path, defPath);
            } catch (Exception e) {
                e.printStackTrace();
                throw new TagException("can't make a defaultView for id="+p.getId() + ": " + e.getMessage());
             }
 
             EasyTagFile defaultFile = fMan.addFile(etf.getUserId(), defName, defPath, etf.getContentType());                                    
             p.setFileId(defaultFile.getId());
             
             em.merge(p);
         }
     }
     
     private static final String SELECT_PHOTO_WITHOUT_PREVIEW = "select p from "
                     + "Photo p where p.fileId = p.previewId";
     
     @Override
     public void generatePreViews() throws TagException {
         Query q = em.createQuery(SELECT_PHOTO_WITHOUT_PREVIEW);            
         List<Photo> list = q.getResultList();
         for(Photo p: list){
             EasyTagFile etf = fMan.findFileById(p.getFileId());
             String path = etf.getCurrentPath();                    
             String prevPath = path.substring(0, path.lastIndexOf(".")) 
                     + PreviewUtils.POSTFIX + path.substring(path.lastIndexOf("."));
 
             String name = etf.getOriginalName();            
             String prevName = name.substring(0, name.lastIndexOf(".")) 
                     + PreviewUtils.POSTFIX + name.substring(name.lastIndexOf("."));
 
             try {                
                 PreviewUtils.makePreview(path, prevPath);
             } catch (Exception e) {            
                throw new TagException("can't make a preview for photo " + p.getId() + ": " + e.getMessage());
             }
 
             EasyTagFile prevFile = fMan.addFile(etf.getUserId(), prevName, prevPath, etf.getContentType());                                    
             p.setPreviewId(prevFile.getId());
             
             em.merge(p);
         }
     } 
 }
