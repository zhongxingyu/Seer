 package ghosty.files;
 
 import java.io.IOException;
 import java.nio.file.DirectoryStream;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.util.Iterator;
 import java.util.LinkedList;
 
 /**
  * Class who manage a directory
  *
  */
 public class Directory {
 	private String path;
 	
 	public Directory(String path) {
 		this.path = path;
 	}
 	
 	public LinkedList<String> getFiles () {
 		return new LinkedList<String>();
 	}
 	
 	public  boolean exists(Path path){
 		 return this.exists(path);
 
 	}
 	
 
 	/**
 	 * Create a directory. Do nothing if the directory already exists
 	 * 
 	 * @param directory
 	 */
 	public static void createDirectory(String directory) {
 		Path monRepertoire = Paths.get(directory);
 		try {
 			Path file = Files.createDirectory(monRepertoire);
 		} catch (IOException e) {}
 	}
 	
 	/**
	 * Return path of everyfiles in a directory
 	 * 
 	 * @param path
 	 * @return
 	 * @throws IOException
 	 */
	public static LinkedList<Path> getFilesFromPath(String path)  throws IOException {
 		
		LinkedList<Path> list = new LinkedList<Path>();
 		Path jdkPath = Paths.get(path);
 		DirectoryStream<Path> stream = Files.newDirectoryStream(jdkPath);
 		try {
 		
 			Iterator<Path> iterator = stream.iterator();
 		
 			while (iterator.hasNext()) {
 
 				Path p = iterator.next();
 
 				if (!p.toString().equals(".ghosty")) {
					list.add(p);
 					if (Files.isDirectory(p))
						getFilesFromPath(p.toString());
 				}
 
 			}
 		
 		} finally {
 		
 		stream.close();
 		
 		}
 		return list;
 	}
 	
 
 }
 
