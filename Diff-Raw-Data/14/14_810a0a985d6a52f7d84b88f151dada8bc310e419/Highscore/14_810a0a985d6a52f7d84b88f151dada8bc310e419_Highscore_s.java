 package data;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
import logic.Highscore;

 import nu.xom.Attribute;
 import nu.xom.Builder;
 import nu.xom.Document;
 import nu.xom.Element;
 import nu.xom.Elements;
 import nu.xom.ParsingException;
 import nu.xom.ValidityException;
 import exceptions.DataException;
 
 public class Highscore {
 	private Document	doc;
 	private File		file;
 
 	public Highscore() throws DataException {
 		try {
 			file = new File(getClass().getResource("/resource/Highscores.xml").toURI());
 		} catch (URISyntaxException e1) {
 			throw new DataException("Het highscorebestand kan niet worden gevonden.");
 		}
 
 		try {
 			doc = new Builder().build(file);
 		} catch (ValidityException e) {
 			throw new DataException("Fout bij validatie van highscorebestand.\r\n" + e.getMessage());
 		} catch (ParsingException e) {
 			throw new DataException("Fout bij parsen van highscorebestand.\r\n" + e.getMessage());
 		} catch (IOException e) {
 			throw new DataException("Het highscorebestand kan niet worden gevonden.");
 		}
 	}
 
 	public void addHighscore(final logic.Highscore score) {
 		// "Naam", "Prijzengeld", "Tijd over"
 		Element elHighscore = new Element("highscore");
 		elHighscore.addAttribute(new Attribute("naam", score.getSpelerNaam()));
 		elHighscore.addAttribute(new Attribute("score", score.getScore()));
 		elHighscore.addAttribute(new Attribute("tijdOver", score.getTijdOver()));
 		doc.getRootElement().appendChild(elHighscore);
 		String xml = doc.toXML();
 		try {
 			FileWriter fstream = new FileWriter(file);
 			BufferedWriter out = new BufferedWriter(fstream);
 			out.write(xml);
 			out.close();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	public ArrayList<logic.Highscore> getHighscores(int hoeveel) {
 		Elements elements = doc.getRootElement().getChildElements();
 		
 		ArrayList<logic.Highscore> highscores = new ArrayList<logic.Highscore>();
 		for (int i = 0; i < elements.size(); i++) {
 			Element node = elements.get(i);
 			
 			logic.Highscore highscore = new logic.Highscore();
 			
 			highscore.setSpelerNaam(node.getAttribute("naam").getValue());
 			highscore.setScore(node.getAttribute("score").getValue());
 			highscore.setTijdOver(node.getAttribute("tijdOver").getValue());
 			
 			highscores.add(highscore);
 		}
 		
 		//Collections.sort(highscores, new )
 		//TODO: sorteer highscores
 		
 		return highscores;
 	}
 }
