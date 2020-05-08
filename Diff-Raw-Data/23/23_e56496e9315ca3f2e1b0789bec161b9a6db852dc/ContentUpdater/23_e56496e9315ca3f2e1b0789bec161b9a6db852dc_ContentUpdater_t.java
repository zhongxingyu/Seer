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
 
 import iudex.core.ContentKeys;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import javax.sql.DataSource;
 
 import org.apache.commons.dbutils.QueryRunner;
 import org.apache.commons.dbutils.ResultSetHandler;
 
 import com.gravitext.htmap.UniMap;
 
 public class ContentUpdater
     extends ContentWriter
 {
     public ContentUpdater( DataSource source, ContentMapper mapper )
     {
         super( source, mapper );
         if( ! mapper.fields().contains( ContentMapper.UHASH ) ) {
             throw new IllegalArgumentException(
                "ContentUpdater needs mapper with UHASH included." );
         }
     }
 
     /**
      * Update both content record and any attached REFERENCES.
      */
     public void update( UniMap content )
         throws SQLException
     {
         Connection conn = dataSource().getConnection();
         try {
             conn.setAutoCommit( false );
             conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
             //FIXME: Correct isolation?
 
             update( content, conn );
 
             List<UniMap> refs = content.get( ContentKeys.REFERENCES );
             if( refs != null ) {
                 update( refs, conn );
             }
 
             conn.commit();
         }
         finally {
             if( conn != null ) conn.close();
         }
     }
 
     //FIXME: Transform interface as param?
     public void update( List<UniMap> references ) throws SQLException
     {
         Connection conn = dataSource().getConnection();
         try {
             conn.setAutoCommit( false );
             conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
 
             update( references, conn );
 
             conn.commit();
         }
         finally {
             if( conn != null ) conn.close();
         }
     }
 
     protected void update( UniMap content, Connection conn )
         throws SQLException
     {
         final StringBuilder qb = new StringBuilder( 256 );
         qb.append( "SELECT " );
         mapper().appendFieldNames( qb );
         qb.append( " FROM urls WHERE uhash = ? FOR UPDATE;" );
 
         UpdateQueryRunner runner = new UpdateQueryRunner();
         int update = (Integer)
             runner.query( conn, qb.toString(),
                           new OneUpdateHandler( content ),
                 new Object[] { content.get( ContentKeys.URL ).uhash() } );
 
         if( update == 0 ) write( transform( content, null ), conn );
     }
 
     protected void update( List<UniMap> references, Connection conn )
         throws SQLException
     {
         final HashMap<String,UniMap> uhashes =
             new HashMap<String,UniMap>( references.size() );
         final String qry = formatSelect( references, uhashes );
         final UpdateQueryRunner runner = new UpdateQueryRunner();
         runner.query( conn, qry, new RefUpdateHandler( uhashes ) );
 
         final ArrayList<UniMap> remains =
             new ArrayList<UniMap>( uhashes.size() );
         for( UniMap rem : uhashes.values() ) {
             remains.add( transform( rem, null ) );
         }
         if( remains.size() > 0 ) write( remains, conn );
     }
 
     private String formatSelect( final List<UniMap> references,
                                  final HashMap<String, UniMap> uhashes )
     {
         final StringBuilder qb = new StringBuilder( 512 );
         qb.append( "SELECT " );
         mapper().appendFieldNames( qb );
         qb.append( " FROM urls WHERE uhash IN (" );
         boolean first = true;
         for( UniMap r : references ) {
             if( first ) first = false;
             else qb.append( ", " );
 
             final String uhash = r.get( ContentKeys.URL ).uhash();
             uhashes.put( uhash, r );
 
             qb.append( '\'' );
             qb.append( uhash );
             qb.append( '\'' );
         }
         qb.append( ") FOR UPDATE;" );
 
         return qb.toString();
     }
 
     protected UniMap transform( final UniMap reference, final UniMap in )
     {
         if( in == null ) return reference;
 
         // Fold ref over top of found.
         UniMap t = in.clone();
         t.putAll( reference );
         return t;
     }
 
     private final class RefUpdateHandler
         implements ResultSetHandler
     {
         public RefUpdateHandler( HashMap<String, UniMap> hashes )
         {
             _hashes = hashes;
         }
 
         @Override
         public Object handle( ResultSet rs ) throws SQLException
         {
             while( rs.next() ) {
                 final UniMap in = mapper().fromResultSet( rs );
                 final UniMap ref = _hashes.remove( rs.getString( "uhash" ) );
 
                 UniMap out = transform( ref, in );
 
                 if( mapper().update( rs, in, out ) ) {
                     rs.updateRow();
                 }
             }
             return null;
         }
 
         private HashMap<String, UniMap> _hashes;
     }
 
     private final class OneUpdateHandler
         implements ResultSetHandler
     {
         public OneUpdateHandler( UniMap content )
         {
             _content = content;
         }
 
         @Override
         public Integer handle( ResultSet rs ) throws SQLException
         {
            if( rs.next() ) {
                 final UniMap in = mapper().fromResultSet( rs );
 
                 UniMap out = transform( _content, in );
 
                 if( mapper().update( rs, in, out ) ) {
                     rs.updateRow();
                 }
                 return 1;
             }
            if( rs.next() ) {
                throw new IllegalStateException(
                   "Multiple url rows returned for uhash=" +
                   _content.get( ContentKeys.URL ).uhash() );
            }
             return 0;
         }
 
         private UniMap _content;
     }
 
     private static final class UpdateQueryRunner extends QueryRunner
     {
         @Override
         protected PreparedStatement prepareStatement( Connection conn,
                                                       String sql )
             throws SQLException
         {
             return conn.prepareStatement( sql,
                                           ResultSet.TYPE_FORWARD_ONLY,
                                           ResultSet.CONCUR_UPDATABLE );
         }
     }
 }
