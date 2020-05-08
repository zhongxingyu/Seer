 /**
  * Java API for management of GlassFish servers.
  * Copyright (C) 2010 Patrik Boström
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  */
 
 package se.glassfish.asadmin.api;
 
 import java.io.*;
 
 public class PasswordFile {
 
     private File file;
 
     public PasswordFile(String masterPassword, String adminPassword, String aliasPassword, String userPassword, Version version) throws IOException {
         file = File.createTempFile("pass", "gf");
         BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, false)));
        if (version.equals(Version.V3)) {
             writer.write("AS_ADMIN_PASSWORD=" + adminPassword);
         } else {
             writer.write("AS_ADMIN_ADMINPASSWORD=" + adminPassword);
         }
         writer.newLine();
         writer.write("AS_ADMIN_MASTERPASSWORD=" + masterPassword);
         writer.newLine();
         writer.write("AS_ADMIN_PASSWORD=" + adminPassword);
         writer.newLine();
         if (aliasPassword != null) {
             writer.write("AS_ADMIN_ALIASPASSWORD=" + aliasPassword);
             writer.newLine();
         }
         if (userPassword != null) {
             writer.write("AS_ADMIN_USERPASSWORD=" + userPassword);
             writer.newLine();
         }
 
 
         writer.flush();
         writer.close();
     }
 
     public boolean delete() {
         return file.delete();
     }
 
     public String getLocation() {
         return file.getAbsolutePath();
     }
 
 }
