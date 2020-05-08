 package com.gyagapen.mrunews.parser;
 
 import java.io.IOException;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 import android.os.StrictMode;
 
 import com.gyagapen.mrunews.common.LogsProvider;
 import com.gyagapen.mrunews.common.StaticValues;
 import com.gyagapen.mrunews.entities.ArticleContent;
 import com.gyagapen.mrunews.entities.SemdexEntities;
 import com.gyagapen.mrunews.entities.SemdexEntity;
 
 public class HTMLPageParser {
 
 	private String linkToParse;
 	private String parseType;
 
 	static final String PING_HOST_1 = "http://www.google.com/";
 	static final String PING_HOST_2 = "http://fr.yahoo.com/";
 
 	private LogsProvider logsProvider = null;
 
 	public HTMLPageParser(String linkToParse, String parseType) {
 		super();
 		this.linkToParse = linkToParse;
 		this.parseType = parseType;
 		logsProvider = new LogsProvider(null, this.getClass());
 	}
 
 	public String getLinkToParse() {
 		return linkToParse;
 	}
 
 	public void setLinkToParse(String linkToParse) {
 		this.linkToParse = linkToParse;
 	}
 
 	public String getParseType() {
 		return parseType;
 	}
 
 	public void setParseType(String parseType) {
 		this.parseType = parseType;
 	}
 
 	public ArticleContent parsePage() throws Exception {
 		ArticleContent artContent = new ArticleContent();
 
 		if (parseType.equals(StaticValues.LEXPRESS_CODE)
 				|| parseType.equals(StaticValues.CINQPLUS_CODE)) {
 			artContent = parseExpressPage();
 		} else if (parseType.equals(StaticValues.LEMAURICIEN_CODE)) {
 			artContent = parseLeMauricienPage();
 
 		} else if (parseType.equals(StaticValues.DEFI_PLUS_CODE)) {
 			artContent = parseDefiPlusPage();
 		} else if (parseType.equals(StaticValues.lE_MATINAL_CODE)) {
 			artContent = parseLeMatinalPage();
 		}
 
 		return artContent;
 	}
 
 	private ArticleContent parseLeMauricienPage() throws Exception {
 
 		ArticleContent artContent = new ArticleContent();
 
 		// get page content
 		Document doc = Jsoup.connect(linkToParse).timeout(10 * 2500).get();
 
 		// get image
 		artContent.setImageLink(getImageFromLink(linkToParse,
 				StaticValues.LEMAURICIEN_CODE, doc));
 
 		// html content
 		String htmlContent = doc.select("div.body-content").first().html();
 
 		// add extra spaces to html content
 		Pattern pHtml = Pattern.compile("<br \\/>");
 		Matcher matcherHtml = pHtml.matcher(htmlContent);
 		htmlContent = matcherHtml.replaceAll("<br \\/><br \\/>");
 		artContent.setHtmlContent(htmlContent);
 
 		// get Title
 		Element TitleElement = doc.select("h1").first();
 		String title = TitleElement.text();
 		artContent.setTitle(title);
 
 		return artContent;
 	}
 
 	private ArticleContent parseExpressPage() throws Exception {
 
 		ArticleContent artContent = new ArticleContent();
 
 		// get page content
 		Document doc = Jsoup.connect(linkToParse).timeout(10 * 2500).get();
 
 		// get image
 		artContent.setImageLink(getImageFromLink(linkToParse,
 				StaticValues.LEXPRESS_CODE, doc));
 		
 		// css link
 		String cssLexpress = "<link href=\"http://www.lexpress.mu/sites/all/themes/lexpress_desk1/css/global.css\" rel=\"stylesheet\">";
 
 
		// html content
		String htmlContent = doc.select("article div.content").first().html();
 		
 		
 		
 		//cater for youtube links
 		Pattern paragraph = Pattern.compile("src=\"//");
 		Matcher sourceMatcher = paragraph.matcher(htmlContent);
 		htmlContent = sourceMatcher
 				.replaceAll("src=\"http://");
 		
 		//cater for images
 		paragraph = Pattern.compile("src=\"/sites");
 		sourceMatcher = paragraph.matcher(htmlContent);
 		htmlContent = sourceMatcher
 				.replaceAll("src=\"http://www.lexpress.mu/sites");
 		
 		artContent.setHtmlContent(cssLexpress+htmlContent);
 		
 
 		// get Title
 		Element TitleElement = doc.select("h1#page-title").first();
 		String title = TitleElement.text();
 		artContent.setTitle(title);
 
 		
 		// comments in html format
 		Element htmlComments = doc.select("div#comments").first();
 		if (htmlComments != null) {
 			artContent.setCommentsHtml(cssLexpress + htmlComments.html());
 		}
 
 		// count number of comments
 		int commentCount = doc.select("div#comments h3 a").size();
 		artContent.setCommentCount(commentCount);
 
 		return artContent;
 	}
 
 	private ArticleContent parseDefiPlusPage() throws Exception {
 
 		ArticleContent artContent = new ArticleContent();
 
 		// get page content
 		Document doc = Jsoup.connect(linkToParse).timeout(10 * 2500).get();
 
 		// get image
 		artContent.setImageLink(getImageFromLink(linkToParse,
 				StaticValues.DEFI_PLUS_CODE, doc));
 
 		// html content
 		 Element htmlContentElt = doc.select("div.itemImageBlock").first();
 		//remove iframe
 		 htmlContentElt.select("iframe").remove();
 		 String htmlContent = htmlContentElt.html();
 
 		htmlContent += doc.select("div.itemIntroText").first().html();
 		htmlContent += doc.select("div.itemFullText").first().html();
 
 		
 		
 		// cater for images sources
 		Pattern paragraph = Pattern.compile("href=\"/media");
 		Matcher sourceMatcher = paragraph.matcher(htmlContent);
 		htmlContent = sourceMatcher
 				.replaceAll("href=\"http://www.defimedia.info/media");
 
 		paragraph = Pattern.compile("src=\"/media");
 		sourceMatcher = paragraph.matcher(htmlContent);
 		htmlContent = sourceMatcher
 				.replaceAll("src=\"http://www.defimedia.info/media");
 
 		artContent.setHtmlContent(htmlContent);
 
 		// get Title
 
 		Element TitleElement = doc.select("h1.itemTitle").first();
 		String title = TitleElement.text();
 		artContent.setTitle(title);
 
 		// disqus iframe
 		String commentsIframeUrl = "http://disqus.com/embed/comments/?base=default&disqus_version=1e42086a&f=ledefimediagroup&t_u="
 				+ linkToParse;
 
 		// get comments count
 		String countCommnetUrl = "http://ledefimediagroup.disqus.com/count-data.js?2="
 				+ linkToParse;
 		Document countCommentDoc = Jsoup.connect(countCommnetUrl)
 				.timeout(10 * 2500).get();
 		artContent.setCommentsIframeLink(commentsIframeUrl);
 
 		String countText = countCommentDoc.text();
 		Pattern p = Pattern.compile("[0-9]+");
 		Matcher m = p.matcher(countText);
 		int commentCount = 0;
 		while (m.find()) {
 			commentCount = Integer.parseInt(m.group());
 		}
 		artContent.setCommentCount(commentCount);
 
 		return artContent;
 	}
 
 	private ArticleContent parseLeMatinalPage() throws Exception {
 
 		ArticleContent artContent = new ArticleContent();
 
 		// get page content
 		Document doc = Jsoup.connect(linkToParse).timeout(10 * 2500).get();
 
 		// get image
 		artContent.setImageLink(getImageFromLink(linkToParse,
 				StaticValues.lE_MATINAL_CODE, doc));
 
 		// html content
 		String htmlContent = doc.select("div#article_body").first().html();
 		artContent.setHtmlContent(htmlContent);
 
 		// get Title
 
 		Element TitleElement = doc.select("div#article_holder h1").first();
 		String title = TitleElement.text();
 		artContent.setTitle(title);
 
 		// count number of comments
 		int commentCount = doc.select("li.post").size();
 
 		return artContent;
 	}
 
 	public static String getImageFromLink(String url, String newsCode,
 			Document doc) throws Exception {
 		String imageLink = "";
 
 		try {
 
 			// get page content if not given
 			if (doc == null) {
 				doc = Jsoup.connect(url).timeout(10 * 2500).get();
 			}
 
 			if (newsCode.equals(StaticValues.LEMAURICIEN_CODE)) {
 
 				// get image
 				Element imgElement = doc.select("div.main-image img").first();
 				imageLink = imgElement.attr("src").toString();
 			} else if (newsCode.equals(StaticValues.LEXPRESS_CODE)
 					|| newsCode.equals(StaticValues.CINQPLUS_CODE)) {
 				// get image
 				Element imgElement = doc.select("div.field-items img[src]")
 						.first();
 				imageLink = imgElement.attr("src").toString();
 
 			} else if (newsCode.equals(StaticValues.DEFI_PLUS_CODE)) {
 				// get image
 				Element imgElement = doc.select("span.itemImage img[src]")
 						.first();
 				imageLink = "http://www.defimedia.info"
 						+ imgElement.attr("src").toString();
 			} else if (newsCode.equals(StaticValues.lE_MATINAL_CODE)) {
 				// get image
 				Element imgElement = doc.select("div.image img[src]").first();
 				imageLink = imgElement.attr("src").toString();
 
 			}
 		} catch (Exception ex) {
 			imageLink = "";
 		}
 
 		return imageLink;
 	}
 
 	static public boolean isInternetConnectionAvailable(
 			LogsProvider logsProvider) {
 		boolean isInternetAvailaible = false;
 
 		// avoid androidblockguard policy error
 		if (android.os.Build.VERSION.SDK_INT > 9) {
 			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
 					.permitAll().build();
 			StrictMode.setThreadPolicy(policy);
 		}
 
 		URL url;
 		try {
 			url = new URL(PING_HOST_1);
 			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
 			// 3 seconds time out
 			conn.setConnectTimeout(3000);
 
 			int responseCode = conn.getResponseCode();
 
 			isInternetAvailaible = (responseCode == 200);
 
 		} catch (MalformedURLException e) {
 			logsProvider.info("net check err : " + e.getMessage());
 			isInternetAvailaible = false;
 		} catch (IOException e) {
 			logsProvider.info("net check err : " + e.getMessage());
 			isInternetAvailaible = false;
 		}
 
 		logsProvider.info("Internet connection check: " + isInternetAvailaible);
 
 		return isInternetAvailaible;
 	}
 
 	public SemdexEntities getSemdexList(boolean isSemdex) throws IOException {
 
 		SemdexEntities semdexEntities = new SemdexEntities();
 
 		ArrayList<SemdexEntity> semdexList = new ArrayList<SemdexEntity>();
 
 		// get page content
 		Document doc = Jsoup.connect(linkToParse).timeout(10 * 2500).get();
 
 		// get last updated value
 		Elements lastUpdatedElt = doc.select("div.last.updated");
 		String lastUpdatedValue = lastUpdatedElt.text();
 		semdexEntities.setLastUpdated(lastUpdatedValue);
 
 		// get equity board table rows
 		Elements equityBoardRows = new Elements();
 
 		if (isSemdex) {
 			equityBoardRows = doc.select("div.officialquote tr");
 		} // else DEM
 		else {
 			equityBoardRows = doc.select("div.demquote tr");
 		}
 
 		// for each row
 		for (int i = 1; i < equityBoardRows.size(); i++) {
 			SemdexEntity semdexEntity = new SemdexEntity();
 
 			// name
 			String semdexName = equityBoardRows.get(i).select("td").get(0)
 					.text();
 			semdexEntity.setName(semdexName);
 
 			// nomimal
 			String semdexNominal = equityBoardRows.get(i).select("td").get(2)
 					.text();
 			semdexEntity.setNominal(semdexNominal);
 
 			// last closing price
 			String semdexLCPrice = equityBoardRows.get(i).select("td").get(3)
 					.text();
 			semdexEntity.setLastClosingPrice(semdexLCPrice);
 
 			// latest price
 			String semdexLatestPrice = equityBoardRows.get(i).select("td")
 					.get(4).text();
 			semdexEntity.setLatestPrice(semdexLatestPrice);
 
 			// change price
 			String semdexChangePrice = equityBoardRows.get(i).select("td")
 					.get(5).text();
 			semdexEntity.setChangePrice(semdexChangePrice);
 
 			// change percentage
 			String semdexChangePercentage = equityBoardRows.get(i).select("td")
 					.get(6).text();
 			semdexEntity.setChangePercentage(semdexChangePercentage);
 
 			semdexList.add(semdexEntity);
 		}
 
 		semdexEntities.setSemdexEntities(semdexList);
 		return semdexEntities;
 	}
 
 }
