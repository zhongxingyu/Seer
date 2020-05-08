 package ar.noxit.ehockey.dao.impl;
 
 import java.util.List;
 
 import org.hibernate.Session;
 import org.springframework.transaction.annotation.Transactional;
 
 import ar.noxit.dataaccessobject.hibernate.HibernateDao;
 import ar.noxit.ehockey.dao.IClubDao;
 import ar.noxit.ehockey.model.Club;
 import ar.noxit.ehockey.model.Jugador;
 
 public class ClubDao extends HibernateDao<Club, Integer> implements IClubDao {
 
     public ClubDao() {
         super(Club.class);
     }
 
     @Override
     public List<Jugador> getClubPorNombre(String nombre, String nombreCompleto) {
         Session session = getSession();
         return session.createQuery(
                 "FROM Club c WHERE c.nombre = :nombre OR c.nombreCompleto = :nombre_completo AND c.activo = TRUE")
                 .setParameter("nombre", nombre).setParameter("nombre_completo", nombreCompleto).list();
     }
 }
