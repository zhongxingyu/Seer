 package models;
 
 import java.math.BigDecimal;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 import javax.validation.Constraint;
 
 import play.Logger;
 import play.data.format.Formats;
 import play.data.validation.Constraints;
 import play.db.ebean.Model;
 
 import com.avaje.ebean.Page;
 
 /**
  * Payment entity managed by Ebean
  */
 @Entity
 @Table(name = "payments")
 public class Payment extends Model {
 
 	@Id
 	public Long id;
 
 	public String name;
 
 	@Constraints.Required
 	public BigDecimal amount;
 
 	@Constraints.MaxLength(value = 500)
 	public String remarks;
 
 	public String reference;
 
 	@Column(name = "paid_date")
 	@Formats.DateTime(pattern = "dd/MM/yyyy")
 	public Date paidDate = new Date();
 
 	@Constraints.Required
 	@ManyToOne
 	public User payee;
 
 	@Constraints.Required
 	@ManyToOne
 	public PaymentType paymentType;
 
 	@Transient
 	private String monthName;
 
 	@Transient
 	public static Map<String, String> months;
 
 	@Column(name = "start_period")
 	@Formats.DateTime(pattern = "dd/MM/yyyy")
 	public Date startPeriod; 
 
 	@Column(name = "end_period")
 	@Formats.DateTime(pattern = "dd/MM/yyyy")
 	public Date endPeriod; 
 
 	@Constraints.Required
 	@ManyToOne
 	public User payer;
 
 	@Constraints.MaxLength(value=50)
     public String payeeAccountNumber;
 	
 
 	/**
 	 * Generic query helper for entity Payment with id Long
 	 */
 	public static Finder<Long, Payment> find = new Finder<Long, Payment>(
 			Long.class, Payment.class);
 
 	/**
 	 * Return a page of payment
 	 * 
 	 * @param page
 	 *            Page to display
 	 * @param pageSize
 	 *            Number of payments per page
 	 * @param sortBy
 	 *            Payment property used for sorting
 	 * @param order
 	 *            Sort order (either or asc or desc)
 	 * @param filter
 	 *            Filter applied on the name column
 	 */
 	public static Page<Payment> page(int page, int pageSize, String sortBy,
 			String order, String filter) {
 		return find.where().ilike("payee.name", "%" + filter + "%")
 				.orderBy(sortBy + " " + order).fetch("payee")
 				.findPagingList(pageSize).getPage(page);
 	}
 	
 	public static Page<Payment> pageByUserId(int page, int pageSize, String sortBy,
 			String order, Long userId) {
 		return find.where().eq("payer.id", userId)
 				.orderBy(sortBy + " " + order).fetch("payee")
 				.findPagingList(pageSize).getPage(page);
 	}
 
 	public static Map<String, String> yearOptions() {
 		LinkedHashMap<String, String> options = new LinkedHashMap<String, String>();
 
		Calendar cal = Calendar.getInstance();
		int currentYear = cal.get(Calendar.YEAR);
		int nextYear = currentYear + 1;
		for(int i=nextYear; i>nextYear-3; i--){
			String year = Integer.toString(i);
			options.put(year, year);
		}
 
 		return options;
 	}
 
 	public static Map<String, String> monthOptions() {
 		LinkedHashMap<String, String> options = new LinkedHashMap<String, String>();
 
 		options.put("1", "January");
 		options.put("2", "February");
 		options.put("3", "March");
 		options.put("4", "April");
 		options.put("5", "May");
 		options.put("6", "June");
 		options.put("7", "July");
 		options.put("8", "August");
 		options.put("9", "September");
 		options.put("10", "October");
 		options.put("11", "November");
 		options.put("12", "December");
 
 		return options;
 	}
 
 	public Date getStartPeriod(){
 		if(this.id == null && this.startPeriod == null){
 			Calendar tempDate = Calendar.getInstance();
 			tempDate.set(Calendar.DATE, 1);
 			this.startPeriod = tempDate.getTime();
 		}
 		return this.startPeriod;
 	}
 	
 	public Date getEndPeriod(){
 		if(this.id == null && this.endPeriod == null){
 			Calendar tempDate = Calendar.getInstance();
 			tempDate.set(Calendar.DATE, tempDate.getActualMaximum(Calendar.DATE));
 			this.endPeriod = tempDate.getTime();
 		}
 		return this.endPeriod;
 	}
 
 }
