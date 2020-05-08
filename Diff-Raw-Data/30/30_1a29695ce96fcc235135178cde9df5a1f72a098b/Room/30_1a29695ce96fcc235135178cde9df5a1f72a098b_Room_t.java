 package org.ggj2013;
 
 import java.util.LinkedList;
 
 import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
 import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
 import org.ggj2013.Enemy.Size;
 import org.ggj2013.FullscreenActivity.Movement;
 
 import android.graphics.Canvas;
 import android.util.Log;
 import android.widget.Toast;
 
 public class Room {
 
 	enum Status {
 		ACTIVE, WON, LOST
 	}
 
 	public LinkedList<Enemy> enemies;
 	public Player player;
 	public Damsel damsel;
 	public Status status = Status.ACTIVE;
 	private final Game context;
 
 	public Room(Game context) {
 		this.context = context;
 
 		player = new Player("player");
 		context.soundManager.play(player.name, SoundPackStandard.BEAT,
 				SoundManager.BALANCE_CENTER, 1f, -1);
 
 		damsel = new Damsel("damsel");
 		damsel.position = player.position.add(new Vector3D(0, 10, 0));
 		enemies = new LinkedList<Enemy>();
 
 		Enemy enemy = new Enemy("enemy-1", Size.MEDIUM);
 		enemy.position = new Vector3D(5, 5, 0);
 		enemies.add(enemy);
 
 		enemy = new Enemy("enemy-2", Size.BIG);
 		enemy.position = new Vector3D(-3, 8, 0);
 		enemies.add(enemy);
 
 		enemy = new Enemy("enemy-3", Size.SMALL);
 		enemy.position = new Vector3D(0, -3, 0);
 		enemies.add(enemy);
 	}
 
 	public boolean startedSound = false;
 
 	public void tryStartSound() {
 		if (!startedSound) {
 			startedSound = true;
 
 			context.soundManager.play(damsel.name, SoundPackStandard.GOAL,
 					SoundManager.BALANCE_CENTER, 1f,
 					SoundManager.LOOPS_INFINITE);
 
 			for (Enemy e : enemies) {
 				context.soundManager.play(e.name, e.getSoundName(),
 						SoundManager.BALANCE_CENTER, 1f,
 						SoundManager.LOOPS_INFINITE);
 			}
 		}
 	}
 
 	protected float lowPass(float newValue, float oldValue, float delta) {
 		delta = MathUtils.clamp(delta, 0, 1);
 
 		Vector3D newV = new Vector3D(Math.toRadians(newValue), 0);
 		Vector3D oldV = new Vector3D(Math.toRadians(oldValue), 0);
 		Rotation r = new Rotation(oldV, newV);
 		float diff = (float) Math.toDegrees(r.getAngle());
 
 		if (r.getAxis().getZ() < 0) {
 			diff = -diff;
 		}
 
 		oldValue = MathUtils.wrap(oldValue + delta * diff, -180f, 180f);
 		return oldValue;
 	}
 
 	public void onUpdate(float timeDiff) {
 		if (this.status != Status.ACTIVE) {
 			return;
 		}
 
 		tryStartSound();
 
 		if (player.collidesWith(damsel)) {
 			this.status = Status.WON;
 			onWonGame();
			return;
 		} else if (player.hitsWall(new Vector3D(-10, 10, 0), // topLeft
 				new Vector3D(10, 10, 0), // topRight
 				new Vector3D(-10, -10, 0), // bottomLeft
 				new Vector3D(10, -10, 0) // bottomRight
 				)) {
 			Log.d("Collision", "Hits wall");
 			player.moveForward(-0.1f);
 			this.context.activity.vibrate();
 			return;
 		} else {
 			for (Entity e : enemies) {
 				if (player.collidesWith(e)) {
 					this.status = Status.LOST;
 					onLostGame(e);
					return;
 				}
 			}
 		}
 
 		player.orientation = lowPass(context.activity.lastOrientation,
 				player.orientation, timeDiff * 10f);
 
 		if (context.activity.lastActivity == Movement.MOVING) {
 			player.moveForward(timeDiff);
 		}
 
 		if (startedSound) {
 			float[] balance = player.getBalanceForSoundFrom(damsel);
 			context.soundManager.changeVolume(damsel.name, balance, 1);
 
 			for (Enemy enemy : enemies) {
 				balance = player.getBalanceForSoundFrom(enemy);
 				context.soundManager.changeVolume(enemy.name, balance, 1);
 			}
 		}
 	}
 
 	private void onWonGame() {
		context.soundManager.stopAll();
		context.onNextLevel();

 		context.activity.runOnUiThread(new Runnable() {
 			@Override
 			public void run() {
 				Toast.makeText(context.activity, "WON!", Toast.LENGTH_LONG)
 						.show();
 			}
 		});
 	}
 
 	private void onLostGame(Entity e) {
		context.soundManager.stopAll();
		context.restart();

 		context.activity.runOnUiThread(new Runnable() {
 			@Override
 			public void run() {
 				Toast.makeText(context.activity, "LOST!", Toast.LENGTH_LONG)
 						.show();
 			}
 		});
 	}
 
 	public void onRender(Canvas c) {
 		// Whoa! Fancy graphics... not! :-P
 	}
 }
