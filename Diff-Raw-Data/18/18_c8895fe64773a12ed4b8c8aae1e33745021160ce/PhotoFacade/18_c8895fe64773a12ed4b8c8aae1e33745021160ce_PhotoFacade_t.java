 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.wazari.dao.jpa;
 
 import net.wazari.dao.exchange.ServiceSession;
 import net.wazari.dao.*;
 import java.util.List;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import javax.ejb.EJB;
 import javax.ejb.Stateless;
 import javax.persistence.EntityManager;
 import javax.persistence.NoResultException;
 import javax.persistence.PersistenceContext;
 import javax.persistence.Query;
 import net.wazari.dao.entity.Photo;
 import net.wazari.dao.entity.facades.SubsetOf;
 import net.wazari.dao.entity.facades.SubsetOf.Bornes;
 import net.wazari.dao.exchange.ServiceSession.ListOrder;
 import net.wazari.dao.jpa.entity.JPAPhoto;
 
 /**
  *
  * @author kevin
  */
 @Stateless
 public class PhotoFacade implements PhotoFacadeLocal {
     private static final Logger log = LoggerFactory.getLogger(PhotoFacade.class.getCanonicalName()) ;
 
     @EJB
     WebAlbumsDAOBean webDAO;
     @EJB
     AlbumFacadeLocal albumDAO;
 
     @PersistenceContext(unitName=WebAlbumsDAOBean.PERSISTENCE_UNIT)
     private EntityManager em;
 
     @Override
     public void create(Photo photo) {
         em.persist(photo);
     }
 
     @Override
     public void edit(Photo photo) {
         em.merge(photo);
     }
 
     @Override
     public void remove(Photo photo) {
         photo.getAlbum().getPhotoList().remove(photo);
         em.remove(photo);
     }
 
     @Override
     public Photo loadIfAllowed(ServiceSession session, int id) {
         try {
             StringBuilder rq = new StringBuilder(80) ;
             rq.append("FROM JPAPhoto p ")
                 .append(" WHERE p.id = :id " )
                 .append(session == null ? "" : " AND " + webDAO.restrictToPhotosAllowed(session, "p")) ;
 
             return (JPAPhoto) em.createQuery(rq.toString()).setParameter("id", id)
                     .setHint("org.hibernate.cacheable", true)
                     .setHint("org.hibernate.readOnly", false)
                     .getSingleResult();
         } catch (NoResultException e) {
             return null;
         }
     }
 
     @Override
     public SubsetOf<Photo> loadFromAlbum(ServiceSession session, int albumId, Bornes bornes, ListOrder order) {
         StringBuilder rq = new StringBuilder(80);
         rq.append("FROM JPAPhoto p " )
             .append(" WHERE p.album.id = :albumId ")
             .append(session == null ? "" : " AND " + webDAO.restrictToPhotosAllowed(session, "p"))
             .append(WebAlbumsDAOBean.getOrder(order, "p.path"));
 
         Query q = em.createQuery(rq.toString()).setParameter("albumId", albumId);
         if (bornes != null && bornes.getFirstElement() != null) {
             q.setFirstResult(bornes.getFirstElement());
             q.setMaxResults(session.getPhotoSize());
         }
 
 
         Query qSize = em.createQuery("SELECT count(*) "+rq)
                 .setParameter("albumId", albumId) ;
         Long size = (Long) qSize.getSingleResult() ;
 
         List<Photo> subset = q
                     .setHint("org.hibernate.cacheable", true)
                     .setHint("org.hibernate.readOnly", true)
                     .getResultList() ;
         if (bornes == null || bornes.getFirstElement() == null)
             return new SubsetOf<Photo>(subset) ;
             else return new SubsetOf<Photo>(bornes, subset, size);
 
     }
 
     @Override
     public Photo loadByPath(String path) {
         try {
             String rq = "FROM JPAPhoto p WHERE p.path = :path";
             return (JPAPhoto) em.createQuery(rq).setParameter("path", path).getSingleResult();
         } catch (NoResultException e) {
             return null ;
         }
     }
 
     @Override
     public SubsetOf<Photo> loadByTags(ServiceSession session, List<Integer> listTagId, Bornes bornes, ListOrder order) {
         //creation de la requete
         StringBuilder rq = new StringBuilder(80);
         rq.append("SELECT p ")
             .append(" FROM JPAPhoto p, JPAAlbum a, JPATagPhoto tp ")
             .append(" WHERE a.id = p.album and p.id = tp.photo")
             .append(" AND tp.tag in ('-1' ");
         for (int id : listTagId) {
             rq.append(", '" )
             .append(id )
             .append( "'");
         }
         rq.append(")")
             .append(" AND " )
             .append(webDAO.restrictToPhotosAllowed(session, "p"))
             .append(" AND " )
             .append(webDAO.restrictToThemeAllowed(session, "a"))
             .append(" GROUP BY p.id " )
             .append(WebAlbumsDAOBean.getOrder(order, "p.path"));
         
            log.error(rq.toString()) ;
         Query q = em.createQuery(rq.toString())
                       .setHint("org.hibernate.cacheable", true)
                       .setHint("org.hibernate.readOnly", true);
         //TODO this might not be the better implementation ...
         int size = q.getResultList().size() ;
         if (bornes != null && bornes.getFirstElement() != null) {
             q.setFirstResult(bornes.getFirstElement());
             q.setMaxResults(session.getPhotoSize());
         }
         
         return new SubsetOf<Photo>(bornes, q.getResultList(), (long) size);
     }
 
     @Override
     public Photo find(Integer id) {
         try {
             String rq = "FROM JPAPhoto p WHERE p.id = :id";
             return (JPAPhoto) em.createQuery(rq).setParameter("id", id)
                     .setHint("org.hibernate.cacheable", true)
                     .getSingleResult();
         } catch (NoResultException e) {
             return null ;
         }
     }
 
     @Override
     public Photo newPhoto() {
         return new JPAPhoto() ;
     }
 
     @Override
     public List<Photo> findAll() {
         String rq = "SELECT o FROM JPAPhoto o";
         return (List<Photo>) em.createQuery(rq)
                 .setHint("org.hibernate.cacheable", true)
                 .setHint("org.hibernate.readOnly", true)
                 .getResultList();
     }
 }
