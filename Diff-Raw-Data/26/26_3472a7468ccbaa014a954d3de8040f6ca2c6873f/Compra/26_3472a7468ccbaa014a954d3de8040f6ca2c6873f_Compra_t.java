 package web.ecommerce.tpfinal.ecommerce_web.clasesDominio;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.OneToMany;
 import javax.persistence.OneToOne;
 
 import web.ecommerce.tpfinal.ecommerce_web.account.Account;
 
 @Entity
public class Compra{ //extends IComentable ??
 
 	private boolean estado = false;
 	
 	@Id
 	@GeneratedValue(strategy=GenerationType.IDENTITY)
 	private long id;
 	
 	@OneToOne
 	private Account account;
 	
 	@OneToMany
 	private List<ProductoComprable> productos;
 	
 	private String estadoCompra;
 	
 	public Compra(Account account, ArrayList<ProductoComprable> carrito) {
 		this.estado = true;
 		this.account = account;
 		this.productos = carrito;
 	}
 
 	public Account getAccount() {
 		return account;
 	}
 
 	public void setAccount(Account account) {
 		this.account = account;
 	}
 
 		public void setProductos(List<ProductoComprable> productos) {
 			this.productos = productos;
 		}
 
 		public boolean isEstado() {
 			return estado;
 		}
 
 		public void setEstado(boolean estado) {
 			this.estado = estado;
 		}
 
 	public List<ProductoComprable> getProductos() {
 		return productos;
 	}
 
 
 	public long getId() {
 		return id;
 	}
 
 	public void setId(long id) {
 		this.id = id;
 	}
 
 	public String getEstadoCompra() {
 		return estadoCompra;
 	}
 
 	public void setEstadoCompra(String estadoCompra) {
 		this.estadoCompra = estadoCompra;
 	}
 
 }
