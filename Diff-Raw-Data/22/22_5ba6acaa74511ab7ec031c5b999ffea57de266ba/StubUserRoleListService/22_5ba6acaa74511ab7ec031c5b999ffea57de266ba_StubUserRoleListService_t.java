 /*
  * This program is free software; you can redistribute it and/or modify it under the 
  * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software 
  * Foundation.
  *
  * You should have received a copy of the GNU Lesser General Public License along with this 
  * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html 
  * or from the Free Software Foundation, Inc., 
  * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
  * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU Lesser General Public License for more details.
  *
  * Copyright 2006 - 2012 Pentaho Corporation.  All rights reserved.
  *
  */
 package org.pentaho.platform.plugin.kettle;
 
 import java.util.Arrays;
 import java.util.List;
 
 import org.pentaho.platform.api.engine.IUserRoleListService;
 import org.pentaho.platform.api.mt.ITenant;
 
 @SuppressWarnings( { "nls", "unchecked" })
 public class StubUserRoleListService implements IUserRoleListService {
 
   public List getAllRoles() {
     // TODO Auto-generated method stub
     return null;
   }
 
   public List getAllUsers() {
     // TODO Auto-generated method stub
     return null;
   }
 
   public List getUsersInRole(String role) {
     // TODO Auto-generated method stub
     return null;
   }
 
   public List getRolesForUser(String userName) {
     return Arrays.asList("FL_GATOR", "FS_SEMINOLE");
   }
 
   @Override
   public List<String> getAllRoles(ITenant arg0) {
     // TODO Auto-generated method stub
     return null;
   }
 
   @Override
   public List<String> getAllUsers(ITenant arg0) {
     // TODO Auto-generated method stub
     return null;
   }
 
   @Override
   public List<String> getRolesForUser(ITenant arg0, String arg1) {
    return Arrays.asList("FL_GATOR", "FS_SEMINOLE");
  }

  @Override
  public List<String> getUsersInRole(ITenant arg0, String arg1) {
     // TODO Auto-generated method stub
     return null;
   }
 
   @Override
  public List<String> getSystemRoles() {
     // TODO Auto-generated method stub
     return null;
   }
 
 }
