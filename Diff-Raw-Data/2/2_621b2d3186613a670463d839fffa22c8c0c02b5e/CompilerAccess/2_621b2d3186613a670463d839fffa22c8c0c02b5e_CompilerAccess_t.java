 package de.unisiegen.informatik.bs.alvis.compiler;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import org.antlr.runtime.RecognitionException;
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.core.runtime.Platform;
 
 import de.unisiegen.informatik.bs.alvis.io.files.FileCopy;
 import de.unisiegen.informatik.bs.alvis.primitive.datatypes.PCObject;
 import de.uni_siegen.informatik.bs.alvic.Compiler;
 
 /**
  * @author mays
  * @author Colin
  *
  */
 public class CompilerAccess {
 	private Compiler c;
 	private Collection<PCObject> datatypes;
 	private Collection<String> datatypePackages;
 	private static CompilerAccess instance;
 
 	public static CompilerAccess getDefault() {
 		if (instance == null)
 			instance = new CompilerAccess();
 		return instance;
 	}
 
 	public boolean compile(String code, ArrayList<PCObject> datatypes) {
 		// TODO Compilieren
 		// CompilerManager manager = new CompilerManager();
 		return true;
 	}
 
 	String algorithmPath = "";
 
 	/**
 	 * 
 	 * @param path path to the source code that
 	 * @return path to the generated .java file if it exists, null otherwise
 	 * 
 	 * @throws IOException, RecognitionException
 	 */
 	public File compile(String path) throws FileNotFoundException, IOException, RecognitionException {
 		c = new Compiler(datatypes, datatypePackages);
 //		testDatatypes();
 		String javaCode = c.compile(readFile(Platform.getInstanceLocation().getURL()
 				.getPath() + path));
 		if (null == javaCode)
 			return null;
 		
 		
 		File result = new File(Platform.getInstanceLocation().getURL()
 				.getPath()
				+ getWorkspacePath(path)  + "/Algorithm.java");//new File(path).getName().toString().replaceAll("\\.[^.]*$", ".java"));
 		FileWriter fstream;
 		fstream = new FileWriter(result);
 		BufferedWriter out = new BufferedWriter(fstream);
 //		out.write(javaCode.replaceAll("#ALGORITHM_NAME#", result.getName().replaceAll("\\.java", "")));
 		out.write(javaCode.replaceAll("#ALGORITHM_NAME#", "Algorithm"));
 		out.close();
 		return result;
 	}
 
 	private String getWorkspacePath(String fileWithPath) {
 		String[] splitedPathToAlgorithm = fileWithPath.split("\\/"); //FIXME this will not work on Windows
 		ArrayList<String> partsOfAlgoPath = new ArrayList<String>();
 		for (String part : splitedPathToAlgorithm) {
 			partsOfAlgoPath.add(part);
 		}
 
 		// get and remove the filename
 		String algoWorkSpaceFile = partsOfAlgoPath.remove(partsOfAlgoPath.size()-1);
 		
 		String SLASH = System.getProperty("file.separator");
 		
 		// getPath
 		String algoWorkSpacePath = "";
 		for (String part : partsOfAlgoPath) {
 			algoWorkSpacePath += part + SLASH;
 		}
 		
 		return algoWorkSpacePath;
 	}
 	
 	public String getAlgorithmPath() throws IOException {
 		String path = "";
 		path = FileLocator.getBundleFile(Activator.getDefault().getBundle())
 				.getCanonicalPath().toString();
 		return algorithmPath;
 	}
 	
 	private String readFile(String fileName) throws FileNotFoundException
 	{
 		BufferedReader fstream = new BufferedReader(new FileReader(fileName));
 		String result = "";
 		
 		try {
 			while (fstream.ready())
 				result += fstream.readLine() + System.getProperty("line.separator");
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		return result;
 	}
 
 	public List<Exception> getExceptions() {
 		return c.getExceptions();
 	}
 
 	/**
 	 * This method copies the Dummy Algorithm file next to the PCAlgorithm file
 	 * that is written by the user.
 	 * To get the path of the created file see getAlgorithmPath().
 	 * @param pathToAlgorithm
 	 *            relative to Alvis-workspace e.g.: "project/src/Algorithm.algo"
 	 * @return Name of the Java Algorithm file
 	 */
 	public File compileThisDummy(String pathToAlgorithm) {
 		String SLASH = System.getProperty("file.separator");
 
 		// the path were the translated java file is.
 		String pathWhereTheJavaIs = "";
 		try {
 			pathWhereTheJavaIs = FileLocator
 					.getBundleFile(Activator.getDefault().getBundle())
 					.getCanonicalPath().toString();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		// The compiled algorithm
 		File source = new File(pathWhereTheJavaIs + SLASH + "Algorithm.java");
 
 		// Get the path to algorithm and separate path and filename
 		String[] splitedPathToAlgorithm = pathToAlgorithm.split("\\/"); //FIXME this will not work on Windows
 		ArrayList<String> partsOfAlgoPath = new ArrayList<String>();
 		for (String part : splitedPathToAlgorithm) {
 			partsOfAlgoPath.add(part);
 		}
 
 		// get and remove the filename
 		String algoWorkSpaceFile = partsOfAlgoPath.remove(partsOfAlgoPath.size()-1);
 		
 		// getPath
 		String algoWorkSpacePath = "";
 		for (String part : partsOfAlgoPath) {
 			algoWorkSpacePath += part + SLASH;
 		}
 
 //		for (String st : splitedPathToAlgorithm)
 //			System.out.println(st);
 
 		// Destination 
 		File destination = new File(Platform.getInstanceLocation().getURL()
 				.getPath()
 				+ algoWorkSpacePath + "Algorithm.java");
 		algorithmPath = Platform.getInstanceLocation().getURL().getPath()
 				+ algoWorkSpacePath + SLASH;
 
 		// Copy compiled file into the workspace
 		FileCopy fileCopy = new FileCopy();
 		fileCopy.copy(source, destination);
 
 		// Still hard Coded.
 		String javaFilePath = "Algorithm"; // TODO HARD CODED!
 		return destination;
 	}
 
 	/* ******************************************
 	 * The Datatypes and Packagenames *****************************************
 	 */
 	/**
 	 * @param datatypes
 	 *            the datatypes to set
 	 */
 	public void setDatatypes(Collection<PCObject> datatypes) {
 		this.datatypes = datatypes;
 	}
 
 	/**
 	 * @param datatypePackages
 	 *            the datatypePackages to set
 	 */
 	public void setDatatypePackages(Collection<String> datatypePackages) {
 		this.datatypePackages = datatypePackages;
 	}
 
 	@SuppressWarnings("unchecked")
 	public void testDatatypes() {
 		try {
 			System.out.println("Compiler shows its datatypes:");
 			for (PCObject obj : datatypes) {
 				System.out.println(obj.getClass());
 				List<String> tmp = ((List<String>)obj.getClass().getMethod("getMembers").invoke(obj));
 				System.out.println("available attributes:" + (tmp==null ? "null" : tmp));
 				tmp = ((List<String>)obj.getClass().getMethod("getMethods").invoke(obj));
 				System.out.println("available methods:" + (tmp==null ? "null" : tmp));
 			}
 			System.out.println("Compiler shows its packages:");
 			for (String obj : datatypePackages) {
 				System.out.println(obj);
 			}
 		} catch (IllegalArgumentException e) {
 			e.printStackTrace();
 		} catch (SecurityException e) {
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			e.printStackTrace();
 		} catch (InvocationTargetException e) {
 			e.printStackTrace();
 		} catch (NoSuchMethodException e) {
 			e.printStackTrace();
 		}
 	}
 
 }
