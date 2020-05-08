 package sse.model;
 
 import java.io.Serializable;
 import java.math.BigDecimal;
 import java.util.Date;
 import java.util.List;
 
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 
 /**
  * 
  * @author cog
  *
  */
 @Entity
 @Table(name = "bill")
 public class Bill implements Serializable {
 	private static final long serialVersionUID = 1393823254480413988L;
 
 	@Id
 	@GeneratedValue(strategy = GenerationType.IDENTITY)
 	private Long id;
 	private String roomNumber;
 //	private LocalDate date;
 	private BigDecimal amount;
 	
 	private Date date;
 
	@OneToMany
 	private List<Reservation> reservations;
 
 	public String getRoomNumber() {
 		return roomNumber;
 	}
 
 	public void setRoomNumber(String roomNumber) {
 		this.roomNumber = roomNumber;
 	}
 
 //	public LocalDate getDate() {
 //		return date;
 //	}
 //
 //	public void setDate(LocalDate date) {
 //		this.date = date;
 //	}
 	
 	
 
 	public Date getDate() {
 		return date;
 	}
 
 	public void setDate(Date date) {
 		this.date = date;
 	}
 
 	public BigDecimal getAmount() {
 		return amount;
 	}
 
 	public void setAmount(BigDecimal amount) {
 		this.amount = amount;
 	}
 
 	public Long getId() {
 		return id;
 	}
 
 	public List<Reservation> getReservations() {
 		return reservations;
 	}
 
 	public void setReservations(List<Reservation> reservations) {
 		this.reservations = reservations;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((id == null) ? 0 : id.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		Bill other = (Bill) obj;
 		if (id == null) {
 			if (other.id != null)
 				return false;
 		} else if (!id.equals(other.id))
 			return false;
 		return true;
 	}
 
 }
