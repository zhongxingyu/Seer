 package data;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 
 import logic.Onderdeel;
 import logic.Onderwerp;
 import logic.Vraag;
 
 import nu.xom.*;
 
 import exceptions.DataException;
 
 public class Content {
 	// verkrijg vragen (met onderwerp)
 	private Document doc = null;
 	/**
 	 * Maak de contentparser aan en bereidt voor op inlezen vragen bestand.
 	 * @throws DataException Als de vragen niet gevonden kunnen worden.
 	 */
 	public Content() throws DataException {
 		File file;
 		try {
 			file = new File(getClass().getResource("/resource/Vragen.xml").toURI());
 		} catch (Exception e1) {
 			throw new DataException("Het vragenbestand kan niet worden gevonden.");
 		}
 
 		try {
 			doc = new Builder().build(file);
 		} catch (ValidityException e) {
 			throw new DataException("Fout bij validatie van vragenbestand.\r\n"+e.getMessage());
 		} catch (ParsingException e) {
 			throw new DataException("Fout bij parsen van vragenbestand.\r\n"+e.getMessage());
 		} catch (IOException e) {
 			throw new DataException("Het vragenbestand kan niet worden gevonden.");
 		}
 	}
 	
 	/**
 	 * Haal alle onderwerpen uit resource/Vragen.xml.
 	 * @return Lijst met onderwerpen.
 	 * @throws DataException Wanneer het path van het plaatje verkeerd is.
 	 */
 	public List<Onderwerp> getOnderwerpen() throws DataException {
 		List<Onderwerp> onderwerpen = new ArrayList<Onderwerp>();
 		Elements elements = doc.getRootElement().getChildElements();
 		
 		for (int i = 0; i < elements.size(); i++) {
 			Element node = elements.get(i);
 			
 			String naam = node.getAttribute("name").getValue();
         	String path = node.getAttribute("image").getValue();
         	
         	File plaatje;
         	try {
         		plaatje = new File(getClass().getResource("/resource/images/"+path).toURI());
         	} catch(Exception e) {
         		throw new DataException("Plaatje \""+path+"\" is onvindbaar.");
         	}
 			if(!plaatje.canRead()){
 				throw new DataException("Plaatje \""+plaatje.getAbsolutePath()+"\" is onvindbaar.");
 			}
 			Onderwerp onderwerp = new Onderwerp(naam, plaatje);
         	onderwerpen.add(onderwerp);
 		}
 		return onderwerpen;
 	}
 	/**
 	 * 
 	 * @param onderwerpNaam
 	 * @return
 	 * @throws DataException
 	 */
 	public List<Vraag> getVragen(String onderwerpNaam) throws DataException {
 		return getVragen(onderwerpNaam, 4);
 	}
 	public List<Vraag> getVragen(String onderwerpNaam, int hoeveel) throws DataException {
         Element onderwerp = null;
 		Elements elements = doc.getRootElement().getChildElements();
 		
 		for (int i = 0; i < elements.size(); i++) {
 			Element node = (Element) elements.get(i);
        	if(node.getAttribute("name").getValue().equals(onderwerpNaam))
         	{
         		onderwerp = node;
         	}
         }
         
         if(onderwerp == null) {
         	throw new DataException("Onderwerp bestaat niet");
         }
         
         List<Vraag> vragen = new ArrayList<Vraag>();
         Elements nodes = onderwerp.getChildElements();
         ArrayList<Integer> vragenKeys = new ArrayList<Integer>();
         
 		for (int i = 0; i < nodes.size(); i++) {
 			vragenKeys.add(i);
         }
         
         Collections.shuffle(vragenKeys);
         if(hoeveel > nodes.size()) {
         	hoeveel = nodes.size();
         }
         for (int i = 0; i < hoeveel; i++) {
         	Element node = nodes.get(vragenKeys.get(i));
         	String tekst = node.getFirstChildElement("tekst").getValue();
         	List<Onderdeel> onderdelen = new ArrayList<Onderdeel>();
         	Elements nOnderdelen = node.getFirstChildElement("onderdelen").getChildElements();
         	for (int j = 0; j < nOnderdelen.size(); j++) {
         		Element nodeOnderdeel = nOnderdelen.get(j);
         		String antwoord = nodeOnderdeel.getValue();
         		String path = nodeOnderdeel.getAttribute("image").getValue();
         		File plaatje = null;
     			try {
     				plaatje = new File(getClass().getResource("/resource/images/"+path).toURI());
     			} catch (URISyntaxException e) {
     				throw new DataException("Fout in filepath van plaatje van onderdeel \""+antwoord+"\"");
     			}
 
         		onderdelen.add(new Onderdeel(antwoord, plaatje));	
 			}
         	vragen.add(new Vraag(tekst, onderdelen));
 		}
 		return vragen;
 	}
 }
