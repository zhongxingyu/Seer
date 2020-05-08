 /*
  * Copyright (c) 2005 Aetrion LLC.
  */
 package com.aetrion.flickr;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 import com.aetrion.flickr.auth.AuthInterface;
 import com.aetrion.flickr.blogs.BlogsInterface;
 import com.aetrion.flickr.favorites.FavoritesInterface;
 import com.aetrion.flickr.groups.GroupsInterface;
 import com.aetrion.flickr.groups.pools.PoolsInterface;
 import com.aetrion.flickr.people.PeopleInterface;
 import com.aetrion.flickr.photos.PhotosInterface;
 import com.aetrion.flickr.photos.licenses.LicensesInterface;
 import com.aetrion.flickr.photosets.PhotosetsInterface;
 import com.aetrion.flickr.reflection.ReflectionInterface;
 import com.aetrion.flickr.tags.TagsInterface;
 import com.aetrion.flickr.test.TestInterface;
 import com.aetrion.flickr.urls.UrlsInterface;
 import com.aetrion.flickr.contacts.ContactsInterface;
 
 /**
  * Main entry point for the Flickrj API.  This class is used to acquire Interface classes which wrap the Flickr API.
  *
  * @author Anthony Eden
  */
 public class Flickr {
 
     /**
      * The default endpoint host.
      */
     public static final String DEFAULT_HOST = "www.flickr.com";
 
     /**
      * Set to true to enable response debugging (print the response stream)
      */
     public static boolean debugStream = false;
 
     /**
      * Set to true to enable request debugging (print the request stream, used for "post")
      */
     public static boolean debugRequest = false;
 
     private String apiKey;
     private Transport transport;
 
     private AuthInterface authInterface;
     private BlogsInterface blogsInterface;
     private ContactsInterface contactsInterface;
     private FavoritesInterface favoritesInterface;
     private GroupsInterface groupsInterface;
     private LicensesInterface licensesInterface;
     private PoolsInterface poolsInterface;
     private PeopleInterface peopleInterface;
     private PhotosInterface photosInterface;
     private PhotosetsInterface photosetsInterface;
     private ReflectionInterface reflectionInterface;
     private TagsInterface tagsInterface;
     private TestInterface testInterface;
     private UrlsInterface urlsInterface;
 
     /**
      * Construct a new Flickr gateway instance.  Defaults to a REST transport.
      *
      * @param apiKey The API key, must be non-null
      */
     public Flickr(String apiKey) {
         setApiKey(apiKey);
         try {
             setTransport(new REST(DEFAULT_HOST));
         } catch (ParserConfigurationException e) {
             throw new RuntimeException(e.getMessage(), e);
         }
     }
 
     /**
      * Construct a new Flickr gateway instance.
      *
      * @param apiKey The API key, must be non-null
      * @param transport The transport (REST or SOAP), must be non-null
      */
     public Flickr(String apiKey, Transport transport) {
         setApiKey(apiKey);
         setTransport(transport);
     }
 
     /**
      * Get the API key.
      *
      * @return The API key
      */
     public String getApiKey() {
         return apiKey;
     }
 
     /**
      * Set the API key to use which must not be null.
      *
      * @param apiKey The API key which cannot be null
      */
     public void setApiKey(String apiKey) {
         if (apiKey == null) {
             throw new IllegalArgumentException("API key must not be null");
         }
         this.apiKey = apiKey;
     }
 
     /**
      * Get the Transport interface
      *
      * @return The Tranport interface
      */
     public Transport getTransport() {
         return transport;
     }
 
     /**
      * Set the Transport which must not be null.
      *
      * @param transport
      */
     public void setTransport(Transport transport) {
         if (transport == null) {
             throw new IllegalArgumentException("Transport must not be null");
         }
         this.transport = transport;
     }
 
     /**
      * Get the AuthInterface.
      *
      * @return The AuthInterface
      */
     public AuthInterface getAuthInterface() {
         if (authInterface == null) {
             authInterface = new AuthInterface(apiKey, transport);
         }
         return authInterface;
     }
 
     public synchronized BlogsInterface getBlogsInterface() {
         if (blogsInterface == null) {
             blogsInterface = new BlogsInterface(apiKey, transport);
         }
         return blogsInterface;
     }
 
     public ContactsInterface getContactsInterface() {
         if (contactsInterface == null) {
             contactsInterface = new ContactsInterface(apiKey, transport);
         }
         return contactsInterface;
     }
 
     public FavoritesInterface getFavoritesInterface() {
         if (favoritesInterface == null) {
             favoritesInterface = new FavoritesInterface(apiKey, transport);
         }
         return favoritesInterface;
     }
 
     public GroupsInterface getGroupsInterface() {
         if (groupsInterface == null) {
             groupsInterface = new GroupsInterface(apiKey, transport);
         }
         return groupsInterface;
     }
 
     public LicensesInterface getLicensesInterface() {
         if (licensesInterface == null) {
             licensesInterface = new LicensesInterface(apiKey, transport);
         }
         return licensesInterface;
     }
 
     public PoolsInterface getPoolsInterface() {
         if (poolsInterface == null) {
             poolsInterface = new PoolsInterface(apiKey, transport);
         }
         return poolsInterface;
     }
 
     public PeopleInterface getPeopleInterface() {
         if (peopleInterface == null) {
             peopleInterface = new PeopleInterface(apiKey, transport);
         }
         return peopleInterface;
     }
 
     public PhotosInterface getPhotosInterface() {
         if (photosInterface == null) {
             photosInterface = new PhotosInterface(apiKey, transport);
         }
         return photosInterface;
     }
 
     public PhotosetsInterface getPhotosetsInterface() {
         if (photosetsInterface == null) {
             photosetsInterface = new PhotosetsInterface(apiKey, transport);
         }
         return photosetsInterface;
     }
 
     public ReflectionInterface getReflectionInterface() {
         if (reflectionInterface == null) {
             reflectionInterface = new ReflectionInterface(apiKey, transport);
         }
         return reflectionInterface;
     }
 
     /**
      * Get the TagsInterface for working with Flickr Tags.
      *
      * @return The TagsInterface
      */
     public TagsInterface getTagsInterface() {
         if (tagsInterface == null) {
             tagsInterface = new TagsInterface(apiKey, transport);
         }
         return tagsInterface;
     }
 
     public TestInterface getTestInterface() {
         if (testInterface == null) {
             testInterface = new TestInterface(apiKey, transport);
         }
         return testInterface;
     }
 
     public UrlsInterface getUrlsInterface() {
         if (urlsInterface == null) {
             urlsInterface = new UrlsInterface(apiKey, transport);
         }
         return urlsInterface;
     }
 
 }
