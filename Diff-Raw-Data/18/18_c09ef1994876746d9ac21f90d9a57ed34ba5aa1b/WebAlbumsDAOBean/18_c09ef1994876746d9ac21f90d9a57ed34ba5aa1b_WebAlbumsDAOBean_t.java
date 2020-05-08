 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.wazari.dao.jpa;
 
 import java.util.Iterator;
 import java.util.List;
 import javax.annotation.security.RolesAllowed;
 import javax.ejb.Stateless;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.criteria.*;
 import net.wazari.dao.AlbumFacadeLocal.Restriction;
 import net.wazari.dao.UtilisateurFacadeLocal;
 import net.wazari.dao.entity.Album;
 import net.wazari.dao.entity.Utilisateur;
 import net.wazari.dao.exchange.ServiceSession;
 import net.wazari.dao.exchange.ServiceSession.ListOrder;
 import net.wazari.dao.jpa.entity.JPAAlbum;
 import net.wazari.dao.jpa.entity.JPAPhoto;
 import net.wazari.dao.jpa.entity.JPATheme;
 import net.wazari.dao.jpa.entity.JPATheme_;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  *
  * @author kevin
  */
 @Stateless
 public class WebAlbumsDAOBean {
     private static final Logger log = LoggerFactory.getLogger(WebAlbumsDAOBean.class.getName());
     
     public static final String PERSISTENCE_UNIT_Derby = "WebAlbums-Derby" ;
 
     public static final String PERSISTENCE_UNIT_MySQL_Prod = "WebAlbums-MySQL" ;
     public static final String PERSISTENCE_UNIT_MySQL_Simu = "WebAlbums-MySQL-Test" ;
     public static final String PERSISTENCE_UNIT_MySQL_Test = "WebAlbums-MySQL-Test2" ;
     public static final String PERSISTENCE_UNIT_MySQL_StandAlone = "WebAlbums-MySQL-StandAlone" ;
 
     public static final String PERSISTENCE_UNIT_Prod = PERSISTENCE_UNIT_MySQL_Prod ;
     
     public static final String PERSISTENCE_UNIT = PERSISTENCE_UNIT_Prod ;
 
     @PersistenceContext(unitName=WebAlbumsDAOBean.PERSISTENCE_UNIT)
     private EntityManager em;
 
     @RolesAllowed(UtilisateurFacadeLocal.VIEWER_ROLE)
     public Predicate getRestrictionToCurrentTheme(ServiceSession session, 
                                                   Path<JPATheme> albm) {
         return getRestrictionToCurrentTheme(session, albm, Restriction.THEME_ONLY) ;
     }
     @RolesAllowed(UtilisateurFacadeLocal.VIEWER_ROLE)
     public Predicate getRestrictionToCurrentTheme(ServiceSession session, 
             Path<JPATheme> theme, Restriction restrict) {
         CriteriaBuilder cb = em.getCriteriaBuilder();
         Predicate TRUE = cb.conjunction() ;
         if (restrict == Restriction.NONE)
             return TRUE ;
         
         if (session.isRootSession()) {
             return TRUE ;
         } else {
             return cb.equal(theme.get(JPATheme_.id),
                             session.getTheme().getId()) ;
         }
 
     }
 
     @RolesAllowed(UtilisateurFacadeLocal.VIEWER_ROLE)
     public void setOrder(CriteriaQuery<?> cq, CriteriaBuilder cb,
             ListOrder order, Expression<?> field) {
         if (order == null) return ;
 
        Order orderBy ;
         switch(order) {
             case ASC: orderBy = cb.asc(field) ; break ;
             case DESC: orderBy = cb.desc(field) ; break ;
             case RANDOM: orderBy = cb.asc(cb.function("RAND", Float.class)) ; break ;
             case DEFAULT:
             default: return ;
         }
         cq.orderBy(orderBy) ;
     }
     
     @RolesAllowed(UtilisateurFacadeLocal.VIEWER_ROLE)
     public List<JPAAlbum> filterAlbumsAllowed(List<JPAAlbum> albums, ServiceSession session) {
         Iterator<JPAAlbum> itA = albums.iterator();
         while (itA.hasNext()) { 
             if (filter(itA.next(), session) == null)
                 itA.remove();
         }
         return albums;
     }
     
     @RolesAllowed(UtilisateurFacadeLocal.VIEWER_ROLE)
     public List<JPAPhoto> filterPhotosAllowed(List<JPAPhoto> photos, ServiceSession session) {
         Iterator<JPAPhoto> itP = photos.iterator();
         while (itP.hasNext()) { 
             if (filter(itP.next(), session) == null)
                 itP.remove();
         }
         return photos;
     }
     
     @RolesAllowed(UtilisateurFacadeLocal.VIEWER_ROLE)
     public JPAPhoto filter(JPAPhoto photo, ServiceSession session) {
         if (mustFilter(session, photo.getDroit())) {
             return null;
         } else {
             return photo;
         }
     }
     
     @RolesAllowed(UtilisateurFacadeLocal.VIEWER_ROLE)
     public Album filter(JPAAlbum album, ServiceSession session) {
         if (mustFilter(session, album.getDroit())) {
             return null;
         } else {
             return album;
         }
     }
     
     private boolean mustFilter(ServiceSession session, Utilisateur user) {
         return session != null && !session.isAdminSession() && session.getUser() != null &&
                 user != null && user.getId() < session.getUser().getId();
     }
     
     private boolean mustFilter(ServiceSession session, Integer userId) {
         return session != null && !session.isAdminSession() && session.getUser() != null
             && userId != null &&
             userId < session.getUser().getId();
     }
 }
