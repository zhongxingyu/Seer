 package burrito.util;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import burrito.links.Linkable;
 
 import siena.Model;
 
 @SuppressWarnings("rawtypes")
 public class BBCodeCreator {
 
 	private static Map<String, String> bbMapPlugins = new HashMap<String, String>();
 	private static String previewJspUrl = "/burrito/bbCodePreview.jsp";
 
 	private static BBCodeCreatorEndEvent endEvent;
 
 	public static String generateHTML(String bbcode) {
 		String html = StringUtils.escapeHtml(bbcode);
 
 		Map<String, String> bbMap = new HashMap<String, String>();
 
 		bbMap.put("(\r\n|\n\r|\r|\n)", "<br/>");
 		bbMap.put("\\[b\\](.*?)\\[/b\\]", "<span style=\"font-weight:bold;\">$1</span>");
 		bbMap.put("\\[i\\](.*?)\\[/i\\]", "<span style=\"font-style:italic;\">$1</span>");
 		bbMap.put("\\[img\\]\\[/img\\]", "");
 		bbMap.put("\\[img\\](.+?)\\[/img\\]", "<img src=\"/blobstore/image?key=$1\" />");
 		bbMap.put("\\[url\\](.*?)\\[/url\\]", "<a href=\"$1\">$1</a>");
 		bbMap.put("\\[url=(.*?)\\](.*?)\\[/url\\]", "<a href=\"$1\">$2</a>");
 
 		for (Map.Entry entry : bbMap.entrySet()) {
 			html = html.replaceAll(entry.getKey().toString(), entry.getValue().toString());
 		}
 
 		for (Map.Entry entry : bbMapPlugins.entrySet()) {
 			html = html.replaceAll(entry.getKey().toString(), entry.getValue().toString());
 		}
 
 		html = generateYouTube(html);
 
 		html = generateLinkableLinks(html);
 
 		if (endEvent != null) {
 			html = endEvent.execute(html);
 		}
 
 		return html;
 	}
 
 	private static String generateYouTube(String bbcode) {
 		bbcode = bbcode.replaceAll("\\[video\\]http://youtu.be/(.+)\\[/video\\]", youtubeEmbed());
 		bbcode = bbcode.replaceAll("\\[video\\]http://www.youtube.com/watch\\?v=([A-Za-z0-9]+)\\[/video\\]", youtubeEmbed());
 		return bbcode;
 	}
 
 	private static String youtubeEmbed() {
 		return "<iframe width=\"560\" height=\"315\" src=\"http://www.youtube.com/embed/$1\" frameborder=\"0\" allowfullscreen></iframe>";
 	}
 
 	private static String generateLinkableLinks(String text) {
 		int startTagBegin;
 
 		while((startTagBegin = text.indexOf("[linkable=")) >= 0) {
 			int startTagEnd = text.indexOf("]", startTagBegin);
 
 			if (startTagEnd < 0) {
 				break;
 			}
 
 			int endTagPos = text.indexOf("[/linkable]", startTagEnd);
 
 			if (endTagPos < 0) {
 				break;
 			}
 
 			String[] targetParts = text.substring(startTagBegin + 10, startTagEnd).split(":");
 
 			String startTag;
 			String endTag;
 
 			if (targetParts.length == 2) {
 				try {
 					Class<?> targetClass = Class.forName(targetParts[0]);
 					Long targetId = Long.valueOf(targetParts[1]);
 
 					Linkable target = (Linkable) Model.getByKey(targetClass, targetId);

					startTag = "<a href=\"" + StringUtils.escapeHtml(target.getUrl()) + "\">";
					endTag = "</a>";
 				}
 				catch (ClassNotFoundException e) {
 					startTag = "<!-- linkable: could not resolve entity class -->";
 					endTag = "<!-- /linkable -->";
 				}
 				catch (NumberFormatException e) {
 					startTag = "<!-- linkable: id is not a valid long -->";
 					endTag = "<!-- /linkable -->";
 				}
 				catch (ClassCastException e) {
 					startTag = "<!-- linkable: target is not a linkable -->";
 					endTag = "<!-- /linkable -->";
 				}
 			}
 			else {
 				startTag = "<!-- linkable: malformed tag -->";
 				endTag = "<!-- /linkable -->";
 			}
 
 			StringBuilder sb = new StringBuilder();
 
 			sb.append(text.substring(0, startTagBegin));
 			sb.append(startTag);
 			sb.append(text.substring(startTagEnd + 1, endTagPos));
 			sb.append(endTag);
 			sb.append(text.substring(endTagPos + 11));
 
 			text = sb.toString();
 		}
 
 		return text;
 	}
 
 	/**
 	 * Add BBCode generate html plugins
 	 * 
 	 * @param key ex \\[b\\](.+?)\\[/b\\]
 	 * @param value ex <span style=\"font-weight:bold;\">$1</span>
 	 */
 	public static void addPlugin(String key, String value) {
 		bbMapPlugins.put(key, value);
 	}
 
 	public static void setEndEvent(BBCodeCreatorEndEvent endEvent2) {
 		endEvent = endEvent2;
 	}
 
 	public static String getPreviewJspUrl() {
 		return previewJspUrl;
 	}
 
 	public static void setPreviewJspUrl(String jspUrl) {
 		previewJspUrl = jspUrl;
 	}
 }
