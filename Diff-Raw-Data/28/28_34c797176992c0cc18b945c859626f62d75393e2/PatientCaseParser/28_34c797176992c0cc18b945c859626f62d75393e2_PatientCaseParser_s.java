 package patientCase;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.util.ArrayList;
 
 public class PatientCaseParser {
 
 	public PatientCaseParser() {
 
 	}
 
 	public PatientCase getCase(String caseName) {
 		PatientCase patientCase;
 		String text = readCase(caseName);
 		patientCase = new PatientCase(caseName, getTextLines(text));
 		return patientCase;
 	}
 
 	private ArrayList<String> getTextLines(String fullText) {
 		String[] lines = fullText.split("\\. ");
 		ArrayList<String> temp = new ArrayList<String>();
 		temp.add(lines[0]);
 		for (int i = 1; i < lines.length; i++) {
 			if (lines[i].length() > 0) {
 				int firstLetter = lines[i].charAt(0);
 				// check if the next letter start with uppercase
 				if ((firstLetter <= 90 && firstLetter >= 65)
 						|| firstLetter == 197 || firstLetter == 198
 						|| firstLetter == 216) {
 					temp.add(lines[i]);
 				} else {
 					temp.set(temp.size() - 1, temp.get(temp.size() - 1) + ". "
 							+ lines[i]);
 				}
 			}
 		}
 		return temp;
 	}
 
 	private static String readCase(String caseName) {
 		String caseText = "";
 		String line = "";
 		try {
 			BufferedReader bReader = new BufferedReader(new FileReader("Cases/"
 					+ caseName + ".txt"));
 			do {
 				if (!line.isEmpty()) {
 					while (line.charAt(line.length() - 1) == ' '
 							| line.charAt(line.length() - 1) == '\t') {
 						line = line.substring(0, line.length() - 1);
 					}
 					if (line.charAt(line.length() - 1) != '.') {
 						line += ".";
 					}
 					line += " ";
 				}
 				caseText += line;
 				line = bReader.readLine();
 			} while (line != null);
 			bReader.close();
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return caseText;
 	}
 }
