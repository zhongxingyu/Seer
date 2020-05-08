 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.nio.MappedByteBuffer;
 import java.nio.channels.FileChannel;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import org.jdom.Element;
 
 import edu.nyu.cs.javagit.api.DotGit;
 import edu.nyu.cs.javagit.api.JavaGitException;
 import edu.nyu.cs.javagit.api.Ref;
 import edu.nyu.cs.javagit.api.WorkingTree;
 import edu.nyu.cs.javagit.api.commands.GitCheckout;
 import edu.nyu.cs.javagit.api.commands.GitCheckoutResponse;
 import edu.nyu.cs.javagit.api.commands.GitLogOptions;
 import edu.nyu.cs.javagit.api.commands.GitLogResponse.Commit;
 import edu.nyu.cs.javagit.api.commands.GitLogResponse.CommitFile;
 
 
 public class GitExtractor {
 	static String REPO_PATH = "git-repos/";
 	//static String REPO_PATH = "D:/junit/junit/";
 	
 	public static ScmFile incrementComponentCount(HashMap<ScmFile, Integer>components, String path) {
     	ScmFile scmFile = new ScmFile(path);
     	
     	Integer changeCount = new Integer(0);
     	if (components.containsKey(scmFile)) {
     		changeCount = (Integer) components.get(scmFile);
     	}
     	
     	changeCount++;
     	components.put(scmFile, changeCount);
     	
     	return scmFile;
 	}
 	
 		
 	private static void addInfoToFiles(HashMap<ScmFile, Integer>components) throws IOException {
 		System.out.println("computing info...");
 		
 		Collection<ScmFile> toRemove = new ArrayList<ScmFile>(); 
 		
 		for (ScmFile scmFile : components.keySet()) {
 			File file = new File(REPO_PATH + "/" + scmFile.getLocalPath());
 			String contents = null;
 			if (file.exists())
 				contents = readFile(file);
 			else
 				toRemove.add(scmFile);
 			scmFile.extractInfo(contents);
 		}
 		
 		for (ScmFile scmFile : toRemove) {
 			components.remove(scmFile);
 		}
 		
 		System.out.println("ok");
 	}
 	
 	// http://stackoverflow.com/questions/326390/how-to-create-a-java-string-from-the-contents-of-a-file
 	private static String readFile(File file) throws IOException {
 		  FileInputStream stream = new FileInputStream(file);
 		  try {
 		    FileChannel fc = stream.getChannel();
 		    MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
 		    /* Instead of using default, pass in a decoder. */
 		    return Charset.defaultCharset().decode(bb).toString();
 		  }
 		  finally {
 		    stream.close();
 		  }
 		}
 	
 	public static void writeCountToFile(HashMap<ScmFile, Integer> components, String filename)
 	throws IOException {
 		/* 
 		 * Bruno
 		 * */	        
 
 		File file = new File(filename);
 		if(file.exists())
 			file.delete();
 		else
 			file.createNewFile();
 		FileWriter output = new FileWriter(file);
 		output.write("Package\tFileName\tNumberOfChanges\tFileType\tLOC\n");
 
 		Set<ScmFile> compsSet = components.keySet();
 		for ( Iterator<ScmFile> compsSetIterator = compsSet.iterator( ); compsSetIterator.hasNext( ); ) {
 			ScmFile comp = (ScmFile)compsSetIterator.next();
 			Integer changeCount = (Integer) components.get(comp);
 
 			output.write(comp.getPackageName() + "\t" + comp.getClassName() + "\t" + changeCount + "\t" + comp.getType() + "\t" + comp.getLinesOfCode() + "\n");
 			//output.close();
 		}
 	}
 	
 	// based on http://wiki.eclipse.org/JGit/User_Guide
 	public static void main(String[] args) throws IOException, JavaGitException {
 		
 		SCMtoXML scmXml;
 		
 		HashMap<ScmFile, Integer> components = new HashMap<ScmFile, Integer>();
 
 		File repositoryDirectory = new File(REPO_PATH);
 		
 		DotGit dotGit = DotGit.getInstance(repositoryDirectory);
 //		WorkingTree tree = dotGit.getWorkingTree();
 		GitLogOptions options = new GitLogOptions();
 		options.setOptFileDetails(true);
 		
 		
 		////////////////////////////////////
 		File repoPathDot = new File(REPO_PATH + "/.");
 		List<File> repoPathDotList = new ArrayList<File>();
 		repoPathDotList.add(repoPathDot);
 		GitCheckout checkout = new GitCheckout();
 		List<Commit> gitlog = dotGit.getLog(options);
 		int version = 1; //count the number of versions at least one java file was modified
 		boolean javaFileChanged = false;
		scmXml = new SCMtoXML("JUnit", gitlog.size()); //TODO: a user interface to choose the project name or get it automatically from Git 
 		
 		try{
 			for (edu.nyu.cs.javagit.api.commands.GitLogResponse.Commit commit : gitlog) {
 				System.out.println("Commit with " + commit.getFilesChanged() + " changes.");
 				//System.out.println("     Msg: "+commit.getMessage());
 				String hash = commit.getSha();
 				Ref ref = Ref.createSha1Ref(hash);
 							
 				GitCheckoutResponse response = checkout.checkout(repositoryDirectory, ref, repoPathDotList);
 				
 				List<CommitFile> files = commit.getFiles();
 				if (files == null || files.size() == 0)
 					continue;
 				
 				for (CommitFile file : files) {				
 					String path = file.getName();
 					
 	                if (ScmFile.isJavaFile(path)){
 	                	ScmFile scmFile = new ScmFile(path);
 	                	
 	        			File aafile = new File(REPO_PATH + "/" + scmFile.getLocalPath());
 	        			String contents = null;
 	        			if (aafile.exists()) {
 	        				javaFileChanged = true;
 		    				contents = readFile(aafile);
 		        			scmFile.extractInfo(contents);
 		    				System.out.println("v" + version + ": " + scmFile.getPackageName() + "." + scmFile.getClassName() + " LOC =  " + scmFile.getLinesOfCode());
 		    				
 		    				Element packageElement = scmXml.addPackage(scmFile.getPackageName());
 	    					Element classElement = scmXml.addClassToPackage(packageElement, scmFile.getClassName(), scmFile.getType());
 							scmXml.addVersionToClass(classElement, version, scmFile.getLinesOfCode());
 	        			}
 	                }
 				}
 				if(javaFileChanged)
 				{
 					scmXml.addLog(version, hash, commit.getAuthor(), commit.getDateString(), commit.getMessage());					
 					version++;
 					javaFileChanged = false;
 					//Warning: This version counting is only for our internal purpose. This is not git version counting.
 					//This counting only considers the java file modifications in the commits.
 					//When a commit is done without modifying at least one java file, the version counting is not considered.
 				}
 			}
 		}catch(Exception e)
 		{
 			e.printStackTrace();
 		}finally
 		{
 			
 		}
 		scmXml.writeToFile();
 
 		System.out.println(components.keySet().size() + " files analyzed.");
 		
 		addInfoToFiles(components);
 		
 		writeCountToFile(components, "FilesChange2.txt");
 	}
 	
 }
