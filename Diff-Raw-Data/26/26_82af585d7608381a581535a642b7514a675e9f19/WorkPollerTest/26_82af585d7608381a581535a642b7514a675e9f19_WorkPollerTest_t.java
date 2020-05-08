 /*
  * Copyright (c) 2008-2012 David Kellum
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package iudex.da;
 
 import iudex.core.VisitURL.SyntaxException;
 
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.junit.Test;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.gravitext.htmap.UniMap;
 
 import static org.junit.Assert.*;
 import static iudex.core.ContentKeys.*;
 import static iudex.da.ContentMapper.*;
 
 public class WorkPollerTest
     extends Helper
 {
     @Test
     public void testAutoCommit() throws SQLException
     {
         Connection conn = dataSource().getConnection();
         assertTrue( conn.getAutoCommit() );
         conn.close();
     }
 
     @Test
     public void test() throws SQLException, SyntaxException
     {
         ContentMapper mapper =
             new ContentMapper( UHASH, DOMAIN, URL, TYPE, PRIORITY,
                                NEXT_VISIT_AFTER );
 
         ContentWriter writer = new ContentWriter( dataSource(), mapper );
         List<UniMap> in = new ArrayList<UniMap>();
         in.add( content( "http://gravitext.com/blog/feed/atom.xml",
                          TYPE_FEED, 1.5f ) );
 
         assertEquals( 1, writer.write( in ) );
 
         mapper = new ContentMapper( URL, TYPE, PRIORITY, NEXT_VISIT_AFTER );
         WorkPoller wpoller = new WorkPoller( dataSource(), mapper );
         List<UniMap> out = wpoller.poll();
         assertNotNull( out );
         _log.info( "Work poll returned {} contents", out.size() );
 
         for( UniMap w : out ) {
             _log.info( w.toString() );
         }
     }
 
    public final Logger _log = LoggerFactory.getLogger( getClass() );
 }
