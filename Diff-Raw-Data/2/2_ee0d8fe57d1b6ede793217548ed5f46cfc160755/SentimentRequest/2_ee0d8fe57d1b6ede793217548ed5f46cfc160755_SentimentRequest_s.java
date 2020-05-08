 package aic12.project3.common.beans;
 
 import java.util.Date;
 
 public class SentimentRequest
 {
     private int id;
     private String companyName;
     private Date from;
     private Date to;
 
     public SentimentRequest() { }
     
     public SentimentRequest(int id) {
     	this.id = id;
 	}
 
 	public int getId()
     {
         return id;
     }
 
     public void setId(int id)
     {
         this.id = id;
     }
 
     public String getCompanyName()
     {
         return companyName;
     }
 
     public void setCompanyName(String companyName)
     {
         this.companyName = companyName;
     }
 
     public Date getFrom()
     {
         return from;
     }
 
     public void setFrom(Date from)
     {
         this.from = from;
     }
 
     public Date getTo()
     {
         return to;
     }
 
     public void setTo(Date to)
     {
         this.to = to;
     }
     
     public String toString(){
    	return this.getCompanyName() + " - from: " + this.getFrom().toString() + " to: " + this.getTo().toString() + " with ID: " + this.getId(); 
     }
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + id;
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
 		SentimentRequest other = (SentimentRequest) obj;
 		if (id != other.id)
 			return false;
 		return true;
 	}
 }
