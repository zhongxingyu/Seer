 package essentials;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.Scanner;
 
 public class Option {
	public static final File optionsFile = new File(System.getProperty("user.dir"), "Options.txt");
 	private static String[] getOptions(){
 		ArrayList<String> contents = new ArrayList<String>(0);
 		try {
 			Scanner scanner = new Scanner(optionsFile);
 			while(scanner.hasNext()){
 				String option = scanner.nextLine();
 				if(option.charAt(option.length() - 1) != ':')
 					contents.add(option);
 			}
 			scanner.close();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 		String[] values = new String[contents.size()];
 		for(int i = 0; i < contents.size(); i++)
 			values[i] = contents.get(i);
 		return values;
 	}
 	public static String getRawOption(int index){
 		return getOptions().length > index ? getOptions()[index] : "";
 	}
 	public static int getNumberOfOptions(){
 		return getOptions().length;
 	}
 }
