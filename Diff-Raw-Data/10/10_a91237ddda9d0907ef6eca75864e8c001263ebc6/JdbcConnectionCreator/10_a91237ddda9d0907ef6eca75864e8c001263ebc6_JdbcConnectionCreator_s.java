 //$HeadURL: svn+ssh://lbuesching@svn.wald.intevation.de/deegree/base/trunk/resources/eclipse/files_template.xml $
 /*----------------------------------------------------------------------------
  This file is part of deegree, http://deegree.org/
  Copyright (C) 2001-2012 by:
  - Department of Geography, University of Bonn -
  and
  - lat/lon GmbH -
 
  This library is free software; you can redistribute it and/or modify it under
  the terms of the GNU Lesser General Public License as published by the Free
  Software Foundation; either version 2.1 of the License, or (at your option)
  any later version.
  This library is distributed in the hope that it will be useful, but WITHOUT
  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
  details.
  You should have received a copy of the GNU Lesser General Public License
  along with this library; if not, write to the Free Software Foundation, Inc.,
  59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 
  Contact information:
 
  lat/lon GmbH
  Aennchenstr. 19, 53177 Bonn
  Germany
  http://lat-lon.de/
 
  Department of Geography, University of Bonn
  Prof. Dr. Klaus Greve
  Postfach 1147, 53001 Bonn
  Germany
  http://www.geographie.uni-bonn.de/deegree/
 
  e-mail: info@deegree.org
  ----------------------------------------------------------------------------*/
 package org.deegree.igeo.dataadapter.jdbc;
 
 import org.deegree.igeo.config.JDBCConnection;
 import org.deegree.igeo.config.JDBCConnectionType;
 import org.deegree.igeo.utils.Encryption;
 
 /**
  * Create internal used {@link JDBCConnection} from config {@link JDBCConnectionType} and vice versa.
  * 
  * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
  * @author last edited by: $Author: lyn $
  * 
  * @version $Revision: $, $Date: $
  */
 public class JdbcConnectionCreator {
 
     public static JDBCConnectionType getAsJDBCConnectionType( JDBCConnection jdbcConnection ) {
         JDBCConnectionType connectionType = new JDBCConnectionType();
         connectionType.setDriver( jdbcConnection.getDriver() );
         connectionType.setUrl( jdbcConnection.getUrl() );
         if ( jdbcConnection.isSaveLogin() ) {
             connectionType.setUser( jdbcConnection.getUser() );
             String password = jdbcConnection.getPassword();
             connectionType.setPassword( password != null ? Encryption.encrypt( password ) : null );
         }
         return connectionType;
     }
 
     public static JDBCConnection getFromJDBCConnectionType( JDBCConnectionType connectionType ) {
         String password = connectionType.getPassword();
         return new JDBCConnection( connectionType.getDriver(), connectionType.getUrl(), connectionType.getUser(),
                                    password != null ? Encryption.decrypt( password ) : null,
                                   connectionType.getPassword() != null );
     }
 
 }
