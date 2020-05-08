 /*******************************************************************************
  * This file is part of MPAF.
  * 
  * MPAF is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * MPAF is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  * 
  * You should have received a copy of the GNU Affero General Public License
  * along with MPAF.  If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package mpaf.ice;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import mpaf.Logger;
import mpaf.games.Battlefield3Handler;
 import mpaf.games.DefaultHandler;
 import Ice.Current;
 import Murmur.Channel;
 import Murmur.InvalidChannelException;
 import Murmur.InvalidSecretException;
 import Murmur.InvalidSessionException;
 import Murmur.ServerBootedException;
 import Murmur.User;
 
 public class ServerCallbackI extends Murmur._ServerCallbackDisp {
 	private static final long serialVersionUID = -666110379922768625L;
 
 	private Murmur.ServerPrx server;
 	@SuppressWarnings("unused")
 	private IceModel im;
 	private Map<String, DefaultHandler> handlers = new HashMap<String, DefaultHandler>();
 
 	public ServerCallbackI(Murmur.ServerPrx server,IceModel im) {
 		this.server = server;
 		this.im = im;
 	}
 
 	@Override
 	public void userConnected(User state, Current __current) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void userDisconnected(User state, Current __current) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void userStateChanged(User state, Current __current) {
 		try {
 			Logger.debug(this.getClass(), "User state has changed");
 			if (state.context.length() < 1)
 				return;
 			
 			// split context with null terminated character
 			String[] splitcontext = state.context.split("\0");
 			if (splitcontext.length < 1) {
 				for (DefaultHandler handler : handlers.values()) {
 					if (handler.isUserInGameChannel(state)) {
 						// return user back to the root game channel
 						Logger.debug(this.getClass(), "Returning user back to gameChannel");
 						state.channel = handler.updateGameTree().c.id;
 						server.setState(state);
 						return;
 					}
 				}
 				return;
 			}
 
 			// first part is the game name
 			String gamename = splitcontext[0];
 			Logger.debug(this.getClass(), gamename);
 			// Get GameHandler from HashMap
 			DefaultHandler handler = handlers.get(gamename);
 
 			// Check if GameHandler exists
 			if (handler == null) {
 				Logger.debug(this.getClass(),"Handler for gamename: "+gamename+" does not exist, please create one manually.");
 				return;
 			}
 			Logger.debug(this.getClass(), "There are " + handlers.size()
 					+ " handlers now.");
 			// execute GameHandler.handle(User state)
 			Logger.debug(this.getClass(), "started handling UserState");
 			handler.handleUserState(state);
 		} catch (InvalidSecretException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ServerBootedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InvalidChannelException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InvalidSessionException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void channelCreated(Channel state, Current __current) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void channelRemoved(Channel state, Current __current) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void channelStateChanged(Channel state, Current __current) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public Map<String, DefaultHandler> getHandlers() {
 		return handlers;
 	}
 
 	public void setHandlers(Map<String, DefaultHandler> handlers) {
 		this.handlers = handlers;
 	}
 
 }
