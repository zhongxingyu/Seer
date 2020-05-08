 package jp.tnasu.f1tvnews.controller;
 
 import java.io.IOException;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.ProtocolException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Map.Entry;
 import java.util.TimeZone;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import jp.tnasu.f1tvnews.client.F1bsfujiClient;
 import jp.tnasu.f1tvnews.client.google.calendar.GoogleCalendarClient;
 import jp.tnasu.f1tvnews.client.google.oauth2.GoogleClient;
 import jp.tnasu.f1tvnews.dao.HtmlDocumentDao;
 import jp.tnasu.f1tvnews.dao.google.calendar.GoogleCalendarDtoMapDao;
 import jp.tnasu.f1tvnews.handler.ErrorHandler;
 import jp.tnasu.f1tvnews.handler.RssHandler;
 import jp.tnasu.f1tvnews.model.HtmlContent;
 import jp.tnasu.f1tvnews.model.HtmlDocument;
 import jp.tnasu.f1tvnews.model.google.calendar.Event;
 import jp.tnasu.f1tvnews.model.google.calendar.GoogleCalendarDto;
 import jp.tnasu.f1tvnews.model.google.calendar.GoogleCalendarDtoMap;
 import jp.tnasu.f1tvnews.model.google.oauth2.Google;
 
 import org.slim3.controller.Controller;
 import org.slim3.controller.Navigation;
 import org.slim3.util.StringUtil;
 
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.Text;
 
 public class RssController extends Controller {
 
 	static final Logger LOGGER = Logger.getLogger(RssController.class.getName());
 
 	HtmlDocumentDao htmlDocumentDao = new HtmlDocumentDao();
 	F1bsfujiClient f1bsfujiClient = new F1bsfujiClient();
 
 	GoogleCalendarDtoMapDao googleCalendarDtoMapDao = new GoogleCalendarDtoMapDao();
 	GoogleClient googleClinet = new GoogleClient();
 	GoogleCalendarClient googleCalendarClient = new GoogleCalendarClient();
 
 	@Override
 	public Navigation run() throws Exception {
 		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Tokyo"));
 		Key key = htmlDocumentDao.getKey();
 		HtmlDocument htmlDocument = htmlDocumentDao.get(key);
 		long lastModified = 0L;
 		long nextModify = 1L;
 		if (htmlDocument.getHtmlContentList().size() != 0) {
 			HtmlContent latest = htmlDocument.getHtmlContentList().get(0);
 			lastModified = latest.getLastModified();
 			nextModify = latest.getNextModify();
 		}
 		LOGGER.severe("" + new Date(lastModified));
 		LOGGER.severe("" + new Date(nextModify));
 		String force = request.getParameter("force");
 		if (!StringUtil.isEmpty(force)) {
 			lastModified = 0L;
 		}
 		if (!StringUtil.isEmpty(force) || (lastModified < nextModify && nextModify < new Date().getTime())) {
 			HtmlContent htmlContent = new HtmlContent();
 			fillHtml(lastModified, htmlContent);
 			LOGGER.severe(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS").format(new Date(htmlContent.getLastModified())));
 			if (lastModified != htmlContent.getLastModified()) {
 				fillData(htmlContent);
 				fillNextModify(htmlContent);
 				fillDocument(htmlDocument, htmlContent);
 				htmlDocument.getHtmlContentList().add(0, htmlContent);
 				htmlDocumentDao.put(htmlDocument);
 				registGoogleCalendar(htmlDocument);
 			}
 		}
 		RssHandler.writeRss(response, htmlDocument);
 		return null;
 	}
 
 	private HtmlContent fillHtml(long lastModified, HtmlContent htmlContent) throws MalformedURLException, ProtocolException, IOException {
 		HttpURLConnection con = null;
 		try {
 			con = f1bsfujiClient.getConnection(lastModified);
 			htmlContent.setLastModified(con.getLastModified());
 			if (con.getResponseCode() == 200) {
 				htmlContent.setHtml(new Text(f1bsfujiClient.getContent(con)));
 			}
 			return htmlContent;
 		} finally {
 			if (con != null) {
 				con.disconnect();
 			}
 		}
 	}
 
 	private HtmlContent fillData(HtmlContent htmlContent) {
 		String html = htmlContent.getHtml().getValue();
 		int beginIndex = html.indexOf("<div id=\"containerInner\" class=\"clearfix\">");
 		if (beginIndex != -1) {
 			html = html.substring(beginIndex);
 		}
 		int endIndex = html.indexOf("<div id=\"rightArea\">");
 		if (endIndex != -1) {
 			html = html.substring(0, endIndex);
 		}
 		// LOGGER.severe(html);
 		htmlContent.setData(new Text(html));
 		htmlContent.setTitle(getString(html, "<div class=\"title\">", "</div>") + getString(html, "<div class=\"subtitle\">", "</div>"));
 		htmlContent.setDescription(getString(html, "<div class=\"midokoroIn\">", "</div>").replaceAll("<.+?>", " "));
 		htmlContent.setShowtime(getString(html, "<div class=\"midokoroIn\"> <p>", "</p>").replaceAll("<.+?>", " "));
 		if (htmlContent.getShowtime().indexOf(regexQualify) == -1 || htmlContent.getShowtime().indexOf(regexRace) == -1) {
 			htmlContent.setShowtime(getString(html, "<div class=\"free\">", "</div>").replaceAll("<br />", " ").replaceAll("<p>", " ").replace("</p>", " "));
 		}
 		fillAnyTime(htmlContent, html);
 		return htmlContent;
 	}
 
 	static final String regexQualify = "【予選】";
 
 	static final String regexRace = "【決勝】";
 
 	static final String regex = "([0-9]?[0-9]月[0-9]?[0-9]日)\\([月火水木金土日]{1}\\)[ ]?([0-9]?[0-9]:[0-9][0-9])～([0-9]?[0-9]:[0-9]?[0-9])";
 
 	private HtmlContent fillAnyTime(HtmlContent htmlContent, String html) {
 		try {
 			fillAnyTime(htmlContent, html, regex, regexQualify);
 			fillAnyTime(htmlContent, html, regex, regexRace);
 		} catch (ParseException e) {
 			LOGGER.severe(e.getLocalizedMessage());
 		}
 		return htmlContent;
 	}
 
 	private void fillAnyTime(HtmlContent htmlContent, String html, String regex, String prefix) throws ParseException {
 		Pattern pattern = Pattern.compile("(" + prefix + regex + ")");
 		Matcher matcher = pattern.matcher(html);
 		if (matcher.find()) {
 			String date = matcher.group(2);
 			String startTime = matcher.group(3);
 			String endTime = matcher.group(4);
 			SimpleDateFormat sdf = new SimpleDateFormat("MM月dd日HH:mm");
 			Date s = sdf.parse(date + startTime);
 			Date e = sdf.parse(date + endTime);
 			Calendar nowCal = Calendar.getInstance();
 			Calendar startCal = Calendar.getInstance();
 			Calendar endCal = Calendar.getInstance();
 			nowCal.setTime(new Date());
 			startCal.setTime(s);
 			startCal.set(Calendar.YEAR, nowCal.get(Calendar.YEAR));
 			endCal.setTime(e);
 			endCal.set(Calendar.YEAR, nowCal.get(Calendar.YEAR));
 			LOGGER.severe(new Date(startCal.getTimeInMillis()).toString());
 			LOGGER.severe(new Date(endCal.getTimeInMillis()).toString());
 			if (regexQualify.equals(prefix)) {
 				htmlContent.setQualifyStartTime(startCal.getTime());
 				htmlContent.setQualifyEndTime(endCal.getTime());
 			} else if (regexRace.equals(prefix)) {
 				htmlContent.setRaceStartTime(startCal.getTime());
 				htmlContent.setRaceEndTime(endCal.getTime());
 			}
 		}
 	}
 
 	private HtmlContent fillNextModify(HtmlContent htmlContent) {
 		if (htmlContent.getRaceEndTime() == null) {
 			return htmlContent;
 		}
 		Calendar next = Calendar.getInstance();
 		next.setTime(htmlContent.getRaceEndTime());
 		next.set(Calendar.HOUR, 9);
 		next.set(Calendar.MINUTE, 0);
 		if (next.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
 			next.add(Calendar.DATE, 1);
 		}
 		LOGGER.severe(new Date(next.getTimeInMillis()).toString());
 		htmlContent.setNextModify(next.getTimeInMillis());
 		return htmlContent;
 	}
 
 	private HtmlDocument fillDocument(HtmlDocument htmlDocument, HtmlContent htmlContent) {
 		String html = htmlContent.getHtml().getValue();
 		String description = getString(html, "<meta name=\"description\" content=\"", "\" />");
 		String title = getString(html, "<title>", "</title>");
 		htmlDocument.setTitle(title);
 		htmlDocument.setLink(F1bsfujiClient.ENDPOINT);
 		htmlDocument.setDescription(description);
 		htmlDocument.setLanguage("ja-JP");
 		htmlDocument.setCopyright("http://www.bsfuji.tv/top/index/copyright.html");
 		htmlDocument.setPublishedDate(new Date(htmlContent.getLastModified()));
 		return htmlDocument;
 	}
 
 	private String getString(String html, String prefix, String suffix) {
 		int beginIndex = html.indexOf(prefix);
 		if (beginIndex != -1) {
 			html = html.substring(beginIndex + prefix.length());
 		}
 		int endIndex = html.indexOf(suffix);
 		if (endIndex != -1) {
 			html = html.substring(0, endIndex);
 		}
 		// LOGGER.severe(html);
 		return html;
 	}
 
 	private void registGoogleCalendar(HtmlDocument htmlDocument) {
 		if (googleClinet.isInitialize()) {
 			return;
 		}
 		try {
 			HtmlContent htmlContent = htmlDocument.getHtmlContentList().get(0);
 			Key key = googleCalendarDtoMapDao.getKey();
 			GoogleCalendarDtoMap googleCalendarDtoMap = googleCalendarDtoMapDao.get(key);
 			for (Entry<String, GoogleCalendarDto> entry : googleCalendarDtoMap.getGoogleCalendarMap().entrySet()) {
 				Google google = entry.getValue().getGoogle();
 				LOGGER.severe("" + google.getClientId());
 				LOGGER.severe("" + google.getAccessToken());
 				LOGGER.severe("" + google.getRefreshToken());
 				Event eventQualify = googleCalendarClient.getEvent(htmlContent.getTitle(), htmlContent.getQualifyStartTime(), htmlContent.getQualifyEndTime());
 				Event eventRace = googleCalendarClient.getEvent(htmlContent.getTitle(), htmlContent.getRaceStartTime(), htmlContent.getRaceEndTime());
 				for (Event event : new Event[] { eventQualify, eventRace }) {
 					LOGGER.severe("" + event.getSummary());
 					LOGGER.severe("" + event.getStart().getModel().getDateTime());
 					entry.getValue().getGoogle().getRefreshToken();
 					String[] calendarIds = entry.getValue().getCalendarList();
 					for (String calendarId : calendarIds) {
 						LOGGER.severe(calendarId);
 						HttpURLConnection http = googleCalendarClient.getEventInsert(google, event, calendarId);
 						String content = googleCalendarClient.getContent(http);
 						LOGGER.severe(content);
 						if (http.getResponseCode() != 200) {
 							// FIXME no test
 							// jp.tnasu.f1tvnews.model.google.calendar.Error error = ErrorMeta.get().jsonToModel(content);
 							// System.out.println(error.getCode());
 							// System.out.println(error.getMessage());
 							// if (error.getErrors() != null) {
 							// ErrorDetail[] errors = ErrorDetailMeta.get().jsonToModels(error.getErrors());
 							// for (ErrorDetail e : errors) {
 							// System.out.println(e.getDomain());
 							// System.out.println(e.getReason());
 							// System.out.println(e.getMessage());
 							// System.out.println(e.getLocationType());
 							// System.out.println(e.getLocation());
 							// }
 							// }
 						}
 					}
 				}
 			}
 		} catch (Exception e) {
 			ErrorHandler.writeErrorLogger(e, LOGGER);
 		}
 	}
 
 }
