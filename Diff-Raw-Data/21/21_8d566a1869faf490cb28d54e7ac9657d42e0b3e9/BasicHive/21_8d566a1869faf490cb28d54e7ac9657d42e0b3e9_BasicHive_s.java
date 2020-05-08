 /*
  * $Id$
  * $Revision$
  * $Date$
  *
  * ====================================================================
  * Copyright (c) 2005, Topicus B.V.
  * All rights reserved.
  */
 package org.apache.wicket.security.hive;
 
 import java.security.CodeSource;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.wicket.security.hive.authentication.Subject;
 import org.apache.wicket.security.hive.authorization.Permission;
 import org.apache.wicket.security.hive.authorization.Principal;
 import org.apache.wicket.security.util.ManyToManyMap;
 
 
 /**
  * Basic implementation of a Hive.
  * @author marrink
  *
  */
 public class BasicHive implements Hive
 {
 	/**
 	 * Maps {@link Permission}s to {@link Principal}s
 	 */
 	private ManyToManyMap principals;
 	
 	public BasicHive()
 	{
 		principals=new ManyToManyMap(500); //guess lots of principals
 	}
 	/**
 	 * Adds a new Principal to the hive.
 	 * @param principal the principal
 	 * @param permissions a required collection of granted permissions for the principal
 	 */
 	public void addPrincipal(Principal principal, Collection permissions)
 	{
 		if(principal==null)
 			throw new IllegalArgumentException("A principal is required.");
 		if(permissions==null)
 			throw new IllegalArgumentException("At least one permission is required for principal "+principal);
 		Iterator it=permissions.iterator();
 		while(it.hasNext())
 			principals.add((Permission)it.next(),principal);
 	}
 	/**
 	 * Adds a new permission to a principal.
 	 * @param principal the principal
 	 * @param permission the permission granted
 	 * @see #addPrincipal(CodeSource, Principal, List)
 	 */
 	public void addPermission(Principal principal, Permission permission)
 	{
 		if(principal==null)
 			throw new IllegalArgumentException("A principal is required.");
 		if(permission==null)
 			throw new IllegalArgumentException("A permission is required.");
 		principals.add(permission, principal);
 	}
 	public boolean containsPrincipal(Principal principal)
 	{
 		return principals.contains(principal);
 	}
 	/**
 	 * Checks if the subject has the permission.
 	 * @param subject the authenticated subject (if any)
 	 * @param permission
 	 * @return true if the subject has the permission, false otherwise.
 	 */
 	public boolean hasPermision(Subject subject,Permission permission)
 	{
 		//TODO caching
 		if(hasPrincipal(subject, principals.get(permission)))
 			return true;
 		//permissie zelf bestaat niet, doe een implies check
 		Iterator it=principals.iterator();
 		Object temp=null;
 		Permission possibleMatch=null;
 		while(it.hasNext())
 		{
 			temp=it.next();
 			if(temp instanceof Permission)
 			{
 				possibleMatch=(Permission)temp;
 				if(!possibleMatch.implies(permission))
 					continue;
 				if(hasPrincipal(subject, principals.get(possibleMatch)))
 					return true;
 			}
 		}
 		return false;
 	}
 	/**
 	 * Checks if the subject has or implies any of the principals in the set.
 	 * @param subject
 	 * @param principalSet
 	 * @return true if the subject has or implies atleast one of the principals, false otherwise.
 	 */
 	private boolean hasPrincipal(Subject subject, Set principalSet)
 	{
 		if(!principalSet.isEmpty())
 		{
 			Iterator it=principalSet.iterator();
 			Principal temp;
 			Set subjectPrincipals;
 			if(subject==null)
				subjectPrincipals=Collections.emptySet();
 			else
 				subjectPrincipals=subject.getPrincipals();
 			while(it.hasNext())
 			{
 				temp=(Principal)it.next();
 				if(subjectPrincipals.contains(temp) || temp.implies(subject))
 					return true;
 			}
 		}
 		return false;
 	}
 	/**
 	 * 
 	 * @see org.apache.wicket.security.hive.Hive#containsPermission(org.apache.wicket.security.hive.authorization.Permission)
 	 */
 	public boolean containsPermission(Permission permission)
 	{
 		return principals.contains(permission);
 	}
 	
 }
