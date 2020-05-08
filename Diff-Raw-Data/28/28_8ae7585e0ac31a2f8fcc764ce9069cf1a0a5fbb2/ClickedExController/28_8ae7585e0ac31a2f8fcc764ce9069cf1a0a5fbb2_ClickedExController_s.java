 package model;
 
 import java.util.ArrayList;
 
 public class ClickedExController {
 	// 
 	private ArrayList<Excel> exes = new ArrayList<Excel>();
 	//        
 	private Excel popExcel()
 	{
 		Excel ex = null;
 		if (!exes.isEmpty())
 		{
 			ex = exes.get(exes.size()-1);
 			exes.remove(exes.size() -1);			
 		}
 		return ex;
 	}
 	// 
 	public Excel getExcel()
 	{
 		return popExcel();
 	}
 	//,    
 	public void removeExcel(Excel ex)
 	{
 		for (Excel e: exes)
 		{
 			if (e.getX() == ex.getX() && e.getY() == ex.getY())
 				{
 				exes.remove(e);
 				break;
 				}
 		}
 	}
 	//  
 	public void addExcel(Excel ex)
 	{
 		exes.add(ex);
 	}
 	//  
 	public void clear()
 	{
 		exes.clear();
 	}
 	
 	public ArrayList<Excel> getClickedExes()
	{
 		return exes;
 	}
 
 }
