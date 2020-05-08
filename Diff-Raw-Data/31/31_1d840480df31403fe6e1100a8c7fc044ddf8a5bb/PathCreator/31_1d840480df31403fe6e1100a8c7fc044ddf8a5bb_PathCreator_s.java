 /*
  *  __  __      
  * /\ \/\ \  __________   
  * \ \ \_\ \/_______  /\   
  *  \ \  _  \  ____/ / /  
  *   \ \_\ \_\ \ \/ / / 
  *    \/_/\/_/\ \ \/ /  
  *             \ \  /
  *              \_\/
  *
  * -----------------------------------------------------------------------------
  * @author: Herbert Veitengruber 
  * @version: 1.0.0
  * -----------------------------------------------------------------------------
  *
  * Copyright (c) 2013 Herbert Veitengruber 
  *
  * Licensed under the MIT license:
  * http://www.opensource.org/licenses/mit-license.php
  */
 package jhv.io;
 
 import java.io.IOException;
 import java.nio.file.Files;
 import java.nio.file.Paths;
 
 import jhv.util.debug.logger.ApplicationLogger;
 
 /**
  * Helper class for creating folder structures.
  * 
  * based on java.nio.file package
  */
 public class PathCreator 
 {
 
 	/**
 	 * createPathes.
 	 * 
 	 * @param arr
 	 * @throws IOException
 	 */
 	public static void createPathes(String[] arr) 
 			throws IOException
 	{
 		for( int i=0; i< arr.length; i++ )
 		{
 			Files.createDirectories(Paths.get(arr[i]));
			ApplicationLogger.logInfo("PathCreator: "+ arr[i]);
 		}
 	}
 
 }
