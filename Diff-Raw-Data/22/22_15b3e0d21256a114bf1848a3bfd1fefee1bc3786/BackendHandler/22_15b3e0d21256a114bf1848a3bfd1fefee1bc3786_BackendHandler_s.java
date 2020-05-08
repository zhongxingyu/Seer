 /*
  * Copyright (c) 2010 StockPlay development team
  * All rights reserved.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 package com.kapti.backend.api.system;
 
 import com.kapti.backend.api.MethodClass;
 import com.kapti.backend.xmlrpc.Servlet;
 import com.kapti.backend.xmlrpc.ServletServer;
 import com.kapti.cache.Manager;
 import com.kapti.exceptions.StockPlayException;
 import java.util.HashMap;
 import org.apache.xmlrpc.XmlRpcException;
 
 /**
  * \brief   Handler van de System.Backend subklasse.
  *
  * Deze klasse is de handler van de System.Backend subklasse. Ze staat in
  * voor de verwerking van aanroepen van functies die zich in deze klasse
  * bevinden, lokaal de correcte aanvragen uit te voeren, en het resultaat
  * op conforme wijze terug te sturen.
  */
 public class BackendHandler extends MethodClass {
     //
     // Methodes
     //
 
     public int Status() throws XmlRpcException {
         return 1;
     }
 
     public boolean Stop() throws XmlRpcException {
         return false;
     }
 
     public boolean Restart() throws XmlRpcException {
         return true;
     }
 
     public HashMap<String, Object> Stats() throws StockPlayException {
         HashMap<String, Object> oStats = new HashMap<String, Object>();
        oStats.put("users", 5);
         oStats.put("req", ServletServer.getRequests());
         oStats.put("uptime", Long.toString(Servlet.getUptime()));
         return oStats;
     }
 
     public boolean ClearCache() {
         Manager.clear();
         return true;
     }
 }
