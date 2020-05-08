 /*
  * Copyright 2004-2011 ICEsoft Technologies Canada Corp. (c)
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions an
  * limitations under the License.
  */
 
 package org.icemobile.samples.mediacast;
 
 import javax.faces.bean.ApplicationScoped;
 import javax.faces.bean.ManagedBean;
 import java.io.Serializable;
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * Simple in memory image cache which stores an uploaded image and a scaled
  * thumbnail version of it.
  */
 @ManagedBean(name = MediaStore.BEAN_NAME)
 @ApplicationScoped
 public class MediaStore implements Serializable {
 
     public static final String BEAN_NAME = "mediaStore";
 
     private static final int MAX_CACHE_SIZE = 10;
     private static final int MAX_CAROUSEL_SIZE = 5;
 
     private LinkedList<MediaMessage> mediaStack;
     private LinkedList<MediaMessage> mediaCarouselStack;
 
     public MediaStore() {
         mediaStack = new LinkedList<MediaMessage>();
         mediaCarouselStack = new LinkedList<MediaMessage>();
     }
 
     public List<MediaMessage> getMediaStack() {
         return mediaStack;
     }
 
     public List<MediaMessage> getCarouselStack() {
         return mediaCarouselStack;
     }
 
     public int getCarouselStackCount() {
        return mediaCarouselStack.size();
     }
 
     /**
      * Add a new image set to the the store.
      *
      * @param photoMessage photo image to add to store.
      */
     public void addMedia(MediaMessage photoMessage) {
         mediaStack.push(photoMessage);
         // keep the list of upload small. we don't want to break the bank!
         if (mediaStack.size() > MAX_CACHE_SIZE) {
             MediaMessage message = mediaStack.removeLast();
             message.dispose();
         }
         mediaCarouselStack.push(mediaStack.peek());
         if (mediaCarouselStack.size() > MAX_CAROUSEL_SIZE) {
             mediaCarouselStack.removeLast();
             // shared object reference with media stack so no cleanup necessary.
         }
     }
 
 }
