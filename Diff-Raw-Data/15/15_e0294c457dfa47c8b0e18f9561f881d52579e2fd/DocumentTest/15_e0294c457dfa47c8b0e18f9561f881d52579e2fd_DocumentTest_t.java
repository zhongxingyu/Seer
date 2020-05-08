 /*
  * Copyright (C) 2010 Klaus Reimer <k@ailis.de>
  * See LICENSE.txt for licensing information.
  */
 
 package de.ailis.jollada.model;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertSame;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 
 import org.junit.Test;
 
import de.ailis.jollada.exceptions.DocumentException;
 
 
 /**
  * Tests the Document class.
  *
  * @author Klaus Reimer (k@ailis.de)
  */
 
 public class DocumentTest
 {
     /**
      * Tests the default constructor.
      */
 
     @Test
     public void testDefaultConstructor()
     {
         final Document document = new Document();
         assertEquals(Version.VERSION_1_5_0, document.getVersion());
         assertNull(document.getBase());
         assertNotNull(document.getAsset());
         assertEquals(0, document.getVisualSceneLibraries().size());
         assertNull(document.getScene());
     }
 
 
     /**
      * Tests the constructor with version argument.
      */
 
     @Test
     public void testConstructorWithVersion()
     {
         final Document document = new Document(Version.VERSION_1_5_0);
         assertEquals(Version.VERSION_1_5_0, document.getVersion());
         assertNull(document.getBase());
         assertNotNull(document.getAsset());
         assertEquals(0, document.getVisualSceneLibraries().size());
         assertEquals(0, document.getGeometryLibraries().size());
         assertNull(document.getScene());
     }
 
 
     /**
      * Tests the constructor with null argument.
      */
 
     @Test(expected = IllegalArgumentException.class)
     public void testNullConstructor()
     {
         new Document(null).toString();
     }
 
 
     /**
      * Tests the document base.
      *
      * @throws URISyntaxException
      *             When URI is invalid.
      */
 
     @Test
     public void testBase() throws URISyntaxException
     {
         final Document document = new Document();
         document.setBase(new URI("BASE"));
         assertEquals(new URI("BASE"), document.getBase());
         document.setBase(null);
         assertNull(document.getBase());
     }
 
 
     /**
      * Tests the scene.
      */
 
     @Test
     public void testScene()
     {
         final Document document = new Document();
         final Scene scene = new Scene();
         assertNull(scene.getDocument());
         document.setScene(scene);
         assertSame(document, scene.getDocument());
         assertNotNull(document.getScene());
         document.setScene(null);
         assertNull(document.getScene());
     }
 
 
     /**
      * Tests setting a scene which was already attached to a different scene.
      */
 
     @Test
     public void testAttachedScene()
     {
         final Document oldDocument = new Document();
         final Document document = new Document();
         final Scene scene = new Scene();
         oldDocument.setScene(scene);
         document.setScene(scene);
         assertNull(oldDocument.getScene());
         assertSame(document, scene.getDocument());
         assertSame(scene, document.getScene());
     }
 
 
     /**
      * Tests setting the same again.
      */
 
     @Test
     public void testSameScene()
     {
         final Document document = new Document();
         final Scene scene = new Scene();
         document.setScene(scene);
         assertSame(document, scene.getDocument());
         assertSame(scene, document.getScene());
         document.setScene(scene); // Set again
         assertSame(document, scene.getDocument());
         assertSame(scene, document.getScene());
     }
 
 
     /**
      * Tests the getById method.
      */
 
     @Test
     public void testGetById()
     {
         final Document document = new Document();
         final Elements<VisualSceneLibrary> libs = document
                 .getVisualSceneLibraries();
         final VisualSceneLibrary lib1 = new VisualSceneLibrary();
         lib1.setId("lib1");
         final VisualSceneLibrary lib2 = new VisualSceneLibrary();
         lib2.setId("lib2");
 
         libs.add(lib1);
         libs.add(lib2);
         assertNull(document.getById("lib3"));
         assertSame(lib1, document.getById("lib1"));
         assertSame(lib2, document.getById("lib2"));
 
         libs.remove(lib1);
         assertNull(document.getById("lib3"));
         assertNull(document.getById("lib1"));
         assertSame(lib2, document.getById("lib2"));
 
         lib2.setId("lib2new");
         assertNull(document.getById("lib2"));
         assertNull(document.getById("lib1"));
         assertSame(lib2, document.getById("lib2new"));
 
         final VisualScene scene = new VisualScene();
         final VisualScenes scenes = lib2.getVisualScenes();
         scenes.add(scene);
         scene.setId("scene");
         assertSame(scene, document.getById("scene"));
         scene.setId("newscene");
         assertNull(document.getById("scene"));
         assertSame(scene, document.getById("newscene"));
 
         final Node node1 = new Node();
         node1.setId("node1");
 
         final Node node2 = new Node();
         node2.setId("node2");
 
         node1.getNodes().add(node2);
         scene.getNodes().add(node1);
 
         assertSame(node1, document.getById("node1"));
         assertSame(node2, document.getById("node2"));
 
         libs.remove(lib2);
         assertNull(document.getById("scenenew"));
         assertNull(document.getById("node1"));
         assertNull(document.getById("node2"));
     }
 
 
     /**
      * Tests register() method with null argument.
      */
 
     @Test(expected = IllegalArgumentException.class)
     public void testRegisterWithNull()
     {
         new Document().register(null);
     }
 
 
     /**
      * Tests register() method with no-id element.
      */
 
     @Test
     public void testRegisterWithNoId()
     {
         final Document document = new Document();
         final VisualScene element = new VisualScene();
         document.register(element);
         document.register(element); // Must work because nothing was registered
     }
 
 
     /**
      * Tests register() method.
      */
 
     @Test
     public void testRegister()
     {
         final Document document = new Document();
         final VisualScene element = new VisualScene();
         element.setId("foo");
         document.register(element);
         assertNull(document.getById("bar"));
         assertSame(element, document.getById("foo"));
     }
 
 
     /**
      * Tests register() method with already registered element.
      */
 
    @Test(expected=DocumentException.class)
     public void testInvalidRegister()
     {
         final Document document = new Document();
         final VisualScene element = new VisualScene();
         element.setId("foo");
         document.register(element);
         document.register(element);
     }
 
     /**
      * Tests unregister() method with null argument.
      */
 
     @Test(expected = IllegalArgumentException.class)
     public void testUnregisterWithNull()
     {
         new Document().unregister(null);
     }
 
 
     /**
      * Tests unregister() method with no-id element.
      */
 
     @Test
     public void testUnregisterWithNoId()
     {
         final Document document = new Document();
         final VisualScene element = new VisualScene();
         document.unregister(element);
     }
 
 
     /**
      * Tests unregister() method.
      */
 
     @Test
     public void testUnregister()
     {
         final Document document = new Document();
         final VisualScene element = new VisualScene();
         element.setId("foo");
         document.register(element);
         assertNull(document.getById("bar"));
         assertSame(element, document.getById("foo"));
 
         document.unregister(element);
         assertNull(document.getById("foo"));
     }
 
 
 
     /**
      * Tests unregister() method with not-registered element.
      */
 
    @Test(expected = DocumentException.class)
     public void testInvalidUnregister()
     {
         final Document document = new Document();
         final VisualScene element = new VisualScene();
         element.setId("foo");
         document.unregister(element);
     }
 }
