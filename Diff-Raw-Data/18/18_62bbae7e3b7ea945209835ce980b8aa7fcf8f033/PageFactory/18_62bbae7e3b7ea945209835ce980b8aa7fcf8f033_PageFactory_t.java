 package page.rank.algorithm;
 
 import java.util.Set;
 import java.util.TreeSet;
 
 public class PageFactory {
 	public Page createPageFromString(String inputLine) {
 		Set<Link> links = new TreeSet<Link>();
 
		if (!inputLine.isEmpty()) {
			String[] linkStrings = inputLine.split(" ");
			for (String linkString : linkStrings) {
				String[] part = linkString.split(":");
 
				Integer target = Integer.parseInt(part[0]);
				Integer numberOfLinks = Integer.parseInt(part[1]);
 
				Link link = new Link(target, numberOfLinks);
				links.add(link);
			}
 		}
 		
 		return new Page(links);
 	}
 }
