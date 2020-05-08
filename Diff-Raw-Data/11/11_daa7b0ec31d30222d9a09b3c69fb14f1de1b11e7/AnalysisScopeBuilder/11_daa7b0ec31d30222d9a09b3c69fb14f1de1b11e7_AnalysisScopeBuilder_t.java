 package iterace.oldjava;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.jar.JarFile;
 
 import com.ibm.wala.classLoader.BinaryDirectoryTreeModule;
 import com.ibm.wala.classLoader.Module;
 import com.ibm.wala.ipa.callgraph.AnalysisScope;
 import com.ibm.wala.util.config.FileOfClasses;
 import com.ibm.wala.util.io.FileProvider;
 
 public class AnalysisScopeBuilder {
 	public List<String> binaryDependencies = new ArrayList<String>();
 	public List<String> jarDependencies = new ArrayList<String>();;
 	public List<String> extensionBinaryDependencies = new ArrayList<String>();;
 	private AnalysisScope scope = AnalysisScope.createJavaAnalysisScope();;
 
 	public AnalysisScopeBuilder(String jreLibPath) throws IllegalArgumentException, IOException {
 		scope.addToScope(scope.getLoader(AnalysisScope.PRIMORDIAL), new JarFile(jreLibPath));
 	}
 
 	public void addBinaryDependency(String directory) throws IOException {
//		System.out.println("Binary: "+directory);
 		File sd = FileProvider.getFile(directory, getLoader());
 		assert sd.isDirectory();
 		scope.addToScope(scope.getLoader(AnalysisScope.APPLICATION), new BinaryDirectoryTreeModule(sd));
 	}
 
 	private ClassLoader getLoader() {
 		return this.getClass().getClassLoader();
 	}
 	
 	public void addExtensionBinaryDependency(String directory) throws IOException {
//		System.out.println("Binary extension: "+directory);
 		File sd = FileProvider.getFile(directory, getLoader());
 		assert sd.isDirectory();
 		scope.addToScope(scope.getLoader(AnalysisScope.EXTENSION), new BinaryDirectoryTreeModule(sd));
 	}
 	
 	public void addJarFolderDependency(String path) throws IOException {
//		System.out.println("Jar folder: "+path);
 	  File dir = new File(path);
 	  String delim;
 	  
 	  if (path.endsWith("/"))
 	    delim = "";
 	  else
 	    delim = "/";
 	  
 	  if (!dir.isDirectory())
 	    return;
 	  
 	  String[] files = dir.list();
 	  if (files == null)
 	    return;
 	  
 	  for (String fileName : files) {
       if (fileName.endsWith(".jar"))
         addJarDependency(path + delim + fileName);
       else {
         File file = new File(fileName);
         if (file.isDirectory())
           addJarFolderDependency(file.getAbsolutePath());
       }
     }
 	}
 
 	public void addJarDependency(String file) throws IOException {
//		System.out.println("Jar: "+file);
 		Module M = FileProvider.getJarFileModule(file, getLoader());
 		scope.addToScope(scope.getLoader(AnalysisScope.APPLICATION), M);
 	}
 	
 	public AnalysisScope getAnalysisScope() throws IOException {
 		return scope;
 	}
 	
 	public void setExclusionsFile(String file) throws IOException {
 		scope.setExclusions(FileOfClasses.createFileOfClasses(new File(file)));
 	}
 }
