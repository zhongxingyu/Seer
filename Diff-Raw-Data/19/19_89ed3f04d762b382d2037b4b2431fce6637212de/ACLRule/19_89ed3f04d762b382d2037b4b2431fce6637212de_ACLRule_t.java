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
 
 /**
  * The class <code>ACLRule</code> implements a ACL rule.
  * 
 * @author bhuisgen
  */
 public class ACLRule
 {
 	/**
 	 * Constructs a new <code>ACLRule</code> object.
 	 * 
 	 * @param mask
 	 *            the mask.
 	 * 
 	 * @param type
 	 *            the type.
 	 */
 	public ACLRule (String mask, TYPE type)
 	{
 		if ((mask == null) || (mask.equals ("")))
 			throw new IllegalArgumentException ("invalid mask");
 
 		this.mask = mask;
 		this.type = type;
 	}
 
 	/**
 	 * Tests if the rule has ALLOW permissions.
 	 * 
 	 * @return <code>true</code> if the rule has ALLOW permissions;
 	 *         <code>false</code> otherwise.
 	 */
 	public boolean isAllowRule ()
 	{
 		return (type == TYPE.ALLOW);
 	}
 
 	/**
 	 * Tests if the rule has DENY permissions.
 	 * 
 	 * @return <code>true</code> if the rule has DENY permissions;
 	 *         <code>false</code> otherwise.
 	 */
 	public boolean isDenyRule ()
 	{
 		return (type == TYPE.DENY);
 	}
 
 	/**
 	 * Returns the mask.
 	 * 
 	 * @return the mask.
 	 */
 	public final String getMask ()
 	{
 		return (mask);
 	}
 
 	private String mask;
 	private TYPE type;
 
 	protected enum TYPE
 	{
 		ALLOW, DENY
 	};
 }
