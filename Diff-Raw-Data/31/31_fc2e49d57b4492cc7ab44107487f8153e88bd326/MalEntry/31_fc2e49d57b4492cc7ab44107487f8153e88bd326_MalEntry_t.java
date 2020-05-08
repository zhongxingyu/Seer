 package toshomal.data;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.lang.StringEscapeUtils;
 
 public class MalEntry {
 
 	private HashMap<String, String> entries;
 	
 	private static final String[] tags = new String[] { 
 		"title", 
 		"id",
 		"episodes",
 		"type",
 		"status",
 		"image",
 		"synonyms",
 		"english",
 		"score",
 		"start_date",
 		"end_date",
 		"synopsis"
 		};
 	
 	public MalEntry() {
 		this.entries = new HashMap<String, String>();
 	}
 	
 //	public MalEntry(String title, String id, String episodes, String type, String status, String image) {
 //		this.title = title;
 //		this.id = id;
 //		this.episodes = episodes;
 //		this.type = type;
 //		this.status = status;
 //		this.image = image;
 //		this.synonyms = synonyms;
 //		
 //	}
 	
 	public static List<MalEntry> parseFromString(String content) {
		
		if(! content.startsWith("<?xml")){
			return null;
		}
 		List<MalEntry> result = new ArrayList<MalEntry>();
 		Pattern p1 = Pattern.compile(".*<anime>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
 		Matcher m1 = p1.matcher(content);
 		content = m1.replaceFirst("");
 		
		System.out.println("Content: \n" + content);
		p1 = Pattern.compile("<entry>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE | Pattern.LITERAL);
 		m1 = p1.matcher(content);
		Pattern p2 = Pattern.compile("</entry>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE | Pattern.LITERAL);
 		Matcher m2 = p2.matcher(content);
 		while (m1.find()) {
 			int entryStart = m1.end();
 			int entryEnd = -1;
 			if (m2.find()) {
 				entryEnd = m2.start();
 			}
 			if (entryEnd < 0) {
 				throw new RuntimeException("No closing </entry> tag found!");
 			}
 			String entry = content.substring(entryStart, entryEnd);
 			MalEntry malEntry = new MalEntry();
 			
 			for (String tag : tags) {
 				p1 = Pattern.compile("<" + tag + ">\\s*(.*)\\s*</" + tag + ">", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
 				m1 = p1.matcher(entry);
				if (m1.find()) {
					String g = m1.group(1);
 					g = StringEscapeUtils.unescapeHtml(g);
 					malEntry.put(tag, g);
 				}
 			}
 			result.add(malEntry);
 		}
 		return result;
 	}
 	
 	public void put(String key, String value) {
 		this.entries.put(key, value);
 	}
 	
 	public String get(String key) {
 		return this.entries.get(key);
 	}
 }
