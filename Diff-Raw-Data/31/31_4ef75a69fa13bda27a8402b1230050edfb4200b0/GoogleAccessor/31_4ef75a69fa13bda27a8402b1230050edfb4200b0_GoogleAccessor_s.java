 package ucmsite.pagegeneration;
 
 import java.io.ByteArrayInputStream;
 import java.io.InputStream;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.apache.poi.hwpf.extractor.WordExtractor;
 
 import ucmsite.util.AppEngineLogger;
 
 import com.google.gdata.client.GoogleService;
 import com.google.gdata.client.calendar.CalendarService;
 import com.google.gdata.client.docs.DocsService;
 import com.google.gdata.client.media.MediaService;
 import com.google.gdata.client.photos.PicasawebService;
 import com.google.gdata.client.spreadsheet.SpreadsheetService;
 import com.google.gdata.data.Link;
 import com.google.gdata.data.MediaContent;
 import com.google.gdata.data.docs.DocumentListEntry;
 import com.google.gdata.data.docs.DocumentListFeed;
 import com.google.gdata.data.media.MediaSource;
 import com.google.gdata.data.photos.AlbumEntry;
 import com.google.gdata.data.photos.AlbumFeed;
 import com.google.gdata.data.photos.PhotoEntry;
 import com.google.gdata.data.photos.UserFeed;
 import com.google.gdata.data.spreadsheet.CellEntry;
 import com.google.gdata.data.spreadsheet.CellFeed;
 import com.google.gdata.data.spreadsheet.SpreadsheetEntry;
 import com.google.gdata.data.spreadsheet.SpreadsheetFeed;
 import com.google.gdata.data.spreadsheet.WorksheetEntry;
 import com.google.gdata.util.AuthenticationException;
 import com.google.gdata.util.ContentType;
 
 public class GoogleAccessor {
 
 	private static GoogleAccessor self = null;
 	private SpreadsheetService sheetService;
 	private CalendarService calendarService;
 	private GoogleService docService;
 	private PicasawebService picService;
 	private String username;
 	private HashMap<String, UserFeed> albumMap;
 	private HashMap<String, AlbumFeed> picMap;
 	
 	private GoogleAccessor(String password, String username) {
 		try {
 			sheetService = new SpreadsheetService("ucmubc");
 			sheetService.setUserCredentials(username, password);
 			
 			calendarService = new CalendarService("ucmubc");
 			calendarService.setUserCredentials(username, password);
 			
 			docService = new DocsService("ucmubc");
 			docService.setUserCredentials(username, password);
 			
 			picService = new PicasawebService("ucmubc");
 			picService.setUserCredentials(username, password);
 			this.username = username.split("@")[0];
 			
 			albumMap = new HashMap<String, UserFeed>();
 			picMap = new HashMap<String, AlbumFeed>();
 			
 		} catch (AuthenticationException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public static GoogleAccessor getGoogleAccessor(String password, String username) {
 		if (self == null) {
 			self = new GoogleAccessor(password, username);
 		}
 		
 		return self;
 	}
 	
 	public static GoogleAccessor getGoogleAccessor() {
 		return self;
 	}
 	
 	public List<DocumentListEntry> getFolderDocuments(List<String> folders) {
 		while(true) {
 			try {
 //				URL docURI = new URL("http://docs.google.com/feeds/documents/private/full/");
 				URL docURI = new URL("https://docs.google.com/feeds/documents/private/full/");
 				DocumentListFeed docFeed = docService.getFeed(docURI, DocumentListFeed.class);
 				
 				List<DocumentListEntry> docEntries = docFeed.getEntries();
 				
 				List<DocumentListEntry> removeEntries = new LinkedList<DocumentListEntry>();
 				valid:
 				for(DocumentListEntry entry : docEntries) {
 					for(Link folder : entry.getParentLinks()) {
 						if (folders.contains(folder.getTitle().trim())) {
 							continue valid;
 						}
 					}
 					removeEntries.add(entry);
 				}
 				for(DocumentListEntry entry : removeEntries) {
 					docEntries.remove(entry);
 				}
 				
 				return docEntries;
 			} catch (java.io.IOException e) {
 			}
 			catch (Exception e) {
 				return null;
 			}
 		}
 	}
 	
 	public InputStream getXmlDocument(DocumentListEntry document) {
 		while(true) {
 			try {				
 				String docResId = document.getResourceId();
 				String docId = docResId.substring(docResId.lastIndexOf(":") + 1);
 				String type = "doc";
 				String exportUrl = "https://docs.google.com/feeds/download/documents/Export?docId=" +
 				docId + "&exportFormat=" + type;
 	
 				MediaContent mc = new MediaContent();
 				mc.setUri(exportUrl);
 				mc.setMimeType(ContentType.TEXT_XML);
 				
 				docService.setConnectTimeout(0);
 				MediaSource ms = ((MediaService) docService).getMedia(mc);
 				InputStream inStream = null;
 				inStream = ms.getInputStream();
 		
 				WordExtractor extractor = new WordExtractor(inStream);
 				
 				String extractStr = extractor.getText().replaceAll("[^\\p{Print}]", "");
 				InputStream returnStream = new ByteArrayInputStream(extractStr.getBytes());
 				
 				return returnStream;
 			} catch (java.io.IOException e) {
 			}
 			catch (Exception e) {
 				return null;
 			}
 		}
 	}
 	
 	public InputStream getDocument(String foldername, String title) {
 		while(true) {
 			try {
 				URL docURI = new URL("https://docs.google.com/feeds/documents/private/full/?category=" + foldername);
 				DocumentListFeed docFeed = docService.getFeed(docURI, DocumentListFeed.class);
 				
 				List<DocumentListEntry> docEntries = docFeed.getEntries();
 				
 				DocumentListEntry docEntry = null;
 				found:
 				for (int i = 0; i < docEntries.size(); i++) {
 					docEntry = docEntries.get(i);
 					String test = docEntry.getUpdated().toString();
 					if (docEntry.getTitle().getPlainText().trim().equals(title)) {
 						break found;
 					}
 				}
 				
 				String docResId = docEntry.getResourceId();
 				String docId = docResId.substring(docResId.lastIndexOf(":") + 1);
 				String type = "doc";
 				String exportUrl = "https://docs.google.com/feeds/download/documents/Export?docId=" +
 				docId + "&exportFormat=" + type;
 	
 				MediaContent mc = new MediaContent();
 				mc.setUri(exportUrl);
 				mc.setMimeType(ContentType.TEXT_XML);
 				
 				docService.setConnectTimeout(0);
 				MediaSource ms = ((MediaService) docService).getMedia(mc);
 				InputStream inStream = null;
 				inStream = ms.getInputStream();
 		
				WordExtractor extractor = new WordExtractor(inStream);
 				String extractStr = extractor.getText();
 				extractStr = extractStr.replaceAll("[“”“”““”“”“”“”“”]", "\"");
 				extractStr = extractStr.replaceAll("[’’]", "\'");
 				
 				extractStr = extractStr.replaceAll("[^\\p{Print}]", "");
 				InputStream returnStream = new ByteArrayInputStream(extractStr.getBytes());
 				
 				return returnStream;
 			} catch (java.io.IOException e) {
 			}
 			catch (Exception e) {
 				return null;
 			}
 		}
 	}
 	
 	public void resetPhotoMaps() {
 		albumMap.clear();
 		picMap.clear();
 	}
 	
 	public String getPicasaURL(String picasaSrc) {
 		try {
 			String album = picasaSrc.split("/")[0];
 			String title = picasaSrc.split("/")[1];
 			String albumStr = "http://picasaweb.google.com/data/feed/api/user/" + username + "/?kind=album";
 			URL albumUrl = new URL(albumStr);
 			
 			UserFeed afeed = albumMap.get(albumStr);
 			if (afeed == null) {
 				afeed = picService.getFeed(albumUrl, UserFeed.class);
 				albumMap.put(albumStr, afeed);
 			}
 			String albumid = null;
 			for(AlbumEntry alb : afeed.getAlbumEntries()) {
 				if (alb.getTitle().getPlainText().trim().equals(album)) {
 					int slashpos = alb.getId().lastIndexOf("/");
 					albumid = alb.getId().substring(slashpos + 1);
 					break;
 				}
 			}
 			
 			String picStr = "http://picasaweb.google.com/data/feed/api/user/" + username + "/albumid/" + albumid;
 			AppEngineLogger.log("ALBUMID: " + albumid);
 			URL picURL = new URL(picStr);
 			AlbumFeed pfeed = picMap.get(picStr);
 			if (pfeed == null) {
 				pfeed = picService.getFeed(picURL, AlbumFeed.class);
 				picMap.put(picStr, pfeed);
 			}
 			for(PhotoEntry photo : pfeed.getPhotoEntries()) {
 				String photoUrl = null;
 				if (photo.getTitle().getPlainText().trim().equals(title)) {
 					/* Temporary fix to get images sized correctly */
 					String photoUrlTest = photo.getMediaThumbnails().get(0).getUrl();
 					long width = photo.getWidth();
 					photoUrlTest = photoUrlTest.replaceFirst("/s[0-9]+/", "/s" + width + "/");
 					if (photoUrl == null)
 						photoUrl = photoUrlTest;
 				}
 				if (photoUrl != null)
 					return photoUrl;
 			}
 			
 			return null;
 		} catch (Exception e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	public String getFormAddress(String title) {
 		try {
 			URL sheetURL = new URL("https://spreadsheets.google.com/feeds/spreadsheets/private/full");
 			
 			SpreadsheetFeed tableFeed = sheetService.getFeed(sheetURL, SpreadsheetFeed.class);
 			
 			List<SpreadsheetEntry> tables = tableFeed.getEntries();
 			
 			SpreadsheetEntry entry = null;
 			for (int i = 0; i < tables.size(); i++) {
 			  entry = tables.get(i);
 			  if (entry.getTitle().getPlainText().trim().equals(title)) 
 				  break;			  
 			}
 			
 			return entry.getKey();
 		} catch (Exception e) {
 			return null;
 		}
 	}
 	
 	public Iterator<SpreadSheet> getSpreadSheet(String title, String tabtitle) {
 		try {
 			URL sheetURL = new URL("https://spreadsheets.google.com/feeds/spreadsheets/private/full");
 			
 			SpreadsheetFeed tableFeed = sheetService.getFeed(sheetURL, SpreadsheetFeed.class);
 			
 			List<SpreadsheetEntry> tables = tableFeed.getEntries();
 			
 			SpreadsheetEntry entry = null;
 			for (int i = 0; i < tables.size(); i++) {
 			  entry = tables.get(i);
 			  if (entry.getTitle().getPlainText().trim().equals(title)) 
 				  break;			  
 			}
 			
 			Iterator workSheets = entry.getWorksheets().iterator();
 			List<SpreadSheet> returnSheets = new LinkedList<SpreadSheet>();
 			while (workSheets.hasNext()) {
 				WorksheetEntry worksheetEntry = (WorksheetEntry) workSheets.next();
 				String workSheetName = worksheetEntry.getTitle().getPlainText().trim();
 				
 				if (tabtitle != null && !workSheetName.equals(tabtitle))
 					continue;
 					
 				URL cellFeedUrl = worksheetEntry.getCellFeedUrl();
 				CellFeed cellfeed = sheetService.getFeed(cellFeedUrl, CellFeed.class);
 				
 				LinkedList<Iterator<String>> rows = new LinkedList<Iterator<String>>();
 				
 				int lastRow = 0;
 				LinkedList<String> cols = new LinkedList<String>();
 				for (CellEntry cell : cellfeed.getEntries()) {
 					if (cell.getCell().getRow() != lastRow && lastRow != 0) {
 						rows.add(cols.iterator());
 						cols = new LinkedList<String>();
 						cols.add(cell.getCell().getValue());
 					}
 					else {
 						cols.add(cell.getCell().getValue());
 					}
 					
 					lastRow = cell.getCell().getRow();
 				}
 				rows.add(cols.iterator());
 				returnSheets.add(new SpreadSheet(workSheetName, rows.iterator()));
 			}
 			
 			return returnSheets.iterator();
 		} catch (Exception e) {
 			return null;
 		}
 	}
 	
 	public class SpreadSheet {
 		String name = null;
 		Iterator sheet = null;
 		public SpreadSheet(String sname, Iterator ssheet) {
 			name = sname;
 			sheet = ssheet;
 		}
 		
 		public String getName() {
 			return name;
 		}
 		public Iterator<Iterator<String>> getSheet() {
 			return sheet;
 		}
 	}
 	
 }
