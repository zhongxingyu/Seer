 import java.lang.Exception;
 import java.util.Arrays;
 import java.util.StringTokenizer;
 import java.sql.Date;
 import java.net.URL;
 import java.net.MalformedURLException;
 import java.lang.Object;
 import java.lang.StringBuffer;
import java.util.Vector;
 import java.io.FileReader;
 import java.io.BufferedReader;
 import java.io.IOException;
 
 /**
  * @class Charger
  * @brief Gestion des charges des informations de toutes les recettes
  */
 public class Charger{
 	
 	/**
 	 * Chemin vers l'index.
 	 */
 	private final String Index = "data/index_livres.txt";
 	
 	/**
 	 * Chemin vers les commentaires.
 	 */
 	private final String Com = "data/commentaire.txt";
 	
 	/**
 	 * Chemin vers les url.
 	 */
 	private final String Url = "data/url.txt";
 	
 	/**
 	 * Chemin vers les préparations.
 	 */
 	private final String Preparation = "data/preparation.txt"; // texte avec les préparations
 	
 	/**
 	 * Choix de la catégorie.
 	 * 
 	 * (salé ou sucré)
 	 */
 	private final String choixduPeuple;
 		
 	/**
 	 * Constructeur de la class Charger.
 	 * 
 	 * @param s
 	 * 		Le choix du fichier de recettes.
 	 */
 	public Charger(String s){
 		this.choixduPeuple = "data/"+s+".txt";
 	}
 	
 	/**
 	 * Charge les indexs dans un tableau.
 	 * 
 	 * @return Un tableau avec les livres utilisés dans l'index.
 	 */
 	public Vector<String> index ()
 	{
 		try
         {
             String line = null ;
 
 	   // Lecture
             BufferedReader input = new BufferedReader(new FileReader(Index));
             try
             {
 				Vector<String> listIndex = new Vector<String>();
 		// Pour toute ligne lue
                 while ((line = input.readLine()) != null)
                 {
 					if (nbIndex < 10)
 					{
 						StringTokenizer st = new StringTokenizer(line,".  ");
 						st.nextToken(); // récupère l'indice, que nous n'utiliserons pas car les indices sont croissants
 						listIndex.add(st.nextToken()); // récupère la chaîne de l'index et la met dans un Vector
 						nbIndex++;
 					}
 					else
 					{
 						StringTokenizer st = new StringTokenizer(line,". ");
 						st.nextToken(); // récupère l'indice, que nous n'utiliserons pas car les indices sont croissants
 						listIndex.add(st.nextToken()); // récupère la chaîne de l'index et la met dans un Vector
 						nbIndex++;
 					}
 				}
             }
             finally // si jamais il y a un problème, on passe tout de même ici
             {
                 input.close() ;
             }
         }
         catch (IOException e)
         {
             e.printStackTrace() ;
         }
 	
 	return listIndex;
 	}
 	
 	/**
 	 * Charge les commentaires dans un tableau.
 	 * 
 	 * @return Un tableau avec les commentaires dans l'ordre des indices.
 	 */
 	public Vector<String> com () // je prends comme convention qu'il n'y a qu'un espace entre "chiffre." et le commentaire (à la différence de l'index)
 	{
 		try
         {
             String line = null ;
 
 	   // Lecture
             BufferedReader input = new BufferedReader(new FileReader(Com));
             try
             {
 				Vector<String> listCom = new Vector<String>();
 		// Pour toute ligne lue
                 while ((line = input.readLine()) != null)
                 {
 					StringTokenizer st = new StringTokenizer(line,". ");
 					st.nextToken(); // récupère l'indice, que nous n'utiliserons pas car les indices sont croissants
 					listCom.add(st.nextToken()); // récupère la chaîne de l'index et la met dans un Vector
 					nbCom++;
 				}
             }
             finally // si jamais il y a un problème, on passe tout de même ici
             {
                 input.close() ;
             }
         }
         catch (IOException e)
         {
             e.printStackTrace() ;
         }
 	
 	return listCom;
 	}
 	
 	/**
 	 * Charge les préparations dans un tableau.
 	 * 
 	 * @return Un tableau avec les préparations dans l'ordre des indices.
 	 */
 	public Vector<String> prep () // de même que pour les commentaires
 	{
 		try
         {
             String line = null ;
 
 	   // Lecture
             BufferedReader input = new BufferedReader(new FileReader(Preparation));
             try
             {
 				Vector<String> listPrep = new Vector<String>();
 		// Pour toute ligne lue
                 while ((line = input.readLine()) != null)
                 {
 					StringTokenizer st = new StringTokenizer(line,". ");
 					st.nextToken(); // récupère l'indice, que nous n'utiliserons pas car les indices sont croissants
 					listPrep.add(st.nextToken()); // récupère la chaîne de l'index et la met dans un Vector
 					nbPrep++;
 				}
             }
             finally // si jamais il y a un problème, on passe tout de même ici
             {
                 input.close() ;
             }
         }
         catch (IOException e)
         {
             e.printStackTrace() ;
         }
 	
 	return listPrep;
 	}
 	
 	/**
 	 * Charge les URLs dans un tableau.
 	 * 
 	 * @return Un tableau avec les URLs dans l'ordre des indices.
 	 */
 	public Vector<String> url () // de même que pour les commentaires
 	{
 		try
         {
             String line = null ;
 
 	   // Lecture
             BufferedReader input = new BufferedReader(new FileReader(Url));
             try
             {
 				Vector<String> listUrl = new Vector<String>();
 		// Pour toute ligne lue
                 while ((line = input.readLine()) != null)
                 {
 					StringTokenizer st = new StringTokenizer(line,". ");
 					st.nextToken(); // récupère l'indice, que nous n'utiliserons pas car les indices sont croissants
 					listUrl.add(st.nextToken()); // récupère la chaîne de l'index et la met dans un Vector
 					nbUrl++;
 				}
             }
             finally // si jamais il y a un problème, on passe tout de même ici
             {
                 input.close() ;
             }
         }
         catch (IOException e)
         {
             e.printStackTrace() ;
         }
 	
 	return listUrl;
 	}
 	
 	/**
 	 * Charge les recettes (salé ou sucré) dans un tableau
 	 * 
 	 * @return Un tableau avec toutes les recettes.
 	 */
 	public Vector<Recette> recette ()
     {
         try
         {
             String line = null ;
 
 	   // Lecture
             BufferedReader input = new BufferedReader(new FileReader(choixduPeuple));
             try
             {
 				Vector<Recette> listRec = new Vector<Recette>();
 		// Pour toute ligne lue
                 while ((line = input.readLine()) != null)
                 { 
                     if (Charger.est_une_sous_cat(line)==true)
                     {
 						StringBuffer sb = new StringBuffer(line);
 						StringBuffer scat = new StringBuffer();
 						
 						for (int j = 2 ; sb.charAt(j)!=':' ;j++)
 						{
 							scat.append(sb.charAt(j));
 						}
 					}
 					
 					else if(Charger.est_une_recette(line)==true)
 					{
 						StringTokenizer st = new StringTokenizer(line,"- ");
 						String sanstiret = st.nextToken;
 						st = new StringTokenizer(sanstiret," (");
 						int passage=0;
 						while (hasMoreTokens())
 						{
 							if (passage==0)
 							{
 								String rec = st.nextToken().toString();
 								Recette r = new Recette(rec,choixduPeuple,scat.toString());
 								passage++;
 							}
 							else
 							{
 								String option = st.nextToken().toString();
 								StringBuffer sb = new StringBuffer(option);
 								sb.deleteCharAt(sb.length()-1);
 								switch (passage){
 									case 1 : if(sb.length>0)
 											{
 												int indexLivre = Integer.parseInt(sb.toString());
 												String livre = index().elementAt(indexLivre-1); // élément indice -1 du vector de la liste d'index
 												r.setLivre(livre);
 											}
 											passage++;
 											break;
 									
 									case 2 : if(sb.length>0)
 											{ 
 												int page = Integer.parseInt(sb.toString());
 												r.setPage(page);
 											}
 											passage++;
 											break;
 											
 									case 3 : if(sb.length>0)
 											{
 												int indexLien = Integer.parseInt(sb.toString());
 												String link = url().elementAt(indexLien-1); // élément indice -1 du vector de la liste d'Url
 												r.setLien(link);
 											}
 											passage++;
 											break;
 											
 									case 4 : if(sb.length>0)
 											{
 												int note = Integer.parseInt(sb.toString());
 												r.setNote(note);
 											}
 											passage++;
 											break;
 											
 									case 5 : if(sb.length>0)
 											{
 												int indexCom = Integer.parseInt(sb.toString());
 												String commentaire = com().elementAt(indexCom-1); // élément indice -1 du vector de la liste d'index;
 												r.setCommentaire(commentaire);
 											}
 											passage++;
 											break;
 											
 									case 6 : if(sb.length>0)
 											{
 												Date date = new Date(Long.parseLong(sb.toString()).longValue());
 												r.setDate(date);
 											}
 											passage++;
 											break;
 											
 									case 7 : if(sb.length>0)
 											{
 												int indexTexte = Integer.parseInt(sb.toString());
 												String text = prep().elementAt(indexTexte-1); // élément indice -1 du vector de la liste de préparation;
 												r.setTexte(text);
 											}
 											passage++;
 											break;
 											
 									default : break;
 								}
 							}
 						}
 						
 						listRec.add(r);
 					}
 								
                 }
             }
             finally // si jamais il y a un problème, on passe tout de même ici
             {
                 input.close() ;
             }
         }
         catch (IOException e)
         {
             e.printStackTrace() ;
         }
 
 		return listRec;
 	}
 	
 	/**
 	 * Liste les sous catégories.
 	 * 
 	 * @return VallSousCat
 	 * 		Toutes les sous-catégories.
 	 */
 	 public Vector<String> sousCat ()
 	 {
 		try
         {
             String line = null ;
 
 	   // Lecture
             BufferedReader input = new BufferedReader(new FileReader(choixduPeuple));
             try
             {
 				Vector<String> VallSousCat = new Vector<String>();
 		// Pour toute ligne lue
                 while ((line = input.readLine()) != null)
                 { 
                     if (Charger.est_une_sous_cat(line)==true)
                     {
 						StringBuffer sb = new StringBuffer(line);
 						StringBuffer scat = new StringBuffer();
 						
 						for (int j = 2 ; sb.charAt(j)!=':' ;j++)
 						{
 							scat.append(sb.charAt(j));
 						}
 						VallSousCat.add(scat.toString());
 					}
 				}
 			}
             finally // si jamais il y a un problème, on passe tout de même ici
             {
                 input.close() ;
             }
         }
         catch (IOException e)
         {
             e.printStackTrace() ;
         }
         
         
     }
 	
 	/**
 	 * Charge les recettes pour une seule sous-catégorie uniquement.
 	 * 
 	 * @param souscat
 	 * 		Sous-catégorie de recette.
 	 * @return Vsouscat
 	 * 		Vector avec les recettes d'une sous-catégorie.
 	 */
 	public Vector<String> sousCatRecette (String souscat)
 	{
 		Vector<Recette> rec = recette();
 		Vector<Recette> Vsouscat = new Vector<Recette>();
 		
 		for (int i = 0; i<rec.size(); i++)
 		{
 			if (rec.elementAt(i).getsousCategorie == souscat)
 			{
 				Vsouscat.add(rec.elementAt(i));
 			}
 		}
 		
 		return Vsouscat;
 	}
 	
 	
 	
 	/**
 	 * Nombre d'index de livre.
 	 * 
 	 * @return Le nombre de lignes comptées dans l'index.
 	 */
 	public int countNbIndex()
 	{
 		index();
 		return nbIndex;
 	}
 	
 	/**
 	 * Nombre total de commentaires.
 	 * 
 	 * @return Le nombre de lignes comptées dans le fichier de commentaires.
 	 */
 	public int countNbCom()
 	{
 		com();
 		return nbCom;
 	}
 	
 	/**
 	 * Nombre total d'URLs.
 	 * 
 	 * @return Le nombre de lignes comptées dans le fichier d'URLs.
 	 */
 	public int countNbUrl()
 	{
 		url();
 		return nbUrl;
 	}
 	
 	/**
 	 * Nombre total de préparations.
 	 * 
 	 * @return Le nombre de lignes comptées dans le fichier de préparations.
 	 */
 	public int countNbPrep()
 	{
 		prep();
 		return nbUrl;
 	}
 	
 	/**
 	 * Nombre de recettes dans une sous-catégorie donnée.
 	 * 
 	 * @param souscat
 	 * 		La sous-catégorie dans laquelle in compte.
 	 * @return nbRecSousCat
 	 */
 	public int countNbRecettePourSousCat(String souscat)
 	/*{
 		try
         {
             String line = null ;
 
 	   // Lecture
             BufferedReader input = new BufferedReader(new FileReader(choixduPeuple));
             try
             {
                 int nbRecSousCat = 0;
                 boolean test=false;
         // Pour toute ligne lue
                 while ((line = input.readLine()) != null)
                 {
 					if (Charger.est_une_sous_cat(line)==true && test==false)
 					{ // si l'on a pas encore trouvé la sous-catégorie
 						StringBuffer sb = new StringBuffer(line);
 						StringBuffer scat = new StringBuffer();
 						
 						for (int j = 2 ; sb.charAt(j)!=':' ;j++)
 						{
 							scat.append(sb.charAt(j));
 						}
 						if (scat.toString().equals(souscat)==true)
 						{ // on teste si c'est celle qu'on cherche
 							test=true; // on met le test comme "trouvé"
 						}
 					}
 					
 					else if (Charger.est_une_recette(line)==true && test==true)
 					{ // si on est dans la bonne sous catégorie et que c'est une recette
 						nbRecSousCat++;
 					}
 					else if (line.equals("\n")==true)
 					{
 						test=false; // on remet le test à faux lorsqu'on arrive à la ligne vide entre sous-catégorie
 					}
 				}
             }
 			finally // si jamais il y a un problème, on passe tout de même ici
 			{
             input.close() ;
 			}
 		}
 		catch (IOException e)
 		{
 			e.printStackTrace() ;
 		}
 
 	return nbRecSousCat;
 	}*/
 	{
 		return sousCatRecette(souscat).size();
 	}
    
     /**
      * @fn public static boolean est_une_sous_cat(String s)
      * @brief Teste si le mot est une sous catégorie
      * @return true si c'est une sous-catégorie, false sinon
      */    
     public static boolean est_une_sous_cat(String s)
     {
 		StringBuffer sb = new StringBuffer(s);
 		if (sb.charAt(0)=='*')
 			return true;
 		return false;
 	}
 	
     /**
      * @fn public static boolean est_une_recette(String s)
      * @brief Teste si le mot est un début d'une recette
      * @return true si c'est une recette, false sinon
      */    
     public static boolean est_une_recette(String s)
     {
 		StringBuffer sb = new StringBuffer(s);
 		if (sb.charAt(0)=='-')
 			return true;
 		return false;
 	}
 	
 }
