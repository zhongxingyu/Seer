 package alpv.mwp.crawler;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class URLParser {
 
 	private InputStream _content;
 	List<HttpURL> _urls;
 	List<String> _mailTos;
 	HttpURL _url;
 	private Map<String, Boolean> _checkedURLs;
 
 	public URLParser(HttpURL url, Map<String, Boolean> checkedURLs) {
 
 		_urls = new ArrayList<HttpURL>();
 		_mailTos = new ArrayList<String>();
 		_checkedURLs = checkedURLs;
 		try {
 			_url = url;
 			_content = _url.openConnection().getContent();
			if (_content != null && _content.available() > 0) {
 				BufferedReader contentReader = new BufferedReader(
 						new InputStreamReader(_content));
 				parse(contentReader);
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void parse(BufferedReader reader) throws IOException {
 		Pattern p = Pattern
 				.compile("href=(\"|')((http:[^\"']*)|(mailto:[^\"']*)|([^\"^:']*))(\"|')");
 		String line;
 		Matcher m;
 		String url;
 
 		while ((line = reader.readLine()) != null) {
 			// System.out.println(line);
 			m = p.matcher(line);
 			while (m.find()) {
 				url = m.group(2);
 				if (url.startsWith("http")) {
 					addURL(url);
 				} else if (url.startsWith("mailto")) {
 					addMailTo(url);
 				} else if (url.startsWith("#")) {
 //					System.out.println("bad url detected: " + url);
 				} else if (url.startsWith("/")) {
 					addURL("http://" + _url.getHost() + url);
 				} else {
 					addURL("http://" + _url.getHost() + _url.getPath() + url);
 				}
 			}
 		}
 	}
 
 	private void addMailTo(String url) {
 		// System.out.println("mailto detected: " + url);
 		//TODO 
 		_mailTos.add(url);
 	}
 
 	private void addURL(String url) {
 		// System.out.println("http url detected: " + url);
 		if (!_checkedURLs.containsKey(url)) {
 //			System.out.println(url + " not checked. Adding to argument pool");
 			_checkedURLs.put(url, null);
 			_urls.add(new HttpURLImpl(url));
 		}
 	}
 
 	public List<HttpURL> get_urls() {
 		return _urls;
 	}
 
 	public List<String> get_mailTos() {
 		return _mailTos;
 	}
 
 }
