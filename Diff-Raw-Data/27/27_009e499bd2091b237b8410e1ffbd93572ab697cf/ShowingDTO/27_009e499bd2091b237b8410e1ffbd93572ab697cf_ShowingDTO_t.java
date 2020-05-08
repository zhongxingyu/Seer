 package edu.unsw.comp9321.jdbc;
 
import java.sql.Date;
import java.sql.Time;
 
 public class ShowingDTO {
 	
 	private int showing_id;
	private Time showingTime;
	private Date showingDate; 
 	private int availableSeats;
 	private int cinema_id;
 	private int movie_id;
 	
	public ShowingDTO(int showing_id, Time showingTime, java.sql.Date showingDate, int availableSeats,
 			int cinema_id, int movie_id) {
 		super();
 		this.showing_id = showing_id;
 		this.showingTime = showingTime;
		this.showingDate = showingDate;
 		this.availableSeats = availableSeats;
 		this.cinema_id = cinema_id;
 		this.movie_id = movie_id;
 	}
 
 	public int getShowing_id() {
 		return showing_id;
 	}
 
 	public void setShowing_id(int showing_id) {
 		this.showing_id = showing_id;
 	}
 
	public Time getShowingTime() {
 		return showingTime;
 	}
 
	public void setShowingTime(Time showingTime) {
 		this.showingTime = showingTime;
 	}
 
	public Date getShowingDate() {
		return showingDate;
	}

	public void setShowingDate(Date showingDate) {
		this.showingDate = showingDate;
	}

 	public int getAvailableSeats() {
 		return availableSeats;
 	}
 
 	public void setAvailableSeats(int availableSeats) {
 		this.availableSeats = availableSeats;
 	}
 
 	public int getCinema_id() {
 		return cinema_id;
 	}
 
 	public void setCinema_id(int cinema_id) {
 		this.cinema_id = cinema_id;
 	}
 
 	public int getMovie_id() {
 		return movie_id;
 	}
 
 	public void setMovie_id(int movie_id) {
 		this.movie_id = movie_id;
 	}
 	
 }
