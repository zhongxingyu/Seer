 /*
  * Copyright (C) 2013 Arix
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.arix;
 
 import com.arix.data.ArxFile;
 import com.arix.data.StructureSetup;
 import com.arix.utils.Log;
 
 /**
  *
  * @author Arix
  */
 public class Server {
 
     private ArxFile serverConfig;
 
     private Server() {
         //TODO: load system-important plugins.
 
     }
 
     public void setConfig(ArxFile af) {
         serverConfig = af;
 
     }
     
     public ArxFile getConfig() {
         return serverConfig;
     }
 
     public void test() {
         System.out.println("Tested");
     }
 
     public static void init() {
         getServer();
         StructureSetup.checkStructureIntegrity();
     }
 
     public static Server getServer() {
         return ServerHolder.INSTANCE;
     }
 
     private static class ServerHolder {
 
         private static final Server INSTANCE = new Server();
     }
 }
