 package files;
 
 import geneticAlogrithm.Formant;
 import geneticAlogrithm.FormantSequence;
 import geneticAlogrithm.GeneticAlgorithmCall;
 import geneticAlogrithm.Sequence;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 
 public class FileGestion {
 	private static FileWriter f;
 	private static BufferedWriter output;
 	
 	public static String writePraatScriptHeader(){
 		StringBuilder stb= new StringBuilder(); 
 		stb.append("#-----------------------------------------------\n");
 		stb.append("# Project : Software synthesis using GA\n");
 		stb.append("# Hervo Pierre-Yves, automatic Script generated in java\n");
 		stb.append("#-----------------------------------------------\n");
 		stb.append("Erase all\n");
 		stb.append("#-----------------------------------------------\n");
 		stb.append("Create Speaker... Robovox Female 2\n"); //nom genre typeDeGlottis
 		stb.append("Create Artword... phon 0.5\n");//creation of actions list(init)
 		stb.append("#-----------------------------------------------\n");
 		return stb.toString();
 	}
 
 	public static String writePraatScriptAsCandidates(Sequence candidat){
 			StringBuilder stb= new StringBuilder(); //utilisation de stringBuilder car je suppose que pas synchroniser
 			stb.append("#-----------------------------------------------\n");
 			stb.append("# Project : Software synthesis using GA\n");
 			stb.append("# Hervo Pierre-Yves, automatic Script generated in java\n");
 			stb.append("#-----------------------------------------------\n");
 			//we complete artword with the set tqrget methods below
 			stb.append("select Artword phon\n");
 			stb.append("# Supply lung energy\n");
 			stb.append("#-----------------------------------------------\n");
 			stb.append("Set target... 0.00    0.1 Lungs\n"); //"+candidat.getValuesAt(0)+"
 			stb.append("Set target... 0.03    0.0 Lungs\n");
 			stb.append("Set target... 0.5     0.0 Lungs\n");
 			stb.append("#-----------------------------------------------\n");
 			stb.append("# Control glottis\n");
 			stb.append("#-----------------------------------------------\n");
 			stb.append("#Glottal closure\n");
 			stb.append("Set target... 0.0   0.5 Interarytenoid\n");
 			stb.append("Set target... 0.5   0.5 Interarytenoid\n");
 			stb.append("#\n");
 			stb.append("# Adduct vocal folds\n");
 			stb.append("Set target... 0.0   1.0 Cricothyroid\n");
 			stb.append("Set target... 0.5   1.0 Cricothyroid\n");
 			stb.append("# Close velopharyngeal port\n");
 			stb.append("#-----------------------------------------------\n");
 			stb.append("Set target... 0.0   1.0 LevatorPalatini\n");
 			stb.append("Set target... 0.5   1.0 LevatorPalatini\n");
 			stb.append("#-----------------------------------------------\n");
 			stb.append("# Shape mouth to open vowel\n");
 			stb.append("#-----------------------------------------------\n");
 			stb.append("# Lower the jaw\n");
 			stb.append("# -----------------------------------------\n");
 			stb.append("Set target... 0.0   -0.4 Masseter\n");
 			stb.append("Set target... 0.5   -0.4 Masseter\n");
 			stb.append("# Pull tongue backwards\n");
 			stb.append("Set target... 0.0   0.4 Hyoglossus\n");
 			stb.append("Set target... 0.5   0.4 Hyoglossus\n");
 			stb.append("# Synthesise the sound\n");
 			stb.append("#-----------------------------------------------\n");
 			stb.append("select Artword phon\n");
 			stb.append("plus Speaker Robovox\n");
 			stb.append("To Sound... 22050 25   0 0 0    0 0 0    0 0 0\n");
 			stb.append("#-----------------------------------------------\n");
 			stb.append("#-----------------------------------------------\n");
 			stb.append("# Automatic data extraction part\n");
 			stb.append("To Formant (burg)... 0 3 5500 0.025 50\n");
 			stb.append("numberOfFormant = Get number of formants... 1\n");
 			stb.append("time = Get total duration\n");
 			stb.append("midTime = time/2\n");
 			stb.append("for intervalNumber from 1 to numberOfFormant \n");
 			stb.append("meanFreq = Get mean... intervalNumber 0 0 \"Hertz\"\n");
 			stb.append("bandWith = Get bandwidth at time... intervalNumber midTime \"Hertz\" \"Linear\"\n");
 			stb.append("endfor\n");
 			stb.append("writeInfoLine(midTime)\n");
 			//stb.append("execute C:/Users/Py/workspace/ProjetSpeechSynthesis/script2\n");
 			stb.append("sendsocket localhost:2009 0 1 2 3 4 5\n");
 			//System.out.println(stb);
 		return stb.toString();
 	}
 	
 	public static void writeInTheLogs(String stringBuffer){ 
 		String adresseDuFichier = "/Users/py/workspace/ProjetSpeechSynthesis/log.txt";
 		try {
 			f= new FileWriter(adresseDuFichier, true); //modifier ici pr le append
 			output= new BufferedWriter(f);
 			output.write(stringBuffer);
 			output.write("\n");
 			output.flush();
 			output.close();
 			f.close();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} 
 	}
 	
 	public static FormantSequence splitChaineToFormantSequence(String chaine){
 		/*function to parse the string i get with praat to a formantSequence*/
 		/*y a peu etre des souccis ici a voir lors des tests*/
 		String[] tab= chaine.split(" ");
 		FormantSequence fms= new FormantSequence("candidat", 3, new ArrayList<Formant>());
 		if(tab.length>1){
 			Formant f1= new Formant();
 			Formant f2= new Formant();
 			Formant f3= new Formant();
 			f1.setFrequency(Integer.parseInt(tab[0]));
 			f2.setFrequency(Integer.parseInt(tab[1]));
 			f3.setFrequency(Integer.parseInt(tab[2]));
 			f1.setBandwith(Integer.parseInt(tab[3]));
 			f2.setBandwith(Integer.parseInt(tab[4]));
 			f3.setBandwith(Integer.parseInt(tab[5]));
 			fms.getList().add(f1);
 			fms.getList().add(f2);
 			fms.getList().add(f3);
 		}//else its "FIN" so dont do anything
 		return fms;
 	}
 }
