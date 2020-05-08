 package org.mashupmedia.restful;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import net.sf.json.JSONArray;
 import net.sf.json.JSONObject;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang3.StringUtils;
 import org.apache.log4j.Logger;
 import org.mashupmedia.model.media.Artist;
 import org.mashupmedia.service.ConnectionManager;
 import org.mashupmedia.service.MusicManager;
 import org.mashupmedia.util.StringHelper.Encoding;
 import org.mashupmedia.web.remote.RemoteImage;
 import org.mashupmedia.web.remote.RemoteMediaMetaItem;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.web.util.UriUtils;
 
 @Service
 public class DiscogsWebServiceImpl implements DiscogsWebService {
 	private static final String PREPEND_CACHE_KEY_ARTIST = "artist_";
 
 	private Logger logger = Logger.getLogger(getClass());
 
 	private Map<String, RemoteMediaMetaItem> remoteMediaCache = new HashMap<String, RemoteMediaMetaItem>();
 	public static int DEFAULT_CACHE_SECONDS = 86400;
 
 	@Autowired
 	private MusicManager musicManager;
 
 	private static String[] ARTICLES = { "the", "a" };
 
 	@Autowired
 	private ConnectionManager connectionManager;
 
 	@Override
 	public RemoteMediaMetaItem getArtistInformation(Artist artist) throws IOException {
 
 		if (artist == null) {
 			return null;
 		}
 
 		String discogsArtistId = getDiscogArtistId(artist, true);
 		RemoteMediaMetaItem remoteMediaMeta = getDiscogsArtistMeta(discogsArtistId);
 		artist.setRemoteId(discogsArtistId);
 		musicManager.saveArtist(artist);
 		return remoteMediaMeta;
 	}
 
 	@Override
 	public RemoteMediaMetaItem getDiscogsArtistMeta(String discogsArtistId) throws IOException {
 		List<RemoteMediaMetaItem> remoteMediaMetaItems = new ArrayList<RemoteMediaMetaItem>();
 		
 		RemoteMediaMetaItem remoteMediaMeta = new RemoteMediaMetaItem();
 		remoteMediaMeta.setId(discogsArtistId);
 		remoteMediaMetaItems.add(remoteMediaMeta);
 		
 		populateRemoteMediaMetaItems(remoteMediaMetaItems);
 		if (remoteMediaMetaItems == null || remoteMediaMetaItems.isEmpty()) {
 			return null;
 		}
 
 		return remoteMediaMetaItems.get(0);
 	}
 
 	protected RemoteMediaMetaItem getRemoteMediaItemFromCache(String cacheKey, Date date) {
 		RemoteMediaMetaItem cachedRemoteMediaMetaItem = remoteMediaCache.get(cacheKey);
 		if (cachedRemoteMediaMetaItem != null) {
 			long seconds = (date.getTime() - cachedRemoteMediaMetaItem.getDate().getTime()) / 1000;
 			if (DEFAULT_CACHE_SECONDS > seconds) {
 //				remoteMediaMetaItems.add(cachedRemoteMediaMetaItem);
 				return cachedRemoteMediaMetaItem;
 //				remoteMediaMetaItems.set(i, cachedRemoteMediaMetaItem);
 //				continue;
 			}
 
 			remoteMediaCache.remove(cachedRemoteMediaMetaItem);
 
 		}
 
 		return null;
 	}
 	
 //	protected void copyRemoteMediaItem(RemoteMediaMetaItem fromRemoteMediaMetaItem, RemoteMediaMetaItem toRemoteMediaMetaItem) {
 //		if (fromRemoteMediaMetaItem == null) {
 //			return;
 //		}
 //				
 //		String toName = toRemoteMediaMetaItem.getName();
 //		if (StringUtils.isNotBlank(toName)) {
 //			return;
 //		}
 //		
 //		String fromName = fromRemoteMediaMetaItem.getName();
 //		toRemoteMediaMetaItem.setName(fromName);
 //		
 //	}
 	
 	protected String prepareRemoteCacheKey(String discogsId) {
 		String cacheKey = PREPEND_CACHE_KEY_ARTIST + discogsId;
 		return cacheKey;
 	}
 	
 	protected void populateRemoteMediaMetaItems(List<RemoteMediaMetaItem> remoteMediaMetaItems) throws IOException {
 
 //		List<RemoteMediaMetaItem> remoteMediaMetaItems = new ArrayList<RemoteMediaMetaItem>();
 
 		for (int i = 0; i < remoteMediaMetaItems.size(); i++) {
 			RemoteMediaMetaItem remoteMediaMetaItem = remoteMediaMetaItems.get(i);
 			String discogsArtistId = remoteMediaMetaItem.getId();
 			
 			if (StringUtils.isBlank(discogsArtistId)) {
 				continue;
 			}
 			
 			String cacheArtistKey = prepareRemoteCacheKey(discogsArtistId);
 			Date date = remoteMediaMetaItem.getDate();
 			
 			RemoteMediaMetaItem cachedRemoteMediaMetaItem = getRemoteMediaItemFromCache(cacheArtistKey, date);
 			if (cachedRemoteMediaMetaItem != null && cachedRemoteMediaMetaItem.isComplete()) {
//				remoteMediaMetaItem = cachedRemoteMediaMetaItem;
				remoteMediaMetaItems.set(i, cachedRemoteMediaMetaItem);
 				continue;
 			}
 			
 //			copyRemoteMediaItem(cachedRemoteMediaMetaItem, remoteMediaMetaItem);
 			
 			
 //			RemoteMediaMetaItem cachedRemoteMediaMetaItem = remoteMediaCache.get(cacheArtistKey);
 //			if (cachedRemoteMediaMetaItem != null) {
 //				long seconds = (date.getTime() - cachedRemoteMediaMetaItem.getDate().getTime()) / 1000;
 //				if (DEFAULT_CACHE_SECONDS > seconds) {
 ////					remoteMediaMetaItems.add(cachedRemoteMediaMetaItem);
 //					remoteMediaMetaItems.set(i, cachedRemoteMediaMetaItem);
 //					continue;
 //				}
 //
 //				remoteMediaCache.remove(cachedRemoteMediaMetaItem);
 //
 //			}
 
 			String artistUrl = "http://api.discogs.com/artists/" + discogsArtistId;
 			logger.debug("Searching Discogs for artist information using url: " + artistUrl);
 			InputStream inputStream = connectionManager.connect(artistUrl);
 			String jsonArtistText = IOUtils.toString(inputStream, Encoding.UTF8.getEncodingString());
 			JSONObject jsonArtist = JSONObject.fromObject(jsonArtistText);
			
			String nameKey = "name";
			if (!jsonArtist.containsKey(nameKey)) {
				inputStream.close();
				continue;
			}
			
 			if (StringUtils.isBlank(remoteMediaMetaItem.getName())) {
 				String name = StringUtils.trimToEmpty(jsonArtist.getString("name"));			
 				remoteMediaMetaItem.setName(name);				
 			}
 
 			String profileKey = "profile";
 			String profile = "";
 			if (jsonArtist.containsKey(profileKey)) {
 				profile = StringUtils.trimToEmpty(jsonArtist.getString(profileKey));
 				profile = profile.replaceAll("\\[.=", "");
 				profile = profile.replaceAll("\\]", "");
 				profile = profile.replaceAll("(\r\n|\n\r|\r|\n)", "<br />");
 			}
 
 //			cachedRemoteMediaMetaItem = new RemoteMediaMeta();
 //			cachedRemoteMediaMetaItem.setId(discogsArtistId);
 			
 			
 			remoteMediaMetaItem.setProfile(profile);
 			
 			List<RemoteImage> remoteImages = new ArrayList<RemoteImage>();
 			String imagesKey = "images";
 			if (jsonArtist.has(imagesKey)) {
 				JSONArray jsonImages = jsonArtist.getJSONArray(imagesKey);
 
 				if (jsonImages != null) {
 					for (int j = 0; j < jsonImages.size(); j++) {
 						JSONObject jsonImage = jsonImages.getJSONObject(j);
 						RemoteImage remoteImage = new RemoteImage();
 						String imageUrl = convertImageToProxyUrl(jsonImage.getString("resource_url"));
 						remoteImage.setImageUrl(imageUrl);
 
 						int width = jsonImage.getInt("width");
 						remoteImage.setWidth(width);
 
 						int height = jsonImage.getInt("height");
 						remoteImage.setHeight(height);
 
 						String thumbUrl = convertImageToProxyUrl(jsonImage.getString("uri150"));
 						remoteImage.setThumbUrl(thumbUrl);
 
 						remoteImages.add(remoteImage);
 					}
 				}				
 			}
 
 
 
 			inputStream.close();
 
 			remoteMediaMetaItem.setRemoteImages(remoteImages);
 			remoteMediaMetaItem.setComplete(true);
 			remoteMediaCache.put(cacheArtistKey, remoteMediaMetaItem);
 			throttle(true);
 		}
 
 	}
 
 	protected String convertImageToProxyUrl(String url) {
 		url = StringUtils.trimToEmpty(url);
 		url = "/app/proxy/discogs-image/" + url.replaceFirst(".*/", "");
 		return url;
 	}
 
 	protected String getDiscogArtistId(Artist artist, boolean isThrottle) throws IOException {
 		String remoteId = StringUtils.trimToEmpty(artist.getRemoteId());
 		if (StringUtils.isNotEmpty(remoteId)) {
 			return remoteId;
 		}
 
 		String artistName = artist.getName();
 		List<RemoteMediaMetaItem> discogsArtistIds = findRemoteMediaMetaItems(artistName, isThrottle, 1);
 
 		if (discogsArtistIds == null || discogsArtistIds.isEmpty()) {
 			return "";
 		}
 
 		remoteId = discogsArtistIds.get(0).getId();
 		return remoteId;
 	}
 
 	protected List<RemoteMediaMetaItem> findRemoteMediaMetaItems(String name, boolean isThrottle, int numberOfArtistIds) throws IOException {
 
 		List<RemoteMediaMetaItem> remoteMediaMetaItems = new ArrayList<RemoteMediaMetaItem>();
 
 		name = prepareSearchParameter(name);
 		if (StringUtils.isEmpty(name)) {
 			return remoteMediaMetaItems;
 		}
 
 		String searchUrl = "http://api.discogs.com/database/search?q=" + name + "&type=artist";
 		logger.debug("Searching Discogs for artist id using url: " + searchUrl);
 		InputStream inputStream = connectionManager.connect(searchUrl);
 		String jsonSearchResults = IOUtils.toString(inputStream, Encoding.UTF8.getEncodingString());
 		JSONObject jsonObject = JSONObject.fromObject(jsonSearchResults);
 		JSONArray jsonArray = jsonObject.getJSONArray("results");
 		if (jsonArray == null) {
 			return remoteMediaMetaItems;
 		}
 		int size = jsonArray.size();
 		if (size == 0) {
 			return remoteMediaMetaItems;
 		}
 
 		for (int i = 0; i < jsonArray.size(); i++) {
 			JSONObject jsonArtist = jsonArray.getJSONObject(i);
 			logger.debug("Found jsonObject: " + jsonArtist.toString(2));
 			String discogsArtistId = jsonArtist.getString("id");
 			RemoteMediaMetaItem remoteMediaMeta = new RemoteMediaMetaItem();
 			remoteMediaMeta.setId(discogsArtistId);
 
 //			String cacheKey = prepareRemoteCacheKey(discogsArtistId);
 //			RemoteMediaMetaItem cachedRemoteMediaMetaItem = getRemoteMediaItemFromCache(cacheKey, remoteMediaMeta.getDate());
 //			if (cachedRemoteMediaMetaItem != null) {
 //				remoteMediaMeta = cachedRemoteMediaMetaItem;
 //			}
 			
 			if (!remoteMediaMetaItems.contains(remoteMediaMeta)) {								
 				String title = jsonArtist.getString("title");
 				remoteMediaMeta.setName(title);
 				
 				
 				remoteMediaMetaItems.add(remoteMediaMeta);
 				
 				
 				
 				
 				if (remoteMediaMetaItems.size() >= numberOfArtistIds) {
 					break;
 				}
 			}
 		}
 
 		inputStream.close();
 		throttle(isThrottle);
 		return remoteMediaMetaItems;
 	}
 
 	@Override
 	public List<RemoteMediaMetaItem> searchArtist(String artistName) throws IOException {
 		List<RemoteMediaMetaItem> remoteMediaMetaItems = findRemoteMediaMetaItems(artistName, true, 5);
 		return remoteMediaMetaItems;
 	}
 
 	protected void throttle(boolean isThrottle) {
 		if (!isThrottle) {
 			return;
 		}
 
 		// Wait for one second to get the artist information
 		try {
 			Thread.sleep(1000);
 		} catch (InterruptedException ex) {
 			Thread.currentThread().interrupt();
 		}
 
 	}
 
 	protected String prepareSearchParameter(String artistName) throws UnsupportedEncodingException {
 		artistName = StringUtils.trimToEmpty(artistName).toLowerCase();
 		if (StringUtils.isEmpty(artistName)) {
 			return artistName;
 		}
 
 		for (String article : ARTICLES) {
 			article = article + " ";
 			if (artistName.startsWith(article)) {
 				artistName.replaceFirst(article, "");
 				artistName += ", " + article;
 				break;
 			}
 		}
 
 		artistName = UriUtils.encodeQueryParam(artistName, Encoding.UTF8.getEncodingString());
 		return artistName;
 	}
 
 }
