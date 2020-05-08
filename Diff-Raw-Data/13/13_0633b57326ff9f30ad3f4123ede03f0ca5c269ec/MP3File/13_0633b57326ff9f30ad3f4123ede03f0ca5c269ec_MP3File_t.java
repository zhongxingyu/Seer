 package awesome;
 
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
 	private byte[] cover = null;
 	
 	private List<ID3Frame> unknownFrames;
 	
 	public MP3File(File file){
 		this.file = file; 
 		this.unknownFrames = new ArrayList<ID3Frame>();
 	}
 	
 	public void tryToParse() {
 		try {
 			parse();//TODO: Better Parse Info Policy?
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
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
		} catch ( IOException e1) {
			e1.printStackTrace(); //TODO: Present Exception to user
 		}
 		byte[] firstThree = {isRightID3[0],isRightID3[1],isRightID3[2]};
 		byte[] nextTwo = {isRightID3[3],isRightID3[4]};
 		String identifier = new String(firstThree);
 		byte[] rightVersionNumber = {3,0};
 		return identifier.equals("ID3") && 
 				Arrays.equals(nextTwo,rightVersionNumber);
 	}
 
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
 		
 		int id = input.readUnsignedShort() << 8;
 		id |= input.readUnsignedByte();
 		int version = input.readUnsignedShort();
 		if(id != 0x00494433 || version != 0x0300){
 			System.out.println("Unable to find ID3v2.3 Header!");
 			return; //maybe we should throw an exception here as MP3File already checked for header.
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
 		switch (frameType) {
 			case "TALB" : setAlbum(input.readStringOfLengthWithCharsetByte(fsize)); break;
 			case "TIT2" : setTitle(input.readStringOfLengthWithCharsetByte(fsize)); break;
 			case "TPE1" : setArtist(input.readStringOfLengthWithCharsetByte(fsize)); break; //check whether TPE2 is correct
 			case "TYER" : setYear(input.readStringOfLengthWithCharsetByte(fsize)); break;
 			case "APIC" : parsePic(input, fsize); break;
 			default: byte[] buff2  = new byte[fsize]; input.read(buff2); 
 			unknownFrames.add(new ID3Frame(frameType, fsize, flags, buff2));
 			break;
 		}
 	}
 	
 	private void parsePic(ID3InputStream input, int fsize) throws FileNotFoundException, IOException {
 		Charset cs = input.readCharset();
 		coverMime = input.readStringUntilZero(cs);
 		int picType = input.readUnsignedByte();
 		if(picType != 0x03) 
 			return;
 		String desc = input.readStringUntilZero(cs);
 		byte buff[] = new byte[fsize-1-coverMime.length()-1-1-desc.length()-1];
 		input.read(buff);
 		input.readPadding();
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
 		if(cover != null)
 			newTagSize += 10 + 3 + cover.length + coverMime.getBytes(cs).length;
 		
 		// create outputstream
 		DataOutputStream dos = new DataOutputStream(new FileOutputStream(file));
 		//write magic bytes and size of tag
 		dos.write(new byte[]{0x49, 0x44, 0x33, 0x03, 0x00}); //write Header start
 		dos.writeByte(headerFlags);
 		dos.write(convertToSynchSafe(newTagSize));
 		
 		// write the four elemental text frames and the cover
 		writeTextFrame(dos, "TPE1", artistData);
 		writeTextFrame(dos, "TALB", albumData);
 		writeTextFrame(dos, "TIT2", titleData);
 		writeTextFrame(dos, "TYER", yearData);
 		if(cover != null)
 			writeCover(dos);
 		
 		// write all the unkown frames not modified by our software
 		for(ID3Frame frame : unknownFrames){
 			frame.writeToStream(dos);
 		}
 		
 		//tag ist finished, we can write the music data
 		dos.write(musicData);
 		// and close
 		dos.close();
 	}
 
 	private void writeCover(DataOutputStream dos) throws UnsupportedEncodingException, IOException {
 		dos.write("APIC".getBytes("ASCII"));
 		dos.writeInt(cover.length+4+coverMime.getBytes("ISO-8859-1").length); // write size of frame
 		dos.writeByte(0); //flag byte 1
 		dos.writeByte(0); //flag byte 2
 		dos.writeByte(0); //charset
 		dos.write(coverMime.getBytes("ISO-8859-1")); //mime type, e.g. image/jpeg
 		dos.write(new byte[]{0,3,0}); // null-terminated string, 3 = cover, 0 = description (null-terminated)
 		dos.write(cover); // write image data
 	}
 
 	private void writeTextFrame(DataOutputStream dos, String id, byte[] titleData) throws UnsupportedEncodingException, IOException {
 		dos.write(id.getBytes("ASCII")); //write frame identifier
 		dos.writeInt(titleData.length+1); //write size of frame
 		dos.writeShort(0); //flags
 		dos.writeByte(0); //charset = ISO-8859-1
 		dos.write(titleData); //write text data
 	}
 
 	private byte[] convertToSynchSafe(int newTagSize) {
 		byte[] ret = new byte[4];
 		for(int i = 3; i >= 0; i--){
 			ret[3-i] = (byte) ((newTagSize & (0x7F << i*7)) >> (i*7));
 		}
 		return ret;
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
 		this.title = title;
 		dirty = true;
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
 		this.artist = artist;
 		dirty = true;
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
 		this.album = album;
 		dirty = true;
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
 		this.year = year;
 		dirty = true;
 	}
 
 	/**
 	 * @return the cover
 	 */
 	public byte[] getCover() {
 		if(cover == null) {
 			tryToParse();
 		}
 		return cover;
 	}
 
 	/**
 	 * @param cover the cover to set
 	 */
 	public void setCover(byte[] cover) {
 		this.cover = cover;
 		dirty = true;
 	}
 
 	public void readCoverFromFile(File file) {
 		//TODO: Add conversion for other image formats like BMP, TIFF, etc
 		try {
 			InputStream in = new BufferedInputStream(new FileInputStream(file));
 			byte buff[] = new byte[(int) file.length()];
 			in.read(buff);
 			cover = buff;
 			coverMime = file.getName().endsWith(".png") ? "image/png" : "image/jpeg";
 			in.close();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 }
