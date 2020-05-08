 package mhcs.danielle;
 
 import java.util.ArrayList;
 
 import com.google.gwt.touch.client.Point;
 
 import mhcs.dan.Module;
 
 
 
 /**
  *  MinimumConfiguration objects returns the 
  *  grid indices of the appropriate locations
  *  of each module in the configuration. 
  *    
  */
 public class MinimumConfiguration{
 	/**
 	 * ArrayList<Minimum>: the data members for this object.
 	 * There are public getters but no setters.
 	 */
 	private ArrayList<Minimum> minArray;
 	
 	/**
 	 * allGood and temp are privately used variables.
 	 */
 	private boolean allGood;
 	private Module temp;
 	int count;
	ArrayList<Module> theList;
 	
 	/**
 	 * MinimumConfiguration constructor creates
 	 * an object that consists of two lists: one of 
 	 * module type and one of coordinate points.
 	 *  
 	 * This coordinate point is returned as Java 
 	 * coordinates and *not* map coordinates.
 	 * 
 	 * @param theList is the list of modules
 	 */
	public MinimumConfiguration(ArrayList<Module> theList){
 		
 		this.theList = theList;
 		count = theList.size();
 		allGood = testMinConfig();
 		minArray = new ArrayList<Minimum>();
 		
 		if(allGood==true){
 			setMinConfig();
 		}else{
 			count = 0;
 			minArray.clear();
 		}
 	}
 	
 	/**
 	 * Tests to see if min config is possible
 	 * @return bool showing if min config is possible
 	 */
 	private boolean testMinConfig(){
 		// Code code;
 		int i = 0;
 		boolean allGood = true;
 		
 		// create array for type count, initialize all to zero
 		int[] codes = new int[10];
 		for(i = 0; i < count; i++){
 			codes[i] = 0;
 		}
 		assert(codes[7]==0);
 		assert(codes[9]==0);
 		
 		// Traverse list of modules counting types
 		for(i = 0; i < count; i++){
 			assert(count == theList.size());
 			
 			temp = theList.get(i);
 			/**
 			 * Counts each module type in our list
 			 * 
 			 * index - code value
 			 * 0....plain
 			 * 1....med
 			 * 2....power
 			 * 3....storage	
 			 * 4....air
 			 * 5....canteen
 			 * 6....dorm
 			 * 7....sanitation
 			 * 8....gym
 			 * 9....control
 			 */
 			String codeTmp = temp.getCode();
 			if(0 == codeTmp.compareTo("plain")){
 				codes[0]++;
 			}else if(0 == codeTmp.compareTo("med")){
 				codes[1]++;
 			}else if(0==codeTmp.compareTo("power")){
 				codes[2]++;
 			}else if(0==codeTmp.compareTo("storage")){
 				codes[3]++;
 			}else if(0==codeTmp.compareTo("air")){
 				codes[4]++;
 			}else if(0==codeTmp.compareTo("canteen")){
 				codes[5]++;
 			}else if(0==codeTmp.compareTo("dorm")){
 				codes[6]++;
 			}else if(0==codeTmp.compareTo("sanitation")){
 				codes[7]++;
 			}else if(0==codeTmp.compareTo("gym")){
 				codes[8]++;
 			}else if(0==codeTmp.compareTo("control")){
 				codes[9]++;
 			}
 			
 		}
 		
 		// Test to make sure we aren't missing any modules
 		for(i = 0; i < count; i++){
 			if(codes[i]==0){
 				allGood = false;
 			}
 		}
 		return allGood;
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public void setMinConfig(){
 		int total = 10;
 		int valuex = 0;
 		int valuey = 0;
 		String str = "";
 		
 		// Traverse list and collect coord. of modules used
 		for(int i = 0; i < total; i++){
 			
 			Point pt = new Point(valuex, valuey);
 			if((i >=0)&&(i < 3)){
 				str = "plain";
 				if(i == 0){
 					valuex = 1;
 					valuey = 1;
 				}else if(i == 1){
 					valuex = 2;
 					valuey = 2;
 				}else if(i == 2){
 					valuex = 1;
 					valuey = 3;
 				}
 			}else if(i == 3){
 				str = "sanitation";
 				valuex = 0;
 				valuey = 1;
 			}else if(i == 4){
 				str = "control";
 				valuex = 1;
 				valuey = 2;
 			}else if(i == 5){
 				str = "air";
 				valuex = 1;
 				valuey = 4;
 			}else if(i ==6){
 				str = "canteen";
 				valuex = 3;
 				valuey = 2;
 			}else if(i == 7){
 				str = "power";
 				valuex = 0;
 				valuey = 2;
 			}else if(i == 8){
 				str = "storage";
 				valuex = 2;
 				valuey = 3;
 			}else if(i == 9){
 				str = "dorm";
 				valuex = 1;
 				valuey = 0;
 			}
 			assert(str.compareTo("") < 0);
 			Minimum min = new Minimum(str,pt);
 			minArray.add(min);
 			
 			assert(minArray.size() == 10);
 		}
 		
 		
 	}
 	
 	/**
 	 * getMinArray() returns arraylist of Minimum objects. 
 	 * This array holds the modules that will be used in the min
 	 * configuration. 
 	 * @return ArrayList<Minimum>
 	 */
 	public ArrayList<Minimum> getMinArray(){
 		return minArray;
 	}
 }
