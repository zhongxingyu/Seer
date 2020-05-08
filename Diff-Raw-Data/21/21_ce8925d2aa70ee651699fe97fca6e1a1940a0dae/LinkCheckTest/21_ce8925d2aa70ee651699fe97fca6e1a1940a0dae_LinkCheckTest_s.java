 package org.apache.maven.doxia.linkcheck;
 
 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import junit.framework.TestCase;
 
 /**
  * @author Ben Walding
  * @author <a href="mailto:carlos@apache.org">Carlos Sanchez</a>
  * @version $Id$
  */
 public class LinkCheckTest extends TestCase
 {
    String baseDir;

    protected void setUp() throws Exception
    {
        super.setUp();

        this.baseDir = System.getProperty( "basedir" ); // TODO
    }

     public void testScan() throws Exception
     {
         LinkCheck lc = new LinkCheck();
 
         lc.setOnline( true ); // TODO: check if online
 
        lc.setBasedir( new File( this.baseDir + "/src/test/resources" ) ); // TODO
 
        lc.setOutput( new File( this.baseDir + "/target/linkcheck/linkcheck.xml" ) );
 
         lc.setOutputEncoding( "UTF-8" );
 
        lc.setCache( this.baseDir + "/target/linkcheck/linkcheck.cache" ); // TODO
 
         String[] excludes = new String[]
             {
                 "http://cvs.apache.org/viewcvs.cgi/maven-pluginszz/",
                 "http://cvs.apache.org/viewcvs.cgi/mavenzz/"
             };
 
         lc.setExcludes( excludes );
 
         lc.doExecute();
 
         Iterator iter = lc.getFiles().iterator();
 
         Map map = new HashMap();
 
         while ( iter.hasNext() )
         {
             FileToCheck ftc = (FileToCheck) iter.next();
             map.put( ftc.getName(), ftc );
         }
 
         assertEquals( "files.size()", 8, lc.getFiles().size() );
 
         check( map, "nolink.html", 0 );
         check( map, "test-resources/nolink.html", 0 );
         check( map, "test-resources/test1/test1.html", 2 );
         check( map, "test-resources/test1/test2.html", 0 );
         check( map, "test1/test1.html", 1 );
         check( map, "testA.html", 3 );
 
         /* test excludes */
         String fileName = "testExcludes.html";
         check( map, fileName, 2 );
 
         FileToCheck ftc = (FileToCheck) map.get( fileName );
         assertEquals( "Excluded links", 2, ftc.getSuccessful() );
 
         // index-all.html should get parsed, but is currently having problems.
         // There are 805 distinct links in this page
         check( map, "index-all.html", 805 );
 
     }
 
     private void check( Map map, String name, int linkCount )
     {
         FileToCheck ftc = (FileToCheck) map.get( name );
 
        assertNotNull( name, ftc );
 
         assertEquals( name + ".getResults().size()", linkCount, ftc.getResults().size() );
     }
 
 }
