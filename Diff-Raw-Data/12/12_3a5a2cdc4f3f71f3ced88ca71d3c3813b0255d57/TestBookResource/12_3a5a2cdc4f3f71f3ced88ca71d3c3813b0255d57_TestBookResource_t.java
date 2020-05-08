 package com.sismics.books.rest;
 
 import java.io.BufferedInputStream;
 import java.io.InputStream;
 
 import javax.ws.rs.core.MediaType;
 
 import junit.framework.Assert;
 
 import org.codehaus.jettison.json.JSONArray;
 import org.codehaus.jettison.json.JSONObject;
 import org.junit.Test;
 
 import com.google.common.io.ByteStreams;
 import com.sismics.books.rest.filter.CookieAuthenticationFilter;
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.ClientResponse.Status;
 import com.sun.jersey.api.client.WebResource;
 import com.sun.jersey.core.util.MultivaluedMapImpl;
 import com.sun.jersey.multipart.FormDataBodyPart;
 import com.sun.jersey.multipart.FormDataMultiPart;
 
 /**
  * Test the book resource.
  * 
  * @author bgamard
  */
 public class TestBookResource extends BaseJerseyTest {
     /**
      * Test the book resource.
      * 
      * @throws Exception 
      */
     @Test
     public void testBookResource() throws Exception {
        // Login admin
        String adminAuthenticationToken = clientUtil.login("admin", "admin", false);
        
        // Put a real key to avoid 400 error on Google API
        WebResource appResource = resource().path("/app");
        appResource.addFilter(new CookieAuthenticationFilter(adminAuthenticationToken));
        MultivaluedMapImpl postParams = new MultivaluedMapImpl();
        postParams.add("api_key_google", "AIzaSyC8CXDtLo-3ErzRF5rXTcAnLbtGjKLgUEE");
        
         // Login book1
         clientUtil.createUser("book1");
         String book1Token = clientUtil.login("book1");
         
         // Create a tag
         WebResource tagResource = resource().path("/tag");
         tagResource.addFilter(new CookieAuthenticationFilter(book1Token));
        postParams = new MultivaluedMapImpl();
         postParams.add("name", "Tag3");
         postParams.add("color", "#ff0000");
         ClientResponse response = tagResource.put(ClientResponse.class, postParams);
         Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
         JSONObject json = response.getEntity(JSONObject.class);
         String tag3Id = json.optString("id");
         Assert.assertNotNull(tag3Id);        
         
         // Add a book
         WebResource bookResource = resource().path("/book");
         bookResource.addFilter(new CookieAuthenticationFilter(book1Token));
         postParams = new MultivaluedMapImpl();
         postParams.add("isbn", "9780345376596");
         response = bookResource.put(ClientResponse.class, postParams);
         Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
         json = response.getEntity(JSONObject.class);
         String book1Id = json.optString("id");
         Assert.assertNotNull(book1Id);
         
         // Add the same book (KO)
         bookResource = resource().path("/book");
         bookResource.addFilter(new CookieAuthenticationFilter(book1Token));
         postParams = new MultivaluedMapImpl();
         postParams.add("isbn", "9780345376596");
         response = bookResource.put(ClientResponse.class, postParams);
         Assert.assertEquals(Status.BAD_REQUEST, Status.fromStatusCode(response.getStatus()));
         
         // Update a book
         bookResource = resource().path("/book/" + book1Id);
         bookResource.addFilter(new CookieAuthenticationFilter(book1Token));
         postParams = new MultivaluedMapImpl();
         postParams.add("tags", tag3Id);
         response = bookResource.post(ClientResponse.class, postParams);
         Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
         
         // Set a book as read
         bookResource = resource().path("/book/" + book1Id + "/read");
         bookResource.addFilter(new CookieAuthenticationFilter(book1Token));
         postParams = new MultivaluedMapImpl();
         postParams.add("read", true);
         response = bookResource.post(ClientResponse.class, postParams);
         Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
         
         // Get the book
         bookResource = resource().path("/book/" + book1Id);
         bookResource.addFilter(new CookieAuthenticationFilter(book1Token));
         response = bookResource.get(ClientResponse.class);
         Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
         json = response.getEntity(JSONObject.class);
         Assert.assertEquals("Pale Blue Dot", json.getString("title"));
         Assert.assertEquals("A Vision of the Human Future in Space", json.getString("subtitle"));
         Assert.assertEquals("Carl Sagan", json.getString("author"));
         Assert.assertEquals(852073200000l, json.getLong("publish_date"));
         Assert.assertEquals(360, json.getLong("page_count"));
         Assert.assertTrue(json.getString("description").contains("the world beyond Earth"));
         Assert.assertEquals("en", json.getString("language"));
         Assert.assertEquals("0345376595", json.getString("isbn10"));
         Assert.assertEquals("9780345376596", json.getString("isbn13"));
         Assert.assertEquals("Tag3", json.getJSONArray("tags").getJSONObject(0).getString("name"));
         Assert.assertNotNull(json.getLong("read_date"));
 
         // Set a book as unread
         bookResource = resource().path("/book/" + book1Id + "/read");
         bookResource.addFilter(new CookieAuthenticationFilter(book1Token));
         postParams = new MultivaluedMapImpl();
         postParams.add("read", false);
         response = bookResource.post(ClientResponse.class, postParams);
         Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
         
         // Get the book
         bookResource = resource().path("/book/" + book1Id);
         bookResource.addFilter(new CookieAuthenticationFilter(book1Token));
         response = bookResource.get(ClientResponse.class);
         Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
         json = response.getEntity(JSONObject.class);
         Assert.assertFalse(json.has("read_date"));
         
         // Get the book cover
         bookResource = resource().path("/book/" + book1Id + "/cover");
         response = bookResource.get(ClientResponse.class);
         Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
         InputStream is = response.getEntityInputStream();
         byte[] fileBytes = ByteStreams.toByteArray(is);
         Assert.assertEquals(14406, fileBytes.length);
         
         // List all books
         bookResource = resource().path("/book/list");
         bookResource.addFilter(new CookieAuthenticationFilter(book1Token));
         MultivaluedMapImpl getParams = new MultivaluedMapImpl();
         response = bookResource.queryParams(getParams).get(ClientResponse.class);
         json = response.getEntity(JSONObject.class);
         Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
         JSONArray books = json.getJSONArray("books");
         Assert.assertTrue(books.length() == 1);
         
         // Search the book
         bookResource = resource().path("/book/list");
         bookResource.addFilter(new CookieAuthenticationFilter(book1Token));
         getParams = new MultivaluedMapImpl();
         getParams.add("tag", "Tag3");
         getParams.add("search", "vision");
         getParams.add("read", false);
         response = bookResource.queryParams(getParams).get(ClientResponse.class);
         json = response.getEntity(JSONObject.class);
         Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
         books = json.getJSONArray("books");
         Assert.assertTrue(books.length() == 1);
         
         // Import a Goodreads CSV
         bookResource = resource().path("/book/import");
         bookResource.addFilter(new CookieAuthenticationFilter(book1Token));
         FormDataMultiPart form = new FormDataMultiPart();
         InputStream goodreadsImport = this.getClass().getResourceAsStream("/import/goodreads.csv");
         FormDataBodyPart fdp = new FormDataBodyPart("file",
                 new BufferedInputStream(goodreadsImport),
                 MediaType.APPLICATION_OCTET_STREAM_TYPE);
         form.bodyPart(fdp);
         response = bookResource.type(MediaType.MULTIPART_FORM_DATA).put(ClientResponse.class, form);
         Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
         
         // List all books
         bookResource = resource().path("/book/list");
         bookResource.addFilter(new CookieAuthenticationFilter(book1Token));
         getParams = new MultivaluedMapImpl();
         getParams.add("limit", 15);
         response = bookResource.queryParams(getParams).get(ClientResponse.class);
         json = response.getEntity(JSONObject.class);
         Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
         books = json.getJSONArray("books");
         Assert.assertEquals(11, books.length());
     }
 }
