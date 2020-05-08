 /*
  * Remoteroid Web Service
  * Copyright(c) 2012 Taeho Kim (jyte82@gmail.com)
  * 
  * This project aims to support 'Remote-connect' feature, 
  * which user can connect to the phone from PC, without any control on the phone.
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.secmem.remoteroid.lib.api;
 
 public class Codes {
 	
 	public static final int NONE = -100;
 	
 	public static class Result{
 		public static final int OK = 0;
 		public static final int FAILED = -1;
 	}
 	
 	public static class Error{
 		public static final int GENERAL = 0x000;
 		
 		public static class Account{
 			public static final int DUPLICATE_EMAIL = 0x100;
 			public static final int AUTH_FAILED = 0x101;
 		}
 		
 		public static class Device{
 			public static final int DUPLICATE_NAME = 0x200;
 			public static final int DEVICE_NOT_FOUND = 0x201;
 			
 		}
 	}
 }
