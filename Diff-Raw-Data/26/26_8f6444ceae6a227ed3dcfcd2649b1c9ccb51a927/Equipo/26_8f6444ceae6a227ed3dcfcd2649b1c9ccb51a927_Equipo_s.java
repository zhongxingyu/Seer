 package ar.noxit.ehockey.model;
 
 import ar.noxit.ehockey.exception.EquiposInvalidosException;
 import ar.noxit.ehockey.exception.FechaInvalidaException;
 import ar.noxit.ehockey.exception.PartidoYaPerteneceATorneoExcepcion;
 import ar.noxit.ehockey.exception.TorneoNoCoincideException;
 import ar.noxit.ehockey.exception.ViolacionReglaNegocioException;
 import org.apache.commons.lang.Validate;
 import org.joda.time.LocalDateTime;
 
 public class Equipo {
 
     private Integer id;
     private String nombre;
 
     private Division division;
     private Sector sector;
     private Club club;
 
     private ListaBuenaFe listaBuenaFe;
 
     private boolean activo = true;
 
     /**
      * No debe ser usado por los clientes
      * 
      * @param nombre
      * @param club
      */
     public Equipo(String nombre, Club club, Division division, Sector sector) {
         Validate.notNull(nombre, "nombre no puede ser null");
         Validate.notNull(club, "club no puede ser null");
         Validate.notNull(division, "division no puede ser null");
         Validate.notNull(sector, "sector no puede ser null");
 
         this.club = club;
         this.nombre = nombre;
         this.division = division;
         this.sector = sector;
     }
 
     public ListaBuenaFe getListaBuenaFe() {
         if (listaBuenaFe == null) {
             listaBuenaFe = new ListaBuenaFe(this);
         }
         return listaBuenaFe;
     }
 
     /**
      * 
      * @param torneo
      *            torneo
      * @param visitante
      *            equipo visitante
      * @param fechaDelTorneo
      *            número de fecha del torneo (de 1 a cantidad de equipos - 1)
      * @param rueda
      *            dice si el partido es ida o vuelta
      * @param partidoN
      *            partido número de la fecha (de 1 a cantidad de equipos / 2)
      * @param inicio
      *            fecha y hora del inicio del partido
      * @param now
      *            instante actual
      * @return
      * @throws EquiposInvalidosException
      * @throws FechaInvalidaException
      */
     public Partido jugarContra(Torneo torneo, Equipo visitante, Integer fechaDelTorneo, Integer rueda,
             Integer partidoN, LocalDateTime inicio, LocalDateTime now)
             throws EquiposInvalidosException, FechaInvalidaException {
 
         try {
             Partido partido = new Partido(torneo, this, visitante, fechaDelTorneo, rueda, partidoN, inicio, now);
             torneo.agregarPartido(partido);
             return partido;
         } catch (TorneoNoCoincideException e) {
             // no deberia pasar nunca ya que el torneo del partido coincide
             throw new ViolacionReglaNegocioException(e);
         } catch (PartidoYaPerteneceATorneoExcepcion e) {
             // no deberia pasar nunca por que el partido esta recien creado
             throw new ViolacionReglaNegocioException(e);
         }
     }
 
     public Division getDivision() {
         return division;
     }
 
     public Sector getSector() {
         return sector;
     }
 
     public void setNombre(String nombre) {
         Validate.notNull(nombre);
 
         this.nombre = nombre;
     }
 
     public void darDeBaja() {
         this.activo = false;
     }
 
     /**
      * No debe ser llamados por los clientes
      */
     public void eliminar() {
         this.club = null;
     }
 
     public String getNombre() {
         return nombre;
     }
 
     public Club getClub() {
         return club;
     }
 
     public Integer getId() {
         return id;
     }
 
     public void setId(Integer id) {
         this.id = id;
     }
 
     protected Equipo() {
     }
 
 }
