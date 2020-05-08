 /*
  * This file is part of verfluchter-android.
  *
  * verfluchter-android is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * verfluchter-android is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package pl.xsolve.verfluchter.tools;
 
 /**
  * @author Konrad Ktoso Malawski
  */
 public class Constants {
 
     public static final String VERSION = "0.1.3";
     public static final String USER_AGENT = "Verfluchter Android " + VERSION;
 
     // HTTP response codes
     public static final int HTTP_OK = 200; //OK
     public static final int HTTP_FOUND = 302; //Found, is also "ok"
     public static final int HTTP_BAD_REQUEST = 400; // Bad Request
     public static final int HTTP_UNAUTHORIZED = 401; // Unauthorized
     public static final int HTTP_FORBIDDEN = 403; // Forbidden
     public static final int HTTP_NOT_FOUND = 404;
     public static final int HTTP_I_AM_A_TEAPOT = 418; // I'm a teapot
     public static final int HTTP_INTERNAL_SERVER_ERROR = 500; // Internal Server Error
     public static final int HTTP_NOT_IMPLEMENTED = 501; // Not Implemented
     public static final int HTTP_SERVICE_UNAVAILABLE = 503; // Service Unavailable
     public static final int HTTP_VERSION_NOT_SUPPORTED = 505; // HTTP Version Not Supported
 
     public static final long MINUTE = 60 * 1000;
 
     public static final String DATE_FORMAT_YYYYMMDD = "yyyy-MM-dd";
 
     public static final String[] weekDays = new String[]{
             "Poniedziałek",
             "Wtorek",
             "Środa",
             "Czwartek",
             "Piątek",
             "Sobota",
            "Niedziela"};
 
     public static class DEFAULT {
         public static final String SERVER_DOMAIN = "verfluchter.xsolve.pl";
         public static final String BASIC_AUTH_USER = "user";
         public static final String BASIC_AUTH_PASS = "";
         public static final Boolean USE_REMINDER_SERVICE = true;
         public static final Boolean USE_REFRESHER_SERVICE = false;
         public static final Integer WORKING_HOURS_START_HOUR = 8;
         public static final Integer WORKING_HOURS_START_MIN = 0;
         public static final Integer WORKING_HOURS_END_HOUR = 16;
         public static final Integer WORKING_HOURS_END_MIN = 0;
         public static final Boolean USE_SOUND = false;
 
         private DEFAULT() {
         }
     }
 
     private Constants() {
     }
 
     public static final String PREFS = "prefz";
 }
