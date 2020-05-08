 /** 
  * Author:  anthony.fodor@gmail.com    
  * This code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version,
 * provided that any use properly credits the author.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details at http://www.gnu.org * * */
 
 
 package bottomUpTree;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.ObjectOutputStream;
 import java.util.HashMap;
 import java.util.List;
 import java.util.zip.GZIPOutputStream;
 
 import eTree.PivotToSpreadheet;
 
 import probabilisticNW.ProbSequence;
 
 public class PivotOut
 {
 	public static void writeBinaryFile(String outPath, List<ProbSequence> list ) throws Exception
 	{
 		ObjectOutputStream out =new ObjectOutputStream( new GZIPOutputStream(
 				new FileOutputStream(new File(outPath))));
 		
 		out.writeObject(list);
 		
 		out.flush(); out.close();
 	}
 	
 	public static void pivotOut(List<ProbSequence> list , String outPath) throws Exception
 	{	
 		HashMap<String, HashMap<String, Integer>> outerMap = new HashMap<String, HashMap<String,Integer>>();
 		
 		PivotToSpreadheet.writeResults( new File(outPath), outerMap);
 	}
 }
