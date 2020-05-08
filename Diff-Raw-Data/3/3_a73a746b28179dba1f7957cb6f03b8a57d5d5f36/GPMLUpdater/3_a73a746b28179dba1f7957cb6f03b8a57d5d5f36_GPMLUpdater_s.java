 import java.io.File;
 
 import org.pathvisio.core.model.ConverterException;
 import org.pathvisio.core.model.Pathway;
 
 
 public class GPMLUpdater {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		if(args.length == 2) {
 			File file = new File(args[0]);
 			File output = new File(args[1]);
 			if(file.exists()) {
 				try {
 					Pathway pathway = new Pathway();
 					pathway.readFromXml(file, true);
 					
 					if(!output.exists()) {	
 						pathway.writeToXml(output, true);
 					} else {
 						System.out.println("Output file already exists. " + output.getAbsolutePath());
 					}
 				} catch (ConverterException e) {
 					System.out.println("Could not read pathway file. " + file.getAbsolutePath());
 				}
 			} else {
 				System.out.println("Input file does not exist. " + file.getAbsolutePath());
 			}
 		} else {
 			System.out.println("Invalid input arguments. Use java -jar gpml-updater.jar /path/to/old-pathway-file.gpml /path/to/new-pathway-file.gpml.");
 		}		
 	}
 
 }
