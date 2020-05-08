 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Quicker;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.util.LinkedList;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpressionException;
 import org.xml.sax.SAXException;
 import Quicker.exceptions.ControllerException;
 import Quicker.provider.Provider;
 import com.sun.jersey.api.client.UniformInterfaceException;
 import java.net.URL;
 import org.w3c.dom.Node;
 
 /**
  *
  * @author Илья
  */
 public class Controller {
 
 	private static Controller instance = null;
 	private Provider provider;
 	private LinkedList<NoteListItem> noteList;
 	private Note note;
 
 	public static Controller getInstance() {
 		if (instance == null) {
 			instance = new Controller();
 		}
 		return instance;
 	}
 
 	private Controller() {
 		try {
 			provider = new Provider(new URL("https://localhost:8181/Quicker/"), "maxim", "123");
 		} catch (MalformedURLException ex) {
 			Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
 		}
 		noteList = new LinkedList<NoteListItem>();
 	}
 
 	public LinkedList<NoteListItem> getNoteList()
 			throws ControllerException {
 		String notes;
 		notes = provider.get("maxim/notes/", String.class);
 
 		try {
 			XMLParser parser = new XMLParser(notes);
 			double num = (Double) parser.execute("count(//note)",
 					XPathConstants.NUMBER);
 
 			NoteListItem item;
 			LinkedList<Media> media;
 			for (int i = 1; i <= num; i++) {
 				media = new LinkedList<Media>();
 				if (((String) parser.execute("/notes/note[" + i + "]/@audio", XPathConstants.STRING)).equals("1")) {
 					media.add(Media.Audio);
 				}
 				if (((String) parser.execute("/notes/note[" + i + "]/@video", XPathConstants.STRING)).equals("1")) {
 					media.add(Media.Video);
 				}
 				if (((String) parser.execute("/notes/note[" + i + "]/@image", XPathConstants.STRING)).equals("1")) {
 					media.add(Media.Graphics);
 				}
 				item = new NoteListItem(
 						Integer.parseInt((String) parser.execute("/notes/note[" + i + "]/id", XPathConstants.STRING)),
 						(String) parser.execute("/notes/note[" + i + "]/title", XPathConstants.STRING),
 						(String) parser.execute("/notes/note[" + i + "]/extractions", XPathConstants.STRING),
 						(String) parser.execute("/notes/note[" + i + "]/date", XPathConstants.STRING),
 						media);
 				noteList.add(item);
 			}
 		} catch (ParserConfigurationException ex) {
 			Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
 			throw new ControllerException(ex);
 		} catch (XPathExpressionException ex) {
 			throw new ControllerException(ex);
 		} catch (SAXException ex) {
 			throw new ControllerException(ex);
 		} catch (IOException ex) {
 			throw new ControllerException(ex);
 		}
 
 		return noteList;
 	}
 
 	public Note getNote(int id) throws ControllerException {
 		String sNote;
 		sNote = provider.get("maxim/note/" + id, String.class);
 
 		System.out.println(sNote);
 		try {
 			XMLParser parser = new XMLParser(sNote);
 			String text = (String) parser.execute("/note/content/text",
 					XPathConstants.STRING);
 			String title = (String) parser.execute("/note/title",
 					XPathConstants.STRING);
 
 			int count = 0;
 			String c = (String) parser.execute("count(/note/content/entity[@type='video'])",
 					XPathConstants.STRING);
 			if (!c.equals("")) {
 				count = Integer.parseInt(c);
 			}
 			LinkedList<String> videos = new LinkedList<String>();
 			for (int i = 1; i <= count; i++) {
 				String video = (String) parser.execute("/note/content/entity[@type='video'][" + i + "]/@name",
 						XPathConstants.STRING);
 				videos.add(video);
 			}
 
 			count = 0;
 			c = (String) parser.execute("count(/note/content/entity[@type='audio'])",
 					XPathConstants.STRING);
 			if (!c.equals("")) {
 				count = Integer.parseInt(c);
 			}
 			LinkedList<String> audios = new LinkedList<String>();
 			for (int i = 1; i <= count; i++) {
 				String audio = (String) parser.execute("/note/content/entity[@type='audio'][" + i + "]/@name",
 						XPathConstants.STRING);
 				audios.add(audio);
 			}
 
 			count = 0;
 			c = (String) parser.execute("count(/note/content/entity[@type='image'])",
 					XPathConstants.STRING);
 			if (!c.equals("")) {
 				count = Integer.parseInt(c);
 			}
 			LinkedList<String> images = new LinkedList<String>();
 			for (int i = 1; i <= count; i++) {
 				String image = (String) parser.execute("/note/content/entity[@type='image'][" + i + "]/@name",
 						XPathConstants.STRING);
 				images.add(image);
 			}
 			String date = (String) parser.execute("/note/date", XPathConstants.STRING);
 
 			note = new Note(id, title, text, videos, images, audios, date);
 		} catch (ParserConfigurationException ex) {
 			throw new ControllerException(ex);
 		} catch (SAXException ex) {
 			throw new ControllerException(ex);
 		} catch (IOException ex) {
 			throw new ControllerException(ex);
 		} catch (XPathExpressionException ex) {
 			throw new ControllerException(ex);
 		}
 		System.out.println("Controll " + note.getText());
 		return note;
 	}
 
 	public Note newNote() {
 		note = new Note();
 		return note;
 	}
 
 	public Boolean saveNewNote(Note noteToSave) {
 		System.out.println("New saved. ");
 		String toSend = "<note>"
 				+ "<title>" + noteToSave.getTitle() + "</title>"
 				+ "<content>"
				+ "<text><![CDATA[" + noteToSave.getText() + "]]></text>";
 		LinkedList<String> imgs = noteToSave.getImages();
 		if (imgs.size() > 0) {
 			for (String img : imgs) {
 				toSend += "<entity name='" + img + "' type='image'/>";
 			}
 		}
 		LinkedList<String> vids = noteToSave.getVideos();
 		if (vids.size() > 0) {
 			for (String vid : vids) {
 				toSend += "<entity name='" + vid + "' type='video'/>";
 			}
 		}
 		LinkedList<String> auds = noteToSave.getAudios();
 		if (auds.size() > 0) {
 			for (String a : imgs) {
 				toSend += "<entity name='" + a + "' type='audio'/>";
 			}
 		}
 		toSend += "</content><date>" + noteToSave.getDate() + "</date>"
 				+ "</note>";
 		Boolean res = true;
 		try {
 			provider.post("maxim/note", toSend, "application/xml");
 		} catch (UniformInterfaceException uniformInterfaceException) {
 			res = false;
 		}
 		return res;
 	}
 
 	public Boolean saveNote(Note noteToSave) {
 		System.out.println("Saved. ");
 		String toSend = "<note>"
 				+ "<id>" + noteToSave.getId() + "</id>"
 				+ "<title>" + noteToSave.getTitle() + "</title>"
 				+ "<content>"
				+ "<text><![CDATA[" + noteToSave.getText() + "]]></text>";
 		LinkedList<String> imgs = noteToSave.getImages();
 		if (imgs.size() > 0) {
 			for (String img : imgs) {
				toSend += "<image>" + img + "</image>";
 			}
 		}
 		LinkedList<String> vids = noteToSave.getVideos();
 		if (vids.size() > 0) {
 			for (String vid : vids) {
				toSend += "<image>" + vid + "</image>";
 			}
 		}
 		LinkedList<String> auds = noteToSave.getAudios();
 		if (auds.size() > 0) {
 			for (String a : imgs) {
				toSend += "<image>" + a + "</image>";
 			}
 		}
 		toSend += "</content><date>" + noteToSave.getDate() + "</date>"
 				+ "</note>";
 		// ToDo: update note list
 		Boolean res = true;
 		try {
 			provider.put("maxim/note/" + noteToSave.getId(), toSend, "application/xml");
 		} catch (UniformInterfaceException uniformInterfaceException) {
 			res = false;
 		}
 		return res;
 	}
 
 	public void deleteNote(int id) {
 		provider.delete("maxim/note/" + id);
 	}
 
 	public LinkedList<NoteListItem> getEventList() {
 		return null;
 	}
 
 	public LinkedList<NoteListItem> getContactList() {
 		return null;
 	}
 }
