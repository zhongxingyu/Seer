 package ie.cit.cloud.tickets.model.performance;
 
 import java.util.Date;
 
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.Inheritance;
 import javax.persistence.InheritanceType;
 import javax.persistence.Table;
 
 import org.hibernate.validator.constraints.NotEmpty;
 
 @Entity
 @Table(name = "EVENT")
 @Inheritance(strategy = InheritanceType.SINGLE_TABLE)
 public class Event
 {
 	@Id
 	@GeneratedValue
 	private String id;
 
 	@NotEmpty
 	private Performer performer;
 
 	@NotEmpty
 	private Location location;
	
 	@NotEmpty
 	private Date date;
 
 	@NotEmpty
 	private String eventName;
 
 	@NotEmpty
 	private int ticketCount;
 
 	public Event()
 	{
		
 	}
	
 	public Event(final Performer performer, final Location location, final Date date, final String eventName, final int ticketCount)
 	{
 		this.performer = performer;
 		this.location = location;
 		this.date = date;
 		this.eventName = eventName;
 		this.ticketCount = ticketCount;
 	}
	
 	public String getId()
 	{
 		return id;
 	}
 	public void setId(final String id)
 	{
 		this.id = id;
 	}
 	public Performer getPerformer()
 	{
 		return performer;
 	}
 	public void setPerformer(final Performer performer)
 	{
 		this.performer = performer;
 	}
 	public Location getLocation()
 	{
 		return location;
 	}
 	public void setLocation(final Location location)
 	{
 		this.location = location;
 	}
 	public Date getDate()
 	{
 		return date;
 	}
 	public void setDate(final Date date)
 	{
 		this.date = date;
 	}
 	public String getEventName()
 	{
 		return eventName;
 	}
 	public void setEventName(final String eventName)
 	{
 		this.eventName = eventName;
 	}
 
 	public int getTicketCount()
 	{
 		return ticketCount;
 	}
 
 	public void setTicketCount(final int ticketCount)
 	{
 		this.ticketCount = ticketCount;
 	}
 
	
 }
