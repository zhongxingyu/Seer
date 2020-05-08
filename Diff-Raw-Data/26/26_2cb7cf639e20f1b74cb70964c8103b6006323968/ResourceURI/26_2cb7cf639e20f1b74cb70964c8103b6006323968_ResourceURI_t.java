 /**
 Copyright (c) 2012 Delcyon, Inc.
 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.
 
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
  */
 package com.delcyon.capo.resourcemanager;
 
 import java.util.Arrays;
 import java.util.HashMap;
 
 import com.delcyon.capo.util.ReflectionUtility;
 
 /**
  * @author jeremiah
  * <a href="http://en.wikipedia.org/wiki/URI_scheme">Based on</a>
  * 
  */
 public class ResourceURI
 {
 	
 	private String resourceURIString = null;
 	private String scheme = null;
 	private String schemeSpecificPart = null;
 	private boolean opaque = false;
 	private String authority = null;
 	private String hierarchy = null;
 	private String userInfo = null;
 	private String hostname = null;
 	private Integer port = null;
 	private String baseURI = null;
 	private String query = null;
 	private String fragment = null;
 	private String path = null;
 	private HashMap<String, String> parameterMap = new HashMap<String, String>();
 	private ResourceURI childResourceURI = null;
 	
 
 	/** needed for serialization **/
 	private ResourceURI(){};
 	
 	public ResourceURI(String resourceURI)
 	{
 		this.resourceURIString = resourceURI;
 		this.scheme = getScheme(resourceURI);
 		this.schemeSpecificPart = getSchemeSpecificPart(resourceURI);
 		this.opaque = isOpaque(resourceURI);
 		this.authority  = getAuthroity(resourceURI);
 		this.hierarchy  = getHierarchy(resourceURI);
 		this.userInfo = getUserInfo(resourceURI);
 		this.hostname = getHostname(resourceURI);
 		this.port = getPort(resourceURI);
 		this.baseURI = getBaseURI(resourceURI);		
 		this.query = getQuery(resourceURI);
 		this.path = getPath(resourceURI);
 		this.fragment = getFragment(resourceURI);
 		
		if(getChildURI(resourceURI) != null)
 		{
 			
			childResourceURI = new ResourceURI(getChildURI(resourceURI));
 		}
 		
 		if (this.query != null)
 		{
 		
 			String[] parameterSplit = query.split("&(?<!\\\\&)|;(?<!\\\\;)");// now split off the parameters from parameter section
 			for (String parameter : parameterSplit)
 			{				
 				String[] avp = parameter.replaceAll("\\\\((?=&)|(?=;))", "").split("=(?<!\\\\=)");// now split off the parameters from parameter section
 				String parameterName = avp[0].replaceAll("\\\\(?==)", "");
 				String parameterValue = "";
 				if(avp.length > 1)
 				{
 					parameterValue = avp[1].replaceAll("\\\\(?==)", "");
 				}
 				parameterMap.put(parameterName, parameterValue);
 			
 			}
 		}
 	}
 	
 	
 	public String getResourceURIString()
 	{
 		return resourceURIString;
 	}
 
 	public String getScheme()
 	{
 		return scheme;
 	}
 
 	public String getSchemeSpecificPart()
 	{
 		return schemeSpecificPart;
 	}
 
 	public boolean isOpaque()
 	{
 		return opaque;
 	}
 
 	public String getAuthority()
 	{
 		return authority;
 	}
 
 	public String getHierarchy()
 	{
 		return hierarchy;
 	}
 
 	public String getUserInfo()
 	{
 		return userInfo;
 	}
 
 	public String getHostname()
 	{
 		return hostname;
 	}
 
 	public Integer getPort()
 	{
 		return port;
 	}
 
 	public String getBaseURI()
 	{
 		return baseURI;
 	}
 
 	public String getQuery()
 	{
 		return query;
 	}
 
 	public String getFragment()
 	{
 		return this.fragment;
 	}
 
 	public String getPath()
 	{
 		return path;
 	}
 
 	public HashMap<String, String> getParameterMap()
 	{
 		return parameterMap;
 	}
 
 	public ResourceURI getChildResourceURI()
 	{
 		return childResourceURI;
 	}
 
 	@Override
 	public boolean equals(Object obj)
 	{		
 		if (obj instanceof ResourceURI)
 		{
 			return resourceURIString.equals(((ResourceURI) obj).getResourceURIString());
 		}
 		else
 		{
 			return false;
 		}
 	}
 	
 	@Override
 	public String toString()
 	{
		return resourceURIString;
 	}
 	
 //================================start static methods==============================================
 
 
 	public static String getScheme(String resourceURI)
 	{
 		String scheme = null;
 		
 		int schemeDeliminatorIndex = resourceURI.indexOf(":");
 		if(schemeDeliminatorIndex > 0)
 		{
 			scheme = resourceURI.substring(0, schemeDeliminatorIndex);
 			if(scheme.toLowerCase().matches("[a-z0-9+\\-\\.]+") == false)
 			{
 				scheme = null;
 			}
 		}
 		return scheme;
 	}
 	
 	public static String getSchemeSpecificPart(String resourceURI)
 	{
 		String scheme = null;
 		String uriRemainder = null;
 		int schemeDeliminatorIndex = resourceURI.indexOf(":");
 		if(schemeDeliminatorIndex > 0)
 		{			
 			scheme = resourceURI.substring(0, schemeDeliminatorIndex);
 			//verify that what we have is actually a scheme
 			if(scheme.toLowerCase().matches("[a-z0-9+\\-\\.]+") == false)
 			{
 				scheme = null;
 			}
 			else
 			{
 				uriRemainder = resourceURI.substring(schemeDeliminatorIndex+1);
 			}
 		}
 		if (scheme == null)
 		{
 			return resourceURI;
 		}
 		else
 		{
 			return uriRemainder;
 		}
 	}
 	
 	public static String removeURN(String uriString)
 	{
 		while(getScheme(uriString) != null)
 		{
 			uriString = getSchemeSpecificPart(uriString); 
 		}
 		return uriString;
 	}
 	
 	/**
 	 * This is straight out of the Java File URL parsing code. 
 	 * It may prove problematic as I'm not sure if this really conforms to the <a href="http://www.w3.org/DesignIssues/Axioms.html#opaque">Opacity Axiom</a>.
 	 * Currently if the scheme specific part of the uri does NOT start with a '/' this will return true.
 	 * @param resourceURI
 	 * @return
 	 */
 	public static boolean isOpaque(String resourceURI)
 	{
 		return getSchemeSpecificPart(resourceURI).startsWith("/") == false;
 	}
 
 	public static String getAuthroity(String resourceURI)
 	{
 
 		String hierarchy = getHierarchy(resourceURI);
 		if (hierarchy != null && hierarchy.matches(".+[/:]{0,1}.*"))
 		{
 			String authority = hierarchy.split("/")[0]; 
 			return authority;
 		}
 		else
 		{
 			return null;
 		}
 	}
 
 	public static String getUserInfo(String resourceURI)
 	{
 		String userInfo = null;
 		String authority = getAuthroity(resourceURI);
 		if(authority != null && authority.matches(".+@.*"))
 		{
 			userInfo = authority.replaceFirst("(.+)@.*", "$1");
 		}
 		return userInfo;
 	}
 	
 	public static String getHostname(String resourceURI)
 	{
 		String hostname = null;
 		String authority = getAuthroity(resourceURI);
 		if(authority != null)
 		{
 			hostname = authority.replaceFirst(".+@", "").replaceFirst(":\\d+", "");
 		}
 		return hostname;
 	}
 	
 	public static Integer getPort(String resourceURI)
 	{
 		Integer port = null;
 		String authority = getAuthroity(resourceURI);
 		if(authority != null && authority.matches(".+:\\d+"))
 		{
 			port = Integer.parseInt(authority.replaceAll(".+:(\\d+)", "$1"));
 		}
 		return port;
 	}
 	
 	/**
 	 * This returns the part of the URI with any sub/content URI's removed.
 	 * Content URI's are deliminated by a '!'. For example 'file:some.jar!something.class'
 	 * This would return file:some.jar  
 	 * @param resourceURI
 	 */
 	public static String getBaseURI(String resourceURI)
 	{
 		//split on the '!' char
 		String baseURI = resourceURI.split("!(?<!\\\\!)")[0];
 		//remove any escape chars that were used in the URL
 		return baseURI.replaceAll("\\\\(?=!)", "");
 	}
 	
	public static String getChildURI(String resourceURI)
    {
	    
        //split on the '!' char
	    String[] splitURI = resourceURI.split("!(?<!\\\\!)");
	    if(splitURI.length == 1)
	    {
	        return null;
	    }
	    String childURI = "";
        for(int index = 1; index < splitURI.length;index++)
        {
            childURI+= splitURI[index]+"!";
        }
        return childURI.substring(0,childURI.length()-1);
    }
	
 	public static boolean hasHierarchy(String resourceURI)
 	{
 		String hierarchy = getBaseURI(resourceURI).split("\\?(?<!\\\\\\?)")[0];
 		if (hierarchy.matches(".+://.+/.*"))
 		{
 			return true;
 		}
 		else
 		{
 			return false;
 		}
 		
 	}
 	public static String getHierarchy(String resourceURI)
 	{
 		if(hasHierarchy(resourceURI) == false)
 		{
 			return null;
 		}
 		
 		String hierarchy = getBaseURI(resourceURI).split("\\?(?<!\\\\\\?)")[0];// now split off the parameter section of the first declaration of the URI
 		return getSchemeSpecificPart(hierarchy).replaceFirst("//(.*)", "$1");
 	}
 	
 
 	public static String getQuery(String resourceURI)
 	{
 		String query = null;
 		String[] querySplit = getBaseURI(resourceURI).split("\\?(?<!\\\\\\?)");
 		if(querySplit.length > 1)
 		{
 			query = querySplit[1].replaceAll("\\\\(?=\\?)", "");
 			if (query.matches(".+#.*"))
 			{
 				query = query.split("#(?<!\\\\#)")[0].replaceAll("\\\\(?=#)", "");
 			}
 		}
 		return query;
 	}
 	
 	public static String getFragment(String resourceURI)
 	{
 		String fragment = null;
 		String[] querySplit = getBaseURI(resourceURI).split("\\?(?<!\\\\\\?)");
 		if(querySplit.length > 1)
 		{
 			fragment = querySplit[1].replaceAll("\\\\(?=\\?)", "");
 			if (fragment.matches(".+#.+"))
 			{
 				fragment = fragment.split("#(?<!\\\\#)")[1].replaceAll("\\\\(?=#)", "");
 			}
 			else
 			{
 				fragment = null;
 			}
 		}
 		return fragment;
 	}
 	
 	public static String getPath(String resourceURI)
 	{
 		String path = null;
 		String baseURI = getBaseURI(resourceURI);
 		if(hasHierarchy(baseURI))
 		{
 			String authority = getAuthroity(baseURI);
 			path = getHierarchy(baseURI);
 			if (authority != null)
 			{
 				path = path.substring(authority.length());
 			}
 		}
 		else
 		{
 			
 			path = getSchemeSpecificPart(getBaseURI(resourceURI).split("\\?(?<!\\\\\\?)")[0]);
 			if (path.matches(".*/.*")) //see if this path is a urn path or a conventional path
 			{
 				path = path.replaceAll(".+:(?<!\\\\:)(.+)", "$1").replaceAll("\\\\(?=:)", "");
 			}			
 		}
 		
 		
 		return path;
 	}
 	
 	public static String[] getParts(String resourceURI)
     {
         String[] parts = resourceURI.split("&(?<!\\\\&)|;(?<!\\\\;)|\\?(?<!\\\\\\?)|#(?<!\\\\#)|!(?<!\\\\!)|:(?<!\\\\:)|/(?<!\\\\/)");        
         return parts;
     }
 	
 	
 	
 }
