 /*
  * Copyright 2010 Andrew De Quincey -  adq@lidskialf.net
  * This file is part of rEdBus.
  *
  *  rEdBus is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  rEdBus is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with rEdBus.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.redbus;
 
 import java.util.List;
 import java.util.Locale;
 
 import android.content.Context;
 import android.location.Address;
 import android.location.Geocoder;
 import android.os.AsyncTask;
 import android.util.Log;
 
 public class GeocodingHelper {
 
 	private static Integer RequestId = new Integer(0);
 
 	public static int geocode(Context ctx, String location, GeocodingResponseListener callback)
 	{
 		int requestId = RequestId++;
 		
 		new AsyncGeocodeRequestTask().execute(new GeocodingRequest(requestId, ctx, location, callback));		
 		
 		return requestId;
 	}
 	
 	private static class AsyncGeocodeRequestTask extends AsyncTask<GeocodingRequest, Integer, GeocodingRequest> {
 		
 		protected GeocodingRequest doInBackground(GeocodingRequest... params) {
 			GeocodingRequest gr = params[0];
 			gr.addresses = null;
 			
 			PointTree pt = PointTree.getPointTree(gr.ctx);
 			
 			try {
 				Geocoder geocoder = new Geocoder(gr.ctx, Locale.UK);
				gr.addresses = geocoder.getFromLocationName(gr.location, 5, pt.lowerLeftLat, pt.lowerLeftLon, pt.upperRightLat, pt.upperRightLon);
 			} catch (Throwable t) {
 				Log.e("AsyncHttpRequestTask.doInBackGround", "Throwable", t);
 			}
 			
 			return gr;
 		}
 
 		protected void onPostExecute(GeocodingRequest request) {
 			if ((request.addresses == null) || (request.addresses.size() == 0)) {
 				request.callback.geocodeResponseError(request.requestId, "Could not find address...");
 			} else {
 				request.callback.geocodeResponseSucccess(request.requestId, request.addresses);
 			}
 		}
 	}
 	
 	private static class GeocodingRequest {
 		
 		public GeocodingRequest(int requestId, Context ctx, String location, GeocodingResponseListener callback)
 		{
 			this.requestId = requestId;
 			this.ctx = ctx;
 			this.location = location;
 			this.callback = callback;
 		}
 		
 		public int requestId;
 		public Context ctx;
 		public String location;
 		public GeocodingResponseListener callback;
 		public List<Address> addresses = null;
 	}
 }
