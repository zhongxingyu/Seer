 package com.playfish;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 public class Dvd {
 
     private String title;
     private String year;
     private String director;
 	private Date dateAdded;
     private final ArrayList<Copy> copies = new ArrayList<Copy>();
 
     public Dvd(String title, String year, String director) {
 
         this.title = title;
         this.year = year;
         this.director = director;
 
         copies.add(new Copy());
     }
 
 
 	public boolean isReleasedAfter(Date lastNewsletterDate) {
 		return dateAdded.after(lastNewsletterDate);
 	}
 
     public String getTitle() {
         return title;
     }
 
     public String getYear() {
         return year;
     }
 
     public String getDirector() {
         return director;
     }
 
     public Date getDateAdded() {
 		return dateAdded;
 	}
 
 	public void setDateAdded(Date dateAdded) {
 		this.dateAdded = dateAdded;
 	}
 
     public boolean isAvailable() {
         return copies.get(copies.size() - 1).isAvailable();
     }
 
 	public Copy borrow(Member member) {
 		
 		Copy result = new Copy();
 		result.borrow(member);
 		return result;
 	}
 
     public Copy findCopy(final Member member) {
         
         for (final Copy candidateToBeAvailable : this.copies) {
             if (member.equals(candidateToBeAvailable.onLoanTo())) {
                 return candidateToBeAvailable;
             }
         }
         return null;
     }
 
 }
