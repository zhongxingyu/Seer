 package com.droidbrew.javakoans.eagkoans;
 
 import java.lang.reflect.Method;
 import java.math.BigDecimal;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Stack;
 
 import static org.junit.Assert.*;
 import static com.droidbrew.javakoans.eagkoans.base.Koan.*;
 
 import org.junit.Test;
 
 interface Vehicle{
 	public String fillTank();
 }
 
 class Car implements Vehicle{
 	private String brand;
 
 	public Car(String brand) {
 		super();
 		this.brand = brand;
 	}
 
 	@Override
 	public String fillTank() {
 		return "... Tank is full now!";
 	}
 	
 	@Override
 	public String toString(){
 		return "Car of brand " + brand;
 	}
 }
 
class GasStationVersion1{  // wrong implementation
	public <T> String fill(T vehicle){
 		String result = "";
 		result += "I'm going to serve " + vehicle.toString();  // Look we can call Object's methods
 		// result += " " + vehicle.fillTank();   // but we can't call specific declared methods of vehicle
 		return result;
 	}
 }
 
 
 public class C_AboutGenericsBoundTypes {
 	@Test
 	public void a_____generics_without_bounding_are_crosstype_but_blind_to_specific_methods(){
		assertEquals(___, new GasStationVersion1().fill(new Car("Ford")));
 	}
 	
 	@Test
 	public void b_____generics_without_bounding_has_no_defence_from_stuppid_use(){
		assertEquals(___, new GasStationVersion1().fill(100));
 	}
 	
	class GasStationVersion2{  // right implementation
		public <T extends Vehicle> String fill(T vehicle){
 			String result = "";
 			result += "I'm going to serve " + vehicle.toString();
 			result += " " + vehicle.fillTank();
 			return result;
 		}
 	}
 
 	@Test
 	public void c_____generics_with_bounding_have_reduced_crosstyping_but_see_specific_methods(){
		assertEquals(___, new GasStationVersion2().fill(new Car("Ford")));
 	}
 	
 	@Test
 	public void d_____generics_with_bounding_do_has_defence_from_stuppid_use(){
		// assertEquals(___, new GasStationVersion2().fill(100)); 
 		// implement Vehicle in couple ways and play with fill method
 	}
 }
