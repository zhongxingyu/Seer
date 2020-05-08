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
 package org.eclipse.jubula.tools.jarutils;
 
 /**
  * Interface to manage all relevant versions for Jubula
  * @author BREDEX GmbH
  * @created 02.11.2005
  */
 public interface IVersion {
     /**
      * major version for communication between the client, AUT-Agent and remote
      * control components please increase this version in case of modification
      * of status messages from server or creation of new messages
      */
     public final Integer JB_PROTOCOL_MAJOR_VERSION = new Integer(10);
     
     /** major version for DB */
    public final Integer JB_DB_MAJOR_VERSION = new Integer(34);
     
     /** minor version for DB */
     public final Integer GD_DB_MINOR_VERSION = new Integer(0);
     
     /** major version for xml import support */
     public final Integer JB_XML_IMPORT_MAJOR_VERSION = new Integer(1);
     
     /** minor version for xml import support */
     public final Integer JB_XML_IMPORT_MINOR_VERSION = new Integer(1);
     
     /** major version for state of metadata in xml-format or from database
      *  modifications in client code without modification of ToolkitPlugins
      *  require an increasement of this version
      */
     public final Integer JB_CLIENT_METADATA_VERSION = new Integer(6);
     
     /** minimum required metadata version for project import */
     public final Integer JB_CLIENT_MIN_XML_METADATA_VERSION = new Integer(5);
     
     /** major version for Jubula preference store */
     public final Integer JB_PREF_MAJOR_VERSION = new Integer(1);
 
     /** minor version for Jubula preference store */
     public final Integer JB_PREF_MINOR_VERSION = new Integer(1);
 }
