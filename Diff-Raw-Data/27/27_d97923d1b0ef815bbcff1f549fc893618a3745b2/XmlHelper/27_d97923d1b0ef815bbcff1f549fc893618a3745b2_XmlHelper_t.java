 package covoiturage;
 
 import iaws.covoiturage.domain.Coordonnee;
 
 import org.jdom2.Document;
 import org.jdom2.Element;
 import org.jdom2.Namespace;
 
 
 /**
  * @author Axel Robert
  */
 public class XmlHelper {
 	/**
      * Return the root Element which contains all parameters 
      * @param coord Coordonnees de l'adresse
      * @param nom Nom du personnel
      * @param prenom Prenom du personnel
      * @param mail Email du personnel
      * @return  the root Element
      */
 
     public static Element getResultsInXml(Coordonnee coord, String nom, String prenom, String mail, String adresse) {
         //Nous allons commencer notre arborescence en créant la racine XML
    		//qui sera ici "personnes".
    		Element racine = new Element("CoVoiturage");
    		racine.setNamespace(Namespace.getNamespace("http://iaws/ws/contractfirst/inscription"));
    		Namespace XSI = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
    		racine.addNamespaceDeclaration(XSI);
    		racine.setAttribute("schemaLocation", "http://iaws/ws/contractfirst/inscription ../resources/CoVoiturageIns.xsd", XSI);
    		Document doc = new Document(racine);
    		Element personnel = new Element("personnel","http://iaws/ws/contractfirst/inscription");
         racine.addContent(personnel);
         Element familyname = new Element("nom","http://iaws/ws/contractfirst/inscription");
         familyname.setText(nom);
         Element firstname = new Element("prenom","http://iaws/ws/contractfirst/inscription");
         firstname.setText(prenom);
         Element email = new Element("mail","http://iaws/ws/contractfirst/inscription");
         email.setText(mail);
         Element adr = new Element("adresse","http://iaws/ws/contractfirst/inscription");
         adr.setText(adresse);
         personnel.addContent(familyname);
         personnel.addContent(firstname);
         personnel.addContent(email);
         personnel.addContent(adr);
         Element coordonnees = new Element("coordonnees","http://iaws/ws/contractfirst/inscription");
         racine.addContent(coordonnees);
         Element lat = new Element("latitude","http://iaws/ws/contractfirst/inscription");
        lat.setText(Double.toString(coord.getLatitude()));
         Element lon = new Element("longitude","http://iaws/ws/contractfirst/inscription");
        lon.setText(Double.toString(coord.getLongitude()));
         coordonnees.addContent(lat);
         coordonnees.addContent(lon);
         
         return doc.getRootElement();
     }
 }
