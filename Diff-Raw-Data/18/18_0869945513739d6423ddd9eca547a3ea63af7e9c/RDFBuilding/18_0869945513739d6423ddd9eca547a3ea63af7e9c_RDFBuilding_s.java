 package models;
 
 import java.io.*;
 import java.util.List;
 import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
 import com.hp.hpl.jena.rdf.model.*;
 import com.hp.hpl.jena.sparql.vocabulary.FOAF;
 import com.hp.hpl.jena.util.FileManager;
 import com.hp.hpl.jena.vocabulary.DC;
 import com.hp.hpl.jena.vocabulary.RDF;
 import com.hp.hpl.jena.vocabulary.RDFS;
 
 public class RDFBuilding {
 	private static volatile RDFBuilding instance = null;
 	private static final String rdf_file = "public/rdf/upyourmood.rdf";
 	private final String prefixe = "http://www.upyourmood.com/";
 	private Model m=null;
 	Property TitreAlbum =null;
 	Property APourConnotation=null;
 	Property AppartientA=null;
 	Property ListenMusic=null;
 	Property CorrespondA=null;
 	Property IsTag=null;
 	
 	private RDFBuilding(){
		m=FileManager.get().loadModel(rdf_file);
 	}
 	
 	public final static RDFBuilding getInstance() {
         if (RDFBuilding.instance == null) {
            synchronized(RDFBuilding.class) {
              if (RDFBuilding.instance == null) {
             	 RDFBuilding.instance = new RDFBuilding();
              }
            }
         }
         return RDFBuilding.instance;
     }
 	
 	/**
 	 * fonction principale qu'il faudra appeler dans la classe Application
 	 * @param List<String> infoMusic information de la musique courante
 	 */
 	public void rdfUpYourMood(List<String> infoMusic,UserInformation userInfo,WordConnotation word){
 		ajouterMusique(infoMusic);
 		ajouterUtilisateur(userInfo);
 		ajouterMot_Connotation(word);
 		FileOutputStream outStream;
 		try {
 			outStream = new FileOutputStream("public/rdf/upyourmood.rdf");
			m.write(outStream, "RDF/XML-ABBREV");
 	        outStream.close();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		 
 	}
 	
 	/**
 	 * Dans cette fonction on ajoute:
 	 * <ul>
 	 * 	<li>l'identifiant de la musique</li>
 	 * 	<li>titre de la musique</li>
 	 * 	<li>titre de l'album</li>
 	 * 	<li>nom de l'artiste</li>
 	 * 	<li>url de la photo de l'album</li>
 	 * </ul>
 	 */
 	private void ajouterMusique(List<String> infoMusic){
 		String music=m.getNsPrefixURI("music");
 		//Literal album=m.createTypedLiteral(infoMusic.get(1),XSDDatatype.XSDstring);
 		Literal artiste=m.createTypedLiteral(infoMusic.get(2),XSDDatatype.XSDstring);
 		//Literal pochette=m.createTypedLiteral(infoMusic.get(3),XSDDatatype.XSDanyURI);
 		//Literal titre=m.createTypedLiteral(infoMusic.get(4),XSDDatatype.XSDstring);
 		
 		Resource Music = m.createResource(music+infoMusic.get(0));
 		Resource Titre = m.createResource(music+infoMusic.get(4));
 		Resource Album = m.createResource(music+infoMusic.get(1));
 		Resource Pochette = m.createResource(music+infoMusic.get(3));
 		if (music!=null){
 			
 			///Music.addLiteral(m.getProperty("AlbumTitle"), album);
 			Music.addLiteral(DC.creator, artiste);
 			/*Music.addLiteral(FOAF.depiction, pochette);
 			Music.addLiteral(DC.title, titre);*/
 			m.add(Music,m.getProperty("CorrespondA"),Titre);
 			m.add(Album,RDF.type,m.getProperty("AlbumTitle"));
 			m.add(Titre,m.getProperty("AppartientA"),Album);
 			m.add(Titre,RDF.type,DC.title);
 			m.add(Album,m.getProperty("AppartientA"),Pochette);
 			m.add(Pochette,RDF.type,FOAF.depiction);
 			m.add(Music,RDF.type,Music);//TODO: à modifier à voir
 		}else{
 			String musicNs=prefixe+"music/";
 			m.setNsPrefix("music", musicNs);
 			Music = m.createResource(musicNs+infoMusic.get(0));
 			TitreAlbum = m.createProperty(musicNs+"AlbumTitle");
 			AppartientA = m.createProperty(musicNs+"AppartientA");
 			CorrespondA = m.createProperty(musicNs+"CorrespondA");
 			IsTag = m.createProperty(musicNs+"IsTag");
 			m.add(TitreAlbum, RDFS.subPropertyOf, DC.title);
 			
 			//Music.addLiteral(m.getProperty("AlbumTitle"), album);
 			Music.addLiteral(DC.creator, artiste);
 			/*Music.addLiteral(FOAF.depiction, pochette);
 			Music.addLiteral(DC.title, titre);*/
 			m.add(Music,m.getProperty("CorrespondA"),Titre);
 			m.add(Album,RDF.type,m.getProperty("AlbumTitle"));
 			m.add(Titre,m.getProperty("AppartientA"),Album);
 			m.add(Titre,RDF.type,DC.title);
 			m.add(Album,m.getProperty("AppartientA"),Pochette);
 			m.add(Pochette,RDF.type,FOAF.depiction);
 			m.add(Music,RDF.type,Music);
 		}
 	}
 	
 	/**
 	 * Dans cette fonction on ajoute:
 	 * a minima:
 	 * <ul>
 	 * 	<li>le pseudo</li>
 	 * </ul>
 	 */
 	private void ajouterUtilisateur(UserInformation userInfo){
 		String user=m.getNsPrefixURI("user");
 		Resource User = m.createResource(user+userInfo.getInfoUser().get(0));
 		if (user!=null){
 			m.add(User,RDF.type,User);//TODO: à modifier à voir
 		}else{
 			String userNs=prefixe+"user/";
 			m.setNsPrefix("user", userNs);
 			User = m.createResource(userNs+"User");
 			Property AEcoute = m.createProperty(userNs+"HasListen");
 			m.add(AEcoute,RDFS.subPropertyOf,FOAF.knows);
 			m.add(User,RDF.type,User);
 			m.add(User,AEcoute,m.getResource("Music"));
 		}
 	}
 	
 	private void ajouterMot_Connotation(WordConnotation word){
 		String mot=m.getNsPrefixURI("wordconnotation");
 		Literal connotation=m.createTypedLiteral(word.getConnotation(),XSDDatatype.XSDfloat);
 		Resource Mot = m.createResource(mot+word.getMot());
 		if (mot!=null){
			Mot.addLiteral(APourConnotation, connotation);
			m.add(Mot,RDF.type,m.getResource("WordConnotation"));
 		}else{
 			String motNs=prefixe+"wordconnotation/";
 			m.setNsPrefix("wordconnotation", motNs);
 			Mot = m.createResource(motNs+"WordConnotation");
 			Property APourConnotation = m.createProperty(motNs+"APourConnotation");
 		}
 	}
 }
