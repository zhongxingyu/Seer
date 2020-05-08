 package com.crosslink.server;
 
 /**
  * @author Hazirah Hamdani
  * 
  */
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import org.apache.commons.lang.WordUtils;
 import org.apache.commons.lang.StringUtils;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 import com.crosslink.client.Webcontent;
 import com.crosslink.client.wikiMEService;
 
 @SuppressWarnings("serial")
 public class wikiMEServiceImpl extends RemoteServiceServlet implements
 		wikiMEService {
 
 	public static final int STATUS_CODE_OK = 200;
 	public ArrayList<String> MalayWordList;
 	private static List<String> anchoredWords = new ArrayList<String>();
 	private static ArrayList<String> newCon = new ArrayList<String>();
 	private static HashMap<String, String> wordList = new HashMap<String, String>();
 
 
 	/***
 	 * Connect to website url to get the HTML contents and parse contents to
 	 * anchor Malay words
 	 * 
 	 */
 	public Webcontent getwebcontent(String weburl, String MalayTable) {
 		// Make Hashmap
 		String MalayList[] = MalayFileList(MalayTable);
 		String mWord = "";
 		String wiki = "";
 		// Take both Malay Word and Wikipedia Index
 		for (int i = 0; i < MalayList.length; i++) {
 			String MalayWord[] = MalayList[i].split(":");
 
 			mWord = MalayWord[0];
 			wiki = MalayWord[1];
 
 			// Insert mWord and wiki into HashMap
 			wordList.put(mWord, wiki);
 		}
 		
 		Document doc = null;
 		String content = null;
 		try {
 			// grab HTML contents form website
 			doc = Jsoup.connect(weburl).timeout(0).get();
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			content = "Could not catch web content";
 		}
 
 		String test = "BANDAR SERI BEGAWAN, 2 Okt - Kebawah Duli Yang Maha Mulia Paduka Seri Baginda "
 				+ "Sultan Haji Hassanal Bolkiah Mu'izzaddin Waddaulah, Sultan dan Yang Di-Pertuan Negara Brunei "
 				+ "Darussalam telah berkenan menghantar titah perutusan tahniah kepada Presiden Republik Korea, "
 				+ "Lee Myung-Bak serta kerajaan dan rakyat Korea bersempena dengan Hari KebangsaanPengasasan Republik Korea.";
 
 		
 		if (weburl.contains("mediapermata")) {
 			Elements text = doc.select("p");
 
			for (Element t : text) {
				String tmp = t.toString();
 				// get web content to anchor
				String webText = t.text();
 				// Find anchor words
 				//System.out.println(webText);
 				String anchorContent = findWord(webText, MalayTable);
				t.html(anchorContent);
				t.prepend("<p>");
				t.append("</p>");
 
 
 			}
 		}
 
 		// if website newspaper from bharian
 		if (weburl.contains("bharian")) {
 			Element text = doc.select("div.article-body").first();
 			String tmp = text.html();
 			// Find anchor words
 			String anchorContent = findWord(tmp, MalayTable);
 			text.html(anchorContent);
 			text.prepend("<div class=\"article-body\">");
 			text.append("</div>");
 
 		}
 		content = doc.html();
 
 		// fixing broken images
 		Elements imgSrc = doc.select("img[src]");
 		for (Element img : imgSrc) {
 			String absHref = img.absUrl("abs:src");
 			String imgAttr = img.attr("src");
 			content = content.replace(imgAttr, absHref);
 
 		}
 		
 		
 		// System.out.println(content);
 		Webcontent webcontent = new Webcontent();
 		webcontent.setwebContent(content);
 
 		return webcontent;
 	}
 
 	/**
 	 * Tag Malay word
 	 * 
 	 * @param content
 	 * @param malayTable
 	 * @return Tagged Malay Word
 	 */
 	private String findWord(String content, String malayTable) {
 		anchoredWords.clear();
 		newCon.clear();
 		ArrayList<String> MalayList = new ArrayList<String>();
 		MalayList = getMalayWord(malayTable);
 		String con = null;
 		String WordsToAnchor[] = content.split(" ");
 
 		boolean found;
 		// Find word by word - article can have up to 4 words that is available
 		// in wikipedia
 		int WordsMax = WordsToAnchor.length;
 
 		for (int j = 0; j < WordsMax; j++) {
 			
 			String tmpWord1 = null;
 			String tmpWord2 = null;
 			String tmpWord3 = null;
 			String tmpWord4 = null;
 			if (j + 3 < WordsMax) {
 				tmpWord4 = WordsToAnchor[j] + " " + WordsToAnchor[j + 1] + " "
 						+ WordsToAnchor[j + 2] + " " + WordsToAnchor[j + 3];
 				// remove punctuation
 				tmpWord4 = tmpWord4.replaceAll("[\\p{Punct}&&[^<>./]]", "");
				System.out.print(tmpWord4 + "\n");
 			}
 
 			if (j + 2 < WordsMax) {
 				tmpWord3 = WordsToAnchor[j] + " " + WordsToAnchor[j + 1] + " "
 						+ WordsToAnchor[j + 2];
 				// remove punctuation
 				tmpWord3 = tmpWord3.replaceAll("[\\p{Punct}&&[^<>./]]", "");
 				
 			}
 
 			if (j + 1 < WordsMax) {
 				tmpWord2 = WordsToAnchor[j] + " " + WordsToAnchor[j + 1];
 				// remove punctuation
 				tmpWord2 = tmpWord2.replaceAll("[\\p{Punct}&&[^<>./]]", "");
 				
 			}
 
 			tmpWord1 = WordsToAnchor[j];
 			//remove punctuation
 			tmpWord1 = tmpWord1.replaceAll("[\\p{Punct}&&[^<>./]]", "");
 			found = false;
 			
 			// Find four words & if found, replace
 			String replacedTmpWord4 = tmpWord4;
 			if (tmpWord4 != null) {
 				replacedTmpWord4 = replaceWord(tmpWord4, MalayList);
 				if (replacedTmpWord4 != null) {
 					// add 3 to point position
 					j = j + 3;
 					// pointing that word has been found and replaced
 					found = true;
 					
 				}
 			}
 
 			String replacedTmpWord3 = tmpWord3;
 			if ((tmpWord3 != null) && (replacedTmpWord4 == null))  {
 				replacedTmpWord3 = replaceWord(tmpWord3, MalayList);
 				if (replacedTmpWord3 != null) {
 					j = j + 2;
 					found = true;
 					
 				}
 			}
 
 			String replacedTmpWord2 = tmpWord2;
 			if ((tmpWord2 != null) && (replacedTmpWord3 == null)) {
 				replacedTmpWord2 = replaceWord(tmpWord2, MalayList);
 				if (replacedTmpWord2 != null) {
 					j = j + 1;
 					found = true;
 					
 				}
 			}
 
 			String replacedTmpWord = tmpWord1;
 			if ((tmpWord1 != null) && (replacedTmpWord2 == null)) {
 				replacedTmpWord = replaceWord(tmpWord1, MalayList);
 				if (replacedTmpWord != null) {
 					found = true;
 				}
 			}
 
 			// if word not found, add the original text
 			if (found == false) {
 				newCon.add(WordsToAnchor[j]);
 			}
 
 		}
 		con = StringUtils.join(newCon, " ");
 		
 		return con;
 	}
 
 	private String replaceWord(String textString, ArrayList<String> MalayList) {
 		boolean replaced = false;
 		for (String word : MalayList) {
 			if ((StringUtils.equalsIgnoreCase(word, textString))
 					&& (!anchoredWords.contains(word))) {
 				
 				// Find English Wikipedia Link
 				String wikiLink = wordList.get(WordUtils.capitalizeFully(word));
 				String anchorURL = "http://en.wikipedia.org/wiki/index.php?curid="
 						+ wikiLink;
 				
 				// anchor word
 				textString = textString.replace(textString, "<a href=\"" + anchorURL + "\">"
 						+ textString + "</a>");
 				anchoredWords.add(word);
 				newCon.add(textString);
 				replaced = true;
 			}
 		}
 		
 		if (replaced == false) {
 		return null;
 		} else if (replaced == true) {
 			return textString;
 		}
 		
 		return null;
 	}
 
 	/**
 	 * Grab all Malay Word only
 	 * 
 	 * @return
 	 */
 	protected ArrayList<String> getMalayWord(String malayTable) {
 
 		String MalayList[] = MalayFileList(malayTable);
 		ArrayList<String> MalayArray = new ArrayList<String>();
 		for (int i = 0; i < MalayList.length; i++) {
 			String MalayWord[] = MalayList[i].split(":");
 			MalayArray.add(MalayWord[0]);
 		}
 		return MalayArray;
 	}
 
 	/**
 	 * Load Malay Words and Wikipedia Index from File
 	 * 
 	 * @return
 	 */
 	private String[] MalayFileList(String malayTable) {
 		String MalayList[] = malayTable.split("\\r?\\n");
 		return MalayList;
 	}
 
 	/**
 	 * Store MalayTable into HashMap and Map the Wiki from Hashmap using
 	 * get(Word)
 	 * 
 	 * @param Malay
 	 *            Word, MalayTable
 	 */
 	@Override
 	public String getMalayWiki(String word, String malayTable) {
 
 		return wordList.get(WordUtils.capitalizeFully(word));
 	}
 }
