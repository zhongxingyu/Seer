 package editor;
 
 import java.awt.image.BufferedImage;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import javax.imageio.ImageIO;
 import org.blinkenlights.jid3.ID3Exception;
 import org.blinkenlights.jid3.ID3Tag;
 import org.blinkenlights.jid3.MP3File;
 import org.blinkenlights.jid3.MediaFile;
 import org.blinkenlights.jid3.v2.APICID3V2Frame;
 import org.blinkenlights.jid3.v2.ID3V2_3_0Tag;
 import org.blinkenlights.jid3.v2.TPE2TextInformationID3V2Frame;
 import org.farng.mp3.TagConstant;
 import org.farng.mp3.TagOptionSingleton;
 
 public class CDFormatter {
 	
 	private MainMenu menu;
 	
 	private final String comment;
 	private String genre;
 	// these will be parsed from dir name
 	private String artist, albumTitle;
 	private int year, trackNumber;
 	private String title, cdPath;
 	private boolean coverFound, ignoreImage;
 	
 	private File albumDirectory;
 	private File[] directoryFile;
 	private ArrayList<File> mp3FileList;
 	
 	public CDFormatter(String path, String genr)
 	{
 		genre = genr;
 		comment = "";
 		// these will be parsed from dir name
 		artist = "Unkown";
 		albumTitle = "Unknown";
 		year = 0;
 		// these will be parsed from individual song name
 		trackNumber = 1;
 		title = "Unknown";
 		
 		cdPath = path;
 		coverFound = false;
 		ignoreImage = false;
 		
 		// save options
 		TagOptionSingleton.getInstance().setDefaultSaveMode(TagConstant.MP3_FILE_SAVE_OVERWRITE);
 		TagOptionSingleton.getInstance().setFilenameTagSave(true);
 		
 		albumDirectory = new File(cdPath);
 		directoryFile = albumDirectory.listFiles();
 		mp3FileList = new ArrayList<File>();
 		populateMP3list();
 	}
 	
 	public int getMP3Count(){
 		return mp3FileList.size();
 	}
 	public int getFileCount(){
 		return directoryFile.length;
 	}
 	public String getCurrentDirectory(){
 		return albumDirectory.getName();
 	}
 	public void setMenu(MainMenu menu){
 		this.menu = menu;
 	}
 	
 	private void populateMP3list()
 	{
 		mp3FileList.clear();
 		for(int i = 0; i < directoryFile.length; i++)
 		{
 			Filename splitPath = new Filename(directoryFile[i].toString(), "\\", '.');
 			
 			//delete hidden .ini files
 			if(splitPath.extension().equals("ini"))
 			{
 				System.out.println("Deleting unwanted file " + directoryFile[i].getName());
 				directoryFile[i].delete();
 				continue;
 			}
 			
 			// delete generated hidden picture files
 			if(splitPath.extension().toLowerCase().equals("jpg"))
 			{
 				String fn = splitPath.filename().toLowerCase();
 				if(fn.contains("small") || fn.contains("large") || fn.contains("folder") || fn.contains("albumart") )
 				{
 					System.out.println("Deleting unwanted file " + directoryFile[i].getName());
 					directoryFile[i].delete();
 					continue;
 				}
 			}
 			
 			// track only mp3 files
 			if(splitPath.extension().equals("mp3"))
 				mp3FileList.add(directoryFile[i]);
 		}
 		//some files may have been deleted, update directory file
 		directoryFile = albumDirectory.listFiles();
 	}
 	
 	public void format(){
 		//printFilesInDirectory();
 		boolean dataExtracted = pullDataFromFilePath();
 		if(!dataExtracted)
 		{
			System.out.println("\nERROR: Invalid Format. \"" + albumDirectory.getName() + "\" is not a valid directory name. " +
								"Refer to documentation for proper name formatting. Skipping this directory.\n");
 			return;
 		}
 		modifySongMetaData();
 	}
 	
 	private boolean pullDataFromFilePath()
 	{
 		String albumDirectoryName = albumDirectory.getName();
 
 		artist = getArtistFromDirectory(albumDirectoryName);
 		albumTitle = getAlbumFromDirectory(albumDirectoryName);
 		String tempyear = getYearFromDirectory(albumDirectoryName);
 		
 		if(artist == null || albumTitle == null || tempyear == null)
 			return false;
 		
 		try{
 			year = Integer.parseInt(tempyear);
 		}catch(NumberFormatException e){
 			return false;
 		}
 		return true;
 	}
 	
 	private static String getArtistFromDirectory(String dir) 
 	{
 		try{
 			int dash = dir.indexOf("-");
 			if(dir.substring(0, dash - 1).trim().isEmpty())
 				return null;
 			return dir.substring(0, dash - 1).trim();
 		}catch(StringIndexOutOfBoundsException e){
 			return null;
 		}
 	}
 
 	private String getAlbumFromDirectory(String dir) {
 		try{
 			int dash = dir.indexOf("-");
 			int parenth = dir.lastIndexOf("(");
 			if(dir.substring(dash + 1, parenth - 1).trim().isEmpty()) {return null;}
 			return dir.substring(dash + 1, parenth - 1).trim();
 		}catch(StringIndexOutOfBoundsException e){
 			return null;
 		}
 	}
 
 	private String getYearFromDirectory(String dir) {
 		try{
 			int parenth = dir.lastIndexOf("(");
 			String yearString = dir.substring(parenth + 1, dir.length() - 1).trim();
 			if(yearString.isEmpty()) {return null;}
 			return yearString;
 		}catch(StringIndexOutOfBoundsException e){
 			return null;
 		}
 	}
 
 	private String formatSongTitle(String name) {
 		name = name.replace('_', ' ');  //replace all underscores with a space
 		name = name.replace('-', ' ');  //replace all dashes with a space
 		name = name.trim();   // trim white space
 		name = name.replaceAll("\\s+", " ");  // reduce number of spaces to one
 		name = capitalizeString(name);  // Make first characters upper case
 		// make sure first characters aren't numbers
 		return name;
 	}
 
 	private String capitalizeString(String string) {
 		char[] chars = string.toLowerCase().toCharArray();
 		boolean found = false;
 		
 		for (int i = 0; i < chars.length; i++) {
 			if (!found && Character.isLetter(chars[i])) {	
 				chars[i] = Character.toUpperCase(chars[i]);
 				found = true;
 			}else if (Character.isWhitespace(chars[i]) || chars[i] == '.'|| chars[i] == '-') { 
 				// You can add other chars here
 				found = false;
 			}
 		}
 		return String.valueOf(chars);
 	}
 	
 	boolean embedCover(){
 		//check if cover is already there
 		for (int k = 0; k < directoryFile.length; k++)
 		{
 			Filename splitPath = new Filename(directoryFile[k].toString(), "\\", '.');
 			if(splitPath.extension().toString().equals("jpg") && splitPath.filename().toString().equals("cover"))
 			{
 				coverFound = true;
 				System.out.println("Image found at file number " + k + ". No extraction necessary.");
 				return true;
 			}
 		}
 		
 		//if no cover was found, extract it from first song
 		if(!coverFound)
 		{
 			if(extractAlbumArtwork(mp3FileList.get(0).toString(),cdPath))
 			{
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	public boolean extractAlbumArtwork(String songPath, String albumPath)
 	{
 		
 		File src = new File(songPath);
 		MediaFile songFile = new MP3File(src);
 
 		ID3Tag[] aoID3Tag;
 		try {
 			aoID3Tag = songFile.getTags();
 			for(int p = 0; p < aoID3Tag.length; p++)
 			{
 				if(aoID3Tag[p] instanceof ID3V2_3_0Tag)
 				{
 					APICID3V2Frame[] frames = ((ID3V2_3_0Tag) aoID3Tag[p]).getAPICFrames();
 					
 					if(frames.length == 0 || frames.equals(null))
 					{
 						return false;
 					}
 					
 					BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(frames[0].getPictureData()));
 			        ImageIO.write(bufferedImage, "jpg", new File(albumPath + "\\cover.jpg"));
 			        bufferedImage.flush();
 			        
 			        System.out.println("Image Saved To Directory.");
 					break;
 				}
 			}
 		} catch (ID3Exception e) {
 			return false;
 		}catch (IOException e) {
 			return false;
 		}
 		
 		return true;
 	}
 	
 	public void modifySongMetaData()
 	{
 		MediaFile[] songFile = new MediaFile[mp3FileList.size()];
 		
 		// loop through each song file and manipulate metadata
 		System.out.println();
 		for (int i = 0; i < mp3FileList.size(); i++) 
 		{
 			// create an MP3File object representing our chosen file
 			songFile[i] = new MP3File(mp3FileList.get(i));
 			
 			//parse the full path of the song
 			Filename splitPath = new Filename(mp3FileList.get(i).toString(), "\\", '.');
 			//printFilenameData(splitPath);
 			System.out.println("Processing >>> " + artist + " : " + splitPath.filename() + "." + splitPath.extension());
 
 			//get track number from filename
 			trackNumber = getTrackNumFromFilename(splitPath.filename());
 			if(trackNumber < 1)
 			{
				System.out.println("\n***ERROR: Invalid Format. \"" + splitPath.filename() + "." + splitPath.extension() + 
 						"\" is not a valid format. Track number could not be found.\n" +
						"***Refer to documentation for proper song format. Skipping this song.\n"); 
 				continue;
 			}
 			
 			// get title from filename
 			title = getSongTitleFromFilename(splitPath.filename());
 			if(title == null)
 			{
				System.out.println("\n***ERROR: Invalid Format. \"" + splitPath.filename() + "." + splitPath.extension() + 
 						"\" is not a valid format. Song name could not be found.\n" +
						"***Refer to documentation for proper song format. Skipping this song.\n"); 
 				continue;
 			}
 			
 			//!!!!!!!!!!!!!!!!check here that all metadata is valid
 			ID3V2_3_0Tag sTag = writeMetaData();
 			
 			if(!ignoreImage)
 			{
 				try {
 					// PICTURE data
 					File fi = new File(splitPath.path() + "\\cover.jpg");
 					byte[] imageInByte;
 					BufferedImage originalImage = ImageIO.read(fi);
 					// convert BufferedImage to byte array
 					ByteArrayOutputStream baos = new ByteArrayOutputStream();
 					ImageIO.write(originalImage, "jpg", baos);
 					baos.flush();
 					imageInByte = baos.toByteArray();
 					baos.close();
 	
 					APICID3V2Frame oAPIC1 = new APICID3V2Frame("image/jpeg", APICID3V2Frame.PictureType.FrontCover, "cover", imageInByte);
 					sTag.addAPICFrame(oAPIC1);
 				} catch (IOException e) {
 					e.printStackTrace();
 				} catch (ID3Exception e) {
 					e.printStackTrace();
 				}
 			}
 			
 			
 			try {
 				// set this v2.3.0 tag in the media file object
 				songFile[i].setID3Tag(sTag);
 				// update the actual file to reflect the current state of our object
 				songFile[i].sync();
 			} catch (ID3Exception e) {
 				e.printStackTrace();
 			}
 			
 			//rename song in same path
 			renameFile(splitPath, mp3FileList.get(i));
 		}
 		System.out.println("Completed \"" + albumDirectory.getName() + "\"\n");
 		menu.addTextToLog("Completed \"" + albumDirectory.getName() + "\"\n");
 	}
 	
 	private String getSongTitleFromFilename(String filename) {
 		try{
 			return formatSongTitle(filename.substring(3));
 		}catch(StringIndexOutOfBoundsException e){
 			return null;
 		}
 	}
 
 	private int getTrackNumFromFilename(String fileName) {
 		if (fileName.charAt(0) == '0') {
 			return fileName.charAt(1) - 48;
 		} else {
 			try{
 				String tracknum = fileName.substring(0, 2);
 				return Integer.parseInt(tracknum);
 			}catch(NumberFormatException e){
 				return -1;
 			}catch(StringIndexOutOfBoundsException e){
 				return -1;
 			}
 		}
 	}
 
 	private synchronized void renameFile(Filename splitPath, File songfile)
 	{
 		// rename the file path AFTER SYNC
 		String newFilename;
 		if (trackNumber < 10)	
 			newFilename = "0" + trackNumber + " " + title;
 		else	
 			newFilename = trackNumber + " " + title;
 		File newFile = new File(splitPath.path() + "\\" + newFilename + "." + splitPath.extension());
 		
 		//check that file was renamed properly
 		boolean copy = songfile.renameTo(newFile);
 		if (!copy) {
 			System.out.println("File " + songfile + " was not properly renamed.");
 		}
 	}
 	
 	public void printFilenameData(Filename splitPath) {
 		System.out.print("Path: " + splitPath.path());
 		System.out.print("\tFilename: " + splitPath.filename());
 		System.out.println("\tExtension: " + splitPath.extension());
 	}
 
 	private ID3V2_3_0Tag writeMetaData()
 	{
 		// create a v2.3.0 tag object, set values using convenience methods
 		ID3V2_3_0Tag songTag = new ID3V2_3_0Tag();
 		try{
 			songTag.setAlbum(albumTitle); // sets TALB frame
 			songTag.setArtist(artist); // sets TPE1 frame
 			songTag.setTrackNumber(trackNumber); // sets TRCK frame
 			songTag.setComment(comment); // sets COMM frame, language "eng", no description
 			songTag.setGenre(genre); // sets TCON frame
 			songTag.setTitle(title); // sets TIT2 frame
 			songTag.setYear(year); // sets TYER frame
 	
 			// Set Album Artist frame = artist
 			TPE2TextInformationID3V2Frame oTCOM = new TPE2TextInformationID3V2Frame(songTag.getArtist());
 			songTag.setTPE2TextInformationFrame(oTCOM);
 		} catch (ID3Exception e) {
 			e.printStackTrace();
 		} 
 		return songTag;
 	}
 	
 	public void printFilesInDirectory()
 	{
 		// print out all files in the directory
 		System.out.println("These are all the files in your directory: length(" + directoryFile.length + ")");
 		for (int i = 0; i < directoryFile.length; i++) 
 		{
 			// weeds out other directories/folders
 			if (directoryFile[i].isFile()) 
 				System.out.println(directoryFile[i].toString());
 		}
 		System.out.println();
 	}
 
 	// 
 	public boolean saveDefaultImage()
 	{
 		try {
 			File sourceFile = new File("res/default.jpg");
 			if(!sourceFile.exists() || !sourceFile.canRead()){
 				return false;
 			}
 			InputStream is = new FileInputStream(sourceFile.getAbsolutePath());
 			File targetFile = new File(albumDirectory.getAbsolutePath() + "\\cover.jpg");
 			OutputStream os = new FileOutputStream(targetFile);
 			
 			byte[] buffer = new byte[4096];
 			int length;
 			while ((length = is.read(buffer)) > 0){
 				os.write(buffer, 0, length);
 			}
 
 			os.close();
 			is.close();
 			
 			System.out.println("WARNING: Copied Default Image To Directory.");
 			return true;
 		} catch (Exception ex) {
 			return false;
 		}
 	}
 
 	public void ignoreImage() 
 	{
 		ignoreImage = true;
 	}
 }
