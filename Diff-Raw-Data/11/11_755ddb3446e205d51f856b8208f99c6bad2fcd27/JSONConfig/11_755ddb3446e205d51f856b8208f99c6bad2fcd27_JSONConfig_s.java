 /**
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 3
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
  *
  */
 
 package truelauncher.config;
 
 import java.util.HashSet;
 import java.util.LinkedHashMap;
 
 public class JSONConfig {
 
 	
 	private int configversion = 0;
	protected int getConfigVersion()
	{
		return configversion;
	}
	
 	private String tempfolder = "temp";
 	private String libsfolder = "libraries";
 	private LinkedHashMap<String, ClientData> clientdata = new LinkedHashMap<String, ClientData>();
 	private HashSet<String> authaddresses = new HashSet<String>();
 	
 	protected String getTempFolder()
 	{
 		return tempfolder;
 	}
 	protected String getLibsFolder()
 	{
 		return libsfolder;
 	}
 	protected LinkedHashMap<String, ClientData> getClientDataMap()
 	{
 		LinkedHashMap<String, ClientData> r = new LinkedHashMap<String, ClientData>();
 		r.putAll(clientdata);
 		return r;
 	}
 	protected HashSet<String> getAllowedAdresses()
 	{
 		return new HashSet<String>(authaddresses);
 	}
 	
 }
