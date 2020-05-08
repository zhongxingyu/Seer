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
 
 import ch.bfh.bti7301.w2013.battleship.game.Board;
 import ch.bfh.bti7301.w2013.battleship.game.Missile;
 import ch.bfh.bti7301.w2013.battleship.game.Player;
 
 /**
  * @author simon
  * 
  */
 public class GenericPlayer implements Player {
 
 	private static String DEFAULT_NAME = "Unnamed player";
 
 	private String name;
 	protected PlayerState status;
 	protected Board playerBoard;
 
 	public enum PlayerState {
 		GAME_STARTED, READY, WAITING, PLAYING, GAME_WON, GAME_LOST
 	}
 
 	public GenericPlayer() {
 		this(DEFAULT_NAME);
 	}
 
 	public GenericPlayer(String name) {
 		this.name = name;
 		this.status = PlayerState.GAME_STARTED;
 		this.playerBoard = new Board(this);
 	}
 
 	public Board getBoard() {
 		return playerBoard;
 	}
 
 	public String getName() {
 		return name;
 	}
 	
 	public Missile placeMissile(Missile m) {
 		throw new RuntimeException("Not implemented");
 	}
 
 	@Override
 	public PlayerState getPlayerState() {
 		return status;
 	}
 	
 	@Override
	public void sendState(PlayerState status) {
 		throw new RuntimeException("Not implemented");
 	}
 
 
 }
