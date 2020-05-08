 /* OpenMark online assessment system
    Copyright (C) 2007 The Open University
 
    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.
 
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
 
    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  */
 package om.tnavigator.auth;
 
 import java.io.IOException;
 import java.util.Map;
 
 import javax.servlet.http.*;
 
 
 /** Interface to handle authentication */
 public interface Authentication
 {
 	/**
 	 * Obtains details for the user and checks that they are authenticated.
 	 * If getUncheckedUserDetails would have returned valid data, but 
 	 * getUserDetails discovers that this is correct, the situation should be
 	 * changed so that in future (e.g. after a redirect) getUncheckedUserDetails
 	 * will return null. Or to put that another way: if the authentication cookie
 	 * turns out to be invalid, clear it here.
 	 * @param request HTTP request from user
 	 * @param response HTTP response (used only if redirecting or to clear
 	 *   cookies)
 	 * @param bRequireLogin True if login is required and therefore invalid
 	 *   logins should be redirected
 	 * @return User details or null if user has been redirected and other
 	 *   processing should cease (applies only if bRequireLogin is set)
 	 * @throws IOException If there is an authentication error
 	 */
   public UserDetails getUserDetails(
   		HttpServletRequest request,HttpServletResponse response,
 		boolean bRequireLogin) throws IOException;
   
   /**
    * Obtain basic details for the user without checking that they are
    * authenticated. Should be a fast operation compared to getUserDetails,
    * ideally requiring no database access. (In other words, if you can get
    * their username from the cookie directly.)
    * @param request HTTP request
    * @return User details (all set to null if they aren't logged in)
    */
   public UncheckedUserDetails getUncheckedUserDetails(
   		HttpServletRequest request);
   
   /**
    * Set up this user's browser with a cookie that will mark them out as
    * one of the test users.
    * @param response HTTP response for setting cookies
    * @param suffix Test user ID
    */
   public void becomeTestUser(HttpServletResponse response,String suffix);
   
   /** Close the authentication system when it will not be used again */
   public void close();
   
   /** 
    * Sends redirect to login page.
    * @param request HTTP request (will come back here later)
    * @param response HTTP response
    * @throws IOException Any error in sending redirect
    */
   public void redirect(HttpServletRequest request,HttpServletResponse response) throws IOException;  
 
   /** Constant used for 'submit confirmation' email */
   public final static int EMAIL_CONFIRMSUBMIT=1;
   
   /**
    * Sends email to a user. Email will only be sent according to the defined
    * EMAIL_xx types.
    * @param username Username of recipient
    * @param personID Person ID of recipient
    * @param email Content of email (text only). First line is subject.
    * @param emailType EMAIL_xx constant.
    * @return Optional information about mail send, for log. (Use "" if none.)
    * @throws IOException If there is any problem sending the email
    */
   public String sendMail(String username,String personID,String email,int emailType)
     throws IOException;
   
   /**
    * Obtains text used to offer the chance for local users to log in when accessing
    * a test that is world-visible so doesn't require them to log in. (The
    * advantage of logging in is that your progress can be stored if you move
    * to a different computer.)
    * @param request HTTP request
    * @return XHTML text offering login chance, including link to login page
    * @throws IOException If there is any problem
    */
   public String getLoginOfferXHTML(HttpServletRequest request) throws IOException;
   
   /**
    * Add performance information for status page (if any) to the map. Map
    * names should match tokens on the status page.
    * @param m Performance map
    */
  public void obtainPerformanceInfo(Map m);
   
   /**
    * Handle a user request inside the /!auth/ path. Should only be used to offer
    * necessary login screens and suchlike.
    * @param subPath Path after /!auth/
    * @param request 
    * @param response
    * @return True if request URL is handled, false if it's not
    * @throws Exception Exceptions will be displayed as server errors
    */
   public boolean handleRequest(String subPath,
   		HttpServletRequest request,HttpServletResponse response) throws Exception;
 }
