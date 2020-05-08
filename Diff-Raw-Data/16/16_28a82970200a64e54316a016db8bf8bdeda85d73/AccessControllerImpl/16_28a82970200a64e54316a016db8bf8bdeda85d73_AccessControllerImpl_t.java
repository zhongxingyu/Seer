 /*
  * regain/ACL - Implement SearchAccessController/CrawlerAccessController for regain (User/Group via URL) 
  * Copyright (C) 2012  Come_IN Computerclubs (University of Siegen)
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  * Contact: Come_IN-Team <come_in-team@listserv.uni-siegen.de>
  */
 
 package de.uni_siegen.wineme.come_in.acl;
 
 import java.util.Properties;
 
 import org.apache.log4j.Logger;
 
 import net.sf.regain.RegainException;
 
 public class AccessControllerImpl {
	protected static Logger mLog = Logger.getLogger(AccessControllerImpl.class);
 	
 	protected String defaultGroups = "";
 	protected String groupSeperator = " ";
 
 	public void init(Properties config) throws RegainException {
 		String groupSeperatorParam = config.getProperty("groupSeperator");
 		if (groupSeperatorParam != null && groupSeperatorParam.length() > 0)
 			groupSeperator = groupSeperatorParam;
 		
 		String defaultGroupsParam = config.getProperty("defaultGroups");
 		if (defaultGroupsParam != null)
 			defaultGroups = defaultGroupsParam + groupSeperator;
 	}
 	
 	protected String[] groupSplit(String groups) {
 		if (groups == null || groups.length() == 0)
 			return new String[]{};
 
 		String[] arr = groups.split(groupSeperator);
 		
 		int nbNull = 0;
 		for (int i = 0; i < arr.length; i++)
 		{
 			if (arr[i] == null || arr[i].isEmpty() || arr[i].contains(" "))
 			{
 				arr[i] = null;
 				nbNull++;
 			}
 		}
 		arr = compact(arr, nbNull);
 
 		if (mLog.isDebugEnabled())
 			mLog.debug("groupSplit returns: " + array2string(arr));
 	
 		return arr;
 	}
 	
 	/**
 	 * Remove all elements with null value
 	 * @param arr
 	 * @return
 	 */
 	protected String[] compact(String[] arr, int nbNull)
 	{
 		if (nbNull == 0)
 			return arr;
 		String[] myArr = new String[arr.length - nbNull];
 		int i = 0;
 		for (String str : arr)
 		{
 			if (str != null && i < myArr.length)
 			{
 				myArr[i] = str;
 				i++;
 			}
 		}
 		return myArr;
 	}
 	
 	private String array2string(String[] arr)
 	{
 		StringBuilder str = new StringBuilder();
 		str.append(arr.length).append(": [");
 		for (String r : arr)
 			str.append(r).append(",");
 		str.append("]");
 		return str.toString();
 	}
 	
 
 }
