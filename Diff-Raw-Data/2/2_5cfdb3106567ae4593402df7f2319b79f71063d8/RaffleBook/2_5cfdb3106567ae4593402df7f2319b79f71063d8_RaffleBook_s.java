 package au.org.scoutmaster.domain;
 
 import java.sql.Date;
 
 import javax.persistence.Access;
 import javax.persistence.AccessType;
 import javax.persistence.AttributeOverride;
 import javax.persistence.AttributeOverrides;
 import javax.persistence.Column;
 import javax.persistence.Embedded;
 import javax.persistence.Entity;
 import javax.persistence.ManyToOne;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 import javax.persistence.Table;
 
 import au.org.scoutmaster.domain.accounting.Money;
 
 /**
  * Represents a Raffle ticket Book that has been sent from Branch to be sold.
  * 
  * @author bsutton
  *
  */
 @Entity(name="RaffleBook")
 @Table(name="RaffleBook")
 @Access(AccessType.FIELD)
 
 @NamedQueries(
 { @NamedQuery(name = RaffleBook.FIND_FIRST_UNALLOCATED, query = "SELECT rafflebook FROM RaffleBook rafflebook WHERE rafflebook.raffleAllocation is null order by rafflebook.firstNo")
 })
 
 
 public class RaffleBook extends BaseEntity
 {
 	private static final long serialVersionUID = 1L;
 	
 	public static final String FIND_FIRST_UNALLOCATED = "RaffleBook.findFirstUnallocated";
 	
 	/**
 	 * The raffle this book is attached to.
 	 */
 	@ManyToOne(targetEntity=Raffle.class)
 	Raffle raffle;
 	
 	/**
 	 * The allocation that this book was issued to a Contact under.
 	 * Will be null if the book hasn't been issued.
 	 */
 	RaffleAllocation raffleAllocation;
 	
 	/**
 	 * The no. of tickets in the book.
 	 */
 	private Integer ticketCount = new Integer(10);
 	
 
 	/**
 	 * The first ticket no in the book.
 	 */
 	private Integer firstNo = new Integer(0);
 	
 	/**
 	 * The contact the book was allocated to.
 	 */
 //	@ManyToOne(targetEntity=Contact.class)
 //	private Contact allocatedTo;
 //	
 //	/**
 //	 * The date the book was allocated to a Contact.
 //	 */
 //	private Date dateAllocated;
 //
 //	/**
 //	 * The contact that issued the book to the 'allocatedTo' contact.
 //	 */
 //	private Contact issuedBy;
 //
 //	/**
 //	 * The date the book was actually given to the Contact.
 //	 */
 //	private Date dateIssued;
 	
 	/**
 	 * The no. of tickets that have been returned.
 	 */
 	private Integer ticketsReturned = new Integer(0);
 	
 	/**
 	 * The amount of money returned (in $) for this book.
 	 */
 	@Embedded
 	@AttributeOverrides(
 	{
 			@AttributeOverride(name = "fixedDoubleValue", column = @Column(name = "amountReturnedMoneyValue")),
 			@AttributeOverride(name = "precision", column = @Column(name = "amountReturnedMoneyPrecision"))
 	})
 	
 	private Money amountReturned = new Money(0);
 	
 	/**
 	 * The date the money and tickets stubs were returned.
 	 */
 	private Date dateReturned;
 	
 	/**
 	 * The contact that collected the money/tickets
 	 */
 	private Contact collectedBy;
 	
 	/**
 	 * True if a receipt has been issued.
 	 */
 	private Boolean receiptIssued = new Boolean(false);
 
 	/**
 	 * Any special notes about the book
 	 */
 	private String notes;
 	
 	
 	public String getName()
 	{
 		return this.firstNo + (this.raffleAllocation == null ? " Available" : " Allocated To: " + this.raffleAllocation.getAllocatedTo().getFullname());
 	}
 	
 	public Integer getTicketCount()
 	{
 		return ticketCount;
 	}
 
 	public void setTicketCount(Integer ticketCount)
 	{
 		this.ticketCount = ticketCount;
 	}
 
 	public Integer getFirstNo()
 	{
 		return firstNo;
 	}
 
 	public void setFirstNo(Integer firstNo)
 	{
 		this.firstNo = firstNo;
 	}
 
 	public Integer getTicketsReturned()
 	{
 		return ticketsReturned;
 	}
 
 	public void setTicketsReturned(Integer ticketsReturned)
 	{
 		this.ticketsReturned = ticketsReturned;
 	}
 
 	public Money getAmountReturned()
 	{
 		return amountReturned;
 	}
 
 	public void setAmountReturned(Money amountReturned)
 	{
 		this.amountReturned = amountReturned;
 	}
 
 	public Date getDateReturned()
 	{
 		return dateReturned;
 	}
 
 	public void setDateReturned(Date dateReturned)
 	{
 		this.dateReturned = dateReturned;
 	}
 
 	public Contact getCollectedBy()
 	{
 		return collectedBy;
 	}
 
 	public void setCollectedBy(Contact collectedBy)
 	{
 		this.collectedBy = collectedBy;
 	}
 
 	public Boolean getReceiptIssued()
 	{
 		return receiptIssued;
 	}
 
 	public void setReceiptIssued(Boolean receiptIssued)
 	{
 		this.receiptIssued = receiptIssued;
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
 
 	public RaffleAllocation getRaffleAllocation()
 	{
		return this.getRaffleAllocation();
 	}
 
 	public void setRaffleAllocation(RaffleAllocation allocation)
 	{
 		this.raffleAllocation = allocation;
 		
 	}
 
 }
