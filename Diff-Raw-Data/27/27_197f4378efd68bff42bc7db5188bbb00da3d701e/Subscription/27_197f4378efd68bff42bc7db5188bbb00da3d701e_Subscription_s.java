 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package model.object;
 
import java.sql.Date;
 import java.sql.Timestamp;
 
 /**
  *
  * @author Yoldark34 (yoldark@gmail.com)
  */
 public class Subscription {
 
 	private int id = -1;
 	private int idPrice;
 	private int idNemoUser;
 	private float amount = -1;
 	private Timestamp startDate;
 	private Timestamp endDate;
 
 	public int getId() {
 		return id;
 	}
 
 	public void setId(int id) {
 		this.id = id;
 	}
 
 	public int getIdPrice() {
 		return idPrice;
 	}
 
 	public void setIdPrice(int idPrice) {
 		this.idPrice = idPrice;
 	}
 
 	public int getIdNemoUser() {
 		return idNemoUser;
 	}
 
 	public void setIdNemoUser(int idNemoUser) {
 		this.idNemoUser = idNemoUser;
 	}
 
 	public float getAmount() {
 		return amount;
 	}
 
 	public void setAmount(float amount) {
 		this.amount = amount;
 	}
 
 	public Timestamp getStartDate() {
 		return startDate;
 	}
 
 	public void setStartDate(Timestamp startDate) {
 		this.startDate = startDate;
 	}
 
 	public Timestamp getEndDate() {
 		return endDate;
 	}
 
 	public void setEndDate(Timestamp endDate) {
 		this.endDate = endDate;
 	}
 }
