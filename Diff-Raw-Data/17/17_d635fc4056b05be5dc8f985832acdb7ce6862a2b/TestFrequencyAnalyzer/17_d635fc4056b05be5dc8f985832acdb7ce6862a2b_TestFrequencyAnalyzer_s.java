 /** ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
  *  +      Zuercher Hochschule angewandter Wissenschaften      +
  *  +                    Software Projekt 2                    +
  *  +                                                          +
  *  +        Gruppe 10: Miro Ljubicic & Mathias Weigert        +
  *  ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
  */
 package tools.decrypt;
 
 import java.io.File;
 
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
		File file = new File("D:\\Daten\\Dropbox\\Java Projekte\\EQ2 Logs\\Test_LOG.txt");
 		ltf.loadFile(file);
 		FrequencyAnalyzer fa = new FrequencyAnalyzer();
		fa.countItems(ltf.getStr().toLowerCase());
 		fa.calculateFrequency();
 	}
 
 }
