 package Principal;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Scanner;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.xml.transform.*;
 import javax.xml.transform.stream.*;
 
 import org.jdom2.Document;
 import org.jdom2.JDOMException;
 import org.jdom2.input.SAXBuilder;
 import org.jdom2.output.Format;
 import org.jdom2.output.XMLOutputter;
 
 import Acces_BD.Requete;
 import IOFichiers.EcritureFichier;
 import IOFichiers.LectureFichier;
 
 public class Menu {
 	
 	/**
 	 * Extrait les information contenu dans les fichier Tex contenu dans le dossier demande par le programme, les noms des fichiers doivent etre de la forme :
 	 * 		Detail_Composition_Filiere_Promo.tex
 	 * Apres l'extraction on transforme ces fichiers en XML, et avec un fichier XSL definit, on transforme le XML de chaque fichier TEX en fichier TXT contenant des requete SQL
 	 * Et on execute les requetes
 	 * Le programme peut prendre des arguments qui avec l'option -p ou --p dans ce cas on passe les chemins de tous les dossiers à ajouter
 	 * @param args
 	 * @throws TransformerException
 	 */
 	public static void main(String[] args) throws TransformerException {
 		File rep = null;
 		//le parametre pour arreter le programme
 		boolean quitter = false;
 		//recupération du nom de fichier
 		rep = readKeyb();
		quitter = (rep == null);	
 		//tant qu'on a encore des dossier que l'utilisateur veut traiter
 		while (!quitter){
 			String doss = rep.getAbsolutePath();
 			//chargement de tous les fichier du dossier 
 			String [] listefichiers = rep.list();
 			//application de la transformation au fichiers TEX
 			try {
 				transform(doss, listefichiers); //on transforme les fichiers TEX en Requete SQL
 				System.out.println("\n*Execution des Requetes contenu dans le repertoir SQL/ ...");
 				System.out.println("********************** Shell Console **********************");
 				//execution des requtes generee 
 				try {
 					System.out.println(Requete.execRequests("SQL/"));
 				} 
 				catch (IOException e) { System.err.println(e.getMessage());}
 				catch (InterruptedException e) { System.err.println(e.getMessage());}
 				System.out.println("***********************************************************");
 				System.out.println("*Fin de tache : Execution Requete");
 				//sinon une erreur
 			} catch (Exception e1) {
 				e1.printStackTrace();
 				return;
 			}
 			
 			//lecture du prochain dossier
 			System.out.println("\n\n*******************");
 			rep = readKeyb();
 			quitter = (rep == null);
 		}
 		
		System.out.println("Fin du Traitement. Exit.");
 	}
 	
 	/**
 	 * Permet de retourner le chemin d'un Dossier existant à partir de saisie de l'utilisateur
 	 * @return : chemin du dossier de fichier Tex
 	 */
 	public static File readKeyb(){
 		System.out.println("Veuillez entrer le chemin du dossier contenant \n" +
 		"les fichiers de type Detail_Composition_Filiere_Promo.tex (e.g. : /home/dossier/). \n Pour Quitter : q");
 		File rep = null;
 		String doss = "";
 		Scanner sc = new Scanner(System.in);
 		doss = sc.nextLine();
 		//parametre qui permet de quitter la partie
 		boolean quitter = doss.trim().matches("q");
 		if(!doss.matches("(.*)/"))
 			doss += "/";
 		rep = new File(doss);
 		
 		//Vérification de l'existence du répertoire passé
 		while( !verifRep(rep) && !quitter ){
 			System.out.println("Dossier innexistant. Veuillez entrer un autre chemin.\n Pour Quitter : q");
 			sc = new Scanner(System.in);
 			doss = sc.nextLine();
 			quitter = doss.trim().matches("q");
 			if(!doss.matches("(.*)/"))
 				doss += "/";
 			rep = new File(doss);
 		}
 		if(quitter)
 			return null;
 		return rep;
 	}
 	
 	/**
 	 * Vérifie si le path fait référence un dossier existant ou non 
 	 * @param path
 	 * @return true si dossier existant, false sinon
 	 */
 	public static boolean verifRep (File path){
 		return path.isDirectory();
 	}
 
 	
 	/**
 	 * Permet de transformer les fichiers Tex contenu dans listefichiers en fichier XML et ensuite appliquer la transformation grace au 
 	 * fichier XSL definit dans le dossier du projet pour avoir des fichier .sql avec contenant des requetes 
 	 * @param doss
 	 * @param listefichiers
 	 * @return
 	 * @throws Exception 
 	 */
 	private static void transform (String doss ,String [] listefichiers) throws Exception{
 		//verification de synthax de chemin 
 		if(!doss.matches("(.*)/"))
 			doss += "/";
 		Pattern extract = Pattern.compile("^Detail_(.+)_(.+)_(.+)\\.{1}(tex){1}$"); // pour l'extraction de la composition, filiere et promotion
 		Matcher intitule;
 		String compo = "";
 		String filiere = "";
 		String promo = "";
 		File lefichier;
 		StreamSource ssXML; //fichier xml 
 		StreamResult ssSQL; //fichier sql de sortie 
 		//Chargement du fichier XSL 
 		File xsl = new File ("transf_requete.xsl");
 		//si le fichier xsl n'existe pas, on arrete l'operation
 		if(! xsl.exists())
 			throw new Exception("Erreur FATAL : Fichier XSL \"transf_requete.xsl\" n'existe pas, Veuillez le placer à la racine du projet !");
 		StreamSource ssXSL = new StreamSource("transf_requete.xsl");
 		TransformerFactory tFactory = TransformerFactory.newInstance();
 		//chargement du fichier XSL
 		Transformer transformer = tFactory.newTransformer(ssXSL);
 		//pour gérer les erreurs de transformation
 		ErrorListener err = transformer.getErrorListener();
 		//une liste contenant les sous-dossier et permettre de traiter tous les fichiers des sous dossier
 		HashSet <File> sousDossier = new HashSet <File>();
 		
 		for(int i = 0; i < listefichiers.length; i++) {
 			lefichier = new File (doss+listefichiers[i]);
 			//si c'est un dossier on refait la meme procedure
 			if(lefichier.isDirectory())
 				sousDossier.add(lefichier);
 			//si c'est un fichier 
 			else{
 				// fichier de la forme :  Detail_compo_filiere_promo.tex
 				if(listefichiers[i].matches("^Detail_(.+)_(.+)_(.+)\\.{1}(tex){1}$")){ 
 					LectureFichier LF = new LectureFichier(doss+listefichiers[i]);
 					//extraire la composition, filiere et promo
 					intitule = extract.matcher(listefichiers[i]);
 					if(intitule.find()){
 						compo = intitule.group(1);
 						filiere = intitule.group(2);
 						promo = intitule.group(3);
 						//extraire tous les modules du fichier tex
 						String module[] = LF.lireModule();
 						System.out.print("Traitement "+listefichiers[i]+" ...");
 						String sortie = "XML/"+listefichiers[i].substring(0,+listefichiers[i].length()-4)+".xml";
 						//Initialisation de l'outils d'ecriture de la transformation en XML 
 						EcritureFichier EF = new EcritureFichier(sortie);
 						EF.ecrire("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
 						EF.ecrire("<livret>\n\n");
 						//ecriture de chaque module en xml
 						Pattern p = Pattern.compile("\\s?\\\\?&\\s?");
 						Matcher m;
 						for(int j = 0;j < module.length; ++j) {
 							EF.ecrire("\t\t<!--%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Module n°"+(j+1)+" %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%-->\n\n"); 
 							// changement de tous les & qui n'entre pas dans une chaine de caractere par des &amp;
 							m = p.matcher(module[j]);
 							if(m.find())
 								module[j] = m.replaceAll(" &amp; ");
 							//transformation du module j en xml
 							EF.ecrireModuleXML(module[j]);
 							System.out.print("module "+(j+1)+" done .... ");
 						}
 						EF.ecrire("</livret>");
 						//Fin ecriture du fichier XML
 						EF.fermer();
 						System.out.println(""); //saut de ligne console
 						
 						//XML Pretty Format
 						prettyFormat ("XML/"+listefichiers[i].substring(0,listefichiers[i].length()-4)+".xml");
 						
 						//chargement du contenu XML 
 						ssXML = new StreamSource("XML/"+listefichiers[i].substring(0,listefichiers[i].length()-4)+".xml");
 						//Preparation du Stream de sortie pour la transfromation
 						ssSQL = new StreamResult("SQL/"+listefichiers[i].substring(0,listefichiers[i].length()-4)+".sql");
 						
 						//on ajoute les valeurs des parametres contenant la composition, la filiere et la promo 
 						transformer.setParameter("compo", compo);
 						transformer.setParameter("filiere", filiere);
 						transformer.setParameter("promo", promo);
 						
 						//CREATION de fichier de requete du fichier traite grace au fichier au fichier XSL
 						try{
 							transformer.transform(ssXML, ssSQL);
 							System.out.println("\t*****Fichier SQL Créé.****");
 						}catch(TransformerException e){
 							System.err.println("\n****Exception Transformation XSL+XSML => SQL :");
 							System.err.println("Rapport Erreur : =================================================");
 							System.err.println("  => From file XML : "+listefichiers[i]);
 							err.error(e);
 							System.err.println(err.toString());
 							System.err.println("==================================================================");
 							e.printStackTrace();
 							return;
 						}
 					}else
 						System.err.println("Menu::transform(String doss ,String [] listefichiers) Erreur => impossible de trouver la composition, filiere et promotion dans le fichier "+listefichiers[i]);	
 				}
 			}
 		}
 		//traitement des sous dossier 
 		if(! sousDossier.isEmpty()){
 			File ssRep = null; //les sous repertoires
 			Iterator <File> iterator = sousDossier.iterator();
 			//parcour de tous les sous repertoires
 			while (iterator.hasNext()){
 				ssRep = iterator.next();
 				System.out.println("Traitement du sous dossier : "+ssRep.toString());
 				transform(ssRep.getAbsolutePath(), ssRep.list());
 			}
 		}
 	}
 	
 	private static void prettyFormat (String path) throws JDOMException, IOException{
 		File fichier = new File (path);
 		if(fichier.exists() && fichier.isFile()){
 			SAXBuilder reader = new SAXBuilder ();
 			Document doc = reader.build(fichier);
 			try{
 				XMLOutputter sortie = new XMLOutputter(Format.getPrettyFormat());
 				sortie.output(doc, new FileOutputStream (fichier));
 			}catch(Exception e){
 				e.printStackTrace();
 			}
 		}else
 			System.err.println("Menu::prettyFormat Erreur => fichier n'existe pas ou fichier invalide : "+path);
 	}
 }
