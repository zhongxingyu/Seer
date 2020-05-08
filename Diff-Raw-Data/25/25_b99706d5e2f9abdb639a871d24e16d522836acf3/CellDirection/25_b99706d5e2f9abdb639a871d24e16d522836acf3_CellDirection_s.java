 import java.util.ArrayList;
 
 
 public class CellDirection {
 	
 	boolean Error;
 	private String carType;
 	private String minSpeed;
 	private String maxSpeed;
 	private String averageSpeed;
 	private ArrayList<byte[]> rawData;
 	
 	public CellDirection(String carType) {
 		this.carType = carType;
 		this.rawData = new ArrayList<byte[]>();
 	}
 	
 	public CellDirection(String carType, String minSpeed, String maxSpeed, String averageSpeed) {
 		this.carType = carType;
 		this.minSpeed = minSpeed;
 		this.maxSpeed = maxSpeed;
 		this.averageSpeed = averageSpeed;
 		this.rawData = new ArrayList<byte[]>();
 	}
 	
 	public String getCarType(){
 		return carType;
 	}
 	public String getMinSpeed() {
 		return minSpeed;
 	}
 	
 	public String getMaxSpeed(){
 		return maxSpeed;
 	}
 	
 	public String getAverageSpeed(){
 		return averageSpeed;
 	}
 	
 	public ArrayList<byte[]> getRawData() {
 		return rawData;
 	}
 	
 	public void addRawData(byte[] raw) {
 		this.rawData.add(raw);
 	}
 	
 	public void setSpeeds(String min, String max, String average) {
 		this.minSpeed = min;
 		this.maxSpeed = max;
 		this.averageSpeed = average;
 	}
 	
 	public void setError(boolean Err)
 	{
 	  this.Error = Err;
 	}
 	
 	
 	
 }
