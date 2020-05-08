 package awesome;
 
 import java.awt.image.BufferedImage;
 import java.io.BufferedInputStream;
 import java.io.ByteArrayInputStream;
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.awt.image.DataBufferByte;
 
 import javax.imageio.ImageIO;
 
 public class MP3File implements FilePathInfo {
 	
 	File file;
 	private String title = null;
 	private String artist = null;
 	private String album = null;
 	private String year = null;
 	private boolean dirty = false; //indicates whether an attribute has changed
 	private int tagSize = -1;
 	private int headerFlags = 0;
 	private String coverMime = "";
 	private byte[] cover = new byte[]{0}; //not null to indicate that cover hasn't been parsed yet
 	
 	private List<ID3Frame> unknownFrames;
 	
 	private static final byte[] ID3_HEADER_START = {0x49,0x44,0x33,0x03,0x00};
 	
 	public MP3File(File file){
 		this.file = file; 
 		this.unknownFrames = new ArrayList<ID3Frame>();
 	}
 	
 	public void tryToParse() {
 		try {
 			parse();//TODO: Better Parse Info Policy?
 		} catch (IOException e) {
 			AwesomeID3.getController().getView().presentException(e);
 		} 
 	}
 	
 	@Override
 	public File getFile() {
 		return file;
 	}
 
 	@Override
 	public boolean isDirectory() {
 		return false;
 	}
 
 	@Override
 	public String toString() {
 		return file.getName();
 	}
 
 	@Override
 	public List<FilePathInfo> listFiles() {
 		return null;
 	}
 
 	public static boolean isMP3(File f) {
 		if(!f.getName().endsWith(".mp3") || !f.exists()){
 			return false; //we only respect files with mp3 suffix as we don't want read every file.
 		}
 		FileInputStream fis = null;
 		byte[] isRightID3 = new byte[5];
 		try {
 			fis = new FileInputStream(f);			
 			fis.read(isRightID3);
 			fis.close();
 		} catch ( IOException e) {
 			AwesomeID3.getController().getView().presentException(e);
 		}
 		byte[] rightBytes = ID3_HEADER_START;
 		return Arrays.equals(isRightID3,rightBytes);
 	}
 	
 	
 	//TODO: Move method to another class as it is not related to MP3File itself
 	public static boolean containsMP3s(File rootFile) {
 		if(rootFile.isFile()) {
 			return MP3File.isMP3(rootFile);
 		} else if(rootFile.isDirectory()){
 			File[] subFiles = rootFile.listFiles();
 			for(File f:subFiles) {
 				if(MP3File.containsMP3s(f)) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 	
 	
 	private void parse() throws IOException {
 		ID3InputStream input = new ID3InputStream(new FileInputStream(file));
 		
 		byte[] hstart = new byte[5];
 		input.read(hstart);
 		if(!Arrays.equals(hstart, ID3_HEADER_START)){
 			System.err.println("Unable to find ID3v2.3 Header!");
 			return; //TODO: maybe we should throw an exception here as MP3File already checked for header.
 		}
 		
 		headerFlags = input.readUnsignedByte();
 		//TODO: Don't touch files which have flags set
 		tagSize = input.readSyncSafeSize();
 		//Extended Header
 		byte[] tag = new byte[tagSize];
 		input.read(tag);
 		input.close();
 		input = new ID3InputStream(new ByteArrayInputStream(tag));
 		while(input.available() > 10){
 			parseFrame(input);
 			input.readPadding();
 		}
 		input.close();
 	}
 
 	private void parseFrame(ID3InputStream input) throws IOException {
 		String frameType = input.readStringOfLength(4);
 		//System.out.println("Frame Type: " + frameType);
 		int fsize = input.readInt();
 		//System.out.println("Frame Size: " + fsize);
 		int flags = input.readUnsignedShort();
 		switch (frameType) { //the frame type indicates which info we read
 			case "TALB" : this.album = input.readStringOfLengthWithCharsetByte(fsize); break;
 			case "TIT2" : this.title = input.readStringOfLengthWithCharsetByte(fsize); break;
 			case "TPE1" : this.artist = input.readStringOfLengthWithCharsetByte(fsize); break; //check whether TPE2 is correct
 			case "TYER" : this.year = input.readStringOfLengthWithCharsetByte(fsize); break;
 			case "APIC" : parsePic(input, fsize); break;
 			default: byte[] buff2  = new byte[fsize]; input.read(buff2); 
 			unknownFrames.add(new ID3Frame(frameType, fsize, flags, buff2)); //we need to restore the frame later
 			break;
 		}
 	}
 	
 	private void parsePic(ID3InputStream input, int fsize) throws FileNotFoundException, IOException {
 		Charset cs = input.readCharset(); //1. charset byte
 		coverMime = input.readStringUntilZero(cs); //2. mime string (zero-terminated)
 		int picType = input.readUnsignedByte(); // 3. byte indicating kind of picture, e.g. front cover
 		if(picType != 0x03) //we want only front covers
 			return;
 		String desc = input.readStringUntilZero(cs); //read image description
 		byte buff[] = new byte[fsize-1-coverMime.length()-1-1-desc.length()-1]; //image is rest of the data
 		input.read(buff);
 		input.readPadding(); //image can be followed by zero bytes used to make modifications easier
 		cover = buff;
 	}
 	
 	public void save() throws IOException{
 		if(!dirty) return; //if nothing changed, we don't need to save anything
 		
 		// read music data
 		byte musicData[] = readMusicData(tagSize);
 		// delete the file so that it can recreated
 		file.delete();
 		//we always use ISO-8859-1 as it is the ID3 standard encoding
 		Charset cs = Charset.forName("ISO-8859-1");
 		
 		//convert strings to byte arrays so that the length can be determined
 		byte[] titleData = title.getBytes(cs);
 		byte[] albumData = album.getBytes(cs);
 		byte[] artistData = artist.getBytes(cs);
 		byte[] yearData = year.getBytes(cs);
 		
 		// compute size of ID3-Tag excluding the 10 header bytes
 		int newTagSize = 0;
 		for(ID3Frame frame : unknownFrames){
 			newTagSize += frame.getSize() + 10;
 		}
 		newTagSize += 40 + titleData.length + albumData.length + artistData.length + yearData.length;
 		if(cover != null && !cover.equals(new byte[]{0}))
 			newTagSize += 10 + 3 + cover.length + coverMime.getBytes(cs).length;
 		
 		// create outputstream
 		ID3OutputStream dos = new ID3OutputStream(new FileOutputStream(file));
 		//write magic bytes and size of tag
 		dos.write(ID3_HEADER_START); //write Header start
 		dos.writeByte(headerFlags);
 		dos.writeAsSynchSafe(newTagSize);
 		
 		// write the four elemental text frames and the cover
 		dos.writeTextFrame("TPE1", artistData);
 		dos.writeTextFrame("TALB", albumData);
 		dos.writeTextFrame("TIT2", titleData);
 		dos.writeTextFrame("TYER", yearData);
 		if(cover != null)
 			dos.writeCover(cover, coverMime);
 		
 		// write all the unkown frames not modified by our software
 		for(ID3Frame frame : unknownFrames){
 			dos.writeFrame(frame);
 		}
 		
 		//tag ist finished, we can write the music data
 		dos.write(musicData);
 		// and close
 		dos.close();
 		dirty = false;
 	}
 
 	private byte[] readMusicData(int tagSize) throws FileNotFoundException,
 			IOException {
 		byte musicData[] = new byte[(int)file.length()-(tagSize+10)];
 		FileInputStream fis = new FileInputStream(file);
 		fis.skip(tagSize+10);
 		fis.read(musicData);
 		fis.close();
 		return musicData;
 	}
 
 	/**
 	 * @return the title
 	 */
 	public String getTitle() {
 		if(title == null) {
 			tryToParse();
 		}
 		return title;
 	}
 
 	/**
 	 * @param title the title to set
 	 */
 	public void setTitle(String title) {
 		dirty |= !(title.equals(this.title));
 		this.title = title;
 	}
 
 	/**
 	 * @return the artist
 	 */
 	public String getArtist() {
 		if(artist == null) {
 			tryToParse();
 		}
 		return artist;
 	}
 
 	/**
 	 * @param artist the artist to set
 	 */
 	public void setArtist(String artist) {
 		dirty |= !(artist.equals(this.artist));
 		this.artist = artist;
 	}
 
 	/**
 	 * @return the album
 	 */
 	public String getAlbum() {
 		if(album == null) {
 			tryToParse();
 		}
 		return album;
 	}
 
 	/**
 	 * @param album the album to set
 	 */
 	public void setAlbum(String album) {
 		dirty |= !(album.equals(this.album));
 		this.album = album;
 	}
 
 	/**
 	 * @return the year
 	 */
 	public String getYear() {
 		if(year == null) {
 			tryToParse();
 		}
 		return year;
 	}
 
 	/**
 	 * @param year the year to set
 	 */
 	public void setYear(String year) {
 		dirty |= !(year.equals(this.year));
 		this.year = year;
 	}
 
 	/**
 	 * @return the cover
 	 */
 	public byte[] getCover() {
 		if(cover != null && cover.equals(new byte[]{0})) {
 			tryToParse();
 		}
 		return cover;
 	}
 
 	/**
 	 * @param cover the cover to set
 	 */
 	public void deleteCover() {
 		dirty |= (cover == null);
 		this.cover = null;	
 		this.coverMime = "";
 	}
 
 	public void readCoverFromFile(File file) {
 		//TODO: Add conversion for other image formats like BMP, TIFF, etc
 		try {
 			byte[] buff;
 			if(!(new ImageFileFilter().accept(file))) {
 				BufferedImage in = ImageIO.read(file);
 				File tempFile = new File(System.getProperty("java.io.tmpdir")+"temp.png");
 				ImageIO.write(in, "png", tempFile);
 				buff = new byte[(int) tempFile.length()];
				FileInputStream fis = FileInputStream(tempFile);
				fis.read(buff);
				fis.close();
 				coverMime = "image/png";
 			} else {
 				InputStream in = new BufferedInputStream(new FileInputStream(file));
 				buff = new byte[(int) file.length()];
 				in.read(buff);
				in.close(); 
 				coverMime = file.getName().endsWith(".png") ? "image/png" : "image/jpeg";
 			}
 			cover = buff;
 			dirty = true;
 		} catch (IOException e) {
 			AwesomeID3.getController().getView().presentException(e);
 		}
 	}

	private FileInputStream FileInputStream(File tempFile) {
		// TODO Auto-generated method stub
		return null;
	}
 	
 }
