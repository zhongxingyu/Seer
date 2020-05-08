 package net.andac.aydin.tvdblibrary.connector;
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 
 import net.andac.aydin.tvdblibrary.datatypes.Actor;
 import net.andac.aydin.tvdblibrary.datatypes.Banner;
 import net.andac.aydin.tvdblibrary.datatypes.BannerType;
 import net.andac.aydin.tvdblibrary.datatypes.BannerType2;
 import net.andac.aydin.tvdblibrary.datatypes.Episode;
 import net.andac.aydin.tvdblibrary.datatypes.Language;
 import net.andac.aydin.tvdblibrary.datatypes.Tvshow;
 
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 public class TVDBMapper {
 	private static TVDBMapper instance;
 	private DateFormat formatter;
 
 	public static synchronized TVDBMapper getInstance() {
 		if (instance == null) {
 
 			instance = new TVDBMapper();
 		}
 		return instance;
 	}
 
 	public TVDBMapper() {
		formatter = new SimpleDateFormat("yyyy-MM-dd");
 	}
 
 	/**
 	 * @param seriesNode
 	 * @return
 	 * @throws ParseException
 	 */
 	public Tvshow mapTvshow(Node seriesNode) throws ParseException {
 		Tvshow tvshow = new Tvshow();
 		NodeList seriesPropertyNodes = seriesNode.getChildNodes();
 		for (int i = 0; i < seriesPropertyNodes.getLength(); i++) {
 			Node item = seriesPropertyNodes.item(i);
 			String nodeName = item.getNodeName().toLowerCase();
 			String nodeValue = item.getTextContent();
 			if (nodeValue == null || nodeValue.isEmpty()) {
 				continue;
 			}
 			if (nodeName.equals("id")) {
 				tvshow.setSeriesid(Long.parseLong(nodeValue));
 			}
 			if (nodeName.equals("firstaired") && !nodeValue.isEmpty()) {
 				tvshow.setFirstAired(formatter.parse(nodeValue));
 			}
 			if (nodeName.equals("seriesname")) {
 				tvshow.setSeriesName(nodeValue);
 			}
 			if (nodeName.equals("overview")) {
 				if (nodeValue.isEmpty()) {
 					tvshow.setOverview("none");
 				} else {
 					tvshow.setOverview(nodeValue);
 				}
 			}
 			if (nodeName.equals("lastupdated")) {
 				tvshow.setLastUpdated(Long.parseLong(nodeValue));
 			}
 			if (nodeName.equals("language")) {
 				tvshow.setLanguage(Language.valueById(nodeValue));
 			}
 			if (nodeName.equals("banner")) {
 				Banner banner = new Banner();
 				banner.setBannerPath(nodeValue);
 				tvshow.getBanners().add(banner);
 			}
 		}
 		return tvshow;
 	}
 
 	public Episode mapEpisode(Node episodeNode) throws ParseException {
 		NodeList childNodes = episodeNode.getChildNodes();
 		Episode episode = new Episode();
 		for (int j = 0; j < childNodes.getLength(); j++) {
 			Node item = childNodes.item(j);
 			String nodeName = item.getNodeName();
 			String nodeValue = item.getTextContent();
 			if (nodeValue == null || nodeValue.isEmpty()) {
 				continue;
 			}
 			if (nodeName.equals("id")) {
 				episode.setEpisodeId(Long.parseLong(nodeValue));
 			}
 			if (nodeName.equals("FirstAired")) {
 				episode.setFirstAired(formatter.parse(nodeValue));
 			}
 			if (nodeName.equals("EpisodeName")) {
 				episode.setEpisodeName(nodeValue);
 			}
 			if (nodeName.equals("Overview")) {
 				episode.setOverview(nodeValue);
 			}
 			if (nodeName.equals("EpisodeNumber")) {
 				episode.setEpisodeNumber(Long.parseLong(nodeValue));
 			}
 			if (nodeName.equals("SeasonNumber")) {
 				episode.setSeasonNumber(Long.parseLong(nodeValue));
 			}
 			if (nodeName.equals("lastupdated")) {
 				episode.setLastUpdated(Long.parseLong(nodeValue));
 			}
 		}
 		return episode;
 	}
 
 	public Long mapTvshowTimestamp(Node seriesnode) throws ParseException {
 		NodeList childNodes = seriesnode.getChildNodes();
 		for (int j = 0; j < childNodes.getLength(); j++) {
 			Node item = childNodes.item(j);
 			String nodeName = item.getNodeName();
 			String nodeValue = item.getTextContent();
 			if (nodeValue == null || nodeValue.isEmpty()) {
 				continue;
 			}
 			if (nodeName.equals("id")) {
 				return Long.parseLong(nodeValue);
 			}
 		}
 		return null;
 	}
 
 	public Banner mapBanner(Node episodeNode) throws ParseException {
 		NodeList childNodes = episodeNode.getChildNodes();
 		Banner banner = new Banner();
 		for (int j = 0; j < childNodes.getLength(); j++) {
 			Node item = childNodes.item(j);
 			String nodeName = item.getNodeName();
 			String nodeValue = item.getTextContent();
 			if (nodeValue == null || nodeValue.isEmpty()) {
 				continue;
 			}
 			if (nodeName.equals("id")) {
 				banner.setId(Long.parseLong(nodeValue));
 			}
 			if (nodeName.equals("BannerPath")) {
 				banner.setBannerPath(nodeValue);
 			}
 			if (nodeName.equals("BannerType")) {
 				banner.setBannerType(BannerType.valueOf(nodeValue.toUpperCase()));
 			}
 			if (nodeName.equals("BannerType2")) {
 				banner.setBannerType2(BannerType2.fromString(nodeValue));
 			}
 			if (nodeName.equals("Language")) {
 				banner.setLanguage(nodeValue);
 			}
 			if (nodeName.equals("Season")) {
 				banner.setSeason(nodeValue);
 			}
 		}
 		return banner;
 	}
 
 	public Actor mapActor(Node episodeNode) throws ParseException {
 		NodeList childNodes = episodeNode.getChildNodes();
 		Actor actor = new Actor();
 		for (int j = 0; j < childNodes.getLength(); j++) {
 			Node item = childNodes.item(j);
 			String nodeName = item.getNodeName();
 			String nodeValue = item.getTextContent();
 			if (nodeValue == null || nodeValue.isEmpty()) {
 				continue;
 			}
 			if (nodeName.equals("Image")) {
 				actor.setImage(nodeValue);
 			}
 			if (nodeName.equals("Name")) {
 				actor.setName(nodeValue);
 			}
 			if (nodeName.equals("Role")) {
 				actor.setRole(nodeValue);
 			}
 			if (nodeName.equals("SortOrder")) {
 				actor.setSortorder(Integer.valueOf(nodeValue));
 			}
 		}
 		return actor;
 	}
 }
