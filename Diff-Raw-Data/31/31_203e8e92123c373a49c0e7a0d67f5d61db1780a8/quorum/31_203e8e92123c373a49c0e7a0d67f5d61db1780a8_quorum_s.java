 /* Quorum protocol classes and Quorum Servers classes
  * Vinay Bharadwaj (vind.1989@gatech.edu)
 */
 
 import java.io.*;
 import java.lang.*;
 import java.math.*;
 import java.nio.MappedByteBuffer;
 import java.nio.channels.FileChannel;
 import java.nio.charset.Charset;
 import java.util.*;
 
 
 class tStamp{
 	private	long timestamp;
 
 	public tStamp(long tstamp){this.timestamp = tstamp;}
 	public long getTimestamp(){return this.timestamp;}
 	
 	public static long []greatest_timeStamp(long []tstamps, int server_count){
 		   long grtstamp[] = new long[server_count];
 		   long maxtStamp = 0;
 		   int index;
 		
 		   for(index=0; index<server_count; index++)
 			if(tstamps[index] > maxtStamp) maxtStamp = tstamps[index]; //First find the max time stamp.
 
 		for(index=0; index<server_count; index++)
 			if(tstamps[index] == maxtStamp) grtstamp[index] = 1; //Next check if multiple files have SAME time stamp.
 
 		return grtstamp;
 		
 	}
 }
 
 class QFILE{
 	private File fileptr;
    	private int serverID;
     private tStamp fileTStamp;
     public int similarityIndex;
 
     public int getServerID(){
     	return this.serverID;
     }
     
     public String toString(){
     	FileInputStream stream = null;
     	MappedByteBuffer bb = null;
     try{
     	try {
     		stream = new FileInputStream(new File(fileptr.getPath()));
     	    FileChannel fc = stream.getChannel();
     	    bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
     	    /* Instead of using default, pass in a decoder. */
     	    
     	  }
     	  
     	  finally {
     	    stream.close();
     	  }
     }
     catch (Exception e){}
     return Charset.defaultCharset().decode(bb).toString();	  
     }
     
     public QFILE(File fileptr, long tstamp, int sID){
     	this.fileptr = fileptr;
     	fileTStamp = new tStamp(tstamp);
     	serverID = sID;
     	similarityIndex = 0;
     }
     
     public File getfile(){
     	return fileptr;
     }
     
     public tStamp gettStamp(){
     	return fileTStamp;
     }
     
 }
 
 
 public class quorum{
 	
 	private long []grTimestamps;
 	private long []timeStamps;
 	private boolean qflag = false;
 		 
 	//Use this static methods to directly call quorum::quorum_files_static() to quorum files.
 	public static int quorum_files_static(QFILE [] qf, int num_servers, String dest_path){
 		quorum q1 = new quorum();
 		return q1.quorum_files(qf, num_servers, dest_path);
 	}
 	
 	//Use this static methods to directly call quorum::quorum_opcodes_static() to quorum opcodes.
 	public static long quorum_opcodes_static(long [] opcodes, int num_opcodes){
 		int opcode_similarity [] = new int[num_opcodes];
 		int i, j, max_sim_opcodes=0;
 		for(i=0; i<num_opcodes; i++) opcode_similarity[i] = 0;
 		for(i=0; i<num_opcodes; i++)
 			for(j=0; j<num_opcodes; j++)
 			{
 				if( i!=j && opcodes[i] == opcodes[j]) opcode_similarity[i]++;
 			}
 		
 		for(i=0; i<num_opcodes; i++){
 			if(opcode_similarity[i] > max_sim_opcodes) max_sim_opcodes = opcode_similarity[i];
 		}
 		
 		if(max_sim_opcodes +1 < (int)Math.floor(num_opcodes/2) + 1){System.out.println(max_sim_opcodes); return -1;}
 		for(i=0; i<num_opcodes; i++){
 			if(opcode_similarity[i] == max_sim_opcodes) return opcodes[i];
 		}
 			
 		return -1;
 	}
 	
     public quorum(){}
     
 	public quorum(QFILE [] qf, int num_servers, String dest_path) {
     	quorum_files(qf, num_servers, dest_path);
 	}
 	
     public int quorum_files(QFILE [] files, int server_count, String dest_path){
     	if(!(server_count > 0)) return -1;

     	int maxSimilarity = 1;
     	int i, count=0;
     	//long grTimestampsarry[] = new long[server_count];
     	String [] grTimestampsarry = new String[server_count];
     	timeStamps = new long[server_count];
 
     	for(i=0; i<server_count; i++){
     		timeStamps[i] = files[i].gettStamp().getTimestamp();
     		grTimestampsarry[i] = "0";
     	}
 
     	grTimestamps = tStamp.greatest_timeStamp(timeStamps, server_count);
 
     	for(i=0; i<server_count; i++)
     		if(grTimestamps[i] == 1) count++;
     	
     	
     	if(count == 1 && qflag){
     		int num_bytes=0;
     		for(i=0; i<server_count; i++){
     			if(grTimestamps[i] == 1){/*Write the contents of file files[i]->fileptr into destination path*/
     				
     				File ftemp;
     				FileOutputStream ostream = null; 
     				File infile = files[i].getfile();
     				//System.out.println(infile.toString());
     				FileInputStream instream = null;
     				try{
     				instream = new FileInputStream(infile);	
     				ftemp = new File(dest_path); 
     				ostream = new FileOutputStream(ftemp);
     				}
     				catch(Exception e){System.err.println(e.toString());}
     				
     				long size = infile.length();
     				byte [] arr = new byte[(int)size];
     				try{
     				instream.read(arr);
     				ostream.write(arr);
     				instream.close();
     				ostream.close();
     				}
     				catch(Exception e){System.err.println(e.toString());}
     				break;
     			}
     		}
     		
     		return 1; //If writing into dest_path was successful.
     	}
     	
     	//Else if there are multiple files with same time stamps, we need to compare the file hashes. If all the files hashes
     	//are equal, then we just write a random file out of the lot into dest_path. Else, we need to have a MAJORITY of the
     	//files whose hashes are equal. If we don't have a majority, we return an error code.
     	try{
     	for(i=0; i<server_count; i++)
    		if(grTimestamps[i] == 1) grTimestampsarry[i] = files[i].toString();//filehash.computeHash(files[i].getfile());
     		else grTimestampsarry[i] = "-1";
     	}
     	catch(Exception e){return -1;}
     	//Now we have an array that indicates which files have the largest (same) timestamps. All we need to do now is
     	//to compare the files with the greatest timestamps and choose a random file out of the correct ones.
 
     	//Compare hashes and update similarity index for each file.
     	for(i=0; i<server_count; i++){
     		//System.out.println("grTimestamps["+i+"]="+grTimestamps[i]);
     		if(grTimestamps[i] == 1)
     		for(int j=0; j<server_count; j++)
     		{
     			//System.out.println("grTimestamps["+j+"]="+ grTimestamps[j]+ "grTimestampsarry["+i+"]=" + grTimestampsarry[i] + "grTimestampsarry["+j+"]=" + grTimestampsarry[j]);
     			if(grTimestamps[j] == 1  && grTimestampsarry[i].equals(grTimestampsarry[j])) files[i].similarityIndex++;
     		}
     	}
 
     	for(i=0; i<server_count; i++)
     		if(grTimestamps[i] == 1 && files[i].similarityIndex > maxSimilarity) maxSimilarity = files[i].similarityIndex;
 
     	for(i=0; i<server_count; i++){System.out.println("MAX SIMILARITY ="+maxSimilarity+"; files["+i+"] similarity = "+files[i].similarityIndex);}
     	
     	int majority = (int)Math.floor(server_count/2);
     	int num_similar_responses=0;
     	for(i=0; i<server_count; i++){
     		if(grTimestamps[i] == 1 && files[i].similarityIndex == maxSimilarity) num_similar_responses++;
     	}
     	
     	if(qflag && num_similar_responses < majority + 1 || maxSimilarity == 1){System.err.println("No consensus! Not writing output file."); return -1;}
 		if(!qflag && num_similar_responses < majority || maxSimilarity == 1){System.err.println("No consensus! Not writing output file."); return -1;}
     		
     	int num_bytes=0;
     	for(i=0; i<server_count; i++){
     		if(files[i].similarityIndex == maxSimilarity)/*Write files[i]->fileptr to dest path. */
     		{
     			File ftemp;
 				FileOutputStream ostream = null; 
 				File infile = files[i].getfile();
 				FileInputStream instream = null;
 				
 				try{
 				instream = new FileInputStream(infile);	
 				ftemp = new File(dest_path); 
 				ostream = new FileOutputStream(ftemp);
 				}
 				catch(Exception e){System.err.println(e.toString());}
 				
 				long size = infile.length();
 				byte [] arr = new byte[(int)size];
 				try{
 				instream.read(arr);
 				ostream.write(arr);
 				instream.close();
 				ostream.close();
 				}
 				catch(Exception e){System.err.println(e.toString());}
 				break;
     		}
     	}
      	
     	
 		return 1;
 	}
     
     
     public static void main(String [] args){
     	File f1 = new File("C:/Users/Vinay Bharadwaj/workspace/quorum/src/1.txt.txt");
     	File f2 = new File("C:/Users/Vinay Bharadwaj/workspace/quorum/src/2.txt.txt");
     	File f3 = new File("C:/Users/Vinay Bharadwaj/workspace/quorum/src/3.txt.txt");
     	File f4 = new File("C:/Users/Vinay Bharadwaj/workspace/quorum/src/4.txt.txt");
     	File f5 = new File("C:/Users/Vinay Bharadwaj/workspace/quorum/src/5.txt.txt");
     	QFILE [] files = new QFILE[4];
     	files[0]= new QFILE(f1, 1 ,1);
     	files[1]= new QFILE(f2, 1, 2);
     	files[2]= new QFILE(f3, 1, 3);
     	files[3] = new QFILE(f4, 1, 4);
     	//files[4] = new QFILE(f5, 1, 5);
     	
     	//Example quoruming files using non-static function.
     	//quorum q1 = new quorum(files, 5, "C:/Users/Vinay Bharadwaj/workspace/quorum/src/output.txt");
     	
     	//Example quoruming files using static function.
     	quorum.quorum_files_static(files,4, "C:/Users/Vinay Bharadwaj/workspace/quorum/src/output.txt");
     	
     	//Eaxmple quoruming opcodes
     	long [] opcodes = {4,3,4,5,4,4};
     	long result = quorum.quorum_opcodes_static(opcodes, 6);
     	System.out.println("Opcode quorum result is:"+ result);
     	
     }
 }
 
 class quorum_server{
 	private int num_servers;
 	private int quorum_server_start(){return 1;}
     private	int quorum_server_stop(){return 1;}
 
     public quorum_server(int num_servers){ 
     	this.num_servers = num_servers;
     	quorum_server_start();
     }
     public void quorum_server_s(){quorum_server_stop();}
 }
