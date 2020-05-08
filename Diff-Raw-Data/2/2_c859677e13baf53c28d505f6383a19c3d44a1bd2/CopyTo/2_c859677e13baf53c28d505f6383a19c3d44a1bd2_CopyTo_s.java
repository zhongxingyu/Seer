 /**
  * 
  */
 package uk.co.unclealex.flacconverter.main;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 import uk.co.unclealex.flacconverter.Constants;
 import uk.co.unclealex.flacconverter.IOUtils;
 
 /**
  * @author alex
  *
  */
 public class CopyTo {
 
 	public static void main(String[] args) {
 		String extension = args[0];
 		String owner = args[1];
 		String targetDir = new File(args[2]).getAbsolutePath();
 
 		File source = new File(new File(Constants.BASE_DIR, extension), owner);
 		int sourceLength = source.getAbsolutePath().length();
 		
 		SortedSet<File> files = new TreeSet<File>();
 		for (File file : IOUtils.getAllFilesWithExtension(source, extension)) {
 			files.add(file);
 		}
 
 		int current = 1;
 		int total = files.size();
 		for (File file : files) {
 			String path = file.getAbsolutePath();
 			File target = new File(targetDir + path.substring(sourceLength));
 			System.out.printf(
					"%d of %d (3.2%f%%), %s -> %s", current, total, 100 * current / (double) total,
 					file.getAbsolutePath(), target.getAbsolutePath());
 			System.out.println();
 			current++;
 			target.getParentFile().mkdirs();
 			try {
 				FileInputStream in = new FileInputStream(file);
 				FileOutputStream out = new FileOutputStream(target);
 				IOUtils.copy(in, out);
 				in.close();
 				out.close();
 			}
 			catch(IOException e) {
 				e.printStackTrace(System.err);
 			}
 		}
 	}
 }
