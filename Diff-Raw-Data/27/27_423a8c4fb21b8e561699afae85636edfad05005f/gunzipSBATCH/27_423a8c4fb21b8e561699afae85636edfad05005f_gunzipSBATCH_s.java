 package variousTools;
 
 import general.ExtendedReader;
 import general.ExtendedWriter;
 import general.Functions;
 import general.IOTools;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Hashtable;
 
 
 
 public class gunzipSBATCH {
 
 	String inDir;
 	String projectDir;
 	String suffix;
 	String time;
 	boolean all;
 
 	public gunzipSBATCH(){
 		inDir = projectDir= suffix = time = null;
 		this.all= false;
 	}
 
 	public static void main(String []args){
 
 		int length = args.length;
 		for (int i = 0; i < length; i++){
 			args[i] = args[i].trim();
 			System.out.print(args[i]+" ");
 		}
 		System.out.println();
 		Hashtable<String,String> T = Functions.parseCommandLine(args);
 		FilterFastqSBATCH filter = new FilterFastqSBATCH();
 		filter.run(T);
 	}
 
 	public void run(Hashtable<String,String> T){
 
 		boolean allPresent = true;
 		String timeStamp = Functions.getDateTime();
 		SBATCHinfo sbatch = new SBATCHinfo();
 		if(!sbatch.addSBATCHinfo(T)) allPresent = false;
 
 		if(T.containsKey("-i"))
 			inDir= Functions.getValue(T, "-i");
 		else{
 			System.out.println("must contain inDirectory -i");
 			allPresent = false;
 		}
 		projectDir= Functions.getValue(T, "-d", IOTools.getCurrentPath());
 
 		if(T.containsKey("-time"))
 			time = Functions.getValue(T, "-time", "1:00:00");
 		else{
 			System.out.println("time is not set. Now set to default 01:00:00");
 			time = Functions.getValue(T, "-time", "1:00:00");
 		}
 		suffix = Functions.getValue(T, "-suffix", ".fastq");
 		if(!T.containsKey("-suffix")){
 			System.out.println("no suffix set compress all files");
 			this.all = true;
 		}
 		boolean compress = true;
 		if(suffix.compareTo("gz") == 0){
 			compress = false;
 		}
 		if(allPresent)
 			gunzipTop(sbatch, timeStamp,suffix,compress);
 		else
 			System.out.println("\n\nAborting run because of missing arguments for gunzip.");
 	}
 
 
 	public void gunzipTop(SBATCHinfo sbatch, String timeStamp,String suffix, boolean compress){
 		try{
 			if(!IOTools.isDir(projectDir+"/scripts"))
 				IOTools.mkDir(projectDir+"/scripts");
 			ExtendedWriter EW = new ExtendedWriter(new FileWriter(projectDir+"/scripts/"+timeStamp+"_gunzip.sh"));
 			if(!all)
 				gunzipDir(EW,sbatch,projectDir+"/"+inDir, timeStamp,suffix,0,compress);
 			else
 				gunzipDirAll(EW,sbatch,projectDir+"/"+inDir, timeStamp,0,compress);
 			EW.flush();
 			EW.close();
 		}catch(Exception E){E.printStackTrace();}
 	} 
 
 
 
 
 	public void gunzipDir(ExtendedWriter generalSbatchScript, SBATCHinfo sbatch , String finalInDir ,String timestamp,String suffix, int count , boolean compress){
 
 
 		ArrayList <String> fileNames = IOTools.getSequenceFiles(finalInDir,suffix);
 
 
 		if(fileNames.isEmpty()){
 			System.out.println("No "+suffix+" files in folder :"+finalInDir);
 		}
 		else{
 			if(!IOTools.isDir(finalInDir+"/reports"))
 				IOTools.mkDir(finalInDir+"/reports");
 			if(!IOTools.isDir(finalInDir+"/scripts"))
 				IOTools.mkDir(finalInDir+"/scripts");
 
 			try{
 				String[] dirs = finalInDir.split("/");
 				String lastDir = dirs[dirs.length-1];
 
 				String 	sbatchFileName = finalInDir+"/scripts/"+timestamp+"_"+count+"_"+lastDir+".gunzip.sbatch";
 
 				generalSbatchScript.println("sbatch "+ sbatchFileName);
 
 				ExtendedWriter EW = new ExtendedWriter(new FileWriter(sbatchFileName));
 				sbatch.printSBATCHinfo(EW,finalInDir,timestamp,count,"gzip"+"_"+lastDir, time);
 
 				EW.println("cd "+finalInDir);
 				EW.println();
 				EW.println();
 
 				for(int i = 0; i < fileNames.size(); i++){	
 					if(compress)
 						EW.println("gzip  "+finalInDir+"/"+fileNames.get(i)+" &");
 					else
 						EW.println("gzip -d   "+finalInDir+"/"+fileNames.get(i)+" &");
 					if((i+1)%8 == 0  &&  fileNames.size()-i != 1){
 
 						EW.println();
 						EW.println();
 						EW.println("wait");
 						EW.flush();
 						EW.close();
 						count++;
 
 						sbatchFileName = finalInDir+"/scripts/"+timestamp+"_"+count+"_"+lastDir+".gunzip.sbatch";
 						generalSbatchScript.println("sbatch "+ sbatchFileName);
 						EW = new ExtendedWriter(new FileWriter(sbatchFileName));
 						sbatch.printSBATCHinfo(EW,finalInDir,timestamp,count,"gzip"+"_"+lastDir, time);
 						EW.println("cd "+finalInDir);
 						EW.println();
 						EW.println();
 
 
 					}
 				}
 				EW.println();
 				EW.println();
 				EW.println("wait");
 				EW.flush();
 				EW.close();
 
 
 
 			}catch(Exception E){E.printStackTrace();}
 
 			count++;
 		}
 		ArrayList <String> subDirs = IOTools.getDirectories(finalInDir);
 		for(int i = 0; i < subDirs.size(); i++){
 			gunzipDir(generalSbatchScript,sbatch,finalInDir+"/"+subDirs.get(i), timestamp,suffix,count, compress);
 		}
 
 
 	}
 
 
 	public int gunzipDirAll(ExtendedWriter generalSbatchScript, SBATCHinfo sbatch , String finalInDir ,String timestamp, int count , boolean compress){
 
 		String[] dirs = finalInDir.split("/");
 		String lastDir = dirs[dirs.length-1];
 
 		ArrayList <String> fileNames = IOTools.getSequenceFiles(finalInDir);
 
 		if(fileNames.isEmpty()){
 			try{
 				for(int i = 0; i < fileNames.size(); i++){
 					if(compress){
 						if(!fileNames.get(i).endsWith(".gz")){
 							String 	sbatchFileName = projectDir+"/scripts/"+timestamp+"_"+count+"_"+lastDir+".gunzip.sbatch";
 							ExtendedWriter EW = new ExtendedWriter(new FileWriter(sbatchFileName));
 							sbatch.printSBATCHinfoCore(EW,finalInDir,timestamp,count,"gzip"+"_"+lastDir, time);
 							generalSbatchScript.println("sbatch "+ sbatchFileName);
 							EW.println("gzip  "+finalInDir+"/"+fileNames.get(i));
 							EW.flush();
 							EW.close();
 							count++;
 						}
 
 					}
 					else
 						if(fileNames.get(i).endsWith(".gz")){
 							String 	sbatchFileName = projectDir+"/scripts/"+timestamp+"_"+count+"_"+lastDir+".gunzip.sbatch";
 							ExtendedWriter EW = new ExtendedWriter(new FileWriter(sbatchFileName));
 							sbatch.printSBATCHinfoCore(EW,finalInDir,timestamp,count,"gzip"+"_"+lastDir, time);
 							generalSbatchScript.println("sbatch "+ sbatchFileName);
 							EW.println("gzip -d "+finalInDir+"/"+fileNames.get(i));
 							EW.flush();
 							EW.close();
 							count++;
 						}
 				}
 			}catch(Exception E){E.printStackTrace();}
 		}
		
 		ArrayList <String> subDirs = IOTools.getDirectories(finalInDir);
 		for(int i = 0; i < subDirs.size(); i++){
 			try{
 				if(compress){
 					String 	sbatchFileName = projectDir+"/scripts/"+timestamp+"_"+count+"_"+lastDir+".gunzip.sbatch";
 					ExtendedWriter EW = new ExtendedWriter(new FileWriter(sbatchFileName));
 					sbatch.printSBATCHinfoCore(EW,finalInDir,timestamp,count,"gzip"+"_"+lastDir, time);
 					generalSbatchScript.println("sbatch "+ sbatchFileName);
 					EW.println("gzip -r "+finalInDir+"/"+subDirs.get(i));
 					EW.flush();
 					EW.close();
 					count++;
 				}
				else
					if(fileNames.get(i).endsWith(".gz")){
						String 	sbatchFileName = projectDir+"/scripts/"+timestamp+"_"+count+"_"+lastDir+".gunzip.sbatch";
						ExtendedWriter EW = new ExtendedWriter(new FileWriter(sbatchFileName));
						sbatch.printSBATCHinfoCore(EW,finalInDir,timestamp,count,"gzip"+"_"+lastDir, time);
						generalSbatchScript.println("sbatch "+ sbatchFileName);
						EW.println("gzip -d -r "+finalInDir+"/"+subDirs.get(i));
						EW.flush();
						EW.close();
						count++;
					}
 
 			}catch(Exception E){E.printStackTrace();}
 		}
 
 		return count;
 
 	}
 
 
 
 }
