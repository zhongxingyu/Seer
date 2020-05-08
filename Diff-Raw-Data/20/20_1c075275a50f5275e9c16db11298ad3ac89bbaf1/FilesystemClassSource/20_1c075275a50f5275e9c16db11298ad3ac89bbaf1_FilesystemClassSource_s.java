 package net.sf.jlayercheck.util.model;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Map;
 import java.util.TreeMap;
 
import net.sf.jlayercheck.util.DependencyVisitor;

 import org.objectweb.asm.ClassReader;
 
 public class FilesystemClassSource implements ClassSource {
 
 	/** Path to the .class files */
 	protected String bin;
 	
 	/** Path to the .java files */
 	protected String src;
 	
 	public FilesystemClassSource(String bin, String src) {
 		this.bin = bin;
 		this.src = src;
 	}
 	
    public void call(DependencyVisitor v) throws IOException {
 		File f = new File(getBin());
 
         checkDirectory(v, f);
 	}
 
	protected void checkDirectory(DependencyVisitor v, File f) throws IOException, FileNotFoundException {
         File files[] = f.listFiles();
         if (files != null) {
         	for(File file : files) {
         		if (file.isDirectory()) {
         			checkDirectory(v, file);
         		} else {
         			String name = file.getAbsolutePath();
         			if (name.endsWith(".class")) {
         				new ClassReader(new FileInputStream(file)).accept(v, 0);
         			}
         		}
         	}
         }
 	}
 
 	public Map<String, URL> getSourceFiles() {
 		Map<String, URL> result = new TreeMap<String, URL>();
 		
 		result.putAll(findFiles(getSrc(), "", ".java"));
 		
 		return result;
 	}
 
 	/**
 	 * Returns a map containing all sources found by a recursive search.
 	 * 
 	 * @param src start directory
 	 * @param extension only files with this extension are added
 	 * @return
 	 */
 	protected Map<String, URL> findFiles(String base, String src, String extension) {
 		Map<String, URL> result = new TreeMap<String, URL>();
 		
 		File searchbase = new File(base, src);
 		File[] files = searchbase.listFiles();
 		if (files != null) {
 			for(File singlefile : files) {
 				if (singlefile.isDirectory()) {
 					result.putAll(findFiles(base, src + File.separator + singlefile.getName(), extension));
 				}
 
 				if (singlefile.isFile()) {
 					if (singlefile.getName().endsWith(extension)) {
 						try {
 							String filename = src + File.separator + singlefile.getName();
 							filename = filename.replaceAll("^/", ""); // remove leading slash
 							filename = filename.replaceAll(extension+"$", ""); // remove extension
 							result.put(filename, singlefile.toURL());
 						} catch (MalformedURLException e) {
 							e.printStackTrace();
 						}
 					}
 				}
 			}
 		}
 		
 		return result;
 	}
 
 	public String getBin() {
 		return bin;
 	}
 
 	public void setBin(String bin) {
 		this.bin = bin;
 	}
 
 	public String getSrc() {
 		return src;
 	}
 
 	public void setSrc(String src) {
 		this.src = src;
 	}
 }
