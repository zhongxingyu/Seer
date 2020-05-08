 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 
 /**
  * @author tamer
  * 
  */
 public class Generator {
 	public static void main(String[] args) {
 		generateFiles("TrendyLogics.com", 1000);
 	}
 
 	public static void generateFiles(String text, int max) {
 		for (int i = 0; i < max; i++) {
 			try {
 				URL url = new URL(
						"http://www.tamersaadeh.com/ColoredText/?text=" + text);
 				URLConnection con = url.openConnection();
 				BufferedReader in = new BufferedReader(new InputStreamReader(
 						con.getInputStream()));
 				File file = new File("Logos/" + i + ".html");
 				FileWriter fw = new FileWriter(file);
 				BufferedWriter bw = new BufferedWriter(fw);
 				String line;
 				while ((line = in.readLine()) != null)
 					bw.write(line + "\n");
 				bw.close();
 				fw.close();
 				in.close();
 			} catch (MalformedURLException e) {
 				e.printStackTrace();
 				System.out.println("it broke at loop: "+ i);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 }
