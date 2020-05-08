 /*
  * Copyright 2009 Chris Searle
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *        http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 
 package net.chrissearle.flickrvote.flickr.impl;
 
 import com.aetrion.flickr.Flickr;
 import com.aetrion.flickr.FlickrException;
 import com.aetrion.flickr.photos.Photo;
 import com.aetrion.flickr.photos.PhotosInterface;
 import com.aetrion.flickr.photos.SearchParameters;
 import net.chrissearle.flickrvote.flickr.FlickrServiceException;
 import net.chrissearle.flickrvote.flickr.ImageTagSearchDAO;
 import net.chrissearle.flickrvote.flickr.model.FlickrImage;
 import net.chrissearle.flickrvote.flickr.model.FlickrImages;
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 import org.xml.sax.SAXException;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 @Component
 public class FlickrJImageTagSearchDAO extends AbstractFlickrJImageSupport implements ImageTagSearchDAO {
     private Logger logger = Logger.getLogger(this.getClass());
     private Flickr flickr;
 
     private static final int MAX_SEARCH_HITS = 500;
     private static final int SEARCH_PAGE_ONE = 1;
 
     @Autowired
     public FlickrJImageTagSearchDAO(Flickr flickr) {
         this.flickr = flickr;
     }
 
     public FlickrImages searchTag(String tag, Date earliestDate) {
         if (logger.isDebugEnabled()) {
             logger.debug("Searching for " + tag + " and date " + earliestDate);
         }
 
         if (earliestDate == null) {
             throw new FlickrServiceException("Earliest date cannot be null");
         }
 
        FlickrImages images = searchTag(tag);
 
        for (FlickrImage image : images.getImages()){
           if (image.getTakenDate() != null && image.getTakenDate().getTime() < earliestDate.getTime()) {
               images.getImages().remove(image);
            }
         }
 
        return images;
     }
 
     public FlickrImages searchTag(String tag) {
         if (logger.isDebugEnabled()) {
             logger.debug("Searching for " + tag);
         }
 
         try {
             List<FlickrImage> images = new ArrayList<FlickrImage>();
 
             for (Photo photo : retrieveByTag(tag)) {
                 images.add(buildImage(photo));
             }
 
             return new FlickrImages(images);
         } catch (IOException e) {
             throw new FlickrServiceException(e);
         } catch (SAXException e) {
             throw new FlickrServiceException(e);
         } catch (FlickrException e) {
             throw new FlickrServiceException(e);
         }
     }
 
     @SuppressWarnings("unchecked")
     private List<Photo> retrieveByTag(String tag) throws IOException, SAXException, FlickrException {
         PhotosInterface photosInterface = flickr.getPhotosInterface();
 
         String[] tags = new String[SEARCH_PAGE_ONE];
         tags[0] = new StringBuilder().append("#").append(tag).toString();
 
         SearchParameters params = new SearchParameters();
         params.setTags(tags);
 
         // By grabbing the dates at this point - saves us a call to get image.
         // We don't bother asking for names here to save a call to get photographer since that is
         // still needed to get the user icon/avatar.
         params.setExtrasDateUpload(true);
         params.setExtrasDateTaken(true);
 
         List<Photo> photos = (List<Photo>) photosInterface.search(params, MAX_SEARCH_HITS, SEARCH_PAGE_ONE);
 
         return photos;
     }
 }
