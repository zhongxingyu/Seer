 /*
  * Copyright (C) 2011  Southern Storm Software, Pty Ltd.
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
 
 package com.southernstorm.tvguide;
 
 import java.util.Calendar;
 
 public interface TvNetworkListener {
 
     /**
      * Sets the current network request channel and date.
      *
      * @param channel the channel that is being fetched
      * @param date the date that is being fetched
      * @param primaryDate the primary date for multi-day requests
      */
     public void setCurrentNetworkRequest(TvChannel channel, Calendar date, Calendar primaryDate);
 
     /**
      * Indicates that there are no more network requests pending.
      */
     public void endNetworkRequests();
 
     /**
      * Reports that previously requested data is now available in the cache.
      *
      * @param channel the channel
      * @param date the date that was successfully fetched
      * @param primaryDate the primary date for multi-day requests
      */
     public void dataAvailable(TvChannel channel, Calendar date, Calendar primaryDate);
 
     /**
      * Reports that a data request has failed.
      *
      * @param channel the channel
      * @param date the date that failed to be fetched
     * @param primaryDate the primary date for multi-day requests
      */
     public void requestFailed(TvChannel channel, Calendar date, Calendar primaryDate);
 }
