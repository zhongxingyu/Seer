 package ru.yinfag.chitose;
 
 import org.jivesoftware.smack.packet.Message;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.net.URL;
 import java.net.MalformedURLException;
 import java.net.URLEncoder;
 import java.net.URLDecoder;
 import java.net.URLConnection;
 import java.util.Scanner;
 import java.io.IOException;
 import java.util.Properties;
 
 /**
  * Google Message Processor for Chitose
  * 
  * You MUST specify 3 properties for this class in chitose.cfg
  * Example:
  * 	httpUserAgent=Mozilla/5.0 (X11; Linux i686; rv:11.0) Gecko/20100101 Firefox/11.0
  * 	httpAcceptLanguage=ru-RU
  * 	googleDomain=www.google.ru
  * 
  * I don't use Google API for fetching search results, because I can not into that =(
  * 
  * @author ryoukura
  */
  
 public class GoogleMessageProcessor implements MessageProcessor {
 	
 	private static final Pattern COMMAND_PATTERN = Pattern.compile(
		"^ *(?:по)*[gг](?:угл|oogle)*и* +(.*)$",
 		Pattern.CASE_INSENSITIVE + Pattern.UNICODE_CASE		
 	);
 	
 	private static final Pattern GOOGLE_RESULT_PATTERN = Pattern.compile(
 		"^<a href=\"([^\"]+)\"[^<>]+>(.+)<cite>.*<span class=\"st\">(.*)$",
 		//using MULTILINE and DOTALL flags because google results page is multiline
 		Pattern.MULTILINE + Pattern.DOTALL
 	);	
 	
 	private final String httpUserAgent;
 	
 	private final String httpAcceptLanguage;
 	
 	private final String googleDomain;
 	
 	public GoogleMessageProcessor(final Properties props) {
 		httpUserAgent = props.getProperty("httpUserAgent");
 		httpAcceptLanguage = props.getProperty("httpAcceptLanguage");
 		googleDomain = props.getProperty("googleDomain");
 	}
 	
 	@Override
 	public CharSequence process(final Message message) throws MessageProcessingException {
 		final Matcher matcher = COMMAND_PATTERN.matcher(message.getBody());		
 		if (matcher.matches()) {
 			String term = matcher.group(1);
 			final URL url;
 			term = URLEncoder.encode(term);
 			try {
 				url = new URL("http://" + googleDomain + "/search?q=" + term);
 			} catch (MalformedURLException e) {
 				throw new MessageProcessingException(e);
 			}
 			final URLConnection conn;
 			try {
 				conn = url.openConnection();
 			} catch (IOException e) {
 				throw new MessageProcessingException(e);
 			}
 			conn.setRequestProperty("Referer", "http://" + googleDomain + "/");
 			conn.setRequestProperty("User-Agent", httpUserAgent);
 			conn.setRequestProperty("Accept-Language", httpAcceptLanguage);
 			String html;
 			try {
 				//This trick is from http://weblogs.java.net/blog/pat/archive/2004/10/stupid_scanner.html
 				//I can't find other simple usable method for reading stream as string
 				html = new Scanner(conn.getInputStream(), "UTF-8").useDelimiter("\\A").next();
 			} catch (IOException e) {
 				throw new MessageProcessingException(e);
 			}
 			//Here we are extracting first item from google results using indexOf() and substring().
 			//I can't make this with single regexp, because lookahead and lookbehind constructs
 			//don't want to work. For example (?<!<h3 class=\"r\">) must lookbehind but it ignored.
 			//Someone please help me to write regexp with lookbehind for extracting first
 			//item between <h3 class="r"> and <h3 class="r">
 			final int googleResultStartPos = html.indexOf("<h3 class=\"r\">");
 			html = html.substring(googleResultStartPos + 14, html.length());
 			final int googleResultEndPos = html.indexOf("<h3 class=\"r\">");			
 			html = html.substring(0, googleResultEndPos);
 
 			final Matcher gmatcher = GOOGLE_RESULT_PATTERN.matcher(html);	
 			if (gmatcher.matches()) {
 				String link = gmatcher.group(1);
 				link = URLDecoder.decode(link);
 				String content = gmatcher.group(2) + " " + gmatcher.group(3);
 				//And here we clear all html formatting and delete all html encoded characters. Why?
 				//Of course because I can not into html decoding...			
 				content = content.replaceAll("<[^<>]+>", " ").replaceAll("&[^&; ]+;", " ").replaceAll(" +", " ");
 				return content + " " + link;
 			}
 			return "что-то сломалось, сообщите дивелоуперам~";
 		}
 		else {
 			return null;
 		}
 	}
 }
