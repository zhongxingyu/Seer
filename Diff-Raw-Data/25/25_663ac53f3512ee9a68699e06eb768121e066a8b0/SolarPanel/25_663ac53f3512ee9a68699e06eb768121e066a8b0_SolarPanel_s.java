 package project.server;
 
 public class SolarPanel {
 
 	private String serialNumber;
 	private String brand;
 	//kiloWatts
 	private double pMax;
 	private double pMaxNOCT;
 	private int price;
 	//m
 	private double length;
 	private double width;
 	private double area;
 	
 	//default panel
 	public SolarPanel(){
 		serialNumber = "REC230PE";
 		brand = "REC";
 		pMax = 0.230;
 		pMaxNOCT = 0.175;
 		price = 450;
 		length = 1.665;
 		width = 0.991;
 		area = length * width;
 	}
 	
 	public SolarPanel(String serialNumber){
 		//TODO get data from datastore
 		
 		
 		area = length * width;
 	}
 	
 	public String getSerialNumber(){
 		return serialNumber;
 	}
 	
 	public String getBrand(){
 		return brand;
 	}
 	
	public int getPMax(){
 		return pMax;
 	}
 	
	public int getPMaxNOCT(){
 		return pMaxNOCT;
 	}
 	
 	public int getPrice(){
 		return price;
 	}
 	
 	public double getWidth(){
 		return width;
 	}
 	
 	public double getLength(){
 		return length;
 	}
 	
 	public double getArea(){
 		return area;
 	}
 }
