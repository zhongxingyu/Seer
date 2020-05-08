 import java.util.*;
 
 public abstract class Vehicle {
 	String color;
 	Double speed;
 	String nickname;
 	
 	Vehicle(String c, Double s, String n)
 	{
 		color = c;
 		speed = s;
 		nickname = n;
 	}
 	
 	public abstract double getNumberOfWheels();	
 }
 
 class VehicleComparator_Speed implements Comparator<Vehicle>
 {
 	public int compare(Vehicle vh1, Vehicle vh2)
 	{
 		if(vh1.speed <= vh2.speed) return -1;
 		return 1;
 	}
 }
 
 class VehicleComparator_Color implements Comparator<Vehicle>
 {
 	public int compare(Vehicle vh1, Vehicle vh2)
 	{
 		if(vh1.color.charAt(0) < vh2.color.charAt(0)) return -1;
 		if(vh1.color.charAt(0) == vh2.color.charAt(0))
 		{
			return compare(vh1.color.substring(1), vh2.color.substring(1));
 		}
 		return 1;
 	}
 	
 	public int compare(String c1, String c2)
 	{
 		if(c1.charAt(0) < c2.charAt(0)) return -1;
 		if(c1.charAt(0) == c2.charAt(0))
 		{
 			if (c1.length() > 1 && c2.length() > 1)
 				{
 					return compare(c1.substring(1), c2.substring(1));
 				}
 		}
 		return 1;
 	}
 }
 
 class VehicleComparator_Wheels implements Comparator<Vehicle>
 {
 	public int compare(Vehicle vh1, Vehicle vh2)
 	{
 		if(vh1.getNumberOfWheels() < vh2.getNumberOfWheels()) return -1;
 		
 		return 1;
 	}
 }
