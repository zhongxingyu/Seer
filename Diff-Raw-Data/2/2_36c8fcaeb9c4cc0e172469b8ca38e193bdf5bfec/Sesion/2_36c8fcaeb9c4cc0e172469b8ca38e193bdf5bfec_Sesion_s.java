 package models;
 
 import infrastructure.Factories;
 
 import java.sql.Date;
 import java.sql.Time;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 
 import play.db.ebean.Model;
 
 @Entity
 public class Sesion extends Model {
 
 	private static final long serialVersionUID = -1969188576839253301L;
 	@Id
 	@GeneratedValue
 	private Long id;
 	@ManyToOne
 	private TipoSesion tipo;
 	@ManyToOne
 	private Sala sala;
 	@ManyToOne
 	private Pelicula pelicula;
 	@OneToMany(mappedBy = "sesion")
 	private List<Entrada> entradas = new ArrayList<Entrada>();
 	private Time hora;
 	private Date fecha;
 
 	public static Sesion findById(Long id) {
 		return Factories.business.getSesionService().findById(id);
 	}
 
 	public static List<Sesion> findAll() {
 		return Factories.business.getSesionService().findAll();
 	}
 
 	public static List<Sesion> findByFecha(Date fecha) {
 		return Factories.business.getSesionService().findByFecha(fecha);
 	}
 
 	public static List<Sesion> findBySalaAndFecha(Sala sala, Date fecha) {
 		return Factories.business.getSesionService().findBySalaAndFecha(sala,
 				fecha);
 	}
 
 	public void update() {
 		Factories.business.getSesionService().update(this);
 	}
 
 	public void save() {
 		Factories.business.getSesionService().save(this);
 	}
 
 	public void delete() {
 		Factories.business.getSesionService().delete(this);
 	}
 
 	public List<Integer> getButacasLibres() {
 		List<Integer> butacas = new ArrayList<Integer>();
 		for (int i = 1; i <= sala.getNumButacas(); i++)
 			butacas.add(i);
 		for (Entrada e : entradas)
 			butacas.remove(e.getButaca());
 		return butacas;
 	}
 
 	public List<Boolean> getButacas() {
 		List<Boolean> butacas = new ArrayList<Boolean>();
 		// Crear todas las butacas como libres
		for (int i = 0; i <= sala.getNumButacas(); i++)
 			butacas.add(false);
 		// Cambiar aquellas que esten ocupadas
 		for (Entrada e : entradas)
 			butacas.set(e.getButaca(), true);
 		return butacas;
 	}
 
 	public Sesion() {
 		super();
 	}
 
 	public Date getFecha() {
 		return fecha;
 	}
 
 	public void setFecha(Date dia) {
 		this.fecha = dia;
 	}
 
 	public Long getId() {
 		return id;
 	}
 
 	public Pelicula getPelicula() {
 		return pelicula;
 	}
 
 	public void setPelicula(Pelicula pelicula) {
 		if (this.pelicula != null)
 			this.pelicula.removeSesion(this);
 		pelicula.addSesion(this);
 	}
 
 	void _setPelicula(Pelicula pelicula) {
 		this.pelicula = pelicula;
 	}
 
 	public Sala getSala() {
 		return sala;
 	}
 
 	public void setSala(Sala sala) {
 		if (this.sala != null)
 			this.sala.removeSesion(this);
 		sala.addSesion(this);
 	}
 
 	void _setSala(Sala sala) {
 		this.sala = sala;
 	}
 
 	public TipoSesion getTipo() {
 		return tipo;
 	}
 
 	public void setTipo(TipoSesion tipo) {
 		if (this.tipo != null)
 			this.tipo.removeSesion(this);
 		tipo.addSesion(this);
 	}
 
 	void _setTipo(TipoSesion tipo) {
 		this.tipo = tipo;
 	}
 
 	public Time getHora() {
 		return hora;
 	}
 
 	public void setHora(Time hora) {
 		this.hora = hora;
 	}
 
 	public void addEntrada(Entrada entrada) {
 		entrada._setSesion(this);
 		entradas.add(entrada);
 	}
 
 	public void removeEntrada(Entrada entrada) {
 		entrada._setSesion(null);
 		entradas.remove(entrada);
 	}
 
 	public List<Entrada> getEntradas() {
 		return Collections.unmodifiableList(entradas);
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = super.hashCode();
 		result = prime * result + ((id == null) ? 0 : id.hashCode());
 		return result;
 	}
 
 	public int diasRestantesParaLaProyeccion() {
 		long MILLSECS_PER_DAY = 24 * 60 * 60 * 1000;
 		java.util.Date today = new java.util.Date();
 		int diferencia = (int) ((fecha.getTime() - today.getTime()) / MILLSECS_PER_DAY);
 		return diferencia;
 
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (!super.equals(obj))
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		Sesion other = (Sesion) obj;
 		if (id == null) {
 			if (other.id != null)
 				return false;
 		} else if (!id.equals(other.id))
 			return false;
 		return true;
 	}
 
 	@Override
 	public String toString() {
 		return "Sesion [id=" + id + ", tipo=" + tipo + ", sala=" + sala
 				+ ", pelicula=" + pelicula + ", hora=" + hora + ", dia=" + fecha
 				+ "]";
 	}
 
 }
