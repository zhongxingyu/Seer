 package org.kevoree.slides.framework;
 
 import org.kevoree.annotation.ComponentType;
 import org.kevoree.annotation.DictionaryAttribute;
 import org.kevoree.annotation.DictionaryType;
 import org.kevoree.annotation.Library;
 import org.kevoree.library.javase.webserver.FileServiceHelper;
 import org.kevoree.library.javase.webserver.KevoreeHttpRequest;
 import org.kevoree.library.javase.webserver.KevoreeHttpResponse;
 import org.kevoree.library.javase.webserver.ParentAbstractPage;
 
 import java.io.InputStream;
 import java.util.HashMap;
 
 /**
  * User: Erwan Daubert - erwan.daubert@gmail.com
  * Date: 30/04/12
  * Time: 10:53
  *
  * @author Erwan Daubert
  * @version 1.0
  */
 
 @Library(name = "KevoreeWeb")
 @ComponentType
 @DictionaryType({
		@DictionaryAttribute(name = "main", defaultValue = "showcase.html"),
 		@DictionaryAttribute(name = "wsurl", defaultValue = "ws://localhost:8092/keynote", optional = true),
 		@DictionaryAttribute(name = "paperURL", optional = false, defaultValue = "")
 })
 public class KevoreeSlidePage extends ParentAbstractPage {
 
 	private HashMap<String, byte[]> contentRawCache = new HashMap<String, byte[]>();
 	private HashMap<String, String> contentTypeCache = new HashMap<String, String>();
 	protected Boolean useCache = true;
 
 	@Override
 	public void startPage () {
 		super.startPage();
 		contentRawCache.clear();
 		contentTypeCache.clear();
 	}
 
 	@Override
 	public void updatePage () {
 		super.updatePage();
 		contentRawCache.clear();
 		contentTypeCache.clear();
 	}
 
 	@Override
 	public void stopPage () {
 		super.stopPage();
 		contentRawCache.clear();
 		contentTypeCache.clear();
 	}
 
 	public void cacheResponse (KevoreeHttpRequest request, KevoreeHttpResponse response) {
 		if (response.getRawContent() != null) {
 			contentRawCache.put(request.getUrl(), response.getRawContent());
 		} else {
 			contentRawCache.put(request.getUrl(), response.getContent().getBytes());
 		}
 		contentTypeCache.put(request.getUrl(), response.getHeaders().get("Content-Type"));
 	}
 
 	protected InputStream loadInternal (String name) {
 		return getClass().getClassLoader().getResourceAsStream(name);
 	}
 
 	@Override
 	public KevoreeHttpResponse process (KevoreeHttpRequest request, KevoreeHttpResponse response) {
 
 		/* Serve File directly from local cache */
 		if (useCache) {
 			if (contentTypeCache.containsKey(request.getUrl())) {
 				response.setRawContent(contentRawCache.get(request.getUrl()));
 				response.getHeaders().put("Content-Type", contentTypeCache.get(request.getUrl()));
 				return response;
 			}
 		}
 
 		if (getLastParam(request.getUrl()).equals("keynote")) {
 			try {
 				String slideURL = request.getUrl().replace("keynote", "");
 				String completeUrl = request.getCompleteUrl().replace(request.getUrl(), "");
 				if (completeUrl.endsWith("/")) {
 					completeUrl = completeUrl.substring(0, completeUrl.length() - 1);
 				}
 				if (!slideURL.startsWith("/")) {
 					slideURL= "/" + slideURL;
 				}
 				response.setRawContent(FileServiceHelper.convertStream(loadInternal("showcaseKeynote.html")));
 //				response.setRawContent(new String(response.getRawContent()).replace("{slideURL}", completeUrl + slideURL).getBytes());
 				response.setRawContent(new String(response.getRawContent()).replace("{slideurl}", completeUrl + slideURL).replace("{wsurl}", getDictionary().get("wsurl").toString()).getBytes());
 				response.getHeaders().put("Content-Type", "text/html");
 				if (useCache) {
 					cacheResponse(request, response);
 				}
 				return response;
 			} catch (Exception e) {
 				logger.error("", e);
 			}
 		}
 
 		if (getLastParam(request.getUrl()).equals("embed")) {
 			try {
 				String slideURL = request.getUrl().replace("embed", "");
 				String completeUrl = request.getCompleteUrl().replace(request.getUrl(), "");
 				if (completeUrl.endsWith("/")) {
 					completeUrl = completeUrl.substring(0, completeUrl.length() - 1);
 				}
 				if (!slideURL.startsWith("/")) {
 					slideURL= "/" + slideURL;
 				}
 				response.setRawContent(FileServiceHelper.convertStream(loadInternal("embedder.html")));
 				response.setRawContent(new String(response.getRawContent()).replace("http://localhost:8080/", completeUrl + slideURL).getBytes());
 				response.getHeaders().put("Content-Type", "text/html");
 				if (useCache) {
 					cacheResponse(request, response);
 				}
 				return response;
 			} catch (Exception e) {
 				logger.error("", e);
 			}
 		}
 		if (getLastParam(request.getUrl()).contains("ws")) {
 			try {
 				String roomID = getLastParam(request.getUrl()).replace("ws", "");
 				String wsUrl = getDictionary().get("wsurl").toString();
 				response.setRawContent(FileServiceHelper.convertStream(loadInternal(getDictionary().get("main").toString())));
 				response.setRawContent(new String(response.getRawContent()).replace("{wsurl}", wsUrl).replace("{roomID}", roomID).getBytes());
 				response.getHeaders().put("Content-Type", "text/html");
 				if (useCache) {
 					cacheResponse(request, response);
 				}
 				return response;
 			} catch (Exception e) {
 				logger.error("", e);
 			}
 		}
 		if (getLastParam(request.getUrl()).contains("master")) {
 			try {
 				String roomID = getLastParam(request.getUrl()).replace("master", "");
 				String wsUrl = getDictionary().get("wsurl").toString();
 				/*String newScript = "<script>" + new String(FileServiceHelper.convertStream(loadInternal("scripts/kslideWebSocketMaster.js")), "UTF-8").replace("{roomID}", roomID)
 						.replace("{wsurl}", getDictionary().get("wsurl").toString()) + "</script></body>";*/
 				response.setRawContent(FileServiceHelper.convertStream(loadInternal(getDictionary().get("main").toString())));
 				response.setRawContent(new String(response.getRawContent()).replace("{wsurl}", wsUrl).replace("{roomID}", roomID).getBytes());
 //				response.setRawContent(new String(response.getRawContent()).replace("</body>", newScript).getBytes());
 				response.getHeaders().put("Content-Type", "text/html");
 				if (useCache) {
 					cacheResponse(request, response);
 				}
 				return response;
 			} catch (Exception e) {
 				logger.error("", e);
 			}
 		}
 		if (!load(request, response)) {
 			response.setStatus(404);
 		}
 		return response;
 	}
 
 	public boolean load (KevoreeHttpRequest request, KevoreeHttpResponse response) {
 		if (FileServiceHelper.checkStaticFile(getDictionary().get("main").toString(), this, request, response)) {
 			String pattern = getDictionary().get("urlpattern").toString();
 			if (pattern.endsWith("**")) {
 				pattern = pattern.replace("**", "");
 			}
 			if (!pattern.endsWith("/")) {
 				pattern = pattern + "/";
 			}
 			if (pattern.equals(request.getUrl() + "/") || request.getUrl().endsWith(".html") || request.getUrl().endsWith(".css")) {
 				if (response.getRawContent() != null) {
 					response.setRawContent(new String(response.getRawContent()).replace("{urlpattern}", pattern).getBytes());
 				} else {
 					response.setContent(response.getContent().replace("{urlpattern}", pattern));
 				}
 			}
 			if (useCache) {
 				cacheResponse(request, response);
 			}
 			return true;
 		} else {
 			return false;
 		}
 	}
 }
