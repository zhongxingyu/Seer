 import java.lang.reflect.Constructor;
 import java.io.File;
 import org.apache.commons.io.FilenameUtils;
 
 public class TestingUtil {
 	
 	private boolean canVerify;
 	private File expectedOutputDirectory;
 	
 	
 	private String website;
 	private String fileName;
 	
 	
 	private boolean isolatePhases;
 	
 	
 	private String tcDirName = "./currentTC";
 	
 	
 	public TestingUtil(String expectedDir, String website, String fileName, boolean isolate){
 		
 		File dir = new File(expectedDir);
 		
 		canVerify = dir.isDirectory();
 		
 		expectedOutputDirectory = dir;
 		
 		this.website = website;
 		this.fileName = fileName;
 		isolatePhases = isolate;
 		
 	}
 	
 	
 	
 	
 
 	private boolean hasRunWebRipper = false;
 	
 	public void runWebRipper(int width, int depth){
 		
 		String widthStr = width < 1 ? "1" : Integer.toString(width);
 		String depthStr = depth < 1 ? "1" : Integer.toString(depth);
 		
		String[] args = {"WebPluginInfo","--website-url", website, "-w", widthStr, "-d", depthStr, "-g", fileName+".GUI", "-l",fileName +"_ph1.log" };
 		
 
 		edu.umd.cs.guitar.ripper.Launcher.main(args);
 		
 		hasRunWebRipper = true;
 		
 	}
 	
 	
 	public boolean verifyWebRipper(){
 		
 		if(!canVerify){
 			System.err.println("ERROR: Cannot verify WebRipper. No expected output!");
 			return false;
 		}
 			
 		
 		if(hasRunWebRipper){
 			File expected = getExpectedFile(fileName, "GUI");
 			File current = new File(fileName+".GUI");
 
 			//use https://code.google.com/p/java-diff-utils/ for diff API
 			
 			
 			//inspect differences and ignore minor things such as different file names (if that comes up)
 			
 		}
 		
 		System.err.println("ERROR: WebRipper has not been run!");
 		return false;
 		
 	}
 	
 	
 	public void runGuiToEfg(){
 		
 		
 		String inputFile = isolatePhases ? FilenameUtils.removeExtension(getExpectedFile(fileName, "GUI").getAbsolutePath()) : fileName;
 		
		String[] args= {"-p", "EFGConverter", "-g", fileName+".GUI", "-e", inputFile+".EFG", "-l", fileName+"_p2.log"};
 		
 		edu.umd.cs.guitar.graph.GUIStructure2GraphConverter.main(args);
 				
 	}
 	
 	
 	
 	public void runTCGenerator(int maxTC){
 		
 		
 		String inputFile = isolatePhases ? FilenameUtils.removeExtension(getExpectedFile(fileName, "EFG").getAbsolutePath()) : fileName;
 		
 		String maxTCstr = maxTC < 1 ? 3 : Integer.toString(maxTC); 
 		
		String[] args = {"-p", "SequenceLengthCoverage", "-e",  inputFile + ".EFG", "-l", fileName+"_p3.log", "--dir", "./currentTC", "-m", maxTCstr};
 		
 		edu.umd.cs.guitar.testcase.TestCaseGenerator.main(args);
 		
 	}
 	
 	
 	public void runWebReplayer(){
 		
 		File tcDir = new File(tcDirName);
 		
 		String inputFile = isolatePhases ? FilenameUtils.removeExtension(getExpectedFile(fileName, "EFG").getAbsolutePath()) : fileName;
 		
 		
 		
 		if(tcDir.isDirectory()){
 			
 			for( File f : tcDir.listFiles()){
 				
 				String testcase = f.getName();
 				String testcaseName = FilenameUtils.removeExtension(testcase);
 				
				String[] args = {"WebPluginInfo","--website-url", website,"-t", testcase, "-g", testcaseName, "-d", "1000", "-g", inputFile+".GUI", "-e", inputFile+".EFG", "-s", fileName+".STA", "-l",fileName +"_ph3.log" };
 				//MADE A CHANGE IN NewGReplayerConfiguration to support parameter for the STA file
 				edu.umd.cs.guitar.replayer.Launcher.main(args);
 				
 				
 			}
 			
 			
 		}
 		
 		
 	}
 	
 	
 	
 	private File getExpectedFile(String fileName, String ext){
 		
 		return new File(expectedOutputDirectry + "/" + fileName + "." + ext);
 		
 	}
 	
 	
 	
 
 }
