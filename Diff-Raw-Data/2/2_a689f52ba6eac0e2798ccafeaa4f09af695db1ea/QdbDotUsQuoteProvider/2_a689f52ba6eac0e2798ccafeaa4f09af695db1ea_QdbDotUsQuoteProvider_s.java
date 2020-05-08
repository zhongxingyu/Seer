 package fr.quoteBrowser.service.provider;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 import android.text.Html;
 import fr.quoteBrowser.Quote;
 
 public class QdbDotUsQuoteProvider extends AbstractQuoteProvider{
 	private static final int START_PAGE = 1;
 
 	@Override
 	public List<Quote> getQuotesFromPage(int pageNumber) throws IOException {
 		
		return getQuotesFromURL("http://qdb.us/latest/" +pageNumber+START_PAGE);
 	}
 
 	private List<Quote> getQuotesFromURL(String url) throws IOException {
 		ArrayList<Quote> quotes = new ArrayList<Quote>();
 		Document doc = getDocumentFromUrl(url);
 		Elements quotesElts = doc.select("td.q");
 		for (Element quotesElt : quotesElts) {
 			CharSequence quoteTitle = Html.fromHtml(quotesElt
 					.select("a.ql").first().ownText());
 			CharSequence quoteScore =  Html.fromHtml(quotesElt
 					.select("span").first().ownText());
 			CharSequence quoteText = Html.fromHtml(quotesElt
 					.select("span.qt").first().html());
 			Quote quote = new Quote(quoteText);
 			quote.setQuoteTitle(quoteTitle);
 			quote.setQuoteSource("qdb.us");
 			quote.setQuoteScore(quoteScore);
 			quotes.add(quote);
 		}
 		return quotes;
 	}
 
 	@Override
 	public QuoteProviderPreferencesDescription getPreferencesDescription() {
 		return new QuoteProviderPreferencesDescription("qdbdotus_preference",
 				"qsb.us", "Enable qdb.us provider");
 	}
 
 	@Override
 	public boolean supportsUsernameColorization() {
 		return true;
 	}
 
 
 
 }
