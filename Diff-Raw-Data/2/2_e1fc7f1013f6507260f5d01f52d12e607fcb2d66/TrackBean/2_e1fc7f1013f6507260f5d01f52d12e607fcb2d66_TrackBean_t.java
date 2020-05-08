 /*
  * The MIT License
  *
  * Copyright 2013 Gravidence.
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package org.gravidence.gravifon.resource.bean;
 
 import com.fasterxml.jackson.annotation.JsonIgnore;
 import com.fasterxml.jackson.annotation.JsonProperty;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import org.gravidence.gravifon.db.domain.TrackDocument;
 import org.gravidence.gravifon.db.domain.VariationInfo;
 import org.gravidence.gravifon.db.message.CreateDocumentResponse;
 import org.gravidence.gravifon.util.BasicUtils;
 
 /**
  * Track bean.<p>
  * Represents Track interface between service and clients.
  * Contains helpful methods to work with {@link TrackDocument}.
  * 
  * @see TrackDocument
  * 
  * @author Maksim Liauchuk <maksim_liauchuk@fastmail.fm>
  */
 public class TrackBean extends ValidateableBean {
     
     /**
      * @see #getId()
      */
     @JsonProperty
     private String id;
     
     /**
      * @see #getTitle()
      */
     @JsonProperty
     private String title;
     
     /**
      * @see #getArtists()
      */
     @JsonProperty
     private List<ArtistBean> artists;
     
     /**
      * @see #getLength()
      */
     @JsonProperty
     private DurationBean length;
     
     /**
      * @see #getAlbum()
      */
     @JsonProperty
     private AlbumBean album;
     
     /**
      * @see #getPosition()
      */
     @JsonProperty
     private String position;
     
     /**
      * @see #getVariationInfo()
      */
     @JsonProperty("variation_info")
     private VariationInfoBean<TrackBean> variationInfo;
 
     /**
      * Returns track identifier.
      * 
      * @return track identifier
      */
     public String getId() {
         return id;
     }
 
     /**
      * @param id
      * @see #getId()
      */
     public void setId(String id) {
         this.id = id;
     }
 
     /**
      * Returns track title.
      * 
      * @return track title
      */
     public String getTitle() {
         return title;
     }
 
     /**
      * @param title
      * @see #getTitle()
      */
     public void setTitle(String title) {
         this.title = title;
     }
 
     /**
      * Returns list of track artists.
      * 
      * @return list of track artists
      */
     public List<ArtistBean> getArtists() {
         return artists;
     }
 
     /**
      * @param artists
      * @see #getArtists()
      */
     public void setArtists(List<ArtistBean> artists) {
         this.artists = artists;
     }
 
     /**
      * Returns track length.
      * 
      * @return track length
      */
     public DurationBean getLength() {
         return length;
     }
 
     /**
      * @param length
      * @see #getLength()
      */
     public void setLength(DurationBean length) {
         this.length = length;
     }
 
     /**
      * Returns track album.
      * 
      * @return track album
      */
     public AlbumBean getAlbum() {
         return album;
     }
 
     /**
      * @param album
      * @see #getAlbum()
      */
     public void setAlbum(AlbumBean album) {
         this.album = album;
     }
 
     /**
      * Returns track position in the album tracklist.
      * 
      * @return track position in the album tracklist
      */
     public String getPosition() {
         return position;
     }
 
     /**
      * @param position
      * @see #getPosition()
      */
     public void setPosition(String position) {
         this.position = position;
     }
 
     /**
      * Returns track variation info.
      * 
      * @return track variation info
      */
     public VariationInfoBean<TrackBean> getVariationInfo() {
         return variationInfo;
     }
 
     /**
      * @param variationInfo
      * @see #getVariationInfo()
      */
     public void setVariationInfo(VariationInfoBean<TrackBean> variationInfo) {
         this.variationInfo = variationInfo;
     }
 
     @Override
     public String toString() {
         return String.format("{id=%s, title=%s}", id, title);
     }
 
     @Override
     public void validate() {
         checkRequired(title, "title");
         checkLength(title, "title", 1, 400);
         
         checkRequired(artists, "artists");
         checkLength(artists, "artists", 1, 40);
         validateCollection(artists);
         
         checkRequired(length, "length");
         length.validate();
         
         if (album != null) {
             album.validate();
         }
         
         if (variationInfo != null) {
             variationInfo.validate();
         }
     }
     
     /**
      * Returns track key.<p>
      * Consists of track artist names (asc sorted), album title and track title. The key is lower cased.
      * 
      * @return track key
      */
     @JsonIgnore
     public List<String> getKey() {
         List<String> result = new ArrayList<>();
         
         for (ArtistBean artist : artists) {
             result.add(BasicUtils.lowerCase(artist.getName()));
         }
         Collections.sort(result);
         
        result.add(album == null ? null : BasicUtils.lowerCase(album.getTitle()));
         
         result.add(BasicUtils.lowerCase(title));
         
         return result;
     }
 
     /**
      * Updates bean with created document identifier.
      * DB returns document identifier and revision only, so there's no need to update bean values.
      * 
      * @param document a created document
      * @return updated bean
      */
     public TrackBean updateBean(CreateDocumentResponse document) {
         if (document != null) {
             id = document.getId();
         }
         
         return this;
     }
     
     /**
      * Updates bean with document values.
      * 
      * @param document track details document
      * @return updated bean
      */
     public TrackBean updateBean(TrackDocument document) {
         if (document != null) {
             id = document.getId();
             title = document.getTitle();
             length = new DurationBean().updateBean(document.getLength());
             album = BeanUtils.idToAlbumBean(document.getAlbumId());
             position = document.getPosition();
             artists = BeanUtils.idsToArtistBeans(document.getArtistIds());
             
             if (document.getVariationInfo() == null) {
                 variationInfo = null;
             }
             else {
                 if (variationInfo == null) {
                     variationInfo = new VariationInfoBean<>();
                 }
                 // Update with upvotes and primary identifier
                 variationInfo.updateBean(document.getVariationInfo());
                 // Update with variation identifiers
                 variationInfo.setVariations(BeanUtils.idsToTrackBeans(document.getVariationInfo().getVariationIds()));
             }
         }
         
         return this;
     }
     
     /**
      * Creates document with bean values.
      * 
      * @return created document
      */
     public TrackDocument createDocument() {
         TrackDocument document = new TrackDocument();
         document.setId(BasicUtils.generateUniqueIdentifier());
         
         if (variationInfo == null) {
             variationInfo = new VariationInfoBean<>();
             variationInfo.setPrimaryVariationId(document.getId());
         }
         
         updateDocument(document);
         
         return document;
     }
     
     /**
      * Updates document with bean values.
      * 
      * @param document track details document
      * @return updated document
      */
     public TrackDocument updateDocument(TrackDocument document) {
         if (document != null) {
             document.setTitle(title);
             document.setLength(BeanUtils.durationBeanToDuration(length));
             document.setAlbumId(album == null ? null : album.getId());
             document.setPosition(position);
             document.setArtistIds(BeanUtils.artistBeansToIds(artists));
             
             if (variationInfo == null) {
                 document.setVariationInfo(null);
             }
             else {
                 VariationInfo vi = new VariationInfo();
                 vi.setKey(getKey());
                 vi.setUpvotes(BeanUtils.upvoteBeansToUpvotes(variationInfo.getUpvotes()));
                 vi.setPrimaryVariationId(variationInfo.getPrimaryVariationId());
                 vi.setVariationIds(BeanUtils.trackBeansToIds(variationInfo.getVariations()));
                 
                 document.setVariationInfo(vi);
             }
         }
         
         return document;
     }
     
 }
