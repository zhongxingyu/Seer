 /**
  * Copyright 2012 Andrew Okin
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package forkk.multimc.data;
 
 public class Version
 {
	public static final Version currentVersion = new Version(0, 1, 0);
 	
 	public Version(int major, int minor, int revision)
 	{
 		this.major = major;
 		this.minor = minor;
 		this.revision = revision;
 	}
 	
 	public int major;
 	
 	public int minor;
 	
 	public int revision;
 	
 	@Override
 	public String toString()
 	{
 		return String.format("%1$s.%2$s.%3$s", major, minor, revision);
 	}
 	
 	public int compareTo(Version other)
 	{
 		if (major != other.major)
 			return (int) Math.copySign(1, other.major - major);
 		else if (minor != other.minor)
 			return (int) Math.copySign(1, other.minor - minor);
 		else if (revision != other.revision)
 			return (int) Math.copySign(1, other.revision - revision);
 		else
 			return 0;
 	}
 	
 	public static Version parseString(String str)
 	{
 		try
 		{
 			int major = Integer.parseInt(str.substring(0, str.indexOf('.')));
 			str = str.substring(str.indexOf('.') + 1);
 			int minor = Integer.parseInt(str.substring(0, str.indexOf('.')));
 			str = str.substring(str.indexOf('.') + 1);
 			int revision = Integer.parseInt(str);
 			return new Version(major, minor, revision);
 		} catch (Exception e)
 		{
 			e.printStackTrace();
 			return null;
 		}
 	}
 }
