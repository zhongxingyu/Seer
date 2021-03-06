 /*******************************************************************************
  * Copyright (c) 2008 Innoopract Informationssysteme GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Innoopract Informationssysteme GmbH - initial API and implementation
  ******************************************************************************/
 
 package org.eclipse.rwt.internal.theme;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 import junit.framework.TestCase;
 
 
 public class QxImage_Test extends TestCase {
 
   ResourceLoader dummyLoader = new ResourceLoader() {
 
     public InputStream getResourceAsStream( final String resourceName )
       throws IOException
     {
       return null;
     }
   };
 
   public void testIllegalArguments() throws Exception {
     try {
       QxImage.valueOf( null, null );
       fail( "Must throw NPE" );
     } catch( NullPointerException e ) {
       // expected
     }
     try {
       QxImage.valueOf( "", null );
       fail( "Must throw NPE" );
     } catch( NullPointerException e ) {
       // expected
     }
     try {
       QxImage.valueOf( "", dummyLoader  );
       fail( "Must throw IAE" );
     } catch( IllegalArgumentException e ) {
       // expected
     }
   }
 
   public void testNone() throws Exception {
     assertSame( QxImage.NONE, QxImage.valueOf( "none", null ) );
     assertSame( QxImage.NONE, QxImage.valueOf( "none", dummyLoader ) );
     assertNotSame( QxImage.NONE, QxImage.valueOf( "None", dummyLoader ) );
     assertTrue( QxImage.NONE.none );
     assertNull( QxImage.NONE.path );
     assertNull( QxImage.NONE.loader );
   }
 
   public void testCreate() throws Exception {
     QxImage qxImage = QxImage.valueOf( "foo", dummyLoader );
     assertFalse( qxImage.none );
     assertEquals( "foo", qxImage.path );
     assertSame( dummyLoader, qxImage.loader );
   }
 
   public void testDefaultString() throws Exception {
     assertEquals( "none", QxImage.NONE.toDefaultString() );
     assertEquals( "", QxImage.valueOf( "foo", dummyLoader ).toDefaultString() );
   }
 }
