 
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
import java.util.logging.Level;
 
 public abstract class FileLoader {
 	protected String filename = "";
 	protected String header ="";
 	
 	protected File obtainFile() {
 		if( filename.isEmpty() ) {
			Server.log(Level.SEVERE, "Filename is blank.");
 			System.exit(-1);
 		}
 		
 		File check;
 		check = new File(filename);		
 		if( !check.exists() ) {
 			try {
 				check.createNewFile();
 			}
 			catch(Exception e) {
				Server.log(Level.SEVERE, "Could not create file: "+filename);
 				System.exit(-1);
 			}
 		}
 
 		return check;
 	}
 	
 	private static void copyFile(File in, File out) throws Exception {
 	    FileInputStream fis  = new FileInputStream(in);
 	    FileOutputStream fos = new FileOutputStream(out);
 	    try {
 	        byte[] buf = new byte[1024];
 	        int i = 0;
 	        while ((i = fis.read(buf)) != -1) {
 	            fos.write(buf, 0, i);
 	        }
 	    } 
 	    catch (Exception e) {
 	        throw e;
 	    }
 	    finally {
 	        if (fis != null) fis.close();
 	        if (fos != null) fos.close();
 	    }
 	  }
 	protected abstract String saveString();
 	public void save() {
 		File outFile = obtainFile();
 		outFile.delete();
 		outFile = obtainFile();
 		if (outFile!=null) {
 			try {
 				BufferedWriter writer = new BufferedWriter(new FileWriter(outFile));
 				writer.write(header);
 		        writer.write(saveString());
 		        writer.flush();
 		        writer.close();
 			}
 			catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 		else {
			Server.log(Level.SEVERE, String.format("Unable to save %s", filename));
 		}
 	}
 	protected abstract void beforeLoad();
 	protected abstract void loadLine(String line);
 	public void load() {
 		beforeLoad();
 		header="";
 		File inFile = obtainFile();
 		boolean readingHeader=true;
 		if (inFile!=null) {
 			try {
 				BufferedReader reader = new BufferedReader(new FileReader(inFile));
 				String line = null;
 		        while ((line=reader.readLine()) != null) {
 		        	if (readingHeader && line.startsWith("#")) {
 		        			header+=line+"\r\n";
 		        	}
 		        	else {
 		        		readingHeader=false;
 		        		loadLine(line);
 		        	}
 		        }
 		        reader.close();
 			}
 			catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 		else {
			Server.log(Level.SEVERE, String.format("Unable to load %s.", filename));
 		}
 	}
 }
