 package burrito.util;
 
 import java.util.HashMap;
 import java.util.Map;
 
 @SuppressWarnings("rawtypes")
 public class BBCodeCreator {
 	
 	private static Map<String, String> bbMapPlugins = new HashMap<String, String>();
 	private static String previewJspUrl = "/burrito/bbCodePreview.jsp";
 	
 	public static String generateHTML(String bbcode) {
 		String html = StringUtils.escapeHtml(bbcode);
 		
         Map<String, String> bbMap = new HashMap<String, String>();
 
         bbMap.put("(\r\n|\r|\n|\n\r)", "<br/>");
         bbMap.put("\\[b\\](.+?)\\[/b\\]", "<span style=\"font-weight:bold;\">$1</span>");
        bbMap.put("\\[i\\](.+?)\\[/i\\]", "<span style=\"font-weight:italic;\">$1</span>");
         bbMap.put("\\[url\\](.+?)\\[/url\\]", "<a href=\"$1\">$1</a>");
         bbMap.put("\\[img\\](.+?)\\[/img\\]", "<img src=\"/blobstore/image?key=$1\" />");
         bbMap.put("\\[url=(.+?)\\](.+?)\\[/url\\]", "<a href=\"$1\">$2</a>");
         
         for (Map.Entry entry : bbMap.entrySet()) {
             html = html.replaceAll(entry.getKey().toString(), entry.getValue().toString());
         }
         for (Map.Entry entry : bbMapPlugins.entrySet()) {
             html = html.replaceAll(entry.getKey().toString(), entry.getValue().toString());
         }
         
         html = generateYouTube(html);
         
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
 	
 	/**
 	 * Add BBCode generate html plugins
 	 * 
 	 * @param key ex \\[b\\](.+?)\\[/b\\]
 	 * @param value ex <span style=\"font-weight:bold;\">$1</span>
 	 */
 	public static void addPlugin(String key, String value) {
 		bbMapPlugins.put(key, value);
 	}
 
 	public static String getPreviewJspUrl() {
 		return previewJspUrl;
 	}
 	
 	public static void setPreviewJspUrl(String jspUrl) {
 		previewJspUrl = jspUrl;
 	}
 
 }
