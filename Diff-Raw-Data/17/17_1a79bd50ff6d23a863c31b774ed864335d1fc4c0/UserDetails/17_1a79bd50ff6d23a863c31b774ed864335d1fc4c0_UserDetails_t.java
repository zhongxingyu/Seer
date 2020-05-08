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
 
 /** Information about a user */
 public interface UserDetails
 {
 	/** Prefix used for test OUCUs */
 	final static String SYSTESTPREFIX="!tst";
 	/** Default user details for user who isn't logged in */
 	public static UserDetails NULLDETAILS=new NullUserDetails();
 	/** Fake student user details for prebuild etc */
 	public static UserDetails FAKESTUDENTDETAILS=new FakeUserDetails();
 	
	/**
	 * Implementation of UserDetails that returns "" or false for everything. Used when we ask
	 * for details of a user who is not logged in.
	 */
 	static class NullUserDetails implements UserDetails
 	{
 		public String getAuthIDsAsString()
 		{
 			return "";
 		}
 
 		public String getCookie()
 		{
 			return "";
 		}
 
 		public String getPersonID()
 		{
 			return "";
 		}
 
 		public String getUsername()
 		{
 			return "";
 		}
 
 		public boolean hasAuthID(String sAuthId)
 		{
 			return false;
 		}
 
 		public boolean isLoggedIn()
 		{
 			return false;
 		}
 
 		public boolean shouldReceiveTestMail()
 		{
 			return false;
 		}
 
 		public boolean isSysTest()
 		{
 			return false;
 		}
 	}
 	
	/**
	 * Implementation of UserDetails for a fake user. Does not seem to be used.
	 */
 	static class FakeUserDetails implements UserDetails
 	{
 		public String getAuthIDsAsString()
 		{
 			return "";
 		}
 
 		public String getCookie()
 		{
 			return "";
 		}
 
 		public String getPersonID()
 		{
 			return "!fake";
 		}
 
 		public String getUsername()
 		{
 			return "!fake";
 		}
 
 		public boolean hasAuthID(String sAuthId)
 		{
 			return false;
 		}
 
 		public boolean isLoggedIn()
 		{
 			return false;
 		}
 
 		public boolean shouldReceiveTestMail()
 		{
 			return true;
 		}
 
 		public boolean isSysTest()
 		{
 			return false;
 		}
 	}
 	
 	/** @return Username or empty string if user is not logged in */
 	public String getUsername();
 	
 	/** @return Person ID or empty string if user isn't logged on */
 	public String getPersonID();
 	
 	/** @return Authentication cookie or empty string if user is not logged in */
 	public String getCookie();
 	
	/** @return whether user is logged in at all */
 	public boolean isLoggedIn();
 	
 	/** @return True if user should receive emails confirming submission 
 	 *  (where enabled in test) i.e. you can return false if somebody isn't
 	 *  an actual student, if you like. Anyone marked admin automatically doesn't
 	 *  get the emails. */ 
 	public boolean shouldReceiveTestMail();
 	
 	/** @return True if this is a test user */
 	public boolean isSysTest();
 	
	/**
	 * @param sAuthId the authid to check.
	 * @return True if user has given authid
	 */
 	public boolean hasAuthID(String sAuthId);
 
 	/**
 	 * Intended for use in XSL; converts authids to a space-separated list.
 	 * @return Space-separated list of all authids.
 	 */
 	public String getAuthIDsAsString();	
 }
