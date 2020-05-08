 package com.jayway.angrytoday;
 
 import org.junit.Test;
 
 import static com.jayway.restassured.RestAssured.expect;
 import static com.jayway.restassured.RestAssured.given;
 import static org.hamcrest.CoreMatchers.equalTo;
 
 public class AngryTodayTest extends AbstractRunner {
 
     @Test
     public void createAngryPost() {
         String message = "SO VERY ANGRY";
 
         String location =
             given().
                     body( "\""+ message + "\"").
             expect().
                     statusCode(201).
             when().
                     post("/").header("Location");
 
         // use "Location" to read the post
         expect().
                 statusCode( 200 ).
                 body("message", equalTo( message )).
         when().
                 get(location + "description");
     }
 
 
     @Test
     public void listAngryPosts() {
         given().body( "\"A\"").expect().statusCode(201).when().post("/");
         given().body("\"B\"").expect().statusCode(201).when().post("/");
         given().body("\"C\"").expect().statusCode(201).when().post("/");
 
         expect().
                 statusCode( 200 ).
                 body("list[0].name", equalTo("C")).
                 body("list[1].name", equalTo("B")).
                 body("list[2].name", equalTo("A")).
                 body("list.size()", equalTo(3)).
         when().
                 get("/discover");
     }
 
     @Test
     public void comment() {
         String location = given().body( "\"A\"").expect().
                 statusCode(201).when().post("/").header("Location");
 
         String comment = "My comment";
         given().body("\"" + comment + "\"").
         expect().statusCode(200).
        when().post(location + "comment");
 
 
         expect().
                 body("comments[0]", equalTo( comment ) ).
         when().
                 get(location + "description");
     }
 
     @Test
     public void tag() {
         String location = given().body( "\"A\"").expect().
                 statusCode(201).when().post("/").header("Location");
 
         String tag = "tag";
         given().body( "\"" + tag + "\"").
         expect().statusCode( 200 ).
        when().post(location + "tag");
 
 
         expect().
                 body("tags[0]", equalTo( tag ) ).
         when().
                 get( location + "description");
 
     }
 
     @Test
     public void untag() {
         String location = given().body( "\"A\"").expect().
                 statusCode(201).when().post("/").header("Location");
 
         String tag = "tag";
         given().body( "\"" + tag + "\"").
         expect().statusCode( 200 ).
        when().post(location + "tag");
 
         expect().body("tags[0]", equalTo( tag ) ).
         when().get( location + "description");
 
         given().body( "\"" + tag + "\"").
         expect().statusCode(200).
        when().post( location + "untag" );
 
         expect().body("tags.size()", equalTo( 0 ) ).
         when().get( location + "description");
     }
 
 }
