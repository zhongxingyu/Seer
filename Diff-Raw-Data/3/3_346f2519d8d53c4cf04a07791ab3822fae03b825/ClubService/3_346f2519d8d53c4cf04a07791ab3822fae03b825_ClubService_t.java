 package ar.noxit.ehockey.service.impl;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.Model;
 import org.springframework.beans.BeanUtils;
 import org.springframework.transaction.annotation.Transactional;
 
 import ar.noxit.ehockey.dao.IClubDao;
 import ar.noxit.ehockey.dao.IEquipoDao;
 import ar.noxit.ehockey.dao.IJugadorDao;
 import ar.noxit.ehockey.exception.ClubYaExistenteException;
 import ar.noxit.ehockey.model.Club;
 import ar.noxit.ehockey.model.Equipo;
 import ar.noxit.ehockey.model.Jugador;
 import ar.noxit.ehockey.service.IClubService;
 import ar.noxit.ehockey.web.pages.clubes.ClubPlano;
 import ar.noxit.exceptions.NoxitException;
 import ar.noxit.exceptions.persistence.PersistenceException;
 
 public class ClubService implements IClubService {
 
     private IClubDao clubDao;
     private IJugadorDao jugadorDao;
     private IEquipoDao equipoDao;
 
     @Transactional(readOnly = true)
     private List<Jugador> getJugadoresPorClub(Integer clubId) throws NoxitException {
         Club club = clubDao.get(clubId);
         return new ArrayList<Jugador>(club.getJugadores());
     }
 
     @Override
     @Transactional(readOnly = true)
     public List<Jugador> getJugadoresParaEquipo(Integer equipoId) throws NoxitException {
         Equipo equipo = equipoDao.get(equipoId);
         List<Jugador> jugadores = getJugadoresPorClub(equipo.getClub().getId());
         List<Jugador> nueva = new ArrayList<Jugador>();
         for (Jugador jug : jugadores) {
             if (jug.getDivision().equals(equipo.getDivision()) && jug.getSector().equals(equipo.getSector())) {
                 nueva.add(jug);
             }
         }
         return nueva;
     }
 
     @Override
     @Transactional(readOnly = true)
     public Club get(Integer id) throws NoxitException {
         return clubDao.get(id);
     }
 
     @Override
     @Transactional(readOnly = true)
     public List<Club> getAll() throws NoxitException {
         return clubDao.getAll();
     }
 
     @Override
     @Transactional(readOnly = true)
     public List<Jugador> getJugadoresPorClub(Integer clubId, List<Integer> idJugadores) throws NoxitException {
         return jugadorDao.getJugadoresFromClub(clubId, idJugadores);
     }
 
     @Override
     @Transactional(readOnly = true)
     public List<Equipo> getEquiposPorClub(Integer clubId) throws NoxitException {
         Club club = clubDao.get(clubId);
         return new ArrayList<Equipo>(club.getEquiposActivos());
     }
 
     @Override
     @Transactional(readOnly = true)
     public List<ClubPlano> getAllPlano() throws NoxitException {
         List<ClubPlano> clubes = new ArrayList<ClubPlano>();
         for (Club each : clubDao.getAll()) {
             clubes.add(aplanar(each));
         }
         return clubes;
     }
 
     private ClubPlano aplanar(Club club) {
         ClubPlano clb = new ClubPlano();
         clb.setId(club.getId());
         clb.setNombre(club.getNombre());
         return clb;
     }
 
     @Override
     @Transactional
     public void save(ClubPlano clubPlano) throws NoxitException {
         Club club = new Club(clubPlano.getNombreCompleto());
         BeanUtils.copyProperties(clubPlano, club);
         clubDao.save(club);
     }
 
     @Override
     @Transactional
     public void update(ClubPlano clubPlano) throws NoxitException {
         Club club = clubDao.get(clubPlano.getId());
         // Modifico los valores del club.
         BeanUtils.copyProperties(clubPlano, club);
     }
 
     @Override
     public IModel<ClubPlano> aplanar(IModel<Club> model) {
         ClubPlano clubPlano = new ClubPlano();
         BeanUtils.copyProperties(model.getObject(), clubPlano);
         return new Model<ClubPlano>(clubPlano);
     }
 
     @Override
     @Transactional(readOnly = true)
     public void verificarNombreClub(ClubPlano clubPlano) throws ClubYaExistenteException {
         String nombre = clubPlano.getNombre();
         String nombreCompleto = clubPlano.getNombreCompleto();
         if (clubDao.getClubPorNombre(nombre, nombreCompleto).size() != 0)
             throw new ClubYaExistenteException("Club de nombre: " + nombre + " ya existente.");
     }
 
     @Override
     @Transactional(readOnly = true)
     public void verificarCambioNombre(ClubPlano clubPlano) throws ClubYaExistenteException {
         try {
             Club club = clubDao.get(clubPlano.getId());
             String nombre = clubPlano.getNombre();
             String nombreCompleto = clubPlano.getNombreCompleto();
             List<Club> clubPorNombre = clubDao.getClubPorNombre(nombre, nombreCompleto);
             boolean actualizando = false;
             for (Club each : clubPorNombre) {
                if (each.getId().equals(club.getId())) {
                     actualizando = true;
                 }
             }
             if (!club.getNombre().equals(nombre) || !club.getNombreCompleto().equals(nombreCompleto)) {
                 if (clubPorNombre.size() != 0 && !actualizando)
                     throw new ClubYaExistenteException("Club de nombre: " + nombre + " ya existente.");
             } else {
                 if (actualizando) {
                     throw new ClubYaExistenteException("Club de nombre: " + nombre + " ya existente.");
                 }
             }
         } catch (PersistenceException e) {
             throw new ClubYaExistenteException(e);
         }
     }
 
     public void setClubDao(IClubDao clubDao) {
         this.clubDao = clubDao;
     }
 
     public void setJugadorDao(IJugadorDao jugadorDao) {
         this.jugadorDao = jugadorDao;
     }
 
     public void setEquipoDao(IEquipoDao equipoDao) {
         this.equipoDao = equipoDao;
     }
 }
