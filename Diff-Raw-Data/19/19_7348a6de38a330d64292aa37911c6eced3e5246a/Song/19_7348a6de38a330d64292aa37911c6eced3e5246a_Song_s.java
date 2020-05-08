 package data.domain;
 
 import java.util.*;
 
 /**
  * User: jpipe
  * Date: 11/19/12
  *
  * Represents a Song. Contains the title, lyrics, date last used, and all other keywords
  * of a song stored in the library. Attributes besides title and lyrics are stored in a
  * {@link List} of Strings.
  */
 public class Song {
 
     String title = "";
     String lyrics = "";
     Date lastUsed;
     String author = "";
     String lyricist = "";
     String copyright = "";
 
     ArrayList<String> keywords = new ArrayList<String>();
 
     public Song() {
         setLastUsedToNow();
     }
 
     public Song(String title) {
         this.title = title;
         setLastUsedToNow();
     }
 
     public String getTitle() {
         return title;
     }
 
     public void setTitle(String title) {
         this.title = title;
     }
 
     public String getLyrics() {
         return lyrics;
     }
 
     public void setLyrics(String lyrics) {
         this.lyrics = lyrics;
     }
 
     public Date getLastUsed() {
         return lastUsed;
     }
 
     public void setLastUsed(Long lastUsed) {
         this.lastUsed = new Date(lastUsed);
     }
 
     public List<String> getKeywords() {
         return keywords;
     }
 
     public String getAuthor() {
         return author;
     }
 
     public void setAuthor(String author) {
         this.author = author;
     }
 
     public String getLyricist() {
         return lyricist;
     }
 
     public void setLyricist(String lyricist) {
         this.lyricist = lyricist;
     }
 
     public String getCopyright() {
         return copyright;
     }
 
     public void setCopyright(String copyright) {
         this.copyright = copyright;
     }
 
     public void addKeyword(String... key) {
        Collections.addAll(keywords, key);
     }
 
     public void addAllKeywords(List<String> keys) {
         keywords.addAll(keys);
     }
     /**
      * Sets the date this song was last used to the current time;
      * that is, sets {@link #lastUsed} equal to a new {@link Date}.
      */
     public void setLastUsedToNow() {
         lastUsed = new Date();
     }
 
     public boolean hasKeyword(String keyword) {
         return keywords.contains(keyword);
     }
 
     public Song copy() {
         Song newSong = new Song();
 
         newSong.setTitle(this.title);
         newSong.setLyrics(this.lyrics);
         newSong.setAuthor(this.author);
         newSong.setLyricist(this.lyricist);
         newSong.setCopyright(this.copyright);
         Collections.copy(newSong.keywords, this.keywords);
         newSong.setLastUsedToNow();
 
         return newSong;
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o)
             return true;
         if (o == null || getClass() != o.getClass())
             return false;
 
         Song other = (Song) o;
 
         return title != null ? title.equalsIgnoreCase(other.title) : other.title == null;
 
     }
 
     @Override
     public int hashCode() {
         return title != null ? title.hashCode() : 0;
     }
 }
