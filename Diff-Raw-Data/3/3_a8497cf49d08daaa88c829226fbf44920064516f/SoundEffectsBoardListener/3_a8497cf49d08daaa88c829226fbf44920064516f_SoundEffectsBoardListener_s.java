 package ch.bfh.bti7301.w2013.battleship.sounds;
 
 import java.util.Random;
 
 import ch.bfh.bti7301.w2013.battleship.game.BoardListener;
 import ch.bfh.bti7301.w2013.battleship.game.Missile;
 import ch.bfh.bti7301.w2013.battleship.gui.SettingsPanel;
 import ch.bfh.bti7301.w2013.battleship.gui.BoardView.BoardType;
 
 public class SoundEffectsBoardListener implements BoardListener {
 	private BoardType boardType;
 
 	public SoundEffectsBoardListener(BoardType boardType) {
 		this.boardType = boardType;
 	}
 
 	@Override
 	public void stateChanged(Missile m) {
 		switch (boardType) {
 		case LOCAL:
 			if (!SettingsPanel.getSettings().isLocalSound())
 				return;
 		case OPPONENT:
 			if (!SettingsPanel.getSettings().isOpponentSound())
 				return;
 		}
 
 		switch (m.getMissileState()) {
 		case HIT:
 			SoundEffects.playHit();
 			break;
 		case SUNK:
 		case GAME_WON:
 			SoundEffects.playSunk();
 			if (new Random().nextBoolean())
 				SoundEffects.playSOS();
 			else
 				SoundEffects.playWilhelmScream();
 		default:
 			break;
 		}
 	}
 }
