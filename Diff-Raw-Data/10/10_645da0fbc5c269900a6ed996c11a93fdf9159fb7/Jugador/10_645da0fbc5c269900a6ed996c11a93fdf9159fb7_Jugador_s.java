 package ar.noxit.ehockey.model;
 
 import ar.noxit.ehockey.exception.JugadorInactivoInmutableException;
 import ar.noxit.ehockey.exception.JugadorYaActivoException;
 import ar.noxit.ehockey.exception.JugadorYaBajaException;
 import ar.noxit.ehockey.exception.NoHayPartidoSiguienteException;
 import ar.noxit.ehockey.exception.SinClubException;
 import ar.noxit.ehockey.exception.SinPartidosException;
 import ar.noxit.ehockey.exception.TarjetaYaUsadaException;
 import ar.noxit.ehockey.model.Tarjeta.TipoTarjeta;
 import ar.noxit.utils.Collections;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import org.apache.commons.lang.Validate;
 import org.joda.time.LocalDate;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Jugador
  * 
  * @author Mauro Ciancio
  * 
  */
 public class Jugador {
 
     public static final int CANTIDADTARJETASROJASSANCION = 1;
     public static final int CANTIDADTARJETASAMARILLASSANCION = 3;
     public static final int CANTIDADTARJETASVERDESSANCION = 4;
     /**
      * Ficha del Jugador
      */
     private Integer ficha;
     /**
      * Apellido del jugador
      */
     private String apellido;
     /**
      * Nombre del jugador
      */
     private String nombre;
 
     private DocumentoJugador documento;
     private LocalDate fechaNacimiento;
     private String telefono;
     private LocalDate fechaAlta;
     private String letraJugador;
     private boolean activo;
 
     private Division division;
     private Club club;
     private Sector sector;
 
     private Set<Tarjeta> tarjetas = new HashSet<Tarjeta>();
     private Set<ISancion> sanciones = new HashSet<ISancion>();
 
     private static final Logger logger = LoggerFactory.getLogger(Jugador.class);
 
     /**
      * Construye un nuevo jugador
      * 
      * @param apellido
      *            del jugador
      * @param nombre
      *            del jugador
      * @throws IllegalArgumentException
      *             si ficha, apellido o nombre son null
      */
     public Jugador(String apellido, String nombre, Club club, Sector sector, Division division, LocalDate now) {
         Validate.notNull(apellido, "apellido no puede ser null");
         Validate.notNull(nombre, "nombre no puede ser null");
         Validate.notNull(division, "division no puede ser null");
         Validate.notNull(sector, "sector no puede ser null");
         Validate.notNull(club, "club no puede ser null");
         Validate.notNull(now, "now no puede ser null");
 
         this.apellido = apellido;
         this.nombre = nombre;
         this.sector = sector;
         this.division = division;
         this.club = club;
         this.documento = new DocumentoJugador();
         this.fechaAlta = now;
         this.activo = true;
     }
 
     /**
      * Obtiene el id del jugador
      * 
      * @return id
      */
     public Integer getFicha() {
         return ficha;
     }
 
     public Club getClub() throws SinClubException {
         if (club == null) {
             throw new SinClubException("el jugador no tiene club");
         }
         return club;
     }
 
     public void asignarClub(Club club) {
         Validate.notNull(club);
 
         if (club != this.club) {
             Club oldClub = this.club;
             this.club = null;
             if (oldClub != null) {
                 oldClub.borrarJugador(this);
             }
 
             this.club = club;
             if (!club.contiene(this)) {
                 club.agregarJugador(this);
             }
         }
     }
 
     public void liberar() {
         Club clubViejo = this.club;
         this.club = null;
         if (clubViejo != null && clubViejo.contiene(this)) {
             clubViejo.borrarJugador(this);
         }
     }
 
     /**
      * Crea y guarda las tarjetas y crea sanciones si corresponde.
      * 
      * @param partido
      * @param equipo
      * @param tarjetasPartido
      */
     public void amonestar(Partido partido, Equipo equipo, TarjetasPartido tarjetasPartido) {
         Validate.notNull(partido);
         Validate.notNull(equipo);
         Validate.notNull(tarjetasPartido);
 
         Integer rojas = tarjetasPartido.getRojas();
         Integer amarillas = tarjetasPartido.getAmarillas();
         Integer verdes = tarjetasPartido.getVerdes();
 
         crearTarjetas(partido, rojas, TipoTarjeta.ROJA);
         crearTarjetas(partido, amarillas, TipoTarjeta.AMARILLA);
         crearTarjetas(partido, verdes, TipoTarjeta.VERDE);
 
         crearSancionesSiCorresponde(partido, equipo);
     }
 
     /**
      * Permite cargar una sanción al jugador que lo inhabilite a jugar los partidos que le corresponda según las reglas
      * de la sanción.
      * 
      * @param sancion
      *            sancion aplicada al jugador
      */
     private void sancionar(ISancion sancion) {
         Validate.notNull(sancion);
         sanciones.add(sancion);
     }
 
     private void crearSancionesSiCorresponde(Partido partido, Equipo equipo) {
         Validate.notNull(partido, "partido no puede ser null");
         Validate.notNull(equipo, "equipo no puede ser null");
 
         // torneo
         Torneo torneo = partido.getTorneo();
 
         // partidos en los que el jugador va a estar suspendido
         Collection<Partido> suspendidos = new HashSet<Partido>();
 
         try {
             Partido partidoActual = partido;
             TipoTarjeta tiposTarjetas[] = { TipoTarjeta.ROJA, TipoTarjeta.AMARILLA, TipoTarjeta.VERDE };
             Integer cantidades[] = { CANTIDADTARJETASROJASSANCION, CANTIDADTARJETASAMARILLASSANCION,
                     CANTIDADTARJETASVERDESSANCION };
 
             for (int i = 0; i < tiposTarjetas.length; i++) {
                 TipoTarjeta tipoTarjeta = tiposTarjetas[i];
                 Integer cantidad = cantidades[i];
 
                while (sumarTarjetas(tipoTarjeta) >= cantidad) {
                     while (!puedeJugar(partidoActual)) {
                         partidoActual = torneo.getProximoPartidoDe(partidoActual, equipo);
                     }
                     suspendidos.add(partidoActual);
                     descontarTarjetas(tipoTarjeta, cantidad);
                 }
             }
         } catch (NoHayPartidoSiguienteException e) {
             logger.debug("Sancion no aplicada, no hay más partidos", e);
         } finally {
             try {
                 this.sancionar(new SancionPartidosInhabilitados(suspendidos));
             } catch (SinPartidosException e) {
                 logger.debug("Se intento sancionar pero no había partidos", e);
             }
         }
     }
 
     public boolean puedeJugar(Partido partido) {
         boolean juega = true;
         for (ISancion sancion : this.sanciones) {
             juega &= sancion.puedeJugar(partido);
         }
         return juega;
     }
 
    private int sumarTarjetas(TipoTarjeta tipo) {
         int suma = 0;
         for (Tarjeta tarjeta : tarjetas) {
            if (tarjeta.getTipo().equals(tipo) && !tarjeta.isUsada())
                 suma++;
         }
         return suma;
     }
 
     private void descontarTarjetas(TipoTarjeta tipo, int cantidad) {
         int suma = 0;
         Iterator<Tarjeta> it = tarjetas.iterator();
         while (it.hasNext() && suma < cantidad) {
             Tarjeta tarjeta = it.next();
             try {
                 if (tarjeta.getTipo().equals(tipo)) {
                     tarjeta.usar();
                     suma++;
                 }
             } catch (TarjetaYaUsadaException e) {
                 logger.debug("seguimos buscando tarjetas", e);
                 // seguimos buscando tarjetas
             }
         }
     }
 
     private void crearTarjetas(Partido partido, Integer cantidad, TipoTarjeta tipo) {
         for (int i = 0; i < cantidad; ++i) {
             tarjetas.add(new Tarjeta(tipo, partido));
         }
     }
 
     /**
      * Obtiene el apellido del jugador
      * 
      * @return apellido
      */
     public String getApellido() {
         return apellido;
     }
 
     /**
      * Obtiene el nombre del jugador
      * 
      * @return nombre
      */
     public String getNombre() {
         return nombre;
     }
 
     public String getNombreApellido() {
         return String.format("%s %s", nombre, apellido);
     }
 
     public Sector getSector() {
         return sector;
     }
 
     public Division getDivision() {
         return division;
     }
 
     /**
      * Este método no debería ser usado por los clientes
      * 
      * @param id
      */
     public void setFicha(Integer id) {
         validarInmutabilidadJugadorInactivo();
         this.ficha = id;
     }
 
     /**
      * Verifica en runtime si se quiere modificar un jugador que se ha dado de baja
      */
     private void validarInmutabilidadJugadorInactivo() {
         if (!this.activo)
             throw new JugadorInactivoInmutableException();
     }
 
     /**
      * Constructor default para la persistencia. No debe ser llamado por los clientes.
      */
     protected Jugador() {
     }
 
     public String getLetraJugador() {
         return letraJugador;
     }
 
     public String getTipoDocumento() {
         return this.documento.getTipo();
     }
 
     public String getDocumento() {
         return this.documento.getNumero();
     }
 
     public void setTipoDocumento(String tipoDocumento) {
         validarInmutabilidadJugadorInactivo();
         this.documento.setTipo(tipoDocumento);
     }
 
     public void setNumeroDocumento(String numeroDocumento) {
         validarInmutabilidadJugadorInactivo();
         this.documento.setNumero(numeroDocumento);
     }
 
     public void setFechaNacimiento(LocalDate fechaNacimiento) {
         validarInmutabilidadJugadorInactivo();
         this.fechaNacimiento = fechaNacimiento;
     }
 
     public void setTelefono(String telefono) {
         validarInmutabilidadJugadorInactivo();
         this.telefono = telefono;
     }
 
     public void setFechaAlta(LocalDate fechaAlta) {
         validarInmutabilidadJugadorInactivo();
         this.fechaAlta = fechaAlta;
     }
 
     public void setLetraJugador(String letraJugador) {
         validarInmutabilidadJugadorInactivo();
         this.letraJugador = letraJugador;
     }
 
     public LocalDate getFechaAlta() {
         return this.fechaAlta;
     }
 
     public LocalDate getFechaNacimiento() {
         return this.fechaNacimiento;
     }
 
     public String getTelefono() {
         return this.telefono;
     }
 
     public void setClub(Club club) {
         validarInmutabilidadJugadorInactivo();
         Validate.notNull(club);
         if (!(club == this.club)) {
             this.liberar();
             club.agregarJugador(this);
         }
     }
 
     public void setApellido(String apellido) {
         validarInmutabilidadJugadorInactivo();
         this.apellido = apellido;
     }
 
     public void setDivision(Division division) {
         validarInmutabilidadJugadorInactivo();
         this.division = division;
     }
 
     public void setNombre(String nombre) {
         validarInmutabilidadJugadorInactivo();
         this.nombre = nombre;
     }
 
     public void setSector(Sector sector) {
         validarInmutabilidadJugadorInactivo();
         this.sector = sector;
     }
 
     public void reactivarJugador() throws JugadorYaActivoException {
         if (this.activo)
             throw new JugadorYaActivoException();
         this.activo = true;
     }
 
     public void bajaJugador() throws JugadorYaBajaException {
         if (!this.activo)
             throw new JugadorYaBajaException();
         this.activo = false;
     }
 
     public boolean estaActivo() {
         return this.activo;
     }
 
     public Tarjeta getTarjeta(Integer id) {
         for (Tarjeta each : tarjetas) {
             if (each.getId().equals(id))
                 return each;
         }
         return null;
     }
 
     public List<Tarjeta> getTarjetas() {
         return Collections.toList(tarjetas.iterator());
     }
 
     public List<ISancion> getSanciones() {
         return Collections.toList(sanciones.iterator());
     }
 
     public ISancion getSancion(Integer id) {
         for (ISancion each : sanciones) {
             if (each.getId().equals(id))
                 return each;
         }
         return null;
     }
 
 }
