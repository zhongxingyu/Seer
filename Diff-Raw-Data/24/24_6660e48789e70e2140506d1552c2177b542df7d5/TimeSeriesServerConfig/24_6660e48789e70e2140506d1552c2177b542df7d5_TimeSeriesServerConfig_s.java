 /**
  * Copyright (C) 2011 (nick @ objectdefinitions.com)
  *
  * This file is part of JTimeseries.
  *
  * JTimeseries is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * JTimeseries is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with JTimeseries.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.od.jtimeseries.ui.config;
 
 import com.od.jtimeseries.net.udp.TimeSeriesServer;
 
 import java.net.UnknownHostException;
 
 /**
 * Created by IntelliJ IDEA.
 * User: nick
 * Date: 05/01/11
 * Time: 18:22
 * To change this template use File | Settings | File Templates.
 */
 public class TimeSeriesServerConfig {
 
     private String hostName;
     private int port;
     private String description;
 
     public TimeSeriesServerConfig(String hostName, int port, String description) {
         this.hostName = hostName;
         this.port = port;
         this.description = description;
     }
 
     public String getHostName() {
         return hostName;
     }
 
     public int getPort() {
         return port;
     }
 
     public String getDescription() {
         return description;
     }
 
     public TimeSeriesServer createServer() throws UnknownHostException {
         return new TimeSeriesServer(
                 hostName,
                 port,
                 description
         );
     }
 
 }
