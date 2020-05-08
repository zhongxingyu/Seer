 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 
 import de.umass.lastfm.Tag;
 
 
 public class controller {
 	private static final String apiKey = "9b24762f4ec58fed178083b4d9f255c2";
 	
 	//hier wordt de gui aangemaakt
 	public static void main(String [] args){
 		new gui();
 	}
 	// vind in de database een matching liedje.
 	public static Lied findSong(Lied invoer){
 		//hier worden methodes aangeroepen om de ingevoerde liedje te vinden in de db
 		try{
 			Lied gevonden = lastFM.searchSong(invoer.getNaam(), invoer.getArtiest());
 			gui.setVerificatie(gevonden);
 			return gevonden;
 		}
 		catch(Exception e){
			System.out.println(e);
 			return null;
 		}
 	}
 	
 	//hier worden liedjes gevonden die de gebruiker mogelijk ook leuk zal vinden 
 	public static void findSimilarSongs(Lied invoer){
 		//methodes anroepen die gelijksoortige liedjes kan vinden op basis van de invoer
 		//arraylist vullen van de gui setFeedbackPanel(Lied invoer)
 		ArrayList<Lied> songs = lastFM.similarSongs(invoer);
 		gui.setFeedbackPanel(songs);
 		
 	}
 	public static void findSimilarSongsDecisionTree(Collection<String> tags, Lied song){
 		ArrayList<Lied> liedjes = new ArrayList<Lied>();
 		ArrayList<Lied> tagLiedjes;	
 		
 		for(String tagName : tags)
 		{
 			Iterator<Tag> i = Tag.search(tagName, apiKey).iterator();
 			Tag tag = i.next();
 			while(!tag.getName().equals(tagName) && i.hasNext())
 			{
 				Tag dummy = i.next();
 				if(dummy.getName().equals(tagName))
 					tag = dummy;
 			}
			tagLiedjes = lastFM.getTopTracks(tag);
 			for(Lied liedje : tagLiedjes)
 			{				
 				liedjes.add(liedje);
 			}
 		}
 				
 		//opbouw v/d decision tree
 		Iterator<String> i = tags.iterator();
 		DTreeNode prev = new DTreeNode(i.next(), song);
 		DTreeNode next;
 		DTreeNode root = new DTreeNode(i.next(), prev, song);;
 		while(i.hasNext())
 		{
 			next = root;
 			root = new DTreeNode(i.next(), prev,  song);
 			prev = next;
 		}
 		
 		//vind 10 meest vergelijkbare liedjes
 		ArrayList<Lied> result = root.classify(liedjes, 10);
 		gui.setFeedbackPanel(result);		
 	}
 }
