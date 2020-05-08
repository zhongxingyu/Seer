 package tecinf.persistencia.entities;
 
 import java.io.Serializable;
 import java.util.Date;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.SequenceGenerator;
 import javax.persistence.Table;
 
 
 @Entity
 @Table(name="usuario_sube_contenido")
 public class UsuarioSubeContenidoEntity implements Serializable {
 
 	private static final long serialVersionUID = 1L;
 	
 	@Id
 	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator =	"usrsubecont_secuencia") 
 	@SequenceGenerator(name = "usrsubecont_secuencia", sequenceName = "usrsubecont_seq", allocationSize = 1)
 	private Integer id;
 	
 	@ManyToOne
 	@JoinColumn(name="usuario", nullable=false)
 	private UsuarioEntity usuarioCliente;
 	
	@ManyToOne(cascade=CascadeType.MERGE)
 	@JoinColumn(name="contenido", nullable=false)
 	private ContenidoEntity contenido;
 	
 	@Column(name="fecha_subida" , nullable=false)
 	private Date fechaSubida;
 	
 	@Column(name="comentario_subida" , nullable=true, length=1000)
 	private String comentarioSubida;
 	
 	@Column(name = "precio_subida", nullable = true)
 	private Float precioSubida;
 
 	public Integer getId() {
 		return id;
 	}
 
 	public void setId(Integer id) {
 		this.id = id;
 	}
 
 	public UsuarioEntity getUsuarioCliente() {
 		return usuarioCliente;
 	}
 
 	public void setUsuarioCliente(UsuarioEntity usuarioCliente) {
 		this.usuarioCliente = usuarioCliente;
 	}
 
 	public ContenidoEntity getContenido() {
 		return contenido;
 	}
 
 	public void setContenido(ContenidoEntity contenido) {
 		this.contenido = contenido;
 	}
 
 	public Date getFechaSubida() {
 		return fechaSubida;
 	}
 
 	public void setFechaSubida(Date fechaSubida) {
 		this.fechaSubida = fechaSubida;
 	}
 
 	public String getComentarioSubida() {
 		return comentarioSubida;
 	}
 
 	public void setComentarioSubida(String comentarioSubida) {
 		this.comentarioSubida = comentarioSubida;
 	}
 
 	public Float getPrecioSubida() {
 		return precioSubida;
 	}
 
 	public void setPrecioSubida(Float precioSubida) {
 		this.precioSubida = precioSubida;
 	}
 	
 }
