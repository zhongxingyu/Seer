 /**
  * JoeReturnCodes interface
  * 
  * Provides a set of return code constants for JOE methods
  * 
  * Copyright (C) 2001 Stan Krute <Stan@StanKrute.com>
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or 
  * without modification, are permitted provided that the 
  * following conditions are met:
  * 
  *  - Redistributions of source code must retain the above copyright 
  *    notice, this list of conditions and the following disclaimer. 
  * 
  *  - Redistributions in binary form must reproduce the above 
  *    copyright notice, this list of conditions and the following 
  *    disclaimer in the documentation and/or other materials provided 
  *    with the distribution. 
  * 
  *  - Neither the names "Java Outline Editor", "JOE" nor the names of its 
  *    contributors may be used to endorse or promote products derived 
  *    from this software without specific prior written permission. 
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT 
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS 
  * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE 
  * REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, 
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, 
  * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT 
  * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN 
  * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
  * POSSIBILITY OF SUCH DAMAGE.
  */
 
 // we're part of this
 package com.organic.maynard.outliner;
 
 // Provide a set of return code constants for JOE programming
 public interface JoeReturnCodes {
 	
 	// standard classics
 	public static final int SUCCESS = 1 ;
 	public static final int FAILURE = 0 ;
 	
 	// set success codes > 1, error/failure codes < 0
 
 	// try to allocate additions with thoughts of future expansion and general elbow room
 	// try to group similars
 	
 
 	// success codes
 	
 	// 1xx success flavors
 	public static final int SUCCESS_MODIFIED = 100;		// from OpenFileFormat
 	
 
 	// failure codes
 	
 	// -1xx boundary errors
 	public static final int ARRAY_SELECTOR_OUT_OF_BOUNDS = -100 ;
 	
 	// -2xx document errors
 	public static final int DOCUMENT_NOT_FOUND = -200 ;
 	public static final int DOCUMENT_IN_USE_ELSEWHERE = -210 ;
 	
 	// -3xx memory errors
 	public static final int UNABLE_TO_ALLOCATE_MEMORY = -300 ;
 	
 	// -4xx printer errors
 	public static final int PRINTER_COMMUNICATION_FAILURE = -400 ;
 	
 	// -5xx arithmetic errors
 	public static final int ATTEMPT_TO_DIVIDE_BY_ZERO = -500 ;
 	
 	// -6xx user actions
 	public static final int USER_ABORTED = -600 ;
 	public static final int FAILURE_USER_ABORTED = -600;  	// from OpenFileFormat
 	
 	// -7xx internet errors
 	public static final int URL_NOT_FOUND = -700 ;
 
 	// -8xx nullness errors
 	public static final int NULL_OBJECT_REFERENCE = -800 ;
 	
 	} // end interface JoeReturnCodes
