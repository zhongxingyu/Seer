 /*
  *  This program is free software; you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation; either version 2 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU Library General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program; if not, write to the Free Software
  *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
  */
 
 /*
  * Copyright 2005-2009 Boris HUISGEN <bhuisgen@hbis.fr>
  *
  * $Id$
  */
 
 package fr.hbis.ircs.lib.acl;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 /**
 * The class <code>ACL</code> implements an access Control list (ACL).
  * 
 * @author Boris HUISGEN
  */
 public class ACL
 {
 	/**
 	 * Constructs a new <code>ACL</code> object.
 	 */
 	public ACL ()
 	{
 		this.entries = new HashMap<String, ACLEntry> ();
 	}
 
 	/**
 	 * Adds an entry to the ACL.
 	 * 
 	 * @param name
 	 *            the name.
 	 * 
 	 * @return the <code>ACLEntry</code> object of the entry.
 	 */
 	public ACLEntry addEntry (String name)
 	{
 		if ((name == null) || (name.equals ("")))
 			throw new IllegalArgumentException ("invalid name");
 
 		String lowerName = name.toLowerCase ();
 
 		if (entries.containsKey (lowerName))
 			return ((ACLEntry) entries.get (lowerName));
 
 		ACLEntry entry = new ACLEntry (name);
 
 		entries.put (lowerName, entry);
 
 		return (entry);
 	}
 
 	/**
 	 * Removes an entry from the ACL.
 	 * 
 	 * @param name
 	 *            the name.
 	 * 
 	 * @return the <code>ACLEntry</code> object of the entry if and only if
 	 *         removed; <code>null</code> otherwise.
 	 */
 	public ACLEntry removeEntry (String name)
 	{
 		if ((name == null) || (name.equals ("")))
 			throw new IllegalArgumentException ("invalid name");
 
 		String lowerName = name.toLowerCase ();
 
 		if (entries.containsKey (lowerName))
 			return (null);
 
 		ACLEntry entry = (ACLEntry) entries.get (lowerName);
 
 		entries.remove (lowerName);
 
 		return (entry);
 	}
 
 	/**
 	 * Gets an ACL entry.
 	 * 
 	 * @param name
 	 *            the name.
 	 * 
 	 * @return the <code>ACLEntry</code> object of the entry if found;
 	 *         <code>null</code> otherwise.
 	 */
 	public ACLEntry getEntry (String name)
 	{
 		if ((name == null) || (name.equals ("")))
 			throw new IllegalArgumentException ("invalid name");
 
 		String lowerName = name.toLowerCase ();
 
 		return ((ACLEntry) entries.get (lowerName));
 	}
 
 	/**
 	 * Returns an array of all ACL entries.
 	 * 
 	 * @return an array of <code>ACLRule></code>.
 	 */
 	public ACLEntry[] entries ()
 	{
 		ArrayList<ACLEntry> list = new ArrayList<ACLEntry> ();
 
 		synchronized (entries)
 		{
 			Iterator<ACLEntry> iterator = entries.values ().iterator ();
 
 			while (iterator.hasNext ())
 			{
 				ACLEntry entry = iterator.next ();
 
 				list.add (entry);
 			}
 		}
 
 		return (list.toArray (new ACLEntry[0]));
 	}
 
 	/**
 	 * Checks if an entry is found.
 	 * 
 	 * @param name
 	 *            the name.
 	 * 
 	 * @return <code>true</code> if the entry is found; <code>false</code>
 	 *         otherwise.
 	 */
 	public boolean containsEntry (String name)
 	{
 		if ((name == null) || (name.equals ("")))
 			throw new IllegalArgumentException ("invalid name");
 
 		String lowerName = name.toLowerCase ();
 
 		return (entries.containsKey (lowerName));
 	}
 
 	/**
 	 * Clears all entries of the ACL.
 	 */
 	public void clear ()
 	{
 		synchronized (entries)
 		{
 			Iterator<ACLEntry> iterator = entries.values ().iterator ();
 
 			while (iterator.hasNext ())
 			{
 				ACLEntry entry = iterator.next ();
 
 				entry.clear ();
 			}
 		}
 
 		entries.clear ();
 	}
 
 	Map<String, ACLEntry> entries;
 }
