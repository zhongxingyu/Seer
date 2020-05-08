 package com.totalchange.bunman.library;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.nio.channels.FileChannel;
 
 import org.jaudiotagger.audio.AudioFile;
 import org.jaudiotagger.audio.AudioFileIO;
 import org.jaudiotagger.audio.exceptions.CannotReadException;
 import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
 import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
 import org.jaudiotagger.audio.mp3.MP3File;
 import org.jaudiotagger.tag.FieldKey;
 import org.jaudiotagger.tag.Tag;
 import org.jaudiotagger.tag.TagException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.totalchange.bunman.CatalogueSongListener;
 import com.totalchange.bunman.Song;
 import com.totalchange.bunman.WritableCatalogue;
 
 public class LibraryCatalogue implements WritableCatalogue {
     private static final Logger logger = LoggerFactory
             .getLogger(LibraryCatalogue.class);
 
     private File root;
 
     public LibraryCatalogue(File root) {
         this.root = root;
     }
 
     private void recurseForMusicFiles(File dir, CatalogueSongListener listener) {
         for (File file : dir.listFiles()) {
             if (file.isFile()) {
                 try {
                     AudioFile audioFile = AudioFileIO.read(file);
                     Tag tag = audioFile.getTag();
                     if (tag != null) {
                         listener.yetAnotherSong(new LibrarySong(file, tag));
                     } else {
                         logger.debug("Skipped file " + file + " as it doesn't "
                                 + "have any tag info");
                     }
                 } catch (CannotReadException crEx) {
                     logger.debug("Skipped file " + file + " as couldn't be "
                             + "read by JAudioTagger", crEx);
                 } catch (InvalidAudioFrameException iafEx) {
                     logger.debug("Skipped file " + file + " as JAudioTagger "
                             + "hates the audio frame", iafEx);
                 } catch (ReadOnlyFileException rofEx) {
                     logger.warn("Skipped file " + file + " as it's read only "
                             + "(odd for read only access)", rofEx);
                 } catch (TagException tagEx) {
                     logger.debug("Skipped file " + file + " as JAudioTagger "
                             + "hates the tag", tagEx);
                 } catch (IOException ioEx) {
                     logger.warn("Skipped file " + file + " as hit a problem "
                             + "reading from storage", ioEx);
                 }
             } else if (file.isDirectory()) {
                 recurseForMusicFiles(file, listener);
             }
         }
     }
 
     private String makeFileSystemSafe(String str) {
         return str.replaceAll("[^\\w ]", "");
     }
 
     private File makeSongFile(Song song) {
         File artistDir = new File(root, makeFileSystemSafe(song.getArtist()));
         File albumDir = new File(artistDir, makeFileSystemSafe(song.getAlbum()));
 
         StringBuilder filename = new StringBuilder();
         if (song.getTrack() > 0 && song.getTrack() < 10) {
             filename.append("0").append(song.getTrack()).append(" - ");
         } else if (song.getTrack() > 0) {
             filename.append(song.getTrack()).append(" - ");
         }
         filename.append(song.getTitle());
         filename.append(".").append(song.getFormat().getExtension());
 
         return new File(albumDir, makeFileSystemSafe(filename.toString()));
     }
 
     public void listAllSongs(CatalogueSongListener listener) {
         recurseForMusicFiles(root, listener);
     }
 
     public Song copySong(Song song) throws IOException {
         File songFile = makeSongFile(song);
         if (songFile.exists()) {
             // TODO: Internationalise
             throw new IOException("File " + songFile
                     + " already exists for song " + song);
         }
 
         InputStream in = song.getInputStream();
         try {
             FileOutputStream out = new FileOutputStream(songFile);
             try {
                 if (in instanceof FileInputStream) {
                     FileChannel src = ((FileInputStream) in).getChannel();
                     FileChannel dest = out.getChannel();
                     dest.transferFrom(src, 0, src.size());
                 } else {
                     byte[] buf = new byte[4 * 1024];
                     int read;
                     while ((read = in.read(buf)) > -1) {
                         out.write(buf, 0, read);
                     }
                 }
             } finally {
                 out.close();
             }
         } finally {
             in.close();
         }
 
         try {
             MP3File f = (MP3File) AudioFileIO.read(songFile);
 
             Tag tag = f.getTagOrCreateAndSetDefault();
             tag.setField(FieldKey.ARTIST, song.getArtist());
             tag.setField(FieldKey.ALBUM, song.getAlbum());
             tag.setField(FieldKey.GENRE, song.getGenre());
             tag.setField(FieldKey.YEAR, String.valueOf(song.getYear()));
             tag.setField(FieldKey.TRACK, String.valueOf(song.getTrack()));
             tag.setField(FieldKey.TITLE, song.getTitle());
 
             f.commit();
 
             return new LibrarySong(songFile, tag);
         } catch (Throwable th) {
             // TODO: Internationalise
             throw new IOException("Failed to write tag data to " + songFile
                     + " based on song " + song + " with error: "
                    + th.getMessage());
         }
     }
 }
