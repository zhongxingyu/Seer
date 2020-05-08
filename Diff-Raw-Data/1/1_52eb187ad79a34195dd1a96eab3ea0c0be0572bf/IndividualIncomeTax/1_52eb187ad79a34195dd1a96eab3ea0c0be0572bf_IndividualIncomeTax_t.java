 package qiren.model;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * @author qiren
  * @version 1.0
  * @created 11-10-2012 13:30:54
  */
 public class IndividualIncomeTax {
 
 	private double fiveOneInsuranceFund;
 	private double[] level = {0, 1500, 4500, 9000, 35000, 55000, 80000};
 	private double[] quickDeduction = {0, 105, 555, 1005, 2755, 5505, 13505};
 	private double salaryAfterTax;
 	private double tax;
 	private double[] taxRate = {0.03, 0.1, 0.2, 0.25, 0.3, 0.35, 0.45};
 	private double threshold;
 	private Map<String, ArrayList<Double>> eachTax;
 	
 	public IndividualIncomeTax(){
 		this.fiveOneInsuranceFund = 0;
 		this.salaryAfterTax = 0;
 		this.tax = 0;
 		this.threshold = 3500;
 		eachTax = new HashMap<String, ArrayList<Double>>();
 		eachTax.clear();
 	}
 
 	public void finalize() throws Throwable {
 
 	}
 
 	/**
 	 * 
 	 * @param salary of double type
 	 * @param city of string arguments
 	 * The standard calculate method that use to 
 	 * calculate the individual income tax  
 	 */
 	public void calculate(double salary, String city){
 		double salaryCal = salary;
 		CityFactory concreteCity = new CityFactory();
 		this.fiveOneInsuranceFund = concreteCity.createCity(city).calculate(salary);
 		salaryCal = salaryCal - this.threshold - this.fiveOneInsuranceFund;
		this.tax = 0;
 		for (int i = taxRate.length - 1; i >= 0; i--) {
 			if (salaryCal > this.level[i]) {
 				this.tax = salaryCal * this.taxRate[i] - this.quickDeduction[i];
 				break;
 			}
 		}
 		this.salaryAfterTax = salary - this.fiveOneInsuranceFund - this.tax;
 
 	}
 	
 	
 	/**
 	 * @return threshold of double type
 	 * To get the threshold of individual income tax
 	 */
 	public double getThreshold() {
 		return threshold;
 	}
 
 
 	/**
 	 * @param threshold of double type
 	 * set the default threshold
 	 */
 	public void setThreshold(double threshold) {
 		this.threshold = threshold;
 	}
 
 	
 	/**
 	 * 
 	 * @param salary of double type
 	 * @param date of string arguments
 	 * The required calculate method that use to 
 	 * calculate the individual income tax  
 	 */
 	public void calculateEachIncome(String date, double salary){
 		ArrayList<Double> tmp = new ArrayList<Double>();
 		double salaryCal = salary;
 		salaryCal = salaryCal - this.threshold;
 		if (!eachTax.containsKey(date)) eachTax.put(date, tmp);
 		if (salaryCal <= 0) {
 			eachTax.get(date).add(salary); 
 			eachTax.get(date).add(0.0);
 		}
 		
 		for (int i = taxRate.length - 1; i >= 0; i--) {
 			if (salaryCal > this.level[i]) {
 				if (eachTax.get(date).isEmpty()) {
 					eachTax.get(date).add(salary);
 					eachTax.get(date).add(salaryCal * this.taxRate[i] - this.quickDeduction[i]);
 				} else {
 					double sum = 0;
 					
 					for (int j = 1; j < eachTax.get(date).size(); j++) {
 						sum += eachTax.get(date).get(j);
 					}
 					eachTax.get(date).set(0, salary);
 					eachTax.get(date).add(salaryCal * this.taxRate[i] - this.quickDeduction[i] - sum);
 				}
 				break;
 			}
 		}
 	}
 	
 	/**
 	 * Get each tax which is saved in a Map data structure
 	 * key is the date value is the tax
 	 * 
 	 * @return eachTax of double type which
 	 * is record each tax of each salary
 	 */
 	public Map<String, ArrayList<Double>> getEachTax() {
 		return eachTax;
 	}
 	
 	/**
 	 * clear the eachTax of map type
 	 */
 	public void clearEachTax() {
 		this.eachTax.clear();
 	}
 
 	/**
 	 * Get current city's five one insurance fund
 	 * 
 	 * @return fiveOneInsuranceFund of this city
 	 */
 	public double getFiveOneInsuranceFund(){
 		return this.fiveOneInsuranceFund;
 	}
 
 	/**
 	 * Get the salary one can get after tax
 	 * 
 	 * @return salaryAfterTax of double type
 	 */
 	public double getSalaryAfterTax(){
 		return this.salaryAfterTax;
 	}
 	
 	/**
 	 * Get the tax one should pay
 	 * @return tax of double type
 	 */
 	public double getTax(){
 		return this.tax;
 	}
 
 }
