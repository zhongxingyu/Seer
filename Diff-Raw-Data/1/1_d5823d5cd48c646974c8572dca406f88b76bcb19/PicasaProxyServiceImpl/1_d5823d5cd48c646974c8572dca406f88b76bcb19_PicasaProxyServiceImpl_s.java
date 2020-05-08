 package de.graind.server;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.LinkedList;
 import java.util.List;
 
 import com.google.gdata.client.photos.PicasawebService;
 import com.google.gdata.data.media.mediarss.MediaThumbnail;
 import com.google.gdata.data.photos.AlbumEntry;
 import com.google.gdata.data.photos.AlbumFeed;
 import com.google.gdata.data.photos.PhotoEntry;
 import com.google.gdata.data.photos.UserFeed;
 import com.google.gdata.util.ServiceException;
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 
 import de.graind.client.model.PicasaAlbum;
 import de.graind.client.model.PicasaImage;
 import de.graind.client.model.Thumbnail;
 import de.graind.client.service.PicasaProxyService;
 import de.graind.shared.Config;
 
 public class PicasaProxyServiceImpl extends RemoteServiceServlet implements PicasaProxyService {
 
   private static final long serialVersionUID = 1L;
   private PicasawebService service;
 
   public static final String PICASA_URI = "https://picasaweb.google.com/data/feed/api/user/default";
   public static final String PICASA_ALBUMS_URI = PICASA_URI + "?kind=album&access=public";
   public static final String PICASA_PICTURES_BASE_URI = PICASA_URI + "/albumid/";
 
   public PicasaProxyServiceImpl() {
     this.service = new PicasawebService(Config.APPLICATION_NAME);
   }
 
   @Override
   public List<PicasaAlbum> getAlbums(String token) {
     List<PicasaAlbum> ret = new LinkedList<PicasaAlbum>();
 
     service.setAuthSubToken(token);
 
     try {
       URL feedUrl = new URL(PICASA_ALBUMS_URI);
       UserFeed userFeed = service.getFeed(feedUrl, UserFeed.class);
 
       for (AlbumEntry currentAlbum : userFeed.getAlbumEntries()) {
         PicasaAlbum tmp = new PicasaAlbum();
         String[] albumIdSplit = currentAlbum.getId().split("/");
         tmp.setId(albumIdSplit[albumIdSplit.length - 1]);
         tmp.setContentUrl(currentAlbum.getMediaContents().get(0).getUrl());
         tmp.setCreatorNickname(currentAlbum.getNickname());
         tmp.setCreatorUsername(currentAlbum.getUsername());
         tmp.setPhotoCount(currentAlbum.getPhotosUsed());
         tmp.setTitle(currentAlbum.getTitle().getPlainText());
 
         Thumbnail thumbnail = new Thumbnail();
 
         MediaThumbnail mediaThumb = currentAlbum.getMediaThumbnails().get(0);
         thumbnail.setHeight(mediaThumb.getHeight());
         thumbnail.setWidth(mediaThumb.getWidth());
         thumbnail.setUrl(mediaThumb.getUrl());
 
         tmp.setThumbnail(thumbnail);
         ret.add(tmp);
       }
     } catch (MalformedURLException e) {
       // TODO Auto-generated catch block
       e.printStackTrace();
     } catch (IOException e) {
       // TODO Auto-generated catch block
       e.printStackTrace();
     } catch (ServiceException e) {
       // TODO Auto-generated catch block
       e.printStackTrace();
     }
 
     return ret;
   }
 
   @Override
   public List<PicasaImage> getImages(String albumId, String token) {
     List<PicasaImage> ret = new LinkedList<PicasaImage>();
 
     service.setAuthSubToken(token);
 
     try {
       URL feedUrl = new URL(PICASA_PICTURES_BASE_URI + albumId + "?imgmax=d");
 
       AlbumFeed feed = service.getFeed(feedUrl, AlbumFeed.class);
 
       for (PhotoEntry photo : feed.getPhotoEntries()) {
         ret.add(fillImage(photo));
       }
     } catch (MalformedURLException e) {
       // TODO Auto-generated catch block
       e.printStackTrace();
     } catch (IOException e) {
       // TODO Auto-generated catch block
       e.printStackTrace();
     } catch (ServiceException e) {
       // TODO Auto-generated catch block
       e.printStackTrace();
     }
 
     return ret;
   }
 
   @Override
   public List<PicasaImage> getRecentImages(String token) {
     List<PicasaImage> ret = new LinkedList<PicasaImage>();
 
     service.setAuthSubToken(token);
 
     try {
       URL feedUrl = new URL(PICASA_URI + "?kind=photo&imgmax=d");
 
       AlbumFeed feed = service.getFeed(feedUrl, AlbumFeed.class);
 
       for (PhotoEntry photo : feed.getPhotoEntries()) {
         ret.add(fillImage(photo));
       }
     } catch (MalformedURLException e) {
       // TODO Auto-generated catch block
       e.printStackTrace();
     } catch (IOException e) {
       // TODO Auto-generated catch block
       e.printStackTrace();
     } catch (ServiceException e) {
       // TODO Auto-generated catch block
       e.printStackTrace();
     }
 
     return ret;
   }
 
   private PicasaImage fillImage(PhotoEntry photo) throws ServiceException {
     PicasaImage ret = new PicasaImage();
     ret.setAlbumId(photo.getAlbumId());
     ret.setId(photo.getId());
     ret.setTitle(photo.getTitle().getPlainText());
     ret.setUrl(photo.getMediaGroup().getContents().get(0).getUrl());
     ret.setTimestamp(photo.getTimestamp());
     ret.setWidth(photo.getWidth());
     ret.setHeight(photo.getHeight());
 
     List<Thumbnail> thumbnails = new LinkedList<Thumbnail>();
     List<MediaThumbnail> photoThumbnails = photo.getMediaThumbnails();
     for (MediaThumbnail mediaThumbnail : photoThumbnails) {
       Thumbnail thumb = new Thumbnail();
       thumb.setHeight(mediaThumbnail.getHeight());
       thumb.setWidth(mediaThumbnail.getWidth());
       thumb.setUrl(mediaThumbnail.getUrl());
     }
 
     ret.setThumbnails(thumbnails);
 
     return ret;
   }
 }
