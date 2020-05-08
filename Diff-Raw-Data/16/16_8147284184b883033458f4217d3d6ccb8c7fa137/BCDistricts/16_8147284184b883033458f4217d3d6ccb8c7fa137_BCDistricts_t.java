 package com.gwt.schoolviewer.server;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 
 import com.google.maps.gwt.client.LatLng;
 
 public class BCDistricts {
 	
     private static final BCDistricts instance = new BCDistricts();
     
     public static BCDistricts getInstance() {
     	return instance;
     }
 	
 //	public static void main(String args[]) throws IOException{
 //		BCDistricts temp = new BCDistricts();
 //		School sTemp = temp.getDistricts().get(0).getSchools().get(0);
 //		//sTemp.printSchool();
 //	}
 
 	ArrayList<District> districts = new ArrayList<District>();
 	private ArrayList<String> schoolnames = new ArrayList<String>();
 	
 	public BCDistricts() {
 		try{
 		populateDistricts(pageToList());
 		populateSchools();
 		//populateValues();
 		} catch (IOException e){}
 	}
 	
 	private void populateDistricts(ArrayList<ArrayList<String>> temp) {
 		ArrayList<ArrayList<String>> disList = transpose(temp);
 		for(int i = 1; i < disList.size(); i++)
 		{
 			ArrayList<String> tempList = disList.get(i);
 			District tempDistrict = new District(tempList.get(0), tempList.get(1), tempList.get(2), tempList.get(3));
 			districts.add(tempDistrict);
 		}
 	}
 	
 	private ArrayList<ArrayList<String>> pageToList() throws IOException {
 		TxtSplitter splitter = new TxtSplitter("http://www.bced.gov.bc.ca/reporting/odefiles/BoardLocations_Current.txt");
 		ArrayList<ArrayList<String>> temp = splitter.split();
		String[] accepted = new String[5];
 		accepted[0] = "DISTRICT_NAME";
 		accepted[1] = "DISTRICT_CITY";
 		accepted[2] = "DISTRICT_PHONE_NUMBER";
 		accepted[3] = "DISTRICT_WEBSITE";
 		for(int i = 0; i < temp.size();) {
 			if (requiredLine(temp.get(i), accepted)){
 				i++;
 			}else{
 				temp.remove(i);
 			}
 		}
 		return temp;
 	}
 	
 	private Boolean requiredLine(ArrayList<String> temp, String[] accepted) {
 		String title = temp.get(0);
 		for(int i = 0; i < accepted.length; i++){
 			if(title.equals(accepted[i]))
 				return true;
 		}
 		return false;
 	}
 	
 	private ArrayList<ArrayList<String>> transpose(ArrayList<ArrayList<String>> orig){
 		ArrayList<ArrayList<String>> temp = new ArrayList<ArrayList<String>>();
 		for(int i = 0; i < orig.get(0).size(); i++)
 		{
 			ArrayList<String> tempLine = new ArrayList<String>();
 			for(int j = 0; j < orig.size(); j++)
 			{
 				tempLine.add(orig.get(j).get(i));
 			}
 			temp.add(tempLine);
 		}
 		return temp;
 	}
 	
 	/*
 	 * 
 	 */
 	private void populateSchools() throws IOException
 	{
 		TxtSplitter splitter = new TxtSplitter("http://www.bced.gov.bc.ca/reporting/odefiles/SchoolLocations_Current.txt");
 		ArrayList<ArrayList<String>> tempList = transpose(splitter.split());
 		
 		for(int i = 1; i < tempList.size(); i++)
 		{
 			ArrayList<String> tempLine = tempList.get(i);
 			for (int j = 0; j < districts.size(); j++)
 			{
 				if (tempLine.get(3).equals(districts.get(j).getName()))
 				{
					Double lat;
					Double lon;
					try{
					lat = Double.parseDouble(tempLine.get(13));
					lon = Double.parseDouble(tempLine.get(14));
					} catch (NumberFormatException e) {
						lat = -1.0;
						lon = -1.0;
					}
					//Double lat = -1.0;
					//Double lon = -1.0;
 //					LatLng gpsLoc = LatLng.create(lat, lon);
 					schoolnames.add(tempLine.get(5));
 					School temp = new School(tempLine.get(5),tempLine.get(7),tempLine.get(11),tempLine.get(1),tempLine.get(8)
 							,tempLine.get(15),tempLine.get(16),tempLine.get(10),districts.get(j), lat, lon);
 					districts.get(j).addSchool(temp);
 				}
 			}
 		}
 	}
 	
 	private void populateValues() throws IOException
 	{
 		populateClassSize();
 	}
 	
 	private void populateClassSize() throws IOException
 	{
 		valSplitter splitter = new valSplitter("http://www.bced.gov.bc.ca/reporting/odefiles/ClassSizeCurr.txt");
 		ArrayList<ArrayList<String>> tempList = transpose(splitter.split());
 		//System.out.println("is it here?");
 		int startIndex = -1;
 		for(int i = 0; i < tempList.size(); i++)
 		{
 			if(schoolnames.contains(tempList.get(i).get(6)))
 			{
 				startIndex = i;
 				break;
 			}
 		}
 		
 		if(startIndex >= 0)
 		{
 			for(int i = 1; i < startIndex; i++)
 			{
 				tempList.remove(0);
 			}
 			
 			for(int i = 0; i < tempList.size(); i++)
 			{
 				System.out.println(tempList.get(i).get(6));
 			}
 		}
 		
 
 	}
 	
 	/*
 	 * returns the list of Districts in BC
 	 */
 	public ArrayList<District> getDistricts()
 	{
 		return districts;
 	}
 
 }
