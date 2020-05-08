 package edu.uib.info310.model.mock;
 
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import edu.uib.info310.model.Artist;
 import edu.uib.info310.model.Event;
 import edu.uib.info310.model.Record;
 
 
 public class MockArtist implements Artist {
 
 	public String getName() {
 		return "Rihanna";
 	}
 
 	public String getBio() {
 		return "Robyn Rihanna Fenty (born February 20, 1988), better known as simply Rihanna, is a Barbadian R&B recording artist. Born in Saint Michael, Barbados, Rihanna moved to the United States at the age of 16 to pursue a recording career under the guidance of record producer Evan Rogers. She subsequently signed a contract with Def Jam Recordings after auditioning for then-label head Jay-Z.\nIn 2005, Rihanna released her debut studio album, Music of the Sun, which peaked in the top ten of the Billboard 200 chart and features the Billboard Hot 100 hit single Pon de Replay. Less than a year later, she released her second studio album, A Girl Like Me (2006), which peaked within the top five of the Billboard albums chart, and produced her first Hot 100 number one single, SOS. Rihannas third studio album, Good Girl Gone Bad (2007), spawned four chart-topping singles Umbrella, Take a Bow, Disturbia and Dont Stop the Music, and was nominated for nine Grammy Awards, winning Best Rap/Sung Collaboration for Umbrella, which features Jay-Z. Her fourth studio album Rated R, released in November 2009, produced the top 10 singles Russian Roulette, Hard and Rude Boy, which achieved the number-one spot on the Billboard Hot 100. Loud (2010), her fifth studio album, contains the number-one hits Only Girl (In the World), Whats My Name? and S&M. We Found Love was released in September 2011 as the lead single from her sixth studio album, Talk That Talk, which is set to be released in November 2011.";
 	}
 
 	public String getImage() {
 		return "http://konsepter.thunemedia.no/SemFM/skisse/rihanna.jpg";
 	}
 
 	public List<Record> getDiscography() {
 
 		return createDiscography();
 	}
 
 	public List<Artist> getSimilar() {
 		return createSimilar();
 	}
 
 
 	public List<Event> getEvents() {
 		return createEvents();
 	}
 
 
 
 	public Map<String, String> getMeta() {
 		return createMeta();
 	}
 
 	private Map<String, String> createMeta() {
 		Map<String, String> meta = new HashMap<String,String>();
 		meta.put("Age", "22");
 		meta.put("Sex", "Female");
 		meta.put("Born", "Alebama");
 		meta.put("Lives", "New York");
 		meta.put("Favorite food", "Taco");
 		return meta;
 	}
 
 	private List<Record> createDiscography() {
 		List<Record> records = new LinkedList<Record>();
 		records.add(new MockRecord());
 		records.add(new MockRecord());
 		records.add(new MockRecord());
 		records.add(new MockRecord());
 		records.add(new MockRecord());
 		return records;
 	}
 
 	private List<Artist> createSimilar() {
 		List<Artist> similar = new LinkedList<Artist>();
 		similar.add(new MockArtist());
 		similar.add(new MockArtist());
 		similar.add(new MockArtist());
 		similar.add(new MockArtist());
 		similar.add(new MockArtist());
 		return similar;
 	}
 
 	private List<Event> createEvents() {
 		List<Event> events = new LinkedList<Event>();
 		events.add(new MockEvent());
 		events.add(new MockEvent());
 		events.add(new MockEvent());
 		events.add(new MockEvent());
 		events.add(new MockEvent());
 		events.add(new MockEvent());
 		return events;
 	}
 
 	public String getShortDescription() {
 		return "Singer, Songwriter";
 	}
 
 	public String getDescription() {
 		return getBio();
 	}
 
 	public String getId() {
 		return "http://dbpedia.org/page/Rihanna";
 	}
 
 	public String getSafeName() {
 		return this.getName();
 	}

	public List<Event> getLocatedEvents() {
		return getEvents();
	}
 }
