 /**
  * Xi Gong
  * Sep 7, 2013
  */
 import java.io.*;
 import java.util.Locale;
 import java.util.Scanner;
 import java.util.Date;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.text.ParseException;
 
 public class Preprocess {
 	public static void main(String[] args) {
 		processTeamRaw();
 		try {
 			processGameRaw();
 		} catch (ParseException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public static void processTeamRaw(){
 		File input = new File("teams_raw.txt");
 		File output = new File("teams.txt");
 		try{
 			Scanner s = new Scanner(input);
 			PrintWriter p = new PrintWriter(output);
 			while(s.hasNextLine()) {
 				String[] buffer = s.nextLine().split("\t");
 				buffer[1] = Integer.toString(toClose(Double.parseDouble(buffer[1])));
 				String b = buffer[0] + "," + buffer[1];
 				p.println(b);
 			}
			s.close();
 			p.close();
 		}catch(FileNotFoundException e1) {
 			e1.printStackTrace();
 		}
 	}
 	
 	public static void processGameRaw() throws ParseException {
 		File input = new File("games_raw.txt");
 		File output = new File("games.txt");
 		try{
 			Scanner s = new Scanner(input);
 			PrintWriter p = new PrintWriter(output);
 			while(s.hasNextLine()) {
 				String[] buffer = s.nextLine().split("\t");
 				String[] subbuffer = buffer[0].split(" ");
 				String dateString = subbuffer[1] + " " + subbuffer[2] + " " + subbuffer[3];
 				DateFormat df = new SimpleDateFormat("MMM dd yyyy", Locale.ENGLISH);
 				Date temp = df.parse(dateString);
 				df = new SimpleDateFormat("MM/dd/yyyy");
 				dateString = df.format(temp);
 				String b = dateString.concat(",").concat(buffer[2]).concat(",").concat(buffer[1]);
 				p.println(b);
 			}
			s.close();
 			p.close();
 		}catch(FileNotFoundException e1) {
 			e1.printStackTrace();
 		}
 	}
 	
 	public static int toClose(double d) {
 		if(d - (int) d >= 0.5) {
 			return (int) d + 1;
 		}
 		else
 			return (int) d;
 	}
 }
