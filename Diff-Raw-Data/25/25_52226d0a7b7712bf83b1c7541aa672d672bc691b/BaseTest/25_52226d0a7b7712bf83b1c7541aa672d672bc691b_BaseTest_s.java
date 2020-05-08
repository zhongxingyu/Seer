 /*
  * Polyglotter (http://polyglotter.org)
  * See the COPYRIGHT.txt file distributed with this work for information
  * regarding copyright ownership.  Some portions may be licensed
  * to Red Hat, Inc. under one or more contributor license agreements.
  * See the AUTHORS.txt file in the distribution for a full listing of 
  * individual contributors.
  *
  * Polyglotter is free software. Unless otherwise indicated, all code in Polyglotter
  * is licensed to you under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * Polyglotter is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package org.modeshape.modeler.test;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.nio.file.FileSystems;
 import java.nio.file.FileVisitResult;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.SimpleFileVisitor;
 import java.nio.file.attribute.BasicFileAttributes;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.runner.RunWith;
 import org.mockito.MockitoAnnotations;
 import org.modeshape.modeler.ModeShapeModeler;
 import org.modeshape.modeler.TestUtil;
 import org.modeshape.modeler.internal.Manager;
 import org.modeshape.modeler.internal.ModelTypeManagerImpl;
 
 @RunWith( TestRunner.class )
 @SuppressWarnings( "javadoc" )
 public abstract class BaseTest {
 
     protected static final String TEST_MODESHAPE_CONFIGURATION_PATH = "testModeShapeConfig.json";
     protected static final String TEST_REPOSITORY_STORE_PARENT_PATH;
 
     protected static final String ARTIFACT_NAME = "artifact";
     protected static final String MODEL_NAME = "model";
     protected static final String XML_MODEL_TYPE_CATEGORY = "xml";
     protected static final String XML_MODEL_TYPE_ID = "org.modeshape.modeler.xml.Xml";
     protected static final String XML_ROOT = "root";
     protected static final String XML_ROOT_PROPERTY = "property";
     protected static final String XML_LEAF = "child";
     protected static final String XML_STRING_VALUE = "string";
     protected static final String XML_SAME_NAME_SIBLING = "sameNameSibling";
     protected static final String XML_DECLARATION = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
     protected static final String XML_ARTIFACT = XML_DECLARATION
                                                  + '<' + XML_ROOT + " " + XML_ROOT_PROPERTY + "='" + XML_STRING_VALUE + "'>"
                                                  + "<" + XML_LEAF + "></" + XML_LEAF + ">"
                                                  + "<" + XML_SAME_NAME_SIBLING + "></" + XML_SAME_NAME_SIBLING + ">"
                                                  + "<" + XML_SAME_NAME_SIBLING + "></" + XML_SAME_NAME_SIBLING + ">"
                                                  + "</" + XML_ROOT + ">";
     protected static final String XSD_ARTIFACT = XML_DECLARATION + "<schema></schema>";
 
     static {
         try {
             final Path path = Files.createTempDirectory( null );
             path.toFile().deleteOnExit();
             TEST_REPOSITORY_STORE_PARENT_PATH = path.toString();
         } catch ( final IOException e ) {
             throw new RuntimeException( e );
         }
     }
 
     private ModeShapeModeler modeler;
     protected URL modelTypeRepository;
 
     @After
     public void after() throws Exception {
         if ( modeler != null ) {
             modeler.close();
             modeler = null;
         }
         deleteFolder( TEST_REPOSITORY_STORE_PARENT_PATH + "/modelerRepository" );
         deleteFolder( System.getProperty( "java.io.tmpdir" ) + "/modeshape-binary-store" );
     }
 
     @Before
     @SuppressWarnings( "unused" )
     public void before() throws Exception {
         MockitoAnnotations.initMocks( this );
     }
 
     private void deleteFolder( final String folder ) throws Exception {
         final File file = new File( folder );
         if ( file.exists() )
             Files.walkFileTree( FileSystems.getDefault().getPath( file.toString() ), new SimpleFileVisitor< Path >() {
 
                 @Override
                 public FileVisitResult postVisitDirectory( final Path folder,
                                                            final IOException e ) throws IOException {
                     if ( e != null ) throw e;
                     Files.delete( folder );
                     return FileVisitResult.CONTINUE;
                 }
 
                 @Override
                 public FileVisitResult visitFile( final Path file,
                                                   final BasicFileAttributes attrs ) throws IOException {
                     Files.delete( file );
                     return FileVisitResult.CONTINUE;
                 }
             } );
     }
 
     public Manager manager() throws Exception {
         return TestUtil.manager( modeler() );
     }
 
     public ModeShapeModeler modeler() throws Exception {
         if ( modeler == null )
             modeler = new ModeShapeModeler( TEST_REPOSITORY_STORE_PARENT_PATH, TEST_MODESHAPE_CONFIGURATION_PATH );
         for ( final URL url : modeler.modelTypeManager().modelTypeRepositories() )
             modeler.modelTypeManager().unregisterModelTypeRepository( url );
         modeler.modelTypeManager().registerModelTypeRepository( modelTypeRepository() );
         return modeler;
     }
 
     public ModelTypeManagerImpl modelTypeManager() throws Exception {
         return ( ModelTypeManagerImpl ) modeler().modelTypeManager();
     }
 
     protected URL modelTypeRepository() throws Exception {
         if ( modelTypeRepository == null ) {
            modelTypeRepository = new URL( "file:src/test/resources/" );
         }
 
         return modelTypeRepository;
     }
 
     public InputStream stream( final String content ) {
         return new ByteArrayInputStream( content.getBytes() );
     }
 }
