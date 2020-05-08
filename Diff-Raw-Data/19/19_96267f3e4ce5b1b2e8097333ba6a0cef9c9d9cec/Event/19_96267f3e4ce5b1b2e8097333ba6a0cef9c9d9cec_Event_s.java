 package uk.ac.cam.signups.models;
 
 import com.google.common.collect.ImmutableMap;
 
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.hibernate.annotations.GenericGenerator;
 import org.hibernate.annotations.Index;
 import org.hibernate.annotations.Sort;
 import org.hibernate.annotations.SortType;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.cam.signups.util.HibernateUtil;
 import uk.ac.cam.signups.util.Util;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.SortedMap;
 import java.util.SortedSet;
 import java.util.TreeMap;
 import java.util.TreeSet;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import javax.persistence.OrderBy;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 
 @Entity
 @Table(name = "EVENTS")
 public class Event implements Mappable {
 
 	@Transient
 	private Logger logger = LoggerFactory.getLogger(Event.class);
 
 	@Id
 	@GeneratedValue(generator = "increment")
 	@GenericGenerator(name = "increment", strategy = "increment")
 	private int id;
 
 	private String location;
 	private String room;
 	private String title;
 	private String sheetType;
 	private Calendar expiryDate;
 
 	@Index(name="obfuscatedIdIndex")
 	private String obfuscatedId;
 
 	@Column(name="dos_visibility", nullable = false, columnDefinition = "boolean default true")
 	private boolean dosVisibility;
 
 	@ManyToOne
 	@JoinColumn(name = "USER_CRSID")
 	private User owner;
 
 	@OneToMany(cascade = CascadeType.ALL, mappedBy = "event")
 	@Sort(type = SortType.NATURAL)
 	@OrderBy("calendar")
 	private SortedSet<Row> rows = new TreeSet<Row>();
 
 	@OneToMany(cascade = CascadeType.ALL, mappedBy = "event")
 	@OrderBy("id")
 	private Set<Type> types = new TreeSet<Type>();
 
 	public Event() {
 	}
 
 	public Event(int id, String location, String title, User owner,
 	    Set<Row> rows, Set<Type> types) {
 		this.id = id;
 		this.location = location;
 		this.owner = owner;
 		this.types.addAll(types);
 		this.rows.addAll(rows);
 		this.title = title;
 	}
 
 	public int getId() {
 		return id;
 	}
 
 	public void setId(int id) {
 		this.id = id;
 	}
 	
 	public String getObfuscatedId() {
 		return obfuscatedId;
 	}
 	
 	public void setObfuscatedId(String obfuscatedId) {
 		this.obfuscatedId = obfuscatedId;
 	}
 	
 	public boolean getDosVisibility() {
 		return dosVisibility;
 	}
 	
 	public void setDosVisibility(boolean dosVisibility) {
 		this.dosVisibility = dosVisibility;
 	}
 
 	public String getLocation() {
 		return location;
 	}
 
 	public void setLocation(String location) {
 		this.location = location;
 	}
 
 	public String getRoom() {
 		return room;
 	}
 
 	public void setRoom(String room) {
 		this.room = room;
 	}
 
 	public String getTitle() {
 		return this.title;
 	}
 
 	public void setTitle(String title) {
 		this.title = title;
 	}
 
 	public String getSheetType() {
 		return this.sheetType;
 	}
 
 	public void setSheetType(String sheetType) {
 		this.sheetType = sheetType;
 	}
 
 	public Calendar getExpiryDate() {
 		return this.expiryDate;
 	}
 
 	public void setExpiryDate(Calendar expiryDate) {
 		this.expiryDate = expiryDate;
 	}
 
 	public User getOwner() {
 		return this.owner;
 	}
 
 	public void setOwner(User owner) {
 		this.owner = owner;
 	}
 
 	public SortedSet<Row> getRows() {
 		return this.rows;
 	}
 
 	public void addRows(SortedSet<Row> rows) {
 		this.rows.addAll(rows);
 	}
 
 	public Set<Type> getTypes() {
 		return this.types;
 	}
 
 	public void addTypes(Set<Type> types) {
 		this.types.addAll(types);
 	}
 
 	@SuppressWarnings("unchecked")
 	public static List<ImmutableMap<String, String>> suggestRooms(
 	    String qbuilding, String qroom) {
 		Session session = HibernateUtil.getTransactionSession();
 		Query q = session
 		    .createQuery("select distinct room from Event as event where lower(event.location) like :building and lower(event.room) like :room");
 		List<String> suggestions = (List<String>) q
 		    .setParameter("building", "%" + qbuilding.toLowerCase() + "%")
 		    .setParameter("room", "%" + qroom.toLowerCase() + "%")
 		    .setMaxResults(10).list();
 		List<ImmutableMap<String, String>> suggestionsMap = new ArrayList<ImmutableMap<String, String>>();
 		for (String suggestion : suggestions)
 			suggestionsMap.add(ImmutableMap.of("room", suggestion));
 
 		return suggestionsMap;
 	}
 
 	public Map<String, String> getExpiryDateMap() {
		SimpleDateFormat formatter = new SimpleDateFormat("EEEE, MMMM d kk:mm");
 		SimpleDateFormat comparativeFormatter = new SimpleDateFormat(
 		    "yyyy MM dd HH mm");
 		String comparativeExpiry = comparativeFormatter
 		    .format(expiryDate.getTime());
 		String prettyExpiry = formatter.format(expiryDate.getTime());
 		return ImmutableMap.of("comparative", comparativeExpiry, "pretty",
 		    prettyExpiry);
 	}
 
 	public Map<String, ?> toMap() {
 		ImmutableMap.Builder<String, Object> builder = new ImmutableMap.Builder<String, Object>();
 		builder = builder.put("obfuscatedId", obfuscatedId);
 		builder = builder.put("title", title);
 		builder = builder.put("location", location);
 		builder = builder.put("room", room == null ? "" : room);
 		builder = builder.put("sheetType", sheetType);
 		builder = builder.put("owner", owner.toMap());
 		builder = builder.put("types", Util.getImmutableCollection(types));
 		builder = builder.put("lastRow", rows.last().toMap());
 
 		// Current date generator
		SimpleDateFormat formatter = new SimpleDateFormat("EEEE, MMMM d");
 		SimpleDateFormat comparativeFormatter = new SimpleDateFormat(
 		    "yyyy MM dd HH mm");
 		String currentDate = comparativeFormatter.format((new GregorianCalendar())
 		    .getTime());
 		builder = builder.put("currentDate", currentDate);
 
 		// Expiry date generator (pretty print and comparative)
 		builder = builder.put("expiryDate", getExpiryDateMap());
 
 		if (sheetType.equals("datetime")) {
 			// Make row hierarchy with dates
 			SortedMap<String, Set<Row>> temp = new TreeMap<String, Set<Row>>();
 			SortedSet<Row> rowContainer;
 			for (Row row : rows) {
 				Calendar cal = row.getCalendar();
 				String key = "" + cal.get(Calendar.YEAR) + ":"
 				    + cal.get(Calendar.MONTH) + ":" + cal.get(Calendar.DAY_OF_MONTH);
 				if (temp.containsKey(key)) {
 					temp.get(key).add(row);
 				} else {
 					rowContainer = new TreeSet<Row>();
 					rowContainer.add(row);
 					temp.put(key, rowContainer);
 				}
 			}
 
 			List<ImmutableMap<String, ?>> dates = new ArrayList<ImmutableMap<String, ?>>();
 			for (String key : temp.keySet()) {
 				// Parse date nicely
 				String[] dateArray = key.split(":");
 				int year = Integer.parseInt(dateArray[0]);
 				int month = Integer.parseInt(dateArray[1]);
 				int day = Integer.parseInt(dateArray[2]);
 				Calendar cal = new GregorianCalendar(year, month, day);
 				String date = formatter.format(cal.getTime());
 
 				dates.add(ImmutableMap.of("date", date, "rows",
 				    Util.getImmutableCollection(temp.get(key))));
 			}
 
 			builder = builder.put("dates", dates);
 			builder = builder.put("rows", new ArrayList<ImmutableMap<String, ?>>());
 			
 		} else if (sheetType.equals("manual")) {
 			builder = builder.put("dates", new ArrayList<ImmutableMap<String, ?>>());
 			List<Map<String, ?>> immutableRows = Util.getImmutableCollection(rows);
 			builder = builder.put("rows", immutableRows);
 		}
 
 		Map<String, ?> eventMap = builder.build();
 		return eventMap; 
 	}
 }
