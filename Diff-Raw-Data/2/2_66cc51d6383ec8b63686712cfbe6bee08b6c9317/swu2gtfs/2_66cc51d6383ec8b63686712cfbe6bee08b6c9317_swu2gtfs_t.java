 package de.uulm.datalove.swu2gtfs;
 
 import java.io.BufferedWriter;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.util.HashMap;
 
 
 public class swu2gtfs {
 	
 	public static int tripCounter  = 0;
 	public static int routeCounter = 0;
 	
 	protected static HashMap<String, Route> routes = new HashMap<String, Route>(22);
 
 
 	public static void main(String[] args) {
 		
 		new Parser(args, routes);				
 				
 	    for( String name: routes.keySet() )
 	    {
 	       Route currentRoute = routes.get(name);
 	       routeCounter++;
 	       
 	       tripCounter = tripCounter + currentRoute.trips().size();
 	    }
 
 		System.out.println(routeCounter + " Routes with " + tripCounter + " trips created.");
 		
		StringBuffer shapeOutput = new StringBuffer("shape_id,shape_pt_lon,shape_pt_lat,shape_pt_sequence\n");
 		new uniqueTripFinder(routes, shapeOutput);
 		new stoptimesWriter(routes);
 		new tripsWriter(routes);
 
 		try {
 			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream( "shapes.txt" ) ) );
 			out.write(shapeOutput.toString());
 			if( out != null ) out.close();
 			System.out.println("\nshapes.txt written");
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		
 		System.out.println("Done.");
 		
 	}
 
 }
