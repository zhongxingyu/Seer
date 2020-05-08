 package baseClasses;
 
 import java.io.Serializable;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 
 /**
  * 
  * @author Jeroen Goossens & Jon Koehmstedt
  * CSC202 Final Project
  */
 public class Transaction implements Comparable<Transaction>, Serializable {
 
 	private static final long serialVersionUID = -7138989113734490032L;
 	private String description;
 	private Calendar date;
 	private double amount;
 	
 	
 	public Transaction(String description){
 		super();
 		setDescription(description);
 	}
 	
 	public Transaction(String description, double amount, String date) {
 		super();
 		setDescription(description);
 		setAmount(amount);
 		setDate(date);
 	}
 	
 	public boolean equals(Transaction t){
 		if (t.getDescription().equals(this.getDescription())){
 			return true;
 		}
 		return false;
 	}
 	
 	//GETTERS
 	public String getDescription() {
 		return description;
 	}
 
 	public double getAmount() {
 		return amount;
 	}
 
 	public Calendar getDate() {
 		return date;
 	}
 
 	@Override
 	public int compareTo(Transaction other) {
 		if (this.getDate().compareTo(other.getDate())<0) {
 			return -1;
 		} else if (this.getDate().compareTo(other.getDate())>0){
 			return 1;
 		} else { //Dates are the same
 			if (this.getDescription().compareTo(other.getDescription())<0){
 				return -1;
 			} else if (this.getDescription().compareTo(other.getDescription())>0){
 				return 1;
 			} else {return 0;} //Dates and Description are the same
 		}
 	}
 	
 	//SETTERS
 	private void setDescription(String description) {
 		this.description = description;
 	}
 
 	private void setAmount(double amount) {
 		this.amount = amount;
 	}
 
 	private void setDate(String date2) {
 		System.out.println(date2);//print out date
 		//TODO change to calendar
 		String[] d = date2.split("/");
 		System.out.println(d[0]);
 		System.out.println(d[1]);
 		System.out.println(d[2]);
 		initializeCalendar();
 		date.clear();
 		date.set(Integer.valueOf(d[2]), Integer.valueOf(d[0]), Integer.valueOf(d[1]));	
 		}	
 	
 	
 	private void initializeCalendar() {
 		date = new Calendar() {
 			private static final long serialVersionUID = 2909678555343046040L;
 			@Override
 			public void roll(int field, boolean up) {
 			}
 			
 			@Override
 			public int getMinimum(int field) {
 				return 0;
 			}
 			
 			@Override
 			public int getMaximum(int field) {
 				return 0;
 			}
 			
 			@Override
 			public int getLeastMaximum(int field) {
 				return 0;
 			}
 			
 			@Override
 			public int getGreatestMinimum(int field) {
 				return 0;
 			}
 			
 			@Override
 			protected void computeTime() {				
 			}
 			
 			@Override
 			protected void computeFields() {				
 			}
 			
 			@Override
 			public void add(int field, int amount) {				
 			}
 			
 			@Override
 			public String toString(){
				String temp = "" + this.get(MONTH) +"/" + this.get(DAY_OF_MONTH) +"/"+ this.get(YEAR);
				return temp;
 			}
 		};
 		
 	}
 
 	public String toString(){
 		return ("[" + date+ " , " + amount + " , " + description + "]");
 	}
 	
 }
