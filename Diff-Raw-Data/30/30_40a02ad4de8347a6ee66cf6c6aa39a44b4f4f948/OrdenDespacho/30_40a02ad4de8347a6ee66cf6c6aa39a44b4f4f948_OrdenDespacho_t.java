 package despacho.backend.entities;
 
 import java.util.*;
 
 import javax.persistence.*;
 
 @Entity
 @Table(name = "ORDEN_DESPACHO")
 public class OrdenDespacho {
 	
 	private String codigo;
 	private String estado;
 	private Date fecha;
 	private List<ArticuloOrdenDespacho> articulos;
 	
 	public OrdenDespacho() {
		this.fecha = new Date();
 	}
 	
 	public OrdenDespacho(String codigo) {
 		this.codigo = codigo;
 		this.estado = "";
 		this.fecha = new Date(); // el constructor devuelve la fecha actual
 		this.articulos = new ArrayList<ArticuloOrdenDespacho>();
 	}
 
 	@Id
 	public String getCodigo() {
 		return codigo;
 	}
 
 	public void setCodigo(String codigo) {
 		this.codigo = codigo;
 	}
 	
 	public String getEstado() {
 		return estado;
 	}
 
 	public void setEstado(String estado) {
 		this.estado = estado;
 	}
 	
 	public Date getFecha() {
 		return fecha;
 	}
 
 	public void setFecha(Date fecha) {
 		this.fecha = fecha;
 	}
 
 	@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY, mappedBy="ordenDespacho")
 	public List<ArticuloOrdenDespacho> getArticulos() {
 		return articulos;
 	}
 
 	public void setArticulos(List<ArticuloOrdenDespacho> articulos) {
 		this.articulos = articulos;
 	}
 	
 	public void setArticulo(ArticuloOrdenDespacho articulo) {
 		if (articulo == null) {
 			return;
 		}
 		
 		this.articulos.add(articulo);
 	}
 }
