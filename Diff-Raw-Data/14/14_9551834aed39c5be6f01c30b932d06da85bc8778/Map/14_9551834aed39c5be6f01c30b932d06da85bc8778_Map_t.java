 package robot;
 
 import java.io.*;
 import java.util.ArrayList;
import java.util.StringTokenizer;
 
 public class Map
 {
 	public final double ROBOT_SIZE = 1.0; //random value?
 	private ArrayList<Point> map;
 	
	public Map(String inputFile, Point start, Point goal)
 	{
 		//read file here
 		map = new ArrayList<Point>();
 		
 		processFile(inputFile);
		
		map.add(start);
		map.add(goal);
 	}
 	
 	
 	private void processFile(String inputFile)
 	{
 		BufferedReader br = new BufferedReader();
 		
 		try {
 			br = new BufferedReader(new FileReader(inputFile));
 		}catch(FileNotFoundException e)
 		{
 			System.out.println(e);
 			e.printStackTrace();
 			return;
 		}
 		try {
 			int numPolygons = Integer.parseInt(br.readLine());
 			Polygon[] polygons = new Polygon[numPolygons];
 			
 			for(int i = 0; i < polygons.length; i++)
 			{
 				int numVertices = Integer.parseInt(br.readLine());
 				for(int j = 0; j < numVertices; j++)
 				{
					String[] nums = br.readLine().split("\\s");					
					polygons[i].add(new Point(Double.parseDouble(nums[0]), 
												Double.parseDouble(nums[1])));
 				}
 				polygons[i].grow();
 				addToMap(polygons[i]);
 			}			
 		}catch(IOException e)
 		{
 			System.out.println(e);
 			e.printStackTrace();
 			return;
 		}finally {
 			try {
 				if(br != null)
 					br.close();
 			}catch(IOException e)
 			{
 				System.out.println(e);
 				e.printStackTrace();
 				return;
 			}
 		}
 	}
 	
 	private void addToMap(Polygon p)
 	{
 		vertices = p.setOfPoints();
 		
 		for(int j = 0; j < vertices.length; i++)
 		{
 			map.add(vertices[j]);
 		}
 	}
 	
 }
