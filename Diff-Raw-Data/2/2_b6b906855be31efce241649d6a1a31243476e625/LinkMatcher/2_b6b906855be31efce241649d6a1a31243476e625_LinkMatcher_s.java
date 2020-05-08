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
 import java.io.IOException;
 import java.io.Reader;
 import java.util.Locale;
 import java.util.Set;
 import java.util.TreeSet;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.codehaus.plexus.util.IOUtil;
 import org.codehaus.plexus.util.ReaderFactory;
 
 /**
  * Link matcher. Reads the contents of a file and tries to match the following:
  * <pre>
  * &lt;a href="".../&gt;
  * &lt;link href="".../&gt;
  * &lt;img src="".../&gt;
  * &lt;script src="".../&gt;
  * </pre>
  *
  * @author <a href="mailto:mac@apache.org">Ignacio G. Mac Dowell </a>
  * @version $Id$
  */
 class LinkMatcher
 {
     /** Regexp for link matching. */
     private static final Pattern MATCH_PATTERN =
         Pattern.compile( "<(?>link|a|img|script)[^>]*?(?>href|src)\\s*?=\\s*?[\\\"'](.*?)[\\\"'][^>]*?",
                          Pattern.CASE_INSENSITIVE );
 
     /** No need to create a new object each time a file is processed. Just clear it. */
     private static final Set<String> LINK_LIST = new TreeSet<String>();
 
     private LinkMatcher()
     {
         // nop
     }
 
     /**
      * Reads a file and returns its contents without any XML comments.
      *
      * @param file the file we are reading
      * @param encoding the encoding file used
     * @return a StringBuffer with file's contents.
      * @throws IOException if something goes wrong.
      * @see ReaderFactory#newReader(File, String)
      * @see IOUtil#toString(Reader)
      */
     private static String toString( File file, String encoding )
         throws IOException
     {
         String content;
         Reader reader = null;
         try
         {
             reader = ReaderFactory.newReader( file, encoding );
 
             content = IOUtil.toString( reader );
         }
         finally
         {
             IOUtil.close( reader );
         }
 
         // some link could be in comments, remove them
         return content.replaceAll( "(?s)<!--.*?-->", "" );
     }
 
     /**
      * Performs the actual matching.
      *
      * @param file the file to check
      * @param encoding the encoding file used
      * @return a set with all links to check
      * @throws IOException if something goes wrong
      */
     static Set<String> match( File file, String encoding )
         throws IOException
     {
         LINK_LIST.clear();
 
         final Matcher m = MATCH_PATTERN.matcher( toString( file, encoding ) );
 
         String link;
 
         while ( m.find() )
         {
             link = m.group( 1 ).trim();
 
             if ( link.length() < 1 )
             {
                 continue;
             }
 
             if ( link.toLowerCase( Locale.ENGLISH ).indexOf( "javascript" ) != -1 )
             {
                 continue;
             }
             // TODO: Review dead code and delete if not needed
             // else if ( link.toLowerCase( Locale.ENGLISH ).indexOf( "mailto:" ) != -1 )
             // {
             // continue;
             // }
 
             LINK_LIST.add( link );
         }
 
         return LINK_LIST;
     }
 }
