 package cidc.proyectosGeneral.obj;
 
 import java.io.Serializable;
 
 public class ExtraDocProyecto implements Serializable{
 
 	private long idProyecto;
 	private long idDoc;
 	private String fechaDoc;
 	private String fechaCarga;
 	private int tipo;
 	private int estado;	
 	private String observaciones;
 	private String nombreArchivo;
 	private String nombreDocumento;
 	private String nombreObservaciones;
 	private String usuario;
	private int tipoProyecto; // 1 si es un proycto de convocatoria. 2 si es proyecto antiguo
 	
 	public String getUsuario() {
 		return usuario;
 	}
 	public void setUsuario(String usuario) {
 		this.usuario = usuario;
 	}
 	public int getEstado() {
 		return estado;
 	}
 	public void setEstado(int estado) {
 		this.estado = estado;
 	}
 	public long getIdProyecto() {
 		return idProyecto;
 	}
 	public long getIdDoc() {
 		return idDoc;
 	}
 	public String getFechaDoc() {
 		return fechaDoc;
 	}
 	public String getFechaCarga() {
 		return fechaCarga;
 	}
 	public int getTipo() {
 		return tipo;
 	}
 	public String getObservaciones() {
 		return observaciones;
 	}
 	public String getNombreArchivo() {
 		return nombreArchivo;
 	}
 	public String getNombreDocumento() {
 		return nombreDocumento;
 	}
 	public String getNombreObservaciones() {
 		return nombreObservaciones;
 	}
 	public void setIdProyecto(long idProyecto) {
 		this.idProyecto = idProyecto;
 	}
 	public void setIdDoc(long idDoc) {
 		this.idDoc = idDoc;
 	}
 	public void setFechaDoc(String fechaDoc) {
 		this.fechaDoc = fechaDoc;
 	}
 	public void setFechaCarga(String fechaCarga) {
 		this.fechaCarga = fechaCarga;
 	}
 	public void setTipo(int tipo) {
 		this.tipo = tipo;
 	}
 	public void setObservaciones(String observaciones) {
 		this.observaciones = observaciones;
 	}
 	public void setNombreArchivo(String nombreArchivo) {
 		this.nombreArchivo = nombreArchivo;
 	}
 	public void setNombreDocumento(String nombreDocumento) {
 		this.nombreDocumento = nombreDocumento;
 	}
 	public void setNombreObservaciones(String nombreObservaciones) {
 		this.nombreObservaciones = nombreObservaciones;
 	}
	public int getTipoProyecto() {
		return tipoProyecto;
	}
	public void setTipoProyecto(int tipoProyecto) {
		this.tipoProyecto = tipoProyecto;
	}
 			
 }
 
