 package de.uni_koeln.spinfo.wafs.mp3;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 
 import org.apache.log4j.Logger;
 
 import de.uni_koeln.spinfo.wafs.mp3.data.Track;
 import de.uni_koeln.spinfo.wafs.mp3.util.ArtworkHelper;
 import de.uni_koeln.spinfo.wafs.mp3.util.MurmurHash;
 
 public class CoverDB {
 
 	private File dir;
 	
 	private Logger logger = Logger.getLogger(getClass());
 
 	public CoverDB(File dir) {
 		this.dir = dir;
 	}
 
 	public BufferedInputStream getCover(Track track) throws NoImageAvailableException {
 		// FIXME: Not always png..., can also be jpeg
 		try {
 			String fileName = track.getArtist()+"_"+track.getAlbum()
 					+ ".png";
 			File coverFile = new File(dir, fileName);
			logger.info("Cover file: " + coverFile.getAbsolutePath());
 			if (coverFile.exists()
 					&& coverFile.lastModified() >= track.getLastModified()) {
 				return new BufferedInputStream(new FileInputStream(coverFile));
 			}
 			InputStream src = ArtworkHelper.getImage(track);
			logger.info("Extracted image: " + src);
 			if (src == null)
 				throw new NoImageAvailableException();
 			copy(src, coverFile);
 			return new BufferedInputStream(new FileInputStream(coverFile));
 		} catch (Mp3Exception | IOException e) {
 			throw new NoImageAvailableException(e);
 		}
 	}
 
 	private void copy(InputStream src, File dest) throws IOException {
		dest.getParentFile().mkdirs();
 		BufferedOutputStream out = new BufferedOutputStream(
 				new FileOutputStream(dest));
 		byte[] buffer = new byte[1024];
 		int len;
 		while ((len = src.read(buffer)) > -1) {
 			out.write(buffer, 0, len);
 		}
 		out.close();
 		src.close();
 	}
 
 	public void storeCover(Track track) {
 		// FIXME: Not always png..., can also be jpeg
 		String fileName = MurmurHash.hash64(track.getLocation().toString())
 				+ ".png";
 		File coverFile = new File(dir, fileName);
 		if (coverFile.exists()
 				&& coverFile.lastModified() >= track.getLastModified()) {
 			return;
 		}
 		try {
 			InputStream src = ArtworkHelper.getImage(track);
 			if (src == null)
 				throw new NoImageAvailableException();
 			copy(src, coverFile);
 		} catch (Mp3Exception | NoImageAvailableException | IOException e) {
 			logger.warn("Could not extract cover from " + track + ": " + e);
 		}
 	}
 
 }
