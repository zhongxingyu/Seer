 /*
  * Licensed to Lolay, Inc. under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  Lolay, Inc. licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  * http://github.com/lolay/citygrid/raw/master/LICENSE
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.    
  */
 package com.lolay.citygrid;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.ResourceBundle;
 
 public enum ErrorCode {
 	QUERY_UNDERSPECIFIED("query.underspecified"),
 	QUERY_TYPE_UNKNOWN("query.type.unknown"),
 	QUERY_OVERSPECIFIED("query.overspecified"),
 	GEOGRAPHY_UNDERSPECIFIED("geography.underspecified"),
 	GEOGRAPHY_OVERSPECIFIED("geography.ovespecified"),
 	RADIUS_REQUIRED("radius.required"),
 	DATE_PAST("date.past"),
 	DATE_RANGE_INCOMPLETE("date.range.incomplete"),
 	DATE_RANGE_TOO_LONG("date.range.to.long"),
 	GEOCODE_FAILURE("geocode.failure"),
 	TAG_ILLEGAL("tag.illegal"),
 	CHAIN_ILLEGAL("chain.illegal"),
 	FIRST_ILLEGAL("first.illegal"),
 	LATITUDE_ILLEGAL("latitude.illegal"),
 	LONGITUDE_ILLEGAL("longitude.illegal"),
 	RADIUS_ILLEGAL("radius.illegal"),
 	PAGE_ILLEGAL("page.illegal"),
 	RESULTS_PER_PAGE_ILLEGAL("rpp.illegal"),
 	FROM_ILLEGAL("from.illegal"),
 	TO_ILLEGAL("to.illegal"),
 	SORT_ILLEGAL("sort.illegal"),
 	RADIUS_OUT_OF_RANGE("radius.out.of.range"),
 	PAGE_OUT_OF_RANGE("page.out.of.range"),
 	RESULTS_PER_PAGE_OUT_OF_RANGE("rpp.out.of.range"),
 	PUBLISHER_REQUIRED("publisher.required"),
	INTERNAL_ERROR("internal.error"),
	LISTING_NOT_FOUND("listing.not.found");
 	
 	private static ResourceBundle bundle = ResourceBundle.getBundle("com.lolay.citygrid.ErrorCode");
 	private static Map<String,ErrorCode> codeMap;
 	private String code = null;
 
 	private ErrorCode(String code) {
 		this.code = code;
 	}
 	
 	public String getCode() {
 		return code;
 	}
 	
 	public String getMessage() {
 		return bundle.containsKey(getCode()) ? bundle.getString(getCode()) : "Code not in ErrorCode.properties";
 	}
 	
 	public static ErrorCode fromCode(String code) {
 		if (codeMap.containsKey(code)) {
 			return codeMap.get(code);
 		} else {
 			throw new IllegalArgumentException(String.format("Could not find %s for %s", ErrorCode.class.getSimpleName(), code));
 		}
 	}
 	
 	static {
 		codeMap = new HashMap<String,ErrorCode>(25);
 		codeMap.put("query.underspecified", QUERY_UNDERSPECIFIED);
 		codeMap.put("query.type.unknown", QUERY_TYPE_UNKNOWN);
 		codeMap.put("query.overspecified", QUERY_OVERSPECIFIED);
 		codeMap.put("geography.underspecified", GEOGRAPHY_UNDERSPECIFIED);
 		codeMap.put("geography.ovespecified", GEOGRAPHY_OVERSPECIFIED);
 		codeMap.put("radius.required", RADIUS_REQUIRED);
 		codeMap.put("date.past", DATE_PAST);
 		codeMap.put("date.range.incomplete", DATE_RANGE_INCOMPLETE);
 		codeMap.put("date.range.to.long", DATE_RANGE_TOO_LONG);
 		codeMap.put("geocode.failure", GEOCODE_FAILURE);
 		codeMap.put("tag.illegal", TAG_ILLEGAL);
 		codeMap.put("chain.illegal", CHAIN_ILLEGAL);
 		codeMap.put("first.illegal", FIRST_ILLEGAL);
 		codeMap.put("latitude.illegal", LATITUDE_ILLEGAL);
 		codeMap.put("longitude.illegal", LONGITUDE_ILLEGAL);
 		codeMap.put("radius.illegal", RADIUS_ILLEGAL);
 		codeMap.put("page.illegal", PAGE_ILLEGAL);
 		codeMap.put("rpp.illegal", RESULTS_PER_PAGE_ILLEGAL);
 		codeMap.put("from.illegal", FROM_ILLEGAL);
 		codeMap.put("to.illegal", TO_ILLEGAL);
 		codeMap.put("sort.illegal", SORT_ILLEGAL);
 		codeMap.put("radius.out.of.range", RADIUS_OUT_OF_RANGE);
 		codeMap.put("page.out.of.range", PAGE_OUT_OF_RANGE);
 		codeMap.put("rpp.out.of.range", RESULTS_PER_PAGE_OUT_OF_RANGE);
 		codeMap.put("publisher.required", PUBLISHER_REQUIRED);
 		codeMap.put("internal.error", INTERNAL_ERROR);
 	}
 }
