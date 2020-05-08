 package cn.edu.zju.plex.tdd.module;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import cn.edu.zju.plex.tdd.dao.DB4Tdd;
 import cn.edu.zju.plex.tdd.entity.RssNews;
 
 /**
  * 天涯小筑中简讯的拆分类
  * <p>
  * url-pattern: http://tvfantasy.net/2013/04/12/newsletter[^#]+
  * <p>
  * 提取页面中讲不同剧集的段落，分开存储,分开后的页面会在link末尾加上#和数字编码
  * 
  * @author WangFengwei
  */
 public class TvfantasySpliter {
 	private static final Logger LOG = Logger.getLogger(TvfantasySpliter.class);
 
 	/**
 	 * split page, remove origin, save spliters
 	 * 
 	 * @param rssNews
 	 */
 	public static void splite(RssNews rssNews) {
 		LOG.info("split rssNews" + rssNews);
 
 		ArrayList<RssNews> res = new ArrayList<RssNews>();
 		Document doc = Jsoup.parse(rssNews.getPage());
 		Element article = doc.getElementsByTag("article").get(0);
 
 		ArrayList<Element> eles = new ArrayList<Element>();
 
 		for (Element e : article.children()) {
 			eles.add(e);
 			e.remove();
 		}
 
 		for (int i = 0; i < eles.size(); i++) {
 			article = doc.getElementsByTag("article").get(0);
 			for (Element e : article.children()) {
 				e.remove();
 			}
			while (i < eles.size() && !eles.get(i).text().contains("●"))
 				i++;
			if (i >= eles.size())
				break;
 			article.appendChild(eles.get(i));
 			while (i + 1 < eles.size() && !eles.get(i + 1).text().contains("●")) {
 				i++;
				if (eles.get(i).text().length() > 10)
 					article.appendChild(eles.get(i));
 			}
 			RssNews newbee = rssNews.clone();
 			newbee.setTitle("");
 			newbee.setPage(doc.html());
 			newbee.setId(-1);
 			newbee.setLink(newbee.getLink() + "#" + i);
 			res.add(newbee);
 		}
 
 		if (res.size() > 0) {
 			for (RssNews rn : res) {
 				LOG.info("insert rssNews" + rn);
 				DB4Tdd.insertRssNews(rn);
 			}
 			LOG.info("Delete rssNews from database:" + rssNews);
 			DB4Tdd.delete(rssNews);
		} else {
			rssNews.setLink(rssNews.getLink() + "#fail");
 			LOG.info("no elements to split:" + rssNews.getLink());
		}
 	}
 
 	public static void splitAll(String linkReg) {
 		while (true) {
 			List<RssNews> rssNewsToSplit = DB4Tdd
 					.getRssNewsToSplit(30, linkReg);
 			LOG.info("Get " + rssNewsToSplit.size() + " rss items to split...");
 
 			if (rssNewsToSplit.size() == 0) {
 				LOG.info("All done");
 				break;
 			} else {
 				for (RssNews rssNews : rssNewsToSplit) {
 					splite(rssNews);
 				}
 			}
 		}
 	}
 
 	// TODO 测试一下分解所有文章，看看会不会出错
 	public static void main(String args[]) {
 		String linkReg = "http://tvfantasy.net/.{10}/newsletter[^#]*";
 		splitAll(linkReg);
 	}
 
 }
