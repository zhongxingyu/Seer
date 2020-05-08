 package pml.interfaces;
 
 import pml.loading.Loader;
 
 public class PMLServices
 {
 	
 	
 	public static boolean isQuery(String uri)
 	{
 		Loader loader = new Loader(false);
 		
 		if ( loader.isQuery(uri) )
 		{
 			return true;
 		}
 		else
 			return false;
 		
 	}
 }
