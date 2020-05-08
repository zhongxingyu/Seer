 package py.com.ait.gestion.domain;
 
 import java.io.Serializable;
import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 
 import javax.persistence.*;
 
 
 /**
  * The persistent class for the proceso database table.
  * 
  */
 @Entity
 @Table(name="proceso")
 public class Proceso  extends Base implements Serializable {
 	private static final long serialVersionUID = 1L;
 	
 	@Id
 	@SequenceGenerator(name="PROCESO_SEQ", sequenceName="proceso_id_seq", allocationSize=1)
 	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="PROCESO_SEQ")
 	@Column(name="id")
 	private Long procesoId;
 	
 	@Column(name="nro_proceso")
 	private String nroProceso;
 	
 	@ManyToOne(fetch=FetchType.EAGER,optional=true)
 	@JoinColumn(name="responsable")
 	private Usuario responsable;
 	
 	@ManyToOne(fetch=FetchType.EAGER,optional=true)
 	@JoinColumn(name="cliente")
 	private Cliente cliente;
 	
 	@ManyToOne(fetch=FetchType.EAGER,optional=true)
 	@JoinColumn(name="cronograma")
 	private Cronograma cronograma;
 	
 	@Column(name="estado")
 	private String estado;
 	
 	@Column(name="descripcion")
 	private String descripcion;
 	
 	@Column(name="fecha_inicio_contratual")
 	private Date fechaInicioContratual;
 	
 	@Column(name="fecha_inicio_real")
 	private Date fechaInicioReal;
 	
 	@Column(name="fecha_fin_prevista")
 	private Date fechaFinPrevista;
 	
 	@Column(name="fecha_fin_reprogramada")
 	private Date fechaFinReprogramada;
 	
 	@Column(name="motivo_reprogramacion")
 	private String motivoReprogramacion;
 	
 	@Column(name="cliente_notificado")
 	private String clienteNotificado;
 
 	@Transient
 	private String lastActividad;
 
 	@OneToMany(mappedBy="master",cascade=CascadeType.ALL)
 	private List<Actividad> actividades;

	@Transient
	private String fechaFinFormateada;

 	public Proceso() {
 		super();
     }
    	
 	public Long getProcesoId() {
 		return procesoId;
 	}
 
 	public void setProcesoId(Long procesoId) {
 		this.procesoId = procesoId;
 	}
 
 	public String getNroProceso() {
 		return nroProceso;
 	}
 
 	public void setNroProceso(String nroProceso) {
 		this.nroProceso = nroProceso;
 	}
 
 	public Usuario getResponsable() {
 		return responsable;
 	}
 
 	public void setResponsable(Usuario responsable) {
 		this.responsable = responsable;
 	}
 
 	public Cliente getCliente() {
 		return cliente;
 	}
 
 	public void setCliente(Cliente cliente) {
 		this.cliente = cliente;
 	}
 
 	public Cronograma getCronograma() {
 		return cronograma;
 	}
 
 	public void setCronograma(Cronograma cronograma) {
 		this.cronograma = cronograma;
 	}
 
 	public String getEstado() {
 		return estado;
 	}
 
 	public void setEstado(String estado) {
 		this.estado = estado;
 	}
 
 	public String getDescripcion() {
 		return descripcion;
 	}
 
 	public void setDescripcion(String descripcion) {
 		this.descripcion = descripcion;
 	}
 	
 	public Date getFechaInicioContratual() {
 		return fechaInicioContratual;
 	}
 
 	public void setFechaInicioContratual(Date fechaInicioContratual) {
 		this.fechaInicioContratual = fechaInicioContratual;
 	}
 
 	public Date getFechaInicioReal() {
 		return fechaInicioReal;
 	}
 
 	public void setFechaInicioReal(Date fechaInicioReal) {
 		this.fechaInicioReal = fechaInicioReal;
 	}
 
 	public Date getFechaFinPrevista() {
 		return fechaFinPrevista;
 	}
 
 	public void setFechaFinPrevista(Date fechaFinPrevista) {
 		this.fechaFinPrevista = fechaFinPrevista;
 	}
 
 	public Date getFechaFinReprogramada() {
 		return fechaFinReprogramada;
 	}
 
 	public void setFechaFinReprogramada(Date fechaFinReprogramada) {
 		this.fechaFinReprogramada = fechaFinReprogramada;
 	}
 
 	public String getMotivoReprogramacion() {
 		return motivoReprogramacion;
 	}
 
 	public void setMotivoReprogramacion(String motivoReprogramacion) {
 		this.motivoReprogramacion = motivoReprogramacion;
 	}
 
 	public String getClienteNotificado() {
 		return clienteNotificado;
 	}
 	
 	public void setClienteNotificado(String clienteNotificado) {
 		this.clienteNotificado = clienteNotificado;
 	}
 
 	public List<Actividad> getActividades() {
 		return actividades;
 	}
 
 	public void setActividades(List<Actividad> actividades) {
 		this.actividades = actividades;
 	}
 
 	public String getLastActividad() {
 		return lastActividad;
 	}
 
 	public void setLastActividad(String lastActividad) {
 		this.lastActividad = lastActividad;
 	}
 
	public String getFechaFinFormateada() {
		String result = "";
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		if (fechaFinPrevista != null)
			result = sdf.format(fechaFinPrevista);
		return result;
	}

	public void setFechaFinFormateada(String fechaFinFormateada) {
		this.fechaFinFormateada = fechaFinFormateada;
	}

 
 }
