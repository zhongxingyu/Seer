 package musicplayer;
 
 import com.mpatric.mp3agic.ID3v1;
 import com.mpatric.mp3agic.ID3v2;
 import com.mpatric.mp3agic.Mp3File;
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import javax.activation.MimetypesFileTypeMap;
 import javax.print.attribute.standard.Media;
 import javazoom.jl.player.Player;
 
 
 /**
  * Song vastaa yksittäisen kappaleen tiedoista ja osaa hakea tiedoston
  * metatiedoista kappaleen pituuden, nimen, soittaa kappaleen jne.
  * @author tuurekau
  */
 public class Song {
     private File file;
     private String artist;
     private String name;
     private int length;
     private String genre;
     private Player player;
     private int likes;
     private int dislikes;
     
 
     /**
      * Konstruktorissa luodaan uusi tiedosto, jos tiedostonimi on annettu
      * @param fileName
      */
     public Song(String fileName) {
         this(new File(fileName));
     }
     
     /**
      * Konstruktori etsii tiedoston metatiedoista kappaleen ja artistin nimen jne.
      * @param file musiikkitiedosto
      */
     public Song(File file) {
         this.file = file;
         
         //check that file exists
         if (this.file.exists()){
             try{
                 Mp3File mp3file = new Mp3File(this.file.getAbsolutePath());
                 this.length = (int) mp3file.getLengthInMilliseconds();
                 
                 if (mp3file.hasId3v1Tag()){
                     ID3v1 tag = mp3file.getId3v1Tag();
                     this.name = tag.getTitle();
                     this.artist = tag.getArtist();
                     this.genre = tag.getGenreDescription();
                 }
                 if (mp3file.hasId3v2Tag()){
                     ID3v2 tag = mp3file.getId3v2Tag();
                     this.name = tag.getTitle();
                     this.artist = tag.getArtist();
                     this.genre = tag.getGenreDescription();
                 }
             }
             catch(Exception e){}
         }
     }
 
     public String getFileName() {
         return this.file.getName();
     }
     
     public String getFullFileName() {
         return this.file.getAbsolutePath();
     }
 
     public File getFile(){
         return this.file;
     }
     
     public void setFile(File file){
         this.file = file;
     }
     
     public void resetPlayer(){
         this.player = null;
     }
     
     //setter and getter for artist
     public void setArtist(String artist) {
         this.artist = artist;
     }
     public String getArtist() {
         return artist;
     }
     
     public int getDislikes() {
         return dislikes;
     }
     public int getLikes() {
         return likes;
     }
     
     /**
      * 
      * @return arvo 0-100, joka kertoo kuinka suosittu kappale on käyttäjän
      * tykkäyksien ja skippauksien jne. perusteella
      */
     public int getPopular(){
         int popular = 50+(likes*10)-(dislikes*dislikes);
         if (popular<0) popular = 0;
         if (popular>100) popular = 100;
         return popular;
     }
     
     //setter and getter for name
     public void setName(String name) {
         this.name = name;
     }
     /**
      *
      * @return Kappaleen nimi. Jos sitä ei ole määritelty, palautetaan tiedoston nimi
      */
     public String getName() {
         if (name == null || name.isEmpty()) return this.getFileName();
         return name;
     }
     
     //setter and getter for length
     public void setLength(int length) {
         if (length<0) length = 0;
         this.length = length;
     }
     public int getLength() {
         return length;
     }
 
     //setter and getter for genre
     public void setGenre(String genre) {
         this.genre = genre;
     }
     public String getGenre() {
         return genre;
     }
     
     /**
      *  Play yrittää soittaa kappaletta
      * @return onnistuiko soittaminen
      */
     public boolean play(final SongList list){
         try {
            FileInputStream fis = new FileInputStream(file.getAbsolutePath());
             BufferedInputStream bis = new BufferedInputStream(fis);
             player = new Player(bis);
         }
         catch (Exception e) {
            System.out.println("Problem playing file " + file.getAbsolutePath());
             System.out.println(e);
             return false;
         }
 
         // run in new thread to play in background
         Thread t = new Thread() {
             public void run() {
                 try { player.play(); }
                 catch (Exception e) {
                     //System.out.println(e);
                 }
                 if (player != null && player.isComplete()) list.next();
             }
         };
         t.start();
         
         return true;
     }
     
     /**
      * Stop pysäyttää kappaleen toistamisen
      */
     public void stop() {
         if (player != null) player.close();
     }
     
     /**
      * getPosition() palauttaa missä kohdassa kappaletta ollaan.
      * @return kuinka pitkään kappaletta on soitettu
      */
     public int getPosition(){
         if (player!=null) return player.getPosition();
         return 0;
     }
     
     /**
      * like lisää kappaleen tykkäyksiä
      */
     public void like(){
         likes++;
     }
     /**
      * dislike lisää kappaleen epä-tykkäyksiä
      */
     public void dislike(){
         dislikes++;
     }
     
     /**
      * Kappaleen tekstiesityksenä toimii sen nimi
      * @return kappaleen nimi
      */
     @Override
     public String toString(){
         return getName();
     }
     
     /**
      *  Kappaleita verrataan niiden koko tiedostonimen mukaan
      * @param o verrattava objekti
      * @return ovatko kappaleet samat
      */
     @Override
     public boolean equals(Object o){
         if (!(o instanceof Song)) return false;
         Song s = (Song)o;
         return s.getFullFileName().equals(this.getFullFileName());
     }
 }
