 /*
  * FlightIntel for Pilots
  *
  * Copyright 2011-2012 Nadeem Hasan <nhasan@nadmm.com>
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
 
 package com.nadmm.airports.wx;
 
 import java.io.File;
 import java.net.URI;
 
 import org.apache.http.client.utils.URIUtils;
 
 import android.content.Intent;
 
 import com.nadmm.airports.AirportsMain;
 import com.nadmm.airports.utils.UiUtils;
 
 public class MetarService extends NoaaService {
 
     private final String METAR_QUERY = "datasource=metars&requesttype=retrieve"
     		+"&format=xml&compression=gzip&hoursBeforeNow=3&mostRecent=true&stationString=";
     private final File METAR_DIR = new File(
             AirportsMain.EXTERNAL_STORAGE_DATA_DIRECTORY, "/metar" );
     private final int METAR_CACHE_MAX_AGE = 30*MSECS_PER_MINUTE;
 
     protected MetarParser mParser;
 
     public MetarService() {
         super( "MetarService" );
         mParser = new MetarParser();
     }
 
     @Override
     public void onCreate() {
         super.onCreate();
 
         if ( !METAR_DIR.exists() ) {
             METAR_DIR.mkdirs();
         }
         // Remove any old METAR files from cache first
         cleanupCache( METAR_DIR, METAR_CACHE_MAX_AGE );
     }
 
     @Override
     protected void onHandleIntent( Intent intent ) {
        if ( !intent.getAction().equals( ACTION_GET_METAR ) ) {
             return;
         }
 
         // Get request parameters
         String stationId = intent.getStringExtra( STATION_ID );
         boolean cacheOnly = intent.getBooleanExtra( CACHE_ONLY, false );
         boolean forceRefresh = intent.getBooleanExtra( FORCE_REFRESH, false );
 
         File xml = new File( METAR_DIR, "METAR_"+stationId+".xml" );
         if ( forceRefresh || ( !cacheOnly && !xml.exists() ) ) {
             fetchMetarFromNOAA( stationId, xml );
         }
 
         Metar metar = new Metar();
 
         if ( xml.exists() ) {
             metar.stationId = stationId;
             mParser.parse( xml, metar );
         }
 
         // Broadcast the result
         Intent result = new Intent();
         result.setAction( ACTION_GET_METAR );
         result.putExtra( STATION_ID, stationId );
         result.putExtra( RESULT, metar );
         sendBroadcast( result );
     }
 
     protected boolean fetchMetarFromNOAA( String stationId, File xml ) {
        boolean result = false;
         try {
             URI uri = URIUtils.createURI( "http", NOAA_HOST, 80, DATASERVER_PATH,
                     METAR_QUERY+stationId, null );
            result = fetchFromNOAA( uri, xml );
         } catch ( Exception e ) {
             UiUtils.showToast( this, "Unable to fetch METAR: "+e.getMessage() );
         }
        return result;
     }
 
 }
