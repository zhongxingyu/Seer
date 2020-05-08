 
 package com.teamluper.luper.rest;
 
 import com.googlecode.androidannotations.annotations.rest.Get;
 import com.googlecode.androidannotations.annotations.rest.Post;
 import com.googlecode.androidannotations.annotations.rest.Rest;
 import org.springframework.http.converter.StringHttpMessageConverter;
 
 // LuperRestClient.java
 // ====================
 
 // This is a very bare-bones interface which simply defines the expected requests and responses for each
 // HTTP API endpoint, in the form of parameter types and return types, each of which is annotated with
 // @Get or @Post and the URL of the corresponding endpoint.
 // (the other end of this connection is defined in the PHP code at luper-web/api/api.php)
 
 // To read more about the REST style of Web API architecture, see:
 // https://en.wikipedia.org/wiki/Representational_state_transfer
 
 // At compile time, the AndroidAnnotations processor script generates a class which implements this one,
 // and its source is automatically filled in (writing it by hand would be very repetitive).
 // this is the beauty of AndroidAnnotations... without it, this file would be 20 times larger.
 
 // To read more about the AndroidAnnotations magic that's going on here, see:
 // https://github.com/excilys/androidannotations/wiki/Rest%20API
 
 // We are currently hosting the luper-web code and the MySQL database at TeamLuper.com.
 // the rootUrl value in the @Rest annotation should be changed if the server is ever relocated.
 
 @Rest(rootUrl = "http://www.teamluper.com", converters = StringHttpMessageConverter.class)
 public interface LuperRestClient {
 
   @Get("/api/null")
   public abstract void main();
 
   @Get("/api/test")
   String getTestString();
 
   @Post("/api/auth-register")
   String registerNewAccount(String requestJSON);
   // requestJSON must contain: email, password, username
 
   @Post("/api/auth-challenge")
   String getLoginChallengeSalt(String requestJSON);
   // requestJSON must contain: email
 
   @Post("/api/auth-login")
   String validateLoginAttempt(String requestJSON);
   // requestJSON must contain: email, attemptHash
 
   @Post("/api/auth-passwd")
   String changeUserPassword(String requestJSON);
   // requestJSON must contain: email, oldPasswordAttemptHash, newPasswordHash
 
  @Get("/api/fetch-user/{email}")
   String fetchUserByEmail(String email);
 
 }
