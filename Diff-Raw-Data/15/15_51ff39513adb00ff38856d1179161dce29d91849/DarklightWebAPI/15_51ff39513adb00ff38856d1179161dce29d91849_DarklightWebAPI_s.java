 package com.ijg.darklight.sdk.web.api;
 
 import java.util.HashMap;
 import java.util.Map.Entry;
 
 /*
  * Copyright (C) 2013  Isaac Grant
  * 
  * This file is part of the Darklight Nova Core.
  *  
  * Darklight Nova Core is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Darklight Nova Core is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with the Darklight Nova Core.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 public class DarklightWebAPI {
 	String protocol;
 	String server;
 	
 	/**
 	 * 
 	 * @param protocol Protocol used by the API server
 	 * @param server Web address of the API server
 	 */
 	public DarklightWebAPI(String protocol, String server) {
 		this.protocol = protocol;
 		this.server = server;
 	}
 	
 	/**
 	 * Create an individual session request
	 * @param sessionID ID of API session
 	 * @param endpoint Endpoint to which this request will be sent
 	 * @param parameters Paremeters with wich this request will be formatted
 	 * @return The generated APIRequest
 	 */
 	public APIRequest individualSessionRequest(APIEndpoint endpoint, HashMap<String, String> parameters) {
 		String query = makeQueryString(parameters);
 		APIRequest request = new APIRequest(protocol, server, APIEndpoint.INDIVIDUAL.endpoint() + endpoint.endpoint(), query);
 		return request;
 	}
 	
 	/**
 	 * Utility to create a query string based off of a HashMap of parameters
 	 * @param parameters HashMap of query strings
 	 * @return Formatted string suitable to be prepended to a URL
 	 */
 	private String makeQueryString(HashMap<String, String> parameters) {
 		StringBuilder query = new StringBuilder();
 		for (Entry<String, String> pair : parameters.entrySet()) {
 			query.append(pair.getKey() + "=" + pair.getValue() + "&");
 		}
 		return query.toString();
 	}
 }
