 /** ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
  *  +      Zuercher Hochschule angewandter Wissenschaften      +
  *  +                    Software Projekt 2                    +
  *  +                                                          +
  *  +        Gruppe 10: Miro Ljubicic & Mathias Weigert        +
  *  ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
  */
 package tools.decrypt;
 
 import java.io.File;
import java.util.Map;
 
 import krypto.io.LoadTextFile;
 import krypto.tools.decrypt.FrequencyAnalyzer;
 
 /**
  * @author Mathias Weigert & Miro Ljubicic
  * @version 1.0
  */
 public class TestFrequencyAnalyzer {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
 		LoadTextFile ltf = new LoadTextFile();
		File file = new File("C:\\Users\\Bunny\\workspace\\Algohol\\doc\\Test.txt");
 		ltf.loadFile(file);
 		FrequencyAnalyzer fa = new FrequencyAnalyzer();
		fa.countItems(ltf.getStr().toUpperCase());
 		fa.calculateFrequency();
		
		String abc ="ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		Map <Character, Integer> amountMap = fa.getAmountMap();
		Map <Character, Double> freqMap = fa.getFrequencyMap();
		System.out.println(ltf.getStr());
		for (int i=0; i <= 25; i++) {
			Character bst = abc.charAt(i);
			System.out.println(bst + "- Anzahl " + amountMap.get(bst) + " Frequenz: " + freqMap.get(bst));
		}
 	}
 
 }
