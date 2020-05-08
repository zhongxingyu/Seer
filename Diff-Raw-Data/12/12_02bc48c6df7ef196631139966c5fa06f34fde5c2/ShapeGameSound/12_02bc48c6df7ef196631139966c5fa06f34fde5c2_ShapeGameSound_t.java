 package it.chalmers.tendu.gamemodel.shapesgame;
 
 import com.badlogic.gdx.Gdx;
 
 import com.badlogic.gdx.audio.Sound;
 
 import it.chalmers.tendu.tbd.C;
 import it.chalmers.tendu.tbd.EventBus;
 import it.chalmers.tendu.tbd.EventMessage;
 import it.chalmers.tendu.tbd.Listener;
 
 /**
  * Class for sounds in ShapesGame
  * @author johannahartman
  * 
  */
 public class ShapeGameSound implements Listener {
 
 	private Sound completedGameSound;
 	private Sound lostGameSound;
 	private Sound succeededSound;
 	private Sound failSound;
 
 	public ShapeGameSound() {
 
 		EventBus.INSTANCE.addListener(this);
 
 		completedGameSound = Gdx.audio.newSound(Gdx.files
 				.internal("completed.wav"));
 		lostGameSound = Gdx.audio.newSound(Gdx.files.internal("gamelost.wav"));
 		succeededSound = Gdx.audio.newSound(Gdx.files.internal("success.wav"));
 		failSound = Gdx.audio.newSound(Gdx.files.internal("fail.wav"));
 	}
 
 	@Override
 	public void onBroadcast(EventMessage message) {
 		
 		if (message.tag == C.Tag.TO_SELF) {
 			if (message.msg == C.Msg.SOUND_WIN) {
 				playSoundGameWon();
 			}else if(message.msg == C.Msg.SOUND_LOST){
 				playSoundGameLost();
 			}else if(message.msg == C.Msg.SOUND_SUCCEED){
 				playSoundSuccess();
 			}else if(message.msg == C.Msg.SOUND_FAIL){
 				playSoundFail();
 			}
 		}
 
 	}
	
 	public void playSoundSuccess() {
 		succeededSound.play();
 	}
 
 	public void playSoundFail() {
 		failSound.play();
 	}
 
 	public void playSoundGameWon() {
 		completedGameSound.play();
 	}
 
 	public void playSoundGameLost() {
 		lostGameSound.play();
 	}
 	
 	/**
 	 * Releases all the sound resources.
 	 */
 	@Override
 	public void unregister() {
 		EventBus.INSTANCE.removeListener(this);
 		completedGameSound.dispose();
 		lostGameSound.dispose();
 		succeededSound.dispose();
 		failSound.dispose();
 	}
 
 }
