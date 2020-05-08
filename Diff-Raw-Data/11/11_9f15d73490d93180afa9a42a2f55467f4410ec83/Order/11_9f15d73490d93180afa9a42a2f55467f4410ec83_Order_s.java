 package entities;
 
 import java.io.Serializable;
 import javax.persistence.*;
 import java.sql.Time;
 import java.util.List;
 
 
 /**
  * The persistent class for the order database table.
  * 
  */
 @Entity
 @Table(name="order")
 public class Order implements Serializable {
 	private static final long serialVersionUID = 1L;
 
 	@Id
 	private int idOrder;
 
 	private byte paid;
 
 	private Time time;
 
 	//bi-directional many-to-one association to User
     @ManyToOne
 	@JoinColumn(name="User_idUser")
 	private User user;
 
 	//bi-directional many-to-one association to Orderproduct
 	@OneToMany(mappedBy="order")
 	private List<Orderproduct> orderproducts;
 
     public Order() {
     }
 
 	public int getIdOrder() {
 		return this.idOrder;
 	}
 
 	public void setIdOrder(int idOrder) {
 		this.idOrder = idOrder;
 	}
 
 	public byte getPaid() {
 		return this.paid;
 	}
 
 	public void setPaid(byte paid) {
 		this.paid = paid;
 	}
 
 	public Time getTime() {
 		return this.time;
 	}
 
 	public void setTime(Time time) {
 		this.time = time;
 	}
 
 	public User getUser() {
 		return this.user;
 	}
 
 	public void setUser(User user) {
 		this.user = user;
 	}
 	
 	public List<Orderproduct> getOrderproducts() {
 		return this.orderproducts;
 	}
 
 	public void setOrderproducts(List<Orderproduct> orderproducts) {
 		this.orderproducts = orderproducts;
 	}
 	
 }
