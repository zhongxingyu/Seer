 /**
  * This is free and unencumbered software released into the public domain.
  * 
  * Anyone is free to copy, modify, publish, use, compile, sell, or distribute
  * this software, either in source code form or as a compiled binary, for any
  * purpose, commercial or non-commercial, and by any means.
  * 
  * In jurisdictions that recognize copyright laws, the author or authors of this
  * software dedicate any and all copyright interest in the software to the
  * public domain. We make this dedication for the benefit of the public at large
  * and to the detriment of our heirs and successors. We intend this dedication
  * to be an overt act of relinquishment in perpetuity of all present and future
  * rights to this software under copyright law.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
  * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
  * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  * 
  * For more information, please refer to [http://unlicense.org]
  */
 package ch.bfh.bti7301.w2013.battleship.game.players;
 
 import java.io.IOException;
 
 import ch.bfh.bti7301.w2013.battleship.game.Missile;
 import ch.bfh.bti7301.w2013.battleship.game.Player;
 import ch.bfh.bti7301.w2013.battleship.network.Connection;
 
 /**
  * @author simon
  * 
  */
 public class NetworkPlayer extends GenericPlayer implements Player {
 
 	private enum NetworkPlayerState {
 		IDLE, SHOT_PLACED, STATE_REQUESTED
 	}
 
 	private Connection networkConnection;
 	
 	private NetworkPlayerState netPlayerState = NetworkPlayerState.IDLE;
 
 	public NetworkPlayer() {
 		super();
		try {
			this.networkConnection = Connection.getInstance();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
 	}
 
 	public NetworkPlayer(String name) {
 		super(name);
		try {
			this.networkConnection = Connection.getInstance();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
 	}
 
 	@Override
 	public void placeMissile(Missile m) {
 		networkConnection.placeShot(m);
 		this.netPlayerState = NetworkPlayerState.SHOT_PLACED;
 	}
 
 	@Override
 	public void getPlayerState() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void sendState(PlayerState status) {
 		// TODO Auto-generated method stub
 		
 	}
 
 
 }
