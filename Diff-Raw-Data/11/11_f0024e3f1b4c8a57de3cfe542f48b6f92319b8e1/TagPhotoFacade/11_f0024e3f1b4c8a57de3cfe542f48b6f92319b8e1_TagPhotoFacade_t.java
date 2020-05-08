 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.wazari.dao.jpa;
 
import java.util.Iterator;
 import java.util.logging.Logger;
 import net.wazari.dao.exchange.ServiceSession;
 import net.wazari.dao.*;
 import java.util.List;
 import javax.ejb.EJB;
 import javax.ejb.Stateless;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import net.wazari.dao.entity.Album;
 import net.wazari.dao.entity.Photo;
 import net.wazari.dao.entity.Tag;
 import net.wazari.dao.entity.TagPhoto;
 import net.wazari.dao.jpa.entity.JPATagPhoto;
 
 /**
  *
  * @author kevin
  */
 @Stateless
 public class TagPhotoFacade implements TagPhotoFacadeLocal {
     private static final Logger log = Logger.getLogger(TagPhotoFacade.class.getName()) ;
     
     @PersistenceContext(unitName=WebAlbumsDAOBean.PERSISTENCE_UNIT)
     private EntityManager em;
 
     @EJB PhotoFacadeLocal photoDAO ;
     @EJB AlbumFacadeLocal albumDAO ;
     @EJB TagFacadeLocal   tagDAO ;
     @EJB ThemeFacadeLocal themeDAO ;
 
     @Override
     public void create(TagPhoto tagPhoto) {
         tagPhoto.getPhoto().getTagPhotoList().add(tagPhoto);
         tagPhoto.getTag().getTagPhotoList().add(tagPhoto);
         em.persist(tagPhoto);
     }
 
     @Override
     public void edit(TagPhoto tagPhoto) {
         em.merge(tagPhoto);
     }
 
     @Override
     public void remove(TagPhoto tagPhoto) {
         tagPhoto.getPhoto().getTagPhotoList().remove(tagPhoto);
         tagPhoto.getTag().getTagPhotoList().remove(tagPhoto);
         em.remove(tagPhoto);
     }
 
     @Override
     public void deleteByPhoto(Photo enrPhoto) {
        Iterator<TagPhoto> it = enrPhoto.getTagPhotoList().iterator() ;
        while (it.hasNext()) {
            TagPhoto enrTagPhoto = it.next() ;
            enrTagPhoto.getTag().getTagPhotoList().remove(enrTagPhoto);
            it.remove();
            em.remove(enrTagPhoto);
         }
     }
 
     @Override
     public List<TagPhoto> queryByAlbum(Album enrAlbum) {
         String rq = "SELECT DISTINCT tp " +
                 "FROM JPAPhoto photo, JPATagPhoto tp " +
                 "WHERE photo.album = :album " +
                 "AND photo.id = tp.photo ";
         return em.createQuery(rq)
                 .setParameter("album", enrAlbum)
                 .getResultList();
     }
 
     @Override
     public TagPhoto loadByTagPhoto(int tagId, int photoId) {
         String rq = "FROM JPATagPhoto tp WHERE tp.photo.id = :photoId AND tp.tag.id = :tagId";
         return (JPATagPhoto) em.createQuery(rq)
                 .setParameter("photoId", photoId)
                 .setParameter("tagId", tagId)
                 .getSingleResult();
     }
 
     @Override
     public List<Tag> selectDistinctTags() {
         String rq = "SELECT DISTINCT tp.tag FROM JPATagPhoto tp";
         return em.createQuery(rq).getResultList();
     }
 
     @Override
     public List<Tag> selectUnusedTags(ServiceSession session) {
         String rq = "SELECT DISTINCT tp.tag " +
                             "FROM JPATagPhoto tp, JPAPhoto p, JPAAlbum a " +
                             "WHERE " +
                             " tp.photo = p.id AND p.album = a.id " +
                             " AND a.theme = :themeId ";
         return em.createQuery(rq)
                 .setParameter("themeId", session.getTheme())
                 .getResultList();
     }
 
     @Override
     public TagPhoto newTagPhoto() {
         return new JPATagPhoto() ;
     }
 }
