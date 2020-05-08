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
 package om.tnavigator.auth.simple;
 
 import java.util.*;
 
 import om.tnavigator.auth.UserDetails;
 
/**
 * The implementation of UserDetails for a user authenticated by SimpleAuth.
 */
 public class SimpleUser implements UserDetails
 {
 	final static SimpleUser NOTLOGGEDIN=new SimpleUser(null,null,null);
 	
 	private String cookie,username,email;
	private Set<String> groups=new HashSet<String>();
 	
 	SimpleUser(String cookie,String username,String email)
 	{
 		this.cookie=cookie;
 		this.username=username;
 		this.email=email;
 	}
 	
 	void addGroup(String group)
 	{
 		groups.add(group);
 	}
 
 	public String getAuthIDsAsString()
 	{
 		StringBuffer result=new StringBuffer();
 		for(Iterator i=groups.iterator();i.hasNext();)
 		{
 			if(result.length()!=0) result.append(' ');
 			result.append((String)i.next());
 		}
 		return result.toString();
 	}
 
 	public String getCookie()
 	{
 		return cookie;
 	}
 
 	public String getPersonID()
 	{
 		return username;
 	}
 
 	public String getUsername()
 	{
 		return username;
 	}
 
 	public boolean hasAuthID(String sAuthId)	
 	{
 		return groups.contains(sAuthId);
 	}
 
 	public boolean isLoggedIn()
 	{
 		return this!=NOTLOGGEDIN;
 	}
 
 	public boolean shouldReceiveTestMail()
 	{
 		return email!=null;
 	}
 
 	public boolean isSysTest()
 	{
 		return username!=null && username.startsWith("!tst");
 	}
 
 }
