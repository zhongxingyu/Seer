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
 
 package truelauncher.client;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 
 import truelauncher.config.AllSettings;
 import truelauncher.utils.LauncherUtils;
 
 public class OutReader extends Thread {
 	
 	private Process p;
 	private String password;
 	public OutReader(Process p, String password)
 	{
 		this.password = password;
 		this.p = p;
 	}
 
 	public void run()
 	{
 		try {
 			InputStream is = p.getErrorStream();
 			BufferedReader reader = new BufferedReader (new InputStreamReader(is));
 			String line;
 			while ((line = reader.readLine ()) != null) 
 			{
 				if (line.contains("AuthConnector")) 
 				{
 					onLoginFinished(line);
 				}
 			}
 			reader.close();
 		} catch (Exception e) {
 			LauncherUtils.logError(e);
 		}
 	}
 	
 
 	private void onLoginFinished(String message)
 	{
 		//loginsystem string format: AuthConnector|authtype|protocolversion|host|port|nick|token|
 		String[] paramarray = message.split("[|]");
 		int authtype = Integer.valueOf(paramarray[1]);
 		String host = paramarray[3];
 		int port = Integer.valueOf(paramarray[4]);
 		int protocolversion = Integer.valueOf(paramarray[2]);
 		String nick = paramarray[5];
 		String token = paramarray[6];
		if (isAddressAllowed(paramarray[2]))
 		{
 			if (authtype == 1)
 			{//1.6.4 and earlier
 				Auth.sendAuth1(host, port, protocolversion, nick, token, password);
 			} else
 			if (authtype == 2)
 			{//1.7.2 and newer (this is not supported currently)
 				Auth.sendAuth2(host, port, protocolversion, nick, token, password);
 			}
 		}
 	}
 	
 	
 	
 	private static boolean isAddressAllowed(String address)
 	{
 		return AllSettings.getAllowedAuthAddresses().contains(address);
 	}
 
 
 }
