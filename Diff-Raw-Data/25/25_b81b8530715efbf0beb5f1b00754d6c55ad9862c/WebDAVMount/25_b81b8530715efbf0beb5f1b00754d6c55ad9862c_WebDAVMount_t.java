 
 				/*
  *  Adito
  *
  *  Copyright (C) 2003-2006 3SP LTD. All Rights Reserved
  *
  *  This program is free software; you can redistribute it and/or
  *  modify it under the terms of the GNU General Public License
  *  as published by the Free Software Foundation; either version 2 of
  *  the License, or (at your option) any later version.
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public
  *  License along with this program; if not, write to the Free Software
  *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
  */
 
 package com.adito.networkplaces.store.webdav;
 
 import java.io.IOException;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.commons.vfs.FileObject;
 import org.apache.commons.vfs.FileSystemException;
 
 import com.adito.networkplaces.AbstractNetworkPlaceMount;
 import com.adito.policyframework.LaunchSession;
 import com.adito.security.PasswordCredentials;
 import com.adito.vfs.VFSStore;
 import com.adito.vfs.utils.URI;
 import com.adito.vfs.webdav.DAVAuthenticationRequiredException;
 import com.adito.vfs.webdav.DAVUtilities;
 
 
 public class WebDAVMount extends AbstractNetworkPlaceMount {
 
     final static Log log = LogFactory.getLog(WebDAVMount.class);
 
     public WebDAVMount(LaunchSession launchSession, VFSStore store) {
         super(launchSession, store);
     }
 
     public FileObject createVFSFileObject(String path, PasswordCredentials credentials) throws IOException {
         try {
             URI uri = getRootVFSURI();
             if (credentials != null) {
                 uri.setUserinfo(DAVUtilities.encodeURIUserInfo(credentials.getUsername() + (credentials.getPassword() != null ? ":" + new String(credentials.getPassword()) : "")));
             }
             uri.setPath(uri.getPath() + (uri.getPath().endsWith("/") ? "" : "/") + DAVUtilities.encodePath(path));
 
			FileObject fileObject = this.getStore().getRepository().getFileSystemManager().resolveFile(uri.toString());
             return fileObject;
         } catch (FileSystemException fse) {
             if (fse.getCode().equals("vfs.provider.ftp/connect.error")) {
                 throw new DAVAuthenticationRequiredException(getMountString());
             }
             throw fse;
         }
     }
 }
