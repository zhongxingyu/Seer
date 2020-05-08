 /**
  * Package de gestion traduction/ecriture enJson.
  * <p>
  * Utilise principalement la classe { @link org.jdom2.Element }
  */
 package xmltojson.jsonWriter;
 
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import net.sf.json.JSONObject;
 
 import org.jdom2.Attribute;
 import org.jdom2.Element;
 
 import xmltojson.xmlparser.XmlParserImpl;
 
 
 /**
  * Classe pour pouvoir traduire un fichier Json.
  * @author Raphaël
  *
  */
 public class ToJson {
 
     /**
      * Constructor ToJson.
      * @param pathFile Chemin du fichier en sortie.
      */
     public ToJson(final String pathFile) {
         super();
         this.pathFile = pathFile;
     }
 
     /**
      * Chemin fichier en sortie.
      */
     private String pathFile;
     /**
      * Map d'arborescende du doc Json complété au long du traitement.
      */
     @SuppressWarnings({ "rawtypes" })
     private Map balise = new LinkedHashMap();
     
     /**
      * Realise les appels aux parseurs, méthodes de traduction, ecriture
      * @param inPathFile Chemin fichier entrée
      */
     public void toJson(String inPathFile) {
         List<Element> elems = new ArrayList<Element>();
         XmlParserImpl xmlparser = new XmlParserImpl();
         elems = xmlparser.parser(inPathFile);
         toJson(elems);
     }
     
     /**
      * Traduire en objet Json et ecriture dans le fichier.
      * @param questions Liste des questions du quiz MoodleXML
      */
     @SuppressWarnings({ "unchecked", "rawtypes" })
     public void toJson(List<Element> questions) {
         JSONObject oJson = new JSONObject();
         
         
         //construire l'arborescence Map/List
         if (questions.size() == 1) { //pour une question
             balise.put(questions.get(0).getName(), toJson(questions.get(0)));
         } else { //pour plusieurs questions
             List<Map> l = creerListes(questions, questions.size(), 0);
             balise.put(questions.get(0).getName(), l);
         }
         
         //Remplir le JSONObject
         oJson.put("quiz", balise);
         
         
         //ecrire dans le fichier
         SaveFileJson save = new SaveFileJson(oJson.toString(1), pathFile);
         save.sauvegarde();
         //TODO Re-indempter le fichier.
         //save.indempter();
         
     }
     
     /**
      * Traduire en objet Json et ecriture dans le fichier.
      * @param element traite une element Jdom2
      * @return Map contenant l'arborescence du parametre element traite
      */
     @SuppressWarnings({ "rawtypes", "unchecked" })
     public Map toJson(Element element) {
         List<Attribute> attList = new ArrayList<Attribute>();
         List<Element> childrens = new ArrayList<Element>();
         List<String> isDone = new ArrayList<String>();
         Map courante = new LinkedHashMap();
         
         //recupere les attributs de la question
         attList = element.getAttributes();
         
         if (!attList.isEmpty()) {
             for (Attribute att : attList) {
                 courante.put(att.getName(), att.getValue());
             }
         }
         
         //On recupere les fils
         childrens = element.getChildren();
         if (!childrens.isEmpty()) {
             //traiter chaque fils en profondeur.
             //CARE:Selon code une balise ne peut avoir texte ET(attribut ou sous balise).
             // i : compteur elem courant
             int i = 0;
             for (Element children : childrens) {
                if (children.getName().equals("answer")) {
                     System.out.println("Debug");
                     // Point arret debug
                 }
 
                 if (!isDone.contains(children.getName())) { //non traité si deja fait
                     //CARE les fils identiques a gerer en Array doivent se suivre.
                     if (getEqualsChildrens(childrens, children, i)) { //plus d'un fils de même nom
                       isDone.add(children.getName());
                       List<Map> l = creerListes(element, children);
                       courante.put(childrens.get(i).getName(), l);
                     }
                     else {
                         if (addChild(children) || addAttributes(children)) {
                             courante.put(children.getName(), toJson(children));
                         } else {
                             String text = children.getValue();
                             if (!text.equals("")) {
                                 courante.put(children.getName(), text);
                             }  else { courante.put(children.getName(), null); }
                         }
                     }
                 }
                 i++;
             }
         } else {
             String text = element.getValue();
             if (!text.equals("")) {
                 courante.put(element.getName(), text);
             }  else { courante.put(element.getName(), null); }
         }
         return courante;
     }
     
     /**
      * Traite le contenu d'un element.
      * @param element elem courant
      * @return Map du contenu de l'element
      */
     @SuppressWarnings({ "rawtypes", "unchecked" })
     public Map toJsonContenu(Element element) {
         List<Element> childrens = new ArrayList<Element>();
         List<Attribute> attList = new ArrayList<Attribute>();
         Map courante = new LinkedHashMap();
         
         //recupere les attributs de la question
         attList = element.getAttributes();
         
         if (!attList.isEmpty()) {
             for (Attribute att : attList) {
                 courante.put(att.getName(), att.getValue());
             }
         }
         
         //On recupere les fils
         childrens = element.getChildren();
         if (!childrens.isEmpty()) {
             //traiter chaque fils en profondeur.
             //CARE:Selon code une balise ne peut avoir texte ET(attribut ou sous balise).
             for (Element children : childrens) {
                 //appel au toJson normal
                //courante.put(children.getName(), toJson(children));
                
                if (addChild(children) || addAttributes(children)) {
                    courante.put(children.getName(), toJson(children));
                } else {
                    String text = children.getValue();
                    if (!text.equals("")) {
                        courante.put(children.getName(), text);
                    }  else { courante.put(children.getName(), null); }
                }
                
             }
         } else {
             String text = element.getValue();
             if (!text.equals("")) {
                 courante.put(text, null);
             }  //else { courante.put(element.getName(), null); }
         }
         return courante;
     }
     
     /**
      * Cree les listes dans un cas particulier de traduction en Json Array.
      * @param childrens Liste des fils de l'element courant
      * @param cpt Nombre de fils identiques
      * @param j position premier fils dans liste
      * @return Liste de Map des element jdom2 fils identiques
      */
     @SuppressWarnings("rawtypes")
     public List<Map> creerListes(List<Element> childrens, int cpt, int j) {
         List<Map> list = new ArrayList<Map>();
         for (int i = j; i < cpt + j; i++) {
             list.add(toJson(childrens.get(i)));
         }
         return list;
     }
     
     /**
      * Creer une liste dans le cas général de trad JsonArray.
      * @param father element pere
      * @param children element courant
      * @return La liste de map du courant
      */
     @SuppressWarnings("rawtypes")
     public List<Map> creerListes(Element father, Element children) {
         List<Map> list = new ArrayList<Map>();
         List<Element> sameChildrens = new ArrayList<Element>();
         sameChildrens = father.getChildren(children.getName());
         for (Element child : sameChildrens) {
             list.add(toJsonContenu(child));
         }
         return list;
     }
 
 
     /**
      * Teste si la liste possede plusieurs elements courant
      * @param childrens liste des fils
      * @param courant element à tester
      * @param i position de courant dans la liste
      * @return boolean possede plusieurs elements courants.
      */
     public boolean getEqualsChildrens(List<Element> childrens, Element courant, int i) {
         int cpt = 0;
         for (int j = i + 1; j < childrens.size(); j++)
             if (courant.getName().equals(childrens.get(j).getName()))
                 cpt++;
         return cpt > 0;
     }
     
     /**
      * Compare suivants à partir de courant et renvoie le nombre de balises identiques.
      * @param childrens Liste des fils de l'element
      * @param courant fils etudie
      * @param i position de ce fils
      * @return nombre de fils identiques a courant dans la suite de la liste childrens
      */
     public int cptEquals(List<Element> childrens, Element courant, int i) {
         int cpt = 0;
         for (int j = i + 1; j < childrens.size(); j++)
             if (courant.getName().equals(childrens.get(j).getName()))
                 cpt++;
         return cpt;
     }
 
     /**
      * Teste si l element courant possede un fils.
      * @param e element courant
      * @return boolean a un fils?
      */
     public boolean addChild(Element e) {
         return !(e.getChildren().isEmpty());
     }
 
     /**
      * Teste si l element courant a un attribut.
      * @param e element courant
      * @return boolean a un attribut?
      */
     public boolean addAttributes(Element e) {
         return !(e.getAttributes().isEmpty());
     }
     
     /**
      * Main pour test.
      * @param args arguments du main
      */
     public static void main(String[] args) {    
         ToJson tj = new ToJson("src/test/resources/USE_TrueFalse_AUTOGEN.json");
         tj.toJson("src/test/resources/USE_TrueFalse_RSC.xml");
         
         ToJson tj2 = new ToJson("src/test/resources/USE_exemple_AUTOGEN.json");
         tj2.toJson("src/test/resources/USE_Exemple_RSC.xml");
         
         ToJson tj3 = new ToJson("src/test/resources/USE_TestLimite_AUTOGEN.json");
         tj3.toJson("src/test/resources/USE_TestLimite_RSC.xml");
 //        
         ToJson tj4 = new ToJson("src/test/resources/USE_XmlWriterTest_AUTOGEN.json");
         tj4.toJson("src/test/resources/USE_XmlWriterTest_RSC.xml");
     }
 
 }
