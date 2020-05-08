 package check;
 import java.io.File;
 import java.io.IOException;
 import java.nio.charset.Charset;
 import java.nio.charset.StandardCharsets;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.JTextField;
 
 
 public class Main {
 	//  final static Charset ENCODING = StandardCharsets.UTF_8;
 	//  final static String OUTPUT_FILE_NAME = "/home/joel/proj/out";
 	//  final static String FILE_NAME = "/home/joel/proj/rai_pub2";
 	 // final static String 
 	
 	  public static void main(String... aArgs) throws IOException{
 		Singleton.ENCODING = StandardCharsets.UTF_8;
 		File f = new File(".","paper_finder_settings");
 
 		Singleton.settings_location = new BTextPairSettings("Settings location: ",f.getAbsolutePath());
 		Singleton.remove = new LTextPair("Contains","In:");
 		Singleton.matches = new LTextPair("Matches", ".* [0-9][0-9][0-9][0-9]$");
 		Singleton.path_in = new BTextPair("Path in","./rai_pub");
 		Singleton.remove_name = new LTextPair("Contains name","Rai");
 		Singleton.split_by = new LTextPair("Split By:", ".");
//		Singleton.area_text = new JTextField("area text");
 		Singleton.path_out = new BTextPair("Path out","./out");
 		Singleton.path_pub = new BTextPair("Path pub","./pub_compare");
 		
 		LoadSettings settings = new LoadSettings("paper_finder_settings");
 		Singleton.global_settings = settings.getFprop();
 		
 		
 		Singleton.setSettings("Rai");
     	Singleton.titles = new ArrayList<String>();
 		Singleton.uploaded_pub_list = new JTextField("");
 	    Singleton.text = new ReadWriteTextFileJDK7();	    
 	    List<String> pub_lines = Singleton.text.readSmallTextFile(Singleton.path_pub.getText());
 	    Singleton.uploaded_pub_list.setText(DataProc.makeBlock(pub_lines));
 
 	   // Singleton.gui = new GUI();
 	    new GUI();
 	  }
 	  
 
 	
 }
