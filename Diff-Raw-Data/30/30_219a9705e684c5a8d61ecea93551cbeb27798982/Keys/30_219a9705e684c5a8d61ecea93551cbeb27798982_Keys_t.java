 package com.aquasheep.GTFTSTG.Items;
 
 import static com.badlogic.gdx.scenes.scene2d.actions.Actions.delay;
 import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;
 
 import com.aquasheep.GTFTSTG.GTFTSTG;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.audio.Sound;
 import com.badlogic.gdx.scenes.scene2d.Action;
 
 public class Keys extends Item {
 
 	private static float soundLength = 10.5f;
 	protected Sound sound2;
 	
 	public Keys(GTFTSTG game, float leftX, float botY) {
 		super(game,"keys",leftX,botY,soundLength);
 		sound2 = Gdx.audio.newSound(Gdx.files.internal("sounds/carAlarm.ogg"));
 	}
 	
 	@Override
 	public void activate() {
 		event = sequence(new Action() {
 			@Override
 			public boolean act(float delta) {
 				game.getBaby().cry.stop();
 				game.getBaby().sleep.stop();
 				if (Math.random() < 0.5) {
 					sound.play();
 					soundLength = 10.5f;
 				}
 				else {
 					sound2.play();
					soundLength = 1f;
 				}
 				return true;
 			}
 		}, delay(soundLength, new Action() {
 
 			@Override
 			public boolean act(float delta) {
 				if (game.DEBUG) {
 					System.out.println(name+" has been activated.");
 					System.out.println("Baby sleeping: "+game.getBaby().attemptSleep((int)(sleepChance*1000)));
 				} else
 					game.getBaby().attemptSleep((int)(sleepChance*1000));
 				return true;
 			}
 			
 		}));
 		this.addAction(event);	
 	}
 }
