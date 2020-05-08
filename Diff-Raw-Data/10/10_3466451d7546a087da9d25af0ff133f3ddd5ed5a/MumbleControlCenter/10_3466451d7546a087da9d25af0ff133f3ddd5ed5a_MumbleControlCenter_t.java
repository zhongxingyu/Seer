 package org.dss.mumbleIceConnector;
 
 import Murmur.InvalidSecretException;
 import Murmur.InvalidSessionException;
 import Murmur.ServerBootedException;
 import Murmur.ServerPrx;
 import Murmur.User;
 
 public class MumbleControlCenter {
 	public static void sendWelcomeMessageToUser(ServerPrx serv, User user) {
 		try {
 			serv.sendMessage(user.session, "Hello '" + user.name + "' !");
		} catch (InvalidSecretException e) {
			e.printStackTrace();
		} catch (InvalidSessionException e) {
			e.printStackTrace();
		} catch (ServerBootedException e) {			
 			e.printStackTrace();
 		}
 	}
}
