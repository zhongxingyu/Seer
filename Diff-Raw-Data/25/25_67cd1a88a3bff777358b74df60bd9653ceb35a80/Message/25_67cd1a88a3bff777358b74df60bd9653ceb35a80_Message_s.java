 /**
  *     Copyright SocialSite (C) 2009
  *
  *     This program is free software: you can redistribute it and/or modify
  *     it under the terms of the GNU General Public License as published by
  *     the Free Software Foundation, either version 3 of the License, or
  *     (at your option) any later version.
  *
  *     This program is distributed in the hope that it will be useful,
  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *     GNU General Public License for more details.
  *
  *     You should have received a copy of the GNU General Public License
  *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.socialsite.persistence;
 
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Set;
 
 public abstract class Message implements AbstractDomain
 {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	private long id;
 
 	private Date time;
 
 	private Set<User> users = new HashSet<User>();
 
 	public Message()
 	{
 		// set the time
 		setTime(new Date());
 	}
 
 	/*
 	 * add the user to the receiver list
 	 */
 	public void addUser(final User user)
 	{
 		getUsers().add(user);
 	}
 
 	public long getId()
 	{
 		return id;
 	}
 
 	public Date getTime()
 	{
 		return time;
 	}
 
 	public Set<User> getUsers()
 	{
 		return users;
 	}
 
 	public void setId(final long id)
 	{
 		this.id = id;
 	}
 
 	public void setTime(final Date time)
 	{
 		this.time = time;
 	}
 
 	public void setUsers(final Set<User> users)
 	{
 		this.users = users;
 	}
 }
