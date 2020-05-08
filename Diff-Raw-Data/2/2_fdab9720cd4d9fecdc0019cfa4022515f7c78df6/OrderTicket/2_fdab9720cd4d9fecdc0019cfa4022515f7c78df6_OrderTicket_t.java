 package it.gas.altichierock.database;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.persistence.Column;
 import javax.persistence.EmbeddedId;
 import javax.persistence.Entity;
 import javax.persistence.JoinColumn;
 import javax.persistence.JoinColumns;
 import javax.persistence.NamedQuery;
 import javax.persistence.NamedQueries;
 import javax.persistence.OneToMany;
 
 @Entity
 @NamedQueries({
 	@NamedQuery(name = "order.notcomplete", query = "SELECT o FROM OrderTicket o WHERE o.completed = FALSE"),
	@NamedQuery(name = "order.notserved", query = "SELECT o FROM OrderTicket o WHERE o.completed = TRUE AND o.served = FALSE"),
 	@NamedQuery(name = "order.maxidtoday", query = "SELECT MAX(i.id.id) FROM OrderTicket i WHERE i.id.created = CURRENT_DATE")
 })
 public class OrderTicket implements Serializable {
 	private static final long serialVersionUID = 1L;
 
 	@EmbeddedId
 	private OrderTicketId id;
 	private boolean completed;
 	private boolean served;
 	@OneToMany
 	@JoinColumns({
 		@JoinColumn(name = "orderId", referencedColumnName = "id"),
 		@JoinColumn(name = "orderCreated", referencedColumnName = "created")
 	})
 	private List<Detail> detail;
 	@Column(length = 2000)
 	private String note;
 	
 	public OrderTicket() {
 		id = new OrderTicketId();
 		detail = new ArrayList<Detail>();
 	}
 
 	public OrderTicketId getId() {
 		return id;
 	}
 
 	public void setId(OrderTicketId id) {
 		this.id = id;
 	}
 
 	public boolean isCompleted() {
 		return completed;
 	}
 
 	public void setCompleted(boolean completed) {
 		this.completed = completed;
 	}
 
 	public boolean isServed() {
 		return served;
 	}
 
 	public void setServed(boolean served) {
 		this.served = served;
 	}
 
 	public List<Detail> getDetail() {
 		return detail;
 	}
 
 	public void setDetail(List<Detail> detail) {
 		this.detail = detail;
 	}
 
 	public String getNote() {
 		return note;
 	}
 
 	public void setNote(String note) {
 		this.note = note;
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
 		OrderTicket other = (OrderTicket) obj;
 		if (id == null) {
 			if (other.id != null)
 				return false;
 		} else if (!id.equals(other.id))
 			return false;
 		return true;
 	}
 
 }
