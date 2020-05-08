 /**
  * 
  */
 package reindent;
 
import java.io.File;

 /**
  * @author Taylor Countryman <countryman.taylor@gmail.com>
  */
 public class Reindent
 {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args)
 	{
 		if(args.length != 2)
 		{
 			System.err.println("Usage: java reindent.Reindent INFILE OUTFILE");
 			System.exit(1);
 		}
		File inFile = new File(args[0]);
		if(!inFile.isFile() || !inFile.canRead() || inFile.length() <= 0)
		{
			System.err.println( "reindent: " + inFile.getName() + ": Must be a readable file with content");
			System.exit(1);
		}
		
		
 	}
 }
