 /**
  * Copyright (C) 2011-2013 Barchart, Inc. <http://www.barchart.com/>
  *
  * All rights reserved. Licensed under the OSI BSD License.
  *
  * http://www.opensource.org/licenses/bsd-license.php
  */
 package com.barchart.http.request;
 
 /**
  * Key for storing and retrieving request attributes.
  */
 public class RequestAttributeKey<T> {
 
	private final String name;
 
 	public RequestAttributeKey(final String name_) {
 		name = name_;
 	}
 
 }
