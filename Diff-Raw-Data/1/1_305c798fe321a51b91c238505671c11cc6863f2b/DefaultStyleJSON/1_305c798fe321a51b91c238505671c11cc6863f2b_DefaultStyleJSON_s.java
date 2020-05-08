 package mobi.monaca.framework.nativeui;
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 /**
  * default values of UIStyle
  * note that default imagepath options should be undefined.
  */
 public class DefaultStyleJSON {
 	private DefaultStyleJSON() {};
 	public static JSONObject toolbar() {
 		JSONObject style = new JSONObject();
 		try {
 			style.put("visibility", true);
 			style.put("disable", false);
 			style.put("opacity", 1.0);
 			style.put("title", "");
 			style.put("subtitle", "");
 			style.put("backgroundColor", "#000000");
 			style.put("titleColor", "#FFFFFF");
 			style.put("subtitleColor", "#FFFFFF");
 			style.put("titleFontScale", 1.0);
 			style.put("subtitleFontScale", 1.0);
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 		return style;
 	}
 
 	public static JSONObject button() {
 		JSONObject style = new JSONObject();
 		try {
 			style.put("visibility", true);
 			style.put("disable", false);
 			style.put("opacity", 1.0);
 			style.put("backgroundColor", "#000000");
 			style.put("activeTextColor", "#0000FF");
 			style.put("text", "");
 			style.put("textColor", "#FFFFFF");
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 		return style;
 	}
 
 	public static JSONObject backButton() {
 		JSONObject style = new JSONObject();
 		try {
 			style.put("visibility", true);
 			style.put("disable", false);
 			style.put("opacity", 1.0);
 			style.put("activeTextColor", "#0000FF");
 			style.put("text", "");
 			style.put("textColor", "#FFFFFF");
 			style.put("forceVisibility", false);
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 		return style;
 	}
 
 	public static JSONObject segment() {
 		JSONObject style = new JSONObject();
 		try {
 			style.put("visibility", true);
 			style.put("disable", false);
 			style.put("opacity", 1.0);
 			style.put("activeTextColor", "#0000FF");
 			style.put("backgroundColor", "#000000");
			style.put("texts", new JSONArray());
 			style.put("textColor", "#FFFFFF");
 			style.put("activeIndex", 0);
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 		return style;
 	}
 
 	public static JSONObject label() {
 		JSONObject style = new JSONObject();
 		try {
 			style.put("opacity", 1.0);
 			style.put("text", "");
 			style.put("textColor", "#FFFFFF");
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 		return style;
 	}
 
 	public static JSONObject searchBox() {
 		JSONObject style = new JSONObject();
 		try {
 			style.put("visibility", true);
 			style.put("disable", false);
 			style.put("opacity", 1.0);
 			style.put("backgroundColor", "#FFFFFF");
 			style.put("textColor", "#000000");
 			style.put("focus", false);
 			style.put("placeholder", "");
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 		return style;
 	}
 	public static JSONObject tabbar() {
 		JSONObject style = new JSONObject();
 		try {
 			style.put("visibility", true);
 			style.put("opacity", 1.0);
 			style.put("backgroundColor", "#000000");
 			style.put("activeIndex", 0);
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 		return style;
 	}
 
 	public static JSONObject tabbarItem() {
 		JSONObject style = new JSONObject();
 		try {
 			style.put("text", "");
 			style.put("backgroundColor", "#000000");
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 		return style;
 	}
 	public static JSONObject page() {
 		JSONObject style = new JSONObject();
 		return style;
 	}
 }
