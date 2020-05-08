 package persistencia;
 
 import java.io.Serializable;
 import javax.persistence.*;
 
 /**
  * Entity implementation class for Entity: Invitacion
  *
  */
 @Entity
 public class Invitacion implements Serializable {
 
 	   
 	@Id
	@GeneratedValue
 	private int id;
 	private static final long serialVersionUID = 1L;
 
 	@ManyToOne
 	private Usuario remitente;
 
 	public Invitacion() {
 		super();
 	}   
 	public int getId() {
 		return this.id;
 	}
 
 	public void setId(int id) {
 		this.id = id;
 	}
 	public Usuario getRemitente() {
 		return remitente;
 	}
 	public void setRemitente(Usuario remitente) {
 		this.remitente = remitente;
 	}
    
 }
