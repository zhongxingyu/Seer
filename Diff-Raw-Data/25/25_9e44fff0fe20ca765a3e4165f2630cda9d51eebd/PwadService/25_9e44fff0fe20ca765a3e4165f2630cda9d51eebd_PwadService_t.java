 /**
  *
  * Copyright 2010 Laurent Desgrange
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  */
 package net.desgrange.pwad.service;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 import net.desgrange.pwad.model.Album;
 import net.desgrange.pwad.model.Picture;
 import net.desgrange.pwad.service.exceptions.BadUrlException;
 import net.desgrange.pwad.service.exceptions.DownloadFailedException;
 
 import org.apache.commons.lang.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.gdata.client.photos.PicasawebService;
 import com.google.gdata.data.photos.AlbumFeed;
 import com.google.gdata.data.photos.GphotoEntry;
 import com.google.gdata.data.photos.PhotoEntry;
 import com.google.gdata.util.ServiceException;
 
 public class PwadService {
     private final Logger logger = LoggerFactory.getLogger(getClass());
     private final PicasawebService picasawebService;
 
    public PwadService(final PicasawebService picasawebService) {
         this.picasawebService = picasawebService;
     }
 
     public Album getAlbumByInvitationLink(final String url) throws BadUrlException {
         final StringBuilder albumUrl = buildAlbumUrl(url);
         logger.debug("album url: {}", albumUrl);
         logger.trace("Connecting to google using proxy: {}:{}", System.getProperty("http.proxyHost"), System.getProperty("http.proxyPort"));
 
         try {
             final AlbumFeed albumFeed = picasawebService.getFeed(new URL(albumUrl.toString()), AlbumFeed.class);
             logger.trace("Received album feed: {}", albumFeed);
             final Album album = new Album();
             album.setId(albumFeed.getGphotoId());
             album.setName(albumFeed.getTitle().getPlainText());
             album.setPictures(getPictures(albumFeed));
             return album;
         } catch (final MalformedURLException e) {
             logger.warn("Error while downloading album", e);
             throw new BadUrlException(e);
         } catch (final IOException e) {
             logger.warn("Error while downloading album", e);
             throw new BadUrlException(e);
         } catch (final ServiceException e) {
             logger.warn("Error while downloading album", e);
             throw new BadUrlException(e);
         }
     }
 
     @SuppressWarnings("rawtypes")
    private List<Picture> getPictures(final AlbumFeed albumFeed) {
         final List<Picture> pictures = new ArrayList<Picture>(albumFeed.getEntries().size());
         final List<GphotoEntry> photoEntries = albumFeed.getEntries();
         for (final GphotoEntry entry : photoEntries) {
             final Picture picture = new Picture();
             picture.setId(entry.getId());
             picture.setName(entry.getTitle().getPlainText());
             picture.setUrl(new PhotoEntry(entry).getMediaContents().get(0).getUrl());
             pictures.add(picture);
         }
         return pictures;
     }
 
     private StringBuilder buildAlbumUrl(final String url) throws BadUrlException {
         final String userName = UrlUtils.getParameter(url, "uname");
         final String targetType = UrlUtils.getParameter(url, "target");
         final String targetId = UrlUtils.getParameter(url, "id");
 
         if (StringUtils.isBlank(userName) || StringUtils.isBlank(targetType) || StringUtils.isBlank(targetId)) {
             throw new BadUrlException("The link provided is not supported.");
         }
         if (!"ALBUM".equalsIgnoreCase(targetType)) {
             throw new BadUrlException("The link provided is not supported.");
         }
 
         final StringBuilder albumUrl = new StringBuilder("http://picasaweb.google.com/data/feed/api");
         albumUrl.append("/user/").append(userName);
         albumUrl.append("/albumid/").append(targetId);
         albumUrl.append("?kind=photo&imgmax=d");
         return albumUrl;
     }
 
     public void downloadPicture(final Picture picture, final File outputDirectory) throws DownloadFailedException {
         try {
             final FileOutputStream output = new FileOutputStream(new File(outputDirectory, picture.getName()));
             final InputStream input = new URL(picture.getUrl()).openStream();
             final byte chunk[] = new byte[1024];
             int read = 0;
             while ((read = input.read(chunk)) != -1) {
                 output.write(chunk, 0, read);
             }
             input.close();
             output.close();
         } catch (final FileNotFoundException e) {
             logger.error("Error while downloading picture", e);
             throw new DownloadFailedException(e);
         } catch (final MalformedURLException e) {
             logger.error("Error while downloading picture", e);
             throw new DownloadFailedException(e);
         } catch (final IOException e) {
             logger.error("Error while downloading picture", e);
             throw new DownloadFailedException(e);
         }
     }
 }
