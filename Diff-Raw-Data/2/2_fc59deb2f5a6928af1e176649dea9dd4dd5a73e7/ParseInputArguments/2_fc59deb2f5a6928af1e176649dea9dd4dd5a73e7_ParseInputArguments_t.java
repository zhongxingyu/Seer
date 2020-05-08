 package tugb.mosnuic;
 
 import java.io.File;
 
 public class ParseInputArguments {
 	private static String targetImageFilePath;
 	private static String tileDirectory;
 	private static String outputImageFilePath;
 	private static int maxNumberOfTiles = 10000;
 	private static String targetImageFileFormat;
 	private static String outputImageFileFormat;
 	/*private static String targetImageFileParent;*/
 
 	static boolean TargetImagePathFound = false;
 	static int OutputImagePathFound = 0;
 	static boolean TileDirectoryPathFound = false;
 	static boolean OutputImagePathToBeConstructed = false;
 
 	static String ERROR = "Please enter input in the following format\nfile1 dir2 [-o file3] [-r n4]";
 	static String OUTPUT_SUFFIX = "_out";
 
 	public boolean ParsePath(String args[]){
 
 		/*Check if argument count is less than 2 or a multiple of 2*/
 		checkNumberOfArguments(args);
 
 		/*Check for any repeated occurrences of flags*/
 		checkRepeatedFlags(args);
 
 		/* Main method to parse the Input arguments and check them */
 		boolean finalCheck = checkArgs(args);
 
 		/* Method to check if mandatory inputs are present */
 		checkMandatoryInputs();
 
 		/* Method to find the file format of the output image */
 		findOutputImageFileFormat();
 
 		/* Method to construct the file path for the output image */
 		constructOutputImagePath();
 
 		return finalCheck;
 	}
 
 	private void constructOutputImagePath() {
 		/*Construct O/P filename if -o is absent */
 		if(OutputImagePathFound==0){
 			outputImageFilePath = new String(targetImageFilePath.substring(0, targetImageFilePath.lastIndexOf('.')) + 
 					OUTPUT_SUFFIX + "."+targetImageFileFormat);
 		}
 		if(OutputImagePathFound==1){
 			if(outputImageFilePath.lastIndexOf('.') == -1)
 				outputImageFilePath = new String(targetImageFilePath.substring(0,
 						targetImageFilePath.lastIndexOf(File.separatorChar)) +File.separatorChar +
 						outputImageFilePath + "."+outputImageFileFormat);
 			else{
 				outputImageFilePath = new String(targetImageFilePath.substring(0,
 						targetImageFilePath.lastIndexOf(File.separatorChar)) +File.separatorChar + 
 						outputImageFilePath);
 			}								
 		}
 		if(OutputImagePathFound==2){
 			if(outputImageFilePath.lastIndexOf('.') == -1)
 				outputImageFilePath = new String(outputImageFilePath + outputImageFileFormat);				
 		}		
 	}
 
 	private void findOutputImageFileFormat() {
 		outputImageFileFormat = targetImageFileFormat;		
 		if(OutputImagePathFound != 0){
 			if(outputImageFilePath.lastIndexOf('.') != -1){
 				int pos = outputImageFilePath.lastIndexOf('.');
 				outputImageFileFormat = outputImageFilePath.substring(pos+1);
 			}				
 		}		
 	}
 
 	/*Method simply checks if the TargetImagePath and TileLibraryDirectory have been specified in the inputs*/
 	private void checkMandatoryInputs() {
 		if(TargetImagePathFound == false || TileDirectoryPathFound == false){
 			System.out.println(ERROR);
 			System.out.println("Mandatory inputs not found");
 			System.exit(0);		
 		}
 	}
 
 	/*Function to check if any flags are repeated or not*/
 	private void checkRepeatedFlags(String[] args) {
 		int count_o = 0;
 		int count_r = 0;
 
 		/*Calculate count of each flags available*/
 		for(int i=0;i<args.length;i++){
 			if(args[i].equals("-o"))
 				count_o++;
 			if(args[i].equals("-r"))
 				count_r++;
 		}
 
 		/*If count > 1 throw error and terminate*/
 		if(count_o > 1 || count_r > 1){
 			System.out.println("ERROR");
 			System.out.println("Flags repeated");
 			System.exit(0);		
 		}
 	}
 
 	/*Function to check number of arguments. It should be atleast 2 and a multiple of 2*/
 	private void checkNumberOfArguments(String[] args) {
 		int arg_count = args.length;
 
 		if(arg_count < 2 || (arg_count%2!=0)){
 			System.out.println(ERROR);
 			System.out.println("Invalid number of arguments");
 			System.exit(0);		
 		}
 	}
 
 	public enum INPUT_TYPE {
 		FLAG,
 		FILE,
 		DIR,
 		ERROR		
 	}
 
 	/*Main function that parses all the inputs*/
 	public static boolean checkArgs(String[] args){
 
 		/*Initialize variables*/
 		int i;
 		boolean result = true;
 
 		/*Determines input type which is either a FLAG, FILE, DIR. If neither then throw error and terminate*/
 		for(i=0;i<args.length;i++){
 			String inputType = checkInputType(args[i]);
 			INPUT_TYPE ip = INPUT_TYPE.valueOf(inputType);
 
 			switch(ip){
 			case FLAG:
 				try {
 					if(!checkFlagArgs(args[i], args[++i])){
 						System.out.println(ERROR);
 						System.out.println("Please check flag arguments");
 						System.exit(0);
 						result = false;
 					}
 				} catch (Exception e) {					
 					System.out.println(ERROR);
 					System.out.println("Please check flag arguments");
 					System.exit(0);
 					result = false;
 				}
 				break;
 			case FILE:
 				if(TargetImagePathFound == false){
 					TargetImagePathFound = true;
 					if(args[i].lastIndexOf(File.separatorChar) == -1){
 						File temp = new File(args[i]);
 						targetImageFilePath = new String(temp.getAbsolutePath());
 						System.out.println(targetImageFilePath);
 					}
 					else
 						targetImageFilePath = args[i];
 					int posTarget = args[i].lastIndexOf('.');
 					targetImageFileFormat = args[i].substring(posTarget+1);
 				}
 				else
 				{
 					//Throw error
 					System.out.println(ERROR);
 					System.out.println("Redundant file in input");
 					System.exit(0);
 					result = false;
 				}
 				break;
 			case DIR:
 				if(TileDirectoryPathFound == false){
 					TileDirectoryPathFound = true;
 					if(args[i].lastIndexOf(File.separatorChar) == -1){
 						File temp = new File(args[i]);
 						tileDirectory = new String(temp.getAbsolutePath());
 						//System.out.println(tileDirectory);
 					}
 					else
 						tileDirectory = args[i];
 				}
 				else
 				{
 					//Throw error
 					System.out.println(ERROR);
 					System.out.println("Redundant dir in input");
 					System.exit(0);
 					result = false;
 				}
 				break;
 			default:
 				result = false;
 				System.out.println(ERROR);
				System.out.println("Invalid inputs provided.");
				System.exit(0);
 			}
 		}
 		return result;
 	}
 
 	/*If input is a flag, this function checks the argument following the flag*/
 	private static boolean checkFlagArgs(String flag, String value) {
 		boolean returnType = false;		
 		if(flag.equals("-o"))
 			returnType = checkFlag_O(value);
 		if(flag.equals("-r"))
 			returnType = checkFlag_R(value);		
 		return returnType;		
 	}
 
 	/*Function to check in case of -r */
 	private static boolean checkFlag_R(String value) {
 		try {
 			int repTiles = Integer.parseInt(value);
 			maxNumberOfTiles = repTiles;
 			return (repTiles > 0);
 		} catch (NumberFormatException e) {
 			System.out.println(ERROR);
 			System.out.println("Integer n4 in [-r n4] not parseable");
 			System.exit(0);
 			return false;
 		}
 	}
 
 	/*Function to check in case of -o */
 	public static boolean checkFlag_O(String args3){
 		String targetPath = new String(args3);
 		int lastIndexSlash = targetPath.lastIndexOf(File.separatorChar);
 		if(checkFileTypes(args3)){
 			if(lastIndexSlash==-1){
 				OutputImagePathFound = 1;
 				outputImageFilePath = args3;
 				return true;
 			}
 			else{
 				File cliarg3 = new File(args3); 
 				String FinalImageParent = cliarg3.getParent();
 				File cliFinalImageParent = new File(FinalImageParent);
 				if(cliFinalImageParent.exists()){
 					OutputImagePathFound = 2;
 					outputImageFilePath = args3;
 					return true;
 				}
 			}
 		}
 		else if(args3.lastIndexOf('.')==-1){
 			OutputImagePathFound = 1;
 			outputImageFilePath = args3;
 			return true;			
 		}
 		return false;
 	}
 
 	/*Determines input type which is either a FLAG, FILE, DIR. If neither then default is ERROR*/
 	private static String checkInputType(String arg) {
 		String inputType = new String("ERROR");
 		//Check if arg is file
 		if(arg.contains("-") && arg.length() == 2)
 			inputType = "FLAG";
 		if(checkTargetImagePath(arg))
 			inputType = "FILE";
 		if(checkTileDirectory(arg))
 			inputType = "DIR";
 		//System.out.println(arg+"\t"+inputType);
 		return inputType;
 	}
 
 	/*Checking for TileDirectory. Should exist, be a directory and not be empty*/
 	private static boolean checkTileDirectory(String path) {
 		boolean isNotEmpty = false;
 		File cliLibImages = new File(path);
 		boolean chkExists = cliLibImages.exists();
 		boolean chkDir = cliLibImages.isDirectory();
 		if(chkDir){
 			isNotEmpty = (cliLibImages.list().length > 0);
 			if(!isNotEmpty){
 				System.out.println(ERROR);
 				System.out.println("Tile Directory empty");
 				System.exit(0);
 			}
 		}
 		return (chkExists && chkDir && isNotEmpty);
 	}
 
 	/*Checking for files. Should be a file, exist and have a supported format*/
 	private static boolean checkTargetImagePath(String path) {
 		File cliTargetImage = new File(path);
 		boolean chkExists = cliTargetImage.exists();
 		boolean chkFile = cliTargetImage.isFile();
 		boolean chkFileTypes = checkFileTypes(path);
 		return (chkExists && chkFile && chkFileTypes);
 	}
 
 	/*Check for formats of file*/
 	private static boolean checkFileTypes(String path) {
 		boolean chkBMP = path.endsWith(".bmp"); 
 		boolean chkGIF = path.endsWith(".gif"); 
 		boolean chkJPEG = path.endsWith(".jpeg"); 
 		boolean chkJPG = path.endsWith(".jpg");
 		boolean chkPNG = path.endsWith(".png"); 
 		boolean chkTIFF = path.endsWith(".tiff");
 		boolean chkTIF = path.endsWith(".tif");		
 		return (chkBMP || chkGIF || chkJPEG || chkJPG || chkPNG || chkTIFF || chkTIF);
 	}
 
 	/*Getter functions for all private members*/
 	public String getTargetImageFilePath()
 	{
 		//System.out.println("TargetImageFilePath\t "+targetImageFilePath);
 		return targetImageFilePath;
 	}
 
 	public String getTileDirectory()
 	{
 		//System.out.println("TileDirectory\t"+tileDirectory);
 		return tileDirectory;
 	}
 
 	public int getMaxNumberOfTiles()
 	{
 		//System.out.println("MaxNumberOfTilesRepeated\t"+maxNumberOfTiles);
 		return maxNumberOfTiles;
 	}
 
 	public String getTargetImageFileFormat(){
 		//System.out.println("TargetImageFileFormat\t"+targetImageFileFormat);
 		return targetImageFileFormat;
 	}
 
 	public String getOutputImageFileFormat(){
 		//System.out.println("OutputImageFileFormat\t"+outputImageFileFormat);
 		return outputImageFileFormat;
 	}
 
 	public String getOutputImageFilePath(){
 		//System.out.println("OutputImageFilePath\t"+outputImageFilePath);
 		return outputImageFilePath;
 	}	
 }
 
