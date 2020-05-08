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
 package com.lolay.citygrid.tracking;
 
 public enum ActionTarget {
 	LISTING_PROFILE,
 	LISTING_REVIEW,
 	LISTING_MAP,
 	LISTING_DRIVING_DIRECTION,
 	LISTING_MAP_PRINT,
	LISTING_PROFILE_PRINT,
 	SEND_LISTING_EMAIL,
 	SEND_LISTING_PHONE,
 	OFFER,
 	PARTNER_MENU,
	PARNTER_RESERVATION;
 	
 	public String toString() {
 		return super.toString().toLowerCase();
 	}
 }
