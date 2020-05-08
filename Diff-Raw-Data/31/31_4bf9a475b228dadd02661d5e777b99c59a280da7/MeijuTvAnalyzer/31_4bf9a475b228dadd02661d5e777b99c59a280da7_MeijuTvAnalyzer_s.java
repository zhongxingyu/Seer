 package cn.edu.zju.plex.tdd.module;
 
 import java.io.FileNotFoundException;
 import java.io.PrintWriter;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.log4j.Logger;
 
 import cn.edu.zju.plex.tdd.dao.DB4Tdd;
 import cn.edu.zju.plex.tdd.entity.RssNews;
 import cn.edu.zju.plex.tdd.entity.TvShows;
 import cn.edu.zju.plex.tdd.seg.MyICTCLAS;
 
 public class MeijuTvAnalyzer {
 	// TODO 考虑更新问题，不要使用static
 	private static final HashMap<String, TvShows> Meijus = DB4Tdd.getMeijuTvs();
 	private static final Logger LOG = Logger.getLogger(MeijuTvAnalyzer.class);
 	private static final Pattern FenciResPatt = Pattern
 			.compile("([^/]+)/([^\\s]+)");
 
 	/**
 	 * 检测文本所属剧集，成功返回预测的TvShows，否则返回空（如果检测到多个剧集，也返回空）
 	 * 
 	 * @param content
 	 * @return
 	 */
 	public static TvShows guessTv(String content) {
 		if (content == null || content.length() < 1)
 			content = "没 有 内 容";
 		else
 			content = content.toLowerCase();
 		// 根据剧名进行分类
 		HashMap<TvShows, Double> guessList = new HashMap<TvShows, Double>();
 		// 美女上错身/aka 不/d 简单/a oh sit!/aka 666 park avenue/aka 逝者之证/aka
 		Matcher mat = FenciResPatt.matcher(MyICTCLAS.fenci(content));
 		while (mat.find()) {
 			String word = mat.group(1).trim();
 			String cixing = mat.group(2).trim();
 			// 词性中有名词(n)成分或手动倒入的剧集(aka)
 			if (cixing.contains("n") || cixing.contains("aka")) {
 				// 短于4的名词权重是0.5，其他是1 考慮到：厨房噩梦/aka 會被分成 厨房/n 噩梦/n
 				double weight = cixing.contains("n") && word.length() <= 4 ? 0.5
 						: 1.0;
 
 				TvShows tvShow = Meijus.get(word);
 				if (tvShow != null) {
 					if (guessList.containsKey(tvShow))
 						weight += guessList.get(tvShow);
 					guessList.put(tvShow, weight);
 				} else {
 					if (cixing.contains("aka"))
 						LOG.warn("no tvshow for word:" + word);
 				}
 			}
 		}
 
 		// 选择权重最大的
 		TvShows result = new TvShows();
 		double maxW = 0;
 		int candidateCount = 0;// 可能的剧集，用来判断这个新闻是不是包含了很多剧集的报道
 		for (Entry<TvShows, Double> entry : guessList.entrySet()) {
 			if (entry.getValue() > maxW) {
 				maxW = entry.getValue();
 				result = entry.getKey();
 			}
 			if (entry.getValue() >= 2)
 				candidateCount++;
 		}
		if (candidateCount > 4) {
 			LOG.warn("found " + candidateCount + " different meiju in \n"
 					+ content);
 			return result = new TvShows();
 		}
 		return result;
 	}
 
 	/**
 	 * 把中文美剧名导出，统一为aka词性 MARK 厨房噩梦@@aka 会被 厨房/n 噩梦/n 覆盖！！！
 	 * 
 	 * @param fname
 	 */
 	public static void outputMeijuDict(String fname) {
 		try {
 			PrintWriter pw = new PrintWriter(fname);
 			for (String name : Meijus.keySet()) {
 				// if (!CharsetUtil.containChinese(name)) {
 				pw.println(name + "@@aka");
 				LOG.info("write user dic item:" + name);
 				// }
 			}
 			pw.close();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 			LOG.warn("fail to output user dict:" + e.getMessage());
 		}
 	}
 
 	public static void main(String args[]) {
 		LOG.warn("会死循环，当content为空的时候");
 		while (true) {
 			List<RssNews> list = DB4Tdd.getRssNewsToGuessTvShows(100);
 			if (list.size() == 0)
 				break;
 			for (RssNews rssNews : list) {
 				if (rssNews.getContent() == null)
 					continue;
 				rssNews.setTvShows(MeijuTvAnalyzer.guessTv(rssNews.getContent()));
 				DB4Tdd.updateRssNewsTvShows(rssNews);
 			}
 		}
 		// outputMeijuDict("meijuDict2.txt");
 	}
 }
