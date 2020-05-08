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
 import java.util.List;
 
 /**
  * The class <code>ACLEntry</code> implements an ACL entry.
  * 
 * @author bhuisgen
  */
 public class ACLEntry
 {
 	/**
 	 * Constructs a new ACL entry.
 	 * 
 	 * @param name
 	 *            the name.
 	 */
 	protected ACLEntry (String name)
 	{
 		if ((name == null) || (name.equals ("")))
 			throw new IllegalArgumentException ("invalid name");
 
 		this.name = name;
 		this.rules = new ArrayList<ACLRule> ();
 	}
 
 	/**
 	 * Adds a ALLOW rule to the ACL entry.
 	 * 
 	 * @param mask
 	 *            the mask.
 	 */
 	public ACLRule addAllowRule (String mask)
 	{
 		if ((mask == null) || (mask.equals ("")))
 			throw new IllegalArgumentException ("invalid mask");
 
 		ACLRule aclRule = new ACLRule (mask, ACLRule.TYPE.ALLOW);
 
 		rules.add (aclRule);
 
 		return (aclRule);
 	}
 
 	/**
 	 * Adds a DENY rule to the ACL entry.
 	 * 
 	 * @param mask
 	 *            the mask.
 	 */
 	public ACLRule addDenyRule (String mask)
 	{
 		if ((mask == null) || (mask.equals ("")))
 			throw new IllegalArgumentException ("invalid mask");
 
 		ACLRule aclRule = new ACLRule (mask, ACLRule.TYPE.DENY);
 
 		rules.add (aclRule);
 
 		return (aclRule);
 	}
 
 	/**
 	 * Returns an array of all ACL rules.
 	 * 
 	 * @return an array of <code>ACLRule></code>.
 	 */
 	public ACLRule[] rules ()
 	{
 		return (rules.toArray (new ACLRule[0]));
 	}
 
 	/**
 	 * Clears all rules.
 	 */
 	protected void clear ()
 	{
 		rules.clear ();
 	}
 
 	/**
 	 * Returns the name of the ACL entry.
 	 * 
 	 * @return the name of the entry.
 	 */
 	public final String getName ()
 	{
 		return (name);
 	}
 
 	private String name;
 	private List<ACLRule> rules;
 }
