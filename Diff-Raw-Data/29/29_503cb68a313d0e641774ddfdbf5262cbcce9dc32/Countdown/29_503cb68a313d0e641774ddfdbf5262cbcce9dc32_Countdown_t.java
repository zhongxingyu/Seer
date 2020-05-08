 package entities;
 
 import java.sql.Date;
 import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
 
 public class Countdown {
 	private int id;
 	private String name, description;
 	private Date endDate;
 	private Time endTime;
 
 	public Countdown() {
 
 	}
 
 	public Countdown(int id, String name, String description, Date endDate, Time endTime) {
 		super();
 		this.id = id;
 		this.name = name;
 		this.description = description;
 		this.endDate = endDate;
 		this.endTime = endTime;
 	}
 
 	public int getId() {
 		return id;
 	}
 
 	public void setId(int id) {
 		this.id = id;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	public Date getEndDate() {
 		return endDate;
 	}
 
 	public void setEndDate(Date endDate) {
 		this.endDate = endDate;
 	}
 
 	public Time getEndTime() {
 		return endTime;
 	}
 
 	public void setEndTime(Time endTime) {
 		this.endTime = endTime;
 	}
	public String printEndDate() throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		java.util.Date d = sdf.parse(endDate.toString());
		sdf.applyPattern("yyyy/MM/dd");
		String newDateformat = sdf.format(d);
		return newDateformat;
	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((description == null) ? 0 : description.hashCode());
 		result = prime * result + ((endDate == null) ? 0 : endDate.hashCode());
 		result = prime * result + ((endTime == null) ? 0 : endTime.hashCode());
 		result = prime * result + id;
 		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
 		Countdown other = (Countdown) obj;
 		if (description == null) {
 			if (other.description != null)
 				return false;
 		} else if (!description.equals(other.description))
 			return false;
 		if (endDate == null) {
 			if (other.endDate != null)
 				return false;
 		} else if (!endDate.equals(other.endDate))
 			return false;
 		if (endTime == null) {
 			if (other.endTime != null)
 				return false;
 		} else if (!endTime.equals(other.endTime))
 			return false;
 		if (id != other.id)
 			return false;
 		if (name == null) {
 			if (other.name != null)
 				return false;
 		} else if (!name.equals(other.name))
 			return false;
 		return true;
 	}
 
 }
