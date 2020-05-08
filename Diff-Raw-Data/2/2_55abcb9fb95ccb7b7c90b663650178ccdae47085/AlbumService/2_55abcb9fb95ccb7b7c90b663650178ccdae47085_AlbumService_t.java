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
 package org.debox.photo.service;
 
 import com.restfb.Connection;
 import com.restfb.DefaultFacebookClient;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.net.HttpURLConnection;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import org.apache.commons.io.FileUtils;
 import org.apache.shiro.SecurityUtils;
 import org.apache.shiro.subject.Subject;
 import org.debox.connector.api.exception.AuthenticationProviderException;
 import org.debox.imaging.ImageUtils;
 import org.debox.photo.dao.AlbumDao;
 import org.debox.photo.dao.CommentDao;
 import org.debox.photo.dao.TokenDao;
 import org.debox.photo.dao.UserDao;
 import org.debox.photo.dao.VideoDao;
 import org.debox.photo.job.RegenerateThumbnailsJob;
 import org.debox.photo.model.Album;
 import org.debox.photo.model.Comment;
 import org.debox.photo.model.Datable;
 import org.debox.photo.model.Media;
 import org.debox.photo.model.user.Contact;
 import org.debox.photo.model.Photo;
 import org.debox.photo.model.Provider;
 import org.debox.photo.model.user.ThirdPartyAccount;
 import org.debox.photo.model.configuration.ThumbnailSize;
 import org.debox.photo.model.Token;
 import org.debox.photo.model.Video;
 import org.debox.photo.model.user.User;
 import org.debox.photo.server.renderer.ZipDownloadRenderer;
 import org.debox.photo.thirdparty.ServiceUtil;
 import org.debox.photo.util.DatableComparator;
 import org.debox.photo.util.SessionUtils;
 import org.debox.photo.util.StringUtils;
 import org.debux.webmotion.server.render.Render;
 import org.debux.webmotion.server.render.RenderStatus;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * @author Corentin Guy <corentin.guy@debox.fr>
  */
 public class AlbumService extends DeboxService {
 
     private static final Logger logger = LoggerFactory.getLogger(AlbumService.class);
     
     protected static AlbumDao albumDao = new AlbumDao();
     protected static CommentDao commentDao = new CommentDao();
     protected static TokenDao tokenDao = new TokenDao();
     protected static VideoDao videoDao = new VideoDao();
     protected static UserDao userDao = new UserDao();
     
     protected RegenerateThumbnailsJob regenerateThumbnailsJob;
     protected ExecutorService threadPool = Executors.newSingleThreadExecutor();
     
     public Render createAlbum(String albumName, String parentId) throws SQLException {
         if (StringUtils.isEmpty(albumName)) {
             return renderError(HttpURLConnection.HTTP_INTERNAL_ERROR, "The name of the album is mandatory.");
         }
         Album album = new Album();
         album.setId(StringUtils.randomUUID());
         album.setName(albumName);
         album.setPublic(false);
         album.setPhotosCount(0);
         album.setDownloadable(false);
         album.setOwnerId(SessionUtils.getUser(SecurityUtils.getSubject()).getId());
         
         if (StringUtils.isEmpty(parentId)) {
             album.setRelativePath(File.separatorChar + album.getName());
             
         } else {
             Album parent = albumDao.getAlbum(parentId);
             if (parent == null) {
                 return renderError(HttpURLConnection.HTTP_INTERNAL_ERROR, "There is not any album with id " + parentId);
             }
             album.setParentId(parentId);
             album.setRelativePath(parent.getRelativePath() + File.separatorChar + album.getName());
         }
         
         Album existingAtPath = albumDao.getAlbumByPath(album.getRelativePath());
         if (existingAtPath != null) {
             return renderError(HttpURLConnection.HTTP_INTERNAL_ERROR, "There is already an album at path (" + album.getRelativePath() + ")");
         }
         
         String[] paths = {ImageUtils.getAlbumsBasePath(album.getOwnerId()), ImageUtils.getThumbnailsBasePath(album.getOwnerId())};
         for (String path : paths) {
             File targetDirectory = new File(path + album.getRelativePath());
             if (!targetDirectory.exists() && !targetDirectory.mkdirs()) {
                 return renderError(HttpURLConnection.HTTP_INTERNAL_ERROR, "Error during directory creation (" + targetDirectory.getAbsolutePath() + ")");
             }
         }
         
         albumDao.save(album);
         return renderJSON(album);
     }
     
     public Render deleteAlbum(String albumId) throws SQLException {
         Album album = albumDao.getAlbum(albumId);
         if (album == null) {
             return renderError(HttpURLConnection.HTTP_NOT_FOUND, "There is not any album with id " + albumId);
         }
         
         String originalDirectory = ImageUtils.getSourcePath(album);
         String workingDirectory = ImageUtils.getTargetPath(album);
         if (!FileUtils.deleteQuietly(new File(workingDirectory)) || !FileUtils.deleteQuietly(new File(originalDirectory))) {
             return renderError(HttpURLConnection.HTTP_INTERNAL_ERROR, "Unable to delete directories from file system.");
         }
 
         albumDao.delete(album);
         return renderStatus(HttpURLConnection.HTTP_NO_CONTENT);
     }
     
     public List<Album> albums(String parentId, String token) throws SQLException {
         boolean isAdministrator = SessionUtils.isAdministrator(SecurityUtils.getSubject());
         List<Album> albums;
         if (isAdministrator) {
             albums = albumDao.getAlbums(parentId);
         } else if (SessionUtils.isLogged(SecurityUtils.getSubject())) {
             albums = albumDao.getVisibleAlbumsForLoggedUser(parentId);
         } else {
             albums = albumDao.getVisibleAlbums(token, parentId);
         }
         return albums;
     }
 
     public Render getAlbums(String parentId, String token, String criteria) throws SQLException {
         Subject subject = SecurityUtils.getSubject();
         boolean isAdministrator = SessionUtils.isAdministrator(subject);
         List<Album> albums;
         if (isAdministrator && "all".equals(criteria)) {
             albums = albumDao.getAllAlbums();
         } else {
             albums = albums(parentId, token);
         }
         return renderJSON("albums", albums);
     }
 
     public Render getAlbum(String token, String id) throws IOException, SQLException, IllegalArgumentException, IOException, AuthenticationProviderException {
         Subject subject = SecurityUtils.getSubject();
         User user = (User) subject.getPrincipal();
         
         boolean isAdministrator = SessionUtils.isAdministrator(subject);
         Album album;
         boolean isLogged = SessionUtils.isLogged(subject);
         if (!isAdministrator && isLogged) {
             album = albumDao.getVisibleAlbumForLoggedUser(user.getId(), id);
         } else if (isAdministrator) {
             album = albumDao.getAlbum(id);
         } else {
             album = albumDao.getVisibleAlbum(token, id);
         }
         if (album == null) {
             return renderStatus(HttpURLConnection.HTTP_NOT_FOUND);
         }
 
         List<Token> tokens = null;
         List<Contact> contacts = new ArrayList<>();
         if (isAdministrator) {
             tokens = tokenDao.getAllTokenWithAccessToAlbum(album);
             List<ThirdPartyAccount> accounts = userDao.getThirdPartyAccounts(user);
             for (ThirdPartyAccount account : accounts) {
                 String thirdPartyToken = account.getToken();
                 if (thirdPartyToken != null) {
                     switch (account.getProviderId()) {
                         case "facebook":
                             DefaultFacebookClient client = new DefaultFacebookClient(thirdPartyToken);
                             Connection<com.restfb.types.User> myFriends = client.fetchConnection("me/friends", com.restfb.types.User.class);
                             contacts.addAll(convert(myFriends.getData()));
                             break;
                         case "google":
 //                            URL url = new URL("https://www.google.com/m8/feeds/contacts/default/full?access_token=" + thirdPartyToken);
 //                            SyndFeedInput input = new SyndFeedInput();
 //                            
 //                            do {
 //                                SyndFeed feed = input.build(new CustomXMLReader(url).getReader());
 //                                List<SyndLinkImpl> links = feed.getLinks();
 //                                url = null;
 //                                if (links != null) {
 //                                    for (SyndLinkImpl link : links) {
 //                                        if (link.getRel().equals("next")) {
 //                                            url = new URL(link.getHref() + "&access_token=" + thirdPartyToken);
 //                                            break;
 //                                        }
 //                                    }
 //                                }
 //                                
 //                                Iterator<SyndEntry> contactsIterator = feed.getEntries().iterator();
 //                                List<Contact> result = convert(contactsIterator);
 //                                contacts.addAll(result);
 //                                
 //                            } while (url != null);
 //                            break;
                     }
                 }
             }
             Collections.sort(contacts);
             
             List<ThirdPartyAccount> authorized = userDao.getAuthorizedThirdPartyAccounts(album);
             for (Contact contact : contacts) {
                 for (ThirdPartyAccount account : authorized) {
                     if (contact.getProvider().getId().equals(account.getProviderId()) && contact.getId().equals(account.getProviderAccountId())) {
                         contact.setAuthorized(true);
                     }
                 }
             }
         }
         
         List<Album> subAlbums = this.albums(album.getId(), token);
         List<Photo> photos = photoDao.getPhotos(id, token);
         List<Video> videos = videoDao.getVideos(id, token);
         List<Datable> medias = new ArrayList<>(photos.size() + videos.size());
         medias.addAll(photos);
         medias.addAll(videos);
         
         Collections.sort(medias, new DatableComparator());
         
         Album parent = albumDao.getAlbum(album.getParentId());
         List<Comment> comments = null;
         if (isAdministrator || isLogged) {
             comments = commentDao.getByAlbum(album.getId());
         }
         
         return renderJSON("album", album, "albumParent", parent,
                 "subAlbums", subAlbums, "medias", medias,
                 "regeneration", getRegenerationData(), "tokens", tokens, "contacts", contacts, "comments", comments);
     }
     
 //    private List<Contact> convert(Iterator<SyndEntry> contactsIterator) {
 //        List<Contact> list = new ArrayList<>();
 //        while (contactsIterator.hasNext()) {
 //            SyndEntry entry = contactsIterator.next();
 //            
 //            Contact contact = new Contact();
 //            contact.setName(entry.getTitle());
 //            contact.setProvider(ServiceUtil.getProvider("google"));
 //            
 //            List<Element> elements = (List<Element>) entry.getForeignMarkup();
 //            for (Element element : elements) {
 //                String prefix = element.getNamespacePrefix();
 //                String name = element.getName();
 //                if ("gd".equals(prefix) && "email".equals(name)) {
 //                    String mail = element.getAttributeValue("address");
 //                    contact.setId(mail);
 //                    if (StringUtils.isEmpty(contact.getName())) {
 //                        contact.setName(mail);
 //                    }
 //                    break;
 //                }
 //            }
 //            if (StringUtils.isNotEmpty(contact.getId())) {
 //                list.add(contact);
 //            }
 //        }
 //        return list;
 //    }
     
     public List<Contact> convert(List<com.restfb.types.User> list) {
         if (list == null) {
             return null;
         }
         List<Contact> result = new ArrayList<>(list.size());
         for (com.restfb.types.User user : list) {
             Contact contact = new Contact();
             contact.setId(user.getId());
             contact.setProvider(ServiceUtil.getProvider("facebook"));
             contact.setName(user.getName());
             result.add(contact);
         }
         return result;
     }
     
     public Render editAlbum(String albumId, String name, String description, String visibility, boolean downloadable, List<String> authorizedTokens) throws SQLException, IOException, IllegalArgumentException, AuthenticationProviderException {
         Album album = albumDao.getAlbum(albumId);
         if (album == null) {
             return renderStatus(HttpURLConnection.HTTP_NOT_FOUND);
         }
         album.setName(name);
         album.setDescription(description);
         album.setPublic(Boolean.parseBoolean(visibility));
         album.setDownloadable(downloadable);
 
         albumDao.save(album);
         
         if (authorizedTokens != null) {
             List<Token> tokens = tokenDao.getAll(album.getOwnerId());
             for (Token token : tokens) {
                 if (authorizedTokens.contains(token.getId())) {
                     addParentAlbumsToToken(album, token);
                 } else {
                     removeChildAlbumToToken(album, token);
                 }
             }
             tokenDao.saveAll(tokens);
             
             // TODO There is a bug here, the case where we unautorize 
             // a thirdparty account for a parent album is not handled
             // (children albums are not unautorized)
             String currentAlbumId = album.getId();
             while (currentAlbumId != null) {
                 List<ThirdPartyAccount> accounts = new ArrayList<>();
                 for (String thirdPartyId : authorizedTokens) {
                     String providerId = StringUtils.substringBefore(thirdPartyId, "-");
                     Provider provider = ServiceUtil.getProvider(providerId);
                     if (provider == null) {
                         continue;
                     }
 
                     String providerAccountId = StringUtils.substringAfter(thirdPartyId, "-");
 
                     ThirdPartyAccount account = userDao.getUser(provider.getId(), providerAccountId);
                     if (account == null) {
                         account = new ThirdPartyAccount(provider, providerAccountId, null);
                         userDao.save(account);
                     }
                     accounts.add(account);
                 }
                 userDao.saveAccess(accounts, currentAlbumId);
                 
                 Album tmp = albumDao.getAlbum(currentAlbumId);
                 currentAlbumId = tmp.getParentId();
             }
         }
         
         return getAlbum(null, albumId);
     }
     
     protected void addParentAlbumsToToken(Album album, Token token) throws SQLException {
         if (album != null && !token.getAlbums().contains(album)) {
             token.getAlbums().add(album);
             addParentAlbumsToToken(albumDao.getAlbum(album.getParentId()), token);
         }
     }
     
     protected void removeChildAlbumToToken(Album album, Token token) throws SQLException {
         if (album != null && token.getAlbums().contains(album)) {
             List<Album> children = albumDao.getAlbums(album.getId());
             for (Album child : children) {
                 removeChildAlbumToToken(child, token);
             }
             token.getAlbums().remove(album);
         }
     }
 
     public Render setAlbumCover(String albumId, String objectId) throws SQLException, IOException {
        boolean emptyId = StringUtils.isEmpty(objectId);
         boolean isPhoto = !emptyId && photoDao.getPhoto(objectId) != null;
         boolean isVideo = !emptyId && videoDao.getVideo(objectId) != null;
         boolean isSubAlbum = !emptyId && objectId.startsWith("a.") && albumDao.getAlbumCover(objectId.substring(2)) != null;
         
         if (!isPhoto && !isVideo && !isSubAlbum) {
             return renderError(HttpURLConnection.HTTP_INTERNAL_ERROR, "The objectId parameter must correspond with a valid media.");
         }
         
         String id;
         if (objectId.startsWith("a.")) {
             Media photo = albumDao.getAlbumCover(objectId.substring(2));
             id = photo.getId();
         } else {
             id = objectId;
         }
         
         photoDao.saveThumbnailGenerationTime("a." + albumId, ThumbnailSize.SQUARE, new Date().getTime());
         albumDao.setAlbumCover(albumId, id);
         return renderStatus(HttpURLConnection.HTTP_NO_CONTENT);
     }
 
     public Render getAlbumCover(String token, String albumId) throws SQLException, IOException {
         albumId = StringUtils.substringBeforeLast(albumId, "-cover.jpg");
         
         Media media;
         if (SessionUtils.isAdministrator(SecurityUtils.getSubject())) {
             media = albumDao.getAlbumCover(albumId);
         } else {
             media = albumDao.getVisibleAlbumCover(token, albumId);
         }
 
         Album album = albumDao.getAlbum(albumId);
         if (album == null) {
             return renderError(HttpURLConnection.HTTP_NOT_FOUND, "");
         } else if (media == null) {
             return renderRedirect("/img/default_album.png");
         }
         
         FileInputStream fis = null;
         try {
             fis = ImageUtils.getStream(media, ThumbnailSize.SQUARE);
             
         } catch (Exception ex) {
             logger.error("Unable to get stream", ex);
         }
         if (fis == null) {
             logger.error("Errror, stream is null for the photo " + media.getFilename());
             return renderRedirect("/img/default_album.png");
         }
         RenderStatus status = handleLastModifiedHeader(album);
         if (status.getCode() == HttpURLConnection.HTTP_NOT_MODIFIED) {
             return status;
         }
         return renderStream(fis, "image/jpeg");
     }
 
     public Render download(String token, String albumId, boolean resized) throws SQLException {
         Subject subject = SecurityUtils.getSubject();
         boolean isAdministrator = SessionUtils.isAdministrator(subject);
         
         Album album;
         if (!isAdministrator && SessionUtils.isLogged(subject)) {
             User user = (User) subject.getPrincipal();
             album = albumDao.getVisibleAlbumForLoggedUser(user.getId(), albumId);
         } else if (isAdministrator) {
             album = albumDao.getAlbum(albumId);
         } else {
             album = albumDao.getVisibleAlbum(token, albumId);
         }
         
         if (album == null) {
             return renderStatus(HttpURLConnection.HTTP_NOT_FOUND);
 
         } else if (!album.isDownloadable() && !isAdministrator) {
             return renderStatus(HttpURLConnection.HTTP_FORBIDDEN);
         }
 
         if (resized) {
             List<Photo> photos = photoDao.getPhotos(album.getId());
             Map<String, String> names = new HashMap<>(photos.size());
             for (Photo photo : photos) {
                 names.put(ThumbnailSize.LARGE.getPrefix() + photo.getFilename(), ThumbnailSize.LARGE.getPrefix() + photo.getFilename());
             }
             return new ZipDownloadRenderer(ImageUtils.getTargetPath(album), album.getName(), names);
         }
         return new ZipDownloadRenderer(ImageUtils.getSourcePath(album), album.getName());
     }
 
     public Render regenerateThumbnails(String albumId) throws SQLException {
         Album album = albumDao.getAlbum(albumId);
         if (album == null) {
             return renderStatus(HttpURLConnection.HTTP_NOT_FOUND);
         }
 
         String strSource = ImageUtils.getSourcePath(album);
         String strTarget = ImageUtils.getTargetPath(album);
         Path source = Paths.get(strSource);
         Path target = Paths.get(strTarget);
 
         if (regenerateThumbnailsJob != null && !regenerateThumbnailsJob.isTerminated()) {
             logger.warn("Cannot launch process, it is already running");
         } else {
             if (regenerateThumbnailsJob == null) {
                 regenerateThumbnailsJob = new RegenerateThumbnailsJob(source, target);
 
             } else if (!regenerateThumbnailsJob.getSource().equals(source) || !regenerateThumbnailsJob.getTarget().equals(target)) {
                 logger.warn("Aborting sync between {} and {}", regenerateThumbnailsJob.getSource(), regenerateThumbnailsJob.getTarget());
                 regenerateThumbnailsJob.abort();
                 regenerateThumbnailsJob.setSource(source);
                 regenerateThumbnailsJob.setTarget(target);
             }
 
             threadPool.execute(regenerateThumbnailsJob);
         }
 
         return renderStatus(HttpURLConnection.HTTP_NO_CONTENT);
     }
     
     public Render getRegenerationProgress() throws SQLException {
         if (regenerateThumbnailsJob == null) {
             return renderStatus(404);
         }
         return renderJSON(getRegenerationData());
     }
     
     protected Map<String, Long> getRegenerationData() throws SQLException {
         if (regenerateThumbnailsJob == null) {
             return null;
         }
         
         long total = regenerateThumbnailsJob.getNumberToProcess();
         long current = regenerateThumbnailsJob.getNumberProcessed();
         Map<String, Long> regeneration = new HashMap<>();
         regeneration.put("total", total);
         regeneration.put("current", current);
         if (total == 0L && regenerateThumbnailsJob.isTerminated()) {
             regeneration.put("percent", 100L);
             regenerateThumbnailsJob = null;
         } else if (total == 0L && !regenerateThumbnailsJob.isTerminated()) {
             regeneration.put("percent", 0L);
         } else {
             Long percent = Double.valueOf(Math.floor(current * 100 / total)).longValue();
             regeneration.put("percent", percent);
             if (percent == 100L) {
                 regenerateThumbnailsJob = null;
             }
         }
         return regeneration;
     }
 
 }
