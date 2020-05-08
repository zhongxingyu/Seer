 package epam.cdp.spring.bean;
 
 import java.util.Date;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.EnumType;
 import javax.persistence.Enumerated;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.OneToOne;
 import javax.persistence.Table;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 import javax.validation.constraints.NotNull;
 
 import org.hibernate.validator.constraints.NotEmpty;
 
 @Entity
 @Table(name = "Ticket")
 public class Ticket implements Comparable<Ticket> {
 
 	@Id
 	@GeneratedValue(strategy = GenerationType.IDENTITY)
 	@Column(name="id")
 	private String id;
 
 	@NotEmpty(message = "title can not be empty")
 	@Column(name="title")
 	private String title;
 
 	@NotNull(message = "date can not be null")
 	@Temporal(TemporalType.DATE)
	@Column(name="dateTime")
 	private Date date;
 
 	@NotNull(message = "category can not be null")
 	@Enumerated(EnumType.ORDINAL)
 	@Column(name = "categoryId")
 	private TicketCategory category;
 	
 	@Column(name = "place")
 	private int place;
 
 	@OneToOne(cascade = CascadeType.ALL)
 	@JoinColumn(name = "userLogin")
 	private User user;
 
 	public Ticket() {
 	}
 
 	public Ticket(String id, String title, Date date, TicketCategory category, int place, User user) {
 		this.id = id;
 		this.title = title;
 		this.date = date;
 		this.category = category;
 		this.place = place;
 		this.user = user;
 	}
 
 	public Ticket(String id, String title, Date date, TicketCategory category, int place) {
 		this(id, title, date, category, place, null);
 	}
 
 	public String getId() {
 		return id;
 	}
 
 	public String getTitle() {
 		return title;
 	}
 
 	public void setTitle(String title) {
 		this.title = title;
 	}
 
 	public Date getDate() {
 		return date;
 	}
 
 	public void setDate(Date date) {
 		this.date = date;
 	}
 
 	public TicketCategory getCategory() {
 		return category;
 	}
 
 	public void setCategory(TicketCategory category) {
 		this.category = category;
 	}
 
 	public int getPlace() {
 		return place;
 	}
 
 	public void setPlace(int place) {
 		this.place = place;
 	}
 
 	public User getUser() {
 		return user;
 	}
 
 	public void setUser(User user) {
 		this.user = user;
 	}
 
 	@Override
 	public String toString() {
 		return "Ticket [id=" + id + ", title=" + title + ", date=" + date + ", category=" + category + ", place="
 				+ place + ", user=" + user + "]";
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
 		Ticket other = (Ticket) obj;
 		if (id == null) {
 			if (other.id != null)
 				return false;
 		} else if (!id.equals(other.id))
 			return false;
 		return true;
 	}
 
 	@Override
 	public int compareTo(Ticket t) {
 		return id.compareTo(t.getId());
 	}
 
 }
