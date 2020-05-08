 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.wazari.dao.jpa;
 
 import java.util.List;
 import net.wazari.dao.exchange.ServiceSession;
 import net.wazari.dao.*;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import javax.ejb.EJB;
 import javax.ejb.Stateless;
 import javax.persistence.EntityManager;
 import javax.persistence.NoResultException;
 import javax.persistence.PersistenceContext;
 import javax.persistence.Query;
 import net.wazari.dao.entity.Album;
 import net.wazari.dao.entity.facades.SubsetOf;
 import net.wazari.dao.entity.facades.SubsetOf.Bornes;
 import net.wazari.dao.exchange.ServiceSession.ListOrder;
 import net.wazari.dao.jpa.entity.JPAAlbum;
 import org.perf4j.StopWatch;
 import org.perf4j.slf4j.Slf4JStopWatch;
 
 /**
  *
  * @author kevin
  */
 @Stateless
 public class AlbumFacade implements AlbumFacadeLocal {
     private static final Logger log = LoggerFactory.getLogger(AlbumFacade.class.getCanonicalName()) ;
 
     @EJB
     WebAlbumsDAOBean webDAO;
 
     @PersistenceContext(unitName=WebAlbumsDAOBean.PERSISTENCE_UNIT)
     private EntityManager em;
 
     @Override
     public void create(Album album) {
         em.persist(album);
     }
 
     @Override
     public void edit(Album album) {
         em.merge(album);
     }
 
     @Override
     public void remove(Album album) {
         em.remove(em.merge(album));
     }
 
     @Override
     public SubsetOf<Album> queryAlbums(ServiceSession session,
             Restriction restrict, TopFirst topFirst, Bornes bornes) {
         StopWatch stopWatch = new Slf4JStopWatch(log) ;
         StringBuilder rq = new StringBuilder(80) ;
         rq.append("FROM ")
           .append(JPAAlbum.class.getName())
           .append(" a ")
           .append(" WHERE " )
           .append(restrict == Restriction.ALLOWED_AND_THEME || restrict == Restriction.ALLOWED_ONLY ? webDAO.restrictToAlbumsAllowed(session, "a") : "1 = 1 " )
           .append(" AND " )
           .append( (restrict == Restriction.ALLOWED_AND_THEME || restrict == Restriction.THEME_ONLY ? webDAO.restrictToThemeAllowed(session, "a") : "1 = 1 ") )
           .append(" ORDER BY a.date DESC ") ;
         Query q = em.createQuery(rq.toString()) ;
         if (topFirst == TopFirst.TOP) {
             q.setFirstResult(0);
             q.setMaxResults(bornes.getNbElement());
         } else if (topFirst == TopFirst.FIRST) {
             q.setFirstResult(bornes.getFirstElement());
             q.setMaxResults(session.getAlbumSize());
         }
         q.setHint("org.hibernate.cacheable", true) ;
         q.setHint("org.hibernate.readOnly", true) ;
         Query qSize = em.createQuery("SELECT count(*) "+rq) ;
 
         List<Album> lstAlbums = q.getResultList() ;
         Long size = (Long) qSize.getSingleResult() ;
         stopWatch.stop("DAO.queryAlbums."+session.getTheme().getNom(), ""+lstAlbums.size()+" albums returned") ;
         return new SubsetOf<Album>(bornes, lstAlbums, size);
     }
 
     @Override
     public SubsetOf<Album> queryRandomFromYear(ServiceSession session,
             Restriction restrict, Bornes bornes, String date) {
         StopWatch stopWatch = new Slf4JStopWatch(log) ;
         StringBuilder rq = new StringBuilder(80) ;
         rq.append("FROM ")
           .append(JPAAlbum.class.getName())
           .append(" a " )
           .append(" WHERE " )
           .append( (restrict == Restriction.ALLOWED_AND_THEME || restrict == Restriction.ALLOWED_ONLY ? webDAO.restrictToAlbumsAllowed(session, "a") : "1 = 1 " ) )
           .append(" AND " )
           .append( (restrict == Restriction.ALLOWED_AND_THEME || restrict == Restriction.THEME_ONLY ? webDAO.restrictToThemeAllowed(session, "a") : "1 = 1 ") )
           .append(" AND a.date LIKE :date ")
          .append(WebAlbumsDAOBean.getOrder(ListOrder.RANDOM, null));
         Query q = em.createQuery(rq.toString())
                .setParameter("date", date+"%")
                .setFirstResult(0)
                .setMaxResults(bornes.getNbElement());
         q.setHint("org.hibernate.cacheable", true)
          .setHint("org.hibernate.readOnly", true) ;
         Long size = (long) bornes.getNbElement() ;
 
         List<Album> lstAlbums = q.getResultList() ;
         stopWatch.stop("DAO.queryRandomFromYear."+session.getTheme().getNom(), ""+lstAlbums.size()+" albums returned") ;
         return new SubsetOf<Album>(bornes,lstAlbums, size);
     }
 
     @Override
     public Album loadFirstAlbum(ServiceSession session,
             Restriction restrict) {
         return loadFirstLastAlbum(session, restrict, ListOrder.ASC) ;
     }
     
     @Override
     public Album loadLastAlbum(ServiceSession session,
             Restriction restrict) {
         return loadFirstLastAlbum(session, restrict, ListOrder.DESC) ;
     }
 
     private Album loadFirstLastAlbum(ServiceSession session,
             Restriction restrict, ListOrder order) {
         StopWatch stopWatch = new Slf4JStopWatch(log) ;
         try {
             StringBuilder rq = new StringBuilder(80) ;
             rq.append("FROM ")
                 .append(JPAAlbum.class.getName())
                 .append(" a " )
                 .append(" WHERE ")
                 //.append(" AND ")
                 .append(restrict == Restriction.ALLOWED_AND_THEME || restrict == Restriction.THEME_ONLY ? webDAO.restrictToThemeAllowed(session, "a") : "1 = 1 ")
                 .append(WebAlbumsDAOBean.getOrder(order, "a.date")) ;
             Query q = em.createQuery(rq.toString())
                    .setFirstResult(0)
                    .setMaxResults(1)
                    .setHint("org.hibernate.cacheable", true)
                    .setHint("org.hibernate.readOnly", true) ;
 
             return (Album) q.getSingleResult();
         } catch (NoResultException e) {
             return null ;
         } finally {
             stopWatch.stop("DAO.loadFirstLastAlbum."+session.getTheme().getNom()) ;
         }
     }
 
     @Override
     public Album loadIfAllowed(ServiceSession session, int id) {
         try {
             StringBuilder rq = new StringBuilder(80) ;
             rq.append("FROM JPAAlbum a ")
                 .append(" WHERE " )
                 .append(webDAO.restrictToAlbumsAllowed(session, "a"))
                 .append(" AND a.id = :id ");
 
             return (JPAAlbum) em.createQuery(rq.toString()).setParameter("id", id)
                     .setHint("org.hibernate.cacheable", true)
                     .setHint("org.hibernate.readOnly", false)
                     .getSingleResult();
         } catch (NoResultException e) {
             return null ;
         }
     }
 
     @Override
     public Album loadByNameDate(String name, String date) {
         try {
             StringBuilder rq = new StringBuilder(80) ;
             rq.append("FROM JPAAlbum a " )
                 .append(" WHERE a.date = :date ")
                 .append(" AND a.nom = :nom");
             return (JPAAlbum) em.createQuery(rq.toString())
                     .setParameter("date", date)
                     .setParameter("nom", name)
                     .setHint("org.hibernate.cacheable", true)
                     .setHint("org.hibernate.readOnly", true)
                     .getSingleResult();
         } catch (NoResultException e) {
             return null ;
         }
     }
 
     @Override
     public Album find(Integer albumId) {
         try {
             String rq = "SELECT a FROM JPAAlbum a WHERE a.id = :id";
         return (JPAAlbum) em.createQuery(rq)
                 .setParameter("id", albumId)
                 .setHint("org.hibernate.cacheable", true)
                 .getSingleResult();
         } catch (NoResultException e) {
             return null ;
         }
     }
 
     @Override
     public Album newAlbum() {
         return new JPAAlbum();
     }
 
     @Override
     public List<Album> findAll() {
         String rq = "SELECT o FROM JPAAlbum o";
         return (List<Album>) em.createQuery(rq)
                 .setHint("org.hibernate.cacheable", true)
                 .setHint("org.hibernate.readOnly", true)
                 .getResultList();
 
     }
 }
