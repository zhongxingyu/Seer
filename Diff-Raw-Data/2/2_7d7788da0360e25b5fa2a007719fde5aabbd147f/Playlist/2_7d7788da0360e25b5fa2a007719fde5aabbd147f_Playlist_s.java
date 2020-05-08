 package com.musiclibrary.euphonybusinesslogicimplementation.entities;
 
 import java.io.Serializable;
 import java.util.Map;
 import javax.persistence.CascadeType;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.ManyToMany;
 
 /**
  * Playlist entity.
  *
  * @author Tomas Smetanka
  */
 @Entity
 public class Playlist implements Serializable {
 
     @Id
     @GeneratedValue(strategy = GenerationType.SEQUENCE)
     private Long id;
     private String name;
    @ManyToMany(cascade = CascadeType.ALL)
     private Map<Integer, Song> songs;
 
     public Playlist() {
     }
 
     public Playlist(Long id, String name, Map<Integer, Song> songs) {
         this.id = id;
         this.name = name;
         this.songs = songs;
     }
 
     public Playlist(Long id, String name) {
         this.id = id;
         this.name = name;
     }
 
     public Playlist(String name) {
         this.name = name;
     }
 
     public Playlist(String name, Map<Integer, Song> songs) {
         this.name = name;
         this.songs = songs;
     }
 
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public Map<Integer, Song> getSongs() {
         return songs;
     }
 
     public void setSongs(Map<Integer, Song> songs) {
         this.songs = songs;
     }
 
     @Override
     public int hashCode() {
         int hash = 7;
         hash = 67 * hash + (this.id != null ? this.id.hashCode() : 0);
         return hash;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         final Playlist other = (Playlist) obj;
         if (this.id != other.id && (this.id == null || !this.id.equals(other.id))) {
             return false;
         }
         return true;
     }
 
     @Override
     public String toString() {
         return "Playlist{" + "id=" + id + ", name=" + name + ", songs=" + songs + '}';
     }
 }
