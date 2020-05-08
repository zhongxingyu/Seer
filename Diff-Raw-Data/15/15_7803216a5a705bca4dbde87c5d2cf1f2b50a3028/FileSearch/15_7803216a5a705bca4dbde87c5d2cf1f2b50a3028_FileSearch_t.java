 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.util.ArrayList;
 import java.util.concurrent.Callable;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class FileSearch implements Callable<Found> {
 	
 	private final File currentFile;
 	private final String patternString;
 	private ArrayList<String> list = new ArrayList<String>();
 	
 	public FileSearch(File currentFile, String patternString){
 		this.currentFile=currentFile;
 		this.patternString=patternString;
 	}
 
 	public Found call() throws Exception{
 		FileReader fstream = new FileReader(currentFile);
 		BufferedReader reader = new BufferedReader(fstream);
 		String currentLine;
 		long lineCount=0;
 		Found result = new Found();
 		Pattern expression = Pattern.compile(patternString);
 		result.setName(currentFile.getName());
		
 		while((currentLine=reader.readLine())!=null){
 			Matcher matcher = expression.matcher(currentLine);
			if(matcher.find()){
				list.add(lineCount +" "+ currentLine);
 			}
 			lineCount++;
 		}
		result.setEntries(list);
 		
 		
 		
 		return result;
 	}
}
