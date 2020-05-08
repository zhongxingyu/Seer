 package info.dourok.android.lyrics;
 
import java.io.File;
import java.io.FileInputStream;
 import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagField;

import android.os.storage.StorageManager;
import android.util.Log;
 
 public class LyricsStorageManager {
 	public static final int FROM_TAG = 0x1;
 	public static final int FROM_FOLDER = 0x2;
 	public static final int FROM_DATASTORE = 0x4;
 
 	public static final LyricsStorage tag = new TagLyricsStorage();
 	public static final LyricsStorage folder = new SameFolder();
 	public static final LyricsStorage storage = new ExternalStorage();
 
 	private LyricsStorageManager() {
 		
 	}
 	
 	
 	
 
 	private static String searchFromDataStore(SongWrapper song){
 		try {
 			return storage.read(song);
 		} catch (IOException e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	private static String searchFromFolder(SongWrapper song){
 		try {
 			return folder.read(song);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	private static String searchFromTag(SongWrapper song) {
 		try {
 			return tag.read(song);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	public static String searchLyrics(SongWrapper song ,int m){
 		
 		String s = null;
 		if(s==null&&(m&FROM_DATASTORE)!=0){
 			
 			s = searchFromDataStore(song);
 		}
 		if(s==null&&(m&FROM_TAG)!=0){
 			s = searchFromTag(song);
 		}
 		if(s==null&&(m&FROM_FOLDER)!=0){
 			s = searchFromFolder(song);
 		}
 		return s;
 	}
 	
 	public static String searchLyrics(SongWrapper song){
 		return searchLyrics(song, FROM_DATASTORE|FROM_FOLDER|FROM_TAG);
 	}
 
 
 	
 }
