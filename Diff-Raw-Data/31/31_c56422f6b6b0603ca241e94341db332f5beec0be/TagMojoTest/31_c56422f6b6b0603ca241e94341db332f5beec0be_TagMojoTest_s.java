 package org.apache.maven.scm.plugin;
 
 import org.apache.maven.plugin.testing.AbstractMojoTestCase;
 import org.apache.maven.scm.provider.svn.SvnScmTestUtils;
 import org.codehaus.plexus.util.FileUtils;
 import org.codehaus.plexus.util.StringUtils;
 
 import java.io.File;
 
 /*
  * Copyright 2001-2006 The Apache Software Foundation.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 /**
  * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
  * @version $Id$
  */
 public class TagMojoTest
     extends AbstractMojoTestCase
 {
     File checkoutDir;
 
     File repository;
 
     protected void setUp()
         throws Exception
     {
         super.setUp();
 
         checkoutDir = getTestFile( "target/checkout" );
 
         FileUtils.forceDelete( checkoutDir );
 
         repository = getTestFile( "target/repository" );
 
         FileUtils.forceDelete( repository );
 
         SvnScmTestUtils.initializeRepository( repository );
 
         CheckoutMojo checkoutMojo = (CheckoutMojo) lookupMojo( "checkout", getTestFile(
             "src/test/resources/mojos/checkout/checkoutWithConnectionUrl.xml" ) );
 
         String connectionUrl = checkoutMojo.getConnectionUrl();
         connectionUrl = StringUtils.replace( connectionUrl, "${basedir}", getBasedir() );
         connectionUrl = StringUtils.replace( connectionUrl, "\\", "/" );
         checkoutMojo.setConnectionUrl( connectionUrl );
 
         checkoutMojo.execute();
     }
 
     public void testTag()
         throws Exception
     {
         TagMojo mojo = (TagMojo) lookupMojo( "tag", getTestFile( "src/test/resources/mojos/tag/tag.xml" ) );
         mojo.setWorkingDirectory( checkoutDir );
 
         String connectionUrl = mojo.getConnectionUrl();
         connectionUrl = StringUtils.replace( connectionUrl, "${basedir}", getBasedir() );
         connectionUrl = StringUtils.replace( connectionUrl, "\\", "/" );
         mojo.setConnectionUrl( connectionUrl );
 
         mojo.execute();
 
         CheckoutMojo checkoutMojo =
             (CheckoutMojo) lookupMojo( "checkout", getTestFile( "src/test/resources/mojos/tag/checkout.xml" ) );
 
         connectionUrl = checkoutMojo.getConnectionUrl();
         connectionUrl = StringUtils.replace( connectionUrl, "${basedir}", getBasedir() );
         connectionUrl = StringUtils.replace( connectionUrl, "\\", "/" );
         checkoutMojo.setConnectionUrl( connectionUrl );
 
         File tagCheckoutDir = getTestFile( "target/tags/mytag" );
         assertFalse( new File( tagCheckoutDir, "pom.xml" ).exists() );
         checkoutMojo.execute();
         assertTrue( new File( tagCheckoutDir, "pom.xml" ).exists() );
     }
 
     public void testTagWithTimestamp()
         throws Exception
     {
         TagMojo mojo =
             (TagMojo) lookupMojo( "tag", getTestFile( "src/test/resources/mojos/tag/tagWithTimestamp.xml" ) );
         mojo.setWorkingDirectory( checkoutDir );
 
         String connectionUrl = mojo.getConnectionUrl();
         connectionUrl = StringUtils.replace( connectionUrl, "${basedir}", getBasedir() );
         connectionUrl = StringUtils.replace( connectionUrl, "\\", "/" );
         mojo.setConnectionUrl( connectionUrl );
 
         mojo.execute();
     }
 }
