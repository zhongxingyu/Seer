 package android.game;
 
 import com.badlogic.gdx.Gdx;
 
 
 
 public class Camera {
 	Vector3D loc;
 	Vector3D velocity;
 	Player player;
 	Vector3D playerCenter;
 	
 	double velocityPreservation = 0.90;
 	double focalAttraction = 0.01;
 	
 	public Camera(Player player) {
 		this.player = player;
 		this.playerCenter = new Vector3D(player.x - Gdx.graphics.getWidth() / 2,
 										player.y - Gdx.graphics.getHeight() / 2,
 										0);
 		this.loc = new Vector3D((float) player.x, player.y, 0);
 	}
 	
	public void update() {
 		this.playerCenter = new Vector3D(player.x - Gdx.graphics.getWidth() / 2,
 				player.y - Gdx.graphics.getHeight() / 2,
 				0);
 		this.velocity = (this.velocity.scale(this.velocityPreservation)).add((playerCenter.subtract(this.loc).scale(this.focalAttraction)));
 		this.loc = this.loc.add(this.velocity);
 	}
 }
