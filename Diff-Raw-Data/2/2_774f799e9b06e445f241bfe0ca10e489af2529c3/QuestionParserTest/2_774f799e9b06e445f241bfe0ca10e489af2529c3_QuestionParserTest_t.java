 package be.noselus.scraping;
 
 import be.noselus.model.Question;
 import be.noselus.repository.DeputyRepository;
 import be.noselus.repository.DeputyRepositoryInMemory;
 import org.joda.time.LocalDate;
 import org.joda.time.format.DateTimeFormatter;
 import org.joda.time.format.DateTimeFormatterBuilder;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.junit.Assert;
 import org.junit.Test;
 
 import java.io.IOException;
 import java.net.URL;
 import java.util.List;
 
 public class QuestionParserTest {
 
     DeputyRepository deputyRepository = new DeputyRepositoryInMemory();
     QuestionParser parser = new QuestionParser(deputyRepository);
 
 	@Test
 	public void openData() throws IOException {
 		String url = "http://parlement.wallonie.be/content/print_container.php?print=quest_rep_voir.php&id_doc=36256&type=all";
 		Question qr = parser.parse(url);
 		
 		Assert.assertEquals("l'\"Open Data - Open Government\"", qr.title);
 		Assert.assertEquals("2010-2011", qr.session);
 		Assert.assertEquals(2011, qr.year.intValue());
 		Assert.assertEquals("594 (2010-2011) 1", qr.number);
 		Assert.assertEquals("2011-08-29", qr.date_asked.toString());
 		Assert.assertEquals("DISABATO Emmanuel", qr.asked_by.toString());
 		Assert.assertEquals("FURLAN Paul", qr.asked_to.toString());
 		Assert.assertEquals("2011-10-07", qr.date_answered.toString());
 		Assert.assertEquals("FURLAN Paul", qr.answered_by.toString());
 		Assert.assertEquals(2596, qr.questionText.length());
 		Assert.assertEquals(5663, qr.answerText.length());
 	}
 	
 	@Test
 	public void eolien() throws IOException {
 		String url = "http://parlement.wallonie.be/content/print_container.php?print=quest_rep_voir.php&id_doc=50370&type=28";
		Question qr = parser.parse(url);
 		
 		Assert.assertEquals("le coût élevé de l'éolien", qr.title);
 		Assert.assertEquals("2013-2014", qr.session);
 		Assert.assertEquals(2013, qr.year.intValue());
 		Assert.assertEquals("51 (2013-2014) 1", qr.number);
 		Assert.assertEquals("2013-10-04", qr.date_asked.toString());
 		Assert.assertEquals("STOFFELS Edmund", qr.asked_by.toString());
 		Assert.assertEquals("NOLLET Jean-Marc", qr.asked_to.toString());
 		Assert.assertEquals(null, qr.date_answered);
 		Assert.assertEquals(null, qr.answered_by);
 		Assert.assertEquals(2044, qr.questionText.length());
 		Assert.assertEquals(null, qr.answerText);
 	}
 	
 	@Test
 	public void texts() throws IOException {
 		String url = "http://parlement.wallonie.be/content/print_container.php?print=quest_rep_voir.php&id_doc=36256&type=all";
 		Document doc = Jsoup.parse(new URL(url).openStream(), "iso-8859-1", url);
 		
 		List<String> texts = parser.extract(doc, "div#print_container div + div");
 		
 		Assert.assertEquals(2596, texts.get(0).length());
 		Assert.assertEquals(5663, texts.get(2).length());
 	}
 	
 	@Test
 	public void parseDate() {
 		DateTimeFormatter dateFormatter = new DateTimeFormatterBuilder()
 			.appendDayOfMonth(2)
 			.appendLiteral('/')
 			.appendMonthOfYear(2)
 			.appendLiteral('/')
 			.appendYear(4, 4)
 			.toFormatter();
 		
 		LocalDate tmp = LocalDate.parse("29/08/2011", dateFormatter);
 		
 		System.out.println(tmp);
 	}
 }
