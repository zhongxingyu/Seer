 package org.cloudydemo.model;
 
 import java.util.Date;
 
 public class Hit {
 	String id;
 	final String type = "hit";
	Date timestamp;
 	int count;
 
	public Hit(String id, Date timestamp, int count) {
 		super();
 		this.id = id;
 		this.timestamp = timestamp;
 		this.count = count;
 	}
 
	public Date getTimestamp() {
 		return timestamp;
 	}
 
	public void setTimestamp(Date timestamp) {
 		this.timestamp = timestamp;
 	}
 
 	public int getCount() {
 		return count;
 	}
 
 	public void setCount(int count) {
 		this.count = count;
 	}
 
 	@Override
 	public String toString() {
 		return "Hit [timestamp=" + timestamp + ", count=" + count + "]";
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
 		Hit other = (Hit) obj;
 		if (id == null) {
 			if (other.id != null)
 				return false;
 		} else if (!id.equals(other.id))
 			return false;
 		return true;
 	}
 
 	public String getType() {
 		return type;
 	}
 }
