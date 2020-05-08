 package actions;
 
 import java.io.*;
 /**
  * 
  * This action copies files to a new destination
  *
  */
 public class CopyAction extends Action {
 
 	private String _dirName;
 	static final int BUFSIZE = 1024;
 	public CopyAction(String param){
 		_dirName = param;
 	}
 
 	
 	public void performAction(File f)
 	{
 		// create the directory if does not exist
 		File outFileDir = new File (_dirName);
 		if (!outFileDir.exists())
 		{
 			outFileDir.mkdirs();
 		}
 		
 		// copy the file to a new file 
 		File outFile = new File (_dirName + File.separator + f.getName());
 		try
 		{
 			InputStream in = new FileInputStream(f);
 			OutputStream out = new FileOutputStream(outFile);
 			byte[] buf = new byte[BUFSIZE];
 		    int len;
 		    while ((len = in.read(buf)) > 0){
 		        out.write(buf, 0, len);
 		    }
 		    in.close();
 		    out.close();
 		}
		catch(FileNotFoundException ex){
			System.out.println(ex.getMessage() + " in the specified directory.");
		    System.exit(0);
 		}
 		catch(IOException e){
		    System.out.println(e.getMessage());      
 		}
 		
 	}
 
 }
