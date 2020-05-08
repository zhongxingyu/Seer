 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 
 public class Reorder {
 
 	/**
 	 * @param args
 	 * @throws IOException 
 	 */
 	
 	
 	public static void main(String[] args) throws IOException {
		File file1 = new File("tree.dev.en");
		File file2 = new File("reordered.dev.en");
 		File file3 = new File("change.log");
 		FileReader fileReader = new FileReader(file1);
 		FileWriter fileWriter2 = new FileWriter(file2);	
 		FileWriter fileWriter3 = new FileWriter(file3);
 		fileWriter3.close();
 		BufferedReader bufferedReader1 = new BufferedReader(fileReader);
 		String nextLine;
 		int i = 1;
 		while(true) {
 			String sentence = "";
 			while((nextLine = bufferedReader1.readLine()) != null) {
 			    if (nextLine.equals("")) {
 			       break;
 			    }
 			    sentence += nextLine;
 			}
 			if (sentence.equals("")) {
 			       break;
 			}
 			//sentence = sentence.replaceAll("\\s","");
 			fileWriter3 = new FileWriter(file3, true);
 			fileWriter3.write(i + ". \n");
 			fileWriter3.close();
 			//System.out.println(i+"\n"+sentence);
 			Sentence stns = new Sentence(i, sentence);
 			stns.rules(0);
 			//System.out.println(i+"\n"+stns.stns);		
 			fileWriter2.write(denest(stns) + "\n");
 			i++;
 		}
 		fileReader.close();
 		fileWriter2.close();		
 	}
 	
 	public static String denest(Sentence stns) {
 		String line = "";
 		int[] range;
 		for (int i = 0; i < stns.stns.length(); i++) {			
 			if (stns.stns.charAt(i) == '(') {
 				if ((range = stns.word(i)) != null) {
 					//System.out.println("add word from "+ range[0] + " to "+range[1]);
 					line += stns.stns.substring(range[0], range[1]);
 					line += " ";
 					//i = stns.match(i);
 					i = range[1];
 				}
 			}
 		}
 		line = line.replace("\\", "");
 		line = line.replaceAll("\\s+(?=\\p{Punct})", "");
 		return line.substring(0, line.length() - 1);
 		
 	}
 	
 
 }
