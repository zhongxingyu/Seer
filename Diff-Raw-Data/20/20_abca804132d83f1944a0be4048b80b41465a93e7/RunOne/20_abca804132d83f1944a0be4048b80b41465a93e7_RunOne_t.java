 /** 
  * Author:  anthony.fodor@gmail.com
  * 
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
 import java.util.List;
 import java.util.zip.GZIPOutputStream;
 
 import probabilisticNW.ProbSequence;
 import dereplicate.DereplicateBySample;
 
 public class RunOne
 {
 	public static final float INITIAL_THRESHOLD = 0.03f;
 	public static final float EXCEED_THRESHOLD = 0.09f;
 	
 	public static void main(String[] args) throws Exception
 	{
 		if( args.length != 2)
 		{
 			System.out.println("Usage RunOne inputFasta outputClusterFile");
 			System.exit(1);
 		}
 		
 		String sampleName = args[0].replace(DereplicateBySample.DEREP_PREFIX, "");
 		File file = new File(args[0]);
 		List<ProbSequence> initialCluster = 
 				ClusterAtLevel.getInitialSequencesFromFasta(
 						file.getAbsolutePath(), sampleName,INITIAL_THRESHOLD, EXCEED_THRESHOLD);
 		
 		int numAttempts = 1;
 		int newClusterSize = initialCluster.size()+1;
 		
 		while( numAttempts <=10 && newClusterSize > initialCluster.size())
 		{
 			System.out.println("Got " + initialCluster.size() + " trying attempt " + numAttempts );
 			newClusterSize = initialCluster.size();
 			initialCluster = ClusterAtLevel.clusterAtLevel(
 					initialCluster, INITIAL_THRESHOLD, EXCEED_THRESHOLD);
 			numAttempts++;
 		}
 		
 		System.out.println("Finished with " + initialCluster.size() + " in " + numAttempts + " attempts");
 		
 		ObjectOutputStream out =new ObjectOutputStream( new GZIPOutputStream(
 				new FileOutputStream(new File(args[1]))));
 		
 		out.writeObject(initialCluster);
 		
 		out.flush(); out.close();
 	}
 }
