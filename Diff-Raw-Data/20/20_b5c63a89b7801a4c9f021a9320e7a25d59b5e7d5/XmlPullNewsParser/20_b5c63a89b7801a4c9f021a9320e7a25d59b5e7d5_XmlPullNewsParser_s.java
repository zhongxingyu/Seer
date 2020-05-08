 package duyhung.news.dao;
 
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.xmlpull.v1.XmlPullParser;
 import org.xmlpull.v1.XmlPullParserException;
 import org.xmlpull.v1.XmlPullParserFactory;
 
 import duyhung.news.CategoryFragment;
 import duyhung.news.model.NewsItem;
 import duyhung.news.model.Variables;
 
 public class XmlPullNewsParser {
 
 	public List<NewsItem> getNewsList(String inputLink) {
 
 		String[] result = new String[CategoryFragment.ITEM_PER_PAGE * 4];
 		int iterator = 0;
 
 		try {
 
 			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
 			factory.setNamespaceAware(true);
 			XmlPullParser parser = factory.newPullParser();
 			parser.setInput(new URL(inputLink).openStream(), "UTF-8");
 
 			int type = parser.getEventType();
 
 			while (type != XmlPullParser.START_TAG || !Variables.ITEM.equals(parser.getName()) && type != XmlPullParser.END_DOCUMENT) {
 				type = parser.next();
 			}
 
 			do {
 
 				while (type != XmlPullParser.START_TAG && type != XmlPullParser.END_DOCUMENT) {
 					type = parser.next();
 				}
 
 				if (Variables.TITLE.equals(parser.getName()) | Variables.DESCRIPTION.equals(parser.getName()) | Variables.DATE.equals(parser.getName()) | Variables.LINK.equals(parser.getName())) {
 					result[iterator++] = getElementText(parser);
 				}
 
 				type = parser.next();
 
 			} while (type != XmlPullParser.END_DOCUMENT && iterator < result.length);
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
		return convertToList(result);
 	}
 
 	public List<NewsItem> getMoreItem(String inputLink, List<NewsItem> list, int start, int end) {
 		try {
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return list;
 	}
 
 	private String getElementText(XmlPullParser parser) throws XmlPullParserException, IOException {
 		parser.next();
 		if (parser.getEventType() == XmlPullParser.TEXT) {
 			return parser.getText();
 		}
 		return "";
 	}
 
 	private List<NewsItem> convertToList(String[] rs) {
 		List<NewsItem> list = new ArrayList<NewsItem>();
 
 		for (int i = 0; i < rs.length; i += 4) {
 			if (!"".equals(rs[i]) && rs[i] != null) {
 				NewsItem item = new NewsItem();
 				item.setTitle(rs[i]);
 				item.setDescription(rs[i + 1]);
 				item.setPubDate(rs[i + 2]);
 				item.setLink(rs[i + 3]);
 				list.add(item);
 			}
 		}

 		return list;
 	}
 
 }
