 /*
  *   Copyright 2010, Maarten Billemont
  *
  *   Licensed under the Apache License, Version 2.0 (the "License");
  *   you may not use this file except in compliance with the License.
  *   You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  *   Unless required by applicable law or agreed to in writing, software
  *   distributed under the License is distributed on an "AS IS" BASIS,
  *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *   See the License for the specific language governing permissions and
  *   limitations under the License.
  */
 package com.lyndir.lhunath.snaplog.webapp.servlet;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 
 import com.google.inject.Inject;
 import com.lyndir.lhunath.lib.system.logging.Logger;
 import com.lyndir.lhunath.snaplog.data.media.Album;
 import com.lyndir.lhunath.snaplog.data.media.Media;
 import com.lyndir.lhunath.snaplog.data.media.Media.Quality;
 import com.lyndir.lhunath.snaplog.data.security.PermissionDeniedException;
 import com.lyndir.lhunath.snaplog.data.security.SecurityToken;
 import com.lyndir.lhunath.snaplog.data.user.User;
 import com.lyndir.lhunath.snaplog.model.AlbumService;
 import com.lyndir.lhunath.snaplog.model.UserService;
 import com.lyndir.lhunath.snaplog.util.URLUtils;
 import com.lyndir.lhunath.snaplog.webapp.SnaplogSession;
 
 
 /**
  * <h2>{@link ImageServlet}<br>
  * <sub>[in short] (TODO).</sub></h2>
  *
  * <p>
  * <i>Jan 19, 2010</i>
  * </p>
  *
  * @author lhunath
  */
 public class ImageServlet extends HttpServlet {
 
     final Logger logger = Logger.get( ImageServlet.class );
 
     /**
      * Context-relative path of this servlet.
      */
     public static final String PATH = "/img";
 
     private static final String PARAM_USER = "u";
     private static final String PARAM_ALBUM = "a";
     private static final String PARAM_MEDIA = "m";
     private static final String PARAM_QUALITY = "q";
 
     private final UserService userService;
     private final AlbumService albumService;
 
 
     /**
      * @param userService  See {@link UserService}
      * @param albumService See {@link AlbumService}
      */
     @Inject
     public ImageServlet(final UserService userService, final AlbumService albumService) {
 
         this.userService = userService;
         this.albumService = albumService;
     }
 
     /**
      * Obtain a context-relative path to the {@link ImageServlet} such that it will render the given media at the given
      * quality.
      *
      * @param media   The media that should be shown at the given URL.
      * @param quality The quality to show the media at.
      *
      * @return A context-relative URL.
      */
     public static String getContextRelativePathFor(final Media media, final Quality quality) {
 
         checkNotNull( media, "Given media must not be null." );
         checkNotNull( quality, "Given quality must not be null." );
 
         Album album = media.getAlbum();
         User user = album.getOwnerProfile().getUser();
 
         StringBuilder path = new StringBuilder( PATH ).append( '?' );
         path.append( PARAM_USER ).append( '=' ).append( URLUtils.encode( user.getUserName() ) ).append( '&' );
         path.append( PARAM_ALBUM ).append( '=' ).append( URLUtils.encode( album.getName() ) ).append( '&' );
         path.append( PARAM_MEDIA ).append( '=' ).append( URLUtils.encode( media.getName() ) ).append( '&' );
         path.append( PARAM_QUALITY ).append( '=' ).append( URLUtils.encode( quality.getName() ) ).append( '&' );
 
         return path.substring( 0, path.length() - 1 );
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected void doGet(final HttpServletRequest req, final HttpServletResponse resp)
             throws ServletException, IOException {
 
         try {
             String userName = req.getParameter( PARAM_USER );
             String albumName = req.getParameter( PARAM_ALBUM );
             String mediaName = req.getParameter( PARAM_MEDIA );
             String qualityName = req.getParameter( PARAM_QUALITY );
             SecurityToken token = SnaplogSession.get().newToken();
 
             logger.dbg( "Getting image for media: %s, in album: %s, of user: %s, at quality: %s", //
                         mediaName, albumName, userName, qualityName );
             User user = checkNotNull( userService.findUserWithUserName( userName ), //
                                       "No user named: %s.", userName );
             Album album = checkNotNull( albumService.findAlbumWithName( token, user, albumName ), //
                                         "User: %s, has no album named: %s.", user, albumName );
             Media media = checkNotNull( albumService.findMediaWithName( token, album, mediaName ), //
                                         "Album: %s, has no media named: %s.", album, mediaName );
            Quality quality = checkNotNull( Quality.findQualityWithName( qualityName ) );
             logger.dbg( "Resolved image request for: %s, in: %s, of: %s, at: %s", //
                         media, album, user, quality );
 
             resp.sendRedirect( albumService.getResourceURL( token, media, quality ).toExternalForm() );
         }
 
         catch (PermissionDeniedException e) {
             resp.sendError( HttpServletResponse.SC_FORBIDDEN, e.getLocalizedMessage() );
         }
     }
 }
