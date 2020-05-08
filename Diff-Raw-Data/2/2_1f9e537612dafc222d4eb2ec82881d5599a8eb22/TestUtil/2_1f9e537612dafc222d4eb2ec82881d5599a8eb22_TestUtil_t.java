 /**
  * Warlock, the open-source cross-platform game client
  *  
  * Copyright 2008, Warlock LLC, and individual contributors as indicated
  * by the @authors tag. 
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package cc.warlock.core.test;
 
 import java.io.IOException;
 import java.util.Hashtable;
 import java.util.Map;
 
 import org.junit.Assert;
 
 import cc.warlock.core.configuration.Profile;
 import cc.warlock.core.stormfront.client.internal.StormFrontClient;
 import cc.warlock.core.stormfront.network.ISGEConnectionListener;
 import cc.warlock.core.stormfront.network.SGEConnection;
 
 // the purpose of this class is to open connections / clients for unit tests and cache them so we don't
 // login to our test profiles more than once in a given unit test session
 public class TestUtil {
 
 
 	protected static Hashtable<Profile, StormFrontClient> clients = new Hashtable<Profile, StormFrontClient>();
 	protected static Hashtable<Profile, Map<String,String>> profileProperties = new Hashtable<Profile, Map<String,String>>();
 	
 	public static Map<String,String> autoLogin (Profile profile, ISGEConnectionListener listener)
 	{
 		if (!profileProperties.containsKey(profile))
 		{
 			profileProperties.put(profile, SGEConnection.autoLogin(profile, listener));
 		}
 		return profileProperties.get(profile);
 	}
 	
 	public static StormFrontClient autoConnectToClient (Profile profile)
 	{
 		if (!clients.containsKey(profile))
 		{
 			Map<String,String> loginProperties = autoLogin(profile, null);
 		
			StormFrontClient client = new StormFrontClient("TS");
 			
 			int port = Integer.parseInt(loginProperties.get(SGEConnection.PROPERTY_GAMEPORT));
 			
 			try {
 				client.connect(loginProperties.get(SGEConnection.PROPERTY_GAMEHOST), port, loginProperties.get(SGEConnection.PROPERTY_KEY));
 			} catch (IOException e) {
 				Assert.fail(e.getMessage());
 			}
 			
 			clients.put(profile, client);
 		}
 		
 		return clients.get(profile);
 	}
 }
