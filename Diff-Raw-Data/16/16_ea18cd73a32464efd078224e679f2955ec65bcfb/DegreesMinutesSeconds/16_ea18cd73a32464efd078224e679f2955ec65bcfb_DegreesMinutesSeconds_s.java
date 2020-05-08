 /*******************************************************************************
  * Copyright (c) 2008, 2009 Bug Labs, Inc.
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *    - Redistributions of source code must retain the above copyright notice,
  *      this list of conditions and the following disclaimer.
  *    - Redistributions in binary form must reproduce the above copyright
  *      notice, this list of conditions and the following disclaimer in the
  *      documentation and/or other materials provided with the distribution.
  *    - Neither the name of Bug Labs, Inc. nor the names of its contributors may be
  *      used to endorse or promote products derived from this software without
  *      specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  *******************************************************************************/
 package com.buglabs.nmea;
 
 import com.buglabs.nmea.sentences.NMEAParserException;
 
 /**
  * A class to store DMS style location information.
  * 
  * @author aroman
  * 
  */
 public class DegreesMinutesSeconds {
 
 	private String dms;
 	private char direction = '?';
 
 	/**
 	 * Will throw NMEAParserException if parsing error occurs.
 	 * 
 	 * @param dms
 	 */
 	public DegreesMinutesSeconds(String dms) {
 		if (dms == null || dms.length() == 0 || dms.indexOf(',') == -1 || dms.indexOf('.') == -1) {
 			throw new NMEAParserException("Unable to parse DMS: " + dms);
 		}
 
 		int i = dms.indexOf(",");
 		this.dms = dms.substring(0, i);
 
 		if (dms.length() > i + 1) {
 			direction = dms.charAt(i + 1);
 		}
 	}
 
 	/**
 	 * @return Degrees part of DMS
 	 */
 	public double getDegrees() {
 		int i = dms.indexOf(".");
 
 		if (i < 2) {
 			throw new NMEAParserException("Unable to calculate degrees from " + dms);
 		}
 
 		double rawVal = Double.parseDouble(dms.substring(0, i - 2));
 
 		// According to
 		// http://www.nationalatlas.gov/articles/mapping/a_latlong.html
 		// S and W directional designators offer negative values for degrees.
 		if (direction == 'S' || direction == 'W') {
 			rawVal = rawVal * -1;
 		}
 
 		return rawVal;
 	}
 
 	/**
 	 * @return Minutes part of DMS value
 	 */
 	public double getMinutes() {
 		int i = dms.indexOf(".");
 
 		if (i < 2) {
 			throw new NMEAParserException("Unable to calculate minutes from " + dms);
 		}
 
 		return Double.parseDouble(dms.substring(i - 2, i));
 	}
 
 	/**
 	 * @return Seconds part of DMS value
 	 */
 	public double getSeconds() {
 		int i = dms.indexOf(".");
 
 		String seconds = dms.substring(i);
 
 		return Double.parseDouble(seconds) * 60;
 	}
 
 	/**
 	 * @return Direction char for unit. Can be N/S/E/W.
 	 */
 	public char getDirection() {
 		return direction;
 	}
 
 	/**
 	 * Convert DegreesMinutesSeconds to decimal degrees
 	 * 
 	 * @return
 	 */
 	public double toDecimalDegrees() {
 		double decimal = getSeconds() / 60.0;
 		double result = (getMinutes() + decimal) / 60.0;
 
 		double degrees = getDegrees();
 
 		if (degrees < 0.0) {
 			return degrees - result;
 		}
 
 		return degrees + result;
 	}
 }
