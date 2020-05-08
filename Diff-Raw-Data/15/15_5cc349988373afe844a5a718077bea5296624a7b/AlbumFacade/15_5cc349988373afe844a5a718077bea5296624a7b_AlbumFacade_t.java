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
 import javax.persistence.TypedQuery;
 import javax.persistence.criteria.CriteriaBuilder;
 import javax.persistence.criteria.CriteriaQuery;
 import javax.persistence.criteria.Root;
 import net.wazari.dao.entity.Album;
 import net.wazari.dao.entity.facades.SubsetOf;
 import net.wazari.dao.entity.facades.SubsetOf.Bornes;
 import net.wazari.dao.exchange.ServiceSession.ListOrder;
 import net.wazari.dao.jpa.entity.JPAAlbum;
 import net.wazari.dao.jpa.entity.JPAAlbum_;
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
 
         CriteriaBuilder cb = em.getCriteriaBuilder();
         CriteriaQuery<JPAAlbum> cq = cb.createQuery(JPAAlbum.class) ;
 
         Root<JPAAlbum> albm = cq.from(JPAAlbum.class);
         cq.where(webDAO.getRestrictionToAlbumsAllowed(session, albm, cq.subquery(JPAAlbum.class), restrict)) ;
         cq.orderBy(cb.desc(albm.get(JPAAlbum_.date))) ;
         TypedQuery<JPAAlbum> q = em.createQuery(cq);
 
         int size = q.getResultList().size() ;
         if (topFirst == TopFirst.TOP) {
             q.setFirstResult(0);
             q.setMaxResults(bornes.getNbElement());
         } else if (topFirst == TopFirst.FIRST) {
             q.setFirstResult(bornes.getFirstElement());
             q.setMaxResults(session.getAlbumSize());
         }
         q.setHint("org.hibernate.cacheable", true) ;
         q.setHint("org.hibernate.readOnly", true) ;
 
         List<JPAAlbum> lstAlbums = q.getResultList() ;
         stopWatch.stop("DAO.queryAlbums."+session.getTheme().getNom(), ""+lstAlbums.size()+" albums returned") ;
         return (SubsetOf) new SubsetOf<JPAAlbum>(bornes, lstAlbums, (long) size);
     }
 
     @Override
     public SubsetOf<Album> queryRandomFromYear(ServiceSession session,
         Restriction restrict, Bornes bornes, String date) {
         StopWatch stopWatch = new Slf4JStopWatch(log) ;
 
         CriteriaBuilder cb = em.getCriteriaBuilder();
         
         CriteriaQuery<JPAAlbum> cq = cb.createQuery(JPAAlbum.class) ;
         //FROM album
         Root<JPAAlbum> albm = cq.from(JPAAlbum.class);
         //where restrict to theme and albums
         cq.where( cb.and(
                 webDAO.getRestrictionToAlbumsAllowed(session, albm, cq.subquery(JPAAlbum.class), restrict),
                 webDAO.getRestrictionToCurrentTheme(session, albm, restrict),
                 cb.like(albm.get(JPAAlbum_.date), date+"%")
                 )) ;
         
         webDAO.setOrder(cq, cb, ListOrder.RANDOM, null) ;
         
         Query q = em.createQuery(cq)
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
             CriteriaBuilder cb = em.getCriteriaBuilder();
             CriteriaQuery<JPAAlbum> cq = cb.createQuery(JPAAlbum.class) ;
             Root<JPAAlbum> albm = cq.from(JPAAlbum.class);
             cq.where(cb.and(
                     webDAO.getRestrictionToAlbumsAllowed(session, albm, cq.subquery(JPAAlbum.class), restrict),
                     webDAO.getRestrictionToCurrentTheme(session, albm, restrict))) ;
             webDAO.setOrder(cq, cb, order, albm.get(JPAAlbum_.date)) ;
 
             Query q = em.createQuery(cq)
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
             CriteriaBuilder cb = em.getCriteriaBuilder();
             CriteriaQuery<JPAAlbum> cq = cb.createQuery(JPAAlbum.class) ;
             Root<JPAAlbum> albm = cq.from(JPAAlbum.class);
             cq.where(cb.and(
                     webDAO.getRestrictionToAlbumsAllowed(session, albm, cq.subquery(JPAAlbum.class), Restriction.ALLOWED_AND_THEME),
                     webDAO.getRestrictionToCurrentTheme(session, albm, Restriction.ALLOWED_AND_THEME),
                     cb.equal(albm.get(JPAAlbum_.id), id))) ;
             return (JPAAlbum) em.createQuery(cq)
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
             CriteriaBuilder cb = em.getCriteriaBuilder();
             CriteriaQuery<JPAAlbum> cq = cb.createQuery(JPAAlbum.class) ;
             Root<JPAAlbum> albm = cq.from(JPAAlbum.class);
             cq.where(cb.and(cb.equal(albm.get(JPAAlbum_.date), date),
                     cb.equal(albm.get(JPAAlbum_.nom), name)));
             return (JPAAlbum) em.createQuery(cq)
                     .setHint("org.hibernate.cacheable", true)
                     .setHint("org.hibernate.readOnly", true)
                     .getSingleResult();
         } catch (NoResultException e) {
             return null ;
         }
     }
 
     @Override
     public Album find(Integer id) {
         try {
             CriteriaBuilder cb = em.getCriteriaBuilder();
             CriteriaQuery<JPAAlbum> cq = cb.createQuery(JPAAlbum.class) ;
             Root<JPAAlbum> albm = cq.from(JPAAlbum.class);
             cq.where(cb.equal(albm.get(JPAAlbum_.id), id)) ;
             return (JPAAlbum) em.createQuery(cq)
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
         CriteriaBuilder cb = em.getCriteriaBuilder();
         CriteriaQuery<JPAAlbum> cq = cb.createQuery(JPAAlbum.class) ;
         cq.from(JPAAlbum.class) ;
         return (List) em.createQuery(cq)
                 .setHint("org.hibernate.cacheable", true)
                 .setHint("org.hibernate.readOnly", true)
                 .getResultList();
 
     }
 }
