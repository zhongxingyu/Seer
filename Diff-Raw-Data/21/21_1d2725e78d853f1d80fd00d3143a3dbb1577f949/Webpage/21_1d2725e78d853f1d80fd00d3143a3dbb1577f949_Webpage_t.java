 import java.io.IOException;
 import java.util.LinkedList;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 
 public class Webpage {
 
 	private LinkedList<CourseDescription> descriptions;
 	
 	
 	public Webpage(String url) throws IOException
 	{
 		descriptions = new LinkedList<CourseDescription>();
 		Document doc = Jsoup.connect(url).get();
 		Elements courseDescriptions = doc.select(".course-descriptions");
 		for(Element e : courseDescriptions)
 		{
 			CourseDescription cd = new CourseDescription(e.text());
 			//System.out.println(e.text());
 			addDescription(cd);
 		}
 		
 	}
 	
 	private void addDescription(CourseDescription c)
 	{
 		descriptions.add(c);
 	}
 	
 	public LinkedList<CourseDescription> getCourseDescriptions()
 	{
 		return descriptions;
 	}
 	
 	public String toString()
 	{
		StringBuilder sb = new StringBuilder();
 		for(CourseDescription cd : descriptions)
 		{
 			sb.append(cd.getDescription() + ". ");
 		}
 		return sb.toString();
 	}
 	
 }
