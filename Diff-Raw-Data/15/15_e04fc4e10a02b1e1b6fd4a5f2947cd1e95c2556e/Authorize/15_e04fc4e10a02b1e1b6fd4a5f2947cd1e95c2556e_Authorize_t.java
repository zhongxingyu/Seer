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
 import io.netty.handler.codec.http.HttpHeaders;
 import io.netty.handler.codec.http.HttpRequest;
 import io.netty.handler.codec.http.QueryStringDecoder;
 import io.netty.handler.codec.http.multipart.HttpData;
 import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
 import io.netty.handler.codec.http.multipart.InterfaceHttpData;
 import io.netty.util.CharsetUtil;
 import net.mms_projects.copy_it.api.http.Page;
 import net.mms_projects.copy_it.api.http.pages.exceptions.ErrorException;
 import net.mms_projects.copy_it.server.database.Database;
 
 import java.net.URI;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.util.List;
 import java.util.Map;
 
 import static io.netty.handler.codec.http.HttpResponseStatus.MOVED_PERMANENTLY;
 import static io.netty.handler.codec.http.HttpResponseStatus.OK;
 import static java.lang.Integer.parseInt;
 
 public class Authorize extends Page {
     private static final String USER_ID = "user_id";
     private static final String OAUTH_TOKEN = "oauth_token";
 
     private static final String MISSING_OAUTH_TOKEN = "We're missing an OAuth token here..";
     private static final String MISSING_USER_ID = "We're missing an user id here..";
 
    public FullHttpResponse onGetRequest(HttpRequest request, Database database) throws Exception {
         final QueryStringDecoder querydecoder = new QueryStringDecoder(new URI(request.getUri()));
         Map<String, List<String>> parameters = querydecoder.parameters();
         if (!parameters.containsKey(OAUTH_TOKEN))
             throw new ErrorException(MISSING_OAUTH_TOKEN);
         String output = "<html><form method=\"post\">\n" +
                 "user_id: <input type=\"text\" name=\"user_id\"><br>\n" +
                 "<input type=\"submit\" value=\"Submit\">\n" +
                 "</form></html>"; //TODO Allow this to be an external html file
         return new DefaultFullHttpResponse(request.getProtocolVersion(), OK, Unpooled.copiedBuffer(output, CharsetUtil.UTF_8));
     }
 
     private static final String CALLBACK_URI = "callback_uri";
     private static final String VERIFIER = "verifier";
     private static final String CALLBACK_URI_PARAMETER = "?oauth_verifier=";
 
     private static final String INVALID_TOKEN = "Invalid token..";
     private static final String USER_ID_NOT_A_NUMBER = "The specified user_id is not a number";
     private static final String WE_MADE_A_BOO_BOO = "We made a boo boo.";
 
     private static final String SELECT_CALLBACK = "SELECT callback_uri, verifier " +
                                                   "FROM request_tokens " +
                                                   "WHERE public_key = ? " +
                                                   "AND (NOW() - INTERVAL 5 MINUTE) < timestamp";
 
     private static final String UPDATE_USER_ID = "UPDATE request_tokens " +
                                                  "SET user_id = ? " +
                                                  "WHERE public_key = ? ";
 
    public FullHttpResponse onPostRequest(HttpRequest request, HttpPostRequestDecoder postRequestDecoder, Database database) throws Exception {
         final QueryStringDecoder querydecoder = new QueryStringDecoder(new URI(request.getUri()));
         Map<String, List<String>> parameters = querydecoder.parameters();
         if (!parameters.containsKey(OAUTH_TOKEN) || parameters.get(OAUTH_TOKEN).size() != 1)
             throw new ErrorException(MISSING_OAUTH_TOKEN);
         if (postRequestDecoder.getBodyHttpData(USER_ID) == null)
             throw new ErrorException(MISSING_USER_ID);
         try {
             InterfaceHttpData user_id_raw = postRequestDecoder.getBodyHttpData(USER_ID);
             if (user_id_raw instanceof HttpData) {
                 int user_id = parseInt(((HttpData) user_id_raw).getString());
                 PreparedStatement update = database.getConnection().prepareStatement(UPDATE_USER_ID);
                 update.setInt(1, user_id);
                 update.setString(2, parameters.get(OAUTH_TOKEN).get(0));
                 if (update.executeUpdate() == 1) {
                     PreparedStatement statement = database.getConnection().prepareStatement(SELECT_CALLBACK);
                     statement.setString(1, parameters.get(OAUTH_TOKEN).get(0));
                     ResultSet result = statement.executeQuery();
                     if (result.first()) {
                         final String callback = result.getString(CALLBACK_URI);
                         final String verifier = result.getString(VERIFIER);
                         result.close();
                         final StringBuilder output = new StringBuilder();
                         output.append(callback);
                         output.append(CALLBACK_URI_PARAMETER);
                         output.append(verifier);
                         DefaultFullHttpResponse response = new DefaultFullHttpResponse(request.getProtocolVersion(), MOVED_PERMANENTLY);
                         HttpHeaders.addHeader(response, HttpHeaders.Names.LOCATION, output.toString());
                         return response;
                     } else {
                         result.close();
                         throw new ErrorException(INVALID_TOKEN);
                     }
                 }
                 throw new ErrorException(WE_MADE_A_BOO_BOO);
             } else
                 throw new ErrorException(USER_ID_NOT_A_NUMBER);
         } catch (NumberFormatException e) {
             throw new ErrorException(USER_ID_NOT_A_NUMBER);
         }
     }
 
     public String GetContentType() {
         return ContentTypes.PLAIN_HTML;
     }
 }
