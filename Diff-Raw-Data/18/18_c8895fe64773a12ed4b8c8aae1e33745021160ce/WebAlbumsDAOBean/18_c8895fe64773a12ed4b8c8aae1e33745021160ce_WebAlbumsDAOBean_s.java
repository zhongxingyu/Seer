 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.wazari.dao.jpa;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import javax.annotation.security.RolesAllowed;
 import net.wazari.dao.exchange.ServiceSession;
 import net.wazari.dao.*;
 import javax.ejb.Stateless;
 import net.wazari.dao.exchange.ServiceSession.ListOrder;
 
 /**
  *
  * @author kevin
  */
 @Stateless
 public class WebAlbumsDAOBean {
     private static final Logger log = LoggerFactory.getLogger(WebAlbumsDAOBean.class.getName());
     
     public static final String PERSISTENCE_UNIT_DERBY = "WebAlbums-Derby" ;
     public static final String PERSISTENCE_UNIT_MySQL = "WebAlbums-MySQL" ;
     public static final String PERSISTENCE_UNIT_MySQL_Test = "WebAlbums-MySQL-Test" ;
     public static final String PERSISTENCE_UNIT_MySQL_Test2 = "WebAlbums-MySQL-Test2" ;
     public static final String PERSISTENCE_UNIT = PERSISTENCE_UNIT_MySQL ;
     
     @RolesAllowed(UtilisateurFacadeLocal.VIEWER_ROLE)
public StringBuilder processListID(ServiceSession session, StringBuilder rq, boolean restrict) {
         StringBuilder newRq = new StringBuilder(rq) ;
         if (restrict && ! session.isRootSession()) {
             newRq.append(" AND ").append(restrictToThemeAllowed(session, "a"));
         }
         return newRq;
     }
 
     @RolesAllowed(UtilisateurFacadeLocal.VIEWER_ROLE)
     public StringBuilder restrictToAlbumsAllowed(ServiceSession session, String album) {
         StringBuilder rq = null;
         if (session.isSessionManager()) {
             rq = new StringBuilder("SELECT a.id FROM JPAAlbum a WHERE 1 = 1 ");
         } else {
             rq = new StringBuilder(80)
                 .append("SELECT a.id ")
                 .append("FROM JPAAlbum a, JPAPhoto p ")
                 .append("WHERE a.id = p.album AND (")
                     //albums autorisé
                 .append("((p.droit = 0 OR p.droit is null) AND a.droit >= '")
                 .append(session.getUser().getId())
                 .append("') OR ")
                     //albums ayant au moins une photo autorisée
                 .append("(p.droit >= '")
                 .append(session.getUser().getId())
                 .append("')")
                 .append(") ");
         }
         return new StringBuilder(50).append(" ").append(album).append(".id IN (").append(processListID(session, rq, true) ).append(") ");
     }
 
     @RolesAllowed(UtilisateurFacadeLocal.VIEWER_ROLE)
     public String restrictToPhotosAllowed(ServiceSession session, String photo) {
         StringBuilder rq = new StringBuilder(80);
         rq.append("SELECT p.id ")
             .append(" FROM JPAPhoto p, JPAAlbum a ")
             .append(" WHERE p.album = a.id ");
         if (!session.isSessionManager()) {
             rq.append(" AND (")
                     //albums autorisé
             .append("((p.droit = 0 OR p.droit is null) AND a.droit >= '")
             .append(session.getUser().getId())
             .append("') OR ")
                     //albums ayant au moins une photo autorisée
             .append("(p.droit >= '")
             .append(session.getUser().getId())
             .append("')" )
             .append(")");
         }
 
         return new StringBuilder(50)
             .append(" ")
             .append(photo )
             .append(".id IN (" )
             .append(processListID(session, rq, true) )
             .append(") ").toString();
     }
 
     @RolesAllowed(UtilisateurFacadeLocal.VIEWER_ROLE)
     public String restrictToThemeAllowed(ServiceSession session, String album) {
         if (session.isRootSession()) {
             return " 1 = 1 ";
         } else {
             return new StringBuilder(25)
             .append(" " )
             .append(album )
             .append(".theme = '")
             .append( session.getTheme().getId())
             .append( "' ").toString();
         }
 
     }
 
     static StringBuilder getOrder(ListOrder order, String field) {
         StringBuilder rq = new StringBuilder(25)
             .append(" ORDER BY ") ;
         if (order == null) return new StringBuilder("") ;
         switch(order) {
             case ASC: return rq
             .append(field)
             .append(" ASC") ;
             case DESC: return rq
             .append(field)
             .append(" DESC") ;
             case RANDOM: return rq
            .append(field)
             .append(" RAND()") ;
             case DEFAULT:
             default: return new StringBuilder("") ;
         }
     }
 
 }
