 package services;
 
 import data.SongDAO;
 import data.domain.Song;
 import org.apache.poi.hslf.model.Slide;
 import org.apache.poi.hslf.model.TextRun;
 import org.apache.poi.hslf.usermodel.SlideShow;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import javax.swing.JOptionPane;
 
 /**
  * User: jpipe
  * Date: 12/7/12
  */
 public class PPTImporter {
 
     private static PPTImporter instance;
 
     public static PPTImporter getInstance() {
         if (instance == null)
             instance = new PPTImporter();
         return instance;
     }
 
     private PPTImporter() {
     }
 
     public List<Song> importSong(File ppt) throws IOException {
         List<Song> songs = new ArrayList<Song>();
 
         SlideShow show = readSlideShow(ppt);
 
         parseAllSongs(songs, show);
 
         return songs;
     }
 
 
 
     private SlideShow readSlideShow(File ppt) throws IOException {
         FileInputStream inputStream = null;
         SlideShow show = null;
         try {
             inputStream = new FileInputStream(ppt);
             show = new SlideShow(inputStream);
         } finally {
             if (inputStream != null) {
                 try {
                     inputStream.close();
                 } catch (IOException e) {
                     //ignore
                 }
             }
         }
 
         return show;
     }
 
     private void parseAllSongs(List<Song> songs, SlideShow show) {
         Slide[] slides = show.getSlides();
         if (slides.length < 0)
             return;
 
         Song currentSong = startNewSong(slides[0]);
         for (Slide slide: slides) {
             String nextTitle = slide.getTitle();
            if (nextTitle == null) {
                 continue;
             }
             if (nextTitle.equals(currentSong.getTitle())) {
                 currentSong.setLyrics(currentSong.getLyrics()
                         + slide.getTextRuns()[1].getText().trim() + "\n\n");
             } else {
                 songs.add(currentSong.copy());
                 currentSong = startNewSong(slide);
             }
         }
         songs.add(currentSong);
     }
 
     private Song startNewSong(Slide slide) {
         Song song = new Song();
 
         song.setTitle(slide.getTitle());
         song.setLyrics("");
 
         TextRun[] textRuns = slide.getTextRuns();
         if (textRuns.length >= 3) {
             song.setAuthor(textRuns[2].getText());
         } else {
             song.setAuthor("");
         }
 
         if (textRuns.length >= 4) {
             if (textRuns[3].getText().toLowerCase().contains("copyright")) {
                 song.setCopyright(textRuns[3].getText());
                 song.setLyricist("");
             } else {
                 song.setLyricist(textRuns[3].getText());
                 song.setCopyright("");
             }
         } else {
             song.setLyricist("");
             song.setCopyright("");
         }
 
         if (textRuns.length > 5) {
             song.setCopyright(textRuns[4].getText());
         }
 
         return song;
     }
 
     //TODO: remove for prod
     public static void main(String[] args) {
         //TO RUN THIS METHOD, THIS FILE MUST ALREADY EXIST.
         File target = new File("/Users/kim/Lyric-Show/PM081212-1.ppt");
 
         try {
             List<Song> songs = getInstance().importSong(target);
             List<String> titles = new ArrayList<String>();
             for (Song song: songs) {
                 titles.add(song.getTitle());
                 SongDAO.getInstance().addSong(song);
             }
 
          //   PPTBuilder.getInstance().buildPPT(titles);
         } catch (Exception e) {
             e.printStackTrace();
         } finally {
          //   SongDAO.deleteStuff();
         }
     }
 }
