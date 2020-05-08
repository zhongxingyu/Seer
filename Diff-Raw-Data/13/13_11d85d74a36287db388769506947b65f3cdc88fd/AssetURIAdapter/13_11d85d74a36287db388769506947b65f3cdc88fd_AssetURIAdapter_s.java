 /**
  * Project Wonderland
  *
  * $RCSfile: AssetType.java,v $
  *
  * Copyright (c) 2004-2007, Sun Microsystems, Inc., All Rights Reserved
  *
  * Redistributions in source code form must reproduce the above
  * copyright and this condition.
  *
  * The contents of this file are subject to the GNU General Public
  * License, Version 2 (the "License"); you may not use this file
  * except in compliance with the License. A copy of the License is
  * available at http://www.opensource.org/licenses/gpl-license.php.
  *
  * $Revision: 1.3 $
  * $Date: 2007/05/04 23:11:34 $
  * $State: Exp $
  */
 
 package org.jdesktop.wonderland.common;
 
 import javax.xml.bind.annotation.adapters.XmlAdapter;
 
 /**
  * Adapter class to read asset uri's from XML files and insert the proper
  * server name.
  * 
  * @author Jordan Slott <jslott@dev.java.net>
  */
 public class AssetURIAdapter extends XmlAdapter<String, AssetURI> {
     /* The server name to insert into the URI */
     private String serverName = null;
     
     /** Default constructor */
     public AssetURIAdapter() {
     }
     
     /** Constructor takes the name of the server session */
     public AssetURIAdapter(String serverName) {
         this.serverName = serverName;
     }
     
     @Override
     public AssetURI unmarshal(String uri) throws Exception {
         /*
          * Take the string URI, put is into a JarURI to parse it out, then
          * add in the server
          */
         AssetURI assetURI = new AssetURI(uri);
         if (uri != null) {
             String moduleName = assetURI.getModuleName();
             String rawPath = assetURI.getRawPath();
            assetURI = new AssetURI(moduleName, serverName, rawPath);
         }
         return assetURI;
     }
 
     @Override
     public String marshal(AssetURI assetURI) throws Exception {
         return assetURI.toString();
     }
 }
