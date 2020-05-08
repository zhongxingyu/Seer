 package models;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import com.google.code.morphia.annotations.Embedded;
 import com.google.code.morphia.annotations.Entity;
 import com.google.code.morphia.query.Query;
 
 import play.modules.morphia.Model;
 
 @Embedded
 public class Booking extends Model {
 	
 	public Date eta;
 	
 	public Date etd;
 	
 	public int quay;
 	
 	public Double bookTugIn;
 	
 	public Double bookTugOut;
 	
 	public String cargo;
 	
 	public int cargoWeight;
 	
 	@Embedded
 	public List<Additional> additional;
 	
	public int sumAdditional;
	
 	//Calculation
 	public Double harbor;
 	public Double queue;
 	public Double pilot;
 	public Double light;
 	public Double tug;
 	public Double gnt;
 	
 	
 	public Booking(Date eta, Date etd, int quay, Double bookTugIn, 
 			Double bookTugOut, String cargo, int cargoWeight) {
 		this.eta = eta;
 		this.etd = etd;
 		this.quay = quay;
 		this.bookTugIn = bookTugIn;
 		this.bookTugOut = bookTugOut;
 		this.cargo = cargo;
 		this.cargoWeight = cargoWeight;
 		this.additional = new ArrayList();
 	}
 	
 	public Booking addAdditional(String name, Double cost) {
 		Additional newAdd = new Additional(name, cost);
         this.additional.add(newAdd);
        this.sumAdditional = calculateAdditional();
         return this;
 	}
	
	private int calculateAdditional() {
		if(this.additional.get(0).cost != null) {
			int sum = 0;
			for(int i = 0; i<this.additional.size(); i++) {
				sum += this.additional.get(i).cost;
			}
			return sum;
		} else {
			return 0;
		}
	}
 
 }
