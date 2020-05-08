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
 
 package net.mms_projects.copy_it.api.oauth;
 
 import io.netty.handler.codec.http.DefaultFullHttpRequest;
 import io.netty.handler.codec.http.HttpMethod;
 import io.netty.handler.codec.http.HttpRequest;
 import io.netty.handler.codec.http.HttpVersion;
 import net.mms_projects.copy_it.api.oauth.exceptions.OAuthException;
 import net.mms_projects.copy_it.server.config.Config;
 import net.mms_projects.copy_it.server.database.Database;
 import net.mms_projects.copy_it.server.database.DatabasePool;
 import net.mms_projects.copy_it.server.database.MySQL;
 import net.mms_projects.copy_it.server.database.OutOfConnectionsException;
 import org.junit.Test;
 
 import java.io.File;
 import java.sql.SQLException;
 
 import static io.netty.handler.codec.http.HttpHeaders.Names.AUTHORIZATION;
 
 /**
  * These tests assume you have imported test/test.sql
  */
 
 public class HeaderVerifierDatabaseTest {
     static {
         try {
             new Config(new File(System.getProperty("testConfigFile")));
             final int maxconnections = Config.getMaxConnectionsDatabasePool();
             new DatabasePool(MySQL.class, maxconnections);
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     @Test(timeout=1000)
     public void verifyConsumer() throws OAuthException, SQLException, OutOfConnectionsException {
         String header = "OAuth realm=\"\", " +
                 "oauth_consumer_key=\"401a131e03357df2a563fba48f98749448ed63d37e007f7353608cf81fa70a2d\", " +
                 "oauth_nonce=\"" + Utils.generateNonce() + "\", " +
                 "oauth_timestamp=\"" + Long.toString(System.currentTimeMillis()/1000) + "\", " +
                 "oauth_signature_method=\"HMAC-SHA1\", " +
                 "oauth_version=\"1.0\", " +
                 "oauth_token=\"oauth_token\", " +
                 "oauth_signature=\"CBTk%2FvzxEqqr0AvhnVgdWNHuKfw%3D\"";
         HttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "http://127.0.0.1:8080/");
         request.headers().add(AUTHORIZATION, header);
         HeaderVerifier headerVerifier = new HeaderVerifier(request);
         Database database = DatabasePool.getDBConnection();
         headerVerifier.verifyConsumer(database);
     }
 
     @Test(expected=OAuthException.class,timeout=1000)
     public void verifyInvalidConsumer() throws OAuthException, SQLException, OutOfConnectionsException {
         String header = "OAuth realm=\"\", " +
                 "oauth_consumer_key=\"This is a totally invalid consumer key\", " +
                 "oauth_nonce=\"" + Utils.generateNonce() + "\", " +
                 "oauth_timestamp=\"" + Long.toString(System.currentTimeMillis()/1000) + "\", " +
                 "oauth_signature_method=\"HMAC-SHA1\", " +
                 "oauth_version=\"1.0\", " +
                 "oauth_token=\"oauth_token\", " +
                 "oauth_signature=\"CBTk%2FvzxEqqr0AvhnVgdWNHuKfw%3D\"";
         HttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "http://127.0.0.1:8080/");
         request.headers().add(AUTHORIZATION, header);
         HeaderVerifier headerVerifier = new HeaderVerifier(request);
         Database database = DatabasePool.getDBConnection();
         headerVerifier.verifyConsumer(database);
     }
 
     @Test(timeout=1000)
     public void verifyUserToken() throws OAuthException, OutOfConnectionsException, SQLException {
         String header = "OAuth realm=\"\", " +
                 "oauth_consumer_key=\"401a131e03357df2a563fba48f98749448ed63d37e007f7353608cf81fa70a2d\", " +
                 "oauth_nonce=\"" + Utils.generateNonce() + "\", " +
                 "oauth_timestamp=\"" + Long.toString(System.currentTimeMillis()/1000) + "\", " +
                 "oauth_signature_method=\"HMAC-SHA1\", " +
                 "oauth_version=\"1.0\", " +
                 "oauth_token=\"9476f5130a07a7c0061de48bc19123f51636af704c5df369701960e0bc151255\", " +
                 "oauth_signature=\"CBTk%2FvzxEqqr0AvhnVgdWNHuKfw%3D\"";
         HttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "http://127.0.0.1:8080/");
         request.headers().add(AUTHORIZATION, header);
         HeaderVerifier headerVerifier = new HeaderVerifier(request);
         Database database = DatabasePool.getDBConnection();
         headerVerifier.verifyConsumer(database);
         headerVerifier.verifyOAuthToken(database);
     }
 
     @Test(expected=OAuthException.class,timeout=1000)
     public void verifyInvalidUserToken() throws OAuthException, OutOfConnectionsException, SQLException {
         String header = "OAuth realm=\"\", " +
                 "oauth_consumer_key=\"401a131e03357df2a563fba48f98749448ed63d37e007f7353608cf81fa70a2d\", " +
                 "oauth_nonce=\"" + Utils.generateNonce() + "\", " +
                 "oauth_timestamp=\"" + Long.toString(System.currentTimeMillis()/1000) + "\", " +
                 "oauth_signature_method=\"HMAC-SHA1\", " +
                 "oauth_version=\"1.0\", " +
                 "oauth_token=\"oauth_token\", " +
                 "oauth_signature=\"CBTk%2FvzxEqqr0AvhnVgdWNHuKfw%3D\"";
         HttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "http://127.0.0.1:8080/");
         request.headers().add(AUTHORIZATION, header);
         HeaderVerifier headerVerifier = new HeaderVerifier(request);
         Database database = DatabasePool.getDBConnection();
         headerVerifier.verifyConsumer(database);
         headerVerifier.verifyOAuthToken(database);
     }
 }
