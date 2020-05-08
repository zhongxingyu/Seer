 /*
  * #%L
  * debox-photos
  * %%
  * Copyright (C) 2012 Debox
  * %%
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  * #L%
  */
 package org.debox.photo.model;
 
 import java.io.File;
 import java.util.Objects;
 
 /**
  * @author Corentin Guy <corentin.guy@debox.fr>
  */
 public class Photo implements Comparable<Photo> {
 
     protected String id;
     protected String name;
     protected String relativePath;
     protected String albumId;
     protected String thumbnailUrl;
     protected String url;
 
     public String getThumbnailUrl() {
         return thumbnailUrl;
     }
 
     public void setThumbnailUrl(String thumbnailUrl) {
         this.thumbnailUrl = thumbnailUrl;
     }
 
     public String getUrl() {
         return url;
     }
 
     public void setUrl(String url) {
         this.url = url;
     }
     
     public String getAlbumId() {
         return albumId;
     }
 
     public void setAlbumId(String albumId) {
         this.albumId = albumId;
     }
 
     public String getId() {
         return id;
     }
 
     public void setId(String id) {
         this.id = id;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public String getRelativePath() {
         return relativePath;
     }
 
     public void setRelativePath(String relativePath) {
         this.relativePath = relativePath;
     }
     
     @Override
     public boolean equals(Object object) {
         if (object == this) {
             return true;
         } 
         if (object instanceof Photo) {
             Photo photo = (Photo) object; 
            return Objects.equals(this.relativePath + File.separatorChar + this.getId(), photo.getRelativePath() + File.separatorChar + photo.getId());
         } 
         return false;
     }
 
     @Override
     public int hashCode() {
         return Objects.hashCode(this.relativePath);
     }
 
     @Override
     public int compareTo(Photo photo) {
         return this.getName().compareToIgnoreCase(photo.getName());
     }
     
 }
