 package univ_angers.virtuose.rendu;
 
 import java.io.File;
 import java.io.StringReader;
 
 import java.util.ArrayList;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 
 /**
  * Extract est utilisé pour extraire des noeuds (parents et fils inclus) d'une carte donnée.
  * L'extraction se fait de sorte que les noeuds liés (parent-fils et frere) soit représentés
  * dans un même contenu xml.
  */
 public class Extract {
     //liste des identifiants à extraire
 	private static ArrayList<String> res_ids;
 	// nom de la carte
 	private static String card;
 	// liste des noeuds lus (utilisé seulement pour l'extraction en cours)
 	private static ArrayList<String> read_ids;
 	// contenu xml extrait
 	private static String xmlText;
 	
 	/**
 	* Fonction qui procède à l'extraction des noeuds.
 	* Seuls les noeuds liés comme décrit précédemment seront extraits.
 	* Une fois l'extraction terminée le paramètre Ids est mis à jour afin de lister les noeuds restants.
 	* @param OrgCard, carte de laquelle sont extraits les noeuds
 	* @param Ids, liste des identifiants des noeuds à extraire
 	* @return Un couple contenant le nom qui sera utilisé pour l'affichage (cf Controller) et le contenu
 	*         xml à utilisé pour la visualisation
 	*/
 	public static ArrayList<String> extract( String OrgCard, ArrayList<String> Ids ) throws Exception{
 		ArrayList<String> d1 = new ArrayList<String>();
 		res_ids = new ArrayList<String>();
 		res_ids = Ids;
 		card = OrgCard;
 		read_ids = new ArrayList<String>();
 		try{
 			DocumentBuilderFactory fabrique = DocumentBuilderFactory.newInstance();
 			fabrique.setValidating(true);
 			File xml = new File(OrgCard);
 			DocumentBuilder constructeur = fabrique.newDocumentBuilder();
 			constructeur.setErrorHandler(null);
 			
 			Document document = constructeur.parse(xml);
 			Element racine = document.getDocumentElement();
 			NodeList liste = racine.getElementsByTagName("node");
 			Element e = null;
 			
 			for(int i=0; i<liste.getLength(); i++){
 				Element e1 = (Element)liste.item(i);
 				String e1Id = e1.getAttribute("ID");
 				
 				if(e1Id.equals(Ids.get(0))){
 					e = (Element) liste.item(i);
 				}
 			}
 			
 			if(e.hasAttribute("TEXT")){
 			    
			    xmlText = "<node ID=\""+e.getAttribute("ID")+"\" TEXT=\""+e.getAttribute("TEXT")+"\" class=\"result\" BACKGROUND_COLOR=\"#FF00FF\">\n</node>";
 			    read_ids.add(e.getAttribute("ID"));
 			    res_ids.remove(res_ids.indexOf(e.getAttribute("ID")));
 				displayChilds(e);
 				System.out.println("display childs and e :\n"+xmlText);
 				displayParents((Element) e.getParentNode(), racine);
 				
 				// ajout du text du 1er noeud (i.e le resultat le plus pertinent de la recherche)
 				d1.add(e.getAttribute("TEXT"));
 				// ajout du contenu xml extrait à partir de ce dernier
 				d1.add(xmlText);
 				// mise à jour des identifiants à extraire
 				Ids = res_ids;
 				return d1;
 			}
 		}catch(Exception e){
 			throw e;
 		}
 		Ids = res_ids;
 		return d1;
 	}
 
 	private static int foundInNodeList(Node n , NodeList nl){
 		int ret = -1;
 		for(int i = 0; i< nl.getLength(); ++i){
 			if (n.equals(nl.item(i))){
 				ret=i;
 			}
 		}
 		return ret;
 	}
 	
 	/**
 	* Ajoute les descendants (fils directs et non) au contenu xml
 	* @param e, noeud pour lequel les descendants doivent être extrait
 	*/
 	private static void displayChilds(Element e) {
 
         NodeList nodeChilds = e.getElementsByTagName("node");
 		NodeList childs = e.getChildNodes();
         if(childs.getLength()>0){
             String eId = e.getAttribute("ID");
             int eInText = xmlText.indexOf(eId);
             String eXMLtmp1="";
             String eXMLtmp2="";
         	if(eInText!=-1){
         	    // on se positionne dans le contenu xml de sorte
         	    // à être dans le noeud représenté par e	        
         	    eXMLtmp2 = xmlText.substring(eInText);
         	    int inNodeE = eXMLtmp2.indexOf(">");
         	    eInText = eInText+inNodeE+1;
         	    eXMLtmp1 = xmlText.substring(0,eInText);
         	    eXMLtmp2 = xmlText.substring(eInText+1);
         	}
             for(int i =0; i<childs.getLength(); ++i){
             	Node tmp = childs.item(i);
        	    	int pos = foundInNodeList(tmp, nodeChilds);
     	        if(pos!=-1 ){
     	            Element ce = (Element) nodeChilds.item(pos);
     	            if (!read_ids.contains(ce.getAttribute("ID"))){
     	                // le fils n'a pas encore été lu et doit donc être àjouté au contenu
     	                String tmp_ret = "";
     	    		    String attributes = "ID=\""+ce.getAttribute("ID")+"\" TEXT=\""+ce.getAttribute("TEXT")+"\"";
 		                if(res_ids.contains(ce.getAttribute("ID"))){
     		                int index = res_ids.indexOf(ce.getAttribute("ID"));
    			            attributes +=" class=\"result\" BACKGROUND_COLOR=\"#FF00FF\"";
     			            read_ids.add(ce.getAttribute("ID"));
     			            res_ids.remove(index);
     		            }
         		        tmp_ret +="<node "+attributes+">\n</node>\n";
 
         	    		xmlText = eXMLtmp1+tmp_ret+eXMLtmp2;
         	    		eXMLtmp2 = xmlText.substring(eInText);
         	    		System.out.println("SETTING EXMLTMP2:\n"+eXMLtmp2);
         	    		System.out.println("normal child extract:\n"+xmlText);
         	    		// on ajoute les descendants du fils au contenu
         	    		displayChilds(ce);
         	    	}else{
         	    	    // le fils a déjà été lu, par conséquent on passe à ses descendants
         	    	    
         	    	    displayChilds(ce);
         	    	}
                 }
     				
     		}
     	}
 	}
 	
 	/**
 	* ajoute les parents d'un noeu au contenu xml à retourner
 	* @ param e, noeud pour lequel les parents doivent être extraits
 	* @ param r, noeud racine de la carte originale
 	*/
 	private static void displayParents(Element e, Element r) {
 	    
 		Element pe= (Element) e.getParentNode();
 		
 		if (!e.equals(r)){
 		    read_ids.add(e.getAttribute("ID"));
 		    String attributes = "ID=\""+e.getAttribute("ID")+"\" TEXT=\""+e.getAttribute("TEXT")+"\"";
 		    if(res_ids.contains(e.getAttribute("ID"))){
 		        // e est un des identifiants à extraire
 		        // il faudra donc aussi extraire ses "autres" descendants
 		        int index = res_ids.indexOf(e.getAttribute("ID"));
			    attributes +=" class=\"result\" BACKGROUND_COLOR=\"#FF00FF\"";
 			    res_ids.remove(index);
 
                 // ajout de e au contenu xml
                 xmlText = "<node "+attributes+">\n"+xmlText+"</node>\n";
                 // ajout de ses descendants au contenu xml
                 displayChilds(e);
 		    }else{
 		        xmlText = "<node "+attributes+">\n"+xmlText+"</node>\n";
 		    }
 		}
 		
 		// si le père de e n'est pas la racine on l'ajoute au contenu
 		if(!pe.equals(r)){
 		    displayParents(pe,r);
 		}
 		
 	}
 }
