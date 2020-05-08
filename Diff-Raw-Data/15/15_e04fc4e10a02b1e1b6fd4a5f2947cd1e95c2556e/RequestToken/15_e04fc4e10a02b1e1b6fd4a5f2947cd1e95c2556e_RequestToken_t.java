 /*  copyit-server
  *  Copyright (C) 2013  Toon Schoenmakers
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package net.mms_projects.copy_it.api.http.pages.oauth;
 
 import io.netty.buffer.Unpooled;
 import io.netty.handler.codec.http.DefaultFullHttpResponse;
 import io.netty.handler.codec.http.FullHttpResponse;
 import io.netty.handler.codec.http.HttpRequest;
 import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
 import io.netty.util.CharsetUtil;
 import net.mms_projects.copy_it.api.http.Page;
 import net.mms_projects.copy_it.api.http.pages.exceptions.ErrorException;
 import net.mms_projects.copy_it.api.oauth.HeaderVerifier;
 import net.mms_projects.copy_it.server.database.Database;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 
 import static io.netty.handler.codec.http.HttpResponseStatus.OK;
 
 public class RequestToken extends Page {
     private static final String CREATE_TOKENS = "INSERT INTO request_tokens (application_id, callback_uri) VALUES (?, ?)";
     private static final String GET_LATEST_TOKEN = "SELECT public_key, secret_key " +
                                                    "FROM request_tokens " +
                                                    "WHERE aid = LAST_INSERT_ID()";
 
     private static final String OAUTH_TOKEN = "oauth_token=";
     private static final String OAUTH_TOKEN_SECRET = "&oauth_token_secret=";
     private static final String OAUTH_CALLBACK_CONFIRMED = "&oauth_callback_confirmed=true";
     private static final String PUBLIC_KEY = "public_key";
     private static final String SECRET_KEY = "secret_key";
 
     private static final String MISSING_OAUTH_CALLBACK = "Missing OAuth callback uri.";
     private static final String INVALID_CALLBACK_URI = "Invalid callback uri.";
     private static final String URI_TOO_LONG = "Your callback uri is too long (longer than 1024 characters).";
 
    public FullHttpResponse onPostRequest(HttpRequest request, HttpPostRequestDecoder postRequestDecoder, Database database) throws Exception {
         URI uri = new URI(request.getUri());
         HeaderVerifier headerVerifier = new HeaderVerifier(request, uri, HeaderVerifier.Flags.MAY_MISS_TOKEN);
         String callback = headerVerifier.getCallbackUri();
         if (callback == null)
             throw new ErrorException(MISSING_OAUTH_CALLBACK);
         try {
             new URI(callback);
         } catch (URISyntaxException e) {
             throw new ErrorException(INVALID_CALLBACK_URI);
         }
         if (callback.length() > 1024)
             throw new ErrorException(URI_TOO_LONG);
         headerVerifier.verifyConsumer(database);
         PreparedStatement statement = database.getConnection().prepareStatement(CREATE_TOKENS);
         statement.setInt(1, headerVerifier.getConsumerId());
         statement.setString(2, callback);
         if (statement.executeUpdate() == 1) {
             statement = database.getConnection().prepareStatement(GET_LATEST_TOKEN);
             ResultSet result = statement.executeQuery();
             if (result.first()) {
                 StringBuilder builder = new StringBuilder(190);
                 builder.append(OAUTH_TOKEN);
                 builder.append(result.getString(PUBLIC_KEY));
                 builder.append(OAUTH_TOKEN_SECRET);
                 builder.append(result.getString(SECRET_KEY));
                 builder.append(OAUTH_CALLBACK_CONFIRMED);
                 result.close();
                 return new DefaultFullHttpResponse(request.getProtocolVersion()
                         ,OK, Unpooled.copiedBuffer(builder.toString(), CharsetUtil.UTF_8));
             }
             result.close();
         }
         return null;
     }
 
    public FullHttpResponse onGetRequest(HttpRequest request, Database database) throws Exception {
         throw new UnsupportedOperationException();
     }
 
     public String GetContentType() {
         return ContentTypes.PLAIN_TEXT;
     }
 }
