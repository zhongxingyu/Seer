 package IOFichiers;
 
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.util.Iterator;
 import java.util.TreeSet;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class EcritureFichier {
 	//pour donner un code Apogee par defaut au module n'ayant pas de code
 	static int iterApogee = 45;
 /**
  * Cette classe vise à simplifier l'écriture dans un fichier
  * Elle est aussi utilisée pour écrire le fichier XML
  */
 	private FileWriter fw;
 	private BufferedWriter output;
 	
 	/**
 	 * construit l'outils d'ecriture de la classe avec un nom de fichier et ecrase son contenu 
 	 * @param fic : le fichier qui sera ecraser
 	 */
 	public EcritureFichier(String fic){
 		try {
 			fw = new FileWriter(fic, false);
 			output = new BufferedWriter(fw);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Constructeur permettant d'ecrire sur la sortie standard 
 	 */
 	public EcritureFichier(){
 		output = new BufferedWriter(new OutputStreamWriter(System.out));
 	}
 	
 	/**
 	 * Ecrit dans le flux de sortie le contenu du parametre aEcrire 
 	 * @param aEcrire : le contenu a ecrire
 	 */
 	public void ecrire(String aEcrire){
 		try {
 			output.write(aEcrire);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Applique un push sur la flux de sortie
 	 */
 	public void envoyer(){
 		try {
 			output.flush();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Ferme le flux d'ecriture
 	 */
 	public void fermer(){
 		try {
 			output.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Permet de verifier que la chaine extraite est en majuscules
 	 * @param s : la chaine extraite
 	 * @return
 	 */
 	public boolean verifierMajuscules(String s){
 		return s.equals(s.toUpperCase());
 	}
 
 	/**
 	 * Verifie que la deuxieme lettre d'une chaine est en minuscule, 
 	 * ce qui permet de differencier les balises des attributs de <module>
 	 * @param s
 	 * @return
 	 */
 	public boolean enMinuscule(String s){
 		return Character.isLowerCase(s.charAt(1));
 	}
 
 	/**
	 * Ecriture de la balise <module> et de ses attributs
 	 * @param aExtraire
 	 * @return
 	 * @throws Exception 
 	 */
 	public boolean ecrireModule(String aExtraire) throws Exception{
 		
		if(aExtraire == null || aExtraire.length() == 0) // on fait rien si le String est 
 			return false;
 		ecrire("<module ");
 		Pattern p = Pattern.compile("codeApogee=\\{(.*)?\\}");
 		Matcher m = p.matcher(aExtraire);
 		String laMatiere="";
 		//extraction du code Apogee
 		if(m.find()){
 			String codeApogee = m.group(1);
 			if(!codeApogee.isEmpty()){
 				String [] spl = codeApogee.split(" ");
 				for(int i = 0; i < spl.length; i++){
 					if(apogee(spl[i])){
 						if(i == 0)
 							ecrire("codeApogee"+(i+1)+"=\""+spl[i]+"\"");
 						else
 							ecrire(" codeApogee"+(i+1)+"=\""+spl[i]+"\"");
 						//on prend un des codes apogee pour extraire
 						if(laMatiere.isEmpty())
 							laMatiere = spl[i];
 					}
 				}
 			}else{
 				ecrire("codeApogee1=\"UE"+iterApogee+"\"");
 				iterApogee++; //pour le prochain qui n'aura pas 
 			}
 		}else{
 			ecrire("codeApogee1=\"UE"+iterApogee+"\"");
 			iterApogee++;
 		}
 		ecrire(" matiere=\""+matiere(laMatiere)+"\"");
 		Pattern attributs = Pattern.compile("\n([A-Z]+)=\\{(.*)\\}");
 		Matcher ma = attributs.matcher(aExtraire);
 		String line = "";
 		String contenu = "";
 		while(ma.find()) {
 			line = ma.group(1);
 			contenu = ma.group(2);
 			//on extrait le numero correspondant au semestre
 			if(line.equals("SEMESTRE")){
 				Pattern giveNumber = Pattern.compile("([a-z]|[A-Z])*\\s*(\\d)");
 				Matcher leSemestre = giveNumber.matcher(contenu);
 				if(leSemestre.find())
 					ecrire(" "+line.toLowerCase()+"=\""+leSemestre.group(2)+"\"");
 			}else
 				ecrire(" "+line.toLowerCase()+"=\""+checkFormat(contenu)+"\"");
 		}
 		ecrire(">\n");
 		return true;
 	}
 	
 	/**
 	 * Vérifie la strucutre du code apogee les 3 cas de strucutre suivant sont accepté : 
 	 * cas 1 =>   SOM4AG06
 	 * cas 2 =>   2IF03
 	 * cas 3 =>   IF12
 	 * @param codeApogee
 	 * @return vrai si le code est bon, faux sinon
 	 */
 	private boolean apogee(String codeApogee){
 		if(!codeApogee.isEmpty()){
 			Pattern p = Pattern.compile("((([A-Z]{3}\\d)?)|\\d?)([A-Z]{2})\\d{1,2}");
 			Matcher m = p.matcher(codeApogee);
 			return m.matches();
 		}else
 			return false;
 	}
 	
 	/**
 	 * Extrait la matiere du code apogee comme définit sur la base de données
 	 * @param codeApogee
 	 * @return IND si la matiere est indefinit, le code de la matiere sur la base sinon
 	 */
 	private String matiere (String codeApogee){
 		if(!codeApogee.isEmpty()){
 			Pattern p = Pattern.compile("((([A-Z]{3}\\d)?)|\\d?)([A-Z]{2})\\d{1,2}");
 			Matcher m = p.matcher(codeApogee);
 			if(m.find()){
 				String laMatiere = m.group(4); //on extrait le quatrieme groupe en partant de la gauche dans l'expression reguliere
 				//nous verifions les code avant de retourner le code, car il existe plusiseurs codes faisant référence a la meme matiere exemple : IN et IF pour informatique
 				if(laMatiere.equals("IN")) //informatique
 					laMatiere = "IF";
 				else if(laMatiere.equals("MT")) //mathématique
 					laMatiere = "MA";
 				else if(laMatiere.equals("PP") || laMatiere.equals("ST") || laMatiere.equals("PR")) //projet
 					laMatiere = "PJ";
 				else if(laMatiere.equals("GE")) //Geologie
 					laMatiere = "GO";
 				else if(laMatiere.equals("UE")) //indefinit
 					laMatiere = "UND";
 				
 				return laMatiere;
 			}else
 				return "UND";
 		}else
 			return "UND";
 	}
 
 	/**
 	 * Ecrit les balises "normales"
 	 * @param aExtraire : le contenu de travail
 	 * @throws Exception 
 	 */
 	public void ecrireBalises(String aExtraire) throws Exception{
 		Pattern p = Pattern.compile("\n(.*)=\\{(.*)\\}");
 		Matcher m = p.matcher(aExtraire);
 		String balise = "";
 		String contenu = "";
 		//on extrait les lignes entre crochet et qui ne correspondent pas a des valeurs booleene ou langue, nbPrerequis
 		while (m.find()){
 			balise = m.group(1);
 			contenu = m.group(2);
 			if(!balise.equals("langue") && !(contenu.equals("true") || contenu.equals("false"))){
 				if(balise.charAt(0) == '%'){
 					ecrire("<"+balise.substring(1)+">"+checkFormat(contenu)+"</"+balise.substring(1)+">\n\n");
 				}
 				if(!verifierMajuscules(balise) && Character.isLetterOrDigit(balise.charAt(0))){
 					balise = balise.toLowerCase();//Pour afficher la balise ouvrante correctement
 					ecrire("<"+balise+">"+checkFormat(contenu) + "</"+balise+">\n\n");
 				}
 			}
 		}
 		
 	}
 
 	/**
 	 * Ecrit les balises composees (comme langues )
 	 * @param aExtraire
 	 * @throws Exception 
 	 */
 	public void ecrireBalisesComposees(String aExtraire) throws Exception{
 		//Recupere toutes les lignes entre emailSecondResp (exclut) et descriptionCourte (incluses) 
 		Pattern pp = Pattern.compile("(langue=\\{)(.*)(\\},)");
 		Matcher mm = pp.matcher(aExtraire);
 		if(mm.find()){
 			String [] lines = mm.group().split("(\\s*),(\\s*)"); //On découpe en plusieurs chaines, suivant les lignes
 			Pattern p = Pattern.compile("(.*)(={1})(\\{)(.*)(\\})"); //On decoupe chaque ligne afin de pouvoir recuperer
 			Matcher m;
 			String gauche = ""; //la partie gauche de la ligne, c-a-d le nom de variable si on peut l'appeler ainsi
 			String droite = ""; //idem
 			//On traite toutes les lignes à l'exception de la premiere et de la derniere
 			for(int i=0;i<lines.length;i++){
 				m = p.matcher(lines[i]);     //le nom de variable et la/les valeurs qu'elle contient
 				if(m.find()){
 					gauche = m.group(1);
 					droite = m.group(4);
 					String [] result = droite.split(",");
 					if(gauche.equals("langue")) {
 						ecrire("<"+gauche+"s>\n");	//On ecrit la balise generale ouvrante
 						//(nom de l'attribut + s; exemple : "langue" devient "langues")
 						for(int j=0;j<result.length;j++)//On ecrit les balises internes avec leurs valeurs
 							ecrire("  <"+gauche+">"+checkFormat(result[j])+"</"+gauche+">\n");
 						ecrire("</"+gauche+"s>\n\n"); //On ecrit la balise generale fermante
 					}
 				}else
 					throw new Exception ("=> Erreur : Impossible de trouver les lignes correspondantes au langues");
 			}
 		}else
 			throw new Exception ("=> Erreur : impossible de trouver les langues ! ");
 	}
 
 	/**
 	 * Ecrit les dernieres balises qui correspondent aux balises description Courte et Longue, prerequis, ressources et biblio
 	 * @param aExtraire
 	 * @throws Exception 
 	 */
 	public void ecrireBalisesTexte(String aExtraire) throws Exception{
 		Pattern pa = Pattern.compile("(\\]){1}(\\s+)(((%.*)*|(\\s*\\{\\s*)|(.*\\w*.*)|(\\s*))*)(\\\\vfill){1}"); //Pour recupere la zone concernee
 		Matcher ma = pa.matcher(aExtraire);
 		if(ma.find()){
 			String [] parties = ma.group().split("(\\}{1}\\s*(%.*)*\\s*\\{{1})"); //on récupere les bloques des differentes partie en prend "} %commentaire {" comme séparateur 
 			for(int i = 0; i < parties.length; i++) {
 				switch(i){
 				case 0 : //bloc de description courte
 					ecrire("<descriptionCourte>");
 					String [] spl  = parties[i].split("\\{");
 					if(spl.length > 1)
 						ecrire(checkFormat(spl[1]));
 					ecrire ("</descriptionCourte>\n\n");break;
 				case 1 : //bloc de description longue
 					ecrire("<descriptionLongue>");
 					ecrire(checkFormat(parties[i]));
 					ecrire("</descriptionLongue>\n\n");
 					break;
 				case 2 : //bloc de prerequis
 					ecrire("<textePrerequis>");
 					ecrire(checkFormat(parties[i]));
 					ecrire("\n</textePrerequis>\n\n");
 					break;
 				case 3 : //bloc d'objectifs
 					ecrire("<objectifs>");
 					ecrire(checkFormat(parties[i]));
 					ecrire("</objectifs>\n\n");
 					break;
 				case 4 : //bloc de ressources
 					ecrire ("<ressources>");
 					ecrire(checkFormat(parties[i]));
 					ecrire("\n</ressources>\n\n");
 					break;
 				case 5 : //bloc de biblio
 					ecrire("<biblio>");
 					String[] spl1 = parties[i].split("\\}");
 					ecrire(checkFormat(spl1[0]));
 					ecrire("\n</biblio>\n\n");
 					break;
 				}
 			}
 		}else
 			throw new Exception ("=> Erreur : impossible de trouver les balise de description, texte Prerequis, objectifs, ressources, et biblio ! ");
 	}
 	
 	/**
 	 * Vérifie la strucutre du bloc, gère la mise en forme des enumerate, itemize et ObjItem des fichier TEX
 	 * @param bloc texte
 	 * @return texte mise en forme en supprimant les symbole des mise en forme de fichier tex
 	 * @throws Exception 
 	 */
 	private static String checkFormat (String bloc) throws Exception{
 		String[] spl = null;
 		StringBuffer result = new StringBuffer();
 		Pattern pattern;
 		Pattern format;
 		Matcher m;
 		String [] portions;
 		//enumeration
 		if(bloc.contains("\\begin{enumerate}") && bloc.contains("\\end{enumerate}")){
 			//on casse le bloc en trois parties : avant "begin", apres et apres "end"
 			pattern = Pattern.compile("\\\\begin\\{enumerate\\}|\\\\end\\{enumerate\\}");
 			portions = pattern.split(bloc);
 			if(portions.length > 0){
 				//partie a l'interieur entre "begin" et "end" on la met en forme 
 				for(int i = 0; i < portions.length ; i++){
 					if((i+1)%2 == 0){
 						spl = portions[i].split("\\\\item");
 						//écrit les lignes avec leurs numerotation
 						for(int j = 1; j < spl.length; j++)
							result.append(i+"- "+spl[j]);
 					}else
 						result.append(portions[i]);
 				}
 			}else
 				throw new Exception ("Erreur EcritureFichier::checkFormat(String bloc) => enumerate erreur ***Bloc :\n"+bloc);
 		//itemize
 		}else if(bloc.contains("\\begin{itemize}") && bloc.contains("\\end{itemize}")){
 			//on casse le bloc en trois parties : avant "begin", apres et apres "end"
 			pattern = Pattern.compile("\\\\begin\\{itemize\\}|\\\\end\\{itemize\\}");
 			portions = pattern.split(bloc); //on recupere les differentes parties
 			if(portions.length > 0){
 				for(int i = 0; i < portions.length ; i++){
 					//partie a l'interieur entre "begin" et "end" on la met en forme 
 					if((i+1)%2 == 0){
 						format = Pattern.compile("\\\\item|\\\\ObjItem");
 						m = format.matcher(portions[i]);
 						result.append(m.replaceAll("*"));
 					//le reste on l'ecrit tel quel
 					}else
 						result.append(portions[i]);
 				}
 			}else
 				throw new Exception ("Erreur EcritureFichier::checkFormat(String bloc) => itemize erreur ***Bloc :\n"+bloc);
 		//s'il s'agit d'une description
 		}else if(bloc.contains("\\begin{description}") && bloc.contains("\\end{description}")){
 			//meme chose 
 			pattern = Pattern.compile("\\\\begin\\{description\\}|\\\\end\\{description\\}");
 			portions = pattern.split(bloc);
 			if(portions.length > 0){
 				for(int i = 0; i < portions.length; i++){
 					//partie a l'interieur entre 'begin' et 'end' on la met en forme
 					if((i+1)%2 == 0){
 						format = Pattern.compile("\\\\item\\[(.*)\\]");
 						m = format.matcher(portions[i]);
 						//garder l'ordre des elements entre parenthese de la regexp pour les mettres a la place de \item[...]
 						TreeSet <String> listeOrdre = new TreeSet <String> ();
 						//on stocke les elements un a un
 						while(m.find())
 							listeOrdre.add(m.group(1));
 						//si on trouve 
 						if(!listeOrdre.isEmpty()){
 							Iterator <String> ordre = listeOrdre.iterator();
 							String str = m.replaceFirst(ordre.next()+":");
 							while(ordre.hasNext()){
 								m = format.matcher(str);
 								str = m.replaceFirst(ordre.next()+":");
 							}
 							result.append(str);
 						}else
 							throw new Exception("Erreur EcritureFichier::checkFormat(String bloc) => description erreur dans la partie : \n"+portions[i]+"\n***Bloc :\n"+bloc);
 					}else
 						result.append(portions[i]);
 				}
 			}else
 				throw new Exception("Erreur EcritureFichier::checkFormat(String bloc) => description erreur ***Bloc : \n"+bloc);
 		}
 		
 		if(result.length()!=0)
 			bloc = result.toString();
 		//on remplace tout les ' par des \'
 		Pattern simpleCote = Pattern.compile("(?!\\\\)'");
 		Matcher findCotes = simpleCote.matcher(bloc);
 		bloc = findCotes.replaceAll("\\\\'");
 		//si on a plusieurs backslash on les supprime et on laisse un seul de sorte a avoir \'
 		simpleCote = Pattern.compile("\\\\{2}'");
 		findCotes = simpleCote.matcher(bloc);
 		bloc = findCotes.replaceAll("\\\\'");
 		return bloc;
 	}
 
 	
 	/**
 	 * Applique toute les extraction pour construire le fichier XML en entier et ecrit le tous sur flux de sortie
 	 * @param aExtraire : le contenu sur le quel on travail
 	 * @throws Exception 
 	 */
 	public void ecrireModuleXML(String aExtraire) throws Exception{
 		ecrireModule(aExtraire);
 		ecrireBalises(aExtraire);
 		ecrireBalisesComposees(aExtraire);
 		ecrireBalisesTexte(aExtraire);
 		ecrire("</module>\n\n");
 		envoyer();
 	}
 	
	
	//recupération des resultats de différentes parties pour tester les résultats 
	public static void main (String [] args) throws Exception{
		String s1 = "\\'avion a décolé ce matin l'arbre est tombé d'un coup j'ai rien compris !";
		System.out.println(checkFormat(s1));
	}
 }
