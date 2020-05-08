 package fr.umlv.escape.file;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.os.Environment;
 
 import fr.umlv.escape.Objects;
 
 /** Supplies some functions to parse a text file. Creating a ParseFile Object just initialize the stream
  *  on the file path given. All the content file is loaded in memory on the first use of a function. 
  *  When the file content is loaded, the stream is closed. Data will be accessed directly in memory. DO NOT use
  *  a ParseFile object with big files.
  */
 public class ParseFile {
 	private final String fileName;
 	private BufferedReader in;
 	private final ArrayList<String> fileContent;
 	private int currentLine;
 	private boolean isInitialased;
 	private final Context context;
 	
 	 /** Create a ParseFile object.
 	  * 
 	  * @param fileName Path of the file.
 	 * @throws IOException 
 	  */
 	public ParseFile(Context context, String fileName) throws IOException{
 		Objects.requireNonNull(fileName);
 		this.fileName=fileName;
 		try{
 			this.in=new BufferedReader(new InputStreamReader(context.getAssets().open(fileName)));
 		} catch (Exception e) {
			File appDir = context.getCacheDir();
			File file = new File(appDir, fileName);
 			this.in=new BufferedReader(new InputStreamReader(new FileInputStream(file)));
 		}
 		this.fileContent=new ArrayList<String>();
 		this.isInitialased=false;
 		this.context = context;
 	}
 
 	/** Load all the file content in memory.
 	 * @throws IOException
 	 */
 	private void loadFileContent() throws IOException{
 		String buffer;
 		while((buffer=in.readLine())!=null){
 			this.fileContent.add(buffer);
 		}
 		this.currentLine = 0;
 		in.close();
 		this.isInitialased=true;
 	}
 	
 	/** Get the next line of data of the file.
 	 * @return The next line of the file or null if the file is ended.
 	 * @throws IOException
 	 */
 	public String getNextLine() throws IOException{
 		if(!this.isInitialased){
 			this.loadFileContent();
 		}
 		if(currentLine < fileContent.size()){
 			String str = fileContent.get(currentLine);
 			currentLine++;
 			return str;
 		}
 		return null;
 	}
 	
 	/** Reset to the beginning. The next call to the {@link #getNextLine()} function will
 	 * return the first line of the file.
 	 */
 	public void reset(){
 		currentLine = 0;
 	}
 	
 	/** Return the name of the file parsed.
 	 * @return the name of the file parsed.
 	 */
 	public String getFileName() {
 		return fileName;
 	}
 }
