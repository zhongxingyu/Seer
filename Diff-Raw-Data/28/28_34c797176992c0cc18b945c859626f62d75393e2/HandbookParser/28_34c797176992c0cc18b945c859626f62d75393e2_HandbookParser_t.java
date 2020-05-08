 package handbook;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import org.json.simple.JSONObject;
 import org.json.simple.parser.JSONParser;
 import org.json.simple.parser.ParseException;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 public class HandbookParser {
 
 	private String chapterPath;
 	private String jsonFilePath = "Handbook/allchapters.json";
 
 	public HandbookParser() {
 		if (!(new File(jsonFilePath).exists())) {
 			createJSONFile();
 		}
 	}
 
 	public ArrayList<Chapter> getMainChapters() {
 		JSONParser parser = new JSONParser();
 		Object obj;
 		JSONObject jsonObject;
 		ArrayList<Chapter> allChapters = new ArrayList<Chapter>();
 		try {
 			obj = parser.parse(new FileReader(jsonFilePath));
 			jsonObject = (JSONObject) obj;
 
 			for (int i = 0; i < jsonObject.size(); i++) {
 				JSONObject chapter = (JSONObject) jsonObject.get(i + "");
 				System.out.println("Loading from JSON file... " + (i + 1) + "/"
 						+ jsonObject.size());
 				allChapters.add(new Chapter(chapter));
 			}
 
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return allChapters;
 
 	}
 	
 	public void createJSONFile(ArrayList<Chapter> allChapters) {
 		JSONObject obj = new JSONObject();
 		int count = 0;
 		System.out.println("Creating JSON objects");
 		for (Chapter chapter : allChapters) {
 			obj.put(count, chapter.toJSON());
 			count++;
 		}
 		System.out.println("Saving JSON objects");
 		try {
 
 			FileWriter file = new FileWriter(jsonFilePath);
 			file.write(obj.toJSONString());
 			file.flush();
 			file.close();
 
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void createJSONFile() {
 		ArrayList<Chapter> allChapters = new ArrayList<Chapter>();
 		Document doc;
 
 		File folder = new File("Handbook/html/L/");
 		for (File file : folder.listFiles()) {
 			doc = Jsoup.parse(readFile(file));
 			chapterPath = file.getPath();
 			System.out.println("Parsing " + file.getName());
 			allChapters.add(getChapter(doc));
 		}
 		folder = new File("Handbook/html/T/");
 		for (File file : folder.listFiles()) {
 			doc = Jsoup.parse(readFile(file));
 			chapterPath = file.getPath();
 			System.out.println("Parsing " + file.getName());
 			allChapters.add(getChapter(doc));
 		}
 		JSONObject obj = new JSONObject();
 		int count = 0;
 		System.out.println("Creating JSON objects");
 		for (Chapter chapter : allChapters) {
 			obj.put(count, chapter.toJSON());
 			count++;
 		}
 		System.out.println("Saving JSON objects");
 		try {
 
 			FileWriter file = new FileWriter(jsonFilePath);
 			file.write(obj.toJSONString());
 			file.flush();
 			file.close();
 
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private Chapter getChapter(Document doc) {
 		if (!doc.getElementsByClass("seksjon2").isEmpty()) {
 			return createChapter(doc.getElementsByClass("seksjon2").get(0));
 		}
 		return null;
 	}
 	
	public static void main(String[] args) {
		File file = new File("Handbook/html/T/T15.1.htm");
		HandbookParser hp = new HandbookParser();
		Document doc = Jsoup.parse(hp.readFile(file));
		Chapter c = hp.createChapter(doc.getElementById("i19650"));
		for (String string : c.getTextLines()) {
			System.out.println(string);
		}
	}
	
 	private Chapter createChapter(Element element) {
 		ArrayList<Chapter> subchapters = new ArrayList<Chapter>();
 		String text = "";
 
 		if (!element.className().equals("seksjon4")) {
 			Elements subchapterElements = element
 					.getElementsByClass(nextclassName(element.className()));
 			for (Element subchapterElement : subchapterElements) {
 				subchapters.add(createChapter(subchapterElement));
 			}
 		}
 
 		Elements allElements = element.getAllElements();
 		Elements headersToBeIgnored = element.select("h5");
 
 		for (Element e : allElements) {
 			if (e.parent().id().equals(element.id())) {
 				if (e.className().equals("def")) {
 					text += getText(e, headersToBeIgnored) + ". ";
 				} else if (e.className().equals("sub8")) {
 					for (Element sub8e : e.children()) {
 						if (sub8e.className().equals("def")) {
 							text += getText(sub8e, headersToBeIgnored) + ". ";
 						} else if ( sub8e.className().equals("") && !sub8e.text().equals("")) {
 							text += sub8e.text() + ". ";
 						}
 					}
 				}
 				
 			}
 		}
 		for (Element child : element.children()) {
 			if (child.className().equals("") && !child.text().equals("")) {
 				//To increase the importance of headlines
 				if (!child.tagName().equals("p")) {
 					text += "\n" + child.text() + ". ";
 					text += "\n" + child.text() + ". ";
 				}
 				text += "\n" + child.text() + ". ";
 			}
 		}
 		text = text.replaceAll("[0-9]", "");
 		text = text.replace("/", " ");
 		text = text.replace("-", " ");
 		text = text.replace("T.", "");
 		text = text.replace("T..", "");
 		text = text.replace("T...", "");
 		text = text.replace("L.", "");
 		text = text.replace("L..", "");
 		text = text.replace("L...", "");
 		
 		ArrayList<String> textLines = getTextLines(text);
 		
 		Chapter c = new Chapter(chapterPath + "#" + element.id(), textLines, subchapters);
 		String chapterName = element.child(0).ownText().replace("*", "");
 		c.setName(chapterName);
 		return c;
 	}
 
 	/**
 	 * We don't want headers in our text, this method removes it. Removes some
 	 * symbols
 	 * 
 	 * @param e
 	 * @param headersToBeIgnored
 	 * @return String
 	 */
 	private String getText(Element e, Elements headersToBeIgnored) {
 		String text = "";
 		for (Element child : e.children()) {
 			if (!headersToBeIgnored.contains(child)) {
 				text += child.text() + ". ";
 			}
 		}
 		
 		return text;
 	}
 
 	private ArrayList<String> getTextLines(String fullText) {
 		String[] lines = fullText.split("\\. ");
 		ArrayList<String> temp = new ArrayList<String>();
 		
 		
 		for (String string : lines) {
			string = string.replaceAll("[^a-zA-Z ]", "");
 			if (string.length() > 4)
 				temp.add(string);
 		}
 		return temp;
 	}
 
 	private String nextclassName(String className) {
 		int newSection = Integer.parseInt(className.substring(
 				className.length() - 1, className.length())) + 1;
 		String newClassName = className.substring(0, className.length() - 1)
 				+ newSection;
 		return newClassName;
 	}
 
 	private String readFile(File file) {
 		String html = "";
 
 		BufferedReader br;
 		try {
 			br = new BufferedReader(new FileReader(file));
 			String nextLine = br.readLine();
 			do {
 				html += nextLine + "\n";
 				nextLine = br.readLine();
 			} while (nextLine != null);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		html = fixNorwegianLetters(html);
 		return html;
 	}
 
 	private String fixNorwegianLetters(String text) {
 		text = text.replace("&aring;", "");
 		text = text.replace("&oslash;", "");
 		text = text.replace("&aelig;", "");
 
 		return text;
 	}
 
 }
