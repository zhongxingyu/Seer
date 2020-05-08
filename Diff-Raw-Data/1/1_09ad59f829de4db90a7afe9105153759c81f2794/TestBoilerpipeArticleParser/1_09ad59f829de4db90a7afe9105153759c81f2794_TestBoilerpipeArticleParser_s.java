 package com.cee.news.parser.impl;
 
 import static org.junit.Assert.assertTrue;
 
 import java.io.IOException;
 
 import org.ccil.cowan.tagsoup.Parser;
 import org.junit.Ignore;
 import org.junit.Rule;
 import org.junit.Test;
 
 import betamax.Betamax;
 import betamax.Recorder;
 import betamax.TapeMode;
 
 import com.cee.news.model.Article;
 import com.cee.news.parser.ArticleParser;
 import com.cee.news.parser.ParserException;
 import com.cee.news.parser.net.ClassResourceWebClient;
 import com.cee.news.parser.net.impl.DefaultWebClient;
 import com.cee.news.parser.net.impl.XmlStreamReaderFactory;
 
 public class TestBoilerpipeArticleParser {
 	
 	private static final String ARTICLE_START_TEXT = "Russischer ";
 	private static final String ARTICLE_LOCATION = "http://www.test.com/com/cee/news/parser/impl/spiegelArticle.html";
 
 	@Rule
 	public Recorder recorder = new Recorder();
 
     @Test
     public void testParse() throws ParserException, IOException {
         Article article = new Article();
         article.setLocation(ARTICLE_LOCATION);
         ArticleParser parser = new BoilerpipeArticleParser(new Parser(), new ClassResourceWebClient());
         parser.parse(article);
         assertTrue(article.getContent().get(0).getContent().startsWith(ARTICLE_START_TEXT));
     }
 
 	@Betamax(tape = "issue120", mode = TapeMode.READ_ONLY)
 	@Test
 	public void testParseRegressionIssue120() throws ParserException, IOException {
 		Article article = new Article();
         article.setLocation("http://www.swr.de/nachrichten/-/id=396/nid=396/did=8825882/1u2s8qj/index.html");
         
         ArticleParser parser = new BoilerpipeArticleParser(new Parser(), new DefaultWebClient(HttpClientFactory.createHttpClient(), new XmlStreamReaderFactory()));
         parser.parse(article);
         assertTrue(article.getContent().get(0).getContent().contains("Kein Referendum, kein Rücktritt, keine Lösung"));
 	}
 	
 	@Betamax(tape = "issue144", mode = TapeMode.READ_ONLY)
 	@Test
 	public void testParseRegressionIssue144() throws ParserException, IOException {
 		Article article = new Article();
         article.setLocation("http://www.spiegel.de/wirtschaft/unternehmen/0,1518,797729,00.html");
         
         ArticleParser parser = new BoilerpipeArticleParser(new Parser(), new DefaultWebClient(HttpClientFactory.createHttpClient(), new XmlStreamReaderFactory()));
         parser.parse(article);
         assertTrue(article.getContent().get(0).getContent().contains("die Polizei durchsucht das Büro"));
 	}
 	
 	@Ignore("The server does not send a UTF-8 Content-Type header, betamax is also unable to detect the charset encoding...")
 	//@Betamax(tape = "issue145", mode = TapeMode.READ_WRITE)
 	@Test
 	public void testParseRegressionIssue145() throws ParserException, IOException {
 		Article article = new Article();
         article.setLocation("http://www.swr.de/nachrichten/-/id=396/nid=396/did=8892142/1aevpcg/index.html");
         
         ArticleParser parser = new BoilerpipeArticleParser(new Parser(), new DefaultWebClient(HttpClientFactory.createHttpClient(), new XmlStreamReaderFactory()));
         parser.parse(article);
         assertTrue(article.getContent().get(0).getContent().contains("täglich"));
 	}
 	
 	@Betamax(tape = "issue146", mode = TapeMode.READ_ONLY)
 	@Test
 	public void testParseRegressionIssue146() throws ParserException, IOException {
 		Article article = new Article();
         article.setLocation("	http://www.tagesspiegel.de/politik/troika-sieht-portugal-auf-gutem-weg/5859778.html");
         
         ArticleParser parser = new BoilerpipeArticleParser(new Parser(), new DefaultWebClient(HttpClientFactory.createHttpClient(), new XmlStreamReaderFactory()));
         parser.parse(article);
         assertTrue(article.getContent().get(0).getContent().contains("Die Portugiesen selbst sind weniger optimistisch"));
 	}
 }
