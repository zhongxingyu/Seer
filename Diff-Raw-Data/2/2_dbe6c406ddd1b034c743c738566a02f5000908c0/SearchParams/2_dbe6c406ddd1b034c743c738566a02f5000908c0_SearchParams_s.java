 package com.nodc.scraper.model;
 
 import java.io.Serializable;
 import java.util.Map;
 
 import org.apache.commons.lang.builder.ToStringBuilder;
 import org.joda.time.LocalDate;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.nodc.scraper.inventory.SessionInfo;
 
 public class SearchParams implements Serializable
 {
 	private static final long serialVersionUID = 1L;
 	private static final Logger logger = LoggerFactory.getLogger(SearchParams.class);
 	
 	public static SearchParams oneRoomOneAdult(LocalDate cIn, LocalDate cOut)
 	{
 		SearchParams sp = oneRoomOneAdult();
 		sp.checkInDate = cIn;
 		sp.checkOutDate = cOut;
 		
 		
 		return sp;
 	}
 	
 	public static SearchParams oneRoomOneAdult()
 	{
 		SearchParams sp = new SearchParams();
 		sp.numAdults1 = 1;
 		sp.numRooms = 1;
 		sp.checkInDate = new LocalDate().plusDays(1);
 		sp.checkOutDate = new LocalDate().plusDays(3);
 		
 		return sp;
 	}
 	
 	private DateTimeFormatter CHECK_IN_OUT_FORMAT = DateTimeFormat.forPattern("MM/dd/yyyy");
 	
 	private LocalDate checkInDate;	
 	private LocalDate checkOutDate;
 	private String preferredProductId;  //is NODC externalId
 	private String preferredProductName; //set by InventoryServiceImpl
 	private int numRooms = 0;
 	private int numAdults1 = 1;
 	private int numChildren1 = 0;
 	private int numAdults2 = 1;
 	private int numChildren2 = 0;
 	private int numAdults3 = 1;
 	private int numChildren3 = 0;
 	private int numAdults4 = 1;
 	private int numChildren4 = 0;
 	private int room1ChildAge1 = 0;
 	private int room1ChildAge2 = 0;
 	private int room1ChildAge3 = 0;
 	private int room2ChildAge1 = 0;
 	private int room2ChildAge2 = 0;
 	private int room2ChildAge3 = 0;
 	private int room3ChildAge1 = 0;
 	private int room3ChildAge2 = 0;
 	private int room3ChildAge3 = 0;
 	private int room4ChildAge1 = 0;
 	private int room4ChildAge2 = 0;
 	private int room4ChildAge3 = 0;
 	private SessionInfo sessionInfo;
 	
 	private SearchParams(){}
 	
 	public SearchParams(Map<String, String> requestParams)
 	{	
 		preferredProductId = requestParams.get("preferredProductId");
 		checkInDate = defCheckIn(requestParams.get("departureDate"));
 		checkOutDate = defCheckOut(requestParams.get("returnDate"));
 		numRooms = defZero(requestParams.get("numRooms"));
 		numAdults1 = defZero(requestParams.get("rl:0:ro:na"));
 		numChildren1 = defZero(requestParams.get("rl:0:ro:ob:ob_body:nc"));
 		if (numRooms >= 2)
 		{
 			numAdults2 = defZero(requestParams.get("rl:1:ro:na"));
 			numChildren2 = defZero(requestParams.get("rl:1:ro:ob:ob_body:nc"));
 		}
 		
 		if (numRooms >= 3)
 		{
 			numAdults3 = defZero(requestParams.get("rl:2:ro:na"));
 			numChildren3 = defZero(requestParams.get("rl:2:ro:ob:ob_body:nc"));			
 		}
 
 		if (numRooms >=4)
 		{
 			numAdults4 = defZero(requestParams.get("rl:3:ro:na"));
 			numChildren4 = defZero(requestParams.get("rl:3:ro:ob:ob_body:nc"));			
 		}
 
 		room1ChildAge1 = defZero(requestParams.get("a:0:b:c:0:d:d_body:e"));
 		room1ChildAge2 = defZero(requestParams.get("a:0:b:c:1:d:d_body:e"));
 		room1ChildAge3 = defZero(requestParams.get("a:0:b:c:2:d:d_body:e"));
 		room2ChildAge1 = defZero(requestParams.get("a:1:b:c:0:d:d_body:e"));
 		room2ChildAge2 = defZero(requestParams.get("a:1:b:c:1:d:d_body:e"));
 		room2ChildAge3 = defZero(requestParams.get("a:1:b:c:2:d:d_body:e"));
 		room3ChildAge1 = defZero(requestParams.get("a:2:b:c:0:d:d_body:e"));
 		room3ChildAge2 = defZero(requestParams.get("a:2:b:c:1:d:d_body:e"));
 		room3ChildAge3 = defZero(requestParams.get("a:2:b:c:2:d:d_body:e"));
 		room4ChildAge1 = defZero(requestParams.get("a:3:b:c:0:d:d_body:e"));
 		room4ChildAge2 = defZero(requestParams.get("a:3:b:c:1:d:d_body:e"));
 		room4ChildAge3 = defZero(requestParams.get("a:3:b:c:2:d:d_body:e"));
 	}
 	
 	public int getNumRooms()
 	{
 		return numRooms;
 	}
 	public int getNumAdults1()
 	{
 		return numAdults1;
 	}
 	public int getNumChildren1()
 	{
 		return numChildren1;
 	}
 	public int getNumAdults2()
 	{
 		return numAdults2;
 	}
 	public int getNumChildren2()
 	{
 		return numChildren2;
 	}
 	public int getNumAdults3()
 	{
 		return numAdults3;
 	}
 	public int getNumChildren3()
 	{
 		return numChildren3;
 	}
 	public int getNumAdults4()
 	{
 		return numAdults4;
 	}
 	public int getNumChildren4()
 	{
 		return numChildren4;
 	}
 	public int getRoom1ChildAge1()
 	{
 		return room1ChildAge1;
 	}
 	public int getRoom1ChildAge2()
 	{
 		return room1ChildAge2;
 	}
 	public int getRoom1ChildAge3()
 	{
 		return room1ChildAge3;
 	}
 	public int getRoom2ChildAge1()
 	{
 		return room2ChildAge1;
 	}
 	public int getRoom2ChildAge2()
 	{
 		return room2ChildAge2;
 	}
 	public int getRoom2ChildAge3()
 	{
 		return room2ChildAge3;
 	}
 	public int getRoom3ChildAge1()
 	{
 		return room3ChildAge1;
 	}
 	public int getRoom3ChildAge2()
 	{
 		return room3ChildAge2;
 	}
 	public int getRoom3ChildAge3()
 	{
 		return room3ChildAge3;
 	}
 	public int getRoom4ChildAge1()
 	{
 		return room4ChildAge1;
 	}
 	public int getRoom4ChildAge2()
 	{
 		return room4ChildAge2;
 	}
 	public int getRoom4ChildAge3()
 	{
 		return room4ChildAge3;
 	}	
 	public LocalDate getCheckInDate()
 	{
 		return checkInDate;
 	}
 	public LocalDate getCheckOutDate()
 	{
 		return checkOutDate;
 	}
 	public SessionInfo getSessionInfo()
 	{
 		return sessionInfo;
 	}
 	public void setSessionInfo(SessionInfo sInfo)
 	{
 		this.sessionInfo = sInfo;
 	}
 
 	public void setCheckInDate(LocalDate checkInDate)
 	{
 		this.checkInDate = checkInDate;
 	}
 
 	public void setCheckOutDate(LocalDate checkOutDate)
 	{
 		this.checkOutDate = checkOutDate;
 	}
 
 	public void setNumRooms(int numRooms)
 	{
 		this.numRooms = numRooms;
 	}
 
 	public void setNumAdults1(int numAdults1)
 	{
 		this.numAdults1 = numAdults1;
 	}
 
 	public void setNumChildren1(int numChildren1)
 	{
 		this.numChildren1 = numChildren1;
 	}
 
 	public void setNumAdults2(int numAdults2)
 	{
 		this.numAdults2 = numAdults2;
 	}
 
 	public void setNumChildren2(int numChildren2)
 	{
 		this.numChildren2 = numChildren2;
 	}
 
 	public void setNumAdults3(int numAdults3)
 	{
 		this.numAdults3 = numAdults3;
 	}
 
 	public void setNumChildren3(int numChildren3)
 	{
 		this.numChildren3 = numChildren3;
 	}
 
 	public void setNumAdults4(int numAdults4)
 	{
 		this.numAdults4 = numAdults4;
 	}
 
 	public void setNumChildren4(int numChildren4)
 	{
 		this.numChildren4 = numChildren4;
 	}
 
 	public void setRoom1ChildAge1(int room1ChildAge1)
 	{
 		this.room1ChildAge1 = room1ChildAge1;
 	}
 
 	public void setRoom1ChildAge2(int room1ChildAge2)
 	{
 		this.room1ChildAge2 = room1ChildAge2;
 	}
 
 	public void setRoom1ChildAge3(int room1ChildAge3)
 	{
 		this.room1ChildAge3 = room1ChildAge3;
 	}
 
 	public void setRoom2ChildAge1(int room2ChildAge1)
 	{
 		this.room2ChildAge1 = room2ChildAge1;
 	}
 
 	public void setRoom2ChildAge2(int room2ChildAge2)
 	{
 		this.room2ChildAge2 = room2ChildAge2;
 	}
 
 	public void setRoom2ChildAge3(int room2ChildAge3)
 	{
 		this.room2ChildAge3 = room2ChildAge3;
 	}
 
 	public void setRoom3ChildAge1(int room3ChildAge1)
 	{
 		this.room3ChildAge1 = room3ChildAge1;
 	}
 
 	public void setRoom3ChildAge2(int room3ChildAge2)
 	{
 		this.room3ChildAge2 = room3ChildAge2;
 	}
 
 	public void setRoom3ChildAge3(int room3ChildAge3)
 	{
 		this.room3ChildAge3 = room3ChildAge3;
 	}
 
 	public void setRoom4ChildAge1(int room4ChildAge1)
 	{
 		this.room4ChildAge1 = room4ChildAge1;
 	}
 
 	public void setRoom4ChildAge2(int room4ChildAge2)
 	{
 		this.room4ChildAge2 = room4ChildAge2;
 	}
 
 	public void setRoom4ChildAge3(int room4ChildAge3)
 	{
 		this.room4ChildAge3 = room4ChildAge3;
 	}
 
 	public String getPreferredProductId()
 	{
 		return preferredProductId;
 	}
 	
 	public void setPreferredProductId(String pProductId)
 	{
 		this.preferredProductId = pProductId;
 	}
 	
 	public String getPreferredProductName()
 	{
 		return preferredProductName;
 	}
 	
 	public void setPreferredProductName(String ppn)
 	{
 		this.preferredProductName = ppn;
 	}
 	
 	private LocalDate defCheckIn(String date)
 	{
 		LocalDate cIn = defDate(date, new LocalDate());
		LocalDate minDate = new LocalDate().plusDays(1);
 		if (cIn.isBefore(minDate))
 			cIn = minDate;
 		
 		return cIn;
 	}
 	
 	private LocalDate defCheckOut(String date)
 	{
 		LocalDate cOut = defDate(date, new LocalDate().plusDays(2));
 		LocalDate minDate = (getCheckInDate() != null ? getCheckInDate().plusDays(1) : new LocalDate().plusDays(2));
 		if (!cOut.isAfter(minDate))
 			cOut = minDate;
 		return cOut;
 	}
 	
 	private LocalDate defDate(String date, LocalDate ld)
 	{
 		LocalDate cDate = ld;
 		if (date != null)
 		{
 			try
 			{
 				cDate = LocalDate.parse(date, CHECK_IN_OUT_FORMAT);
 			}
 			catch (Exception e)
 			{
 				logger.error("unable to parse checkInDate", e);
 			}
 		}
 		return cDate;		
 	}
 	
 	private int defZero(String s)
 	{
 		int returnVal;
 		if (s == null || s.length() == 0)
 			returnVal = 0;
 		else
 			returnVal = Integer.valueOf(s);
 		return returnVal;
 	}
 	
 	public String toString()
 	{
 		return ToStringBuilder.reflectionToString(this);
 	}
 }
