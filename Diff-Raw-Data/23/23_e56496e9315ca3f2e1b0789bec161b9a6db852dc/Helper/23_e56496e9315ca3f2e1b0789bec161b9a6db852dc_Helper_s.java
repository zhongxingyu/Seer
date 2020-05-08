 /*
  * Copyright (C) 2008-2009 David Kellum
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
 
 import static iudex.core.ContentKeys.NEXT_VISIT_AFTER;
 import static iudex.core.ContentKeys.PRIORITY;
 import static iudex.core.ContentKeys.TYPE;
 import static iudex.core.ContentKeys.URL;
 import iudex.core.VisitURL;
 import iudex.core.VisitURL.SyntaxException;
 
 import java.sql.SQLException;
 import java.util.Date;
 
 import javax.sql.DataSource;
 
 import org.apache.commons.dbutils.QueryRunner;
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
import org.junit.Test;
 
 import com.gravitext.htmap.UniMap;
 
 public class Helper
 {
     @BeforeClass
     public static void setup() throws SQLException
     {
         _dataSource = DataSourceFactory.create();
         QueryRunner runner = new QueryRunner( _dataSource );
         runner.update( "DELETE from urls;" );
     }
 
     @AfterClass
     public static void close()
     {
     }
 
     public void testEmpty()
     {
         // make mvn/junit happy.
     }
 
     protected static DataSource dataSource()
     {
         return _dataSource;
     }
 
     protected static UniMap content( String url, String type, float priority )
         throws SyntaxException
     {
         UniMap content = new UniMap();
         content.set( URL, VisitURL.normalize( url ) );
         content.set( TYPE, type );
         content.set( PRIORITY, priority );
         content.set( NEXT_VISIT_AFTER, new Date() );
         return content;
     }
 
     private static DataSource _dataSource = null;
 }
