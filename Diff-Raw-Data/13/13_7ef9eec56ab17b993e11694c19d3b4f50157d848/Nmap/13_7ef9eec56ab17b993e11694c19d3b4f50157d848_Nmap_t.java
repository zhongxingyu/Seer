 package com.emn.fil.automaticdiscover.nmap;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Scanner;
 
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.stereotype.Component;
 
 import com.emn.fil.automaticdiscover.dto.IPMask;
 import com.emn.fil.automaticdiscover.parser.SaxHandler;
 
 /** Classe qui permet d'utiliser la commande Nmap. 
  * 
  * @author Clement
  *
  */
 @Component("nmap")
 public class Nmap {
 
 	/** Chemin vers l'application nmap. */
 	@Value("${chemin_nmap}")
 	private String cheminNmap;
 	
 	/** Chemin où on enregistre le fichier xml. */
	private static final String cheminEnregistrementFichier = ".\resultat.xml";
 	
 	/** Méthode permettant de scanner le réseau indiqué.
 	 * 
 	 * @param ipMask est l'ip voulue
 	 * @throws IOException
 	 */
 	public void scanner(IPMask ipMask) throws IOException{
 		StringBuilder commandeNmap = new StringBuilder();
 		commandeNmap.append(cheminNmap);
 		commandeNmap.append(" " + OptionsNmap.OS_DETECTION.getCommande());
 		commandeNmap.append(" " +OptionsNmap.HOST_NAME_DETECTION.getCommande());
 		commandeNmap.append(" " +OptionsNmap.PING.getCommande());
		commandeNmap.append(" " +OptionsNmap.EXPORT_XML.getCommande());
		commandeNmap.append(" " + cheminEnregistrementFichier);
 		commandeNmap.append(" " + ipMask);
 		Process processNmap = Runtime.getRuntime().exec(commandeNmap.toString());
 		Scanner scannerNmap = new Scanner(processNmap.getInputStream());
 		while(scannerNmap.hasNext()){
 			System.out.println(scannerNmap.nextLine());
 		}
 		scannerNmap.close();
 		processNmap.destroy();
 		parsageResultat();
 	}
 	
 	public static void parsageResultat(){
 		try{
 			SAXParserFactory usine = SAXParserFactory.newInstance();
 			SAXParser parseur =usine.newSAXParser();
 
 			File fichier =new File(".\\resultat.xml");
 			SaxHandler gestionnaire =new SaxHandler();
 			parseur.parse(fichier, gestionnaire);
 			
 			System.out.println("\nRésultat :\n");
 			System.out.println("hostname : "+gestionnaire.getHostname());
 			System.out.println("os : "+gestionnaire.getOs());
 			
 		}catch (Exception e) {
 			System.out.println("Erreur avec le parsage XML");
 		}
 	}
 }
