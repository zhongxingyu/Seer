 package crawling;
 
 import java.io.IOException;
 import java.io.Serializable;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import util.Util;
 
 import com.google.api.services.plus.Plus;
 import com.google.api.services.plus.model.Activity;
 import com.google.api.services.plus.model.ActivityFeed;
 import com.google.api.services.plus.model.PeopleFeed;
 import com.google.api.services.plus.model.Person;
 
 /**
  * 
  * @author otruffer
  * 
  *         This class represents a node in the Plus1 Network. Note that it is
  *         empty if you didn't use crawl().
  */
 
 @SuppressWarnings("serial")
 public class Node implements Serializable {
 
 	private String id;
 	Map<String, Integer> plusOnersMap;
 	String name;
 	boolean crawled;
 	private List<Node> plusOners;
 
 	public Node(String id) {
 		this.id = id;
 		this.crawled = false;
 		this.plusOnersMap = new HashMap<String, Integer>();
 	}
 
 	/**
 	 * Collects all plus1 this user receives for his activities.
 	 * 
 	 * @throws IOException
 	 */
 	public void crawl(Plus plus) throws IOException {
 		if (crawled)
 			return;
 
 		Person profile = plus.people().get(id).execute();
 		this.name = profile.getNickname();
 		Util.write("crawling for: " + id + "(" + this.name + ")");
 
 		ActivityFeed feed = plus.activities().list(profile.getId(), "public")
 				.execute();
 
 		for (Activity activity : feed.getItems()) {
 			Util.write("PlusOners for activity: " + activity.getId());
 			String aId = activity.getId();
 			PeopleFeed people = plus.people().listByActivity(aId, "plusoners")
 					.execute();
 			for (Person p : people.getItems()) {
 				addPlusOne(p.getId());
 				Util.write(p.getId() + "(" + p.getNickname() + ")");
 			}
 		}
 		crawled = true;
 	}
 
 	private void addPlusOne(String id) {
 		if (plusOnersMap.containsKey(id))
 			plusOnersMap.put(id, plusOnersMap.get(id).intValue() + 1);
 		else
 			plusOnersMap.put(id, 1);
 	}
 
 	public String getId() {
 		return this.id;
 	}
 
 	public List<String> getPlusOners() {
 		return new LinkedList<String>(this.plusOnersMap.keySet());
 	}
 
 	/**
 	 * Gives the number of +1s the person with the specified id (key) gave me.
 	 * 
 	 * @param key
 	 * @return
 	 */
 	public int getPlusOnesFrom(String key) {
 		int a = 0;
 		if (plusOnersMap.get(key) != null)
 			a = plusOnersMap.get(key);
 		return a;
 	}
 
 	public int getAllSentPlusOnes() {
 		int count = 0;
 		for (Node node : plusOners) {
			count += node.getPlusOnesFrom(this.id);
 		}
 		return count;
 	}
 
 	public int receivedPlusOnes() {
 		int count = 0;
 		for (Integer i : plusOnersMap.values())
 			count += i;
 		return count;
 	}
 
 	public void write() {
 		Util.write("->" + this.id);
 		for (String key : plusOnersMap.keySet())
 			Util.write(key + " : " + plusOnersMap.get(key));
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public boolean isCrawled() {
 		return crawled;
 	}
 
 	public void setPlusOners(List<Node> plusReceivers) {
 		this.plusOners = plusReceivers;
 	}
 }
