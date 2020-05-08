 package variousTools;
 
 import java.util.Hashtable;
 
 import general.ExtendedWriter;
 import general.Functions;
 import general.IOTools;
 
 public class SBATCHinfo {
 
 
 	private String projectNumber;
 	private String email;
 	private String[] module;
 	
 	
 
 
 	public SBATCHinfo(){}
 
 	public boolean run(Hashtable<String,String> T){
 		boolean allInfo = true;
 		projectNumber= Functions.getValue(T, "-pNr", "b2010035");
 		email = Functions.getValue(T, "-email", "johan.reimegard@scilifelab.se");
 		module =null;
 		if(T.containsKey("-modules")){
 			String modules = Functions.getValue(T, "-modules");
 			System.out.println("modules found");
 			if(modules.indexOf(" ")>-1)
 				module = modules.split(" ");
 			else{
 				module = new String[1];
 				module[0]=modules;
 			}
 		}
 		
 		if(T.containsKey("-fastQC")){
 			FastQCSBATCH sbatch = new FastQCSBATCH();
 			sbatch.run(T);
 		}
 		if(T.containsKey("-filter")){
 			FilterFastqSBATCH sbatch = new FilterFastqSBATCH();
 			sbatch.run(T);
 		}
 
 		if(T.containsKey("-gzip")){
 			gunzipSBATCH sbatch = new gunzipSBATCH();
 			sbatch.run(T);
 		}
 
 		if(T.containsKey("-cutAdapt")){
 			WriteTocutAdaptoSBATCH sbatch = new WriteTocutAdaptoSBATCH();
 			sbatch.run(T);
 		}
 
 		if(T.containsKey("-shrimp")){
 			ShrimpSBATCH sbatch = new ShrimpSBATCH();
 			sbatch.run(T);
 		}
 
 		if(T.containsKey("-tophat")){
 			TopHatSBATCH sbatch = new TopHatSBATCH();
 			sbatch.run(T);
 		}
 
 		if(T.containsKey("-cufflinks")){
 			CufflinksSBATCH sbatch = new CufflinksSBATCH();
 			sbatch.run(T);
 		}
 
 		if(T.containsKey("-samtools")){
 			SamtoolsSBATCH sbatch = new SamtoolsSBATCH();
 			sbatch.run(T);
 		}
 
 
 		if(T.containsKey("-bowtie2")){
 			bowtie2SBATCH sbatch = new bowtie2SBATCH();
 			sbatch.run(T);
 		}
 
 		if(T.containsKey("-SeqPrep")){
 			SeqPrep sbatch = new SeqPrep();
 			sbatch.run(T);
 		}
 		if(T.containsKey("-trinity")){
 			trinity sbatch = new trinity();
 			sbatch.run(T);
 		}
 		if(T.containsKey("-DigiNorm")){
 			DigiNorm sbatch = new DigiNorm();
 			sbatch.run(T);
 		}
 
 		if(T.containsKey("-script")){
 			Script script = new Script();
 			script.run(T);
 
 		}
 
 		return allInfo;
 
 
 
 	}
 
 
 
 
 
 	public boolean addSBATCHinfo(Hashtable<String,String> T ){
 		boolean allInfo = true;
 		projectNumber= Functions.getValue(T, "-pNr", "b2010035");
 		System.out.println("must contain projectNumber -pNr Default:"+ projectNumber);
 		email = Functions.getValue(T, "-email", "johan.reimegard@scilifelab.se");
 		System.out.println("must contain -email  Default:"+ email);
 		
 		module =null;
 		if(T.containsKey("-modules")){
 			String modules = Functions.getValue(T, "-modules");
 			System.out.println("modules found");
 			if(modules.indexOf(" ")>-1)
 				module = modules.split(" ");
 			else{
 				module = new String[1];
 				module[0]=modules;
 			}
 		}
 		
 
 		return allInfo;
 	}
 
 
 
 
 
 
 	public void printSBATCHinfo(ExtendedWriter EW, String directory,String timestamp, int ID, String program, String time){
		if(IOTools.isDir(directory+"/reports")){
 			IOTools.mkDir(directory+"/reports");
 		}
 
 		if(program.indexOf("/")>-1){
 			program = program.substring(program.indexOf("/"));
 		}
 		String jobName = ID+"_"+program+"_"+timestamp;
 
 		EW.println("#! /bin/bash -l");
 		EW.println ("#SBATCH -A "+projectNumber);
 		EW.println("#SBATCH -p node -n 8 ");
 		EW.println("#SBATCH -C thin");
 		EW.println("#SBATCH -t "+time);
 		EW.println("#SBATCH -J "+jobName);
 		EW.println("#SBATCH -e "+directory+"/reports/"+jobName+".stderr.txt");
 		EW.println("#SBATCH -o "+directory+"/reports/"+jobName+".stdout.txt");
 
 		if(email != null){
 			EW.println("#SBATCH --mail-type=All");
 			EW.println("#SBATCH --mail-user="+email);
 		}
 		
 		if(module != null){
 			EW.println();
 			EW.println();
 			EW.println("module load bioinfo-tools");
 			for(int i = 0; i < module.length;i++){
 				EW.println("module load "+module[i]);
 			}
 		}
 
 
 		EW.println();
 		EW.println();
 	}
 
 	public void printSBATCHinfo72GB(ExtendedWriter EW, String directory,String timestamp, int ID, String program, String time){
		if(IOTools.isDir(directory+"/reports")){
 			IOTools.mkDir(directory+"/reports");
 		}
 
 		String jobName = ID+"_"+program+"_"+timestamp;
 
 		EW.println("#! /bin/bash -l");
 		EW.println ("#SBATCH -A "+projectNumber);
 		EW.println("#SBATCH -p node");
 		EW.println("#SBATCH -C mem72GB");
 		EW.println("#SBATCH -t "+time);
 		EW.println("#SBATCH -J "+jobName);
 		EW.println("#SBATCH -e "+directory+"/reports/"+jobName+".stderr.txt");
 		EW.println("#SBATCH -o "+directory+"/reports/"+jobName+".stdout.txt");
 
 		if(email != null){
 			EW.println("#SBATCH --mail-type=All");
 			EW.println("#SBATCH --mail-user="+email);
 		}
 
 		if(module != null){
 			EW.println();
 			EW.println();
 			EW.println("module load bioinfo-tools");
 			for(int i = 0; i < module.length;i++){
 				EW.println("module load "+module[i]);
 			}
 		}
 
 		EW.println();
 		EW.println();
 	}
 
 
 
 	public void printSBATCHinfoCore(ExtendedWriter EW, String directory,String timestamp, int ID, String program, String time){
 
 		
		if(IOTools.isDir(directory+"/reports")){
 			IOTools.mkDir(directory+"/reports");
 		}
 		
 		String jobName = ID+"_"+program+"_"+timestamp;
 
 		EW.println("#! /bin/bash -l");
 		EW.println ("#SBATCH -A "+projectNumber);
 		EW.println("#SBATCH -p core");
 		EW.println("#SBATCH -t "+time);
 		EW.println("#SBATCH -J "+jobName);
 		EW.println("#SBATCH -e "+directory+"/reports/"+jobName+".stderr.txt");
 		EW.println("#SBATCH -o "+directory+"/reports/"+jobName+".stdout.txt");
 
 		if(email != null){
 			EW.println("#SBATCH --mail-type=All");
 			EW.println("#SBATCH --mail-user="+email);
 		}
 		if(module != null){
 			EW.println();
 			EW.println();
 			EW.println("module load bioinfo-tools");
 			for(int i = 0; i < module.length;i++){
 				EW.println("module load "+module[i]);
 			}
 		}
 
 
 		EW.println();
 		EW.println();
 	}
 
 	public void printSBATCHinfoFat(ExtendedWriter EW, String directory,String timestamp, int ID, String program, String time){
 
		if(IOTools.isDir(directory+"/reports")){
 			IOTools.mkDir(directory+"/reports");
 		}
 		String jobName = ID+"_"+program+"_"+timestamp;
 
 		EW.println("#! /bin/bash -l");
 		EW.println ("#SBATCH -A "+projectNumber);
 		EW.println("#SBATCH -p node");
 		EW.println("#SBATCH -C fat");
 		EW.println("#SBATCH -t "+time);
 		EW.println("#SBATCH -J "+jobName);
 		EW.println("#SBATCH -e "+directory+"/reports/"+jobName+".stderr.txt");
 		EW.println("#SBATCH -o "+directory+"/reports/"+jobName+".stdout.txt");
 
 		if(email != null){
 			EW.println("#SBATCH --mail-type=All");
 			EW.println("#SBATCH --mail-user="+email);
 		}
 		if(module != null){
 			EW.println();
 			EW.println();
 			EW.println("module load bioinfo-tools");
 			for(int i = 0; i < module.length;i++){
 				EW.println("module load "+module[i]);
 			}
 		}
 
 
 		EW.println();
 		EW.println();
 	}
 
 
 	public void printSBATCHinfohalvan(ExtendedWriter EW, String directory,String timestamp, int ID, String program, String time, int MB){
		if(IOTools.isDir(directory+"/reports")){
 			IOTools.mkDir(directory+"/reports");
 		}
 
 		
 		int nrofnodes = MB/32 + 1;
 		if(nrofnodes < 4){nrofnodes = 4; System.out.println("Memory allocated will be "+4*32+" GB");}
 		else if(nrofnodes < 8){nrofnodes = 8; System.out.println("Memory allocated will be "+8*32+" GB");}
 		else if(nrofnodes < 16){nrofnodes = 16; System.out.println("Memory allocated will be "+8*32+" GB");}
 		else if(nrofnodes < 24){nrofnodes = 24; System.out.println("Memory allocated will be "+8*32+" GB");}
 		else if(nrofnodes < 32){nrofnodes = 32; System.out.println("Memory allocated will be "+8*32+" GB");}
 		else if(nrofnodes < 40){nrofnodes = 40; System.out.println("Memory allocated will be "+8*32+" GB");}
 		else if(nrofnodes < 48){nrofnodes = 48; System.out.println("Memory allocated will be "+8*32+" GB");}
 		else if(nrofnodes < 56){nrofnodes = 56; System.out.println("Memory allocated will be "+8*32+" GB");}
 		else nrofnodes = 64;
 
 		String jobName = ID+"_"+program+"_"+timestamp;
 
 		EW.println("#! /bin/bash -l");
 		EW.println ("#SBATCH -A "+projectNumber);
 		EW.println("#SBATCH -M halvan");
 		EW.println("#SBATCH -p halvan");
 		EW.println("#SBATCH -n "+nrofnodes);
 		EW.println("#SBATCH -t "+time);
 		EW.println("#SBATCH -J "+jobName);
 		EW.println("#SBATCH -e "+directory+"/reports/"+jobName+".stderr.txt");
 		EW.println("#SBATCH -o "+directory+"/reports/"+jobName+".stdout.txt");
 
 		if(email != null){
 			EW.println("#SBATCH --mail-type=All");
 			EW.println("#SBATCH --mail-user="+email);
 		}
 
 		if(module != null){
 			EW.println();
 			EW.println();
 			EW.println("module load bioinfo-tools");
 			for(int i = 0; i < module.length;i++){
 				EW.println("module load "+module[i]);
 			}
 		}
 
 		EW.println();
 		EW.println();
 	}
 
 
 
 }
