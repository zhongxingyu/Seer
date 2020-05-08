 package com.github.seqware.queryengine.system.rest.resources;
 
 import static org.junit.Assert.assertEquals;
 
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import com.github.seqware.queryengine.model.Tag;
 
 public class TagResourceTest {
   public static final String WEBSERVICE_URL = "http://localhost:8889/seqware-queryengine-webservice/api/";
   public TagResourceTest() {
   }
   
   @BeforeClass
   public static void setUpClass() {
     //Create some Tags
   }
   
   @AfterClass
   public static void tearDownClass() {
     //Disable and Drop the created tags
   }
   
   @Before
   public void setUp() {
   }
   
   @After
   public void tearDown() {
   }
 
   /**
    * Test of getClassName method, of class TagResource.
    */
   @Test
   public void testGetClassName() {
     TagResource instance = new TagResource();
     String expResult = "Tag";
     String result = instance.getClassName();
     assertEquals(expResult, result);
   }
 
   /**
    * Test of getModelClass method, of class TagResource.
    */
   @Test
   public void testGetModelClass() {
     TagResource instance = new TagResource();
     Class expResult = Tag.class;
     Class result = instance.getModelClass();
     assertEquals(expResult, result);
   }
 }
