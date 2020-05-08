 /*
  * Copyright (C) 2011 - Jingle Nodes - Yuilop - Neppo
  *
  *   This file is part of Switji (http://jinglenodes.org)
  *
  *   Switji is free software; you can redistribute it and/or modify
  *   it under the terms of the GNU General Public License as published by
  *   the Free Software Foundation; either version 2 of the License, or
  *   (at your option) any later version.
  *
  *   Switji is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *   GNU General Public License for more details.
  *
  *   You should have received a copy of the GNU General Public License
  *   along with MjSip; if not, write to the Free Software
  *   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  *   Author(s):
  *   Benhur Langoni (bhlangonijr@gmail.com)
  *   Thiago Camargo (barata7@gmail.com)
  */
 
 package org.jinglenodes.credit;
 
 /**
  * Created by IntelliJ IDEA.
  * User: thiago
  * Date: 3/26/12
  * Time: 6:09 PM
  */
 public class SessionCredit {
     private int maxDurationInSeconds = -1;
     private RouteType routeType = RouteType.ip;
    private long startTime;
     private long finishTime;
     private boolean charged=false;
 
     public SessionCredit(RouteType routeType) {
         this.routeType = routeType;
     }
 
     public RouteType getRouteType() {
         return routeType;
     }
 
     public long getFinishTime() {
         return finishTime;
     }
 
     public void setFinishTime(long finishTime) {
         this.finishTime = finishTime;
     }
 
     public long getStartTime() {
         return startTime;
     }
 
     public void setStartTime(long startTime) {
         this.startTime = startTime;
     }
 
     public int getMaxDurationInSeconds() {
         return maxDurationInSeconds;
     }
 
     public void setMaxDurationInSeconds(int maxDurationInSeconds) {
         this.maxDurationInSeconds = maxDurationInSeconds;
     }
 
     public boolean isCharged() {
         return charged;
     }
 
     public void setCharged(boolean charged) {
         this.charged = charged;
     }
 
     public enum RouteType {
         ip, pstn
     }
 }
