 package uk.co.mindbadger.footballresults.loader;
 
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 import uk.co.mindbadger.footballresults.reader.FootballResultsReader;
 
 public class LoadResultsFromXMLApplication {
 
	public static void main(String[] args) {
		ApplicationContext context = new ClassPathXmlApplicationContext("spring-xml-reader.xml");
 
		FootballResultsReader reader = (FootballResultsReader) context.getBean("reader");
		reader.readFixturesForSeason(2000);
	}
 }
