 import java.io.File;
 import java.io.IOException;
 import java.sql.Timestamp;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Scanner;
 
 
 public class AircraftScheduler {
 	
 	public LinkedList<Plane> flights;
 	public Airstrip resourceAirstrips[] = new Airstrip[3];
 
 	public AircraftScheduler(){
 
 		flights = new LinkedList<Plane>();
 		
 		for (int i = 0; i < resourceAirstrips.length; i++) {
 			 resourceAirstrips[i] = new Airstrip();
 		}
 		
 
 	}
 
 
 	public LinkedList<Plane> getFlights() {
 			return flights;
 		}
 		
 	public void removePlane(Plane rmvPlane) {
 			flights.remove(rmvPlane);
 			Collections.sort(flights, Plane.PlaneByTimeComparator);
 		}
 			
 			
 	public void createPlanes() throws IOException, ParseException, InstantiationException, IllegalAccessException
 	{
 
 		//Create Predefined Planes
 		String textFromFile = getTextFromFile("PredefinedPlanes.txt");
 		String[] PlanesData = textFromFile.split("#");
 		String[] PlaneData = null;
 		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 		
 		
 		Plane[] PlanesToAdd = new Plane[PlanesData.length-1];
 		//System.out.println( textFromFile.split("#").length );
 		
 		for( int i = 1, j = 0; i < PlanesData.length; i++)
 		{
 			
 			PlaneData = PlanesData[i].split(";");
 			
 			PlanesToAdd[j] = new Plane(  PlaneData[0],
					                     PlaneData[1].contains("1"),
 					                     new Integer(PlaneData[2]),
 					                     dateFormat.parse(PlaneData[3]).getTime() 
 					                  );
 			j++;
 
 		}
 						
 		for( int i = 0; i < PlanesToAdd.length; i++)
 		{ flights.add(PlanesToAdd[i]); }
 		
 		Collections.sort(flights, Plane.PlaneByTimeComparator);
 	}
 
 	public void addPlane( String PlaneName, boolean EmergencyFlag, int LandingDuration, long DateTime) throws ParseException
 	{
 	               
 	    Plane PlaneToAdd = new Plane(  PlaneName,
 					                 EmergencyFlag,
 					                 LandingDuration,
 					                 DateTime
 					                );
 						
         flights.add(PlaneToAdd); 
 		
 		Collections.sort(flights, Plane.PlaneByTimeComparator);
 		
 	}
 
 	public void landPlanes(){
 		
 		int i,k;
 		int RunWayWithMinGap;
 		long Gap, newGap;
 		boolean allLastPlanesAreEmergent;
 		Plane LastPlane = null;
 		
 		Collections.sort(flights, Plane.PlaneByTimeComparator);
 		
 		System.out.println("Landing Planes...");
 		
 		//Clear runways before scheduling
 		for( k=0; k < resourceAirstrips.length; k++)
 		{
 			resourceAirstrips[k].removeAllPlane();
 		}
 		
 		for( i=0; i < flights.size(); i++ )
 		{		
 			//If no available runways - then shift last plane, verify on which line gap is minimal
 			allLastPlanesAreEmergent = true;
 			RunWayWithMinGap=0;
 			Gap = resourceAirstrips[0].getGapBetweenNewAndLast(flights.get(i));
 			newGap = Gap;			
 			
 			for( k=0; k < resourceAirstrips.length; k++)
 			{
 				if( Gap > (newGap = resourceAirstrips[k].getGapBetweenNewAndLast(flights.get(i))) )
 				{
 					Gap = newGap;
 					RunWayWithMinGap = k;
 				}				
 				
 			if( ! resourceAirstrips[k].isLastPlaneEmergency() ) { allLastPlanesAreEmergent = false; }
 			}
 				
 			if( Gap == 0 || ! flights.get(i).isEmergencyFlag() || allLastPlanesAreEmergent )
 			{
 			   flights.get(i).setScheduledTime( new Timestamp(flights.get(i).getScheduledTime().getTime() + Gap) );
 			   resourceAirstrips[RunWayWithMinGap].addPlane(flights.get(i));
 			}
 			else
 			{				
 				
 				for( k=0; k < resourceAirstrips.length; k++)
 				{
 					if(  ! resourceAirstrips[k].isLastPlaneEmergency() 
 							&& Gap > (newGap = resourceAirstrips[k].getGapBetweenNewAndLast(flights.get(i))) )
 					{
 						Gap = newGap;
 						RunWayWithMinGap = k;
 					}					
 				}
 				
 				LastPlane = resourceAirstrips[RunWayWithMinGap].getLastPlane();
 				
 				if(LastPlane == null)
 				{
 					flights.get(i).setScheduledTime( new Timestamp(flights.get(i).getScheduledTime().getTime() + Gap) );
 					resourceAirstrips[RunWayWithMinGap].addPlane(flights.get(i));
 					continue;
 				}
 				
 				flights.get(i).setScheduledTime( new Timestamp(LastPlane.getScheduledTime().getTime()) );
 				
 				LastPlane.setScheduledTime(new Timestamp(flights.get(i).getScheduledTime().getTime() 
 						                                   + flights.get(i).getLandingDuration()*60*1000) );     
 												
 				resourceAirstrips[RunWayWithMinGap].removeLastPlane();
 				resourceAirstrips[RunWayWithMinGap].addPlane(flights.get(i));
 				resourceAirstrips[RunWayWithMinGap].addPlane(LastPlane);
 			}
 		}
 		
 		//Print results		
 		List<Plane> Rez = new ArrayList<Plane>();
 		
 		for( k=0; k < resourceAirstrips.length; k++)
 		{
 			Rez = resourceAirstrips[k].getPlanes();
 				
 			for( i=0; i < Rez.size(); i++)
 			{
 				System.out.println( k + " " + Rez.get(i).getScheduledTime().toString() + " " + Rez.get(i).getPlaneName());
 			}	
 		}			
 		
 	}
 	
 	private static String getTextFromFile(String fileName) throws IOException {
 		StringBuilder textFromFile = new StringBuilder();
 		Scanner scanner = new Scanner(new File(fileName));
 		try {
 			while (scanner.hasNextLine()) {
 				textFromFile.append(scanner.nextLine());
 			}
 		} finally {
 			scanner.close();
 		}
 
 		return textFromFile.toString();
 	}
 	
 }
