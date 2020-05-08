 /*******************************************************************************
  * Copyright (c) 2004, 2010 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.client.core.preferences.database;
 
 import org.apache.commons.lang.StringUtils;
 import org.eclipse.jubula.client.core.persistence.DatabaseConnectionInfo;
 import org.eclipse.persistence.config.PersistenceUnitProperties;
 
 /**
  * 
  * @author BREDEX GmbH
  * @created 19.01.2011
  */
 public class H2ConnectionInfo extends DatabaseConnectionInfo {
 
     /** name of <code>location</code> property */
     public static final String PROP_NAME_LOCATION = "location"; //$NON-NLS-1$
 
     /** the username to use for connections of this type */
     private static final String DEFAULT_USERNAME = "sa"; //$NON-NLS-1$
     
     /** the password to use for connections of this type */
     private static final String DEFAULT_PASSWORD = StringUtils.EMPTY;
     
     /** the location of the database files (on the filesystem) */
     private String m_location = "~/.jubula-db/jubula-db"; //$NON-NLS-1$
 
     /**
      * 
      * Constructor
      */
     public H2ConnectionInfo() {
         setProperty(PersistenceUnitProperties.JDBC_USER, DEFAULT_USERNAME);
         setProperty(PersistenceUnitProperties.JDBC_PASSWORD, DEFAULT_PASSWORD);
     }
     
     /**
      * 
      * @return the location of the database files (on the filesystem).
      */
     public String getLocation() {
         return m_location;
     }
 
     /**
      * 
      * @param location The location of the database files (on the filesystem).
      */
     public void setLocation(String location) {
         m_location = location;
         fireConnectionUrlChanged();
     }
     
     @Override
     public String getConnectionUrl() {
         StringBuilder sb = new StringBuilder();
         sb.append("jdbc:h2:") //$NON-NLS-1$
             .append(getLocation())
            .append(";MVCC=TRUE;AUTO_SERVER=TRUE"); //$NON-NLS-1$
         return sb.toString();
     }
 
     @Override
     public String getDriverClassName() {
         return "org.h2.Driver"; //$NON-NLS-1$
     }
 
 }
