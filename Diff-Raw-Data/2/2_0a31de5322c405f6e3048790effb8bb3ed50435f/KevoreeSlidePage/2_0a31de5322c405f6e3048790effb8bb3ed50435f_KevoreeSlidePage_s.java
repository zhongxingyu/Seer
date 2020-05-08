 package org.kevoree.slides.framework;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Node;
 import org.jsoup.select.Elements;
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
 		@DictionaryAttribute(name = "main", defaultValue = "index.html"),
 		@DictionaryAttribute(name = "wsurl", defaultValue = "ws://localhost:8092/keynote", optional = true),
 		@DictionaryAttribute(name = "paperURL", optional = false, defaultValue = "")
 })
 public class KevoreeSlidePage extends ParentAbstractPage {
 
 	private HashMap<String, byte[]> contentRawCache = new HashMap<String, byte[]>();
 	private HashMap<String, String> contentTypeCache = new HashMap<String, String>();
 	protected Boolean useCache = true;
 
 	private static String kslideshowKeynoteScript = "\n\tjQuery(document).ready(function ($) {\n"
 			+ "\t\tvar ks = new KSlideShowKeynote();\n"
 			+ "\t\tvar keyboardPlugin = new KKeyboard(ks);\n"
 			+ "\t\tvar popupPlugin = new KPopupMaster(ks, \"{slideurl}\");\n"
 			+ "\t\tvar websocketPlugin = new KWebsocketMaster(ks, \"{wsurl}\");\n"
 			+ "\t\tvar iframePlugin = new KIFrameMaster(ks, \"{slideurl}\");\n"
 			+ "\t\tvar slideCountPlugin = new KSlideCount(ks);\n"
 			+ "\t\tvar notesPlugin = new KNotes();\n"
 			+ "\t\tvar timePlugin = new KTime();\n"
 			+ "\t\tks.addPluginListener(keyboardPlugin);\n"
 			+ "\t\tks.addPluginListener(popupPlugin);\n"
 			+ "\t\tks.addPluginListener(websocketPlugin);\n"
			+ "\t\tks.addPluginListener(iframePlugin);\n"
 			+ "\t\tks.addPluginListener(slideCountPlugin);\n"
 			+ "\t\tks.addPluginListener(notesPlugin);\n"
 			+ "\t\tks.addPluginListener(timePlugin);\n"
 			+ "\t\tks.startKeynote();\n"
 			+ "\t});";
 
 	private static String kslideshowMasterScript = "\n\tjQuery(document).ready(function ($) {\n"
 			+ "\t\tvar ks = new KSlideShow();\n"
 			+ "\t\tvar keyboardPlugin = new KKeyboard(ks);\n"
             + "\t\tvar includePlugin = new IncludePlugin(ks);\n"
             + "\t\tvar h2list = new H2List(ks, 4);\n"
 			+ "\t\tvar webSocketPlugin = new KWebsocketMaster(ks, \"{wsurl}\", \"{roomID}\");\n"
 			+ "\t\tks.addPluginListener(keyboardPlugin);\n"
             + "\t\tks.addPluginListener(includePlugin, true);\n"
             + "\t\tks.addPluginListener(h2list, true);\n"
 			+ "\t\tks.addPluginListener(webSocketPlugin);\n"
 			+ "\t\tks.startKeynote();\n"
 			+ "\t});";
 
 	private static String kslideshowSlaveScript = "\n\tjQuery(document).ready(function ($) {\n"
 			+ "\t\tvar ks = new KSlideShow();\n"
             + "\t\tvar includePlugin = new IncludePlugin(ks);\n"
             + "\t\tvar h2list = new H2List(ks, 4);\n"
 			+ "\t\tvar webSocketPlugin = new KWebsocketSlave(ks, \"{wsurl}\", \"{roomID}\");\n"
             + "\t\tks.addPluginListener(includePlugin, true);\n"
             + "\t\tks.addPluginListener(h2list, true);\n"
 			+ "\t\tks.addPluginListener(webSocketPlugin);\n"
 			+ "\t\tks.startKeynote();\n"
 			+ "\t});";
 
 	private static String kslideshowFrameScript = "\n\tjQuery(document).ready(function ($) {\n"
 			+ "\t\tvar ks = new KSlideShow();\n"
             + "\t\tvar includePlugin = new IncludePlugin(ks);\n"
             + "\t\tvar h2list = new H2List(ks, 4);\n"
 			+ "\t\tvar iframePlugin = new KIFrameSlave(ks);\n"
             + "\t\tks.addPluginListener(includePlugin, true);\n"
             + "\t\tks.addPluginListener(h2list, true);\n"
 			+ "\t\tks.addPluginListener(iframePlugin);\n"
 			+ "\t\tks.startKeynote();\n"
 			+ "\t});";
 
 	private static String kslideshowPopupSlaveScript = "\n\tjQuery(document).ready(function ($) {\n"
 			+ "\t\tvar ks = new KSlideShow();\n"
 			+ "\t\tvar keyboardPlugin = new KKeyboard(ks);\n"
 			+ "\t\tvar popupPlugin = new KPopupSlave(ks);\n"
             + "\t\tvar includePlugin = new IncludePlugin(ks);\n"
             + "\t\tvar h2list = new H2List(ks, 4);\n"
 			+ "\t\tks.addPluginListener(keyboardPlugin);\n"
 			+ "\t\tks.addPluginListener(popupPlugin);\n"
             + "\t\tks.addPluginListener(includePlugin, true);\n"
             + "\t\tks.addPluginListener(h2list, true);\n"
 			+ "\t\tks.startKeynote();\n"
 			+ "\t});";
 
 		private static String kslideshowEmbedderScript = "\n\tjQuery(document).ready(function ($) {\n"
 				+ "\t\tvar ks = new KSlideShowKeynote();\n"
 				+ "\t\tvar keyboardPlugin = new KKeyboard(ks);\n"
                 + "\t\tvar includePlugin = new IncludePlugin(ks);\n"
                 + "\t\tvar h2list = new H2List(ks, 4);\n"
 				+ "\t\tvar embedderPlugin = new KEmbedder(ks);\n"
 				+ "\t\tks.addPluginListener(keyboardPlugin);\n"
                 + "\t\tks.addPluginListener(includePlugin, true);\n"
                 + "\t\tks.addPluginListener(h2list, true);\n"
 				+ "\t\tks.addPluginListener(embedderPlugin);\n"
 				+ "\t\tks.startKeynote();\n"
 				+ "\t\tks.sendEvent(null, {'type':'RELOAD', 'url':'{slideurl}'});\n"
 				+ "\t});";
 
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
 					slideURL = "/" + slideURL;
 				}
 				Document document = Jsoup.parse(new String(FileServiceHelper.convertStream(loadInternal("showcaseKeynote.html")), "UTF-8"));
 				Document.OutputSettings settings = document.outputSettings();
 				settings.prettyPrint(false);
 				settings.charset("ASCII");
 
 				Elements kslideshowElements = document.select("#kSlideshow");
 				if (kslideshowElements.size() == 1) {
 					Node kslideshow = kslideshowElements.first().childNode(0);
 
 					// replace slideURL and wsurl on the script
 					String script = kslideshowKeynoteScript.replace("{slideurl}", completeUrl + slideURL).replace("{wsurl}", getDictionary().get("wsurl").toString());
 					kslideshow.attr("data", script);
 
 					response.getHeaders().put("Content-Type", "text/html");
 					response.setRawContent(document.html().getBytes());
 					if (useCache) {
 						cacheResponse(request, response);
 					}
 					return response;
 				} else {
 					logger.error("Multiple elements with the same id: kSlideshow");
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		} else if (getLastParam(request.getUrl()).equals("frame")) {
 			try {
 				Document document = Jsoup.parse(new String(FileServiceHelper.convertStream(loadInternal(getDictionary().get("main").toString())), "UTF-8"));
 				Document.OutputSettings settings = document.outputSettings();
 				settings.prettyPrint(false);
 				settings.charset("ASCII");
 
 				Elements kslideshowElements = document.select("#kSlideshow");
 				if (kslideshowElements.size() == 1) {
 					kslideshowElements.first().before("<script type=\"text/javascript\" src=\"scripts/plugins/iframe.js\"></script>");
 					Node kslideshow = kslideshowElements.first().childNode(0);
 
 					kslideshow.attr("data", kslideshowFrameScript);
 
 					response.getHeaders().put("Content-Type", "text/html");
 					response.setRawContent(document.html().getBytes());
 					if (useCache) {
 						cacheResponse(request, response);
 					}
 					return response;
 				} else {
 					logger.error("Multiple elements with the same id: kSlideshow");
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		} else if (getLastParam(request.getUrl()).equals("popup")) {
 			try {
 				Document document = Jsoup.parse(new String(FileServiceHelper.convertStream(loadInternal(getDictionary().get("main").toString())), "UTF-8"));
 				Document.OutputSettings settings = document.outputSettings();
 				settings.prettyPrint(false);
 				settings.charset("ASCII");
 
 				Elements kslideshowElements = document.select("#kSlideshow");
 				if (kslideshowElements.size() == 1) {
 					kslideshowElements.first().before("<script type=\"text/javascript\" src=\"scripts/plugins/popup.js\"></script>");
 					Node kslideshow = kslideshowElements.first().childNode(0);
 
 					kslideshow.attr("data", kslideshowPopupSlaveScript);
 
 					response.getHeaders().put("Content-Type", "text/html");
 					response.setRawContent(document.html().getBytes());
 					if (useCache) {
 						cacheResponse(request, response);
 					}
 					return response;
 				} else {
 					logger.error("Multiple elements with the same id: kSlideshow");
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		} else if (getLastParam(request.getUrl()).equals("embed")) {
 			try {
 				String slideURL = request.getUrl().replace("embed", "");
 				String completeUrl = request.getCompleteUrl().replace(request.getUrl(), "");
 				if (completeUrl.endsWith("/")) {
 					completeUrl = completeUrl.substring(0, completeUrl.length() - 1);
 				}
 				if (!slideURL.startsWith("/")) {
 					slideURL = "/" + slideURL;
 				}
 				Document document = Jsoup.parse(new String(FileServiceHelper.convertStream(loadInternal("showcaseEmbedder.html")), "UTF-8"));
 				Document.OutputSettings settings = document.outputSettings();
 				settings.prettyPrint(false);
 				settings.charset("ASCII");
 
 				Elements kslideshowElements = document.select("#kSlideshow");
 				if (kslideshowElements.size() == 1) {
 					Node kslideshow = kslideshowElements.first().childNode(0);
 
 					// replace slideURL and wsurl on the script
 					String script = kslideshowEmbedderScript.replace("{slideurl}", completeUrl + slideURL);
 					kslideshow.attr("data", script);
 
 					response.getHeaders().put("Content-Type", "text/html");
 					response.setRawContent(document.html().getBytes());
 
 					if (useCache) {
 						cacheResponse(request, response);
 					}
 					return response;
 				} else {
 					logger.error("Multiple elements with the same id: kSlideshow");
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		} /*else if (getLastParam(request.getUrl()).contains("ws")) {
 			*//*try {
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
 			}*//*
 		}*/ else if (getLastParam(request.getUrl()).contains("master")) {
 			try {
 				String roomID = getLastParam(request.getUrl()).replace("master", "");
 				Document document = Jsoup.parse(new String(FileServiceHelper.convertStream(loadInternal(getDictionary().get("main").toString())), "UTF-8"));
 				Document.OutputSettings settings = document.outputSettings();
 				settings.prettyPrint(false);
 				settings.charset("ASCII");
 
 				Elements kslideshowElements = document.select("#kSlideshow");
 				if (kslideshowElements.size() == 1) {
 					kslideshowElements.first().before("<script type=\"text/javascript\" src=\"scripts/plugins/websocket.js\"></script>");
 					Node kslideshow = kslideshowElements.first().childNode(0);
 
 					// replace roomID and wsurl on the script
 					String script = kslideshowMasterScript.replace("{roomID}", roomID).replace("{wsurl}", getDictionary().get("wsurl").toString());
 					kslideshow.attr("data", script);
 
 					response.getHeaders().put("Content-Type", "text/html");
 					response.setRawContent(document.html().getBytes());
 					if (useCache) {
 						cacheResponse(request, response);
 					}
 					return response;
 				} else {
 					logger.error("Multiple elements with the same id: kSlideshow");
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		} else if (getLastParam(request.getUrl()).contains("slave")) {
 			try {
 				String roomID = getLastParam(request.getUrl()).replace("slave", "");
 				Document document = Jsoup.parse(new String(FileServiceHelper.convertStream(loadInternal(getDictionary().get("main").toString())), "UTF-8"));
 				Document.OutputSettings settings = document.outputSettings();
 				settings.prettyPrint(false);
 				settings.charset("ASCII");
 
 				Elements kslideshowElements = document.select("#kSlideshow");
 				if (kslideshowElements.size() == 1) {
 					kslideshowElements.first().before("<script type=\"text/javascript\" src=\"scripts/plugins/websocket.js\"></script>");
 					Node kslideshow = kslideshowElements.first().childNode(0);
 
 					// replace roomID and wsurl on the script
 					String script = kslideshowSlaveScript.replace("{roomID}", roomID).replace("{wsurl}", getDictionary().get("wsurl").toString());
 					kslideshow.attr("data", script);
 
 					response.getHeaders().put("Content-Type", "text/html");
 					response.setRawContent(document.html().getBytes());
 					if (useCache) {
 						cacheResponse(request, response);
 					}
 					return response;
 				} else {
 					logger.error("Multiple elements with the same id: kSlideshow");
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		} else if (!load(request, response)) {
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
