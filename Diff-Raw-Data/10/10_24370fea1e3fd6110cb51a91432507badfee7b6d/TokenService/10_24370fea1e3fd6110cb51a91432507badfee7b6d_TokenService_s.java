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
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.HttpURLConnection;
 import java.net.URLDecoder;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 import org.apache.shiro.SecurityUtils;
 import org.debox.photo.dao.AlbumDao;
 import org.debox.photo.dao.TokenDao;
 import org.debox.photo.dao.UserDao;
 import org.debox.photo.model.Album;
 import org.debox.photo.model.user.ThirdPartyAccount;
 import org.debox.photo.model.Token;
 import org.debox.photo.model.user.User;
 import org.debox.photo.thirdparty.ServiceUtil;
 import org.debox.photo.util.SessionUtils;
 import org.debox.photo.util.StringUtils;
 import org.debux.webmotion.server.render.Render;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * @author Corentin Guy <corentin.guy@debox.fr>
  */
 public class TokenService extends DeboxService {
     
     private static final Logger logger = LoggerFactory.getLogger(TokenService.class);
     protected static AlbumDao albumDao = new AlbumDao();
     protected static TokenDao tokenDao = new TokenDao();
     protected UserDao userDao = new UserDao();
     
     public Render createToken(String label) throws SQLException, UnsupportedEncodingException {
         Token token = new Token();
         token.setId(StringUtils.randomUUID());
         token.setLabel(URLDecoder.decode(label, "UTF-8"));
         token.setOwner(SessionUtils.getUser(SecurityUtils.getSubject()));
         
         tokenDao.save(token);
         return renderJSON(token);
     }
     
     public Render getTokens() throws SQLException, IOException {
         User user = (User) SecurityUtils.getSubject().getPrincipal();
         List<ThirdPartyAccount> accounts = userDao.getThirdPartyAccounts(user);
         return renderJSON(
                 "tokens", tokenDao.getAll(user.getId()),
                 "albums", albumDao.getAlbums(null),
                 "providers", ServiceUtil.getAuthenticationUrls(),
                 "accounts", accounts);
     }
     
     public Render getToken(String id) throws SQLException {
         Token token = tokenDao.getById(id);
         if (token == null) {
             return renderError(HttpURLConnection.HTTP_NOT_FOUND, "");
         }
         
         return renderJSON(
                 "albums", albumDao.getAlbums(null),
                 "token", token);
     }
     
     public Render editToken(String id, String label, List<String> albums, List<String> ignore) throws SQLException {
         Token token = tokenDao.getById(id);
         if (token == null) {
             return renderError(HttpURLConnection.HTTP_NOT_FOUND, "");
         }
         
         if (label != null) {
             token.setLabel(label);
         }
         if (albums != null || ignore != null) {
             List<Album> currentVisibleAlbums = new ArrayList<>(token.getAlbums());
             token.getAlbums().clear();
             if (albums != null) {
                 if (ignore != null) {
                     // Convert String list to Album list
                     List<Album> albumsToIgnore = new ArrayList<>(ignore.size());
                     for (String toIgnoreAlbumId : ignore) {
                         albumsToIgnore.add(albumDao.getAlbum(toIgnoreAlbumId));
                     }
                     
                     // Keep old visible subAlbums if their parent has to be ignored
                     for (Album visibleAlbum : currentVisibleAlbums) {
                         for (Album albumToIgnore : albumsToIgnore) {
                             if (visibleAlbum.isSubAlbum(albumToIgnore) && albums.contains(albumToIgnore.getId())) {
                                 token.getAlbums().add(visibleAlbum);
                             }
                         }
                     }
                 }
                 
                 for (String albumId : albums) {
                     Album album = albumDao.getAlbum(albumId);
                     token.getAlbums().add(album);
                 }
             }
         }
         
         tokenDao.save(token);
         return renderJSON(token);
     }
     
     public Render deleteToken(String id) throws SQLException {
         tokenDao.delete(id);
         return renderStatus(HttpURLConnection.HTTP_NO_CONTENT);
     }
     
     public Render reinitToken(String id) throws SQLException {
         Token token = tokenDao.getById(id);
         if (token == null) {
             return renderError(HttpURLConnection.HTTP_NOT_FOUND, "");
         }
         
         Token newToken = new Token();
         newToken.setLabel(token.getLabel());
         newToken.setAlbums(token.getAlbums());
         newToken.setId(StringUtils.randomUUID());
         
         tokenDao.save(newToken);
         tokenDao.delete(id);
         
         return renderJSON(newToken);
     }
 }
