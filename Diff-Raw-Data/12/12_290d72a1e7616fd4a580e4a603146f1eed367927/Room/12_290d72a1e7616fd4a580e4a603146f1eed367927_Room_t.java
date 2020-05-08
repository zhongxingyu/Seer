 /**
  * 
  */
 package sse.model;
 
 import java.io.Serializable;
 import java.math.BigDecimal;
 import java.util.List;
 
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 
 /**
  * @author tobihammerer
  * @author cog
  * @author Andrea Fueresz
  * 
  */
 @NamedQueries({@NamedQuery(name = "freeRoomsInTimespan", query="SELECT r from Room r WHERE r NOT IN " +
					 "(SELECT rr from Room rr JOIN rr.reservations res WHERE res.fromDate <= :fromDate AND res.toDate >= :fromDate AND res.processed = false) " +
		"AND r NOT IN (SELECT rr from Room rr JOIN rr.reservations res WHERE res.fromDate <= :toDate AND res.toDate >= :toDate AND res.processed = false)" +
		"AND r NOT IN (SELECT rr from Room rr JOIN rr.reservations res WHERE :fromDate < res.fromDate AND :toDate > res.toDate AND res.processed = false)"),
 		@NamedQuery(name="filterRooms", query="SELECT r FROM Room r WHERE r.occupancy = ? or r.roomNumber = ?")})
 @Entity
 @Table(name = "room")
 public class Room implements Serializable {
 	private static final long serialVersionUID = 9167943127699764813L;
 
 	@Id
 	@GeneratedValue(strategy = GenerationType.IDENTITY)
 	private Long id;
 	private Integer roomNumber;
 	private Integer occupancy;
 	private BigDecimal rateSingle;
 	private BigDecimal rateDouble;
 	private BigDecimal rateTriple;
 	private BigDecimal rateSingleOneChild;
 	private BigDecimal rateSingleTwoChildren;
 	private BigDecimal rateDoubleOneChild;
 
 	@OneToMany(mappedBy = "room")
 	private List<Reservation> reservations;
 
 	public Integer getOccupancy() {
 		return occupancy;
 	}
 
 	public void setOccupancy(Integer occupancy) {
 		this.occupancy = occupancy;
 	}
 
 	public BigDecimal getRateSingle() {
 		return rateSingle;
 	}
 
 	public void setRateSingle(BigDecimal rateSingle) {
 		this.rateSingle = rateSingle;
 	}
 
 	public BigDecimal getRateDouble() {
 		return rateDouble;
 	}
 
 	public void setRateDouble(BigDecimal rateDouble) {
 		this.rateDouble = rateDouble;
 	}
 
 	public BigDecimal getRateTriple() {
 		return rateTriple;
 	}
 
 	public void setRateTriple(BigDecimal rateTriple) {
 		this.rateTriple = rateTriple;
 	}
 
 	public BigDecimal getRateSingleOneChild() {
 		return rateSingleOneChild;
 	}
 
 	public void setRateSingleOneChild(BigDecimal rateSingleOneChild) {
 		this.rateSingleOneChild = rateSingleOneChild;
 	}
 
 	public BigDecimal getRateSingleTwoChildren() {
 		return rateSingleTwoChildren;
 	}
 
 	public void setRateSingleTwoChildren(BigDecimal rateSingleTwoChildren) {
 		this.rateSingleTwoChildren = rateSingleTwoChildren;
 	}
 
 	public BigDecimal getRateDoubleOneChild() {
 		return rateDoubleOneChild;
 	}
 
 	public void setRateDoubleOneChild(BigDecimal rateDoubleOneChild) {
 		this.rateDoubleOneChild = rateDoubleOneChild;
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
 		Room other = (Room) obj;
 		if (id == null) {
 			if (other.id != null)
 				return false;
 		} else if (!id.equals(other.id))
 			return false;
 		return true;
 	}
 
 	public void setRoomNumber(Integer roomNumber) {
 		this.roomNumber = roomNumber;
 	}
 
 	public Integer getRoomNumber() {
 		return roomNumber;
 	}
 
 }
