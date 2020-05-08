 package controller;
 
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Map.Entry;
 
 import model.BaseTable;
 import model.entity.AudioAlbum;
 import model.entity.Author;
 import model.entity.Book;
 import model.entity.Genre;
 import model.entity.Movie;
 import model.enums.BookIndustryIdentifier;
 import model.utilities.AudioAlbumTrack;
 import settings.GlobalPaths;
 
 /**
  * Klasa tworzy opis HTML przekazanego obiektu.
  * @author kornicameister
  *
  */
 public class HTMLDescriptor {
 	
 	public static URL toHTML(BaseTable bt){
 		
 		String fullContent = new String();
 		fullContent += "<html><body>";
 		fullContent += HTMLDescriptor.generateBody(bt);
 		fullContent += "</body></html>";;
 
 		return HTMLDescriptor.saveToFileSystem(fullContent);
 	}
 
 	private static String generateBody(BaseTable bt) {
 		switch(bt.getTableType()){
 		case AUDIO_ALBUM:
 			return HTMLDescriptor.generateAudioAlbumBody((AudioAlbum)bt);
 		case MOVIE:
 			return HTMLDescriptor.generateMovieBody((Movie)bt);
 		case BOOK:
 			return HTMLDescriptor.generateBookBody((Book)bt);
 		default:
 			return new String();
 		}
 	}
 
 	private static String generateBookBody(Book bt) {
 		String bodyPart = new String();
 		bodyPart += "<h1>" + bt.getTitle() + "</h1>";
 		if (bt.getSubtitle() != null && !bt.getSubtitle().isEmpty()) {
 			bodyPart += "<h2>" + bt.getSubtitle() + "</h2>";
 		}
 		bodyPart += "<table border='0'>";
 		bodyPart += "<tr>";
 		bodyPart += "<td width='240'>";
 		if(bt.getCover() != null && bt.getCover().getImageFile() != null){
 			bodyPart += "<img height='230' width='230' src='" + bt.getCover().getImageFile().getAbsolutePath() + "'/>";
 		}
 		bodyPart += "</td>";
 		bodyPart += "<td>";		
 			bodyPart += "<p><b>Rating:</b>" + bt.getRating() + "</p>";
 			bodyPart += "<p><b>ID:</b>" + bt.getPrimaryKey() + "</p>";
 			bodyPart += "<p><b>Pages:</b>" + bt.getPages() + "</p>";
 		bodyPart += "</td>";
 	
 		if (bt.getAuthors() != null && !bt.getAuthors().isEmpty()) {
 			bodyPart += "<td>";
 				bodyPart += "<b>Directors:</b>";
 				bodyPart += "<ul>";
 				for (Author author : bt.getAuthors()) {
 					bodyPart += "<li>" + author.getFirstName() + " " + author.getLastName() + "</li>";
 				}
 				bodyPart += "</ul>";
 			bodyPart += "</td>";
 		}
 		
 		if(bt.getDescription() != null && !bt.getDescription().isEmpty()){
 			bodyPart += "</tr><tr>";
 			bodyPart += "<td width=\"240\">";
 				bodyPart += "<p><b>Description</b></p>";
 				bodyPart += bt.getDescription();
 			bodyPart += "</td>";
 		}
 		
 		if (bt.getIdentifiers() != null && !bt.getIdentifiers().isEmpty()) {
 			bodyPart += "<td>";
 				bodyPart += "<b>Identifiers:</b>";
 				bodyPart += "<ul>";
 					for (Entry<BookIndustryIdentifier, String> e : bt.getIdentifiers().entrySet()) {
 						bodyPart += "<li><i>" + e.getKey().toString() + "</i><p>" + e.getValue() + "</p></li>";
 					}
 				bodyPart += "</ul>";
 			bodyPart += "</td>";
 		}
 
 		if (bt.getGenres() != null && !bt.getGenres().isEmpty()) {
 			bodyPart += "<td>";
 				bodyPart += "<b>Genres:</b>";
 				bodyPart += "<ul>";
 				for (Genre g : bt.getGenres()) {
 					bodyPart += "<li>" + g.getGenre() + "</li>";
 				}
 				bodyPart += "</ul>";
 			bodyPart += "</td>";
 		}else{
 			bodyPart += "</tr>";
 		}
 		bodyPart += "</table>";
 		return bodyPart;
 	}
 
 	private static String generateMovieBody(Movie m) {
 		String bodyPart = new String();
 		bodyPart += "<h1>" + m.getTitle() + "</h1>";
 		if (m.getSubtitle() != null && !m.getSubtitle().isEmpty()) {
 			bodyPart += "<h2>" + m.getSubtitle() + "</h2>";
 		}
 		
 		bodyPart += "<table border='0'>";
 			bodyPart += "<tr>";
 				bodyPart += "<td>";
 				if(m.getCover() != null && m.getCover().getImageFile() != null){;
 					bodyPart += "<img height='230' width='230' src='" + m.getCover().getImageFile().getAbsolutePath() + "'/>";
 				}
 				bodyPart += "</td>";
 				bodyPart += "<td>";		
 					bodyPart += "<p><b>Rating:</b>" + m.getRating() + "</p>";
 					bodyPart += "<p><b>ID:</b>" + m.getPrimaryKey() + "</p>";
 					bodyPart += "<p><b>Lenght:</b>" + m.getDuration() + "</p>";
 				bodyPart += "</td>";
 				
 				if (m.getAuthors() != null && !m.getAuthors().isEmpty()) {
 					bodyPart += "<td>";
 						bodyPart += "<b>Directors:</b>";
 						bodyPart += "<ul>";
 						for (Author author : m.getAuthors()) {
 							bodyPart += "<li>" + author.getFirstName() + " " + author.getLastName() + "</li>";
 						}
 						bodyPart += "</ul>";
 					bodyPart += "</td>";
				}
				
				if(m.getDescription() != null && !m.getDescription().isEmpty()){
					bodyPart += "</tr><tr>";
					bodyPart += "<td width=\"240\">";
						bodyPart += "<p><b>Description</b></p>";
						bodyPart += m.getDescription();
					bodyPart += "</td>";
 				}
 				
 				if (m.getGenres() != null && !m.getGenres().isEmpty()) {
 						bodyPart += "<td>";
 							bodyPart += "<b>Genres:</b>";
 							bodyPart += "<ul>";
 							for (Genre g : m.getGenres()) {
 								bodyPart += "<li>" + g.getGenre() + "</li>";
 							}
 							bodyPart += "</ul>";
 						bodyPart += "</td>";
 				}else{
 					bodyPart += "</tr>";
 				}
 		bodyPart += "</table>";
 		return bodyPart;
 	}
 
 	private static String generateAudioAlbumBody(AudioAlbum a) {
 		String bodyPart = new String();
 		bodyPart += "<h1>" + a.getBand().getName() + "</h1>";
 		
 		bodyPart += "<table border='0'>";
 			bodyPart += "<tr>";
 				bodyPart += "<td>";
 				if(a.getCover() != null && a.getCover().getImageFile() != null){;
 					bodyPart += "<img height='230' width='230' src='" + a.getCover().getImageFile().getAbsolutePath() + "'/>";
 				}
 				bodyPart += "</td>";
 				bodyPart += "<td>";		
 					bodyPart += "<p><b>Rating:</b>" + a.getRating() + "</p>";
 					bodyPart += "<p><b>ID:</b>" + a.getPrimaryKey() + "</p>";
 					bodyPart += "<p><b>Title:</b>" + a.getTitle() + "</p>";
 					if (a.getSubtitle() != null && !a.getSubtitle().isEmpty()) {
 						bodyPart += "<b><i>Subtitle:</i></b>" + a.getSubtitle() + "</p>";
 					}
 					bodyPart += "<p><b>Lenght:</b>" + a.getDuration() + "</p>";
 				bodyPart += "</td>";
 			
 				if (a.getTrackList() != null && !a.getTrackList().isEmpty()) {
 					bodyPart += "<td>";
 						bodyPart += "<b>Tracklist:</b>";
 						bodyPart += "<ul>";
 						for (AudioAlbumTrack t : a.getTrackList()) {
 							bodyPart += "<li>" + t.toString() + "</li>";
 						}
 						bodyPart += "</ul>";
 					bodyPart += "</td>";
 				}
 		
 				if (a.getGenres() != null && !a.getGenres().isEmpty()) {
 					bodyPart += "</tr>";
 					bodyPart += "<tr>";
 						bodyPart += "<td>";
 							bodyPart += "<b>Genres:</b>";
 							bodyPart += "<ul>";
 							for (Genre g : a.getGenres()) {
 								bodyPart += "<li>" + g.getGenre() + "</li>";
 							}
 							bodyPart += "</ul>";
 						bodyPart += "</td>";
 					bodyPart += "</tr>";
 				}else{
 					bodyPart += "</tr>";
 				}
 		bodyPart += "</table>";
 		return bodyPart;
 	}
 
 	/**
 	 * @param fullContent
 	 * @return
 	 */
 	private static URL saveToFileSystem(String fullContent) {
 		DataOutputStream dos = null;
 		String path = GlobalPaths.TMP
 				+ String.valueOf(Math.random() * Double.MAX_EXPONENT);
 		try {
 			dos = new DataOutputStream(new FileOutputStream(new File(path)));
 			dos.writeBytes(fullContent);
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} finally {
 			if (dos != null) {
 				try {
 					dos.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 
 		try {
 			URL ulr = new URL("file:///" + path);
 			return ulr;
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 }
