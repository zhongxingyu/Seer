 package devhood.im.sim.ui.util;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.inject.Named;
 
 /**
  * Nutzt die Smileys von Yahoo Messenger.
  * 
  * @author Schiefele.Andreas
  */
 @Named("yahooSmileyFactory")
 public class YahooSmileyFactory implements SmileyFactory {
 
 	/* Hier sind zu jedem Smiley-Kürzel die Grafik-Pfade hinterlegt. */
 	private Map<String[], String> smileys = new HashMap<String[], String>();
 
 	private String imagePath = "/images/yahoo_smileys/";
 	
 	/**
 	 * Tauscht die Smiley-Kürzel gegen die Smiley-Grafiken aus.
 	 * 
 	 * @param String
 	 *            text Der Text in dem die Smiley's ausgetausch werden sollen.
 	 */
 	public String applySmiles(String text) {
 
 		for (String[] keys : smileys.keySet()) {
 			for (String key : keys) {
 				if (text.equals(key)) {
 					return "<img src=\""
 							+ getClass().getResource(
 									imagePath + smileys.get(keys)).toString()
 							+ " \" />";
 				}
 			}
 		}
 
 		return text;
 	}
 
 	/**
 	 * Die Initialisierung führt das Pairing der Kurzschreibweisen zu den
 	 * Bildnamen durch.
 	 */
 	public void init() {
 
 		smileys.put(new String[] { ":)", ":-)" }, "01.gif");
 		smileys.put(new String[] { ":(", ":-(" }, "02.gif");
 		smileys.put(new String[] { ";)", ";-)" }, "03.gif");
 		smileys.put(new String[] { ":-D", ":D" }, "04.gif");
 		smileys.put(new String[] { ";;)", ";;-)" }, "05.gif");
 		smileys.put(new String[] { ":-\\", ":-/" }, "06.gif");
 		smileys.put(new String[] { ":-x", ":x", ":-X", ":X" }, "07.gif");
 		smileys.put(new String[] { ":\"&gt;", ":-\"&gt;" }, "08.gif");
 		smileys.put(new String[] { ":-p", ":-P", ";-p", ";-P", ":p", ":P",
 				";p", ";P", ":-)p", ":-)P", ";-)p", ";-)P", ":)p", ":)P",
 				";)p", ";)P" }, "09.gif");
 		smileys.put(new String[] { ":*", ":-*" }, "10.gif");
 		smileys.put(new String[] { ":O", ":-O", ":o", ":-o" }, "11.gif");
 		smileys.put(new String[] { "X-(", "X(", "x-(", "x(" }, "12.gif");
 		smileys.put(new String[] { ":-&gt;", ":&gt;" }, "13.gif");
 		smileys.put(new String[] { "B-)", "B)" }, "14.gif");
 		smileys.put(new String[] { ":s", ":-s", ":S", ":-S" }, "15.gif");
 		smileys.put(new String[] { "&gt;:)", "&gt;:-)" }, "16.gif");
 		smileys.put(new String[] { ":-((", ":((" }, "17.gif");
 		smileys.put(new String[] { ":-))", ":))" }, "18.gif");
 		smileys.put(new String[] { ":-|", ":|" }, "19.gif");
 		smileys.put(new String[] { "/:)", "/:-)" }, "20.gif");
 		smileys.put(new String[] { "0:)", "0:-)", "O:)", "O:-)" }, "21.gif");
 		smileys.put(new String[] { ":-B", ":B", "8)", "8-)" }, "22.gif");
 		smileys.put(new String[] { "=;-" }, "23.gif");
 		smileys.put(new String[] { "|-)", "|)", "I-)", "I)", "zzz" }, "24.gif");
 		smileys.put(new String[] { "8-|", "8|" }, "25.gif");
 		smileys.put(new String[] { ":-&", ":&" }, "26.gif");
 		smileys.put(new String[] { ":-$", ":$" }, "27.gif");
 		smileys.put(new String[] { "[-(" }, "28.gif");
 		smileys.put(new String[] { ":o)" }, "29.gif");
 		smileys.put(new String[] { "8-}" }, "30.gif");
 		smileys.put(new String[] { ":|", ":-|" }, "31.gif");
 		smileys.put(new String[] { "=P~" }, "32.gif");
 		smileys.put(new String[] { ":-?", ":?" }, "33.gif");
 		smileys.put(new String[] { "#-o", "#-O" }, "34.gif");
 		smileys.put(new String[] { "=D&gt;" }, "35.gif");
 		smileys.put(new String[] { ":@)" }, "36.gif");
 		smileys.put(new String[] { "3:-O" }, "37.gif");
 		smileys.put(new String[] { ":(|)" }, "38.gif");
 		smileys.put(new String[] { "~:&gt;" }, "39.gif");
 		smileys.put(new String[] { "@};-" }, "40.gif");
 		smileys.put(new String[] { "%%-" }, "41.gif");
 		smileys.put(new String[] { "**==" }, "42.gif");
 		smileys.put(new String[] { "(~~)" }, "43.gif");
 		smileys.put(new String[] { "~o)" }, "44.gif");
 		smileys.put(new String[] { "*-:)" }, "45.gif");
 		smileys.put(new String[] { "8-X", "8-x" }, "46.gif");
 		smileys.put(new String[] { "=:)", "=:-)" }, "47.gif");
 		smileys.put(new String[] { "&gt;-)" }, "48.gif");
 		smileys.put(new String[] { ":-L" }, "49.gif");
 		smileys.put(new String[] { "&lt;):)", "&lt;):-)" }, "50.gif");
 		smileys.put(new String[] { "[-o&lt;", "[o&lt;" }, "51.gif");
 		smileys.put(new String[] { "@-)" }, "52.gif");
 		smileys.put(new String[] { "$-)" }, "53.gif");
 		smileys.put(new String[] { ":-\"" }, "54.gif");
 		smileys.put(new String[] { ":^o" }, "55.gif");
 		smileys.put(new String[] { "b-(" }, "56.gif");
 		smileys.put(new String[] { ":)&gt;-", ":-)&gt;-" }, "57.gif");
 		smileys.put(new String[] { "[-X", "[-x" }, "58.gif");
 		smileys.put(new String[] { "\\:D/", "\\:-D/" }, "59.gif");
 		smileys.put(new String[] { "&gt;:D&lt;", "&gt;:-D&lt;" }, "60.gif");
 		smileys.put(new String[] { "o-&gt;)" }, "61.gif");
 		smileys.put(new String[] { "o=&gt;" }, "62.gif");
 		smileys.put(new String[] { "o-+" }, "63.gif");
 		smileys.put(new String[] { "(%)" }, "64.gif");
 		smileys.put(new String[] { "^:)^", "^:-)^" }, "77.gif");
 		smileys.put(new String[] { ":-j", ":-J" }, "78.gif");
 		smileys.put(new String[] { "(*)" }, "79.gif");
 		smileys.put(new String[] { ":)]" }, "100.gif");
 		smileys.put(new String[] { ":-c", ":-C" }, "101.gif");
 		smileys.put(new String[] { "~X(", "~x(", "~X-(", "~x-(" }, "102.gif");
 		smileys.put(new String[] { ":-h", ":-)h" }, "103.gif");
 		smileys.put(new String[] { ":-t" }, "104.gif");
 		smileys.put(new String[] { "8-&gt;" }, "105.gif");
 		smileys.put(new String[] { ":-??" }, "106.gif");
 		smileys.put(new String[] { "%-(" }, "107.gif");
 		smileys.put(new String[] { ":o3" }, "108.gif");
 		smileys.put(new String[] { "x_x" }, "109.gif");
 		smileys.put(new String[] { ":!!" }, "110.gif");
 		smileys.put(new String[] { "\\m/" }, "111.gif");
 		smileys.put(new String[] { ":-q" }, "112.gif");
 		smileys.put(new String[] { ":-bd", ":-)bd", ":)bd", "b:)d", "b:-)d",";-bd", ";-)bd", ";)bd", "b;)d", "b;-)d" },
 				"113.gif");
 		smileys.put(new String[] { "^#(^", "#(^^" }, "114.gif");
 		smileys.put(new String[] { ":ar!" }, "116.gif");
 		smileys.put(new String[] { "~^o^~", ":-)" }, "120.gif");
 		smileys.put(new String[] { "'@^@|||" }, "121.gif");
 		smileys.put(new String[] { "[]---" }, "122.gif");
 		smileys.put(new String[] { "^o^||3" }, "123.gif");
 		smileys.put(new String[] { ":-(||&gt;" }, "124.gif");
 		smileys.put(new String[] { "'+_+" }, "125.gif");
 		smileys.put(new String[] { ":::^^:::" }, "126.gif");
 		smileys.put(new String[] { "o|^_^|o" }, "127.gif");
 		smileys.put(new String[] { ":puke!" }, "128.gif");
 		smileys.put(new String[] { "o|\\~" }, "129.gif");
		smileys.put(new String[] { "o|\\~" }, "130.gif");
		smileys.put(new String[] { "o|:-)", "o|:)" }, "131.gif");
		smileys.put(new String[] { "[]==[]" }, "132.gif");
		smileys.put(new String[] { ":-)/\\:-)", ":)/\\:)" }, "133.gif");
		smileys.put(new String[] { ":(game)" }, "134.gif");
		smileys.put(new String[] { "@-@" }, "135.gif");
		smileys.put(new String[] { ":-&gt;~~" }, "136.gif");
		smileys.put(new String[] { "?@_@?" }, "137.gif");
		smileys.put(new String[] { ":(tv)" }, "138.gif");
		smileys.put(new String[] { "&[]" }, "139.gif");
		smileys.put(new String[] { "%||:-{", "%||:{" }, "140.gif");
 		smileys.put(new String[] { ":(fight)" }, "141.gif");
 		smileys.put(new String[] { "[..]" }, "142.gif");
 	}
 }
