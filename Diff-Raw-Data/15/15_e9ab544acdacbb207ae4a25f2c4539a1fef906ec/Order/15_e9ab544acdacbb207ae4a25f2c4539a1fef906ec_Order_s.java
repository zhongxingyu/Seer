 package models;
 
 import java.util.ArrayList;
 import java.util.Currency;
 import java.util.Date;
 import java.util.List;
 
 import com.google.code.morphia.annotations.Embedded;
 import com.google.code.morphia.annotations.Entity;
 import com.google.code.morphia.annotations.Reference;
 
 import play.modules.morphia.Model;
 
 @Entity
 public class Order extends Model {
 
 	public Date date;
 	public Date deliberyDate;
 	@Reference
 	public Client client;
 	@Embedded
 	public List<Detail> detail;
 	@Embedded
 	public List<Payment> payments;
 
 	public Order(Date date, Client cl) {
 		this.date = date;
 		this.client = cl;
 		this.detail = new ArrayList<Detail>();
 		this.payments = new ArrayList<Payment>();
 	}
 
 	public void addDetail(Detail det) {
 		this.detail.add(det);
 	}
 
 	public List<Detail> getDetails() {
 		return detail;
 	}
 
 	public boolean removeDetail(Detail det) {
 		return this.detail.remove(det);
 	}
 
 	public void addPayment(Payment pay) {
 		this.payments.add(pay);
 	}
 
 	public List<Payment> getPayments() {
 		return this.payments;
 	}
 
 	public int getSize() {
 		if (detail != null) {
 			return detail.size();
 		} else {
 			return 0;
 		}
 	}
 
 	public List<String> viewDetail() {
 		List<String> lstDetail = new ArrayList<String>();
 		if (detail != null) {
 			for (Detail det : detail) {
 				String description = "";
 				switch (det.color) {
 				case 1:
 					description = "NEGRO " + Integer.toString(det.number);
 					break;
 				case 2:
 					description = "BLANCO " + Integer.toString(det.number);
 					break;
 				case 3:
 					description = "MIXTO " + Integer.toString(det.number);
 					break;
 				default:
 					break;
 				}
 
 				lstDetail.add(description);
 			}
 		} else {
 			lstDetail.add("Sin Cantidad");
 		}
 		return lstDetail;
 	}
 
 	public int getDebt() {
 		int total = 0;
 		if (detail != null) {
 			for (Detail det : detail) {
 				switch (det.color) {
 				case 1:
					total = total + 12;
 					break;
 				case 2:
					total = total + 25;
 					break;
 				case 3:
					total = total + 35;
 					break;
 				default:
 					break;
 				}
 			}
 		}
 		return total;
 	}
 }
