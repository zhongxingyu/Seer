 package de.uni_koeln.spinfo.wafs.mp3.util;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.InputStream;
 import java.util.List;
 
 import org.jaudiotagger.audio.AudioFile;
 import org.jaudiotagger.audio.AudioFileIO;
 import org.jaudiotagger.audio.mp3.MP3File;
 import org.jaudiotagger.tag.datatype.Artwork;
 import org.jaudiotagger.tag.id3.AbstractID3Tag;
 import org.jaudiotagger.tag.id3.ID3v1Tag;
 import org.jaudiotagger.tag.id3.ID3v22Tag;
 import org.jaudiotagger.tag.id3.ID3v23Tag;
 import org.jaudiotagger.tag.id3.ID3v24Tag;
 
 import de.uni_koeln.spinfo.wafs.mp3.Mp3Exception;
 import de.uni_koeln.spinfo.wafs.mp3.data.Track;
 
 public class ArtworkHelper {
 	
 
 	public static InputStream getImage(Track track) throws Mp3Exception {
 		try {
			File file = new File(track.getLocation());
 			AudioFile readFile = AudioFileIO.read(file);
 			if (readFile instanceof MP3File) {
 				MP3File f = (MP3File) AudioFileIO.read(file);
 				AbstractID3Tag tag = f.getID3v2Tag();
 				if (tag == null)
 					tag = f.getID3v1Tag();
 				ByteArrayInputStream imageData = getArtWork(tag);
 				return imageData;
 			}
 		} catch (Exception e) {
 			throw new Mp3Exception(e);
 		}
 		return null;
 
 	}
 
 	private static ByteArrayInputStream getArtWork(AbstractID3Tag tag)
 			throws Mp3Exception {
 		try {
 			List<Artwork> artworkList = null;
 			if (tag instanceof ID3v1Tag) {
 				artworkList = ((ID3v1Tag) tag).getArtworkList();
 			}
 			if (tag instanceof ID3v22Tag) {
 				artworkList = ((ID3v22Tag) tag).getArtworkList();
 			}
 			if (tag instanceof ID3v23Tag) {
 				artworkList = ((ID3v23Tag) tag).getArtworkList();
 			}
 			if (tag instanceof ID3v24Tag) {
 				artworkList = ((ID3v24Tag) tag).getArtworkList();
 			}
 			for (Artwork artwork : artworkList) {
 				if (artwork == null)
 					continue;
 				byte[] bytes = artwork.getBinaryData();
 				if (bytes == null)
 					continue;
 				return new ByteArrayInputStream(bytes);
 			}
 			return null;
 		} catch (Exception e) {
 			return null;
 		}
 	}
 
 }
