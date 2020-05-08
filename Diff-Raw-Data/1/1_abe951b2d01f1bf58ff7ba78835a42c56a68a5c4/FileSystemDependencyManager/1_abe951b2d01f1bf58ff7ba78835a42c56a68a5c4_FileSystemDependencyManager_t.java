 package languish.compiler;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.List;
 
 import languish.compiler.error.DependencyUnavailableError;
 
 public class FileSystemDependencyManager implements DependencyManager {
 
 	private final List<String> paths;
 
 	public FileSystemDependencyManager(List<String> paths) {
 		this.paths = paths;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * languish.interpreter.fooDependencyManager#getResource(java.lang.String)
 	 */
 	@Override
 	public String getResource(String resourceName) {
 		for (String path : paths) {
 			String docPath = path + '/' + resourceName + ".lish";
 			File f = new File(docPath);
 
 			if (!f.exists()) {
 				continue;
 			}
 
 			StringBuilder doc = new StringBuilder();
 
 			try {
 				BufferedReader br = new BufferedReader(new FileReader(f));
 				while (true) {
 					String line = br.readLine();
 					if (line == null) {
 						break;
 					}
 					doc.append(line).append('\n');
 				}
 			} catch (IOException ioe) {
 				throw new DependencyUnavailableError(ioe);
 			}
 
 			return doc.toString();
 		}
 		throw new DependencyUnavailableError(resourceName);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * languish.interpreter.fooDependencyManager#hasResource(java.lang.String)
 	 */
	@Override
 	public boolean hasResource(String resourceName) {
 
 		System.out.println("hasResources");
 		for (String path : paths) {
 			String docPath = path + '/' + resourceName + ".lish";
 			File f = new File(docPath);
 
 			if (f.exists()) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 }
