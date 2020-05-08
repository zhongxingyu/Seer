 /*
  */
 
 package org.fit.pis.library.data;
 
 import java.io.Serializable;
 import java.util.Date;
 import javax.persistence.Basic;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 import javax.persistence.Table;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 import javax.validation.constraints.NotNull;
 import javax.xml.bind.annotation.XmlID;
 import javax.xml.bind.annotation.XmlIDREF;
 import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
 import org.fit.pis.library.back.converters.IntegerAdapter;
 
 /**
  *
  * @author Lukáš Černý <cerny.l@gmail.com>
  */
 @Entity
 @Table(name = "booking")
 @NamedQueries({
 	@NamedQuery(name = "Booking.findAll", query = "SELECT b FROM Booking b"),
 	@NamedQuery(name = "Booking.findByUser", query = "SELECT b FROM Booking b WHERE b.user = :user ORDER BY b.date ASC"),
	@NamedQuery(name = "Booking.findByBook", query = "SELECT b FROM Booking b WHERE b.book = :book"),
 	@NamedQuery(name = "Booking.findByIdbooking", query = "SELECT b FROM Booking b WHERE b.idbooking = :idbooking"),
 	@NamedQuery(name = "Booking.findByDate", query = "SELECT b FROM Booking b WHERE b.date = :date"),
 	@NamedQuery(name = "Booking.findByState", query = "SELECT b FROM Booking b WHERE b.state = :state")})
 public class Booking implements Serializable {
 	private static final long serialVersionUID = 1L;
 	@Id
 //    @GeneratedValue(strategy = GenerationType.IDENTITY)
 //    @Basic(optional = false)
 //    @NotNull
     @Column(name = "idbooking")
 	@XmlID
 	@XmlJavaTypeAdapter(IntegerAdapter.class)
 	private Integer idbooking;
 	@Basic(optional = false)
     @NotNull
     @Column(name = "date")
     @Temporal(TemporalType.DATE)
 	private Date date;
 	@Basic(optional = false)
     @NotNull
     @Column(name = "state")
 	private int state;
 	@JoinColumn(name = "iduser", referencedColumnName = "iduser")
     @ManyToOne(optional = false)
 	@XmlIDREF
 	private User user;
 	@JoinColumn(name = "idbook", referencedColumnName = "idbook")
     @ManyToOne(optional = false)
 	@XmlIDREF
 	private Book book;
 
 	public Booking() {
 	}
 
 	public Booking(Integer idbooking) {
 		this.idbooking = idbooking;
 	}
 
 	public Booking(Integer idbooking, Date date, int state) {
 		this.idbooking = idbooking;
 		this.date = date;
 		this.state = state;
 	}
 
 	public Integer getIdbooking() {
 		return idbooking;
 	}
 
 	public void setIdbooking(Integer idbooking) {
 		this.idbooking = idbooking;
 	}
 
 	public Date getDate() {
 		return date;
 	}
 
 	public void setDate(Date date) {
 		this.date = date;
 	}
 
 	public int getState() {
 		return state;
 	}
 
 	public void setState(int state) {
 		this.state = state;
 	}
 
 	public User getUser() {
 		return user;
 	}
 
 	public void setUser(User user) {
 		this.user = user;
 	}
 
 	public Book getBook() {
 		return book;
 	}
 
 	public void setBook(Book book) {
 		this.book = book;
 	}
 
 	@Override
 	public int hashCode() {
 		int hash = 0;
 		hash += (idbooking != null ? idbooking.hashCode() : 0);
 		return hash;
 	}
 
 	@Override
 	public boolean equals(Object object) {
 		// TODO: Warning - this method won't work in the case the id fields are not set
 		if (!(object instanceof Booking)) {
 			return false;
 		}
 		Booking other = (Booking) object;
 		if ((this.idbooking == null && other.idbooking != null) || (this.idbooking != null && !this.idbooking.equals(other.idbooking))) {
 			return false;
 		}
 		return true;
 	}
 
 	@Override
 	public String toString() {
 		return "org.fit.pis.library.data.Booking[ idbooking=" + idbooking + " ]";
 	}
 
 }
