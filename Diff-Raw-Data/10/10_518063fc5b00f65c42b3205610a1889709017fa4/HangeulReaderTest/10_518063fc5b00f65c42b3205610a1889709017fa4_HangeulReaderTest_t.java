 /**
  * 
  */
 package se.iroiro.md.hangeulreader;
 
 import java.awt.Font;
 import java.text.DateFormat;
 import java.text.DecimalFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import se.iroiro.md.hangeul.CharacterRenderer;
 import se.iroiro.md.hangeul.Hangeul;
 import se.iroiro.md.hangeul.HangeulClassifier;
 import se.iroiro.md.hangeul.JamoReferenceDB;
 
 /**
  * This class tests the classifier and displays results.
  * @author j
  *
  */
 public class HangeulReaderTest {
 	
 	public static final int CHARSIZE = 200;
 	
 	private String makeString(char from, char to){
 		StringBuilder s = new StringBuilder();
 		for(char c = from; c <= to; c++){
 			s.append(c);
 		}
 		return s.toString();
 	}
 	
 	public String testAll(Font font, JamoReferenceDB jrdb){
 		return test(makeString('\uAC00','\uD7A3'), font, jrdb);
 	}
 	
 	public String test(char from, char to, Font font, JamoReferenceDB jrdb){
 		return test(makeString(from,to), font, jrdb);
 	}
 	
 	public String test(String characters, Font font, JamoReferenceDB jrdb){
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd H:m:ss");
 		StringBuilder result = new StringBuilder();
 		result.append("Character images rendered using font \""+font.getName()+"\" at "+CHARSIZE+"x"+CHARSIZE+" pixels.\n");
 		result.append("Test started: "+dateFormat.format(new Date())+"\n");
 		System.out.println("Preparing to scan " + (int) (characters.length()) + " characters.");
 		HangeulClassifier hc = new HangeulClassifier(jrdb);
 		StringBuilder matches = new StringBuilder();
 		StringBuilder misses = new StringBuilder();
 		int count = 0;
 		boolean isMatch;
 		System.out.println("Classifying characters rendered from font \""+font.getName()+"\".");
 		System.out.println("Circle (o) means a character was correctly classified,\ndash (-) means it was incorrectly classified.");
 		for(int nn = 0; nn < characters.length(); nn++){
 //			CharacterRenderer.makeCharacterImage(c, CHARSIZE, CHARSIZE);
 			char c = characters.charAt(nn);
 			hc.newClassification(CharacterRenderer.makeCharacterImage(c, CHARSIZE, CHARSIZE, font));
 			Hangeul h = hc.getHangeul();
 			if(h != null && h.toString().charAt(0) == c){
 				isMatch = true;
 				matches.append(c);
 			}else{
 				isMatch = false;
 //				if(h != null){
 //					misses.append(String.valueOf(c)+"-"+h.toString()+" ");
 //				}else{
 //					misses.append(String.valueOf(c)+"-? ");
 //				}
 				misses.append(c);
 			}
 			if(count >= 100){
 				count = 0;
 				System.out.println();
 			}
 			count++;
 			if(isMatch) System.out.print("o");
 			if(!isMatch) System.out.print("-");
 		}
 		System.out.println();
 		System.out.println();
 		DecimalFormat df = new DecimalFormat("0.##");
 		int matchesc = matches.length();
 		result.append("Test finished: "+dateFormat.format(new Date())+"\n\n");
 		result.append(matchesc + " correct out of " + characters.length() + " characters (" + df.format(((matchesc*100)/(characters.length())))+" %).\n\n");
 		result.append("Correctly classified characters:\n");
 		int counter = 0;
 		for(int i = 0; i < matches.length(); i++){
 			char c = matches.charAt(i);
 			result.append(c);
 //			Helper.p(c);
 			if(++counter >= 80){
 				counter = 0;
 //				result.append("\n");
 //				System.out.println();
 			}
 		}
 		result.append("\n");
 //		System.out.println();
 		if(misses.length() > 0){
 			result.append("\nMissed characters:\n");
 //			System.out.println();
 //			System.out.println("Missed characters:");
 			counter = 0;
 			for(int i = 0; i < misses.length(); i++){
 				char c = misses.charAt(i);
 //				Helper.p(c);
 				result.append(c);
 				if(++counter >= 80){
 					counter = 0;
 //					System.out.println();
 //					result.append("\n");
 				}
 			}
 //			System.out.println();
 		}
 		Helper.p("Done.\n");
 		return result.toString();
 //		Helper.dump(result.toString(), "/Users/j/Desktop/result.txt");
 //		System.out.println();
 //		System.out.println();
 	}
 	
 //	private void writeImage(BufferedImage img, String fileName){
 //		try{
 //			ImageIO.write(img, "png", new FileOutputStream(fileName));
 //		} catch (FileNotFoundException e) {
 //			e.printStackTrace();
 //		} catch (IOException e) {
 //			e.printStackTrace();
 //		}
 //	}
 	
 }
