 package au.org.scoutmaster.domain;
 
 import java.sql.Date;
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.persistence.Access;
 import javax.persistence.AccessType;
 import javax.persistence.Entity;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 
 /**
  * Represents an Allocation of Raffle Books to a contact from a Raffle.
  * 
  * @author bsutton
  *
  */
 @Entity(name="RaffleAllocation")
 @Table(name="RaffleAllocation")
 @Access(AccessType.FIELD)
 
 public class RaffleAllocation extends BaseEntity
 {
 	private static final long serialVersionUID = 1L;
 	
 	/**
 	 * The raffle this book is attached to.
 	 */
 	@ManyToOne(targetEntity=Raffle.class)
 	Raffle raffle;
 	
 	/**
 	 * The contact the book was allocated to.
 	 */
 	@ManyToOne(targetEntity=Contact.class)
	private Contact allocatedTo = new Contact();
 	
 	/**
 	 * The date the book was allocated to a Contact.
 	 */
 	private Date dateAllocated = new Date(new java.util.Date().getTime());
 
 	/**
 	 * The contact that issued the book to the 'allocatedTo' contact.
 	 */
 	private Contact issuedBy;
 
 	/**
 	 * The date the book was actually given to the Contact.
 	 */
 	private Date dateIssued = new Date(new java.util.Date().getTime());
 	
 	/**
 	 * Any special notes about the allocation
 	 */
 	private String notes;
 
 	/**
 	 * The set of raffle books allocated by this allocation.
 	 */
 	@OneToMany(mappedBy="raffleAllocation")
 	private Set<RaffleBook> books = new HashSet<>();
 	
 	public String getName()
 	{
 		return "Allocated To: " + this.allocatedTo.getFullname() + " Date Allocated: " + this.dateIssued;
 	}
 	
 	public Contact getAllocatedTo()
 	{
 		return allocatedTo;
 	}
 
 	public void setAllocatedTo(Contact allocatedTo)
 	{
 		this.allocatedTo = allocatedTo;
 	}
 
 	public Date getDateAllocated()
 	{
 		return dateAllocated;
 	}
 
 	public void setDateAllocated(Date dateAllocated)
 	{
 		this.dateAllocated = dateAllocated;
 	}
 
 	public Contact getIssuedBy()
 	{
 		return issuedBy;
 	}
 
 	public void setIssuedBy(Contact issuedBy)
 	{
 		this.issuedBy = issuedBy;
 	}
 
 	public Date getDateIssued()
 	{
 		return dateIssued;
 	}
 
 	public void setDateIssued(Date dateIssued)
 	{
 		this.dateIssued = dateIssued;
 	}
 
 	public String getNotes()
 	{
 		return notes;
 	}
 
 	public void setNotes(String notes)
 	{
 		this.notes = notes;
 	}
 
 	public void setRaffle(Raffle raffle)
 	{
 		this.raffle = raffle;
 	}
 
 	public Set<RaffleBook> getBooks()
 	{
 		return books;
 	}
 
 	public void setBooks(Set<RaffleBook> books)
 	{
 		this.books = books;
 	}
 	
 	public String toString()
 	{
 		return "To: " + this.allocatedTo.getFullname() + " By:" + this.issuedBy.getFullname() + " On: " + this.dateAllocated + " Books: " + books.size(); 
 	}
 
 	public void addBook(RaffleBook book)
 	{
 		this.books.add(book);
 		
 	}
 	@Transient
 	public Long getBookCount()
 	{
 		return new Long(this.books.size());
 	}
 
 }
