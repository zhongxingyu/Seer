 package fr.ujm.tse.info4.pgammon.models;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.jdom2.Document;
 import org.jdom2.Element;
 import org.jdom2.JDOMException;
 import org.jdom2.input.SAXBuilder;
 import org.jdom2.output.Format;
 import org.jdom2.output.XMLOutputter;
 
 public class GestionDeSession {
 	public List<Session> listSession = new ArrayList<Session>();
 	private static GestionDeSession gestionDeSession;
 	
 	private GestionDeSession(){
 		
 	}
 	
 	public static GestionDeSession getGestionDeSession() throws IOException, JDOMException{
 		
 		if(gestionDeSession == null){
 			gestionDeSession = new GestionDeSession();  
 			gestionDeSession.charger();
 		} 
 		return gestionDeSession; 
 	}
 	
 	public void sauvegarder(){
 		try{
 			for(int i=0;i<listSession.size();i++){
 				
 				String tmpNom = "Session"+String.valueOf(listSession.get(i).getIdSession());
 				Element racine = new Element(tmpNom);
 				Document document = new Document(racine);
 				listSession.get(i).sauvegarder(racine);
 				XMLOutputter sortie = new XMLOutputter(Format.getPrettyFormat());
 				String tmpPath = "sauvegardeSessions/"+tmpNom+".xml"; 
 				sortie.output(document, new FileOutputStream(tmpPath));
 				
 				
 				
 			}
 	    }catch(Exception e){
 				System.out.println("Erreur d'enregistrement");
 			}
 	}
 
 	
 	public void charger() throws JDOMException, IOException{
 		
 		File files[]; 
 		File path = new File("sauvegardeSessions");
 		files = path.listFiles();
	
 		Arrays.sort(files);
 		
 		SAXBuilder builder = new SAXBuilder();
 		
 		for (int i = 0; i < files.length; i++) {
 			
 			String tmpPath = files[i].toString();
 			Document document = builder.build(tmpPath);
 			Element racine = document.getRootElement();
 			listSession.add(new Session());
 			listSession.get(i).charger(racine);
 
 		}
 		
 
 		
 	//	listJoueurs = racine.getChildren("joueurs");
 		//Iterator<Element> it = listJoueurs.iterator();
 		 
 		// while(it.hasNext()){
 		//	 Joueur j = new Joueur();
 		//	 j.charger(it.next());
 		//	 joueurs.add(j);
 		 }	 
 		
 	//}
 
 		
 	public List<Session> getListSession() {
 		return listSession;
 	}
 
 	public void setListSession(List<Session> listSession) {
 		this.listSession = listSession;
 	}
 
 	
 	
 }
